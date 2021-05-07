package org.verapdf.wcag.algorithms.semanticalgorithms.tables;

import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TableUtils;

import java.util.ArrayList;
import java.util.List;

public class TableRecognizer {

    private List<TableCluster> headers;
    private List<TableCluster> clusters;
    private Integer numRows;

    public TableRecognizer(TableRecognitionArea recognitionArea) {
        headers = recognitionArea.getHeaders();
        clusters = recognitionArea.getClusters();
    }

    public void recognize() {
        preprocess();
    }

    private void preprocess() {
        setupInitialRowsAndColumns();
    }

    private void setupInitialRowsAndColumns() {
        List<TableCluster> cleanClusters = new ArrayList<>();
        for (TableCluster cluster : clusters) {
            for (TextLine line : cluster.getLines()) {
                TableCluster cleanCluster = new TableCluster(line);
                cleanCluster.setHeader(cluster.getHeader());
                cleanCluster.setLastHeader(cluster.getLastHeader());
                cleanClusters.add(cleanCluster);
            }
        }
        clusters = cleanClusters;
        TableUtils.sortClusters(clusters);

        setupInitialRows();
        setupInitialColumns();
    }

    private void setupInitialRows() {
        int currentRow = 1;
        clusters.get(0).setRowNumber(0, currentRow);
        for (int i = 1; i < clusters.size(); ++i) {
            TableCluster curCluster = clusters.get(i);
            TableCluster prevCluster = clusters.get(i - 1);

            double fontSize = Math.max(curCluster.getFirstLine().getFontSize(),
                    prevCluster.getFirstLine().getFontSize());
            double baseLineTolerance = fontSize * TableUtils.ONE_LINE_TOLERANCE_FACTOR;

            if (prevCluster.getBaseLine() < curCluster.getBaseLine() + baseLineTolerance) {
                currentRow += 1;
            }
            curCluster.setRowNumber(0, currentRow);
        }
        numRows = currentRow + 1;
    }

    private void setupInitialColumns() {
        for (int i = 0; i < headers.size(); ++i) {
            TableCluster header = headers.get(i);
            header.setColNumber(i);
        }

        for (TableCluster cluster : clusters) {
            TableCluster header = cluster.getHeader();
            if (header != null) {
                cluster.setColNumber(header.getColNumber());
                cluster.setLastColNumber(cluster.getLastHeader().getColNumber());
            }
        }
    }
}
