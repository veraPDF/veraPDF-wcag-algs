package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticHeading;
import org.verapdf.wcag.algorithms.entities.SemanticTextNode;
import org.verapdf.wcag.algorithms.entities.SemanticImageNode;

public class NodeUtils {

	private static final double FLOATING_POINT_OPERATIONS_EPS = 1e-7;
	private static final double[] DEFAULT_INTERVAL_BEFORE_IMAGE = {0, 1.75};
	private static final double[] DEFAULT_INTERVAL_AFTER_IMAGE = {0, 1.8};
	private static final double IMAGE_INTERVAL_STANDARD = 1;
	private static final double WITH_TOLERANCE_FACTOR = 0.33;
	private static final double[] HEADING_PROBABILITY_PARAMS = {0.55, 0.55, 0.3, 0.0291};
	private static final double[] CAPTION_PROBABILITY_PARAMS = {1.0, 0.95, 0.9, 0.85};

	public static double headingProbability(INode node, INode previousNode, INode nextNode) {
		if (previousNode == null || previousNode instanceof SemanticHeading) {
			if (nextNode == null) {
				return 0.0;
			}
			return headingProbability(node, nextNode);
		}
		if (nextNode == null) {
			return headingProbability(node, previousNode);
		}
		return Math.min(headingProbability(node, previousNode), headingProbability(node, nextNode));
	}

	public static double headingProbability(INode node, INode neighborNode) {
		if (node == null) {
			return 0.0;
		}
		if (neighborNode == null) {
			return 1.0;
		}
		if (!(node instanceof SemanticTextNode)) {
			return 0.0;
		}
		if (!(neighborNode instanceof SemanticTextNode)) {
			return 0.0;
		}
		SemanticTextNode textNode = (SemanticTextNode) node;
		if (textNode.isEmpty()) {
			return 0.0;
		}
		SemanticTextNode neighborTextNode = (SemanticTextNode) neighborNode;
		double probability = 0.0;
		if (textNode.getFontWeight() > neighborTextNode.getFontWeight() + FLOATING_POINT_OPERATIONS_EPS) {
			probability += HEADING_PROBABILITY_PARAMS[0];
		}
		if (textNode.getFontSize() > neighborTextNode.getFontSize() + FLOATING_POINT_OPERATIONS_EPS) {
			probability += HEADING_PROBABILITY_PARAMS[1];
		}
		if (textNode.hasFullLines()) {
			probability += HEADING_PROBABILITY_PARAMS[2];
		}
		return Math.min(probability * getLineSizeHeadingProbability(textNode), 1.0);
	}

	private static double getLineSizeHeadingProbability(SemanticTextNode textNode) {
		return Math.max(0, 1 - HEADING_PROBABILITY_PARAMS[3] * (textNode.getLinesNumber() - 1) * (textNode.getLinesNumber() - 1));
	}

	public static double captionProbability(INode node, INode neighborNode) {
		if (node == null) {
			return 0.0;
		}
		if (neighborNode == null) {
			return 1.0;
		}
		if (!(node instanceof SemanticTextNode)) {
			return 0.0;
		}
		if (!(neighborNode instanceof SemanticImageNode)) {
			return 0.0;
		}
		SemanticTextNode textNode = (SemanticTextNode) node;
		SemanticImageNode neighborImageNode = (SemanticImageNode) neighborNode;
		double captionProbability = captionVerticalProbability(textNode, neighborImageNode);
		captionProbability *= captionHorizontalProbability(textNode, neighborImageNode);
		return captionProbability;
	}

	private static boolean isContaining(SemanticTextNode textNode, SemanticImageNode imageNode) {
		double tol = WITH_TOLERANCE_FACTOR * textNode.getFontSize();
		return (imageNode.getLeftX() + tol > textNode.getLeftX() && imageNode.getRightX() < textNode.getRightX() + tol);
	}

	private static boolean isContaining(SemanticImageNode imageNode, SemanticTextNode textNode) {
		double tol = WITH_TOLERANCE_FACTOR * textNode.getFontSize();
		return (textNode.getLeftX() + tol > imageNode.getLeftX() && textNode.getRightX() < imageNode.getRightX() + tol);
	}

