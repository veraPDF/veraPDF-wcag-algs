package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextColumn;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.TextFormat;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.ArabicNumbersListLabelsDetectionAlgorithm;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.ListLabelsDetectionAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

public class SemanticTextNode extends SemanticNode {
    private final List<TextColumn> columns;

    private Double fontWeight;
    private Double fontSize;
    private double[] textColor;
    private Double italicAngle;
    private String fontName;
    private TextFormat textFormat = TextFormat.NORMAL;
    private Double maxFontSize;

    public SemanticTextNode(SemanticTextNode textNode) {
        super(textNode.getBoundingBox(), textNode.getInitialSemanticType(), textNode.getSemanticType());
        columns = new ArrayList<>(textNode.getColumns());
        this.fontWeight = textNode.fontWeight;
        this.fontSize = textNode.fontSize;
        this.textColor = textNode.textColor;
        this.italicAngle = textNode.italicAngle;
        this.fontName = textNode.fontName;
        this.textFormat = textNode.textFormat;
        this.maxFontSize = textNode.maxFontSize;
    }

    public SemanticTextNode() {
        columns = new ArrayList<>();
    }

    public SemanticTextNode(SemanticType initialSemanticType) {
        super(initialSemanticType);
        columns = new ArrayList<>();
    }

    public SemanticTextNode(BoundingBox bbox) {
        super(bbox);
        columns = new ArrayList<>();
    }

    public SemanticTextNode(BoundingBox bbox, SemanticType initialSemanticType) {
        super(bbox, initialSemanticType);
        this.columns = new ArrayList<>();
    }

    public SemanticTextNode(BoundingBox bbox, List<TextColumn> columns) {
        super(bbox);
        this.columns = new ArrayList<>(columns);
    }

    public SemanticTextNode(TextChunk textChunk) {
        columns = new ArrayList<>();
        add(new TextLine(textChunk));
    }

    public SemanticTextNode(TextChunk textChunk, SemanticType initialSemanticType) {
        this(initialSemanticType);
        add(new TextLine(textChunk));
    }

    public void add(TextLine textLine) {
        if (columns.isEmpty()) {
            columns.add(new TextColumn(textLine));
        } else {
            getLastColumn().add(textLine);
        }
        getBoundingBox().union(textLine.getBoundingBox());
        updateVariables();
    }

    public void addAll(List<TextColumn> text) {
        columns.addAll(text);
        updateVariables();
    }

    private void updateVariables() {
        fontSize = null;
        fontWeight = null;
        textColor = null;
        italicAngle = null;
        fontName = null;
        maxFontSize = null;
    }

    public List<TextColumn> getColumns() {
        return columns;
    }

    public int getLinesNumber() {
        int linesNumber = 0;
        for (TextColumn textColumn : columns) {
            linesNumber += textColumn.getLinesNumber();
        }
        return linesNumber;
    }

    public int getColumnsNumber() {
        return columns.size();
    }

    public TextLine getFirstLine() {
        if (!columns.isEmpty()) {
            if (!getFirstColumn().getLines().isEmpty()) {
                return getFirstColumn().getFirstLine();
            }
        }
        return null;
    }

    public void setFirstLine(TextLine firstLine) {
        if (!columns.isEmpty()) {
            getFirstColumn().setFirstLine(firstLine);
        } else {
            columns.add(new TextColumn(firstLine));
        }
        updateVariables();
    }

    public void setLastLine(TextLine lastLine) {
        if (!columns.isEmpty()) {
            getLastColumn().setLastLine(lastLine);
        } else {
            columns.add(new TextColumn(lastLine));
        }
        updateVariables();
    }

    public void setLastColumn(TextColumn lastColumn) {
        if (!columns.isEmpty()) {
            columns.set(columns.size() - 1, lastColumn);
        } else {
            columns.add(lastColumn);
        }
        updateVariables();
    }

    public TextColumn getFirstColumn() {
        if (columns.isEmpty()) {
            return null;
        }
        return columns.get(0);
    }

    public TextColumn getLastColumn() {
        if (columns.isEmpty()) {
            return null;
        }
        return columns.get(columns.size() - 1);
    }

    public TextLine getSecondLine() {//fix?
        if (!columns.isEmpty()) {
            return getFirstColumn().getSecondLine();
        }
        return null;
    }

    public TextLine getPenultLine() {//fix?
        if (!columns.isEmpty()) {
            return getLastColumn().getPenultLine();
        }
        return null;
    }

    public TextLine getLastLine() {
        return getLastColumn().getLastLine();
    }

    public double getFirstBaseline() {
        if (!isEmpty()) {
            double baseLine = -Double.MAX_VALUE;;
            for (TextColumn column : columns) {
                baseLine = Math.max(baseLine, column.getFirstLine().getBaseLine());
            }
            return baseLine;
        }
        return 0.0;
    }

