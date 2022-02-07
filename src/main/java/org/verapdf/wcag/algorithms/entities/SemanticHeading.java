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
		super(span);
		setSemanticType(SemanticType.HEADING);
	}

	public SemanticHeading(SemanticTextNode textNode) {
		super(textNode);
		setSemanticType(SemanticType.HEADING);
	}

	public SemanticHeading() {
		setSemanticType(SemanticType.HEADING);
	}
}
