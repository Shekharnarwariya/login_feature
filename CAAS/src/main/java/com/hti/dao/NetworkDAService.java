package com.hti.dao;

import java.util.Map;

import com.hti.network.dto.NetworkEntry;

public interface NetworkDAService {
	public Map<Integer, NetworkEntry> list();
}
