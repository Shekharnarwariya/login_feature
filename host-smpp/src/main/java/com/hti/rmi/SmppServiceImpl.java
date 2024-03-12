/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.thread.ResendSMS;
import com.hti.util.Constants;
import com.hti.util.GlobalCache;

/**
 * @author Administrator
 */
public class SmppServiceImpl extends UnicastRemoteObject implements SmppService {
	private Logger logger = LoggerFactory.getLogger(SmppServiceImpl.class);

	public SmppServiceImpl() throws RemoteException {
	}

	@Override
	public int resendCount(Map params) throws RemoteException {
		int to_be_resend = 0;
		logger.info("<- Resend Count Request via Web ->");
		logger.info("Params: " + params);
		String sql = "";
		// String sql_in = "select * from smsc_in where ";
		String start = null;
		String end = null;
		boolean msg_id_criteria = false;
		if (params.containsKey("fromMsgId") && params.containsKey("toMsgId")) {
			msg_id_criteria = true;
			start = (String) params.get("fromMsgId");
			end = (String) params.get("toMsgId");
		} else {
			start = (String) params.get("start");
			end = (String) params.get("end");
		}
		String username = null, sender = null, smsc = null, country = null;
		if (params.containsKey("username")) {
			username = (String) params.get("username");
		}
		if (params.containsKey("sender")) {
			sender = (String) params.get("sender");
		}
		if (params.containsKey("smsc")) {
			smsc = (String) params.get("smsc");
		}
		if (params.containsKey("oprCountry")) {
			country = (String) params.get("oprCountry");
		}
		if (params.containsKey("status")) {
			String status = (String) params.get("status");
			if (status.equalsIgnoreCase("NON_RESPOND") || status.equalsIgnoreCase("ZERO_COST")
					|| status.equalsIgnoreCase("ERR_RESPOND") || GlobalCache.EsmeErrorFlag.containsKey(status)) {// only smsc_in records
				sql = "select count(msg_id) from smsc_in where s_flag=";
				if (status.equalsIgnoreCase("NON_RESPOND")) {
					sql += "'F'";
				} else if (status.equalsIgnoreCase("ERR_RESPOND")) {
					sql += "'E'";
				} else if (status.equalsIgnoreCase("ZERO_COST")) {
					sql += "'M'";
				} else {
					String flag_symbol = (String) GlobalCache.EsmeErrorFlag.get(status);
					sql += "'" + flag_symbol + "'";
				}
				if (username != null) {
					sql += "and username='" + username + "' ";
				}
				if (smsc != null) {
					sql += "and smsc='" + smsc + "' ";
				}
				if (sender != null) {
					sql += "and source_no='" + sender + "' ";
				}
				if (country != null) {
					sql += "and oprCountry in(" + country + ") ";
				}
				if (msg_id_criteria) {
					if (start.equalsIgnoreCase(end)) {
						sql += "and msg_id='" + start + "' ";
					} else {
						sql += "and msg_id between " + start + " and " + end + "";
					}
				} else {
					if (start.equalsIgnoreCase(end)) {
						sql += "and time='" + start + "' ";
					} else {
						sql += "and time between '" + start + "' and '" + end + "'";
					}
				}
			} else {
				sql = "select count(msg_id) from " + Constants.LOG_DB + ".smsc_in_log where ";
				// ------------ mis ---------------------------
				String mis_sql = "select msg_id from mis_table where status='" + status + "'";
				if (username != null) {
					mis_sql += " and username='" + username + "'";
				}
				if (smsc != null) {
					mis_sql += " and Route_to_smsc ='" + smsc + "'";
				}
				if (msg_id_criteria) {
					if (start.equalsIgnoreCase(end)) {
						mis_sql += " and msg_id='" + start + "'";
					} else {
						mis_sql += " and msg_id  between " + start + " and " + end + "";
					}
				} else {
					if (start.equalsIgnoreCase(end)) {
						mis_sql += " and submitted_time='" + start + "'";
					} else {
						mis_sql += " and submitted_time  between '" + start + "' and '" + end + "'";
					}
				}
				// ------------ mis ---------------------------
				if (sender != null) {
					sql += "source_no like '" + sender + "' and ";
				}
				if (country != null) {
					sql += "oprCountry in(" + country + ") and ";
				}
				sql += "msg_id in(" + mis_sql + ") order by msg_id";
			}
		} else {
			sql = "select count(msg_id) from " + Constants.LOG_DB + ".smsc_in_log where ";
			if (username != null) {
				sql += "username='" + username + "' and ";
			}
			if (smsc != null) {
				sql += "smsc='" + smsc + "' and ";
			}
			if (sender != null) {
				sql += "source_no like '" + sender + "' and ";
			}
			if (country != null) {
				sql += "oprCountry in(" + country + ") and ";
			}
			if (msg_id_criteria) {
				if (start.equalsIgnoreCase(end)) {
					sql += "msg_id='" + start + "'";
				} else {
					sql += "msg_id between '" + start + "' and '" + end + "'";
				}
			} else {
				if (start.equalsIgnoreCase(end)) {
					sql += "time='" + start + "'";
				} else {
					sql += "time between '" + start + "' and '" + end + "'";
				}
			}
			sql += " order by msg_id";
		}
		logger.info("Resend count SQL: " + sql);
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		try {
			connection = GlobalCache.connnection_pool_1.getConnection();
			statement = connection.createStatement();
			rs = statement.executeQuery(sql);
			if (rs.next()) {
				to_be_resend = rs.getInt(1);
			}
		} catch (Exception ex) {
			logger.error("resendCount(" + params + ")", ex);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
					rs = null;
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
				}
			}
			GlobalCache.connnection_pool_1.putConnection(connection);
		}
		logger.info("To Be Resend Count: " + to_be_resend);
		return to_be_resend;
	}

	@Override
	public void resend(Map params) throws RemoteException {
		logger.info("Resend Request via Web "+"Params: " + params);
		ResendSMS resend = new ResendSMS(params);
		new Thread(resend, "ResendSMS").start();
	}

	@Override
	public boolean getSmppFlagStatus() throws RemoteException {
		return Constants.PROCESSING_STATUS;
	}

	@Override
	public void setUserHostStatus(boolean status) throws RemoteException, Exception {
		Constants.USER_HOST_STATUS = status;
	}
}
