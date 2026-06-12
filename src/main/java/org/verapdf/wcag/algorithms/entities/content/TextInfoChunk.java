/*
 * This file is part of veraPDF wcag algorithms, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF wcag algorithms is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF wcag algorithms as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF wcag algorithms as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;

import java.util.Objects;

public abstract class TextInfoChunk extends InfoChunk {
    protected double maxFontSize = 0d;
    protected double baseLine = Double.MAX_VALUE;
    protected double slantDegree = 0.0;
    protected boolean isHiddenText = false;

    public TextInfoChunk() {
    }

    public TextInfoChunk(BoundingBox bbox) {
        super(bbox);
    }

    public TextInfoChunk(BoundingBox bbox, double maxFontSize, double baseLine) {
        super(bbox);
        this.maxFontSize = maxFontSize;
        this.baseLine = baseLine;
    }

    public TextInfoChunk(BoundingBox bbox, double maxFontSize, double baseLine, double slantDegree) {
        this(bbox, maxFontSize, baseLine);
        this.slantDegree = slantDegree;
    }

    public TextInfoChunk(TextInfoChunk textInfoChunk) {
        super(textInfoChunk);
        this.maxFontSize = textInfoChunk.maxFontSize;
        this.baseLine = textInfoChunk.baseLine;
        this.slantDegree = textInfoChunk.slantDegree;
        this.isHiddenText = textInfoChunk.isHiddenText;
    }

    public void setFontSize(double maxFontSize) {
        this.maxFontSize = maxFontSize;
    }

    public double getFontSize() {
        return maxFontSize;
    }

    public void setBaseLine(double baseLine) {
        this.baseLine = baseLine;
    }

    public double getBaseLine() {
        return baseLine;
    }

    protected void add(TextInfoChunk chunk) {
        if (maxFontSize < chunk.getFontSize()) {
            maxFontSize = chunk.getFontSize();
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

    public void setTextStart(double textStart) {
        if (isLeftRightHorizontalText()) {
            getBoundingBox().setLeftX(textStart);
        } else if (isRightLeftHorizontalText()) {
            getBoundingBox().setRightX(textStart);
        } else if (isBottomUpVerticalText()) {
            getBoundingBox().setBottomY(textStart);
        } else if (isUpBottomVerticalText()) {
            getBoundingBox().setTopY(textStart);
        } else {
            getBoundingBox().setLeftX(textStart);
        }
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

    public void setTextEnd(double textEnd) {
        if (isLeftRightHorizontalText()) {
            getBoundingBox().setRightX(textEnd);
        } else if (isRightLeftHorizontalText()) {
            getBoundingBox().setLeftX(textEnd);
        } else if (isBottomUpVerticalText()) {
            getBoundingBox().setTopY(textEnd);
        } else if (isUpBottomVerticalText()) {
            getBoundingBox().setBottomY(textEnd);
        } else {
            getBoundingBox().setRightX(textEnd);
        }
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

    public boolean isHiddenText() {
        return isHiddenText;
    }

    public void setHiddenText(boolean hiddenText) {
        isHiddenText = hiddenText;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        TextInfoChunk that = (TextInfoChunk) o;
        return Double.compare(that.maxFontSize, maxFontSize) == 0 &&
                Double.compare(that.baseLine, baseLine) == 0 &&
                Double.compare(that.slantDegree, slantDegree) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hash(maxFontSize, baseLine);
        return result;
    }
}
