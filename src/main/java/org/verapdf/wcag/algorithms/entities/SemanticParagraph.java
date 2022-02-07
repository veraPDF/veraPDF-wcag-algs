package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.TextColumn;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import java.util.List;
import java.util.Objects;

public class SemanticParagraph extends SemanticTextNode {

	protected boolean enclosedTop;
	protected boolean enclosedBottom;
	protected int indentation; // 0 - left, 1 - right, 2 - center

	public SemanticParagraph(SemanticParagraph paragraph) {
		super(paragraph);
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

	public SemanticParagraph(BoundingBox bbox, List<TextColumn> columns) {
		super(bbox, columns);
		setSemanticType(SemanticType.PARAGRAPH);
	}

	public SemanticParagraph(BoundingBox bbox) {
		super(bbox);
		setSemanticType(SemanticType.PARAGRAPH);
	}

	public SemanticParagraph(SemanticTextNode textNode) {
		super(textNode);
		setSemanticType(SemanticType.PARAGRAPH);
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
		if (!(o instanceof SemanticParagraph)) {
			return false;
		}
		SemanticParagraph that = (SemanticParagraph) o;
		return enclosedTop == that.enclosedTop
		       && enclosedBottom == that.enclosedBottom
		       && indentation == that.indentation;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + Objects.hash(enclosedTop, enclosedBottom, indentation);
		return result;
	}
}
