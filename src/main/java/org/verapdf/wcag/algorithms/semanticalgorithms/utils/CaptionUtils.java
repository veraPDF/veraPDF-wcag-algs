package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticImageNode;
import org.verapdf.wcag.algorithms.entities.SemanticTextNode;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.ArabicNumbersListLabelsDetectionAlgorithm;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.ListLabelsDetectionAlgorithm;

public class CaptionUtils {

	private static final double FLOATING_POINT_OPERATIONS_EPS = 1e-7;
	private static final double[] DEFAULT_INTERVAL_BEFORE_IMAGE = {0, 1.8};
	private static final double[] DEFAULT_INTERVAL_AFTER_IMAGE = {0, 1.8};
	private static final double IMAGE_INTERVAL_STANDARD = 1;
	private static final double[] CAPTION_PROBABILITY_PARAMS = {1.0, 0.95, 0.9, 0.85, 0.2, 0.1, 0.03};
	private static final double WITH_TOLERANCE_FACTOR = 0.33;

	public static double imageCaptionProbability(INode node, SemanticImageNode imageNode) {
		if (node == null) {
			return 0;
		}
		if (node.getSemanticType() == SemanticType.HEADING ||
		    node.getSemanticType() == SemanticType.NUMBER_HEADING ||
		    node.getSemanticType() == SemanticType.LIST) {
			return 0;
		}
		INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(node);
		if (!(accumulatedNode instanceof SemanticTextNode)) {
			return 0.0;
		}
		SemanticTextNode textNode = (SemanticTextNode) accumulatedNode;
		double captionProbability = captionVerticalProbability(textNode, imageNode.getBoundingBox());
		captionProbability *= captionHorizontalProbability(textNode, imageNode.getBoundingBox());
		captionProbability *= getLinesNumberCaptionProbability(textNode);
		captionProbability += captionContentProbability(textNode, SemanticType.FIGURE.getValue());
		return Math.min(captionProbability, 1.0);
	}

	public static double tableCaptionProbability(INode node, BoundingBox tableBoundingBox) {
		if (node == null) {
			return 0.0;
		}
		if (node.getSemanticType() == SemanticType.HEADING ||
				node.getSemanticType() == SemanticType.NUMBER_HEADING ||
				node.getSemanticType() == SemanticType.LIST) {
			return 0.0;
		}
		INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(node);
		if (accumulatedNode == null) {
			return 0.0;
		}
		if (!(accumulatedNode instanceof SemanticTextNode)) {
			return 0.0;
		}
		SemanticTextNode textNode = (SemanticTextNode) accumulatedNode;
		double captionProbability = captionVerticalProbability(textNode, tableBoundingBox);
		captionProbability *= captionHorizontalProbability(textNode, tableBoundingBox);
		captionProbability *= getLinesNumberCaptionProbability(textNode);
		captionProbability += captionContentProbability(textNode, SemanticType.TABLE.getValue());
		return Math.min(captionProbability, 1.0);
	}

	private static double getLinesNumberCaptionProbability(SemanticTextNode textNode) {
		return Math.max(0, 1 - CAPTION_PROBABILITY_PARAMS[6] *
				(textNode.getLinesNumber() - 1) * (textNode.getLinesNumber() - 1));
	}

	private static boolean isContaining(SemanticTextNode textNode, BoundingBox imageBoundingBox) {
		double tol = WITH_TOLERANCE_FACTOR * textNode.getFontSize();
		return (imageBoundingBox.getLeftX() + tol > textNode.getLeftX() &&
				imageBoundingBox.getRightX() < textNode.getRightX() + tol);
	}

	private static boolean isContaining(BoundingBox imageBoundingBox, SemanticTextNode textNode) {
		double tol = WITH_TOLERANCE_FACTOR * textNode.getFontSize();
		return (textNode.getLeftX() + tol > imageBoundingBox.getLeftX() &&
				textNode.getRightX() < imageBoundingBox.getRightX() + tol);
	}

