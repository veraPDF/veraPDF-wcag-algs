package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.enums.TextFormat;
import org.verapdf.wcag.algorithms.entities.lists.ListInterval;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorder;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;
import org.verapdf.wcag.algorithms.entities.SemanticParagraph;
import org.verapdf.wcag.algorithms.entities.SemanticSpan;
import org.verapdf.wcag.algorithms.entities.SemanticHeading;
import org.verapdf.wcag.algorithms.entities.SemanticImageNode;
import org.verapdf.wcag.algorithms.entities.SemanticFigure;
import org.verapdf.wcag.algorithms.entities.SemanticTextNode;
import org.verapdf.wcag.algorithms.entities.SemanticCaption;
import org.verapdf.wcag.algorithms.entities.SemanticNumberHeading;
import org.verapdf.wcag.algorithms.entities.content.TextColumn;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.content.LineArtChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.ImageChunk;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ChunksMergeUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ListLabelsUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Objects;
import java.util.Arrays;

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
		boolean isSpan  = isLeafChild || node.getChildren()
		                      				.stream()
		                      				.allMatch(child -> ((child instanceof SemanticSpan) ||
		                                          (child instanceof SemanticImageNode) ||
									              (child instanceof SemanticFigure) ||
		                                          child.getSemanticType() == SemanticType.SPAN ||
		                                          child.getSemanticType() == null));
		if (node.getInitialSemanticType() == SemanticType.SPAN && isSpan) {
			acceptSemanticSpan(node);
		} else {
			acceptSemanticParagraph(node);
		}

		acceptSemanticImage(node);

		checkSemanticSpanChildren(node);

		if (!isLeafChild) {
			acceptChildrenSemanticHeading(node);
			acceptSemanticList(node);
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
								!Objects.equals(textNode.getFontName(), textChunk.getFontName()) ||
								!Arrays.equals(textNode.getTextColor(), textChunk.getFontColor()) ||
								!Objects.equals(textNode.getFontWeight(), textChunk.getFontWeight())) {
							textChunk.setHasSpecialStyle();
						}
					}
				}
			}
		}
	}

	private void acceptSemanticImage(INode node) {
		INode imageNode = null;
		for (INode child : node.getChildren()) {
			INode accumulatedChild = StaticContainers.getAccumulatedNodeMapper().get(child);
			if (accumulatedChild instanceof SemanticTextNode) {
				if (!((SemanticTextNode)accumulatedChild).isEmpty() && !((SemanticTextNode)accumulatedChild).isSpaceNode()) {
					return;
				}
			} else if (accumulatedChild instanceof SemanticImageNode) {
				if (imageNode != null) {
					return;
				}
				imageNode = accumulatedChild;
			} else if (accumulatedChild instanceof SemanticFigure) {
				if (imageNode != null) {
					return;
				}
				imageNode = accumulatedChild;
			}
		}
		if (imageNode != null) {
			StaticContainers.getAccumulatedNodeMapper().updateNode(node, imageNode,
					imageNode.getCorrectSemanticScore(), SemanticType.FIGURE);
		}
	}

	private void acceptSemanticSpan(INode node) {
		double spanProbability = 1;
		SemanticSpan span  = null;
		for (INode child : node.getChildren()) {
			if (child.getSemanticType() == null || SemanticType.isIgnoredStandardType(child.getInitialSemanticType()) ||
					child.getSemanticType().equals(SemanticType.FIGURE)) {
				continue;
			}
			INode accumulatedChild = StaticContainers.getAccumulatedNodeMapper().get(child);
			if (span == null) {
				span = buildSpanFromNode(accumulatedChild);
				spanProbability = accumulatedChild.getCorrectSemanticScore();
			} else {
				spanProbability = Math.min(spanProbability, toSpanMergeProbability(span, accumulatedChild));
			}
		}
		StaticContainers.getAccumulatedNodeMapper().updateNode(node, span, spanProbability, SemanticType.SPAN);
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
				return toTextNodeMergeProbability(span, (SemanticSpan) node);
			default:
				return 0d;
		}
	}

	private void acceptSemanticParagraph(INode node) {
		double paragraphProbability = 1;
		SemanticParagraph paragraph = null;
		for (INode child : node.getChildren()) {
			if (child.getSemanticType() == null || SemanticType.isIgnoredStandardType(child.getInitialSemanticType()) ||
					child.getSemanticType().equals(SemanticType.FIGURE)) {
				continue;
			}
			INode accumulatedChild = StaticContainers.getAccumulatedNodeMapper().get(child);
			if (paragraph == null) {
				paragraph = buildParagraphFromNode(accumulatedChild);
				paragraphProbability = accumulatedChild.getCorrectSemanticScore();
			} else {
				paragraphProbability = Math.min(paragraphProbability, toParagraphMergeProbability(paragraph, accumulatedChild));
			}
		}
		StaticContainers.getAccumulatedNodeMapper().updateNode(node, paragraph, paragraphProbability, SemanticType.PARAGRAPH);
	}

	private SemanticParagraph buildParagraphFromNode(INode node) {
		if (isNullableSemanticType(node)) {
			return null;
		}
		if (node instanceof SemanticParagraph) {
			return new SemanticParagraph((SemanticParagraph) node);
		} else if (node instanceof SemanticTextNode) {
			SemanticTextNode textNode = (SemanticTextNode) node;
			return new SemanticParagraph(textNode.getBoundingBox(), textNode.getColumns());
		}
		return null;
	}

	private double toParagraphMergeProbability(SemanticParagraph paragraph, INode node) {
		if (isNullableSemanticType(node)) {
			return 0d;
		}
		if (node instanceof SemanticTextNode) {
			return toTextNodeMergeProbability(paragraph, (SemanticTextNode) node);
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

	private double toTextNodeMergeProbability(SemanticTextNode currentTextNode, SemanticTextNode nextTextNode) {
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
		double footnoteProbability = ChunksMergeUtils.getFootnoteProbability(currentTextNode, nextTextNode, lastLine, nextLine);
		double mergeProbability;
		if (footnoteProbability > Math.max(oneLineProbability, differentLinesProbability) &&
		    footnoteProbability > FOOTNOTE_MIN_PROBABILITY_THRESHOLD) {
			mergeProbability = footnoteProbability;
		} else if (oneLineProbability < Math.max(Math.max(differentLinesProbability, toColumnsMergeProbability), ONE_LINE_MIN_PROBABILITY_THRESHOLD)) {
			if (differentLinesProbability >= toColumnsMergeProbability) {
				mergeProbability = differentLinesProbability;
				TextColumn lastColumn = new TextColumn(currentTextNode.getLastColumn());
				lastColumn.getLines().addAll(lines);
				currentTextNode.setLastColumn(lastColumn);
				if (nextTextNode.getColumnsNumber() > 1) {
					currentTextNode.addAll(nextTextNode.getColumns().subList(1, nextTextNode.getColumnsNumber()));
				}
			} else {
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
			lastLine = new TextLine(lastLine);
			lastLine.add(nextLine);
			currentTextNode.getLastColumn().setLastLine(lastLine);
			if (lines.size() > 1) {
				if (currentTextNode.getLastColumn().getLinesNumber() > 2 && lines.size() > 1) {
					mergeProbability *= ChunksMergeUtils.toParagraphMergeProbability(currentTextNode.getPenultLine(), lastLine);
				}
				if (currentTextNode.getLastColumn().getLinesNumber() > 1 && lines.size() > 2) {
					mergeProbability *= ChunksMergeUtils.toParagraphMergeProbability(lastLine, lines.get(1));
				}
				currentTextNode.getLastColumn().getLines().addAll(lines.subList(1, lines.size()));
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
		if (SemanticType.LIST.equals(node.getSemanticType())) {
			return;
		}
		double headingProbability = NodeUtils.headingProbability(StaticContainers.getAccumulatedNodeMapper().get(node),
				StaticContainers.getAccumulatedNodeMapper().get(previousNode), StaticContainers.getAccumulatedNodeMapper().get(nextNode),
				StaticContainers.getAccumulatedNodeMapper().get(nextNextNode), node.getInitialSemanticType());
		if (headingProbability >= MERGE_PROBABILITY_THRESHOLD) {
			INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(node);
			if (node.getInitialSemanticType() == SemanticType.NUMBER_HEADING) {
				if (accumulatedNode instanceof SemanticSpan) {
					StaticContainers.getAccumulatedNodeMapper().updateNode(node, new SemanticNumberHeading((SemanticSpan)accumulatedNode),
					           headingProbability * node.getCorrectSemanticScore(), SemanticType.NUMBER_HEADING);
				} else if (accumulatedNode instanceof SemanticParagraph) {
					StaticContainers.getAccumulatedNodeMapper().updateNode(node, new SemanticNumberHeading((SemanticParagraph)accumulatedNode),
					           headingProbability * node.getCorrectSemanticScore(), SemanticType.NUMBER_HEADING);
				}
			} else {
				if (accumulatedNode instanceof SemanticSpan) {
					StaticContainers.getAccumulatedNodeMapper().updateNode(node, new SemanticHeading((SemanticSpan)accumulatedNode),
					           headingProbability * node.getCorrectSemanticScore(), SemanticType.HEADING);
				} else if (accumulatedNode instanceof SemanticParagraph) {
					StaticContainers.getAccumulatedNodeMapper().updateNode(node, new SemanticHeading((SemanticParagraph)accumulatedNode),
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
		double previousCaptionProbability = NodeUtils.imageCaptionProbability(previousNode, image);
		double nextCaptionProbability = NodeUtils.imageCaptionProbability(nextNode, image);
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

	private void acceptSemanticList(INode node) {
		INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(node);
		TableBorder tableBorder = StaticContainers.getTableBordersCollection().getTableBorder(node.getBoundingBox());
		if (accumulatedNode != null && tableBorder != null &&
				tableBorder.getTableBorderCell(accumulatedNode.getBoundingBox()) == null) {
			return;
		}
		List<INode> textChildren = new ArrayList<>(node.getChildren().size());
		List<TextLine> childrenFirstLines = new ArrayList<>(node.getChildren().size());
		List<INode> imageChildren = new ArrayList<>(node.getChildren().size());
		List<ImageChunk> childrenImages = new ArrayList<>(node.getChildren().size());
		List<INode> lineArtChildren = new ArrayList<>(node.getChildren().size());
		List<LineArtChunk> childrenLineArts = new ArrayList<>(node.getChildren().size());
		for (INode child : node.getChildren()) {
			if (child != null) {
				INode newChild = child;
				while (!newChild.getChildren().isEmpty()) {
					newChild = newChild.getChildren().get(0);
				}
				if (newChild instanceof SemanticImageNode) {
					imageChildren.add(child);
					childrenImages.add(((SemanticImageNode) newChild).getImage());
				} else if (newChild instanceof SemanticFigure) {
					lineArtChildren.add(child);
					childrenLineArts.add(((SemanticFigure) newChild).getLineArt());
				} else {
					INode accumulatedChild = StaticContainers.getAccumulatedNodeMapper().get(child);
					if (accumulatedChild instanceof SemanticTextNode) {
						SemanticTextNode textNode = (SemanticTextNode)accumulatedChild;
						if (!textNode.isSpaceNode() && !textNode.isEmpty()) {
							for (TextLine line : textNode.getFirstColumn().getLines()) {
								if (!line.getValue().trim().isEmpty()) {
									textChildren.add(child);
									childrenFirstLines.add(line);
									break;
								}
							}
						}
					}
				}
			}
		}
		if (textChildren.size() > 1) {
			List<String> listItems = new ArrayList<>(node.getChildren().size());
			for (TextLine line : childrenFirstLines) {
				listItems.add(line.getValue().trim());
			}
			ListUtils.updateTreeWithRecognizedLists(node, textChildren,
					ListUtils.getChildrenListIntervals(ListLabelsUtils.getListItemsIntervals(listItems), textChildren,
							childrenFirstLines));
		} else if (textChildren.size() == 1 && SemanticType.LIST.equals(node.getInitialSemanticType()) &&
		           ListLabelsUtils.isListLabel(childrenFirstLines.get(0).getValue().trim().charAt(0))) {
			ListUtils.updateTreeWithRecognizedList(node, textChildren, new ListInterval(0, 0));
		}
		if (imageChildren.size() > 1) {
			ListUtils.updateTreeWithRecognizedLists(node, imageChildren,
					ListUtils.getChildrenListIntervals(ListLabelsUtils.getImageListItemsIntervals(childrenImages),
							imageChildren, childrenImages));
		}
		if (lineArtChildren.size() > 1) {
			ListUtils.updateTreeWithRecognizedLists(node, lineArtChildren,
					ListUtils.getChildrenListIntervals(ListLabelsUtils.getImageListItemsIntervals(childrenLineArts),
							lineArtChildren, childrenLineArts));
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
