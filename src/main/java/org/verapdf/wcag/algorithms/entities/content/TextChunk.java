package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.enums.TextFormat;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;

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
    private boolean isUnderlinedText = false;
    private TextFormat textFormat = TextFormat.NORMAL;
    private List<Double> symbolEnds;

    public TextChunk() {
    }

    public TextChunk(BoundingBox bbox, String value, double fontSize, double baseLine) {
        super(bbox, fontSize, baseLine);
        this.value = value;
    }

    public TextChunk(BoundingBox bbox, String value, String fontName, double fontSize, double fontWeight,
                double italicAngle, double baseLine, double[] fontColor, List<Double> symbolEnds, double slantDegree) {
        this(bbox, value, fontName, fontSize, fontWeight, italicAngle, baseLine, fontColor, slantDegree);
        this.symbolEnds = symbolEnds;
        adjustSymbolEndsToBoundingBox();
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
                chunk.baseLine, chunk.fontColor, chunk.slantDegree);
        this.symbolEnds = new LinkedList<>(chunk.symbolEnds);
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

    private void adjustSymbolEndsToBoundingBox() {
        if (this.symbolEnds == null) {
            symbolEnds = new ArrayList<>(value.length() + 1);
            double symbolEnd = getTextStart();
            symbolEnds.add(symbolEnd);
            double averageWidth = getAverageSymbolWidth();
            if (isRightLeftHorizontalText() || isUpBottomVerticalText()) {
                for (int i = 0; i < value.length(); i++) {
                    symbolEnd -= averageWidth;
                    symbolEnds.add(symbolEnd);
                }
            } else {
                for (int i = 0; i < value.length(); i++) {
                    symbolEnd += averageWidth;
                    symbolEnds.add(symbolEnd);
                }
            }
            return;
        }
        double textStart = getTextStart();
        if (isRightLeftHorizontalText() || isUpBottomVerticalText()) {
            this.symbolEnds = this.symbolEnds.stream().map(e -> e - textStart).collect(Collectors.toList());
        } else {
            this.symbolEnds = this.symbolEnds.stream().map(e -> e + textStart).collect(Collectors.toList());
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

    public static boolean areTextChunksHaveSameStyle(TextChunk firstTextChunk, TextChunk secondTextChunk) {
        return Objects.equals(firstTextChunk.fontName, secondTextChunk.fontName) &&
                NodeUtils.areCloseNumbers(firstTextChunk.fontWeight, secondTextChunk.fontWeight) &&
                NodeUtils.areCloseNumbers(firstTextChunk.italicAngle, secondTextChunk.italicAngle) &&
                Arrays.equals(firstTextChunk.fontColor, secondTextChunk.fontColor) &&
                NodeUtils.areCloseNumbers(firstTextChunk.fontSize, secondTextChunk.fontSize) &&
                NodeUtils.areCloseNumbers(firstTextChunk.slantDegree, secondTextChunk.slantDegree);
    }

    public static boolean areTextChunksHaveSameBaseLine(TextChunk firstTextChunk, TextChunk secondTextChunk) {
        return NodeUtils.areCloseNumbers(firstTextChunk.baseLine, secondTextChunk.baseLine);
    }

    public static TextChunk unionTextChunks(TextChunk textChunk, TextChunk secondTextChunk) {
        TextChunk newTextChunk = new TextChunk(textChunk);
        newTextChunk.setValue(textChunk.getValue() + secondTextChunk.getValue());
        newTextChunk.getBoundingBox().union(secondTextChunk.getBoundingBox());
        newTextChunk.getSymbolEnds().addAll(secondTextChunk.getSymbolEnds().subList(1, secondTextChunk.getSymbolEnds().size()));
        return newTextChunk;
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
