/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.smsc;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.RoutePDU;
import com.hti.user.UserBalance;
import com.hti.util.Converter;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;
import com.logica.msgContent.ConcatUnicode;
import com.logica.smpp.Data;
import com.logica.smpp.pdu.Request;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.WrongDateFormatException;
import com.logica.smpp.util.ByteBuffer;
import com.logica.smpp.util.Queue;

/**
 *
 * @author Administrator
 */
public class SendErrorResponseSMS implements Runnable {
	private String smsc;
	private String flag;
	private String reRoute;
	private String senderRepl;
	private Logger logger = LoggerFactory.getLogger(SendErrorResponseSMS.class);
	private int LIMIT = 1000;
	private Queue localQueue = new Queue();
	private String sql;
	// private UserServiceInvoke remoteProcess;

	public SendErrorResponseSMS(String smsc, String reRoute, String flag) {
		Thread.currentThread().setName("SendErrorResponseSMS");
		this.smsc = smsc;
		this.reRoute = reRoute;
		this.flag = flag;
	}

	public SendErrorResponseSMS(String sql, String reRoute) {
		this.sql = sql;
		this.reRoute = reRoute;
	}

	public void setSenderRepl(String senderRepl) {
		this.senderRepl = senderRepl;
	}

	@Override
	public void run() {
		logger.info(" <-- SendErrorResponseSMS Started --> ");
		int count = 0;
		int counter = 0;
		while (true) {
			if (smsc != null) {
				sql = "select * from smsc_in where smsc ='" + smsc + "' and s_flag like '" + flag + "' limit " + LIMIT;
			} else {
				sql += " limit " + LIMIT;
			}
			logger.info("Error SQL: " + sql);
			do {
				count = proceed();
				counter += count;
				if (count < LIMIT) { // No need to Query again
					break;
				}
			} while (count > 0);
			logger.info(" Total Error <" + flag + "> Clear Count: " + counter);
			break;
		}
		logger.info(" <-- SendErrorResponseSMS Stopped --> ");
	}

