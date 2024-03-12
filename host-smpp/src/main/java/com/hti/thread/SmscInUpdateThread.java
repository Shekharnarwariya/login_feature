/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.SmscInObj;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;

/**
 *
 * @author Administrator
 */
public class SmscInUpdateThread implements Runnable {
	private Connection connection = null;
	private PreparedStatement statement = null;
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private static Map<String, Long> waitingQueue = new HashMap<String, Long>();
	private static Set<String> enqueueSet = new HashSet<String>();
	private Map<String, SmscInObj> waitingObjectQueue = new HashMap<String, SmscInObj>();
	private Map<Integer, SmscInObj> tempMap = new HashMap<Integer, SmscInObj>();
	private Iterator<Map.Entry<String, Long>> itr;
	private Map.Entry<String, Long> entry = null;
	private static long discarded = 0;
	private int loop_counter = 0;
	private String sql = "update smsc_in set s_flag=?,smsc=?,group_id=? where msg_id=?";
	private SmscInObj smsc_in_obj_temp = null;
	private SmscInObj smsc_in_obj = null;
	private boolean stop;

	public SmscInUpdateThread() {
		logger.info("SmscInUpdateThread Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			if (++loop_counter > 10) {
				loop_counter = 0;
				if (!waitingQueue.isEmpty()) {
					itr = waitingQueue.entrySet().iterator();
					String messageid = null;
					while (itr.hasNext()) {
						entry = itr.next();
						if (System.currentTimeMillis() >= entry.getValue()) {
							messageid = entry.getKey();
							if (waitingObjectQueue.containsKey(messageid)) {
								GlobalQueue.smsc_in_update_Queue.enqueue(waitingObjectQueue.remove(messageid));
							}
							logger.debug(messageid + " Enqueued to update");
							enqueueSet.add(messageid);
							itr.remove();
						}
					}
				}
			}
			if (GlobalQueue.smsc_in_update_Queue.isEmpty()) {
				// doWait = false;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			} else {
				int count = 0;
				try {
					connection = GlobalCache.connnection_pool_2.getConnection();
					statement = connection.prepareStatement(sql);
					connection.setAutoCommit(false);
					while (!GlobalQueue.smsc_in_update_Queue.isEmpty()) {
						smsc_in_obj = (SmscInObj) GlobalQueue.smsc_in_update_Queue.dequeue();
						statement.setString(1, smsc_in_obj.getFlag());
						statement.setString(2, smsc_in_obj.getSmsc());
						statement.setInt(3, smsc_in_obj.getGroupId());
						statement.setString(4, smsc_in_obj.getMsgid());
						statement.addBatch();
						tempMap.put(count, smsc_in_obj);
						if (++count >= 100) {
							break;
						}
					}
					if (count > 0) {
						int[] rowup = statement.executeBatch();
						connection.commit();
						count = rowup.length;
						String messageid = null;
						for (int i = 0; i < rowup.length; i++) {
							smsc_in_obj_temp = (SmscInObj) tempMap.remove(i);
							if (!smsc_in_obj_temp.getFlag().equalsIgnoreCase("F")) { // no need to put Non Responding in waiting as it may be deleted
								messageid = smsc_in_obj_temp.getMsgid();
								if (rowup[i] < 1) {
									if (enqueueSet.contains(messageid)) {
										enqueueSet.remove(messageid);
										discarded++;
									} else {
										waitingQueue.put(messageid, System.currentTimeMillis() + (60 * 1000));
										waitingObjectQueue.put(messageid, smsc_in_obj_temp);
										logger.debug("[" + messageid + ": " + smsc_in_obj_temp.getFlag()
												+ "] Unable to Update. Enqueued to waiting");
									}
									count--;
								} else {
									/*
									 * logger.info( smsc_in_obj_temp.getSmsc() + "[" + messageid + "] : " + smsc_in_obj_temp.getFlag());
									 */
									if (enqueueSet.contains(messageid)) {
										logger.debug("[" + messageid + ": " + smsc_in_obj_temp.getFlag()
												+ "] Updated.Removed From Waiting");
										enqueueSet.remove(messageid);
									}
								}
							}
						}
						if (count > 0) {
							logger.debug("Smscin updation Count => " + count);
						}
					}
				} catch (SQLException se) {
					logger.error("process(1)", se.fillInStackTrace());
				} catch (Exception ew) {
					logger.error("process(2)", ew.fillInStackTrace());
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
				tempMap.clear();
			}
		}
		logger.info("SmscInUpdateThread Stopped.Queue: " + GlobalQueue.smsc_in_update_Queue.size());
	}

	public static String getStatistics() {
		String statistics = "Queue: " + GlobalQueue.smsc_in_update_Queue.size() + " Enqueued: " + enqueueSet.size()
				+ " waiting: " + waitingQueue.size() + " Discarded: " + discarded;
		return statistics;
	}

	public void stop() {
		logger.info("SmscInUpdateThread Stopping.Queue: " + GlobalQueue.smsc_in_update_Queue.size());
		stop = true;
	}
}
