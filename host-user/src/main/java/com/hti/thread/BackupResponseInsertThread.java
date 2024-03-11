/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.logica.smpp.pdu.DeliverSM;

/**
 *
 * @author Amit_vish 30-AUG-2013
 */
public class BackupResponseInsertThread implements Runnable {
	private Connection connection = null;
	private PreparedStatement statement = null;
	private String sql = null;
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	public static boolean DELETE_OLD_RECORDS;
	private boolean stop;

	public BackupResponseInsertThread() {
		logger.info("BackupResponseInsert Thread Starting ");
		sql = "insert into backup_response (msg_id,client_name,pdu,flag,time,destination,source,server_id) values(?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE pdu=?,time=?";
	}

	public void run() {
		while (!stop) {
			DeliverSM deliverSM = null;
			String Systime = null;
			if (!GlobalQueue.DLRInsertQueue.isEmpty()) {
				int count = 0;
				try {
					connection = GlobalCache.connection_pool_proc.getConnection();
					statement = connection.prepareStatement(sql);
					connection.setAutoCommit(false);
					while (!GlobalQueue.DLRInsertQueue.isEmpty()) {
						try {
							deliverSM = (DeliverSM) GlobalQueue.DLRInsertQueue.remove(0);
							// messageId = deliverSM.getReceiptedMessageId();
							// client_name = deliverSM.getClientName();
							Systime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
							// --------------------------------------------------
							statement.setString(1, deliverSM.getReceiptedMessageId());
							statement.setString(2, deliverSM.getClientName());
							statement.setString(3, deliverSM.getShortMessage());
							statement.setString(4, "False");
							statement.setString(5, Systime);
							statement.setString(6, deliverSM.getDestAddr().getAddress());
							statement.setString(7, deliverSM.getSourceAddr().getAddress());
							statement.setInt(8, deliverSM.getServerId());
							statement.setObject(9, deliverSM.getShortMessage());
							statement.setString(10, Systime);
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
					GlobalCache.connection_pool_proc.putConnection(connection);
					connection = null;
				}
			} else {
				if (DELETE_OLD_RECORDS) {
					Calendar calendar = Calendar.getInstance();
					calendar.add(Calendar.DATE, -3);
					String max_msg_id = new SimpleDateFormat("yyMMdd").format(calendar.getTime());
					max_msg_id = max_msg_id + "0000000000000";
					String deletesql = "delete from backup_response where msg_id < " + max_msg_id + " limit 25000";
					logger.info(deletesql);
					try {
						connection = GlobalCache.connection_pool_proc.getConnection();
						statement = connection.prepareStatement(deletesql);
						int deletecount = statement.executeUpdate();
						logger.info("backup_response Deletion Count : " + deletecount);
						if (deletecount < 25000) {
							DELETE_OLD_RECORDS = false;
							logger.info("<-- No Records To Delete [backup_response] -->");
						}
					} catch (Exception ex) {
						logger.error("Delete Error: ", ex);
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
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			}
		}
		logger.info("BackupResponseInsert Thread Stopped.Queue: " + GlobalQueue.DLRInsertQueue.size());
	}

	public void stop() {
		logger.info("BackupResponseInsert Thread Stopping.Queue: " + GlobalQueue.DLRInsertQueue.size());
		stop = true;
	}
}
