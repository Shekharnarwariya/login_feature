package com.hti.smpp.common.service.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.service.UserDAService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.util.GlobalVars;

@Service
public class UserDAServiceImpl implements UserDAService {

	private Logger logger = LoggerFactory.getLogger(UserDAServiceImpl.class);

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

//	@Override
//	public int saveUserEntry(UserEntryExt entry) throws DuplicateEntryException, Exception {
//		logger.debug("saveUserEntry():" + entry);
//		try {
//			int userId = userDAO.saveUserEntry(entry);
//			return userId;
//		} catch (DuplicateEntryException e) {
//			throw new DuplicateEntryException(e.getMessage());
//		} catch (Exception e) {
//			if (entry.getUserEntry().getId() > 0) {
//				userDAO.deleteUserEntry(entry);
//			}
//			throw new Exception(e.getMessage());
//		}
//	}

//	@Override
//	public void updateUserEntry(UserEntry entry) {
//		logger.debug("updateUserEntry()");
//		userDAO.updateUserEntry(entry);
//	}
//
//	@Override
//	public void updateUserEntryExt(UserEntryExt entry) {
//		userDAO.updateUserEntry(entry.getUserEntry());
//		userDAO.updateDlrSettingEntry(entry.getDlrSettingEntry());
//		userDAO.updateWebMasterEntry(entry.getWebMasterEntry());
//		userDAO.updateProfessionEntry(entry.getProfessionEntry());
//	}
//
//	@Override
//	public void deleteUserEntry(UserEntryExt entry) {
//		logger.info("deleteUserEntry: " + entry);
//		userDAO.deleteUserEntry(entry);
//	}

//	@Override
//	public int validateUser(String systemId, String password) {
//		logger.debug("Checking For User Validation: " + systemId + ":" + password);
//		if (GlobalVars.UserMapping.containsKey(systemId)) {
//			int userid = GlobalVars.UserMapping.get(systemId);
//			UserEntry entry = GlobalVars.UserEntries.get(userid);
//			if (entry.getPassword().equals(password)) {
//				return entry.getId();
//			} else {
//				logger.info(systemId + " Invalid Password: " + password);
//				return 0;
//			}
//		} else {
//			logger.info(systemId + ": Invalid SystemId");
//			return 0;
//		}
//	}

//	@Override
//	public int validateUser(String accessKey) {
//		logger.debug("Checking For User Validation AccessKey: " + accessKey);
//		Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject().get("provCode")
//				.equal(accessKey);
//		for (WebMasterEntry webEntryItr : GlobalVars.WebmasterEntries.values(p)) {
//			return webEntryItr.getUserId();
//		}
//		logger.info("Invalid accessKey: " + accessKey);
//		return 0;
//	}

