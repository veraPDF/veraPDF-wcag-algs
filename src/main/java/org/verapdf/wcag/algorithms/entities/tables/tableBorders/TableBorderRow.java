package org.verapdf.wcag.algorithms.entities.tables.tableBorders;

import org.verapdf.wcag.algorithms.entities.BaseObject;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

public class TableBorderRow extends BaseObject {
    private final int rowNumber;
    protected final TableBorderCell[] cells;
    private SemanticType semanticType;
    private INode node;

    public TableBorderRow(int rowNumber, int numberOfColumns, Long id) {
        super(new BoundingBox());
        this.rowNumber = rowNumber;
        cells = new TableBorderCell[numberOfColumns];
        setRecognizedStructureId(id);
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

    public int getRowNumber() {
        return rowNumber;
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
