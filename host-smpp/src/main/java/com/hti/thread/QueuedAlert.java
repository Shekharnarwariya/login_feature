package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.AlertDTO;
import com.hti.user.dto.UserEntry;
import com.hti.util.Constants;
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

public class QueuedAlert implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private String test_user;
	private String test_password;
	private boolean stop;
	private Thread thread;
	private boolean sleep;

	public QueuedAlert() {
		logger.info("QueuedAlert Thread Starting");
		this.thread = Thread.currentThread();
		getInternalUser();
	}

	@Override
	public void run() {
		while (!stop) {
			logger.info(
					"QueuedAlert Waiting for Duration " + com.hti.util.Constants.QUEUED_ALERT_DURATION + " Minutes ");
			sleep = true;
			try {
				Thread.sleep(Constants.QUEUED_ALERT_DURATION * 60 * 1000);
			} catch (InterruptedException ex) {
			}
			sleep = false;
			Map<String, Integer> smscCounter = new HashMap<String, Integer>();
			if (!stop) {
				logger.info("******* Checking Queued Entries ************");
				String sql = "select count(msg_id) as count,smsc from smsc_in where s_flag='Q' group by smsc";
				PreparedStatement statement = null;
				ResultSet rs = null;
				Connection connection = null;
				int counter = 0;
				try {
					connection = GlobalCache.connnection_pool_1.getConnection();
					statement = connection.prepareStatement(sql);
					rs = statement.executeQuery();
					while (rs.next()) {
						if (rs.getInt("count") > 0) {
							counter += rs.getInt("count");
							smscCounter.put(rs.getString("smsc"), rs.getInt("count"));
						}
					}
				} catch (Exception ex) {
					logger.error("", ex);
				} finally {
					if (rs != null) {
						try {
							rs.close();
						} catch (Exception ex) {
						}
					}
					if (statement != null) {
						try {
							statement.close();
						} catch (Exception ex) {
						}
					}
					GlobalCache.connnection_pool_1.putConnection(connection);
				}
				if (counter > Constants.QUEUED_ALERT_COUNT) {
					sendAlert(counter, smscCounter);
				}
			}
		}
		logger.info("QueuedAlert Stopped");
	}

	private void sendAlert(int totalDown, Map<String, Integer> smscCounter) {
		if (Constants.QUEUED_ALERT_EMAILS != null) {
			String recipients[];
			//int i = 0;
			List<String> recipientlist = new ArrayList<String>();
			for (String recipient : Constants.QUEUED_ALERT_EMAILS.split(",")) {
				if (recipient.contains("@") && recipient.contains(".")) {
					recipientlist.add(recipient);
					//i++;
				} else {
					logger.error("<- QueuedAlert Invalid Email: <" + recipient + "> ");
				}
			}
			recipients = recipientlist.toArray(new String[0]);
			String subject = "QueuedAlert On " + com.hti.util.Constants.SERVER_NAME;
			String mailMessage = "<body><span >Dear Team,</span><br><br>";
			mailMessage += "Please Find the Unexpected Queued Result as below: <br><br>";
			mailMessage += "<table cellspacing='1' cellpadding='2' border='0' align='left'><tbody>";
			mailMessage += "<tr><td>Smsc</td><td>DownCount</td></tr>";
			for (String smsc : smscCounter.keySet()) {
				mailMessage += "<tr><td>" + smsc + "</td><td>" + smscCounter.get(smsc) + "</td></tr>";
			}
			mailMessage += "<tr><td>Total</td><td>" + totalDown + "</td></tr>";
			mailMessage += "<tr><td colspan='2'>&nbsp;</td></tr>";
			mailMessage += "<tr><td colspan='2'>&nbsp;</td></tr>";
			mailMessage += "<tr><td colspan='2'>Please Check and Do Needful.</td></tr>";
			mailMessage += "<tr><td colspan='2'>&nbsp;</td></tr>";
			mailMessage += "<tr><td colspan='2'>&nbsp;</td></tr>";
			mailMessage += "<tr><td colspan='2'>Thanks & Regards,</td></tr>";
			mailMessage += "<tr><td colspan='2'>Support Team - Broadnet</td></tr>";
			mailMessage += "</tbody></table><br><br><br><br>";
			mailMessage += "</body>";
			try {
				new GoogleTest().sendSSLMessage(recipients, subject, mailMessage);
			} catch (MessagingException me) {
				logger.error(me + " While Sending Performance Alert @ " + Constants.QUEUED_ALERT_EMAILS);
			}
		} else {
			logger.info("Invalid Email Found For QueuedAlert");
		}
		if (test_user != null && test_password != null) {
			if (Constants.QUEUED_ALERT_NUMBERS != null && Constants.QUEUED_ALERT_NUMBERS.length() > 0) {
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
						String message = "Hello, Total Queued " + totalDown + " found on "
								+ com.hti.util.Constants.SERVER_NAME;
						String destination = "";
						SubmitSM msg = null;
						StringTokenizer tokens = new StringTokenizer(Constants.QUEUED_ALERT_NUMBERS, ",");
						while (tokens.hasMoreTokens()) {
							destination = tokens.nextToken();
							try {
								Long.parseLong(destination);
								msg = new SubmitSM();
								msg.setSourceAddr((byte) 5, (byte) 0, com.hti.util.Constants.SERVER_NAME + "-QAlert");
								msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination);
								msg.setRegisteredDelivery((byte) 1);
								msg.setDataCoding((byte) 0);
								msg.setEsmClass((byte) 0);
								msg.setShortMessage(message);
								resp = session.submit(msg);
								if (resp.getCommandStatus() == Data.ESME_ROK) {
									logger.info(" QAlert Message submitted -> " + destination);
								} else {
									logger.error(" QAlert Message submission failed. Status=" + resp.getCommandStatus()
											+ " -> " + destination);
								}
							} catch (NumberFormatException ne) {
								logger.error("Invalid Alert Number Found For QAlert: " + destination);
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
					logger.error("", ex);
				}
			} else {
				logger.info("Invalid Number Found For QueuedAlert");
			}
		} else {
			logger.info("No Internal User Found For QueuedAlert");
		}
	}

	private void sendSMSAlert(AlertDTO result) {
	}

	private void getInternalUser() {
		UserEntry userEntry = GlobalVars.userService.getInternalUser();
		if (userEntry != null) {
			test_user = userEntry.getSystemId();
			test_password = userEntry.getPassword();
		}
	}

	public void stop() {
		logger.info("Queued Alert Thread Stopping");
		stop = true;
		if (sleep) {
			logger.info("Queued Alert Thread Interrupting");
			thread.interrupt();
		}
	}
}
