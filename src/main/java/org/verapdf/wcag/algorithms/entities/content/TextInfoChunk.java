package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

public abstract class TextInfoChunk extends InfoChunk {
    protected double fontSize = 0d;
    protected double baseLine = 0d;

    public TextInfoChunk() {
    }

    public TextInfoChunk(BoundingBox bbox) {
        super(bbox);
    }

    public TextInfoChunk(BoundingBox bbox, double fontSize, double baseLine) {
        super(bbox);
        this.fontSize = fontSize;
        this.baseLine = baseLine;
    }

    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
    }

    public double getFontSize() {
        return fontSize;
    }

    public void setBaseLine(double baseLine) {
        this.baseLine = baseLine;
    }

    public double getBaseLine() {
        return baseLine;
    }
}
