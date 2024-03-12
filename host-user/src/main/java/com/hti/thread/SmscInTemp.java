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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.DatabaseDumpObject;
import com.hti.objects.ReportLogObject;
import com.hti.objects.RoutePDU;
import com.hti.objects.SmOptParamEntry;
import com.hti.objects.SmscInObj;
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
 * @author
 */
public class SmscInTemp implements Runnable {
	private DatabaseDumpObject database_dump_object = null;
	private Connection connection = null;
	private PreparedStatement statement = null;
	private RoutePDU route = null;
	private Request request = null;
	private ConcatUnicode concate = null;
	private ByteBuffer buffer = null;
	private String content = "";
	private String msgType = "";
	private int esm = 0;
	private int dcs = 0;
	private int count;
	// private static boolean doWait = false;
	private String messageid = null;
	public static int totalCount = 0;
	private Logger logger = LoggerFactory.getLogger("dbLogger");
	private Map<String, String> msgHeaders = new HashMap<String, String>();
	private Map<String, Long> waitingQueue = new HashMap<String, Long>();
	private Map<String, SmscInObj> waitingObjectQueue = new HashMap<String, SmscInObj>();
	private Iterator<Map.Entry<String, Long>> wait_itr = null;
	private int temp_Queue_counter;
	private int LOOP_COUNTER = 0;
	// private int h_flag_counter;
	// private int discard_counter;
	public static boolean INITIALIZE = true;
	private Set<String> enqueueSet = new HashSet<String>();
	private boolean stop;
	private Calendar nextClearTime;
	private Set<String> OptionalParamSet = new HashSet<String>();

	public SmscInTemp() {
		logger.info("SmscInTemp Thread Starting");
		msgHeaders.put("0B0504158100000003", "concateRingTone");
		msgHeaders.put("06050415811581", "singleRingTone");
		msgHeaders.put("0B0504158A00000003", "concatePicMsg");
		msgHeaders.put("060504158A158A", "singlePicMsg");
		msgHeaders.put("06050415821582", "singleOptLogo");
		msgHeaders.put("0B0504158200000003", "concateOptLogo");
		msgHeaders.put("0B050423F400000003", "concateVCard");
		msgHeaders.put("06050423F40000", "singleVCard");
		msgHeaders.put("0605040B8423F0", "singleWapPush");
		msgHeaders.put("0B05040B8400000003", "concateWapPush");
	}

	private void init() {
		String temp_sql = "delete from smsc_in_temp where msg_id in(select msg_id from smsc_in where server_id="
				+ GlobalVars.SERVER_ID + ")";
		Statement temp_statement = null;
		Connection temp_connection = null;
		// ResultSet temp_rs = null;
		try {
			temp_connection = GlobalCache.connection_pool_proc.getConnection();
			temp_statement = temp_connection.createStatement();
			int delete_counter = temp_statement.executeUpdate(temp_sql);
			logger.info("Total Records Deleted From Temp Smsc_in: " + delete_counter);
		} catch (Exception ex) {
			logger.error("init()", ex.fillInStackTrace());
		} finally {
			if (temp_statement != null) {
				try {
					temp_statement.close();
				} catch (SQLException ex) {
					temp_statement = null;
				}
			}
			if (temp_connection != null) {
				GlobalCache.connection_pool_proc.putConnection(temp_connection);
				temp_connection = null;
			}
		}
	}

	private int hlrPending(String backdate) {
		String h_sql = null;
		if (backdate != null) {
			h_sql = "select count(msg_id) as count from smsc_in_temp where server_id=" + GlobalVars.SERVER_ID
					+ " and s_flag='H' and time < '" + backdate + "'";
		} else {
			h_sql = "select count(msg_id) as count from smsc_in_temp where server_id=" + GlobalVars.SERVER_ID
					+ " and s_flag='H'";
		}
		Statement temp_statement = null;
		Connection temp_connection = null;
		ResultSet temp_rs = null;
		int to_be_return = 0;
		try {
			temp_connection = GlobalCache.connection_pool_proc.getConnection();
			temp_statement = temp_connection.createStatement();
			temp_rs = temp_statement.executeQuery(h_sql);
			if (temp_rs.next()) {
				to_be_return = temp_rs.getInt("count");
			}
			if (to_be_return > 0) {
				logger.info("Lookup Pending Counter: " + to_be_return);
			}
		} catch (Exception ex) {
			logger.error("hlrPending()", ex.fillInStackTrace());
		} finally {
			if (temp_rs != null) {
				try {
					temp_rs.close();
				} catch (SQLException ex) {
					temp_rs = null;
				}
			}
			if (temp_statement != null) {
				try {
					temp_statement.close();
				} catch (SQLException ex) {
					temp_statement = null;
				}
			}
			if (temp_connection != null) {
				GlobalCache.connection_pool_proc.putConnection(temp_connection);
				temp_connection = null;
			}
		}
		return to_be_return;
	}

