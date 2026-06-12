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

import org.verapdf.wcag.algorithms.entities.enums.TextAlignment;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;

import java.util.*;

public class TextBlock extends TextInfoChunk {

	private final List<TextLine> textLines = new ArrayList<>();

	private boolean hasStartLine = false;
	private boolean hasEndLine = false;
    private Double mostCommonFontSize = null;

	private TextAlignment textAlignment = null;

	public TextBlock() {

	}

	public TextBlock(BoundingBox bbox, double maxFontSize, double baseLine) {
		super(bbox, maxFontSize, baseLine);
		this.maxFontSize = maxFontSize;
		this.baseLine = baseLine;
	}

	public TextBlock(BoundingBox boundingBox) {
		super(boundingBox);
	}
	
	public TextBlock(TextLine line) {
		super(line.getBoundingBox(), line.getFontSize(), line.getBaseLine());
		textLines.add(line);
		setHiddenText(line.isHiddenText());
	}

    private void updateVariables() {
        mostCommonFontSize = null;
    }

	public TextBlock(TextBlock block) {
		super(block.getBoundingBox(), block.getFontSize(), block.getBaseLine());
		textLines.addAll(block.getLines());
		setHiddenText(block.isHiddenText());
        this.mostCommonFontSize = block.mostCommonFontSize;
        this.textAlignment = block.textAlignment;
        this.hasStartLine = block.hasStartLine;
        this.hasEndLine = block.hasEndLine;
	}

    public double getMostCommonFontSize() {
        if (mostCommonFontSize == null) {
            mostCommonFontSize = calculateFontSize();
        }
        return mostCommonFontSize;
    }

	public List<TextLine> getLines() {
		return textLines;
	}

	public TextLine getFirstLine() {
		if (textLines.isEmpty()) {
			return null;
		}
		return textLines.get(0);
	}

	public TextLine getLastLine() {
		if (textLines.isEmpty()) {
			return null;
		}
		return textLines.get(textLines.size() - 1);
	}

	public void setLastLine(TextLine lastLine) {
		if (!textLines.isEmpty()) {
			textLines.set(textLines.size() - 1, lastLine);
		} else {
			textLines.add(lastLine);
		}
	}

	public void setFirstLine(TextLine firstLine) {
		if (!textLines.isEmpty()) {
			textLines.set(0, firstLine);
		} else {
			textLines.add(firstLine);
		}
	}

	public TextLine getSecondLine() {
		if (textLines.size() > 1) {
			return textLines.get(1);
		}
		return null;
	}

	public TextLine getPenultLine() {
		if (textLines.size() > 1) {
			return textLines.get(textLines.size() - 2);
		}
		return null;
	}

	public void add(TextLine line) {
		textLines.add(line);
		super.add(line);
        updateVariables();
	}

	public void add(List<TextLine> lines) {
		for (TextLine line : lines) {
			add(line);
		}
	}

	public double getFirstLineIndent() {
		TextLine secondTextLine = getSecondLine();
		if (secondTextLine != null) {
			return getFirstLine().getLeftX() - secondTextLine.getLeftX();
		}
		return 0.0d;
	}

    private double calculateFontSize() {
        Map<Double, Double> fontSizeMap = new HashMap<>();

        for (TextLine line : this.getLines()) {
            for (TextChunk chunk : line.getTextChunks()) {
                if (!TextChunkUtils.isWhiteSpaceChunk(chunk)) {
                    Double sizeLength = fontSizeMap.get(chunk.getFontSize());
                    fontSizeMap.put(chunk.getFontSize(),
                            ((sizeLength == null) ? 0 : sizeLength) + chunk.getTextLength());
                }
            }
        }

        if (!fontSizeMap.isEmpty()) {
            return fontSizeMap.entrySet()
                    .stream()
                    .max(Comparator.comparingDouble(Map.Entry::getValue))
                    .get().getKey();
        }
        return 0.0;
    }

	public int getLinesNumber() {
		return textLines.size();
	}

	public boolean isEmpty() {
		return textLines.isEmpty() || textLines.stream().allMatch(TextLine::isEmpty);
	}

	@Override
	public String toString() {
		if (textLines.isEmpty()) {
			return "";
		}

		StringBuilder result = new StringBuilder("");
		for (int i = 0; i < textLines.size() - 1; ++i) {
			String textString = textLines.get(i).getValue();
			result.append(textString);
			TextChunkUtils.formatLineEnd(result);
		}

		result.append(textLines.get(textLines.size() - 1).getValue());
		return result.toString();
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + textLines.size();
		for (TextLine textLine : textLines) {
			result = 31 * result + textLine.hashCode();
		}
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (!super.equals(o)) {
			return false;
		}
		if (!(o instanceof TextBlock)) {
			return false;
		}
		TextBlock that = (TextBlock) o;
		return this.textLines.equals(that.getLines());
	}

	public TextAlignment getTextAlignment() {
		return textAlignment;
	}

	public void setTextAlignment(TextAlignment textAlignment) {
		this.textAlignment = textAlignment;
	}

	public boolean isHasStartLine() {
		return hasStartLine;
	}

	public void setHasStartLine(boolean hasStartLine) {
		this.hasStartLine = hasStartLine;
	}

	public boolean isHasEndLine() {
		return hasEndLine;
	}

	public void setHasEndLine(boolean hasEndLine) {
		this.hasEndLine = hasEndLine;
	}
	
	public Set<Double> getTextSizes() {
		Set<Double> textSizes = new HashSet<>();
		for (TextLine textLine : getLines()) {
			for (TextChunk textChunk : textLine.getTextChunks()) {
				textSizes.add(textChunk.getFontSize());
			}
		}
		return textSizes;
	}
}
