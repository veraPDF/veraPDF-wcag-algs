package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

public class SemanticTextNode extends SemanticNode {
    protected final List<TextLine> lines;

    private Double fontWeight;
    private Double fontSize;
    private double[] textColor;
    private Double italicAngle;
    private String fontName;

    public SemanticTextNode(SemanticTextNode textNode) {
        super(textNode.getBoundingBox(), textNode.getInitialSemanticType(), textNode.getSemanticType());
        lines = new ArrayList<>(textNode.getLines());
        this.fontWeight = textNode.fontWeight;
        this.fontSize = textNode.fontSize;
        this.textColor = textNode.textColor;
        this.italicAngle = textNode.italicAngle;
        this.fontName = textNode.fontName;
    }

    public SemanticTextNode() {
        lines = new ArrayList<>();
    }

    public SemanticTextNode(SemanticType initialSemanticType) {
        super(initialSemanticType);
        lines = new ArrayList<>();
    }

    public SemanticTextNode(BoundingBox bbox) {
        super(bbox);
        lines = new ArrayList<>();
    }

    public SemanticTextNode(BoundingBox bbox, SemanticType initialSemanticType) {
        super(bbox, initialSemanticType);
        this.lines = new ArrayList<>();
    }

    public SemanticTextNode(BoundingBox bbox, List<TextLine> lines) {
        super(bbox);
        this.lines = new ArrayList<>(lines);
    }

    public SemanticTextNode(TextChunk textChunk) {
        lines = new ArrayList<>();
        add(new TextLine(textChunk));
    }

    public SemanticTextNode(BoundingBox bbox, List<TextLine> lines, SemanticType initialSemanticType) {
        super(bbox, initialSemanticType);
        this.lines = new ArrayList<>(lines);
    }

    public SemanticTextNode(TextChunk textChunk, SemanticType initialSemanticType) {
        this(initialSemanticType);
        add(new TextLine(textChunk));
    }

    public void add(TextLine textLine) {
        lines.add(textLine);
        getBoundingBox().union(textLine.getBoundingBox());
        updateVariables();
    }

    public void addAll(List<TextLine> text) {
        if (text == null || text.size() == 0) {
            return;
        }
        lines.addAll(text);
        for (TextLine textLine : text) {
            getBoundingBox().union(textLine.getBoundingBox());
        }
        updateVariables();
    }

    private void updateVariables() {
        fontSize = null;
        fontWeight = null;
        textColor = null;
        italicAngle = null;
        fontName = null;
    }

    public List<TextLine> getLines() {
        return lines;
    }

    public int getLinesNumber() {
        return lines.size();
    }

    public TextLine getFirstLine() {
        if (!lines.isEmpty()) {
            return lines.get(0);
        }
        return null;
    }

    public void setFirstLine(TextLine firstLine) {
        if (!lines.isEmpty()) {
            lines.set(0, firstLine);
        } else {
            lines.add(firstLine);
        }
        updateVariables();
    }

    public void setLastLine(TextLine lastLine) {
        if (!lines.isEmpty()) {
            lines.set(lines.size() - 1, lastLine);
        } else {
            lines.add(lastLine);
        }
        updateVariables();
    }

    public TextLine getSecondLine() {
        if (lines.size() > 1) {
            return lines.get(1);
        }
        return null;
    }

    public TextLine getPenultLine() {
        if (lines.size() > 1) {
            return lines.get(lines.size() - 2);
        }
        return null;
    }

    public TextLine getLastLine() {
        if (!lines.isEmpty()) {
            return lines.get(lines.size() - 1);
        }
        return null;
    }

    public double getFirstBaseline() {
        if (!isEmpty()) {
            return lines.get(0).getBaseLine();
        }
        return 0.0;
    }

