package com.hti.smpp.common.sms.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.route.dto.OptionalRouteEntry;
import com.hti.smpp.common.route.dto.RouteEntry;
import com.hti.smpp.common.route.dto.RouteEntryExt;
import com.hti.smpp.common.route.repository.RouteEntryRepository;
import com.hti.smpp.common.sms.service.RouteDAService;
import com.hti.smpp.common.sms.util.GlobalVars;

@Service
public class RouteDAServiceImpl implements RouteDAService {

	private Logger logger = LoggerFactory.getLogger(RouteDAServiceImpl.class);

	@Autowired
	private RouteEntryRepository routeEntryRepository;

	@Override
	public Map<Integer, RouteEntryExt> listRouteEntries(int userId, boolean hlr, boolean optional, boolean display) {
		logger.info("Listing RouteEntries For " + userId + " hlr: " + hlr + " optional: " + optional);

		Map<Integer, RouteEntryExt> list = new HashMap<>();
		Specification<RouteEntry> spec = (root, query, cb) -> cb.equal(root.get("userId"), userId);
		List<RouteEntry> routeEntries = routeEntryRepository.findAll(spec);

		Map<Integer, String> smsc_name_mapping = null;
		Map<Integer, String> group_name_mapping = null;
		if (display) {
			smsc_name_mapping = GlobalVars.smscService.listNames();
			group_name_mapping = GlobalVars.smscService.listGroupNames();
		}
		for (RouteEntry basic : routeEntries) {
			RouteEntryExt entry = new RouteEntryExt(basic);
			if (display) {
				// ------ set user values -----------------
				if (GlobalVars.UserEntries.containsKey(entry.getBasic().getUserId())) {
					entry.setSystemId(GlobalVars.UserEntries.get(entry.getBasic().getUserId()).getSystemId());
					entry.setMasterId(GlobalVars.UserEntries.get(entry.getBasic().getUserId()).getMasterId());
					entry.setCurrency(GlobalVars.UserEntries.get(entry.getBasic().getUserId()).getCurrency());
					entry.setAccountType(GlobalVars.WebmasterEntries.get(basic.getUserId()).getAccountType());
				}
				// ------ set network values -----------------
				// NetworkEntry network = CacheService.getNetworkEntry(entry.getNetworkId());
				if (GlobalVars.NetworkEntries.containsKey(entry.getBasic().getNetworkId())) {
					NetworkEntry network = GlobalVars.NetworkEntries.get(entry.getBasic().getNetworkId());
					entry.setCountry(network.getCountry());
					entry.setOperator(network.getOperator());
					entry.setMcc(network.getMcc());
					entry.setMnc(network.getMnc());
				}
				// ------ set Smsc values -----------------
				if (entry.getBasic().getSmscId() == 0) {
					entry.setSmsc("Down");
				} else {
					if (smsc_name_mapping.containsKey(entry.getBasic().getSmscId())) {
						entry.setSmsc(smsc_name_mapping.get(entry.getBasic().getSmscId()));
					}
				}
				if (group_name_mapping.containsKey(entry.getBasic().getGroupId())) {
					entry.setGroup(group_name_mapping.get(entry.getBasic().getGroupId()));
				}
			}
			if (hlr) {
				if (GlobalVars.HlrRouteEntries.containsKey(basic.getId())) {
					entry.setHlrRouteEntry(GlobalVars.HlrRouteEntries.get(basic.getId()));
				} else {
					logger.info(basic.getId() + " Hlr Entry Not Found For: " + userId);
				}
			}
			if (optional) {
				if (GlobalVars.OptionalRouteEntries.containsKey(basic.getId())) {
					OptionalRouteEntry optEntry = GlobalVars.OptionalRouteEntries.get(basic.getId());
					// OptionalRouteEntryExt optEntryExt = new OptionalRouteEntryExt(optEntry);
					if (display) {
						if (optEntry.getNumSmscId() == 0) {
							entry.setNumSmsc("Down");
						} else {
							if (smsc_name_mapping.containsKey(optEntry.getNumSmscId())) {
								entry.setNumSmsc(smsc_name_mapping.get(optEntry.getNumSmscId()));
							}
						}
						if (optEntry.getBackupSmscId() == 0) {
							entry.setBackupSmsc("Down");
						} else {
							if (smsc_name_mapping.containsKey(optEntry.getBackupSmscId())) {
								entry.setBackupSmsc(smsc_name_mapping.get(optEntry.getBackupSmscId()));
							}
						}
						if (optEntry.getRegSmscId() == 0) {
							entry.setRegSmsc("Down");
						} else {
							if (smsc_name_mapping.containsKey(optEntry.getRegSmscId())) {
								entry.setRegSmsc(smsc_name_mapping.get(optEntry.getRegSmscId()));
							}
						}
						// ----------- Configure Content Replacement ----------------------
						if (optEntry.isReplaceContent()) {
							Map<String, String> content_map = optEntry.getReplaceContentMap();
							String converted_text = "";
							for (Map.Entry<String, String> map_entry : content_map.entrySet()) {
								converted_text += map_entry.getKey() + "|" + map_entry.getValue() + ",";
							}
							if (converted_text.length() > 0) {
								converted_text = converted_text.substring(0, converted_text.length() - 1);
								optEntry.setReplacement(converted_text);
							}
						}
					}
					entry.setRouteOptEntry(optEntry);
					// --------------------------------------
				} else {
					logger.info(basic.getId() + " Optional Entry Not Found For: " + userId);
				}
			}
			list.put(basic.getId(), entry);
		}
		if (list.isEmpty()) {
			logger.info("Routing Entries Not Found For " + userId);
		} else {
			logger.info(userId + " RouteEntries: " + list.size());
		}
		return list;
	}

