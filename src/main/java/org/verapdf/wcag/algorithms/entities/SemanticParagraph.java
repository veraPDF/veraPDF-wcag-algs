package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SemanticParagraph extends SemanticNode {

	private boolean enclosedTop;
	private boolean enclosedBottom;
	private int indentation; // 0 - left, 1 - right, 2 - center
	private List<TextLine> lines;

	public SemanticParagraph(SemanticParagraph paragraph) {
		super(paragraph.getBoundingBox(), paragraph.getInitialSemanticType(), paragraph.getSemanticType());
		enclosedBottom = paragraph.enclosedBottom;
		enclosedTop = paragraph.enclosedTop;
		indentation = paragraph.indentation;
		lines = new ArrayList<>(paragraph.getLines());
	}

	public SemanticParagraph() {
		setSemanticType(SemanticType.PARAGRAPH);
	}

	public SemanticParagraph(SemanticType initialSemanticType) {
		super(initialSemanticType);
		setSemanticType(SemanticType.PARAGRAPH);
	}

	public SemanticParagraph(BoundingBox bbox, List<TextLine> lines) {
		super(bbox, null, SemanticType.PARAGRAPH);
		this.lines = new ArrayList<>(lines);
	}

	public SemanticParagraph(BoundingBox bbox) {
		super(bbox, null, SemanticType.PARAGRAPH);
		this.lines = new ArrayList<>();
	}

	public SemanticParagraph(BoundingBox bbox, List<TextLine> lines, SemanticType initialSemanticType) {
		super(bbox, initialSemanticType, SemanticType.PARAGRAPH);
		this.lines = new ArrayList<>(lines);
	}

	public boolean isEnclosedTop() {
		return enclosedTop;
	}

	public void setEnclosedTop(boolean enclosedTop) {
		this.enclosedTop = enclosedTop;
	}

	public boolean isEnclosedBottom() {
		return enclosedBottom;
	}

	public void setEnclosedBottom(boolean enclosedBottom) {
		this.enclosedBottom = enclosedBottom;
	}

	public void setFirstLine(TextLine firstLine) {
		if (lines.size() != 0) {
			lines.set(0, firstLine);
		} else {
			lines.add(firstLine);
		}
	}

	public void setLastLine(TextLine lastLine) {
		if (lines.size() != 0) {
			lines.set(lines.size() - 1, lastLine);
		} else {
			lines.add(lastLine);
		}
	}

	public int getLinesNumber() {
		return lines.size();
	}

	public List<TextLine> getLines() {
		return lines;
	}

	public TextLine getFirstLine() {
		if (lines.size() != 0) {
			return lines.get(0);
		}
		return null;
	}

	public TextLine getSecondLine() {
		if (lines.size() > 1) {
			return lines.get(1);
		}
		return null;
	}
	public TextLine getPenultLine() {
		if (lines.size() > 1) {
			return lines.get(lines.size() - 2);
		}
		return null;
	}

	public TextLine getLastLine() {
		if (lines.size() != 0) {
			return lines.get(lines.size() - 1);
		}
		return null;
	}

	public int getIndentation() {
		return indentation;
	}

	public void setIndentation(int indentation) {
		this.indentation = indentation;
	}

	@Override
	public boolean equals(Object o) {
		if (!super.equals(o)) {
			return false;
		}

		SemanticParagraph that = (SemanticParagraph) o;
		return enclosedTop == that.enclosedTop
		       && enclosedBottom == that.enclosedBottom
		       && indentation == that.indentation
		       && this.lines.equals(that.getLines());
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + lines.size();
		for (TextLine textLine : lines) {
			result = 31 * result + textLine.hashCode();
		}
		result = 31 * result + Objects.hash(enclosedTop, enclosedBottom, indentation);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("SemanticParagraph{");
		result.append("enclosedTop=");
		result.append(enclosedTop);
		result.append(", enclosedBottom=");
		result.append(enclosedBottom);
		result.append(", indentation=");
		result.append(indentation);
		result.append(", lines=[");
		result.append(lines.get(0));
		for (int i = 1; i < lines.size(); ++i) {
			result.append(", ");
			result.append(lines.get(i));
		}
		result.append("]}");
		return result.toString();
	}
}
