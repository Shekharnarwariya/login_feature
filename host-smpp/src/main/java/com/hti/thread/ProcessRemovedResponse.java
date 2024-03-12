package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.DeliverSMExt;
import com.hti.objects.LogPDU;
import com.hti.objects.ReportLogObject;
import com.hti.objects.ResponseObj;
import com.hti.objects.SerialQueue;
import com.hti.objects.StatusObj;
import com.hti.objects.SubmittedObj;
import com.hti.user.UserBalance;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;

public class ProcessRemovedResponse implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	// private Logger logger_debug = LoggerFactory.getLogger(ProcessRemovedResponse.class);
	private Logger tracklogger = LoggerFactory.getLogger("trackLogger");
	private boolean stop;
	private static int success_counter;
	private static int failure_counter;

	public ProcessRemovedResponse() {
		logger.info(" ProcessRemovedResponse Thread Starting ");
	}

	@Override
	public void run() {
		logger.info(" ProcessRemovedResponse Started ");
		while (!stop) {
			try {
				if (GlobalQueue.processRemovedResponseQueue.isEmpty()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				} else {
					int counter = 0;
					ResponseObj resp_obj = null;
					String message_id = null, status = null;
					LogPDU log_pdu = null;
					StatusObj statusObj = null;
					while (!GlobalQueue.processRemovedResponseQueue.isEmpty()) {
						resp_obj = (ResponseObj) GlobalQueue.processRemovedResponseQueue.dequeue();
						message_id = resp_obj.getMsgid();
						status = resp_obj.getStatus();
						tracklogger.info("ProcessingRem Response: " + resp_obj.toString());
						log_pdu = checkFromMappedId(resp_obj.getResponseid());
						if (log_pdu != null) {
							String username = log_pdu.getUsername();
							String msg_id = log_pdu.getMsgid();
							String submitOn = null;
							Date submit_date = null;
							String source = log_pdu.getSource();
							String destination = log_pdu.getDestination();
							if (GlobalCache.DndSourceMsgId.containsKey(message_id)) {
								SubmittedObj submitObj = GlobalCache.DndSourceMsgId.remove(message_id);
								submitObj.setDeliverTime(
										new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(resp_obj.getTime()));
								GlobalQueue.dndLogQueue.enqueue(submitObj);
							}
							// String time = resp_obj.getTime();
							if (log_pdu.getSubmitOn() != null) {
								try {
									submit_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
											.parse(log_pdu.getSubmitOn());
									submitOn = new SimpleDateFormat("yyMMddHHmmss").format(submit_date);
								} catch (ParseException e) {
									submit_date = new Date();
									submitOn = new SimpleDateFormat("yyMMddHHmmss").format(submit_date);
								}
							} else {
								submit_date = new Date();
								submitOn = new SimpleDateFormat("yyMMddHHmmss").format(submit_date);
							}
							Date deliverOn = null;
							if (resp_obj.getTime() == null) {
								deliverOn = new Date();
								logger.info(log_pdu.getMsgid() + " Invalid deliver time: " + resp_obj.getTime());
							} else {
								deliverOn = resp_obj.getTime();
							}
							if (GlobalCache.CustomDlrTime.containsKey(log_pdu.getRoute())) {
								String custom_time = GlobalCache.CustomDlrTime.get(log_pdu.getRoute());
								try {
									String hh = custom_time.substring(1, 3);
									String mm = custom_time.substring(3, 5);
									String ss = custom_time.substring(5, 7);
									Calendar calendar = Calendar.getInstance();
									calendar.setTime(deliverOn);
									if (custom_time.startsWith("+")) {
										calendar.add(Calendar.HOUR, Integer.parseInt(hh));
										calendar.add(Calendar.MINUTE, Integer.parseInt(mm));
										calendar.add(Calendar.SECOND, Integer.parseInt(ss));
									} else {
										boolean applyCustomTime = true;
										// check for latency if configured
										if (custom_time.contains("#")) {
											int max_latency = Integer
													.parseInt(custom_time.substring(custom_time.indexOf("#") + 1));
											if (submit_date.before(deliverOn)) {
												long secondDiff = (deliverOn.getTime() - submit_date.getTime()) / 1000;
												if (secondDiff <= max_latency) { // no changes in dlr time
													applyCustomTime = false;
												}
											}
										}
										if (applyCustomTime) {
											calendar.add(Calendar.HOUR, -Integer.parseInt(hh));
											calendar.add(Calendar.MINUTE, -Integer.parseInt(mm));
											calendar.add(Calendar.SECOND, -Integer.parseInt(ss));
										}
									}
									deliverOn = calendar.getTime();
								} catch (Exception ex) {
									logger.error(
											log_pdu.getRoute() + " Custom Dlr Time[" + custom_time + "] Error: " + ex);
								}
							}
							if (deliverOn.before(submit_date)) {
								deliverOn = submit_date;
							}
							statusObj = new StatusObj(log_pdu);
							// ------- check for Refund --------------------
							if (log_pdu.isRefund()) {
								if (status != null && !status.startsWith("ACCEP") && !status.startsWith("DELIV")) { // make refund
									logger.debug(msg_id + " <-- Need to Refund --> " + log_pdu.getCost());
									if (doRefund(username, log_pdu.getCost())) {
										logger.debug(msg_id + " <-- Refund Success --> " + log_pdu.getCost());
										statusObj.setRefund(true); // no chargeable cost
									} else {
										logger.info(msg_id + " <-- Refund Failed --> " + log_pdu.getCost());
									}
								}
							}
							// ---------------------------------------------
							statusObj.setResponseid(resp_obj.getResponseid());
							statusObj.setDeliverOn(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(deliverOn));
							statusObj.setStatus(status);
							statusObj.setErrorCode(resp_obj.getErrorCode());
							statusObj.setFlag("True");
							GlobalQueue.MIS_Dump_Queue.enqueue(statusObj);
							enqueueUserMisQueue(statusObj);
							GlobalQueue.reportUpdateQueue.enqueue(new ReportLogObject(log_pdu.getMsgid(),
									log_pdu.getRoute(), status, log_pdu.getSource()));
							// --------- put to user omq -------------------
							if (status != null && !status.startsWith("ACCEP")) {
								if (log_pdu.getRegisterdlr() == 0) {
									logger.info(msg_id + "[" + username + "] Dlr Receiving disabled");
								} else {
									GlobalQueue.DeliverProcessQueue.enqueue(new DeliverSMExt(msg_id, username, submitOn,
											new SimpleDateFormat("yyMMddHHmmss").format(deliverOn), source, destination,
											status, resp_obj.getErrorCode(), log_pdu.getServerId()));
								}
							}
							statusObj = null;
							success_counter++;
						} else {
							failure_counter++;
							logger.info(message_id + "<" + resp_obj.getResponseid() + "> Mapped_id Not Found <"
									+ resp_obj.getStatus());
						}
						if (++counter > 1000) {
							break;
						}
					}
				}
			} catch (Exception ex) {
				logger.error("", ex.fillInStackTrace());
			}
		}
		logger.info(" ProcessRemovedResponse Thread Stopped.Queue: " + GlobalQueue.processRemovedResponseQueue.size());
	}

	private void enqueueUserMisQueue(StatusObj statusObj) {
		SerialQueue user_mis_Queue = null;
		// String client_name = statusObj.getUsername();
		if (GlobalCache.UserMisQueueObject.containsKey(statusObj.getUsername())) {
			user_mis_Queue = ((UserWiseMis) GlobalCache.UserMisQueueObject.get(statusObj.getUsername())).getQueue();
		} else {
			user_mis_Queue = new SerialQueue();
			UserWiseMis userWiseMis = new UserWiseMis(statusObj.getUsername(), user_mis_Queue);
			new Thread(userWiseMis, statusObj.getUsername() + "_Mis").start();
			GlobalCache.UserMisQueueObject.put(statusObj.getUsername(), userWiseMis);
		}
		user_mis_Queue.enqueue(statusObj);
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
				log_pdu.setRegisterdlr((byte) 1);
			}
		} catch (Exception ex) {
			logger.error("checkFromMappedId(" + responseid + ")", ex.fillInStackTrace());
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

	private boolean doRefund(String to, double amount) {
		try {
			UserBalance balance = GlobalVars.userService.getUserBalance(to);
			if (balance != null) {
				if (balance.getFlag().equalsIgnoreCase("No")) {
					return balance.refundCredit(1);
				} else {
					return balance.refundAmount(amount);
				}
			} else {
				return false;
			}
		} catch (Exception ex) {
			logger.info(to + " <-- Balance refund Error [" + amount + "] --> " + ex);
			return false;
		}
	}

	public static String getStatistics() {
		return "Queue: " + GlobalQueue.processRemovedResponseQueue.size() + " Success: " + success_counter + " Failed: "
				+ failure_counter;
	}

	public void stop() {
		stop = true;
		logger.info(" ProcessRemovedResponse Thread Stopping.Queue: " + GlobalQueue.processRemovedResponseQueue.size());
	}
}
