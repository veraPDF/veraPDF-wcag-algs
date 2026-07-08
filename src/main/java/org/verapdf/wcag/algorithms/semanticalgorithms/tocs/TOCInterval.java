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
package org.verapdf.wcag.algorithms.semanticalgorithms.tocs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TOCInterval implements Comparable {
	private List<TOCIInfo> tocItemsInfos = new ArrayList<>();

	public TOCInterval() {

	}

	public Integer getTOCItemsStart() {
		return tocItemsInfos.isEmpty() ? null : tocItemsInfos.get(0).getIndex();
	}
	
	public Integer getTOCItemsEnd() {
		return tocItemsInfos.isEmpty() ? null : tocItemsInfos.get(tocItemsInfos.size() - 1).getIndex();
	}
	
	public List<TOCIInfo> getTOCItemsInfos() {
		return tocItemsInfos;
	}

	public TOCIInfo getLastTOCItemInfo() {
		return tocItemsInfos.get(tocItemsInfos.size() - 1);
	}

	public void setLastTOCItemInfo(TOCIInfo tocItemTextInfo) {
		tocItemsInfos.set(tocItemsInfos.size() - 1, tocItemTextInfo);
	}

	public TOCIInfo getPenultTOCItemInfo() {
		return tocItemsInfos.get(tocItemsInfos.size() - 2);
	}

	public TOCIInfo getFirstTOCItemInfo() {
		return tocItemsInfos.get(0);
	}

	public TOCIInfo getSecondTOCItemInfo() {
		return tocItemsInfos.get(1);
	}
	
	public int getNumberOfTOCItems() {
		return tocItemsInfos.size();
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TOCInterval interval = (TOCInterval) o;
		return tocItemsInfos.equals(interval.tocItemsInfos);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tocItemsInfos);
	}

	@Override
	public int compareTo(Object o) {
		TOCInterval interval = (TOCInterval) o;
		if (!Objects.equals(getFirstTOCItemInfo().getPageNumber(), interval.getFirstTOCItemInfo().getPageNumber())) {
			return getFirstTOCItemInfo().getPageNumber() - interval.getFirstTOCItemInfo().getPageNumber();
		}
		return getTOCItemsEnd() - interval.getTOCItemsStart();
	}
}
