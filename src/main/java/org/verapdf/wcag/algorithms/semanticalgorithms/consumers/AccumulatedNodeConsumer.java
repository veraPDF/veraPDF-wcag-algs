package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticParagraph;
import org.verapdf.wcag.algorithms.entities.SemanticSpan;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.maps.AccumulatedNodeMapper;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ChunksMergeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccumulatedNodeConsumer implements Consumer<INode> {

	private static final Logger LOGGER = Logger.getLogger(AccumulatedNodeConsumer.class.getCanonicalName());

	private static final double MERGE_PROBABILITY_THRESHOLD = 0.75;

	private AccumulatedNodeMapper accumulatedNodeMapper;

	public AccumulatedNodeConsumer() {
		accumulatedNodeMapper = new AccumulatedNodeMapper();
	}

	@Override
	public void accept(INode node) {
		int numChildren = node.getChildren().size();

		if (numChildren <= 1) {
			acceptNodeWithLeq1Child(node);
			return;
		}

		// Pair <first, second> means that [first, second) chunks in range construct the longest possible line
		// or this chunk is a paragraph
		List<SemanticChunkBoundaries> semanticChunksBoundaries = new ArrayList<>();
		List<Double> mergeProbabilities = new ArrayList<>(); // Gives merge score of two adjacent nodes
		findSemanticChunksBoundaries(node, semanticChunksBoundaries, mergeProbabilities);

		if (semanticChunksBoundaries.get(0).equals(new SemanticChunkBoundaries(0, numChildren))) {
			// All children can be merged into one line
			double lineProbability = 1;
			for (int i = 0; i < numChildren - 1; ++i) {
				lineProbability *= mergeProbabilities.get(i);
			}
			updateNode(node, lineProbability, SemanticType.SPAN);
			return;
		}

		// Not a line -> build paragraph

		double paragraphProbability = 1;
		SemanticParagraph paragraph = null;
		for (SemanticChunkBoundaries semanticChunksBoundary : semanticChunksBoundaries) {
			INode currentText = buildSemanticChunkFromChildren(node, semanticChunksBoundary);

			if (paragraph == null) {
				paragraph = buildParagraphFromNode(currentText);
			} else {
				paragraphProbability *= toParagraphMergeProbability(paragraph, currentText);
			}
		}
		updateNode(node, paragraphProbability, SemanticType.PARAGRAPH);
	}

	private void acceptNodeWithLeq1Child(INode node) {
		List<INode> children = node.getChildren();
		if (children.isEmpty()) {
			updateNode(node, 1, node.getSemanticType());
		} else {
			INode child = children.get(0);
			updateNode(node, child.getCorrectSemanticScore(), child.getSemanticType());
		}
	}

	private void updateNode(INode node, double correctSemanticScore, SemanticType semanticType) {
		node.setCorrectSemanticScore(correctSemanticScore);
		node.setSemanticType(semanticType);
		node.setBoundingBox(accumulatedNodeMapper.get(node).getBoundingBox());
	}

	private INode buildSemanticChunkFromChildren(INode node, SemanticChunkBoundaries semanticChunkBoundaries) {
		int numChunks = semanticChunkBoundaries.getEnd() - semanticChunkBoundaries.getStart();
		return numChunks == 1 ? accumulatedNodeMapper.get(node.getChildren().get(semanticChunkBoundaries.getStart()))
		                      : buildLineFromChildren(node, semanticChunkBoundaries);
	}

	private void findSemanticChunksBoundaries(INode node, List<SemanticChunkBoundaries> semanticChunksBoundaries,
	                                          List<Double> mergeProbabilities) {
		List<INode> children = node.getChildren();
		int numChildren = node.getChildren().size();

		// Use two pointers method to build linesBoundaries
		int currentLineLeftBoundary = -1; // left pointer
		for (int i = 0; i < numChildren - 1; ++i) { // i is the right pointer
			mergeProbabilities.add(i, 0d);

			INode currentChild = accumulatedNodeMapper.get(children.get(i));
			INode nextChild = accumulatedNodeMapper.get(children.get(i + 1));

			if (!SemanticType.SPAN.equals(currentChild.getSemanticType())) {
				semanticChunksBoundaries.add(new SemanticChunkBoundaries(i, i + 1));
				continue;
			}

			if (currentLineLeftBoundary == -1) {
				currentLineLeftBoundary = i;
			}

			if (!SemanticType.SPAN.equals(nextChild.getSemanticType())) {
				semanticChunksBoundaries.add(new SemanticChunkBoundaries(currentLineLeftBoundary, i + 1));
				currentLineLeftBoundary = -1;
				continue;
			}

			// now: currentChild.semanticType == nextChild.semanticType == SEMANTIC_TYPE_SPAN

			double mergeProbability = ChunksMergeUtils.toLineMergeProbability(((SemanticSpan) currentChild).getTextChunks().get(0),
																			  ((SemanticSpan) nextChild).getTextChunks().get(0));
			mergeProbabilities.set(i, mergeProbability);

			if (mergeProbability < MERGE_PROBABILITY_THRESHOLD) {
				semanticChunksBoundaries.add(new SemanticChunkBoundaries(currentLineLeftBoundary, i + 1));
				currentLineLeftBoundary = -1;
			}
		}
		if (currentLineLeftBoundary == -1) {
			currentLineLeftBoundary = numChildren - 1;
		}
		semanticChunksBoundaries.add(new SemanticChunkBoundaries(currentLineLeftBoundary, numChildren));
	}

	private SemanticParagraph buildParagraphFromNode(INode node) {
		if (isNullableSemanticType(node)) {
			return null;
		}
		switch (node.getSemanticType()) {
			case SPAN:
				SemanticSpan span = (SemanticSpan) node;
				return new SemanticParagraph(span.getBoundingBox(), span.getTextChunks().get(0), span.getTextChunks().get(0));
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
				return toParagraphMergeProbability(paragraph, (SemanticSpan) node);
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

	private double toParagraphMergeProbability(SemanticParagraph paragraph, SemanticSpan span) {
		TextChunk chunk = span.getTextChunks().get(0);
		if (isOneLineParagraph(paragraph)) {
			paragraph.getBoundingBox().union(span.getBoundingBox());
			paragraph.setLastLine(chunk);
			paragraph.setLastPageNumber(span.getPageNumber());

			return ChunksMergeUtils.mergeLeadingProbability(paragraph.getFirstLine(), chunk);
		}

		return ChunksMergeUtils.toParagraphMergeProbability(paragraph.getLastLine(), chunk);
	}

	private double toParagraphMergeProbability(SemanticParagraph paragraph1, SemanticParagraph paragraph2) {
		if (isOneLineParagraph(paragraph1)) {
			paragraph1.getBoundingBox().union(paragraph2.getBoundingBox());
			paragraph1.setLastLine(paragraph2.getLastLine());
			paragraph1.setLastPageNumber(paragraph2.getLastPageNumber());

			return ChunksMergeUtils.mergeLeadingProbability(paragraph1.getFirstLine(), paragraph2.getFirstLine());
		}

		return ChunksMergeUtils.toParagraphMergeProbability(paragraph1.getLastLine(), paragraph2.getFirstLine());
	}

	private boolean isOneLineParagraph(SemanticParagraph paragraph) {
		return paragraph.getFirstLine() == paragraph.getLastLine();
	}

	private BoundingBox minMaxBoundingBox(INode node, SemanticChunkBoundaries semanticChunkBoundaries) {
		List<INode> children = node.getChildren();
		BoundingBox minMaxBoundingBox = new BoundingBox();

		for (int i = semanticChunkBoundaries.getStart(); i < semanticChunkBoundaries.getEnd(); ++i) {
			INode chunk = accumulatedNodeMapper.get(children.get(i));
			minMaxBoundingBox.union(chunk.getBoundingBox());
		}

		return minMaxBoundingBox;
	}

	private INode buildLineFromChildren(INode node, SemanticChunkBoundaries semanticChunkBoundaries) {
		List<INode> children = node.getChildren();
		double fontSize = 0;
		double baseLine = 0;
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = semanticChunkBoundaries.getStart(); i < semanticChunkBoundaries.getEnd(); ++i) {
			SemanticSpan span = (SemanticSpan) accumulatedNodeMapper.get(children.get(i));
			TextChunk chunk = span.getTextChunks().get(0);
			stringBuilder.append(chunk.getValue());
			if (chunk.getFontSize() > fontSize) {
				fontSize = chunk.getFontSize();
				baseLine = chunk.getBaseLine();
			}
		}

		TextChunk textChunk = new TextChunk(minMaxBoundingBox(node, semanticChunkBoundaries),
											stringBuilder.toString(), fontSize, baseLine);
		SemanticSpan semanticSpan = new SemanticSpan(textChunk);
		return semanticSpan;
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