	private Map<Integer, RouteEntryExt> getNetworkRouting(int userId, boolean hlr) {
		Map<Integer, RouteEntryExt> networkRouting = new HashMap<Integer, RouteEntryExt>();
		Map<Integer, RouteEntryExt> routingEntries = listRouteEntries(userId, hlr, false, false);
		for (RouteEntryExt entry : routingEntries.values()) {
			networkRouting.put(entry.getBasic().getNetworkId(), entry);
		}
		return networkRouting;
	}

	@Override
	public Map<Integer, RouteEntry> getNetworkRouting(int userId) {
		Map<Integer, RouteEntry> networkRouting = new HashMap<Integer, RouteEntry>();
		List<RouteEntry> routeEntries = routeEntryRepository.findByUserId(userId);
		for (RouteEntry entry : routeEntries) {
			networkRouting.put(entry.getNetworkId(), entry);
		}
		return networkRouting;
	}

	@Override
	public double calculateRoutingCost(int userId, List<String> numbers, int msgParts) {
		Map<String, Integer> prefix_mapping = new HashMap<String, Integer>(GlobalVars.PrefixMapping);
		double totalcost = 0;
		Map<Integer, RouteEntryExt> routingEntries = getNetworkRouting(userId, false);
		for (String destination : numbers) {
			int networkId = 0;
			int length = destination.length();
			for (int i = length; i >= 1; i--) {
				if (prefix_mapping.containsKey(destination.substring(0, i))) {
					networkId = prefix_mapping.get(destination.substring(0, i));
					break;
				}
			}
			RouteEntryExt route = null;
			if (routingEntries.containsKey(networkId)) {
				route = routingEntries.get(networkId);
			} else {
				route = routingEntries.get(0); // get default routing
			}
			double cost = 0;
			if (route != null) {
				cost = route.getBasic().getCost();
			}
			totalcost = totalcost + (cost * msgParts);
			// System.out.println("Number: " + destination + " cost: " + cost + " total:" +
			// totalcost + " route: " + route);
		}
		return totalcost;
	}

	@Override
	public double calculateRoutingCost(int userId, Map<String, Integer> numbersParts) {
		Map<String, Integer> prefix_mapping = new HashMap<String, Integer>(GlobalVars.PrefixMapping);
		double totalcost = 0;
		Map<Integer, RouteEntryExt> routingEntries = getNetworkRouting(userId, false);
		for (Map.Entry<String, Integer> entry : numbersParts.entrySet()) {
			String destination = entry.getKey();
			int msgParts = entry.getValue();
			int networkId = 0;
			int length = destination.length();
			for (int i = length; i >= 1; i--) {
				if (prefix_mapping.containsKey(destination.substring(0, i))) {
					networkId = prefix_mapping.get(destination.substring(0, i));
					break;
				}
			}
			RouteEntryExt route = null;
			if (routingEntries.containsKey(networkId)) {
				route = routingEntries.get(networkId);
			} else {
				route = routingEntries.get(0); // get default routing
			}
			double cost = 0;
			if (route != null) {
				cost = route.getBasic().getCost();
			}
			totalcost = totalcost + (cost * msgParts);
			// System.out.println("Number: " + destination + " cost: " + cost + " total:" +
			// totalcost + " route: " + route);
		}
		return totalcost;
	}
}
