package com.hti.smpp.common.util;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class MessageResourceBundle {

	private final MessageSource messageSource;

	@Autowired
	public MessageResourceBundle(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public String getMessage(String key) {
		Locale locale = Locale.getDefault();
		return messageSource.getMessage(key, null, locale);
	}

	public String getLogMessage(String key) {
		Locale locale = new Locale("log", "US");
		return messageSource.getMessage(key, null, locale);
	}
}