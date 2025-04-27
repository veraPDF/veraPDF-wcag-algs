package org.verapdf.wcag.algorithms.entities.content;

import java.util.ArrayList;
import java.util.List;

public class TextBlock extends TextInfoChunk {

	private final List<TextLine> textLines = new ArrayList<>();

	public TextBlock() {

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
		StringBuilder result = new StringBuilder(textLines.get(0).getValue());
		for (int i = 1; i < textLines.size(); ++i) {
			result.append('\n').append(textLines.get(i).getValue());
		}
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
}
