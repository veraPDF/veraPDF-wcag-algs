package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticHeading;
import org.verapdf.wcag.algorithms.entities.SemanticTextNode;
import org.verapdf.wcag.algorithms.entities.SemanticImageNode;
import org.verapdf.wcag.algorithms.entities.content.LineChunk;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.ArabicNumbersListLabelsDetectionAlgorithm;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.ListLabelsDetectionAlgorithm;

import java.util.Arrays;

public class NodeUtils {

	private static final double FLOATING_POINT_OPERATIONS_EPS = 1e-7;
	private static final double[] DEFAULT_INTERVAL_BEFORE_IMAGE = {0, 1.75};
	private static final double[] DEFAULT_INTERVAL_AFTER_IMAGE = {0, 1.8};
	private static final double IMAGE_INTERVAL_STANDARD = 1;
	public static final double EPSILON = 0.0001;
	public static final double TABLE_BORDER_EPSILON = 0.011;
	public static final double[] UNDERLINED_TEXT_EPSILONS = {0.08, 0.3, 0.3, 0.3};
	private static final double WITH_TOLERANCE_FACTOR = 0.33;
	private static final double[] HEADING_PROBABILITY_PARAMS = {0.3, 0.0291, 0.15, 0.27, 0.1, 0.25, 0.2, 0.5, 0.05};
	private static final double[] HEADING_PROBABILITY_PARAMS_SAME_FONT = {0.55, 0.15, 0.55, 0.4, 0.5, 0.15};
	private static final double[] HEADING_PROBABILITY_PARAMS_DIFF_FONT = {0.44, 0.1, 0.4, 0.23, 0.35, 0.1};
	private static final double[] HEADING_EPSILONS = {0.05, 0.08};
	private static final double[] CAPTION_PROBABILITY_PARAMS = {1.0, 0.95, 0.9, 0.85, 0.2, 0.1, 0.03};
	public static final String FIGURE = "Figure";

