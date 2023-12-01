package com.hti.service;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.dao.NetworkDAService;
import com.hti.dao.RouteDAService;
import com.hti.dao.SmscDAService;
import com.hti.dao.UserDAService;
import com.hti.dao.impl.NetworkDAServiceImpl;
import com.hti.dao.impl.RouteDAServiceImpl;
import com.hti.dao.impl.SmscDAServiceImpl;
import com.hti.dao.impl.UserDAServiceImpl;
import com.hti.network.dto.NetworkEntry;
import com.hti.route.dto.HlrRouteEntry;
import com.hti.route.dto.MmsRouteEntry;
import com.hti.route.dto.OptionalRouteEntry;
import com.hti.route.dto.RouteEntry;
import com.hti.smsc.dto.GroupEntry;
import com.hti.smsc.dto.GroupMemberEntry;
import com.hti.smsc.dto.SmscEntry;
import com.hti.user.dto.BalanceEntry;
import com.hti.user.dto.DlrSettingEntry;
import com.hti.user.dto.ProfessionEntry;
import com.hti.user.dto.UserEntry;
import com.hti.user.dto.WebMasterEntry;
import com.hti.util.Constants;
import com.hti.util.FileUtil;
import com.hti.util.FlagStatus;
import com.hti.util.GlobalVar;

public class CacheService {
	private static Logger logger = LoggerFactory.getLogger(CacheService.class);

	public static void init() throws FileNotFoundException {
		GlobalVar.network_entries = GlobalVar.hazelInstance.getMap("network_entries");
		GlobalVar.smsc_entries = GlobalVar.hazelInstance.getMap("smsc_entries");
		GlobalVar.smsc_name_mapping = GlobalVar.hazelInstance.getMap("smsc_name_mapping");
		GlobalVar.smsc_group = GlobalVar.hazelInstance.getMap("smsc_group");
		GlobalVar.smsc_group_member = GlobalVar.hazelInstance.getMultiMap("smsc_group_member");
		GlobalVar.user_entries = GlobalVar.hazelInstance.getMap("user_entries");
		GlobalVar.balance_entries = GlobalVar.hazelInstance.getMap("balance_entries");
		GlobalVar.user_mapping = GlobalVar.hazelInstance.getMap("user_mapping");
		GlobalVar.profession_entries = GlobalVar.hazelInstance.getMap("profession_entries");
		GlobalVar.webmaster_entries = GlobalVar.hazelInstance.getMap("webmaster_entries");
		GlobalVar.dlrSetting_entries = GlobalVar.hazelInstance.getMap("dlrSetting_entries");
		GlobalVar.basic_routing = GlobalVar.hazelInstance.getMap("basic_routing");
		GlobalVar.hlr_routing = GlobalVar.hazelInstance.getMap("hlr_routing");
		GlobalVar.mms_routing = GlobalVar.hazelInstance.getMap("mms_routing");
		GlobalVar.optional_routing = GlobalVar.hazelInstance.getMap("optional_routing");
		GlobalVar.user_flag_status = GlobalVar.hazelInstance.getMap("user_flag_status");
		GlobalVar.smsc_flag_status = GlobalVar.hazelInstance.getMap("smsc_flag_status");
		GlobalVar.smsc_submit_counter = GlobalVar.hazelInstance.getMap("smsc_submit_count");
		GlobalVar.user_submit_counter = GlobalVar.hazelInstance.getMap("user_submit_count");
		GlobalVar.BatchQueue = GlobalVar.hazelInstance.getMap("batch_queue");
		GlobalVar.HlrBatchQueue = GlobalVar.hazelInstance.getMap("hlr_batch_queue");
		GlobalVar.HttpDlrParam = GlobalVar.hazelInstance.getMap("http_dlr_param");
	}

	public static void loadNetworkEntries() {
		logger.info("********* Start Loading Network Entries ************");
		Map<Integer, NetworkEntry> map = GlobalVar.networkService.list();
		if (!map.isEmpty()) {
			GlobalVar.network_entries.clear();
			GlobalVar.network_entries.putAll(map);
			NetworkEntry defaultEntry = new NetworkEntry("Default", "Default", "0", "0", 0, "0");
			defaultEntry.setId(0);
			GlobalVar.network_entries.put(0, defaultEntry); // default entry
		}
		logger.info("NetworkEntries: " + GlobalVar.network_entries.size());
		logger.info("********* End Loading Network Entries **************");
	}

