package org.verapdf.wcag.algorithms.entities.tables.tableBorders;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

public class TableBorderRow {
    public int rowNumber;
    protected TableBorderCell[] cells;
    private SemanticType semanticType;
    private INode node;
    private BoundingBox boundingBox;

    public TableBorderRow(int rowNumber, int numberOfColumns) {
        this.rowNumber = rowNumber;
        cells = new TableBorderCell[numberOfColumns];
    }

    public SemanticType getSemanticType() {
        return semanticType;
    }

    public void setSemanticType(SemanticType semanticType) {
        this.semanticType = semanticType;
    }

    public INode getNode() {
        return node;
    }

    public void setNode(INode node) {
        this.node = node;
    }

    public double getTopY() {
        return boundingBox.getTopY();
    }

    public double getBottomY() {
        return boundingBox.getBottomY();
    }

    public double getLeftX() {
        return boundingBox.getLeftX();
    }

    public double getRightX() {
        return boundingBox.getRightX();
    }

    public double getWidth() {
        return boundingBox.getWidth();
    }

    public double getHeight() {
        return boundingBox.getHeight();
    }

    public Integer getPageNumber() {
        return boundingBox.getPageNumber();
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public int getNumberOfCellWithContent() {
        int numberOfCellsWithContent = 0;
        for (int colNumber = 0; colNumber < cells.length; colNumber++) {
            TableBorderCell cell = cells[colNumber];
            if (cell.colNumber == colNumber && cell.rowNumber == rowNumber && !cell.getContent().isEmpty()) {
                numberOfCellsWithContent++;
            }
        }
        return numberOfCellsWithContent;
    }

    public int getNumberOfCells() {
        int numberOfCells = 0;
        for (int colNumber = 0; colNumber < cells.length; colNumber++) {
            TableBorderCell cell = cells[colNumber];
            if (cell.colNumber == colNumber && cell.rowNumber == rowNumber) {
                numberOfCells++;
            }
        }
        return numberOfCells;
    }

    public TableBorderCell[] getCells() {
        return cells;
    }

    public TableBorderCell getCell(int colNumber) {
        return cells[colNumber];
    }
}
