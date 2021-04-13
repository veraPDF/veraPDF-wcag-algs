package org.verapdf.wcag.algorithms.entities.content;

import java.util.ArrayList;
import java.util.List;

public class TextLine extends InfoChunk {
    private final List<TextChunk> textChunks = new ArrayList<>();
    private double fontSize = 0d;
    private double baseLine = 0d;

    public TextLine() {
    }

    public TextLine(TextChunk chunk) {
        super(chunk.getBoundingBox());
        textChunks.add(chunk);
        fontSize = chunk.getFontSize();
        baseLine = chunk.getBaseLine();
    }

    public TextLine(TextLine line) {
        super(line.getBoundingBox());
        textChunks.addAll(line.getTextChunks());
        fontSize = line.getFontSize();
        baseLine = line.getBaseLine();
    }

    public double getFontSize() {
        return fontSize;
    }

    public double getBaseLine() {
        return baseLine;
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

    @Override
    public String toString() {
        if (textChunks.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder(textChunks.get(0).getValue());
        for (int i = 1; i < textChunks.size(); ++i) {
            result.append(' ').append(getTextChunks().get(i).getValue());
        }
        return result.toString();
    }
}
