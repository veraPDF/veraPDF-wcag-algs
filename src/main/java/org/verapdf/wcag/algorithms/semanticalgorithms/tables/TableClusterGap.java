package org.verapdf.wcag.algorithms.semanticalgorithms.tables;

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
}