	private static boolean areStrongCenterOverlapping(SemanticTextNode textNode, BoundingBox imageBoundingBox) {
		double tol = WITH_TOLERANCE_FACTOR * textNode.getFontSize();
		double textCenter = textNode.getBoundingBox().getCenterX();
		double imageCenter = imageBoundingBox.getCenterX();

		if (textCenter + tol > imageBoundingBox.getRightX() || textCenter < imageBoundingBox.getLeftX() + tol) {
			return false;
		}
		if (imageCenter + tol > textNode.getRightX() || imageCenter < textNode.getLeftX() + tol) {
			return false;
		}
		return true;
	}

	private static boolean areCenterOverlapping(SemanticTextNode textNode, BoundingBox imageBoundingBox) {
		double tol = WITH_TOLERANCE_FACTOR * textNode.getFontSize();
		double textCenter = textNode.getBoundingBox().getCenterX();
		double imageCenter = imageBoundingBox.getCenterX();

		if (textCenter + tol < imageBoundingBox.getRightX() && textCenter > imageBoundingBox.getLeftX() + tol) {
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

	private static double captionVerticalProbability(SemanticTextNode textNode, BoundingBox imageBoundingBox) {
		if (textNode.getLastPageNumber() == null || imageBoundingBox.getPageNumber() == null ||
				textNode.getPageNumber() == null || imageBoundingBox.getLastPageNumber() == null) {
			return 0.0;
		}
		if (!textNode.getPageNumber().equals(textNode.getLastPageNumber()) ||
				!textNode.getPageNumber().equals(imageBoundingBox.getPageNumber())) {
			return 0.0;
		}
		double firstBaseline = textNode.getFirstBaseline();
		double lastBaseline = textNode.getLastBaseline();
		if (lastBaseline > imageBoundingBox.getTopY() + FLOATING_POINT_OPERATIONS_EPS) {
			return ChunksMergeUtils.getUniformProbability(DEFAULT_INTERVAL_BEFORE_IMAGE,
					(textNode.getBottomY() - imageBoundingBox.getTopY()) / textNode.getFontSize(), IMAGE_INTERVAL_STANDARD);
		}
		if (firstBaseline < imageBoundingBox.getBottomY() - FLOATING_POINT_OPERATIONS_EPS) {
			return ChunksMergeUtils.getUniformProbability(DEFAULT_INTERVAL_AFTER_IMAGE,
					(imageBoundingBox.getBottomY() - textNode.getTopY()) / textNode.getFontSize(), IMAGE_INTERVAL_STANDARD);
		}
		return 0.0;
	}

	private static double captionContentProbability(SemanticTextNode textNode, String prefix) {
		String value = textNode.getFirstLine().getValue().trim();
		if (value.startsWith(prefix)) {
			value = value.substring(prefix.length()).trim();
			if (!value.isEmpty() && ListLabelsDetectionAlgorithm.getRegexStartLength(value,
					ArabicNumbersListLabelsDetectionAlgorithm.ARABIC_NUMBER_REGEX) > 0) {
				return CAPTION_PROBABILITY_PARAMS[4];
			}
			return CAPTION_PROBABILITY_PARAMS[5];
		}
		return 0.0;
	}

	private static double captionHorizontalProbability(SemanticTextNode textNode, BoundingBox imageBoundingBox) {
		if (isContaining(imageBoundingBox, textNode) && areStrongCenterOverlapping(textNode, imageBoundingBox)) {
			return CAPTION_PROBABILITY_PARAMS[0];
		}
		if (isContaining(imageBoundingBox, textNode) && areCenterOverlapping(textNode, imageBoundingBox)) {
			return CAPTION_PROBABILITY_PARAMS[1];
		}
		if (isContaining(textNode, imageBoundingBox) && areStrongCenterOverlapping(textNode, imageBoundingBox)) {
			return CAPTION_PROBABILITY_PARAMS[2];
		}
		if (isContaining(textNode, imageBoundingBox) && areCenterOverlapping(textNode, imageBoundingBox)) {
			return CAPTION_PROBABILITY_PARAMS[3];
		}
		return 0.0;
	}

}
