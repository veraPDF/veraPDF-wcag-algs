package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticHeading;
import org.verapdf.wcag.algorithms.entities.SemanticTextNode;
import org.verapdf.wcag.algorithms.entities.content.LineChunk;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;

import java.awt.*;
import java.util.Arrays;

public class NodeUtils {

	public static final double EPSILON = 0.0001;
	public static final double TABLE_BORDER_EPSILON = 0.011;
	public static final double[] UNDERLINED_TEXT_EPSILONS = {0.08, 0.3, 0.3, 0.3};
	private static final double[] HEADING_PROBABILITY_PARAMS = {0.3, 0.0291, 0.15, 0.27, 0.1, 0.25, 0.2, 0.5, 0.05, 0.1};
	private static final double[] HEADING_PROBABILITY_PARAMS_SAME_FONT = {0.55, 0.15, 0.55, 0.4, 0.5, 0.15};
	private static final double[] HEADING_PROBABILITY_PARAMS_DIFF_FONT = {0.44, 0.1, 0.4, 0.23, 0.35, 0.1};
	public static final double[] HEADING_EPSILONS = {0.05, 0.08};

	public static final double BACKGROUND_FIRST_COLOR_EPSILON = 0.03;
	public static final double BACKGROUND_SECOND_COLOR_EPSILON = BACKGROUND_FIRST_COLOR_EPSILON * 255;
	public static final double MIN_GOOD_HEADING_PROBABILITY = 0.9;

	public static double headingProbability(SemanticTextNode textNode, SemanticTextNode previousNode, SemanticTextNode nextNode,
											INode initialNode) {
		double headingProbability = 0.0;
		if (previousNode instanceof SemanticHeading) {
			if (nextNode == null) {
				return 0.0;
			}
			headingProbability += headingProbability(textNode, nextNode);
		} else {
			headingProbability += Math.min(headingProbability(textNode, previousNode), headingProbability(textNode, nextNode));
		}
		if (textNode.hasFullLines()) {
			headingProbability += HEADING_PROBABILITY_PARAMS[0];
		} else if (textNode.getFirstLine().isLineStart()) {
			headingProbability += HEADING_PROBABILITY_PARAMS[8];
		} else if (!textNode.getFirstLine().isLineStart() && !textNode.getLastLine().isLineEnd()) {
			headingProbability -= HEADING_PROBABILITY_PARAMS[9];
		}
		if (textNode.isStartsWithArabicNumber()) {
			headingProbability += HEADING_PROBABILITY_PARAMS[2];
		}
		if (HeadingUtils.isInitialHeadingNode(initialNode)) {
			headingProbability += HEADING_PROBABILITY_PARAMS[3];
		}
		INode nextNeighbor = getNextNonEmptyNode(initialNode);
		if (nextNeighbor != null && initialNode.getPageNumber() != null && !initialNode.getPageNumber().equals(nextNeighbor.getPageNumber())) {
			headingProbability -= HEADING_PROBABILITY_PARAMS[7];
		}
		return Math.max(Math.min(headingProbability * getLinesNumberHeadingProbability(textNode), 1.0), 0.0);
	}

	public static INode getNextNonEmptyNode(INode node) {
		INode currentNode = node.getNextNode();
		while (currentNode != null) {
			if (currentNode.getPageNumber() != null) {
				return currentNode;
			}
			currentNode = currentNode.getNextNode();
		}
		return null;
	}

	public static double headingProbability(SemanticTextNode textNode, INode neighborNode) {
		if (neighborNode == null) {
			return 1.0;
		}
		SemanticTextNode neighborTextNode = (SemanticTextNode) neighborNode;
		double probability;
		if (textNode.getFontName() != null && textNode.getFontName().equals(neighborTextNode.getFontName())) {
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

	public static boolean hasSameStyle(SemanticTextNode textNode, SemanticTextNode neighborTextNode,
											 double weightEps, double fontEps) {
		if (!NodeUtils.areCloseNumbers(textNode.getFontWeight(), neighborTextNode.getFontWeight(), weightEps)) {
			return false;
		}
		if (!NodeUtils.areCloseNumbers(textNode.getFontSize(), neighborTextNode.getFontSize(), fontEps)) {
			return false;
		}
		if (!NodeUtils.areCloseNumbers(textNode.getMaxFontSize(), neighborTextNode.getMaxFontSize(), fontEps)) {
			return false;
		}
		if (isUpperCaseString(textNode.getValue()) != isUpperCaseString(neighborTextNode.getValue())) {
			return false;
		}
		return true;
	}

	private static double getLinesNumberHeadingProbability(SemanticTextNode textNode) {
		return Math.max(0, 1 - HEADING_PROBABILITY_PARAMS[1] *
				(textNode.getLinesNumber() - 1) * (textNode.getLinesNumber() - 1));
	}

	public static boolean areOverlapping(TextChunk textChunk, LineChunk lineChunk) {
		return Math.min(lineChunk.getRightX() - textChunk.getLeftX(), textChunk.getRightX() - lineChunk.getLeftX()) >
				UNDERLINED_TEXT_EPSILONS[0] * textChunk.getBoundingBox().getWidth();
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

	public static boolean hasSimilarBackgroundColor(double[] firstColor, double[] secondColor) {
		for (int i = 0; i < firstColor.length; i++) {
			if (Math.abs(firstColor[i] - secondColor[i]) > BACKGROUND_FIRST_COLOR_EPSILON) {
				return false;
			}
		}
		return true;
	}

	public static boolean hasSimilarBackgroundColor(Color firstColor, Color secondColor) {
		return Math.abs(firstColor.getRed() - secondColor.getRed()) < BACKGROUND_SECOND_COLOR_EPSILON &&
		       Math.abs(firstColor.getGreen() - secondColor.getGreen()) < BACKGROUND_SECOND_COLOR_EPSILON &&
		       Math.abs(firstColor.getBlue() - secondColor.getBlue()) < BACKGROUND_SECOND_COLOR_EPSILON;
	}
}
