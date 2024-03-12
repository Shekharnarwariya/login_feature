/*
 * To change this template, choose Tools | Templates
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.PriorityQueue;
import com.hti.objects.RoutePDU;
import com.hti.smsc.DistributionGroupManager;
import com.hti.util.Constants;
import com.hti.util.Converter;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.logica.msgContent.ConcatUnicode;
import com.logica.smpp.Data;
import com.logica.smpp.pdu.Request;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.WrongDateFormatException;
import com.logica.smpp.pdu.tlv.TLV;
import com.logica.smpp.util.ByteBuffer;

public class HtiQueueLoader implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	// private Logger tracklogger = LoggerFactory.getLogger("trackLogger");
	private Map<String, RoutePDU> localQueue = new LinkedHashMap<String, RoutePDU>();
	private boolean stop;
	private Connection connection = null;
	private Statement statement = null;
	private PreparedStatement preStatement = null;
	private ResultSet rs = null;
	// private Set<String> CflagCache = new HashSet<String>();
	private Set<String> UNflagCache = new HashSet<String>();
	private Map<Integer, Map<String, Integer>> localDownCount = new HashMap<Integer, Map<String, Integer>>();
	private Map<Integer, Map<String, Integer>> localLoadCount = new HashMap<Integer, Map<String, Integer>>();
	private int LOAD_LIMIT = 20000;
	private Set<String> OptionalParamSet = new HashSet<String>();

	public HtiQueueLoader() {
		logger.info("HtiQueueLoader Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				Thread.sleep(3 * 1000);
			} catch (InterruptedException iex) {
			}
			if (Constants.PROCESSING_STATUS) {
				// ************ Checking For Down Counter *******************
				try {
					connection = GlobalCache.connnection_pool_2.getConnection();
					statement = connection.createStatement();
					rs = statement.executeQuery(
							"select count(msg_id) as count,group_id,smsc from smsc_in where s_flag='Q' group by group_id,smsc");
					while (rs.next()) {
						if (rs.getString("smsc") != null) {
							Map<String, Integer> smsc_down = null;
							if (localDownCount.containsKey(rs.getInt("group_id"))) {
								smsc_down = localDownCount.get(rs.getInt("group_id"));
							} else {
								smsc_down = new HashMap<String, Integer>();
							}
							smsc_down.put(rs.getString("smsc"), rs.getInt("count"));
							localDownCount.put(rs.getInt("group_id"), smsc_down);
						}
					}
				} catch (Exception ex) {
					logger.error("", ex.fillInStackTrace());
				} finally {
					if (statement != null) {
						try {
							statement.close();
						} catch (SQLException e) {
						}
					}
					if (rs != null) {
						try {
							rs.close();
						} catch (SQLException e) {
						}
					}
					GlobalCache.connnection_pool_2.putConnection(connection);
				}
				// ************ Checking For Loading Counter *******************
				if (!localDownCount.isEmpty()) {
					logger.info("SmscDownCounter: " + localDownCount);
					for (int group_id : localDownCount.keySet()) {
						Map<String, Integer> smsc_down = localDownCount.get(group_id);
						for (Map.Entry<String, Integer> entry : smsc_down.entrySet()) {
							String smsc = entry.getKey();
							try {
								int downCounter = entry.getValue();
								int to_be_load_counter = 0;
								if (GlobalCache.SmscConnectionSet.contains(smsc)) {
									int smsc_live_queue_size = 0;
									for (int j = 1; j <= Constants.noofqueue; j++) {
										smsc_live_queue_size += ((PriorityQueue) GlobalCache.SmscQueueCache
												.get(smsc)).PQueue[j].size();
									}
									to_be_load_counter = LOAD_LIMIT - smsc_live_queue_size;
									if (downCounter < to_be_load_counter) {
										to_be_load_counter = downCounter;
									}
								} else {
									if (group_id > 0) {
										Set<String> members = DistributionGroupManager.listMembers(group_id);
										boolean isActiveMember = false;
										for (String member : members) {
											if (!member.equalsIgnoreCase(smsc)) {
												if (GlobalCache.SmscConnectionSet.contains(member)) {
													logger.info("Down " + smsc + " Group Member Active: " + member);
													isActiveMember = true;
													break;
												}
											}
										}
										if (isActiveMember) {
											int smsc_live_queue_size = 0;
											if (GlobalCache.SmscQueueCache.containsKey(smsc)) {
												for (int j = 1; j <= Constants.noofqueue; j++) {
													smsc_live_queue_size += ((PriorityQueue) GlobalCache.SmscQueueCache
															.get(smsc)).PQueue[j].size();
												}
											}
											to_be_load_counter = LOAD_LIMIT - smsc_live_queue_size;
											if (downCounter < to_be_load_counter) {
												to_be_load_counter = downCounter;
											}
										} else {
											logger.info("Down " + smsc + " No Active Group Member Found");
										}
									}
								}
								if (to_be_load_counter > 0) {
									Map<String, Integer> smsc_to_be_load = null;
									if (localLoadCount.containsKey(group_id)) {
										smsc_to_be_load = localLoadCount.get(group_id);
									} else {
										smsc_to_be_load = new HashMap<String, Integer>();
									}
									smsc_to_be_load.put(smsc, to_be_load_counter);
									localLoadCount.put(group_id, smsc_to_be_load);
								}
							} catch (Exception e) {
								logger.error(group_id + "[" + smsc + "]", e.fillInStackTrace());
							}
						}
					}
					localDownCount.clear();
				}
				// ****************** Start Loading From Database ***********************
				if (!localLoadCount.isEmpty()) {
					logger.info("SmscQueueLoadCounter: " + localLoadCount);
					for (Map.Entry<Integer, Map<String, Integer>> group_entry : localLoadCount.entrySet()) {
						int group_id = group_entry.getKey();
						Map<String, Integer> smsc_to_be_load = group_entry.getValue();
						for (Map.Entry<String, Integer> entry : smsc_to_be_load.entrySet()) {
							logger.info(group_id + "[" + entry.getKey() + "] Loading Queue Size: " + entry.getValue());
							String sql = "select * from smsc_in where group_id='" + group_id + "' and smsc = '"
									+ entry.getKey()
									+ "' and s_flag = 'Q' order by msg_id,username,destination_no limit "
									+ entry.getValue();
							try {
								connection = GlobalCache.connnection_pool_2.getConnection();
								statement = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
										java.sql.ResultSet.CONCUR_READ_ONLY);
								statement.setFetchSize(Integer.MIN_VALUE);
								rs = statement.executeQuery(sql);
								String content = null, msg_type = null, esm_class = null, dcs = null;
								// double cost = 0;
								ConcatUnicode concate = null;
								String msg_id = null;
								while (rs.next()) {
									try {
										msg_id = rs.getString("msg_id");
										content = rs.getString("content");
										esm_class = rs.getString("esm_class");
										dcs = rs.getString("dcs");
										concate = new ConcatUnicode();
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
										SubmitSM submit_sm = new SubmitSM();
										int intdcs = Integer.parseInt(dcs.trim());
										submit_sm.setDataCoding((byte) intdcs);
										submit_sm.setDestAddr(Byte.parseByte(rs.getString("dest_ton")),
												Byte.parseByte(rs.getString("dest_npi")),
												rs.getString("destination_no"));
										submit_sm.setSourceAddr(Byte.parseByte(rs.getString("sour_ton")),
												Byte.parseByte(rs.getString("sour_npi")), rs.getString("source_no"));
										submit_sm.setEsmClass(Byte.parseByte(esm_class));
										submit_sm.setSequenceNumber(Integer.parseInt(rs.getString("seq_no")));
										submit_sm.setRegisteredDelivery(Byte.parseByte(rs.getString("registered")));
										try {
											submit_sm.setValidityPeriod(rs.getString("validity_period"));
										} catch (WrongDateFormatException we) {
											logger.error(msg_id + " Invalid Validity Period Found: "
													+ rs.getString("validity_period"));
										}
										if ((intdcs == 0) || (intdcs == 1) || (intdcs == 240)) { // 7-bit
											if (esm_class.equals("64")) {
												ByteBuffer bfr = concate.getByteBuffer();
												submit_sm.setBody(bfr); // Concatenated
											} else {
												submit_sm.setShortMessage(content); // Single
											}
										} else if (esm_class.equals("64") && intdcs == 8) { // Concatenated Unicode
											ByteBuffer bfr = concate.getByteBuffer();
											submit_sm.setBody(bfr);
										} else if (esm_class.equals("64") && intdcs == 245) {
											if (msg_type.equals("buffer")) {
												ByteBuffer bfr = concate.getByteBuffer(); // for ringtone and wap push
												submit_sm.setBody(bfr);
											} else {
												submit_sm.setShortMessage(concate.getMsg());
											}
										} else { // Unicode single
											content = Converter.getUnicode(content.toCharArray());
											submit_sm.setShortMessage(content, Data.ENC_UTF16_BE);
										}
										RoutePDU pdu = new RoutePDU((Request) submit_sm, msg_id,
												Integer.parseInt(rs.getString("seq_no")),
												String.valueOf(rs.getInt("session_id")), rs.getInt("Priority"));
										pdu.setSmsc(rs.getString("smsc"));
										pdu.setUsername(rs.getString("username"));
										pdu.setBackupSmsc(rs.getString("Secondry_smsc_id"));
										// pdu.setRegisterdlrValue(Byte.parseByte(rs.getString("registered")));
										pdu.setCost(rs.getDouble("cost"));
										try {
											pdu.setNetworkId(Integer.parseInt(rs.getString("oprCountry")));
										} catch (Exception ex) {
											pdu.setNetworkId(0);
										}
										pdu.setTime(rs.getString("time"));
										if (rs.getInt("refund") == 2) {
											pdu.setRefund(true);
											pdu.setDeduct(true);
										} else {
											pdu.setRefund(rs.getBoolean("refund"));
										}
										pdu.setOriginalSourceAddr(rs.getString("orig_source"));
										pdu.setRegisterSender(rs.getBoolean("is_reg_sender"));
										pdu.setGroupId(rs.getInt("group_id"));
										// tracklogger.debug(pdu.getHtiMsgId() + " localQueue: " + pdu.getUsername() + " ["+ pdu.getSmsc() + "]");
										if (rs.getBoolean("opt_param")) {
											OptionalParamSet.add(msg_id);
										}
										localQueue.put(msg_id, pdu);
										// CflagCache.add(msg_id);
										pdu = null;
									} catch (Exception ex) {
										UNflagCache.add(msg_id);
										logger.error(msg_id + " <- UNSUPPORTED MESSAGE TYPE -> ",
												ex.fillInStackTrace());
									}
								}
							} catch (Exception e) {
								logger.error(entry.getKey(), e.fillInStackTrace());
							} finally {
								if (statement != null) {
									try {
										statement.close();
									} catch (SQLException e) {
									}
								}
								if (rs != null) {
									try {
										rs.close();
									} catch (SQLException e) {
									}
								}
								GlobalCache.connnection_pool_2.putConnection(connection);
							}
							// ************* Update Flags ***************************************
							if (!localQueue.isEmpty()) {
								int c_update_count = 0;
								try {
									connection = GlobalCache.connnection_pool_2.getConnection();
									preStatement = connection
											.prepareStatement("update smsc_in set s_flag='C' where msg_id in ("
													+ String.join(",", localQueue.keySet()) + ")");
									c_update_count = preStatement.executeUpdate();
								} catch (Exception e) {
									logger.error(entry.getKey(), e.fillInStackTrace());
								} finally {
									if (preStatement != null) {
										try {
											preStatement.close();
										} catch (SQLException e) {
										}
									}
									GlobalCache.connnection_pool_2.putConnection(connection);
								}
								logger.info(entry.getKey() + " <- C Flagged Records Update[" + c_update_count
										+ "] Finished-> ");
								// CflagCache.clear();
							}
							if (!UNflagCache.isEmpty()) {
								int un_update_count = 0;
								try {
									connection = GlobalCache.connnection_pool_2.getConnection();
									preStatement = connection
											.prepareStatement("update smsc_in set s_flag='UN' where msg_id in ("
													+ String.join(",", UNflagCache) + ")");
									un_update_count = preStatement.executeUpdate();
								} catch (Exception e) {
									logger.error(entry.getKey(), e.fillInStackTrace());
								} finally {
									if (preStatement != null) {
										try {
											preStatement.close();
										} catch (SQLException e) {
										}
									}
									GlobalCache.connnection_pool_2.putConnection(connection);
								}
								logger.info(entry.getKey() + " <- UN Flagged Records Update[" + un_update_count
										+ "] Finished-> ");
								UNflagCache.clear();
							}
							if (!OptionalParamSet.isEmpty()) {
								logger.info("<--- Checking For Extra Optional Params[" + OptionalParamSet.size()
										+ "] --->");
								try {
									connection = GlobalCache.connnection_pool_2.getConnection();
									statement = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
											java.sql.ResultSet.CONCUR_READ_ONLY);
									statement.setFetchSize(Integer.MIN_VALUE);
									rs = statement.executeQuery("select * from sm_opt_param where msg_id in("
											+ String.join(",", OptionalParamSet) + ")");
									while (rs.next()) {
										String msg_id = rs.getString("msg_id");
										TLV peId = null, templateId = null, tmId = null, channel_type = null,
												caption = null;
										if (rs.getBlob("pe_id") != null) {
											InputStream is = rs.getBlob("pe_id").getBinaryStream(); // this mthd give/get input stream...
											if (is.available() > 0) {
												ObjectInputStream oip = new ObjectInputStream(is);
												Object obj = oip.readObject();
												is.close();
												oip.close();
												peId = (TLV) obj;
											}
										}
										if (rs.getBlob("template_id") != null) {
											InputStream is = rs.getBlob("template_id").getBinaryStream(); // this mthd give/get input stream...
											if (is.available() > 0) {
												ObjectInputStream oip = new ObjectInputStream(is);
												Object obj = oip.readObject();
												is.close();
												oip.close();
												templateId = (TLV) obj;
											}
										}
										if (rs.getBlob("tm_id") != null) {
											InputStream is = rs.getBlob("tm_id").getBinaryStream(); // this mthd give/get input stream...
											if (is.available() > 0) {
												ObjectInputStream oip = new ObjectInputStream(is);
												Object obj = oip.readObject();
												is.close();
												oip.close();
												tmId = (TLV) obj;
											}
										}
										if (rs.getBlob("channel_type") != null) {
											InputStream is = rs.getBlob("channel_type").getBinaryStream(); // this mthd give/get input stream...
											if (is.available() > 0) {
												ObjectInputStream oip = new ObjectInputStream(is);
												Object obj = oip.readObject();
												is.close();
												oip.close();
												channel_type = (TLV) obj;
											}
										}
										if (rs.getBlob("caption") != null) {
											InputStream is = rs.getBlob("caption").getBinaryStream(); // this mthd give/get input stream...
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
									logger.error(entry.getKey(), e.fillInStackTrace());
								} finally {
									if (statement != null) {
										try {
											statement.close();
										} catch (SQLException e) {
										}
									}
									if (rs != null) {
										try {
											rs.close();
										} catch (SQLException e) {
										}
									}
									GlobalCache.connnection_pool_2.putConnection(connection);
								}
								OptionalParamSet.clear();
							}
							// **************** Put To processing Queue *************************
							for (RoutePDU routePDU : localQueue.values()) {
								GlobalQueue.interProcessRequest.enqueue(routePDU);
							}
							localQueue.clear();
							// ************* Finished For One Smsc ******************************
						}
					}
					localLoadCount.clear();
				}
			} else {
				logger.info(" <-- Queue Load Process on Hold --> ");
			}
		}
		logger.info("HtiQueueLoader Stopped");
	}

	public void stop() {
		logger.info("HtiQueueLoader Stopping");
		stop = true;
	}
}
