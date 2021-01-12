package org.verapdf.wcag.algorithms.implementation;

import org.verapdf.wcag.algorithms.entity.SemanticParagraph;
import org.verapdf.wcag.algorithms.entity.SemanticTextChunk;
import org.verapdf.wcag.algorithms.entity.enums.SemanticType;
import org.verapdf.wcag.algorithms.implementation.utils.ChunksMergeUtils;
import org.verapdf.wcag.algorithms.entity.IChunk;
import org.verapdf.wcag.algorithms.entity.INode;
import org.verapdf.wcag.algorithms.entity.ITextChunk;

import java.util.*;
import java.util.function.Consumer;

public class AccumulatedNodeBuilder implements Consumer<INode> {

	private static final double MERGE_PROBABILITY_THRESHOLD = 0.75;

	private final Map<INode, INode> nodeToAccumulatedNodeMap;

	public AccumulatedNodeBuilder() {
		nodeToAccumulatedNodeMap = new HashMap<>();
	}

	@Override
	public void accept(INode node) {
		int numChildren = node.numChildren();

		if (numChildren <= 1) {
			acceptNodeWithLeq1Child(node);
			return;
		}

		// Pair <first, second> means that [first, second) chunks in range construct the longest possible line
		// or this chunk is a paragraph
		List<Map.Entry<Integer, Integer>> semanticChunksBoundaries = new ArrayList<>();
		List<Double> mergeProbabilities = new ArrayList<>(); // Gives merge score of two adjacent nodes
		findSemanticChunksBoundaries(node, semanticChunksBoundaries, mergeProbabilities);

		if (semanticChunksBoundaries.get(0).equals(new AbstractMap.SimpleEntry<>(0, numChildren))) {
			// All children can be merged into one line
			double lineProbability = 1;
			for (int i = 0; i < numChildren - 1; ++i) {
				lineProbability *= mergeProbabilities.get(i);
			}
			addAccumulatedNode(node, buildLineFromChildren(node, new AbstractMap.SimpleEntry<>(0, numChildren)),
			                   lineProbability, SemanticType.SPAN);
			return;
		}

		// Not a line -> build paragraph

		double paragraphProbability = 1;
		SemanticParagraph paragraph = null;
		for (Map.Entry<Integer, Integer> semanticChunksBoundary : semanticChunksBoundaries) {
			INode currentText = buildSemanticChunkFromChildren(node, semanticChunksBoundary);

			if (paragraph == null) {
				paragraph = buildParagraphFromNode(currentText);
			} else {
				paragraphProbability *= toParagraphMergeProbability(paragraph, currentText);
			}
		}
		addAccumulatedNode(node, buildParagraphFromChildren(node), paragraphProbability, SemanticType.PARAGRAPH);
	}

	private void acceptNodeWithLeq1Child(INode node) {
		if (node.numChildren() < 1) {
			addAccumulatedNode(node, node, 1, node.getSemanticType());
		} else {
			INode child = node.getChildren().get(0);
			addAccumulatedNode(node, nodeToAccumulatedNodeMap.get(child), child.getCorrectSemanticScore(),
			                   child.getSemanticType());
		}
	}

	private void addAccumulatedNode(INode node, INode accumulatedNode, double correctSemanticScore,
	                                SemanticType semanticType) {
		nodeToAccumulatedNodeMap.put(node, accumulatedNode);
		node.setCorrectSemanticScore(correctSemanticScore);
		node.setSemanticType(semanticType);
	}

	private INode buildSemanticChunkFromChildren(INode node, Map.Entry<Integer, Integer> semanticChunkBoundaries) {
		int numChunks = semanticChunkBoundaries.getValue() - semanticChunkBoundaries.getKey();
		return numChunks == 1 ? nodeToAccumulatedNodeMap.get(node.getChildren().get(semanticChunkBoundaries.getKey()))
		                      : buildLineFromChildren(node, semanticChunkBoundaries);
	}

	private void findSemanticChunksBoundaries(INode node, List<Map.Entry<Integer, Integer>> semanticChunksBoundaries,
	                                          List<Double> mergeProbabilities) {
		List<INode> children = node.getChildren();
		int numChildren = node.numChildren();

		// Use two pointers method to build linesBoundaries
		int currentLineLeftBoundary = -1; // left pointer
		for (int i = 0; i < numChildren - 1; ++i) { // i is the right pointer
			mergeProbabilities.add(i, 0d);

			INode currentChild = nodeToAccumulatedNodeMap.get(children.get(i));
			INode nextChild = nodeToAccumulatedNodeMap.get(children.get(i + 1));

			if (!currentChild.getSemanticType().equals(SemanticType.SPAN)) {
				semanticChunksBoundaries.add(new AbstractMap.SimpleEntry<>(i, i + 1));
				continue;
			}

			if (currentLineLeftBoundary == -1) {
				currentLineLeftBoundary = i;
			}

			if (!nextChild.getSemanticType().equals(SemanticType.SPAN)) {
				semanticChunksBoundaries.add(new AbstractMap.SimpleEntry<>(currentLineLeftBoundary, i + 1));
				currentLineLeftBoundary = -1;
				continue;
			}

			// now: currentChild.semanticType == nextChild.semanticType == SEMANTIC_TYPE_SPAN

			double mergeProbability = ChunksMergeUtils.toLineMergeProbability((ITextChunk) currentChild,
			                                                                  (ITextChunk) nextChild);
			mergeProbabilities.set(i, mergeProbability);

			if (mergeProbability < MERGE_PROBABILITY_THRESHOLD) {
				semanticChunksBoundaries.add(new AbstractMap.SimpleEntry<>(currentLineLeftBoundary, i + 1));
				currentLineLeftBoundary = -1;
			}
		}
		if (currentLineLeftBoundary == -1) {
			currentLineLeftBoundary = numChildren - 1;
		}
		semanticChunksBoundaries.add(new AbstractMap.SimpleEntry<>(currentLineLeftBoundary, numChildren));
	}

