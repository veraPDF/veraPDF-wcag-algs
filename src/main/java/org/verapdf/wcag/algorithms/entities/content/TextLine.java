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
        if (textChunks.isEmpty()) {
            fontSize = chunk.getFontSize();
            baseLine = chunk.getBaseLine();
        } else {
            if (chunk.getBaseLine() < baseLine) {
                baseLine = chunk.getBaseLine();
            }
            if (fontSize < chunk.getFontSize()) {
                fontSize = chunk.getFontSize();
            }
        }

        textChunks.add(chunk);
        getBoundingBox().union(chunk.getBoundingBox());
    }

    public void add(TextLine line) {
        if (textChunks.isEmpty()) {
            fontSize = line.getFontSize();
            baseLine = line.getBaseLine();
        } else {
            if (line.getBaseLine() < baseLine) {
                baseLine = line.getBaseLine();
            }
            if (fontSize < line.getFontSize()) {
                fontSize = line.getFontSize();
            }
        }

        textChunks.addAll(line.getTextChunks());
        getBoundingBox().union(line.getBoundingBox());
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
