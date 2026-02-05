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
package org.verapdf.wcag.algorithms.entities.text;

import org.verapdf.wcag.algorithms.entities.SemanticTextNode;

import java.util.Arrays;
import java.util.Objects;

public class TextStyle implements Comparable<TextStyle> {
    private Double fontWeight;
    private Double fontSize;
    private double[] textColor;
    private Double italicAngle;
    private String fontName;
    private Double maxFontSize;

    public TextStyle(Double fontWeight, Double fontSize, double[] textColor, Double italicAngle, String fontName, Double maxFontSize) {
        this.fontWeight = fontWeight;
        this.fontSize = fontSize;
        this.textColor = textColor;
        this.italicAngle = italicAngle;
        this.fontName = fontName;
        this.maxFontSize = maxFontSize;
    }

    public Double getFontWeight() {
        return fontWeight;
    }

    public void setFontWeight(Double fontWeight) {
        this.fontWeight = fontWeight;
    }

    public Double getFontSize() {
        return fontSize;
    }

    public void setFontSize(Double fontSize) {
        this.fontSize = fontSize;
    }


    public double[] getTextColor() {
        return textColor;
    }

    public void setTextColor(double[] textColor) {
        this.textColor = textColor;
    }

    public Double getItalicAngle() {
        return italicAngle;
    }

    public void setItalicAngle(Double italicAngle) {
        this.italicAngle = italicAngle;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public Double getMaxFontSize() {
        return maxFontSize;
    }

    public void setMaxFontSize(Double maxFontSize) {
        this.maxFontSize = maxFontSize;
    }
    
    public static boolean areSameTextStyles(TextStyle style1, TextStyle style2) {
        if (!Objects.equals(style1.fontWeight, style2.fontWeight)) {
            return false;
        }

        if (!Objects.equals(style1.fontName, style2.fontName)) {
            return false;
        }

        if (!Arrays.equals(style1.textColor, style2.textColor)) {
            return false;
        }

        if (!Objects.equals(style1.italicAngle, style2.italicAngle)) {
            return false;
        }

//        if (Math.abs(style1.maxFontSize -  style2.maxFontSize) > 1) {
//            return false;
//        }
        if (!Objects.equals(style1.maxFontSize, style2.maxFontSize)) {
            return false;
        }
        if (!Objects.equals(style1.fontSize, style2.fontSize)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int compareTo(TextStyle textStyle) {
        if (!Objects.equals(maxFontSize, textStyle.maxFontSize)) {
            return textStyle.maxFontSize - maxFontSize > 0 ? 1 : -1;
        }
        if (!Objects.equals(fontWeight, textStyle.fontWeight)) {
            return textStyle.fontWeight - fontWeight > 0 ? 1 : -1;
        }
        return 0;
    }
    
    public static TextStyle getTextStyle(SemanticTextNode semanticTextNode) {
        return new TextStyle(semanticTextNode.getFontWeight(), semanticTextNode.getFontSize(), semanticTextNode.getTextColor(), 
                semanticTextNode.getItalicAngle(), semanticTextNode.getFontName(), semanticTextNode.getMaxFontSize());
    }
}