	private int countPending() {
		logger.debug("<-- Checking For Temparory Pending Counter -->");
		String h_sql = "select count(msg_id) as count from smsc_in_temp where s_flag not in('H')";
		Statement temp_statement = null;
		Connection temp_connection = null;
		ResultSet temp_rs = null;
		int to_be_return = 0;
		try {
			temp_connection = GlobalCache.connection_pool_proc.getConnection();
			temp_statement = temp_connection.createStatement();
			temp_rs = temp_statement.executeQuery(h_sql);
			if (temp_rs.next()) {
				temp_Queue_counter = temp_rs.getInt("count");
			}
			if (temp_Queue_counter > 0) {
				logger.debug("Temparory Pending Counter: " + temp_Queue_counter);
			}
		} catch (Exception ex) {
			logger.error("countPending()", ex.fillInStackTrace());
		} finally {
			if (temp_rs != null) {
				try {
					temp_rs.close();
				} catch (SQLException ex) {
					temp_rs = null;
				}
			}
			if (temp_statement != null) {
				try {
					temp_statement.close();
				} catch (SQLException ex) {
					temp_statement = null;
				}
			}
			if (temp_connection != null) {
				GlobalCache.connection_pool_proc.putConnection(temp_connection);
				temp_connection = null;
			}
		}
		return to_be_return;
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				if (INITIALIZE) {
					init();
					nextClearTime = Calendar.getInstance();
					nextClearTime.add(Calendar.SECOND, +3);
					countPending();
					INITIALIZE = false;
				}
				// -------------------
				if (nextClearTime.getTime().before(new Date())) {
					nextClearTime.add(Calendar.SECOND, +3);
					Calendar calendar = Calendar.getInstance();
					calendar.add(Calendar.MINUTE, -3);
					String backdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
					if (hlrPending(backdate) > 0) {
						clearHlrPendings(backdate);
					}
					if (!waitingQueue.isEmpty()) {
						wait_itr = waitingQueue.entrySet().iterator();
						logger.debug("Smsc In Temp Update waiting Queue : " + waitingQueue.size());
						Map.Entry<String, Long> entry;
						while (wait_itr.hasNext()) {
							entry = wait_itr.next();
							if (System.currentTimeMillis() >= entry.getValue()) {
								String messageid = entry.getKey();
								if (waitingObjectQueue.containsKey(messageid)) {
									GlobalQueue.smsc_in_temp_update_Queue.enqueue(waitingObjectQueue.remove(messageid));
								}
								logger.debug(messageid + " Enqueued to update");
								enqueueSet.add(messageid);
								wait_itr.remove();
							}
						}
					}
					// temp_Queue_counter = 0;
				}
				if (++LOOP_COUNTER >= 5) {
					LOOP_COUNTER = 0;
					countPending();
				}
				// -------------------
				if (!GlobalQueue.smsc_in_temp_Queue.isEmpty()) {
					logger.debug("smsc_in_temp_Queue:-> " + GlobalQueue.smsc_in_temp_Queue.size());
					count = 0;
					// doWait = true;
					// List tempList = new ArrayList();
					String received_time = null, username = null;
					// String cost = "0";
					String sql = "insert ignore into smsc_in_temp(msg_id,time ,seq_no ,content,dest_ton,dest_npi,destination_no ,oprCountry,sour_ton,sour_npi,source_no,registered,esm_class,"
							+ "dcs,cost,username,s_flag,smsc,Priority,session_id,Secondry_smsc_id,msg_object,msg_type,refund,validity_period,orig_source,is_reg_sender,server_id,group_id,opt_param)"
							+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					try {
						connection = GlobalCache.connection_pool_proc.getConnection();
						statement = connection.prepareStatement(sql);
						connection.setAutoCommit(false);
						while (!GlobalQueue.smsc_in_temp_Queue.isEmpty()) {
							try {
								database_dump_object = (DatabaseDumpObject) GlobalQueue.smsc_in_temp_Queue.dequeue();
								if (database_dump_object != null) {
									route = database_dump_object.getRoute();
									String s_flag = database_dump_object.getS_flag();
									received_time = route.getTime();
									username = route.getUsername();
									// cost = database_dump_object.getCost();
									totalCount++;
									// SMPPAPPLICATION.smsccounter++;
									request = route.getRequestPDU();
									messageid = route.getHtiMsgId();
									statement.setString(1, messageid);
									statement.setString(2, received_time);
									statement.setInt(3, database_dump_object.getSeqNo());
									statement.setString(4, database_dump_object.getContent());
									statement.setInt(5, database_dump_object.getDestinationTON());
									statement.setInt(6, database_dump_object.getDestinationNPI());
									statement.setString(7, database_dump_object.getDestinationNo());
									statement.setInt(8, route.getNetworkId());
									statement.setInt(9, database_dump_object.getSource_TON());
									statement.setInt(10, database_dump_object.getSource_NPI());
									statement.setString(11, database_dump_object.getFrom());
									statement.setByte(12, database_dump_object.isRegistered());
									statement.setInt(13, database_dump_object.getEsm());
									statement.setInt(14, database_dump_object.getDcs());
									statement.setString(15, route.getCost() + "");
									statement.setString(16, username);
									statement.setString(17, database_dump_object.getS_flag());
									statement.setString(18, route.getSmsc());
									statement.setInt(19, route.getPriority());
									statement.setString(20, route.getSessionId());
									statement.setString(21, route.getBackupSmsc());
									esm = database_dump_object.getEsm();
									dcs = database_dump_object.getDcs();
									concate = new ConcatUnicode();
									if (esm == 64 && dcs == 0) {
										buffer = ((SubmitSM) request).getBody();
										concate.setByteBuffer(buffer);
										msgType = "buffer";
									} else if (esm == 64 && dcs == 8) {
										buffer = ((SubmitSM) request).getBody();
										concate.setByteBuffer(buffer);
										msgType = "buffer";
									} else if (esm == 64 && dcs == 245) {
										String foundMsgType = "";
										buffer = ((SubmitSM) request).getBody();
										String bufferHexa = buffer.getHexDump();
										for (String head : msgHeaders.keySet()) {
											bufferHexa = bufferHexa.toUpperCase();
											if (bufferHexa.contains(head)) {
												foundMsgType = (String) msgHeaders.get(head); // see the list of
												break;
											}
										}
										if (foundMsgType.length() > 0 && (foundMsgType.equals("singleOptLogo")
												|| foundMsgType.equals("concateOptLogo"))) {
											msgType = "short";
											content = ((SubmitSM) request).getShortMessage(Data.ENC_ISO8859_1);
											concate.setMsg(content);
										} else {
											msgType = "buffer";
											concate.setByteBuffer(buffer);
										}
									}
									if (msgType.length() > 0) {
										statement.setObject(22, concate);
										statement.setString(23, msgType);
									} else {
										statement.setObject(22, "");
										statement.setString(23, "");
									}
									statement.setBoolean(24, route.isRefund());
									String validity_period = ((SubmitSM) request).getValidityPeriod();
									if (validity_period != null && validity_period.length() == 0) {
										validity_period = null;
									}
									statement.setString(25, validity_period);
									statement.setString(26, route.getOriginalSourceAddr());
									statement.setBoolean(27, route.isRegisterSender());
									statement.setInt(28, GlobalVars.SERVER_ID);
									statement.setInt(29, route.getGroupId());
									if (((SubmitSM) request).getExtraOptional((short) 0x1400) != null
											|| ((SubmitSM) request).getExtraOptional((short) 0x1500) != null) {
										statement.setBoolean(30, true);
									} else {
										statement.setBoolean(30, false);
									}
									statement.addBatch();
									SmOptParamEntry optionalEntry = null;
									try {
										if (((SubmitSM) request).getExtraOptional((short) 0x1400) != null) {
											optionalEntry = new SmOptParamEntry(
													((SubmitSM) request).getExtraOptional((short) 0x1400),
													((SubmitSM) request).getExtraOptional((short) 0x1401),
													((SubmitSM) request).getExtraOptional((short) 0x1402), messageid);
										}
									} catch (Exception e) {
										logger.error(messageid + " extraoptional1", e);
									}
									try {
										if (((SubmitSM) request).getExtraOptional((short) 0x1500) != null) {
											if (optionalEntry == null) {
												optionalEntry = new SmOptParamEntry(
														((SubmitSM) request).getExtraOptional((short) 0x1500),
														((SubmitSM) request).getExtraOptional((short) 0x1501),
														messageid);
											} else {
												optionalEntry.setChannelType(
														((SubmitSM) request).getExtraOptional((short) 0x1500));
												optionalEntry.setCaption(
														((SubmitSM) request).getExtraOptional((short) 0x1501));
											}
										}
									} catch (Exception e) {
										logger.error(messageid + " extraoptional2", e);
									}
									if (optionalEntry != null) {
										SmOptParamInsert.submitRequests.enqueue(optionalEntry);
									}
									msgType = "";
									route = null;
									request = null;
									database_dump_object = null;
									concate = null;
									if (++count >= 1000) {
										break;
									}
								}
							} catch (Exception ex) {
								logger.error("Insert[1]", ex.fillInStackTrace());
							}
						}
						if (count > 0) {
							statement.executeBatch();
							connection.commit();
						}
						/*
						 * SmscStatus.insertSmscin.addAll(set); set.clear();
						 */
					} catch (Exception ex) {
						logger.error("Insert[2]", ex.fillInStackTrace());
					} finally {
						if (statement != null) {
							try {
								statement.close();
							} catch (SQLException ex) {
								statement = null;
							}
						}
						if (connection != null) {
							GlobalCache.connection_pool_proc.putConnection(connection);
							connection = null;
						}
					}
				}
				if (!GlobalQueue.smsc_in_temp_update_Queue.isEmpty()) {
					logger.debug("smsc_in_temp_update_Queue:-> " + GlobalQueue.smsc_in_temp_update_Queue.size());
					String temp_sql = "update smsc_in_temp set s_flag=?,smsc=?,group_id=?,cost=?,oprcountry=? where msg_id =? ";
					PreparedStatement temp_statement = null;
					Connection temp_connection = null;
					Map<Integer, SmscInObj> tempMap = new HashMap<Integer, SmscInObj>();
					try {
						temp_connection = GlobalCache.connection_pool_proc.getConnection();
						temp_statement = temp_connection.prepareStatement(temp_sql);
						temp_connection.setAutoCommit(false);
						int update_counter = 0;
						while (!GlobalQueue.smsc_in_temp_update_Queue.isEmpty()) {
							SmscInObj smscin_obj = (SmscInObj) GlobalQueue.smsc_in_temp_update_Queue.dequeue();
							temp_statement.setString(1, smscin_obj.getFlag());
							temp_statement.setString(2, smscin_obj.getSmsc());
							temp_statement.setInt(3, smscin_obj.getGroupId());
							temp_statement.setDouble(4, smscin_obj.getCost());
							temp_statement.setInt(5, smscin_obj.getNetworkId());
							temp_statement.setString(6, smscin_obj.getMsgid());
							temp_statement.addBatch();
							tempMap.put(update_counter, smscin_obj);
							if (++update_counter > 1000) {
								break;
							}
						}
						if (update_counter > 0) {
							int[] rowup = temp_statement.executeBatch();
							temp_connection.commit();
							for (int i = 0; i < rowup.length; i++) {
								SmscInObj smsc_in_obj = (SmscInObj) tempMap.remove(i);
								String messageid = smsc_in_obj.getMsgid();
								if (rowup[i] < 1) {
									if (enqueueSet.contains(messageid)) {
										enqueueSet.remove(messageid);
										// discard_counter++;
									} else {
										waitingQueue.put(messageid, System.currentTimeMillis() + (60 * 1000));
										waitingObjectQueue.put(messageid, smsc_in_obj);
									}
									update_counter--;
								} else {
									if (enqueueSet.contains(messageid)) {
										enqueueSet.remove(messageid);
									}
								}
								smsc_in_obj = null;
							}
							// logger.info("Temp Queue Update Count: ==> " + update_count.length);
						}
					} catch (Exception ex) {
						logger.error("Temp Update", ex.fillInStackTrace());
					} finally {
						if (temp_statement != null) {
							try {
								temp_statement.close();
							} catch (SQLException ex) {
								temp_statement = null;
							}
						}
						if (temp_connection != null) {
							GlobalCache.connection_pool_proc.putConnection(temp_connection);
							temp_connection = null;
						}
					}
				}
				if (temp_Queue_counter > 0) {
					if (GlobalVars.SMPP_STATUS && GlobalVars.OMQ_PDU_STATUS && !GlobalVars.HOLD_ON_TRAFFIC) {
						logger.debug("<-- Processing Temporary Pendings[" + temp_Queue_counter + "]-->");
						// logger.info("Temporary Queue Counter: " + temp_Queue_counter);
						String temp_sql = "select * from smsc_in_temp where server_id=" + GlobalVars.SERVER_ID
								+ " and s_flag not in('H') order by msg_id,username,destination_no limit 1000";
						// System.out.println("SQL: " + temp_sql);
						Statement temp_statement = null;
						Connection temp_connection = null;
						ResultSet temp_rs = null;
						Map<String, RoutePDU> localQueue = new LinkedHashMap<String, RoutePDU>();
						Map<String, ReportLogObject> reportQueue = new LinkedHashMap<String, ReportLogObject>();
						List<String> msg_id_list = new ArrayList<String>();
						// Set<String> delete_list = new HashSet<String>();
						try {
							temp_connection = GlobalCache.connection_pool_proc.getConnection();
							temp_statement = temp_connection.createStatement();
							temp_rs = temp_statement.executeQuery(temp_sql);
							String time = null, msg_id = null, seq_no = null, content = null, dest_ton = null,
									dest_npi = null, destination_no = null, oprCountry = null, sour_ton = null,
									sour_npi = null;
							String msg_type = null, source_no = null, registered = null, esm_class = null, dcs = null,
									username = null, smsc = null, status = null;
							double cost = 0;
							int Priority = 3, session_id = 0;
							boolean refund = false;
							ConcatUnicode concate = null;
							// String msg_id_list = "";
							boolean is_reg_sender = false;
							boolean opt_param = false;
							while (temp_rs.next()) {
								boolean proceed = true;
								try {
									msg_id = temp_rs.getString("msg_id");
									// System.out.println("msgId[" + msg_id + "] Processing");
									time = temp_rs.getString("time");
									seq_no = temp_rs.getString("seq_no");
									content = temp_rs.getString("content");
									dest_ton = temp_rs.getString("dest_ton");
									dest_npi = temp_rs.getString("dest_npi");
									destination_no = temp_rs.getString("destination_no");
									oprCountry = temp_rs.getString("oprCountry");
									sour_ton = temp_rs.getString("sour_ton");
									sour_npi = temp_rs.getString("sour_npi");
									source_no = temp_rs.getString("source_no");
									registered = temp_rs.getString("registered");
									esm_class = temp_rs.getString("esm_class");
									dcs = temp_rs.getString("dcs");
									cost = temp_rs.getDouble("cost");
									username = temp_rs.getString("username");
									// String status = rs.getString("status");
									String s_flag = temp_rs.getString("s_flag");
									smsc = temp_rs.getString("smsc");
									Priority = temp_rs.getInt("Priority");
									session_id = temp_rs.getInt("session_id");
									is_reg_sender = temp_rs.getBoolean("is_reg_sender");
									concate = new ConcatUnicode();
									if (temp_rs.getBlob("msg_object") != null) {
										InputStream is = temp_rs.getBlob("msg_object").getBinaryStream(); // this mthd give/get input stream...
										if (is.available() > 0) {
											ObjectInputStream oip = new ObjectInputStream(is);
											Object obj = oip.readObject();
											is.close();
											oip.close();
											concate = (ConcatUnicode) obj;
										}
									}
									msg_type = temp_rs.getString("msg_type");
									refund = temp_rs.getBoolean("refund");
									String validity_period = temp_rs.getString("validity_period");
									String orig_source_adrr = temp_rs.getString("orig_source");
									// System.out.println("Messsage Type: " + msg_type + " " + proceed);
									if (proceed) {
										// System.out.println(msg_id + " esm_class1:" + esm_class + " " + dcs);
										SubmitSM submit_sm = new SubmitSM();
										// System.out.println(msg_id + " esm_class2:" + esm_class + " " + dcs);
										int intdcs = Integer.parseInt(dcs.trim());
										submit_sm.setDataCoding((byte) intdcs);
										// submit_sm.setDataCoding(Byte.parseByte(dcs));
										submit_sm.setDestAddr(Byte.parseByte(dest_ton), Byte.parseByte(dest_npi),
												destination_no);
										submit_sm.setSourceAddr(Byte.parseByte(sour_ton), Byte.parseByte(sour_npi),
												source_no);
										submit_sm.setEsmClass(Byte.parseByte(esm_class));
										submit_sm.setSequenceNumber(Integer.parseInt(seq_no));
										submit_sm.setRegisteredDelivery(Byte.parseByte(registered));
										try {
											submit_sm.setValidityPeriod(validity_period);
										} catch (WrongDateFormatException we) {
											logger.error(msg_id + " Invalid Validity Period Found: " + validity_period);
										}
										// System.out.println("MessageID ===> " + msg_id);
										// System.out.println(msg_id + " esm_class3:" + esm_class + " " + intdcs);
										if ((intdcs == 0) || (intdcs == 1) || (intdcs == 240)) {
											if (esm_class.equals("64")) {
												ByteBuffer bfr = concate.getByteBuffer();
												submit_sm.setBody(bfr); // Concatinated English
											} else {
												submit_sm.setShortMessage(content); // Single english,flash
											}
										} else if (esm_class.equals("64") && intdcs == 8) {// concate unicode and may be concate english if dcs isnot
																							// zero
											// System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>Concat UNICODE");
											ByteBuffer bfr = concate.getByteBuffer();
											submit_sm.setBody(bfr);
										} else if (esm_class.equals("64") && intdcs == 245) {
											if (msg_type.equals("buffer")) {
												// System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>RINGTONE");
												ByteBuffer bfr = concate.getByteBuffer(); // for ringtone and wap push
												submit_sm.setBody(bfr);
											} else {
												submit_sm.setShortMessage(concate.getMsg());
											}
										} else { // for Unicode single msg
											content = Converter.getUnicode(content.toCharArray());
											submit_sm.setShortMessage(content, Data.ENC_UTF16_BE);
										}
										// System.out.println(msg_id + " Creating RoutePDU");
										RoutePDU pdu = new RoutePDU((Request) submit_sm, msg_id,
												Integer.parseInt(seq_no), String.valueOf(session_id), Priority);
										pdu.setSmsc(smsc);
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
										pdu.setGroupId(temp_rs.getInt("group_id"));
										// System.out.println("S_flag: " + msg_id + " " + s_flag);
										if (s_flag != null) {
											if (s_flag.equalsIgnoreCase("C")) {
												if (temp_rs.getBoolean("opt_param")) {
													OptionalParamSet.add(msg_id);
													opt_param = true;
												}
												localQueue.put(msg_id, pdu);
											} else {
												status = s_flag;
											}
										}
										pdu = null;
									}
								} catch (Exception ex) {
									status = "UN";
									logger.error(msg_id + " <- UNSUPPORTED MESSAGE TYPE -> ", ex);
									String unsupportedSql = "update smsc_in_temp set s_flag='UN' where msg_id ='"
											+ msg_id + "'";
									Connection connection = GlobalCache.connection_pool_proc.getConnection();
									Statement unstatement = null;
									try {
										unstatement = connection.createStatement();
										unstatement.executeUpdate(unsupportedSql);
									} catch (Exception excp) {
										logger.info(
												"<---------------EXCEPTION IN UPDATING UNSUPPORTED MSG-------------->");
									} finally {
										if (unstatement != null) {
											try {
												unstatement.close();
											} catch (SQLException sqle) {
											}
										}
										GlobalCache.connection_pool_proc.putConnection(connection);
									}
								}
								msg_id_list.add(msg_id);
								if (opt_param) {
									reportQueue.put(msg_id, new ReportLogObject(msg_id, oprCountry, username, smsc,
											cost + "", status, time, destination_no, source_no));
								} else {
									GlobalQueue.reportLogQueue.enqueue(new ReportLogObject(msg_id, oprCountry, username,
											smsc, cost + "", status, time, destination_no, source_no));
								}
								temp_Queue_counter--;
							}
						} catch (Exception ex) {
							logger.error("Select Part: " + temp_sql, ex.fillInStackTrace());
						} finally {
							if (temp_rs != null) {
								try {
									temp_rs.close();
								} catch (SQLException ex) {
									temp_rs = null;
								}
							}
							if (temp_statement != null) {
								try {
									temp_statement.close();
								} catch (SQLException ex) {
									temp_statement = null;
								}
							}
							if (temp_connection != null) {
								GlobalCache.connection_pool_proc.putConnection(temp_connection);
								temp_connection = null;
							}
						}
						if (!OptionalParamSet.isEmpty()) {
							try {
								Thread.sleep(500);
							} catch (Exception e1) {
							}
							logger.info(
									"<--- Checking For Extra Optional Params[" + OptionalParamSet.size() + "] --->");
							String msg_id = null;
							try {
								temp_connection = GlobalCache.connection_pool_proc.getConnection();
								temp_statement = temp_connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
										java.sql.ResultSet.CONCUR_READ_ONLY);
								temp_statement.setFetchSize(Integer.MIN_VALUE);
								temp_rs = temp_statement.executeQuery("select * from sm_opt_param where msg_id in("
										+ String.join(",", OptionalParamSet) + ")");
								while (temp_rs.next()) {
									msg_id = temp_rs.getString("msg_id");
									TLV peId = null, templateId = null, tmId = null, channel_type = null,
											caption = null;
									if (temp_rs.getBlob("pe_id") != null) {
										InputStream is = temp_rs.getBlob("pe_id").getBinaryStream(); // this mthd give/get input stream...
										if (is.available() > 0) {
											ObjectInputStream oip = new ObjectInputStream(is);
											Object obj = oip.readObject();
											is.close();
											oip.close();
											peId = (TLV) obj;
										}
									}
									if (temp_rs.getBlob("template_id") != null) {
										InputStream is = temp_rs.getBlob("template_id").getBinaryStream(); // this mthd give/get input stream...
										if (is.available() > 0) {
											ObjectInputStream oip = new ObjectInputStream(is);
											Object obj = oip.readObject();
											is.close();
											oip.close();
											templateId = (TLV) obj;
										}
									}
									if (temp_rs.getBlob("tm_id") != null) {
										InputStream is = temp_rs.getBlob("tm_id").getBinaryStream(); // this mthd give/get input stream...
										if (is.available() > 0) {
											ObjectInputStream oip = new ObjectInputStream(is);
											Object obj = oip.readObject();
											is.close();
											oip.close();
											tmId = (TLV) obj;
										}
									}
									if (temp_rs.getBlob("channel_type") != null) {
										InputStream is = temp_rs.getBlob("channel_type").getBinaryStream(); // this mthd give/get input stream...
										if (is.available() > 0) {
											ObjectInputStream oip = new ObjectInputStream(is);
											Object obj = oip.readObject();
											is.close();
											oip.close();
											channel_type = (TLV) obj;
										}
									}
									if (temp_rs.getBlob("caption") != null) {
										InputStream is = temp_rs.getBlob("caption").getBinaryStream(); // this mthd give/get input stream...
										if (is.available() > 0) {
											ObjectInputStream oip = new ObjectInputStream(is);
											Object obj = oip.readObject();
											is.close();
											oip.close();
											caption = (TLV) obj;
										}
									}
									OptionalParamSet.remove(msg_id);
									GlobalQueue.reportLogQueue.enqueue(reportQueue.remove(msg_id));
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
								logger.error(msg_id, e.fillInStackTrace());
							} finally {
								if (temp_rs != null) {
									try {
										temp_rs.close();
									} catch (SQLException ex) {
										temp_rs = null;
									}
								}
								if (temp_statement != null) {
									try {
										temp_statement.close();
									} catch (SQLException ex) {
										temp_statement = null;
									}
								}
								if (temp_connection != null) {
									GlobalCache.connection_pool_proc.putConnection(temp_connection);
									temp_connection = null;
								}
							}
							//
							if (!OptionalParamSet.isEmpty()) {
								// logger.info("missing dlt params[" + OptionalParamSet.size() + "]: " + OptionalParamSet);
								for (String missing_msg_id : OptionalParamSet) {
									localQueue.remove(missing_msg_id);
									msg_id_list.remove(missing_msg_id);
								}
							}
							reportQueue.clear();
							OptionalParamSet.clear();
						}
						reportQueue = null;
						if (!msg_id_list.isEmpty()) {
							Statement stmt = null;
							try {
								temp_connection = GlobalCache.connection_pool_proc.getConnection();
								stmt = temp_connection.createStatement();
								temp_sql = "insert ignore into smsc_in select * from smsc_in_temp where msg_id in("
										+ String.join(",", msg_id_list) + ")";
								int insert_counter = stmt.executeUpdate(temp_sql);
								logger.debug("Records Inserted From Temp: " + insert_counter);
								temp_sql = "delete from smsc_in_temp where msg_id in(" + String.join(",", msg_id_list)
										+ ")";
								int delete_counter = stmt.executeUpdate(temp_sql);
								logger.debug("Records deleted From Temp: " + delete_counter);
							} catch (Exception e) {
								logger.error("", e.fillInStackTrace());
							} finally {
								if (stmt != null) {
									try {
										stmt.close();
									} catch (SQLException ex) {
										stmt = null;
									}
								}
								if (temp_connection != null) {
									GlobalCache.connection_pool_proc.putConnection(temp_connection);
									temp_connection = null;
								}
							}
							msg_id_list.clear();
						}
						msg_id_list = null;
						if (!localQueue.isEmpty()) {
							// logger.info("Temp Queue Size ===> " + local_Queue.size());
							for (RoutePDU routePDU : localQueue.values()) {
								GlobalQueue.interProcessRequest.enqueue(routePDU);
							}
							localQueue.clear();
						}
						localQueue = null;
					} else {
						// System.out.println("Unable To Process:" + GlobalVars.SMPP_STATUS + GlobalVars.OMQ_PDU_STATUS
						// + GlobalVars.HOLD_ON_TRAFFIC);
					}
				}
			} catch (Exception e) {
				logger.error("", e);
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
			}
		}
		logger.info("SmscInTemp Thread Stopped.Queue: " + GlobalQueue.smsc_in_temp_Queue.size());
	}

	private void clearHlrPendings(String backdate) {
		logger.info("Updating Pending lookup Records");
		Statement temp_statement = null;
		Connection temp_connection = null;
		String h_sql = null;
		if (backdate != null) {
			h_sql = "update smsc_in_temp set smsc='" + com.hti.util.Constants.HLR_DOWN_SMSC_1 + "',group_id=0"
					+ " where server_id=" + GlobalVars.SERVER_ID + " and time < '" + backdate
					+ "' and destination_no in(select destination from lookup_status where server_id="
					+ GlobalVars.SERVER_ID + " and status='UNDELIV')";
		} else {
			h_sql = "update smsc_in_temp set smsc='" + com.hti.util.Constants.HLR_DOWN_SMSC_1 + "',group_id=0"
					+ " where server_id=" + GlobalVars.SERVER_ID
					+ " and destination_no in(select destination from lookup_status where server_id="
					+ GlobalVars.SERVER_ID + " and status='UNDELIV')";
		}
		try {
			temp_connection = GlobalCache.connection_pool_proc.getConnection();
			temp_statement = temp_connection.createStatement();
			int h_update_counter = temp_statement.executeUpdate(h_sql);
			logger.info("Records Updated From lookup_status[1]: " + h_update_counter);
			if (backdate != null) {
				h_sql = "update smsc_in_temp set s_flag='C' where server_id=" + GlobalVars.SERVER_ID
						+ " and s_flag='H' and time < '" + backdate + "'";
			} else {
				h_sql = "update smsc_in_temp set s_flag='C' where server_id=" + GlobalVars.SERVER_ID
						+ " and s_flag='H'";
			}
			h_update_counter = temp_statement.executeUpdate(h_sql);
			logger.info("HlrPending Records Updated in temp: " + h_update_counter);
		} catch (Exception ex) {
			logger.error("Temp H Update", ex.fillInStackTrace());
		} finally {
			if (temp_statement != null) {
				try {
					temp_statement.close();
				} catch (SQLException ex) {
					temp_statement = null;
				}
			}
			if (temp_connection != null) {
				GlobalCache.connection_pool_proc.putConnection(temp_connection);
				temp_connection = null;
			}
		}
	}

	public void stop() {
		logger.info("SmscInTemp Thread Stopping.Queue: " + GlobalQueue.smsc_in_temp_Queue.size());
		stop = true;
	}
}
