/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.HTIQueue;
import com.hti.user.UserDeliverForward;
import com.hti.user.WebDeliverProcess;
import com.hti.user.dto.DlrSettingEntry;
import com.hti.util.FlagStatus;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;

/**
 *
 * @author Administrator
 */
public class DLRLoader implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private String FLAG = FlagStatus.DEFAULT;
	// private boolean stop;

	public DLRLoader(String FLAG) {
		this.FLAG = FLAG;
		logger.info("DLRLoader Thread Starting[" + FLAG + "]");
	}

	@Override
	public void run() {
		while (true) {
			try {
				if (FLAG.equalsIgnoreCase(FlagStatus.DEFAULT)) {
					checkDlrPendings();
				} else {
					if (FLAG.equalsIgnoreCase(FlagStatus.RESEND)) {
						updateBackupResponse();
					}
					// ---------------- initiating reload DLRs for Connected Receiver Users -------------
					for (String username : GlobalCache.UserRxObject.keySet()) {
						if (((UserDeliverForward) GlobalCache.UserRxObject.get(username)).isActive()) {
							((UserDeliverForward) GlobalCache.UserRxObject.get(username)).setReload(true);
						}
						try {
							Thread.sleep(500);
						} catch (InterruptedException ie) {
						}
					}
					// ---------------- initiating reload DLRs for Web Users -------------
					for (WebDeliverProcess webDeliverProcess : GlobalCache.UserWebObject.values()) {
						webDeliverProcess.setReload(true);
						try {
							Thread.sleep(500);
						} catch (InterruptedException ie) {
						}
					}
				}
			} catch (Exception e) {
				logger.error("", e.fillInStackTrace());
			}
			break;
		}
		logger.info("DLRLoader Thread Stopped[" + FLAG + "]");
	}

	public void checkDlrPendings() {
		List<String> pending_user_list = new ArrayList<String>();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		String sql = "select distinct(client_name) from backup_response where server_id=" + GlobalVars.SERVER_ID;
		try {
			connection = GlobalCache.connection_pool_user.getConnection();
			statement = connection.prepareStatement(sql);
			rs = statement.executeQuery();
			while (rs.next()) {
				pending_user_list.add(rs.getString("client_name"));
			}
		} catch (Exception q) {
			logger.error("", q.fillInStackTrace());
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException sqle) {
			}
			GlobalCache.connection_pool_user.putConnection(connection);
		}
		// ---------- Check web Dlrs Users ------------
		if (!pending_user_list.isEmpty()) {
			for (String system_id : pending_user_list) {
				if (!GlobalCache.BlockedUser.contains(system_id)) {
					DlrSettingEntry entry = GlobalVars.userService.getDlrSettingEntry(system_id);
					// System.out.println(system_id + " dlrEntry: " + entry);
					if (entry != null && entry.isWebDlr()) {
						boolean web_dlr_param = false;
						if (entry.getWebDlrParam() != null && entry.getWebDlrParam().length() > 0) {
							web_dlr_param = true;
						}
						if (!GlobalCache.WebDeliverProcessQueue.containsKey(system_id)) {
							HTIQueue dlrQueue = new HTIQueue();
							GlobalCache.WebDeliverProcessQueue.put(system_id, dlrQueue);
							if (!GlobalCache.UserWebObject.containsKey(system_id)) {
								WebDeliverProcess webDeliverForward = new WebDeliverProcess(system_id,
										entry.getWebUrl(), dlrQueue, true, web_dlr_param);
								GlobalCache.UserWebObject.put(system_id, webDeliverForward);
								new Thread(webDeliverForward, system_id + "_WebDeliverForward").start();
							}
						}
					}
				}
			}
		}
	}

	private void updateBackupResponse() {
		logger.info("updating BackupResponse For Dlr Process");
		Connection connection = null;
		String sql = null;
		Statement statement = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		List<String> list = null;
		try {
			list = new ArrayList<String>();
			connection = GlobalCache.connection_pool_proc.getConnection();
			if (connection != null) {
				sql = "select msg_id from mis_table where s_flag='R'";
				statement = connection.createStatement();
				rs = statement.executeQuery(sql);
				while (rs.next()) {
					list.add(rs.getString("msg_id"));
				}
				rs.close();
				while (!list.isEmpty()) {
					List<String> part_list = new ArrayList<String>();
					while (!list.isEmpty()) {
						part_list.add(list.remove(0));
						if (part_list.size() > 300) {
							break;
						}
					}
					if (!part_list.isEmpty()) {
						sql = "insert ignore into backup_response select * from host_zlog.backup_response_log where msg_id in("
								+ String.join(",", part_list) + ")";
						preparedStatement = connection.prepareStatement(sql);
						int updateCount = preparedStatement.executeUpdate();
						logger.info("log to backup_response insert count: " + updateCount);
						// System.out.println(">>>> Flag Updated Count :" + updateCount);
						sql = "update mis_table set s_flag='True' where msg_id in(" + String.join(",", part_list)
								+ ") and s_flag='R'";
						preparedStatement = connection.prepareStatement(sql);
						updateCount = preparedStatement.executeUpdate();
						// System.out.println(">>>> Flag Updated Count(MIS) :" + updateCount);
					}
				}
			}
		} catch (Exception e) {
			logger.error("updateBackupResponse()", e);
		} finally {
			GlobalCache.connection_pool_proc.putConnection(connection);
			try {
				if (statement != null) {
					statement.close();
				}
				if (preparedStatement != null) {
					preparedStatement.close();
				}
			} catch (SQLException sqle) {
			}
		}
		logger.info("End update BackupResponse For Dlr Process");
	}
}
