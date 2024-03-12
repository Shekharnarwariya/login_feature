/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.AlertDTO;
import com.hti.util.FlagStatus;
import com.hti.util.GlobalCache;

/**
 *
 * @author Administrator
 */
public class AlertHandler implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private List<AlertDTO> list = new ArrayList<AlertDTO>();
	private Map<Integer, PerformAlert> runningAlerts = new HashMap<Integer, PerformAlert>();
	private boolean stop;
	public static String PERFORM_ALERT_FLAG = FlagStatus.DEFAULT;

	public AlertHandler() {
		logger.info("AlertHandler Starting");
		initialize();
	}

	private void initialize() {
		getAlerts();
		startAlerts();
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				if (PERFORM_ALERT_FLAG.equalsIgnoreCase(FlagStatus.BLOCKED)) {
					logger.info("***** Stopping Alerts *******");
					stopAlerts(); // Stop Alerts
				} else {
					if (PERFORM_ALERT_FLAG.equalsIgnoreCase(FlagStatus.REFRESH)) {
						logger.info("***** Alert Refreshed *******");
						initialize();
					} else if (PERFORM_ALERT_FLAG.equalsIgnoreCase(FlagStatus.REMOVED)) // remove deleted alert
					{
						logger.info("***** Command To Removed Alert *******");
						removeAlert();
					}
				}
				PERFORM_ALERT_FLAG = FlagStatus.DEFAULT;
			} catch (Exception ex) {
				logger.error("", ex);
			}
			try {
				Thread.sleep(30 * 1000);
			} catch (InterruptedException ex) {
			}
		}
		logger.info("AlertHandler Stopped");
	}

	private void stopAlerts() {
		runningAlerts.forEach((k, v) -> {
			((PerformAlert) v).stop();
		});
		runningAlerts.clear();
		list.clear();
		GlobalCache.WorstDeliveryRoute.clear();
		GlobalCache.WorstResponseRoute.clear();
	}

	private void removeAlert() {
		logger.debug("Total Running Alerts: " + runningAlerts.size());
		List<Integer> alert_list = getAlertList();
		Iterator<Map.Entry<Integer, PerformAlert>> itr = runningAlerts.entrySet().iterator();
		Map.Entry<Integer, PerformAlert> entry;
		while (itr.hasNext()) {
			entry = itr.next();
			if (!alert_list.contains(entry.getKey())) {
				logger.info(" ** Removing Alert[" + entry.getKey() + "] ** ");
				(entry.getValue()).stop();
				itr.remove();
			}
		}
		logger.debug("After Remove Running Alerts: " + runningAlerts.size());
	}

	public void stop() {
		logger.info("AlertHandler Stopping");
		stopAlerts();
	}

	private void startAlerts() {
		for (AlertDTO alert : list) {
			if (!runningAlerts.containsKey(alert.getId())) {
				// Start new Thread
				PerformAlert performAlert = new PerformAlert(alert);
				Thread thread = new Thread(performAlert, "PerformanceAlert[" + alert.getId() + "]");
				performAlert.setThread(thread);
				thread.start();
				runningAlerts.put(alert.getId(), performAlert);
			} else {
				// Update Existing Thread
				logger.debug("Modifying Existing " + alert.getStatus() + " Alert[" + alert.getId() + "]");
				((PerformAlert) runningAlerts.get(alert.getId())).setAlert(alert);
			}
		}
	}

	private void getAlerts() {
		list.clear();
		String sql = "select * from perform_alert";
		PreparedStatement statement = null;
		ResultSet rs = null;
		Connection connection = null;
		AlertDTO alert = null;
		try {
			connection = GlobalCache.connnection_pool_1.getConnection();
			statement = connection.prepareStatement(sql);
			rs = statement.executeQuery();
			while (rs.next()) {
				if (rs.getInt("duration") > 0 && rs.getInt("percent") > 0) {
					alert = new AlertDTO(rs.getInt("id"), rs.getString("smsc"), rs.getString("country"),
							rs.getInt("duration"), rs.getInt("percent"), rs.getString("status"), rs.getString("email"),
							rs.getString("number"), rs.getInt("min_submit"), rs.getBoolean("hold_traffic"),
							rs.getString("sender"));
					alert.setRemarks(rs.getString("remarks"));
					list.add(alert);
				}
			}
		} catch (Exception ex) {
			logger.error(ex + " While Getting performance Alert Data");
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception ex) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (Exception ex) {
				}
			}
			GlobalCache.connnection_pool_1.putConnection(connection);
		}
		logger.info("Total Alerts Configured: " + list.size());
	}

	private List<Integer> getAlertList() {
		List<Integer> alert_list = new ArrayList<Integer>();
		String sql = "select id from perform_alert";
		PreparedStatement statement = null;
		ResultSet rs = null;
		Connection connection = null;
		try {
			connection = GlobalCache.connnection_pool_1.getConnection();
			statement = connection.prepareStatement(sql);
			rs = statement.executeQuery();
			while (rs.next()) {
				alert_list.add(rs.getInt("id"));
			}
		} catch (Exception ex) {
			logger.error(ex + " While Getting performance Alert Data");
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception ex) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (Exception ex) {
				}
			}
			GlobalCache.connnection_pool_1.putConnection(connection);
		}
		logger.info("Total Alerts Configured: " + alert_list.size());
		return alert_list;
	}
}
