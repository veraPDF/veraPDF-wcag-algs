package org.verapdf.wcag.algorithms.entities.tables;

import org.verapdf.wcag.algorithms.entities.content.InfoChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableCluster;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TableUtils;

import java.util.*;

public class Table extends InfoChunk {

    private static Long tableCounter = 0l;
    private static final double ROW_GAP_DIFF_TOLERANCE = 0.35; // Sensitive parameter !!!
    private static final double ROW_WIDTH_FACTOR = 1.2; // Validation parameter

    private Long id = tableCounter++;
    private List<TableRow> rows;

    private Double validationScore = null;

    public Table(List<TableCluster> headers) {
        rows = new ArrayList<>();
        TableRow headersRow = new TableRow(SemanticType.TABLE_HEADERS);
        for (TableCluster header : headers) {
            headersRow.add(new TableCell(header, SemanticType.TABLE_HEADER));
            getBoundingBox().union(header.getBoundingBox());
        }
        rows.add(headersRow);
    }

    public int numberOfRows() {
        return rows.size();
    }

    public int numberOfColumns() {
        if (rows.isEmpty()) {
            return 0;
        }
        return rows.get(0).getCells().size();
    }

    public List<TableRow> getRows() {
        return rows;
    }

    public Long getId() {
        return id;
    }

    public void add(TableRow row) {
        rows.add(row);
        getBoundingBox().union(row.getBoundingBox());
    }

    public void updateTableRows() {
        if (rows.size() < 2) {
            return;
        }

        int numColumns = numberOfColumns();
        List<Double> maxRowGaps = new ArrayList<>(numColumns);
        for (int col = 0; col < numColumns; ++col) {
            double maxGap = 0.0;
            double minGap = Double.MAX_VALUE;
            List<TableCell> firstRowCells = rows.get(1).getCells();
            TableTokenRow currentTokenRow = (col < firstRowCells.size()) ? firstRowCells.get(col).getLastTokenRow() : null;
            for (int i = 2; i < rows.size(); ++i) {
                List<TableCell> rowCells = rows.get(i).getCells();

                if (col < rowCells.size() && !rowCells.get(col).isEmpty()) {
                    if (currentTokenRow != null) {
                        double gap = TableUtils.getRowGapFactor(currentTokenRow, rowCells.get(col).getFirstTokenRow());

                        if (gap < minGap) {
                            minGap = gap;
                        }
                        if (maxGap < gap) {
                            maxGap = gap;
                        }
                    }
                    currentTokenRow = rowCells.get(col).getLastTokenRow();
                }
            }
            minGap += ROW_GAP_DIFF_TOLERANCE;
            maxRowGaps.add(maxGap > minGap ? minGap : 0d);
        }

        List<TableRow> result = new ArrayList<>();
        result.add(rows.get(0));
        result.add(rows.get(1));
        TableRow currentRow = rows.get(1);
        for (int i = 2; i < rows.size(); ++i) {

            if (areSeparateRows(currentRow, rows.get(i), maxRowGaps)) {
                currentRow = rows.get(i);
                result.add(currentRow);
            } else {
                currentRow.merge(rows.get(i));
            }
        }
        rows = result;
    }

    private boolean areSeparateRows(TableRow row, TableRow nextRow, List<Double> maxRowGaps) {
        boolean disconnected = true;
        int numColumns = Math.min(Math.min(row.getCells().size(), nextRow.getCells().size()), maxRowGaps.size());

        for (int col = 0; col < numColumns; ++col) {
            TableTokenRow tokenRow = row.getCells().get(col).getLastTokenRow();
            TableTokenRow nextTokenRow = nextRow.getCells().get(col).getFirstTokenRow();
            if (tokenRow != null && nextTokenRow != null) {
                double gap = TableUtils.getRowGapFactor(tokenRow, nextTokenRow);
                if (gap > maxRowGaps.get(col)) {
                    return true;
                }
                disconnected = false;
            }
        }
        return disconnected;
    }

    public double getValidationScore() {
        if (validationScore == null) {
            validate();
        }
        return validationScore;
    }

    public void validate() {
        if (rows.size() < 2 || numberOfColumns() < 2) {
            validationScore = 0.0;
            return;
        }

        double maxIntersection = 0d;
        for (int i = 2; i < rows.size(); ++i) {
            double prevRowBaseLine = rows.get(i - 1).getBaseLine();
            TableRow row = rows.get(i);

            for (TableCell cell : row.getCells()) {
                TableTokenRow firstTokenRow = cell.getFirstTokenRow();
                if (firstTokenRow != null) {
                    double rowWidth = firstTokenRow.getFontSize() * ROW_WIDTH_FACTOR;
                    double intersection = 1.0 - (prevRowBaseLine - firstTokenRow.getBaseLine()) / rowWidth;
                    if (maxIntersection < intersection) {
                        maxIntersection = intersection;
                    }
                }
            }
        }

        validationScore = Math.max(0d, 1d - maxIntersection);
    }
}
