package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import java.util.*;
import static java.lang.Character.isSpaceChar;

public class TextChunkUtils {

    public static final double WHITE_SPACE_FACTOR = 0.25;

    public static final double NEIGHBORS_EPSILON = 0.2;
    public static final double BASELINE_DIFFERENCE_EPSILON = 0.01;
    public static final double TEXT_CHUNK_SPACE_RATIO = 170;
    public static final double TEXT_LINE_SPACE_RATIO = 0.17;
    public static final double SPLIT_THRESHOLD_FACTOR = 0.77;

    public static final Set<Character> HYPHENATION_SIGNS = new HashSet<>(Arrays.asList('\u002D','\u2014','\u00AD'));

    public static boolean isSpaceChunk(TextChunk chunk) {
        for (char symbol : chunk.getValue().toCharArray()) {
            if (!isSpaceChar(symbol)) {
                return false;
            }
        }
        return true;
    }

    public static void formatLineEnd(StringBuilder stringBuilder) {
        if (StaticContainers.isKeepLineBreaks()) {
            stringBuilder.append("\n");
        } else {
            if (TextChunkUtils.HYPHENATION_SIGNS.contains(stringBuilder.charAt(stringBuilder.length() - 1))) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            } else {
                stringBuilder.append(" ");
            }

        }
    }

    public static boolean isWhiteSpaceChunk(TextChunk chunk) {
        for (char symbol : chunk.getValue().toCharArray()) {
            if (!isWhiteSpaceChar(symbol)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isContainsWhiteSpaceChar(String string) {
        for (char symbol : string.toCharArray()) {
            if (isWhiteSpaceChar(symbol)) {
                return true;
            }
        }
        return false;
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

    public static List<TextChunk> splitTextChunk(TextChunk originalChunk) {
        List<Integer> partsBoundaries = findPartsBoundaries(originalChunk);

        if (partsBoundaries.size() <= 2) {
            List<TextChunk> result = new ArrayList<>();
            result.add(originalChunk);
            return result;
        }
        return createPartsFromBoundaries(originalChunk, partsBoundaries);
    }

    private static List<Integer> findPartsBoundaries(TextChunk chunk) {
        double threshold = chunk.getFontSize() * SPLIT_THRESHOLD_FACTOR;
        List<Integer> boundaries = new ArrayList<>();
        boundaries.add(-1);
        String text = chunk.getValue();

        for (int i = 1; i < text.length(); i++) {
            if (isWhiteSpaceChar(text.charAt(i))) {
                Double width = chunk.getSymbolWidth(i);
                if (width != null && width > threshold) {
                    boundaries.add(i);
                }
            }
        }
        boundaries.add(text.length());
        return boundaries;
    }

    private static List<TextChunk> createPartsFromBoundaries(TextChunk originalChunk, List<Integer> boundaries) {
        List<TextChunk> parts = new ArrayList<>();

        for (int i = 0; i < boundaries.size() - 1; i++) {
            int start = boundaries.get(i);
            int end = boundaries.get(i + 1);
            TextChunk part = ChunksMergeUtils.getTrimTextChunk(TextChunk.getTextChunk(originalChunk, start + 1, end));
            if (part != null && !part.isWhiteSpaceChunk()) {
                parts.add(part);
            }
        }
        return parts;
    }

}
