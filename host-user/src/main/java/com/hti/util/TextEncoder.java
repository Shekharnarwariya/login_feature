/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hti.util;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 *
 * @author Administrator
 */
public class TextEncoder {

	private final String ALGO = "Blowfish";
	private final byte[] keyValue = new byte[] { 'T', 'h', 'e', 'B', 'e', 's', 't', 'S', 'e', 'c', 'r', 'e', 't', 'K',
			'e', 'y' };
	byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0 };

	public String encode(String Data) throws Exception {
		Key key = generateKey();
		IvParameterSpec ivspec = new IvParameterSpec(iv);
		Cipher c = Cipher.getInstance("Blowfish/CBC/PKCS5Padding");
		c.init(Cipher.ENCRYPT_MODE, key, ivspec);
		byte[] encVal = c.doFinal(Data.trim().getBytes());
		String encryptedValue = Base64.getEncoder().encodeToString(encVal);
		return encryptedValue;
	}

	public String decode(String encryptedData) throws Exception {
		Key key = generateKey();
		IvParameterSpec ivspec = new IvParameterSpec(iv);
		Cipher c = Cipher.getInstance("Blowfish/CBC/PKCS5Padding");
		c.init(Cipher.DECRYPT_MODE, key, ivspec);
		byte[] decordedValue = Base64.getDecoder().decode(encryptedData.trim());
		byte[] decValue = c.doFinal(decordedValue);
		String decryptedValue = new String(decValue);
		return decryptedValue;
	}

	private Key generateKey() throws Exception {
		Key key = new SecretKeySpec(keyValue, ALGO);
		return key;
	}

}
