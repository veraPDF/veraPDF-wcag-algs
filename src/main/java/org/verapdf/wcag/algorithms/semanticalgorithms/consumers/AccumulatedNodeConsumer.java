package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticParagraph;
import org.verapdf.wcag.algorithms.entities.SemanticSpan;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.maps.AccumulatedNodeMapper;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ChunksMergeUtils;

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

		if (node.getChildren().isEmpty()) {				;
			updateNode(node, 1, node.getSemanticType());
			return;
		}

		boolean isLeafChild  = true;
		for (INode child : node.getChildren()) {
			if (!(child instanceof SemanticSpan)) {
				isLeafChild = false;
			}
		}
		if (isLeafChild && node.getInitialSemanticType() == SemanticType.SPAN) {
			acceptSemanticSpan(node);
			return;
		}

		acceptSemanticParagraph(node);
	}

	private void acceptNodeWithLeq1Child(INode node) {
		List<INode> children = node.getChildren();
		if (children.isEmpty()) {
			updateNode(node, 1, node.getSemanticType());
		} else {
			INode child = children.get(0);
			if (child.getSemanticType() == SemanticType.SPAN && (!(child instanceof SemanticSpan) || node.getInitialSemanticType() == SemanticType.PARAGRAPH)) {
				updateNode(node, child.getCorrectSemanticScore(), SemanticType.PARAGRAPH);
			} else {
				updateNode(node, child.getCorrectSemanticScore(), child.getSemanticType());
			}
		}
	}

	private void acceptSemanticSpan(INode node) {
		double spanProbability = 1;
		SemanticSpan span  = null;
		for (INode child : node.getChildren()) {
			INode accumulatedChild = accumulatedNodeMapper.get(child);
			if (span == null) {
				span = buildSpanFromNode(accumulatedChild);
				spanProbability = accumulatedChild.getCorrectSemanticScore();
			} else {
				spanProbability *= toSpanMergeProbability(span, accumulatedChild);
			}
		}
		updateNode(node, span, spanProbability, SemanticType.SPAN);
	}

	private SemanticSpan buildSpanFromNode(INode node) {
		if (isNullableSemanticType(node)) {
			return null;
		}
		switch (node.getSemanticType()) {
			case SPAN:
				return new SemanticSpan((SemanticSpan) node);
			default:
				return null;
		}
	}

	private double toSpanMergeProbability(SemanticSpan span, INode node) {
		if (isNullableSemanticType(node)) {
			return 0d;
		}
		switch (node.getSemanticType()) {
			case SPAN:
				return toSpanMergeProbability(span, (SemanticSpan) node);
			default:
				return 0d;
		}
	}

	private double toSpanMergeProbability(SemanticSpan span, SemanticSpan secondSpan) {
		if (secondSpan.getLinesNumber() == 0) {
			return 1;
		}
		TextChunk lastLine = span.getLastLine();
		TextChunk nextLine = secondSpan.getFirstLine();
		double mergeProbability = ChunksMergeUtils.toLineMergeProbability(lastLine, nextLine);
		if (mergeProbability < MERGE_PROBABILITY_THRESHOLD) {
			if (span.getLines().size() != 1 && secondSpan.getLinesNumber() > 1) {
				mergeProbability = ChunksMergeUtils.toParagraphMergeProbability(lastLine, nextLine);
			} else {
				mergeProbability = ChunksMergeUtils.mergeLeadingProbability(lastLine, nextLine);
			}
			span.getLines().addAll(secondSpan.getLines());
		} else {
			lastLine = new TextChunk(lastLine);
			lastLine.append(nextLine);
			span.getLines().set(span.getLines().size() - 1, lastLine);
			if (secondSpan.getLinesNumber() > 1) {
				if (span.getLines().size() != 1 && secondSpan.getLinesNumber() > 2) {
					mergeProbability *= ChunksMergeUtils.mergeIndentationProbability(lastLine, secondSpan.getSecondLine());
				}
				span.getLines().addAll(secondSpan.getLines().subList(1, secondSpan.getLinesNumber() - 1));
			}
		}
		span.getBoundingBox().union(secondSpan.getBoundingBox());
		return (secondSpan.getCorrectSemanticScore() == null) ? mergeProbability : (secondSpan.getCorrectSemanticScore() * mergeProbability);
	}

	private void updateNode(INode node, double correctSemanticScore, SemanticType semanticType) {
		node.setSemanticType(semanticType);
		INode accumulatedNode = accumulatedNodeMapper.calculateAccumulatedNode(node);
		updateNode(node, accumulatedNode, correctSemanticScore, semanticType);
	}

	private void updateNode(INode node, INode accumulatedNode, double correctSemanticScore, SemanticType semanticType) {
		node.setCorrectSemanticScore(correctSemanticScore);
		node.setSemanticType(semanticType);
		node.setBoundingBox(accumulatedNode.getBoundingBox());
		accumulatedNode.setCorrectSemanticScore(correctSemanticScore);
		accumulatedNodeMapper.put(node, accumulatedNode);
	}

	private void acceptSemanticParagraph(INode node) {
		double paragraphProbability = 1;
		SemanticParagraph paragraph = null;
		for (INode child : node.getChildren()) {
			INode accumulatedChild = accumulatedNodeMapper.get(child);
			if (paragraph == null) {
				paragraph = buildParagraphFromNode(accumulatedChild);
				paragraphProbability = accumulatedChild.getCorrectSemanticScore();
			} else {
				paragraphProbability *= toParagraphMergeProbability(paragraph, accumulatedChild);
			}
		}

		updateNode(node, paragraph, paragraphProbability, SemanticType.PARAGRAPH);
	}

	private SemanticParagraph buildParagraphFromNode(INode node) {
		if (isNullableSemanticType(node)) {
			return null;
		}
		switch (node.getSemanticType()) {
			case SPAN:
				SemanticSpan span = (SemanticSpan) node;
				List<TextChunk> text = span.getLines();
				return new SemanticParagraph(span.getBoundingBox(), text.get(0), text.get(text.size() - 1));
			case PARAGRAPH:
				return new SemanticParagraph((SemanticParagraph) node);
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
		List<TextChunk> nextLines = span.getLines();
		if (nextLines.size() == 0) {
			return 1;
		}
		TextChunk lastLine = paragraph.getLastLine();
		TextChunk nextLine = nextLines.get(0);

		double mergeProbability = ChunksMergeUtils.toLineMergeProbability(lastLine, nextLine);
		if (mergeProbability < MERGE_PROBABILITY_THRESHOLD) {
			if (!isOneLineParagraph(paragraph) && nextLines.size() > 1) {
				mergeProbability = ChunksMergeUtils.toParagraphMergeProbability(lastLine, nextLine);
				paragraph.setLastLine(nextLines.get(nextLines.size() - 1));
			} else {
				mergeProbability = ChunksMergeUtils.mergeLeadingProbability(lastLine, nextLine);
				paragraph.setLastLine(nextLine);
			}
		} else {
			lastLine = new TextChunk(lastLine);
			lastLine.append(nextLine);
			if (isOneLineParagraph(paragraph)) {
				paragraph.setFirstLine(lastLine);
			}
			paragraph.setLastLine(lastLine);
			if (!isOneLineParagraph(paragraph) && nextLines.size() > 2) {
				mergeProbability *= ChunksMergeUtils.mergeIndentationProbability(lastLine, nextLines.get(1));
				paragraph.setLastLine(nextLines.get(nextLines.size() - 1));
			} else if (nextLines.size() > 1) {
				paragraph.setLastLine(nextLines.get(nextLines.size() - 1));
			}
		}
		paragraph.getBoundingBox().union(span.getBoundingBox());
		return (span.getCorrectSemanticScore() == null) ? mergeProbability : (span.getCorrectSemanticScore() * mergeProbability);
	}

	private double toParagraphMergeProbability(SemanticParagraph paragraph1, SemanticParagraph paragraph2) {
		paragraph1.getBoundingBox().union(paragraph2.getBoundingBox());
		double mergeProbability = ChunksMergeUtils.toLineMergeProbability(paragraph1.getLastLine(), paragraph2.getFirstLine());
		if (mergeProbability < MERGE_PROBABILITY_THRESHOLD) {
			if (isOneLineParagraph(paragraph1) || isOneLineParagraph(paragraph2)) {
				mergeProbability = ChunksMergeUtils.mergeLeadingProbability(paragraph1.getFirstLine(), paragraph2.getFirstLine());
			} else {
				mergeProbability = ChunksMergeUtils.toParagraphMergeProbability(paragraph1.getLastLine(), paragraph2.getFirstLine());
			}
			paragraph1.setLastLine(paragraph2.getLastLine());
		} else {
			//ToDo: calculate mergeProbability
			TextChunk newLine = new TextChunk(paragraph1.getLastLine());
			newLine.append(paragraph2.getFirstLine());
			if (isOneLineParagraph(paragraph1)) {
				paragraph1.setFirstLine(newLine);
			}
			paragraph1.setLastLine(newLine);
		}
		mergeProbability *= (paragraph2.getCorrectSemanticScore() == null) ? 1 : paragraph2.getCorrectSemanticScore();
		return mergeProbability;
	}

	private boolean isOneLineParagraph(SemanticParagraph paragraph) {
		return paragraph.getFirstLine() == paragraph.getLastLine();
	}
}
