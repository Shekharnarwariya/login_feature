/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.logica.smpp.pdu.DeliverSM;

/**
 *
 * @author Administrator
 */
public class BackupResponseLogThread implements Runnable {
	private Connection connection = null;
	private PreparedStatement statement = null;
	private String sql = null;
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private boolean stop;

	public BackupResponseLogThread() {
		logger.info("BackupResponseLog Thread Starting ");
		sql = "insert into backup_response_log (msg_id,client_name,pdu,flag,time,destination,source,server_id) values(?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE pdu=?,time=?";
	}

	@Override
	public void run() {
		while (!stop) {
			DeliverSM deliverSM = null;
			String Systime = null;
			if (!GlobalQueue.backupRespLogQueue.isEmpty()) {
				int count = 0;
				try {
					connection = GlobalCache.logConnectionPool.openConnection();
					statement = connection.prepareStatement(sql);
					connection.setAutoCommit(false);
					while (!GlobalQueue.backupRespLogQueue.isEmpty()) {
						try {
							deliverSM = (DeliverSM) GlobalQueue.backupRespLogQueue.remove(0);
							// messageId = deliverSM.getReceiptedMessageId();
							// client_name = deliverSM.getClientName();
							Systime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
							// --------------------------------------------------
							statement.setString(1, deliverSM.getReceiptedMessageId());
							statement.setString(2, deliverSM.getClientName());
							statement.setString(3, deliverSM.getShortMessage());
							statement.setString(4, "True");
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
					int[] rowup = statement.executeBatch();
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
					GlobalCache.logConnectionPool.releaseConnection(connection);
					connection = null;
				}
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			}
		}
		logger.info("BackupResponseLog Thread Stopped.Queue: " + GlobalQueue.backupRespLogQueue.size());
	}

	public void stop() {
		logger.info("BackupResponseLog Thread Stopping.Queue: " + GlobalQueue.backupRespLogQueue.size());
		stop = true;
	}
}
