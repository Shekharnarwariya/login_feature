package com.hti.smpp.common.twoway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;
/**
 *  The `LocaleConfig` class is a configuration class used to customize locale settings
 */
@Configuration
public class LocaleConfig {
	/**
	 * The `localeResolver` method in the `LocaleConfig` class defines a custom bean
	 * @return
	 */
	@Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.US); // Set the default locale
        return resolver;
    }

}
