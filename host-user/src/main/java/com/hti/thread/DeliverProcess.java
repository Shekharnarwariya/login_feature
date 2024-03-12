package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.DeliverSMExt;
import com.hti.objects.HTIQueue;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.user.UserDeliverForward;
import com.hti.user.WebDeliverProcess;
import com.hti.user.dto.DlrSettingEntry;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;
import com.logica.smpp.Data;
import com.logica.smpp.pdu.DeliverSM;
import com.logica.smpp.pdu.WrongLengthOfStringException;
import com.logica.smpp.pdu.tlv.WrongLengthException;
import com.logica.smpp.util.ByteBuffer;

public class DeliverProcess implements Runnable {
	private boolean stop;
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private int received_couner = 0;
	private Map<String, Integer> prefix_mapping;
	public static boolean RELOAD_NNC;
	StringBuilder buffer = null;
	private Map<String, String> StatusCode = new HashMap<String, String>();
	private int processed_counter = 0;

	public DeliverProcess() {
		logger.info("DeliverProcess Thread Started");
		loadStatusCode();
		prefix_mapping = new HashMap<String, Integer>(GlobalCache.PrefixMapping);
	}

	public void run() {
		while (!stop) {
			try {
				if (RELOAD_NNC) {
					try {
						prefix_mapping = new HashMap<String, Integer>(GlobalCache.PrefixMapping);
						logger.info("********* NNC Records Reloaded *********");
					} catch (Exception e) {
						logger.error("", e.fillInStackTrace());
					}
					RELOAD_NNC = false;
				}
				if (GlobalQueue.DeliverProcessQueue.isEmpty()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				} else {
					int couner = 0;
					while (!GlobalQueue.DeliverProcessQueue.isEmpty()) {
						process((DeliverSMExt) GlobalQueue.DeliverProcessQueue.dequeue());
						received_couner++;
						if (++processed_counter >= 5000) {
							processed_counter = 0;
							logger.info("DeliverRequest Process Queue: " + GlobalQueue.DeliverProcessQueue.size()
									+ " Processed: " + received_couner);
						}
						if (++couner > 1000) {
							break;
						}
					}
				}
			} catch (Exception e) {
				logger.error("run()", e);
			}
		}
		logger.info("DeliverProcess Thread Stopped.Queue: " + GlobalQueue.DeliverProcessQueue.size());
	}

