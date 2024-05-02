package org.verapdf.wcag.algorithms.semanticalgorithms.tables;

import java.util.Objects;

public class TableClusterGap {
    private TableCluster link;
    private double gap;

    public TableClusterGap(TableCluster link, double gap) {
        this.link = link;
        this.gap = gap;
    }

    public void setLink(TableCluster link) {
        this.link = link;
    }

    public TableCluster getLink() {
        return link;
    }

    public void setGap(double gap) {
        this.gap = gap;
    }

    public double getGap() {
        return gap;
    }

    @Override
    public int hashCode() {
        return Objects.hash(link.hashCode(), gap);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!super.equals(o)) {
            return false;
        }
        TableClusterGap that = (TableClusterGap) o;
        return Objects.equals(gap, that.gap) &&
                Objects.equals(link.getId(), that.getLink().getId());
    }
}
