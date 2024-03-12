package com.hti.thread;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.SubmittedObj;
import com.hti.util.Constants;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;

public class SmscSubmitLog implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private Connection connection = null;
	private PreparedStatement statement = null;
	private String sql = null;
	private boolean stop;
	public static boolean RENAME_TABLE;

	public SmscSubmitLog() {
		logger.info("SmscSubmitLog Thread starting");
	}

	@Override
	public void run() {
		while (!stop) {
			if (RENAME_TABLE) {
				RENAME_TABLE = false;
				change_table();
			}
			if (GlobalQueue.submitLogQueue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			} else {
				sql = "insert into " + Constants.LOG_DB + ".smsc_out(msg_id,smsc,time) values(?,?,?)";
				int count = 0;
				try {
					connection = GlobalCache.connnection_pool_1.getConnection();
					statement = connection.prepareStatement(sql);
					connection.setAutoCommit(false);
					SubmittedObj log = null;
					while (!GlobalQueue.submitLogQueue.isEmpty()) {
						// add = false;
						log = (SubmittedObj) GlobalQueue.submitLogQueue.dequeue();
						statement.setString(1, log.getMsgId());
						statement.setString(2, log.getSmsc());
						statement.setString(3, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(log.getSubmitTime()));
						statement.addBatch();
						if (++count >= 1000) {
							break;
						}
					}
					if (count > 0) {
						int[] rowup = statement.executeBatch();
						connection.commit();
						count = rowup.length;
						logger.debug("Smsc Submit Log Insert Counter:-> " + count);
					}
				} catch (java.sql.SQLException sqle) {
					logger.error("process(1)", sqle.fillInStackTrace());
				} catch (Exception e) {
					logger.error("process(2)", e.fillInStackTrace());
				} finally {
					if (statement != null) {
						try {
							statement.close();
						} catch (SQLException ex) {
							statement = null;
						}
					}
					GlobalCache.connnection_pool_1.putConnection(connection);
					connection = null;
				}
			}
		}
		logger.info("SmscSubmitLog Thread Stopped. Queue: " + GlobalQueue.submitLogQueue.size());
	}

	private Connection getConnection() {
		logger.info("createConnection()");
		Connection connection = null;
		try {
			Class.forName(Constants.JDBC_DRIVER);
			connection = DriverManager.getConnection(Constants.LOG_DB_URL, Constants.ALTER_DB_USER,
					Constants.ALTER_DB_PASSWORD);
		} catch (ClassNotFoundException | SQLException ex) {
			logger.error("createConnection(): " + ex);
		}
		return connection;
	}

	private void closeConnection(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error("", e);
			}
		}
	}

	private void change_table() {
		Calendar end = Calendar.getInstance();
		end.add(Calendar.MONTH, -1);
		String sql = "RENAME TABLE smsc_out TO smsc_out_" + new SimpleDateFormat("MM_yyyy").format(end.getTime());
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = getConnection();
			statement = connection.prepareStatement(sql);
			statement.executeUpdate();
			statement.close();
			logger.info("<-- smsc_out table renamed --> ");
			sql = "CREATE TABLE IF NOT EXISTS smsc_out (" + "`id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
					+ "`msg_id` bigint(20) NOT NULL," + "`smsc` varchar(10) NOT NULL,"
					+ "`time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," + "PRIMARY KEY (`id`),"
					+ "KEY `msg_id` (`msg_id`)) ENGINE=MyISAM DEFAULT CHARSET=utf8;";
			statement = connection.prepareStatement(sql);
			statement.executeUpdate();
			logger.info("<-- Empty smsc_out table created --> ");
		} catch (Exception ex) {
			logger.error("createSmscOutTable() ", ex);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
				}
			}
			closeConnection(connection);
		}
	}

	public void stop() {
		logger.info("SmscSubmitLog Thread Stopping");
		stop = true;
	}
}
