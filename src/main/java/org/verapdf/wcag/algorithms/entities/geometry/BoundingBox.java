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
package org.verapdf.wcag.algorithms.entities.geometry;

import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ContextUtils;

import java.util.Arrays;
import java.util.Objects;

public class BoundingBox {
    protected Integer pageNumber;
    protected Integer lastPageNumber;
    protected double leftX;
    protected double bottomY;
    protected double rightX;
    protected double topY;

    private final static double EPSILON = 1.e-18;

    public BoundingBox() {
        init(Double.MAX_VALUE, Double.MAX_VALUE, 0, 0);
    }

    public BoundingBox(Integer pageNumber) {
        init(Double.MAX_VALUE, Double.MAX_VALUE, 0, 0);
        this.pageNumber = this.lastPageNumber = pageNumber;
    }

    public BoundingBox(double[] bbox) {
        init(Math.min(bbox[0], bbox[2]), Math.min(bbox[1], bbox[3]), Math.max(bbox[0], bbox[2]), Math.max(bbox[1], bbox[3]));
    }

    public BoundingBox(Integer pageNumber, double[] bbox) {
        init(Math.min(bbox[0], bbox[2]), Math.min(bbox[1], bbox[3]), Math.max(bbox[0], bbox[2]), Math.max(bbox[1], bbox[3]));
        this.pageNumber = this.lastPageNumber = pageNumber;
    }

    public BoundingBox(Integer pageNumber, Integer lastPageNumber, double[] bbox) {
        init(Math.min(bbox[0], bbox[2]), Math.min(bbox[1], bbox[3]), Math.max(bbox[0], bbox[2]), Math.max(bbox[1], bbox[3]));
        this.pageNumber = pageNumber;
        this.lastPageNumber = lastPageNumber;
    }

    public BoundingBox(double left, double bottom, double right, double top) {
        init(Math.min(left, right), Math.min(bottom, top), Math.max(left, right), Math.max(bottom, top));
    }

    public BoundingBox(Integer pageNumber, double left, double bottom, double right, double top) {
        init(Math.min(left, right), Math.min(bottom, top), Math.max(left, right), Math.max(bottom, top));
        this.pageNumber = this.lastPageNumber = pageNumber;
    }

    public BoundingBox(Integer pageNumber, Integer lastPageNumber, double left, double bottom, double right, double top) {
        init(Math.min(left, right), Math.min(bottom, top), Math.max(left, right), Math.max(bottom, top));
        this.pageNumber = pageNumber;
        this.lastPageNumber = lastPageNumber;
    }

    public BoundingBox(BoundingBox o) {
        init(o);
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
        if (lastPageNumber == null || lastPageNumber < pageNumber) {
            this.lastPageNumber = pageNumber;
        }
    }