	// ------------ Get Entries ------------------------
//	@Override
//	public UserSessionObject getUserSessionObject(String systemId) {
//		logger.info(systemId + " UserSessionObject creation start");
//		UserSessionObject userSessionObject = new UserSessionObject();
//		try {
//			logger.info(systemId + " Checking For UserEntryExt.");
//			UserEntryExt entryExt = getUserEntryExt(systemId);
//			logger.info(systemId + " UserEntryExt Found.");
//			UserEntry entry = entryExt.getUserEntry();
//			if (entry != null) {
//				BeanUtils.copyProperties(userSessionObject, entry);
//				BeanUtils.copyProperties(userSessionObject, entryExt);
//				userSessionObject.setMasterUserId(GlobalVars.UserMapping.get(entry.getMasterId()));
//				userSessionObject.setBalance(GlobalVars.BalanceEntries.get(userSessionObject.getId()));
//				// Collection<UserEntryExt> list = listUserEntries().values();
//				int admins = 0, users = 0;
//				// int counter = 0;
//				// for (UserEntryExt userEntryExt : list) {
//				// logger.debug(counter + " -> " + userEntryExt);
//				logger.info(systemId + " Checking For Under User Counts.");
//				if (userSessionObject.getRole().equalsIgnoreCase("superadmin")
//						|| userSessionObject.getRole().equalsIgnoreCase("system")) {
//					Predicate<Integer, UserEntry> p = new PredicateBuilderImpl().getEntryObject().get("role")
//							.in("admin", "user");
//					for (UserEntry loopEntry : GlobalVars.UserEntries.values(p)) {
//						if (loopEntry.getRole().equalsIgnoreCase("admin")) {
//							admins++;
//						} else if (loopEntry.getRole().equalsIgnoreCase("user")) {
//							users++;
//						}
//					}
//				} else {
//					if (userSessionObject.getRole().equalsIgnoreCase("admin")) {
//						// check smsc ownership
//						if (!GlobalVars.smscService.listNames(systemId).isEmpty()) {
//							userSessionObject.setSmscOwner(true);
//						} else {
//							logger.info(systemId + " Has No Ownership of smsc.");
//						}
//						EntryObject e = new PredicateBuilderImpl().getEntryObject();
//						Predicate<Integer, UserEntry> p = e.get("masterId").equal(systemId)
//								.and(e.get("role").in("admin", "user"));
//						for (UserEntry loopEntry : GlobalVars.UserEntries.values(p)) {
//							if (loopEntry.getRole().equalsIgnoreCase("admin")) {
//								admins++;
//							} else if (loopEntry.getRole().equalsIgnoreCase("user")) {
//								users++;
//							}
//						}
//					}
//				}
//				logger.info(systemId + " Under Admins:" + admins + " Users:" + users);
//				// counter++;
//				// }
//				userSessionObject.setAdminCount(admins);
//				userSessionObject.setUserCount(users);
//				// -----------------------------------------------------
//				if (userSessionObject.getRole().equalsIgnoreCase("superadmin")) {
//					userSessionObject.setMasterBalance(userSessionObject.getBalance());
//				} else {
//					int master_id = GlobalVars.UserMapping.get(entry.getMasterId());
//					userSessionObject.setMasterBalance(GlobalVars.BalanceEntries.get(master_id));
//				}
//				if (userSessionObject.getRole().equalsIgnoreCase("user")) {
//					ProfessionEntry masterProEntry = GlobalVars.ProfessionEntries
//							.get(userSessionObject.getMasterUserId());
//					if (masterProEntry.getReferenceId() != null && masterProEntry.getReferenceId().length() == 10) {
//						userSessionObject.getProfessionEntry().setReferenceId(masterProEntry.getReferenceId());
//					}
//				}
//				logger.info(systemId + " UserSessionObject created");
//				return userSessionObject;
//			} else {
//				logger.info(systemId + " UserEntryExt Not Found");
//				return null;
//			}
//		} catch (Exception e) {
//			logger.error(systemId, e);
//			return null;
//		}
//	}
//
//	@Override
//	public UserEntryExt getUserEntryExt(int userid) {
//		logger.debug("getUserEntry(" + userid + ")");
//		if (GlobalVars.UserEntries.containsKey(userid)) {
//			UserEntryExt entry = new UserEntryExt(GlobalVars.UserEntries.get(userid));
//			entry.setDlrSettingEntry(GlobalVars.DlrSettingEntries.get(userid));
//			WebMasterEntry webEntry = GlobalVars.WebmasterEntries.get(userid);
//			entry.setWebMasterEntry(webEntry);
//			entry.setProfessionEntry(GlobalVars.ProfessionEntries.get(userid));
//			logger.debug("end getUserEntry(" + userid + ")");
//			return entry;
//		} else {
//			return null;
//		}
//	}
//
//	@Override
//	public UserEntryExt getUserEntryExt(String systemid) {
//		logger.debug("getUserEntry(" + systemid + ")");
//		if (GlobalVars.UserMapping.containsKey(systemid)) {
//			int userid = GlobalVars.UserMapping.get(systemid);
//			return getUserEntryExt(userid);
//		} else {
//			return null;
//		}
//	}
//
//	@Override
//	public UserEntry getUserEntry(int userid) {
//		logger.debug("getUserEntry(" + userid + ")");
//		if (GlobalVars.UserEntries.containsKey(userid)) {
//			return GlobalVars.UserEntries.get(userid);
//		} else {
//			return null;
//		}
//	}
//
//	@Override
//	public UserEntry getUserEntry(String systemid) {
//		logger.debug("getUserEntry(" + systemid + ")");
//		if (GlobalVars.UserMapping.containsKey(systemid)) {
//			int userid = GlobalVars.UserMapping.get(systemid);
//			return getUserEntry(userid);
//		} else {
//			return null;
//		}
//	}

