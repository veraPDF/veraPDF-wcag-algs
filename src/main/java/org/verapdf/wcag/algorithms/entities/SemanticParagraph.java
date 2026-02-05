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
package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.content.TextColumn;
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
