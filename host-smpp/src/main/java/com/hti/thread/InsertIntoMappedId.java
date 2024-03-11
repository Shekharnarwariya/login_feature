/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.LogPDU;
import com.hti.util.FileUtil;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;

/**
 *
 * @author Administrator
 */
public class InsertIntoMappedId implements Runnable {
	private Connection connection = null;
	private PreparedStatement statement = null;
	private String sql = null;
	private int count = 0;
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private boolean stop;

	public InsertIntoMappedId() {
		logger.info("InsertIntoMappedId Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			if (GlobalQueue.MAPPED_ID_QUEUE.isEmpty()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			} else {
				count = 0;
				sql = "insert ignore into mapped_id(response_id,client_name,msg_id,route_to_SMSC,time,cost,refund,source,destination,server_id) values(?,?,?,?,?,?,?,?,?,?)";
				// String response_id = null, client_name = null, msg_id = null, smsc = null;
				try {
					connection = GlobalCache.connnection_pool_2.getConnection();
					statement = connection.prepareStatement(sql);
					connection.setAutoCommit(false);
					LogPDU log_pdu = null;
					while (!GlobalQueue.MAPPED_ID_QUEUE.isEmpty()) {
						try {
							log_pdu = (LogPDU) GlobalQueue.MAPPED_ID_QUEUE.dequeue();
							if (log_pdu != null) {
								statement.setString(1, log_pdu.getResponseid());
								statement.setString(2, log_pdu.getUsername());
								statement.setString(3, log_pdu.getMsgid());
								statement.setString(4, log_pdu.getRoute());
								statement.setString(5, log_pdu.getSubmitOn());
								statement.setDouble(6, log_pdu.getCost());
								statement.setBoolean(7, log_pdu.isRefund());
								statement.setString(8, log_pdu.getSource());
								statement.setString(9, log_pdu.getDestination());
								statement.setInt(10, log_pdu.getServerId());
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
					// Do null all the variables
					// tokenizer = null;
					connection = null;
					// mapped_id_data = null;
				}
			}
		}
		logger.info("InsertIntoMappedId Stopped.Queue: " + GlobalQueue.MAPPED_ID_QUEUE.size());
		if (!GlobalQueue.MAPPED_ID_QUEUE.isEmpty()) {
			try {
				FileUtil.writeObject("backup//MappedIdQueue.mapped", GlobalQueue.MAPPED_ID_QUEUE);
			} catch (Exception ex) {
				logger.error(ex + " While Writing MappedIdQueue Object -> ");
			}
		}
	}

	public void stop() {
		logger.info("InsertIntoMappedId Stopping.Queue: " + GlobalQueue.MAPPED_ID_QUEUE.size());
		stop = true;
	}
}
