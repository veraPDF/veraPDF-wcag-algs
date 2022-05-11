package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.enums.TextFormat;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;

import java.util.ArrayList;
import java.util.List;

public class TextLine extends TextInfoChunk {
    private final List<TextChunk> textChunks = new ArrayList<>();
    private boolean isLineStart = true;
    private boolean isLineEnd = true;

    public TextLine() {
    }

    public TextLine(TextChunk chunk) {
        super(chunk.getBoundingBox(), chunk.getFontSize(), chunk.getBaseLine(), chunk.getSlantDegree());
        textChunks.add(chunk);
    }

    public TextLine(TextLine line) {
        super(line.getBoundingBox(), line.getFontSize(), line.getBaseLine(), line.getSlantDegree());
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

    public TextChunk getLastNormalTextChunk() {
        for (int i = textChunks.size() - 2; i >= 0; i--) {
            TextChunk textChunk = textChunks.get(i);
            if (TextFormat.NORMAL.equals(textChunk.getTextFormat())) {
                return textChunk;
            }
        }
        return null;
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

    public boolean isEmpty() {
        return textChunks.isEmpty() || textChunks.stream().allMatch(TextChunk::isEmpty);
    }

    public boolean isSpaceLine() {
        for (TextChunk textChunk : textChunks) {
            if (!TextChunkUtils.isWhiteSpaceChunk(textChunk)) {
                return false;
            }
        }
        return true;
    }

    public boolean isFullLine() {
        return isLineStart && isLineEnd;
    }

    public boolean isLineStart() {
        return isLineStart;
    }

    public void setNotLineStart() {
        isLineStart = false;
    }

    public boolean isLineEnd() {
        return isLineEnd;
    }

    public void setNotLineEnd() {
        isLineEnd = false;
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
