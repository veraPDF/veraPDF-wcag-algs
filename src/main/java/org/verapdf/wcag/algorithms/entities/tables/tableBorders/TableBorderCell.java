package org.verapdf.wcag.algorithms.entities.tables.tableBorders;

import org.verapdf.wcag.algorithms.entities.BaseObject;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.MultiBoundingBox;
import org.verapdf.wcag.algorithms.entities.tables.TableToken;

import java.util.LinkedList;
import java.util.List;

public class TableBorderCell extends BaseObject {
    protected int rowNumber;
    protected int colNumber;
    protected int rowSpan;
    protected int colSpan;
    private final List<TableToken> content;
    private SemanticType semanticType;
    private INode node;

    public TableBorderCell(int rowNumber, int colNumber, int rowSpan, int colSpan, Long id) {
        super(new BoundingBox());
        this.rowNumber = rowNumber;
        this.colNumber = colNumber;
        this.rowSpan = rowSpan;
        this.colSpan = colSpan;
        content = new LinkedList<>();
        setRecognizedStructureId(id);
    }

    public TableBorderCell(INode node, int rowNumber, int colNumber) {
        super(node.getBoundingBox());
        this.node = node;
        this.rowSpan = (int) node.getAttributesDictionary().getRowSpan();
        this.colSpan = (int) node.getAttributesDictionary().getColSpan();
        this.rowNumber = rowNumber;
        this.colNumber = colNumber;
        content = new LinkedList<>();
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

    public BoundingBox getContentBoundingBox() {
        BoundingBox boundingBox = new MultiBoundingBox();
        for (TableToken token : content) {
            boundingBox.union(token.getBoundingBox());
        }
        return boundingBox;
    }
}
