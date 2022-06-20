package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.content.TextChunk;

import java.util.Arrays;
import java.util.Objects;

public class TextChunkUtils {

    public static final double WHITE_SPACE_FACTOR = 0.25;

    public static final double NEIGHBORS_EPSILON = 0.2;
    public static final double BASELINE_DIFFERENCE_EPSILON = 0.01;

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

    public static boolean areTextChunksHaveSameStyle(TextChunk firstTextChunk, TextChunk secondTextChunk) {
        return Objects.equals(firstTextChunk.getFontName(), secondTextChunk.getFontName()) &&
                NodeUtils.areCloseNumbers(firstTextChunk.getFontWeight(), secondTextChunk.getFontWeight()) &&
                NodeUtils.areCloseNumbers(firstTextChunk.getItalicAngle(), secondTextChunk.getItalicAngle()) &&
                Arrays.equals(firstTextChunk.getFontColor(), secondTextChunk.getFontColor()) &&
                NodeUtils.areCloseNumbers(firstTextChunk.getFontSize(), secondTextChunk.getFontSize()) &&
                NodeUtils.areCloseNumbers(firstTextChunk.getSlantDegree(), secondTextChunk.getSlantDegree());
    }

    public static boolean areTextChunksHaveSameBaseLine(TextChunk firstTextChunk, TextChunk secondTextChunk) {
        return NodeUtils.areCloseNumbers(firstTextChunk.getBaseLine(), secondTextChunk.getBaseLine(),
                BASELINE_DIFFERENCE_EPSILON * firstTextChunk.getBoundingBox().getHeight());
    }

    public static TextChunk unionTextChunks(TextChunk firstTextChunk, TextChunk secondTextChunk) {
        TextChunk newTextChunk = new TextChunk(firstTextChunk);
        newTextChunk.setValue(firstTextChunk.getValue() + secondTextChunk.getValue());
        newTextChunk.getBoundingBox().union(secondTextChunk.getBoundingBox());
        newTextChunk.getSymbolEnds().addAll(secondTextChunk.getSymbolEnds().subList(1, secondTextChunk.getSymbolEnds().size()));
        return newTextChunk;
    }

    public static boolean areNeighborsTextChunks(TextChunk firstTextChunk, TextChunk secondTextChunk) {
        return NodeUtils.areCloseNumbers(firstTextChunk.getTextEnd(), secondTextChunk.getTextStart(),
                NEIGHBORS_EPSILON * firstTextChunk.getBoundingBox().getHeight());
    }

}
