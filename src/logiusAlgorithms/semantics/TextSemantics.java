package logiusAlgorithms.semantics;

import logiusAlgorithms.PDTree.PDTextChunk;
import logiusAlgorithms.PDTree.PDTextLine;

import java.util.Arrays;

public class TextSemantics {
    // TODO: move all constants in settings.json
    // TODO: replace numbers in the code with constants
    // TODO: move some methods into new classes
    private final static String settingsFileName = "settings.json";
    private final static double FLOATING_POINT_OPERATIONS_EPS = 1e-7;
    private final static double FONT_METRIC_UNIVERSAL_TEMPORARY_THRESHOLD = 0.1;
    private final static double FONT_SIZE_COMPARISON_THRESHOLD = 0.05;
    private final static double FONT_WHITESPACE_COMPARISON_THRESHOLD = 0.1;
    private final static double FONT_LEADING_INTERVAL_STANDARD = 0.1;
    private final static double[] FONT_WHITESPACE_INTERVAL_STANDARD = { 0.2, 0.25 };
    private final static double[] FONT_WHITESPACE_INTERVAL_JUSTIFIED_TEXT = { 0.16, 0.33 };
    private final static double[] DEFAULT_FONT_CHAR_SPACING_INTERVAL = { 0, 0.33 };
    private final static double[] DEFAULT_FONT_LEADING_INTERVAL = { 0, 1.5 };

    private double thresholdProbability(double[] probability1Interval, double point, double initIntervalLength) {
        if (point + FLOATING_POINT_OPERATIONS_EPS > probability1Interval[0] &&
                point < probability1Interval[1] + FLOATING_POINT_OPERATIONS_EPS) {
            return 1;
        }

        if (point < probability1Interval[0] - initIntervalLength - FLOATING_POINT_OPERATIONS_EPS ||
                point > probability1Interval[1] + initIntervalLength + FLOATING_POINT_OPERATIONS_EPS)
            return 0;

        double deviation = point < probability1Interval[0] + FLOATING_POINT_OPERATIONS_EPS ?
                probability1Interval[0] - point :
                point - probability1Interval[1];

        // double[] initInterval = { 0, length };
        double[] targetProbabilityInterval = { 0, 1 };

        // linear mapping
        double probabilityFactor =
                (targetProbabilityInterval[1] - targetProbabilityInterval[0]) / initIntervalLength;
        return (initIntervalLength - deviation) * probabilityFactor + targetProbabilityInterval[0];
    }

    private double mergeByFontNameProbability(PDTextChunk x, PDTextChunk y) {
        return x.getFontName().equals(y.getFontName()) ? 1 : 0;
    }

    private double mergeByFontSizeProbability(PDTextChunk x, PDTextChunk y) {
        double fontSize1 = x.getFontSize();
        double fontSize2 = y.getFontSize();

//        double ratio = fontSize1 < fontSize2 ?
//                fontSize1 / fontSize2 :
//                fontSize2 / fontSize1;

        double difference = Math.abs(fontSize1 - fontSize2);

        return thresholdProbability(new double[] { 1, 1 },
//                ratio,
                difference,
                FONT_SIZE_COMPARISON_THRESHOLD);
    }

    private double mergeByBaseLineProbability(PDTextChunk x, PDTextChunk y) {
        return 1;
    }

    private double mergeByFontColorProbability(PDTextChunk x, PDTextChunk y) {
        return Arrays.equals(x.getFontColor(), y.getFontColor()) ? 1 : 0;
    }

    private boolean lastCharIsWhitespace(String str) {
        if (str.length() > 0 && str.charAt(str.length() - 1) == ' ')
            return true;
        return false;
    }

    private boolean firstCharIsWhitespace(String str) {
        if (str.length() > 0 && str.charAt(0) == ' ')
            return true;
        return false;
    }

    private double whitespaceSize(double fontSize) {
        return fontSize / 4;
    }

