package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TextChunk extends TextInfoChunk {
    private String value;
    private String fontName;
    private double fontWeight;
    private double italicAngle;
    private double[] fontColor;
    private String fontColorSpace;
    private double contrastRatio;
    private boolean hasSpecialStyle = false;
    private boolean isUnderlinedText = false;

    public TextChunk() {
    }

    public TextChunk(BoundingBox bbox, String value, double fontSize, double baseLine) {
        super(bbox, fontSize, baseLine);
        this.value = value;
    }

    public TextChunk(BoundingBox bbox, String value, String fontName, double fontSize,
                     double fontWeight, double italicAngle, double baseLine, double[] fontColor, String fontColorSpace) {
        super(bbox, fontSize, baseLine);
        this.value = value;
        this.fontName = fontName;
        this.fontWeight = fontWeight;
        this.italicAngle = italicAngle;
        this.fontColor = fontColor.clone();
        this.fontColorSpace = fontColorSpace;
    }

    public TextChunk(TextChunk chunk) {
        this(chunk.getBoundingBox(), chunk.value, chunk.fontName, chunk.fontSize,
                chunk.fontWeight, chunk.italicAngle, chunk.baseLine, chunk.fontColor, chunk.fontColorSpace);
    }

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

    public boolean getIsUnderlinedText() {
        return isUnderlinedText;
    }

    public void setIsUnderlinedText() {
        this.isUnderlinedText = true;
    }

    public String getFontColorSpace() {
        return fontColorSpace;
    }

    public void setFontColorSpace(String fontColorSpace) {
        this.fontColorSpace = fontColorSpace;
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
                && Objects.equals(fontColorSpace, that.fontColorSpace)
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
                ", fontColorSpace='" + fontColorSpace + '\'' +
                '}';
    }
}
