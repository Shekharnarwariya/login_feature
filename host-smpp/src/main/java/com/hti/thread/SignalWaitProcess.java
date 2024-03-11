package com.hti.thread;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.DatabaseDumpObject;
import com.hti.objects.HTIQueue;
import com.hti.objects.RoutePDU;
import com.hti.objects.SignalRetryObj;
import com.hti.util.Converter;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.logica.msgContent.ConcatUnicode;
import com.logica.smpp.Data;
import com.logica.smpp.pdu.Request;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.WrongDateFormatException;
import com.logica.smpp.util.ByteBuffer;

public class SignalWaitProcess implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private Map<String, Integer> wait_attempt_map = new HashMap<String, Integer>();
	private Map<String, Long> next_attempt_time_map = new HashMap<String, Long>();
	private HTIQueue pdu_log_insert_Queue = new HTIQueue();
	private HTIQueue wait_status_insert_Queue = new HTIQueue();
	private Iterator<Map.Entry<String, Long>> wait_iterator;
	private Map<String, String> msgHeaders = new HashMap<String, String>();
	private Set<String> enqueueSet = new HashSet<String>();
	public static Set<String> DELIVERED_SET = Collections.synchronizedSet(new HashSet<String>());
	private boolean stop;

	public SignalWaitProcess() {
		logger.info("SignalWaitProcess Thread Starting");
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
		getStatusLog();
	}

	private class WaitLogStatus {
		private String messgaeid;
		private int waitAttemptCount;
		private String nextAttemtTime;

		public WaitLogStatus(String messgaeid, int waitAttemptCount, String nextAttemtTime) {
			this.messgaeid = messgaeid;
			this.waitAttemptCount = waitAttemptCount;
			this.nextAttemtTime = nextAttemtTime;
		}

		public String getMessgaeid() {
			return messgaeid;
		}

		public int getWaitAttemptCount() {
			return waitAttemptCount;
		}

		public String getNextAttemtTime() {
			return nextAttemtTime;
		}
	}

	@Override
	public void run() {
		while (!stop) {
			if (!DELIVERED_SET.isEmpty()) {
				Iterator<String> itr = wait_attempt_map.keySet().iterator();
				List<String> msgid_list = new ArrayList<String>();
				while (itr.hasNext()) {
					String msgid = itr.next();
					if (DELIVERED_SET.contains(msgid)) {
						itr.remove();
						DELIVERED_SET.remove(msgid);
						msgid_list.add(msgid);
					}
				}
				if (!msgid_list.isEmpty()) {
					deleteStatusLog(msgid_list);
				}
			}
			if (!next_attempt_time_map.isEmpty()) {
				wait_iterator = next_attempt_time_map.entrySet().iterator();
				while (wait_iterator.hasNext()) {
					Map.Entry<String, Long> entry = wait_iterator.next();
					if (entry.getValue() <= System.currentTimeMillis()) {
						// logger.info(entry.getKey() + " putting to enQueue");
						enqueueSet.add(entry.getKey());
						wait_iterator.remove();
					}
				}
			}
			if (!enqueueSet.isEmpty()) {
				List<RoutePDU> list = getRecordList();
				for (RoutePDU route : list) {
					// logger.info(route.getHtiMsgId() + " putting to interProcessManage Queue");
					GlobalQueue.interProcessManage.enqueue(route);
				}
				list.clear();
				deleteRecords();
				enqueueSet.clear();
			}
			if (GlobalQueue.signalWaitQueue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			} else {
				// logger.info("SignalWait Queue:-> " + GlobalQueue.signalWaitQueue.size());
				SignalRetryObj retry_obj = null;
				while (!GlobalQueue.signalWaitQueue.isEmpty()) {
					int wait_attempt = 0;
					retry_obj = (SignalRetryObj) GlobalQueue.signalWaitQueue.dequeue();
					RoutePDU route = retry_obj.getRoutePDU();
					Integer[] criteria = retry_obj.getCriteria();
					if (wait_attempt_map.containsKey(route.getHtiMsgId())) {
						wait_attempt = wait_attempt_map.remove(route.getHtiMsgId());
					}
					wait_attempt++;
					int total_wait_attempt = criteria.length - 2;
					logger.debug(route.getHtiMsgId() + " WaitAttempt Current:" + wait_attempt + " total:"
							+ total_wait_attempt);
					if (wait_attempt <= total_wait_attempt) {
						int next_attemt_seconds = criteria[wait_attempt + 1];
						long next_attemp_time = System.currentTimeMillis() + (next_attemt_seconds * 1000);
						next_attempt_time_map.put(route.getHtiMsgId(), next_attemp_time);
						// put to wait_log & wait_status tables
						wait_status_insert_Queue.enqueue(new WaitLogStatus(route.getHtiMsgId(), wait_attempt,
								new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(next_attemp_time))));
						DatabaseDumpObject database_dump_object = null;
						Request request = route.getRequestPDU();
						if ((((SubmitSM) request).getDataCoding() == (byte) 8)
								|| (((SubmitSM) request).getDataCoding() == (byte) 245)) {
							String content = null;
							try {
								content = getHexDump(((SubmitSM) request).getShortMessage(Data.ENC_UTF16_BE));
							} catch (UnsupportedEncodingException e) {
								logger.error(route.getHtiMsgId(), e);
							}
							database_dump_object = new DatabaseDumpObject(content, ((SubmitSM) request).getSourceAddr(),
									((SubmitSM) request).getDestAddr(), ((SubmitSM) request).getSequenceNumber(),
									((SubmitSM) request).getRegisteredDelivery(), ((SubmitSM) request).getEsmClass(),
									((SubmitSM) request).getDataCoding(), route);
						} else {
							database_dump_object = new DatabaseDumpObject(((SubmitSM) request).getShortMessage(),
									((SubmitSM) request).getSourceAddr(), ((SubmitSM) request).getDestAddr(),
									((SubmitSM) request).getSequenceNumber(),
									((SubmitSM) request).getRegisteredDelivery(), ((SubmitSM) request).getEsmClass(),
									((SubmitSM) request).getDataCoding(), route);
						}
						pdu_log_insert_Queue.enqueue(database_dump_object);
						wait_attempt_map.put(route.getHtiMsgId(), wait_attempt);
					} else {
						logger.info(route.getHtiMsgId() + " WaitAttempt limit Exceeded Current:" + wait_attempt
								+ " total:" + total_wait_attempt);
						List<String> msgid_list = new ArrayList<String>();
						msgid_list.add(route.getHtiMsgId());
						deleteStatusLog(msgid_list);
					}
				}
			}
			if (!wait_status_insert_Queue.isEmpty()) {
				insertWaitStatus();
			}
			if (!pdu_log_insert_Queue.isEmpty()) {
				insertPduWaitLog();
			}
		}
		logger.info("SignalWaitProcess Thread Stopped");
	}

	private void insertWaitStatus() {
		Connection connection = null;
		PreparedStatement pstmt = null;
		String sql = "insert into retry_wait_status(msg_id,attempt,next_time) values(?,?,?) ON DUPLICATE KEY UPDATE attempt=?,next_time=?";
		try {
			connection = GlobalCache.connnection_pool_1.getConnection();
			pstmt = connection.prepareStatement(sql);
			connection.setAutoCommit(false);
			while (!wait_status_insert_Queue.isEmpty()) {
				WaitLogStatus waitLogStatus = (WaitLogStatus) wait_status_insert_Queue.dequeue();
				pstmt.setString(1, waitLogStatus.getMessgaeid());
				pstmt.setInt(2, waitLogStatus.getWaitAttemptCount());
				pstmt.setString(3, waitLogStatus.getNextAttemtTime());
				pstmt.setInt(4, waitLogStatus.getWaitAttemptCount());
				pstmt.setString(5, waitLogStatus.getNextAttemtTime());
				pstmt.addBatch();
			}
			int[] count = pstmt.executeBatch();
			connection.commit();
			logger.debug("retry_wait_status counter:-> " + count.length);
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
			GlobalCache.connnection_pool_1.putConnection(connection);
		}
	}

	private void insertPduWaitLog() {
		Connection connection = null;
		PreparedStatement statement = null;
		String sql = "insert ignore into retry_wait_log values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			connection = GlobalCache.connnection_pool_1.getConnection();
			statement = connection.prepareStatement(sql);
			connection.setAutoCommit(false);
			// ------------------------------------------------
			while (!pdu_log_insert_Queue.isEmpty()) {
				DatabaseDumpObject database_dump_object = (DatabaseDumpObject) pdu_log_insert_Queue.dequeue();
				RoutePDU route = database_dump_object.getRoute();
				Request request = route.getRequestPDU();
				String received_time = route.getTime();
				String username = route.getUsername();
				String cost = route.getCost() + "";
				request = route.getRequestPDU();
				String messageid = route.getHtiMsgId();
				statement.setString(1, messageid);
				statement.setString(2, received_time);
				statement.setInt(3, database_dump_object.getSeqNo());
				statement.setString(4, database_dump_object.getContent());
				statement.setInt(5, database_dump_object.getDestinationTON());
				statement.setInt(6, database_dump_object.getDestinationNPI());
				statement.setString(7, database_dump_object.getDestinationNo());
				statement.setString(8, route.getNetworkId() + "");
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
				int esm = database_dump_object.getEsm();
				int dcs = database_dump_object.getDcs();
				ConcatUnicode concate = new ConcatUnicode();
				ByteBuffer buffer = null;
				String msgType = "";
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
					if (foundMsgType.length() > 0
							&& (foundMsgType.equals("singleOptLogo") || foundMsgType.equals("concateOptLogo"))) {
						msgType = "short";
						concate.setMsg(((SubmitSM) request).getShortMessage(Data.ENC_ISO8859_1));
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
				statement.addBatch();
			}
			// ------------------------------------------------
			int[] count = statement.executeBatch();
			connection.commit();
			logger.debug("retry_wait_log counter:-> " + count.length);
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
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

	private List<RoutePDU> getRecordList() {
		List<RoutePDU> list = new ArrayList<RoutePDU>();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		String msg_id_text = String.join(",", enqueueSet);
		String sql = "select * from retry_wait_log where msg_id in (" + msg_id_text + ")";
		try {
			connection = GlobalCache.connnection_pool_1.getConnection();
			statement = connection.prepareStatement(sql);
			logger.debug("SQl:-> " + statement.toString());
			rs = statement.executeQuery();
			// ------------------------------------------------------
			String msg_id = null, time = null, seq_no = null, content = null, dest_ton = null, dest_npi = null,
					destination_no = null;
			String oprCountry = null, sour_ton = null, sour_npi = null, source_no = null, registered = null;
			String esm_class = null, dcs = null, username = null, msg_type = null;
			String backup_smsc = null;
			double cost = 0;
			SubmitSM submit_sm = null;
			RoutePDU pdu = null;
			boolean refund = false, is_reg_sender = false;
			int Priority = 3, session_id = 0;
			ConcatUnicode concate = null;
			// ------------------------------------------------------
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
					esm_class = rs.getString("esm_class");
					dcs = rs.getString("dcs");
					cost = rs.getDouble("cost");
					username = rs.getString("username");
					String smsc = rs.getString("smsc");
					Priority = rs.getInt("Priority");
					session_id = rs.getInt("session_id");
					backup_smsc = rs.getString("Secondry_smsc_id");
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
					refund = rs.getBoolean("refund");
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
					// submit_sm.setShortMessage(content);
					try {
						submit_sm.setValidityPeriod(validity_period);
					} catch (WrongDateFormatException we) {
						logger.error(msg_id + " Invalid Validity Period Found: " + validity_period);
					}
					if ((intdcs == 0) || (intdcs == 1) || (intdcs == 240)) {
						if (esm_class.equals("64")) {
							ByteBuffer bfr = concate.getByteBuffer();
							submit_sm.setBody(bfr); // Concatinated English
						} else {
							submit_sm.setShortMessage(content); // Single english,flash
						}
					} else if (esm_class.equals("64") && intdcs == 8) {// concate unicode and may be concate english if dcs isnot
																		// zero
						// logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>Concat UNICODE");
						ByteBuffer bfr = concate.getByteBuffer();
						submit_sm.setBody(bfr);
					} else if (esm_class.equals("64") && intdcs == 245) {
						if (msg_type.equals("buffer")) {
							ByteBuffer bfr = concate.getByteBuffer(); // for ringtone and wap push
							submit_sm.setBody(bfr);
						} else {
							submit_sm.setShortMessage(concate.getMsg());
						}
					} else { // for Unicode single msg
						content = Converter.getUnicode(content.toCharArray());
						submit_sm.setShortMessage(content, Data.ENC_UTF16_BE);
					}
					pdu = new RoutePDU((Request) submit_sm, msg_id, Integer.parseInt(seq_no),
							String.valueOf(session_id), Priority);
					pdu.setSmsc(smsc);
					pdu.setUsername(username);
					pdu.setBackupSmsc(backup_smsc);
					// pdu.setRegisterdlrValue(Byte.parseByte(registered));
					pdu.setCost(cost);
					// pdu.putPriority(Priority);
					try {
						pdu.setNetworkId(Integer.parseInt(oprCountry));
					} catch (Exception ex) {
						pdu.setNetworkId(0);
					}
					pdu.setTime(time);
					pdu.setRefund(refund);
					pdu.setOriginalSourceAddr(orig_source_adrr);
					pdu.setRegisterSender(is_reg_sender);
					list.add(pdu);
				} catch (Exception ex) {
					logger.error(msg_id + " <- UNSUPPORTED MESSAGE TYPE -> " + ex);
				}
			}
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			GlobalCache.connnection_pool_1.putConnection(connection);
		}
		return list;
	}

	private void deleteStatusLog(List<String> msgid_list) {
		Connection connection = null;
		PreparedStatement statement = null;
		String msg_id_text = String.join(",", msgid_list);
		String sql = "delete from retry_wait_status where msg_id in (" + msg_id_text + ")";
		try {
			connection = GlobalCache.connnection_pool_1.getConnection();
			statement = connection.prepareStatement(sql);
			logger.debug("SQl:-> " + statement.toString());
			int count = statement.executeUpdate();
			logger.debug("retry_wait_status Records Deleted:-> " + count);
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
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

	private void getStatusLog() {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		String sql = "delete from retry_wait_status where msg_id not in (select msg_id from retry_wait_log)";
		try {
			connection = GlobalCache.connnection_pool_1.getConnection();
			statement = connection.prepareStatement(sql);
			statement.executeUpdate();
			statement.close();
			sql = "select * from retry_wait_status";
			statement = connection.prepareStatement(sql);
			rs = statement.executeQuery();
			while (rs.next()) {
				wait_attempt_map.put(rs.getString("msg_id"), rs.getInt("attempt"));
				String next_time = rs.getString("next_time");
				if (next_time != null) {
					try {
						next_attempt_time_map.put(rs.getString("msg_id"),
								new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(next_time).getTime());
					} catch (Exception ex) {
						logger.info(rs.getString("msg_id") + " Invalid Time: " + next_time);
					}
				} else {
					logger.info(rs.getString("msg_id") + " Invalid Time: " + next_time);
				}
			}
			logger.info("retry_wait_status Records Found:-> " + wait_attempt_map.size() + " time:"
					+ next_attempt_time_map.size());
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			GlobalCache.connnection_pool_1.putConnection(connection);
		}
	}

	private void deleteRecords() {
		// logger.info("Records To Delete: " + enqueueSet);
		Connection connection = null;
		PreparedStatement statement = null;
		String msg_id_text = String.join(",", enqueueSet);
		String sql = "delete from retry_wait_log where msg_id in (" + msg_id_text + ")";
		try {
			connection = GlobalCache.connnection_pool_1.getConnection();
			statement = connection.prepareStatement(sql);
			logger.debug("SQl:-> " + statement.toString());
			int count = statement.executeUpdate();
			// logger.debug("retry_wait_log Records Deleted:-> " + count);
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
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

	private String getHexDump(String getString) {
		String dump = "";
		try {
			// int dataLen = getString.length();
			byte[] buffer = getString.getBytes(Data.ENC_UTF16_BE);
			for (int i = 0; i < buffer.length; i++) {
				dump += Character.forDigit((buffer[i] >> 4) & 0x0f, 16);
				dump += Character.forDigit(buffer[i] & 0x0f, 16);
			}
			buffer = null;
		} catch (Throwable t) {
			dump = "Throwable caught when dumping = " + t;
		}
		return dump;
	}

	public void stop() {
		logger.info("SignalWaitProcess Thread Stopping");
		stop = true;
	}
}
