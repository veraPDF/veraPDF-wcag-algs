package org.verapdf.wcag.algorithms.entities.lists;

import java.util.Set;

public class ListInterval {
	public int start;
	public int end;

	public ListInterval(int start, int end) {
		this.start = start;
		this.end = end;
	}

	@Override
	public int hashCode() {
		return 31 * end + start;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ListInterval)) {
			return false;
		}
		ListInterval that = (ListInterval) o;
		return this.start == that.start && this.end == that.end;
	}

	public boolean contains(ListInterval second) {
		return this.start <= second.start && this.end >= second.end;
	}

	public void put(Set<ListInterval> set, ListInterval listInterval) {
		for (ListInterval interval : set) {
			if (interval.contains(listInterval)) {
				return;
			} else if (listInterval.contains(interval)) {
				set.remove(interval);
			}
		}
		set.add(listInterval);
	}
}