    private double mergeByCharSpacingProbability(PDTextChunk x, PDTextChunk y) {
        if (Math.abs(x.getBaseLine() - y.getBaseLine()) > 0.95)
            return 1;

        // We assume that x < y

        double leftChunkRightX = x.getBoundingBox()[2];
        double rightChunkLeftX = y.getBoundingBox()[0];
        double distanceBetweenChunks = Math.abs(leftChunkRightX - rightChunkLeftX);

        if (lastCharIsWhitespace(x.getText()))
            distanceBetweenChunks += whitespaceSize(x.getFontSize());

        if (firstCharIsWhitespace(y.getText()))
            distanceBetweenChunks += whitespaceSize(y.getFontSize());

        // replace?
        double maxFontSize = Math.max(x.getFontSize(), y.getFontSize());

        return thresholdProbability(DEFAULT_FONT_CHAR_SPACING_INTERVAL,
                distanceBetweenChunks / maxFontSize,
                FONT_WHITESPACE_COMPARISON_THRESHOLD);
    }

    private double mergeLeadingProbability(PDTextChunk x, PDTextChunk y) {
        if (Math.abs(x.getFontSize() - y.getFontSize()) > 0.95)
            return 0;

        double maxFontSize = Math.max(x.getFontSize(), y.getFontSize());
        double baseLineDifference = Math.abs(x.getBaseLine() - y.getBaseLine());

        return thresholdProbability(DEFAULT_FONT_LEADING_INTERVAL,
                baseLineDifference / maxFontSize,
                FONT_LEADING_INTERVAL_STANDARD);
    }

    private double mergeByBoundingBoxProbability(PDTextChunk x, PDTextChunk y) {
        // We assume that x, y have approx the same fontSize
        double maxFontSize = Math.max(x.getFontSize(), y.getFontSize());

        double leftXDifference = Math.abs(x.getLeftX() - y.getLeftX());
        double rightXDifference = Math.abs(x.getRightX() - y.getRightX());
        double centerXDifference = Math.abs((x.getRightX() + x.getLeftX()) -
                (y.getRightX() + y.getLeftX())) / 2;

        double minDifference = Math.min(leftXDifference, rightXDifference);
        minDifference = Math.min(minDifference, centerXDifference);
        minDifference /= maxFontSize;

        return thresholdProbability(new double[] { 1, 1 },
                minDifference,
                FONT_METRIC_UNIVERSAL_TEMPORARY_THRESHOLD);
    }

    // TODO: check that we start with easy fast checks and move to computational (if exists) checks:
    //       may be useful in future if we compare probability after each "mergeByXXXProbability" with some threshold
    public double toChunkMergeProbability(PDTextChunk x, PDTextChunk y) {
        double resultProbability = 1;

        resultProbability *= mergeByFontNameProbability(x, y);
        resultProbability *= mergeByFontSizeProbability(x, y);
        resultProbability *= mergeByFontColorProbability(x, y);
        resultProbability *= mergeByBaseLineProbability(x, y);
        resultProbability *= mergeByCharSpacingProbability(x, y);

        return resultProbability;
    }

    public double toLineMergeProbability(PDTextChunk x, PDTextChunk y) {
        double resultProbability = 1;

        resultProbability *= mergeByFontSizeProbability(x, y);
        resultProbability *= mergeByBaseLineProbability(x, y);
//        resultProbability *= (1 - mergeByCharSpacingProbability(x, y));

        return resultProbability;
    }

    // TODO: when will be used in loop to merge several lines
    //       remember that the first line may be indented. So loop 2, ..., n
    public double toParagraphMergeProbability(PDTextChunk x, PDTextChunk y) {
        double resultProbability = 1;

        resultProbability *= mergeLeadingProbability(x, y);
        resultProbability *= mergeByBoundingBoxProbability(x, y);

        return resultProbability;
    }
} 
