package com.hti.smpp.common.request;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.smpp.common.dto.UserEntryExt;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class MailUtility {
	private static Logger logger = LoggerFactory.getLogger(MailUtility.class);

	public String mailOnLoginContent(String systemId, String ipAddress) {
		String text = "";
		try {
			text = MultiUtility.readContent(IConstants.WEBAPP_DIR + "format//email//login_email.txt");
			do {
				text = text.replaceFirst("#", systemId);
				text = text.replaceFirst("#", IConstants.GATEWAY_NAME);
				text = text.replaceFirst("#", ipAddress);
				text = text.replaceFirst("#", new Date() + " (" + IConstants.DEFAULT_GMT + ")");
			} while (text.contains("#"));
		} catch (Exception ex) {
			logger.error(systemId, ex.getMessage());
			text = "Hello " + systemId + ",<br>"
					+ "This is an informational email to let you know that we have identified a login to your account at "
					+ IConstants.GATEWAY_NAME + " from the IP address " + ipAddress + " time on " + new Date() + " ("
					+ IConstants.DEFAULT_GMT + ")" + ".<br>"
					+ "If this was not you,please contact support immediately.Otherwise you may safely ignore this email.<br><br><br>"
					+ "Kind Regards,<br>" + "Support team";
		}
		String content = "";
		content += "<body>																																					  "
				+ "<br>																																							  "
				+ "<table height='48' cellspacing='1' cellpadding='2' border='0' align='left' summary=''>																			                                                                          "
				+ "<tbody>																																						  "
				+ "<tr>																																							  "
				+ "<td colspan='3'><span >" + text + "</span>"
				+ "</td>                                                                                                                                       "
				+ "</tr>" + "<tr>"
				+ "<td colspan='3'>&nbsp;                                                                                                                                       "
				+ "</td>																		                                                                                                                                                          "
				+ "</tr>"
				+ "</tbody>																																						  "
				+ "</table></ br>																																					  "
				+ "<br >	" + "<br >	" + "<br >	" + "</body>";
		String filename = IConstants.WEBAPP_DIR + "mail//" + systemId + "_login_"
				+ new SimpleDateFormat("yyyy-MM-dd_hhmmss").format(new Date()) + ".html";
		MultiUtility.writeMailContent(filename, content);
		return filename;
	}

	public String mailOnLoginFailedContent(String systemId, String ipAddress, int attempts) {
		String text = "";
		try {
			text = MultiUtility.readContent(IConstants.WEBAPP_DIR + "format//email//login_failed_email.txt");
			do {
				text = text.replaceFirst("#", systemId);
				text = text.replaceFirst("#", String.valueOf(attempts));
				text = text.replaceFirst("#", IConstants.GATEWAY_NAME);
				text = text.replaceFirst("#", ipAddress);
				text = text.replaceFirst("#", new Date() + " (" + IConstants.DEFAULT_GMT + ")");
			} while (text.contains("#"));
		} catch (Exception ex) {
			logger.error(systemId, ex.getMessage());
			text = "Hello " + systemId + ",<br>"
					+ "This is an informational email to let you know that we have identified " + attempts
					+ " failed login attempt(s) to your account at " + IConstants.GATEWAY_NAME + " from the IP address "
					+ ipAddress + " time on " + new Date() + " (" + IConstants.DEFAULT_GMT + ")" + ".<br>"
					+ "If this was not you,please contact support immediately.<br><br><br>" + "Kind Regards,<br>"
					+ "Support team";
		}
		String content = "";
		content += "<body>																																					  "
				+ "<br>																																							  "
				+ "<table height='48' cellspacing='1' cellpadding='2' border='0' align='left' summary=''>																			                                                                          "
				+ "<tbody>																																						  "
				+ "<tr>																																							  "
				+ "<td colspan='3'><span >" + text + "</span>"
				+ "</td>                                                                                                                                       "
				+ "</tr>" + "<tr>"
				+ "<td colspan='3'>&nbsp;                                                                                                                                       "
				+ "</td>																		                                                                                                                                                          "
				+ "</tr>"
				+ "</tbody>																																						  "
				+ "</table></ br>																																					  "
				+ "<br >	" + "<br >	" + "<br >	" + "</body>";
		String filename = IConstants.WEBAPP_DIR + "mail//" + systemId + "_failed_login_"
				+ new SimpleDateFormat("yyyy-MM-dd_hhmmss").format(new Date()) + ".html";
		MultiUtility.writeMailContent(filename, content);
		return filename;
	}

	public String mailOnFailedAPIAccessContent(String systemId, String ipAddress) {
		String text = "Hello " + systemId + ",<br>"
				+ "This is an informational email to let you know that we have identified http api access to your account at "
				+ IConstants.GATEWAY_NAME + " from the IP address " + ipAddress + " time on " + new Date() + " ("
				+ IConstants.DEFAULT_GMT + ")" + ".<br>"
				+ "If this was not you,please contact support immediately.<br><br><br>" + "Kind Regards,<br>"
				+ "Support team";
		String content = "";
		content += "<body>																																					  "
				+ "<br>																																							  "
				+ "<table height='48' cellspacing='1' cellpadding='2' border='0' align='left' summary=''>																			                                                                          "
				+ "<tbody>																																						  "
				+ "<tr>																																							  "
				+ "<td colspan='3'><span >" + text + "</span>"
				+ "</td>                                                                                                                                       "
				+ "</tr>" + "<tr>"
				+ "<td colspan='3'>&nbsp;                                                                                                                                       "
				+ "</td>																		                                                                                                                                                          "
				+ "</tr>"
				+ "</tbody>																																						  "
				+ "</table></ br>																																					  "
				+ "<br >	" + "<br >	" + "<br >	" + "</body>";
		String filename = IConstants.WEBAPP_DIR + "mail//" + systemId + "_http_api_access_"
				+ new SimpleDateFormat("yyyy-MM-dd_hhmmss").format(new Date()) + ".html";
		MultiUtility.writeMailContent(filename, content);
		return filename;
	}

	public String createMailContent(String systemId, PasswordLinkEntry linkEntry) {
		String content = "";
		content += "<body>" + "<span >Dear " + systemId + ",</span>"
				+ "<br>																																							  "
				+ "<br>																																							  "
				+ "<table height='48' cellspacing='1' cellpadding='2' border='0' align='left' summary=''>																			                                                                          "
				+ "<tbody>																																						  "
				+ "<tr>																																							  "
				+ "<td colspan='3'><span ><a href='" + IConstants.WebUrl + "/action/setPassword?system_id=" + systemId
				+ "&linkid=" + linkEntry.getLinkId() + "&hid=" + linkEntry.getHashId() + "'>Click here</a>"
				+ " (Valid For " + IConstants.PASSWORD_LINK_VALIDITY
				+ " Minutes) to Set password </span></td>                                                                                                                                                                                                                                                           "
				+ "</tr>" + "<tr>"
				+ "<td colspan='3'>&nbsp;                                                                                                                                       "
				+ "</td>																		                                                                                                                                                          "
				+ "</tr>" + "<tr>"
				+ "<td colspan='3'>&nbsp;                                                                                                                                       "
				+ "</td>																		                                                                                                                                                          "
				+ "</tr>" + "<tr >"
				+ "<td colspan='3' style='font-family: Calibri,sans-serif;font-size: 13pt'>Kind Regards,"
				+ "</td>                                                                                                                                       "
				+ "</tr>" + "<tr>"
				+ "<td colspan='3' style='font-family: Calibri,sans-serif;font-size: 13pt'>Support Team"
				+ "</td>                                                                                                                                       "
				+ "</tr>" + "<tr>"
				+ "<td colspan='3'>&nbsp;                                                                                                                                       "
				+ "</td>																		                                                                                                                                                          "
				+ "</tr>"
				+ "</tbody>																																						  "
				+ "</table></ br>																																					  "
				+ "<br >	" + "<br >	" + "<br >	" + "</body>";
		String filename = IConstants.WEBAPP_DIR + "mail//" + systemId + "_setpassword_"
				+ new SimpleDateFormat("yyyy-MM-dd_hhmmss").format(new Date()) + ".html";
		MultiUtility.writeMailContent(filename, content);
		return filename;
	}

	public String createMailContent(UserEntryExt userDTO, PasswordLinkEntry linkEntry) {
		String WebUrl = IConstants.WebUrl;
		if (userDTO.getProfessionEntry().getReferenceId() != null) {
			WebUrl += "/login?ref=" + userDTO.getProfessionEntry().getReferenceId();
		}
		String content = "";
		content += "<body>" + "<span >Dear User,</span>"
				+ "<br>																																							  "
				+ "<br>																																							  "
				+ "<table height='48' cellspacing='1' cellpadding='2' border='0' align='left' summary=''>																			                                                                          "
				+ "<tbody>																																						  "
				+ "<tr>																																							  "
				+ "<td colspan='3'><span >We would like to inform you for Successful Registration with our SMSC.</span></td>																		                                                                                                                                                  "
				+ "</tr>                                                                                                                                                                                                                                                                                                                  "
				+ "<tr>																																							  "
				+ "<td colspan='3'><span >Please find below your Account Details:</span></td>																		                                                                                                                                                  "
				+ "</tr>                                                                                                                                                                                                                                                                                                                  "
				+ "<tr>																																							  "
				+ "<td colspan='3'>&nbsp;</td>																		                                                                                                                                                  "
				+ "</tr>                                                                                                                                                                                                                                                                                                                  "
				+ "<tr>																																							  "
				+ "<td colspan='3'>&nbsp;</td>																		                                                                                                                                                  "
				+ "</tr>                                                                                                                                                                                                                                                                                                                  "
				+ "<tr>																																							  "
				+ "	    <td>System id</td>																		                                                                                                                                                  "
				+ "	    <td>:</td>																		                                                                                                                                                          "
				+ "	    <td><strong>" + userDTO.getUserEntry().getSystemId()
				+ "</strong></td>																		                                                                                                          "
				+ "</tr>                                                                                                                                                                                                                                                                                                                "
				+ "<tr>																																							  "
				+ "	    <td>Master id</td>																		                                                                                                                                                  "
				+ "	    <td>:</td>																		                                                                                                                                                          "
				+ "	    <td><strong>" + userDTO.getUserEntry().getMasterId()
				+ "</strong></td>																		                                                                                                          "
				+ "</tr>                                                                                                                                                                                                                                                                                                                  "
				+ "<tr>																																							  "
				+ "	    <td>Balance</td>																		                                                                                                                                                  "
				+ "	    <td>:</td>																		                                                                                                                                                          ";
		if (userDTO.getBalance().getWalletFlag().equalsIgnoreCase("yes")) {
			content += "<td><strong>" + userDTO.getBalance().getWalletAmount() + " "
					+ userDTO.getUserEntry().getCurrency()
					+ "</strong></td>																		                                                                          ";
		} else {
			content += "<td><strong>" + userDTO.getBalance().getCredits()
					+ " Credits</strong></td>																		                                                                                                          ";
		}
		content += "</tr>                                                                                                                                                                                                                                                                                                                 "
				+ "<tr>"
				+ "<td>APIAccessKey                                                                                                                             "
				+ "</td>                                                                                                                                       "
				+ "<td>:                                                                                                                                       "
				+ "</td>																		                                                                                                                                                          "
				+ "<td><strong>" + userDTO.getWebMasterEntry().getProvCode()
				+ "</strong></td>																		                                                                                                                                          "
				+ "</tr>"
				+ "<tr>																																							  "
				+ "<td colspan='3'>&nbsp;                                              "
				+ "</td>                                                                                                                                                                                                             "
				+ "</tr>																																							  "
				+ "<tr>																																							  "
				+ "<td colspan='3'><span >Web Interface URL : <strong > " + WebUrl
				+ "</strong></span>                                               "
				+ "</td>                                                                                                                                                                                                             "
				+ "</tr>																																							  "
				+ "<tr>																																							  "
				+ "<td colspan='3'><span >Connection Details for SMPP v3.4 is as: </span></td>                                                                                                                                                                                                                                                           "
				+ "</tr>																																							  "
				+ "<tr>																																							  "
				+ "<td>IP</td>" + "<td>:</td> " + "<td><strong >" + IConstants.SMPP_IP + "</strong></td>"
				+ "</tr>									                                                                                                                                                                                          "
				+ "<tr>" + "<td>Port</td>" + "<td>:</td> " + "<td><strong >" + IConstants.SMPP_USER_PORT
				+ "</strong></td>"
				+ "</tr>									                                                                                                                                                                                                  "
				+ "<tr>" + "<td>System_type</td>" + "<td>:</td> "
				+ "<td><strong >Null</strong></td>									                                                                                                                                                                                  "
				+ "</tr>" + "<tr>" + "<td>Enquire Link PDU</td>" + "<td>:</td> "
				+ "<td><strong >30 Seconds</strong></td>"
				+ "</tr>									                                                                                                                                                                                                  "
				+ "<tr>" + "<td>Throughput</td>" + "<td>:</td> " + "<td><strong >50 sms/s</strong></td>" + "</tr>"
				+ "<tr>																																							  "
				+ "<td colspan='3'><span ><a href='" + IConstants.WebUrl + "/action/setPassword?system_id="
				+ userDTO.getUserEntry().getSystemId() + "&linkid=" + linkEntry.getLinkId() + "&hid="
				+ linkEntry.getHashId() + "'>Click here</a>" + " (Valid For " + IConstants.PASSWORD_LINK_VALIDITY
				+ " Minutes) to Set password </span></td>                                                                                                                                                                                                                                                           "
				+ "</tr>" + "<tr>"
				+ "<td colspan='3'>&nbsp;                                                                                                                                       "
				+ "</td>																		                                                                                                                                                          "
				+ "</tr>" + "<tr>"
				+ "<td colspan='3'>&nbsp;                                                                                                                                       "
				+ "</td>																		                                                                                                                                                          "
				+ "</tr>" + "<tr >"
				+ "<td colspan='3' style='font-family: Calibri,sans-serif;font-size: 13pt'>Kind Regards,"
				+ "</td>                                                                                                                                       "
				+ "</tr>" + "<tr>"
				+ "<td colspan='3' style='font-family: Calibri,sans-serif;font-size: 13pt'>Support Team"
				+ "</td>                                                                                                                                       "
				+ "</tr>" + "<tr>"
				+ "<td colspan='3'>&nbsp;                                                                                                                                       "
				+ "</td>																		                                                                                                                                                          "
				+ "</tr>"
				+ "</tbody>																																						  "
				+ "</table></ br>																																					  "
				+ "<br >	" + "<br >	" + "<br >	" + "</body>";
		String filename = IConstants.WEBAPP_DIR + "mail//" + userDTO.getUserEntry().getSystemId() + "_Creation_"
				+ new SimpleDateFormat("yyyy-MM-dd_hhmmss").format(new Date()) + ".html";
		MultiUtility.writeMailContent(filename, content);
		return filename;
	}

	public static void send(String email, String fileName, String subject, String from, boolean IsCC)
			throws AddressException, MessagingException, Exception {
		String host = IConstants.mailHost;
		String to = email;
		final String pass = IConstants.mailPassword;
		final String mailAuthUser = IConstants.mailId;
		// String messagetext = "";
		// boolean sessionDebug = false;
		Properties props = new Properties();
		// props.put("mail.smtp.user", mailAuthUser);
		props.put("mail.smtp.host", host);
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", IConstants.smtpPort + "");
		// props.put("mail.smtp.auth", "true");
		// props.put("mail.smtp.debug", "true");
		// props.put("mail.smtp.starttls.enable", "true");
		props.put("filepath", fileName);
		/*
		 * Session mailSession = Session.getInstance(props, new Authenticator() {
		 * 
		 * @Override protected PasswordAuthentication getPasswordAuthentication() {
		 * return new PasswordAuthentication(mailAuthUser, pass); } });
		 * 
		 * mailSession.setDebug(sessionDebug);
		 */
		Session mailSession = Session.getDefaultInstance(props);
		Message message = new MimeMessage(mailSession);
		message.setFrom(new InternetAddress(from, from));
		InternetAddress[] address;
		if (to.contains(",")) {
			StringTokenizer emails = new StringTokenizer(to, ",");
			address = new InternetAddress[emails.countTokens()];
			int i = 0;
			while (emails.hasMoreTokens()) {
				String emailID = emails.nextToken();
				if (emailID.contains("@")) {
					address[i] = new InternetAddress(emailID);
					i++;
				}
			}
		} else {
			address = new InternetAddress[1];
			address[0] = new InternetAddress(to);
		}
		message.setRecipients(Message.RecipientType.TO, address);
		if (IsCC) {
			InternetAddress[] ccaddress = new InternetAddress[IConstants.CC_EMAIL.length];
			for (String cc : IConstants.CC_EMAIL) {
				ccaddress[0] = new InternetAddress(cc);
			}
			message.setRecipients(Message.RecipientType.CC, ccaddress);
		}
		message.setSubject(subject);
		message.setSentDate(new Date());
		Multipart multipart = new MimeMultipart();
		/*
		 * BodyPart messageBodyPart = new MimeBodyPart();
		 * messageBodyPart.setText(messagetext); multipart.addBodyPart(messageBodyPart);
		 */
		// String htmlfile = (String) props.get("filepath");
		try {
			// System.out.println("HTML File: " + fileName);
			BodyPart bp = new MimeBodyPart();
			// BodyPart bp2 = new MimeBodyPart();
			bp.setDataHandler(new DataHandler(new FileDataSource(fileName)));
			// bp2 = bp;
			multipart.addBodyPart(bp);
		} catch (Exception ex) {
			System.out.println("Error in Adding Multipart Data into Email: " + ex);
		}
		message.setContent(multipart);
		Transport.send(message, mailAuthUser, pass);
		System.out.println("Mail Sent to ::" + to);
		// File mailfile = new File(fileName);
		// System.out.println("Mail File Exists : " + mailfile.exists());
		// System.out.println("Mail File Deleted : " + mailfile.delete());
	}

	public static boolean sendDLRReport(String email, String from, String fileName, boolean IsCC, String time)
			throws MessagingException, UnsupportedEncodingException {
		String host = IConstants.mailHost;
		String to = email;
		// String from = IConstants.supportId;
		final String pass = IConstants.mailPassword;
		final String mailAuthUser = IConstants.mailId;
		String messagetext = "Dear User,\n\n";
		messagetext += "Please Find Delivery Report for " + time + " as attachment.\n\n";
		messagetext += "Thanks & Regards \n";
		messagetext += "Support Team \n";
		// boolean sessionDebug = false;
		Properties props = new Properties();
		// props.put("mail.smtp.user", mailAuthUser);
		props.put("mail.smtp.host", host);
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", IConstants.smtpPort + "");
		// props.put("mail.smtp.auth", "true");
		// props.put("mail.smtp.debug", "true");
		/*
		 * Session mailSession = Session.getInstance(props, new Authenticator() {
		 * 
		 * @Override protected PasswordAuthentication getPasswordAuthentication() {
		 * return new PasswordAuthentication(mailAuthUser, pass); } });
		 * 
		 * mailSession.setDebug(sessionDebug);
		 */
		Session mailSession = Session.getDefaultInstance(props);
		Message message = new MimeMessage(mailSession);
		message.setFrom(new InternetAddress(from, from));
		InternetAddress[] address;
		if (to.indexOf(",") > -1) {
			StringTokenizer emails = new StringTokenizer(to, ",");
			address = new InternetAddress[emails.countTokens()];
			int i = 0;
			while (emails.hasMoreTokens()) {
				String emailID = emails.nextToken();
				if (emailID.indexOf("@") > -1) {
					address[i] = new InternetAddress(emailID);
					i++;
				}
			}
		} else {
			address = new InternetAddress[1];
			address[0] = new InternetAddress(to);
		}
		message.setRecipients(Message.RecipientType.TO, address);
		if (IsCC) {
			InternetAddress[] ccaddress = new InternetAddress[IConstants.CC_EMAIL.length];
			for (String cc : IConstants.CC_EMAIL) {
				ccaddress[0] = new InternetAddress(cc);
			}
			message.setRecipients(Message.RecipientType.CC, ccaddress);
		}
		message.setSubject("Delivery Report " + time);
		message.setSentDate(new Date());
		// Create the message part
		BodyPart messageBodyPart = new MimeBodyPart();
		// Now set the actual message
		messageBodyPart.setText(messagetext);
		// Create a multipar message
		Multipart multipart = new MimeMultipart();
		// Set text message part
		multipart.addBodyPart(messageBodyPart);
		// Part two is attachment
		messageBodyPart = new MimeBodyPart();
		File attach = new File(fileName);
		DataSource source = new FileDataSource(attach);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(attach.getName());
		multipart.addBodyPart(messageBodyPart);
		// Send the complete message parts
		message.setContent(multipart);
		// Send message
		Transport.send(message, mailAuthUser, pass);
		System.out.println("Mail Sent: " + to);
		return true;
	}

	public static boolean sendReport(String username, String subject, String from, String email, String fileName,
			boolean IsCC) throws MessagingException, UnsupportedEncodingException {
		String host = IConstants.mailHost;
		String to = email;
		// String from = IConstants.RouteId;
		final String pass = IConstants.mailPassword;
		final String mailAuthUser = IConstants.mailId;
		String messagetext = "Dear User,\n\n";
		messagetext += "System_id: " + username + "\n\n";
		messagetext += "Please Find Coverage Report as attachment.\n\n";
		messagetext += "Thanks & Regards \n";
		messagetext += "Support Team \n";
		// boolean sessionDebug = false;
		Properties props = new Properties();
		// props.put("mail.smtp.user", mailAuthUser);
		props.put("mail.smtp.host", host);
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", IConstants.smtpPort + "");
		// props.put("mail.smtp.auth", "true");
		// props.put("mail.smtp.debug", "true");
		/*
		 * Session mailSession = Session.getInstance(props, new Authenticator() {
		 * 
		 * @Override protected PasswordAuthentication getPasswordAuthentication() {
		 * return new PasswordAuthentication(mailAuthUser, pass); } });
		 * 
		 * mailSession.setDebug(sessionDebug);
		 */
		Session mailSession = Session.getDefaultInstance(props);
		Message message = new MimeMessage(mailSession);
		message.setFrom(new InternetAddress(from, from));
		InternetAddress[] address;
		if (to.indexOf(",") > -1) {
			StringTokenizer emails = new StringTokenizer(to, ",");
			address = new InternetAddress[emails.countTokens()];
			int i = 0;
			while (emails.hasMoreTokens()) {
				String emailID = emails.nextToken();
				if (emailID.indexOf("@") > -1 && emailID.indexOf(".") > -1) {
					address[i] = new InternetAddress(emailID);
					i++;
				}
			}
		} else {
			address = new InternetAddress[1];
			address[0] = new InternetAddress(to);
		}
		message.setRecipients(Message.RecipientType.TO, address);
		if (IsCC) {
			InternetAddress[] ccaddress = new InternetAddress[IConstants.CC_EMAIL.length];
			for (String cc : IConstants.CC_EMAIL) {
				ccaddress[0] = new InternetAddress(cc);
			}
			message.setRecipients(Message.RecipientType.CC, ccaddress);
		}
		message.setSubject(subject);
		message.setSentDate(new Date());
		// Create the message part
		BodyPart messageBodyPart = new MimeBodyPart();
		// Now set the actual message
		messageBodyPart.setText(messagetext);
		// Create a multipar message
		Multipart multipart = new MimeMultipart();
		// Set text message part
		multipart.addBodyPart(messageBodyPart);
		// Part two is attachment
		messageBodyPart = new MimeBodyPart();
		File attach = new File(fileName);
		DataSource source = new FileDataSource(attach);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(attach.getName());
		multipart.addBodyPart(messageBodyPart);
		// Send the complete message parts
		message.setContent(multipart);
		// Send message
		Transport.send(message, mailAuthUser, pass);
		// System.out.println("Mail Sent: " + to);
		new File(fileName).delete();
		return true;
	}

	public static void send(String from_email, String to_email, String subject, String content)
			throws AddressException, MessagingException, Exception {
		String host = IConstants.mailHost;
		final String pass = IConstants.mailPassword;
		final String mailAuthUser = IConstants.mailId;
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", IConstants.smtpPort + "");
		Session mailSession = Session.getDefaultInstance(props);
		Message message = new MimeMessage(mailSession);
		message.setFrom(new InternetAddress(from_email));
		InternetAddress[] address;
		if (to_email.indexOf(",") > -1) {
			StringTokenizer emails = new StringTokenizer(to_email, ",");
			address = new InternetAddress[emails.countTokens()];
			int i = 0;
			while (emails.hasMoreTokens()) {
				String emailID = emails.nextToken();
				if (emailID.indexOf("@") > -1 && emailID.indexOf(".") > -1) {
					address[i] = new InternetAddress(emailID);
					i++;
				}
			}
		} else {
			address = new InternetAddress[1];
			address[0] = new InternetAddress(to_email);
		}
		message.setRecipients(Message.RecipientType.TO, address);
		message.setSubject(subject);
		message.setSentDate(new Date());
		message.setText(content);
		// Send message
		Transport.send(message, mailAuthUser, pass);
		System.out.println("Mail Sent From[" + from_email + "] To[" + to_email + "]");
	}
}
