package org.verapdf.wcag.algorithms.entities.enums;

public enum SemanticType {
	PARAGRAPH("P"),
	SPAN("Span");

	private String value;

	SemanticType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
