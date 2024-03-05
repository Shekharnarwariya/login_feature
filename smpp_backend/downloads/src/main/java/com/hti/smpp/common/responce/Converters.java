package com.hti.smpp.common.responce;

public class Converters {
	public String UTF8(String utf8TA) {
		String tempS;
		StringBuffer strBuff = new StringBuffer();
		try {
			utf8TA = new String(utf8TA.getBytes("UTF-8"), "UTF-8");
			byte[] byteBuff = utf8TA.getBytes("UTF-8");
			for (int l = 0; l < byteBuff.length; l++) {
				tempS = byteToHex(byteBuff[l]);
				if (!tempS.equalsIgnoreCase("0D")) {
					strBuff.append(tempS);
				}
			}
			utf8TA = strBuff.toString();
			strBuff = null;
		} catch (Exception ex) {
			System.out.println("EXCEPTION FROM UTF8 method :: " + ex);
			ex.printStackTrace();
		}
		return utf8TA;
	}

	public String UTF16(String utf16TA) {
		byte[] byteBuff;
		StringBuffer strBuff = new StringBuffer();
		String tempS;
		try {
			utf16TA = new String(utf16TA.getBytes("UTF-16"), "UTF-16");
			if (utf16TA != null && utf16TA.compareTo("") != 0) {
				byteBuff = utf16TA.getBytes("UTF-16");
				for (int l = 0; l < byteBuff.length; l++) {
					tempS = byteToHex(byteBuff[l]);
					if (!tempS.equalsIgnoreCase("0D")) {
						strBuff.append(tempS);
					} else {
						strBuff.delete(strBuff.length() - 2, strBuff.length());
					}
				}
				utf16TA = strBuff.toString();
				utf16TA = utf16TA.substring(4, utf16TA.length());
				strBuff = null;
			}
		} catch (Exception ex) {
			System.out.println("EXCEPTION FROM UTF16 method :: " + ex);
		}
		return utf16TA;
	}

	public String Uni(String uniTA, String pref, boolean reqSpac) {
		StringBuffer strBuff = new StringBuffer();
		for (int l = 0; l < uniTA.length(); l += 4) {
			strBuff.append(pref + uniTA.substring(l, l + 4));
			if (reqSpac && l != uniTA.length() - 4) {
				strBuff.append(" ");
			}
		}
		uniTA = strBuff.toString();
		strBuff = null;
		return uniTA;
	}

	public String hexEnc(String hexTA) {
		int l, indx;
		String tempS;
		StringBuffer strBuff = new StringBuffer();
		for (l = 0; l < hexTA.length(); l += 4) {
			tempS = hexTA.substring(l, l + 4);
			indx = tempS.indexOf("00");
			if (indx >= 0) {
				tempS = tempS.substring(tempS.indexOf("00") + 2, tempS.length());
			}
			strBuff.append(tempS);
			if (l < hexTA.length() - 4) {
				strBuff.append(" ");
			}
		}
		hexTA = strBuff.toString();
		strBuff.delete(0, strBuff.length());
		strBuff = null;
		return hexTA;
	}

	public String hexNCR(String hexTA) {
		int l, indx;
		String tempS, hncrTA;
		StringBuffer str2Buff = new StringBuffer();
		for (l = 0; l < hexTA.length(); l += 4) {
			tempS = hexTA.substring(l, l + 4);
			indx = tempS.indexOf("00");
			if (indx >= 0) {
				tempS = tempS.substring(tempS.indexOf("00") + 2, tempS.length());
			}
			str2Buff.append("&amp;#x" + tempS + ";");
		}
		hncrTA = str2Buff.toString();
		str2Buff.delete(0, str2Buff.length());
		str2Buff = null;
		return hncrTA;
	}

	public String deciEnc(String decTA) {
		StringBuffer strBuff = new StringBuffer();
		char[] charBuff = decTA.toCharArray();
		int l, code = 0;
		for (l = 0; l < charBuff.length; l += 4) {
			code += Character.digit(charBuff[l], 16) * 4096;
			code += Character.digit(charBuff[l + 1], 16) * 256;
			code += Character.digit(charBuff[l + 2], 16) * 16;
			code += Character.digit(charBuff[l + 3], 16);
			strBuff.append(code);
			if (l < charBuff.length - 4) {
				strBuff.append(" ");
			}
			code = 0;
		}
		decTA = strBuff.toString();
		strBuff = null;
		return decTA;
	}

