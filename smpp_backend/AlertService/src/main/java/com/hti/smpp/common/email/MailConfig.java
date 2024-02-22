package com.hti.smpp.common.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

	 @Autowired
	 private MailProperties mailProperties;

	   
	    public JavaMailSender javaMailSender() {
	        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	        System.out.println(mailProperties.getMailHost());
	       
	        mailSender.setHost(mailProperties.getMailHost());
	        mailSender.setPort(mailProperties.getSmtpport());
	        mailSender.setProtocol(mailProperties.getProtocol());
	        mailSender.setUsername(mailProperties.getMailId());
	        mailSender.setPassword(mailProperties.getMailPassword());
	        System.out.println(mailProperties.getMailId());
	        return mailSender;
	    }
	    
}
