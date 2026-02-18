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

import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

public class ListItemTextInfo extends ListItemInfo {
	private final TextLine firstLine;
	private final String listItem;
	private final boolean hasOneLine;
	private String prefix;
	private String numberedPart;
	private Integer number;
	private String suffix;
    private int commonSuffixLength = 0;
	
	public ListItemTextInfo(ListItemTextInfo info) {
		super(info);
		this.firstLine = info.firstLine;
		this.listItem = info.listItem;
		this.hasOneLine = info.hasOneLine;
	}

	public ListItemTextInfo(int index, SemanticType semanticType, TextLine firstLine, String listItem, boolean hasOneLine) {
		super(index, semanticType);
		this.firstLine = firstLine;
		this.listItem = listItem;
		this.hasOneLine = hasOneLine;
	}

	@Override
	public TextLine getListItemValue() {
		return firstLine;
	}

	public String getListItem() {
		return listItem;
	}

	public boolean hasOneLine() {
		return hasOneLine;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getNumberedPart() {
		return numberedPart;
	}

	public void setNumberedPart(String numberedPart) {
		this.numberedPart = numberedPart;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	public String getBody() {
		return getListItem().substring(getPrefix().length() + getNumberedPart().length() + getSuffix().length());
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

    public int getCommonSuffixLength() {
        return commonSuffixLength;
    }

    public void setCommonSuffixLength(int commonSuffixLength) {
        this.commonSuffixLength = commonSuffixLength;
    }

    public int getCommonLabelLength() {
        return this.prefix.length() + this.numberedPart.length() + this.commonSuffixLength;
    }
}

