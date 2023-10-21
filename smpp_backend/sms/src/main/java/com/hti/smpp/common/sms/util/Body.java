package com.hti.smpp.common.sms.util;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.logica.smpp.util.ByteBuffer;
//import com.logica.webems.client.Binary;

@Component
public class Body {
	public static ByteBuffer getHead() {
		ByteBuffer bfm = new ByteBuffer();
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 1);
		bfm.appendByte((byte) 57);
		bfm.appendByte((byte) 49);
		bfm.appendByte((byte) 57);
		bfm.appendByte((byte) 56);
		bfm.appendByte((byte) 50);
		bfm.appendByte((byte) 55);
		bfm.appendByte((byte) 51);
		bfm.appendByte((byte) 57);
		bfm.appendByte((byte) 56);
		bfm.appendByte((byte) 56);
		bfm.appendByte((byte) 57);
		bfm.appendByte((byte) 51);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 64);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		bfm.appendByte((byte) 0);
		return bfm;
	}

	public static ByteBuffer getHeader(int random, int total, int current) {
		ByteBuffer bf = new ByteBuffer();
		bf.appendByte((byte) 5);
		bf.appendByte((byte) 0);
		bf.appendByte((byte) 3);
		bf.appendByte((byte) random);
		bf.appendByte((byte) total);
		bf.appendByte((byte) current);
		return bf;
	}

	public static List getEnglishno(String msg) {
		List toReturn = new ArrayList();
		if (msg.length() > 160) {
			while (msg.length() > 153) {
				toReturn.add(msg.substring(0, 153));
				msg = msg.substring(153, msg.length());
			}
			toReturn.add(msg);
		}
		return toReturn;
	}

	public static List<String> getUnicodeno(String msg) {
		List<String> toReturn = new ArrayList<String>();
		if (msg.length() > 280) {
			while (msg.length() > 268) {
				toReturn.add(msg.substring(0, 268));
				msg = msg.substring(268, msg.length());
			}
			toReturn.add(msg);
		}
		return toReturn;
	}

	public static ByteBuffer getRingHeader() {
		ByteBuffer bf = new ByteBuffer();
		bf.appendByte((byte) 6);
		bf.appendByte((byte) 5);
		bf.appendByte((byte) 4);
		bf.appendByte((byte) 21);
		bf.appendByte((byte) 129);
		bf.appendByte((byte) 21);
		bf.appendByte((byte) 129);
		return bf;
	}

	public static ByteBuffer getRingBuffer(char[] st) {
		ByteBuffer bf = new ByteBuffer();
		int code = 0;
		for (int i = 0; i < st.length; i += 2) {
			code += Character.digit(st[i], 16) * 16;
			code += Character.digit(st[i + 1], 16);
			bf.appendByte((byte) code);
			code = 0;
		}
		return bf;
	}

	public static ByteBuffer Header(char[] st) {
		ByteBuffer bf = new ByteBuffer();
		int code = 0;
		for (int i = 0; i < st.length; i += 2) {
			code += Character.digit(st[i], 16) * 16;
			code += Character.digit(st[i + 1], 16);
			bf.appendByte((byte) code);
			code = 0;
		}
		return bf;
	}

	public static String getCharFromHex(char[] st) {
		String toReturn = "";
		int code = 0;
		for (int i = 0; i < st.length; i += 2) {
			code += Character.digit(st[i], 16) * 16;
			code += Character.digit(st[i + 1], 16);
			toReturn += (char) code;
			code = 0;
		}
		return toReturn;
	}

	public static String getEnglishMessage(String engmsg) throws Exception {
		if (engmsg.length() < 153) {
			engmsg += " ";
		}
		return engmsg;
	}

	public static String getUnicodeMessage(String unimsg) throws Exception {
		if (unimsg.length() < 268) {
			unimsg += " ";
		}
		return unimsg;
	}

	public static int getLength(int contentlength) {
		int len1 = 0;
		int len = 0;
		int ext = 0;
		// if((contentlength%2)!=0)
		ext = 1;
		// else ext=2;
		len = ((contentlength) * 7);
		len1 = len / 8;
		len1 = ext + len1 + 6;
		if (contentlength < 153) {
			len = len - 1;
		}
		return len1;
	}

	public static int getLength1(int contentlength) {
		int len1 = 0;
		int len = 0;
		int ext = 0;
		// if((contentlength%2)!=0)
		ext = 1;
		// else ext=2;
		len = ((contentlength) * 7);
		len1 = len / 8;
		len1 = ext + len1 + 6;
		if (contentlength < 268) {
			len = len - 1;
		}
		return len1;
	}

	public static String getBinary(int no) {
		String binary = "";
		String fb = "";
		while (no != 0) {
			binary += no % 2;
			no = no / 2;
		}
		// if(no==6)return binary;
		char[] arr = binary.toCharArray();
		int len = arr.length;
		if (len < 7) {
			for (int i = 1; i + len <= 7; i++) {
				fb += 0;
			}
			for (int i = len - 1; i > -1; i--) {
				fb += arr[i];
			}
		} else {
			for (int i = len - 1; i > -1; i--) {
				fb += arr[i];
			}
		}
		// System.out.println(fb);
		return fb;
	}
}