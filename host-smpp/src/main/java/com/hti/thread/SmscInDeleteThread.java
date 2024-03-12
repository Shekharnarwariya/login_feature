/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;

/**
 *
 * @author Administrator
 */
public class SmscInDeleteThread implements Runnable {
	private Connection connection = null;
	private PreparedStatement statement = null;
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private boolean stop;
	private List<String> temp_list = new ArrayList<String>();

	public SmscInDeleteThread() {
		logger.info("SmscInDeleteThread Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				Thread.sleep(2 * 1000);
			} catch (InterruptedException ex) {
			}
			if (!GlobalQueue.smsc_in_delete_Queue.isEmpty()) {
				int count = 0;
				String msg_id = null;
				while (!GlobalQueue.smsc_in_delete_Queue.isEmpty()) {
					msg_id = (String) GlobalQueue.smsc_in_delete_Queue.dequeue();
					temp_list.add(msg_id);
					if (++count >= 1000) {
						break;
					}
				}
				if (!temp_list.isEmpty()) {
					try {
						connection = GlobalCache.connnection_pool_2.getConnection();
						statement = connection.prepareStatement(
								"delete from smsc_in where msg_id in(" + String.join(",", temp_list) + ")");
						int deleted = statement.executeUpdate();
						logger.debug("smsc_in Records removed : " + deleted + " Requested: " + temp_list.size());
					} catch (Exception e) {
						logger.error("", e);
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
					temp_list.clear();
				}
			}
		}
		logger.info("SmscInDeleteThread Stopped.Queue: " + GlobalQueue.smsc_in_delete_Queue.size());
	}

	public void stop() {
		logger.info("SmscInDeleteThread Stopping.Queue: " + GlobalQueue.smsc_in_delete_Queue.size());
		stop = true;
	}
}
