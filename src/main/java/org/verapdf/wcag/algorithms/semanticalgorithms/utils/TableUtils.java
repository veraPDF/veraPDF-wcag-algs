package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.content.TextInfoChunk;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableCluster;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableClusterGap;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TableUtils {
    private static final double WITH_TOLERANCE_FACTOR = 0.33;

    public static final double EPSILON = 1.e-18;

    public static final double TABLE_PROBABILITY_THRESHOLD = 0.75;
    public static final double MERGE_PROBABILITY_THRESHOLD = 0.75;
    public static final double HEADERS_PROBABILITY_THRESHOLD = 0.75;

    public static final double NEXT_LINE_TOLERANCE_FACTOR = 1.05;
    public static final double ONE_LINE_TOLERANCE_FACTOR = 0.9;
    public static final double TABLE_GAP_FACTOR = 5.0;

    private TableUtils() {}

    public static boolean areStrongContaining(TextInfoChunk chunk1, TextInfoChunk chunk2) {
        return isAnyContaining(chunk1, chunk2) && areStrongCenterOverlapping(chunk1, chunk2);
    }

    public static boolean isAnyContaining(TextInfoChunk chunk1, TextInfoChunk chunk2) {
        return (isContaining(chunk1, chunk2) || isContaining(chunk2, chunk1));
    }

    public static boolean isContaining(TextInfoChunk first, TextInfoChunk second) {
        double tol = WITH_TOLERANCE_FACTOR * Math.min(first.getFontSize(), second.getFontSize());
        return (second.getLeftX() + tol > first.getLeftX() && second.getRightX() < first.getRightX() + tol);

    }

    public static boolean areStrongCenterOverlapping(TextInfoChunk chunk1, TextInfoChunk chunk2) {
        double tol = WITH_TOLERANCE_FACTOR * Math.min(chunk1.getFontSize(), chunk2.getFontSize());
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
        double tol = WITH_TOLERANCE_FACTOR * Math.min(chunk1.getFontSize(), chunk2.getFontSize());
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
        double tol = WITH_TOLERANCE_FACTOR * Math.min(chunk1.getFontSize(), chunk2.getFontSize());
        return (chunk1.getLeftX() + tol < chunk2.getRightX() && chunk2.getLeftX() + tol < chunk1.getRightX());
    }

    public static void sortClustersLeftToRight(List<TableCluster> clusters) {
        Collections.sort(clusters, Comparator.comparingDouble(TableCluster::getLeftX));
    }

    static public void sortClustersUpToBottom(List<TableCluster> clusters) {
        Collections.sort(clusters, Comparator.comparingDouble(TableCluster::getBaseLine).reversed());
    }

    static public boolean isWeakCluster(TableCluster cluster, List<TableCluster> headers) {
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

    static public double getRowGapFactor(TextInfoChunk tokenRow, TextInfoChunk nextTokenRow) {
        return (tokenRow.getBaseLine() - nextTokenRow.getBaseLine()) / nextTokenRow.getFontSize();
    }
}
