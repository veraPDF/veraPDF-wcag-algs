package org.verapdf.wcag.algorithms.implementation.utils;

import org.verapdf.wcag.algorithms.entity.ITextChunk;

import java.util.Arrays;

public class ChunksMergeUtils {

	private static final double FLOATING_POINT_OPERATIONS_EPS = 1e-7;
	private static final double FONT_METRIC_UNIVERSAL_TEMPORARY_THRESHOLD = 0.1;
	private static final double FONT_SIZE_COMPARISON_THRESHOLD = 0.05;
	private static final double FONT_WHITESPACE_COMPARISON_THRESHOLD = 0.1;
	private static final double FONT_LEADING_INTERVAL_STANDARD = 1;
//	private final static double[] FONT_WHITESPACE_INTERVAL_STANDARD = { 0.2, 0.25 };
//  private final static double[] FONT_WHITESPACE_INTERVAL_JUSTIFIED_TEXT = { 0.16, 0.33 };
	private static final double[] DEFAULT_FONT_CHAR_SPACING_INTERVAL = {0, 0.33};
	private static final double[] DEFAULT_FONT_LEADING_INTERVAL = {0, 1.5};

	private ChunksMergeUtils() {
	}

	public static double toChunkMergeProbability(ITextChunk x, ITextChunk y) {
		double resultProbability = 1;

		resultProbability *= mergeByFontNameProbability(x, y);
		resultProbability *= mergeByFontSizeProbability(x, y);
		resultProbability *= mergeByFontColorProbability(x, y);
		resultProbability *= mergeByBaseLineProbability(x, y);
		resultProbability *= mergeByCharSpacingProbability(x, y);

		return resultProbability;
	}

	public static double toLineMergeProbability(ITextChunk x, ITextChunk y) {
		double resultProbability = 1;

		resultProbability *= mergeByCharSpacingProbability(x, y);
		resultProbability *= mergeYAlmostNestedProbability(x, y);

		return resultProbability;
	}

	public static double toParagraphMergeProbability(ITextChunk x, ITextChunk y) {
		double resultProbability = 1;

		resultProbability *= mergeLeadingProbability(x, y);
		resultProbability *= mergeIndentationProbability(x, y);

		return resultProbability;
	}

	public static double mergeLeadingProbability(ITextChunk x, ITextChunk y) {
		if (Math.abs(x.getFontSize() - y.getFontSize()) > 0.95) {
			return 0;
		}

		double maxFontSize = Math.max(x.getFontSize(), y.getFontSize());
		double baseLineDifference = Math.abs(x.getBaseLine() - y.getBaseLine());

		return getUniformProbability(DEFAULT_FONT_LEADING_INTERVAL, baseLineDifference / maxFontSize,
		                             FONT_LEADING_INTERVAL_STANDARD);
	}

	private static double mergeByFontNameProbability(ITextChunk x, ITextChunk y) {
		return x.getFontName().equals(y.getFontName()) ? 1 : 0;
	}

	private static double mergeByFontSizeProbability(ITextChunk x, ITextChunk y) {
		double fontSize1 = x.getFontSize();
		double fontSize2 = y.getFontSize();

		double ratio = fontSize1 < fontSize2 ? fontSize1 / fontSize2
		                                     : fontSize2 / fontSize1;

		return getUniformProbability(new double[]{0, 0}, ratio, FONT_SIZE_COMPARISON_THRESHOLD);
	}

	private static double mergeByFontColorProbability(ITextChunk x, ITextChunk y) {
		return Arrays.equals(x.getFontColor(), y.getFontColor()) ? 1 : 0;
	}

	private static double mergeByBaseLineProbability(ITextChunk x, ITextChunk y) {
		return getUniformProbability(new double[]{0, FLOATING_POINT_OPERATIONS_EPS},
		                             Math.abs(x.getBaseLine() - y.getBaseLine()),
		                             FONT_METRIC_UNIVERSAL_TEMPORARY_THRESHOLD);
	}

	private static double mergeByCharSpacingProbability(ITextChunk x, ITextChunk y) {
//        if (Math.abs(x.getBaseLine() - y.getBaseLine()) > 0.95)
//            return 1;
		// replace with mergeYAlmostNestedProbability

		// We assume that x < y

		double leftChunkRightX = x.getBoundingBox()[2];
		double rightChunkLeftX = y.getBoundingBox()[0];
		double distanceBetweenChunks = Math.abs(leftChunkRightX - rightChunkLeftX);

		if (lastCharIsWhitespace(x.getText())) {
			distanceBetweenChunks += whitespaceSize(x.getFontSize());
		}

		if (firstCharIsWhitespace(y.getText())) {
			distanceBetweenChunks += whitespaceSize(y.getFontSize());
		}

		double maxFontSize = Math.max(x.getFontSize(), y.getFontSize());

		return getUniformProbability(DEFAULT_FONT_CHAR_SPACING_INTERVAL, distanceBetweenChunks / maxFontSize,
		                             FONT_WHITESPACE_COMPARISON_THRESHOLD);
	}

	private static double mergeIndentationProbability(ITextChunk x, ITextChunk y) {
		// We assume that x, y have approx the same fontSize
		double maxFontSize = Math.max(x.getFontSize(), y.getFontSize());

		double leftXDifference = Math.abs(x.getLeftX() - y.getLeftX());
		double rightXDifference = Math.abs(x.getRightX() - y.getRightX());
		double centerXDifference = Math.abs((x.getRightX() + x.getLeftX()) - (y.getRightX() + y.getLeftX())) / 2;

		double minDifference = Math.min(leftXDifference, rightXDifference);
		minDifference = Math.min(minDifference, centerXDifference);
		minDifference /= maxFontSize;

		return getUniformProbability(new double[]{0, FLOATING_POINT_OPERATIONS_EPS}, minDifference,
		                             FONT_METRIC_UNIVERSAL_TEMPORARY_THRESHOLD);
	}

	private static double mergeIndentationProbability(ITextChunk x, ITextChunk y, int indentation) {
		double maxFontSize = Math.max(x.getFontSize(), y.getFontSize());

		double difference = 0;
		switch (indentation) {
			case 0: // left
				difference = Math.abs(x.getLeftX() - y.getLeftX());
				break;
			case 1: // right
				difference = Math.abs(x.getRightX() - y.getRightX());
				break;
			case 2: // center
				difference = Math.abs((x.getRightX() + x.getLeftX()) - (y.getRightX() + y.getLeftX())) / 2;
				break;
			default:
				break;
		}
		difference /= maxFontSize;

		return getUniformProbability(new double[]{0, FLOATING_POINT_OPERATIONS_EPS}, difference,
		                             FONT_METRIC_UNIVERSAL_TEMPORARY_THRESHOLD);
	}

	private static double mergeYAlmostNestedProbability(ITextChunk x, ITextChunk y) {
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

//    private static boolean isTextChunkAlmostNested(TextChunk x, TextChunk y) {
//        return x.getBottomY() < y.getBottomY() + FLOATING_POINT_OPERATIONS_EPS ?
//                x.getTopY() > y.getTopY() - FLOATING_POINT_OPERATIONS_EPS:
//                x.getTopY() < y.getTopY() + FLOATING_POINT_OPERATIONS_EPS;
//    }
//
} 
