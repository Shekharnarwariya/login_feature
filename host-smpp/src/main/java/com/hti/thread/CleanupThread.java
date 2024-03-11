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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.DeliverSMExt;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;

public class CleanupThread implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private boolean RESET;
	private int CLEAR_LIMIT = 25000;
	private boolean optimize;
	private boolean PART_1;
	private boolean PART_2;

	public CleanupThread(boolean PART_1, boolean PART_2) {
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
			if (Calendar.getInstance().get(Calendar.DATE) == 1) { // first day of month
				logger.info("<-- Renaming smsc_out -->");
				SmscSubmitLog.RENAME_TABLE = true;
			}
			//logger.info("Clearing SubmitCounter Cache: " + GlobalCache.SmscSubmitCounter);
			//GlobalCache.SmscSubmitCounter.clear();
			//logger.info("*** SubmitCounter Cache Cleared[" + LocalTime.now() + "] ***");
			while (true) {
				if (!RESET) {
					LocalTime parse_time = LocalTime.parse("00:05");
					if (LocalTime.now().isAfter(parse_time)) {
						logger.info("******** Processing Tables Cleanup ************ ");
						MappedIdDeletion.DELETE_OLD_RECORDS = true;
						MIS.SHIFT_OLD_RECORDS = true;
						RepeatedNumberInsert.CLEANUP = true;
						RESET = true;
					}
				} else {
					LocalTime parse_time = LocalTime.parse("00:10");
					if (LocalTime.now().isAfter(parse_time)) {
						logger.info("******** Checking For Garbage Collection ************ ");
						CheckCpuCycle.EXECUTE_GC = true;
						break;
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			// -------- mis_table_log --------
			while (MIS.SHIFT_OLD_RECORDS) {
				logger.info("Waiting For Mis Table Cleanup");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}
		if (PART_2) {
			if (optimize) {
				logger.info("******** Executing mis log Optimize ************ ");
				int misDeleteCounter = 0;
				do {
					misDeleteCounter = clearLog("mis_table_log", 30);
				} while (misDeleteCounter == CLEAR_LIMIT);
				optimizeTable("mis_table_log");
			}
			Map<String, List<String>> pendingDlrs = listPendingDlrs();
			logger.info("******** Executing mis User Tables Cleanup ************ ");
			List<String> list_tables = listTables();
			Map<String, Integer> keepLogDays = listKeepLogDays();
			for (String system_id : keepLogDays.keySet()) {
				if (list_tables.contains("mis_" + system_id.toLowerCase())) {
					String table = "mis_" + system_id;
					int days = keepLogDays.get(system_id);
					if (days > 0) {
						int deleteCounter = 0;
						logger.info("Checking For Cleanup of " + table + " Username: " + system_id);
						if (GlobalCache.UserMisQueueObject.containsKey(system_id)) { // give command to stop insertion
							((UserWiseMis) GlobalCache.UserMisQueueObject.get(system_id)).setHoldOn(true);
							do {
								deleteCounter = clearLog(table, days);
							} while (deleteCounter == CLEAR_LIMIT);
							if (optimize) {
								optimizeTable(table);
							}
							((UserWiseMis) GlobalCache.UserMisQueueObject.get(system_id)).setHoldOn(false);
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
			// ----------------- clearing pending dlrs ------------------------
			logger.info("listing ACCEPTD Disabled Users");
			List<String> acceptedDisabledUsers = listAcceptedDisabledUsers();
			logger.info("ACCEPTD Disabled Users: " + acceptedDisabledUsers.size());
			for (Map.Entry<String, List<String>> entry : pendingDlrs.entrySet()) {
				String system_id = entry.getKey();
				if (acceptedDisabledUsers.contains(system_id)) {
					logger.info("listing Pending Dlrs For " + system_id);
					if (list_tables.contains("mis_" + system_id.toLowerCase())) {
						List<DeliverSMExt> dlr_list = listUserWisePendings(system_id, entry.getValue());
						logger.info(system_id + " Pending Dlrs: " + dlr_list.size());
						while (!dlr_list.isEmpty()) {
							GlobalQueue.DeliverProcessQueue.enqueue(dlr_list.remove(0));
						}
					} else {
						logger.info("mis_" + system_id + " Not Found.");
						continue;
					}
				} else {
					logger.info(system_id + " Already Received ACCEPTD Receiept.");
				}
				logger.info("Updating mis_" + system_id + " Status");
				updateUserWiseStatus(system_id, entry.getValue());
			}
		}
		logger.info("CleanupThread Stopped");
	}

	private List<DeliverSMExt> listUserWisePendings(String username, List<String> pendings) {
		List<DeliverSMExt> dlr_list = new ArrayList<DeliverSMExt>();
		ResultSet rs = null;
		Connection connection = null;
		PreparedStatement statement = null;
		String sql = "select msg_id,source_no,dest_no,submitted_time from mis_" + username + " where msg_id in("
				+ String.join(",", pendings) + ")";
		try {
			connection = GlobalCache.connnection_pool_2.getConnection();
			statement = connection.prepareStatement(sql);
			rs = statement.executeQuery();
			DeliverSMExt deliver = null;
			while (rs.next()) {
				String msg_id = rs.getString("msg_id");
				String submitOn = new SimpleDateFormat("yyMMddHHmmss")
						.format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("submitted_time")));
				String deliverOn = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
				deliver = new DeliverSMExt(msg_id, username, submitOn, deliverOn, rs.getString("source_no"),
						rs.getString("dest_no"), "ACCEPTD", "000");
				dlr_list.add(deliver);
			}
		} catch (Exception ex) {
			logger.error(username, ex.fillInStackTrace());
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
			GlobalCache.connnection_pool_2.putConnection(connection);
		}
		return dlr_list;
	}

	private void updateUserWiseStatus(String system_id, List<String> pendings) {
		Connection connection = null;
		PreparedStatement statement = null;
		String sql = "update mis_" + system_id + " set status='ACCEPTD',err_code='000' where msg_id in("
				+ String.join(",", pendings) + ")";
		logger.debug(sql);
		try {
			connection = GlobalCache.connnection_pool_2.getConnection();
			statement = connection.prepareStatement(sql);
			int update_count = statement.executeUpdate();
			logger.info("mis_" + system_id + " Update Counter : " + update_count);
			statement.close();
			sql = "update mis_table_log set status='ACCEPTD' where msg_id in(" + String.join(",", pendings) + ")";
			logger.debug(sql);
			statement = connection.prepareStatement(sql);
			update_count = statement.executeUpdate();
			logger.info("mis_table_log Update Counter : " + update_count);
		} catch (Exception ex) {
			logger.error(system_id, ex.fillInStackTrace());
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException sqle) {
				}
			}
			GlobalCache.connnection_pool_2.putConnection(connection);
		}
	}

	private Map<String, List<String>> listPendingDlrs() {
		logger.info("listing pending dlrs");
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		ResultSet rs = null;
		Connection connection = null;
		Statement statement = null;
		String sql = "select msg_id,username from mis_table_log where status='ATES'";
		try {
			connection = GlobalCache.connnection_pool_2.getConnection();
			statement = connection.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				String username = rs.getString("username");
				String msg_id = rs.getString("msg_id");
				List<String> list = null;
				if (map.containsKey(username)) {
					list = map.remove(username);
				} else {
					list = new ArrayList<String>();
				}
				list.add(msg_id);
				map.put(username, list);
			}
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
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
			GlobalCache.connnection_pool_2.putConnection(connection);
		}
		logger.info("Pending Dlrs Users: " + map.size());
		return map;
	}

	private List<String> listAcceptedDisabledUsers() {
		List<String> users = new ArrayList<String>();
		ResultSet rs = null;
		Connection connection = null;
		Statement statement = null;
		String sql = "select A.system_id,B.accepted from usermaster A,user_dlr_setting B where A.id=B.user_id";
		try {
			connection = GlobalCache.connnection_pool_2.getConnection();
			statement = connection.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				if (!rs.getBoolean("accepted")) {
					users.add(rs.getString("system_id"));
				}
			}
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
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
			GlobalCache.connnection_pool_2.putConnection(connection);
		}
		return users;
	}

	private Map<String, Integer> listKeepLogDays() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		ResultSet rs = null;
		Connection connection = null;
		Statement statement = null;
		String sql = "select system_id,log_days from usermaster";
		try {
			connection = GlobalCache.connnection_pool_2.getConnection();
			statement = connection.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				map.put(rs.getString("system_id"), rs.getInt("log_days"));
			}
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
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
			GlobalCache.connnection_pool_2.putConnection(connection);
		}
		return map;
	}

	private int clearLog(String table, int days) {
		int deletecount = 0;
		String deletesql = "delete from " + table + " where DATE(submitted_time) < ? limit ?";
		logger.debug(deletesql);
		Connection connection = null;
		PreparedStatement statement = null;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -days);
		try {
			connection = GlobalCache.connnection_pool_2.getConnection();
			statement = connection.prepareStatement(deletesql);
			statement.setString(1, new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
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
			GlobalCache.connnection_pool_2.putConnection(connection);
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
			connection = GlobalCache.connnection_pool_2.getConnection();
			pstmt = connection.prepareStatement("optimize table " + table);
			optimized = pstmt.execute();
			logger.info(table + " Optimized: " + optimized);
		} catch (Exception ex) {
			logger.error("", ex);
		} finally {
			GlobalCache.connnection_pool_2.putConnection(connection);
			connection = null;
		}
		return optimized;
	}

	private List<String> listTables() {
		logger.info("listing mis tables");
		List<String> list = new ArrayList<String>();
		ResultSet rs = null;
		Connection connection = null;
		try {
			connection = GlobalCache.connnection_pool_2.getConnection();
			DatabaseMetaData md = connection.getMetaData();
			rs = md.getTables(null, null, "mis_%", null);
			while (rs.next()) {
				String table_name = rs.getString(3);
				list.add(table_name.toLowerCase());
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
			GlobalCache.connnection_pool_2.putConnection(connection);
			connection = null;
		}
		return list;
	}
}
