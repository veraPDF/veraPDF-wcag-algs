package org.verapdf.wcag.algorithms.entities.enums;

public enum SemanticType {
	DOCUMENT("Document"),
	DIV("Div"),
	PARAGRAPH("P"),
	SPAN("Span"),
	TABLE("Table"),
	TABLE_ROW("TR"),
	TABLE_CELL("TD");

	private String value;

	SemanticType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