	// ------------- list names -----------------------
	@Override
	public Map<Integer, String> listUsers() {
		logger.debug("listUsers()");
		Map<Integer, String> map = new HashMap<Integer, String>();
		for (UserEntry entry : GlobalVars.UserEntries.values()) {
			map.put(entry.getId(), entry.getSystemId());
		}
		Map<Integer, String> sortedMap = map.entrySet().stream()
				.sorted(Entry.comparingByValue(Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return sortedMap;
	}

//	@Override
//	public Map<Integer, String> listUsers(Set<String> roles) {
//		logger.debug("listUsers(roles)");
//		Map<Integer, String> map = new HashMap<Integer, String>();
//		for (UserEntry entry : GlobalVars.UserEntries.values()) {
//			if (roles.contains(entry.getRole().toLowerCase())) {
//				map.put(entry.getId(), entry.getSystemId());
//			}
//		}
//		Map<Integer, String> sortedMap = map.entrySet().stream()
//				.sorted(Entry.comparingByValue(Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)))
//				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
//		return sortedMap;
//	}

//	@Override
//	public Map<Integer, String> listUsers(Integer[] useridarr) {
//		logger.debug("listUsers(Integer[])");
//		Map<Integer, String> map = new LinkedHashMap<Integer, String>();
//		for (Integer userId : useridarr) {
//			if (GlobalVars.UserEntries.containsKey(userId)) {
//				map.put(userId, GlobalVars.UserEntries.get(userId).getSystemId());
//			}
//		}
//		return map;
//	}

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

//	@Override
//	public Map<Integer, UserEntryExt> listUserEntryUnderSeller(int seller) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	// ----------- list Entries -----------------------
//	@Override
//	public Map<Integer, UserEntryExt> listUserEntries() {
//		logger.debug("listUserEntries():" + GlobalVars.UserEntries);
//		Map<Integer, UserEntryExt> map = new LinkedHashMap<Integer, UserEntryExt>();
//		for (int userId : GlobalVars.UserMapping.values()) {
//			UserEntryExt entry = getUserEntryExt(userId);
//			map.put(userId, entry);
//		}
//		return map;
//	}

	// @Override
//	public Map<Integer, UserEntryExt> listUserEntries(Set<String> roles) {
//		logger.debug("listUserEntries(" + roles + ")");
//		Map<Integer, UserEntryExt> map = new LinkedHashMap<Integer, UserEntryExt>();
//		for (UserEntry userEntry : GlobalVars.UserEntries.values()) {
//			if (roles.contains(userEntry.getRole().toLowerCase())) {
//				UserEntryExt entry = new UserEntryExt(userEntry);
//				entry.setDlrSettingEntry(GlobalVars.DlrSettingEntries.get(userEntry.getId()));
//				entry.setWebMasterEntry(GlobalVars.WebmasterEntries.get(userEntry.getId()));
//				entry.setProfessionEntry(GlobalVars.ProfessionEntries.get(userEntry.getId()));
//				map.put(userEntry.getId(), entry);
//			}
//		}
//		return map;
//	}

//	@Override
//	public Map<Integer, UserEntryExt> listUserEntryUnderMaster(String master) {
//		logger.debug("listUserEntryUnderMaster(" + master + ")");
//		Map<Integer, UserEntryExt> map = new LinkedHashMap<Integer, UserEntryExt>();
//		for (UserEntry userEntry : GlobalVars.UserEntries.values()) {
//			if (userEntry.getMasterId().equalsIgnoreCase(master)) {
//				UserEntryExt entry = new UserEntryExt(userEntry);
//				entry.setDlrSettingEntry(GlobalVars.DlrSettingEntries.get(userEntry.getId()));
//				entry.setWebMasterEntry(GlobalVars.WebmasterEntries.get(userEntry.getId()));
//				entry.setProfessionEntry(GlobalVars.ProfessionEntries.get(userEntry.getId()));
//				map.put(userEntry.getId(), entry);
//			}
//		}
//		return map;
//	}

//	@Override
//	public Map<Integer, UserEntryExt> listUserEntryUnderSeller(int seller) {
//		logger.info("listing UserEntries Under Seller(" + seller + ")");
//		Map<Integer, UserEntryExt> map = new HashMap<Integer, UserEntryExt>();
//		for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values()) {
//			if (webEntry.getExecutiveId() == seller) {
//				UserEntryExt entry = getUserEntryExt(webEntry.getUserId());
//				if (entry.getUserEntry().getRole().equalsIgnoreCase("admin")) {
//					Map<Integer, UserEntryExt> under_users = listUserEntryUnderMaster(
//							entry.getUserEntry().getSystemId());
//					map.putAll(under_users);
//				}
//				map.put(webEntry.getUserId(), entry);
//			}
//		}
//		logger.info("UserEntries Found Under Seller(" + seller + "): " + map.size());
//		return map;
//	}

	// -------------- Recharge Entries -------------------
//	@Override
//	public int saveRechargeEntry(RechargeEntry entry) {
//		logger.debug("saveRechargeEntry(" + entry + ")");
//		return userDAO.saveRechargeEntry(entry);
//	}
//
//	@Override
//	public Map<Integer, RechargeEntry> listRecentRecharges(Integer[] userid) {
//		logger.debug("listRecentRecharges(" + userid + ")");
//		Map<Integer, RechargeEntry> map = new HashMap<Integer, RechargeEntry>();
//		List<RechargeEntry> results = userDAO.listRecentRecharges(userid);
//		for (RechargeEntry entry : results) {
//			map.put(entry.getUserId(), entry);
//		}
//		return map;
//	}
//
//	@Override
//	public Map<Integer, List<RechargeEntry>> listTransactions(Integer[] userid, String txnType, String startTime,
//			String endTime) {
//		logger.debug("listTransactions(" + userid + ")");
//		// Map<Integer, UserEntryExt> usermap = listUserBalance();
//		Map<Integer, List<RechargeEntry>> map = new HashMap<Integer, List<RechargeEntry>>();
//		List<RechargeEntry> list = userDAO.listTransactions(userid, txnType, startTime, endTime);
//		for (RechargeEntry entry : list) {
//			String particular = entry.getParticular();
//			String[] particular_arr = particular.split("_");
//			entry.setParticular(particular_arr[0]);
//			String effectiveUser = particular_arr[1];
//			entry.setEffectiveUser(effectiveUser);
//			if (GlobalVars.UserEntries.containsKey(entry.getUserId())) {
//				UserEntry userEntry = GlobalVars.UserEntries.get(entry.getUserId());
//				BalanceEntry balance = GlobalVars.BalanceEntries.get(entry.getUserId());
//				logger.debug(userEntry.getSystemId() + "[" + userEntry.getRole() + "]: " + entry);
//				entry.setSystemId(userEntry.getSystemId());
//				if (userEntry.getRole().equalsIgnoreCase("superadmin")
//						|| userEntry.getRole().equalsIgnoreCase("system")) {
//					if (GlobalVars.UserMapping.containsKey(effectiveUser)) {
//						int effective_user_id = GlobalVars.UserMapping.get(effectiveUser);
//						if (GlobalVars.BalanceEntries.containsKey(effective_user_id)) {
//							BalanceEntry effectiveBalanceEntry = GlobalVars.BalanceEntries.get(effective_user_id);
//							entry.setWalletFlag(effectiveBalanceEntry.getWalletFlag());
//						} else {
//							entry.setWalletFlag(balance.getWalletFlag());
//						}
//					} else {
//						entry.setWalletFlag(balance.getWalletFlag());
//					}
//				} else {
//					entry.setWalletFlag(balance.getWalletFlag());
//				}
//			}
//			List<RechargeEntry> entrylist = null;
//			if (map.containsKey(entry.getUserId())) {
//				entrylist = map.get(entry.getUserId());
//			} else {
//				entrylist = new ArrayList<RechargeEntry>();
//			}
//			entrylist.add(entry);
//			map.put(entry.getUserId(), entrylist);
//		}
//		return map;
//	}
//
//	@Override
//	public Map<Integer, List<RechargeEntry>> listTransactions(Integer[] userid) {
//		return listTransactions(userid, null, null, null);
//	}
//
//	@Override
//	public void saveAccessLogEntry(AccessLogEntry entry) {
//		userDAO.saveAccessLogEntry(entry);
//	}
//
//	@Override
//	public List<AccessLogEntry> listAccessLog(int[] userid_arr) {
//		String[] system_id_arr = new String[userid_arr.length];
//		int i = 0;
//		for (int userId : userid_arr) {
//			if (GlobalVars.UserEntries.containsKey(userId)) {
//				system_id_arr[i] = GlobalVars.UserEntries.get(userId).getSystemId();
//				i++;
//			}
//		}
//		List<AccessLogEntry> list = userDAO.listAccessLog(system_id_arr);
//		return list;
//	}
//
//	@Override
//	public long countUsersUnderSeller(int sellerId) {
//		return userDAO.countUsersUnderSeller(sellerId);
//	}
//
//	@Override
//	public UserEntry getInternUserEntry() {
//		logger.debug("Checking For User Internal User ");
//		Predicate<Integer, UserEntry> p = new PredicateBuilderImpl().getEntryObject().get("role").equal("internal");
//		for (UserEntry entry : GlobalVars.UserEntries.values(p)) {
//			return entry;
//		}
//		return null;
//	}
//
//	@Override
//	public void saveOTPEntry(OTPEntry entry) {
//		userDAO.saveOTPEntry(entry);
//	}
//
//	@Override
//	public void updateOTPEntry(OTPEntry entry) {
//		userDAO.updateOTPEntry(entry);
//	}
//
//	@Override
//	public OTPEntry getOTPEntry(String systemId) {
//		return userDAO.getOTPEntry(systemId);
//	}
//
//	@Override
//	public Map<String, SessionEntry> listSessionLog() {
//		List<SessionEntry> list = userDAO.listSessionLog();
//		Map<String, SessionEntry> map = new HashMap<String, SessionEntry>();
//		for (SessionEntry entry : list) {
//			map.put(entry.getSystemId(), entry);
//		}
//		return map;
//	}
//
//	@Override
//	public Map<String, BindErrorEntry> listBindErrorLog() {
//		List<BindErrorEntry> list = userDAO.listBindErrorLog();
//		Map<String, BindErrorEntry> map = new HashMap<String, BindErrorEntry>();
//		for (BindErrorEntry entry : list) {
//			map.put(entry.getSystemId(), entry);
//		}
//		return map;
//	}
//
//	@Override
//	public void updateWebMasterEntry(WebMasterEntry entry) {
//		userDAO.updateWebMasterEntry(entry);
//	}
//
//	@Override
//	public void updateProfessionEntry(ProfessionEntry entry) {
//		userDAO.updateProfessionEntry(entry);
//	}
//
//	@Override
//	public WebMenuAccessEntry getWebMenuAccessEntry(int userId) {
//		return userDAO.getWebMenuAccessEntry(userId);
//	}
//
//	@Override
//	public void saveWebMenuAccessEntry(WebMenuAccessEntry entry) {
//		userDAO.saveWebMenuAccessEntry(entry);
//	}
//
//	@Override
//	public void updateWebMenuAccessEntry(WebMenuAccessEntry entry) {
//		userDAO.updateWebMenuAccessEntry(entry);
//	}
//
//	@Override
//	public ProfessionEntry getProfessionEntry(String systemId) {
//		ProfessionEntry entry = null;
//		if (GlobalVars.UserMapping.containsKey(systemId)) {
//			int userid = GlobalVars.UserMapping.get(systemId);
//			entry = GlobalVars.ProfessionEntries.get(userid);
//		}
//		return entry;
//	}
//
//	@Override
//	public WebMasterEntry getWebmasterEntry(String systemId) {
//		WebMasterEntry entry = null;
//		if (GlobalVars.UserMapping.containsKey(systemId)) {
//			int userid = GlobalVars.UserMapping.get(systemId);
//			entry = GlobalVars.WebmasterEntries.get(userid);
//		}
//		return entry;
//	}
//
//	@Override
//	public void saveLimit(UserLimitEntry entry) throws DuplicateEntryException {
//		userDAO.saveLimit(entry);
//	}
//
//	@Override
//	public void updateLimit(UserLimitEntry entry) {
//		userDAO.updateLimit(entry);
//	}
//
//	@Override
//	public void deleteLimit(UserLimitEntry entry) {
//		userDAO.deleteLimit(entry);
//	}
//
//	@Override
//	public List<UserLimitEntry> listLimit() {
//		List<UserLimitEntry> list = userDAO.listLimit();
//		for (UserLimitEntry entry : list) {
//			if (GlobalVars.UserEntries.containsKey(entry.getUserId())) {
//				entry.setSystemId(GlobalVars.UserEntries.get(entry.getUserId()).getSystemId());
//			}
//			if (GlobalVars.SmscEntries.containsKey(entry.getRerouteSmscId())) {
//				if (entry.getRerouteSmscId() > 0) {
//					entry.setReroute(GlobalVars.SmscEntries.get(entry.getRerouteSmscId()).getName());
//				} else {
//					entry.setReroute("BLOCK");
//				}
//			} else {
//				entry.setReroute("BLOCK");
//			}
//		}
//		return list;
//	}
//
//	@Override
//	public UserLimitEntry getLimitEntry(int userId) {
//		UserLimitEntry entry = userDAO.getLimitEntry(userId);
//		if (GlobalVars.UserEntries.containsKey(entry.getUserId())) {
//			entry.setSystemId(GlobalVars.UserEntries.get(entry.getUserId()).getSystemId());
//		}
//		if (GlobalVars.SmscEntries.containsKey(entry.getRerouteSmscId())) {
//			if (entry.getRerouteSmscId() > 0) {
//				entry.setReroute(GlobalVars.SmscEntries.get(entry.getRerouteSmscId()).getName());
//			} else {
//				entry.setReroute("BLOCK");
//			}
//		} else {
//			entry.setReroute("BLOCK");
//		}
//		return entry;
//	}
}
