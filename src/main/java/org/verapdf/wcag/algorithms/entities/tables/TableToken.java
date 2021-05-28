package org.verapdf.wcag.algorithms.entities.tables;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;

public class TableToken extends TextChunk {
    private INode node;

    public TableToken(TextChunk textChunk, INode node) {
        super(textChunk);
        this.node = node;
    }

    public INode getNode() {
        return node;
    }
}