	public static void loadUserConfig(int user_id) {
		logger.info(" ******* Start Loading User Configuration[" + user_id + "] *********** ");
		UserEntry entry = GlobalVar.userService.getUserEntry(user_id);
		if (entry != null) {
			String password = GlobalVar.dbService.listPassword(user_id);
			entry.setPassword(password);
			// ---- loading user based entries -------------------
			ProfessionEntry professionEntry = GlobalVar.userService.getProfessionEntry(user_id);
			WebMasterEntry webMasterEntry = GlobalVar.userService.getWebMasterEntry(user_id);
			DlrSettingEntry dlrSettingEntry = GlobalVar.userService.getDlrSettingEntry(user_id);
			if (professionEntry != null && webMasterEntry != null && dlrSettingEntry != null) {
				if (!GlobalVar.user_mapping.containsKey(entry.getSystemId())) {
					GlobalVar.user_mapping.put(entry.getSystemId(), entry.getId());
				}
				GlobalVar.user_entries.put(entry.getId(), entry);
				GlobalVar.profession_entries.put(entry.getId(), professionEntry);
				GlobalVar.dlrSetting_entries.put(entry.getId(), dlrSettingEntry);
				GlobalVar.webmaster_entries.put(entry.getId(), webMasterEntry);
				if (!GlobalVar.balance_entries.containsKey(entry.getId())) {
					BalanceEntry balance = GlobalVar.userService.getBalance(entry.getId());
					GlobalVar.balance_entries.put(entry.getId(), balance);
				}
				// ---- loading routing based entries -------------------
				logger.info(" ******* Start Loading Routing[" + user_id + "] *********** ");
				com.hazelcast.query.Predicate p = new PredicateBuilderImpl().getEntryObject().get("userId")
						.equal(user_id);
				Set<Integer> set = new HashSet<Integer>(GlobalVar.basic_routing.keySet(p));
				for (int route_id : set) {
					GlobalVar.basic_routing.remove(route_id);
					GlobalVar.optional_routing.remove(route_id);
					GlobalVar.hlr_routing.remove(route_id);
					GlobalVar.mms_routing.remove(route_id);
				}
				try {
					Map<Integer, RouteEntry> basic = GlobalVar.routeService.listBasic(user_id);
					Map<Integer, OptionalRouteEntry> optional = GlobalVar.routeService
							.listOptional(basic.keySet().toArray(new Integer[0]));
					Map<Integer, HlrRouteEntry> hlr = GlobalVar.routeService
							.listHlr(basic.keySet().toArray(new Integer[0]));
					Map<Integer, MmsRouteEntry> mms = GlobalVar.routeService
							.listMms(basic.keySet().toArray(new Integer[0]));
					for (RouteEntry basicEntry : basic.values()) {
						if (optional.containsKey(basicEntry.getId()) && hlr.containsKey(basicEntry.getId())
								&& mms.containsKey(basicEntry.getId())) {
							GlobalVar.basic_routing.put(basicEntry.getId(), basicEntry);
							GlobalVar.optional_routing.put(basicEntry.getId(), optional.get(basicEntry.getId()));
							GlobalVar.hlr_routing.put(basicEntry.getId(), hlr.get(basicEntry.getId()));
							GlobalVar.mms_routing.put(basicEntry.getId(), mms.get(basicEntry.getId()));
						} else {
							logger.warn("Invalid Route Entry: " + basicEntry.getId());
						}
					}
					logger.info(user_id + " Routing Entries: " + basic.size());
				} catch (Exception ex) {
					logger.error(user_id + "", ex);
				}
				logger.info("********** End Loading Routing[" + user_id + "] *********** ");
			} else {
				logger.error("Missing User Configuration: " + entry);
			}
		} else {
			logger.error("Invalid UserId: " + user_id);
		}
		logger.info(" ******* End Loading User Config[" + user_id + "] *********** ");
	}

