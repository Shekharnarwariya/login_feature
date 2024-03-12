package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.SmOptParamEntry;
import com.hti.util.GlobalCache;
import com.logica.smpp.util.Queue;

public class SmOptParamInsert implements Runnable {
	private Logger logger = LoggerFactory.getLogger("dbLogger");
	private boolean keepRunning = true;
	private long waitForQueueInterval = 1000; // in ms
	public static Queue submitRequests = new Queue();
	private Connection connection = null;
	private PreparedStatement statement = null;
	private String sql = "insert ignore into sm_opt_param(msg_id,pe_id,template_id,tm_id,channel_type,caption) values(?,?,?,?,?,?)";
	public static boolean DELETE_OLD_RECORDS;

	public SmOptParamInsert() {
		logger.info("SmOptParamInsert Thread Starting");
	}

	@Override
	public void run() {
		while (keepRunning) {
			if (submitRequests.isEmpty()) {
				try {
					synchronized (submitRequests) {
						submitRequests.wait(waitForQueueInterval);
					}
				} catch (InterruptedException e) {
					// it's ok to be interrupted when waiting
				}
			} else {
				SmOptParamEntry entry = null;
				int counter = 0;
				String msg_id = null;
				try {
					connection = GlobalCache.connection_pool_proc.getConnection();
					statement = connection.prepareStatement(sql);
					connection.setAutoCommit(false);
					while (!submitRequests.isEmpty()) {
						entry = (SmOptParamEntry) submitRequests.dequeue();
						msg_id = entry.getMessageId();
						statement.setString(1, entry.getMessageId());
						statement.setObject(2, entry.getPeId());
						statement.setObject(3, entry.getTemplateId());
						statement.setObject(4, entry.getTelemarketerId());
						statement.setObject(5, entry.getChannelType());
						statement.setObject(6, entry.getCaption());
						statement.addBatch();
						if (++counter > 1000) {
							break;
						}
					}
					if (counter > 0) {
						statement.executeBatch();
						connection.commit();
					}
				} catch (Exception ex) {
					logger.error(msg_id, ex);
				} finally {
					if (statement != null) {
						try {
							statement.close();
						} catch (SQLException ex) {
							statement = null;
						}
					}
					if (connection != null) {
						GlobalCache.connection_pool_proc.putConnection(connection);
						connection = null;
					}
				}
			}
			if (DELETE_OLD_RECORDS) {
				logger.info("<-- Checking For Cleanup [sm_opt_param] -->");
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DATE, -3);
				String max_msg_id = new SimpleDateFormat("yyMMdd").format(calendar.getTime());
				max_msg_id = max_msg_id + "0000000000000";
				String deletesql = "delete from sm_opt_param where msg_id  < " + max_msg_id + " limit 25000";
				logger.info(deletesql);
				try {
					connection = GlobalCache.connection_pool_proc.getConnection();
					statement = connection.prepareStatement(deletesql);
					int deletecount = statement.executeUpdate();
					logger.info("sm_opt_param Deletion Count : " + deletecount);
					if (deletecount < 25000) {
						DELETE_OLD_RECORDS = false;
						logger.info("<-- No Records To Delete [sm_opt_param] -->");
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
		}
		logger.info("SmOptParamInsert Thread Stopped");
	}

	public void stop() {
		logger.info("SmOptParamInsert Thread Stopping");
		keepRunning = false;
	}
}
