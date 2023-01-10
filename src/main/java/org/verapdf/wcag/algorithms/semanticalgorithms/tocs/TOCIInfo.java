package org.verapdf.wcag.algorithms.semanticalgorithms.tocs;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.TOCDetectionConsumer;

public class TOCIInfo {

    private String text;
    private Integer destinationPageNumber;
    private INode destinationStructElem;
    private Integer pageNumberLabel;
    private double right;
    private double maxTextSize;

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getTextForSearching() {
        return text.replaceAll(TOCDetectionConsumer.NON_CONTENT_REGEX,"").toUpperCase();
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

    public INode getDestinationStructElem() {
        return destinationStructElem;
    }

    public void setDestinationStructElem(INode destinationStructElem) {
        this.destinationStructElem = destinationStructElem;
    }
}
