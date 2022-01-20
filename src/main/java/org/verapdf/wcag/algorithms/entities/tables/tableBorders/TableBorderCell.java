package org.verapdf.wcag.algorithms.entities.tables.tableBorders;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.tables.TableToken;

import java.util.LinkedList;
import java.util.List;

public class TableBorderCell {
    protected int rowNumber;
    protected int colNumber;
    protected int rowSpan;
    protected int colSpan;
    private BoundingBox boundingBox;
    private final List<TableToken> content;
    private SemanticType semanticType;
    private INode node;

    public TableBorderCell(int rowNumber, int colNumber, int rowSpan, int colSpan) {
        this.rowNumber = rowNumber;
        this.colNumber = colNumber;
        this.rowSpan = rowSpan;
        this.colSpan = colSpan;
        content = new LinkedList<>();
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
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

    public void addContent(TableToken token) {
        content.add(token);
    }

    public List<TableToken> getContent() {
        return content;
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

    public int getColNumber() {
        return colNumber;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public int getRowSpan() {
        return rowSpan;
    }

    public int getColSpan() {
        return colSpan;
    }
}
