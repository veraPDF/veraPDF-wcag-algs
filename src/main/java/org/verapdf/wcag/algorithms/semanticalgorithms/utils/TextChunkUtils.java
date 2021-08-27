package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.content.TextChunk;

public class TextChunkUtils {

    private TextChunkUtils() {}

    public static final double WHITE_SPACE_FACTOR = 0.25;

    public static boolean isSpaceChunk(TextChunk chunk) {
        for (char symbol : chunk.getValue().toCharArray()) {
            if (!Character.isSpaceChar(symbol)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isWhiteSpaceChunk(TextChunk chunk) {
        for (char symbol : chunk.getValue().toCharArray()) {
            if (!isWhiteSpaceChar(symbol)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isWhiteSpaceChar(char symbol) {
        return Character.isWhitespace(symbol) || symbol == '\u00A0' || symbol == '\u2007' || symbol == '\u202F';
    }
}
