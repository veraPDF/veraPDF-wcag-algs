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

    public BoundingBox(int pageNumber) {
        init(Double.MAX_VALUE, Double.MAX_VALUE, 0, 0);
        this.pageNumber = this.lastPageNumber = pageNumber;
    }

    public BoundingBox(double[] bbox) {
        init(bbox[0], bbox[1], bbox[2], bbox[3]);
    }

    public BoundingBox(int pageNumber, double[] bbox) {
        init(bbox[0], bbox[1], bbox[2], bbox[3]);
        this.pageNumber = this.lastPageNumber = pageNumber;
    }

    public BoundingBox(int pageNumber, int lastPageNumber, double[] bbox) {
        init(bbox[0], bbox[1], bbox[2], bbox[3]);
        this.pageNumber = pageNumber;
        this.lastPageNumber = lastPageNumber;
    }

    public BoundingBox(double left, double bottom, double right, double top) {
        init(left, bottom, right, top);
    }

    public BoundingBox(int pageNumber, double left, double bottom, double right, double top) {
        init(left, bottom, right, top);
        this.pageNumber = this.lastPageNumber = pageNumber;
    }

    public BoundingBox(int pageNumber, int lastPageNumber, double left, double bottom, double right, double top) {
        init(left, bottom, right, top);
        this.pageNumber = pageNumber;
        this.lastPageNumber = lastPageNumber;
    }

    public BoundingBox(BoundingBox o) {
        init(o);
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        if (lastPageNumber == null || lastPageNumber < pageNumber) {
            this.lastPageNumber = pageNumber;
        }
    }

    public void setLastPageNumber(int lastPageNumber) {
        this.lastPageNumber = lastPageNumber;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public Integer getLastPageNumber() {
        return lastPageNumber;
    }

    static public BoundingBox union(BoundingBox first, BoundingBox second) {
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
        return Math.abs(that.topY - topY) > EPSILON;
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
        if (pageNumber == null || other.pageNumber == null) {
            return false;
        }
        return leftX < (other.leftX + EPSILON) && other.rightX < (rightX + EPSILON)
                && (pageNumber < other.pageNumber
                    || (pageNumber.equals(other.pageNumber) && other.topY < (topY + EPSILON)))
                && (lastPageNumber > other.lastPageNumber
                    || (lastPageNumber.equals(other.lastPageNumber) && other.bottomY < (bottomY + EPSILON)));
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
        else {
            return contains(fullRectangle);
        }
    }

    static public BoundingBox cross(BoundingBox first, BoundingBox second) {
        BoundingBox result = new BoundingBox(first);
        return result.cross(second);
    }

    public BoundingBox cross(BoundingBox other) {
        if (notOverlaps(other)) {
            return null;
        }

        if (leftX < other.leftX) {
            leftX = other.leftX;
        }
        if (rightX > other.rightX) {
            rightX = other.rightX;
        }
        if (pageNumber < other.pageNumber) {
            pageNumber = other.pageNumber;
            topY = other.topY;
        }
        else if (pageNumber.equals(other.pageNumber) && other.topY < topY) {
            topY = other.topY;
        }
        if (lastPageNumber > other.lastPageNumber) {
            lastPageNumber = other.lastPageNumber;
            bottomY = other.bottomY;
        }
        else if (lastPageNumber.equals(other.lastPageNumber) && bottomY < other.bottomY) {
            bottomY = other.bottomY;
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

    public double getCenterX() {
        return 0.5 * (leftX + rightX);
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

    public String getLocation() {
        return ContextUtils.getContext(this);
    }

    @Override
    public String toString() {
        return Arrays.toString(new double[] {leftX, bottomY, rightX, topY});
    }
}
