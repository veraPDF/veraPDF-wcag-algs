package org.verapdf.wcag.algorithms.entities.tables;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.content.ImageChunk;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;

public class TableToken extends TextChunk {
    private final INode node;
    private final TableTokenType type;

    public TableToken(TextChunk textChunk, INode node) {
        super(textChunk);
        this.node = node;
        type = TableTokenType.TEXT;
    }

    public TableToken(ImageChunk imageChunk, INode node) {
        super(imageChunk.getBoundingBox(), "", imageChunk.getBoundingBox().getHeight(),
                imageChunk.getBoundingBox().getBottomY());
        this.node = node;
        type = TableTokenType.IMAGE;
    }

    public INode getNode() {
        return node;
    }

    public TableTokenType getType() {
        return type;
    }

    public enum TableTokenType {
        IMAGE,
        TEXT
    }
}
