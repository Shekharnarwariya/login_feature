package com.hti.thread;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.hlr.StatusInsertThread;
import com.hti.user.dto.UserEntry;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;

public class CleanUpThread implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private boolean RESET;
	private int CLEAR_LIMIT = 25000;
	private boolean optimize;
	private boolean PART_1;
	private boolean PART_2;

	public CleanUpThread(boolean PART_1, boolean PART_2) {
		logger.info("CleanupThread Starting");
		this.PART_1 = PART_1;
		this.PART_2 = PART_2;
		if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			optimize = true;
		}
	}

	@Override
	public void run() {
		if (PART_1) {
			logger.info("Clearing SubmitCounter Cache: " + ProcessPDU.submittedCounter);
			ProcessPDU.submittedCounter = 0;
			if (Calendar.getInstance().get(Calendar.DATE) == 1) { // first day of month
				logger.info("<-- Renaming smsc_in_log -->");
				SmscInLogThread.RENAME_TABLE = true;
				RequestLogInsert.RENAME_TABLE = true;
			}
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				if (!RESET) {
					LocalTime parse_time = LocalTime.parse("00:03");
					if (LocalTime.now().isAfter(parse_time)) {
						logger.info("******** Processing Tables Cleanup ************ ");
						ReportLog.SHIFT_OLD_RECORDS = true;
						SmscIn.DELETE_OLD_RECORDS = true;
						SmOptParamInsert.DELETE_OLD_RECORDS = true;
						StatusInsertThread.DELETE_OLD_RECORDS = true;
						BackupResponseInsertThread.DELETE_OLD_RECORDS = true;
						RESET = true;
					}
				} else {
					LocalTime parse_time = LocalTime.parse("00:08");
					if (LocalTime.now().isAfter(parse_time)) {
						logger.info("******** Checking For Garbage Collection ************ ");
						CheckCpuCycle.EXECUTE_GC = true;
						break;
					}
				}
			}
		}
		if (PART_2) {
			logger.info("******** Content_user Tables Cleanup ************ ");
			List<String> list = listTables();
			Map<String, Integer> keepLogDays = listKeepLogDays();
			logger.info("KeepLogUsers: " + keepLogDays.size() + " Tables: " + list.size());
			for (String system_id : keepLogDays.keySet()) {
				if (list.contains("content_" + system_id)) {
					String table = "content_" + system_id;
					int days = keepLogDays.get(system_id);
					if (days > 0) {
						int deleteCounter = 0;
						logger.info("Checking For Cleanup of " + table + " Username: " + system_id);
						if (GlobalCache.UserContentQueueObject.containsKey(system_id)) { // give command to stop insertion
							GlobalCache.UserContentQueueObject.get(system_id).setHoldOn(true);
							do {
								deleteCounter = clearLog(table, days);
							} while (deleteCounter == CLEAR_LIMIT);
							if (optimize) {
								optimizeTable(table);
							}
							GlobalCache.UserContentQueueObject.get(system_id).setHoldOn(false);
						} else {
							do {
								deleteCounter = clearLog(table, days);
							} while (deleteCounter == CLEAR_LIMIT);
							if (optimize) {
								optimizeTable(table);
							}
						}
					} else {
						logger.info(table + " Cleanup Disabled");
					}
				}
			}
		}
		// --------------------------------------------------------------
		logger.info("CleanUpThread Stopped");
	}

	private Map<String, Integer> listKeepLogDays() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		Collection<UserEntry> list = GlobalVars.userService.listUserEntries();
		for (UserEntry entry : list) {
			map.put(entry.getSystemId(), entry.getLogDays());
		}
		return map;
	}

	private int clearLog(String table, int days) {
		int deletecount = 0;
		String deletesql = "delete from " + table + " where msg_id < ? limit ?"; // use msg_id instead of time
		logger.debug(deletesql);
		Connection connection = null;
		PreparedStatement statement = null;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -days);
		try {
			connection = GlobalCache.connection_pool_proc.getConnection();
			statement = connection.prepareStatement(deletesql);
			statement.setString(1, new SimpleDateFormat("yyMMdd").format(calendar.getTime()) + "0000000000000");
			statement.setInt(2, CLEAR_LIMIT);
			deletecount = statement.executeUpdate();
			if (deletecount > 0) {
				logger.info(table + " Deletion Count : " + deletecount);
			} else {
				logger.info("<-- No Records to delete [" + table + "] -->");
			}
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
		} finally {
			GlobalCache.connection_pool_proc.putConnection(connection);
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
					statement = null;
				}
			}
		}
		return deletecount;
	}

	private boolean optimizeTable(String table) {
		logger.info("optimizing table: " + table);
		Connection connection = null;
		PreparedStatement pstmt = null;
		boolean optimized = false;
		try {
			connection = GlobalCache.connection_pool_proc.getConnection();
			pstmt = connection.prepareStatement("optimize table " + table);
			optimized = pstmt.execute();
			logger.info(table + " Optimized: " + optimized);
		} catch (Exception ex) {
			logger.error("", ex);
		} finally {
			GlobalCache.connection_pool_proc.putConnection(connection);
			connection = null;
		}
		return optimized;
	}

	private List<String> listTables() {
		logger.info("listing content tables");
		List<String> list = new ArrayList<String>();
		ResultSet rs = null;
		Connection connection = null;
		try {
			connection = GlobalCache.connection_pool_proc.getConnection();
			DatabaseMetaData md = connection.getMetaData();
			rs = md.getTables(null, null, "content_%", null);
			while (rs.next()) {
				String table_name = rs.getString(3);
				list.add(table_name);
			}
		} catch (Exception ex) {
			logger.error("", ex);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
					rs = null;
				}
			}
			GlobalCache.connection_pool_proc.putConnection(connection);
			connection = null;
		}
		return list;
	}
}
