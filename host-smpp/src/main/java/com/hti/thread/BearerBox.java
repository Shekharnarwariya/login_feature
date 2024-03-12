/*
 * BearerBox.java
 * Thread Responsible for Making SMSC Connection, making new thread
 * smsc connection for each SMSC, assign new queue for each connection
 * manipulate < TrackHtiClientBackLogQueue SmscQueueSelection DatabaseSMSCOutThread
 * ThreadQueueChange ,EnquireLink Thread>
 * Created on 06 April 2004, 12:45
 */
package com.hti.thread;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.dlt.DltFilter;
import com.hti.objects.HTIQueue;
import com.hti.objects.LogPDU;
import com.hti.smsc.DistributionGroupManager;
import com.hti.util.Constants;
import com.hti.util.FileUtil;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;
import com.hti.tw.filter.FilterService;

public class BearerBox implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private OmqConnection omqConnection;
	private OmqReceivedProcess omqReceivedProcess;
	private AlertHandler alertHandler;
	private DLRProcess dlrProcessor;
	private SignalWaitProcess signalWaitProcess;
	private int process_day = 0;
	// private String process_time = null;
	private boolean stop;

	public BearerBox() throws Exception {
		try {
			logger.info("BearerBox Starting");
			GlobalCache.SmscSubmitCounter = GlobalVars.hazelInstance.getMap("smsc_submit_count");
			GlobalVars.smscService.initializeGlobalCache();
			checkMappedQueue();
			if (GlobalVars.MASTER_CLIENT) {
				initializeSubmitLimitCounter();
				initializeResponseCache();
			}
			process_day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
			throw new Exception(ex);
		}
		try {
			checkBackupQueue();
			loadSpecialEncoding();
			initializeDGM();
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
			throw new Exception(ex);
		}
		try {
			dlrProcessor = new DLRProcess();
			new Thread(dlrProcessor, "DLRProcess").start();
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
			throw new Exception(ex);
		}
		try {
			GlobalVars.smscService.initializeLimit();
			GlobalVars.smscService.initializeStatus();
			GlobalVars.smscService.initializeLoopingRules();
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
			throw new Exception(ex);
		}
		DltFilter.loadRules();
		FilterService.load2wayFilter();
		try {
			GlobalVars.userService.reloadStatus(true);
			checkMemoryUsage(1);
			alertHandler = new AlertHandler();
			new Thread(alertHandler, "AlertHandler").start();
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
			throw new Exception(ex);
		}
		try {
			omqConnection = new OmqConnection();
			new Thread(omqConnection, "OmqConnection").start();
			omqReceivedProcess = new OmqReceivedProcess();
			new Thread(omqReceivedProcess, "OmqReceivedProcess").start();
			signalWaitProcess = new SignalWaitProcess();
			new Thread(signalWaitProcess, "SignalWaitProcess").start();
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
			throw new Exception(ex);
		}
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				String flagVal = null;
				if (process_day != Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
					logger.info("******** Date Changed ************ ");
					if (GlobalVars.MASTER_CLIENT) {
						new Thread(new CleanupThread(true, true), "CleanUpThread").start();
						DeliverLogInsert.DELETE_OLD_RECORDS = true;
					}
					process_day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
					GlobalVars.smscService.initializeLimit();
				} else {
					flagVal = FileUtil.readFlag(Constants.Dashboard_flag_file, true);
					if ((flagVal != null) && (flagVal.contains("707"))) {
						logger.info("******** Dashboard Reload ************ ");
						FileUtil.setDefaultFlag(Constants.Dashboard_flag_file);
					}
				}
				if (Constants.RELOAD_USER_CONFIG) {
					logger.info("***** Checking For Flag Status Of All Users ***** ");
					Constants.RELOAD_USER_CONFIG = false;
					GlobalVars.userService.reloadStatus(false);
				}
				if (Constants.RELOAD_SMSC_CONFIG) {
					Constants.RELOAD_SMSC_CONFIG = false;
					logger.info("***** Checking For Flag Status Of All Routes ***** ");
					GlobalVars.smscService.reloadStatus();
				}
				if (Constants.RELOAD_SMSC_LT_CONFIG) {
					Constants.RELOAD_SMSC_LT_CONFIG = false;
					logger.info("***** Smsc Limit Configuration Refreshed ***** ");
					GlobalVars.smscService.initializeLimit();
				}
				if (Constants.RELOAD_SMSC_SPCL_SETTING) {
					logger.info("******** Smsc Special Setting Refreshed ************ ");
					Constants.RELOAD_SMSC_SPCL_SETTING = false;
					GlobalVars.smscService.initSpecialSetting();
				}
				if (Constants.RELOAD_SMSC_BSFM) {
					logger.info("******** Smsc BSFM Refreshed ************ ");
					Constants.RELOAD_SMSC_BSFM = false;
					GlobalVars.smscService.initSmscBsfm();
				}
				if (Constants.RELOAD_SMSC_LOOP_CONFIG) {
					logger.info("******** Smsc Looping Rules Refreshed ************ ");
					Constants.RELOAD_SMSC_LOOP_CONFIG = false;
					GlobalVars.smscService.initializeLoopingRules();
				}
				if (Constants.RELOAD_ESME_ERROR_CONFIG) {
					logger.info("******** ESME ERROR CODE REFRESHED ************ ");
					Constants.RELOAD_ESME_ERROR_CONFIG = false;
					GlobalVars.smscService.initEsmeErrorConfig();
				}
				if (Constants.RELOAD_SIGNAL_ERROR_CONFIG) {
					logger.info("******** SIGNAL ERROR CODE REFRESHED ************ ");
					Constants.RELOAD_SIGNAL_ERROR_CONFIG = false;
					GlobalVars.smscService.initSignalErrorConfig();
				}
				if (Constants.RELOAD_DGM_CONFIG) {
					Constants.RELOAD_DGM_CONFIG = false;
					initializeDGM();
				}
				if (Constants.RELOAD_DLT_CONFIG) {
					Constants.RELOAD_DLT_CONFIG = false;
					DltFilter.loadRules();
				}
				if (Constants.RELOAD_SPCL_ENCODING) {
					logger.info("******** Special Encoding Refreshed ************ ");
					Constants.RELOAD_SPCL_ENCODING = false;
					loadSpecialEncoding();
				}
				if (Constants.RELOAD_TW_FILTER) {
					logger.info("******** 2Way Filter Refreshed ************ ");
					Constants.RELOAD_TW_FILTER = false;
					FilterService.load2wayFilter();
				}
				flagVal = FileUtil.readFlag(Constants.CACHE_PRINT, true);
				if ((flagVal != null) && (flagVal.contains("707"))) {
					logger.info("");
					logger.info("******** Command To Print Cache ************ ");
					logger.info("");
					if (GlobalVars.MASTER_CLIENT) {
						new Thread(new CleanupThread(false, true), "CleanUpThread").start();
					}
					CheckCpuCycle.PRINT = true;
					FileUtil.setDefaultFlag(Constants.CACHE_PRINT);
				}
				if (GlobalVars.MASTER_CLIENT) {
					if (!GlobalCache.SmscSubmitCounter.isEmpty()) {
						insertSubmitLimitCounter();
					}
				}
				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException ie) {
					// no harm
				}
			} catch (Exception e) {
				logger.error("run()", e.fillInStackTrace());
			}
		}
		logger.info("BearerBox Stopped");
	}

	private void loadSpecialEncoding() {
		GlobalCache.SpecialEncoding.clear();
		try {
			Properties props = FileUtil.readProperties(Constants.SpecialEncodingFile, true);
			for (String key : props.stringPropertyNames()) {
				GlobalCache.SpecialEncoding.put(key.toUpperCase(), props.getProperty(key).toUpperCase());
			}
		} catch (Exception e) {
			logger.error(Constants.SpecialEncodingFile, e.fillInStackTrace());
		}
		logger.info("SpecialEncoding: " + GlobalCache.SpecialEncoding);
	}

	private void checkMemoryUsage(int i) {
		long mb = 1024 * 1024;
		Runtime runtime = Runtime.getRuntime();
		long used_memory = (runtime.totalMemory() - runtime.freeMemory()) / mb;
		// long max_memory = (runtime.maxMemory() / mb);
		logger.info("Memory Usage[" + i + "]:---> " + used_memory + " MB");
	}

	private void checkBackupQueue() {
		logger.info("Checking Backup Response Queue");
		File resp_backup_dir = new File(Constants.resp_backup_dir);
		if (resp_backup_dir.exists()) {
			try {
				FileUtils.forceDelete(resp_backup_dir);
				logger.info("<-- Deleted Response backup Directory -->");
			} catch (IOException e) {
				logger.error("<-- Unable to delete Directory --> " + Constants.resp_backup_dir);
			}
		}
		if (resp_backup_dir.mkdir()) {
			logger.info("<-- Response backup Directory Created -->");
		} else {
			logger.info("<-- Unable to create Response backup Directory -->");
		}
		File dlr_backup_dir = new File(Constants.deliver_backup_dir);
		if (!dlr_backup_dir.exists()) {
			if (dlr_backup_dir.mkdir()) {
				logger.info("<-- Deliver backup Directory Created -->");
			} else {
				logger.info("<-- Unable to create Deliver backup Directory -->");
			}
		}
		File resp_process_backup_dir = new File(Constants.resp_process_backup_dir);
		if (!resp_process_backup_dir.exists()) {
			if (resp_process_backup_dir.mkdir()) {
				logger.info("<-- Response Process backup Directory Created -->");
			} else {
				logger.info("<-- Unable to create  Response Process backup Directory -->");
			}
		}
		logger.info("Backup Queue Check Finished");
	}

	private void checkMappedQueue() {
		logger.info("Checking MappedIdQueue Backup");
		File file = new File(Constants.BACKUP_FOLDER + "MappedIdQueue.mapped");
		if (file.exists()) {
			try {
				HTIQueue mapped_id_Queue = (HTIQueue) FileUtil
						.readObject(Constants.BACKUP_FOLDER + "MappedIdQueue.mapped", true);
				while (!mapped_id_Queue.isEmpty()) {
					GlobalQueue.MAPPED_ID_QUEUE.enqueue((LogPDU) mapped_id_Queue.dequeue());
				}
			} catch (IOException ex) {
				logger.error(" Object File IOError ", ex);
			}
			logger.info("MappedIdQueue Queue Size: " + GlobalQueue.MAPPED_ID_QUEUE.size());
		}
		logger.info("MappedIdQueue Check Finished");
	}

	private void initializeResponseCache() {
		logger.info("Initializing Smsc Wise Response Cache");
		Connection con = null;
		Statement statement = null;
		ResultSet rs = null;
		String msgid;
		String responseid;
		String smsc;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, -3);
		String backdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
		String sql = "select * from mapped_id where server_id=" + GlobalVars.SERVER_ID + " and time > '" + backdate
				+ "' order by route_to_SMSC";
		try {
			con = GlobalCache.connnection_pool_1.getConnection();
			if (con != null) {
				statement = con.createStatement();
				rs = statement.executeQuery(sql);
				while (rs.next()) {
					msgid = rs.getString("msg_id");
					responseid = rs.getString("response_id");
					smsc = rs.getString("route_to_SMSC");
					// System.out.println(msgid + " " + responseid + " " + smsc);
					GlobalCache.ResponseLogDlrCache.put(msgid,
							new LogPDU(msgid, smsc, rs.getString("client_name"), rs.getString("destination"),
									rs.getString("source"), rs.getString("time"), rs.getDouble("cost"),
									rs.getBoolean("refund"), rs.getInt("server_id"), 0));
					// System.out.println("SmscSessionId: " + GlobalCache.SmscSessionId.get(smsc));
					int session_id = GlobalCache.SmscSessionId.get(smsc);
					// System.out.println("smscwiseResponseMap: " + GlobalCache.smscwiseResponseMap.get(session_id));
					GlobalCache.smscwiseResponseMap.get(session_id).put(responseid, msgid);
					// ---------------- End By Amit_Vish ------------------
				}
			}
		} catch (Exception q) {
			logger.error("initializeResponseCache()", q.fillInStackTrace());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqle) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException sqle) {
				}
			}
			GlobalCache.connnection_pool_1.putConnection(con);// done by ashish 131006
		}
		logger.info("Response Cache Initialized");
	}

	public void stop() {
		logger.info("BearerBox Stopping");
		try {
			omqConnection.stop();
			omqReceivedProcess.stop();
			alertHandler.stop();
			GlobalVars.smscService.stopConnections();
			logger.info("<---- All Smsc Disconnected ---> ");
			dlrProcessor.stop();
			signalWaitProcess.stop();
		} catch (Exception e) {
			logger.error("", e.fillInStackTrace());
		}
		stop = true;
	}

	private void initializeSubmitLimitCounter() {
		logger.info("initializing submit_limit Counters");
		java.sql.Statement statement = null;
		java.sql.Connection con = null;
		java.sql.ResultSet rs = null;
		String sql = "select * from submit_limit_status where time='"
				+ new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "'";
		try {
			con = GlobalCache.connnection_pool_1.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				GlobalCache.SmscSubmitCounter.put(rs.getInt("limit_id"), rs.getInt("counter"));
			}
		} catch (Exception ex) {
			logger.error("initializeSubmitLimitCounter()", ex.fillInStackTrace());
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
				GlobalCache.connnection_pool_1.putConnection(con);
			}
		}
		logger.info("SmscSubmitCounter: " + GlobalCache.SmscSubmitCounter);
	}

	private void insertSubmitLimitCounter() {
		logger.debug(" submit_limit_status inserting ");
		java.sql.PreparedStatement statement = null;
		java.sql.Connection con = null;
		java.sql.ResultSet rs = null;
		String sql = "insert into submit_limit_status(limit_id,time,counter) values(?,?,?) ON DUPLICATE KEY update counter=?";
		try {
			con = GlobalCache.connnection_pool_1.getConnection();
			con.setAutoCommit(false);
			statement = con.prepareStatement(sql);
			for (int limit_id : GlobalCache.SmscSubmitCounter.keySet()) {
				int counter = GlobalCache.SmscSubmitCounter.get(limit_id);
				statement.setInt(1, limit_id);
				statement.setString(2, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
				statement.setInt(3, counter);
				statement.setInt(4, counter);
				statement.addBatch();
			}
			int[] insertCounter = statement.executeBatch();
			con.commit();
			logger.debug("submit_limit_status insert counter: " + insertCounter.length);
		} catch (Exception ex) {
			logger.error("insertSubmitLimitCounter()", ex.fillInStackTrace());
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
				GlobalCache.connnection_pool_1.putConnection(con);
			}
		}
	}

	private void initializeDGM() {
		if (DistributionGroupManager.initialize()) {
			Constants.DISTRIBUTION = true;
			logger.info("**** Distribution is Enabled ****");
		} else {
			Constants.DISTRIBUTION = false;
			logger.info("**** Distribution is Disabled ****");
		}
	}
}
