package com.hti.smpp.common.util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class EmailValidator {
	private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

	public static boolean isEmailValid(String email) {
		Pattern pattern = Pattern.compile(EMAIL_REGEX);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
}