	private static boolean areStrongCenterOverlapping(SemanticTextNode textNode, SemanticImageNode imageNode) {
		double tol = WITH_TOLERANCE_FACTOR * textNode.getFontSize();
		double textCenter = textNode.getBoundingBox().getCenterX();
		double imageCenter = imageNode.getBoundingBox().getCenterX();

		if (textCenter + tol > imageNode.getRightX() || textCenter < imageNode.getLeftX() + tol) {
			return false;
		}
		if (imageCenter + tol > textNode.getRightX() || imageCenter < textNode.getLeftX() + tol) {
			return false;
		}
		return true;
	}

	private static boolean areCenterOverlapping(SemanticTextNode textNode, SemanticImageNode imageNode) {
		double tol = WITH_TOLERANCE_FACTOR * textNode.getFontSize();
		double textCenter = textNode.getBoundingBox().getCenterX();
		double imageCenter = imageNode.getBoundingBox().getCenterX();

		if (textCenter + tol < imageNode.getRightX() && textCenter > imageNode.getLeftX() + tol) {
			return true;
		}
		if (imageCenter + tol < textNode.getRightX() && imageCenter > textNode.getLeftX() + tol) {
			return true;
		}
		return false;
	}

	private static boolean areOverlapping(SemanticTextNode textNode, SemanticImageNode imageNode) {
		double tol = WITH_TOLERANCE_FACTOR * textNode.getFontSize();
		return (textNode.getLeftX() + tol < imageNode.getRightX() && imageNode.getLeftX() + tol < textNode.getRightX());
	}

	private static boolean areOverlapping(SemanticImageNode imageNode, SemanticTextNode textNode) {
		double tol = WITH_TOLERANCE_FACTOR * textNode.getFontSize();
		return (imageNode.getLeftX() + tol < textNode.getRightX() && textNode.getLeftX() + tol < imageNode.getRightX());
	}

	private static double captionVerticalProbability(SemanticTextNode textNode, SemanticImageNode imageNode) {
		if (textNode.getLastPageNumber() == null || imageNode.getPageNumber() == null ||
		    textNode.getPageNumber() == null || imageNode.getLastPageNumber() == null) {
			return 0.0;
		}
		if (!textNode.getPageNumber().equals(textNode.getLastPageNumber()) ||
		    !textNode.getPageNumber().equals(imageNode.getPageNumber())) {
			return 0.0;
		}
		double firstBaseline = textNode.getFirstBaseline();
		double lastBaseline = textNode.getLastBaseline();
		if (lastBaseline > imageNode.getTopY() + FLOATING_POINT_OPERATIONS_EPS) {
			return ChunksMergeUtils.getUniformProbability(DEFAULT_INTERVAL_BEFORE_IMAGE, (lastBaseline - imageNode.getTopY()) / textNode.getFontSize(),
			                                              IMAGE_INTERVAL_STANDARD);
		}
		if (firstBaseline < imageNode.getBottomY() - FLOATING_POINT_OPERATIONS_EPS) {
			return ChunksMergeUtils.getUniformProbability(DEFAULT_INTERVAL_AFTER_IMAGE, (imageNode.getBottomY() - firstBaseline) / textNode.getFontSize(),
			                                              IMAGE_INTERVAL_STANDARD);
		}
		return 0.0;
	}

	private static double captionHorizontalProbability(SemanticTextNode textNode, SemanticImageNode imageNode) {
		if (isContaining(imageNode, textNode) && areStrongCenterOverlapping(textNode, imageNode)) {
			return CAPTION_PROBABILITY_PARAMS[0];
		}
		if (isContaining(imageNode, textNode) && areCenterOverlapping(textNode, imageNode)) {
			return CAPTION_PROBABILITY_PARAMS[1];
		}
		if (isContaining(textNode, imageNode) && areStrongCenterOverlapping(textNode, imageNode)) {
			return CAPTION_PROBABILITY_PARAMS[2];
		}
		if (isContaining(textNode, imageNode) && areCenterOverlapping(textNode, imageNode)) {
			return CAPTION_PROBABILITY_PARAMS[3];
		}
		return 0.0;
	}

}
