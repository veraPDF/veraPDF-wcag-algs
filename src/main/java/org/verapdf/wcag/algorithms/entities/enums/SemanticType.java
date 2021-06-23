package org.verapdf.wcag.algorithms.entities.enums;

public enum SemanticType {
	DOCUMENT("Document"),
	DIV("Div"),
	PARAGRAPH("P"),
	SPAN("Span"),
	TABLE("Table"),
	TABLE_HEADERS("THead"),
	TABLE_BODY("TBody"),
	TABLE_ROW("TR"),
	TABLE_HEADER("TH"),
	TABLE_CELL("TD"),
	FORM("Form"),
	LINK("Link"),
	ANNOT("Annot"),
	CAPTION("Caption"),
	LIST("L"),
	LIST_LABEL("Lbl"),
	LIST_BODY("LBody"),
	LIST_ITEM("Li"),
	NUMBER_HEADING("Hn"),
	HEADING("H");

	private final String value;

	SemanticType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static boolean isIgnoredStandardType(SemanticType type) {
		return type == ANNOT || type == FORM;
	}

}
