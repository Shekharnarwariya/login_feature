/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.smpp.common.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.route.dto.HlrRouteEntry;
import com.hti.smpp.common.route.dto.OptionalRouteEntry;
import com.hti.smpp.common.session.SessionHandler;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;

@Component
public class GlobalVarsSms {
	public static Map<String, SessionHandler> UserSessionHandler = Collections
			.synchronizedMap(new HashMap<String, SessionHandler>());

	public static Map<String, Set<Integer>> ScheduledBatches = new ConcurrentHashMap<String, Set<Integer>>();

	public static Set<Integer> RepeatedSchedules = Collections.synchronizedSet(new HashSet<Integer>());

	public static Map<Integer, UserEntry> UserEntries;

	public static Map<Integer, WebMasterEntry> WebmasterEntries;

	public static Map<Integer, NetworkEntry> NetworkEntries;

	public static Map<Integer, HlrRouteEntry> HlrRouteEntries;

	public static Map<Integer, OptionalRouteEntry> OptionalRouteEntries;

	public static Map<String, Integer> PrefixMapping = Collections.synchronizedMap(new HashMap<String, Integer>());

	public static Map<String, String> hashTabOne = new HashMap<String, String>();

	static {
		hashTabOne.put("A", "41");
		hashTabOne.put("B", "42");
		hashTabOne.put("C", "43");
		hashTabOne.put("D", "44");
		hashTabOne.put("E", "45");
		hashTabOne.put("F", "46");
		hashTabOne.put("G", "47");
		hashTabOne.put("H", "48");
		hashTabOne.put("I", "49");
		hashTabOne.put("J", "4A");
		hashTabOne.put("K", "4B");
		hashTabOne.put("L", "4C");
		hashTabOne.put("M", "4D");
		hashTabOne.put("N", "4E");
		hashTabOne.put("O", "4F");
		hashTabOne.put("P", "50");
		hashTabOne.put("Q", "51");
		hashTabOne.put("R", "52");
		hashTabOne.put("S", "53");
		hashTabOne.put("T", "54");
		hashTabOne.put("U", "55");
		hashTabOne.put("V", "56");
		hashTabOne.put("W", "57");
		hashTabOne.put("X", "58");
		hashTabOne.put("Y", "59");
		hashTabOne.put("Z", "5A");
		hashTabOne.put("a", "61");
		hashTabOne.put("b", "62");
		hashTabOne.put("c", "63");
		hashTabOne.put("d", "64");
		hashTabOne.put("e", "65");
		hashTabOne.put("f", "66");
		hashTabOne.put("g", "67");
		hashTabOne.put("h", "68");
		hashTabOne.put("i", "69");
		hashTabOne.put("j", "6A");
		hashTabOne.put("k", "6B");
		hashTabOne.put("l", "6C");
		hashTabOne.put("m", "6D");
		hashTabOne.put("n", "6E");
		hashTabOne.put("o", "6F");
		hashTabOne.put("p", "70");
		hashTabOne.put("q", "71");
		hashTabOne.put("r", "72");
		hashTabOne.put("s", "73");
		hashTabOne.put("t", "74");
		hashTabOne.put("u", "75");
		hashTabOne.put("v", "76");
		hashTabOne.put("w", "77");
		hashTabOne.put("x", "78");
		hashTabOne.put("y", "79");
		hashTabOne.put("z", "7A");
		hashTabOne.put("0", "30");
		hashTabOne.put("1", "31");
		hashTabOne.put("2", "32");
		hashTabOne.put("3", "33");
		hashTabOne.put("4", "34");
		hashTabOne.put("5", "35");
		hashTabOne.put("6", "36");
		hashTabOne.put("7", "37");
		hashTabOne.put("8", "38");
		hashTabOne.put("9", "39");
		hashTabOne.put("~", "1B3D"); // hashTabOne.put("\\", "1B2E");
		hashTabOne.put("|", "1B40");
		hashTabOne.put("]", "1B3E");
		hashTabOne.put("[", "1B3C");
		hashTabOne.put("}", "1B29");
		hashTabOne.put("{", "1B28");
		hashTabOne.put("^", "1B14");
		hashTabOne.put("@", "00");
		hashTabOne.put("$", "02");
		hashTabOne.put("_", "11");
		hashTabOne.put("!", "21");
		hashTabOne.put("\"", "22");
		hashTabOne.put("#", "23");
		hashTabOne.put("%", "25");
		hashTabOne.put("&", "26");
		hashTabOne.put("'", "27");
		hashTabOne.put("(", "28");
		hashTabOne.put(")", "29");
		hashTabOne.put("\n", "0A");
		hashTabOne.put("*", "2A");
		hashTabOne.put("+", "2B");
		hashTabOne.put(",", "2C");
		hashTabOne.put("-", "2D");
		hashTabOne.put(".", "2E");
		hashTabOne.put("/", "2F");
		hashTabOne.put(":", "3A");
		hashTabOne.put(";", "3B");
		hashTabOne.put("<", "3C");
		hashTabOne.put("=", "3D");
		hashTabOne.put(">", "3E");
		hashTabOne.put("?", "3F");
		

	}

}