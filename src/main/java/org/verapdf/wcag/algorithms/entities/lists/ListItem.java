package org.verapdf.wcag.algorithms.entities.lists;

import org.verapdf.wcag.algorithms.entities.content.TextInfoChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.tables.TableRow;

public class ListItem extends TextInfoChunk {
    private final ListLabel label;
    private final ListBody body;
    private SemanticType semanticType;

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
}
