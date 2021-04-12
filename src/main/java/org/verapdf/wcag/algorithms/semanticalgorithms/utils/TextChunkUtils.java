package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.content.TextChunk;

public class TextChunkUtils {

    public static final double WHITE_SPACE_FACTOR = 0.25;

    public static boolean isSpaceChunk(TextChunk chunk) {
        for (char symbol : chunk.getValue().toCharArray()) {
            if (!Character.isSpaceChar(symbol)) {
                return false;
            }
        }
        return true;
    }
}
