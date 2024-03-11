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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.HTIQueue;
import com.hti.objects.RoutePDU;
import com.hti.smsc.SendErrorResponseSMS;
import com.hti.user.UserBalance;
import com.hti.util.Constants;
import com.hti.util.Converter;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;
import com.logica.msgContent.ConcatUnicode;
import com.logica.smpp.Data;
import com.logica.smpp.pdu.Request;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.WrongDateFormatException;
import com.logica.smpp.pdu.tlv.TLV;
import com.logica.smpp.util.ByteBuffer;

/**
 *
 * @author Administrator
 */
public class ResendSMS implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private boolean stop;
	private Map<String, String> params;
	private Map<String, RoutePDU> localQueue = new LinkedHashMap<String, RoutePDU>();

	public ResendSMS(Map<String, String> params) {
		logger.info("ResendSMS Thread Starting");
		this.params = params;
	}

	@Override
	public void run() {
		putQueue(params);
		while (!stop) {
			int counter = 0;
			logger.info(" Checking For Resend ");
			int count = 0;
			do {
				count = proceed();
				if (count > 0) {
					counter += count;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				logger.info(" Resend Count: " + count);
			} while (count > 0);
			logger.info(" Total Resend Count: " + counter);
			break;
		}
		logger.info("ResendSMS Thread Stopped");
	}

	public void putQueue(Map<String, String> params) {
		logger.info("<- put/update records on smsc_in ->");
		String sql = "";
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
		String reroute = null, username = null, sender = null, senderRepl = null, smsc = null, country = null;
		boolean proceed = true;
		if (params.containsKey("reroute")) {
			reroute = params.get("reroute");
		}
		if (params.containsKey("username")) {
			username = params.get("username");
		}
		if (params.containsKey("sender")) {
			sender = params.get("sender");
			if (params.containsKey("sender_repl")) {
				senderRepl = params.get("sender_repl");
			}
		}
		if (params.containsKey("smsc")) {
			smsc = params.get("smsc");
		}
		if (params.containsKey("oprCountry")) {
			country = params.get("oprCountry");
		}
		if (params.containsKey("status")) {
			String status = params.get("status");
			if (status.equalsIgnoreCase("NON_RESPOND") || status.equalsIgnoreCase("ZERO_COST")
					|| status.equalsIgnoreCase("ERR_RESPOND") || GlobalCache.EsmeErrorFlag.containsKey(status)) {// only smsc_in records
				sql = "select * from smsc_in where s_flag=";
				if (status.equalsIgnoreCase("NON_RESPOND")) {
					sql += "'F'";
				} else if (status.equalsIgnoreCase("ZERO_COST")) {
					sql += "'M'";
				} else if (status.equalsIgnoreCase("ERR_RESPOND")) {
					sql += "'E'";
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
						sql += "and msg_id between '" + start + "' and '" + end + "'";
					}
				} else {
					if (start.equalsIgnoreCase(end)) {
						sql += "and time='" + start + "' ";
					} else {
						sql += "and time between '" + start + "' and '" + end + "'";
					}
				}
				proceed = false;
				if (status.equalsIgnoreCase("NON_RESPOND")) {
					new Thread(new ClearNonResponding(true, sql, reroute, senderRepl), "ClearNonResponding").start();
				} else {
					if (senderRepl != null) {
						SendErrorResponseSMS errorRespResend = new SendErrorResponseSMS(sql, reroute);
						errorRespResend.setSenderRepl(senderRepl);
						new Thread(errorRespResend).start();
					} else {
						new Thread(new SendErrorResponseSMS(sql, reroute)).start();
					}
				}
			} else {
				sql = "insert into smsc_in(msg_id,time,seq_no,content,dest_ton,dest_npi,destination_no,oprCountry,sour_ton,sour_npi,source_no,registered,esm_class,dcs,cost,username,smsc,Priority,session_id,Secondry_smsc_id,msg_object,msg_type,s_flag,refund,validity_period,orig_source)"
						+ " select msg_id,time,seq_no,content,dest_ton,dest_npi,destination_no,oprCountry,sour_ton,sour_npi,";
				if (senderRepl != null) {
					sql += "'" + senderRepl + "'";
				} else {
					sql += "source_no";
				}
				sql += ",registered,esm_class,dcs,cost,username,";
				if (reroute != null) {
					sql += "'" + reroute + "'";
				} else {
					sql += "smsc";
				}
				sql += ",Priority,session_id,Secondry_smsc_id,msg_object,msg_type,'R',refund,validity_period,orig_source "
						+ "from " + Constants.LOG_DB + ".smsc_in_log where ";
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
						mis_sql += " and msg_id  between '" + start + "' and '" + end + "'";
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
				sql += "msg_id in(" + mis_sql + ") " + "order by msg_id";
				sql += " ON DUPLICATE KEY UPDATE s_flag='R'";
				if (reroute != null) {
					sql += ",smsc='" + reroute + "'";
				}
			}
		} else {
			sql = "insert into smsc_in(msg_id,time,seq_no,content,dest_ton,dest_npi,destination_no,oprCountry,sour_ton,sour_npi,source_no,registered,esm_class,dcs,cost,username,smsc,Priority,session_id,Secondry_smsc_id,msg_object,msg_type,s_flag,refund,validity_period,orig_source)"
					+ " select msg_id,time,seq_no,content,dest_ton,dest_npi,destination_no,oprCountry,sour_ton,sour_npi,";
			if (senderRepl != null) {
				sql += "'" + senderRepl + "'";
			} else {
				sql += "source_no";
			}
			sql += ",registered,esm_class,dcs,cost,username,";
			if (reroute != null) {
				sql += "'" + reroute + "'";
			} else {
				sql += "smsc";
			}
			sql += ",Priority,session_id,Secondry_smsc_id,msg_object,msg_type,'R',refund,validity_period,orig_source "
					+ "from " + Constants.LOG_DB + ".smsc_in_log where ";
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
			sql += " ON DUPLICATE KEY UPDATE s_flag='R'";
			if (reroute != null) {
				sql += ",smsc='" + reroute + "'";
			}
		}
		logger.info("Resend SQL: " + sql);
		if (proceed) {
			Connection connection = null;
			PreparedStatement statement = null;
			try {
				connection = GlobalCache.connnection_pool_1.getConnection();
				statement = connection.prepareStatement(sql);
				int affected_count = statement.executeUpdate();
				logger.info("Records Affected to smscIn ======> " + affected_count);
			} catch (Exception ex) {
			} finally {
				if (statement != null) {
					try {
						statement.close();
					} catch (SQLException e) {
					}
				}
				GlobalCache.connnection_pool_1.putConnection(connection);
			}
		}
	}

	private int proceed() {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement pstm = null;
		String msg_id = null;
		int count = 0;
		String msg_id_status = "";
		String sql = "select * from smsc_in where s_flag='R' limit 1000";
		Set<String> OptionalParamSet = new HashSet<String>();
		try {
			con = GlobalCache.connnection_pool_1.getConnection();
			pstm = con.prepareStatement(sql);
			rs = pstm.executeQuery();
			// msg_ids = "";
			String time = null, seq_no = null, msg_type = null, content = null, dest_ton = null, dest_npi = null,
					destination_no = null;
			String oprCountry = null, sour_ton = null, sour_npi = null, source_no = null, registered = null,
					esm_class = null, dcs = null;
			double cost = 0;
			boolean refund = false, is_reg_sender = false;
			SubmitSM submit_sm = null;
			String username = null, smsc = null;
			String backup_smsc = null;
			List<String> clist = new ArrayList<String>();
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
					source_no = rs.getString("source_no");
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
					if (rs.getInt("refund") == 2) {
						refund = true;
					} else {
						refund = rs.getBoolean("refund");
					}
					//
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
					if (refund) {
						msg_id_status += "'" + msg_id + "',";
					}
					pdu.setSmsc(smsc);
					pdu.setBackupSmsc(backup_smsc);
					pdu.setUsername(username);
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
					// ************* add to next processing *************
					clist.add(msg_id);
					if (rs.getBoolean("opt_param")) {
						OptionalParamSet.add(msg_id);
					}
					localQueue.put(msg_id, pdu);
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
			// ---------- Updating Flag as C ----------
			if (clist.size() > 0) {
				logger.info(" C Flagged Records Update(R): " + clist.size());
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
				logger.info("<- C Flagged Records Update Finished (R)-> ");
			}
		} catch (Exception ex) {
			logger.error("proceed(3)", ex.fillInStackTrace());
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
		if (!OptionalParamSet.isEmpty()) {
			logger.info("<--- Checking For Extra Optional Params[" + OptionalParamSet.size() + "] --->");
			Connection connection = null;
			ResultSet res = null;
			Statement statement = null;
			try {
				connection = GlobalCache.connnection_pool_1.getConnection();
				statement = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
						java.sql.ResultSet.CONCUR_READ_ONLY);
				statement.setFetchSize(Integer.MIN_VALUE);
				res = statement.executeQuery(
						"select * from sm_opt_param where msg_id in(" + String.join(",", OptionalParamSet) + ")");
				while (res.next()) {
					msg_id = res.getString("msg_id");
					TLV peId = null, templateId = null, tmId = null, channel_type = null, caption = null;
					if (res.getBlob("pe_id") != null) {
						InputStream is = res.getBlob("pe_id").getBinaryStream(); // this mthd give/get input stream...
						if (is.available() > 0) {
							ObjectInputStream oip = new ObjectInputStream(is);
							Object obj = oip.readObject();
							is.close();
							oip.close();
							peId = (TLV) obj;
						}
					}
					if (res.getBlob("template_id") != null) {
						InputStream is = res.getBlob("template_id").getBinaryStream(); // this mthd give/get input stream...
						if (is.available() > 0) {
							ObjectInputStream oip = new ObjectInputStream(is);
							Object obj = oip.readObject();
							is.close();
							oip.close();
							templateId = (TLV) obj;
						}
					}
					if (res.getBlob("tm_id") != null) {
						InputStream is = res.getBlob("tm_id").getBinaryStream(); // this mthd give/get input stream...
						if (is.available() > 0) {
							ObjectInputStream oip = new ObjectInputStream(is);
							Object obj = oip.readObject();
							is.close();
							oip.close();
							tmId = (TLV) obj;
						}
					}
					if (res.getBlob("channel_type") != null) {
						InputStream is = res.getBlob("channel_type").getBinaryStream(); // this mthd give/get input stream...
						if (is.available() > 0) {
							ObjectInputStream oip = new ObjectInputStream(is);
							Object obj = oip.readObject();
							is.close();
							oip.close();
							channel_type = (TLV) obj;
						}
					}
					if (res.getBlob("caption") != null) {
						InputStream is = res.getBlob("caption").getBinaryStream(); // this mthd give/get input stream...
						if (is.available() > 0) {
							ObjectInputStream oip = new ObjectInputStream(is);
							Object obj = oip.readObject();
							is.close();
							oip.close();
							caption = (TLV) obj;
						}
					}
					RoutePDU routePDU = localQueue.get(msg_id);
					if (peId != null) {
						((SubmitSM) routePDU.getRequestPDU()).setExtraOptional(peId);
					}
					if (templateId != null) {
						((SubmitSM) routePDU.getRequestPDU()).setExtraOptional(templateId);
					}
					if (tmId != null) {
						((SubmitSM) routePDU.getRequestPDU()).setExtraOptional(tmId);
					}
					if (channel_type != null) {
						((SubmitSM) routePDU.getRequestPDU()).setExtraOptional(channel_type);
					}
					if (caption != null) {
						((SubmitSM) routePDU.getRequestPDU()).setExtraOptional(caption);
					}
				}
			} catch (Exception e) {
				logger.error("", e.fillInStackTrace());
			} finally {
				if (statement != null) {
					try {
						statement.close();
					} catch (SQLException e) {
					}
				}
				if (res != null) {
					try {
						res.close();
					} catch (SQLException e) {
					}
				}
				GlobalCache.connnection_pool_1.putConnection(connection);
			}
			OptionalParamSet.clear();
		}
		// ---------- Checking status From mis_table ----------
		Map<String, String> status_map = new HashMap<String, String>();
		if (msg_id_status.length() > 0) {
			Statement statement = null;
			Connection connection = null;
			ResultSet res = null;
			msg_id_status = msg_id_status.substring(0, msg_id_status.length() - 1);
			String mis_status_sql = "select msg_id,status from mis_table where msg_id in(" + msg_id_status + ")";
			try {
				connection = GlobalCache.connnection_pool_1.getConnection();
				statement = connection.createStatement();
				res = statement.executeQuery(mis_status_sql);
				while (res.next()) {
					status_map.put(res.getString("msg_id"), res.getString("status"));
				}
			} catch (Exception ex) {
				logger.error("proceed(4)", ex.fillInStackTrace());
			} finally {
				GlobalCache.connnection_pool_1.putConnection(connection);
				if (statement != null) {
					try {
						statement.close();
					} catch (SQLException e) {
					}
				}
				if (res != null) {
					try {
						res.close();
					} catch (SQLException e) {
					}
				}
			}
		}
		// ----------------- Put Records to process Queue ----------------------------
		// int Queue_Counter = 0;
		double delay = 0;
		if (params.containsKey("delay")) {
			try {
				delay = Double.parseDouble(params.get("delay"));
			} catch (Exception ex) {
				logger.error(params.get("delay") + " -> " + ex);
			}
			logger.info("Resending " + localQueue.size() + " with Delay: " + delay);
		} else {
			logger.info("Resending : " + localQueue.size());
		}
		for (RoutePDU route : localQueue.values()) {
			boolean proceed = true;
			if (status_map.containsKey(route.getHtiMsgId())) {
				String status = (String) status_map.remove(route.getHtiMsgId());
				if (status.equalsIgnoreCase("ATES") || status.startsWith("DELIV") || status.startsWith("ACCEP")) {
					route.setDeduct(false);
				} else {
					String username = route.getUsername();
					double cost = route.getCost();
					proceed = deductBalance(username, cost);
				}
			} else {
				route.setDeduct(false);
			}
			if (proceed) {
				// *********** Distribution For Smsc **************
				/*
				 * if (BearerBox.DISTRIBUTION) { String next_smsc = null; String desti = ((SubmitSM) route.getRequestPDU()).getDestAddr().getAddress(); if (last_destination != null && last_smsc !=
				 * null) { if (last_destination.equalsIgnoreCase(desti)) { next_smsc = last_smsc; // Using same connection for same destination } else { next_smsc =
				 * DistributionGroupManager.findRoute(route.getSmsc()); } } else { next_smsc = DistributionGroupManager.findRoute(route.getSmsc()); } route.setSmsc(next_smsc); last_destination =
				 * desti; last_smsc = next_smsc; }
				 */
				// *********** End Distribution For Smsc **************
				GlobalQueue.interProcessRequest.enqueue(route);
				if (delay > 0) {
					try {
						Thread.sleep((long) ((double) delay * 1000));
					} catch (InterruptedException e) {
					}
				}
			}
		}
		logger.info("Resent : " + localQueue.size());
		localQueue.clear();
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

	public void stop() {
		stop = true;
		logger.info("ResendSMS Thread Stopping");
	}
}
