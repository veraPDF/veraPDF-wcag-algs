package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

public class SemanticNumberHeading extends SemanticHeading {

	public SemanticNumberHeading(SemanticNumberHeading numberHeading) {
		super(numberHeading);
		setSemanticType(SemanticType.NUMBER_HEADING);
	}

	public SemanticNumberHeading(SemanticHeading heading) {
		super(heading);
		setSemanticType(SemanticType.NUMBER_HEADING);
	}

	public SemanticNumberHeading(SemanticParagraph paragraph) {
		super(paragraph);
		setSemanticType(SemanticType.NUMBER_HEADING);
	}

	public SemanticNumberHeading(SemanticTextNode textNode) {
		super(textNode);
		setSemanticType(SemanticType.NUMBER_HEADING);
	}

	public SemanticNumberHeading(SemanticSpan span) {
		super(span);
		setSemanticType(SemanticType.NUMBER_HEADING);
	}

	public SemanticNumberHeading() {
		setSemanticType(SemanticType.NUMBER_HEADING);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("SemanticNumberHeading{");
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
