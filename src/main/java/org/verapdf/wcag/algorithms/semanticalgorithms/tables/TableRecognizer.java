package org.verapdf.wcag.algorithms.semanticalgorithms.tables;

import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TableUtils;

import java.util.*;

public class TableRecognizer {

    private List<TableCluster> headers;
    private List<TableCluster> clusters;
    private Map<TableCluster, TableCluster> columns;
    private Integer numRows;

    private int clusterCounter = 0;

    public TableRecognizer(TableRecognitionArea recognitionArea) {
        headers = recognitionArea.getHeaders();
        clusters = recognitionArea.getClusters();
        columns = new HashMap<>();
    }

    public void recognize() {
        preprocess();
        calculateInitialColumns();
        mergeWeakClusters();
        mergeClustersByMinGaps();
    }

    private void mergeClustersByMinGaps() {
        int numClusters = 0;
        while (numClusters != clusters.size()) {
            numClusters = clusters.size();

            for (TableCluster cluster : clusters) {
                if (cluster.getId() == null || cluster.getMinRightGap() == null) {
                    continue;
                }

                TableClusterGap rightGap = cluster.getMinRightGap();
                TableClusterGap leftGap = cluster.getMinLeftGap();

                TableCluster nextCluster = rightGap.getLink();
                TableClusterGap nextRightGap = nextCluster.getMinRightGap();
                TableClusterGap nextLeftGap = nextCluster.getMinLeftGap();

                if (cluster == nextLeftGap.getLink() &&
                        (cluster.getHeader() == null || nextCluster.getHeader() == null) &&
                        (leftGap == null || rightGap.getGap() < leftGap.getGap()) &&
                        (nextRightGap == null || nextLeftGap.getGap() < nextRightGap.getGap())) {

                    if (nextCluster.getHeader() != null) {
                        nextCluster.merge(cluster, true);
                        cluster.setId(null);
                    } else {
                        cluster.merge(nextCluster, true);
                        nextCluster.setId(null);
                    }
                }
            }

            clusters = getActualClusters(clusters);
        }
    }

    private void calculateInitialColumns() {
        for (TableCluster cluster : clusters) {

            if (cluster.getHeader() == null) {
                TableCluster closestHeader = null;
                double minDist = Double.MAX_VALUE;

                for (TableCluster header : headers) {
                    double factor = Double.MAX_VALUE;
                    if (TableUtils.areStrongContaining(cluster, header)) {
                        factor = 0.1;
                    } else if (TableUtils.isContaining(cluster, header)) {
                        factor = 1.0;
                    }
                    if (factor < 2.0) {
                        double dist = factor * Math.abs(cluster.getCenterX() - header.getCenterX());
                        if (dist < minDist) {
                            closestHeader = header;
                            minDist = dist - TableUtils.EPSILON;
                        }
                    }
                }
                cluster.setHeader(closestHeader);
            }
            addClusterToColumnByHeader(cluster);
        }
        clusters = getActualClusters(clusters);
    }

    private void addClusterToColumnByHeader(TableCluster cluster) {
        TableCluster header = cluster.getHeader();
        if (header == null) {
            return;
        }

        if (columns.containsKey(header)) {
            TableCluster originalCluster = columns.get(header);
            originalCluster.merge(cluster, true);
            cluster.setId(null);
        } else {
            columns.put(header, cluster);
        }
    }

    private void mergeWeakClusters() {
        int position = getNextWeakCluster(0);
        while (position < clusters.size()) {
            TableCluster cluster = clusters.get(position);
            TableCluster closestHeader = null;
            double minDist = Double.MAX_VALUE;

            for (TableCluster header : headers) {
                double factor = 1.0;

                if (TableUtils.areCenterOverlapping(cluster, header)) {
                    factor = 0.01;
                } else if (TableUtils.areOverlapping(cluster, header)) {
                    factor = 0.1;
                }

                double dist = factor * Math.abs(cluster.getCenterX() - header.getCenterX());
                if (dist < minDist) {
                    closestHeader = header;
                    minDist = dist - TableUtils.EPSILON;
                }
            }
            cluster.setHeader(closestHeader);
            addClusterToColumnByHeader(cluster);

            position = getNextWeakCluster(position + 1);
        }

        clusters = getActualClusters(clusters);
    }

    private int getNextWeakCluster(int position) {
        while (position < clusters.size()) {
            TableCluster cluster = clusters.get(position);
            if (TableUtils.isWeakCluster(cluster, headers)) {
                return position;
            }
            ++position;
        }
        return position;
    }

    private void preprocess() {
        setupRowAndColNumbers();
        calculateInitialClusters();
    }