	public static void loadUserConfig(Set<Integer> users) {
		logger.info(" ******* Start Loading User Configuration. Total[" + users.size() + "] *********** ");
		Map<Integer, UserEntry> entries = GlobalVar.userService.listUser(users.toArray(new Integer[0]));
		Map<Integer, Map<Integer, RouteEntry>> basic = GlobalVar.routeService
				.listBasic(entries.keySet().toArray(new Integer[0]));
		Map<Integer, String> password_list = GlobalVar.dbService.listPassword(users);
		Map<Integer, RouteEntry> basic_final = new java.util.HashMap<Integer, RouteEntry>();
		Set<Integer> basicRouteIdSet = new HashSet<Integer>();
		for (UserEntry entry : entries.values()) {
			int user_id = entry.getId();
			if (password_list.containsKey(user_id)) {
				entry.setPassword(password_list.get(user_id));
			}
			try {
				// ---- loading user based entries -------------------
				ProfessionEntry professionEntry = GlobalVar.userService.getProfessionEntry(user_id);
				WebMasterEntry webMasterEntry = GlobalVar.userService.getWebMasterEntry(user_id);
				DlrSettingEntry dlrSettingEntry = GlobalVar.userService.getDlrSettingEntry(user_id);
				if (professionEntry != null && webMasterEntry != null && dlrSettingEntry != null) {
					if (!GlobalVar.user_mapping.containsKey(entry.getSystemId())) {
						GlobalVar.user_mapping.put(entry.getSystemId(), entry.getId());
					}
					GlobalVar.user_entries.put(entry.getId(), entry);
					GlobalVar.profession_entries.put(entry.getId(), professionEntry);
					GlobalVar.dlrSetting_entries.put(entry.getId(), dlrSettingEntry);
					GlobalVar.webmaster_entries.put(entry.getId(), webMasterEntry);
					if (!GlobalVar.balance_entries.containsKey(entry.getId())) {
						BalanceEntry balance = GlobalVar.userService.getBalance(entry.getId());
						GlobalVar.balance_entries.put(entry.getId(), balance);
					}
					basic_final.putAll(basic.get(user_id));
					basicRouteIdSet.addAll(basic.get(user_id).keySet());
				} else {
					basic.remove(user_id);
					logger.error("Missing User Configuration: " + entry);
				}
			} catch (Exception ex) {
				logger.error(user_id + "", ex);
			}
		}
		try {
			Map<Integer, OptionalRouteEntry> optional = GlobalVar.routeService
					.listOptional(basicRouteIdSet.toArray(new Integer[0]));
			Map<Integer, HlrRouteEntry> hlr = GlobalVar.routeService.listHlr(basicRouteIdSet.toArray(new Integer[0]));
			Map<Integer, MmsRouteEntry> mms = GlobalVar.routeService.listMms(basicRouteIdSet.toArray(new Integer[0]));
			com.hazelcast.query.Predicate p = null;
			// ---- loading routing based entries -------------------
			logger.info(" ******* Start Loading Routing For Users: [" + basic.keySet() + "] *********** ");
			p = new PredicateBuilderImpl().getEntryObject().get("userId")
					.in(basic.keySet().stream().toArray(Integer[]::new));
			Set<Integer> set = new HashSet<Integer>(GlobalVar.basic_routing.keySet(p));
			logger.info("***** Removing Cached Entries ******** ");
			logger.info("Basic Routing Size: " + GlobalVar.basic_routing.size());
			GlobalVar.basic_routing.removeAll(p);
			logger.info("After Remove Basic Size: " + GlobalVar.basic_routing.size());
			p = new PredicateBuilderImpl().getEntryObject().get("routeId").in(set.stream().toArray(Integer[]::new));
			logger.info("Optional Routing Size: " + GlobalVar.optional_routing.size());
			GlobalVar.optional_routing.removeAll(p);
			logger.info("After Remove Optional Size: " + GlobalVar.optional_routing.size());
			logger.info("Hlr Routing Size : " + GlobalVar.hlr_routing.size());
			GlobalVar.hlr_routing.removeAll(p);
			logger.info("After Remove Hlr Size: " + GlobalVar.hlr_routing.size());
			logger.info("Mms Routing Size : " + GlobalVar.mms_routing.size());
			GlobalVar.mms_routing.removeAll(p);
			logger.info("After Remove Mms Size: " + GlobalVar.mms_routing.size());
			GlobalVar.basic_routing.putAll(basic_final);
			GlobalVar.optional_routing.putAll(optional);
			GlobalVar.hlr_routing.putAll(hlr);
			GlobalVar.mms_routing.putAll(mms);
			logger.info("Final Basic Size: " + GlobalVar.basic_routing.size() + " Hlr: " + GlobalVar.hlr_routing.size()
					+ " Optional: " + GlobalVar.optional_routing.size() + " Mms:" + GlobalVar.mms_routing.size());
		} catch (Exception ex) {
			logger.error("", ex);
		}
		logger.info(" ******* End Loading Users Config[" + users.size() + "] *********** ");
	}

