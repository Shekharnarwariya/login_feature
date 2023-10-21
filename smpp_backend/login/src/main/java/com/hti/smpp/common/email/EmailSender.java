package com.hti.smpp.common.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
//import org.thymeleaf.TemplateEngine;
//import org.thymeleaf.context.Context;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.HashMap;
import java.util.Map;

@Component
public class EmailSender {

	private static final String INLINE_IMAGE_RESOURCE = "/templates/hti.jpg";

	//private final JavaMailSender javaMailSender;
	//private final TemplateEngine templateEngine;
	private final Logger log = LoggerFactory.getLogger(EmailSender.class);

	@Value("${username}")
	private String username;

	// @Autowired
	// public EmailSender(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
	// 	this.javaMailSender = javaMailSender;
	// 	this.templateEngine = templateEngine;
	// }

	public void sendEmail(String emailTo, String subject, String filePath, Map<String, String> sourceMap) {
		// try {
		// 	// MimeMessage message = javaMailSender.createMimeMessage();
		// 	// MimeMessageHelper helper = new MimeMessageHelper(message, true);

		// 	//Context context = createContext(sourceMap);
		// 	//String emailContent = templateEngine.process(filePath, context);

		// 	//setupEmailMessage(emailTo, subject, emailContent, helper);

		// 	//javaMailSender.send(message);
		// 	log.info("Email send called successfully");
		// } catch (MessagingException e) {
		// 	log.error("Error sending email", e);
		// }
	}

	// private Context createContext(Map<String, String> sourceMap) {
	// 	//Context context = new Context();
	// 	for (Map.Entry<String, String> entry : sourceMap.entrySet()) {
	// 		context.setVariable(entry.getKey(), entry.getValue());
	// 	}
	// 	return context;
	// }

	private void setupEmailMessage(String emailTo, String subject, String emailContent, MimeMessageHelper helper)
			throws MessagingException {
		helper.setTo(emailTo);
		helper.setSubject(subject);
		helper.setText(emailContent, true);
		ClassPathResource imageResource = new ClassPathResource(INLINE_IMAGE_RESOURCE);
		helper.addInline("htiLogo", imageResource);
	}

	public Map<String, String> createSourceMap(String message, String otp, String username, String password) {
		Map<String, String> sourceMap = new HashMap<>();
		sourceMap.put("message", message);
		sourceMap.put("otp", otp);
		sourceMap.put("username", username);
		sourceMap.put("password", password);
		return sourceMap;
	}

	public Map<String, String> createSourceMap(String message) {
		Map<String, String> sourceMap = new HashMap<String, String>();
		sourceMap.put("message", message);
		return sourceMap;
	}

	public Map<String, String> createSourceMap(String message, String username, String password) {
		Map<String, String> sourceMap = new HashMap<String, String>();
		sourceMap.put("message", message);
		sourceMap.put("username", username);
		sourceMap.put("password", password);
		return sourceMap;

	}

	public Map<String, String> createSourceMap(String message, String otp) {
		Map<String, String> sourceMap = new HashMap<String, String>();
		sourceMap.put("message", message);
		sourceMap.put("otp", otp);
		return sourceMap;

	}
}