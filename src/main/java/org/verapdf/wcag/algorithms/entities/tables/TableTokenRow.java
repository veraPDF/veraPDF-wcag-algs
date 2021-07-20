package org.verapdf.wcag.algorithms.entities.tables;

import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableClusterGap;

import java.util.Objects;

public class TableTokenRow extends TextLine {
    private Integer rowNumber = null;
    private TableClusterGap leftGap = null;
    private TableClusterGap rightGap = null;
    private TableToken.TableTokenType type;

    public TableTokenRow() {
    }

    public TableTokenRow(TableToken token) {
        super(token);
        type = token.getType();
    }

    public TableTokenRow(TableTokenRow row) {
        super(row);
        rowNumber = row.getRowNumber();
        leftGap = row.getLeftGap();
        rightGap = row.getRightGap();
        type = row.getType();
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

    public String getString() {
        return super.toString();
    }

    public TableToken.TableTokenType getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(Objects.toString(rowNumber));
        result.append(" : ");
        result.append(super.toString());
        return result.toString();
    }
}
