package com.hti.init;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.topic.ITopic;
import com.hti.listener.FlagEventListener;
import com.hti.listener.TableEventListener;
import com.hti.service.CacheService;
import com.hti.util.Constants;
import com.hti.util.FileUtil;
import com.hti.util.FlagStatus;
import com.hti.util.GlobalVar;

public class FlagReader implements Runnable {
	private boolean stop;
	private Logger logger = LoggerFactory.getLogger(FlagReader.class);
	private String NETWORK_FLAG = null;
	private String SMSC_FLAG = null;
	private String SMSC_SH_FLAG = null;
	private String SMSC_LT_FLAG = null;
	private String SMSC_LOOP_FLAG = null;
	private String DGM_FLAG = null;
	private String CLIENT_FLAG = null;
	private String APPLICATION_FLAG = null;
	private String BSFM_FLAG = null;
	private String USER_BSFM_FLAG = null;
	private String NETWORK_BSFM_FLAG = null;
	private String DLT_FLAG = null;
	private String SMSC_SPCL_SETTING_FLAG = null;
	private String SMSC_BSFM_FLAG = null;
	private String ESME_ERROR_FLAG = null;
	private String SIGNAL_ERROR_FLAG = null;
	private String SPCL_ENCODING_FLAG = null;
	private String RESEND_DLR_FLAG = null;
	private String PERFORM_ALERT_FLAG = null;
	private String TW_KEYWORD_FLAG = null;
    private String TW_FILTER_FLAG = null;
	private String USER_LT_FLAG = null;
	private String BLOCK_NUMBER_FLAG = null;
	private String UNICODE_REPL_FLAG = null;
	private static ITopic<Map<String, String>> flag_topic;
	private Map<String, String> cache_refresh = new HashMap<String, String>();

	public FlagReader() {
		logger.info("FlagReader Thread Starting");
		// -------- Add listener to check activity of Flags -----------
		flag_topic = GlobalVar.hazelInstance.getTopic("flag_status");
		flag_topic.addMessageListener(new FlagEventListener());
		ITopic<Map<String, Boolean>> table_topic = GlobalVar.hazelInstance.getTopic("table_locked");
		table_topic.addMessageListener(new TableEventListener());
		FileUtil.setDefaultFlag(Constants.APPLICATION_FLAG_FILE);
		FileUtil.setDefaultFlag(Constants.NETWORK_FLAG_FILE);
		FileUtil.setDefaultFlag(Constants.SMSC_FLAG_FILE);
		FileUtil.setDefaultFlag(Constants.SMSC_SH_FLAG_FILE);
		FileUtil.setDefaultFlag(Constants.SMSC_LT_FLAG_FILE);
		FileUtil.setDefaultFlag(Constants.CLIENT_FLAG_FILE);
		FileUtil.setDefaultFlag(Constants.DGM_FLAG_FILE);
		FileUtil.setDefaultFlag(Constants.ALERT_FLAG_FILE);
		FileUtil.setDefaultFlag(Constants.SMSC_LOOP_FLAG_FILE);
		FileUtil.setDefaultFlag(Constants.DLT_FLAG_FILE);
		FileUtil.setDefaultFlag(Constants.SMSC_BSFM_FLAG_FILE);
	}

	public void initFlagStatus() {
		logger.info("Checking For Smsc Flags");
		for (String smsc : GlobalVar.smsc_flag_status.keySet()) {
			Properties props = GlobalVar.smsc_flag_status.get(smsc);
			String flag = props.getProperty("FLAG");
			if (flag.equalsIgnoreCase(FlagStatus.BLOCKED)) {
				FileUtil.setSmscBlocked(Constants.SMSC_FLAG_DIR + smsc + ".txt");
				logger.info(smsc + " Smsc Blocked.");
			} else {
				FileUtil.setSmscDefault(Constants.SMSC_FLAG_DIR + smsc + ".txt");
			}
		}
		logger.info("Checking For User Flags");
		for (Map.Entry<String, String> flag_entry : GlobalVar.user_flag_status.entrySet()) {
			if (flag_entry.getValue().equalsIgnoreCase(FlagStatus.BLOCKED)) {
				FileUtil.setBlocked(Constants.USER_FLAG_DIR + flag_entry.getKey() + ".txt");
				logger.info(flag_entry.getKey() + " User Blocked.");
			} else {
				FileUtil.setDefaultFlag(Constants.USER_FLAG_DIR + flag_entry.getKey() + ".txt");
			}
		}
		logger.info("End Checking For Flags");
	}

