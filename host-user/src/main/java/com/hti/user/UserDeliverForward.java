/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.HTIQueue;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;
import com.logica.smpp.Data;
import com.logica.smpp.pdu.DeliverSM;
import com.logica.smpp.pdu.PDU;
import com.logica.smpp.pdu.ValueNotSetException;
import com.logica.smpp.pdu.WrongLengthOfStringException;
import com.logica.smpp.pdu.tlv.WrongLengthException;
import com.logica.smpp.util.ByteBuffer;

/**
 *
 * @author Administrator
 */
public class UserDeliverForward implements Runnable {
	private boolean stop;
	private String systemid;
	private UserSession session;
	private HTIQueue dlrQueue;
	private boolean reload = true;
	private boolean active = true;
	private boolean started; // notify to thread is started or not
	private Logger logger = LoggerFactory.getLogger("userLogger");
	private List<DeliverSM> process_list = new ArrayList<DeliverSM>();
	private List<String> temp_list = new ArrayList<String>();

	public UserDeliverForward(String systemid, HTIQueue dlrQueue) {
		logger.info(systemid + " DeliverForward Thread Starting");
		this.systemid = systemid;
		this.dlrQueue = dlrQueue;
	}

	@Override
	public void run() {
		started = true;
		logger.info(systemid + " DeliverForward Thread Started: " + dlrQueue.size());
		while (!stop) {
			if (active) {
				if (dlrQueue.isEmpty()) {
					if (reload) {
						logger.debug(systemid + " Getting DLRs From Database");
						int isMore = putProcessQueue(1000);
						if (isMore < 1000) {
							reload = false;
						}
					} else {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					int counter = 0;
					logger.debug(systemid + " DLR Queue : " + dlrQueue.size());
					while (!dlrQueue.isEmpty()) {
						DeliverSM deliver_sm = (DeliverSM) dlrQueue.dequeue();
						if (session.sendPDU((PDU) deliver_sm)) {
							GlobalQueue.backupRespLogQueue.add(deliver_sm);
							try {
								System.out.println("<" + systemid + ":-->" + deliver_sm.getReceiptedMessageId()
										+ "><----Deliver Request Sent ------>");
							} catch (ValueNotSetException vex) {
							}
							session.increaseCloseTime();
							if (++counter > 100) {
								counter = 0;
								try {
									Thread.sleep(100);
								} catch (InterruptedException ex) {
								}
							}
						} else {
							dlrQueue.enqueue(deliver_sm);
							if (GlobalCache.UserSessionObject.containsKey(systemid)) {
								SessionManager userSession = (SessionManager) GlobalCache.UserSessionObject
										.get(systemid);
								logger.info(systemid + " Checking Receiver To Forward DLR (Rx:"
										+ userSession.getRxCount() + ")(TRx:" + userSession.getTRxCount() + ")");
								session = userSession.getReceiver();
								if (session != null) {
									logger.info(systemid + " Valid Receiver Found To Forward DLR "
											+ session.getSessionId());
									continue;
								}
							}
							logger.error(
									systemid + " No Valid Receiver Found To Forward DLR < " + dlrQueue.size() + " >");
							active = false;
							break;
						}
					}
					// reload = false;
				}
			} else {
				logger.debug("<- " + systemid + " Deliver Forward Inactive -> < " + dlrQueue.size() + " >");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			}
		}
		if (!dlrQueue.isEmpty()) {
			logger.info(systemid + " Pending(To Be Forward) DLR : " + dlrQueue.size());
			while (!dlrQueue.isEmpty()) {
				GlobalQueue.DLRInsertQueue.add((DeliverSM) dlrQueue.dequeue());
			}
		}
	}

	private int putProcessQueue(int limit) {
		logger.debug(systemid + " putProcessQueue()");
		int count = 0;
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		DeliverSM deliverSM;
		// String msg_id = null;
		String sql = "select * from backup_response where client_name=? and server_id=? limit " + limit;
		try {
			connection = GlobalCache.connection_pool_user.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setString(1, systemid);
			statement.setInt(2, GlobalVars.SERVER_ID);
			rs = statement.executeQuery();
			// String client_name = null;
			while (rs.next()) {
				deliverSM = new DeliverSM();
				deliverSM.setEsmClass((byte) Data.SM_SMSC_DLV_RCPT_TYPE);
				deliverSM.setServerId(rs.getInt("server_id"));
				// deliverSM.setDataCoding((byte) 0x03); // ISO-Latin-1
				deliverSM.setClientName(rs.getString("client_name"));
				deliverSM.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, rs.getString("destination"));
				deliverSM.setSourceAddr((byte) 5, (byte) 0, rs.getString("source"));
				try {
					deliverSM.setReceiptedMessageId(rs.getString("msg_id"));
				} catch (WrongLengthException wle) {
					logger.error(rs.getString("client_name") + ":-> WrongLengthException(msg_id) For Deliver_SM: "
							+ rs.getString("msg_id"));
				}
				String pdutext = rs.getString("pdu");
				try {
					deliverSM.setShortMessage(pdutext);
				} catch (WrongLengthOfStringException wlse) {
					logger.error(rs.getString("client_name") + ":-> WrongLengthOfStringException(pdu) For Deliver_SM: "
							+ rs.getString("msg_id"));
				}
				for (String keyvalue : pdutext.split(" ")) {
					if (keyvalue != null) {
						if (keyvalue.contains("err:")) {
							deliverSM.setNetworkErrorCode(new ByteBuffer(keyvalue.split(":")[1].getBytes()));
						}
						if (keyvalue.contains("stat:")) {
							int statusCode = 1;
							String status = keyvalue.split(":")[1];
							if (status.equalsIgnoreCase("DELIVRD")) {
								statusCode = Data.SM_STATE_DELIVERED;
							} else if (status.equalsIgnoreCase("UNDELIV")) {
								statusCode = Data.SM_STATE_UNDELIVERABLE;
							} else if (status.equalsIgnoreCase("ACCEPTD")) {
								statusCode = Data.SM_STATE_ACCEPTED;
							} else if (status.equalsIgnoreCase("REJECTD")) {
								statusCode = Data.SM_STATE_REJECTED;
							} else if (status.equalsIgnoreCase("EXPIRED")) {
								statusCode = Data.SM_STATE_EXPIRED;
							}
							deliverSM.setMessageState((byte) statusCode);
						}
					}
				}
				process_list.add(deliverSM);
				temp_list.add(rs.getString("msg_id"));
				count++;
			}
			logger.debug(systemid + " deliver process list: " + process_list.size());
			if (!temp_list.isEmpty()) {
				sql = "delete from backup_response where msg_id in(" + String.join(",", temp_list) + ")";
				statement = connection.prepareStatement(sql);
				int delete_count = statement.executeUpdate();
				logger.info(
						systemid + " backup_response removed : " + delete_count + " Requested: " + temp_list.size());
				temp_list.clear();
			}
		} catch (Exception q) {
			logger.error("putProcessQueue(" + systemid + ")", q.fillInStackTrace());
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
		while (!process_list.isEmpty()) {
			dlrQueue.enqueue((DeliverSM) process_list.remove(0));
		}
		logger.debug(systemid + " DLR Enqueued Count: " + dlrQueue.size());
		return count;
	}

	public void setReload(boolean reload) {
		this.reload = reload;
	}

	public boolean isActive() {
		return active;
	}

	public void setSession(UserSession session) {
		this.session = session;
		active = true;
	}

	public boolean isStarted() {
		return started;
	}

	public void stop() {
		logger.info(systemid + " DeliverForward Thread Stopping <" + dlrQueue.size() + ">");
		GlobalCache.UserRxObject.remove(systemid);
		stop = true;
	}
}
