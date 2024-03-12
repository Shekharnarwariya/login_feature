package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.DeliverSMExt;
import com.hti.util.Constants;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;

public class DeliverTempInsert implements Runnable {
	private boolean stop;
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private Connection connection = null;
	private PreparedStatement statement = null;
	private String sql = "insert into deliver_sm_temp(msg_id,username,submitOn,deliverOn,source,destination,status,err_code,server_id) values(?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE status=?,deliverOn=?,err_code=?";
	private int temp_counter = 0;

	public DeliverTempInsert() {
		logger.info("DeliverTempInsert Thread Starting");
	}

	private void getTempCounter() {
		String temp_sql = "select count(msg_id) as count from deliver_sm_temp ";
		Connection temp_connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			temp_connection = GlobalCache.connnection_pool_1.getConnection();
			stmt = temp_connection.createStatement();
			rs = stmt.executeQuery(temp_sql);
			while (rs.next()) {
				temp_counter = rs.getInt("count");
			}
		} catch (Exception ex) {
			logger.error("getTempCounter()", ex.fillInStackTrace());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			GlobalCache.connnection_pool_1.putConnection(temp_connection);
		}
		logger.info("Temparary DeliverSM Counter:----> " + temp_counter);
	}

	private void putTempDeliver() {
		String temp_sql = "select * from deliver_sm_temp where server_id=" + GlobalVars.SERVER_ID + " limit 1000";
		Connection temp_connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		String msg_id_list = "";
		try {
			temp_connection = GlobalCache.connnection_pool_1.getConnection();
			stmt = temp_connection.createStatement();
			rs = stmt.executeQuery(temp_sql);
			DeliverSMExt deliver_sm = null;
			while (rs.next()) {
				deliver_sm = new DeliverSMExt(rs.getString("msg_id"), rs.getString("username"),
						rs.getString("submitOn"), rs.getString("deliverOn"), rs.getString("source"),
						rs.getString("destination"), rs.getString("status"), rs.getString("err_code"),
						rs.getInt("server_id"));
				GlobalQueue.DeliverProcessQueue.enqueue(deliver_sm);
				msg_id_list += "'" + rs.getString("msg_id") + "',";
				temp_counter--;
			}
			if (msg_id_list.length() > 0) {
				msg_id_list = msg_id_list.substring(0, msg_id_list.length() - 1);
				temp_sql = "delete from deliver_sm_temp where msg_id in(" + msg_id_list + ")";
				int delete_counter = stmt.executeUpdate(temp_sql);
				logger.debug("Records Deleted From DeliverSMTemp: " + delete_counter);
			}
		} catch (Exception ex) {
			logger.error("putTempDeliver()", ex.fillInStackTrace());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			GlobalCache.connnection_pool_1.putConnection(temp_connection);
		}
	}

	@Override
	public void run() {
		getTempCounter();
		while (!stop) {
			if (GlobalQueue.DeliverSMTempQueue.isEmpty()) {
				if (temp_counter > 0) {
					if (Constants.OMQ_DELIVER_STATUS && Constants.USER_HOST_STATUS) {
						putTempDeliver();
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			} else {
				DeliverSMExt deliver_sm_ext;
				int counter = 0;
				try {
					connection = GlobalCache.connnection_pool_1.getConnection();
					statement = connection.prepareStatement(sql);
					connection.setAutoCommit(false);
					while (!GlobalQueue.DeliverSMTempQueue.isEmpty()) {
						deliver_sm_ext = (DeliverSMExt) GlobalQueue.DeliverSMTempQueue.dequeue();
						statement.setString(1, deliver_sm_ext.getMsgid());
						statement.setString(2, deliver_sm_ext.getUsername());
						statement.setString(3, deliver_sm_ext.getSubmitOn());
						statement.setString(4, deliver_sm_ext.getDeliverOn());
						statement.setString(5, deliver_sm_ext.getSource());
						statement.setString(6, deliver_sm_ext.getDestination());
						statement.setString(7, deliver_sm_ext.getStatus());
						statement.setString(8, deliver_sm_ext.getErrorCode());
						statement.setInt(9, deliver_sm_ext.getServerId());
						statement.setString(10, deliver_sm_ext.getStatus());
						statement.setString(11, deliver_sm_ext.getDeliverOn());
						statement.setString(12, deliver_sm_ext.getErrorCode());
						statement.addBatch();
						temp_counter++;
						if (++counter > 1000) {
							break;
						}
					}
					if (counter > 0) {
						statement.executeBatch();
						connection.commit();
					}
				} catch (Exception ex) {
					logger.error("run()", ex);
				} finally {
					if (statement != null) {
						try {
							statement.close();
						} catch (SQLException e) {
							statement = null;
						}
					}
					GlobalCache.connnection_pool_1.putConnection(connection);
				}
			}
		}
		logger.info("DeliverTempInsert Thread Stopped. Queue: " + GlobalQueue.DeliverSMTempQueue.size());
	}

	public void stop() {
		logger.info("DeliverTempInsert Thread Stopping");
		stop = true;
	}
}
