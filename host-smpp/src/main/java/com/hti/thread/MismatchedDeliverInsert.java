/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.DumpMIS;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;

/**
 *
 * @author Administrator
 */
public class MismatchedDeliverInsert implements Runnable {
	private String sql = "INSERT INTO mismatch_dlr (response_id,time,Status,Err_code,smsc,source_no,dest_no) VALUES (?,?,?,?,?,?,?)";
	private Connection connection = null;
	private PreparedStatement pstm = null;
	private DumpMIS dumpMis = null;
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private boolean stop;

	public MismatchedDeliverInsert() {
		logger.info("MismatchedDeliverInsert Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			if (GlobalQueue.mismatchedDeliverQueue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException iex) {
				}
			} else {
				try {
					connection = GlobalCache.connnection_pool_1.getConnection();
					pstm = connection.prepareStatement(sql);
					connection.setAutoCommit(false);
					String year = null, month = null, date = null, hour = null, min = null, fDTiem = null, dTime = null;
					String error_code = null;
					while (!GlobalQueue.mismatchedDeliverQueue.isEmpty()) {
						dumpMis = (DumpMIS) GlobalQueue.mismatchedDeliverQueue.dequeue();
						pstm.setString(1, dumpMis.getResponseId());
						dTime = dumpMis.gettime();
						try {
							int interr_code = Integer.parseInt(dumpMis.geterrorcode());
							error_code = interr_code + "";
							if (error_code.length() > 0) {
								error_code = error_code.substring(0, 5);
							}
						} catch (Exception ex) {
							error_code = "000";
						}
						try {
							year = dTime.substring(0, 2);
							month = dTime.substring(2, 4);
							date = dTime.substring(4, 6);
							hour = dTime.substring(6, 8);
							min = dTime.substring(8, 10);
							fDTiem = "20" + year + "-" + month + "-" + date + " " + hour + ":" + min;
							pstm.setString(2, fDTiem);
						} catch (Exception ex) {
							pstm.setString(2, dTime);
						}
						pstm.setString(3, dumpMis.getStatus());
						pstm.setString(4, error_code);
						pstm.setString(5, dumpMis.getSmscName());
						pstm.setString(6, dumpMis.getSourceId());
						pstm.setString(7, dumpMis.getDestionationNo());
						pstm.addBatch();
					}
					pstm.executeBatch();
					connection.commit();
				} catch (Exception ex) {
					logger.error("", ex.fillInStackTrace());
				} finally {
					GlobalCache.connnection_pool_1.putConnection(connection);
					if (pstm != null) {
						try {
							pstm.close();
						} catch (SQLException ex) {
							pstm = null;
						}
					}
					connection = null;
					dumpMis = null;
				}
			}
		}
		logger.info("MismatchedDeliverInsert Stopped.Queue: " + GlobalQueue.mismatchedDeliverQueue.size());
	}

	public void stop() {
		logger.info("MismatchedDeliverInsert Stopping.Queue: " + GlobalQueue.mismatchedDeliverQueue.size());
		stop = true;
	}
}
