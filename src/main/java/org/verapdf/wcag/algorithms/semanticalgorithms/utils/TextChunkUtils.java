/*
 * This file is part of veraPDF wcag algorithms, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF wcag algorithms is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF wcag algorithms as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF wcag algorithms as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import java.util.*;

public class TextChunkUtils {

    public static final double WHITE_SPACE_FACTOR = 0.25;

    public static final double NEIGHBORS_EPSILON = 0.2;
    public static final double BASELINE_DIFFERENCE_EPSILON = 0.01;
    public static final double TEXT_CHUNK_SPACE_RATIO = 170;
    public static final double TEXT_LINE_SPACE_RATIO = 0.17;
    public static final double SPLIT_THRESHOLD_FACTOR = 0.21;

    public static final Set<Character> HYPHENATION_SIGNS = new HashSet<>(Arrays.asList('\u002D','\u2014','\u00AD'));

    public static boolean isSpaceChunk(TextChunk chunk) {
        for (char symbol : chunk.getValue().toCharArray()) {
            if (!Character.isSpaceChar(symbol)) {
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
    
    public static Integer getIndexOfFirstWhiteSpaceChar(String string) {
        for (int index = 0; index < string.length(); index++) {
            if (isWhiteSpaceChar(string.charAt(index))) {
                return index;
            }
        }
        return null;
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
        newTextChunk.getStreamInfos().addAll(secondTextChunk.getStreamInfos());
        return newTextChunk;
    }

    public static boolean areNeighborsTextChunks(TextChunk firstTextChunk, TextChunk secondTextChunk) {
        return NodeUtils.areCloseNumbers(firstTextChunk.getTextEnd(), secondTextChunk.getTextStart(),
                NEIGHBORS_EPSILON * firstTextChunk.getBoundingBox().getHeight());
    }

    public static List<TextChunk> splitTextChunkByWhiteSpaces(TextChunk originalChunk) {
        List<Integer> partsBoundaries = findWideWhiteSpaces(originalChunk);

        if (partsBoundaries.size() <= 2) {
            List<TextChunk> result = new ArrayList<>();
            result.add(originalChunk);
            return result;
        }
        return createPartsFromBoundaries(originalChunk, partsBoundaries);
    }

    private static List<Integer> findWideWhiteSpaces(TextChunk chunk) {
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

    public static TextChunk getTextChunkPartForRange(TextChunk textChunk, double leftX, double rightX, boolean isTrim) {
        Integer start = textChunk.getSymbolStartIndexByCoordinate(leftX);
        if (start == null) {
            return null;
        }
        Integer end = textChunk.getSymbolEndIndexByCoordinate(rightX);
        if (end == null) {
            return null;
        }
        if (end != textChunk.getValue().length()) {
            end++;
        }
        TextChunk result = TextChunk.getTextChunk(textChunk, start, end);
        return isTrim ? ChunksMergeUtils.getTrimTextChunk(result) : result;
    }

    public static TextChunk getTextChunkPartBeforeBoundingBox(TextChunk textChunk, BoundingBox bbox) {
        Integer end = textChunk.getSymbolEndIndexByCoordinate(bbox.getLeftX());
        if (end == null) {
            return null;
        }
        if (end != textChunk.getValue().length()) {
            end++;
        }
        TextChunk result = TextChunk.getTextChunk(textChunk, 0, end);
        return ChunksMergeUtils.getTrimTextChunk(result);
    }

    public static TextChunk getTextChunkPartAfterBoundingBox(TextChunk textChunk, BoundingBox bbox) {
        Integer start = textChunk.getSymbolStartIndexByCoordinate(bbox.getRightX());
        if (start == null) {
            return null;
        }
        TextChunk result = TextChunk.getTextChunk(textChunk, start, textChunk.getValue().length());
        return ChunksMergeUtils.getTrimTextChunk(result);
    }
}
