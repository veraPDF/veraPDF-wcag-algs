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

public class SemanticTextNode extends SemanticNode {
    protected final List<TextLine> lines;

    public SemanticTextNode(SemanticTextNode textNode) {
        super(textNode.getBoundingBox(), textNode.getInitialSemanticType(), textNode.getSemanticType());
        lines = new ArrayList<>(textNode.getLines());
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
    }

    public void addAll(List<TextLine> text) {
        if (text == null || text.size() == 0) {
            return;
        }
        lines.addAll(text);
        for (TextLine textLine : text) {
            getBoundingBox().union(textLine.getBoundingBox());
        }
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
    }

    public void setLastLine(TextLine lastLine) {
        if (!lines.isEmpty()) {
            lines.set(lines.size() - 1, lastLine);
        } else {
            lines.add(lastLine);
        }
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
        Map<Double, Double> weightMap = new HashMap<>();
        for (TextLine line : lines) {
            for (TextChunk chunk : line.getTextChunks()) {
                if (!TextChunkUtils.isWhiteSpaceChunk(chunk)) {
                    Double weight = chunk.getFontWeight();
                    Double weightLength = weightMap.get(weight);
                    if (weightLength == null) {
                        weightMap.put(weight, chunk.getBoundingBox().getWidth());
                    } else {
                        weightMap.put(weight, weightLength + chunk.getBoundingBox().getWidth());
                    }
                }
            }
        }
        double commonWeight = 0.0;
        double commonWeightLength = 0.0;
        for (Map.Entry<Double, Double> weight : weightMap.entrySet()) {
            if (commonWeightLength < weight.getValue()) {
                commonWeightLength = weight.getValue();
                commonWeight = weight.getKey();
            }
        }
        return commonWeight;
    }

    public boolean hasFullLines() {
        if (!isEmpty()) {
            return getFirstLine().isFullLine() && getLastLine().isFullLine();
        }
        return true;
    }

    public double getFontSize() {
        Map<Double, Double> sizeMap = new HashMap<>();
        for (TextLine line : lines) {
            for (TextChunk chunk : line.getTextChunks()) {
                if (!TextChunkUtils.isWhiteSpaceChunk(chunk)) {
                    Double size = chunk.getFontSize();
                    Double sizeLength = sizeMap.get(size);
                    if (sizeLength == null) {
                        sizeMap.put(size, chunk.getBoundingBox().getWidth());
                    } else {
                        sizeMap.put(size, sizeLength + chunk.getBoundingBox().getWidth());
                    }
                }
            }
        }
        double commonSize = 0.0;
        double commonSizeLength = 0.0;
        for (Map.Entry<Double, Double> weight : sizeMap.entrySet()) {
            if (commonSizeLength < weight.getValue()) {
                commonSizeLength = weight.getValue();
                commonSize = weight.getKey();
            }
        }
        return commonSize;
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
