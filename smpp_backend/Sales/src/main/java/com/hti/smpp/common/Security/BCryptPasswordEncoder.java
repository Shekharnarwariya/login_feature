package com.hti.smpp.common.Security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoder implements PasswordEncoder {

	@Override
	public String encode(CharSequence rawPassword) {
		String textToHash = (String) rawPassword;
		String encodedPassword = null;
		try {
			encodedPassword = Base64.getEncoder().encodeToString(
					MessageDigest.getInstance("SHA-256").digest(textToHash.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return encodedPassword;
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		String encodedRawPassword = encode(rawPassword);
		return encodedRawPassword.equals(encodedPassword);
	}
}
