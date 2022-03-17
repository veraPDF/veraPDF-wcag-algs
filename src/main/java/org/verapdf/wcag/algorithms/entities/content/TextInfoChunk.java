package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;

import java.util.Objects;

public abstract class TextInfoChunk extends InfoChunk {
    protected double fontSize = 0d;
    protected double baseLine = Double.MAX_VALUE;
    protected double slantDegree = 0.0;

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

    public TextInfoChunk(BoundingBox bbox, double fontSize, double baseLine, double slantDegree) {
        this(bbox, fontSize, baseLine);
        this.slantDegree = slantDegree;
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
        if (NodeUtils.areCloseNumbers(0.0, Math.abs(slantDegree)) ||
                NodeUtils.areCloseNumbers(90.0, Math.abs(slantDegree))) {
            if (chunk.getBaseLine() < baseLine) {
                baseLine = chunk.getBaseLine();
            }
        } else if (NodeUtils.areCloseNumbers(180.0, Math.abs(slantDegree)) ||
                NodeUtils.areCloseNumbers(-90.0, slantDegree)) {
            if (chunk.getBaseLine() > baseLine) {
                baseLine = chunk.getBaseLine();
            }
        } else {
            if (chunk.getBaseLine() < baseLine) {
                baseLine = chunk.getBaseLine();
            }
        }

        unionBoundingBox(chunk.getBoundingBox());
    }

    public double getSlantDegree() {
        return slantDegree;
    }

    public void setSlantDegree(double slantDegree) {
        this.slantDegree = slantDegree;
    }

    public double getTextStart() {
        if (NodeUtils.areCloseNumbers(0.0, Math.abs(slantDegree))) {
            return getLeftX();
        }
        if (NodeUtils.areCloseNumbers(180.0, Math.abs(slantDegree))) {
            return getRightX();
        }
        if (NodeUtils.areCloseNumbers(90.0, slantDegree)) {
            return getBottomY();
        }
        if (NodeUtils.areCloseNumbers(-90.0, slantDegree)) {
            return getTopY();
        }
        return getLeftX();
    }

    public double getTextEnd() {
        if (NodeUtils.areCloseNumbers(0.0, Math.abs(slantDegree))) {
            return getRightX();
        }
        if (NodeUtils.areCloseNumbers(180.0, Math.abs(slantDegree))) {
            return getLeftX();
        }
        if (NodeUtils.areCloseNumbers(90.0, slantDegree)) {
            return getTopY();
        }
        if (NodeUtils.areCloseNumbers(-90.0, slantDegree)) {
            return getBottomY();
        }
        return getRightX();
    }

    public double getTextCenter() {
        if (NodeUtils.areCloseNumbers(0.0, Math.abs(slantDegree)) ||
                NodeUtils.areCloseNumbers(180.0, Math.abs(slantDegree))) {
            return getCenterX();
        }
        if (NodeUtils.areCloseNumbers(90.0, Math.abs(slantDegree))) {
            return getCenterY();
        }
        return getCenterX();
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        TextInfoChunk that = (TextInfoChunk) o;
        return Double.compare(that.fontSize, fontSize) == 0 &&
                Double.compare(that.baseLine, baseLine) == 0 &&
                Double.compare(that.slantDegree, slantDegree) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hash(fontSize, baseLine);
        return result;
    }
}
