/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Administrator
 */
public class Converter {
	private static Logger logger = LoggerFactory.getLogger(Converter.class);

	public static String getUnicode(char[] buffer) {
		String unicode = "";
		int code = 0;
		int j = 0;
		char[] unibuffer = new char[buffer.length / 4];
		for (int i = 0; i < buffer.length; i += 4) {
			code += Character.digit(buffer[i], 16) * 4096;
			code += Character.digit(buffer[i + 1], 16) * 256;
			code += Character.digit(buffer[i + 2], 16) * 16;
			code += Character.digit(buffer[i + 3], 16);
			unibuffer[j++] = (char) code;
			code = 0;
		}
		unicode = new String(unibuffer);
		return unicode;
	}
}
