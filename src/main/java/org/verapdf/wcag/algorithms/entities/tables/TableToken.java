package org.verapdf.wcag.algorithms.entities.tables;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.content.ImageChunk;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;

public class TableToken extends TextChunk {
    private INode node;

    public TableToken(TextChunk textChunk, INode node) {
        super(textChunk);
        this.node = node;
    }

    public TableToken(ImageChunk imageChunk, INode node) {
        super(imageChunk.getBoundingBox(), "", imageChunk.getBoundingBox().getHeight(),
                imageChunk.getBoundingBox().getBottomY());
        this.node = node;
    }

    public INode getNode() {
        return node;
    }
}
