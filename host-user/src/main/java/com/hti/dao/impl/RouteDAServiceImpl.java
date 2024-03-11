package com.hti.dao.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.dao.RouteDAService;
import com.hti.objects.RoutingDTO;
import com.hti.route.dto.HlrRouteEntry;
import com.hti.route.dto.MmsRouteEntry;
import com.hti.route.dto.OptionalRouteEntry;
import com.hti.route.dto.RouteEntry;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;

public class RouteDAServiceImpl implements RouteDAService {
	private Logger logger = LoggerFactory.getLogger(RouteDAServiceImpl.class);
	// private MultiMap<Integer, Integer> user_route_mapping;
	private IMap<Integer, RouteEntry> basic_routing;
	private IMap<Integer, HlrRouteEntry> hlr_routing;
	private IMap<Integer, MmsRouteEntry> mms_routing;
	private IMap<Integer, OptionalRouteEntry> optional_routing;

	public RouteDAServiceImpl() {
		basic_routing = GlobalVars.hazelInstance.getMap("basic_routing");
		hlr_routing = GlobalVars.hazelInstance.getMap("hlr_routing");
		optional_routing = GlobalVars.hazelInstance.getMap("optional_routing");
		mms_routing = GlobalVars.hazelInstance.getMap("mms_routing");
	}

	@Override
	public Map<Integer, RoutingDTO> listRouting(String systemId) {
		int user_id = 0;
		if (GlobalCache.SystemIdMapping.containsKey(systemId)) {
			user_id = GlobalCache.SystemIdMapping.get(systemId);
		}
		Predicate<Integer, RouteEntry> p = new PredicateBuilderImpl().getEntryObject().get("userId").equal(user_id);
		Map<Integer, RoutingDTO> routing = new HashMap<Integer, RoutingDTO>();
		OptionalRouteEntry optional = null;
		HlrRouteEntry hlrEntry = null;
		RoutingDTO routeDTO = null;
		for (RouteEntry basic : basic_routing.values(p)) {
			optional = optional_routing.get(basic.getId());
			hlrEntry = hlr_routing.get(basic.getId());
			Set<String> reg_sender_list = new HashSet<String>();
			if (optional.getRegSender() != null && optional.getRegSender().length() > 0) {
				reg_sender_list.addAll(Arrays.asList(optional.getRegSender().toLowerCase().split(",")));
			}
			// --------- pdu expiry -------------------------------------------
			String setExpiry = optional.getExpiredOn();
			if (setExpiry != null && !setExpiry.equalsIgnoreCase("0")) { // check the format
				long expiry_hours = 0;
				try {
					expiry_hours = Long.parseLong(setExpiry);
				} catch (NumberFormatException ne) {
					logger.warn(optional.getRouteId() + ": Invalid pdu Expiry Configured");
				}
				if (expiry_hours > 0) {
					setExpiry = "0000";
					long seconds = expiry_hours * 3600;
					long days = seconds / 86400;
					if (days > 0) {
						if (days < 10) {
							setExpiry += "0" + days;
						} else {
							setExpiry += days;
						}
					} else {
						setExpiry += "00";
					}
					long remain = seconds % 86400;
					if (remain > 0) {
						long hours = remain / 3600;
						if (hours > 0) {
							if (hours < 10) {
								setExpiry += "0" + hours;
							} else {
								setExpiry += hours;
							}
						} else {
							setExpiry += "00";
						}
					} else {
						setExpiry += "00";
					}
					setExpiry = setExpiry + "0000000R";
				} else {
					setExpiry = "0";
				}
			}
			// --------- end pdu expiry ---------------------------------------
			String numberLength = null;
			if (GlobalCache.NetworkEntries.containsKey(basic.getNetworkId())) {
				numberLength = GlobalCache.NetworkEntries.get(basic.getNetworkId()).getNumberLength();
			}
			routeDTO = new RoutingDTO(basic.getNetworkId(), basic.getGroupId(), basic.getCost(), hlrEntry.isHlr(),
					hlrEntry.getCost(), optional.getForceSenderNum(), optional.getForceSenderAlpha(), setExpiry,
					optional.getSmsLength(), optional.isRefund(), optional.getCodeLength(), reg_sender_list,
					optional.isReplaceContent(), optional.getReplaceContentMap(), optional.getSourceAppender(),
					optional.getMsgAppender(), optional.getRegGroupId(), hlrEntry.isMnp(), numberLength);
			// -------- dynamic values --------------
			routeDTO.setUsername(systemId);
			if (optional.getSenderReplFrom() != null && optional.getSenderReplFrom().length() > 0
					&& optional.getSenderReplTo() != null && optional.getSenderReplTo().length() > 0) {
				String[] repl_from = optional.getSenderReplFrom().split(",");
				String[] repl_to = optional.getSenderReplTo().split(",");
				int map_size = 0;
				if (repl_from.length == repl_to.length) {
					map_size = repl_from.length;
				} else {
					if (repl_from.length > repl_to.length) {
						map_size = repl_to.length;
					} else if (repl_to.length > repl_from.length) {
						map_size = repl_from.length;
					}
				}
				if (map_size > 0) {
					Map<String, String> senderReplacement = new HashMap<String, String>();
					for (int i = 0; i < map_size; i++) {
						senderReplacement.put(repl_from[i].toLowerCase(), repl_to[i]);
					}
					routeDTO.setSenderReplacement(senderReplacement);
					logger.info(systemId + "[" + basic.getId() + "]: " + senderReplacement);
				} else {
					logger.info(systemId + "[" + basic.getId() + "]: Sender Replacement Not Applicable");
				}
			}
			if (basic.getSmscId() == 0) {
				routeDTO.setSmsc("Down");
			} else {
				if (GlobalCache.SmscEntries.containsKey(basic.getSmscId())) {
					routeDTO.setSmsc(GlobalCache.SmscEntries.get(basic.getSmscId()).getName());
				} else {
					logger.error(basic.getSmscId() + " Not Found in Cluster Cache.");
					routeDTO.setSmsc("Down");
				}
			}
			if (optional.getNumSmscId() > 0) {
				if (GlobalCache.SmscEntries.containsKey(optional.getNumSmscId())) {
					routeDTO.setNumsmsc(GlobalCache.SmscEntries.get(optional.getNumSmscId()).getName());
				} else {
					logger.error(optional.getNumSmscId() + " Not Found in Cluster Cache.");
				}
			}
			if (optional.getBackupSmscId() > 0) {
				if (GlobalCache.SmscEntries.containsKey(optional.getBackupSmscId())) {
					routeDTO.setBackupSmsc(GlobalCache.SmscEntries.get(optional.getBackupSmscId()).getName());
				} else {
					logger.error(optional.getBackupSmscId() + " Not Found in Cluster Cache.");
				}
			}
			if (optional.getRegSmscId() > 0) {
				if (GlobalCache.SmscEntries.containsKey(optional.getRegSmscId())) {
					routeDTO.setRegisterSmsc(GlobalCache.SmscEntries.get(optional.getRegSmscId()).getName());
				} else {
					logger.error(optional.getRegSmscId() + " Not Found in Cluster Cache.");
				}
			}
			// ----------- end -----------------------
			routing.put(basic.getNetworkId(), routeDTO);
		}
		if (routing.isEmpty()) {
			return null;
		} else {
			return routing;
		}
	}