	public static void loadUserEntry(int user_id) {
		logger.info("loadUserEntry: " + user_id);
		UserEntry entry = GlobalVar.userService.getUserEntry(user_id);
		if (entry != null) {
			String password = GlobalVar.dbService.listPassword(user_id);
			entry.setPassword(password);
			if (!GlobalVar.user_mapping.containsKey(entry.getSystemId())) {
				GlobalVar.user_mapping.put(entry.getSystemId(), entry.getId());
			}
			// ---- loading user based entries -------------------
			ProfessionEntry professionEntry = GlobalVar.userService.getProfessionEntry(user_id);
			WebMasterEntry webMasterEntry = GlobalVar.userService.getWebMasterEntry(user_id);
			DlrSettingEntry dlrSettingEntry = GlobalVar.userService.getDlrSettingEntry(user_id);
			GlobalVar.user_entries.put(entry.getId(), entry);
			GlobalVar.profession_entries.put(entry.getId(), professionEntry);
			GlobalVar.webmaster_entries.put(entry.getId(), webMasterEntry);
			GlobalVar.dlrSetting_entries.put(entry.getId(), dlrSettingEntry);
		}
	}

	public static void removeUserConfig(String system_id) { // remove routing & balance
		int user_id = GlobalVar.user_mapping.remove(system_id);
		// remove routing & balance
		GlobalVar.user_entries.remove(user_id);
		GlobalVar.profession_entries.remove(user_id);
		GlobalVar.webmaster_entries.remove(user_id);
		GlobalVar.dlrSetting_entries.remove(user_id);
		GlobalVar.balance_entries.remove(user_id);
		logger.info(user_id + " User & Balance Entry Removed.");
		com.hazelcast.query.Predicate p = new PredicateBuilderImpl().getEntryObject().get("userId").equal(user_id);
		Set<Integer> set = new HashSet<Integer>(GlobalVar.basic_routing.keySet(p));
		for (int route_id : set) {
			GlobalVar.basic_routing.remove(route_id);
			GlobalVar.optional_routing.remove(route_id);
			GlobalVar.hlr_routing.remove(route_id);
			GlobalVar.mms_routing.remove(route_id);
		}
		logger.info(user_id + " Route Entries Removed.");
	}

