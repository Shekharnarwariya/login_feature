package com.hti.util;

import java.security.Security;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailSender {
	private static Logger logger = LoggerFactory.getLogger(EmailSender.class);

	public EmailSender() {
		//Security.addProvider(new javax.net.ssl.);
	}

	public void sendSSLMessage(String recipients[], String subject, String message, String from)
			throws MessagingException {
		Properties props = new Properties();
		props.put("mail.smtp.host", Constants.SMTP_HOST_NAME);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", Constants.SMTP_PORT);
		props.put("mail.smtp.socketFactory.port", Constants.SMTP_PORT);
		props.put("mail.smtp.socketFactory.fallback", "false");
		Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(Constants.EMAIL_USER, Constants.EMAIL_PASSWORD);
			}
		});
		Message msg = new MimeMessage(session);
		InternetAddress addressFrom = new InternetAddress(from);
		msg.setFrom(addressFrom);
		InternetAddress[] addressTo = new InternetAddress[recipients.length];
		for (int i = 0; i < recipients.length; i++) {
			addressTo[i] = new InternetAddress(recipients[i]);
		}
		msg.setRecipients(Message.RecipientType.TO, addressTo);
		msg.setSubject(subject);
		msg.setContent(message, "text/html");
		Transport.send(msg);
	}
}