    public void setLastPageNumber(Integer lastPageNumber) {
        this.lastPageNumber = lastPageNumber;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public Integer getLastPageNumber() {
        return lastPageNumber;
    }

    public static BoundingBox union(BoundingBox first, BoundingBox second) {
        BoundingBox result = new BoundingBox(first);
        return result.union(second);
    }

    public BoundingBox union(BoundingBox second) {
        if (second == null || second.pageNumber == null) {
            return this;
        }
        if (pageNumber == null) {
            init(second);
            return this;
        }
        if (second.leftX < this.leftX) {
            this.leftX = second.leftX;
        }
        if (this.rightX < second.rightX) {
            this.rightX = second.rightX;
        }
        if (this.pageNumber.equals(second.pageNumber)) {
            this.topY = Math.max(this.topY, second.topY);
        }
        else if (second.pageNumber < this.pageNumber) {
            this.topY = second.topY;
            this.pageNumber = second.pageNumber;
        }
        if (this.lastPageNumber.equals(second.lastPageNumber)) {
            this.bottomY = Math.min(this.bottomY, second.bottomY);
        }
        else if (this.lastPageNumber < second.lastPageNumber) {
            this.bottomY = second.bottomY;
            this.lastPageNumber = second.lastPageNumber;
        }
        return this;
    }

    public BoundingBox normalize() {
        if (pageNumber.equals(lastPageNumber)) {
            return normalize(leftX, bottomY, rightX, topY);
        }
        return normalize(leftX, rightX);
    }

    public double getWidth() {
        return rightX > leftX ? rightX - leftX : 0;
    }

    public double getHeight() {
        return (pageNumber != null && pageNumber.equals(lastPageNumber) && topY > bottomY) ? topY - bottomY : 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(leftX);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(bottomY);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(rightX);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(topY);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + Objects.hash(pageNumber, lastPageNumber);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BoundingBox that = (BoundingBox) o;

        if (!Objects.equals(pageNumber, that.pageNumber)) {
            return false;
        }

        if (!Objects.equals(lastPageNumber, that.lastPageNumber)) {
            return false;
        }

        if (Math.abs(that.leftX - leftX) > EPSILON) {
            return false;
        }
        if (Math.abs(that.bottomY - bottomY) > EPSILON) {
            return false;
        }
        if (Math.abs(that.rightX - rightX) > EPSILON) {
            return false;
        }
        return Math.abs(that.topY - topY) < EPSILON;
    }

    public static boolean areSameBoundingBoxes(BoundingBox boundingBox1, BoundingBox boundingBox2) {
        if (!Objects.equals(boundingBox1.getPageNumber(), boundingBox2.getPageNumber())) {
            return false;
        }
        if (Math.abs(boundingBox1.leftX - boundingBox2.leftX) > EPSILON) {
            return false;
        }
        if (Math.abs(boundingBox1.bottomY - boundingBox2.bottomY) > EPSILON) {
            return false;
        }
        if (Math.abs(boundingBox1.rightX - boundingBox2.rightX) > EPSILON) {
            return false;
        }
        return Math.abs(boundingBox1.topY - boundingBox2.topY) < EPSILON;
    }
    
    public static boolean areSameBoundingBoxesExcludingPages(BoundingBox boundingBox1, BoundingBox boundingBox2) {
        if (Math.abs(boundingBox1.leftX - boundingBox2.leftX) > EPSILON) {
            return false;
        }
        if (Math.abs(boundingBox1.bottomY - boundingBox2.bottomY) > EPSILON) {
            return false;
        }
        if (Math.abs(boundingBox1.rightX - boundingBox2.rightX) > EPSILON) {
            return false;
        }
        return Math.abs(boundingBox1.topY - boundingBox2.topY) < EPSILON;
    }

    public static boolean areOverlapsBoundingBoxesExcludingPages(BoundingBox boundingBox1, BoundingBox boundingBox2) {
        if (boundingBox1.leftX - boundingBox2.rightX > EPSILON) {
            return false;
        }
        if (boundingBox1.bottomY - boundingBox2.topY > EPSILON) {
            return false;
        }
        if (boundingBox2.leftX - boundingBox1.rightX > EPSILON) {
            return false;
        }
        return boundingBox2.bottomY - boundingBox1.topY < EPSILON;
    }


    public boolean overlaps(BoundingBox other) {
        return overlaps(other, EPSILON);
    }

    public boolean overlaps(BoundingBox other, double eps) {
        if (pageNumber == null || other.pageNumber == null) {
            return false;
        }
        return !notOverlaps(other, eps);
    }

    public boolean notOverlaps(BoundingBox other) {
        return notOverlaps(other, EPSILON);
    }

    public boolean notOverlaps(BoundingBox other, double eps) {
        if (pageNumber == null || other.pageNumber == null) {
            return true;
        }
        return leftX > (other.rightX + eps) || (rightX + eps) < other.leftX
                || pageNumber > other.lastPageNumber || lastPageNumber < other.pageNumber
                || (bottomY > (other.topY + eps) && lastPageNumber.equals(other.pageNumber))
                || ((topY + eps) < other.bottomY && pageNumber.equals(other.lastPageNumber));
    }

    public boolean contains(BoundingBox other) {
        return contains(other, EPSILON, EPSILON);
    }

    public boolean contains(BoundingBox other, double horizontalOffset, double verticalOffset) {
        if (pageNumber == null || other.pageNumber == null) {
            return false;
        }
        return leftX < (other.leftX + horizontalOffset) && other.rightX < (rightX + horizontalOffset)
               && (pageNumber < other.pageNumber
                   || (pageNumber.equals(other.pageNumber) && other.topY < (topY + verticalOffset)))
               && (lastPageNumber > other.lastPageNumber
                   || (lastPageNumber.equals(other.lastPageNumber) && bottomY < (other.bottomY + verticalOffset)));
    }

    public boolean weaklyContains(BoundingBox fullRectangle) {
        if (pageNumber == null || fullRectangle.pageNumber == null) {
            return false;
        }
        if (pageNumber.equals(fullRectangle.pageNumber)
                && lastPageNumber.equals(fullRectangle.pageNumber)
                && pageNumber.equals(lastPageNumber)) {
            BoundingBox part = cross(fullRectangle);
            return part != null && part.getArea() >= fullRectangle.getArea() * 0.7;
        }
        return contains(fullRectangle);
    }

    public static boolean areHorizontalOverlapping(BoundingBox first, BoundingBox second) {
        return areHorizontalOverlapping(first, second, 0.0d);
    }

    private static boolean areHorizontalOverlapping(BoundingBox first, BoundingBox second, double epsilon) {
        return first.getLeftX() + epsilon < second.getRightX() && second.getLeftX() + epsilon < first.getRightX();
    }

    public static boolean areVerticalOverlapping(BoundingBox first, BoundingBox second) {
        return areVerticalOverlapping(first, second, 0.0d);
    }

    private static boolean areVerticalOverlapping(BoundingBox first, BoundingBox second, double epsilon) {
        return first.getBottomY() + epsilon < second.getTopY() && second.getBottomY() + epsilon < first.getTopY();
    }
    
    public static BoundingBox cross(BoundingBox first, BoundingBox second) {
        BoundingBox result = new BoundingBox(first);
        return result.cross(second, 0, 0);
    }

    public BoundingBox cross(BoundingBox other) {
        return this.cross(other, 0,0);
    }

     public static BoundingBox cross(BoundingBox first, BoundingBox second, double horizontalOffset, double verticalOffset) {
        BoundingBox result = new BoundingBox(first);
        return result.cross(second, horizontalOffset, verticalOffset);
    }

    public BoundingBox cross(BoundingBox other, double horizontalOffset, double verticalOffset) {
        if (notOverlaps(other)) {
            return null;
        }

        if (leftX < other.leftX - horizontalOffset) {
            leftX = other.leftX - horizontalOffset;
        }
        if (rightX > other.rightX + horizontalOffset) {
            rightX = other.rightX + horizontalOffset;
        }
        if (pageNumber < other.pageNumber) {
            pageNumber = other.pageNumber;
            topY = other.topY;
        }
        else if (pageNumber.equals(other.pageNumber) && other.topY + verticalOffset < topY) {
            topY = other.topY + verticalOffset;
        }
        if (lastPageNumber > other.lastPageNumber) {
            lastPageNumber = other.lastPageNumber;
            bottomY = other.bottomY;
        }
        else if (lastPageNumber.equals(other.lastPageNumber) && bottomY < other.bottomY - verticalOffset) {
            bottomY = other.bottomY - verticalOffset;
        }

        return this;
    }

    public double getArea() {
        return isEmpty() ? 0 : getWidth() * getHeight();
    }

    public double getBottomY() {
        return bottomY;
    }

    public double getTopY() {
        return topY;
    }

    public double getLeftX() {
        return leftX;
    }

    public double getRightX() {
        return rightX;
    }

    public void setBottomY(double bottomY) {
        this.bottomY = bottomY;
    }

    public void setTopY(double topY) {
        this.topY = topY;
    }

    public void setLeftX(double leftX) {
        this.leftX = leftX;
    }

    public void setRightX(double rightX) {
        this.rightX = rightX;
    }

    public double getCenterX() {
        return 0.5 * (leftX + rightX);
    }

    public double getCenterY() {
        return 0.5 * (topY + bottomY);
    }

    public Double getRightX(int pageNumber) {
        if (this.pageNumber <= pageNumber && this.lastPageNumber >= pageNumber) {
            return getRightX();
        }
        return null;
    }

    public Double getLeftX(int pageNumber) {
        if (this.pageNumber <= pageNumber && this.lastPageNumber >= pageNumber) {
            return getLeftX();
        }
        return null;
    }

    public boolean isEmpty() {
        return pageNumber == null
                ||leftX > (rightX + EPSILON) || lastPageNumber < pageNumber
                || (pageNumber.equals(lastPageNumber) && bottomY > (topY + EPSILON));
    }

    public BoundingBox scale(double coeffX, double coeffY) {
        rightX = leftX + (rightX - leftX) * coeffX;
        if (pageNumber == null || pageNumber.equals(lastPageNumber)) {
            topY = bottomY + (topY - bottomY) * coeffY;
        }
        return this;
    }

    public BoundingBox move(double xShift, double yShift) {
        leftX += xShift;
        rightX += xShift;

        bottomY += yShift;
        topY += yShift;
        return this;
    }

    public void setSizes(double width, double height) {
        init(leftX, bottomY, leftX + width, bottomY + height);
    }

    public void init(BoundingBox rect) {
        init(rect.leftX, rect.bottomY, rect.rightX, rect.topY);
        pageNumber = rect.pageNumber;
        lastPageNumber = rect.lastPageNumber;
    }

    public BoundingBox getBoundingBox(int pageNumber) {
        if (this.pageNumber > pageNumber || this.lastPageNumber < pageNumber) {
            return null;
        }
        return this;
    }

    public boolean isOnePageBoundingBox() {
        return pageNumber != null && Objects.equals(pageNumber, lastPageNumber);
    }

    public boolean isSeveralPagesBoundingBox() {
        return pageNumber != null && !Objects.equals(pageNumber, lastPageNumber);
    }

    public void init(double left, double bottom, double right, double top) {
        this.leftX = left;
        this.bottomY = bottom;

        this.rightX = right;
        this.topY = top;
    }

    private BoundingBox normalize(double left, double bottom, double right, double top) {
        this.leftX = Math.min(left, right);
        this.rightX = Math.max(left, right);
        this.bottomY = Math.min(bottom, top);
        this.topY = Math.max(bottom, top);
        return this;
    }

    private BoundingBox normalize(double left, double right) {
        this.leftX = Math.min(left, right);
        this.rightX = Math.max(left, right);
        return this;
    }

    public double getIntersectionPercent(BoundingBox boundingBox) {
        if (!Objects.equals(getPageNumber(), boundingBox.getPageNumber())) {
            return 0.0;
        }
        double xIntersection = Math.min(Math.min(getWidth(), boundingBox.getWidth()),
                Math.min(getRightX() - boundingBox.getLeftX(), boundingBox.getRightX() - getLeftX()));
        double yIntersection = Math.min(Math.min(getHeight(), boundingBox.getHeight()),
                Math.min(getTopY() - boundingBox.getBottomY(), boundingBox.getTopY() - getBottomY()));
        if (xIntersection <= 0.0 || yIntersection <= 0.0) {
            return 0.0;
        }
        return (xIntersection / getWidth()) * (yIntersection / getHeight());
    }

    public double getVerticalIntersectionPercent(BoundingBox boundingBox) {
        if (!Objects.equals(getPageNumber(), boundingBox.getPageNumber())) {
            return 0.0;
        }
        double yIntersection = Math.min(Math.min(getHeight(), boundingBox.getHeight()),
                Math.min(getTopY() - boundingBox.getBottomY(), boundingBox.getTopY() - getBottomY()));
        if (yIntersection <= 0.0) {
            return 0.0;
        }
        return yIntersection / getHeight();
    }

    public double getVerticalGap(BoundingBox boundingBox) {
        if (boundingBox == null) {
            return 0.0;
        }
        if (areVerticalOverlapping(this, boundingBox)) {
            return 0.0;
        }
        if (this.getTopY() <= boundingBox.getBottomY()) {
            return boundingBox.getBottomY() - this.getTopY();
        }
        if (boundingBox.getTopY() <= this.getBottomY()) {
            return this.getBottomY() - boundingBox.getTopY();
        }
        return 0.0;
    }

    public String getLocation() {
        return ContextUtils.getContext(this);
    }

    @Override
    public String toString() {
        return Arrays.toString(new double[] {leftX, bottomY, rightX, topY});
    }
}
