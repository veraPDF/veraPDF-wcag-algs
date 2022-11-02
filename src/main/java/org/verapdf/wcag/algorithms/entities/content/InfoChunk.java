package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.BaseObject;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import java.util.Objects;

public abstract class InfoChunk extends BaseObject implements IChunk {

    public InfoChunk() {
        super(new BoundingBox());
    }

    public InfoChunk(BoundingBox bbox) {
        super(new BoundingBox());
        setBoundingBox(bbox);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getBoundingBox());
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
        return that.getBoundingBox().equals(getBoundingBox());
    }

    protected void unionBoundingBox(BoundingBox bbox) {
        getBoundingBox().union(bbox);
    }
}
