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

public class DNDEntryLog implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private Connection connection = null;
	private PreparedStatement statement = null;
	private String sql = null;
	private boolean stop;

	public DNDEntryLog() {
		logger.info("DNDEntryLog Thread starting");
	}

	@Override
	public void run() {
		while (!stop) {
			if (GlobalQueue.dndLogQueue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			} else {
				sql = "insert into " + Constants.LOG_DB
						+ ".dnd_entry_log(msg_id,smsc,source,destination,time) values(?,?,?,?,?) ON DUPLICATE KEY update dlr_time = ?";
				int count = 0;
				try {
					connection = GlobalCache.connnection_pool_1.getConnection();
					statement = connection.prepareStatement(sql);
					connection.setAutoCommit(false);
					SubmittedObj log = null;
					while (!GlobalQueue.dndLogQueue.isEmpty()) {
						// add = false;
						log = (SubmittedObj) GlobalQueue.dndLogQueue.dequeue();
						statement.setString(1, log.getMsgId());
						statement.setString(2, log.getSmsc());
						statement.setString(3, log.getSource());
						statement.setString(4, log.getDestination());
						statement.setString(5, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(log.getSubmitTime()));
						if (log.getDeliverTime() != null) {
							statement.setString(6, log.getDeliverTime());
						} else {
							statement.setString(6, "");
						}
						statement.addBatch();
						if (++count >= 1000) {
							break;
						}
					}
					if (count > 0) {
						int[] rowup = statement.executeBatch();
						connection.commit();
						count = rowup.length;
						logger.debug("DND Entry Log Counter:-> " + count);
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
		logger.info("DNDEntryLog Thread Stopped. Queue: " + GlobalQueue.dndLogQueue.size());
	}

	public void stop() {
		logger.info("DNDEntryLog Thread Stopping");
		stop = true;
	}
}
