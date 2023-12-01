package com.hti.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnicodeMapConverter implements AttributeConverter<Map<String, String>, String> {
	private Logger logger = LoggerFactory.getLogger(UnicodeMapConverter.class);

	@Override
	public String convertToDatabaseColumn(Map<String, String> arg0) {
		String replacement_text = "";
		for (Map.Entry<String, String> entry : arg0.entrySet()) {
			try {
				replacement_text += getUTF8toHexDig(entry.getKey()) + "|" + getUTF8toHexDig(entry.getValue()) + ",";
			} catch (Exception e) {
				logger.error(entry.getKey() + " : " + entry.getValue(), e.fillInStackTrace());
			}
		}
		if (replacement_text.length() > 0) {
			replacement_text = replacement_text.substring(0, replacement_text.length() - 1);
		}
		return replacement_text;
	}

	@Override
	public Map<String, String> convertToEntityAttribute(String replaceContent) {
		Map<String, String> content_key_value = new HashMap<String, String>();
		if (replaceContent != null && replaceContent.length() > 0 && replaceContent.contains("|")) {
			String[] tokens = replaceContent.split(",");
			for (String part : tokens) {
				try {
					if (part.contains("|")) {
						String key = part.substring(0, part.indexOf("|"));
						String value = part.substring(part.indexOf("|") + 1, part.length());
						content_key_value.put(getUnicode(key.toCharArray()), getUnicode(value.toCharArray()));
					}
				} catch (Exception e) {
					logger.error(part, e.fillInStackTrace());
				}
			}
		}
		return content_key_value;
	}

	private String getUnicode(char[] buffer) throws Exception {
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

	private String getUTF8toHexDig(String str) throws Exception {
		String dump = "";
		int fina = 0;
		int big = 128;
		int first = 0;
		for (int i = 0; i < str.length();) {
			first = str.charAt(i);
			char ch = str.charAt(i);
			if (first > 128) {
				int count = 0;
				int value = 128;
				i = i + 1;
				fina = first;
				value = big;
				while (fina >= value) {
					count += 1;
					fina = fina - value;
					value = value / 2;
				}
				if (fina == 0) {
					first = 0;
					value = big;
					fina = 0;
					count = 1;
					first = str.charAt(i);
					i = i + 1;
					fina = first;
					while (fina >= value) {
						count += 1;
						fina = fina - value;
						value = value / 2;
					}
				}
				first = 0;
				if (count > 2) {
					fina = fina << 12;
				} else {
					fina = fina << 6;
				}
				while (count > 1) {
					first = str.charAt(i);
					first = first - big;
					// System.out.println("value fo"+first);
					count = count - 1;
					if (count > 1) {
						first = first << 6;
						fina += first;
					} else {
						fina += first;
					}
					i = i + 1;
					first = 0;
				}
				String temp1 = "";
				String temp = Integer.toHexString(fina);
				for (int j = temp.length(); j < 4; j++) {
					temp1 += "0";
				}
				dump += temp1 + temp;
			} else {
				String onechar = String.valueOf(ch);
				dump += getUnicodeTOHex(onechar);
				first = 0;
				i += 1;
			}
		}
		if (dump.toUpperCase().contains("000D")) {
			dump = dump.toUpperCase().replaceAll("000D", "");
		}
		return dump;
	}

	private String getUnicodeTOHex(String unicode) throws Exception {
		String hexa = "";
		Writer file_writer;
		InputStreamReader in = null;
		try {
			file_writer = new OutputStreamWriter(new FileOutputStream("Utf"), "UTF-8");
			file_writer.write(unicode);
			file_writer.close();
			in = new InputStreamReader(new FileInputStream(new File("Utf")), "UTF-8");
			int a = 0;
			while ((a = in.read()) != -1) {
				if (a > 65000) {
					continue;
				}
				String temp = Integer.toHexString(a);
				String put = "";
				for (int i = temp.length(); i < 4; i++) {
					put += "0";
				}
				put += temp;
				hexa += put;
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
		return (hexa);
	}
}