	private SemanticParagraph buildParagraphFromNode(INode node) {
		switch (node.getSemanticType()) {
			case SPAN:
				ITextChunk textChunk = (ITextChunk) node;
				return new SemanticParagraph(textChunk.getBoundingBox(), textChunk.getPageNumber(),
				                             textChunk, textChunk, textChunk.getPageNumber());
			case PARAGRAPH:
				return (SemanticParagraph) node;
			default:
				return null;
		}
	}

	private double toParagraphMergeProbability(SemanticParagraph paragraph, INode node) {
		switch (node.getSemanticType()) {
			case SPAN:
				return toParagraphMergeProbability(paragraph, (ITextChunk) node);
			case PARAGRAPH:
				return toParagraphMergeProbability(paragraph, (SemanticParagraph) node);
			default:
				return 0d;
		}
	}

	private double toParagraphMergeProbability(SemanticParagraph paragraph, ITextChunk textChunk) {
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

	private int getIndentation(IChunk x, IChunk y) {
		double[] differences = new double[3];

		differences[0] = Math.abs(x.getLeftX() - y.getLeftX());
		differences[1] = Math.abs(x.getRightX() - y.getRightX());
		differences[2] = (differences[0] + differences[1]) / 2;

		int iMin = 0;
		double minDifference = differences[0];
		for (int i = 1; i < 3; ++i) {
			if (minDifference > differences[i]) {
				iMin = i;
				minDifference = differences[i];
			}
		}

		return iMin;
	}

	private double[] minMaxBoundingBox(INode node, Map.Entry<Integer, Integer> lineBoundary) {
		List<INode> children = node.getChildren();

		double max = Double.MAX_VALUE;
		double[] minMaxBoundingBox = {max, max, -max, -max};

		for (int i = lineBoundary.getKey(); i < lineBoundary.getValue(); ++i) {
			IChunk chunk = (IChunk) nodeToAccumulatedNodeMap.get(children.get(i));

			minMaxBoundingBox[0] = Math.min(minMaxBoundingBox[0], chunk.getBoundingBox()[0]);
			minMaxBoundingBox[1] = Math.min(minMaxBoundingBox[1], chunk.getBoundingBox()[1]);
			minMaxBoundingBox[2] = Math.max(minMaxBoundingBox[2], chunk.getBoundingBox()[2]);
			minMaxBoundingBox[3] = Math.max(minMaxBoundingBox[3], chunk.getBoundingBox()[3]);
		}

		return minMaxBoundingBox;
	}

	private double[] minMaxBoundingBox(IChunk chunk1, IChunk chunk2) {
		return new double[]{Math.min(chunk1.getLeftX(), chunk2.getLeftX()),
		                    Math.min(chunk1.getBottomY(), chunk2.getBottomY()),
		                    Math.max(chunk1.getRightX(), chunk2.getRightX()),
		                    Math.max(chunk1.getTopY(), chunk2.getTopY())
		};
	}

	private INode buildLineFromChildren(INode node, Map.Entry<Integer, Integer> lineBoundary) {
		List<INode> children = node.getChildren();

		double fontSize = 0;
		double baseLine = 0;

		for (int i = lineBoundary.getKey(); i < lineBoundary.getValue(); ++i) {
			ITextChunk chunk = (ITextChunk) nodeToAccumulatedNodeMap.get(children.get(i));
			if (chunk.getFontSize() > fontSize) {
				fontSize = chunk.getFontSize();
				baseLine = chunk.getBaseLine();
			}
		}

		ITextChunk textChunk = (ITextChunk) nodeToAccumulatedNodeMap.get(children.get(lineBoundary.getKey()));

		return new SemanticTextChunk(minMaxBoundingBox(node, lineBoundary), textChunk.getPageNumber(), "",
		                             fontSize, baseLine);
	}

	private INode buildParagraphFromChildren(INode node) {
		INode firstChild = nodeToAccumulatedNodeMap.get(node.getChildren().get(0));
		INode lastChild = nodeToAccumulatedNodeMap.get(node.getChildren().get(node.numChildren() - 1));
		ITextChunk firstLine = firstChild.getSemanticType().equals(SemanticType.SPAN)
		                       ? (ITextChunk) firstChild : ((SemanticParagraph) firstChild).getFirstLine();
		ITextChunk lastLine = lastChild.getSemanticType().equals(SemanticType.SPAN)
		                      ? (ITextChunk) lastChild : ((SemanticParagraph) lastChild).getLastLine();

		return new SemanticParagraph(minMaxBoundingBox(node, new AbstractMap.SimpleEntry<>(0, node.numChildren())),
		                             ((IChunk) firstChild).getPageNumber(), firstLine, lastLine,
		                             ((IChunk) lastChild).getPageNumber());
	}
}


