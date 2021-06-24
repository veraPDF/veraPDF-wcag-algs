package org.verapdf.wcag.algorithms.entities.tables;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.content.ImageChunk;

public class TableImageToken extends ImageChunk implements ITableToken {

    private INode node;

    public TableImageToken(ImageChunk chunk, INode node) {
        super(chunk.getBoundingBox());
        this.node = node;
    }

    @Override
    public INode getNode() {
        return node;
    }

    @Override
    public double getBaseLine() {
        return getBottomY();
    }

    @Override
    public double getHeight() {
        return getBoundingBox().getHeight();
    }
}
