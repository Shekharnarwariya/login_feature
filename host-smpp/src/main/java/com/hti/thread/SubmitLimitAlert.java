package com.hti.thread;

import java.util.StringTokenizer;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.SmscLimit;
import com.hti.user.dto.UserEntry;
import com.hti.util.GlobalVars;
import com.hti.util.GoogleTest;
import com.logica.smpp.Data;
import com.logica.smpp.Session;
import com.logica.smpp.TCPIPConnection;
import com.logica.smpp.pdu.BindRequest;
import com.logica.smpp.pdu.BindTransmitter;
import com.logica.smpp.pdu.Response;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.UnbindResp;

public class SubmitLimitAlert implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private String test_user;
	private String test_password;
	private SmscLimit limit;

	public SubmitLimitAlert(SmscLimit limit) {
		this.limit = limit;
		getInternalUser();
	}

	@Override
	public void run() {
		logger.info(limit.getSmsc() + "[" + limit.getNetworkId() + "] Submit Limit Alert Thread Started");
		try {
			if (limit.getAlertEmail() != null && limit.getAlertEmail().contains(".")
					&& limit.getAlertEmail().contains("@")) {
				String recipients[];
				StringTokenizer tokens = new StringTokenizer(limit.getAlertEmail(), ",");
				recipients = new String[tokens.countTokens()];
				int i = 0;
				while (tokens.hasMoreTokens()) {
					recipients[i] = tokens.nextToken();
					logger.info("<- Smsc " + limit.getSmsc() + " Submit Limit Alert Email: <" + recipients[i] + "> ");
					i++;
				}
				String subject = limit.getSmsc() + " On " + com.hti.util.Constants.SERVER_NAME
						+ " Submit Limit Reached.";
				String mailMessage = "Dear Team,\n\n";
				mailMessage += "This is to Inform that Route [" + limit.getSmsc() + "] On "
						+ com.hti.util.Constants.SERVER_NAME + " for network[" + limit.getNetworkId()
						+ "] Reached to Submit limit[" + limit.getLimit() + "] \n\n";
				mailMessage += "Thanks & Regards, \n";
				mailMessage += "Support Team - Broadnet, \n";
				try {
					new GoogleTest().sendSSLMessage(recipients, subject, mailMessage);
				} catch (MessagingException me) {
					logger.error(me + " While Sending " + limit.getSmsc() + " Alert @ " + limit.getAlertEmail());
				}
			} else {
				logger.error(
						"Invalid Submit limit Alert Email Found For " + limit.getSmsc() + ": " + limit.getAlertEmail());
			}
			if (test_user != null && test_user.length() > 0) {
				if (limit.getAlertNumber() != null && limit.getAlertNumber().length() > 0) {
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
							String message = "Hello, Informing that Route " + limit.getSmsc() + "-"
									+ limit.getNetworkId() + " On " + com.hti.util.Constants.SERVER_NAME
									+ " reached to submit limit " + limit.getLimit() + ".";
							String senderId = "LIMIT-ALERT";
							if (limit.getAlertSender() != null) {
								senderId = limit.getAlertSender();
							}
							String destination = "";
							SubmitSM msg = null;
							StringTokenizer tokens = new StringTokenizer(limit.getAlertNumber(), ",");
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
										logger.info(limit.getSmsc() + " Submit Limit Alert Message submitted -> "
												+ destination);
									} else {
										logger.error(limit.getSmsc()
												+ " Submit Limit Alert Message submission failed. Status="
												+ resp.getCommandStatus() + " -> " + destination);
									}
								} catch (NumberFormatException ne) {
									logger.error("Invalid Alert Number Found For Smsc " + limit.getSmsc() + ": "
											+ destination);
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
						logger.error(limit.getSmsc(), ex.fillInStackTrace());
					}
				} else {
					logger.error(
							"Invalid Alert Number Found For Smsc " + limit.getSmsc() + ": " + limit.getAlertNumber());
				}
			} else {
				logger.error("<-- No internal User Found to Send Alert Message -->");
			}
		} catch (Exception ex) {
			logger.info(limit.getSmsc(), ex);
		}
		logger.info(limit.getSmsc() + "[" + limit.getNetworkId() + "] Submit Limit Alert Thread Stopped");
	}

	private void getInternalUser() {
		UserEntry userEntry = GlobalVars.userService.getInternalUser();
		if (userEntry != null) {
			test_user = userEntry.getSystemId();
			test_password = userEntry.getPassword();
		}
	}
}
