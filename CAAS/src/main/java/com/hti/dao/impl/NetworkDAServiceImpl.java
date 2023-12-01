package com.hti.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.dao.NetworkDAO;
import com.hti.dao.NetworkDAService;
import com.hti.network.dto.NetworkEntry;
import com.hti.util.GlobalVar;

public class NetworkDAServiceImpl implements NetworkDAService {
	private NetworkDAO networkDAO;
	private Logger logger = LoggerFactory.getLogger(NetworkDAServiceImpl.class);

	public NetworkDAServiceImpl() {
		this.networkDAO = GlobalVar.context.getBean(NetworkDAO.class);
	}

	@Override
	public Map<Integer, NetworkEntry> list() {
		Map<Integer, NetworkEntry> networks = new HashMap<Integer, NetworkEntry>();
		List<NetworkEntry> list = networkDAO.list();
		for (NetworkEntry entry : list) {
			if (entry.getNumberLength() != null) {
				if (entry.getNumberLength().contains(",")) {
					try {
						if ((Integer.parseInt(entry.getNumberLength().split(",")[0]) <= 0)
								|| Integer.parseInt(entry.getNumberLength().split(",")[1]) <= 0) {
							logger.error("Network[" + entry.getId() + "] Invalid Number Length Range: "
									+ entry.getNumberLength());
							entry.setNumberLength(null);
						} else {
							if (Integer.parseInt(entry.getNumberLength().split(",")[0]) >= Integer
									.parseInt(entry.getNumberLength().split(",")[1])) {
								logger.error("Network[" + entry.getId() + "] Invalid Number Length Range: "
										+ entry.getNumberLength());
								entry.setNumberLength(null);
							}
						}
					} catch (Exception ex) {
						logger.error("Network[" + entry.getId() + "] Invalid Number Length Range: "
								+ entry.getNumberLength());
						entry.setNumberLength(null);
					}
				} else {
					try {
						if (Integer.parseInt(entry.getNumberLength()) <= 0) {
							logger.error(
									"Network[" + entry.getId() + "] Invalid Number Length: " + entry.getNumberLength());
							entry.setNumberLength(null);
						}
					} catch (Exception ex) {
						logger.error(
								"Network[" + entry.getId() + "] Invalid Number Length: " + entry.getNumberLength());
						entry.setNumberLength(null);
					}
				}
			}
			networks.put(entry.getId(), entry);
		}
		return networks;
	}
}
