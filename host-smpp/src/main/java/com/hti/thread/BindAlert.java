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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.smsc.dto.SmscEntry;
import com.hti.user.dto.UserEntry;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;
import com.hti.util.GoogleTest;
import com.hti.util.TextEncoder;
import com.logica.smpp.Data;
import com.logica.smpp.Session;
import com.logica.smpp.TCPIPConnection;
import com.logica.smpp.pdu.BindRequest;
import com.logica.smpp.pdu.BindTransmitter;
import com.logica.smpp.pdu.Response;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.UnbindResp;

/**
 *
 * @author Administrator
 */
public class BindAlert implements Runnable {
	public static long WAIT_DURATION = 60;
	public String DLR_SMSC = "";
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private Map<String, Long> notified = new HashMap<String, Long>();
	private String test_user;
	private String test_password;
	private boolean stop;

	public BindAlert() {
		logger.info("BindAlert Starting");
		getInternalUser();
		logger.debug("Smsc Unbind Alert Wait Duration: " + WAIT_DURATION);
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				Thread.sleep(5 * 1000);
			} catch (InterruptedException ex) {
			}
			try {
				// ---------------- Checking Smsc Status to alert ----------------------
				if (!GlobalCache.SmscDisconnection.isEmpty()) {
					logger.info("DisConnected Routes: " + GlobalCache.SmscDisconnection.keySet());
					Iterator<Map.Entry<String, Long>> itr = GlobalCache.SmscDisconnection.entrySet().iterator();
					Map.Entry<String, Long> entry = null;
					while (itr.hasNext()) {
						entry = itr.next();
						String smscName = entry.getKey();
						if (!notified.containsKey(smscName)) {
							if (System.currentTimeMillis() > entry.getValue() + (WAIT_DURATION * 1000)) {
								if (sendAlert(smscName, false, WAIT_DURATION)) {
									notified.put(smscName, entry.getValue());
								}
							}
						} else {
							logger.info("<- Already sent Alert for Down Smsc " + smscName + " -> ");
						}
					}
				} else {
					logger.info(" No Route For DisConnection Alert ");
				}
				// --------------- Removing Connected Smsc if found in Notified Set ----
				if (!notified.isEmpty()) {
					Iterator<String> itr = notified.keySet().iterator();
					while (itr.hasNext()) {
						String smsc_key = itr.next();
						if (!GlobalCache.SmscDisconnection.containsKey(smsc_key)) {
							logger.info("<-- " + smsc_key + " is Connected Now --> ");
							long duration = (System.currentTimeMillis() - (notified.get(smsc_key))) / 1000;
							sendAlert(smsc_key, true, duration);
							itr.remove();
						}
					}
				}
			} catch (Exception e) {
				logger.error("bind alert ", e.fillInStackTrace());
			}
		}
		logger.info("BindAlert Stopped");
	}

	private boolean sendAlert(String smsc_id, boolean bound, long duration) {
		String status = "Disconnected";
		if (bound) {
			status = "Connected";
		}
		logger.info("<- Sending Alert for " + status + " Smsc " + smsc_id + " -> ");
		boolean sent = false;
		SmscEntry smscDTO = GlobalVars.smscService.getEntry(smsc_id);
		if (smscDTO != null) {
			if (smscDTO.getDownEmail() != null && smscDTO.getDownEmail().contains(".")
					&& smscDTO.getDownEmail().contains("@")) {
				String recipients[];
				StringTokenizer tokens = new StringTokenizer(smscDTO.getDownEmail(), ",");
				recipients = new String[tokens.countTokens()];
				int i = 0;
				while (tokens.hasMoreTokens()) {
					recipients[i] = tokens.nextToken();
					logger.info("<- Smsc " + smsc_id + " Alert Email: <" + recipients[i] + "> ");
					i++;
				}
				String subject = smscDTO.getName() + " Route On " + com.hti.util.Constants.SERVER_NAME + " " + status
						+ " Alert";
				String mailMessage = "Dear Team,\n\n";
				mailMessage += "This is to Inform that Following Route On " + com.hti.util.Constants.SERVER_NAME
						+ " is " + status + ": \n\n";
				mailMessage += "Route    : " + smscDTO.getName() + " [" + smscDTO.getId() + "]" + "\n";
				mailMessage += "Duration : " + duration + "Seconds\n";
				mailMessage += "IP       : " + smscDTO.getIp() + " \n";
				mailMessage += "Port     : " + smscDTO.getPort() + " \n";
				mailMessage += "Systemid : " + smscDTO.getSystemId() + "\n";
				mailMessage += "Bindmode : " + smscDTO.getBindMode() + "\n\n";
				if (bound) {
					mailMessage += "\n";
				} else {
					mailMessage += "Please Check and Do Needful. \n\n\n";
				}
				mailMessage += "Thanks & Regards, \n";
				mailMessage += "Support Team - Broadnet, \n";
				try {
					new GoogleTest().sendSSLMessage(recipients, subject, mailMessage);
					sent = true;
				} catch (MessagingException me) {
					logger.error(me + " While Sending " + smsc_id + " Down Alert @ " + smscDTO.getDownEmail());
				}
			} else {
				logger.error("Invalid Alert Email Found For " + smsc_id + ": " + smscDTO.getDownEmail());
			}
			if (smscDTO.getAlertUrl() != null && smscDTO.getAlertUrl().length() > 0) {
				// logger.info(smsc_id + ": Sending Url Bind Alert[" + smscDTO.getAlertUrl() + "]");
				BufferedReader in = null;
				try {
					String alert_url = smscDTO.getAlertUrl().substring(0, smscDTO.getAlertUrl().indexOf("?") + 1);
					String parmas = smscDTO.getAlertUrl().substring(smscDTO.getAlertUrl().indexOf("?") + 1);
					for (String token : parmas.split("&")) {
						// String token = tokens.nextToken();
						if (token.contains("=")) {
							String param = token.substring(0, token.indexOf("="));
							String value = java.net.URLEncoder.encode(token.substring(token.indexOf("=") + 1));
							alert_url += param + "=" + value + "&";
						}
					}
					String message = "hi dear this to announce that " + smscDTO.getName() + " On "
							+ com.hti.util.Constants.SERVER_NAME + " is ";
					if (status.equalsIgnoreCase("Disconnected")) {
						message += "Down " + duration + " Seconds ago.";
					} else {
						message += "Connected Now. Duration: " + duration + " Seconds.";
					}
					alert_url += "text=" + java.net.URLEncoder.encode(message);
					logger.info(smsc_id + ": Sending Url Bind Alert[" + alert_url + "]");
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
					logger.error(smsc_id, ex);
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException ex) {
						}
					}
				}
			} else {
				logger.error("Invalid Bind Alert Url Found For " + smsc_id + ": " + smscDTO.getAlertUrl());
			}
			if (test_user != null && test_user.length() > 0) {
				if (smscDTO.getDownNumber() != null && smscDTO.getDownNumber().length() > 0) {
					com.logica.smpp.Connection conn = new TCPIPConnection(StartSmppServer.USER_SERVER_IP,
							StartSmppServer.USER_SERVER_PORT);
					Session session = new Session(conn, "local");
					BindRequest breq = new BindTransmitter();
					try {
						breq.setSystemId(test_user);
						breq.setPassword(test_password);
						logger.info("Sending Bind Request: " + test_user + " " + test_password);
						Response resp = session.bind(breq);
						logger.info(resp.debugString());
						if (resp.getCommandStatus() == Data.ESME_ROK) {
							String message = "Hello, This is to inform that Route " + smscDTO.getName() + " On "
									+ com.hti.util.Constants.SERVER_NAME + " is ";
							String senderId = "DOWN-ALERT";
							if (status.equalsIgnoreCase("Disconnected")) {
								message += "Down " + duration + " Seconds ago. Please check & do Needful.";
							} else {
								message += "Connected Now. Duration: " + duration + " Seconds.";
								senderId = "UP-ALERT";
							}
							String destination = "";
							SubmitSM msg = null;
							StringTokenizer tokens = new StringTokenizer(smscDTO.getDownNumber(), ",");
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
										logger.info(smsc_id + " Alert Message submitted -> " + destination);
										sent = true;
									} else {
										logger.error(smsc_id + " Alert Message submission failed. Status="
												+ resp.getCommandStatus() + " -> " + destination);
									}
								} catch (NumberFormatException ne) {
									logger.error("Invalid Alert Number Found For Smsc " + smsc_id + ": " + destination);
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
						logger.error("sendAlert(" + smsc_id + "," + bound + ")", ex.fillInStackTrace());
					}
				} else {
					logger.error("Invalid Alert Number Found For Smsc " + smsc_id + ": " + smscDTO.getDownNumber());
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
