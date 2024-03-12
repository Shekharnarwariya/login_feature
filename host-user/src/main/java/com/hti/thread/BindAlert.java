/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.util.EmailSender;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;
import com.logica.smpp.Data;
import com.logica.smpp.Session;
import com.logica.smpp.pdu.BindRequest;
import com.logica.smpp.pdu.BindTransmitter;
import com.logica.smpp.pdu.Response;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.UnbindResp;
import com.hti.user.dto.UserEntry;
import com.hti.user.smpp.UserTCPIPConnection;

/**
 *
 * @author Administrator
 */
public class BindAlert implements Runnable {
	// public static long WAIT_DURATION = 60;
	// public String DLR_SMSC = "";
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private Map<String, Long> notified = new HashMap<String, Long>();
	private String test_user;
	private String test_password;
	private boolean stop;

	public BindAlert() {
		logger.info("BindAlert Starting");
		getInternalUser();
		// logger.debug("Smsc Unbind Alert Wait Duration: " + WAIT_DURATION);
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1 * 60 * 1000);
		} catch (InterruptedException ex) {
		}
		GlobalCache.UserDisconnectionAlert.clear();
		logger.info("BindAlert Started");
		while (!stop) {
			try {
				// ---------------- Checking Smsc Status to alert ----------------------
				if (!GlobalCache.UserDisconnectionAlert.isEmpty()) {
					logger.info("DisConnected Users: " + GlobalCache.UserDisconnectionAlert.keySet());
					Iterator<Map.Entry<String, Long>> itr = GlobalCache.UserDisconnectionAlert.entrySet().iterator();
					Map.Entry<String, Long> entry = null;
					while (itr.hasNext()) {
						entry = itr.next();
						String system_id = entry.getKey();
						if (!notified.containsKey(system_id)) {
							if (System.currentTimeMillis() > entry.getValue()) {
								if (sendAlert(system_id, false, entry.getValue())) {
									notified.put(system_id, entry.getValue());
								}
							}
						} else {
							logger.info("<- Already sent Alert for Disconnected User " + system_id + " -> ");
						}
					}
				} else {
					logger.info(" No User For Disconnection Alert ");
				}
				// --------------- Removing Connected Smsc if found in Notified Set ----
				if (!notified.isEmpty()) {
					Iterator<String> itr = notified.keySet().iterator();
					while (itr.hasNext()) {
						String system_id = itr.next();
						if (!GlobalCache.UserDisconnectionAlert.containsKey(system_id)) {
							logger.info("<-- " + system_id + " is Connected Now --> ");
							long duration = (System.currentTimeMillis() - (notified.get(system_id))) / 1000;
							sendAlert(system_id, true, duration);
							itr.remove();
						}
					}
				}
			} catch (Exception e) {
				logger.error("bind alert ", e.fillInStackTrace());
			}
			try {
				Thread.sleep(5 * 1000);
			} catch (InterruptedException ex) {
			}
		}
		logger.info("BindAlert Stopped");
	}

	private boolean sendAlert(String system_id, boolean bound, long duration) {
		String status = "Disconnected";
		if (bound) {
			status = "Connected";
		}
		logger.info("<- Sending Alert for " + status + " User " + system_id + " -> ");
		boolean sent = false;
		UserEntry userEntry = GlobalVars.userService.getUserEntry(system_id);
		if (userEntry != null) {
			if (!bound) {
				duration = userEntry.getAlertWaitDuration();
			}
			if (userEntry.getAlertEmail() != null && userEntry.getAlertEmail().contains(".")
					&& userEntry.getAlertEmail().contains("@")) {
				String recipients[];
				StringTokenizer tokens = new StringTokenizer(userEntry.getAlertEmail(), ",");
				recipients = new String[tokens.countTokens()];
				int i = 0;
				while (tokens.hasMoreTokens()) {
					recipients[i] = tokens.nextToken();
					logger.info("<- User " + system_id + " Alert Email: <" + recipients[i] + "> ");
					i++;
				}
				String from = com.hti.util.Constants.EMAIL_FROM;
				String subject = system_id + " User " + status + " Alert";
				String mailMessage = "Dear Team,\n\n";
				mailMessage += "This is to Inform that Following User is " + status + ": \n\n";
				mailMessage += "SystemId    : " + system_id + " [" + userEntry.getId() + "]" + "\n";
				mailMessage += "Duration : " + duration + "Seconds\n";
				mailMessage += "Server : " + com.hti.util.Constants.SERVER_NAME + "\n";
				if (bound) {
					mailMessage += "\n";
				} else {
					mailMessage += "Please Check and Do Needful. \n\n\n";
				}
				mailMessage += "Thanks & Regards, \n";
				mailMessage += "Support Team - Broadnet, \n";
				try {
					com.hti.user.dto.ProfessionEntry professionEntry = GlobalVars.userService
							.getProfessionEntry(system_id);
					if (professionEntry != null && professionEntry.getDomainEmail() != null
							&& professionEntry.getDomainEmail().contains("@")
							&& professionEntry.getDomainEmail().contains(".")) {
						from = professionEntry.getDomainEmail();
						logger.info(system_id + " Domain-Email Found: " + from);
					} else {
						logger.info(system_id + " Checking master[" + userEntry.getMasterId() + "] doamin email.");
						com.hti.user.dto.ProfessionEntry masterProfessionEntry = GlobalVars.userService
								.getProfessionEntry(userEntry.getMasterId());
						if (masterProfessionEntry != null && masterProfessionEntry.getDomainEmail() != null
								&& masterProfessionEntry.getDomainEmail().contains("@")
								&& masterProfessionEntry.getDomainEmail().contains(".")) {
							from = masterProfessionEntry.getDomainEmail();
							logger.info(
									system_id + " master[" + userEntry.getMasterId() + "] Domain-Email Found: " + from);
						} else {
							logger.info(system_id + " master[" + userEntry.getMasterId() + "] Domain-Email Not Found.");
						}
					}
					new EmailSender().sendSSLMessage(recipients, subject, mailMessage, from);
					sent = true;
				} catch (MessagingException me) {
					logger.error(me + " While Sending " + system_id + " Bind Alert @ " + userEntry.getAlertEmail());
				}
			} else {
				logger.error("Invalid Alert Email Found For " + system_id + ": " + userEntry.getAlertEmail());
			}
			if (userEntry.getAlertUrl() != null && userEntry.getAlertUrl().length() > 0) {
				BufferedReader in = null;
				try {
					String alert_url = userEntry.getAlertUrl().substring(0, userEntry.getAlertUrl().indexOf("?") + 1);
					String parmas = userEntry.getAlertUrl().substring(userEntry.getAlertUrl().indexOf("?") + 1);
					for (String token : parmas.split("&")) {
						// String token = tokens.nextToken();
						if (token.contains("=")) {
							String param = token.substring(0, token.indexOf("="));
							String value = java.net.URLEncoder.encode(token.substring(token.indexOf("=") + 1));
							alert_url += param + "=" + value + "&";
						}
					}
					String message = "hi dear this to announce that User " + system_id + " On "
							+ com.hti.util.Constants.SERVER_NAME + " is ";
					if (status.equalsIgnoreCase("Disconnected")) {
						message += "Disconnected " + duration + " Seconds ago.";
					} else {
						message += "Connected Now. Duration: " + duration + " Seconds.";
					}
					alert_url = alert_url + "text=" + java.net.URLEncoder.encode(message);
					logger.info(system_id + ": Sending Url Bind Alert[" + alert_url + "]");
					URL url = new URL(alert_url);
					URLConnection con = url.openConnection();
					in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine = null;
					// System.out.println("<-- Reading Response --> ");
					while ((inputLine = in.readLine()) != null) {
						logger.info("Response: " + inputLine);
					}
					sent = true;
				} catch (Exception ex) {
					logger.error(system_id, ex);
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException ex) {
						}
					}
				}
			} else {
				logger.error("Invalid Bind Alert Url Found For " + system_id + ": " + userEntry.getAlertUrl());
			}
			if (test_user != null && test_user.length() > 0) {
				if (userEntry.getAlertNumber() != null && userEntry.getAlertNumber().length() > 0) {
					com.logica.smpp.Connection conn = new UserTCPIPConnection(com.hti.util.Constants.LOCAL_IP,
							com.hti.util.Constants.LOCAL_PORT, true);
					Session session = new Session(conn, "local");
					BindRequest breq = new BindTransmitter();
					try {
						breq.setSystemId(test_user);
						breq.setPassword(test_password);
						logger.info("Sending Bind Request: " + test_user + " " + test_password);
						Response resp = session.bind(breq);
						logger.info(resp.debugString());
						if (resp.getCommandStatus() == Data.ESME_ROK) {
							String message = "Hello, This is to inform that " + system_id + " On "
									+ com.hti.util.Constants.SERVER_NAME + " is ";
							String senderId = "UNBIND-ALERT";
							if (status.equalsIgnoreCase("Disconnected")) {
								message += "Disconnected " + duration + " Seconds ago. Please check & do Needful.";
							} else {
								message += "Connected Now. Duration: " + duration + " Seconds.";
								senderId = "BIND-ALERT";
							}
							String destination = "";
							SubmitSM msg = null;
							StringTokenizer tokens = new StringTokenizer(userEntry.getAlertNumber(), ",");
							while (tokens.hasMoreTokens()) {
								destination = tokens.nextToken();
								try {
									Long.parseLong(destination);
									msg = new SubmitSM();
									msg.setSourceAddr((byte) 5, (byte) 0, senderId);
									msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination);
									msg.setRegisteredDelivery((byte) 1);
									msg.setDataCoding((byte) 0);
									msg.setEsmClass((byte) 0);
									msg.setShortMessage(message);
									resp = session.submit(msg);
									if (resp.getCommandStatus() == Data.ESME_ROK) {
										logger.info(system_id + " Alert Message submitted -> " + destination);
										sent = true;
									} else {
										logger.error(system_id + " Alert Message submission failed. Status="
												+ resp.getCommandStatus() + " -> " + destination);
									}
								} catch (NumberFormatException ne) {
									logger.error(
											"Invalid Alert Number Found For User " + system_id + ": " + destination);
								}
							}
							UnbindResp unbind = session.unbind();
							if (unbind != null) {
								logger.info(test_user + " -> " + unbind.debugString());
							}
						} else {
							logger.error(test_user + " SMPP Connection Error: " + resp.getCommandStatus());
						}
					} catch (Exception ex) {
						logger.error("sendAlert(" + system_id + "," + bound + ")", ex.fillInStackTrace());
					}
				} else {
					logger.error(
							"Invalid Alert Number Found For User " + system_id + ": " + userEntry.getAlertNumber());
				}
			} else {
				logger.error("<-- No internal User Found to Send Bind Alert Message -->");
			}
		}
		return sent;
	}

	private void getInternalUser() {
		UserEntry userEntry = GlobalVars.userService.getInternalUser();
		if (userEntry != null) {
			test_user = userEntry.getSystemId();
			test_password = userEntry.getPassword();
		}
	}

	public void stop() {
		logger.info("BindAlert Stopping");
		stop = true;
	}
}
