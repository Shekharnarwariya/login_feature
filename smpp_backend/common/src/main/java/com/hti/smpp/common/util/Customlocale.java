package com.hti.smpp.common.util;

import java.util.Locale;

public class Customlocale {

	public static Locale getLocaleByLanguage(String lang) {
	    Locale locale;

	    switch (lang.toLowerCase()) {
	        case "ar":
	            locale = new Locale("ar", "SA");
	            break;
	        case "fr":
	            locale = new Locale("fr", "FR");
	            break;
	        case "es":
	            locale = new Locale("es", "ES");
	            break;
	        case "en":
	            locale = new Locale("en", "US");
	            break;
	        case "de":
	            locale = new Locale("de", "DE");
	            break;
	        case "zh":
	            locale = new Locale("zh", "CN");
	            break;
	        case "ru":
	            locale = new Locale("ru", "RU");
	            break;
	        case "ja":
	            locale = new Locale("ja", "JP");
	            break;
	        case "it":
	            locale = new Locale("it", "IT");
	            break;
	        case "hi":
	            locale = new Locale("hi", "IN");
	            break;
	        case "pt":
	            locale = new Locale("pt", "BR");
	            break;
	        case "sv":
	            locale = new Locale("sv", "SE");
	            break;
	        case "tr":
	            locale = new Locale("tr", "TR");
	            break;
	        case "el":
	            locale = new Locale("el", "GR");
	            break;
	        case "ko":
	            locale = new Locale("ko", "KR");
	            break;
	        case "no":
	            locale = new Locale("no", "NO");
	            break;
	        default:
	            // Default to English if the provided language is not recognized
	            locale = new Locale("en", "US");
	            break;
	    }

	    return locale;
	}

}
