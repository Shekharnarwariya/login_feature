package com.hti.dao.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.dao.NetworkDAService;
import com.hti.network.dto.NetworkEntry;
import com.hti.util.GlobalVars;

public class NetworkDAServiceImpl implements NetworkDAService {
	private Logger logger = LoggerFactory.getLogger(NetworkDAServiceImpl.class);
	private Map<Integer, NetworkEntry> entries;

	public NetworkDAServiceImpl() {
		entries = GlobalVars.hazelInstance.getMap("network_entries");
		logger.info("NetworkEntries: " + entries.size());
	}

	@Override
	public NetworkEntry getNetworkEntry(int network_id) {
		if (entries.containsKey(network_id)) {
			return entries.get(network_id);
		} else {
			logger.error(network_id + " NetworkEntry Not Found");
		}
		return null;
	}
}
