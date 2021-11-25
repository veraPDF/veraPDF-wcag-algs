package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticHeading;
import org.verapdf.wcag.algorithms.entities.SemanticTextNode;
import org.verapdf.wcag.algorithms.entities.SemanticImageNode;
import org.verapdf.wcag.algorithms.entities.content.LineChunk;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.ArabicNumbersListLabelsDetectionAlgorithm;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.ListLabelsDetectionAlgorithm;

public class NodeUtils {

	private static final double FLOATING_POINT_OPERATIONS_EPS = 1e-7;
	private static final double[] DEFAULT_INTERVAL_BEFORE_IMAGE = {0, 1.75};
	private static final double[] DEFAULT_INTERVAL_AFTER_IMAGE = {0, 1.8};
	private static final double IMAGE_INTERVAL_STANDARD = 1;
	private static final double EPSILON = 0.0001;
	private static final double WITH_TOLERANCE_FACTOR = 0.33;
	private static final double[] HEADING_PROBABILITY_PARAMS = {0.55, 0.55, 0.3, 0.0291, 0.15, 0.15, 0.1, 0.1};
	private static final double[] CAPTION_PROBABILITY_PARAMS = {1.0, 0.95, 0.9, 0.85, 0.2, 0.1, 0.03};
	public static final String FIGURE = "Figure";

	public static double headingProbability(INode node, INode previousNode, INode nextNode, SemanticType initialSemanticType) {
		if (node == null) {
			return 0.0;
		}
		if (!(node instanceof SemanticTextNode)) {
			return 0.0;
		}
		SemanticTextNode textNode = (SemanticTextNode) node;
		if (textNode.isEmpty()) {
			return 0.0;
		}
		double headingProbability = 0.0;
		if (previousNode == null || previousNode instanceof SemanticHeading) {
			if (nextNode != null) {
				headingProbability += headingProbability(textNode, nextNode);
			}
		} else if (nextNode == null) {
			headingProbability += headingProbability(textNode, previousNode);
		} else {
			double probability = headingProbability(textNode, nextNode);
			if (areCloseNumbers(probability, 0.0)) {
				headingProbability += headingProbability(textNode, previousNode);
			} else {
				headingProbability += Math.min(headingProbability(textNode, previousNode), probability);
			}
		}
		if (textNode.hasFullLines()) {
			headingProbability += HEADING_PROBABILITY_PARAMS[2];
		}
		if (textNode.isStartsWithArabicNumber()) {
			headingProbability += HEADING_PROBABILITY_PARAMS[6];
		}
		if (SemanticType.HEADING.equals(initialSemanticType) || SemanticType.NUMBER_HEADING.equals(initialSemanticType)) {
			headingProbability += HEADING_PROBABILITY_PARAMS[7];
		}

		return Math.max(Math.min(headingProbability * getLinesNumberHeadingProbability(textNode), 1.0), 0.0);
	}

	public static double headingProbability(SemanticTextNode textNode, INode neighborNode) {
		if (neighborNode == null) {
			return 1.0;
		}
		if (!(neighborNode instanceof SemanticTextNode)) {
			return 0.0;
		}
		SemanticTextNode neighborTextNode = (SemanticTextNode) neighborNode;
		double probability = 0.0;
		if (textNode.getFontWeight() > neighborTextNode.getFontWeight() + FLOATING_POINT_OPERATIONS_EPS) {
			probability += HEADING_PROBABILITY_PARAMS[0];
		} else if (neighborTextNode.getFontWeight() > textNode.getFontWeight() + FLOATING_POINT_OPERATIONS_EPS) {
			probability -= HEADING_PROBABILITY_PARAMS[4];
		}
		if (textNode.getFontSize() > neighborTextNode.getFontSize() + FLOATING_POINT_OPERATIONS_EPS) {
			probability += HEADING_PROBABILITY_PARAMS[1];
		} else if (neighborTextNode.getFontSize() > textNode.getFontSize() + FLOATING_POINT_OPERATIONS_EPS) {
			probability -= HEADING_PROBABILITY_PARAMS[5];
		}
		return probability;
	}

	private static double getLinesNumberHeadingProbability(SemanticTextNode textNode) {
		return Math.max(0, 1 - HEADING_PROBABILITY_PARAMS[3] *
				(textNode.getLinesNumber() - 1) * (textNode.getLinesNumber() - 1));
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
		captionProbability *= getLinesNumberCaptionProbability(textNode);
		captionProbability += captionContentProbability(textNode);
		return Math.min(captionProbability, 1.0);
	}

	private static double getLinesNumberCaptionProbability(SemanticTextNode textNode) {
		return Math.max(0, 1 - CAPTION_PROBABILITY_PARAMS[6] *
				(textNode.getLinesNumber() - 1) * (textNode.getLinesNumber() - 1));
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

	public static boolean areOverlapping(TextChunk textChunk, LineChunk lineChunk) {
		return ((textChunk.getLeftX() < lineChunk.getRightX()) &&
				(lineChunk.getLeftX() < textChunk.getRightX()));
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
			return ChunksMergeUtils.getUniformProbability(DEFAULT_INTERVAL_BEFORE_IMAGE,
					(lastBaseline - imageNode.getTopY()) / textNode.getFontSize(), IMAGE_INTERVAL_STANDARD);
		}
		if (firstBaseline < imageNode.getBottomY() - FLOATING_POINT_OPERATIONS_EPS) {
			return ChunksMergeUtils.getUniformProbability(DEFAULT_INTERVAL_AFTER_IMAGE,
					(imageNode.getBottomY() - firstBaseline) / textNode.getFontSize(), IMAGE_INTERVAL_STANDARD);
		}
		return 0.0;
	}

	private static double captionContentProbability(SemanticTextNode textNode) {
		String value = textNode.getFirstLine().getValue().trim();
		if (value.startsWith(FIGURE)) {
			value = value.substring(FIGURE.length()).trim();
			if (!value.isEmpty() && ListLabelsDetectionAlgorithm.getRegexStartLength(value,
					ArabicNumbersListLabelsDetectionAlgorithm.ARABIC_NUMBER_REGEX) > 0) {
				return CAPTION_PROBABILITY_PARAMS[4];
			}
			return CAPTION_PROBABILITY_PARAMS[5];
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

	public static boolean areCloseNumbers(double d1, double d2, double epsilon) {
		return Math.abs(d1 - d2) < epsilon;
	}

	public static boolean areCloseNumbers(double d1, double d2) {
		return areCloseNumbers(d1, d2, EPSILON);
	}
}