	public String decNCR(String decTA) {
		String dncrTA;
		StringBuffer str2Buff = new StringBuffer();
		char[] charBuff = decTA.toCharArray();
		int code = 0;
		for (int l = 0; l < charBuff.length; l += 4) {
			code += Character.digit(charBuff[l], 16) * 4096;
			code += Character.digit(charBuff[l + 1], 16) * 256;
			code += Character.digit(charBuff[l + 2], 16) * 16;
			code += Character.digit(charBuff[l + 3], 16);
			str2Buff.append("&amp;#" + code + ";");
			code = 0;
		}
		dncrTA = str2Buff.toString();
		str2Buff = null;
		return dncrTA;
	}

	public String percent(String percTA) {
		byte[] byteBuff;
		StringBuffer strBuff = new StringBuffer();
		try {
			percTA = new String(percTA.getBytes("UTF-8"), "UTF-8");
			byteBuff = percTA.getBytes("UTF-8");
			for (int l = 0; l < byteBuff.length; l++) {
				if (byteBuff[l] >= 0 && byteBuff[l] <= 127) {
					if (byteBuff[l] == 13 || byteBuff[l] == 10 || byteBuff[l] == 47 || byteBuff[l] == 96
							|| (byteBuff[l] >= 32 && byteBuff[l] <= 44) || (byteBuff[l] >= 58 && byteBuff[l] <= 64)
							|| (byteBuff[l] >= 91 && byteBuff[l] <= 94) || (byteBuff[l] >= 123 && byteBuff[l] <= 125)) {
						if (byteBuff[l] != 13) {
							strBuff.append("%" + byteToHex(byteBuff[l]));
						}
					} else {
						strBuff.append((char) byteBuff[l]);
					}
				} else {
					strBuff.append("%" + byteToHex(byteBuff[l]));
				}
			}
			percTA = strBuff.toString();
			strBuff = null;
		} catch (Exception ex) {
			System.out.println("EXCEPTION FROM percent method :: " + ex);
			ex.printStackTrace();
		}
		return percTA;
	}

	public String htmlEnc(String htmlTA) {
		byte[] byteBuff, var, charByte, charsByte;
		int x = 0, m;
		try {
			htmlTA = new String(htmlTA.getBytes("UTF-8"), "UTF-8");
			byteBuff = htmlTA.getBytes("UTF-8");
			var = new byte[byteBuff.length * 10];
			for (int l = 0; l < byteBuff.length; l++) //
			{
				if (byteBuff[l] == 34 || byteBuff[l] == 38 || byteBuff[l] == 60 || byteBuff[l] == 62) {
					if (byteBuff[l] == 34) {
						charByte = "&amp;quot;".getBytes("UTF-8");
						for (m = 0; m < charByte.length; m++) {
							var[x++] = charByte[m];
						}
					} else if (byteBuff[l] == 38) {
						charByte = "&amp;amp;".getBytes("UTF-8");
						for (m = 0; m < charByte.length; m++) {
							var[x++] = charByte[m];
						}
					} else if (byteBuff[l] == 60) {
						charByte = "&amp;lt;".getBytes("UTF-8");
						for (m = 0; m < charByte.length; m++) {
							var[x++] = charByte[m];
						}
					} else if (byteBuff[l] == 62) {
						charByte = "&amp;gt;".getBytes("UTF-8");
						for (m = 0; m < charByte.length; m++) {
							var[x++] = charByte[m];
						}
					} else {
						var[x++] = byteBuff[l];
					}
				} else {
					var[x++] = byteBuff[l];
				}
			}
			charsByte = new byte[x];
			for (m = 0; m < x; m++) {
				charsByte[m] = var[m];
			}
			htmlTA = new String(charsByte, "UTF-8");
			var = null;
			charsByte = null;
			charByte = null;
		} catch (Exception ex) {
			System.out.println("EXCEPTION FROM htmlEnc method :: " + ex);
		}
		return htmlTA;
	}

