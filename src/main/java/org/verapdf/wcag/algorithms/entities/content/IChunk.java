package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

public interface IChunk {

    double getLeftX();

    double getRightX();

    double getBottomY();

    double getTopY();

    void setPageNumber(int pageNumber);

    Integer getPageNumber();

    void setLastPageNumber(int lastPageNumber);

    Integer getLastPageNumber();

    void setBoundingBox(BoundingBox bbox);

    BoundingBox getBoundingBox();
}
