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
package org.verapdf.wcag.algorithms.semanticalgorithms.tocs;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.TOCDetectionConsumer;

public class TOCIInfo {

    private String text;
    private Integer destinationPageNumber;
    private INode destinationStructElem;
    private Integer pageNumberLabel;
    private double right;
    private double maxTextSize;
    private int index;
    private TextLine firstLine;

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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    
    public Integer getPageNumber() {
        return firstLine.getPageNumber();
    }
    
    public TextLine getTOCItemValue() {
        return firstLine;
    }
    
    public void setTOCItemValue(TextLine firstLine) {
        this.firstLine = firstLine;
    }
}
