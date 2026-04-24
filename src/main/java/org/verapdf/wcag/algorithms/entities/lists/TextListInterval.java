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
package org.verapdf.wcag.algorithms.entities.lists;

import org.verapdf.wcag.algorithms.entities.lists.info.ListItemInfo;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemTextInfo;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ListLabelsUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.NumberingStyleNames;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TextListInterval implements Comparable {
	private List<ListItemTextInfo> listItemsInfos = new ArrayList<>();
	private String numberingStyle = NumberingStyleNames.UNKNOWN;

	public TextListInterval() {

	}
	
	public TextListInterval(ListInterval interval) {
		for (ListItemInfo info : interval.getListItemsInfos()) {
			getListItemsInfos().add((ListItemTextInfo) info);
		}
        setNumberingStyle(interval.getNumberingStyle());
	}

	public TextListInterval(List<ListItemTextInfo> listItemsInfos) {
		this.listItemsInfos = listItemsInfos;
	}
	
	public String getNumberingStyle() {
		return numberingStyle;
	}

	public void setNumberingStyle(String numberingStyle) {
		this.numberingStyle = numberingStyle;
	}

	public Integer getListItemsStart() {
		return listItemsInfos.isEmpty() ? null : listItemsInfos.get(0).getIndex();
	}
	
	public Integer getListItemsEnd() {
		return listItemsInfos.isEmpty() ? null : listItemsInfos.get(listItemsInfos.size() - 1).getIndex();
	}
	
	public List<ListItemTextInfo> getListItemsInfos() {
		return listItemsInfos;
	}

	public ListItemTextInfo getLastListItemInfo() {
		return listItemsInfos.get(listItemsInfos.size() - 1);
	}

	public void setLastListItemInfo(ListItemTextInfo listItemTextInfo) {
		listItemsInfos.set(listItemsInfos.size() - 1, listItemTextInfo);
	}

	public ListItemTextInfo getPenultListItemInfo() {
		return listItemsInfos.get(listItemsInfos.size() - 2);
	}

	public ListItemTextInfo getFirstListItemInfo() {
		return listItemsInfos.get(0);
	}

	public ListItemTextInfo getSecondListItemInfo() {
		return listItemsInfos.get(1);
	}
	
	public String getCommonPrefix() {
		ListItemTextInfo listItemInfo = getFirstListItemInfo();
		if (listItemInfo != null) {
			return listItemInfo.getPrefix();
		}
		return null;
	}

    public void setCommonSuffixLengthToAllInfos() {
        if (listItemsInfos == null || listItemsInfos.isEmpty()) {
            return;
        }
        String common = listItemsInfos.get(0).getSuffix();
        if (common == null) {
            return;
        }
        for (int i = 1; i < listItemsInfos.size(); i++) {
            String next = listItemsInfos.get(i).getSuffix();
            if (next == null) {
                return;
            }
            int commonLength = ListLabelsUtils.getCommonStartLength(common, next);
            if (commonLength == 0) {
                return;
            }
            common = common.substring(0, commonLength);
        }
        int finalLength = common.length();
        for (ListItemTextInfo item : listItemsInfos) {
            item.setCommonSuffixLength(finalLength);
        }
    }

	public int getNumberOfListItems() {
		return listItemsInfos.size();
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TextListInterval interval = (TextListInterval) o;
		return listItemsInfos.equals(interval.listItemsInfos);
	}

	@Override
	public int hashCode() {
		return Objects.hash(listItemsInfos);
	}

	@Override
	public int compareTo(Object o) {
		TextListInterval interval = (TextListInterval) o;
		if (!Objects.equals(getFirstListItemInfo().getPageNumber(), interval.getFirstListItemInfo().getPageNumber())) {
			return getFirstListItemInfo().getPageNumber() - interval.getFirstListItemInfo().getPageNumber();
		}
		return getListItemsEnd() - interval.getListItemsStart();
	}
}