	private void process(DeliverSMExt deliver_sm) {
		String client_name = deliver_sm.getUsername();
		logger.debug(client_name + " Processing Dlr[" + deliver_sm.getMsgid() + "].");
		try {
			DlrSettingEntry entry = GlobalVars.userService.getDlrSettingEntry(client_name);
			if (entry != null) {
				boolean proceed = true;
				if (deliver_sm.getStatus() != null && deliver_sm.getStatus().startsWith("ACCEP")) {
					if (entry.isAccepted()) {
						// user is enabled to get Accepted Status
					} else {
						proceed = false;
						logger.info(client_name + "[" + deliver_sm.getMsgid() + "] ACCEPTD Dlr is Disabled.");
					}
				}
				if (proceed) {
					DeliverSM forward = new DeliverSM();
					buffer = new StringBuilder();
					buffer.append("id:").append(deliver_sm.getMsgid()).append(" ");
					buffer.append("sub:001").append(" ");
					buffer.append("dlvrd:001").append(" ");
					try {
						String gmt = entry.getCustomGmt();
						if (gmt != null) {
							String custom_submit_on = timeConversion(gmt, deliver_sm.getSubmitOn());
							String custom_deliver_on = timeConversion(gmt, deliver_sm.getDeliverOn());
							buffer.append("submit date:").append(custom_submit_on).append(" ");
							buffer.append("done date:").append(custom_deliver_on).append(" ");
						} else {
							buffer.append("submit date:").append(deliver_sm.getSubmitOn()).append(" ");
							buffer.append("done date:").append(deliver_sm.getDeliverOn()).append(" ");
						}
						if (entry.isReverseSrc()) {
							forward.setSourceAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164,
									deliver_sm.getDestination());
							forward.setDestAddr((byte) 5, (byte) 0, deliver_sm.getSource());
						} else {
							forward.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164,
									deliver_sm.getDestination());
							forward.setSourceAddr((byte) 5, (byte) 0, deliver_sm.getSource());
						}
						if (entry.isNncDlr()) {
							String destination = deliver_sm.getDestination();
							int length = destination.length();
							String mcc = "", mnc = "";
							int network_id = 0;
							try {
								for (int i = length; i >= 1; i--) {
									if (prefix_mapping != null
											&& prefix_mapping.containsKey(destination.substring(0, i))) {
										network_id = prefix_mapping.get(destination.substring(0, i));
										break;
									}
								}
								if (network_id > 0) {
									NetworkEntry network_entry = GlobalCache.NetworkEntries.get(network_id);
									if (network_entry != null) {
										mcc = network_entry.getMcc();
										mnc = network_entry.getMnc();
									}
								}
							} catch (Exception e) {
								logger.info(
										e + " While Checking NNC for Deliver_sm<" + destination + ">: " + client_name);
							}
							buffer.append("mcc:").append(mcc).append(" ").append("mnc:").append(mnc).append(" ");
						}
						buffer.append("stat:").append(deliver_sm.getStatus()).append(" ");
						String errorCode = deliver_sm.getErrorCode();
						if (entry.isFixedCode() && StatusCode.containsKey(deliver_sm.getStatus())) {
							errorCode = StatusCode.get(deliver_sm.getStatus());
						}
						buffer.append("err:").append(errorCode).append(" ");
						buffer.append("text:").append(" ");
						forward.setNetworkErrorCode(new ByteBuffer(errorCode.getBytes()));
						int statusInt = 1;
						if (deliver_sm.getStatus().equalsIgnoreCase("DELIVRD")) {
							statusInt = Data.SM_STATE_DELIVERED;
						} else if (deliver_sm.getStatus().equalsIgnoreCase("UNDELIV")) {
							statusInt = Data.SM_STATE_UNDELIVERABLE;
						} else if (deliver_sm.getStatus().equalsIgnoreCase("ACCEPTD")) {
							statusInt = Data.SM_STATE_ACCEPTED;
						} else if (deliver_sm.getStatus().equalsIgnoreCase("REJECTD")) {
							statusInt = Data.SM_STATE_REJECTED;
						} else if (deliver_sm.getStatus().equalsIgnoreCase("EXPIRED")) {
							statusInt = Data.SM_STATE_EXPIRED;
						}
						forward.setMessageState((byte) statusInt);
						forward.setEsmClass((byte) Data.SM_SMSC_DLV_RCPT_TYPE);
					} catch (Exception ex) {
						logger.error("", ex.fillInStackTrace());
					}
					try {
						forward.setShortMessage(buffer.toString());
					} catch (WrongLengthOfStringException wlse) {
						logger.error(client_name + ":-> WrongLengthOfStringException For Deliver_SM: "
								+ deliver_sm.getMsgid());
					}
					try {
						forward.setReceiptedMessageId(deliver_sm.getMsgid());
					} catch (WrongLengthException wle) {
						logger.error(client_name + ":-> WrongLengthException For Deliver_SM: " + deliver_sm.getMsgid());
					}
					forward.setClientName(client_name);
					forward.setServerId(deliver_sm.getServerId());
					// System.out.println(deliver_sm.getMsgid() + ": " + deliver_sm.getServerId());
					if (entry.isWebDlr()) {
						if (GlobalCache.WebDeliverProcessQueue.containsKey(client_name)) {
							((HTIQueue) GlobalCache.WebDeliverProcessQueue.get(client_name)).enqueue(forward);
						} else {
							HTIQueue dlrQueue = new HTIQueue();
							dlrQueue.enqueue(forward);
							GlobalCache.WebDeliverProcessQueue.put(client_name, dlrQueue);
							if (!GlobalCache.UserWebObject.containsKey(client_name)) {
								boolean web_dlr_param = false;
								if (entry.getWebDlrParam() != null && entry.getWebDlrParam().length() > 0) {
									web_dlr_param = true;
								}
								WebDeliverProcess webDeliverForward = new WebDeliverProcess(client_name,
										entry.getWebUrl(), dlrQueue, false, web_dlr_param);
								GlobalCache.UserWebObject.put(client_name, webDeliverForward);
								new Thread(webDeliverForward, client_name + "_WebDeliverForward").start();
							}
						}
					} else {
						if (GlobalCache.UserRxObject.containsKey(client_name)
								&& ((UserDeliverForward) GlobalCache.UserRxObject.get(client_name)).isActive()) {
							// logger.debug(client_name + " : Enqueued to DLR Queue");
							((HTIQueue) GlobalCache.UserDeliverProcessQueue.get(client_name)).enqueue(forward);
						} else {
							logger.debug(client_name + " : No Receiver Found");
							GlobalQueue.DLRInsertQueue.add(forward);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(client_name, e);
		}
	}

	private void loadStatusCode() {
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Connection connection = null;
		String sql = "select * from status_code";
		try {
			connection = GlobalCache.connection_pool_proc.getConnection();
			pstmt = connection.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String status = rs.getString("status");
				String error_code = rs.getString("error_code");
				StatusCode.put(status, error_code);
			}
		} catch (Exception ex) {
			logger.error("loadStatusCode()", ex.fillInStackTrace());
		} finally {
			GlobalCache.connection_pool_proc.putConnection(connection);
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException ex) {
				}
			}
		}
	}

	private String timeConversion(String gmt, String time) {
		String converted_time = null;
		DateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");
		Date custom_date_time = null;
		try {
			custom_date_time = formatter.parse(time);
			formatter.setTimeZone(TimeZone.getTimeZone("GMT" + gmt));
		} catch (ParseException e) {
			custom_date_time = new Date();
			logger.error(gmt + " Time Conversion error: " + time);
		}
		converted_time = formatter.format(custom_date_time);
		// System.out.println("using calendar " + converted_time);
		return converted_time;
	}

	public void stop() {
		logger.info("DeliverProcess Thread Stopping");
		stop = true;
	}
}
