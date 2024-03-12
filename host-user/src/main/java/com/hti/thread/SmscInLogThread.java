/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.topic.ITopic;
import com.hti.objects.DatabaseDumpObject;
import com.hti.objects.RoutePDU;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;
import com.logica.msgContent.ConcatUnicode;
import com.logica.smpp.Data;
import com.logica.smpp.pdu.Request;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.util.ByteBuffer;

/**
 *
 * @author Administrator
 */
public class SmscInLogThread implements Runnable {
	Connection connection;
	PreparedStatement statement;
	String sql;
	private Logger logger = LoggerFactory.getLogger("dbLogger");
	private Map<String, String> msgHeaders = new HashMap<String, String>();
	public static boolean RENAME_TABLE;
	private boolean stop;
	private int total_insert_count = 0;
	public static boolean TABLE_LOCKED;

	public SmscInLogThread() {
		logger.info("SmscInLogThread Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			if (RENAME_TABLE) {
				RENAME_TABLE = false;
				ITopic<Map<String, Boolean>> table_topic = GlobalVars.hazelInstance.getTopic("table_locked");
				Map<String, Boolean> table_locked = new HashMap<String, Boolean>();
				table_locked.put("SMSC_IN_LOG", true);
				table_topic.publish(table_locked);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
				change_table();
				table_locked.put("SMSC_IN_LOG", false);
				table_topic.publish(table_locked);
			}
			if (GlobalQueue.smsc_in_log_Queue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			} else {
				if (!TABLE_LOCKED) {
					logger.debug("SmscInLogQueue: " + GlobalQueue.smsc_in_log_Queue.size());
					int counter = 0;
					sql = "insert ignore into smsc_in_log(msg_id,time,seq_no,content,dest_ton,dest_npi,destination_no,oprCountry,sour_ton,sour_npi,source_no,registered,esm_class,"
							+ "dcs,cost,username,smsc,Priority,session_id,Secondry_smsc_id,msg_object,msg_type,refund,validity_period,orig_source,is_reg_sender,server_id,group_id,opt_param) "
							+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					String msgid = null;
					DatabaseDumpObject database_dump_object;
					RoutePDU route = null;
					Request request = null;
					ConcatUnicode concate = null;
					ByteBuffer buffer = null;
					String msgType = "";
					String content = "";
					int esm = 0;
					int dcs = 0;
					try {
						connection = GlobalCache.logConnectionPool.openConnection();
						statement = connection.prepareStatement(sql);
						connection.setAutoCommit(false);
						while (!GlobalQueue.smsc_in_log_Queue.isEmpty()) {
							database_dump_object = (DatabaseDumpObject) GlobalQueue.smsc_in_log_Queue.dequeue();
							if (database_dump_object != null) {
								// boolean optional_param = false;
								route = database_dump_object.getRoute();
								msgid = route.getHtiMsgId();
								try {
									request = route.getRequestPDU();
									statement.setString(1, msgid);
									statement.setString(2, route.getTime());
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
									statement.setString(16, route.getUsername());
									statement.setString(17, route.getSmsc());
									statement.setInt(18, route.getPriority());
									statement.setString(19, route.getSessionId());
									statement.setString(20, route.getBackupSmsc());
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
										statement.setObject(21, concate);
										statement.setString(22, msgType);
									} else {
										statement.setObject(21, "");
										statement.setString(22, "");
									}
									statement.setBoolean(23, route.isRefund());
									String validity_period = ((SubmitSM) request).getValidityPeriod();
									if (validity_period != null && validity_period.length() == 0) {
										validity_period = null;
									}
									statement.setString(24, validity_period);
									statement.setString(25, route.getOriginalSourceAddr());
									statement.setBoolean(26, route.isRegisterSender());
									statement.setInt(27, GlobalVars.SERVER_ID);
									statement.setInt(28, route.getGroupId());
									if (((SubmitSM) request).getExtraOptional((short) 0x1400) != null
										|| ((SubmitSM) request).getExtraOptional((short) 0x1500) != null) {
										statement.setBoolean(29, true);
									} else {
										statement.setBoolean(29, false);
									}
									statement.addBatch();
									++total_insert_count;
									if (++counter > 1000) {
										logger.debug("SmscInLog Insert: " + total_insert_count);
										break;
									}
								} catch (java.sql.SQLException se) {
									logger.error("MsgID: " + msgid, se);
								}
							}
							database_dump_object = null;
						}
						statement.executeBatch();
						connection.commit();
					} catch (Exception se) {
						logger.error("", se.fillInStackTrace());
					} finally {
						GlobalCache.logConnectionPool.releaseConnection(connection);
						if (statement != null) {
							try {
								statement.close();
							} catch (SQLException ex) {
								statement = null;
							}
						}
					}
				} else {
					try {
						Thread.sleep(100);
					} catch (InterruptedException ex) {
					}
				}
			}
		}
		logger.info("SmscInLogThread Stopped.Queue: " + GlobalQueue.smsc_in_log_Queue.size());
	}

	private void change_table() {
		Calendar end = Calendar.getInstance();
		end.add(Calendar.MONTH, -1);
		String sql = "RENAME TABLE smsc_in_log TO smsc_in_log_" + new SimpleDateFormat("MM_yyyy").format(end.getTime());
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = getConnection();
			statement = connection.prepareStatement(sql);
			statement.executeUpdate();
			logger.info("<-- smsc_in_log table renamed --> ");
			sql = "CREATE TABLE IF NOT EXISTS smsc_in_log (`msg_id` bigint(20) UNSIGNED NOT NULL DEFAULT 0,`time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
					+ "  `seq_no` int(10) DEFAULT '0',`content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,`dest_ton` tinyint(1) unsigned NOT NULL DEFAULT 0,"
					+ "  `dest_npi` tinyint(1) unsigned NOT NULL DEFAULT 0,`destination_no` bigint(15) DEFAULT NULL,  `oprCountry` int(11) NOT NULL DEFAULT 0,"
					+ "`sour_ton` tinyint(1) unsigned NOT NULL DEFAULT 0,  `sour_npi` tinyint(1) unsigned NOT NULL DEFAULT 0,  `source_no` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
					+ "`registered` tinyint(1) unsigned NOT NULL DEFAULT '0', `esm_class` int(3) unsigned DEFAULT '0',  `dcs` int(3) unsigned DEFAULT 0,  `cost` decimal(7,5) NOT NULL DEFAULT 0.00000,"
					+ "`username` varchar(15) NOT NULL DEFAULT '',  `s_flag` varchar(3) NOT NULL DEFAULT 'L',`smsc` varchar(10) DEFAULT NULL,  `Priority` int(3) unsigned NOT NULL DEFAULT '3',"
					+ "  `session_id` int(11) DEFAULT NULL,Secondry_smsc_id varchar(10) DEFAULT NULL,`msg_object` blob,  `msg_type` varchar(10) DEFAULT NULL,  `refund` tinyint(1) DEFAULT 0,"
					+ "  `validity_period` varchar(16) DEFAULT NULL,  `orig_source` varchar(16) DEFAULT NULL,`is_reg_sender` tinyint(1) DEFAULT 0, server_id int(1) NOT NULL DEFAULT 1,group_id int(3) NOT NULL DEFAULT 0,opt_param tinyint(1) DEFAULT 0,"
					+ " PRIMARY KEY (`msg_id`),INDEX (time)) DEFAULT CHARSET=latin1;";
			statement = connection.prepareStatement(sql);
			statement.executeUpdate();
			logger.info("<-- Empty smsc_in_log table created --> ");
		} catch (Exception ex) {
			logger.error("createsmscInLogTable() ", ex);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
				}
			}
			closeConnection(connection);
		}
	}

	private Connection getConnection() {
		logger.info("createConnection()");
		Connection connection = null;
		try {
			Class.forName(GlobalVars.JDBC_DRIVER);
			connection = DriverManager.getConnection(GlobalVars.LOG_DB_URL, GlobalVars.ALTER_DB_USER,
					GlobalVars.ALTER_DB_PASSWORD);
		} catch (ClassNotFoundException | SQLException ex) {
			logger.error("createConnection(): " + ex);
		}
		return connection;
	}

	private void closeConnection(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error("", e);
			}
		}
	}

	public void stop() {
		logger.info("SmscInLogThread Stopping.Queue: " + GlobalQueue.smsc_in_log_Queue.size());
		stop = true;
	}
}
