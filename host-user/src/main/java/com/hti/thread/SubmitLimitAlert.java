package com.hti.thread;

import java.util.StringTokenizer;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hti.user.dto.SubmitLimitEntry;
import com.hti.user.dto.UserEntry;
import com.hti.user.smpp.UserTCPIPConnection;
import com.hti.util.EmailSender;
import com.hti.util.GlobalVars;
import com.logica.smpp.Data;
import com.logica.smpp.Session;
import com.logica.smpp.pdu.BindRequest;
import com.logica.smpp.pdu.BindTransmitter;
import com.logica.smpp.pdu.Response;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.UnbindResp;

public class SubmitLimitAlert implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private String test_user;
	private String test_password;
	private SubmitLimitEntry limit;
	private String systemId;
	private UserEntry userEntry;

	public SubmitLimitAlert(UserEntry userEntry, SubmitLimitEntry limit) {
		this.limit = limit;
		this.userEntry = userEntry;
		this.systemId = userEntry.getSystemId();
		getInternalUser();
	}

	@Override
	public void run() {
		logger.info(systemId + " Submit Limit Alert Thread Started");
		try {
			if (limit.getAlertEmail() != null && limit.getAlertEmail().contains(".")
					&& limit.getAlertEmail().contains("@")) {
				String recipients[];
				StringTokenizer tokens = new StringTokenizer(limit.getAlertEmail(), ",");
				recipients = new String[tokens.countTokens()];
				int i = 0;
				while (tokens.hasMoreTokens()) {
					recipients[i] = tokens.nextToken();
					logger.info("<- User " + systemId + " Submit Limit Alert Email: <" + recipients[i] + "> ");
					i++;
				}
				String from = com.hti.util.Constants.EMAIL_FROM;
				String subject = systemId + " User Submit Limit Alert";
				String mailMessage = "Dear Team,\n\n";
				mailMessage += "This is to Inform that Following User reached to submit limit: \n\n";
				mailMessage += "SystemId    : " + systemId + " [" + limit.getUserId() + "]" + "\n";
				mailMessage += "Duration : " + limit.getDuration() + " Minutes\n";
				mailMessage += "Server : " + com.hti.util.Constants.SERVER_NAME + "\n";
				mailMessage += "Please Check and Do Needful. \n\n\n";
				mailMessage += "Thanks & Regards, \n";
				mailMessage += "Support Team - Broadnet, \n";
				try {
					com.hti.user.dto.ProfessionEntry professionEntry = GlobalVars.userService
							.getProfessionEntry(systemId);
					if (professionEntry != null && professionEntry.getDomainEmail() != null
							&& professionEntry.getDomainEmail().contains("@")
							&& professionEntry.getDomainEmail().contains(".")) {
						from = professionEntry.getDomainEmail();
						logger.info(systemId + " Domain-Email Found: " + from);
					} else {
						logger.info(systemId + " Checking master[" + userEntry.getMasterId() + "] doamin email.");
						com.hti.user.dto.ProfessionEntry masterProfessionEntry = GlobalVars.userService
								.getProfessionEntry(userEntry.getMasterId());
						if (masterProfessionEntry != null && masterProfessionEntry.getDomainEmail() != null
								&& masterProfessionEntry.getDomainEmail().contains("@")
								&& masterProfessionEntry.getDomainEmail().contains(".")) {
							from = masterProfessionEntry.getDomainEmail();
							logger.info(
									systemId + " master[" + userEntry.getMasterId() + "] Domain-Email Found: " + from);
						} else {
							logger.info(systemId + " master[" + userEntry.getMasterId() + "] Domain-Email Not Found.");
						}
					}
					new EmailSender().sendSSLMessage(recipients, subject, mailMessage, from);
				} catch (MessagingException me) {
					logger.error(
							me + " While Sending " + systemId + " Submit Limit Alert @ " + userEntry.getAlertEmail());
				}
			} else {
				logger.error("Invalid Submit limit Alert Email Found For " + systemId + ": " + limit.getAlertEmail());
			}
			if (test_user != null && test_user.length() > 0) {
				if (limit.getAlertNumber() != null && limit.getAlertNumber().length() > 0) {
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
							String message = "Hello, Informing that User " + systemId + " On "
									+ com.hti.util.Constants.SERVER_NAME + " reached to submit limit "
									+ limit.getCount() + ".";
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
										logger.info(
												systemId + " Submit Limit Alert Message submitted -> " + destination);
									} else {
										logger.error(systemId + " Submit Limit Alert Message submission failed. Status="
												+ resp.getCommandStatus() + " -> " + destination);
									}
								} catch (NumberFormatException ne) {
									logger.error(
											"Invalid Alert Number Found For User " + systemId + ": " + destination);
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
						logger.error(systemId, ex.fillInStackTrace());
					}
				} else {
					logger.error("Invalid Alert Number Found For User " + systemId + ": " + limit.getAlertNumber());
				}
			} else {
				logger.error("<-- No internal User Found to Send Alert Message -->");
			}
		} catch (Exception ex) {
			logger.info(systemId, ex);
		}
		logger.info(systemId + " Submit Limit Alert Thread Stopped");
	}

	private void getInternalUser() {
		UserEntry userEntry = GlobalVars.userService.getInternalUser();
		if (userEntry != null) {
			test_user = userEntry.getSystemId();
			test_password = userEntry.getPassword();
		}
	}
}