    public double getLastBaseline() {
        if (!isEmpty()) {
            return lines.get(lines.size() - 1).getBaseLine();
        }
        return 0.0;
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public double getFontWeight() {
        if (fontWeight == null) {
            fontWeight = calculateFontWeight();
        }
        return fontWeight;
    }

    private double calculateFontWeight() {
        Map<Double, Double> fontWeightMap = new HashMap<>();
        for (TextLine line : lines) {
            for (TextChunk chunk : line.getTextChunks()) {
                if (!TextChunkUtils.isWhiteSpaceChunk(chunk)) {
                    Double weightLength = fontWeightMap.get(chunk.getFontWeight());
                    fontWeightMap.put(chunk.getFontWeight(),
                                  ((weightLength == null) ? 0 : weightLength) + chunk.getBoundingBox().getWidth());
                }
            }
        }
        if (!fontWeightMap.isEmpty()) {
            return fontWeightMap.entrySet()
                                .stream()
                                .max(Comparator.comparingDouble(Map.Entry::getValue))
                                .get().getKey();
        }
        return 0.0;
    }

    public boolean hasFullLines() {
        if (!isEmpty()) {
            return getFirstLine().isFullLine() && getLastLine().isFullLine();
        }
        return true;
    }

    public double getFontSize() {
        if (fontSize == null) {
            fontSize = calculateFontSize();
        }
        return fontSize;
    }

    private double calculateFontSize() {
        Map<Double, Double> fontSizeMap = new HashMap<>();
        for (TextLine line : lines) {
            for (TextChunk chunk : line.getTextChunks()) {
                if (!TextChunkUtils.isWhiteSpaceChunk(chunk)) {
                    Double sizeLength = fontSizeMap.get(chunk.getFontSize());
                    fontSizeMap.put(chunk.getFontSize(),
                                ((sizeLength == null) ? 0 : sizeLength) + chunk.getBoundingBox().getWidth());
                }
            }
        }
        if (!fontSizeMap.isEmpty()) {
            return fontSizeMap.entrySet()
                              .stream()
                              .max(Comparator.comparingDouble(Map.Entry::getValue))
                              .get().getKey();
        }
        return 0.0;
    }

    public double getItalicAngle() {
        if (italicAngle == null) {
            italicAngle = calculateItalicAngle();
        }
        return italicAngle;
    }

    private double calculateItalicAngle() {
        Map<Double, Double> italicAngleMap = new HashMap<>();
        for (TextLine line : lines) {
            for (TextChunk chunk : line.getTextChunks()) {
                if (!TextChunkUtils.isWhiteSpaceChunk(chunk)) {
                    Double sizeLength = italicAngleMap.get(chunk.getItalicAngle());
                    italicAngleMap.put(chunk.getItalicAngle(),
                                    ((sizeLength == null) ? 0 : sizeLength) + chunk.getBoundingBox().getWidth());
                }
            }
        }
        if (!italicAngleMap.isEmpty()) {
            return italicAngleMap.entrySet()
                               .stream()
                               .max(Comparator.comparingDouble(Map.Entry::getValue))
                               .get().getKey();
        }
        return 0.0;
    }

    public double[] getTextColor() {
        if (textColor == null) {
            textColor = calculateTextColor();
        }
        return textColor;
    }

    private double[] calculateTextColor() {
        Map<double[], Double> textColorMap = new HashMap<>();
        for (TextLine line : lines) {
            for (TextChunk chunk : line.getTextChunks()) {
                if (!TextChunkUtils.isWhiteSpaceChunk(chunk)) {
                    Double fontNameLength = textColorMap.get(chunk.getFontColor());
                    textColorMap.put(chunk.getFontColor(),
                                    ((fontNameLength == null) ? 0 : fontNameLength) + chunk.getBoundingBox().getWidth());
                }
            }
        }
        if (!textColorMap.isEmpty()) {
            return textColorMap.entrySet()
                               .stream()
                               .max(Comparator.comparingDouble(Map.Entry::getValue))
                               .get().getKey();
        }
        return new double[]{0.0};
    }

    public String getFontName() {
        if (fontName == null) {
            fontName = calculateFontName();
        }
        return fontName;
    }

    private String calculateFontName() {
        Map<String, Double> fontNameMap = new HashMap<>();
        for (TextLine line : lines) {
            for (TextChunk chunk : line.getTextChunks()) {
                if (!TextChunkUtils.isWhiteSpaceChunk(chunk)) {
                    Double fontNameLength = fontNameMap.get(chunk.getFontName());
                    fontNameMap.put(chunk.getFontName(),
                                    ((fontNameLength == null) ? 0 : fontNameLength) + chunk.getBoundingBox().getWidth());
                }
            }
        }
        if (!fontNameMap.isEmpty()) {
            return fontNameMap.entrySet().stream().max(Comparator.comparingDouble(Map.Entry::getValue)).get().getKey();
        }
        return "";
    }

    public boolean isSpaceNode() {
        for (TextLine line : lines) {
            for (TextChunk chunk : line.getTextChunks()) {
                if (!TextChunkUtils.isWhiteSpaceChunk(chunk)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        if (!(o instanceof SemanticTextNode)) {
            return false;
        }
        SemanticTextNode that = (SemanticTextNode) o;
        return this.lines.equals(that.getLines());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + lines.size();
        for (TextLine textLine : lines) {
            result = 31 * result + textLine.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        if (lines.size() == 0) {
            return "SemanticTextNode{}";
        }
        StringBuilder result = new StringBuilder("SemanticTextNode{");
        result.append(lines.get(0));
        for (int i = 1; i < lines.size(); ++i) {
            result.append(", ");
            result.append(lines.get(i));
        }
        result.append("}");
        return result.toString();
    }
}
