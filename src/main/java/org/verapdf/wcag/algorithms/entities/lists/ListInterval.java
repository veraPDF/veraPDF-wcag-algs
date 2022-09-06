package org.verapdf.wcag.algorithms.entities.lists;

import org.verapdf.wcag.algorithms.entities.lists.info.ListItemInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListInterval {
	private List<Integer> listsIndexes = new ArrayList<>();
	private List<ListItemInfo> listItemsInfos = new ArrayList<>();
	public Integer numberOfColumns;

	public  ListInterval() {

	}

	public ListInterval(List<ListItemInfo> listItemsInfos, List<Integer> listsIndexes, Integer numberOfColumns) {
		this.listItemsInfos = listItemsInfos;
		this.listsIndexes = listsIndexes;
		this.numberOfColumns = numberOfColumns;
	}

	public ListInterval(int start, int end) {
		for (int i = start; i <= end; i++) {
			listItemsInfos.add(new ListItemInfo(i));
		}
	}

	public int getStart() {
		if (!listItemsInfos.isEmpty() && !listsIndexes.isEmpty()) {
			return Math.min(getListItemsStart(), getListsStart());
		}
		if (listsIndexes.isEmpty()) {
			return getListItemsStart();
		}
		return getListsStart();
	}

	public Integer getListItemsStart() {
		return listItemsInfos.isEmpty() ? null : listItemsInfos.get(0).getIndex();
	}

	public Integer getListsStart() {
		return listsIndexes.isEmpty() ? null : listsIndexes.get(0);
	}

	public int getEnd() {
		if (!listItemsInfos.isEmpty() && !listsIndexes.isEmpty()) {
			return Math.max(getListItemsEnd(), getListsEnd());
		}
		if (listsIndexes.isEmpty()) {
			return getListItemsEnd();
		}
		return getListsEnd();
	}

	public Integer getListItemsEnd() {
		return listItemsInfos.isEmpty() ? null : listItemsInfos.get(listItemsInfos.size() - 1).getIndex();
	}

	public Integer getListsEnd() {
		return listsIndexes.isEmpty() ? null : listsIndexes.get(listsIndexes.size() - 1);
	}

	public List<ListItemInfo> getListItemsInfos() {
		return listItemsInfos;
	}

	public ListItemInfo getLastListItemInfo() {
		return listItemsInfos.get(listItemsInfos.size() - 1);
	}

	public ListItemInfo getPenultListItemInfo() {
		return listItemsInfos.get(listItemsInfos.size() - 2);
	}

	public ListItemInfo getFirstListItemInfo() {
		return listItemsInfos.get(0);
	}

	public ListItemInfo getSecondListItemInfo() {
		return listItemsInfos.get(1);
	}

	public List<Integer> getListsIndexes() {
		return listsIndexes;
	}

	public Integer getNumberOfColumns() {
		return numberOfColumns;
	}

	public int getNumberOfListItemsAndLists() {
		return getNumberOfListItems() + getNumberOfLists();
	}

	public int getNumberOfListItems() {
		return listItemsInfos.size();
	}

	public int getNumberOfLists() {
		return listsIndexes.size();
	}

	public List<Integer> getListsIndexesContainedInListItemsIndexes(List<ListItemInfo> listItemsInfos) {
		if (listsIndexes.isEmpty()) {
			return new ArrayList<>();
		}
		int firstIndex = listItemsInfos.get(0).getIndex();
		int lastIndex = listItemsInfos.get(listItemsInfos.size() - 1).getIndex();
		List<Integer> newListIndexes = listsIndexes.stream().filter(i -> i > firstIndex && i < lastIndex)
		                                            .collect(Collectors.toList());
		for (int i = firstIndex - 1; i >= getListsStart(); i--) {
			if (!listsIndexes.contains(i)) {
				break;
			}
			newListIndexes.add(i);
		}
		for (int i = lastIndex + 1; i <= getListsEnd(); i++) {
			if (!listsIndexes.contains(i)) {
				break;
			}
			newListIndexes.add(i);
		}
		Collections.sort(newListIndexes);
		return newListIndexes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ListInterval interval = (ListInterval) o;
		return listItemsInfos.equals(interval.listItemsInfos) && listsIndexes.equals(interval.listsIndexes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(listItemsInfos, listsIndexes);
	}

	public boolean contains(ListInterval second) {
		return this.getStart() <= second.getStart() && this.getEnd() >= second.getEnd();
	}
}
