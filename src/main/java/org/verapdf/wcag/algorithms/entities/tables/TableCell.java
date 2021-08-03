package org.verapdf.wcag.algorithms.entities.tables;

import org.verapdf.wcag.algorithms.entities.content.TextInfoChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableCluster;

import java.util.ArrayList;
import java.util.List;

public class TableCell extends TextInfoChunk {
    private final List<TableTokenRow> content;
    private SemanticType semanticType = null;

    public TableCell() {
        content = new ArrayList<>();
    }

    public TableCell(SemanticType semanticType) {
        this();
        this.semanticType = semanticType;
    }

    public TableCell(TableTokenRow row, SemanticType semanticType) {
        super(row.getBoundingBox(), row.getFontSize(), row.getBaseLine());
        content = new ArrayList<>();
        content.add(row);
        this.semanticType = semanticType;
    }

    public TableCell(TableCluster cluster, SemanticType semanticType) {
        super(cluster.getBoundingBox(), cluster.getFontSize(), cluster.getBaseLine());
        content = new ArrayList<>();
        content.addAll(cluster.getRows());
        this.semanticType = semanticType;
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

    public List<TableTokenRow> getContent() {
        return content;
    }

    public TableTokenRow getFirstTokenRow() {
        if (content.isEmpty()) {
            return null;
        }
        return content.get(0);
    }

    public TableTokenRow getLastTokenRow() {
        if (content.isEmpty()) {
            return null;
        }
        return content.get(content.size() - 1);
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public void merge(TableCell other) {
        if (other.isEmpty()) {
            return;
        }
        content.addAll(other.getContent());
        super.add(other);
    }

    public boolean isTextCell() {
        for (TableTokenRow tableTokenRow : content) {
            if (tableTokenRow.getType() != TableToken.TableTokenType.TEXT) {
                return false;
            }
        }
        return true;
    }

    public String getString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (TableTokenRow tokenRow : content) {
            stringBuilder.append(tokenRow.getString());
        }
        return stringBuilder.toString();
    }
}
