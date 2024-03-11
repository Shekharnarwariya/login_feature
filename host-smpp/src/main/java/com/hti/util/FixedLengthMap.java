package com.hti.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class FixedLengthMap<K, V> extends LinkedHashMap<K, V> {
	private final int maxSize;

	public FixedLengthMap(int maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxSize;
	}
}
