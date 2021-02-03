package org.verapdf.wcag.algorithms.semanticalgorithms.mapper;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticParagraph;
import org.verapdf.wcag.algorithms.entities.SemanticTextChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.AccumulatedNodeConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ChunksMergeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccumulatedNodeMapper {

	private static final Logger LOGGER = Logger.getLogger(AccumulatedNodeConsumer.class.getCanonicalName());

	private static final double MERGE_PROBABILITY_THRESHOLD = 0.75;

	private final Map<INode, INode> nodeToAccumulatedNodeMap;

	public AccumulatedNodeMapper() {
		this.nodeToAccumulatedNodeMap = new HashMap<>();
	}

	public void calculateSemanticScore(INode node) {

		if (nodeToAccumulatedNodeMap.containsKey(node)) {
			return;
		}

		List<INode> children = node.getChildren();
		int numChildren = children.size();

		if (children.isEmpty()) {
			addAccumulatedNode(node, node, 1, node.getSemanticType());
			return;
		} else if (children.size() == 1) {
			//recursively call this method to check whether children nodes where processed and have semantic score
			INode child = children.get(0);
			calculateSemanticScore(child);
			addAccumulatedNode(node, nodeToAccumulatedNodeMap.get(child), child.getCorrectSemanticScore(),
			                   child.getSemanticType());
			return;
		}

		//recursively call this method to check whether children nodes where processed and have semantic score
		for (INode child : children) {
			calculateSemanticScore(child);
		}

		// Pair <first, second> means that [first, second) chunks in range construct the longest possible line
		// or this chunk is a paragraph
		List<AccumulatedNodeMapper.SemanticChunkBoundaries> semanticChunksBoundaries = new ArrayList<>();
		List<Double> mergeProbabilities = new ArrayList<>(); // Gives merge score of two adjacent nodes
		findSemanticChunksBoundaries(children, semanticChunksBoundaries, mergeProbabilities);

		if (semanticChunksBoundaries.get(0).equals(new AccumulatedNodeMapper.SemanticChunkBoundaries(0, numChildren))) {
			// All children can be merged into one line
			double lineProbability = 1;
			for (int i = 0; i < numChildren - 1; ++i) {
				lineProbability *= mergeProbabilities.get(i);
			}
			addAccumulatedNode(node, buildLineFromChildren(node, new AccumulatedNodeMapper.SemanticChunkBoundaries(0, numChildren)),
			                   lineProbability, SemanticType.SPAN);
			return;
		}

		// Not a line -> build paragraph

		double paragraphProbability = 1;
		SemanticParagraph paragraph = null;
		for (AccumulatedNodeMapper.SemanticChunkBoundaries semanticChunksBoundary : semanticChunksBoundaries) {
			INode currentText = buildSemanticChunkFromChildren(node, semanticChunksBoundary);

			if (paragraph == null) {
				paragraph = buildParagraphFromNode(currentText);
			} else {
				paragraphProbability *= toParagraphMergeProbability(paragraph, currentText);
			}
		}
		addAccumulatedNode(node, buildParagraphFromChildren(node), paragraphProbability, SemanticType.PARAGRAPH);
	}

	private void addAccumulatedNode(INode node, INode accumulatedNode, double correctSemanticScore,
	                                SemanticType semanticType) {
		nodeToAccumulatedNodeMap.put(node, accumulatedNode);
		node.setCorrectSemanticScore(correctSemanticScore);
		node.setSemanticType(semanticType);
	}

	private INode buildSemanticChunkFromChildren(INode node,
	                                             AccumulatedNodeMapper.SemanticChunkBoundaries semanticChunkBoundaries) {
		int numChunks = semanticChunkBoundaries.getEnd() - semanticChunkBoundaries.getStart();
		return numChunks == 1 ? nodeToAccumulatedNodeMap.get(node.getChildren().get(semanticChunkBoundaries.getStart()))
		                      : buildLineFromChildren(node, semanticChunkBoundaries);
	}

	private void findSemanticChunksBoundaries(List<INode> children,
	                                          List<AccumulatedNodeMapper.SemanticChunkBoundaries> semanticChunksBoundaries,
	                                          List<Double> mergeProbabilities) {
		int numChildren = children.size();

		// Use two pointers method to build linesBoundaries
		int currentLineLeftBoundary = -1; // left pointer
		for (int i = 0; i < numChildren - 1; ++i) { // i is the right pointer
			mergeProbabilities.add(i, 0d);

			INode currentChild = nodeToAccumulatedNodeMap.get(children.get(i));
			INode nextChild = nodeToAccumulatedNodeMap.get(children.get(i + 1));

			if (!SemanticType.SPAN.equals(currentChild.getSemanticType())) {
				semanticChunksBoundaries.add(new AccumulatedNodeMapper.SemanticChunkBoundaries(i, i + 1));
				continue;
			}

			if (currentLineLeftBoundary == -1) {
				currentLineLeftBoundary = i;
			}

			if (!SemanticType.SPAN.equals(nextChild.getSemanticType())) {
				semanticChunksBoundaries.add(new AccumulatedNodeMapper.SemanticChunkBoundaries(currentLineLeftBoundary,
				                                                                               i + 1));
				currentLineLeftBoundary = -1;
				continue;
			}

			// now: currentChild.semanticType == nextChild.semanticType == SEMANTIC_TYPE_SPAN

			double mergeProbability = ChunksMergeUtils.toLineMergeProbability((SemanticTextChunk) currentChild,
			                                                                  (SemanticTextChunk) nextChild);
			mergeProbabilities.set(i, mergeProbability);

			if (mergeProbability < MERGE_PROBABILITY_THRESHOLD) {
				semanticChunksBoundaries.add(new AccumulatedNodeMapper.SemanticChunkBoundaries(currentLineLeftBoundary,
				                                                                               i + 1));
				currentLineLeftBoundary = -1;
			}
		}
		if (currentLineLeftBoundary == -1) {
			currentLineLeftBoundary = numChildren - 1;
		}
		semanticChunksBoundaries.add(new AccumulatedNodeMapper.SemanticChunkBoundaries(currentLineLeftBoundary,
		                                                                               numChildren));
	}

	private SemanticParagraph buildParagraphFromNode(INode node) {
		if (isNullableSemanticType(node)) {
			return null;
		}
		switch (node.getSemanticType()) {
			case SPAN:
				SemanticTextChunk textChunk = (SemanticTextChunk) node;
				return new SemanticParagraph(textChunk.getPageNumber(), textChunk.getBoundingBox(),
				                             textChunk.getPageNumber(), textChunk, textChunk);
			case PARAGRAPH:
				return (SemanticParagraph) node;
			default:
				return null;
		}
	}

	private double toParagraphMergeProbability(SemanticParagraph paragraph, INode node) {
		if (isNullableSemanticType(node)) {
			return 0d;
		}
		switch (node.getSemanticType()) {
			case SPAN:
				return toParagraphMergeProbability(paragraph, (SemanticTextChunk) node);
			case PARAGRAPH:
				return toParagraphMergeProbability(paragraph, (SemanticParagraph) node);
			default:
				return 0d;
		}
	}

	private boolean isNullableSemanticType(INode node) {
		if (node.getSemanticType() == null) {
			LOGGER.log(Level.WARNING, "Node with nullable semantic type: {}", node);
			return true;
		}
		return false;
	}

	private double toParagraphMergeProbability(SemanticParagraph paragraph, SemanticTextChunk textChunk) {
		if (isOneLineParagraph(paragraph)) {
			paragraph.setBoundingBox(minMaxBoundingBox(paragraph, textChunk));
			paragraph.setLastLine(textChunk);
			paragraph.setLastPageNumber(textChunk.getPageNumber());

			return ChunksMergeUtils.mergeLeadingProbability(paragraph.getFirstLine(), textChunk);
		}

		return ChunksMergeUtils.toParagraphMergeProbability(paragraph.getLastLine(), textChunk);
	}

	private double toParagraphMergeProbability(SemanticParagraph paragraph1, SemanticParagraph paragraph2) {
		if (isOneLineParagraph(paragraph1)) {
			paragraph1.setBoundingBox(minMaxBoundingBox(paragraph1, paragraph2));
			paragraph1.setLastLine(paragraph2.getLastLine());
			paragraph1.setLastPageNumber(paragraph2.getLastPageNumber());

			return ChunksMergeUtils.mergeLeadingProbability(paragraph1.getFirstLine(), paragraph2.getFirstLine());
		}

		return ChunksMergeUtils.toParagraphMergeProbability(paragraph1.getLastLine(), paragraph2.getFirstLine());
	}

	private boolean isOneLineParagraph(SemanticParagraph paragraph) {
		return paragraph.getFirstLine() == paragraph.getLastLine();
	}

	private double[] minMaxBoundingBox(INode node,
	                                   AccumulatedNodeMapper.SemanticChunkBoundaries semanticChunkBoundaries) {
		List<INode> children = node.getChildren();

		double max = Double.MAX_VALUE;
		double[] minMaxBoundingBox = {max, max, -max, -max};

		for (int i = semanticChunkBoundaries.getStart(); i < semanticChunkBoundaries.getEnd(); ++i) {
			INode chunk = nodeToAccumulatedNodeMap.get(children.get(i));

			minMaxBoundingBox[0] = Math.min(minMaxBoundingBox[0], chunk.getBoundingBox()[0]);
			minMaxBoundingBox[1] = Math.min(minMaxBoundingBox[1], chunk.getBoundingBox()[1]);
			minMaxBoundingBox[2] = Math.max(minMaxBoundingBox[2], chunk.getBoundingBox()[2]);
			minMaxBoundingBox[3] = Math.max(minMaxBoundingBox[3], chunk.getBoundingBox()[3]);
		}

		return minMaxBoundingBox;
	}

	private double[] minMaxBoundingBox(INode chunk1, INode chunk2) {
		return new double[]{Math.min(chunk1.getLeftX(), chunk2.getLeftX()),
		                    Math.min(chunk1.getBottomY(), chunk2.getBottomY()),
		                    Math.max(chunk1.getRightX(), chunk2.getRightX()),
		                    Math.max(chunk1.getTopY(), chunk2.getTopY())
		};
	}

	private INode buildLineFromChildren(INode node,
	                                    AccumulatedNodeMapper.SemanticChunkBoundaries semanticChunkBoundaries) {
		List<INode> children = node.getChildren();

		double fontSize = 0;
		double baseLine = 0;
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = semanticChunkBoundaries.getStart(); i < semanticChunkBoundaries.getEnd(); ++i) {
			SemanticTextChunk chunk = (SemanticTextChunk) nodeToAccumulatedNodeMap.get(children.get(i));
			stringBuilder.append(chunk.getText());
			if (chunk.getFontSize() > fontSize) {
				fontSize = chunk.getFontSize();
				baseLine = chunk.getBaseLine();
			}
		}

		SemanticTextChunk textChunk
				= (SemanticTextChunk) nodeToAccumulatedNodeMap.get(children.get(semanticChunkBoundaries.getStart()));

		return new SemanticTextChunk(textChunk.getPageNumber(), minMaxBoundingBox(node, semanticChunkBoundaries),
		                             stringBuilder.toString(), fontSize, baseLine);
	}

	private INode buildParagraphFromChildren(INode node) {
		INode firstChild = nodeToAccumulatedNodeMap.get(node.getChildren().get(0));
		INode lastChild = nodeToAccumulatedNodeMap.get(node.getChildren().get(node.getChildren().size() - 1));
		SemanticTextChunk firstLine = SemanticType.SPAN.equals(firstChild.getSemanticType())
		                              ? (SemanticTextChunk) firstChild : ((SemanticParagraph) firstChild).getFirstLine();
		SemanticTextChunk lastLine = SemanticType.SPAN.equals(lastChild.getSemanticType())
		                             ? (SemanticTextChunk) lastChild : ((SemanticParagraph) lastChild).getLastLine();

		return new SemanticParagraph((firstChild).getPageNumber(),
		                             minMaxBoundingBox(node, new AccumulatedNodeMapper.SemanticChunkBoundaries(0, node.getChildren().size())),
		                             (lastChild).getPageNumber(), firstLine, lastLine);
	}

	private static class SemanticChunkBoundaries {

		private final int start;
		private final int end;

		SemanticChunkBoundaries(int start, int end) {
			this.start = start;
			this.end = end;
		}

		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}
	}
}
