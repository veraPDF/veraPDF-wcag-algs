package org.verapdf.wcag.algorithms.entities.lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListInterval {
	private List<Integer> listItemsIndexes = new ArrayList<>();
	private List<Integer> listsIndexes = new ArrayList<>();
	public Integer numberOfColumns;

	public ListInterval(List<Integer> listItemsIndexes, List<Integer> listsIndexes) {
		this.listItemsIndexes = listItemsIndexes;
		this.listsIndexes = listsIndexes;
	}

	public ListInterval(List<Integer> listItemsIndexes, List<Integer> listsIndexes, Integer numberOfColumns) {
		this.listItemsIndexes = listItemsIndexes;
		this.listsIndexes = listsIndexes;
		this.numberOfColumns = numberOfColumns;
	}

	public ListInterval(int start, int end) {
		for (int i = start; i <= end; i++) {
			listItemsIndexes.add(i);
		}
	}

	public int getStart() {
		if (!listItemsIndexes.isEmpty() && !listsIndexes.isEmpty()) {
			return Math.min(getListItemsStart(), getListsStart());
		}
		if (listsIndexes.isEmpty()) {
			return getListItemsStart();
		}
		return getListsStart();
	}

	public Integer getListItemsStart() {
		return listItemsIndexes.isEmpty() ? null : listItemsIndexes.get(0);
	}

	public Integer getListsStart() {
		return listsIndexes.isEmpty() ? null : listsIndexes.get(0);
	}

	public int getEnd() {
		if (!listItemsIndexes.isEmpty() && !listsIndexes.isEmpty()) {
			return Math.max(getListItemsEnd(), getListsEnd());
		}
		if (listsIndexes.isEmpty()) {
			return getListItemsEnd();
		}
		return getListsEnd();
	}

	public Integer getListItemsEnd() {
		return listItemsIndexes.isEmpty() ? null : listItemsIndexes.get(listItemsIndexes.size() - 1);
	}

	public Integer getListsEnd() {
		return listsIndexes.isEmpty() ? null : listsIndexes.get(listsIndexes.size() - 1);
	}

	public List<Integer> getListItemsIndexes() {
		return listItemsIndexes;
	}

	public List<Integer> getListsIndexes() {
		return listsIndexes;
	}

	public Integer getNumberOfColumns() {
		return numberOfColumns;
	}

	public int getNumberOfListItemsAndLists() {
		return listItemsIndexes.size() + listsIndexes.size();
	}

	public int getNumberOfListItems() {
		return listItemsIndexes.size();
	}

	public int getNumberOfLists() {
		return listsIndexes.size();
	}

	public List<Integer> getListsIndexesContainedInListItemsIndexes(List<Integer> listItemsIndexes) {
		if (listsIndexes.isEmpty()) {
			return new ArrayList<>();
		}
		int firstIndex = listItemsIndexes.get(0);
		int lastIndex = listItemsIndexes.get(listItemsIndexes.size() - 1);
		List<Integer> newListIndexes =  listsIndexes.stream().filter(i -> i > firstIndex && i < lastIndex)
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
		return listItemsIndexes.equals(interval.listItemsIndexes) && listsIndexes.equals(interval.listsIndexes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(listItemsIndexes, listsIndexes);
	}

	public boolean contains(ListInterval second) {
		return this.getStart() <= second.getStart() && this.getEnd() >= second.getEnd();
	}
}