	public static void loadRouting() {
		logger.info(" ******* Start Loading Routing *********** ");
		try {
			// Map<Integer, Map<Integer, Integer>> allocation = service.listProfileAllocation();
			Map<Integer, RouteEntry> basic = GlobalVar.routeService.listBasic();
			Map<Integer, OptionalRouteEntry> optional = GlobalVar.routeService
					.listOptional(basic.keySet().toArray(new Integer[0]));
			Map<Integer, HlrRouteEntry> hlr = GlobalVar.routeService.listHlr(basic.keySet().toArray(new Integer[0]));
			Map<Integer, MmsRouteEntry> mms = GlobalVar.routeService.listMms(basic.keySet().toArray(new Integer[0]));
			// -------- clear existing cache -------------------
			GlobalVar.basic_routing.clear();
			GlobalVar.optional_routing.clear();
			GlobalVar.hlr_routing.clear();
			GlobalVar.mms_routing.clear();
			// -------- match keys from all 3 maps -----
			optional.keySet().retainAll(hlr.keySet());
			hlr.keySet().retainAll(optional.keySet());
			basic.keySet().retainAll(optional.keySet());
			mms.keySet().retainAll(optional.keySet());
			GlobalVar.basic_routing.putAll(basic);
			GlobalVar.optional_routing.putAll(optional);
			GlobalVar.hlr_routing.putAll(hlr);
			GlobalVar.mms_routing.putAll(mms);
		} catch (Exception e) {
			logger.error("loadRouting()", e.fillInStackTrace());
		}
		logger.info("Basic Routing Entries: " + GlobalVar.basic_routing.size());
		logger.info("Hlr Routing Entries: " + GlobalVar.hlr_routing.size());
		logger.info("Optional Routing Entries: " + GlobalVar.optional_routing.size());
		logger.info("Mms Routing Entries: " + GlobalVar.mms_routing.size());
		logger.info("********** End Loading Routing *********** ");
	}

	public static void loadUserConfig() {
		logger.info("**** Start Loading User Configuration ********");
		Map<Integer, UserEntry> usermaster = GlobalVar.userService.listUser();
		Map<Integer, ProfessionEntry> profession = GlobalVar.userService.listProfession();
		Map<Integer, WebMasterEntry> webmaster = GlobalVar.userService.listWebMaster();
		Map<Integer, DlrSettingEntry> dlrSetting = GlobalVar.userService.listDlrSetting();
		Map<Integer, BalanceEntry> balancemaster = GlobalVar.userService.listBalance();
		Map<Integer, String> password_list = GlobalVar.dbService.listPassword(usermaster.keySet());
		// -------- clear existing cache -------------------
		GlobalVar.user_entries.clear();
		GlobalVar.profession_entries.clear();
		GlobalVar.webmaster_entries.clear();
		GlobalVar.dlrSetting_entries.clear();
		GlobalVar.user_mapping.clear();
		// ------- put new entries -------------------------
		for (Map.Entry<Integer, UserEntry> entry : usermaster.entrySet()) {
			if (profession.containsKey(entry.getKey()) && webmaster.containsKey(entry.getKey())
					&& dlrSetting.containsKey(entry.getKey()) && balancemaster.containsKey(entry.getKey())) {
				String system_id = entry.getValue().getSystemId();
				if (password_list.containsKey(entry.getKey())) {
					entry.getValue().setPassword(password_list.get(entry.getKey()));
				}
				try {
					String flag = FileUtil.readFlag(Constants.USER_FLAG_DIR + system_id + ".txt", true);
					if (flag.equalsIgnoreCase(FlagStatus.BLOCKED)) {
						GlobalVar.user_flag_status.put(system_id, FlagStatus.BLOCKED);
					} else {
						GlobalVar.user_flag_status.put(system_id, FlagStatus.DEFAULT);
						if (!flag.equalsIgnoreCase(FlagStatus.DEFAULT)) {
							FileUtil.setDefaultFlag(Constants.USER_FLAG_DIR + system_id + ".txt");
						}
					}
				} catch (Exception e) {
					logger.error(system_id, e);
				}
				GlobalVar.user_entries.put(entry.getKey(), entry.getValue());
				GlobalVar.webmaster_entries.put(entry.getKey(), webmaster.get(entry.getKey()));
				GlobalVar.profession_entries.put(entry.getKey(), profession.get(entry.getKey()));
				GlobalVar.dlrSetting_entries.put(entry.getKey(), dlrSetting.get(entry.getKey()));
				GlobalVar.user_mapping.put(system_id, entry.getKey());
				GlobalVar.balance_entries.put(entry.getKey(), balancemaster.get(entry.getKey()));
			} else {
				logger.warn("Invalid User Entry: " + entry.getKey());
			}
		}
		logger.info("User Entries: " + GlobalVar.user_entries.size());
		logger.info("Balance Entries: " + GlobalVar.balance_entries.size());
		logger.info("User Mapping: " + GlobalVar.user_mapping.size());
		logger.info("WebMaster Entries: " + GlobalVar.webmaster_entries.size());
		logger.info("Profession Entries: " + GlobalVar.profession_entries.size());
		logger.info("DlrSetting Entries: " + GlobalVar.dlrSetting_entries.size());
		logger.info("***** End Loading User Configuration **********");
	}

