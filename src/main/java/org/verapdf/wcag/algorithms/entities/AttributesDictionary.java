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

public class AttributesDictionary implements IAttributesDictionary {

	private long rowSpan;
	private long colSpan;

	public AttributesDictionary() {
		this.rowSpan = 1;
		this.colSpan = 1;
	}

	public AttributesDictionary(long rowSpan, long colSpan) {
		this.rowSpan = rowSpan;
		this.colSpan = colSpan;
	}

	public void setRowSpan(long rowSpan) {
		this.rowSpan = rowSpan;
	}

	public void setColSpan(long colSpan) {
		this.colSpan = colSpan;
	}

	@Override
	public long getRowSpan() {
		return rowSpan;
	}

	@Override
	public long getColSpan() {
		return colSpan;
	}
}
