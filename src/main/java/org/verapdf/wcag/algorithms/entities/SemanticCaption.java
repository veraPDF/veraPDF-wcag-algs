package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

public class SemanticCaption extends SemanticTextNode {

    public SemanticCaption(SemanticCaption caption) {
        super(caption);
    }

    public SemanticCaption(SemanticTextNode textNode) {
        super(textNode);
    }

    public SemanticCaption() {
        setSemanticType(SemanticType.CAPTION);
    }

    public SemanticCaption(SemanticType initialSemanticType) {
        super(initialSemanticType);
        setSemanticType(SemanticType.CAPTION);
    }

    public SemanticCaption(BoundingBox bbox) {
        super(bbox);
    }

    public SemanticCaption(BoundingBox bbox, SemanticType initialSemanticType) {
        super(bbox, initialSemanticType);
        setSemanticType(SemanticType.CAPTION);
    }

    public SemanticCaption(TextChunk textChunk) {
        setSemanticType(SemanticType.CAPTION);
        add(new TextLine(textChunk));
    }

    public SemanticCaption(TextChunk textChunk, SemanticType initialSemanticType) {
        this(initialSemanticType);
        add(new TextLine(textChunk));
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        return (o instanceof SemanticCaption);
    }

    @Override
    public String toString() {
        if (lines.size() == 0) {
            return "SemanticCaption{}";
        }
        StringBuilder result = new StringBuilder("SemanticCaption{");
        result.append(lines.get(0));
        for (int i = 1; i < lines.size(); ++i) {
            result.append(", ");
            result.append(lines.get(i));
        }
        result.append("}");
        return result.toString();
    }
}
