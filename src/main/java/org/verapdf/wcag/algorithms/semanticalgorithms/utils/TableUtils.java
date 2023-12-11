package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.content.IChunk;
import org.verapdf.wcag.algorithms.entities.content.TextInfoChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableCluster;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableClusterGap;

import java.util.*;

public class TableUtils {
    private static final double WIDTH_TOLERANCE_FACTOR = 0.33;

    public static final double EPSILON = 1.e-18;

    public static final double TABLE_PROBABILITY_THRESHOLD = 0.75;
    public static final double MERGE_PROBABILITY_THRESHOLD = 0.75;
    public static final double HEADERS_PROBABILITY_THRESHOLD = 0.75;


    public static final double NEXT_TOKEN_LENGTH_THRESHOLD = 1.2;
    public static final double NEXT_LINE_TOLERANCE_FACTOR = 1.05;
    public static final double NEXT_LINE_MAX_TOLERANCE_FACTOR = 1.5;
    public static final double ONE_LINE_TOLERANCE_FACTOR = 0.9;
    public static final double TABLE_GAP_FACTOR = 3.0;

    private static final Set<SemanticType> tableSemanticTypes = new HashSet<>(Arrays.asList(
            SemanticType.TABLE, SemanticType.TABLE_ROW, SemanticType.TABLE_HEADER, SemanticType.TABLE_CELL,
            SemanticType.TABLE_HEADERS, SemanticType.TABLE_BODY, SemanticType.TABLE_FOOTER));

    private TableUtils() {}

    public static boolean isTableNode(INode node) {
        return tableSemanticTypes.contains(node.getSemanticType());
    }

    public static boolean isInitialTableNode(INode node) {
        return tableSemanticTypes.contains(node.getInitialSemanticType());
    }

    /*
    TODO: add comments
     */
    public static boolean areStrongContaining(TextInfoChunk chunk1, TextInfoChunk chunk2) {
        return isAnyContaining(chunk1, chunk2) && areStrongCenterOverlapping(chunk1, chunk2);
    }

    public static boolean isAnyContaining(TextInfoChunk chunk1, TextInfoChunk chunk2) {
        return (isContaining(chunk1, chunk2) || isContaining(chunk2, chunk1));
    }

    public static boolean isContaining(TextInfoChunk first, TextInfoChunk second) {
        double tol = WIDTH_TOLERANCE_FACTOR * Math.min(first.getFontSize(), second.getFontSize());
        return (second.getLeftX() + tol > first.getLeftX() && second.getRightX() < first.getRightX() + tol);

    }

    public static boolean areStrongCenterOverlapping(TextInfoChunk chunk1, TextInfoChunk chunk2) {
        double tol = WIDTH_TOLERANCE_FACTOR * Math.min(chunk1.getFontSize(), chunk2.getFontSize());
        double center1 = chunk1.getCenterX();
        double center2 = chunk2.getCenterX();

        if (center1 + tol > chunk2.getRightX() || center1 < chunk2.getLeftX() + tol) {
            return false;
        }
        if (center2 + tol > chunk1.getRightX() || center2 < chunk1.getLeftX() + tol) {
            return false;
        }
        return true;
    }

    public static boolean areCenterOverlapping(TextInfoChunk chunk1, TextInfoChunk chunk2) {
        double tol = WIDTH_TOLERANCE_FACTOR * Math.min(chunk1.getFontSize(), chunk2.getFontSize());
        double center1 = chunk1.getCenterX();
        double center2 = chunk2.getCenterX();

        if (center1 + tol < chunk2.getRightX() && center1 > chunk2.getLeftX() + tol) {
            return true;
        }
        if (center2 + tol < chunk1.getRightX() && center2 > chunk1.getLeftX() + tol) {
            return true;
        }
        return false;
    }

    public static boolean areOverlapping(TextInfoChunk chunk1, TextInfoChunk chunk2) {
        double tol = WIDTH_TOLERANCE_FACTOR * Math.min(chunk1.getFontSize(), chunk2.getFontSize());
        return (chunk1.getLeftX() + tol < chunk2.getRightX() && chunk2.getLeftX() + tol < chunk1.getRightX());
    }

    public static void sortClustersLeftToRight(List<TableCluster> clusters) {
        clusters.sort(Comparator.comparingDouble(TableCluster::getLeftX));
    }

    public static void sortClustersUpToBottom(List<TableCluster> clusters) {
        clusters.sort(Comparator.comparingDouble(TableCluster::getFirstBaseLine).reversed());
    }

    public static boolean isWeakCluster(TableCluster cluster, List<TableCluster> headers) {
        if (cluster.getHeader() != null) {
            return false;
        }

        TableClusterGap gap = cluster.getMinLeftGap();
        while (gap != null && gap.getLink().getHeader() == null) {
            gap = gap.getLink().getMinLeftGap();
        }
        TableCluster leftHeader = (gap == null) ? null : gap.getLink().getHeader();

        gap = cluster.getMinRightGap();
        while (gap != null && gap.getLink().getHeader() == null) {
            gap = gap.getLink().getMinRightGap();
        }
        TableCluster rightHeader = (gap == null) ? null : gap.getLink().getHeader();

        if (leftHeader == null) {
            if (rightHeader == null || rightHeader.getColNumber() > 0) {
                return true;
            }
        } else {
            if (rightHeader == null || rightHeader.getColNumber() < headers.size() - 1) {
                return true;
            } else if (rightHeader.getColNumber() - leftHeader.getColNumber() > 1) {
                return true;
            }
        }
        return false;
    }

    public static double getRowGapFactor(TextInfoChunk tokenRow, TextInfoChunk nextTokenRow) {
        return (tokenRow.getBaseLine() - nextTokenRow.getBaseLine()) / nextTokenRow.getFontSize();
    }

    public static double minDeviation(IChunk first, IChunk second) {
        double width = Math.max(first.getBoundingBox().getWidth(), second.getBoundingBox().getWidth());
        if (width < EPSILON) {
            return 0.0;
        }

        double leftDeviation = Math.abs(first.getLeftX() - second.getLeftX());
        double rightDeviation = Math.abs(first.getRightX() - second.getRightX());
        double centerDeviation = Math.abs(first.getCenterX() - second.getCenterX());

        return Math.min(Math.min(leftDeviation, rightDeviation), centerDeviation) / width;
    }
}
