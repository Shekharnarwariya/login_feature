/*
 * BearerBox.java
 * Thread Responsible for Making SMSC Connection, making new thread
 * smsc connection for each SMSC, assign new queue for each connection
 * manipulate < TrackHtiClientBackLogQueue SmscQueueSelection DatabaseSMSCOutThread
 * ThreadQueueChange ,EnquireLink Thread>
 * Created on 06 April 2004, 12:45
 */
package com.hti.thread;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.bsfm.SpamFilter;
import com.hti.dlt.DltFilter;
import com.hti.user.SessionManager;
import com.hti.user.WebDeliverProcess;
import com.hti.util.Constants;
import com.hti.util.DistributionGroupManager;
import com.hti.util.FileUtil;
import com.hti.util.FlagStatus;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;

public class BearerBox implements Runnable {
	public static boolean BULKFILTER = false;
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private ProcessPDU processPDU;
	private DeliverProcess deliverProcess;
	// private DLRLoader dlrLoader;
	private String process_time;
	private boolean stop;

	public BearerBox() {
		logger.info("BearerBox Starting");
		process_time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		GlobalVars.networkService.loadNetworkConfig();
		GlobalVars.smscService.loadSmscConfiguration();
		GlobalVars.userService.initializeStatus();
		GlobalVars.userService.initializeUserBsfm();
		GlobalVars.networkService.initializeNetworkBsfm();
		GlobalCache.HttpDlrParam = GlobalVars.hazelInstance.getMap("http_dlr_param");
		GlobalCache.UserSubmitCounter = GlobalVars.hazelInstance.getMap("user_submit_count");
		deliverProcess = new DeliverProcess();
		new Thread(deliverProcess, "DeliverProcess").start();
		// dlrLoader = new DLRLoader();
		new Thread(new DLRLoader(FlagStatus.DEFAULT), "DLRLoader").start();
		checkMemoryUsage(1);
		processPDU = new ProcessPDU();
		new Thread(processPDU, "ProcessPDU").start();
		checkMemoryUsage(2);
		initializeDGM();
		initializeBSFM();
		initializeSubmitLimitEntries();
		initializeOptOutEntries();
		com.hti.dlt.DltFilter.loadRules();
		com.hti.bsfm.LinkFilter.loadLinks();
		loadUnicodeEncoding();
		System.out.println();
		checkMemoryUsage(3);
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				if (GlobalVars.APPLICATION_STATUS) {
					// String current_date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
					if (!process_time.equals(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))) {
						logger.info("******** Date Changed ************ ");
						process_time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
						if (GlobalVars.MASTER_CLIENT) {
							new Thread(new DLRLoader(FlagStatus.REFRESH), "DLRLoader").start();
							new Thread(new CleanUpThread(true, true), "CleanUpThread").start();
						}
					}
					// ----------- Network Flag Status ----------------
					try {
						if (GlobalVars.RELOAD_NETWORK_CONFIG) {
							logger.info("******** NETWORK FLAG REFRESHED ************ ");
							GlobalVars.RELOAD_NETWORK_CONFIG = false;
							GlobalVars.networkService.loadNetworkConfig();
							DeliverProcess.RELOAD_NNC = true;
							try {
								GlobalCache.UserSessionObject.forEach((k, v) -> {
									((SessionManager) v).refreshNetwork();
								});
							} catch (Exception e) {
								logger.error(e + " While Reload Network");
							}
						}
					} catch (Exception ex) {
						logger.error("reload_network", ex.fillInStackTrace());
					}
					// --------------- check smsc flag status ------------
					if (GlobalVars.RELOAD_SMSC_CONFIG) {
						logger.info("******** SMSC FLAG REFRESHED ************ ");
						GlobalVars.RELOAD_SMSC_CONFIG = false;
						GlobalVars.smscService.loadSmscFlagStatus();
					}
					if (GlobalVars.RELOAD_USER_CONFIG) {
						logger.info("******** CLIENT FLAG REFRESHED ************ ");
						GlobalVars.RELOAD_USER_CONFIG = false;
						GlobalVars.userService.reloadUserFlagStatus();
					}
					// --------------- check dgm flag status ------------
					if (GlobalVars.RELOAD_DGM_CONFIG) {
						logger.info("******** DGM REFRESHED ************ ");
						GlobalVars.RELOAD_DGM_CONFIG = false;
						initializeDGM();
					}
					// --------------- check bsfm flag status ------------
					if (GlobalVars.RELOAD_BSFM_CONFIG) {
						logger.info("******** BSFM REFRESHED ************ ");
						GlobalVars.RELOAD_BSFM_CONFIG = false;
						initializeBSFM();
					}
					if (GlobalVars.RELOAD_USER_BSFM_CONFIG) {
						logger.info("******** USER BASED BSFM REFRESHED ************ ");
						GlobalVars.RELOAD_USER_BSFM_CONFIG = false;
						GlobalVars.userService.initializeUserBsfm();
					}
					if (GlobalVars.RELOAD_TW_FILTER) {
						logger.info("******** OPTOUT FILTER REFRESHED ************ ");
						GlobalVars.RELOAD_TW_FILTER = false;
						initializeOptOutEntries();
					}
					if (GlobalVars.RELOAD_UNICODE_ENCODING) {
						logger.info("******** Unicode Replacement Encoding Refreshed ************ ");
						GlobalVars.RELOAD_UNICODE_ENCODING = false;
						loadUnicodeEncoding();
					}
					if (GlobalVars.RELOAD_USER_LIMIT_CONFIG) {
						logger.info("******** USER TRAFFIC LIMIT REFRESHED ************ ");
						GlobalVars.RELOAD_USER_LIMIT_CONFIG = false;
						initializeSubmitLimitEntries();
						if (!GlobalCache.UserSubmitLimitEntries.isEmpty()) {
							for (String systemId : GlobalCache.UserRoutingThread.keySet()) {
								if (GlobalCache.UserSubmitLimitEntries.containsKey(systemId)) {
									GlobalCache.UserRoutingThread.get(systemId)
											.setSubmitLimitEntry(GlobalCache.UserSubmitLimitEntries.get(systemId));
									logger.info(systemId + " Submit Limit Entry Refreshed.");
								} else {
									GlobalCache.UserRoutingThread.get(systemId).setSubmitLimitEntry(null);
								}
							}
						}
					}
					if (GlobalVars.RELOAD_NETWORK_BSFM_CONFIG) {
						logger.info("******** NETWORK BASED BSFM REFRESHED ************ ");
						GlobalVars.RELOAD_NETWORK_BSFM_CONFIG = false;
						GlobalVars.networkService.initializeNetworkBsfm();
						try {
							GlobalCache.UserSessionObject.forEach((k, v) -> {
								((SessionManager) v).refreshNetworkBsfm();
							});
						} catch (Exception e) {
							logger.error(e + " While Reload Network Bsfm");
						}
					}
					if (GlobalVars.RELOAD_DLT_CONFIG) {
						GlobalVars.RELOAD_DLT_CONFIG = false;
						DltFilter.loadRules();
					}
					String flagVal = FileUtil.readFlag(Constants.CACHE_PRINT, true);
					if ((flagVal != null) && (flagVal.contains("707"))) {
						logger.info("");
						logger.info("******** Command To Print Cache ************ ");
						logger.info("");
						new Thread(new CleanUpThread(false, true), "CleanUpThread").start();
						CheckCpuCycle.PRINT = true;
						FileUtil.setDefaultFlag(Constants.CACHE_PRINT);
					}
					flagVal = FileUtil.readFlag(Constants.TEMP_QUEUE_FLAG, true);
					if (flagVal != null && flagVal.equalsIgnoreCase("707")) {
						logger.info("******** Temporary Queue Refreshed ************ ");
						FileUtil.setDefaultFlag(Constants.TEMP_QUEUE_FLAG);
						SmscInTemp.INITIALIZE = true;
					}
					flagVal = FileUtil.readFlag(Constants.HLR_CONFIG_FLAG, true);
					if (flagVal != null && flagVal.equalsIgnoreCase("707")) {
						logger.info("******** HLR CONFIG REFRESHED ************ ");
						FileUtil.setDefaultFlag(Constants.HLR_CONFIG_FLAG);
						loadHlrConfig();
					}
					// *************** Bulk Filter **************************
				}
			} catch (Exception e) {
				logger.error("process()", e.fillInStackTrace());
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
				// no harm
			}
		}
		logger.info("BearerBox Stopped");
	}

	private void loadHlrConfig() {
		try {
			Properties props = FileUtil.readProperties(Constants.HLR_CONFIG_FILE);
			Constants.HLR_DOWN_SMSC_1 = props.getProperty("HLR_DOWN_SMSC_1");
			Constants.HLR_DOWN_SMSC_2 = props.getProperty("HLR_DOWN_SMSC_2");
			Constants.HLR_DOWN_SMSC_3 = props.getProperty("HLR_DOWN_SMSC_3");
			Constants.HLR_DOWN_SMSC_4 = props.getProperty("HLR_DOWN_SMSC_4");
			Constants.HLR_DOWN_SMSC_5 = props.getProperty("HLR_DOWN_SMSC_5");
			Constants.HLR_STATUS_WAIT_DURATION = Integer.parseInt(props.getProperty("HLR_WAIT_DURATION"));
			Constants.HLR_SERVER_IP = props.getProperty("HLR_SERVER_IP").trim();
			Constants.HLR_SERVER_PORT = Integer.parseInt(props.getProperty("HLR_SERVER_PORT").trim());
			Constants.HLR_SESSION_LIMIT = Integer.parseInt(props.getProperty("HLR_SESSION_LIMIT").trim());
			Constants.PROMO_SENDER = props.getProperty("PROMO_SENDER");
			Constants.DND_SMSC = props.getProperty("DND_SMSC");
		} catch (Exception e) {
			logger.error("HlrConfiguration File Read Error: " + e.getMessage());
		}
	}

	private void checkMemoryUsage(int i) {
		long mb = 1024 * 1024;
		Runtime runtime = Runtime.getRuntime();
		long used_memory = (runtime.totalMemory() - runtime.freeMemory()) / mb;
		// long max_memory = (runtime.maxMemory() / mb);
		logger.info("Memory Usage[" + i + "]:---> " + used_memory + " MB");
	}

	private void initializeDGM() {
		if (DistributionGroupManager.initialize()) {
			GlobalVars.DISTRIBUTION = true;
			logger.info("**** Distribution is Enabled ****");
		} else {
			GlobalVars.DISTRIBUTION = false;
			logger.info("**** Distribution is Disabled ****");
		}
	}

	private void loadUnicodeEncoding() {
		GlobalCache.UnicodeReplacement.clear();
		try {
			Properties props = FileUtil.readProperties(Constants.UnicodeEncodingFile, true);
			for (String key : props.stringPropertyNames()) {
				GlobalCache.UnicodeReplacement.put(key.toUpperCase(), props.getProperty(key).toUpperCase());
			}
		} catch (Exception e) {
			logger.error(Constants.UnicodeEncodingFile, e.fillInStackTrace());
		}
		logger.info("UnicodeEncoding: " + GlobalCache.UnicodeReplacement);
	}

	private void initializeOptOutEntries() {
		logger.info("initializing optout entries");
		GlobalCache.OptOutFilter.clear();
		java.sql.Statement statement = null;
		java.sql.Connection con = null;
		java.sql.ResultSet rs = null;
		String sql = "select * from optout_report";
		Map<String, Set<Long>> OptOutFilter = new HashMap<String, Set<Long>>();
		try {
			con = GlobalCache.connection_pool_proc.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				if (rs.getString("short_code") != null && rs.getLong("number") > 0) {
					Set<Long> set = null;
					if (OptOutFilter.containsKey(rs.getString("short_code"))) {
						set = OptOutFilter.get(rs.getString("short_code"));
					} else {
						set = new java.util.HashSet<Long>();
					}
					set.add(rs.getLong("number"));
					OptOutFilter.put(rs.getString("short_code"), set);
				}
			}
		} catch (Exception ex) {
			logger.error("initializeOptOutEntries()", ex.fillInStackTrace());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
				}
			}
			if (con != null) {
				GlobalCache.connection_pool_proc.putConnection(con);
			}
		}
		if (!OptOutFilter.isEmpty()) {
			GlobalCache.OptOutFilter.putAll(OptOutFilter);
		}
		logger.info("OptOutEntries: " + GlobalCache.OptOutFilter.size());
	}

	private void initializeSubmitLimitEntries() {
		logger.info("initializing submit_limit");
		GlobalCache.UserSubmitLimitEntries.clear();
		java.sql.Statement statement = null;
		java.sql.Connection con = null;
		java.sql.ResultSet rs = null;
		String sql = "select * from user_limit";
		com.hti.user.dto.SubmitLimitEntry submitLimitEntry = null;
		try {
			con = GlobalCache.connection_pool_proc.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				if (rs.getInt("duration") > 0 && rs.getInt("count") > 0) {
					submitLimitEntry = new com.hti.user.dto.SubmitLimitEntry(rs.getInt("user_id"),
							rs.getInt("duration"), rs.getInt("count"), rs.getInt("reroute_smsc_id"),
							rs.getString("alert_number"), rs.getString("alert_email"), rs.getString("alert_sender"));
					if (GlobalCache.SmscEntries.containsKey(rs.getInt("reroute_smsc_id"))) {
						submitLimitEntry
								.setRerouteSmsc(GlobalCache.SmscEntries.get(rs.getInt("reroute_smsc_id")).getName());
					}
					if (GlobalCache.UserEntries.containsKey(rs.getInt("user_id"))) {
						GlobalCache.UserSubmitLimitEntries
								.put(GlobalCache.UserEntries.get(rs.getInt("user_id")).getSystemId(), submitLimitEntry);
					}
				}
			}
		} catch (Exception ex) {
			logger.error("initializeSubmitLimitEntries()", ex.fillInStackTrace());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
				}
			}
			if (con != null) {
				GlobalCache.connection_pool_proc.putConnection(con);
			}
		}
		logger.info("SubmitLimitEntries: " + GlobalCache.UserSubmitLimitEntries.keySet());
	}

	public void stop() {
		logger.info("BearerBox Stopping");
		deliverProcess.stop();
		for (WebDeliverProcess webDeliverProcess : GlobalCache.UserWebObject.values()) {
			webDeliverProcess.stop();
		}
		processPDU.stop();
		stop = true;
	}

	private void initializeBSFM() {
		SpamFilter.clearCache();
		if (SpamFilter.loadProfiles() > 0) {
			BULKFILTER = true;
		} else {
			BULKFILTER = false;
		}
	}
}