	private int proceed() {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement pstm = null;
		// PreparedStatement delPstm = null;
		String msg_id = null;
		int count = 0;
		try {
			con = GlobalCache.connnection_pool_1.getConnection();
			pstm = con.prepareStatement(sql);
			rs = pstm.executeQuery();
			// msg_ids = "";
			String time = null, seq_no = null, msg_type = null, content = null, dest_ton = null, dest_npi = null,
					destination_no = null;
			String oprCountry = null, sour_ton = null, sour_npi = null, source_no = null, registered = null,
					esm_class = null, dcs = null;
			String backup_smsc = null;
			double cost = 0;
			boolean refund = false;
			SubmitSM submit_sm = null;
			List clist = new ArrayList();
			boolean is_reg_sender = false;
			while (rs.next()) {
				try {
					msg_id = rs.getString("msg_id");
					time = rs.getString("time");
					seq_no = rs.getString("seq_no");
					content = rs.getString("content");
					dest_ton = rs.getString("dest_ton");
					dest_npi = rs.getString("dest_npi");
					destination_no = rs.getString("destination_no");
					oprCountry = rs.getString("oprCountry");
					sour_ton = rs.getString("sour_ton");
					sour_npi = rs.getString("sour_npi");
					if (senderRepl != null) {
						source_no = senderRepl;
					} else {
						source_no = rs.getString("source_no");
					}
					registered = rs.getString("registered");
					// type = rs.getString("type");
					esm_class = rs.getString("esm_class");
					dcs = rs.getString("dcs");
					cost = rs.getDouble("cost");
					String username = rs.getString("username");
					// String status = rs.getString("status");
					// String s_flag = rs.getString("s_flag");
					String route = rs.getString("smsc");
					int Priority = rs.getInt("Priority");
					int session_id = rs.getInt("session_id");
					is_reg_sender = rs.getBoolean("is_reg_sender");
					backup_smsc = rs.getString("Secondry_smsc_id");
					ConcatUnicode concate = new ConcatUnicode();
					if (rs.getBlob("msg_object") != null) {
						InputStream is = rs.getBlob("msg_object").getBinaryStream(); // this mthd give/get input stream...
						if (is.available() > 0) {
							ObjectInputStream oip = new ObjectInputStream(is);
							Object obj = oip.readObject();
							is.close();
							oip.close();
							concate = (ConcatUnicode) obj;
						}
					}
					msg_type = rs.getString("msg_type");
					refund = rs.getBoolean("refund");
					String validity_period = rs.getString("validity_period");
					String orig_source_adrr = rs.getString("orig_source");
					submit_sm = new SubmitSM();
					// submit_sm.setDataCoding(Byte.parseByte(dcs));
					int intdcs = Integer.parseInt(dcs.trim());
					submit_sm.setDataCoding((byte) intdcs);
					submit_sm.setDestAddr(Byte.parseByte(dest_ton), Byte.parseByte(dest_npi), destination_no);
					submit_sm.setSourceAddr(Byte.parseByte(sour_ton), Byte.parseByte(sour_npi), source_no);
					submit_sm.setEsmClass(Byte.parseByte(esm_class));
					submit_sm.setSequenceNumber(Integer.parseInt(seq_no));
					submit_sm.setRegisteredDelivery(Byte.parseByte(registered));
					try {
						submit_sm.setValidityPeriod(validity_period);
					} catch (WrongDateFormatException we) {
						logger.error(msg_id + " Invalid Validity Period Found: " + validity_period);
					}
					// submit_sm.setShortMessage(content);
					if ((intdcs == 0) || (intdcs == 1) || (intdcs == 240)) {
						if (esm_class.equals("64")) {
							ByteBuffer bfr = concate.getByteBuffer();
							submit_sm.setBody(bfr); // Concatinated English
						} else {
							submit_sm.setShortMessage(content); // Single english,flash
						}
					} else if (esm_class.equals("64") && intdcs == 8) {// concate unicode and may be concate english if dcs isnot zero
						// logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>Concat UNICODE");
						ByteBuffer bfr = concate.getByteBuffer();
						submit_sm.setBody(bfr);
					} else if (esm_class.equals("64") && intdcs == 245) {
						if (msg_type.equals("buffer")) {
							// logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>RINGTONE");
							ByteBuffer bfr = concate.getByteBuffer(); // for ringtone and wap push
							submit_sm.setBody(bfr);
						} else {
							// logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>LOGO");
							submit_sm.setShortMessage(concate.getMsg());
							// }
						}
					} else { // for Unicode single msg
						content = Converter.getUnicode(content.toCharArray());
						submit_sm.setShortMessage(content, Data.ENC_UTF16_BE);
					}
					RoutePDU pdu = new RoutePDU((Request) submit_sm, msg_id, Integer.parseInt(seq_no),
							String.valueOf(session_id), Priority);
					if (reRoute != null) {
						pdu.setSmsc(reRoute);
					} else {
						pdu.setSmsc(route);
					}
					pdu.setUsername(username);
					pdu.setBackupSmsc(backup_smsc);
					// pdu.setRegisterdlrValue(Byte.parseByte(registered));
					pdu.setCost(cost);
					try {
						pdu.setNetworkId(Integer.parseInt(oprCountry));
					} catch (Exception ex) {
						pdu.setNetworkId(0);
					}
					pdu.setTime(time);
					// pdu.putPriority(Priority);
					pdu.setRefund(refund);
					pdu.setOriginalSourceAddr(orig_source_adrr);
					pdu.setRegisterSender(is_reg_sender);
					localQueue.enqueue(pdu);
					clist.add(msg_id);
					pdu = null;
					concate = null;
					submit_sm = null;
				} catch (Exception ex) {
					logger.error(msg_id + " <- UNSUPPORTED MESSAGE TYPE -> " + ex.fillInStackTrace());
					String unsupportedSql = "update smsc_in set s_flag='UN' where msg_id ='" + msg_id + "'";
					Statement unstatement = null;
					Connection unsupported = null;
					try {
						unsupported = GlobalCache.connnection_pool_1.getConnection();
						unstatement = unsupported.createStatement();
						unstatement.executeUpdate(unsupportedSql);
					} catch (Exception unex) {
						logger.error("proceed(1)", unex.fillInStackTrace());
					} finally {
						if (unstatement != null) {
							try {
								unstatement.close();
							} catch (SQLException ex1) {
								unstatement = null;
							}
						}
						GlobalCache.connnection_pool_1.putConnection(unsupported);
					}
				}
				count++;
			}
			if (clist.size() > 0) {
				logger.info(" C Flagged Records Update(NG): " + clist.size());
				String messageid = null;
				PreparedStatement preStatement = null;
				Connection cFlagConn = null;
				try {
					cFlagConn = GlobalCache.connnection_pool_1.getConnection();
					preStatement = cFlagConn.prepareStatement("update smsc_in set s_flag=? where msg_id =?");
					cFlagConn.setAutoCommit(false);
					while (clist.size() > 0) {
						messageid = (String) clist.remove(0);
						preStatement.setString(1, "C");
						preStatement.setString(2, messageid);
						preStatement.addBatch();
					}
					preStatement.executeBatch();
					cFlagConn.commit();
				} catch (Exception ex) {
					logger.error("proceed(2)", ex.fillInStackTrace());
				} finally {
					if (preStatement != null) {
						try {
							preStatement.close();
						} catch (SQLException ex) {
						}
					}
					GlobalCache.connnection_pool_1.putConnection(cFlagConn);
				}
				logger.info("<- C Flagged Records Update Finished (NG)-> ");
				int counter = 0;
				while (!localQueue.isEmpty()) {
					RoutePDU route = (RoutePDU) localQueue.dequeue();
					boolean proceed = true;
					if (route.isRefund()) {
						String username = route.getUsername();
						double amount = route.getCost();
						proceed = deductBalance(username, amount);
					}
					if (proceed) {
						GlobalQueue.interProcessRequest.enqueue(route);
					} else {
						logger.error(route.getUsername() + " <--- Unable To Resend PDU ---> " + route.getHtiMsgId());
						/// messageid = null;
						preStatement = null;
						cFlagConn = null;
						try {
							cFlagConn = GlobalCache.connnection_pool_1.getConnection();
							preStatement = cFlagConn.prepareStatement("update smsc_in set s_flag=? where msg_id =?");
							preStatement.setString(1, "X");
							preStatement.setString(2, route.getHtiMsgId());
							preStatement.executeUpdate();
						} catch (Exception ex) {
							logger.error("proceed(3)", ex.fillInStackTrace());
						} finally {
							if (preStatement != null) {
								try {
									preStatement.close();
								} catch (SQLException ex) {
								}
							}
							GlobalCache.connnection_pool_1.putConnection(cFlagConn);
						}
					}
					if (++counter > 10) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException ie) {
						}
						counter = 0;
					}
				}
			}
		} catch (Exception ex) {
			logger.error("proceed(4)", ex.fillInStackTrace());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
					rs = null;
				}
			}
			if (pstm != null) {
				try {
					pstm.close();
				} catch (SQLException ex) {
					pstm = null;
				}
			}
			GlobalCache.connnection_pool_1.putConnection(con);
			con = null;
		}
		return count;
	}

	private boolean deductBalance(String username, double cost) {
		try {
			UserBalance balance = GlobalVars.userService.getUserBalance(username);
			if (balance != null) {
				if (balance.getFlag().equalsIgnoreCase("No")) {
					return balance.deductCredit(1);
				} else {
					return balance.deductAmount(cost);
				}
			} else {
				return false;
			}
		} catch (Exception ex) {
			logger.info(username + " <-- Balance Deduct Error [" + cost + "] --> " + ex);
			return false;
		}
	}
}