    public double getLastBaseline() {
        if (!isEmpty()) {
            double baseLine = Double.MAX_VALUE;
            for (TextColumn column : columns) {
                baseLine = Math.min(baseLine, column.getLastLine().getBaseLine());
            }
            return baseLine;
        }
        return 0.0;
    }

    public boolean isEmpty() {
        return columns.isEmpty();
    }

    public double getFontWeight() {
        if (fontWeight == null) {
            fontWeight = calculateFontWeight();
        }
        return fontWeight;
    }

    private double calculateFontWeight() {
        Map<Double, Double> fontWeightMap = new HashMap<>();
        for (TextColumn column : columns) {
            for (TextLine line : column.getLines()) {
                for (TextChunk chunk : line.getTextChunks()) {
                    if (!TextChunkUtils.isWhiteSpaceChunk(chunk)) {
                        Double weightLength = fontWeightMap.get(chunk.getFontWeight());
                        fontWeightMap.put(chunk.getFontWeight(),
                                ((weightLength == null) ? 0 : weightLength) + chunk.getBoundingBox().getWidth());
                    }
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
        maxFontSize = 0.0;
        for (TextColumn column : columns) {
            for (TextLine line : column.getLines()) {
                for (TextChunk chunk : line.getTextChunks()) {
                    if (!TextChunkUtils.isWhiteSpaceChunk(chunk)) {
                        Double sizeLength = fontSizeMap.get(chunk.getFontSize());
                        fontSizeMap.put(chunk.getFontSize(),
                                ((sizeLength == null) ? 0 : sizeLength) + chunk.getBoundingBox().getWidth());
                        maxFontSize = Math.max(maxFontSize, chunk.getFontSize());
                    }
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
        for (TextColumn column : columns) {
            for (TextLine line : column.getLines()) {
                for (TextChunk chunk : line.getTextChunks()) {
                    if (!TextChunkUtils.isWhiteSpaceChunk(chunk)) {
                        Double sizeLength = italicAngleMap.get(chunk.getItalicAngle());
                        italicAngleMap.put(chunk.getItalicAngle(),
                                ((sizeLength == null) ? 0 : sizeLength) + chunk.getBoundingBox().getWidth());
                    }
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
        for (TextColumn column : columns) {
            for (TextLine line : column.getLines()) {
                for (TextChunk chunk : line.getTextChunks()) {
                    if (!TextChunkUtils.isWhiteSpaceChunk(chunk)) {
                        Double fontNameLength = textColorMap.get(chunk.getFontColor());
                        textColorMap.put(chunk.getFontColor(),
                                ((fontNameLength == null) ? 0 : fontNameLength) + chunk.getBoundingBox().getWidth());
                    }
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
        for (TextColumn column : columns) {
            for (TextLine line : column.getLines()) {
                for (TextChunk chunk : line.getTextChunks()) {
                    if (!TextChunkUtils.isWhiteSpaceChunk(chunk)) {
                        Double fontNameLength = fontNameMap.get(chunk.getFontName());
                        fontNameMap.put(chunk.getFontName(),
                                ((fontNameLength == null) ? 0 : fontNameLength) + chunk.getBoundingBox().getWidth());
                    }
                }
            }
        }
        if (!fontNameMap.isEmpty()) {
            return fontNameMap.entrySet().stream().max(Comparator.comparingDouble(Map.Entry::getValue)).get().getKey();
        }
        return "";
    }

    public TextFormat getTextFormat() {
        return textFormat;
    }

    public void setTextFormat(TextFormat textFormat) {
        this.textFormat = textFormat;
    }

    public Double getMaxFontSize() {
        if (maxFontSize == null) {
            calculateFontSize();
        }
        return maxFontSize;
    }

    public boolean isSpaceNode() {
        for (TextColumn column : columns) {
            for (TextLine line : column.getLines()) {
                for (TextChunk chunk : line.getTextChunks()) {
                    if (!TextChunkUtils.isWhiteSpaceChunk(chunk)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean isStartsWithArabicNumber() {
        String value = getFirstLine().getValue().trim();
        if (!value.isEmpty() && ListLabelsDetectionAlgorithm.getRegexStartLength(value,
                ArabicNumbersListLabelsDetectionAlgorithm.ARABIC_NUMBER_REGEX) > 0) {
            return true;
        }
        return false;
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
        return this.columns.equals(that.getColumns());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + columns.size();
        for (TextColumn textColumn : columns) {
            result = 31 * result + textColumn.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        if (columns.size() == 0) {
            return this.getClass().getName() + "{}";
        }
        StringBuilder result = new StringBuilder(this.getClass().getSimpleName());
        result.append("{");
        result.append(columns.get(0));
        for (int i = 1; i < columns.size(); ++i) {
            result.append(", ");
            result.append(columns.get(i));
        }
        result.append("}");
        return result.toString();
    }
}
