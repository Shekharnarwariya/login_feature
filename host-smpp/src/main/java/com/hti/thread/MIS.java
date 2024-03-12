package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.StatusObj;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;

public class MIS implements Runnable {
	private Connection connection = null;
	private PreparedStatement statement = null;
	private String sql = null;
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private StatusObj smscqueue = null;
	public static boolean SHIFT_OLD_RECORDS;
	private boolean stop;
	private int HOUR = 0;

	public MIS() {
		logger.info("MIS Thread Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			if (SHIFT_OLD_RECORDS) {
				String max_msg_id = null;
				Calendar calendar = null;
				HOUR = HOUR + 1;
				if (HOUR <= 23) {
					calendar = Calendar.getInstance();
					calendar.add(Calendar.DATE, -4);
					calendar.set(Calendar.HOUR_OF_DAY, HOUR);
					max_msg_id = new SimpleDateFormat("yyMMddHH").format(calendar.getTime());
					max_msg_id = max_msg_id + "00000000000";
				} else {
					calendar = Calendar.getInstance();
					calendar.add(Calendar.DATE, -3);
					max_msg_id = new SimpleDateFormat("yyMMdd").format(calendar.getTime());
					max_msg_id = max_msg_id + "0000000000000";
					SHIFT_OLD_RECORDS = false;
					HOUR = 0;
				}
				String shift_sql = "insert ignore into mis_table_log(msg_id,username,route_to_smsc,submitted_time,s_flag,status) select msg_id,username,route_to_smsc,submitted_time,s_flag,status from mis_table where msg_id  < "
						+ max_msg_id;
				logger.info(shift_sql);
				try {
					connection = GlobalCache.connnection_pool_2.getConnection();
					statement = connection.prepareStatement(shift_sql);
					int shift_count = statement.executeUpdate();
					logger.info("mis_table to log Shift Count : " + shift_count);
					// if (shift_count > 0) {
					shift_sql = "delete from mis_table where msg_id  < " + max_msg_id + " limit 25000";
					logger.info(shift_sql);
					statement = connection.prepareStatement(shift_sql);
					do {
						shift_count = statement.executeUpdate();
						logger.info("mis_table Delete Counter : " + shift_count);
					} while (shift_count == 25000);
				} catch (Exception ex) {
					logger.error("Delete Error: ", ex);
				} finally {
					GlobalCache.connnection_pool_2.putConnection(connection);
					if (statement != null) {
						try {
							statement.close();
						} catch (SQLException ex) {
							statement = null;
						}
					}
				}
				if (!SHIFT_OLD_RECORDS) {
					calendar = Calendar.getInstance();
					calendar.add(Calendar.DATE, -30);
					max_msg_id = new SimpleDateFormat("yyMMdd").format(calendar.getTime());
					max_msg_id = max_msg_id + "0000000000000";
					shift_sql = "delete from mis_table_log where msg_id  < " + max_msg_id + " limit 50000";
					int delete_count = 0;
					try {
						connection = GlobalCache.connnection_pool_2.getConnection();
						statement = connection.prepareStatement(shift_sql);
						do {
							logger.info(shift_sql);
							delete_count = statement.executeUpdate();
							logger.info("mis_table_log Delete Counter : " + delete_count);
						} while (delete_count == 50000);
					} catch (Exception ex) {
						logger.error("mis_table_log Delete Error: ", ex);
					} finally {
						GlobalCache.connnection_pool_2.putConnection(connection);
						if (statement != null) {
							try {
								statement.close();
							} catch (SQLException ex) {
								statement = null;
							}
						}
					}
				}
			}
			if (!GlobalQueue.MIS_Dump_Queue.isEmpty()) {
				int count = 0;
				try {
					smscqueue = (StatusObj) GlobalQueue.MIS_Dump_Queue.dequeue();
					if (smscqueue != null) {
						// System.out.println(smscqueue.toString());
						connection = GlobalCache.connnection_pool_2.getConnection();
						sql = "insert into mis_table(msg_id,username,route_to_smsc,submitted_time,s_flag,status) values(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE s_flag=?,status=?";
						statement = connection.prepareStatement(sql);
						String status = smscqueue.getStatus();
						if (status != null && status.length() > 0) {
							if (status.length() > 8) {
								status = status.substring(0, 8);
							}
						} else {
							status = "ATES";
						}
						statement.setString(1, smscqueue.getMsgid());
						statement.setString(2, smscqueue.getUsername());
						statement.setString(3, smscqueue.getRoute());
						if (smscqueue.getSubmitOn() == null) {
							statement.setString(4, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
						} else {
							statement.setString(4, smscqueue.getSubmitOn());
						}
						statement.setString(5, smscqueue.getFlag());
						statement.setString(6, status);
						statement.setString(7, smscqueue.getFlag());
						statement.setString(8, status);
						statement.execute();
						if (status != null) {
							if (!status.equalsIgnoreCase("ATES") && !status.startsWith("ACCEP")) {
								GlobalCache.recievedDlrResponseId.addElement(smscqueue.getResponseid());
							}
						}
						connection.setAutoCommit(false);
						while (!GlobalQueue.MIS_Dump_Queue.isEmpty()) {
							try {
								smscqueue = (StatusObj) GlobalQueue.MIS_Dump_Queue.dequeue();
								status = smscqueue.getStatus();
								if (status != null && status.length() > 0) {
									if (status.length() > 8) {
										status = status.substring(0, 8);
									}
								} else {
									status = "ATES";
								}
								statement.setString(1, smscqueue.getMsgid());
								statement.setString(2, smscqueue.getUsername());
								statement.setString(3, smscqueue.getRoute());
								if (smscqueue.getSubmitOn() == null) {
									statement.setString(4,
											new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
								} else {
									statement.setString(4, smscqueue.getSubmitOn());
								}
								statement.setString(5, smscqueue.getFlag());
								statement.setString(6, status);
								statement.setString(7, smscqueue.getFlag());
								statement.setString(8, status);
								statement.addBatch();
								if (status != null) {
									if (!status.equalsIgnoreCase("ATES") && !status.startsWith("ACCEP")) {
										GlobalCache.recievedDlrResponseId.addElement(smscqueue.getResponseid());
									}
								}
							} catch (Exception e) {
								logger.error("process(1)", e.fillInStackTrace());
							}
							smscqueue = null;
							if (++count >= 1000) {
								break;
							}
						}
						if (count > 0) {
							statement.executeBatch();
							connection.commit();
						}
					}
				} catch (java.sql.SQLException sqle) {
					logger.error("process(2)", sqle.fillInStackTrace());
				} catch (Exception e) {
					logger.error("process(3)", e.fillInStackTrace());
				} finally {
					if (statement != null) {
						try {
							statement.close();
						} catch (SQLException ex) {
							statement = null;
						}
					}
					GlobalCache.connnection_pool_2.putConnection(connection);
					connection = null;
				}
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
				}
			}
		}
		logger.info("MIS Thread Stopped.Queue: " + GlobalQueue.MIS_Dump_Queue.size());
	}

	public void stop() {
		stop = true;
		logger.info("MIS Thread Stopping.Queue: " + GlobalQueue.MIS_Dump_Queue.size());
		if (!GlobalCache.UserMisQueueObject.isEmpty()) {
			for (UserWiseMis userWiseMis : GlobalCache.UserMisQueueObject.values()) {
				try {
					userWiseMis.stop();
				} catch (Exception ex) {
					logger.error("stop()", ex);
				}
			}
		}
		// Wait Untill All SMSC Connection not Closed
	}
}
