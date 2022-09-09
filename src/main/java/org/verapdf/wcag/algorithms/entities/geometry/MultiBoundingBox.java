package org.verapdf.wcag.algorithms.entities.geometry;

import java.util.LinkedList;
import java.util.List;

public class MultiBoundingBox extends BoundingBox {

    private List<BoundingBox> boundingBoxes = new LinkedList<>();

    public MultiBoundingBox() {
        init(Double.MAX_VALUE, Double.MAX_VALUE, 0, 0);
        boundingBoxes.add(new BoundingBox());
    }

    public MultiBoundingBox(int pageNumber) {
        init(Double.MAX_VALUE, Double.MAX_VALUE, 0, 0);
        this.pageNumber = this.lastPageNumber = pageNumber;
        boundingBoxes.add(new BoundingBox(pageNumber));
    }

    public MultiBoundingBox(double[] bbox) {
        init(bbox[0], bbox[1], bbox[2], bbox[3]);
        boundingBoxes.add(new BoundingBox(bbox));
    }

    public MultiBoundingBox(int pageNumber, double[] bbox) {
        init(bbox[0], bbox[1], bbox[2], bbox[3]);
        this.pageNumber = this.lastPageNumber = pageNumber;
        boundingBoxes.add(new BoundingBox(pageNumber, bbox));
    }

    public MultiBoundingBox(int pageNumber, int lastPageNumber, double[] bbox) {
        init(bbox[0], bbox[1], bbox[2], bbox[3]);
        this.pageNumber = pageNumber;
        this.lastPageNumber = lastPageNumber;
        boundingBoxes.add(new BoundingBox(pageNumber, lastPageNumber, bbox));
    }

    public MultiBoundingBox(double left, double bottom, double right, double top) {
        init(left, bottom, right, top);
        boundingBoxes.add(new BoundingBox(left, bottom, right, top));
    }

    public MultiBoundingBox(int pageNumber, double left, double bottom, double right, double top) {
        init(left, bottom, right, top);
        this.pageNumber = this.lastPageNumber = pageNumber;
        boundingBoxes.add(new BoundingBox(pageNumber, left, bottom, right, top));
    }

    public MultiBoundingBox(int pageNumber, int lastPageNumber, double left, double bottom, double right, double top) {
        init(left, bottom, right, top);
        this.pageNumber = pageNumber;
        this.lastPageNumber = lastPageNumber;
        boundingBoxes.add(new BoundingBox(pageNumber, lastPageNumber, left, bottom, right, top));
    }

    public MultiBoundingBox(BoundingBox o) {
        init(o);
    }

    public static MultiBoundingBox union(BoundingBox first, BoundingBox second) {
        MultiBoundingBox result = new MultiBoundingBox(first);
        return result.union(second);
    }

    @Override
    public MultiBoundingBox union(BoundingBox second) {
        if (second == null || second.getPageNumber() == null) {
            return this;
        }
        if (pageNumber == null) {
            init(second);
            return this;
        }
        if (second instanceof MultiBoundingBox) {
            boundingBoxes.addAll(((MultiBoundingBox)second).boundingBoxes);
        } else {
            boundingBoxes.add(new BoundingBox(second));
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

    //ToDo: cross methods

    @Override
    public BoundingBox scale(double coeffX, double coeffY) {
        for (BoundingBox boundingBox : boundingBoxes) {
            boundingBox.scale(coeffX, coeffY);
        }
        return super.scale(coeffX, coeffY);
    }

    @Override
    public BoundingBox move(double xShift, double yShift) {
        for (BoundingBox boundingBox : boundingBoxes) {
            boundingBox.move(xShift, yShift);
        }
        return super.move(xShift, yShift);
    }

    @Override
    public void init(BoundingBox rect) {
        super.init(rect);

        boundingBoxes = new LinkedList<>();
        if (rect instanceof MultiBoundingBox) {
            boundingBoxes.addAll(((MultiBoundingBox)rect).boundingBoxes);
        } else {
            boundingBoxes.add(new BoundingBox(rect));
        }
    }

    @Override
    public void init(double left, double bottom, double right, double top) {
        super.init(left, bottom, right, top);

        boundingBoxes = new LinkedList<>();
        boundingBoxes.add(new BoundingBox(this));
    }

    public List<BoundingBox> getBoundingBoxes() {
        return boundingBoxes;
    }

    public Double getRightX(int pageNumber) {
        if (this.pageNumber > pageNumber || this.lastPageNumber < pageNumber) {
            return null;
        }
        if (this.pageNumber == pageNumber && this.lastPageNumber == pageNumber) {
            return getRightX();
        }
        Double maxRight = null;
        for (BoundingBox box : boundingBoxes) {
            Double right = box.getRightX(pageNumber);
            if (maxRight == null || (right != null && maxRight < right)) {
                maxRight = right;
            }
        }
        return maxRight;
    }

    public Double getLeftX(int pageNumber) {
        if (this.pageNumber > pageNumber || this.lastPageNumber < pageNumber) {
            return null;
        }
        if (this.pageNumber == pageNumber && this.lastPageNumber == pageNumber) {
            return getLeftX();
        }
        Double minLeft = null;
        for (BoundingBox box : boundingBoxes) {
            Double left = box.getLeftX(pageNumber);
            if (minLeft == null || (left != null && minLeft > left)) {
                minLeft = left;
            }
        }
        return minLeft;
    }
}
