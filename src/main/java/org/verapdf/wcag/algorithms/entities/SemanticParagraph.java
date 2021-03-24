package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import java.util.Objects;

public class SemanticParagraph extends SemanticNode {

	private boolean enclosedTop;
	private boolean enclosedBottom;
	private int indentation; // 0 - left, 1 - right, 2 - center
	private TextChunk firstLine;
	private TextChunk lastLine;

	public SemanticParagraph(SemanticParagraph paragraph) {
		super(paragraph.getBoundingBox(), paragraph.getInitialSemanticType(), paragraph.getSemanticType());
		firstLine = paragraph.getFirstLine();
		lastLine = paragraph.getLastLine();
		enclosedBottom = paragraph.enclosedBottom;
		enclosedTop = paragraph.enclosedTop;
		indentation = paragraph.indentation;
	}

	public SemanticParagraph() {
		setSemanticType(SemanticType.PARAGRAPH);
	}

	public SemanticParagraph(SemanticType initialSemanticType) {
		super(initialSemanticType);
		setSemanticType(SemanticType.PARAGRAPH);
	}

	public SemanticParagraph(BoundingBox bbox, TextChunk firstLine, TextChunk lastLine) {
		super(bbox, null, SemanticType.PARAGRAPH);
		this.firstLine = firstLine;
		this.lastLine = lastLine;
	}

	public SemanticParagraph(BoundingBox bbox, TextChunk firstLine, TextChunk lastLine, SemanticType initialSemanticType) {
		super(bbox, initialSemanticType, SemanticType.PARAGRAPH);
		this.firstLine = firstLine;
		this.lastLine = lastLine;
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

	public TextChunk getFirstLine() {
		return firstLine;
	}

	public void setFirstLine(TextChunk firstLine) {
		this.firstLine = firstLine;
	}

	public TextChunk getLastLine() {
		return lastLine;
	}

	public void setLastLine(TextChunk lastLine) {
		this.lastLine = lastLine;
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
		       && Objects.equals(firstLine, that.firstLine)
		       && Objects.equals(lastLine, that.lastLine);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + Objects.hash(enclosedTop, enclosedBottom, indentation, firstLine);
		return result;
	}

	@Override
	public String toString() {
		return "SemanticParagraph{" +
		       "enclosedTop=" + enclosedTop +
		       ", enclosedBottom=" + enclosedBottom +
		       ", indentation=" + indentation +
		       ", firstLine=" + firstLine +
		       ", lastLine=" + lastLine +
		       '}';
	}
}
