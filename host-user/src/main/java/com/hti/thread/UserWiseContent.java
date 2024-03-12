/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.HTIQueue;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;
import com.logica.msgContent.MsgContent;
import com.logica.smpp.Data;

/**
 *
 * @author Administrator
 */
public class UserWiseContent implements Runnable {
	Connection connection = null;
	PreparedStatement prestatement = null;
	String sql = null;
	private MsgContent msgContent = null;
	private String username;
	private HTIQueue Queue;
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private String content_table_name;
	private boolean stop;
	private boolean holdOn;

	public UserWiseContent(String username, HTIQueue Queue) {
		logger.info(username + "_Content Starting");
		this.username = username;
		this.Queue = Queue;
		content_table_name = "content_" + username;
		sql = "insert ignore into " + content_table_name
				+ "(msg_id,esm,dcs,content,total,part_number,ref_number) values(?,?,?,?,?,?,?)";
		// setThreadName(username + "_Content");
	}

	public void setHoldOn(boolean holdOn) {
		this.holdOn = holdOn;
	}

	public HTIQueue getQueue() {
		return Queue;
	}

	@Override
	public void run() {
		while (!stop) {
			if (holdOn) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			} else {
				if (Queue.isEmpty()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
					}
				} else {
					int count = 0;
					try {
						msgContent = (MsgContent) Queue.dequeue();
						if (msgContent != null) {
							try {
								connection = GlobalCache.connection_pool_proc.getConnection();
								prestatement = connection.prepareStatement(sql);
								prestatement.setString(1, msgContent.getMsg_id());
								prestatement.setInt(2, msgContent.getEsm());
								if (msgContent.getDcs() == 8 || msgContent.getDcs() == 245) {
									prestatement.setInt(3, msgContent.getDcs());
									prestatement.setString(4, msgContent.getMsg_content());
								} else {
									prestatement.setInt(3, 1);
									prestatement.setString(4,
											getHexDump(msgContent.getMsg_content(), msgContent.getEsm()));
								}
								prestatement.setInt(5, msgContent.getTotal());
								prestatement.setInt(6, msgContent.getPartNumber());
								prestatement.setInt(7, msgContent.getReferenceNumber());
								prestatement.execute();
							} catch (SQLSyntaxErrorException se) {
								if (se.getErrorCode() == 1146) {
									if (createContentTable()) {
										logger.info(content_table_name + " Created");
									} else {
										logger.error(content_table_name + " Creation Failed");
									}
								} else {
									logger.error(username, se);
								}
								Queue.enqueue(msgContent);
							} catch (Exception ex) {
								// ex.printStackTrace();
								if (ex.getMessage().contains("Table") && ex.getMessage().contains("doesn't exist")) {
									if (createContentTable()) {
										logger.info(content_table_name + " Created");
									} else {
										logger.error(content_table_name + " Creation Failed");
									}
								} else {
									logger.error(username, ex);
								}
								Queue.enqueue(msgContent);
							}
							connection.setAutoCommit(false);
							while (!Queue.isEmpty()) {
								msgContent = (MsgContent) Queue.dequeue();
								if (msgContent != null) {
									prestatement.setString(1, msgContent.getMsg_id());
									prestatement.setInt(2, msgContent.getEsm());
									if (msgContent.getDcs() == 8 || msgContent.getDcs() == 245) {
										prestatement.setInt(3, msgContent.getDcs());
										prestatement.setString(4, msgContent.getMsg_content());
									} else {
										prestatement.setInt(3, 1);
										prestatement.setString(4,
												getHexDump(msgContent.getMsg_content(), msgContent.getEsm()));
									}
									prestatement.setInt(5, msgContent.getTotal());
									prestatement.setInt(6, msgContent.getPartNumber());
									prestatement.setInt(7, msgContent.getReferenceNumber());
									prestatement.addBatch();
									if (++count >= 1000) {
										break;
									}
								}
								msgContent = null;
							}
							if (count > 0) {
								prestatement.executeBatch();
								connection.commit();
							}
						}
					} catch (Exception ex) {
						logger.error(username, ex);
					} finally {
						GlobalCache.connection_pool_proc.putConnection(connection);
						if (prestatement != null) {
							try {
								prestatement.close();
							} catch (SQLException ex) {
								prestatement = null;
							}
						}
					}
				}
			}
		}
		logger.info(username + "_Content Stopped. Queue: " + Queue.size());
	}

	public void stop() {
		logger.info(username + "_Content Stopping");
		stop = true;
		// super.stopProcessing(null);
	}

	private String getHexDump(String getString, int esm) {
		String dump = "";
		String header = "";
		try {
			// int dataLen = getString.length();
			byte[] buffer = getString.getBytes(Data.ENC_UTF16_BE);
			for (int i = 0; i < buffer.length; i++) {
				dump += Character.forDigit((buffer[i] >> 4) & 0x0f, 16);
				dump += Character.forDigit(buffer[i] & 0x0f, 16);
				// System.out.println(dump);
			}
			buffer = null;
			dump = dump.toUpperCase();
			if (esm == (byte) Data.SM_UDH_GSM || esm == (byte) Data.SM_UDH_GSM_2) {
				int header_length = Integer.parseInt(dump.substring(0, 4));
				if (header_length == 5) {
					header = dump.substring(0, 24);
					dump = dump.substring(24, dump.length());
				} else if (header_length == 6) {
					header = dump.substring(0, 28);
					dump = dump.substring(28, dump.length());
				}
			}
			if (dump.contains("001B003C")) {
				dump = dump.replaceAll("001B003C", "005B");
			}
			if (dump.contains("001B003E")) {
				dump = dump.replaceAll("001B003E", "005D");
			}
			if (dump.contains("001B0065")) { // €
				dump = dump.replaceAll("001B0065", "20AC");
			}
			int len = dump.length();
			// System.out.println("len: " + len);
			int temp = 0, chars = 4;
			String[] equalStr = new String[len / chars];
			for (int i = 0; i < len; i = i + chars) {
				equalStr[temp] = dump.substring(i, i + chars);
				temp++;
			}
			dump = "";
			for (int i = 0; i < equalStr.length; i++) {
				if (equalStr[i].equalsIgnoreCase("0000")) {
					dump += "0040";
				} else if (equalStr[i].equalsIgnoreCase("0002")) {
					dump += "0024";
				} else {
					dump += equalStr[i];
				}
			}
			// System.out.println("final: " + dump);
		} catch (Exception ex) {
			logger.error(username, getString, ex);
		}
		return header + dump;
	}

	private boolean createContentTable() {
		logger.info("createContentTable() :" + content_table_name);
		String sql = "CREATE TABLE IF NOT EXISTS " + content_table_name + " ("
				+ "msg_id bigint(20) UNSIGNED NOT NULL DEFAULT 0," + "esm int(3) unsigned DEFAULT 0,"
				+ "dcs int(3) unsigned DEFAULT 0,"
				+ "content longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
				+ "total int(3) unsigned DEFAULT 0," + "part_number int(3) unsigned DEFAULT 0,"
				+ "ref_number int(3) unsigned DEFAULT 0," + "PRIMARY KEY (msg_id)) ENGINE=MyISAM DEFAULT CHARSET=utf8";
		if (GlobalVars.DB_CLUSTER) {
			sql += " TABLESPACE disk_table_ts_1 STORAGE DISK;";
		} else {
			sql += ";";
		}
		Connection connection = null;
		PreparedStatement pStmt = null;
		boolean execute = true;
		try {
			connection = GlobalCache.connection_pool_proc.getConnection();
			pStmt = connection.prepareStatement(sql);
			pStmt.executeUpdate();
		} catch (Exception ex) {
			execute = false;
			logger.error(content_table_name + " createContentTable() " + ex);
		} finally {
			if (pStmt != null) {
				try {
					pStmt.close();
				} catch (SQLException ex) {
				}
			}
			if (connection != null) {
				GlobalCache.connection_pool_proc.putConnection(connection);
			}
		}
		logger.debug("createContentTable() :" + execute);
		return execute;
	}
}