	public String jsEnc(String jsTA) {
		byte[] byteBuff, var, charByte, charsByte;
		StringBuffer strBuff = new StringBuffer();
		String tempS, pref;
		boolean wasNULL = false, first = true;
		int x, m, l;
		try {
			jsTA = new String(jsTA.getBytes("UTF-8"), "UTF-8");
			byteBuff = jsTA.getBytes("UTF-8");
			var = new byte[byteBuff.length * 6];
			x = 0;
			for (l = 0; l < byteBuff.length; l++) //
			{
				if (byteBuff[l] == 34) {
					charByte = "\\\"".getBytes("UTF-8");
					for (m = 0; m < charByte.length; m++) {
						var[x++] = charByte[m];
					}
				} else if (byteBuff[l] == 39) {
					charByte = "\\\'".getBytes("UTF-8");
					for (m = 0; m < charByte.length; m++) {
						var[x++] = charByte[m];
					}
				} else if (byteBuff[l] == 92) {
					charByte = "\\\\".getBytes("UTF-8");
					for (m = 0; m < charByte.length; m++) {
						var[x++] = charByte[m];
					}
				} else if (byteBuff[l] == 13) {
					// just drop it.
				} else if (byteBuff[l] == 10) {
					charByte = "\\ n".getBytes("UTF-8");
					for (m = 0; m < charByte.length; m += 2) {

						var[x++] = charByte[m];
					}
				} else {
					var[x++] = byteBuff[l];
				}
			}
			charsByte = new byte[x];
			for (m = 0; m < x; m++) {
				charsByte[m] = var[m];
			}
			jsTA = new String(charsByte, "UTF-8");
			jsTA = new String(jsTA.getBytes("UTF-16"), "UTF-16");
			pref = "\\u";
			byteBuff = jsTA.getBytes("UTF-16");
			for (l = 2; l < byteBuff.length; l++) {
				if (byteBuff[l] == 0) {
					wasNULL = true;
				} else if (wasNULL && (byteBuff[l] >= 1 && byteBuff[l] <= 127)) {
					strBuff.append((char) byteBuff[l]);
					wasNULL = false;
				} else if (wasNULL) {
					tempS = byteToHex(byteBuff[l]);
					strBuff.append(pref + "00" + tempS);
					wasNULL = false;
				} else {
					tempS = byteToHex(byteBuff[l]);
					if (first) {
						strBuff.append(pref + tempS);
						first = false;
					} else {
						strBuff.append(tempS);
						first = true;
					}
				}
			}

			jsTA = strBuff.toString();
			strBuff = null;
		} catch (Exception ex) {
			System.out.println("EXCEPTION FROM jsEnc method :: " + ex);
		}
		return jsTA;
	}

	public String cssEnc(String cssTA) {
		byte[] byteBuff, var, charByte, charsByte;
		StringBuffer strBuff = new StringBuffer();
		String tempS, pref;
		boolean wasNULL = false, first = true;
		int x, m, l;
		try {
			cssTA = new String(cssTA.getBytes("UTF-8"), "UTF-8");
			byteBuff = cssTA.getBytes("UTF-8");
			var = new byte[byteBuff.length * 7];
			x = 0;
			for (l = 0; l < byteBuff.length; l++) {
				if (byteBuff[l] == 13) {
					// just drop it.
				} else if (byteBuff[l] == 92) {
					charByte = "\\\\".getBytes("UTF-8");
					for (m = 0; m < charByte.length; m++) {
						var[x++] = charByte[m];
					}
				} else {
					var[x++] = byteBuff[l];
				}
			}
			charsByte = new byte[x];
			for (m = 0; m < x; m++) {
				charsByte[m] = var[m];
			}
			cssTA = new String(charsByte, "UTF-8");
			cssTA = new String(cssTA.getBytes("UTF-16"), "UTF-16");
			pref = "\\";
			byteBuff = cssTA.getBytes("UTF-16");
			for (l = 2; l < byteBuff.length; l++) {
				if (byteBuff[l] == 0) {
					wasNULL = true;
				} else if (wasNULL && (byteBuff[l] >= 1 && byteBuff[l] <= 127) && byteBuff[l] != 10) {
					strBuff.append((char) byteBuff[l]);
					wasNULL = false;
				} else if (wasNULL) {
					tempS = byteToHex(byteBuff[l]);
					if (!tempS.equalsIgnoreCase("0A")) {
						strBuff.append(pref + "00" + tempS);
					} else {
						strBuff.append(pref + "0000" + tempS);
					}
					wasNULL = false;
				} else {
					tempS = byteToHex(byteBuff[l]);
					if (first) {
						strBuff.append(pref + tempS);
						first = false;
					} else {
						strBuff.append(tempS);
						first = true;
					}
				}
			}
			cssTA = strBuff.toString();
			strBuff = null;
		} catch (Exception ex) {
			System.out.println("EXCEPTION FROM cssEnc method :: " + ex);
		}
		return cssTA;
	}

	/*
	 * Converting byte to its hex value.
	 */
	public String byteToHex(byte data) {
		StringBuffer buf = new StringBuffer();
		buf.append(toHexChar((data >>> 4) & 0x0F));
		buf.append(toHexChar(data & 0x0F));
		return (buf.toString()).toUpperCase();
	}

	/*
	 * Converting hex to its character/symbol/number.
	 */
	public char toHexChar(int i) {
		if ((i >= 0) && (i <= 9)) {
			return (char) ('0' + i);
		} else {
			return (char) ('a' + (i - 10));
		}
	}
}