	public static Map<String, Integer> listUsernames() {
		return GlobalVar.userService.listUsernames();
	}

	public static Map<String, Integer> listSmscNames() {
		return GlobalVar.smscService.listNames();
	}

	public static void loadSmscEntries() {
		logger.info("********* Start Loading Smsc Configuration **************");
		Map<Integer, SmscEntry> smscmaster = GlobalVar.smscService.list();
		GlobalVar.smsc_entries.clear();
		GlobalVar.smsc_name_mapping.clear();
		Map<Integer, String> driver_list = GlobalVar.dbService.listSmscDriver();
		for (Map.Entry<Integer, SmscEntry> entry : smscmaster.entrySet()) {
			if (driver_list.containsKey(entry.getKey())) {
				entry.getValue().setPassword(driver_list.get(entry.getKey()));
				GlobalVar.smsc_entries.put(entry.getKey(), entry.getValue());
				GlobalVar.smsc_name_mapping.put(entry.getValue().getName(), entry.getKey());
				Properties props = FileUtil.readSmscFlag(Constants.SMSC_FLAG_DIR + entry.getValue().getName() + ".txt",
						true);
				GlobalVar.smsc_flag_status.put(entry.getValue().getName(), props);
			}
		}
		logger.info("SmscEntries: " + GlobalVar.smsc_entries.size());
		logger.info("******** End Loading Smsc Configuration *****************");
	}

	public static void loadSmscEntry(int smsc_id) {
		logger.info("********* Start Loading Smsc Entry[" + smsc_id + "] ********** ");
		SmscEntry entry = GlobalVar.smscService.getEntry(smsc_id);
		String driver = GlobalVar.dbService.getSmscDriver(smsc_id);
		entry.setPassword(driver);
		GlobalVar.smsc_entries.put(entry.getId(), entry);
		GlobalVar.smsc_name_mapping.put(entry.getName(), entry.getId());
		logger.info("******** End Loading Smsc Entry[" + smsc_id + "] *****************");
	}

	public static void loadSmscGroup() {
		logger.info("********** Start loading Smsc Group ******************");
		GlobalVar.smsc_group.clear();
		List<GroupEntry> list = GlobalVar.smscService.listGroup();
		for (GroupEntry entry : list) {
			if (GlobalVar.smsc_entries.containsKey(entry.getPrimeMember())) {
				entry.setPrimeMemberName(GlobalVar.smsc_entries.get(entry.getPrimeMember()).getName());
			}
			GlobalVar.smsc_group.put(entry.getId(), entry);
		}
		logger.info("SmscGroup: " + GlobalVar.smsc_group.values());
		logger.info("************ End loading Smsc Group *******************");
	}

	public static void loadSmscGroupMember() {
		logger.info("********** Start loading Smsc Group Member ******************");
		GlobalVar.smsc_group_member.clear();
		List<GroupMemberEntry> list = GlobalVar.smscService.listGroupMember();
		for (GroupMemberEntry entry : list) {
			if (GlobalVar.smsc_entries.containsKey(entry.getSmscId())) {
				entry.setSmsc(GlobalVar.smsc_entries.get(entry.getSmscId()).getName());
			}
			GlobalVar.smsc_group_member.put(entry.getGroupId(), entry);
		}
		logger.info("SmscGroupMember: " + GlobalVar.smsc_group_member.values());
		logger.info("************ End loading Smsc Group Member *******************");
	}

	public static void loadSmscSchedule() {
		logger.info("********** Start loading Smsc Schedule ******************");
		GlobalVar.SmscScheduleConfig.clear();
		GlobalVar.SmscScheduleConfig.putAll(GlobalVar.smscService.listSchedule());
		logger.info("SmscSchedule: " + GlobalVar.SmscScheduleConfig);
	}

	public static void shutdown() {
		GlobalVar.hazelInstance.shutdown();
		logger.info("Cache Service Shut Down");
	}
}
