package org.verapdf.wcag.algorithms.entities.lists;

import java.util.HashSet;
import java.util.Set;

public class ListIntervalsCollection {

	private final Set<ListInterval> set;

	public ListIntervalsCollection() {
		set = new HashSet<>();
	}

	public ListIntervalsCollection(Set<ListInterval> listIntervals) {
		set = new HashSet<>(listIntervals);
	}

	public void put(ListInterval listInterval) {
		for (ListInterval interval : set) {
			if (interval.contains(listInterval)) {
				return;
			} else if (listInterval.contains(interval)) {
				set.remove(interval);
			}
		}
		set.add(listInterval);
	}

	public void putAll(Set<ListInterval> intervalsSet) {
		intervalsSet.forEach(this::put);
	}

	public Set<ListInterval> getSet() {
		return set;
	}
}
