package com.hti.smpp.common.services.Impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.services.UserDAService;
import com.hti.smpp.common.user.dto.RechargeEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.GlobalVars;

@Service
public class UserDAServiceImpl implements UserDAService {

	private Logger logger = LoggerFactory.getLogger(UserDAServiceImpl.class);

	@Autowired
	private UserEntryRepository userRepository;

	public UserDAServiceImpl() {
		GlobalVars.UserEntries = GlobalVars.hazelInstance.getMap("user_entries");
		GlobalVars.BalanceEntries = GlobalVars.hazelInstance.getMap("balance_entries");
		GlobalVars.UserMapping = GlobalVars.hazelInstance.getMap("user_mapping");
		GlobalVars.ProfessionEntries = GlobalVars.hazelInstance.getMap("profession_entries");
		GlobalVars.WebmasterEntries = GlobalVars.hazelInstance.getMap("webmaster_entries");
		GlobalVars.DlrSettingEntries = GlobalVars.hazelInstance.getMap("dlrSetting_entries");
		GlobalVars.UserFlagStatus = GlobalVars.hazelInstance.getMap("user_flag_status");
		logger.info("UserEntries: " + GlobalVars.UserEntries.size());
		logger.info("BalanceEntries: " + GlobalVars.BalanceEntries.size());
		logger.info("ProfessionEntries: " + GlobalVars.ProfessionEntries.size());
		logger.info("WebmasterEntries: " + GlobalVars.WebmasterEntries.size());
		logger.info("DlrSettingEntries: " + GlobalVars.DlrSettingEntries.size());

	}

	public UserEntry getUserEntry(int userid) {
		logger.debug("getUserEntry(" + userid + ")");
		if (GlobalVars.UserEntries.containsKey(userid)) {
			return GlobalVars.UserEntries.get(userid);
		} else {
			return null;
		}
	}

	// ------------- list names -----------------------
	@Override
	public Map<Integer, String> listUsers() {
		logger.debug("listUsers()");
		Map<Integer, String> map = new HashMap<Integer, String>();
		for (UserEntry entry : userRepository.findAll()) {
			map.put(entry.getId(), entry.getSystemId());
		}
		Map<Integer, String> sortedMap = map.entrySet().stream()
				.sorted(Entry.comparingByValue(Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return sortedMap;
	}

	@Override
	public Map<Integer, String> listUsersUnderMaster(String master) {
		logger.debug("listUsersUnderMaster(" + master + ")");
		Map<Integer, String> map = new HashMap<Integer, String>();
		for (UserEntry entry : GlobalVars.UserEntries.values()) {
			if (entry.getMasterId().equalsIgnoreCase(master)) {
				map.put(entry.getId(), entry.getSystemId());
			}
		}
		Map<Integer, String> sortedMap = map.entrySet().stream()
				.sorted(Entry.comparingByValue(Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return sortedMap;
	}

	@Override
	public Map<Integer, String> listUsersUnderSeller(int seller) {
		logger.debug("listUsersUnderSeller(" + seller + ")");
		Map<Integer, String> map = new HashMap<Integer, String>();
		for (WebMasterEntry entry : GlobalVars.WebmasterEntries.values()) {
			if (entry.getExecutiveId() == seller) {
				UserEntry userEntry = GlobalVars.UserEntries.get(entry.getUserId());
				if (userEntry.getRole().equalsIgnoreCase("admin")) {
					Map<Integer, String> under_users = listUsersUnderMaster(userEntry.getSystemId());
					map.putAll(under_users);
				}
				map.put(entry.getUserId(), userEntry.getSystemId());
			}
		}
		Map<Integer, String> sortedMap = map.entrySet().stream()
				.sorted(Entry.comparingByValue(Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return sortedMap;
	}

	@Override
	public Map<Integer, RechargeEntry> listRecentRecharges(Integer[] userid) {
		logger.debug("listRecentRecharges(" + userid + ")");
		Map<Integer, RechargeEntry> map = new HashMap<Integer, RechargeEntry>();
		List<RechargeEntry> results = (List<RechargeEntry>) listRecentRecharges(userid);
		for (RechargeEntry entry : results) {
			map.put(entry.getUserId(), entry);
		}
		return map;
	}

	@Override
	public Map<Integer, List<RechargeEntry>> listTransactions(Integer[] userid) {
		return listTransactions(userid, null, null, null);
	}

	@Override
	public UserEntry getInternUserEntry() {
		logger.debug("Checking For User Internal User ");
		List<UserEntry> userEntry = userRepository.getByRole("internal");
		for (UserEntry entry : userEntry) {
			return entry;
		}
		return null;
	}

	@Override
	public Map<Integer, List<RechargeEntry>> listTransactions(Integer[] userid, String txnType, String startTime,
			String endTime) {
		// TODO Auto-generated method stub
		return null;
	}

}
