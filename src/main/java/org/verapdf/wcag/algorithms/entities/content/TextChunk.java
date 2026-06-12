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
package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.enums.TextFormat;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.ContrastRatioConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.StreamInfo;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TextChunk extends TextInfoChunk {
    private String value;
    private String fontName;
    private double fontWeight;
    private double italicAngle;
    private double[] fontColor;
    private double contrastRatio = Integer.MAX_VALUE;
    private boolean hasSpecialStyle = false;
    private boolean hasSpecialBackground = false;
    private double[] backgroundColor;
    private boolean isUnderlinedText = false;
    private boolean isStrikethroughText = false;
    private TextFormat textFormat = TextFormat.NORMAL;
    private List<Double> symbolEnds;

    public TextChunk() {
    }

    public TextChunk(String value) {
        this.value = value;
    }

    public TextChunk(BoundingBox bbox, String value, double fontSize, double baseLine) {
        super(bbox, fontSize, baseLine);
        this.value = value;
    }

    public TextChunk(BoundingBox bbox, String value, String fontName, double fontSize, double fontWeight,
                     double italicAngle, double baseLine, double[] fontColor, List<Double> symbolEnds, double slantDegree) {
        this(bbox, value, fontName, fontSize, fontWeight, italicAngle, baseLine, fontColor, slantDegree);
        if (symbolEnds == null) {
            adjustSymbolEndsToBoundingBox(null);
        } else {
            this.symbolEnds = symbolEnds;
        }
    }

    public TextChunk(BoundingBox bbox, String value, String fontName, double fontSize, double fontWeight,
                     double italicAngle, double baseLine, double[] fontColor, double slantDegree) {
        super(bbox, fontSize, baseLine, slantDegree);
        this.value = value;
        this.fontName = fontName;
        this.fontWeight = fontWeight;
        this.italicAngle = italicAngle;
        this.fontColor = fontColor != null ? fontColor.clone() : null;
    }

    public TextChunk(TextChunk chunk) {
        super(chunk);
        this.value = chunk.value;
        this.fontName = chunk.fontName;
        this.fontWeight = chunk.fontWeight;
        this.italicAngle = chunk.italicAngle;
        this.fontColor = chunk.fontColor != null ? chunk.fontColor.clone() : null;
        this.contrastRatio = chunk.contrastRatio;
        this.hasSpecialStyle = chunk.hasSpecialStyle;
        this.hasSpecialBackground = chunk.hasSpecialBackground;
        this.backgroundColor = chunk.backgroundColor;
        this.isUnderlinedText = chunk.isUnderlinedText;
        this.isStrikethroughText = chunk.isStrikethroughText;
        this.textFormat = chunk.textFormat;
        this.symbolEnds = chunk.symbolEnds != null ? new ArrayList<>(chunk.symbolEnds) : null;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public double getFontWeight() {
        return fontWeight;
    }

    public void setFontWeight(double fontWeight) {
        this.fontWeight = fontWeight;
    }

    public double getItalicAngle() {
        return italicAngle;
    }

    public void setItalicAngle(double italicAngle) {
        this.italicAngle = italicAngle;
    }

    public double[] getFontColor() {
        return fontColor;
    }

    public void setFontColor(double[] fontColor) {
        this.fontColor = fontColor;
    }

    public double getContrastRatio() {
        return contrastRatio;
    }

    public void setContrastRatio(double contrastRatio) {
        this.contrastRatio = contrastRatio;
    }

    public boolean getHasSpecialStyle() {
        return hasSpecialStyle;
    }

    public void setHasSpecialStyle() {
        this.hasSpecialStyle = true;
    }

    public boolean getHasSpecialBackground() {
        return hasSpecialBackground;
    }

    public void setHasSpecialBackground() {
        this.hasSpecialBackground = true;
    }

    public double[] getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(double[] backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public boolean getIsUnderlinedText() {
        return isUnderlinedText;
    }

    public void setIsUnderlinedText() {
        this.isUnderlinedText = true;
    }

    public boolean getIsStrikethroughText() {
        return isStrikethroughText;
    }

    public void setIsStrikethroughText() {
        this.isStrikethroughText = true;
    }

    public TextFormat getTextFormat() {
        return textFormat;
    }

    public void setTextFormat(TextFormat textFormat) {
        this.textFormat = textFormat;
    }

    public List<Double> getSymbolEnds() {
        return symbolEnds;
    }

    public void setSymbolEnds(List<Double> symbolEnds) {
        this.symbolEnds = symbolEnds;
    }

    public Double getSymbolStartCoordinate(int index) {
        return index >= 0 && index < this.symbolEnds.size() ? this.symbolEnds.get(index) : null;
    }

    public Double getSymbolEndCoordinate(int index) {
        return index >= -1 && index < this.symbolEnds.size() - 1 ? this.symbolEnds.get(index + 1) : null;
    }
    
    public Integer getSymbolEndIndexByCoordinate(double coordinate) {
        for (int index = this.symbolEnds.size() - 1; index >= 0; index--) {
            if (symbolEnds.get(index) < coordinate) {
                return index;
            }
        }
        return null;
    }

    public Integer getSymbolStartIndexByCoordinate(double coordinate) {
        for (int index = 0; index < this.symbolEnds.size(); index++) {
            if (symbolEnds.get(index) >= coordinate) {
                return index;
            }
        }
        return null;
    }

    public Double getSymbolWidth(int index) {
        return index >= 0 && index < this.symbolEnds.size() - 1 ?
               Math.abs(this.symbolEnds.get(index + 1) - this.symbolEnds.get(index)) : null;
    }

    public void adjustSymbolEndsToBoundingBox(List<Double> symbolEnds) {
        double textStart = getTextStart();
        double textEnd = getTextEnd();
        if (symbolEnds == null) {
            this.symbolEnds = new ArrayList<>(value.length() + 1);
            double symbolEnd = textStart;
            this.symbolEnds.add(symbolEnd);
            double averageWidth = (textEnd - textStart) / value.length();
            for (int i = 0; i < value.length(); i++) {
                symbolEnd += averageWidth;
                this.symbolEnds.add(symbolEnd);
            }
            return;
        }
        double multiplier;
        if (NodeUtils.areCloseNumbers(symbolEnds.get(symbolEnds.size() - 1), symbolEnds.get(0))) {
            multiplier = 0;
        } else {
            multiplier = (textEnd - textStart) / (symbolEnds.get(symbolEnds.size() - 1) - symbolEnds.get(0));
        }
        this.symbolEnds = symbolEnds.stream().map(e -> textStart + e * multiplier).collect(Collectors.toList());
    }

    public double getAverageSymbolWidth() {
        return getTextLength() / getValue().length();
    }

    public double getTextLength() {
        if (isHorizontalText()) {
            return getBoundingBox().getWidth();
        }
        if (isVerticalText()) {
            return getBoundingBox().getHeight();
        }
        return getBoundingBox().getWidth();
    }

    public static TextChunk getTextChunk(TextChunk textChunk, int start, int end) {
        if (start == 0 && textChunk.getValue().length() == end) {
            return textChunk;
        }
        if (start >= end) {
            return null;
        }
        TextChunk newTextChunk = new TextChunk(textChunk);
        StreamInfo.updateStreamInfos(newTextChunk.getStreamInfos(), newTextChunk.getValue().length(), start, end);
        newTextChunk.setValue(textChunk.getValue().substring(start, end));
        newTextChunk.setSymbolEnds(textChunk.getSymbolEnds().subList(start, end + 1));
        if (newTextChunk.isHorizontalText() || newTextChunk.isVerticalText()) {
            newTextChunk.setTextStart(textChunk.getSymbolStartCoordinate(start));
            newTextChunk.setTextEnd(textChunk.getSymbolEndCoordinate(end - 1));
        }
        return newTextChunk;
    }

    public void addAll(List<TextChunk> otherChunks) {
        StringBuilder text = new StringBuilder(value);
        for (TextChunk chunk : otherChunks) {
            text.append(chunk.getValue());
            super.add(chunk);
        }
        value = text.toString();
    }

    public void add(TextChunk chunk) {
        value += chunk.getValue();
        super.add(chunk);
    }

    public boolean isEmpty() {
        return value.isEmpty() || (StaticContainers.getIsIgnoreCharactersWithoutUnicode() && value.matches("\u0000+"));
    }

    public boolean isWhiteSpaceChunk() {
        return TextChunkUtils.isWhiteSpaceChunk(this);
    }

    public void compressSpaces() {
        if (value != null && !value.isEmpty() && hasConsecutiveWhiteSpaces()) {
            StringBuilder newValue = new StringBuilder();
            List<Double> newSymbolEnds = new ArrayList<>();
            List<Integer> extraSpaceIndexes = new ArrayList<>();
            boolean lastWasSpace = false;
            newSymbolEnds.add(symbolEnds.get(0));

            for (int i = 0; i < value.length(); i++) {
                char currentChar = value.charAt(i);

                if (TextChunkUtils.isWhiteSpaceChar(currentChar)) {
                    if (!lastWasSpace) {
                        newValue.append(currentChar);
                        lastWasSpace = true;
                        newSymbolEnds.add(symbolEnds.get(i + 1));
                    } else {
                        newSymbolEnds.set(newSymbolEnds.size() - 1, symbolEnds.get(i + 1));
                        extraSpaceIndexes.add(i);
                    }
                } else {
                    newValue.append(currentChar);
                    lastWasSpace = false;
                    newSymbolEnds.add(symbolEnds.get(i + 1));
                }
            }
            this.value = newValue.toString();
            this.symbolEnds = newSymbolEnds;
            StreamInfo.updateStreamInfos(getStreamInfos(), extraSpaceIndexes);
        }
    }

    public boolean hasConsecutiveWhiteSpaces() {
        if (value == null || value.length() < 2) {
            return false;
        }

        boolean lastWasSpace = false;
        for (int i = 0; i < value.length(); i++) {
            if (TextChunkUtils.isWhiteSpaceChar(value.charAt(i))) {
                if (lastWasSpace) {
                    return true;
                }
                lastWasSpace = true;
            } else {
                lastWasSpace = false;
            }
        }
        return false;
    }

    public boolean isItalic() {
        return !NodeUtils.areCloseNumbers(italicAngle, 0);
    }

    public Color getTextColor() {
        return ContrastRatioConsumer.getTextColorFromComponentArray(fontColor);
    }

    public int getRoundedFontWeight() {
        int rounded = (int) Math.round(fontWeight / 100.0) * 100;

        if (rounded < 100) {
            return 100;
        }
        return Math.min(rounded, 900);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        TextChunk that = (TextChunk) o;
        return Double.compare(that.fontWeight, fontWeight) == 0
                && Double.compare(that.italicAngle, italicAngle) == 0
                && Objects.equals(value, that.value)
                && Objects.equals(fontName, that.fontName)
                && Arrays.equals(fontColor, that.fontColor);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hash(fontWeight, italicAngle);
        return result;
    }

    @Override
    public String toString() {
        return "TextChunk{" +
                "text='" + value + '\'' +
                ", fontName='" + fontName + '\'' +
                ", fontSize=" + maxFontSize +
                ", fontWeight=" + fontWeight +
                ", italicAngle=" + italicAngle +
                ", baseLine=" + baseLine +
                ", fontColor=" + Arrays.toString(fontColor) +
                '}';
    }
}
