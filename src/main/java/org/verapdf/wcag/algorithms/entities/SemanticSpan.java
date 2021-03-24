package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public class SemanticSpan extends SemanticNode {
    private List<TextChunk> lines;

    public SemanticSpan(SemanticSpan span) {
        super(span.getBoundingBox(), span.getInitialSemanticType(), span.getSemanticType());
        lines = new ArrayList<>(span.getLines());
    }

    public SemanticSpan() {
        setSemanticType(SemanticType.SPAN);
        lines = new ArrayList<>();
    }

    public SemanticSpan(SemanticType initialSemanticType) {
        super(initialSemanticType);
        setSemanticType(SemanticType.SPAN);
        lines = new ArrayList<>();
    }

    public SemanticSpan(BoundingBox bbox) {
        this(bbox, null);
    }

    public SemanticSpan(BoundingBox bbox, SemanticType initialSemanticType) {
        super(bbox, initialSemanticType, SemanticType.SPAN);
        this.lines = new ArrayList<>();
    }

    public SemanticSpan(TextChunk textChunk) {
        this();
        add(textChunk);
    }

    public SemanticSpan(TextChunk textChunk, SemanticType initialSemanticType) {
        this(initialSemanticType);
        add(textChunk);
    }

    public void add(TextChunk textChunk) {
        lines.add(textChunk);
        getBoundingBox().union(textChunk.getBoundingBox());
    }

    public void addAll(List<TextChunk> text) {
        if (text == null || text.size() == 0) {
            return;
        }
        lines.addAll(text);
        for (TextChunk textChunk : text) {
            getBoundingBox().union(textChunk.getBoundingBox());
        }
    }

    public List<TextChunk> getLines() {
        return lines;
    }

    public int getLinesNumber() {
        return lines.size();
    }

    public TextChunk getFirstLine() {
        if (lines.size() != 0) {
            return lines.get(0);
        }
        return null;
    }

    public TextChunk getSecondLine() {
        if (lines.size() > 1) {
            return lines.get(1);
        }
        return null;
    }

    public TextChunk getLastLine() {
        if (lines.size() != 0) {
            return lines.get(lines.size() - 1);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        SemanticSpan that = (SemanticSpan) o;
        return this.lines.equals(that.getLines());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + lines.size();
        for (TextChunk textChunk : lines) {
            result = 31 * result + textChunk.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        if (lines.size() == 0) {
            return "SemanticSpan{}";
        }
        StringBuilder result = new StringBuilder("SemanticSpan{");
        result.append(lines.get(0));
        for (int i = 1; i < lines.size(); ++i) {
            result.append(", ");
            result.append(lines.get(i));
        }
        result.append("}");
        return result.toString();
    }
}
