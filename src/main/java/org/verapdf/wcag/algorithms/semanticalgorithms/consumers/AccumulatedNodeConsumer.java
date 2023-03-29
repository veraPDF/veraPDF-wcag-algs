package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.content.*;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.enums.TextFormat;
import org.verapdf.wcag.algorithms.entities.geometry.MultiBoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccumulatedNodeConsumer extends WCAGConsumer implements Consumer<INode> {

	private static final Logger LOGGER = Logger.getLogger(AccumulatedNodeConsumer.class.getCanonicalName());

	public static final double MERGE_PROBABILITY_THRESHOLD = 0.75;
	public static final double ONE_LINE_MIN_PROBABILITY_THRESHOLD = 0.1;
	public static final double FOOTNOTE_MIN_PROBABILITY_THRESHOLD = 0.75;

	public AccumulatedNodeConsumer() {
	}

	@Override
	public void accept(INode node) {

		if (node.getChildren().isEmpty()) {
			StaticContainers.getAccumulatedNodeMapper().updateNode(node, node, 1.0, node.getSemanticType());
			return;
		}

		boolean isLeafChild = node.getChildren()
				.stream()
				.allMatch(child -> ((child instanceof SemanticSpan) ||
						(child instanceof SemanticFigure) ||
						child.getSemanticType() == null));

		acceptSpanParagraphPart(node, isLeafChild);

		acceptSemanticImage(node);

		checkSemanticSpanChildren(node);
	}

	private void checkSemanticSpanChildren(INode node) {
		INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(node);
		if (!(accumulatedNode instanceof SemanticTextNode)) {
			return;
		}
		if (node.getChildren()
		        .stream()
		        .noneMatch((child) -> child instanceof SemanticSpan)) {
			return;
		}
		SemanticTextNode textNode = (SemanticTextNode) accumulatedNode;
		for (INode child : node.getChildren()) {
			if (!(child instanceof SemanticSpan)) {
				continue;
			}
			for (TextColumn textColumn : ((SemanticSpan)child).getColumns()) {
				for (TextLine textLine : textColumn.getLines()) {
					for (TextChunk textChunk : textLine.getTextChunks()) {
						if (TextChunkUtils.isWhiteSpaceChunk(textChunk)) {
							continue;
						}
						if (!Objects.equals(textNode.getFontSize(), textChunk.getFontSize()) ||
								!Objects.equals(textNode.getItalicAngle(), textChunk.getItalicAngle()) ||
								!Arrays.equals(textNode.getTextColor(), textChunk.getFontColor()) ||
								!Objects.equals(textNode.getFontWeight(), textChunk.getFontWeight())) {
							textChunk.setHasSpecialStyle();
						}
						if (textChunk.getBackgroundColor() != null &&
						    !NodeUtils.hasSimilarBackgroundColor(textNode.getBackgroundColor(),
						                                         textChunk.getBackgroundColor())) {
							textChunk.setHasSpecialBackground();
						}
					}
				}
			}
		}
	}

	private void acceptSemanticImage(INode node) {
		SemanticFigure figureNode = null;
		for (INode child : node.getChildren()) {
			INode accumulatedChild = StaticContainers.getAccumulatedNodeMapper().get(child);
			if (accumulatedChild instanceof SemanticTextNode) {
				if (!((SemanticTextNode)accumulatedChild).isEmpty() && !((SemanticTextNode)accumulatedChild).isSpaceNode()) {
					return;
				}
			} else if (accumulatedChild instanceof SemanticFigure) {
				if (figureNode == null) {
					figureNode = new SemanticFigure((SemanticFigure)accumulatedChild);
				} else {
					figureNode.addImages(((SemanticFigure)accumulatedChild).getImages());
					figureNode.addLineArts(((SemanticFigure)accumulatedChild).getLineArts());
				}
			}
		}
		if (figureNode != null) {
			StaticContainers.getAccumulatedNodeMapper().updateNode(node, figureNode, 1.0, SemanticType.FIGURE);
		}
	}

	private void acceptSpanParagraphPart(INode node, boolean isLeafChild) {
		double probability = 1;
		SemanticPart part = null;
		MultiBoundingBox boundingBox = new MultiBoundingBox();
		for (INode child : node.getChildren()) {
			if (child.getSemanticType() == null || SemanticType.isIgnoredStandardType(child.getInitialSemanticType()) ||
					 (child instanceof IAnnotation)) {
				continue;
			}
			INode accumulatedChild = StaticContainers.getAccumulatedNodeMapper().get(child);
			if (accumulatedChild instanceof SemanticFigure) {
				boundingBox.union(child.getBoundingBox());
				continue;
			}
			if (child.getSemanticType().equals(SemanticType.FIGURE)) {
				continue;
			}
			if (part == null) {
				part = buildPartFromNode(accumulatedChild);
				probability = accumulatedChild.getCorrectSemanticScore();
			} else {
				probability = Math.min(probability, toPartMergeProbability(part, accumulatedChild));
			}
			if (child.getInitialSemanticType() != SemanticType.NOTE &&
					accumulatedChild.getSemanticType() != SemanticType.NOTE) {
				boundingBox.union(child.getBoundingBox());
			}
			if (accumulatedChild.getSemanticType() == SemanticType.NOTE) {
				child.setSemanticType(SemanticType.NOTE);
			}
		}
		if (part != null) {
			part.setBoundingBox(boundingBox);
			node.setBoundingBox(boundingBox);
		}
		SemanticType semanticType = SemanticType.PART;
		INode accumulatedNode = part;
		if (part != null && part.getColumns().stream().allMatch(TextColumn::hasOnlyOneBlock)) {
			boolean isSpan  = SemanticType.SPAN.equals(node.getInitialSemanticType()) &&
					(isLeafChild || node.getChildren().stream()
							.allMatch(AccumulatedNodeConsumer::isAppropriateSpanChild));
			if (isSpan) {
				semanticType = SemanticType.SPAN;
				accumulatedNode = new SemanticSpan(part.getBoundingBox(), part.getColumns());
			} else {
				semanticType = SemanticType.PARAGRAPH;
				accumulatedNode = new SemanticParagraph(part.getBoundingBox(), part.getColumns());
			}
		}
		StaticContainers.getAccumulatedNodeMapper().updateNode(node, accumulatedNode, probability, semanticType);
	}

	private static boolean isAppropriateSpanChild(INode child) {
		return (child instanceof SemanticSpan) || (child instanceof SemanticFigure) ||
				child.getInitialSemanticType() == SemanticType.LINK ||
				child.getSemanticType() == SemanticType.SPAN || child.getSemanticType() == null;
	}

	private SemanticPart buildPartFromNode(INode node) {
		if (isNullableSemanticType(node)) {
			return null;
		}
		 if (node instanceof SemanticTextNode) {
			SemanticTextNode textNode = (SemanticTextNode) node;
			return new SemanticPart(textNode.getBoundingBox(), textNode.getColumns());
		}
		return null;
	}

	private double toPartMergeProbability(SemanticPart part, INode node) {
		if (isNullableSemanticType(node)) {
			return 0d;
		}
		if (node instanceof SemanticTextNode) {
			return toTextNodeMergeProbability(part, (SemanticTextNode) node);
		}
		return 0d;
	}

	private boolean isNullableSemanticType(INode node) {
		if (node.getSemanticType() == null) {
			LOGGER.log(Level.WARNING, "Node with nullable semantic type: {}", node);
			return true;
		}
		return false;
	}

	private double toTextNodeMergeProbability(SemanticPart currentTextNode, SemanticTextNode nextTextNode) {
		if (nextTextNode.isEmpty()) {
			return 1;
		}
		List<TextLine> lines = nextTextNode.getFirstColumn().getLines();
		TextLine lastLine = currentTextNode.getLastLine();
		TextLine nextLine = nextTextNode.getFirstLine();
		double oneLineProbability = ChunksMergeUtils.countOneLineProbability(nextTextNode, lastLine, nextLine);
		double differentLinesProbability;
		if (currentTextNode.getLastColumn().getLinesNumber() > 1 && nextTextNode.getFirstColumn().getLinesNumber() > 1) {
			differentLinesProbability = ChunksMergeUtils.toParagraphMergeProbability(lastLine, nextLine);
		} else {
			differentLinesProbability = ChunksMergeUtils.mergeLeadingProbability(lastLine, nextLine);
		}
		double toColumnsMergeProbability = ChunksMergeUtils.toColumnsMergeProbability(lastLine, nextLine);
		double footnoteProbability = ChunksMergeUtils.getFootnoteProbability(currentTextNode, nextTextNode,
		                                                                     lastLine, nextLine);
		double mergeProbability;
		if (footnoteProbability > Math.max(toColumnsMergeProbability, Math.max(oneLineProbability,
		                                                                       differentLinesProbability)) &&
		    footnoteProbability > FOOTNOTE_MIN_PROBABILITY_THRESHOLD) {
			mergeProbability = footnoteProbability;
			nextTextNode.setSemanticType(SemanticType.NOTE);
		} else if (oneLineProbability < Math.max(Math.max(differentLinesProbability, toColumnsMergeProbability),
		                                         ONE_LINE_MIN_PROBABILITY_THRESHOLD)) {
			if (Math.max(differentLinesProbability, toColumnsMergeProbability) < MERGE_PROBABILITY_THRESHOLD) {
				double toPartMergeProbability = 0.0;
				TextLine penultLine = currentTextNode.getPenultLine();
				TextLine secondLine = nextTextNode.getSecondLine();
				if (penultLine != null && secondLine != null) {
					toPartMergeProbability = ChunksMergeUtils.toPartMergeProbability(lastLine, nextLine, penultLine, secondLine);
				}
				mergeProbability = toPartMergeProbability;
				TextColumn lastColumn = new TextColumn(currentTextNode.getLastColumn());
				lastColumn.getBlocks().addAll(nextTextNode.getFirstColumn().getBlocks());
				currentTextNode.setLastColumn(lastColumn);
				if (nextTextNode.getColumnsNumber() > 1) {
					currentTextNode.addAll(nextTextNode.getColumns().subList(1, nextTextNode.getColumnsNumber()));
				}
			}
			else if (differentLinesProbability >= toColumnsMergeProbability) {
				mergeProbability = differentLinesProbability;
				TextColumn lastColumn = new TextColumn(currentTextNode.getLastColumn());
				TextBlock lastBlock = new TextBlock(lastColumn.getLastTextBlock());
				lastBlock.getLines().addAll(lines);
				lastColumn.setLastTextBlock(lastBlock);
				currentTextNode.setLastColumn(lastColumn);
				if (nextTextNode.getColumnsNumber() > 1) {
					currentTextNode.addAll(nextTextNode.getColumns().subList(1, nextTextNode.getColumnsNumber()));
				}
			}
			else {
				mergeProbability = toColumnsMergeProbability;
				currentTextNode.addAll(nextTextNode.getColumns());
			}
			nextTextNode.setTextFormat(TextFormat.NORMAL);
		} else {
			updateTextChunksFormat(nextTextNode);
			mergeProbability = oneLineProbability;
			lastLine.setNotLineEnd();
			nextLine.setNotLineStart();
			currentTextNode.setLastColumn(new TextColumn(currentTextNode.getLastColumn()));
			TextBlock lastBlock = new TextBlock(currentTextNode.getLastColumn().getLastTextBlock());
			lastLine = new TextLine(lastLine);
			lastLine.add(nextLine);
			lastBlock.setLastLine(lastLine);
			currentTextNode.getLastColumn().setLastTextBlock(lastBlock);
			if (lines.size() > 1) {
				if (currentTextNode.getLastColumn().getLinesNumber() > 2 && lines.size() > 1) {
					mergeProbability *= ChunksMergeUtils.toParagraphMergeProbability(currentTextNode.getPenultLine(), lastLine);
				}
				if (currentTextNode.getLastColumn().getLinesNumber() > 1 && lines.size() > 2) {
					mergeProbability *= ChunksMergeUtils.toParagraphMergeProbability(lastLine, lines.get(1));
				}
				currentTextNode.getLastColumn().getLastTextBlock().getLines().addAll(lines.subList(1, lines.size()));
			}
			if (nextTextNode.getColumnsNumber() > 1) {
				currentTextNode.addAll(nextTextNode.getColumns().subList(1, nextTextNode.getColumnsNumber()));
			}
		}
		return (nextTextNode.getCorrectSemanticScore() == null) ? mergeProbability : (Math.min(mergeProbability, nextTextNode.getCorrectSemanticScore()));
	}

	private void updateTextChunksFormat(SemanticTextNode textNode) {
		TextFormat format = textNode.getTextFormat();
		TextLine line = textNode.getFirstLine();
		for (TextChunk chunk : line.getTextChunks()) {
			if (chunk.getTextFormat() != format) {
				chunk.setTextFormat(format);
			} else {
				break;
			}
		}
	}

	public WCAGProgressStatus getWCAGProgressStatus() {
		return WCAGProgressStatus.PARAGRAPH_DETECTION;
	}
}
