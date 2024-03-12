package com.hti.thread;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.exception.EntryNotFoundException;
import com.hti.objects.DeliverSMExt;
import com.hti.objects.LogPDU;
import com.hti.objects.ReportLogObject;
import com.hti.objects.ResponseObj;
import com.hti.objects.RoutePDU;
import com.hti.objects.SerialQueue;
import com.hti.objects.SignalRetryObj;
import com.hti.objects.SmscInObj;
import com.hti.objects.StatusObj;
import com.hti.objects.SubmittedObj;
import com.hti.user.UserBalance;
import com.hti.util.Constants;
import com.hti.util.FileUtil;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;
import com.logica.smpp.Data;

public class ProcessResponseThread implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private Logger tracklogger = LoggerFactory.getLogger("trackLogger");
	private boolean stop;
	private Map<String, Integer> retryCount = new HashMap<String, Integer>();
	private Set<String> retryWait = new HashSet<String>();
	private Map<String, ResponseObj> PartStatusForDlr = new HashMap<String, ResponseObj>();

	public ProcessResponseThread() {
		logger.info(" ProcessResponseThread Starting ");
		checkBackFolder();
	}

	@Override
	public void run() {
		logger.info(" ProcessResponseThread Started ");
		while (!stop) {
			try {
				if (GlobalQueue.processResponseQueue.isEmpty()) {
					// logger.info(" <--- processResponseQueue Empty --> ");
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				} else {
					// logger.info(" <--- processResponseQueue Size: --> " + GlobalQueue.processResponseQueue.size());
					int counter = 0;
					ResponseObj resp_obj = null;
					String message_id = null, status = null;
					StatusObj statusObj = null;
					while (!GlobalQueue.processResponseQueue.isEmpty()) {
						resp_obj = (ResponseObj) GlobalQueue.processResponseQueue.dequeue();
						message_id = resp_obj.getMsgid();
						status = resp_obj.getStatus();
						tracklogger.info("Processing Response: " + resp_obj.toString());
						LogPDU log_pdu = null;
						if (resp_obj.getCommandid() == Data.SUBMIT_SM_RESP) {
							if (GlobalCache.ResponseLogCache.containsKey(message_id)) {
								log_pdu = GlobalCache.ResponseLogCache.get(message_id);
							}
							if (log_pdu != null) {
								/*
								 * logger.info( "Response:--> " + log_pdu.getMsgid() + " Response:" + resp_obj.getResponseid());
								 */
								log_pdu.setResponseid(resp_obj.getResponseid());
								if (status.equalsIgnoreCase("ERR_RESP")) {
									statusObj = new StatusObj(log_pdu);
									if (log_pdu.isRefund()) {
										logger.debug(
												message_id + " <-- Need to Refund ERR_RESP --> " + log_pdu.getCost());
										if (doRefund(log_pdu.getUsername(), log_pdu.getCost())) {
											logger.debug(log_pdu.getMsgid() + " <-- Refund Success --> "
													+ log_pdu.getCost());
											statusObj.setRefund(true); // no chargeable cost
										} else {
											logger.info(
													log_pdu.getMsgid() + " <-- Refund Failed --> " + log_pdu.getCost());
										}
									}
									statusObj.setStatus(status);
									statusObj.setFlag("False");
									GlobalQueue.MIS_Dump_Queue.enqueue(statusObj);
									GlobalQueue.smsc_in_update_Queue.enqueue(new SmscInObj(log_pdu.getMsgid(),
											resp_obj.getFlag(), log_pdu.getRoute(), 0, log_pdu.getUsername()));
									enqueueUserMisQueue(statusObj);
									if (PartStatusForDlr.containsKey(message_id)) {
										PartStatusForDlr.remove(message_id);
									}
								} else {
									if (resp_obj.isMappedid()) {
										GlobalCache.ResponseLogDlrCache.put(message_id, log_pdu);
										GlobalQueue.MAPPED_ID_QUEUE.enqueue(log_pdu);
										GlobalCache.ResponseLogCache.remove(message_id); // no need further
									}
									if (resp_obj.isMis()) {
										statusObj = new StatusObj(log_pdu);
										statusObj.setStatus(status);
										statusObj.setFlag("False");
										if (resp_obj.getOldRoute() != null) {
											statusObj.setRoute(resp_obj.getOldRoute());
										}
										GlobalQueue.MIS_Dump_Queue.enqueue(statusObj);
										enqueueUserMisQueue(statusObj);
									}
									if (PartStatusForDlr.containsKey(message_id)) {
										ResponseObj resp_clone = PartStatusForDlr.remove(message_id);
										GlobalQueue.processResponseQueue.enqueue(resp_clone);
										logger.info(message_id + " Clone Added For Dlr Processing");
									}
								}
								if (log_pdu.isRerouted() && log_pdu.getRoutedSmsc() != null) { // enforce smsc case
									GlobalQueue.reportUpdateQueue.enqueue(new ReportLogObject(log_pdu.getMsgid(),
											log_pdu.getRoutedSmsc(), status, log_pdu.getSource()));
								} else {
									GlobalQueue.reportUpdateQueue.enqueue(new ReportLogObject(log_pdu.getMsgid(),
											log_pdu.getRoute(), status, log_pdu.getSource()));
								}
							} else {
								tracklogger.info(message_id + ":[" + resp_obj.getResponseid()
										+ "] <-- Log Pdu Not Found --> " + status);
							}
						} else if (resp_obj.getCommandid() == Data.DELIVER_SM) {
							boolean remove = true;
							if (GlobalCache.ResponseLogDlrCache.containsKey(message_id)) {
								log_pdu = GlobalCache.ResponseLogDlrCache.get(message_id);
							}
							if (log_pdu == null) {
								GlobalQueue.processRemovedResponseQueue.enqueue(resp_obj);
							} else {
								if (GlobalCache.DndSourceMsgId.containsKey(message_id)) {
									SubmittedObj submitObj = GlobalCache.DndSourceMsgId.remove(message_id);
									submitObj.setDeliverTime(
											new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(resp_obj.getTime()));
									GlobalQueue.dndLogQueue.enqueue(submitObj);
								}
								// logger.info(resp_obj.getMsgid() + " Dlr Process Routes:"+GlobalCache.DelayedDlrRoute);
								if (GlobalCache.DelayedDlrRoute.containsKey(log_pdu.getRoute())) {
									if (resp_obj.getDelayDlr() == 0) {
										logger.info(resp_obj.getMsgid() + " < Dlr Process Delay >");
										resp_obj.setDelayDlr(GlobalCache.DelayedDlrRoute.get(log_pdu.getRoute()));
										GlobalQueue.DelayedDlrQueue.enqueue(resp_obj);
										continue;
									}
								}
								/*
								 * logger.info( "Deliver:--> " + log_pdu.getMsgid() + " Response:" + resp_obj.getResponseid());
								 */
								if (status != null) {
									if (status.startsWith("ACCEP")) {
										remove = false;
									} else {
										if (status.startsWith("UNDELIV")) {
											boolean isSignaling = false;
											Integer[] criteria = null;
											if (GlobalCache.SignalingErrorCriteria.containsKey(resp_obj.getErrorCode()
													+ "#" + log_pdu.getRoute() + "#" + log_pdu.getUsername())) {
												isSignaling = true;
												criteria = GlobalCache.SignalingErrorCriteria
														.get(resp_obj.getErrorCode() + "#" + log_pdu.getRoute() + "#"
																+ log_pdu.getUsername());
												logger.debug(log_pdu.getMsgid() + " Criteria Found: "
														+ resp_obj.getErrorCode() + "#" + log_pdu.getRoute() + "#"
														+ log_pdu.getUsername());
											} else if (GlobalCache.SignalingErrorCriteria
													.containsKey(resp_obj.getErrorCode() + "#" + log_pdu.getRoute())) {
												isSignaling = true;
												criteria = GlobalCache.SignalingErrorCriteria
														.get(resp_obj.getErrorCode() + "#" + log_pdu.getRoute());
											}
											if (isSignaling) {
												if (criteria != null && criteria.length > 0) {
													int attempt = 1;
													if (retryCount.containsKey(log_pdu.getMsgid())) {
														attempt = retryCount.remove(log_pdu.getMsgid());
													}
													if (attempt <= criteria[0]) {
														logger.debug(log_pdu.getMsgid() + "[" + resp_obj.getErrorCode()
																+ "] Put to Resend. Attempt: " + attempt + " Interval: "
																+ criteria[1]);
														if (criteria[1] > 0) {
															GlobalQueue.signalRetryQueue.enqueue(new SignalRetryObj(
																	log_pdu.getMsgid(),
																	System.currentTimeMillis() + (criteria[1] * 1000)));
														} else {
															if (GlobalCache.ResendPDUCache
																	.containsKey(log_pdu.getMsgid())) {
																GlobalQueue.interProcessManage
																		.enqueue(GlobalCache.ResendPDUCache
																				.get(log_pdu.getMsgid()));
															}
														}
														retryCount.put(log_pdu.getMsgid(), ++attempt);
														remove = false;
													} else {
														logger.debug(log_pdu.getMsgid() + "[" + resp_obj.getErrorCode()
																+ "] Max Attempt Limit Exceeded. Attempt: " + attempt
																+ " Max: " + criteria[0]);
														if (criteria.length > 2) {
															logger.debug(
																	log_pdu.getMsgid() + "[" + resp_obj.getErrorCode()
																			+ "] Put to Wait FOr Retry.");
															if (GlobalCache.ResendPDUCache
																	.containsKey(log_pdu.getMsgid())) {
																GlobalQueue.signalWaitQueue.enqueue(
																		new SignalRetryObj(GlobalCache.ResendPDUCache
																				.get(log_pdu.getMsgid()), criteria));
																logger.debug(log_pdu.getMsgid() + "["
																		+ resp_obj.getErrorCode()
																		+ "] Added To WaitingQueue");
																if (!retryWait.contains(log_pdu.getMsgid())) {
																	retryWait.add(log_pdu.getMsgid());
																	logger.debug(log_pdu.getMsgid() + "["
																			+ resp_obj.getErrorCode()
																			+ "] Added To WaitingCounter");
																}
															} else {
																logger.debug(log_pdu.getMsgid() + "["
																		+ resp_obj.getErrorCode()
																		+ "] Not Found in Cache.");
															}
														}
														if (GlobalCache.ResendPDUCache
																.containsKey(log_pdu.getMsgid())) {
															GlobalCache.ResendPDUCache.remove(log_pdu.getMsgid());
														}
													}
												}
											}
										} else {
											if (retryCount.containsKey(log_pdu.getMsgid())) {
												retryCount.remove(log_pdu.getMsgid());
											}
											if (retryWait.contains(log_pdu.getMsgid())) {
												retryWait.remove(log_pdu.getMsgid());
												SignalWaitProcess.DELIVERED_SET.add(log_pdu.getMsgid());
											}
											if (GlobalCache.ResendPDUCache.containsKey(log_pdu.getMsgid())) {
												logger.debug(log_pdu.getMsgid() + " Removing From Resend Cache");
												GlobalCache.ResendPDUCache.remove(log_pdu.getMsgid());
											}
										}
									}
								}
								String submitOn = null;
								Date submit_date = null;
								// String time = resp_obj.getTime();
								/*
								 * GlobalQueue.MappingLogWriteQueue .enqueue(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " [dlr]:-> [" + message_id + ":" + log_pdu.getMsgid() +
								 * "][" + resp_obj.getResponseid() + "][" + status + "]" + "[" + log_pdu.getRoute() + "]" + "[" + log_pdu.getUsername() + "]" + "\n");
								 */
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
													long secondDiff = (deliverOn.getTime() - submit_date.getTime())
															/ 1000;
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
										logger.error(log_pdu.getRoute() + " Custom Dlr Time[" + custom_time
												+ "] Error: " + ex);
									}
								}
								if (deliverOn.before(submit_date)) {
									deliverOn = submit_date;
								}
								statusObj = new StatusObj(log_pdu);
								// ------- check for Refund --------------------
								if (log_pdu.isRefund()) {
									if (status != null && !status.startsWith("ACCEP") && !status.startsWith("DELIV")) { // make
																														// refund
										logger.debug(
												log_pdu.getMsgid() + " <-- Need to Refund --> " + log_pdu.getCost());
										if (doRefund(log_pdu.getUsername(), log_pdu.getCost())) {
											logger.debug(log_pdu.getMsgid() + " <-- Refund Success --> "
													+ log_pdu.getCost());
											statusObj.setRefund(true); // no chargeable cost
										} else {
											logger.info(
													log_pdu.getMsgid() + " <-- Refund Failed --> " + log_pdu.getCost());
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
								if (log_pdu.isRerouted() && log_pdu.getRoutedSmsc() != null) {
									GlobalQueue.reportUpdateQueue.enqueue(new ReportLogObject(log_pdu.getMsgid(),
											log_pdu.getRoutedSmsc(), status, log_pdu.getSource()));
								} else {
									GlobalQueue.reportUpdateQueue.enqueue(new ReportLogObject(log_pdu.getMsgid(),
											log_pdu.getRoute(), status, log_pdu.getSource()));
								}
								enqueueUserMisQueue(statusObj);
								// --------- put to user omq -------------------
								if (remove) { // final dlr
									GlobalCache.ResponseLogDlrCache.remove(message_id);
									if (log_pdu.getRegisterdlr() == 0) {
										logger.info(log_pdu.getMsgid() + " [" + log_pdu.getUsername()
												+ "] Dlr Receiving disabled");
									} else {
										GlobalQueue.DeliverProcessQueue.enqueue(
												new DeliverSMExt(log_pdu.getMsgid(), log_pdu.getUsername(), submitOn,
														new SimpleDateFormat("yyMMddHHmmss").format(deliverOn),
														log_pdu.getSource(), log_pdu.getDestination(), status,
														resp_obj.getErrorCode(), log_pdu.getServerId()));
									}
								}
								if (GlobalCache.WaitingForDeliveredPart.containsKey(log_pdu.getMsgid())) {
									List<RoutePDU> pdu_list = null;
									boolean part_mapping = false;
									if (status.startsWith("ACCEP")) {
										pdu_list = GlobalCache.WaitingForDeliveredPart.get(log_pdu.getMsgid());
										if (GlobalCache.PartMappingForDlr.containsKey(log_pdu.getMsgid())) {
											part_mapping = true;
										}
									} else {
										pdu_list = GlobalCache.WaitingForDeliveredPart.remove(log_pdu.getMsgid());
										if (GlobalCache.PartMappingForDlr.containsKey(log_pdu.getMsgid())) {
											GlobalCache.PartMappingForDlr.remove(log_pdu.getMsgid());
											part_mapping = true;
										}
									}
									if (status.startsWith("DELIV")) { // proceed to submit
										for (RoutePDU pdu : pdu_list) {
											if (part_mapping) {
												ResponseObj resp_obj_clone = (ResponseObj) resp_obj.clone();
												resp_obj_clone.setMsgid(pdu.getHtiMsgId());
												PartStatusForDlr.put(pdu.getHtiMsgId(), resp_obj_clone);
											}
											logger.info(pdu.getHtiMsgId() + " Adding to interprocess Queue");
											GlobalQueue.interProcessManage.enqueue(pdu);
										}
									} else { // send dlr as received for first part
										for (RoutePDU pdu : pdu_list) {
											logger.info(pdu.getHtiMsgId() + " Creating Response as per first Part");
											GlobalQueue.smsc_in_delete_Queue.enqueue(pdu.getHtiMsgId());
											StatusObj partStatusObject = (StatusObj) statusObj.clone();
											partStatusObject.setMsgid(pdu.getHtiMsgId());
											if (log_pdu.isRefund()) {
												if (!status.startsWith("ACCEP")) { // make refund
													logger.debug(pdu.getHtiMsgId() + " <-- Need to Refund --> "
															+ pdu.getCost());
													if (doRefund(pdu.getUsername(), pdu.getCost())) {
														logger.debug(pdu.getHtiMsgId() + " <-- Refund Success --> "
																+ pdu.getCost());
														partStatusObject.setRefund(true); // no chargeable cost
													} else {
														logger.info(pdu.getHtiMsgId() + " <-- Refund Failed --> "
																+ pdu.getCost());
													}
												}
											}
											GlobalQueue.MIS_Dump_Queue.enqueue(partStatusObject);
											if (log_pdu.isRerouted() && log_pdu.getRoutedSmsc() != null) {
												GlobalQueue.reportUpdateQueue
														.enqueue(new ReportLogObject(pdu.getHtiMsgId(),
																log_pdu.getRoutedSmsc(), status, log_pdu.getSource()));
											} else {
												GlobalQueue.reportUpdateQueue
														.enqueue(new ReportLogObject(pdu.getHtiMsgId(),
																log_pdu.getRoute(), status, log_pdu.getSource()));
											}
											enqueueUserMisQueue(partStatusObject);
											if (log_pdu.getRegisterdlr() == 0) {
												logger.info(pdu.getHtiMsgId() + " [" + log_pdu.getUsername()
														+ "] Dlr Receiving disabled");
											} else {
												GlobalQueue.DeliverProcessQueue.enqueue(new DeliverSMExt(
														pdu.getHtiMsgId(), log_pdu.getUsername(), submitOn,
														new SimpleDateFormat("yyMMddHHmmss").format(deliverOn),
														log_pdu.getSource(), log_pdu.getDestination(), status,
														resp_obj.getErrorCode(), log_pdu.getServerId()));
											}
										}
									}
								} else {
									if (GlobalCache.PartMappingForDlr.containsKey(log_pdu.getMsgid())) {
										HashSet<String> parts = GlobalCache.PartMappingForDlr.get(log_pdu.getMsgid());
										GlobalCache.PartMappingForDlr.remove(log_pdu.getMsgid());
										for (String part_msg_id : parts) {
											logger.info(part_msg_id + " Creating Clone Deliver Status.");
											// put in queue again with replaced msg_id
											ResponseObj resp_obj_clone = (ResponseObj) resp_obj.clone();
											resp_obj_clone.setMsgid(part_msg_id);
											GlobalQueue.processResponseQueue.enqueue(resp_obj_clone);
										}
									}
								}
								statusObj = null;
							}
						} else {
							// tracklogger.info("Unknown Object Found: " + resp_obj);
						}
						if (++counter > 1000) {
							break;
						}
					}
				}
			} catch (Exception e) {
				logger.error("run()", e.fillInStackTrace());
			}
		}
		if (!GlobalQueue.processResponseQueue.isEmpty()) {
			logger.info(" Process Response Queue: " + GlobalQueue.processResponseQueue.size());
			try {
				FileUtil.writeObject(Constants.resp_process_backup_dir + "respProcessQueue.ser",
						GlobalQueue.processResponseQueue);
			} catch (Exception ex) {
				logger.error(ex + " While Writing Response Process Backup Queue ");
			}
		}
		logger.info(" ProcessResponseThread Stopped.");
	}

	private boolean doRefund(String to, double amount) {
		try {
			UserBalance balance = GlobalVars.userService.getUserBalance(to);
			if (balance != null) {
				logger.debug(to + " Wallet Flag: " + balance.getFlag());
				if (balance.getFlag().equalsIgnoreCase("No")) {
					return balance.refundCredit(1);
				} else {
					return balance.refundAmount(amount);
				}
			} else {
				return false;
			}
		} catch (EntryNotFoundException ex) {
			logger.error(to + " <-- Balance Entry Not Found [" + amount + "] --> ");
			return false;
		} catch (Exception ex) {
			logger.info(to + " <-- Balance refund Error [" + amount + "] --> " + ex);
			return false;
		}
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

	private void checkBackFolder() {
		logger.info("<- Checking for Response Process Backup File ->");
		File file = new File(Constants.resp_process_backup_dir + "respProcessQueue.ser");
		if (file.exists()) {
			logger.info("<- Response Process Backup File Found -> ");
			try {
				SerialQueue tempQueue = (SerialQueue) FileUtil
						.readObject(Constants.resp_process_backup_dir + "respProcessQueue.ser", true);
				if (tempQueue != null && !tempQueue.isEmpty()) {
					// logger.info(" Response temp Queue Size ---> " + tempQueue.size());
					while (!tempQueue.isEmpty()) {
						GlobalQueue.processResponseQueue.enqueue((ResponseObj) tempQueue.dequeue());
					}
					logger.info(" Response Process Backup Queue Size ---> " + GlobalQueue.processResponseQueue.size());
				}
			} catch (Exception e) {
				logger.error(e + " While Reading Response Process Backup Queue ");
			}
		}
	}

	public void stop() {
		logger.info(" ProcessResponseThread Stopping ");
		stop = true;
	}
}
