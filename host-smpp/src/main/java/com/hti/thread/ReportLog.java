package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.ReportLogObject;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;

public class ReportLog implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	Connection connection = null;
	PreparedStatement statement = null;
	String sql = null;
	private boolean stop;

	public ReportLog() {
		logger.info("ReportLog Thread Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			if (GlobalQueue.reportLogQueue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			} else {
				sql = "update report_log set status=? where msg_id =?";
				int count = 0;
				try {
					connection = GlobalCache.connnection_pool_1.getConnection();
					String status = null;
					statement = connection.prepareStatement(sql);
					connection.setAutoCommit(false);
					ReportLogObject log = null;
					while (!GlobalQueue.reportLogQueue.isEmpty()) {
						// add = false;
						log = (ReportLogObject) GlobalQueue.reportLogQueue.dequeue();
						status = log.getStatus();
						if (status.length() > 0) {
							if (status.length() > 8) {
								status = status.substring(0, 8);
							}
						}
						statement.setString(1, status);
						statement.setString(2, log.getMsgid());
						statement.addBatch();
						if (++count >= 1000) {
							break;
						}
					}
					if (count > 0) {
						int[] rowup = statement.executeBatch();
						connection.commit();
						count = rowup.length;
						logger.debug("Report Log Updated:-> " + count);
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
		logger.info("ReportLog Thread Stopped.Queue: " + GlobalQueue.reportLogQueue.size());
	}

	public void stop() {
		logger.info("ReportLog Thread Stopping");
		stop = true;
	}
}
