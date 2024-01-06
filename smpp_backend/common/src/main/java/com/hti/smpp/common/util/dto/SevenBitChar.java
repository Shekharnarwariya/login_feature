package com.hti.smpp.common.util.dto;

import java.util.HashMap;
import java.util.Map;

public class SevenBitChar {
	static Map hashTabOne = new HashMap();
	static {
		hashTabOne.put("65", "41");
		hashTabOne.put("66", "42");
		hashTabOne.put("67", "43");
		hashTabOne.put("68", "44");
		hashTabOne.put("69", "45");
		hashTabOne.put("70", "46");
		hashTabOne.put("71", "47");
		hashTabOne.put("72", "48");
		hashTabOne.put("73", "49");
		hashTabOne.put("74", "4A");
		hashTabOne.put("75", "4B");
		hashTabOne.put("76", "4C");
		hashTabOne.put("77", "4D");
		hashTabOne.put("78", "4E");
		hashTabOne.put("79", "4F");
		hashTabOne.put("80", "50");
		hashTabOne.put("81", "51");
		hashTabOne.put("82", "52");
		hashTabOne.put("83", "53");
		hashTabOne.put("84", "54");
		hashTabOne.put("85", "55");
		hashTabOne.put("86", "56");
		hashTabOne.put("87", "57");
		hashTabOne.put("88", "58");
		hashTabOne.put("89", "59");
		hashTabOne.put("90", "5A");
		hashTabOne.put("97", "61");
		hashTabOne.put("98", "62");
		hashTabOne.put("99", "63");
		hashTabOne.put("100", "64");
		hashTabOne.put("101", "65");
		hashTabOne.put("102", "66");
		hashTabOne.put("103", "67");
		hashTabOne.put("104", "68");
		hashTabOne.put("105", "69");
		hashTabOne.put("106", "6A");
		hashTabOne.put("107", "6B");
		hashTabOne.put("108", "6C");
		hashTabOne.put("109", "6D");
		hashTabOne.put("110", "6E");
		hashTabOne.put("111", "6F");
		hashTabOne.put("112", "70");
		hashTabOne.put("113", "71");
		hashTabOne.put("114", "72");
		hashTabOne.put("115", "73");
		hashTabOne.put("116", "74");
		hashTabOne.put("117", "75");
		hashTabOne.put("118", "76");
		hashTabOne.put("119", "77");
		hashTabOne.put("120", "78");
		hashTabOne.put("121", "79");
		hashTabOne.put("122", "7A");
		hashTabOne.put("48", "30");
		hashTabOne.put("49", "31");
		hashTabOne.put("50", "32");
		hashTabOne.put("51", "33");
		hashTabOne.put("52", "34");
		hashTabOne.put("53", "35");
		hashTabOne.put("54", "36");
		hashTabOne.put("55", "37");
		hashTabOne.put("56", "38");
		hashTabOne.put("57", "39");
		hashTabOne.put("27", "1B");
		hashTabOne.put("12", "1B0A");
		hashTabOne.put("94", "1B14");
		hashTabOne.put("123", "1B28");
		hashTabOne.put("125", "1B29");
		hashTabOne.put("92", "1B2F");
		hashTabOne.put("91", "1B3C");
		hashTabOne.put("126", "1B3D");
		hashTabOne.put("93", "1B3E");
		hashTabOne.put("124", "1B40");
		hashTabOne.put("8364", "1B65");
		hashTabOne.put("37", "25");
		hashTabOne.put("38", "26");
		hashTabOne.put("39", "27");
		hashTabOne.put("40", "28");
		hashTabOne.put("41", "29");
		hashTabOne.put("42", "2A");
		hashTabOne.put("43", "2B");
		hashTabOne.put("44", "2C");
		hashTabOne.put("45", "2D");
		hashTabOne.put("46", "2E");
		hashTabOne.put("47", "2F");
		hashTabOne.put("58", "3A");
		hashTabOne.put("59", "3B");
		hashTabOne.put("60", "3C");
		hashTabOne.put("61", "3D");
		hashTabOne.put("62", "3E");
		hashTabOne.put("63", "3F");
		hashTabOne.put("161", "40");//
		hashTabOne.put("228", "7B");//
		hashTabOne.put("246", "7C");//
		hashTabOne.put("241", "7D");//
		hashTabOne.put("252", "7E");//
		hashTabOne.put("224", "7F");//
		hashTabOne.put("196", "5B");//
		hashTabOne.put("214", "5C");//
		hashTabOne.put("209", "5D");//
		hashTabOne.put("220", "5E");//
		hashTabOne.put("167", "5F");//
		hashTabOne.put("191", "60");
		hashTabOne.put("64", "00"); // @
		hashTabOne.put("163", "01");//
		hashTabOne.put("36", "02");//
		hashTabOne.put("165", "03");
		hashTabOne.put("232", "04");
		hashTabOne.put("233", "05");
		hashTabOne.put("249", "06");
		hashTabOne.put("236", "07");
		hashTabOne.put("242", "08");
		hashTabOne.put("199", "09");
		hashTabOne.put("216", "0B");
		hashTabOne.put("248", "0C");
		hashTabOne.put("13", "0D");
		hashTabOne.put("197", "0E");
		hashTabOne.put("229", "0F");
		hashTabOne.put("95", "11");
		hashTabOne.put("198", "1C");
		hashTabOne.put("230", "1D");
		hashTabOne.put("223", "1E");
		hashTabOne.put("201", "1F");
		hashTabOne.put("10", "0A");
		hashTabOne.put("20", "32");
		hashTabOne.put("916", "10");
		hashTabOne.put("934", "12");
		hashTabOne.put("915", "13");// Correct Encoding
		hashTabOne.put("923", "14");
		hashTabOne.put("937", "15");// Correct Encoding
		hashTabOne.put("928", "16");// Correct Encoding
		hashTabOne.put("936", "17");// Correct Encoding
		hashTabOne.put("931", "18");
		hashTabOne.put("920", "19");
		hashTabOne.put("926", "1A");
		hashTabOne.put("33", "21");
		hashTabOne.put("34", "22");
		hashTabOne.put("35", "23");
		hashTabOne.put("164", "24");
		hashTabOne.put("32", "20");
	}

	public static String getHexValue(String msg) {
		String HexMessage = "";
		char[] arr = msg.toCharArray();
		String ascii = "";
		for (int i = 0; i < arr.length; i++) {
			// System.out.println("char : "+i);
			if (arr[i] != ',') {
				ascii += "" + arr[i];
			} else if (arr[i] == ',' || arr[i] == ' ') {
				// System.out.println("ascii : "+ascii);
				String hexv = (String) hashTabOne.get((ascii.trim()));
				// System.out.println("hex : "+hexv);
				if (hexv != null) {
					HexMessage += hexv;
				} else {
					HexMessage += "";
				}
				ascii = "";
			}
		}
		return HexMessage;
	}
	/*
	 * public static String getHexValue(String msg) { String HexMessage = ""; char[] arr = msg.toCharArray(); String ascii = ""; for (int i = 0; i < arr.length; i++) {
	 * 
	 * // System.out.println("char : "+i); if (arr[i] != ',') { ascii += "" + arr[i]; } else if (arr[i] == ',' || arr[i] == ' ') { //System.out.println("ascii : "+ascii); String hexv = (String)
	 * hashTabOne.get((ascii.trim())); System.out.println("hex : "+hexv); if (hexv != null) { HexMessage += hexv; } else { HexMessage += ""; }
	 * 
	 * ascii = ""; }
	 * 
	 * }
	 * 
	 * return HexMessage; }
	 */
}
