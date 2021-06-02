package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

public class SemanticHeading extends SemanticParagraph {

	public SemanticHeading(SemanticHeading heading) {
		super(heading);
		setSemanticType(SemanticType.HEADING);
	}

	public SemanticHeading(SemanticParagraph paragraph) {
		super(paragraph);
		setSemanticType(SemanticType.HEADING);
	}

	public SemanticHeading(SemanticSpan span) {
		super(span.getBoundingBox(), span.getLines());
		setSemanticType(SemanticType.HEADING);
	}

	public SemanticHeading(SemanticTextNode textNode) {
		super(textNode);
		setSemanticType(SemanticType.HEADING);
	}

	public SemanticHeading() {
		setSemanticType(SemanticType.HEADING);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("SemanticHeading{");
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
