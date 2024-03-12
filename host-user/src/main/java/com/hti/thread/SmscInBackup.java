package com.hti.thread;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.ReportLogObject;
import com.hti.objects.SmscInObj;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalMethods;
import com.hti.util.GlobalQueue;
import com.logica.smpp.Data;
import com.logica.smpp.util.Queue;
import com.hti.hlr.RouteObject;

public class SmscInBackup implements Runnable {
	private Logger logger = LoggerFactory.getLogger("dbLogger");
	private boolean stop;
	private Connection connection = null;
	private Statement statement = null;
	// private PreparedStatement preStatement = null;
	private ResultSet rs = null;
	private Map<String, Map<String, List<SmscInObj>>> userWiseMap = new java.util.HashMap<String, Map<String, List<SmscInObj>>>();

	public SmscInBackup() {
		logger.info("SmscInBackup Thread Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				Thread.sleep(3 * 1000);
			} catch (InterruptedException e) {
			}
			try {
				userWiseMap.clear();
				// ************ Checking For Down Counter *******************
				try {
					connection = GlobalCache.connection_pool_proc.getConnection();
					statement = connection.createStatement();
					rs = statement.executeQuery(
							"select msg_id,s_flag,username,oprCountry,group_id,cost,smsc,time,destination_no,source_no,esm_class,content,dcs from smsc_in_backup order by username,msg_id,s_flag");
					while (rs.next()) {
						String username = rs.getString("username");
						int network_id = 0;
						try {
							network_id = Integer.parseInt(rs.getString("oprCountry"));
						} catch (Exception ex) {
							logger.error(rs.getString("msg_id") + " " + rs.getString("oprCountry"), ex);
						}
						SmscInObj pdu = new SmscInObj(rs.getString("msg_id"), rs.getString("s_flag"),
								rs.getString("smsc"), rs.getInt("group_id"), rs.getDouble("cost"), network_id);
						pdu.setUsername(rs.getString("username"));
						pdu.setTime(rs.getString("time"));
						pdu.setContent(rs.getString("content"));
						pdu.setEsm(rs.getInt("esm_class"));
						pdu.setDcs(rs.getInt("dcs"));
						pdu.setDestination(rs.getString("destination_no"));
						pdu.setSource(rs.getString("source_no"));
						if (userWiseMap.containsKey(username)) {
							if (userWiseMap.get(username).containsKey(rs.getString("s_flag"))) {
								userWiseMap.get(username).get(rs.getString("s_flag")).add(pdu);
							} else {
								List<SmscInObj> msg_id_list = new java.util.ArrayList<SmscInObj>();
								msg_id_list.add(pdu);
								userWiseMap.get(username).put(rs.getString("s_flag"), msg_id_list);
							}
						} else {
							Map<String, List<SmscInObj>> inner = new java.util.HashMap<String, List<SmscInObj>>();
							List<SmscInObj> msg_id_list = new java.util.ArrayList<SmscInObj>();
							msg_id_list.add(pdu);
							inner.put(rs.getString("s_flag"), msg_id_list);
							userWiseMap.put(username, inner);
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
					GlobalCache.connection_pool_proc.putConnection(connection);
				}
				if (!userWiseMap.isEmpty()) { // proceed further
					for (String username : userWiseMap.keySet()) {
						Map<String, List<SmscInObj>> inner = userWiseMap.get(username);
						for (String flag : inner.keySet()) {
							List<SmscInObj> list = inner.get(flag);
							logger.info("Processing Backup Records For " + username + "[" + flag + "]: " + list.size());
							if (flag.equalsIgnoreCase("H")) {
								List<RouteObject> pduList = new ArrayList<RouteObject>();
								List<String> msg_id_list = new ArrayList<String>();
								for (SmscInObj pdu : list) {
									try {
										int part_number = 0;
										if (pdu.getEsm() == 64 || pdu.getEsm() == 67) { // multipart
											// System.out.println(msg_id + " : " + rs.getString("content"));
											if (pdu.getDcs() == 8) {
												part_number = getPartNumber(pdu.getContent());
											} else {
												part_number = getPartNumber(getHexDump(pdu.getContent()));
											}
										}
										msg_id_list.add(pdu.getMsgid());
										pduList.add(new RouteObject(pdu.getMsgid(), pdu.getSmsc(), pdu.getGroupId(),
												pdu.getCost(), part_number, pdu.getSource(), pdu.getDestination(),
												false, false, pdu.getNetworkId(), false));
									} catch (Exception e) {
										logger.error(pdu.getMsgid(), e);
									}
								}
								if (!msg_id_list.isEmpty()) {
									String temp_sql = "insert ignore into smsc_in_temp select * from smsc_in_backup where msg_id in("
											+ String.join(",", msg_id_list) + ")";
									try {
										connection = GlobalCache.connection_pool_proc.getConnection();
										statement = connection.createStatement();
										int insert_counter = statement.executeUpdate(temp_sql);
										logger.debug("Records Inserted From Backup: " + insert_counter);
										temp_sql = "delete from smsc_in_backup where msg_id in("
												+ String.join(",", msg_id_list) + ")";
										int delete_counter = statement.executeUpdate(temp_sql);
										logger.debug("Records deleted From Backup: " + delete_counter);
									} catch (Exception ex) {
										logger.error("Select Part: " + temp_sql, ex.fillInStackTrace());
									} finally {
										if (statement != null) {
											try {
												statement.close();
											} catch (SQLException e) {
											}
										}
										GlobalCache.connection_pool_proc.putConnection(connection);
									}
								}
								if (!pduList.isEmpty()) {
									Queue hlrQueue = GlobalMethods.getHLRQueueProcess(username);
									if (hlrQueue != null) {
										for (RouteObject hlrObj : pduList) {
											hlrQueue.enqueue(hlrObj);
										}
										logger.info(username + " Filled HlrQueue: " + hlrQueue.size());
									} else {
										logger.error(username + " Unable To Get HlrQueue");
									}
								}
							} else if (flag.equalsIgnoreCase("B") || flag.equalsIgnoreCase("M")) { // put record to report & smsc_in table
								List<String> msg_id_list = new ArrayList<String>();
								for (SmscInObj pdu : list) {
									GlobalQueue.reportLogQueue
											.enqueue(new ReportLogObject(pdu.getMsgid(), pdu.getNetworkId() + "",
													pdu.getUsername(), pdu.getSmsc(), pdu.getCost() + "", flag,
													pdu.getTime(), pdu.getDestination(), pdu.getSource()));
									msg_id_list.add(pdu.getMsgid());
								}
								if (!msg_id_list.isEmpty()) {
									String temp_sql = "insert ignore into smsc_in select * from smsc_in_backup where msg_id in("
											+ String.join(",", msg_id_list) + ")";
									try {
										connection = GlobalCache.connection_pool_proc.getConnection();
										statement = connection.createStatement();
										int insert_counter = statement.executeUpdate(temp_sql);
										logger.debug("Records Inserted From Backup: " + insert_counter);
										temp_sql = "delete from smsc_in_backup where msg_id in("
												+ String.join(",", msg_id_list) + ")";
										int delete_counter = statement.executeUpdate(temp_sql);
										logger.debug("Records deleted From Backup: " + delete_counter);
									} catch (Exception ex) {
										logger.error(temp_sql, ex.fillInStackTrace());
									} finally {
										if (statement != null) {
											try {
												statement.close();
											} catch (SQLException e) {
											}
										}
										GlobalCache.connection_pool_proc.putConnection(connection);
									}
								}
							} else { // put record to temp table
								List<String> msg_id_list = new ArrayList<String>();
								for (SmscInObj pdu : list) {
									msg_id_list.add(pdu.getMsgid());
								}
								if (!msg_id_list.isEmpty()) {
									String temp_sql = "insert ignore into smsc_in_temp select * from smsc_in_backup where msg_id in("
											+ String.join(",", msg_id_list) + ")";
									try {
										connection = GlobalCache.connection_pool_proc.getConnection();
										statement = connection.createStatement();
										int insert_counter = statement.executeUpdate(temp_sql);
										logger.debug("Records Inserted From Backup: " + insert_counter);
										temp_sql = "delete from smsc_in_backup where msg_id in("
												+ String.join(",", msg_id_list) + ")";
										int delete_counter = statement.executeUpdate(temp_sql);
										logger.debug("Records deleted From Backup: " + delete_counter);
									} catch (Exception ex) {
										logger.error(temp_sql, ex.fillInStackTrace());
									} finally {
										if (statement != null) {
											try {
												statement.close();
											} catch (SQLException e) {
											}
										}
										GlobalCache.connection_pool_proc.putConnection(connection);
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		logger.info("SmscInBackup Thread Stopped.");
	}

	private String getHexDump(String getString) throws UnsupportedEncodingException {
		String dump = "";
		byte[] buffer = getString.getBytes(Data.ENC_UTF16_BE);
		for (int i = 0; i < buffer.length; i++) {
			dump += Character.forDigit((buffer[i] >> 4) & 0x0f, 16);
			dump += Character.forDigit(buffer[i] & 0x0f, 16);
		}
		// System.out.println("hexdump:" + dump);
		buffer = null;
		return dump;
	}

	private int getPartNumber(String hex_dump) {
		int part_number = 0;
		try {
			int header_length = Integer.parseInt(hex_dump.substring(0, 2));
			// System.out.println("Header Length:" + header_length);
			if (header_length == 0) {
				header_length = Integer.parseInt(hex_dump.substring(0, 4));
				if (header_length == 5) {
					try {
						part_number = Integer.parseInt(hex_dump.substring(20, 24));
					} catch (Exception ex) {
					}
				} else if (header_length == 6) {
					try {
						part_number = Integer.parseInt(hex_dump.substring(24, 28));
					} catch (Exception ex) {
					}
				} else {
					System.out.println("Unknown Header Found:" + hex_dump.substring(0, 14));
				}
			} else {
				if (header_length == 5) {
					try {
						part_number = Integer.parseInt(hex_dump.substring(10, 12));
					} catch (Exception ex) {
					}
				} else if (header_length == 6) {
					try {
						part_number = Integer.parseInt(hex_dump.substring(12, 14));
					} catch (Exception ex) {
					}
				} else {
					System.out.println("Unknown Header Found:" + hex_dump.substring(0, 14));
				}
			}
		} catch (Exception une) {
		}
		// System.out.println("part_number: " + part_number);
		return part_number;
	}

	public void stop() {
		logger.info("SmscInBackup Thread Stopping.");
		stop = true;
	}
}
