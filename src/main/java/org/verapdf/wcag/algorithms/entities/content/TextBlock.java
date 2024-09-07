package org.verapdf.wcag.algorithms.entities.content;

import org.verapdf.wcag.algorithms.entities.enums.TextAlignment;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;

import java.util.ArrayList;
import java.util.List;

public class TextBlock extends TextInfoChunk {

	private final List<TextLine> textLines = new ArrayList<>();

	private TextAlignment textAlignment = null;

	public TextBlock() {

	}

	public TextBlock(BoundingBox bbox, double fontSize, double baseLine) {
		super(bbox, fontSize, baseLine);
		this.fontSize = fontSize;
		this.baseLine = baseLine;
	}

	public TextBlock(BoundingBox boundingBox) {
		super(boundingBox);
	}
	
	public TextBlock(TextLine line) {
		super(line.getBoundingBox(), line.getFontSize(), line.getBaseLine());
		textLines.add(line);
	}

	public TextBlock(TextBlock block) {
		super(block.getBoundingBox(), block.getFontSize(), block.getBaseLine());
		textLines.addAll(block.getLines());
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
	}

	public void add(List<TextLine> lines) {
		for (TextLine line : lines) {
			add(line);
		}
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
			result.append(TextChunkUtils.suppressEndHyphenation(textLines.get(i).getValue())).append('\n');
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
}