	public static double headingProbability(INode node, INode previousNode, INode nextNode, INode nextNextNode, SemanticType initialSemanticType) {
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
				double probabilityNextNode = headingProbability(textNode, nextNode);
				if (areCloseNumbers(probabilityNextNode, 0.0) && nextNextNode != null) {
					headingProbability += headingProbability(textNode, nextNextNode);
				} else {
					headingProbability += probabilityNextNode;
				}
			}
		} else if (nextNode == null) {
			headingProbability += headingProbability(textNode, previousNode);
		} else {
			double probability = headingProbability(textNode, nextNode);
			if (areCloseNumbers(probability, 0.0)) {
				if (nextNextNode != null) {
					headingProbability += Math.min(headingProbability(textNode, previousNode),
					                               headingProbability(textNode, nextNextNode));
				} else {
					headingProbability += headingProbability(textNode, previousNode);
				}
			} else {
				headingProbability += Math.min(headingProbability(textNode, previousNode), probability);
			}
		}
		if (textNode.hasFullLines()) {
			headingProbability += HEADING_PROBABILITY_PARAMS[0];
		} else if (!textNode.isEmpty() && textNode.getFirstLine().isLineStart()) {
			headingProbability += HEADING_PROBABILITY_PARAMS[8];
		}
		if (textNode.isStartsWithArabicNumber()) {
			headingProbability += HEADING_PROBABILITY_PARAMS[2];
		}
		if (SemanticType.HEADING.equals(initialSemanticType) || SemanticType.NUMBER_HEADING.equals(initialSemanticType)) {
			headingProbability += HEADING_PROBABILITY_PARAMS[3];
		}
		if (nextNode != null && !node.getPageNumber().equals(nextNode.getPageNumber())) {
			headingProbability -= HEADING_PROBABILITY_PARAMS[7];
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
		double probability;
		if (textNode.getFontName().equals(neighborTextNode.getFontName())) {
			probability = headingProbability(textNode, neighborTextNode, HEADING_PROBABILITY_PARAMS_SAME_FONT,
			                                 HEADING_EPSILONS[0], HEADING_EPSILONS[0]);
		} else {
			probability = headingProbability(textNode, neighborTextNode, HEADING_PROBABILITY_PARAMS_DIFF_FONT,
			                                 HEADING_EPSILONS[0], HEADING_EPSILONS[1]);
		}
		if (!Arrays.equals(textNode.getTextColor(), neighborTextNode.getTextColor())) {
			probability += HEADING_PROBABILITY_PARAMS[4];
		}
		if (isUpperCaseString(textNode.getValue()) && !isUpperCaseString(neighborTextNode.getValue())) {
			probability += HEADING_PROBABILITY_PARAMS[5];
		} else if (!isUpperCaseString(textNode.getValue()) && isUpperCaseString(neighborTextNode.getValue())) {
			probability -= HEADING_PROBABILITY_PARAMS[6];
		}
		return probability;
	}

	private static double headingProbability(SemanticTextNode textNode, SemanticTextNode neighborTextNode, double[] params,
	                                         double weightEps, double fontEps) {
		double probability = 0.0;
		if (textNode.getFontWeight() > neighborTextNode.getFontWeight() + weightEps) {
			probability += params[0];
		} else if (neighborTextNode.getFontWeight() > textNode.getFontWeight() + weightEps) {
			probability -= params[1];
		}
		if (textNode.getFontSize() > neighborTextNode.getMaxFontSize() + fontEps) {
			probability += params[2];
		} else if (neighborTextNode.getFontSize() > textNode.getMaxFontSize() + fontEps) {
			probability -= params[3];
		} else if (textNode.getFontSize() > neighborTextNode.getFontSize() + fontEps) {
			probability += params[4];
		} else if (neighborTextNode.getFontSize() > textNode.getFontSize() + fontEps) {
			probability -= params[5];
		}
		return probability;
	}

	private static double getLinesNumberHeadingProbability(SemanticTextNode textNode) {
		return Math.max(0, 1 - HEADING_PROBABILITY_PARAMS[1] *
				(textNode.getLinesNumber() - 1) * (textNode.getLinesNumber() - 1));
	}

	public static double imageCaptionProbability(INode node, SemanticImageNode imageNode) {
		if (node == null || node.getSemanticType() == SemanticType.HEADING || node.getSemanticType() == SemanticType.NUMBER_HEADING) {
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
		captionProbability += captionContentProbability(textNode, FIGURE);
		return Math.min(captionProbability, 1.0);
	}

	public static double tableCaptionProbability(INode node, BoundingBox tableBoundingBox) {
		if (node == null) {
			return 0.0;
		}
		if (!(node instanceof SemanticTextNode)) {
			return 0.0;
		}
		SemanticTextNode textNode = (SemanticTextNode) node;
		double captionProbability = NodeUtils.captionVerticalProbability(textNode, tableBoundingBox);
		captionProbability *= NodeUtils.captionHorizontalProbability(textNode, tableBoundingBox);
		captionProbability *= NodeUtils.getLinesNumberCaptionProbability(textNode);
		captionProbability += NodeUtils.captionContentProbability(textNode, SemanticType.TABLE.getValue());
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

	public static boolean areOverlapping(TextChunk textChunk, LineChunk lineChunk) {
		return Math.min(lineChunk.getRightX() - textChunk.getLeftX(), textChunk.getRightX() - lineChunk.getLeftX()) >
				UNDERLINED_TEXT_EPSILONS[0] * textChunk.getBoundingBox().getWidth();
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
					(lastBaseline - imageBoundingBox.getTopY()) / textNode.getFontSize(), IMAGE_INTERVAL_STANDARD);
		}
		if (firstBaseline < imageBoundingBox.getBottomY() - FLOATING_POINT_OPERATIONS_EPS) {
			return ChunksMergeUtils.getUniformProbability(DEFAULT_INTERVAL_AFTER_IMAGE,
					(imageBoundingBox.getBottomY() - firstBaseline) / textNode.getFontSize(), IMAGE_INTERVAL_STANDARD);
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

	private static boolean isUpperCaseString(String str) {
		int nonLetters = 0;
		for (char character : str.toCharArray()) {
			if (!Character.isLetter(character)) {
				nonLetters++;
				continue;
			}
			if (!Character.isUpperCase(character)) {
				return false;
			}
		}
		return nonLetters != str.length();
	}

	public static boolean areCloseNumbers(double d1, double d2, double epsilon) {
		return Math.abs(d1 - d2) <= epsilon;
	}

	public static boolean areCloseNumbers(double d1, double d2) {
		return areCloseNumbers(d1, d2, EPSILON);
	}
}
