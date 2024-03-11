package com.hti.dao.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.dao.UserDAService;
import com.hti.exception.EntryNotFoundException;
import com.hti.smsc.SendErrorResponseSMS;
import com.hti.thread.ClearNonResponding;
import com.hti.user.UserBalance;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;
import com.hti.user.dto.UserEntry;

public class UserDAServiceImpl implements UserDAService {
	private Logger logger = LoggerFactory.getLogger(UserDAServiceImpl.class);
	private Map<Integer, UserBalance> UserBalanceObject = new HashMap<Integer, UserBalance>();

	public UserDAServiceImpl() {
		GlobalCache.SystemIdMapping = GlobalVars.hazelInstance.getMap("user_mapping");
		GlobalCache.BalanceEntries = GlobalVars.hazelInstance.getMap("balance_entries");
		GlobalCache.UserEntries = GlobalVars.hazelInstance.getMap("user_entries");
	}
	
	@Override
	public UserEntry getInternalUser() {
		Predicate<Integer, UserEntry> predicate = new PredicateBuilderImpl().getEntryObject().get("role")
				.equal("internal");
		for (UserEntry entry : GlobalCache.UserEntries.values(predicate)) {
			return entry;
		}
		return null;
	}

	@Override
	public void reloadStatus(boolean init) {
		if (init) {
			logger.info("********** Loading User Account Details *******");
			com.hazelcast.query.Predicate<Integer, UserEntry> p = new PredicateBuilderImpl().getEntryObject()
					.is("skipLoopingRule");
			for (com.hti.user.dto.UserEntry entry : GlobalCache.UserEntries.values(p)) {
				GlobalCache.SkipLoopingUsers.add(entry.getSystemId());
			}
			p = new PredicateBuilderImpl().getEntryObject().get("loopSmscId").notEqual(0);
			for (com.hti.user.dto.UserEntry entry : GlobalCache.UserEntries.values(p)) {
				com.hti.smsc.dto.SmscEntry smscEntry = GlobalVars.smscService.getEntry(entry.getLoopSmscId());
				if (smscEntry != null) {
					GlobalCache.LoopingRoutes.put(entry.getSystemId(), smscEntry.getName());
					logger.info(entry.getSystemId() + " Added Looping Route: " + smscEntry.getName());
				} else {
					logger.info(entry.getSystemId() + " Looping Route Not Found: " + entry.getLoopSmscId());
				}
			}
			logger.info("SkipLoopingUsers: " + GlobalCache.SkipLoopingUsers);
			logger.info("LoopingRoutes: " + GlobalCache.LoopingRoutes);
		} else {
			Map<String, String> UserFlagStatus = GlobalVars.hazelInstance.getMap("user_flag_status");
			for (Entry<String, String> map_entry : UserFlagStatus.entrySet()) {
				String username = map_entry.getKey();
				String flag = UserFlagStatus.get(username);
				if (flag.equalsIgnoreCase("505")) {
					logger.info("**** Got Command To Reload Account Details For " + username + "*****");
					int user_id = GlobalCache.SystemIdMapping.get(username);
					if (user_id > 0) {
						com.hti.user.dto.UserEntry entry = GlobalCache.UserEntries.get(user_id);
						if (entry.isSkipLoopingRule()) {
							if (!GlobalCache.SkipLoopingUsers.contains(username)) {
								GlobalCache.SkipLoopingUsers.add(username);
							}
						} else {
							if (GlobalCache.SkipLoopingUsers.contains(username)) {
								GlobalCache.SkipLoopingUsers.remove(username);
							}
						}
						if (entry.getLoopSmscId() > 0) {
							com.hti.smsc.dto.SmscEntry smscEntry = GlobalVars.smscService
									.getEntry(entry.getLoopSmscId());
							if (smscEntry != null) {
								GlobalCache.LoopingRoutes.put(username, smscEntry.getName());
								logger.info(username + " Added Looping Route: " + smscEntry.getName());
							} else {
								GlobalCache.LoopingRoutes.remove(username);
								logger.info(username + " Looping Route Not Found: " + entry.getLoopSmscId());
							}
						} else {
							if (GlobalCache.LoopingRoutes.containsKey(username)) {
								GlobalCache.LoopingRoutes.remove(username);
								logger.info(username + " Removed Looping Route.");
							}
						}
					} else {
						logger.error(username + " UserEntry Not Found.");
					}
					logger.info("SkipLoopingUsers: " + GlobalCache.SkipLoopingUsers);
					logger.info("LoopingRoutes: " + GlobalCache.LoopingRoutes);
				} else if (flag.equalsIgnoreCase("909")) {
					logger.info("**** Got Command To Clear Non Responding For " + username + "*****");
					new Thread(new ClearNonResponding(username), "ClearNonResponding_" + username).start();
				} else {
					if (GlobalCache.EsmeErrorFlag.containsKey(flag)) {
						String flag_symbol = (String) GlobalCache.EsmeErrorFlag.get(flag);
						logger.info(
								"<-- " + username + " Command to Resend Error Responding <" + flag_symbol + "> --> ");
						String error_sql = "select * from smsc_in where username ='" + username + "' and s_flag like '"
								+ flag_symbol + "'";
						new Thread(new SendErrorResponseSMS(error_sql, null)).start();
					}
				}
			}
		}
	}

	@Override
	public synchronized UserBalance getUserBalance(int user_id) throws EntryNotFoundException {
		if (UserBalanceObject.containsKey(user_id)) {
			return UserBalanceObject.get(user_id);
		} else {
			if (GlobalCache.BalanceEntries.containsKey(user_id)) {
				UserBalance userBalance = new UserBalance(user_id);
				UserBalanceObject.put(user_id, userBalance);
				return userBalance;
			} else {
				logger.error(user_id + " <Balance Entry Not Found >");
				return null;
			}
		}
	}

	@Override
	public synchronized UserBalance getUserBalance(String system_id) throws EntryNotFoundException {
		Map<String, Integer> map = GlobalVars.hazelInstance.getMap("user_mapping");
		if (map.containsKey(system_id)) {
			return getUserBalance(map.get(system_id));
		} else {
			logger.error(system_id + " < Invalid System_id >");
		}
		return null;
	}
}
