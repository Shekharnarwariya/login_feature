package com.hti.thread;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.RequestLog;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;
import com.logica.smpp.Data;

public class RequestLogInsert implements Runnable {
	Connection connection;
	PreparedStatement statement;
	String sql;
	private Logger logger = LoggerFactory.getLogger("dbLogger");
	public static boolean RENAME_TABLE;
	private boolean stop;
	private int total_insert_count = 0;

	public RequestLogInsert() {
		logger.info("RequestLogInsert Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			if (RENAME_TABLE) {
				RENAME_TABLE = false;
				change_table();
			}
			if (GlobalQueue.request_log_Queue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			} else {
				logger.debug("RequestLogQueue: " + GlobalQueue.request_log_Queue.size());
				int counter = 0;
				sql = "insert ignore into request_log(msg_id,seq_no,content,destination,source,registered,esm,"
						+ "dcs,username,ip_address) " + "values(?,?,?,?,?,?,?,?,?,?)";
				String msgid = null;
				RequestLog request_log_object;
				try {
					connection = GlobalCache.logConnectionPool.openConnection();
					statement = connection.prepareStatement(sql);
					connection.setAutoCommit(false);
					while (!GlobalQueue.request_log_Queue.isEmpty()) {
						request_log_object = (RequestLog) GlobalQueue.request_log_Queue.dequeue();
						if (request_log_object != null) {
							// boolean optional_param = false;
							msgid = request_log_object.getMsgId();
							try {
								statement.setString(1, msgid);
								statement.setInt(2, request_log_object.getSequence());
								statement.setString(3, request_log_object.getContent());
								statement.setString(4, request_log_object.getDestination());
								statement.setString(5, request_log_object.getSource());
								statement.setInt(6, request_log_object.getRegistered());
								statement.setInt(7, request_log_object.getEsm());
								statement.setInt(8, request_log_object.getDcs());
								statement.setString(9, request_log_object.getSystemId());
								statement.setString(10, request_log_object.getIpAddress());
								statement.addBatch();
								++total_insert_count;
								if (++counter > 1000) {
									logger.debug("RequestLog Insert: " + total_insert_count);
									break;
								}
							} catch (java.sql.SQLException se) {
								logger.error("MsgID: " + msgid, se);
							}
						}
						request_log_object = null;
					}
					statement.executeBatch();
					connection.commit();
				} catch (Exception se) {
					logger.error("", se.fillInStackTrace());
				} finally {
					GlobalCache.logConnectionPool.releaseConnection(connection);
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
		logger.info("RequestLogInsert Thread Stopped.Queue: " + GlobalQueue.request_log_Queue.size());
	}

	private void change_table() {
		Calendar end = Calendar.getInstance();
		end.add(Calendar.MONTH, -1);
		String sql = "RENAME TABLE request_log TO request_log_" + new SimpleDateFormat("MM_yyyy").format(end.getTime());
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = getConnection();
			statement = connection.prepareStatement(sql);
			statement.executeUpdate();
			statement.close();
			logger.info("<-- request_log table renamed --> ");
			sql = "CREATE TABLE IF NOT EXISTS request_log"
					+ "(`msg_id` bigint(20) unsigned NOT NULL DEFAULT '0',  `seq_no` int(10) DEFAULT '0',  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,"
					+ "`destination` bigint(15) DEFAULT NULL,  `source` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,  `registered` tinyint(1) unsigned NOT NULL DEFAULT '0',"
					+ "`esm` int(3) unsigned DEFAULT '0',  `dcs` int(3) unsigned DEFAULT '0',  `username` varchar(15) DEFAULT NULL,ip_address varchar(50) DEFAULT NULL,"
					+ " PRIMARY KEY (`msg_id`)) ENGINE=MyISAM DEFAULT CHARSET=utf8;";
			statement = connection.prepareStatement(sql);
			statement.executeUpdate();
			logger.info("<-- Empty request_log table created --> ");
		} catch (Exception ex) {
			logger.error("createRequestLogTable() ", ex);
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

	private Connection getConnection() {
		logger.info("createConnection()");
		Connection connection = null;
		try {
			Class.forName(GlobalVars.JDBC_DRIVER);
			connection = DriverManager.getConnection(GlobalVars.LOG_DB_URL, GlobalVars.ALTER_DB_USER,
					GlobalVars.ALTER_DB_PASSWORD);
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

	public void stop() {
		logger.info("RequestLogInsert Stopping.Queue: " + GlobalQueue.request_log_Queue.size());
		stop = true;
	}
}
