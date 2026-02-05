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
