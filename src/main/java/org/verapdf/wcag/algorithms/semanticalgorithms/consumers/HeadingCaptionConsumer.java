package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.*;

import java.util.*;
import java.util.function.Consumer;

public class HeadingCaptionConsumer extends WCAGConsumer implements Consumer<INode> {

	public static final double MERGE_PROBABILITY_THRESHOLD = 0.75;

	@Override
	public void accept(INode node) {
		if (node.getChildren().isEmpty()) {
			return;
		}
		boolean isLeafChild = node.getChildren()
				.stream()
				.allMatch(child -> ((child instanceof SemanticSpan) ||
						(child instanceof SemanticFigure) || (child instanceof SemanticAnnot) ||
						child.getSemanticType() == null));
		if (isLeafChild) {
			return;
		}
		acceptChildrenSemanticHeading(node);
		acceptChildrenSemanticCaption(node);
	}

	private void acceptChildrenSemanticHeading(INode node) {
		List<INode> children = new ArrayList<>(node.getChildren().size());
		List<SemanticTextNode> textChildren = new ArrayList<>(node.getChildren().size());
		for (INode child : node.getChildren()) {
			if (child != null && SemanticType.BLOCK_QUOTE != child.getInitialSemanticType()) {
				INode accumulatedChild = StaticContainers.getAccumulatedNodeMapper().get(child);
				if (accumulatedChild instanceof SemanticTextNode) {
					SemanticTextNode textNode = (SemanticTextNode)accumulatedChild;
					if (!textNode.isSpaceNode() && !textNode.isEmpty()) {
						children.add(child);
						textChildren.add(textNode);
					}
				}
			}
		}
		if (children.isEmpty()) {
			return;
		}
		boolean singleChild = false;
		if (children.size() == 1) {
			INode child = children.get(0);
			if (HeadingUtils.isInitialHeadingNode(child)) {
				INode nextNode = getNextNonEmptyTextNode(child);
				singleChild = true;
				if (nextNode != null) {
					children.add(nextNode);
					textChildren.add((SemanticTextNode)StaticContainers.getAccumulatedNodeMapper().get(nextNode));
				}
			}
		}
		List<Integer> indexes = getIndexes(textChildren);
		if (indexes.size() == 1) {
			return;
		}
		acceptHeadings(children, textChildren, indexes, singleChild);
	}

	private static void acceptHeadings(List<INode> children, List<SemanticTextNode> textChildren, List<Integer> indexes,
									   boolean singleChild) {
		for (int i = 0; i < indexes.size() - 1; i++) {
			if (indexes.get(i + 1) - indexes.get(i) > 3) {
				continue;
			}
			List<Double> probabilities = new LinkedList<>();
			boolean areHeadings = true;
			for (int index = indexes.get(i); index < indexes.get(i + 1); index++) {
				double probability = NodeUtils.headingProbability(textChildren.get(index),
						i != 0 ? textChildren.get(indexes.get(i - 1)) : null,
						textChildren.get(indexes.get(i + 1)) , children.get(index));
				if (probability < MERGE_PROBABILITY_THRESHOLD) {
					areHeadings = false;
					break;
				}
				probabilities.add(probability);
			}
			if (!areHeadings) {
				continue;
			}
			if (singleChild) {
				ErrorCodes.addErrorCodeWithArguments(children.get(0), ErrorCodes.ERROR_CODE_1300);
			}
			for (int index = indexes.get(i); index < indexes.get(i + 1); index++) {
				INode child = children.get(index);
				SemanticTextNode textChild = textChildren.get(index);
				SemanticTextNode accumulatedHeadingChild;
				SemanticType type;
				if (child.getInitialSemanticType() == SemanticType.NUMBER_HEADING) {
					accumulatedHeadingChild = new SemanticNumberHeading(textChild);
					type = SemanticType.NUMBER_HEADING;
				} else {
					accumulatedHeadingChild = new SemanticHeading(textChild);
					type = SemanticType.HEADING;
				}
				textChildren.set(index, accumulatedHeadingChild);
				StaticContainers.getAccumulatedNodeMapper().updateNode(child, accumulatedHeadingChild,
						probabilities.get(index - indexes.get(i)) * child.getCorrectSemanticScore(), type);
			}
		}
	}

	private static INode getNextNonEmptyTextNode(INode node) {
		INode currentNode = node.getNextNode();
		while (currentNode != null) {
			INode accumulatedChild = StaticContainers.getAccumulatedNodeMapper().get(currentNode);
			if (accumulatedChild instanceof SemanticTextNode) {
				SemanticTextNode textNode = (SemanticTextNode)accumulatedChild;
				if (!textNode.isSpaceNode() && !textNode.isEmpty()) {
					break;
				}
			}
			currentNode = currentNode.getNextNode();
		}
		if (currentNode == null) {
			return null;
		}
		while (!currentNode.getChildren().isEmpty()) {
			for (INode child : currentNode.getChildren()) {
				INode accumulatedChild = StaticContainers.getAccumulatedNodeMapper().get(child);
				if (accumulatedChild instanceof SemanticTextNode) {
					SemanticTextNode textNode = (SemanticTextNode) accumulatedChild;
					if (!textNode.isSpaceNode() && !textNode.isEmpty()) {
						if (child.getCorrectSemanticScore() > MERGE_PROBABILITY_THRESHOLD) {
							return child;
						} else {
							currentNode = child;
							break;
						}
					}
				}
			}
		}
		return currentNode;
	}

	private static List<Integer> getIndexes(List<SemanticTextNode> textChildren) {
		List<Integer> indexes = new LinkedList<>();
		indexes.add(0);
		for (int index = 1; index < textChildren.size(); index++) {
			if (!NodeUtils.hasSameStyle(textChildren.get(index - 1), textChildren.get(index),
					NodeUtils.HEADING_EPSILONS[0], NodeUtils.HEADING_EPSILONS[0])) {
				indexes.add(index);
			}
		}
		return indexes;
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
				} else if (accumulatedChild instanceof SemanticFigure) {
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
		SemanticFigure figure = (SemanticFigure)StaticContainers.getAccumulatedNodeMapper().get(imageNode);
		if (figure.getImages().isEmpty()) {
			return;
		}
		double previousCaptionProbability = CaptionUtils.imageCaptionProbability(previousNode, figure);
		double nextCaptionProbability = CaptionUtils.imageCaptionProbability(nextNode, figure);
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

	@Override
	public WCAGProgressStatus getWCAGProgressStatus() {
		return WCAGProgressStatus.HEADING_AND_CAPTION_DETECTION;
	}
}
