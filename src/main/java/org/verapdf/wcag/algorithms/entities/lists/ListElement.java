package org.verapdf.wcag.algorithms.entities.lists;

import org.verapdf.wcag.algorithms.entities.content.TextInfoChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.tables.TableCell;
import org.verapdf.wcag.algorithms.entities.tables.TableTokenRow;

import java.util.ArrayList;
import java.util.List;

public class ListElement extends TextInfoChunk {

    private final List<TableTokenRow> content;
    private SemanticType semanticType;

    public ListElement(TableCell cell, SemanticType semanticType) {
        super(cell.getBoundingBox(), cell.getFontSize(), cell.getBaseLine());
        this.semanticType = semanticType;
        content = new ArrayList<>();
        content.addAll(cell.getContent());
    }

    public void setSemanticType(SemanticType semanticType) {
        this.semanticType = semanticType;
    }

    public SemanticType getSemanticType() {
        return semanticType;
    }

    public void add(TableTokenRow row) {
        content.add(row);
        super.add(row);
    }

    public void add(TableCell cell) {
        for (TableTokenRow tableTokenRow : cell.getContent()) {
            add(tableTokenRow);
        }
    }

    public List<TableTokenRow> getContent() {
        return content;
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public String getString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (TableTokenRow tokenRow : content) {
            stringBuilder.append(tokenRow.getString());
        }
        return stringBuilder.toString();
    }

}
