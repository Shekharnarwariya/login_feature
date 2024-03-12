/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.RoutePDU;
import com.hti.util.Converter;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.logica.msgContent.ConcatUnicode;
import com.logica.smpp.Data;
import com.logica.smpp.pdu.Request;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.WrongDateFormatException;
import com.logica.smpp.util.ByteBuffer;
import com.logica.smpp.util.Queue;

/**
 * @author Administrator
 */
public class ClearNonResponding implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private boolean isSmsc;
	private boolean isCustom;
	private String name = null;
	private Queue localQueue = new Queue();
	private String reRoute;
	private String sql;
	private String senderRepl;

	public ClearNonResponding(boolean custom, String sql, String reRoute, String senderRepl) {
		this.isCustom = custom;
		this.sql = sql;
		this.reRoute = reRoute;
		this.senderRepl = senderRepl;
		logger.info(" Clear Non Responding From <" + reRoute + "> Thread Starting");
	}

	public ClearNonResponding(String smsc, String reRoute) {
		this.isSmsc = true;
		this.name = smsc;
		this.reRoute = reRoute;
		logger.info(name + " Clear Non Responding From <" + reRoute + "> Thread Starting");
	}

	public ClearNonResponding(String username) {
		this.isSmsc = false;
		this.name = username;
		logger.info(name + " Clear Non Responding Thread Starting");
	}

	public ClearNonResponding() {
		this.isSmsc = true;
		logger.info("Clear All Non Responding Thread Starting");
	}

	private void resubmitSmsc() {
		logger.info("Checking Non Responding For All Connected Smsc");
		Set<String> names = getNonResponding();
		for (String smsc : names) {
			resubmitSmsc(smsc);
		}
	}

	private void resubmitSmsc(String smsc) {
		int counter = 0;
		logger.info("Checking Non Responding For Smsc: " + smsc);
		String smsc_to_be_checked = smsc;
		if (reRoute != null) {
			smsc_to_be_checked = reRoute;
		}
		if (GlobalCache.SmscConnectionSet.contains(smsc_to_be_checked)) {
			String sql = "select * from smsc_in where smsc = '" + smsc + "' and s_flag= 'F' limit " + 1000;
			int count = 0;
			do {
				count = proceed(sql);
				counter += count;
				logger.info(smsc + " Non Responding Clear Count: " + count);
			} while (count > 0);
		} else {
			logger.info(smsc_to_be_checked + " not Connected. Can't Proceed For Clear Non Responding.");
		}
		logger.info(smsc + " Total Non Responding Clear Count: " + counter);
	}

	private void resubmitUser(String username) {
		int counter = 0;
		logger.info("Checking Non Responding For User: " + username);
		String sql = "select * from smsc_in where username = '" + username + "' and s_flag= 'F' limit " + 1000;
		int count = 0;
		do {
			count = proceed(sql);
			counter += count;
			logger.info(username + " Non Responding Clear Count: " + count);
		} while (count > 0);
		logger.info(username + " Total Non Responding Clear Count: " + counter);
	}

	private void resubmitCustom() {
		int counter = 0;
		logger.info(" Checking Non Responding For Custom ");
		int count = 0;
		do {
			count = proceed(sql + " limit " + 1000);
			counter += count;
			logger.info(" Non Responding Clear Count: " + count);
		} while (count > 0);
		logger.info(" Total Non Responding Clear Count: " + counter);
	}

	private int proceed(String sql) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement pstm = null;
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
			boolean is_reg_sender = false;
			SubmitSM submit_sm = null;
			String username = null, smsc = null;
			List clist = new ArrayList();
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
					username = rs.getString("username");
					smsc = rs.getString("smsc");
					backup_smsc = rs.getString("Secondry_smsc_id");
					int Priority = rs.getInt("Priority");
					int session_id = rs.getInt("session_id");
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
					// refund = rs.getBoolean("refund");
					is_reg_sender = rs.getBoolean("is_reg_sender");
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
						pdu.setSmsc(smsc);
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
					if (rs.getInt("refund") == 2) {
						pdu.setRefund(true);
						pdu.setDeduct(true);
					} else {
						pdu.setRefund(rs.getBoolean("refund"));
					}
					pdu.setOriginalSourceAddr(orig_source_adrr);
					pdu.setRegisterSender(is_reg_sender);
					// ************* add to next processing *************
					clist.add(msg_id);
					localQueue.enqueue(pdu);
					// **************************************************
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
						logger.error("", unex.fillInStackTrace());
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
				logger.info(" C Flagged Records Update(F): " + clist.size());
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
					logger.error("proceed(1)", ex.fillInStackTrace());
				} finally {
					if (preStatement != null) {
						try {
							preStatement.close();
						} catch (SQLException ex) {
						}
					}
					GlobalCache.connnection_pool_1.putConnection(cFlagConn);
				}
				logger.info("<- C Flagged Records Update Finished (F)-> ");
				int Queue_Counter = 0;
				while (!localQueue.isEmpty()) {
					if (++Queue_Counter >= 100) {
						Queue_Counter = 0;
						try {
							Thread.sleep(100);
						} catch (InterruptedException ie) {
						}
					}
					GlobalQueue.interProcessRequest.enqueue((RoutePDU) localQueue.dequeue());
				}
			}
		} catch (Exception ex) {
			logger.error("proceed(" + sql + ")", ex.fillInStackTrace());
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

	@Override
	public void run() {
		while (true) {
			if (isCustom) {
				resubmitCustom();
			} else {
				if (isSmsc) {
					if (name == null) {
						resubmitSmsc();
					} else {
						resubmitSmsc(name);
					}
				} else {
					resubmitUser(name);
				}
			}
			break;
		}
		logger.info("Clear Non Responding Thread Stopped");
	}

	private Set<String> getNonResponding() {
		Set<String> names = new HashSet<String>();
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement pstm = null;
		String sql = "select distinct(smsc) from smsc_in where s_flag='F'";
		try {
			con = GlobalCache.connnection_pool_1.getConnection();
			pstm = con.prepareStatement(sql);
			rs = pstm.executeQuery();
			while (rs.next()) {
				String smsc = rs.getString("smsc");
				if (smsc != null) {
					names.add(smsc);
				}
			}
		} catch (Exception ex) {
			logger.error("checkNonResponding()", ex);
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
		return names;
	}
}
