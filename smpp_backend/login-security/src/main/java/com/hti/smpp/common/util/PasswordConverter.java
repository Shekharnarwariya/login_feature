package com.hti.smpp.common.util;

import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Component
@Converter(autoApply = true)
public class PasswordConverter implements AttributeConverter<String, String> {
	private Logger logger = LoggerFactory.getLogger(PasswordConverter.class);
	private final String ALGO = "Blowfish";
	private final byte[] keyValue = new byte[] { 'T', 'h', 'e', 'B', 'e', 's', 't', 'S', 'e', 'c', 'r', 'e', 't', 'K',
			'e', 'y' };
	byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0 };

	@Override
	public String convertToDatabaseColumn(String arg0) {
		logger.debug("convertToDatabaseColumn(" + arg0 + ")");
		try {
			Key key = generateKey();
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			Cipher c = Cipher.getInstance("Blowfish/CBC/PKCS5Padding");
			c.init(Cipher.ENCRYPT_MODE, key, ivspec);
			byte[] encVal = c.doFinal(arg0.trim().getBytes());
			return Base64.getEncoder().encodeToString(encVal);
		} catch (Exception e) {
			logger.error(arg0 + " EncodeError: " + e);
			return null;
		}
	}

	@Override
	public String convertToEntityAttribute(String arg0) {
		logger.debug("convertToEntityAttribute(" + arg0 + ")");
		try {
			Key key = generateKey();
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			Cipher c = Cipher.getInstance("Blowfish/CBC/PKCS5Padding");
			c.init(Cipher.DECRYPT_MODE, key, ivspec);
			byte[] decordedValue = Base64.getDecoder().decode(arg0.trim());
			byte[] decValue = c.doFinal(decordedValue);
			return new String(decValue);
		} catch (Exception e) {
			logger.error(arg0 + " DecodeError: " + e);
			return null;
		}
	}

	private Key generateKey() throws Exception {
		Key key = new SecretKeySpec(keyValue, ALGO);
		return key;
	}
}
