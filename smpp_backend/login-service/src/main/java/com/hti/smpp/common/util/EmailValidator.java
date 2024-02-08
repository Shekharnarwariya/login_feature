package com.hti.smpp.common.util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * The {@code EmailValidator} class provides a utility method for validating
 * email addresses. This version uses a more comprehensive regular expression
 * for better accuracy.
 */
public class EmailValidator {
	private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@"
			+ "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

	public static boolean isEmailValid(String email) {
		Pattern pattern = Pattern.compile(EMAIL_REGEX);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
}
