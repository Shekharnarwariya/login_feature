package com.hti.smpp.common.email;

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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
/**
 * The EmailSender class is a Spring component for sending emails.
 */
@Component
public class EmailSender {

	private static final String INLINE_IMAGE_RESOURCE = "/templates/logo.png";
	private static final String INLINE_BACKGROUND_IMAGE_RESOURCE = "/templates/Background.png";
	private final JavaMailSender javaMailSender;
	private final TemplateEngine templateEngine;
	private final Logger log = LoggerFactory.getLogger(EmailSender.class);

	@Value("${spring.mail.username}")
	private String username;

	@Autowired
	public EmailSender(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
		this.javaMailSender = javaMailSender;
		this.templateEngine = templateEngine;
	}
/**
 * Sends an email with attachments using JavaMailSender and TemplateEngine.
 * @param emailTo
 * @param subject
 * @param filePath
 * @param sourceMap
 */
	public void sendEmail(String emailTo, String subject, String filePath, Map<String, String> sourceMap) {
		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			Context context = createContext(sourceMap);
			String emailContent = templateEngine.process(filePath, context);

			setupEmailMessage(emailTo, subject, emailContent, helper);

			javaMailSender.send(message);
			log.info("Email send called successfully");
		} catch (MessagingException e) {
			log.error("Error sending email", e);
		}
	}
/**
 * Creates a Thymeleaf Context with variables based on the provided key-value pairs.
 * @param sourceMap
 * @return
 */
	private Context createContext(Map<String, String> sourceMap) {
		Context context = new Context();
		for (Map.Entry<String, String> entry : sourceMap.entrySet()) {
			context.setVariable(entry.getKey(), entry.getValue());
		}
		return context;
	}
/**
 * Sets up an email message with recipient, subject, content, and inline images.
 * @param emailTo
 * @param subject
 * @param emailContent
 * @param helper
 * @throws MessagingException
 */
	private void setupEmailMessage(String emailTo, String subject, String emailContent, MimeMessageHelper helper)
			throws MessagingException {
		helper.setTo(emailTo);
		helper.setSubject(subject);
		helper.setText(emailContent, true);
		ClassPathResource imageResource = new ClassPathResource(INLINE_IMAGE_RESOURCE);
		ClassPathResource backgroundImageResource = new ClassPathResource(INLINE_BACKGROUND_IMAGE_RESOURCE);
		helper.addInline("htiLogo", imageResource);
		helper.addInline("background", backgroundImageResource);
	}

//	public Map<String, String> createSourceMap(String message, String otp, String username, String password) {
//		Map<String, String> sourceMap = new HashMap<>();
//		sourceMap.put("message", message);
//		sourceMap.put("otp", otp);
//		sourceMap.put("username", username);
//		sourceMap.put("password", password);
//		return sourceMap;
//	}
/**
 *  Creates a simple key-value map with a message.
 * @param message
 * @return
 */
	public Map<String, String> createSourceMap(String message) {
		Map<String, String> sourceMap = new HashMap<String, String>();
		sourceMap.put("message", message);
		return sourceMap;
	}
/**
 *  Creates a map with key-value pairs for a message, username, and flag.
 * @param message
 * @param username
 * @param flag
 * @return
 */
	public Map<String, String> createSourceMap(String message, String username, String flag) {
		Map<String, String> sourceMap = new HashMap<String, String>();
		sourceMap.put("message", message);
		sourceMap.put("username", username);
		sourceMap.put("flag", flag);
		return sourceMap;

	}
/**
 * Creates a map with key-value pairs for a message, OTP, second message, flag, and username.
 * @param message
 * @param otp
 * @param secondMessage
 * @param flag
 * @param username
 * @return
 */
	public Map<String, String> createSourceMap(String message, String otp, String secondMessage, String flag,
			String username) {
		Map<String, String> sourceMap = new HashMap<String, String>();
		sourceMap.put("message", message);
		sourceMap.put("otp", otp);
		sourceMap.put("secondMessage", secondMessage);
		sourceMap.put("flag", flag);
		sourceMap.put("username", username);
		return sourceMap;

	}
	
	public Map<String, String> createCustomSourceMap(String username, String gateway, String ipaddress, String date, String gmt) {
		Map<String, String> sourceMap = new HashMap<String, String>();
		sourceMap.put("username", username);
		sourceMap.put("gateway", gateway);
		sourceMap.put("ipaddress",ipaddress);
		sourceMap.put("date", date);
		sourceMap.put("gmt", gmt);
		return sourceMap;
	}
}