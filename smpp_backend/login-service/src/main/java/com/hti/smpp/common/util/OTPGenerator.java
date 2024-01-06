package com.hti.smpp.common.util;

import java.util.Random;
/**
 * The {@code OTPGenerator} class provides a utility method for generating One-Time Passwords (OTPs).
 */
public class OTPGenerator {
	public static String generateOTP(int length) {
		String characters = "0123456789"; // Possible characters in OTP
		Random random = new Random();
		StringBuilder otp = new StringBuilder();

		for (int i = 0; i < length; i++) {
			int index = random.nextInt(characters.length());
			char digit = characters.charAt(index);
			otp.append(digit);
		}

		return otp.toString();
	}
}