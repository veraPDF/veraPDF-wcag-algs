package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextColumn;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import java.util.List;

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

    public SemanticSpan(BoundingBox bbox, List<TextColumn> columns) {
        super(bbox, columns);
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
}
