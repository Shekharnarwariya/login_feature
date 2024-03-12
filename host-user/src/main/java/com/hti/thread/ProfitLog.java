package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.ProfitLogObject;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;

public class ProfitLog implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	Connection connection = null;
	PreparedStatement statement = null;
	String sql = null;
	public static boolean SHIFT_OLD_RECORDS = false;
	private boolean stop;

	public ProfitLog() {
		logger.info("ProfitLog Thread Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			if (GlobalQueue.profitReportQueue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			} else {
				// logger.info("profitReportQueue: " + GlobalQueue.profitReportQueue.size());
				sql = "insert ignore into profit_report(msg_id,network_id,reseller_id,user_id,purchase_cost,selling_cost,wallet,admindepend) values(?,?,?,?,?,?,?,?)";
				int count = 0;
				ProfitLogObject log = null;
				try {
					connection = GlobalCache.connection_pool_proc.getConnection();
					statement = connection.prepareStatement(sql);
					connection.setAutoCommit(false);
					while (!GlobalQueue.profitReportQueue.isEmpty()) {
						log = (ProfitLogObject) GlobalQueue.profitReportQueue.dequeue();
						statement.setString(1, log.getMsgId());
						statement.setInt(2, log.getNetworkId());
						statement.setInt(3, log.getResellerId());
						statement.setInt(4, log.getUserId());
						statement.setDouble(5, log.getPurchaseCost());
						statement.setDouble(6, log.getSellingCost());
						statement.setBoolean(7, log.isWallet());
						statement.setBoolean(8, log.isAdminDepend());
						statement.addBatch();
						log = null;
						if (++count >= 100) {
							break;
						}
					}
					if (count > 0) {
						statement.executeBatch();
						connection.commit();
						logger.debug("Profit Report Update Counter: " + count);
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
		logger.info("ProfitLog Thread Stopped.Queue: " + GlobalQueue.profitReportQueue.size());
	}

	public void stop() {
		logger.info("ProfitLog Thread Stopping.Queue: " + GlobalQueue.profitReportQueue.size());
		stop = true;
	}
}
