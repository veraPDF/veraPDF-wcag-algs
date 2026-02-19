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
package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.enums.TextFormat;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.MultiBoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ChunksMergeUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;

import java.util.ArrayList;
import java.util.List;

public class TextLine extends TextInfoChunk {
    private final List<TextChunk> textChunks = new ArrayList<>();
    private boolean isLineStart = true;
    private boolean isLineEnd = true;
    private boolean isListLine = false;
    private LineArtChunk connectedLineArtLabel = null;  

    public TextLine() {
    }

    public TextLine(TextChunk chunk) {
        super(new MultiBoundingBox(chunk.getBoundingBox()), chunk.getFontSize(), chunk.getBaseLine(), chunk.getSlantDegree());
        textChunks.add(chunk);
        setHiddenText(chunk.isHiddenText());
    }

    public TextLine(TextLine line) {
        super(line.getBoundingBox(), line.getFontSize(), line.getBaseLine(), line.getSlantDegree());
        textChunks.addAll(line.getTextChunks());
        setHiddenText(line.isHiddenText());
    }

    public TextLine(TextLine line, int beginIndex, int endIndex) {
        super(new BoundingBox(), line.getFontSize(), line.getBaseLine(), line.getSlantDegree());
        setHiddenText(line.isHiddenText());
        int currentIndex = 0;
        for (TextChunk textChunk : line.textChunks) {
            int nextIndex = currentIndex + textChunk.getValue().length();
            if (nextIndex > beginIndex) {
                add(ChunksMergeUtils.getTrimTextChunk(TextChunk.getTextChunk(textChunk, Math.max(beginIndex - currentIndex, 0),
                        Math.min(nextIndex - currentIndex, endIndex - currentIndex))));
            }
            currentIndex = nextIndex;
            if (currentIndex >= endIndex) {
                return;
            }
        }
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
            if (TextFormat.NORMAL == textChunk.getTextFormat()) {
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
        if (!StaticContainers.isDataLoader()) {
            addSpaceIfRequired(line);
        }
        double size = this.fontSize;
        textChunks.addAll(line.getTextChunks());
        super.add(line);
        if (line.isSpaceLine()) {
            this.fontSize = size;
        } else if (isSpaceLine()) {
            this.fontSize = line.getFontSize();
        }
    }

    protected void addSpaceIfRequired(TextLine line) {
        if (line.getLeftX() - this.getRightX() < this.fontSize * TextChunkUtils.TEXT_LINE_SPACE_RATIO) {
            return;
        }

        BoundingBox boundingBox = new BoundingBox();
        boundingBox.init(this.getRightX(), this.getBottomY(), line.getLeftX(), this.getTopY());
        textChunks.add(new TextChunk(boundingBox, " ", this.getFontSize(), this.baseLine));
    }

    @Override
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
    
    public Double getSymbolEndCoordinate(int index) {
        int currentIndex = 0;
        for (TextChunk textChunk : textChunks) {
            if (currentIndex + textChunk.getValue().length() > index) {
                return textChunk.getSymbolEndCoordinate(index - currentIndex);
            }
            currentIndex += textChunk.getValue().length();
        }
        return null;
    }

    public Double getSymbolStartCoordinate(int index) {
        int currentIndex = 0;
        for (TextChunk textChunk : textChunks) {
            if (currentIndex + textChunk.getValue().length() > index) {
                return textChunk.getSymbolStartCoordinate(index - currentIndex);
            }
            currentIndex += textChunk.getValue().length();
        }
        return null;
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

    public boolean isListLine() {
        return isListLine;
    }

    public void setListLine(boolean listLine) {
        isListLine = listLine;
    }

    public LineArtChunk getConnectedLineArtLabel() {
        return connectedLineArtLabel;
    }

    public void setConnectedLineArtLabel(LineArtChunk connectedLineArtLabel) {
        this.connectedLineArtLabel = connectedLineArtLabel;
    }
}
