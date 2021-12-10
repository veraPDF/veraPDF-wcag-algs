package org.verapdf.wcag.algorithms.entities.lists;

import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class ListIntervalsCollection {

	private final SortedSet<ListInterval> set;

	public ListIntervalsCollection(Set<ListInterval> listIntervals) {
		set = new TreeSet<>(Comparator.comparing(ListInterval::getStart));
		set.addAll(listIntervals);
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

	public SortedSet<ListInterval> getSet() {
		return set;
	}
}
