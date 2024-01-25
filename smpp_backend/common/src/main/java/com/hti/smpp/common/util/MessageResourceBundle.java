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
		Locale hindiLocale = new Locale("hi", "IN");
		return messageSource.getMessage(key, null, hindiLocale);
	}
}
