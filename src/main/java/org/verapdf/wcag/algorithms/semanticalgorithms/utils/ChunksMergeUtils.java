package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.content.TextChunk;

import java.util.Arrays;

public class ChunksMergeUtils {

	private static final double FLOATING_POINT_OPERATIONS_EPS = 1e-7;
	private static final double FONT_METRIC_UNIVERSAL_TEMPORARY_THRESHOLD = 0.1;
	private static final double FONT_SIZE_COMPARISON_THRESHOLD = 0.05;
	private static final double FONT_WHITESPACE_COMPARISON_THRESHOLD = 0.1;
	private static final double FONT_LEADING_INTERVAL_STANDARD = 1;
	private static final double[] DEFAULT_FONT_CHAR_SPACING_INTERVAL = {0, 0.33};
	private static final double[] DEFAULT_FONT_LEADING_INTERVAL = {0, 1.5};

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

	public static double toLineMergeProbability(TextChunk x, TextChunk y) {
		double resultProbability = 1;

		resultProbability *= mergeByCharSpacingProbability(x, y);
		resultProbability *= mergeYAlmostNestedProbability(x, y);

		return resultProbability;
	}

	public static double toParagraphMergeProbability(TextChunk x, TextChunk y) {
		double resultProbability = 1;

		resultProbability *= mergeLeadingProbability(x, y);
		resultProbability *= mergeIndentationProbability(x, y);

		return resultProbability;
	}

	public static double mergeLeadingProbability(TextChunk x, TextChunk y) {
		if (Math.abs(x.getFontSize() - y.getFontSize()) > 0.95) {
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

		double maxFontSize = Math.max(x.getFontSize(), y.getFontSize());
		double baseLineDifference = Math.abs(x.getBaseLine() - y.getBaseLine());

		return getUniformProbability(DEFAULT_FONT_LEADING_INTERVAL, baseLineDifference / maxFontSize,
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

	private static double mergeByFontColorProbability(TextChunk x, TextChunk y) {
		return Arrays.equals(x.getFontColor(), y.getFontColor()) ? 1 : 0;
	}

	private static double mergeByBaseLineProbability(TextChunk x, TextChunk y) {
		return getUniformProbability(new double[]{0, FLOATING_POINT_OPERATIONS_EPS},
		                             Math.abs(x.getBaseLine() - y.getBaseLine()),
		                             FONT_METRIC_UNIVERSAL_TEMPORARY_THRESHOLD);
	}

	private static double mergeByCharSpacingProbability(TextChunk x, TextChunk y) {
//todo        if (Math.abs(x.getBaseLine() - y.getBaseLine()) > 0.95)
//            return 1;
//            replace with mergeYAlmostNestedProbability
//            We assume that x < y

		double leftChunkRightX = x.getRightX();
		double rightChunkLeftX = y.getLeftX();
		double distanceBetweenChunks = Math.abs(leftChunkRightX - rightChunkLeftX);

		if (lastCharIsWhitespace(x.getValue())) {
			distanceBetweenChunks += whitespaceSize(x.getFontSize());
		}

		if (firstCharIsWhitespace(y.getValue())) {
			distanceBetweenChunks += whitespaceSize(y.getFontSize());
		}

		double maxFontSize = Math.max(x.getFontSize(), y.getFontSize());

		return getUniformProbability(DEFAULT_FONT_CHAR_SPACING_INTERVAL, distanceBetweenChunks / maxFontSize,
		                             FONT_WHITESPACE_COMPARISON_THRESHOLD);
	}

	public static double mergeIndentationProbability(TextChunk x, TextChunk y) {
		// We assume that x, y have approx the same fontSize
		double maxFontSize = Math.max(x.getFontSize(), y.getFontSize());

		double leftXDifference = Math.abs(x.getLeftX() - y.getLeftX());
		double rightXDifference = Math.abs(x.getRightX() - y.getRightX());
		double centerXDifference = 0.5 * Math.abs((x.getRightX() + x.getLeftX()) - (y.getRightX() + y.getLeftX()));

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

	private static double getUniformProbability(double[] probability1Interval, double point,
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

	private static boolean lastCharIsWhitespace(String str) {
		return str.length() > 0 && str.charAt(str.length() - 1) == ' ';
	}

	private static boolean firstCharIsWhitespace(String str) {
		return str.length() > 0 && str.charAt(0) == ' ';
	}

	private static double whitespaceSize(double fontSize) {
		return fontSize / 4;
	}
} 
