package org.verapdf.wcag.algorithms.entities.lists;

import java.util.*;

public class ListIntervalsCollection {

	private final SortedSet<ListInterval> set;

	public ListIntervalsCollection() {
		set = new TreeSet<>(Comparator.comparing(ListInterval::getStart));
	}

	public ListIntervalsCollection(Set<ListInterval> listIntervals) {
		set = new TreeSet<>(Comparator.comparing(ListInterval::getStart));
		set.addAll(listIntervals);
	}

	public void put(ListInterval listInterval) {
		Set<ListInterval> intervalsToRemove = new HashSet<>();
		for (ListInterval interval : set) {
			if (interval.contains(listInterval)) {
				return;
			} else if (listInterval.contains(interval)) {
				intervalsToRemove.add(interval);
			}
		}
		for (ListInterval interval : intervalsToRemove) {
			set.remove(interval);
		}
		set.add(listInterval);
	}

	public void putAll(Set<ListInterval> intervalsSet) {
		intervalsSet.forEach(this::put);
	}

	public SortedSet<ListInterval> getSet() {
		return set;
	}
}
