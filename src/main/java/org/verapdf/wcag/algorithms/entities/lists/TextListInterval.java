package org.verapdf.wcag.algorithms.entities.lists;

import org.verapdf.wcag.algorithms.entities.lists.info.ListItemInfo;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemTextInfo;
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
