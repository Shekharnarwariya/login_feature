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

import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.hti.objects.DeliverLogObject;

public class DeliverLogInsert implements Runnable {
	private Connection connection = null;
	private PreparedStatement statement = null;
	private String sql = null;
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	public static boolean DELETE_OLD_RECORDS;
	private boolean stop;
	private int HOUR = 0;

	public DeliverLogInsert() {
		logger.info("DeliverLogInsert Thread Starting ");
		sql = "insert into smsc_dlr (response_id,pdu,smsc,received_on,source,destination) values(?,?,?,?,?,?)";
	}

	public void run() {
		while (!stop) {
			if (DELETE_OLD_RECORDS) {
				Connection log_connection = null;
				Statement log_statement = null;
				HOUR = HOUR + 1;
				String receivedOn = null;
				if (HOUR <= 23) {
					Calendar calendar = Calendar.getInstance();
					calendar.add(Calendar.DATE, -1);
					calendar.set(Calendar.HOUR_OF_DAY, HOUR);
					receivedOn = new SimpleDateFormat("yyyy-MM-dd HH").format(calendar.getTime());
					receivedOn += ":00:00";
				} else {
					receivedOn = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
					receivedOn += " 00:00:00";
					DELETE_OLD_RECORDS = false;
					HOUR = 0;
				}
				String log_sql = "insert ignore into smsc_dlr_log select * from smsc_dlr where received_on < '"
						+ receivedOn + "'";
				logger.info(log_sql);
				try {
					log_connection = GlobalCache.connnection_pool_1.getConnection();
					log_statement = log_connection.createStatement();
					int log_insert_counter = log_statement.executeUpdate(log_sql);
					logger.info("Records Inserted From smsc_dlr: " + log_insert_counter);
					log_sql = "delete from smsc_dlr where received_on < '" + receivedOn + "' LIMIT 25000";
					logger.info(log_sql);
					int delete_counter = 0;
					do {
						delete_counter = log_statement.executeUpdate(log_sql);
						logger.info("Records Deleted From smsc_dlr: " + delete_counter);
					} while (delete_counter == 25000);
				} catch (Exception e) {
					logger.error("insertLog()", e.fillInStackTrace());
				} finally {
					if (log_statement != null) {
						try {
							log_statement.close();
						} catch (SQLException e) {
						}
					}
					GlobalCache.connnection_pool_1.putConnection(log_connection);
				}
				if (!DELETE_OLD_RECORDS) {
					logger.info("Shifted from smsc_dlr to Log.Queue: " + GlobalQueue.DeliverLogQueue.size());
					Calendar calendar = Calendar.getInstance();
					calendar.add(Calendar.DATE, -30);
					receivedOn = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
					receivedOn += " 00:00:00";
					log_sql = "delete from smsc_dlr_log where received_on < '" + receivedOn + "' LIMIT 25000";
					logger.info(log_sql);
					try {
						log_connection = GlobalCache.connnection_pool_1.getConnection();
						log_statement = log_connection.createStatement();
						int delete_counter = 0;
						do {
							delete_counter = log_statement.executeUpdate(log_sql);
							logger.info("Records Deleted From smsc_dlr_log: " + delete_counter);
						} while (delete_counter == 25000);
					} catch (Exception e) {
						logger.error("deleteLog()", e.fillInStackTrace());
					} finally {
						if (log_statement != null) {
							try {
								log_statement.close();
							} catch (SQLException e) {
							}
						}
						GlobalCache.connnection_pool_1.putConnection(log_connection);
					}
				}
			}
			if (!GlobalQueue.DeliverLogQueue.isEmpty()) {
				DeliverLogObject deliverSM = null;
				int count = 0;
				try {
					connection = GlobalCache.connnection_pool_1.getConnection();
					statement = connection.prepareStatement(sql);
					connection.setAutoCommit(false);
					while (!GlobalQueue.DeliverLogQueue.isEmpty()) {
						try {
							deliverSM = (DeliverLogObject) GlobalQueue.DeliverLogQueue.dequeue();
							statement.setString(1, deliverSM.getResponseId());
							statement.setString(2, deliverSM.getShortMessage());
							statement.setString(3, deliverSM.getSmsc());
							statement.setString(4,
									new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(deliverSM.getReceivedOn()));
							statement.setString(5, deliverSM.getSource());
							statement.setString(6, deliverSM.getDestination());
							statement.addBatch();
							if (++count >= 1000) {
								break;
							}
						} catch (SQLException sqle) {
							logger.error("", sqle.fillInStackTrace());
						} catch (Exception e) {
							logger.error("", e.fillInStackTrace());
						}
					}
					statement.executeBatch();
					connection.commit();
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
					GlobalCache.connnection_pool_1.putConnection(connection);
					connection = null;
				}
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			}
		}
		logger.info("DeliverLogInsert Thread Stopped.Queue: " + GlobalQueue.DeliverLogQueue.size());
	}

	private void insertLog() {
	}

	public void stop() {
		logger.info("DeliverLogInsert Thread Stopping.Queue: " + GlobalQueue.DeliverLogQueue.size());
		stop = true;
	}
}
