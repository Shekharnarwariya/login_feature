/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.ReportLogObject;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;

/**
 * @author Administrator
 */
public class Report implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	Connection connection = null;
	PreparedStatement statement = null;
	String sql = null;
	Map<Integer, String> tempMap = new HashMap<Integer, String>();
	Iterator<Map.Entry<String, Long>> itr;
	Map.Entry<String, Long> entry;
	private boolean stop;
	ReportLog reportLog;
	private Map<String, ReportLogObject> preparedQueue = new HashMap<String, ReportLogObject>();

	public Report() {
		logger.info("Report Thread Starting");
		reportLog = new ReportLog();
		new Thread(reportLog, "ReportLog").start();
	}

	@Override
	public void run() {
		while (!stop) {
			if (GlobalQueue.reportUpdateQueue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			} else {
				// logger.debug("reportUpdateQueue: " + GlobalQueue.reportUpdateQueue.size());
				ReportLogObject log = null;
				int count = 0;
				while (!GlobalQueue.reportUpdateQueue.isEmpty()) {
					log = (ReportLogObject) GlobalQueue.reportUpdateQueue.dequeue();
					preparedQueue.put(log.getMsgid(), log);
					if (++count > 1000) {
						break;
					}
				}
				// logger.info("reportUpdateQueue: " + GlobalQueue.reportUpdateQueue.size() + " PreparedQueue: "
				// + preparedQueue);
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
				}
				sql = "update report set status=?,smsc=?,sender=? where msg_id =?";
				count = 0;
				try {
					connection = GlobalCache.connnection_pool_2.getConnection();
					statement = connection.prepareStatement(sql);
					connection.setAutoCommit(false);
					// String msg_id = null;
					String status = null;
					for (ReportLogObject report_log : preparedQueue.values()) {
						status = report_log.getStatus();
						if (status != null && status.length() > 0) {
							if (status.length() > 8) {
								status = status.substring(0, 8);
							}
							statement.setString(1, status);
							statement.setString(2, report_log.getSmsc());
							statement.setString(3, report_log.getSenderid());
							statement.setString(4, report_log.getMsgid());
							statement.addBatch();
							tempMap.put(count, report_log.getMsgid());
							count++;
						}
					}
					if (count > 0) {
						int[] rowup = statement.executeBatch();
						connection.commit();
						count = 0;
						String msg_id = null;
						for (int i = 0; i < rowup.length; i++) {
							msg_id = tempMap.remove(i);
							if (rowup[i] < 1) {
								GlobalQueue.reportLogQueue.enqueue(preparedQueue.get(msg_id));
							} else {
								count++;
							}
						}
						logger.debug("Report updation Count: " + count);
					}
				} catch (java.sql.SQLException sqle) {
					logger.error("process()", sqle.fillInStackTrace());
				} catch (Exception e) {
					logger.error("process()", e.fillInStackTrace());
				} finally {
					tempMap.clear();
					preparedQueue.clear();
					if (statement != null) {
						try {
							statement.close();
						} catch (SQLException ex) {
							statement = null;
						}
					}
					GlobalCache.connnection_pool_2.putConnection(connection);
					connection = null;
				}
			}
		}
		reportLog.stop();
		logger.info("Report Thread Stopped." + "Queue: " + GlobalQueue.reportUpdateQueue.size());
	}

	public void stop() {
		stop = true;
		logger.info("Report Thread Stopping.Queue: " + GlobalQueue.reportUpdateQueue.size());
	}
}
