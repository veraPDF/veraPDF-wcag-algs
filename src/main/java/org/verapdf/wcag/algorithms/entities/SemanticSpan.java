package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public class SemanticSpan extends SemanticNode {
    private List<TextChunk> textChunks;

    public SemanticSpan() {
        setSemanticType(SemanticType.SPAN);
        textChunks = new ArrayList<>();
    }

    public SemanticSpan(BoundingBox bbox) {
        super(bbox, SemanticType.SPAN);
        this.textChunks = new ArrayList<>();
    }

    public SemanticSpan(TextChunk textChunk) {
        this();
        add(textChunk);
    }

    public void add(TextChunk textChunk) {
        textChunks.add(textChunk);
        getBoundingBox().union(textChunk.getBoundingBox());
    }

    public void addAll(List<TextChunk> text) {
        if (text == null || text.size() == 0) {
            return;
        }
        textChunks.addAll(text);
        for (TextChunk textChunk : text) {
            getBoundingBox().union(textChunk.getBoundingBox());
        }
    }

    public List<TextChunk> getTextChunks() {
        return textChunks;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        SemanticSpan that = (SemanticSpan) o;
        return this.textChunks.equals(that.getTextChunks());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + textChunks.size();
        for (TextChunk textChunk : textChunks) {
            result = 31 * result + textChunk.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        if (textChunks.size() == 0) {
            return "SemanticSpan{}";
        }
        StringBuilder result = new StringBuilder("SemanticSpan{ ");
        result.append(textChunks.get(0));
        for (int i = 1; i < textChunks.size(); ++i) {
            result.append(", ");
            result.append(textChunks.get(i));
        }
        return result.toString();
    }
}
