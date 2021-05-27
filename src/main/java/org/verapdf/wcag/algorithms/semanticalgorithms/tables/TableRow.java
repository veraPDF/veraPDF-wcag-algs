package org.verapdf.wcag.algorithms.semanticalgorithms.tables;

import org.verapdf.wcag.algorithms.entities.content.TextLine;

import java.util.Objects;

public class TableRow extends TextLine {
    private Integer rowNumber = null;
    private TableClusterGap leftGap = null;
    private TableClusterGap rightGap = null;

    public TableRow() {
    }

    public TableRow(TableToken token) {
        super(token);
    }

    public TableRow(TableRow row) {
        super(row);
        rowNumber = row.getRowNumber();
        leftGap = row.getLeftGap();
        rightGap = row.getRightGap();
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setLeftGap(TableClusterGap gap) {
        leftGap = gap;
    }

    public TableClusterGap getLeftGap() {
        return leftGap;
    }

    public void setRightGap(TableClusterGap gap) {
        rightGap = gap;
    }

    public TableClusterGap getRightGap() {
        return rightGap;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(Objects.toString(rowNumber));
        result.append(" : ");
        result.append(super.toString());
        return result.toString();
    }
}
