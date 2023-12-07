package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.SemanticTextNode;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextColumn;
import org.verapdf.wcag.algorithms.entities.content.TextInfoChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.TextFormat;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ChunksMergeUtils {

	private static final double FLOATING_POINT_OPERATIONS_EPS = 1e-7;
	private static final double FONT_METRIC_UNIVERSAL_TEMPORARY_THRESHOLD = 0.1;
	private static final double FONT_SIZE_COMPARISON_THRESHOLD = 0.05;
	private static final double FONT_WHITESPACE_COMPARISON_THRESHOLD = 0.33;
	private static final double FONT_LEADING_INTERVAL_STANDARD = 1;
	private static final double[] DEFAULT_FONT_CHAR_SPACING_INTERVAL = {0, 0.67};
	private static final double[] DEFAULT_FONT_LEADING_INTERVAL = {0.7, 1.51};
	private static final double[] PART_FONT_LEADING_INTERVAL = {0.2, 1.5};

	private static final double TO_LINE_PROBABILITY_THRESHOLD = 0.75;
	private static final double[] NORMAL_LINE_PROBABILITY_PARAMS = {2, 0.033};
	private static final double[] NORMAL_LINE_PROBABILITY_PARAMS_2 = {0.5, 0.033};
	private static final double[] SUPERSCRIPT_PROBABILITY_PARAMS = {0.69438, 1.70575, 1.43819};
	private static final double[] SUBSCRIPT_PROBABILITY_PARAMS = {0.71932, 1.0483, 0.37555};
	private static final double[] COLUMNS_PROBABILITY_PARAMS = {0.75, 0.75};
	private static final double[] FONT_SIZE_DIFFERENCE_PARAMS = {0.95, 3.97};
	private static final double SUPERSCRIPT_BASELINE_THRESHOLD = 0.1;
	private static final double SUPERSCRIPT_FONTSIZE_THRESHOLD = 0.1;
	private static final double SUBSCRIPT_BASELINE_THRESHOLD = 0.08;
	private static final double SUBSCRIPT_FONTSIZE_THRESHOLD = 0.1;
	private static final double[] FOOTNOTE_PROBABILITY_PARAMS = {0.35, 0.5, 0.15, 0.4, 0.55, 0.08, 0.05};

	private ChunksMergeUtils() {
	}

	public static double toChunkMergeProbability(TextChunk x, TextChunk y) {
		double resultProbability = 1;

		resultProbability *= mergeByFontNameProbability(x, y);
		resultProbability *= mergeByFontSizeProbability(x, y);
		resultProbability *= mergeByFontColorProbability(x, y);
		resultProbability *= mergeByBaseLineProbability(x, y);
		resultProbability *= mergeByCharSpacingProbability(x, y);

		return resultProbability;
	}

	public static double getBaseLineDifference(TextInfoChunk x, TextInfoChunk y) {
		double baseLineDiff = x.getBaseLine() - y.getBaseLine();
		if (x.isRightLeftHorizontalText() || x.isBottomUpVerticalText()) {
			baseLineDiff = -baseLineDiff;
		}
		baseLineDiff /= Math.max(x.getFontSize(), y.getFontSize());
		return baseLineDiff;
	}

	public static double getCentersDifference(TextChunk x, TextChunk y) {
		double centersDiff = x.getCenterY() - y.getCenterY();
		if (x.isVerticalText()) {
			centersDiff = x.getCenterX() - y.getCenterX();
		}
		centersDiff /= Math.max(x.getFontSize(), y.getFontSize());
		return Math.abs(centersDiff);
	}

	public static double getFontSizeDifference(TextInfoChunk x, TextInfoChunk y) {
		double fontSizeDiff = x.getFontSize() - y.getFontSize();
		fontSizeDiff /= Math.max(x.getFontSize(), y.getFontSize());
		return fontSizeDiff;
	}

	public static double toLineMergeProbability(TextInfoChunk x, TextInfoChunk y) {
		double baseLineDiff = getBaseLineDifference(x, y);
		double fontSizeDiff = getFontSizeDifference(x, y);

		double charSpacingProbability = mergeByCharSpacingProbability(x, y);
		double resultProbability = charSpacingProbability *
				mergeNormalLineProbability(Math.abs(baseLineDiff), Math.abs(fontSizeDiff),
						NORMAL_LINE_PROBABILITY_PARAMS);

		if (resultProbability < TO_LINE_PROBABILITY_THRESHOLD &&
			charSpacingProbability > TO_LINE_PROBABILITY_THRESHOLD) {

			double superscriptProbability = charSpacingProbability;
			double subscriptProbability = charSpacingProbability;

			if ((fontSizeDiff > SUPERSCRIPT_FONTSIZE_THRESHOLD &&
					baseLineDiff < -SUPERSCRIPT_BASELINE_THRESHOLD) ||
					(fontSizeDiff < -SUPERSCRIPT_FONTSIZE_THRESHOLD &&
							baseLineDiff > SUPERSCRIPT_BASELINE_THRESHOLD)) {

				superscriptProbability *= toLineProbabilityFunction(Math.abs(baseLineDiff), Math.abs(fontSizeDiff), SUPERSCRIPT_PROBABILITY_PARAMS);
			} else {
				superscriptProbability = 0.0;
			}
			if ((fontSizeDiff > SUBSCRIPT_FONTSIZE_THRESHOLD &&
					baseLineDiff > SUBSCRIPT_BASELINE_THRESHOLD) ||
					(fontSizeDiff < -SUBSCRIPT_FONTSIZE_THRESHOLD &&
							baseLineDiff < -SUBSCRIPT_BASELINE_THRESHOLD)) {

				subscriptProbability *= toLineProbabilityFunction(Math.abs(baseLineDiff), Math.abs(fontSizeDiff), SUBSCRIPT_PROBABILITY_PARAMS);
			} else {
				subscriptProbability = 0.0;
			}

			return Math.max(resultProbability, Math.max(superscriptProbability, subscriptProbability));
		}

		return resultProbability;
	}

	public static double countOneLineProbability(SemanticTextNode secondNode, TextLine lastLine, TextLine nextLine) {
		double oneLineProbability;
		TextChunk x = lastLine.getLastTextChunk();
		TextChunk y = nextLine.getFirstTextChunk();

		if (!NodeUtils.areCloseNumbers(x.getSlantDegree(), y.getSlantDegree())) {
			return 0.0;
		}

		double baseLineDiff = getBaseLineDifference(x, y);
		double centersDiff = getCentersDifference(x, y);
		double fontSizeDiff = getFontSizeDifference(x, y);

		if (!TextFormat.NORMAL.equals(x.getTextFormat())) {
			TextChunk z = lastLine.getLastNormalTextChunk();
			if (z != null) {
				baseLineDiff = getBaseLineDifference(z, y);
				centersDiff = getCentersDifference(z, y);
				fontSizeDiff = getFontSizeDifference(z, y);
			}
		}

		double normalTextProbability = getNormalTextProbabilitySecondChunk(x, y, Math.abs(baseLineDiff) < centersDiff ?
		                                                                         baseLineDiff : centersDiff, fontSizeDiff);
		double superscriptProbability = getSuperscriptProbabilitySecondChunk(x, y, baseLineDiff, fontSizeDiff);
		double subscriptProbability = getSubscriptProbabilitySecondChunk(x, y, baseLineDiff, fontSizeDiff);

		if (Math.max(superscriptProbability, subscriptProbability) > normalTextProbability) {
			if (superscriptProbability > subscriptProbability) {
				oneLineProbability = superscriptProbability;
				secondNode.setTextFormat(TextFormat.SUPERSCRIPT);
			} else {
				oneLineProbability = subscriptProbability;
				secondNode.setTextFormat(TextFormat.SUBSCRIPT);
			}
		} else {
			double superscriptProbabilityFirst = getSuperscriptProbabilityFirstChunk(x, y, baseLineDiff, fontSizeDiff);
			double subscriptProbabilityFirst = getSubscriptProbabilityFirstChunk(x, y, baseLineDiff, fontSizeDiff);
			if (Math.max(superscriptProbabilityFirst, subscriptProbabilityFirst) > normalTextProbability) {
				if (superscriptProbabilityFirst > subscriptProbabilityFirst) {
					oneLineProbability = superscriptProbabilityFirst;
					x.setTextFormat(TextFormat.SUPERSCRIPT);
				} else {
					oneLineProbability = subscriptProbabilityFirst;
					x.setTextFormat(TextFormat.SUBSCRIPT);
				}
			} else {
				oneLineProbability = normalTextProbability;
				secondNode.setTextFormat(TextFormat.NORMAL);
			}
		}
		return oneLineProbability;
	}

	public static double getNormalTextProbabilitySecondChunk(TextChunk x, TextChunk y, double baseLineDiff, double fontSizeDiff) {
		double mergeNormalLineProbability = Math.abs(fontSizeDiff) < Math.min(SUPERSCRIPT_FONTSIZE_THRESHOLD,
				SUBSCRIPT_FONTSIZE_THRESHOLD) ? mergeNormalLineProbability(Math.abs(baseLineDiff), Math.abs(fontSizeDiff),
				NORMAL_LINE_PROBABILITY_PARAMS_2) : mergeNormalLineProbability(Math.abs(baseLineDiff), Math.abs(fontSizeDiff),
				NORMAL_LINE_PROBABILITY_PARAMS);
		return mergeByCharSpacingProbability(x, y) * mergeNormalLineProbability;
	}

	public static double getSuperscriptProbabilitySecondChunk(TextChunk x, TextChunk y, double baseLineDiff, double fontSizeDiff) {
		if (fontSizeDiff > SUPERSCRIPT_FONTSIZE_THRESHOLD && baseLineDiff < -SUPERSCRIPT_BASELINE_THRESHOLD) {
			return  mergeByCharSpacingProbability(x, y) * toLineProbabilityFunction(Math.abs(baseLineDiff),
			                                                                        Math.abs(fontSizeDiff),
			                                                                        SUPERSCRIPT_PROBABILITY_PARAMS);
		}
		return 0.0;
	}

	public static double getSubscriptProbabilitySecondChunk(TextChunk x, TextChunk y, double baseLineDiff, double fontSizeDiff) {
		if (fontSizeDiff > SUBSCRIPT_FONTSIZE_THRESHOLD && baseLineDiff > SUBSCRIPT_BASELINE_THRESHOLD) {
			return  mergeByCharSpacingProbability(x, y) * toLineProbabilityFunction(Math.abs(baseLineDiff),
			                                                                        Math.abs(fontSizeDiff),
			                                                                        SUBSCRIPT_PROBABILITY_PARAMS);
		}
		return 0.0;
	}

	public static double getSuperscriptProbabilityFirstChunk(TextChunk x, TextChunk y, double baseLineDiff, double fontSizeDiff) {
		if (fontSizeDiff < -SUPERSCRIPT_FONTSIZE_THRESHOLD && baseLineDiff > SUPERSCRIPT_BASELINE_THRESHOLD) {
			return mergeByCharSpacingProbability(x, y) * toLineProbabilityFunction(Math.abs(baseLineDiff),
			                                                                       Math.abs(fontSizeDiff),
			                                                                       SUPERSCRIPT_PROBABILITY_PARAMS);
		}
		return 0.0;
	}

	public static double getSubscriptProbabilityFirstChunk(TextChunk x, TextChunk y, double baseLineDiff, double fontSizeDiff) {
		if (fontSizeDiff < -SUBSCRIPT_FONTSIZE_THRESHOLD && baseLineDiff < -SUBSCRIPT_BASELINE_THRESHOLD) {
			return mergeByCharSpacingProbability(x, y) * toLineProbabilityFunction(Math.abs(baseLineDiff),
			                                                                       Math.abs(fontSizeDiff),
			                                                                       SUBSCRIPT_PROBABILITY_PARAMS);
		}
		return 0.0;
	}

	public static double getFootnoteProbability(SemanticTextNode firstNode, SemanticTextNode secondNode,
	                                            TextLine lastLine, TextLine nextLine) {
		if (!firstNode.getPageNumber().equals(secondNode.getPageNumber()) ||
		    lastLine.getBaseLine() < nextLine.getBaseLine() + FOOTNOTE_PROBABILITY_PARAMS[6]) {
			return 0.0;
		}
		double footnoteProbability = 0.0;
		TextChunk y = nextLine.getFirstTextChunk();
		List<TextChunk> superscriptTextChunks = new LinkedList<>();
		for (TextColumn column : firstNode.getColumns()) {
			for (TextLine line : column.getLines()) {
				for (TextChunk chunk : line.getTextChunks()) {
					if (chunk.getTextFormat() == TextFormat.SUPERSCRIPT) {
						superscriptTextChunks.add(chunk);
					}
				}
			}
		}

		if (!superscriptTextChunks.isEmpty()) {
			footnoteProbability += FOOTNOTE_PROBABILITY_PARAMS[0];
		} else {
			footnoteProbability -= FOOTNOTE_PROBABILITY_PARAMS[3];
		}

		String yValue = y.getValue();
		for (TextChunk chunk : superscriptTextChunks) {
			if (chunk.getValue().equals(yValue.substring(0, Math.min(chunk.getValue().length(), yValue.length())))) {
				footnoteProbability += FOOTNOTE_PROBABILITY_PARAMS[1];
				break;
			}
		}
		if (firstNode.getFontSize() > secondNode.getFontSize() + FOOTNOTE_PROBABILITY_PARAMS[6]) {
			footnoteProbability += FOOTNOTE_PROBABILITY_PARAMS[2];
		} else if (secondNode.getFontSize() > firstNode.getFontSize() + FOOTNOTE_PROBABILITY_PARAMS[6]) {
			footnoteProbability -= FOOTNOTE_PROBABILITY_PARAMS[5];
		}
		return footnoteProbability;
	}

	/**
	 * Calculates linear function: 1 - ax - by
	 * @param x : first argument (baseline difference)
	 * @param y : second argument (fontsize difference)
	 * @param params : array of coefficients a, b
	 * @return function result
	 */
	private static double mergeNormalLineProbability(double x, double y, double[] params) {
		return 1 - params[0] * x - params[1] * y;
	}

	/**
	 * Calculates quadratic function: 1 - ax^2 - by^2 + cxy
	 * @param x : first argument (baseline difference)
	 * @param y : second argument (fontsize difference)
	 * @param params : array of coefficients a, b, c
	 * @return function result
	 */
	private static double toLineProbabilityFunction(double x, double y, double[] params) {
		double result = 1 - params[0] * x * x - (params[1] * y - params[2] * x) * y;
		return result;
	}

	public static double toLineMergeProbability(TextLine x, TextLine y) {
		return toLineMergeProbability(x.getLastTextChunk(), y.getFirstTextChunk());
	}

	public static double toParagraphMergeProbability(TextLine x, TextLine y) {
		double resultProbability = 1;

		resultProbability *= mergeLeadingProbability(x, y);
		resultProbability *= mergeIndentationProbability(x, y);

		return resultProbability;
	}

	public static double toPartMergeProbability(TextLine x, TextLine y, TextLine penultLine, TextLine secondLine) {
		double resultProbability = 1;

		resultProbability *= mergeLeadingProbability(x, y, PART_FONT_LEADING_INTERVAL);
		resultProbability *= mergeIndentationProbability(secondLine, penultLine);

		return resultProbability;
	}

	public static double toColumnsMergeProbability(TextLine x, TextLine y) {
		if (Math.abs(x.getFontSize() - y.getFontSize()) > FONT_SIZE_DIFFERENCE_PARAMS[0]) {
			return 0;
		}

		if (x.getLastPageNumber() == null || y.getPageNumber() == null) {
			return 0;
		}

		if (x.getLastPageNumber() > y.getPageNumber()) {
			return 0;
		}
		if (x.getLastPageNumber().equals(y.getPageNumber())) {
			if (x.getTextEnd() > y.getTextStart()) {
				return 0;
			}
			double maxFontSize = Math.max(x.getFontSize(), y.getFontSize());
			if ((y.getTextStart() - x.getTextEnd()) / maxFontSize < COLUMNS_PROBABILITY_PARAMS[0] &&
					(Math.abs(x.getBaseLine() - y.getBaseLine())) / maxFontSize < COLUMNS_PROBABILITY_PARAMS[1]) {
				return 0;
			}
		}

		return mergeByFontSizeProbability(x, y);
	}

	public static double mergeLeadingProbability(TextLine x, TextLine y) {
		return mergeLeadingProbability(x, y, DEFAULT_FONT_LEADING_INTERVAL);
	}

	public static double mergeLeadingProbability(TextLine x, TextLine y, double[] fontLeadingInterval) {
		if (Math.abs(x.getFontSize() - y.getFontSize()) > FONT_SIZE_DIFFERENCE_PARAMS[1]) {
			return 0;
		}

		if (x.getLastPageNumber() == null || y.getPageNumber() == null) {
			return 0;
		}
		if (x.getLastPageNumber() < y.getPageNumber()) {
			return 1;
		}
		if (!x.getLastPageNumber().equals(y.getPageNumber())) {
			return 0;
		}

		double maxFontSize = y.getFontSize();
		double baseLineDifference = x.getBaseLine() - y.getBaseLine();

		return getUniformProbability(fontLeadingInterval, baseLineDifference / maxFontSize,
		                             FONT_LEADING_INTERVAL_STANDARD);
	}

	private static double mergeByFontNameProbability(TextChunk x, TextChunk y) {
		return x.getFontName().equals(y.getFontName()) ? 1 : 0;
	}

	private static double mergeByFontSizeProbability(TextChunk x, TextChunk y) {
		double fontSize1 = x.getFontSize();
		double fontSize2 = y.getFontSize();

		double ratio = fontSize1 < fontSize2 ? fontSize1 / fontSize2
		                                     : fontSize2 / fontSize1;

		return getUniformProbability(new double[]{1, 1}, ratio, FONT_SIZE_COMPARISON_THRESHOLD);
	}

	private static double mergeByFontSizeProbability(TextLine x, TextLine y) {
		double fontSize1 = x.getFontSize();
		double fontSize2 = y.getFontSize();

		double ratio = fontSize1 < fontSize2 ? fontSize1 / fontSize2
		                                     : fontSize2 / fontSize1;

		return getUniformProbability(new double[]{1, 1}, ratio, FONT_SIZE_COMPARISON_THRESHOLD);
	}

	private static double mergeByFontColorProbability(TextChunk x, TextChunk y) {
		return Arrays.equals(x.getFontColor(), y.getFontColor()) ? 1 : 0;
	}

	private static double mergeByBaseLineProbability(TextChunk x, TextChunk y) {
		return getUniformProbability(new double[]{0, FLOATING_POINT_OPERATIONS_EPS},
		                             Math.abs(x.getBaseLine() - y.getBaseLine()),
		                             FONT_METRIC_UNIVERSAL_TEMPORARY_THRESHOLD);
	}

	private static double mergeByCharSpacingProbability(TextInfoChunk x, TextInfoChunk y) {
//todo        if (Math.abs(x.getBaseLine() - y.getBaseLine()) > 0.95)
//            return 1;
//            replace with mergeYAlmostNestedProbability
//            We assume that x < y

		if (!NodeUtils.areCloseNumbers(y.getSlantDegree(), x.getSlantDegree())) {
			return 0.0;
		}

		double firstChunkEnd = x.getTextEnd();
		double secondChunkStart = y.getTextStart();

		int numberOfEndWhiteSpaces = numberOfEndWhiteSpaces(x.getValue());
		if (numberOfEndWhiteSpaces != x.getValue().length()) {
			firstChunkEnd -= numberOfEndWhiteSpaces * whitespaceSize(x.getFontSize());
		}

		int numberOfStartsWhiteSpaces = numberOfStartsWhiteSpaces(y.getValue());
		if (numberOfStartsWhiteSpaces != y.getValue().length()) {
			secondChunkStart += numberOfStartsWhiteSpaces * whitespaceSize(y.getFontSize());
		}

		double distanceBetweenChunks = Math.abs(firstChunkEnd - secondChunkStart);
		double maxFontSize = Math.max(x.getFontSize(), y.getFontSize());

		return getUniformProbability(DEFAULT_FONT_CHAR_SPACING_INTERVAL, distanceBetweenChunks / maxFontSize,
		                             FONT_WHITESPACE_COMPARISON_THRESHOLD);
	}

	public static double mergeIndentationProbability(TextLine x, TextLine y) {
		// We assume that x, y have approx the same fontSize
		double maxFontSize = Math.max(x.getFontSize(), y.getFontSize());

		double leftXDifference = Math.abs(x.getTextStart() - y.getTextStart());
		double rightXDifference = Math.abs(x.getTextEnd() - y.getTextEnd());
		double centerXDifference = Math.abs(x.getTextCenter() - y.getTextCenter());

		double minDifference = Math.min(leftXDifference, rightXDifference);
		minDifference = Math.min(minDifference, centerXDifference);
		minDifference /= maxFontSize;

		return getUniformProbability(new double[]{0, FLOATING_POINT_OPERATIONS_EPS}, minDifference,
		                             FONT_METRIC_UNIVERSAL_TEMPORARY_THRESHOLD);
	}

	private static double mergeYAlmostNestedProbability(TextChunk x, TextChunk y) {
		double minBottomY = Math.min(x.getBottomY(), y.getBottomY());
		double maxBottomY = Math.max(x.getBottomY(), y.getBottomY());
		double minTopY = Math.min(x.getTopY(), y.getTopY());
		double maxTopY = Math.max(x.getTopY(), y.getTopY());

		double boundingBoxYIntersection = minTopY - maxBottomY;
		double minBoundingBoxDifference = Math.min(maxBottomY - minBottomY, maxTopY - minTopY);
		double ratio = minBoundingBoxDifference / boundingBoxYIntersection;

		return getUniformProbability(new double[]{0, 0}, ratio, FONT_METRIC_UNIVERSAL_TEMPORARY_THRESHOLD);
	}

	public static double getUniformProbability(double[] probability1Interval, double point,
	                                            double initIntervalLength) {
		if (point + FLOATING_POINT_OPERATIONS_EPS > probability1Interval[0]
		    && point < probability1Interval[1] + FLOATING_POINT_OPERATIONS_EPS) {
			return 1;
		}

		if (point < probability1Interval[0] - initIntervalLength - FLOATING_POINT_OPERATIONS_EPS
		    || point > probability1Interval[1] + initIntervalLength + FLOATING_POINT_OPERATIONS_EPS) {
			return 0;
		}

		double deviation = point < probability1Interval[0] + FLOATING_POINT_OPERATIONS_EPS
		                   ? probability1Interval[0] - point
		                   : point - probability1Interval[1];

		// double[] initInterval = { 0, length };
		double[] targetProbabilityInterval = {0, 1};

		// linear mapping
		double probabilityFactor
				= (targetProbabilityInterval[1] - targetProbabilityInterval[0]) / initIntervalLength;
		return (initIntervalLength - deviation) * probabilityFactor + targetProbabilityInterval[0];
	}

	private static int numberOfEndWhiteSpaces(String str) {
		for (int i = str.length() - 1; i >= 0 ; i--) {
			if (!TextChunkUtils.isWhiteSpaceChar(str.charAt(i))) {
				return str.length() - 1 - i;
			}
		}
		return str.length();
	}

	private static int numberOfStartsWhiteSpaces(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (!TextChunkUtils.isWhiteSpaceChar(str.charAt(i))) {
				return i;
			}
		}
		return str.length();
	}

	private static double whitespaceSize(double fontSize) {
		return TextChunkUtils.WHITE_SPACE_FACTOR * fontSize;
	}
} 
