/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.ReportLogObject;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;

/**
 * @author Administrator
 */
public class ReportLog implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	Connection connection = null;
	PreparedStatement statement = null;
	String sql = null;
	public static boolean SHIFT_OLD_RECORDS = false;
	private boolean stop;
	private int HOUR = 0;

	public ReportLog() {
		logger.info("ReportLog Thread Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			if (SHIFT_OLD_RECORDS) {
				logger.info("Shifting from Report to Log.Queue: " + GlobalQueue.reportLogQueue.size());
				Connection log_connection = null;
				Statement log_statement = null;
				String max_msg_id = null;
				HOUR = HOUR + 1;
				if (HOUR <= 23) {
					Calendar calendar = Calendar.getInstance();
					calendar.add(Calendar.DATE, -1);
					calendar.set(Calendar.HOUR_OF_DAY, HOUR);
					max_msg_id = new SimpleDateFormat("yyMMddHH").format(calendar.getTime());
					max_msg_id = max_msg_id + "00000000000";
				} else {
					max_msg_id = new SimpleDateFormat("yyMMdd").format(new Date());
					max_msg_id = max_msg_id + "0000000000000";
					SHIFT_OLD_RECORDS = false;
					HOUR = 0;
				}
				String log_sql = "insert ignore into report_log(msg_id,username,smsc,oprCountry,cost,status,time,sender) select msg_id,username,smsc,oprCountry,cost,status,date(time),sender from report where msg_id < "
						+ max_msg_id + "";
				logger.info(log_sql);
				try {
					log_connection = GlobalCache.connection_pool_proc.getConnection();
					log_statement = log_connection.createStatement();
					int log_insert_counter = log_statement.executeUpdate(log_sql);
					logger.info("Records Inserted From Report: " + log_insert_counter);
					log_sql = "delete from report where msg_id < " + max_msg_id + " limit 25000";
					logger.info(log_sql);
					int delete_counter = 0;
					do {
						delete_counter = log_statement.executeUpdate(log_sql);
						logger.info("Records Deleted From Report: " + delete_counter);
					} while (delete_counter == 25000);
				} catch (Exception e) {
					logger.error("insertLog", e.fillInStackTrace());
				} finally {
					if (log_statement != null) {
						try {
							log_statement.close();
						} catch (SQLException e) {
						}
					}
					GlobalCache.connection_pool_proc.putConnection(log_connection);
				}
				logger.info("Shifted from Report to Log.Queue: " + GlobalQueue.reportLogQueue.size());
				if (!SHIFT_OLD_RECORDS) {
					try {
						log_connection = GlobalCache.connection_pool_proc.getConnection();
						log_statement = log_connection.createStatement();
						log_sql = "delete from report_summary where time> DATE_SUB(NOW(), INTERVAL 3 DAY) and time< DATE_SUB(NOW(), INTERVAL 30 DAY)";
						logger.info(log_sql);
						int delete_counter = log_statement.executeUpdate(log_sql);
						logger.info("Records Deleted from ReportSummmary: " + delete_counter);
						log_sql = "insert into report_summary(total,time,username,smsc,oprCountry,sender,status,cost) select t.count,t.time,t.username,t.smsc,t.oprCountry,t.sender,t.status,t.cost from (select count(msg_id) as count,time,username,smsc,oprCountry,sender,IFNULL(status, 'Q') as status,cost from report_log group by time,username,smsc,oprCountry,sender,status,cost)AS t ON DUPLICATE KEY UPDATE total=t.count";
						logger.info(log_sql);
						int log_insert_counter = log_statement.executeUpdate(log_sql);
						logger.info("Records Inserted To ReportSummmary: " + log_insert_counter);
						Calendar calendar = Calendar.getInstance();
						calendar.add(Calendar.DATE, -3);
						max_msg_id = new SimpleDateFormat("yyMMdd").format(calendar.getTime());
						max_msg_id = max_msg_id + "0000000000000";
						log_sql = "delete from report_log where msg_id < " + max_msg_id + " limit 50000";
						logger.info(log_sql);
						do {
							delete_counter = log_statement.executeUpdate(log_sql);
							logger.info("Records Deleted From ReportLog: " + delete_counter);
						} while (delete_counter == 50000);
					} catch (Exception e) {
						logger.error("", e.fillInStackTrace());
					} finally {
						if (log_statement != null) {
							try {
								log_statement.close();
							} catch (SQLException e) {
							}
						}
						GlobalCache.connection_pool_proc.putConnection(log_connection);
					}
				}
			}
			if (GlobalQueue.reportLogQueue.isEmpty()) {
				try {
					Thread.sleep(300);
				} catch (InterruptedException ex) {
				}
			} else {
				sql = "insert ignore into report(msg_id,username,smsc,oprCountry,cost,status,time,sender,destination) values(?,?,?,?,?,?,?,?,?)";
				int count = 0;
				ReportLogObject log = null;
				try {
					connection = GlobalCache.connection_pool_proc.getConnection();
					statement = connection.prepareStatement(sql);
					connection.setAutoCommit(false);
					while (!GlobalQueue.reportLogQueue.isEmpty()) {
						log = (ReportLogObject) GlobalQueue.reportLogQueue.dequeue();
						statement.setString(1, log.getMsgid());
						statement.setString(2, log.getUsername());
						statement.setString(3, log.getSmsc());
						statement.setString(4, log.getOprCountry());
						statement.setString(5, log.getCost());
						statement.setString(6, log.getStatus());
						statement.setString(7, log.getTime());
						statement.setString(8, log.getSenderid());
						statement.setString(9, log.getDestination());
						statement.addBatch();
						log = null;
						if (++count >= 100) {
							break;
						}
					}
					if (count > 0) {
						statement.executeBatch();
						connection.commit();
					}
				} catch (java.sql.SQLException sqle) {
					logger.error("", sqle.fillInStackTrace());
				} catch (Exception e) {
					logger.error("", e.fillInStackTrace());
				} finally {
					if (statement != null) {
						try {
							statement.close();
						} catch (SQLException ex) {
							statement = null;
						}
					}
					GlobalCache.connection_pool_proc.putConnection(connection);
					connection = null;
				}
			}
		}
		logger.info("ReportLog Thread Stopped.Queue: " + GlobalQueue.reportLogQueue.size());
	}

	private void insertLog() {
	}

	public void stop() {
		logger.info("ReportLog Thread Stopping.Queue: " + GlobalQueue.reportLogQueue.size());
		stop = true;
	}
}
