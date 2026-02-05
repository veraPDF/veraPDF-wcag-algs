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
package org.verapdf.wcag.algorithms.entities.lists.info;

import org.verapdf.wcag.algorithms.entities.content.InfoChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.util.Objects;

public class ListItemInfo {
	private int index;
	private SemanticType semanticType;

	public ListItemInfo(ListItemInfo info) {
		this(info.getIndex(), info.getSemanticType());
	}

	public ListItemInfo(int index) {
		this.index = index;
	}

	protected ListItemInfo(int index, SemanticType semanticType) {
		this.index = index;
		this.semanticType = semanticType;
	}

	public int getIndex() {
		return index;
	}

	public static ListItemInfo createListItemInfo(ListItemInfo info) {
		if (info instanceof ListItemTextInfo) {
			return new ListItemTextInfo((ListItemTextInfo) info);
		} else if (info instanceof ListItemLineArtInfo) {
			return new ListItemLineArtInfo((ListItemLineArtInfo) info);
		} else if (info instanceof ListItemImageInfo) {
			return new ListItemImageInfo((ListItemImageInfo) info);
		}
		return new ListItemInfo(info);
	}

	public SemanticType getSemanticType() {
		return semanticType;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public InfoChunk getListItemValue() {
		return null;
	}
	
	public Integer getPageNumber() {
		return getListItemValue() != null ? getListItemValue().getPageNumber() : null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ListItemInfo that = (ListItemInfo) o;
		return index == that.index && semanticType == that.semanticType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(index, semanticType);
	}

	@Override
	public String toString() {
		return "ListItemInfo{" +
		       "index=" + index +
		       ", semanticType=" + semanticType +
		       '}';
	}
}
