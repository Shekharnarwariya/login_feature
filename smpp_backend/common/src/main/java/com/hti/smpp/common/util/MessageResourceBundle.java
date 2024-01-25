package com.hti.smpp.common.util;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import java.util.Locale;
@Component
public class MessageResourceBundle {

	private final MessageSource messageSource;

	@Autowired
	public MessageResourceBundle(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

    public String getMessage(String key, String languageCode) {
        Locale locale;
        if (languageCode != null && languageCode.length > 0) {
            locale = Customlocale.getLocaleByLanguage(languageCode[0]);
        } else {
            locale = LocaleContextHolder.getLocale();
        }
        return messageSource.getMessage(key, null, locale);
    }
}
