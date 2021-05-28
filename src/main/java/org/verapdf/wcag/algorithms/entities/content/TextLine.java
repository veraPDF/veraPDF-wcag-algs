package org.verapdf.wcag.algorithms.entities.content;

import java.util.ArrayList;
import java.util.List;

public class TextLine extends TextInfoChunk {
    private final List<TextChunk> textChunks = new ArrayList<>();

    public TextLine() {
    }

    public TextLine(TextChunk chunk) {
        super(chunk.getBoundingBox(), chunk.getFontSize(), chunk.getBaseLine());
        textChunks.add(chunk);
    }

    public TextLine(TextLine line) {
        super(line.getBoundingBox(), line.getFontSize(), line.getBaseLine());
        textChunks.addAll(line.getTextChunks());
    }

    public List<TextChunk> getTextChunks() {
        return textChunks;
    }

    public TextChunk getFirstTextChunk() {
        if (textChunks.isEmpty()) {
            return null;
        }
        return textChunks.get(0);
    }

    public TextChunk getLastTextChunk() {
        if (textChunks.isEmpty()) {
            return null;
        }
        return textChunks.get(textChunks.size() - 1);
    }

    public void add(TextChunk chunk) {
        textChunks.add(chunk);
        super.add(chunk);
    }

    public void add(TextLine line) {
        textChunks.addAll(line.getTextChunks());
        super.add(line);
    }

    public String getValue() {
        if (textChunks.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder(textChunks.get(0).getValue());
        for (int i = 1; i < textChunks.size(); ++i) {
            result.append(textChunks.get(i).getValue());
        }
        return result.toString();
    }

    @Override
    public String toString() {
        if (textChunks.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder(textChunks.get(0).getValue());
        for (int i = 1; i < textChunks.size(); ++i) {
            result.append(' ').append(textChunks.get(i).getValue());
        }
        return result.toString();
    }
}
