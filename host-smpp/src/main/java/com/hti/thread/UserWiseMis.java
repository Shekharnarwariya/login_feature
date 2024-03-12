package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.SerialQueue;
import com.hti.objects.StatusObj;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;

public class UserWiseMis implements Runnable {
	// private SmscQueue smscqueue = null;
	private Connection connection = null;
	private PreparedStatement statement = null;
	private String sql = null;
	private String username;
	private SerialQueue Queue;
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private String table_name;
	private boolean stop;
	private StatusObj smscqueue = null;
	private boolean holdOn;

	public UserWiseMis(String username, SerialQueue Queue) {
		this.username = username;
		this.Queue = Queue;
		table_name = "mis_" + username;
		logger.info(username + "_Mis Starting");
	}

	public SerialQueue getQueue() {
		return Queue;
	}

	public void setHoldOn(boolean holdOn) {
		this.holdOn = holdOn;
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
				if (!Queue.isEmpty()) {
					sql = "insert into " + table_name
							+ "(msg_id,response_id,Route_to_SMSC,oprCountry,source_no,dest_no,cost,submitted_time,deliver_time,Status,Err_code,refund) values(?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE deliver_time=?,status=?,err_code=?,refund=?,response_id=?";
					int count = 0;
					String status = null, error_code = null;
					try {
						smscqueue = (StatusObj) Queue.dequeue();
						if (smscqueue != null) {
							connection = GlobalCache.connnection_pool_2.getConnection();
							statement = connection.prepareStatement(sql);
							try {
								statement.setString(1, smscqueue.getMsgid());
								statement.setString(2, smscqueue.getResponseid());
								statement.setString(3, smscqueue.getRoute());
								statement.setString(4, smscqueue.getOprCountry());
								statement.setString(5, smscqueue.getOrigSource());
								statement.setString(6, smscqueue.getDestination());
								statement.setDouble(7, smscqueue.getCost());
								if (smscqueue.getSubmitOn() == null) {
									String Systime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
									statement.setString(8, Systime);
								} else {
									if (smscqueue.getSubmitOn().length() > 19) {
										statement.setString(8, smscqueue.getSubmitOn().substring(0, 19));
									} else {
										statement.setString(8, smscqueue.getSubmitOn());
									}
								}
								statement.setString(9, smscqueue.getDeliverOn());
								status = smscqueue.getStatus();
								if (status != null && status.length() > 0) {
									if (status.length() > 8) {
										status = status.substring(0, 8);
									}
								} else {
									status = "ATES";
								}
								statement.setString(10, status);
								error_code = smscqueue.getErrorCode();
								if (error_code != null) {
									if (error_code.length() > 5) {
										error_code = error_code.substring(0, 5);
									}
								} else {
									error_code = "000";
								}
								statement.setString(11, error_code);
								statement.setBoolean(12, smscqueue.isRefund());
								statement.setString(13, smscqueue.getDeliverOn());
								statement.setString(14, status);
								statement.setString(15, error_code);
								statement.setBoolean(16, smscqueue.isRefund());
								statement.setString(17, smscqueue.getResponseid());
								statement.execute();
								count++;
							} catch (SQLSyntaxErrorException se) {
								if (se.getErrorCode() == 1146) {
									if (createTable()) {
										logger.info(table_name + " Created");
									} else {
										logger.error(table_name + " Creation Failed");
									}
								} else {
									logger.error(username, se);
								}
								Queue.enqueue(smscqueue);
							} catch (Exception ex) {
								// ex.printStackTrace();
								if (ex.getMessage().contains("Table") && ex.getMessage().contains("doesn't exist")) {
									if (createTable()) {
										logger.info(table_name + " Created");
									} else {
										logger.error(table_name + " Creation Failed");
									}
								} else {
									logger.error(username, ex);
								}
								Queue.enqueue(smscqueue);
							}
							connection.setAutoCommit(false);
							while (!Queue.isEmpty()) {
								try {
									smscqueue = (StatusObj) Queue.dequeue();
									statement.setString(1, smscqueue.getMsgid());
									statement.setString(2, smscqueue.getResponseid());
									statement.setString(3, smscqueue.getRoute());
									statement.setString(4, smscqueue.getOprCountry());
									statement.setString(5, smscqueue.getOrigSource());
									statement.setString(6, smscqueue.getDestination());
									statement.setDouble(7, smscqueue.getCost());
									if (smscqueue.getSubmitOn() == null) {
										String Systime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
										statement.setString(8, Systime);
									} else {
										if (smscqueue.getSubmitOn().length() > 19) {
											statement.setString(8, smscqueue.getSubmitOn().substring(0, 19));
										} else {
											statement.setString(8, smscqueue.getSubmitOn());
										}
									}
									statement.setString(9, smscqueue.getDeliverOn());
									status = smscqueue.getStatus();
									if (status != null && status.length() > 0) {
										if (status.length() > 8) {
											status = status.substring(0, 8);
										}
									} else {
										status = "ATES";
									}
									statement.setString(10, status);
									error_code = smscqueue.getErrorCode();
									if (error_code != null) {
										if (error_code.length() > 5) {
											error_code = error_code.substring(0, 5);
										}
									} else {
										error_code = "000";
									}
									statement.setString(11, error_code);
									statement.setBoolean(12, smscqueue.isRefund());
									statement.setString(13, smscqueue.getDeliverOn());
									statement.setString(14, status);
									statement.setString(15, error_code);
									statement.setBoolean(16, smscqueue.isRefund());
									statement.setString(17, smscqueue.getResponseid());
									// statement.execute();
									statement.addBatch();
								} catch (Exception e) {
									logger.error(username, e);
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
						logger.error(username, sqle);
					} catch (Exception e) {
						logger.error(username, e);
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
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
					}
				}
			}
		}
		logger.info(username + "_Mis Stopped. Queue Size: " + Queue.size());
	}

	private boolean createTable() {
		logger.debug("createTable() :" + table_name);
		String sql = "CREATE TABLE IF NOT EXISTS " + table_name + " ("
				+ "msg_id bigint(20) UNSIGNED NOT NULL DEFAULT 0," + "response_id varchar(50) NOT NULL DEFAULT '',"
				+ "Route_to_SMSC varchar(10) DEFAULT NULL," + "oprCountry int(5) DEFAULT 0,"
				+ "source_no varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
				+ "dest_no bigint(15) DEFAULT NULL," + "cost decimal(7,5) NOT NULL DEFAULT '0.00000',"
				+ "submitted_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
				+ "deliver_time timestamp NULL DEFAULT CURRENT_TIMESTAMP," + "Status varchar(8) DEFAULT 'PENDING',"
				+ "Err_code varchar(5) DEFAULT '000',"
				+ "post_crm tinyint(1) NULL DEFAULT 0,refund tinyint(1) NULL DEFAULT 0,"
				+ "PRIMARY KEY (msg_id),INDEX (submitted_time))";
		if (GlobalVars.DB_CLUSTER) {
			sql += " TABLESPACE disk_table_ts_1 STORAGE DISK;";
		} else {
			sql += ";";
		}
		Connection connection = null;
		PreparedStatement pStmt = null;
		boolean execute = true;
		try {
			connection = GlobalCache.connnection_pool_2.getConnection();
			pStmt = connection.prepareStatement(sql);
			pStmt.executeUpdate();
		} catch (Exception ex) {
			execute = false;
			logger.error(table_name + "createTable() " + ex);
		} finally {
			if (pStmt != null) {
				try {
					pStmt.close();
				} catch (SQLException ex) {
				}
			}
			if (connection != null) {
				GlobalCache.connnection_pool_2.putConnection(connection);
			}
		}
		logger.debug("createTable() :" + execute);
		return execute;
	}

	public void stop() {
		logger.info(username + "_Mis Stopping");
		stop = true;
	}
}
