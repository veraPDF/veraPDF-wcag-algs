package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.content.TextBlock;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextColumn;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.enums.TextFormat;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.CaptionUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ChunksMergeUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccumulatedNodeConsumer implements Consumer<INode> {

	private static final Logger LOGGER = Logger.getLogger(AccumulatedNodeConsumer.class.getCanonicalName());

	public static final double MERGE_PROBABILITY_THRESHOLD = 0.75;
	public static final double ONE_LINE_MIN_PROBABILITY_THRESHOLD = 0.1;
	public static final double FOOTNOTE_MIN_PROBABILITY_THRESHOLD = 0.1;

	public AccumulatedNodeConsumer() {
	}

	@Override
	public void accept(INode node) {

		if (node.getChildren().isEmpty()) {
			StaticContainers.getAccumulatedNodeMapper().updateNode(node, node, 1.0, node.getSemanticType());
			return;
		}

		boolean isLeafChild  = node.getChildren()
				.stream()
				.allMatch(child -> ((child instanceof SemanticSpan) ||
						(child instanceof SemanticImageNode) ||
						(child instanceof SemanticFigure) ||
						child.getSemanticType() == null));

		acceptSpanParagraphPart(node, isLeafChild);

		acceptSemanticImage(node);

		checkSemanticSpanChildren(node);

		if (!isLeafChild) {
			acceptChildrenSemanticHeading(node);
			acceptChildrenSemanticCaption(node);
		}

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
		SemanticImageNode imageNode = null;
		SemanticFigure lineArtNode = null;
		for (INode child : node.getChildren()) {
			INode accumulatedChild = StaticContainers.getAccumulatedNodeMapper().get(child);
			if (accumulatedChild instanceof SemanticTextNode) {
				if (!((SemanticTextNode)accumulatedChild).isEmpty() && !((SemanticTextNode)accumulatedChild).isSpaceNode()) {
					return;
				}
			} else if (accumulatedChild instanceof SemanticImageNode) {
				if (imageNode != null || lineArtNode != null) {
					return;
				}
				imageNode = (SemanticImageNode)accumulatedChild;
			} else if (accumulatedChild instanceof SemanticFigure) {
				if (imageNode != null || lineArtNode != null) {
					return;
				}
				lineArtNode = (SemanticFigure)accumulatedChild;
			}
		}
		if (imageNode != null) {
			StaticContainers.getAccumulatedNodeMapper().updateNode(node, new SemanticImageNode(imageNode),
					imageNode.getCorrectSemanticScore(), SemanticType.FIGURE);
		} else if (lineArtNode != null) {
			StaticContainers.getAccumulatedNodeMapper().updateNode(node, new SemanticFigure(lineArtNode),
					lineArtNode.getCorrectSemanticScore(), SemanticType.FIGURE);
		}
	}

	private void acceptSpanParagraphPart(INode node, boolean isLeafChild) {
		double probability = 1;
		SemanticPart part = null;
		for (INode child : node.getChildren()) {
			if (child.getSemanticType() == null || SemanticType.isIgnoredStandardType(child.getInitialSemanticType()) ||
					child.getSemanticType().equals(SemanticType.FIGURE)) {
				continue;
			}
			INode accumulatedChild = StaticContainers.getAccumulatedNodeMapper().get(child);
			if (part == null) {
				part = buildPartFromNode(accumulatedChild);
				probability = accumulatedChild.getCorrectSemanticScore();
			} else {
				probability = Math.min(probability, toPartMergeProbability(part, accumulatedChild));
			}
		}

		SemanticType semanticType = SemanticType.PART;
		INode accumulatedNode = part;
		if (part != null && part.getColumns().stream().allMatch(TextColumn::hasOnlyOneBlock)) {
			boolean isSpan  = SemanticType.SPAN.equals(node.getInitialSemanticType()) &&
			                  (isLeafChild || node.getChildren().stream()
			                                      .allMatch(child -> ((child instanceof SemanticSpan) ||
			                                                          (child instanceof SemanticImageNode) ||
			                                                          (child instanceof SemanticFigure) ||
			                                                          child.getSemanticType() == SemanticType.SPAN ||
			                                                          child.getSemanticType() == null)));
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

	private void acceptChildrenSemanticHeading(INode node) {
		List<INode> children = new ArrayList<>(node.getChildren().size());
		for (INode child : node.getChildren()) {
			if (child != null && !SemanticType.BLOCK_QUOTE.equals(child.getInitialSemanticType())) {
				INode accumulatedChild = StaticContainers.getAccumulatedNodeMapper().get(child);
				if (accumulatedChild instanceof SemanticTextNode) {
					SemanticTextNode textNode = (SemanticTextNode)accumulatedChild;
					if (!textNode.isSpaceNode() && !textNode.isEmpty()) {
						children.add(child);
					}
				}
			}
		}
		if (children.size() <= 1) {
			return;
		}
		if (children.size() == 2) {
			acceptSemanticHeading(children.get(0), null, children.get(1), null);
			return;
		}
		acceptSemanticHeading(children.get(0), null, children.get(1), children.get(2));
		for (int i = 1; i < children.size() - 2; i++) {
			acceptSemanticHeading(children.get(i), children.get(i - 1), children.get(i + 1), children.get(i + 2));
		}
		acceptSemanticHeading(children.get(children.size() - 2), children.get(children.size() - 3), children.get(children.size() - 1), null);
		if (node.getNextNode() == null || !children.get(children.size() - 1).getPageNumber()
		                                           .equals(node.getNextNode().getPageNumber())) {
			return;
		}
		acceptSemanticHeading(children.get(children.size() - 1), children.get(children.size() - 2), null, null);
	}

	private void acceptSemanticHeading(INode node, INode previousNode, INode nextNode, INode nextNextNode) {
		double headingProbability = NodeUtils.headingProbability(StaticContainers.getAccumulatedNodeMapper().get(node),
				StaticContainers.getAccumulatedNodeMapper().get(previousNode), StaticContainers.getAccumulatedNodeMapper().get(nextNode),
				StaticContainers.getAccumulatedNodeMapper().get(nextNextNode), node);
		if (headingProbability >= MERGE_PROBABILITY_THRESHOLD) {
			INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(node);
			if (node.getInitialSemanticType() == SemanticType.NUMBER_HEADING) {
				if (accumulatedNode instanceof SemanticSpan) {
					StaticContainers.getAccumulatedNodeMapper().updateNode(node, new SemanticNumberHeading((SemanticSpan)accumulatedNode),
					           headingProbability * node.getCorrectSemanticScore(), SemanticType.NUMBER_HEADING);
				} else if (accumulatedNode instanceof SemanticParagraph) {
					StaticContainers.getAccumulatedNodeMapper().updateNode(node, new SemanticNumberHeading((SemanticParagraph)accumulatedNode),
					           headingProbability * node.getCorrectSemanticScore(), SemanticType.NUMBER_HEADING);
				} else if (accumulatedNode instanceof SemanticPart) {
					StaticContainers.getAccumulatedNodeMapper().updateNode(node, new SemanticNumberHeading((SemanticPart)accumulatedNode),
					           headingProbability * node.getCorrectSemanticScore(), SemanticType.NUMBER_HEADING);
				}
			} else {
				if (accumulatedNode instanceof SemanticSpan) {
					StaticContainers.getAccumulatedNodeMapper().updateNode(node, new SemanticHeading((SemanticSpan)accumulatedNode),
					           headingProbability * node.getCorrectSemanticScore(), SemanticType.HEADING);
				} else if (accumulatedNode instanceof SemanticParagraph) {
					StaticContainers.getAccumulatedNodeMapper().updateNode(node, new SemanticHeading((SemanticParagraph)accumulatedNode),
					           headingProbability * node.getCorrectSemanticScore(), SemanticType.HEADING);
				} else if (accumulatedNode instanceof SemanticPart) {
					StaticContainers.getAccumulatedNodeMapper().updateNode(node, new SemanticHeading((SemanticPart)accumulatedNode),
					           headingProbability * node.getCorrectSemanticScore(), SemanticType.HEADING);
				}
			}
		}
	}

	private void acceptChildrenSemanticCaption(INode node) {
		INode imageNode = null;
		INode lastTextNode = null;
		for (INode child : node.getChildren()) {
			if (child != null) {
				INode accumulatedChild = StaticContainers.getAccumulatedNodeMapper().get(child);
				if (accumulatedChild instanceof SemanticTextNode) {
					SemanticTextNode textNode = (SemanticTextNode)accumulatedChild;
					if (!textNode.isSpaceNode() && !textNode.isEmpty()) {
						if (imageNode != null) {
							acceptImageCaption(imageNode, lastTextNode, child);
							imageNode = null;
						}
						lastTextNode = child;
					}
				} else if (accumulatedChild instanceof SemanticImageNode) {
					if (imageNode != null) {
						acceptImageCaption(imageNode, lastTextNode, null);
						lastTextNode = null;
					}
					imageNode = child;
				}
			}
		}
		if (imageNode != null) {
			acceptImageCaption(imageNode, lastTextNode, null);
		}
	}

	private void acceptImageCaption(INode imageNode, INode previousNode, INode nextNode) {
		SemanticImageNode image = (SemanticImageNode)StaticContainers.getAccumulatedNodeMapper().get(imageNode);
		double previousCaptionProbability = CaptionUtils.imageCaptionProbability(previousNode, image);
		double nextCaptionProbability = CaptionUtils.imageCaptionProbability(nextNode, image);
		double captionProbability;
		INode captionNode;
		if (previousCaptionProbability > nextCaptionProbability) {
			captionProbability = previousCaptionProbability;
			captionNode = previousNode;
		} else {
			captionProbability = nextCaptionProbability;
			captionNode = nextNode;
		}
		if (captionProbability >= MERGE_PROBABILITY_THRESHOLD) {
			StaticContainers.getAccumulatedNodeMapper().updateNode(captionNode,
					new SemanticCaption((SemanticTextNode) StaticContainers.getAccumulatedNodeMapper().get(captionNode)),
					captionProbability * captionNode.getCorrectSemanticScore(), SemanticType.CAPTION);
		}
	}

	private void updateTextChunksFormat(SemanticTextNode textNode) {
		TextFormat format = textNode.getTextFormat();
		for (TextColumn column : textNode.getColumns()) {
			for (TextLine line : column.getLines()) {
				for (TextChunk chunk : line.getTextChunks()) {
					chunk.setTextFormat(format);
				}
			}
		}
	}
}
