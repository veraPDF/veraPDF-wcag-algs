package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public class SemanticSpan extends SemanticNode {
    private final List<TextLine> lines;

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
        add(new TextLine(textChunk));
    }

    public SemanticSpan(TextChunk textChunk, SemanticType initialSemanticType) {
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
        if (lines.size() != 0) {
            return lines.get(0);
        }
        return null;
    }

    public void setFirstLine(TextLine firstLine) {
        if (lines.size() != 0) {
            lines.set(0, firstLine);
        } else {
            lines.add(firstLine);
        }
    }

    public void setLastLine(TextLine lastLine) {
        if (lines.size() != 0) {
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
        for (TextLine textLine : lines) {
            result = 31 * result + textLine.hashCode();
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
