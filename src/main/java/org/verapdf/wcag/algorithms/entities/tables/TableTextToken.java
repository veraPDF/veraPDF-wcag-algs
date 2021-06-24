package org.verapdf.wcag.algorithms.entities.tables;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;

public class TableTextToken extends TextChunk implements ITableToken {
    private INode node;

    public TableTextToken(TextChunk textChunk, INode node) {
        super(textChunk);
        this.node = node;
    }

    @Override
    public double getHeight() {
        return fontSize;
    }

    @Override
    public INode getNode() {
        return node;
    }
}
