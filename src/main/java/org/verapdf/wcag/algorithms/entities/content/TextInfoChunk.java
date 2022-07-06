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
        if (isLeftRightHorizontalText() || isUpBottomVerticalText()) {
            if (chunk.getBaseLine() < baseLine) {
                baseLine = chunk.getBaseLine();
            }
        } else if (isRightLeftHorizontalText() || isBottomUpVerticalText()) {
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
        if (isLeftRightHorizontalText()) {
            return getLeftX();
        }
        if (isRightLeftHorizontalText()) {
            return getRightX();
        }
        if (isBottomUpVerticalText()) {
            return getBottomY();
        }
        if (isUpBottomVerticalText()) {
            return getTopY();
        }
        return getLeftX();
    }

    public boolean isHorizontalText() {
        return isLeftRightHorizontalText() || isRightLeftHorizontalText();
    }

    public boolean isRightLeftHorizontalText() {
        return NodeUtils.areCloseNumbers(180.0, Math.abs(slantDegree));
    }

    public boolean isLeftRightHorizontalText() {
        return NodeUtils.areCloseNumbers(0.0, Math.abs(slantDegree));
    }

    public boolean isVerticalText() {
        return NodeUtils.areCloseNumbers(90.0, Math.abs(slantDegree));
    }

    public boolean isBottomUpVerticalText() {
        return NodeUtils.areCloseNumbers(90.0, slantDegree);
    }

    public boolean isUpBottomVerticalText() {
        return NodeUtils.areCloseNumbers(-90.0, slantDegree);
    }

    public double getTextEnd() {
        if (isLeftRightHorizontalText()) {
            return getRightX();
        }
        if (isRightLeftHorizontalText()) {
            return getLeftX();
        }
        if (isBottomUpVerticalText()) {
            return getTopY();
        }
        if (isUpBottomVerticalText()) {
            return getBottomY();
        }
        return getRightX();
    }

    public double getTextCenter() {
        if (isHorizontalText()) {
            return getCenterX();
        }
        if (isVerticalText()) {
            return getCenterY();
        }
        return getCenterX();
    }

    public String getValue() {
        return "";
    }

    public double getFirstBaseLine() {
        return getBaseLine();
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
