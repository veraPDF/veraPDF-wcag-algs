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

import java.util.*;

public class ListIntervalsCollection {

	private final SortedSet<ListInterval> set;

	public ListIntervalsCollection() {
		set = new TreeSet<>(ListInterval::compareTo);
	}

	public ListIntervalsCollection(Set<ListInterval> listIntervals) {
		this();
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
