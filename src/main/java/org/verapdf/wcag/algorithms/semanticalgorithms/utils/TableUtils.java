package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.content.InfoChunk;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableCluster;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TableUtils {
    public static final double MERGE_PROBABILITY_THRESHOLD = 0.75;
    public static final double HEADERS_PROBABILITY_THRESHOLD = 0.75;

    public static final double NEXT_LINE_TOLERANCE_FACTOR = 1.05;
    public static final double ONE_LINE_TOLERANCE_FACTOR = 0.9;
    public static final double TABLE_GAP_FACTOR = 5.0;

    private TableUtils() {}

    public static boolean stronglyOverlapping(InfoChunk chunk1, InfoChunk chunk2) {
        double center1 = 0.5 * (chunk1.getLeftX() + chunk1.getRightX());
        double center2 = 0.5 * (chunk2.getLeftX() + chunk2.getRightX());

        if (center1 < chunk2.getRightX() && center1 > chunk2.getLeftX()) {
            return true;
        }
        if (center2 < chunk1.getRightX() && center2 > chunk1.getRightX()) {
            return true;
        }
        return false;
    }

    public static void sortHeadersLeftToRight(List<TableCluster> headers) {
        Collections.sort(headers, (Comparator.<TableCluster>
                comparingDouble(header1 -> header1.getLeftX())
                .thenComparingDouble(header2 -> header2.getLeftX())));
    }

    static public void sortClusters(List<TableCluster> clusters) {
        Collections.sort(clusters, new Comparator<TableCluster>() {
            @Override
            public int compare(TableCluster c1, TableCluster c2) {
                double fontSize = Math.max(c1.getFirstLine().getFontSize(),
                        c2.getFirstLine().getFontSize());
                double baseLineTolerance = fontSize * TableUtils.ONE_LINE_TOLERANCE_FACTOR;

                if (c1.getBaseLine() > c2.getBaseLine() + baseLineTolerance) {
                    return -1;
                }
                if (c2.getBaseLine() > c1.getBaseLine() + baseLineTolerance) {
                    return 1;
                }
                if (c1.getLeftX() < c2.getLeftX()) {
                    return -1;
                }
                return 1;
            }
        });
    }
}
