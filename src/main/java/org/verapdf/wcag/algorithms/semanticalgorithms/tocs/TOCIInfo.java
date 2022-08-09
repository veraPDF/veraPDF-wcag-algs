package org.verapdf.wcag.algorithms.semanticalgorithms.tocs;

public class TOCIInfo {

    private String text;
    private Integer destinationPageNumber;
    private Integer pageNumberLabel;
    private double right;
    private double maxTextSize;

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setDestinationPageNumber(Integer destinationPageNumber) {
        this.destinationPageNumber = destinationPageNumber;
    }

    public Integer getDestinationPageNumber() {
        return destinationPageNumber;
    }

    public void setPageNumberLabel(Integer pageNumberLabel) {
        this.pageNumberLabel = pageNumberLabel;
    }

    public Integer getPageNumberLabel() {
        return pageNumberLabel;
    }

    public void setRight(double right) {
        this.right = right;
    }

    public double getRight() {
        return right;
    }

    public void setMaxTextSize(double maxTextSize) {
        this.maxTextSize = maxTextSize;
    }

    public double getMaxTextSize() {
        return maxTextSize;
    }
}