	@Override
	public Map<Integer, RoutingDTO> listBasicRouting(String systemId) {
		int user_id = 0;
		if (GlobalCache.SystemIdMapping.containsKey(systemId)) {
			user_id = GlobalCache.SystemIdMapping.get(systemId);
		}
		Predicate<Integer, RouteEntry> p = new PredicateBuilderImpl().getEntryObject().get("userId").equal(user_id);
		Map<Integer, RoutingDTO> routing = new HashMap<Integer, RoutingDTO>();
		RoutingDTO routeDTO = null;
		for (RouteEntry basic : basic_routing.values(p)) {
			routeDTO = new RoutingDTO();
			routeDTO.setUsername(systemId);
			routeDTO.setNetworkId(basic.getNetworkId());
			routeDTO.setGroupId(basic.getGroupId());
			routeDTO.setCost(basic.getCost());
			if (basic.getSmscId() == 0) {
				routeDTO.setSmsc("Down");
			} else {
				if (GlobalCache.SmscEntries.containsKey(basic.getSmscId())) {
					routeDTO.setSmsc(GlobalCache.SmscEntries.get(basic.getSmscId()).getName());
				} else {
					logger.error(basic.getSmscId() + " Not Found in Cluster Cache.");
					routeDTO.setSmsc("Down");
				}
			}
			routing.put(basic.getNetworkId(), routeDTO);
		}
		if (routing.isEmpty()) {
			return null;
		} else {
			return routing;
		}
	}

	@Override
	public Map<Integer, RoutingDTO> listMmsRouting(String systemId) {
		int user_id = 0;
		if (GlobalCache.SystemIdMapping.containsKey(systemId)) {
			user_id = GlobalCache.SystemIdMapping.get(systemId);
		}
		Predicate<Integer, RouteEntry> p = new PredicateBuilderImpl().getEntryObject().get("userId").equal(user_id);
		Map<Integer, RoutingDTO> routing = new HashMap<Integer, RoutingDTO>();
		RoutingDTO routeDTO = null;
		MmsRouteEntry mmsEntry = null;
		HlrRouteEntry hlrEntry = null;
		for (RouteEntry basic : basic_routing.values(p)) {
			mmsEntry = mms_routing.get(basic.getId());
			hlrEntry = hlr_routing.get(basic.getId());
			routeDTO = new RoutingDTO();
			routeDTO.setUsername(systemId);
			routeDTO.setNetworkId(basic.getNetworkId());
			routeDTO.setCost(mmsEntry.getCost());
			if (mmsEntry.getSmscId() == 0) {
				routeDTO.setSmsc("Down");
			} else {
				if (GlobalCache.SmscEntries.containsKey(mmsEntry.getSmscId())) {
					routeDTO.setSmsc(GlobalCache.SmscEntries.get(mmsEntry.getSmscId()).getName());
				} else {
					logger.error(mmsEntry.getSmscId() + " Not Found in Cluster Cache.");
					routeDTO.setSmsc("Down");
				}
			}
			routeDTO.setHlr(hlrEntry.isHlr());
			routeDTO.setHlrCost(hlrEntry.getCost());
			routeDTO.setMnp(hlrEntry.isMnp());
			routing.put(basic.getNetworkId(), routeDTO);
		}
		if (routing.isEmpty()) {
			return null;
		} else {
			return routing;
		}
	}
}
