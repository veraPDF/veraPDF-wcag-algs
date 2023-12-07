package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.enums.TextFormat;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;

import java.util.*;
import java.util.stream.Collectors;

public class TextChunk extends TextInfoChunk {
    private String value;
    private String fontName;
    private double fontWeight;
    private double italicAngle;
    private double[] fontColor;
    private double contrastRatio;
    private boolean hasSpecialStyle = false;
    private boolean hasSpecialBackground = false;
    private double[] backgroundColor;
    private boolean isUnderlinedText = false;
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
        this.fontColor = fontColor.clone();
    }

    public TextChunk(TextChunk chunk) {
        this(chunk.getBoundingBox(), chunk.value, chunk.fontName, chunk.fontSize, chunk.fontWeight, chunk.italicAngle,
                chunk.baseLine, chunk.fontColor, chunk.symbolEnds, chunk.slantDegree);
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
        return index >= 0 && index < this.symbolEnds.size() - 1 ? this.symbolEnds.get(index) : null;
    }

    public Double getSymbolEndCoordinate(int index) {
        return index >= 0 && index < this.symbolEnds.size() - 1 ? this.symbolEnds.get(index + 1) : null;
    }

    public Double getSymbolWidth(int index) {
        return index >= 0 && index < this.symbolEnds.size() - 1 ?
               this.symbolEnds.get(index + 1) - this.symbolEnds.get(index) : null;
    }

    public void adjustSymbolEndsToBoundingBox(List<Double> symbolEnds) {
        if (this.symbolEnds == null) {
            this.symbolEnds = new ArrayList<>(value.length() + 1);
            double symbolEnd = getTextStart();
            this.symbolEnds.add(symbolEnd);
            double averageWidth = getAverageSymbolWidth();
            if (isRightLeftHorizontalText() || isUpBottomVerticalText()) {
                for (int i = 0; i < value.length(); i++) {
                    symbolEnd -= averageWidth;
                    this.symbolEnds.add(symbolEnd);
                }
            } else {
                for (int i = 0; i < value.length(); i++) {
                    symbolEnd += averageWidth;
                    this.symbolEnds.add(symbolEnd);
                }
            }
            return;
        }
        double textStart = getTextStart();
        if (isRightLeftHorizontalText() || isUpBottomVerticalText()) {
            this.symbolEnds = symbolEnds.stream().map(e -> e - textStart).collect(Collectors.toList());
        } else {
            this.symbolEnds = symbolEnds.stream().map(e -> e + textStart).collect(Collectors.toList());
        }
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
        return value.isEmpty() || value.matches("\u0000+");
    }

    public boolean isWhiteSpaceChunk() {
        return TextChunkUtils.isWhiteSpaceChunk(this);
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
                ", fontSize=" + fontSize +
                ", fontWeight=" + fontWeight +
                ", italicAngle=" + italicAngle +
                ", baseLine=" + baseLine +
                ", fontColor=" + Arrays.toString(fontColor) +
                '}';
    }
}
