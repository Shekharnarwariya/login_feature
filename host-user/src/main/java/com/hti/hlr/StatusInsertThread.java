/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.hlr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;
import com.logica.smpp.util.ProcessingThread;

/**
 *
 * @author Administrator
 */
public class StatusInsertThread extends ProcessingThread {
	private Logger logger = LoggerFactory.getLogger("hlrLogger");
	private Connection connection = null;
	private PreparedStatement statement = null;
	private String sql = null;
	public static boolean DELETE_OLD_RECORDS;
	private List<LookupDTO> insertList = new ArrayList<LookupDTO>();
	private List<LookupDTO> respList = new ArrayList<LookupDTO>();
	private List<LookupDTO> dlrList = new ArrayList<LookupDTO>();

	public StatusInsertThread() {
		setThreadName("LookupStatusInsertThread");
	}

	@Override
	public void process() {
		if (GlobalVar.lookupStatusInsertQueue.isEmpty()) {
			if (DELETE_OLD_RECORDS) {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DATE, -1);
				String max_msg_id = new SimpleDateFormat("yyMMdd").format(calendar.getTime());
				max_msg_id = max_msg_id + "0000000000000";
				String deletesql = "delete from lookup_status where msg_id < " + max_msg_id + " limit 25000";
				logger.info(deletesql);
				try {
					connection = GlobalCache.connection_pool_proc.getConnection();
					statement = connection.prepareStatement(deletesql);
					int deletecount = statement.executeUpdate();
					System.out.println("lookup_status Deletion Count : " + deletecount);
					if (deletecount < 25000) {
						DELETE_OLD_RECORDS = false;
						logger.info("<-- No Records To Delete [lookup_status] -->");
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
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
			}
		} else {
			LookupDTO lookupDTO = null;
			int counter = 0;
			while (!GlobalVar.lookupStatusInsertQueue.isEmpty()) {
				lookupDTO = (LookupDTO) GlobalVar.lookupStatusInsertQueue.dequeue();
				if (lookupDTO.getQueryType().equalsIgnoreCase("insert")) {
					insertList.add(lookupDTO);
				} else if (lookupDTO.getQueryType().equalsIgnoreCase("resp")) {
					respList.add(lookupDTO);
				} else if (lookupDTO.getQueryType().equalsIgnoreCase("dlr")) {
					dlrList.add(lookupDTO);
				}
				if (++counter > 1000) {
					break;
				}
			}
			if (!insertList.isEmpty()) {
				logger.debug("Lookup Status Insert List: " + insertList.size());
				insert();
			}
			if (!respList.isEmpty()) {
				logger.debug("Lookup Status Response List: " + respList.size());
				response();
			}
			if (!dlrList.isEmpty()) {
				logger.debug("Lookup Status Dlr List: " + dlrList.size());
				dlr();
			}
		}
	}

	private void insert() {
		sql = "insert ignore into lookup_status(msg_id,username,destination,flag,server_id,time,seqNum) values(?,?,?,?,?,?,?)";
		try {
			connection = GlobalCache.connection_pool_proc.getConnection();
			statement = connection.prepareStatement(sql);
			connection.setAutoCommit(false);
			LookupDTO lookupDTO = null;
			while (!insertList.isEmpty()) {
				lookupDTO = (LookupDTO) insertList.remove(0);
				// logger.info(lookupDTO.getMsgid() + " [" + lookupDTO.getHlrid() + "] --->" + lookupDTO.getFlag());
				statement.setString(1, lookupDTO.getMsgid());
				statement.setString(2, lookupDTO.getUsername());
				statement.setString(3, lookupDTO.getDestination());
				statement.setString(4, lookupDTO.getFlag());
				statement.setInt(5, GlobalVars.SERVER_ID);
				statement.setString(6, lookupDTO.getSubmitTime());
				statement.setInt(7, lookupDTO.getSeqNum());
				statement.addBatch();
			}
			int[] executeBatch = statement.executeBatch();
			connection.commit();
			logger.debug("Lookup Status Insert Count: " + executeBatch.length);
		} catch (Exception ex) {
			logger.error("Lookup Status Insert Error: " + ex);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
					statement = null;
				}
			}
			GlobalCache.connection_pool_proc.putConnection(connection);
		}
	}

	private void response() {
		sql = "update lookup_status set flag=?,errorCode=?,error=?,status=?,resp_time=?,hlr_id=? where msg_id=?";
		try {
			connection = GlobalCache.connection_pool_proc.getConnection();
			statement = connection.prepareStatement(sql);
			connection.setAutoCommit(false);
			LookupDTO lookupDTO = null;
			while (!respList.isEmpty()) {
				lookupDTO = (LookupDTO) respList.remove(0);
				// logger.info(lookupDTO.getMsgid() + " [" + lookupDTO.getHlrid() + "] --->" + lookupDTO.getFlag());
				statement.setString(1, lookupDTO.getFlag());
				statement.setString(2, lookupDTO.getErrorCode());
				statement.setString(3, lookupDTO.getError());
				statement.setString(4, lookupDTO.getStatus());
				statement.setString(5, lookupDTO.getRespTime());
				statement.setString(6, lookupDTO.getHlrid());
				statement.setString(7, lookupDTO.getMsgid());
				statement.addBatch();
			}
			int[] executeBatch = statement.executeBatch();
			connection.commit();
			logger.debug("Lookup Status Resp Count: " + executeBatch.length);
		} catch (Exception ex) {
			logger.error("Lookup Status Resp Error: " + ex);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
					statement = null;
				}
			}
			GlobalCache.connection_pool_proc.putConnection(connection);
		}
	}

	private void dlr() {
		sql = "update lookup_status set flag=?,errorCode=?,error=?,status=?,nnc=?,isPorted=?,ported_nnc=?,isRoaming=?,roaming_nnc=?,dlr_time=? where msg_id = ?";
		try {
			connection = GlobalCache.connection_pool_proc.getConnection();
			statement = connection.prepareStatement(sql);
			connection.setAutoCommit(false);
			LookupDTO lookupDTO = null;
			while (!dlrList.isEmpty()) {
				lookupDTO = (LookupDTO) dlrList.remove(0);
				// logger.info(lookupDTO.getMsgid() + " [" + lookupDTO.getHlrid() + "] --->" + lookupDTO.getFlag());
				statement.setString(1, lookupDTO.getFlag());
				statement.setString(2, lookupDTO.getErrorCode());
				statement.setString(3, lookupDTO.getError());
				statement.setString(4, lookupDTO.getStatus());
				statement.setString(5, lookupDTO.getNnc());
				statement.setBoolean(6, lookupDTO.isPorted());
				statement.setString(7, lookupDTO.getPortedNNC());
				statement.setBoolean(8, lookupDTO.isRoaming());
				statement.setString(9, lookupDTO.getRoamingNNC());
				statement.setString(10, lookupDTO.getDlrTime());
				statement.setString(11, lookupDTO.getMsgid());
				statement.addBatch();
			}
			int[] executeBatch = statement.executeBatch();
			connection.commit();
			logger.debug("Lookup Status Dlr Count: " + executeBatch.length);
		} catch (Exception ex) {
			logger.error("Lookup Status Dlr Error: " + ex);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
					statement = null;
				}
			}
			GlobalCache.connection_pool_proc.putConnection(connection);
		}
	}

	@Override
	public void stop() {
		super.stopProcessing(null);
	}
}
