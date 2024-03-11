package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.DeliverObj;
import com.hti.objects.DumpMIS;
import com.hti.objects.LogPDU;
import com.hti.objects.ResponseObj;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.logica.smpp.Data;

public class TrackMismatchedDeliver implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private boolean stop;
	private Map<String, Long> waitingQueue = new HashMap<String, Long>();
	private Map<String, DeliverObj> waitingQueueObject = new HashMap<String, DeliverObj>();
	private Set<String> enqueuedSet = new HashSet<String>();
	Iterator<Map.Entry<String, Long>> iterator;
	private int loop_counter = 0;
	private int processed_counter = 0;
	private int received_counter = 0; // total received
	private int waitingCounter = 0;
	private int waitingSuccess = 0;

	public TrackMismatchedDeliver() {
		logger.info(" TrackMismatchedDeliver Starting ");
	}

	@Override
	public void run() {
		while (!stop) {
			if (++loop_counter >= 30) {
				loop_counter = 0;
				if (!waitingQueue.isEmpty()) {
					iterator = waitingQueue.entrySet().iterator();
					Map.Entry<String, Long> entry = null;
					String resp_id = null;
					while (iterator.hasNext()) {
						entry = iterator.next();
						resp_id = entry.getKey();
						if (System.currentTimeMillis() >= entry.getValue()) {
							if (waitingQueueObject.containsKey(resp_id)) {
								logger.debug(" Putting to DeliverQueue to Recheck ");
								GlobalQueue.deliverWaitingQueue.enqueue(waitingQueueObject.remove(resp_id));
							}
							enqueuedSet.add(resp_id);
							iterator.remove();
						}
					}
				}
			}
			try {
				if (GlobalQueue.deliverWaitingQueue.isEmpty()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				} else {
					DeliverObj deliver = null;
					String response_id = null;
					String message_id = null;
					String status = null;
					int counter = 0;
					while (!GlobalQueue.deliverWaitingQueue.isEmpty()) {
						deliver = (DeliverObj) GlobalQueue.deliverWaitingQueue.dequeue();
						response_id = deliver.getResponseId();
						status = deliver.getStatus();
						message_id = null;
						boolean proceed = false;
						LogPDU log_pdu = checkFromMappedId(response_id);
						if (log_pdu == null) {
							if (enqueuedSet.contains(response_id)) { // already removed from waitingQueue.
								enqueuedSet.remove(response_id);
								System.out
										.println(response_id + "<- Mismatched Deliver Response ->" + deliver.getSmsc());
								GlobalQueue.mismatchedDeliverQueue.enqueue(
										new DumpMIS("", new SimpleDateFormat("yyMMddHHmmss").format(deliver.getTime()),
												deliver.getStatus(), deliver.getErrorCode(), response_id,
												deliver.getSmsc(), deliver.getSource(), deliver.getDestination()));
							} else { // put to waitingQueue
								received_counter++;
								waitingCounter++;
								waitingQueue.put(response_id, System.currentTimeMillis() + (60 * 1000));
								waitingQueueObject.put(response_id, deliver);
							}
						} else {
							if (enqueuedSet.contains(response_id)) {
								waitingSuccess++;
								enqueuedSet.remove(response_id);
							} else {
								received_counter++;
							}
							message_id = log_pdu.getMsgid();
							GlobalCache.ResponseLogDlrCache.put(message_id, log_pdu);
							proceed = true;
						}
						if (proceed) {
							String error_code = deliver.getErrorCode();
							String destination = deliver.getDestination();
							String source = deliver.getSource();
							// String time = deliver.getTime();
							System.out.println("<" + response_id + ">< Deliver Request ><" + destination + "> " + status
									+ ":-> " + deliver.getSmsc() + "-> " + source);
							GlobalQueue.processResponseQueue.enqueue(new ResponseObj(message_id, response_id, status,
									deliver.getTime(), error_code, Data.DELIVER_SM));
						}
						if (++processed_counter >= 1000) {
							processed_counter = 0;
							logger.info(" Waiting Deliver Received Count: " + received_counter + " Queue : "
									+ GlobalQueue.deliverWaitingQueue.size() + " WaitingQueue : " + waitingQueue.size()
									+ " WaitingCounter: " + waitingCounter + " WaitSuccess: " + waitingSuccess);
						}
						if (++counter > 1000) {
							break;
						}
					}
				}
			} catch (Exception ex) {
			}
		}
		logger.info(" Waiting Deliver Received Count: " + received_counter + " Queue : "
				+ GlobalQueue.deliverWaitingQueue.size() + " WaitingCounter: " + waitingCounter + " WaitSuccess: "
				+ waitingSuccess);
		if (!waitingQueueObject.isEmpty()) {
			Iterator<DeliverObj> itr = waitingQueueObject.values().iterator();
			DeliverObj deliver = null;
			while (itr.hasNext()) {
				deliver = itr.next();
				GlobalQueue.mismatchedDeliverQueue
						.enqueue(new DumpMIS("", new SimpleDateFormat("yyMMddHHmmss").format(deliver.getTime()),
								deliver.getStatus(), deliver.getErrorCode(), deliver.getResponseId(), deliver.getSmsc(),
								deliver.getSource(), deliver.getDestination()));
			}
		}
		if (!GlobalQueue.deliverWaitingQueue.isEmpty()) {
			DeliverObj deliver = null;
			while (!GlobalQueue.deliverWaitingQueue.isEmpty()) {
				deliver = (DeliverObj) GlobalQueue.deliverWaitingQueue.dequeue();
				GlobalQueue.mismatchedDeliverQueue
						.enqueue(new DumpMIS("", new SimpleDateFormat("yyMMddHHmmss").format(deliver.getTime()),
								deliver.getStatus(), deliver.getErrorCode(), deliver.getResponseId(), deliver.getSmsc(),
								deliver.getSource(), deliver.getDestination()));
			}
		}
		waitingQueueObject.clear();
		waitingQueue.clear();
		logger.info(" TrackMismatchedDeliver Stopped");
	}

	private LogPDU checkFromMappedId(String responseid) {
		logger.info("Checking From mapped_id: " + responseid);
		LogPDU log_pdu = null;
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		String sql = "select * from mapped_id where response_id=?";
		try {
			connection = GlobalCache.connnection_pool_1.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setString(1, responseid);
			rs = statement.executeQuery();
			if (rs.next()) {
				log_pdu = new LogPDU(rs.getString("msg_id"), rs.getString("route_to_SMSC"), rs.getString("client_name"),
						rs.getString("destination"), rs.getString("source"), rs.getString("time"), rs.getDouble("cost"),
						rs.getBoolean("refund"), rs.getInt("server_id"), 0);
			}
		} catch (Exception ex) {
			logger.error("checkFromMappedId(" + responseid + ")", ex);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
				}
			}
			if (connection != null) {
				GlobalCache.connnection_pool_1.putConnection(connection);
			}
		}
		return log_pdu;
	}

	public void stop() {
		logger.info(" TrackMismatchedDeliver Stopping ");
		stop = true;
	}
}