    private void setupRowAndColNumbers() {
        for (TableCluster header : headers) {
            header.setId(generateClusterId());
        }
        List<TableCluster> cleanClusters = new ArrayList<>();
        for (TableCluster cluster : clusters) {
            for (TableRow row : cluster.getRows()) {
                TableCluster cleanCluster = new TableCluster(row);
                cleanCluster.setHeader(cluster.getHeader());
                cleanCluster.setId(generateClusterId());
                cleanClusters.add(cleanCluster);
            }
        }
        clusters = cleanClusters;
        TableUtils.sortClustersUpToBottom(clusters);

        setupRowNumbers();
        setupColNumbers();
    }

    private void setupRowNumbers() {
        int currentRow = 1;
        TableCluster firstCluster = clusters.get(0);
        firstCluster.setRowNumber(0, currentRow);
        for (int i = 1; i < clusters.size(); ++i) {
            TableCluster cluster = clusters.get(i);

            double fontSize = cluster.getFirstRow().getFontSize();
            double baseLineTolerance = fontSize * TableUtils.ONE_LINE_TOLERANCE_FACTOR;

            if (firstCluster.getBaseLine() > cluster.getBaseLine() + baseLineTolerance) {
                currentRow += 1;
                firstCluster = cluster;
            }
            cluster.setRowNumber(0, currentRow);
        }
        numRows = currentRow + 1;
    }

    private void setupColNumbers() {
        for (int i = 0; i < headers.size(); ++i) {
            TableCluster header = headers.get(i);
            header.setColNumber(i);
        }

        for (TableCluster cluster : clusters) {
            TableCluster header = cluster.getHeader();
            if (header != null) {
                cluster.setColNumber(header.getColNumber());
            }
        }
    }

    private void calculateInitialClusters() {
        List<List<TableCluster>> clusterRows = new ArrayList<>(numRows - 1);
        for (int i = 0; i < numRows - 1; ++i) {
            clusterRows.add(new ArrayList<>());
        }
        for (TableCluster cluster : clusters) {
            clusterRows.get(cluster.getFirstRow().getRowNumber() - 1).add(cluster);
        }

        clusters = mergeInitialClusters(clusterRows);
        updateMinGaps();
    }

    private void updateMinGaps() {
        for (TableCluster cluster : clusters) {
            cluster.updateMinGaps();
        }
    }

    private List<TableCluster> mergeInitialClusters(List<List<TableCluster>> clusterRows) {
        List<TableCluster> initialClusters = new ArrayList<>();

        for (int i = 0; i < clusterRows.size(); ++i) {
            List<TableCluster> clusterRow = clusterRows.get(i);
            TableUtils.sortClustersLeftToRight(clusterRow);

            for (int j = 0; j < clusterRow.size(); ++j) {
                TableCluster cluster = clusterRow.get(j);
                if (j < clusterRow.size() - 1) {
                    TableCluster nextCluster = clusterRow.get(j + 1);

                    double gap = nextCluster.getLeftX() - cluster.getRightX();
                    cluster.getFirstRow().setRightGap(new TableClusterGap(nextCluster, gap));
                    nextCluster.getFirstRow().setLeftGap(new TableClusterGap(cluster, gap));
                }

                boolean merged = false;
                for (TableCluster initialCluster : initialClusters) {
                    if (initialCluster.getId() == null) {
                        continue;
                    }
                    if (cluster.getHeader() != null && cluster.getHeader() == initialCluster.getHeader()) {
                        initialCluster.merge(cluster, false);
                        cluster.setId(null);
                        cluster = initialCluster;
                        merged = true;
                    } else if (cluster.getHeader() == null || initialCluster.getHeader() == null) {
                        if (TableUtils.isAnyContaining(cluster, initialCluster) ||
                            TableUtils.areStrongCenterOverlapping(cluster, initialCluster)) {
                            initialCluster.merge(cluster, false);
                            cluster.setId(null);
                            cluster = initialCluster;
                            merged = true;
                        }
                    }
                }
                if (!merged) {
                    initialClusters.add(cluster);
                }
            }
        }
        return getActualClusters(initialClusters);
    }

    private List<TableCluster> getActualClusters(List<TableCluster> oldClusters) {
        List<TableCluster> actualClusters = new ArrayList<>();
        for (TableCluster cluster : oldClusters) {
            if (cluster.getId() != null) {
                actualClusters.add(cluster);
            }
        }
        return actualClusters;
    }

    private int generateClusterId() {
        int id = clusterCounter;
        clusterCounter++;
        return id;
    }
}
