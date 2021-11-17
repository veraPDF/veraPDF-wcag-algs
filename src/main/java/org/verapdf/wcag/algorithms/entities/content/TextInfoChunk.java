package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import java.util.Objects;

public abstract class TextInfoChunk extends InfoChunk {
    protected double fontSize = 0d;
    protected double baseLine = Double.MAX_VALUE;

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

    protected void add(TextInfoChunk chunk) {
        if (fontSize < chunk.getFontSize()) {
            fontSize = chunk.getFontSize();
        }
        if (chunk.getBaseLine() < baseLine) {
            baseLine = chunk.getBaseLine();
        }
        unionBoundingBox(chunk.getBoundingBox());
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        TextInfoChunk that = (TextInfoChunk) o;
        return Double.compare(that.fontSize, fontSize) == 0 &&
                Double.compare(that.baseLine, baseLine) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hash(fontSize, baseLine);
        return result;
    }
}
