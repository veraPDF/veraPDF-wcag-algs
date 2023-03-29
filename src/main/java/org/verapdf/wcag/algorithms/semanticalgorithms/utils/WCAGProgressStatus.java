package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

public enum WCAGProgressStatus {
	DOCUMENT_PREPROCESSING("Document preprocessing"),
	DOCUMENT_POSTPROCESSING("Document postprocessing"),
	LINES_PREPROCESSING("Lines preprocessing"),
	PARAGRAPH_DETECTION("Paragraph detection"),
	HEADING_AND_CAPTION_DETECTION("Heading and Caption detection"),
	CONTRAST_DETECTION("Contrast detection"),
	LIST_DETECTION("List detection"),
	TOC_DETECTION("Table of contents detection"),
	TABLE_VALIDATION("Table validation"),
	TABLE_BORDER_DETECTION("Table border detection"),
	TABLE_DETECTION("Table detection");

	private final String value;

	WCAGProgressStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
