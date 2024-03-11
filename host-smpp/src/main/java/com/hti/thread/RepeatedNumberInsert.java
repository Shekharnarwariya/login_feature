package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.RepeatedNumberEntry;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;

public class RepeatedNumberInsert implements Runnable {
	private Connection connection = null;
	private PreparedStatement statement = null;
	private String sql = null;
	private int count = 0;
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private boolean stop;
	public static boolean CLEANUP = true;

	public RepeatedNumberInsert() {
		logger.info("RepeatedNumberInsert Thread Starting");
	}

	@Override
	public void run() {
		// ----------- initialize cache with recorded number ----------------
		logger.info("Checking For Repeated Number to load");
		sql = "select group_id,number,count from repeated_number_log";
		ResultSet rs = null;
		try {
			connection = GlobalCache.connnection_pool_2.getConnection();
			statement = connection.prepareStatement(sql);
			rs = statement.executeQuery();
			while (rs.next()) {
				if (rs.getInt("group_id") > 0) {
					GlobalCache.GroupWiseRepeatedNumbers.put(rs.getInt("group_id") + "#" + rs.getString("number"),
							rs.getInt("count"));
				}
			}
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			GlobalCache.connnection_pool_2.putConnection(connection);
		}
		logger.info("Repeated Number Cache: " + GlobalCache.GroupWiseRepeatedNumbers.size());
		// ----------- end initialize cache ---------------------------------
		while (!stop) {
			if (GlobalQueue.RepeatedNumberQueue.isEmpty()) {
				if (CLEANUP) {
					CLEANUP = false;
					logger.info("Checking For Repeated Number Log Cleanup");
					Set<Integer> groups = new HashSet<Integer>();
					sql = "select distinct(group_id) as group_id from repeated_number_log";
					try {
						connection = GlobalCache.connnection_pool_2.getConnection();
						statement = connection.prepareStatement(sql);
						rs = statement.executeQuery();
						while (rs.next()) {
							groups.add(rs.getInt("group_id"));
						}
					} catch (Exception ex) {
						logger.error("", ex.fillInStackTrace());
					} finally {
						if (statement != null) {
							try {
								statement.close();
							} catch (SQLException e) {
							}
						}
						if (rs != null) {
							try {
								rs.close();
							} catch (SQLException e) {
							}
						}
						GlobalCache.connnection_pool_2.putConnection(connection);
					}
					logger.info("Repeated Number Groups: " + groups);
					if (!groups.isEmpty()) {
						for (int groupId : groups) {
							// logger.info(groupId + " -> " + GlobalCache.SmscGroupEntries);
							if (GlobalCache.SmscGroupEntries.containsKey(groupId)) {
								int keep_log_days = GlobalCache.SmscGroupEntries.get(groupId).getKeepRepeatDays();
								if (keep_log_days > 0) {
									sql = "delete from repeated_number_log where group_id=" + groupId
											+ " and DATE(updatedOn) < DATE_SUB(NOW(), INTERVAL " + keep_log_days
											+ " DAY) ";
									logger.info("SQL: " + sql);
									try {
										connection = GlobalCache.connnection_pool_2.getConnection();
										statement = connection.prepareStatement(sql);
										int deletecount = statement.executeUpdate();
										logger.info("Repeated Number Deletion Count : " + deletecount);
									} catch (Exception ex) {
										logger.error("", ex.fillInStackTrace());
									} finally {
										if (statement != null) {
											try {
												statement.close();
											} catch (SQLException e) {
											}
										}
										GlobalCache.connnection_pool_2.putConnection(connection);
									}
								} else {
									logger.info(groupId + " Keep Repeated Number Days No limit ");
								}
							} else {
								logger.info("GroupEntry Not Found : " + groupId);
							}
						}
					}
				} else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			} else {
				count = 0;
				sql = "insert into repeated_number_log(group_id,number,count) values(?,?,?) ON DUPLICATE KEY update count=?";
				try {
					connection = GlobalCache.connnection_pool_2.getConnection();
					statement = connection.prepareStatement(sql);
					connection.setAutoCommit(false);
					RepeatedNumberEntry entry = null;
					while (!GlobalQueue.RepeatedNumberQueue.isEmpty()) {
						try {
							entry = (RepeatedNumberEntry) GlobalQueue.RepeatedNumberQueue.dequeue();
							if (entry != null) {
								statement.setInt(1, entry.getGroupId());
								statement.setString(2, entry.getNumber());
								statement.setInt(3, entry.getCount());
								statement.setInt(4, entry.getCount());
								statement.addBatch();
							}
						} catch (SQLException sqle) {
							logger.error("process(3)", sqle.fillInStackTrace());
						}
						if (++count >= 1000) {
							break;
						}
					}
					if (count > 0) {
						statement.executeBatch();
						connection.commit();
					}
				} catch (SQLException sqle) {
					logger.error("process(4)", sqle.fillInStackTrace());
				} catch (Exception ex) {
					logger.error("process(5)", ex.fillInStackTrace());
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
			}
		}
		logger.info("RepeatedNumberInsert Stopped.Queue: " + GlobalQueue.RepeatedNumberQueue.size());
	}

	public void stop() {
		logger.info("RepeatedNumberInsert Stopping.Queue: " + GlobalQueue.RepeatedNumberQueue.size());
		stop = true;
	}
}
