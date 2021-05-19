package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticParagraph;
import org.verapdf.wcag.algorithms.entities.SemanticSpan;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
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

		if (node.getChildren().isEmpty()) {
			updateNode(node, node, 1, node.getSemanticType());
			return;
		}

		boolean isLeafChild  = true;
		for (INode child : node.getChildren()) {
			if (!(child instanceof SemanticSpan)) {
				isLeafChild = false;
				break;
			}
		}
		if (isLeafChild && node.getInitialSemanticType() == SemanticType.SPAN) {
			acceptSemanticSpan(node);
			return;
		}

		acceptSemanticParagraph(node);
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
				spanProbability = Math.min(spanProbability, toSpanMergeProbability(span, accumulatedChild));
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
		TextLine lastLine = span.getLastLine();
		TextLine nextLine = secondSpan.getFirstLine();
		double mergeProbability = ChunksMergeUtils.toLineMergeProbability(lastLine, nextLine);
		if (mergeProbability < MERGE_PROBABILITY_THRESHOLD) {
			if (span.getLinesNumber() > 1 && secondSpan.getLinesNumber() > 1) {
				mergeProbability = ChunksMergeUtils.toParagraphMergeProbability(lastLine, nextLine);
			} else {
				mergeProbability = ChunksMergeUtils.mergeLeadingProbability(lastLine, nextLine);
			}
			double toColumnsMergeProbability = ChunksMergeUtils.toColumnsMergeProbability(lastLine, nextLine);
			if (toColumnsMergeProbability > mergeProbability) {
				mergeProbability = toColumnsMergeProbability;
			}
			span.getLines().addAll(secondSpan.getLines());
		} else {
			lastLine = new TextLine(lastLine);
			lastLine.add(nextLine);
			span.setLastLine(lastLine);
			if (secondSpan.getLinesNumber() > 1) {
				if (span.getLinesNumber() > 2 && secondSpan.getLinesNumber() > 1) {
					mergeProbability *= ChunksMergeUtils.toParagraphMergeProbability(span.getPenultLine(), lastLine);
				}
				if (span.getLinesNumber() > 1 && secondSpan.getLinesNumber() > 2) {
					mergeProbability *= ChunksMergeUtils.toParagraphMergeProbability(lastLine, secondSpan.getSecondLine());
				}
				span.getLines().addAll(secondSpan.getLines().subList(1, secondSpan.getLinesNumber() - 1));
			}
		}
		span.getBoundingBox().union(secondSpan.getBoundingBox());
		return (secondSpan.getCorrectSemanticScore() == null) ? mergeProbability : (Math.min(mergeProbability, secondSpan.getCorrectSemanticScore()));
	}

	private void updateNode(INode node, INode accumulatedNode, double correctSemanticScore, SemanticType semanticType) {
		if (accumulatedNode == null) {
			node.setCorrectSemanticScore(0.0d);
			return;
		}
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
				paragraphProbability = Math.min(paragraphProbability, toParagraphMergeProbability(paragraph, accumulatedChild));
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
				return new SemanticParagraph(span.getBoundingBox(), span.getLines());
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

	private double toParagraphMergeProbability(SemanticParagraph paragraph, List<TextLine> lines) {
		if (lines.isEmpty()) {
			return 1;
		}
		TextLine lastLine = paragraph.getLastLine();
		TextLine nextLine = lines.get(0);
		double mergeProbability = ChunksMergeUtils.toLineMergeProbability(lastLine, nextLine);
		if (mergeProbability < MERGE_PROBABILITY_THRESHOLD) {
			if (paragraph.getLines().size() > 1 && lines.size() > 1) {
				mergeProbability = ChunksMergeUtils.toParagraphMergeProbability(lastLine, nextLine);
			} else {
				mergeProbability = ChunksMergeUtils.mergeLeadingProbability(lastLine, nextLine);
			}
			double toColumnsMergeProbability = ChunksMergeUtils.toColumnsMergeProbability(lastLine, nextLine);
			if (toColumnsMergeProbability > mergeProbability) {
				mergeProbability = toColumnsMergeProbability;
			}
			paragraph.getLines().addAll(lines);
		} else {
			lastLine = new TextLine(lastLine);
			lastLine.add(nextLine);
			paragraph.setLastLine(lastLine);
			if (lines.size() > 1) {
				if (paragraph.getLinesNumber() > 2 && lines.size() > 1) {
					mergeProbability *= ChunksMergeUtils.toParagraphMergeProbability(paragraph.getPenultLine(), lastLine);
				}
				if (paragraph.getLinesNumber() > 1 && lines.size() > 2) {
					mergeProbability *= ChunksMergeUtils.toParagraphMergeProbability(lastLine, lines.get(1));
				}
				paragraph.getLines().addAll(lines.subList(1, lines.size() - 1));
			}
		}
		return mergeProbability;
	}

	private double toParagraphMergeProbability(SemanticParagraph paragraph, SemanticSpan span) {
		double mergeProbability = toParagraphMergeProbability(paragraph, span.getLines());
		paragraph.getBoundingBox().union(span.getBoundingBox());
		return (span.getCorrectSemanticScore() == null) ? mergeProbability : (Math.min(span.getCorrectSemanticScore(), mergeProbability));
	}

	private double toParagraphMergeProbability(SemanticParagraph paragraph1, SemanticParagraph paragraph2) {
		double mergeProbability = toParagraphMergeProbability(paragraph1, paragraph2.getLines());
		paragraph1.getBoundingBox().union(paragraph2.getBoundingBox());
		return (paragraph2.getCorrectSemanticScore() == null) ? mergeProbability : (Math.min(paragraph2.getCorrectSemanticScore(), mergeProbability));
	}
}
