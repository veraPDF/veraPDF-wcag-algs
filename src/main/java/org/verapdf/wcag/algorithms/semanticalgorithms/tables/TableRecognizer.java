package org.verapdf.wcag.algorithms.semanticalgorithms.tables;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.tables.*;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TableUtils;

import java.util.*;

public class TableRecognizer {

    private Long clusterCounter = 0L;

    private final List<TableCluster> headers;
    private List<TableCluster> clusters;
    private final Map<TableCluster, TableCluster> columns;
    private Integer numRows = null;

    private Table table = null;
    private TableBorder tableBorder;

    public TableRecognizer(TableRecognitionArea recognitionArea) {
        headers = recognitionArea.getHeaders();
        clusters = recognitionArea.getClusters();
        columns = new HashMap<>();
        tableBorder = recognitionArea.getTableBorder();
    }

    public void recognize() {
        preprocess();
        calculateInitialColumns();
        mergeWeakClusters();
        mergeClustersByMinGaps();
        postprocess();
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
                setupStrongHeaderForCluster(cluster);
            }
            addClusterToColumnByHeader(cluster);
        }
        clusters = getActualClusters(clusters);
    }

    private void setupStrongHeaderForCluster(TableCluster cluster) {
        TableCluster containingHeader = null;

        for (TableCluster header : headers) {
            if (TableUtils.isContaining(cluster, header)) {
                if (containingHeader != null) {
                    return;
                }
                containingHeader = header;
            }
        }
        cluster.setHeader(containingHeader);
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

    /*
    TODO: add comments
     */
    private void mergeWeakClusters() {
        int position = getNextWeakCluster(0);
        while (position < clusters.size()) {
            TableCluster cluster = clusters.get(position);
            TableCluster closestHeader = null;
            double minDist = Double.MAX_VALUE;

            for (TableCluster header : headers) {
                double factor = 1.0;

                if (TableUtils.areStrongContaining(cluster, header)) {
                    factor = 0.0001;
                } else if (TableUtils.isContaining(cluster, header)) {
                    factor = 0.001;
                }
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
            for (TableTokenRow row : cluster.getRows()) {
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
        TableUtils.sortClustersLeftToRight(headers);
        for (int i = 0; i < headers.size(); ++i) {
            TableCluster header = headers.get(i);
            header.setColNumber(i);
        }
    }

    private void calculateInitialClusters() {
        List<List<TableCluster>> clusterRows = new ArrayList<>(numRows - 1);
        for (int i = 0; i < numRows - 1; ++i) {
            clusterRows.add(new ArrayList<>());
        }
        for (TableCluster cluster : clusters) {
            clusterRows.get(cluster.getFirstRow().getRowNumber() - 1).add(cluster);
            if (cluster.getHeader() == null) {
                setupStrongHeaderForCluster(cluster);
            }
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

        for (List<TableCluster> clusterRow : clusterRows) {
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

    public Table getTable() {
        return table;
    }

    public void postprocess() {
        if (headers.size() < clusters.size()) {
            return;
        }
        for (TableCluster cluster : clusters) {
            TableCluster header = cluster.getHeader();
            if (header == null || header.getColNumber() == null) {
                return;
            }
        }
        table = constructTable();
    }

    private void updateColumns() {
        for (TableCluster cluster : clusters) {
            cluster.sortAndMergeRows();
            cluster.setColNumber(cluster.getHeader().getColNumber());
        }
    }

    private Table constructTable() {
        updateColumns();
        Table table = new Table(headers);
        table.setTableBorder(tableBorder);

        List<Integer> rowIds = new ArrayList<>(Collections.nCopies(headers.size(), 0));
        for (TableCluster cluster : clusters) {
            rowIds.add(cluster.getFirstRow().getRowNumber());
        }
        for (int i = 1; i < numRows; ++i) {
            TableRow tableRow = new TableRow(SemanticType.TABLE_BODY);
            for (Map.Entry<TableCluster, TableCluster> entry : columns.entrySet()) {
                TableCluster column = entry.getValue();
                if (column == null || rowIds.get(column.getColNumber()) >= column.getRows().size()) {
                    tableRow.add(new TableCell(SemanticType.TABLE_CELL));
                    continue;
                }

                int rowId = rowIds.get(column.getColNumber());
                Integer rowNumber = column.getRows().get(rowId).getRowNumber();
                if (rowNumber == null || rowNumber <= i) {
                    if (rowNumber == i) {
                        tableRow.add(new TableCell(column.getRows().get(rowId), SemanticType.TABLE_CELL));
                    }
                    rowIds.set(column.getColNumber(), rowId + 1);
                } else {
                    tableRow.add(new TableCell(SemanticType.TABLE_CELL));
                }
            }
            table.add(tableRow);
        }
        table.updateTableRows();

        if (table.getValidationScore() < TableUtils.TABLE_PROBABILITY_THRESHOLD) {
            return null;
        }
        return table;
    }

    private Long generateClusterId() {
        return clusterCounter++;
    }

    public TableBorder getTableBorder() {
        return tableBorder;
    }

    public void setTableBorder(TableBorder tableBorder) {
        this.tableBorder = tableBorder;
    }
}
