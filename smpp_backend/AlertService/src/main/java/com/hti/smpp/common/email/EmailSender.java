package com.hti.smpp.common.email;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Component;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//
//@Component
//public class EmailSender {
//
//
//
//	private final JavaMailSender javaMailSender;
//	private final Logger log = LoggerFactory.getLogger(EmailSender.class);
//
//
//	@Autowired
//	public EmailSender(JavaMailSender javaMailSender) {
//		this.javaMailSender = javaMailSender;
//	}
//
//	public void sendEmail(String emailTo, String subject) {
//		try {
//			
//			MimeMessage message = javaMailSender.createMimeMessage();
//			MimeMessageHelper helper = new MimeMessageHelper(message, true);
//			
//
//			setupEmailMessage(emailTo, subject, helper);
//			
//			javaMailSender.send(message);
//			System.out.println("om");
//			log.info("Sending email to {} with subject: {}", emailTo, subject);
//			log.info("Email send called successfully");
//		} catch (MessagingException e) {
//			log.error("Error sending email", e);
//			log.error("Exception details: {}", e.getMessage());
//		}catch (Exception ex) {
//            log.error("Unexpected error", ex);
//        }
//	}
//
//
//	private void setupEmailMessage(String emailTo, String subject, MimeMessageHelper helper)
//			throws MessagingException {
//		helper.setTo(emailTo);
//		helper.setSubject(subject);
//
//		
//	}
//}



import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
public class EmailSender {

	
	private final JavaMailSender javaMailSender;
	private final Logger log = LoggerFactory.getLogger(EmailSender.class);

	@Value("${spring.mail.username}")
	private String username;

	@Autowired
	public EmailSender(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}

	public void sendEmail(String emailTo, String emailContent, String subject) {
		try {
			
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			
//			helper.setText(emailContent, true);

			
			//Context context = createContext(sourceMap);
			setupEmailMessage(emailTo, subject, emailContent, helper);

			javaMailSender.send(message);
			log.info("Email send called successfully");
		} catch (MessagingException e) {
			log.error("Error sending email", e);
		}
	}

	

	private void setupEmailMessage(String emailTo, String subject, String emailContent, MimeMessageHelper helper)
			throws MessagingException {
		helper.setTo(emailTo);
		helper.setSubject(subject);
		helper.setText(emailContent, true);
	
	}
	
	public Map<String, String> createSourceMap(String message, String username) {
		Map<String, String> sourceMap = new HashMap<String, String>();
		sourceMap.put("message", message);
		sourceMap.put("username", username);
		
		return sourceMap;

	}
	
	
}