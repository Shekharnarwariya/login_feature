package com.hti.smpp.common.util.dto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordUtil {
	public static String hashWith256(String textToHash) throws NoSuchAlgorithmException {
		return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(textToHash.getBytes(StandardCharsets.UTF_8)));
	}
}
