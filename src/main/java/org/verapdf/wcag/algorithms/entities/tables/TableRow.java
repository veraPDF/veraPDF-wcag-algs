package org.verapdf.wcag.algorithms.entities.tables;

import org.verapdf.wcag.algorithms.entities.content.TextInfoChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.MultiBoundingBox;

import java.util.ArrayList;
import java.util.List;

public class TableRow extends TextInfoChunk {
    private Long id;
    private final List<TableCell> cells;
    private SemanticType semanticType = null;

    public TableRow() {
        super(new MultiBoundingBox());
        cells = new ArrayList<>();
    }

    public TableRow(SemanticType semanticType, Long id) {
        this();
        this.semanticType = semanticType;
        this.id = id;
    }

    public void setSemanticType(SemanticType semanticType) {
        this.semanticType = semanticType;
    }

    public SemanticType getSemanticType() {
        return semanticType;
    }

    public void add(TableCell cell) {
        cells.add(cell);
        super.add(cell);
    }

    public List<TableCell> getCells() {
        return cells;
    }

    public int getNumberOfCellsWithContent() {
        int numberOfCellsWithContent = 0;
        for (TableCell cell : cells) {
            if (!cell.getContent().isEmpty()) {
                numberOfCellsWithContent++;
            }
        }
        return numberOfCellsWithContent;
    }

    public void merge(TableRow other) {
        List<TableCell> otherCells = other.getCells();
        for (int i = 0; i < otherCells.size(); ++i) {
            if (i < cells.size()) {
                cells.get(i).merge(otherCells.get(i));
            } else {
                cells.add(otherCells.get(i));
            }
        }
        super.add(other);
    }

    public Long getId() {
        return id;
    }
}
