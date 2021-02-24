package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticParagraph;
import org.verapdf.wcag.algorithms.entities.SemanticSpan;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
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

		if (node.getSemanticType() == SemanticType.SPAN) {
			acceptSemanticSpan((SemanticSpan) node);
			return;
		}

		if (numChildren <= 1) {
			acceptNodeWithLeq1Child(node);
			return;
		}

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
		updateNode(node, paragraphProbability, SemanticType.PARAGRAPH);
	}

	private void acceptSemanticSpan(SemanticSpan node) {
		if (node.getTextChunks().size() <= 1) {
			acceptNodeWithLeq1Child(node);
		}
		double mergeProbability = 1;
		List<TextChunk> textChunks = node.getTextChunks();
		List<TextChunk> textChunksToRemove = new ArrayList<TextChunk>();
		List<TextChunk> textLine = new ArrayList<TextChunk>();
		int current = 0;
		int next = 1;
		while (next < textChunks.size()) {
			TextChunk prevChunk = textChunks.get(next - 1);
			TextChunk nextChunk = textChunks.get(next);
			double lineMergeProbability = ChunksMergeUtils.toLineMergeProbability(prevChunk, nextChunk);
			if (lineMergeProbability < MERGE_PROBABILITY_THRESHOLD) {
				if (textLine.size() != 0) {
					textChunks.get(current).append(textLine);
					textLine = new ArrayList<>();
				}
				current = next;
			} else {
				textLine.add(nextChunk);
				textChunksToRemove.add(nextChunk);
				mergeProbability *= lineMergeProbability;
			}
			++next;
		}
		if (textLine.size() != 0) {
			textChunks.get(current).append(textLine);
		}
		textChunks.removeAll(textChunksToRemove);

		for (current = 1; current < textChunks.size(); ++current) {
			TextChunk prevChunk = node.getTextChunks().get(current - 1);
			TextChunk currentChunk = node.getTextChunks().get(current);
			if (current == 1 || current == textChunks.size() - 1) {
				mergeProbability *= ChunksMergeUtils.mergeLeadingProbability(prevChunk, currentChunk);
			} else {
				mergeProbability *= ChunksMergeUtils.toParagraphMergeProbability(prevChunk, currentChunk);
			}
		}

		updateNode(node, mergeProbability, SemanticType.SPAN);
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

	private SemanticParagraph buildParagraphFromNode(INode node) {
		if (isNullableSemanticType(node)) {
			return null;
		}
		switch (node.getSemanticType()) {
			case SPAN:
				SemanticSpan span = (SemanticSpan) node;
				List<TextChunk> text = span.getTextChunks();
				return new SemanticParagraph(span.getBoundingBox(), text.get(0), text.get(text.size() - 1));
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
		List<TextChunk> nextLines = span.getTextChunks();
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
			if (!isOneLineParagraph(paragraph) && nextLines.size() > 2) {
				mergeProbability *= ChunksMergeUtils.toParagraphMergeProbability(lastLine, nextLines.get(1));
				paragraph.setLastLine(nextLines.get(nextLines.size() - 1));
			}
			else if (nextLines.size() > 1) {
				mergeProbability *= ChunksMergeUtils.mergeLeadingProbability(lastLine, nextLines.get(1));
				paragraph.setLastLine(nextLines.get(nextLines.size() - 1));
			}
		}
		paragraph.getBoundingBox().union(span.getBoundingBox());
		return (span.getCorrectSemanticScore() == null) ? mergeProbability : (span.getCorrectSemanticScore() * mergeProbability);
	}

	private double toParagraphMergeProbability(SemanticParagraph paragraph1, SemanticParagraph paragraph2) {
		paragraph1.getBoundingBox().union(paragraph2.getBoundingBox());
		paragraph1.setLastLine(paragraph2.getLastLine());
		double mergeProbability = (paragraph2.getCorrectSemanticScore() == null) ? 1 : paragraph2.getCorrectSemanticScore();
		if (isOneLineParagraph(paragraph1)) {
			mergeProbability *= ChunksMergeUtils.mergeLeadingProbability(paragraph1.getFirstLine(), paragraph2.getFirstLine());
		} else {
			mergeProbability *= ChunksMergeUtils.toParagraphMergeProbability(paragraph1.getLastLine(), paragraph2.getFirstLine());
		}
		return mergeProbability;
	}

	private boolean isOneLineParagraph(SemanticParagraph paragraph) {
		return paragraph.getFirstLine() == paragraph.getLastLine();
	}
}
