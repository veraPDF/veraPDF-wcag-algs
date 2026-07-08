/*
 * This file is part of veraPDF wcag algorithms, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF wcag algorithms is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF wcag algorithms as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF wcag algorithms as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.wcag.algorithms.entities.enums;

public enum SemanticType {
	DOCUMENT("Document"),
	DIV("Div"),
	PARAGRAPH("P"),
	SPAN("Span"),
	TABLE("Table"),
	TABLE_HEADERS("THead"),
	TABLE_FOOTER("TFoot"),
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
	LIST_ITEM("LI"),
	TABLE_OF_CONTENT("TOC"),
	TABLE_OF_CONTENT_ITEM("TOCI"),
	FIGURE("Figure"),
	NUMBER_HEADING("Hn"),
	HEADING("H"),
	TITLE("Title"),
	BLOCK_QUOTE("BlockQuote"),
	NOTE("Note"),
    FENOTE("FENote"),
	HEADER("Header"),
	FOOTER("Footer"),
	CODE("Code"),
	PART("Part");

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
