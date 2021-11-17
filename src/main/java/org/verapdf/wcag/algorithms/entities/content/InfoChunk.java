package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import java.util.Objects;

public abstract class InfoChunk implements IChunk {
    private final BoundingBox boundingBox;

    public InfoChunk() {
        boundingBox = new BoundingBox();
    }

    public InfoChunk(BoundingBox bbox) {
        this.boundingBox = new BoundingBox(bbox);
    }

    @Override
    public double getLeftX() {
        return boundingBox.getLeftX();
    }

    @Override
    public double getBottomY() {
        return boundingBox.getBottomY();
    }

    @Override
    public double getRightX() {
        return boundingBox.getRightX();
    }

    @Override
    public double getTopY() {
        return boundingBox.getTopY();
    }

    @Override
    public double getCenterX() {
        return boundingBox.getCenterX();
    }

    @Override
    public double getCenterY() {
        return boundingBox.getCenterY();
    }

    @Override
    public void setLastPageNumber(int lastPageNumber) {
        boundingBox.setLastPageNumber(lastPageNumber);
    }

    @Override
    public void setPageNumber(int pageNumber) {
        boundingBox.setPageNumber(pageNumber);
    }

    @Override
    public Integer getPageNumber() {
        return boundingBox.getPageNumber();
    }

    @Override
    public Integer getLastPageNumber() {
        return boundingBox.getLastPageNumber();
    }

    @Override
    public void setBoundingBox(BoundingBox bbox) {
        boundingBox.init(bbox);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(boundingBox);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InfoChunk that = (InfoChunk) o;
        return that.boundingBox.equals(boundingBox);
    }

    protected void unionBoundingBox(BoundingBox bbox) {
        boundingBox.union(bbox);
    }
}
