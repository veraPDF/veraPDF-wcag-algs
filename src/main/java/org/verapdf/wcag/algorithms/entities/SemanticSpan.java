package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

public class SemanticSpan extends SemanticTextNode {

    public SemanticSpan(SemanticSpan span) {
        super(span);
    }

    public SemanticSpan() {
        setSemanticType(SemanticType.SPAN);
    }

    public SemanticSpan(SemanticType initialSemanticType) {
        super(initialSemanticType);
        setSemanticType(SemanticType.SPAN);
    }

    public SemanticSpan(BoundingBox bbox) {
        super(bbox);
    }

    public SemanticSpan(BoundingBox bbox, SemanticType initialSemanticType) {
        super(bbox, initialSemanticType);
        setSemanticType(SemanticType.SPAN);
    }

    public SemanticSpan(TextChunk textChunk) {
        this();
        add(new TextLine(textChunk));
    }

    public SemanticSpan(TextChunk textChunk, SemanticType initialSemanticType) {
        this(initialSemanticType);
        add(new TextLine(textChunk));
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        return (o instanceof SemanticSpan);
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
