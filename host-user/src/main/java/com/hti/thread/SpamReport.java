package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.ReportLogObject;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;

public class SpamReport implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	Connection connection = null;
	PreparedStatement statement = null;
	String sql = null;
	private boolean stop;

	public SpamReport() {
		logger.info("SpamReport Thread Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			if (GlobalQueue.spamReportQueue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			} else {
				sql = "insert ignore into report_spam(msg_id,username,smsc,oprCountry,cost,time,sender,destination,profile_id,remarks) values(?,?,?,?,?,?,?,?,?,?)";
				int count = 0;
				ReportLogObject log = null;
				try {
					connection = GlobalCache.connection_pool_proc.getConnection();
					statement = connection.prepareStatement(sql);
					connection.setAutoCommit(false);
					while (!GlobalQueue.spamReportQueue.isEmpty()) {
						log = (ReportLogObject) GlobalQueue.spamReportQueue.dequeue();
						statement.setString(1, log.getMsgid());
						statement.setString(2, log.getUsername());
						statement.setString(3, log.getSmsc());
						statement.setString(4, log.getOprCountry());
						statement.setString(5, log.getCost());
						statement.setString(6, log.getTime());
						statement.setString(7, log.getSenderid());
						statement.setString(8, log.getDestination());
						statement.setInt(9, log.getProfileId());
						statement.setString(10, log.getRemarks());
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
		logger.info("Spam Report Thread Stopped.Queue: " + GlobalQueue.spamReportQueue.size());
	}

	public void stop() {
		logger.info("Spam Report Thread Stopping.Queue: " + GlobalQueue.spamReportQueue.size());
		stop = true;
	}
}