	@Override
	public void run() {
		while (!stop) {
			System.out.println("Flag Reader is Running");
			try {
				APPLICATION_FLAG = FileUtil.readFlag(Constants.APPLICATION_FLAG_FILE, true);
				if ((APPLICATION_FLAG != null) && (APPLICATION_FLAG.contains("404"))) {
					logger.info("Command To Stop Application");
					GlobalVar.APPLICATION_STOP = true;
				}
				NETWORK_FLAG = FileUtil.readFlag(Constants.NETWORK_FLAG_FILE, true);
				if ((NETWORK_FLAG != null && NETWORK_FLAG.contains("707"))) {
					FileUtil.setDefaultFlag(Constants.NETWORK_FLAG_FILE);
					CacheService.loadNetworkEntries();
					// Map<String, String> cache_refresh = new HashMap<String, String>();
					cache_refresh.put("NETWORK_FLAG", "707");
					// flag_topic.publish(cache_refresh);
					logger.info("********* Network Entries Notification Added ****");
				}
				DLT_FLAG = FileUtil.readFlag(Constants.DLT_FLAG_FILE, true);
				if ((DLT_FLAG != null && DLT_FLAG.contains("707"))) {
					FileUtil.setDefaultFlag(Constants.DLT_FLAG_FILE);
					// Map<String, String> cache_refresh = new HashMap<String, String>();
					cache_refresh.put("DLT_FLAG", "707");
					// flag_topic.publish(cache_refresh);
					logger.info("********* DLT Entries Notification Added ****");
				}
				BSFM_FLAG = FileUtil.readFlag(Constants.BSFM_FLAG_FILE, true);
				if ((BSFM_FLAG != null && BSFM_FLAG.contains("707"))) {
					FileUtil.setDefaultFlag(Constants.BSFM_FLAG_FILE);
					// Map<String, String> cache_refresh = new HashMap<String, String>();
					cache_refresh.put("BSFM_FLAG", "707");
					// flag_topic.publish(cache_refresh);
					logger.info("********* BSFM Entries Notification Added ****");
				}
				USER_BSFM_FLAG = FileUtil.readFlag(Constants.USER_BSFM_FLAG_FILE, true);
				if ((USER_BSFM_FLAG != null && USER_BSFM_FLAG.contains("707"))) {
					FileUtil.setDefaultFlag(Constants.USER_BSFM_FLAG_FILE);
					// Map<String, String> cache_refresh = new HashMap<String, String>();
					cache_refresh.put("USER_BSFM_FLAG", "707");
					// flag_topic.publish(cache_refresh);
					logger.info("********* User Based BSFM Entries Notification Added ****");
				}
				NETWORK_BSFM_FLAG = FileUtil.readFlag(Constants.NETWORK_BSFM_FLAG_FILE, true);
				if ((NETWORK_BSFM_FLAG != null && NETWORK_BSFM_FLAG.contains("707"))) {
					FileUtil.setDefaultFlag(Constants.NETWORK_BSFM_FLAG_FILE);
					// Map<String, String> cache_refresh = new HashMap<String, String>();
					cache_refresh.put("NETWORK_BSFM_FLAG", "707");
					// flag_topic.publish(cache_refresh);
					logger.info("********* Network Based BSFM Entries Notification Added ****");
				}
				SMSC_BSFM_FLAG = FileUtil.readFlag(Constants.SMSC_BSFM_FLAG_FILE, true);
				if ((SMSC_BSFM_FLAG != null && SMSC_BSFM_FLAG.contains("707"))) {
					FileUtil.setDefaultFlag(Constants.SMSC_BSFM_FLAG_FILE);
					// Map<String, String> cache_refresh = new HashMap<String, String>();
					cache_refresh.put("SMSC_BSFM", "707");
					// flag_topic.publish(cache_refresh);
					logger.info("********* SMSC BSFM Entries Notification Added ****");
				}
				SMSC_SH_FLAG = FileUtil.readFlag(Constants.SMSC_SH_FLAG_FILE, true);
				if ((SMSC_SH_FLAG != null && SMSC_SH_FLAG.contains("707"))) {
					FileUtil.setDefaultFlag(Constants.SMSC_SH_FLAG_FILE);
					CacheService.loadSmscSchedule();
					BearerBox.RELOAD = true;
					cache_refresh.put("SMSC_SH_FLAG", "707");
					logger.info("********* SMSC Schedule Notification Added ****");
				}
				SMSC_LT_FLAG = FileUtil.readFlag(Constants.SMSC_LT_FLAG_FILE, true);
				if ((SMSC_LT_FLAG != null && SMSC_LT_FLAG.contains("707"))) {
					FileUtil.setDefaultFlag(Constants.SMSC_LT_FLAG_FILE);
					// Map<String, String> cache_refresh = new HashMap<String, String>();
					cache_refresh.put("SMSC_LT_FLAG", "707");
					// flag_topic.publish(cache_refresh);
					logger.info("********* SMSC Limit Notification Added ****");
				}
				SMSC_LOOP_FLAG = FileUtil.readFlag(Constants.SMSC_LOOP_FLAG_FILE, true);
				if ((SMSC_LOOP_FLAG != null && SMSC_LOOP_FLAG.contains("707"))) {
					FileUtil.setDefaultFlag(Constants.SMSC_LOOP_FLAG_FILE);
					// Map<String, String> cache_refresh = new HashMap<String, String>();
					cache_refresh.put("SMSC_LOOP_FLAG", "707");
					// flag_topic.publish(cache_refresh);
					logger.info("********* SMSC Looping Notification Added ****");
				}
				PERFORM_ALERT_FLAG = FileUtil.readFlag(Constants.ALERT_FLAG_FILE, true);
				if (PERFORM_ALERT_FLAG != null) {
					if (!PERFORM_ALERT_FLAG.equalsIgnoreCase(FlagStatus.DEFAULT)) {
						FileUtil.setDefaultFlag(Constants.ALERT_FLAG_FILE);
						cache_refresh.put("PERFORM_ALERT_FLAG", PERFORM_ALERT_FLAG);
						logger.info("********* Perform Alert Notification Added ****");
					}
				}
				USER_LT_FLAG = FileUtil.readFlag(Constants.USER_LT_FLAG_FILE, true);
				if ((USER_LT_FLAG != null && USER_LT_FLAG.contains("707"))) {
					FileUtil.setDefaultFlag(Constants.USER_LT_FLAG_FILE);
					// Map<String, String> cache_refresh = new HashMap<String, String>();
					cache_refresh.put("USER_LT_FLAG", "707");
					// flag_topic.publish(cache_refresh);
					logger.info("********* User Limit Notification Added ****");
				}
				TW_KEYWORD_FLAG = FileUtil.readFlag(Constants.TW_KEYWORD_FLAG_FILE, true);
				if (TW_KEYWORD_FLAG != null) {
					if (!TW_KEYWORD_FLAG.equalsIgnoreCase(FlagStatus.DEFAULT)) {
						FileUtil.setDefaultFlag(Constants.TW_KEYWORD_FLAG_FILE);
						cache_refresh.put("TW_KEYWORD_FLAG", TW_KEYWORD_FLAG);
						logger.info("********* Twoway Keyword Notification Added ****");
					}
				}
				TW_FILTER_FLAG = FileUtil.readFlag(Constants.TW_FILTER_FLAG_FILE, true);
				if (TW_FILTER_FLAG != null) {
					if (!TW_FILTER_FLAG.equalsIgnoreCase(FlagStatus.DEFAULT)) {
						FileUtil.setDefaultFlag(Constants.TW_FILTER_FLAG_FILE);
						cache_refresh.put("TW_FILTER_FLAG", TW_FILTER_FLAG);
						logger.info("********* Twoway Filter Notification Added ****");
					}
				}
				BLOCK_NUMBER_FLAG = FileUtil.readFlag(Constants.BLOCK_NUMBER_FLAG_FILE, true);
				if (BLOCK_NUMBER_FLAG != null) {
					if (!BLOCK_NUMBER_FLAG.equalsIgnoreCase(FlagStatus.DEFAULT)) {
						FileUtil.setDefaultFlag(Constants.BLOCK_NUMBER_FLAG_FILE);
						cache_refresh.put("BLOCK_NUMBER_FLAG", BLOCK_NUMBER_FLAG);
						logger.info("********* Block Number Notification Added ****");
					}
				}
				UNICODE_REPL_FLAG = FileUtil.readFlag(Constants.UNICODE_REPL_FLAG_FILE, true);
				if (UNICODE_REPL_FLAG != null) {
					if (!UNICODE_REPL_FLAG.equalsIgnoreCase(FlagStatus.DEFAULT)) {
						FileUtil.setDefaultFlag(Constants.UNICODE_REPL_FLAG_FILE);
						cache_refresh.put("UNICODE_REPL_FLAG", UNICODE_REPL_FLAG);
						logger.info("********* Unicode Replacement Notification Added ****");
					}
				}
				SMSC_FLAG = FileUtil.readFlag(Constants.SMSC_FLAG_FILE, true);
				if ((SMSC_FLAG != null && SMSC_FLAG.contains("707"))) {
					logger.info("********* Smsc Configuration Refreshed ****");
					FileUtil.setDefaultFlag(Constants.SMSC_FLAG_FILE);
					Map<String, Integer> before_loading = new HashMap<String, Integer>(GlobalVar.smsc_name_mapping);
					Map<String, Integer> after_loading = CacheService.listSmscNames();
					Properties props = null;
					for (String smsc : after_loading.keySet()) {
						props = FileUtil.readSmscFlag(Constants.SMSC_FLAG_DIR + smsc + ".txt", true);
						String flag = props.getProperty("FLAG");
						if (!before_loading.containsKey(smsc)) {
							logger.info("New Smsc Entry Found: " + smsc);
							CacheService.loadSmscEntry(after_loading.get(smsc));
							props.put("FLAG", FlagStatus.ACCOUNT_ADDED);
						} else {
							before_loading.remove(smsc);
							if (flag.equalsIgnoreCase(FlagStatus.BLOCKED)) {
								// block smsc connection
							} else {
								if (flag.equalsIgnoreCase(FlagStatus.ACCOUNT_REFRESH)) { // Smsc Entry Refreshed
									logger.info(smsc + " Account Refreshed");
									CacheService.loadSmscEntry(after_loading.get(smsc));
									props.put("FLAG", FlagStatus.ACCOUNT_REFRESH);
								}
								FileUtil.setSmscDefault(Constants.SMSC_FLAG_DIR + smsc + ".txt");
							}
						}
						GlobalVar.smsc_flag_status.put(smsc, props);
					}
					if (!before_loading.isEmpty()) {
						for (String smsc : before_loading.keySet()) {
							props = new Properties();
							props.put("FLAG", FlagStatus.ACCOUNT_REMOVED);
							logger.info("Smsc Entry Removed: " + smsc);
							GlobalVar.smsc_flag_status.put(smsc, props);
							GlobalVar.smsc_entries.remove(before_loading.get(smsc));
						}
					}
					GlobalVar.smsc_name_mapping.clear();
					GlobalVar.smsc_name_mapping.putAll(after_loading);
					// Map<String, String> flag_status_map = new HashMap<String, String>();
					cache_refresh.put("SMSC_FLAG", "707");
					// flag_topic.publish(cache_refresh);
					logger.info("********* Smsc Entries Notification Added ****");
				}
				CLIENT_FLAG = FileUtil.readFlag(Constants.CLIENT_FLAG_FILE, true);
				if ((CLIENT_FLAG != null && CLIENT_FLAG.contains("707"))) {
					logger.info("********* User Configuration Refreshed ****");
					FileUtil.setDefaultFlag(Constants.CLIENT_FLAG_FILE);
					GlobalVar.user_flag_status.clear();
					Map<String, Integer> before_loading = new HashMap<String, Integer>(GlobalVar.user_mapping);
					Map<String, Integer> after_loading = CacheService.listUsernames();
					Set<Integer> routing_refresh_users = new java.util.HashSet<Integer>();
					String flag = null;
					logger.debug("Before :" + before_loading);
					for (String system_id : after_loading.keySet()) {
						flag = FileUtil.readFlag(Constants.USER_FLAG_DIR + system_id + ".txt", true);
						int user_id = after_loading.get(system_id);
						if (!before_loading.containsKey(system_id)) {
							logger.info("New User Entry Found: " + system_id);
							CacheService.loadUserConfig(user_id);
							flag = FlagStatus.ACCOUNT_ADDED;
						} else {
							if (flag.equalsIgnoreCase(FlagStatus.BLOCKED)) {
								logger.info(system_id + " Account Blocked");
								// CacheService.blockUserConfig(user_id);
							} else {
								if (flag.equalsIgnoreCase(FlagStatus.ACCOUNT_REFRESH)) { // User Entry Refreshed
									logger.info(system_id + " Account Refreshed");
									CacheService.loadUserEntry(user_id);
								} else if (flag.equalsIgnoreCase(FlagStatus.REFRESH)) { // Route Entry Refreshed or removed from block state
									logger.info(system_id + " Routing Refreshed");
									// CacheService.loadUserConfig(user_id);
									routing_refresh_users.add(user_id);
								} else if (flag.equalsIgnoreCase(FlagStatus.BALANCE_REFRESH)) { // Balance Refreshed
									logger.info(system_id + " Balance Refreshed");
								}
								FileUtil.setDefaultFlag(Constants.USER_FLAG_DIR + system_id + ".txt");
							}
							before_loading.remove(system_id);
						}
						GlobalVar.user_flag_status.put(system_id, flag);
					}
					logger.debug("End: " + before_loading);
					if (!before_loading.isEmpty()) {
						for (String system_id : before_loading.keySet()) {
							logger.info("User Entry Removed: " + system_id);
							CacheService.removeUserConfig(system_id);
							GlobalVar.user_flag_status.put(system_id, FlagStatus.ACCOUNT_REMOVED);
						}
					}
					if (!routing_refresh_users.isEmpty()) {
						CacheService.loadUserConfig(routing_refresh_users);
					}
					// ----------publish topic to notify flag change status -----------------
					// Map<String, String> cache_refresh = new HashMap<String, String>();
					cache_refresh.put("CLIENT_FLAG", "707");
					// flag_topic.publish(cache_refresh);
					logger.info("********* User Configuration Notification Added ****");
				}
				DGM_FLAG = FileUtil.readFlag(Constants.DGM_FLAG_FILE, true);
				if ((DGM_FLAG != null && DGM_FLAG.contains("707"))) {
					// Map<String, String> flag_status_map = new HashMap<String, String>();
					FileUtil.setDefaultFlag(Constants.DGM_FLAG_FILE);
					CacheService.loadSmscGroup();
					CacheService.loadSmscGroupMember();
					cache_refresh.put("DGM_FLAG", "707");
					// flag_topic.publish(cache_refresh);
					logger.info("********* Smsc Grouping Notification Added ****");
				}
				SMSC_SPCL_SETTING_FLAG = FileUtil.readFlag(Constants.SMSC_SPCL_SETTING_FLAG_FILE, true);
				if (SMSC_SPCL_SETTING_FLAG != null && SMSC_SPCL_SETTING_FLAG.equalsIgnoreCase(FlagStatus.REFRESH)) {
					FileUtil.setDefaultFlag(Constants.SMSC_SPCL_SETTING_FLAG_FILE);
					cache_refresh.put("SMSC_SPCL_SETTING", "707");
				}
				ESME_ERROR_FLAG = FileUtil.readFlag(Constants.ESME_ERROR_FLAG_FILE, true);
				if (ESME_ERROR_FLAG != null && ESME_ERROR_FLAG.equalsIgnoreCase(FlagStatus.REFRESH)) {
					FileUtil.setDefaultFlag(Constants.ESME_ERROR_FLAG_FILE);
					cache_refresh.put("ESME_ERROR_FLAG", "707");
				}
				SIGNAL_ERROR_FLAG = FileUtil.readFlag(Constants.SIGNAL_ERROR_FLAG_FILE, true);
				if (SIGNAL_ERROR_FLAG != null && SIGNAL_ERROR_FLAG.equalsIgnoreCase(FlagStatus.REFRESH)) {
					FileUtil.setDefaultFlag(Constants.SIGNAL_ERROR_FLAG_FILE);
					cache_refresh.put("SIGNAL_ERROR_FLAG", "707");
				}
				SPCL_ENCODING_FLAG = FileUtil.readFlag(Constants.SPCL_ENCODING_FLAG_FILE, true);
				if (SPCL_ENCODING_FLAG != null && SPCL_ENCODING_FLAG.equalsIgnoreCase(FlagStatus.REFRESH)) {
					FileUtil.setDefaultFlag(Constants.SPCL_ENCODING_FLAG_FILE);
					cache_refresh.put("SPCL_ENCODING_FLAG", "707");
				}
				RESEND_DLR_FLAG = FileUtil.readFlag(Constants.RESEND_DLR_FLAG_FILE, true);
				if (RESEND_DLR_FLAG != null) {
					if (RESEND_DLR_FLAG.equalsIgnoreCase(FlagStatus.REFRESH)) {
						FileUtil.setDefaultFlag(Constants.RESEND_DLR_FLAG_FILE);
						cache_refresh.put("RESEND_DLR_FLAG", "707");
					} else if (RESEND_DLR_FLAG.equalsIgnoreCase(FlagStatus.RESEND)) {
						FileUtil.setDefaultFlag(Constants.RESEND_DLR_FLAG_FILE);
						cache_refresh.put("RESEND_DLR_FLAG", "101");
					}
				}
			} catch (Exception e) {
				logger.error("FLAG READER", e.fillInStackTrace());
			}
			if (!cache_refresh.isEmpty()) {
				flag_topic.publish(cache_refresh);
				logger.info("********* Notification Published ****");
				cache_refresh.clear();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		logger.info("FlagReader Thread Stopped");
	}

	public void stop() {
		logger.info("FlagReader Thread Stopping");
		stop = true;
	}
}
