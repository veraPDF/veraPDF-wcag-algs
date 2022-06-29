package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import java.util.HashMap;
import java.util.Map;

public class IdMapper {
	private final Map<Long,Long> map = new HashMap<>();

	public void put(Long key, Long value) {
		Long newValue = map.getOrDefault(value, value);
		map.put(key, newValue);
		for (Map.Entry<Long, Long> entry : map.entrySet()) {
			if (entry.getValue().equals(key)) {
				map.put(entry.getKey(), newValue);
			}
		}
	}

	public Long get(Long key) {
		return map.get(key);
	}
}
