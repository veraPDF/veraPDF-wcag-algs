package org.verapdf.wcag.algorithms.entities.lists;

import org.verapdf.wcag.algorithms.entities.IObject;
import org.verapdf.wcag.algorithms.entities.content.TextBlock;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.tables.TableRow;

import java.util.ArrayList;
import java.util.List;

public class ListItem extends TextBlock {
    private final ListLabel label;
    private final ListBody body;
    private SemanticType semanticType;
    private List<IObject> contents = new ArrayList<>();

    public ListItem(TableRow tableRow) {
        super(tableRow.getBoundingBox(), tableRow.getFontSize(), tableRow.getBaseLine());
        semanticType = SemanticType.LIST_ITEM;
        label = new ListLabel(tableRow.getCells().get(0));
        body = new ListBody(tableRow.getCells().get(1));
        setRecognizedStructureId(tableRow.getId());
    }

    public ListItem(BoundingBox boundingBox, Long id) {
        super(boundingBox);
        label = null;
        body = null;
        setRecognizedStructureId(id);
    }

    public void add(TableRow tableRow) {
        label.add(tableRow.getCells().get(0));
        body.add(tableRow.getCells().get(1));
        super.add(tableRow);
    }

    public void setSemanticType(SemanticType semanticType) {
        this.semanticType = semanticType;
    }

    public SemanticType getSemanticType() {
        return semanticType;
    }

    public ListLabel getLabel() {
        return label;
    }

    public ListBody getBody() {
        return body;
    }

    public List<IObject> getContents() {
        return contents;
    }

    public void setContents(List<IObject> contents) {
        this.contents = contents;
    }
}
