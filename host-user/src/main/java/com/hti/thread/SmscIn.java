/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.DatabaseDumpObject;
import com.hti.objects.RoutePDU;
import com.hti.objects.SmOptParamEntry;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;
import com.logica.msgContent.ConcatUnicode;
import com.logica.smpp.Data;
import com.logica.smpp.pdu.Request;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.util.ByteBuffer;
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;

/**
 *
 * @author
 */
public class SmscIn implements Runnable {
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
	private String sql = "insert into smsc_in(msg_id,time ,seq_no ,content,dest_ton,dest_npi,destination_no ,oprCountry,sour_ton,sour_npi,source_no,registered,esm_class,dcs,cost,username,s_flag,smsc,Priority,session_id,Secondry_smsc_id,msg_object,msg_type,refund,validity_period,orig_source,is_reg_sender,server_id,group_id,opt_param) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE s_flag=?";
	// private int count;
	private String messageid = null;
	private Logger logger = LoggerFactory.getLogger("dbLogger");
	private Map<String, String> msgHeaders = new HashMap<String, String>();
	private int insert_count = 0;
	private int total_insert_count = 0;
	public static boolean DELETE_OLD_RECORDS;
	private boolean SHIFT_OLD_RECORDS = true;
	private boolean stop;
	// private Map<String, List<TLV>> OptionalParam = new HashMap<String, List<TLV>>();

	public SmscIn() {
		logger.info("SmscIn Thread Starting");
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

	@Override
	public void run() {
		while (!stop) {
			if (!GlobalQueue.smsc_in_Queue.isEmpty()) {
				logger.debug("SmscInQueue: " + GlobalQueue.smsc_in_Queue.size());
				String received_time = null, username = null;
				String cost = "0";
				int count = 0;
				try {
					connection = GlobalCache.connection_pool_proc.getConnection();
					statement = connection.prepareStatement(sql);
					connection.setAutoCommit(false);
					while (!GlobalQueue.smsc_in_Queue.isEmpty()) {
						// boolean optional_param = false;
						try {
							database_dump_object = (DatabaseDumpObject) GlobalQueue.smsc_in_Queue.dequeue();
							route = database_dump_object.getRoute();
							received_time = route.getTime();
							username = route.getUsername();
							cost = route.getCost() + "";
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
							statement.setString(15, cost);
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
								Iterator<String> headers = msgHeaders.keySet().iterator();
								while (headers.hasNext()) {
									String head = headers.next();
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
							statement.setString(31, database_dump_object.getS_flag());
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
													((SubmitSM) request).getExtraOptional((short) 0x1501), messageid);
										} else {
											optionalEntry.setChannelType(
													((SubmitSM) request).getExtraOptional((short) 0x1500));
											optionalEntry
													.setCaption(((SubmitSM) request).getExtraOptional((short) 0x1501));
										}
									}
								} catch (Exception e) {
									logger.error(messageid + " extraoptional2", e);
								}
								if (optionalEntry != null) {
									SmOptParamInsert.submitRequests.enqueue(optionalEntry);
								}
							total_insert_count++;
							if (++insert_count >= 100) {
								insert_count = 0;
								logger.debug("SmscIn Insert Counter:-> " + total_insert_count);
							}
							if (++count >= 1000) {
								break;
							}
						} catch (Exception ex) {
							String loggedMsg = "MsgID(1): " + messageid;
							logger.error(loggedMsg, ex);
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
					String loggedMsg = "MsgID(2): " + messageid;
					logger.error(loggedMsg, ex);
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
			} else {
				if (DELETE_OLD_RECORDS) {
					Calendar calendar = Calendar.getInstance();
					calendar.add(Calendar.DATE, -3);
					String max_msg_id = new SimpleDateFormat("yyMMdd").format(calendar.getTime());
					max_msg_id = max_msg_id + "0000000000000";
					if (SHIFT_OLD_RECORDS) {
						logger.info("Shifting Unprocessed Smsc_in Records");
						SHIFT_OLD_RECORDS = false;
						String shift_sql = "insert ignore into unprocessed select * from smsc_in where msg_id  < "
								+ max_msg_id;
						logger.info(shift_sql);
						try {
							connection = GlobalCache.connection_pool_proc.getConnection();
							statement = connection.prepareStatement(shift_sql);
							int shifted = statement.executeUpdate();
							logger.info("smsc_in Shift Count : " + shifted);
						} catch (Exception ex) {
							logger.error(" ", ex.fillInStackTrace());
						} finally {
							GlobalCache.connection_pool_proc.putConnection(connection);
							if (statement != null) {
								try {
									statement.close();
								} catch (SQLException ex) {
									statement = null;
								}
							}
						}
					}
					// ----------------------- Start Deletion -----------------------
					String deletesql = "delete from smsc_in where msg_id  < " + max_msg_id + " limit 25000";
					logger.info(deletesql);
					try {
						connection = GlobalCache.connection_pool_proc.getConnection();
						statement = connection.prepareStatement(deletesql);
						int deletecount = statement.executeUpdate();
						logger.info("smsc_in Deletion Count : " + deletecount);
						if (deletecount < 25000) {
							DELETE_OLD_RECORDS = false;
							SHIFT_OLD_RECORDS = true;
							logger.info("<-- No Records To Delete [smsc_in] -->");
						}
					} catch (Exception ex) {
						logger.error("Delete Error: ", ex);
					} finally {
						GlobalCache.connection_pool_proc.putConnection(connection);
						if (statement != null) {
							try {
								statement.close();
							} catch (SQLException ex) {
								statement = null;
							}
						}
					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
				}
			}
		}
		logger.info("SmscIn Thread Stopped.Queue: " + GlobalQueue.smsc_in_Queue.size());
	}

	public void stop() {
		logger.info("SmscIn Thread Stopping.Queue: " + GlobalQueue.smsc_in_Queue.size());
		stop = true;
	}
}
