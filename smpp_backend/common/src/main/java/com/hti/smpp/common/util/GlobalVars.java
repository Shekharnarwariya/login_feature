package com.hti.smpp.common.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import com.hti.smpp.common.contacts.dto.GroupEntry;
import com.hti.smpp.common.contacts.dto.GroupMemberEntry;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.route.dto.HlrRouteEntry;
import com.hti.smpp.common.route.dto.OptionalRouteEntry;
import com.hti.smpp.common.route.dto.RouteEntry;
import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.smsc.dto.SmscEntry;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.DlrSettingEntry;
import com.hti.smpp.common.user.dto.ProfessionEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.util.dto.AlertDTO;

/**
 * The GlobalVars class serves as a repository for various global variables used
 * across the application. It includes settings, distributed maps, and other
 * shared data structures.
 */
public class GlobalVars {
	public static HazelcastInstance hazelInstance;

	// HazelcastInstance representing the distributed Hazelcast cluster.

	public static boolean MASTER_CLIENT = false;
	public static boolean DB_CLUSTER = false;
//	public static IMap<Integer, BatchObject> BatchQueue;
//	public static IMap<Integer, BatchObject> HlrBatchQueue;
	public static IMap<Integer, GroupEntry> SmscGroupEntries;
	public static IMap<String, String> HttpDlrParam;
	public static Map<String, Set<Integer>> ScheduledBatches = new ConcurrentHashMap<String, Set<Integer>>();
	public static Set<Integer> RepeatedSchedules = Collections.synchronizedSet(new HashSet<Integer>());
	public static Map<String, String> ActiveUsers = Collections.synchronizedMap(new HashMap<String, String>());
	public static Map<Integer, AlertDTO> PerformanceAlerts = Collections
			.synchronizedMap(new HashMap<Integer, AlertDTO>());
	public static Map<Integer, SalesEntry> ExecutiveEntryMap = Collections
			.synchronizedMap(new HashMap<Integer, SalesEntry>());
	// ----------------------------------------------------------------------------------------
	// public static Set<String> UsedCurrencies = new TreeSet<String>();
	public static Map<String, String> currencies = new java.util.TreeMap<String, String>();
	public static Set<String> smscTypes = new TreeSet<String>();
	// Distributed maps for storing various entries using Hazelcast IMap.
	// --------------------------------------------------------------

	// Group entries related to SMS centers.

	// public static Set<String> UsedSmscTypes = new TreeSet<String>();
	public static Map<String, String> GmtMapping = Collections.synchronizedMap(new LinkedHashMap<String, String>());
	public static Map<String, Integer> PrefixMapping = Collections.synchronizedMap(new HashMap<String, Integer>());
	public static Map<String, Integer> LoginAttempts = Collections.synchronizedMap(new HashMap<String, Integer>());

	// ************** hazelcast **************************************
	// ---------- network ----------------------------
	public static IMap<Integer, NetworkEntry> NetworkEntries;
	// ---------- smsc -------------------------------
	public static IMap<Integer, SmscEntry> SmscEntries;
	public static MultiMap<Integer, GroupMemberEntry> SmscGroupMember;
	// --------- routing entries ---------------------
	public static IMap<Integer, RouteEntry> BasicRouteEntries;
	public static IMap<Integer, HlrRouteEntry> HlrRouteEntries;
	public static IMap<Integer, OptionalRouteEntry> OptionalRouteEntries;
	// public static MultiMap<Integer, Integer> RouteIdentity;
	// ------ user based entries ---------------------

	public static IMap<String, String> UserFlagStatus;
	public static IMap<Integer, UserEntry> UserEntries;
	public static IMap<Integer, BalanceEntry> BalanceEntries;
	public static IMap<String, Integer> UserMapping;
	public static IMap<Integer, ProfessionEntry> ProfessionEntries;
	public static IMap<Integer, WebMasterEntry> WebmasterEntries;
	public static IMap<Integer, DlrSettingEntry> DlrSettingEntries;

	//public static IMap<Integer,bulkService> bulkService;
	static {
		smscTypes.add("A");
		smscTypes.add("B");
		smscTypes.add("C");
		smscTypes.add("D");
		smscTypes.add("E");
		smscTypes.add("F");
		smscTypes.add("G");
		smscTypes.add("H");
		smscTypes.add("I");
		smscTypes.add("J");
		smscTypes.add("K");
		smscTypes.add("L");
		smscTypes.add("M");
		smscTypes.add("N");
		smscTypes.add("O");
		smscTypes.add("P");
		smscTypes.add("Q");
		smscTypes.add("R");
		smscTypes.add("S");
		smscTypes.add("T");
		smscTypes.add("U");
		smscTypes.add("V");
		smscTypes.add("W");
		smscTypes.add("X");
		smscTypes.add("Y");
		smscTypes.add("Z");
		// ----- gmt -----------
		GmtMapping.put("GMT-12:00#1", "International Date Line West(GMT-12:00)");
		GmtMapping.put("GMT-11:00#1", "Midway Island, Samoa(GMT-11:00)");
		GmtMapping.put("GMT-10:00#1", "Hawaii(GMT-10:00) ");
		GmtMapping.put("GMT-09:00#1", "Alaska(GMT-09:00) ");
		GmtMapping.put("GMT-08:00#1", "Pacific Time (US & Canada)(GMT-08:00) ");
		GmtMapping.put("GMT-08:00#2", "Tijuana Baja California(GMT-08:00) ");
		GmtMapping.put("GMT-07:00#1", "Arizona (GMT-07:00)");
		GmtMapping.put("GMT-07:00#2", "Chihuahua, La paz, Mazatlan(GMT-07:00) ");
		GmtMapping.put("GMT-07:00#3", "Mountain Time(US & Canada)(GMT-07:00) ");
		GmtMapping.put("GMT-06:00#1", "Central America(GMT-06:00)");
		GmtMapping.put("GMT-06:00#2", "Central Time (US & Canada)(GMT-06:00)");
		GmtMapping.put("GMT-06:00#3", "Guadalajara, Mexico City, Monterrey(GMT-06:00)");
		GmtMapping.put("GMT-06:00#4", "Saskatchewan(GMT-06:00)");
		GmtMapping.put("GMT-05:00#1", "Bogota, Lima, Quito, Rio Branco(GMT-05:00)");
		GmtMapping.put("GMT-05:00#2", "Eastern Time (US & Canada)(GMT-05:00)");
		GmtMapping.put("GMT-05:00#3", "Indiana (East)(GMT-05:00)");
		GmtMapping.put("GMT-04:30#1", "Caracas(GMT-04:30)");
		GmtMapping.put("GMT-04:00#1", "Atlentic Time (Canada)(GMT-04:00)");
		GmtMapping.put("GMT-04:00#2", "La Paz(GMT-04:00)");
		GmtMapping.put("GMT-04:00#3", "Manaus(GMT-04:00)");
		GmtMapping.put("GMT-04:00#4", "Santiago(GMT-04:00)");
		GmtMapping.put("GMT-03:30#1", "Newfoundland(GMT-03:30)");
		GmtMapping.put("GMT-03:00#1", "Brasilia(GMT-03:00)");
		GmtMapping.put("GMT-03:00#2", "Buenos Aires, Georgetown(GMT-03:00)");
		GmtMapping.put("GMT-03:00#3", "Greenland(GMT-03:00)");
		GmtMapping.put("GMT-03:00#4", "Montevideo(GMT-03:00)");
		GmtMapping.put("GMT-02:00#1", "mid-Atlantic(GMT-02:00)");
		GmtMapping.put("GMT-01:00#1", "Azores(GMT-01:00)");
		GmtMapping.put("GMT-01:00#2", "Cape Verde Is(GMT-01:00)");
		GmtMapping.put("GMT#1", "Casablanca, Monrovia, Reykjavik(GMT)");
		GmtMapping.put("GMT#2", "Greenwich Mean Time:Bublin,Edinburgh,Lisban,London(GMT)");
		GmtMapping.put("GMT+01:00#1", "Amsterdam, Berlin, Bern, Rome, Stockholm, Vienna(GMT+01:00)");
		GmtMapping.put("GMT+01:00#2", "Belgrade, Bratislava, Budapest, Ljubljana, Prague(GMT+01:00)");
		GmtMapping.put("GMT+01:00#3", "Brassels, Copenhegen, Mardrid, Paris(GMT+01:00)");
		GmtMapping.put("GMT+01:00#4", "Sarajevo, Skopje, Warsaw,Zagreb(GMT+01:00)");
		GmtMapping.put("GMT+01:00#5", "West Central Africa(GMT+01:00)");
		GmtMapping.put("GMT+02:00#1", "Amman(GMT+02:00)");
		GmtMapping.put("GMT+02:00#2", "Athens, Bucharest, Istanbul(GMT+02:00)");
		GmtMapping.put("GMT+02:00#3", "Beirut(GMT+02:00)");
		GmtMapping.put("GMT+02:00#4", "Cairo(GMT+02:00)");
		GmtMapping.put("GMT+02:00#5", "Harare, Pretoria(GMT+02:00)");
		GmtMapping.put("GMT+02:00#6", "Helsinki, Kyiv, Riga, Sofia, Tallinn, Vilnius(GMT+02:00)");
		GmtMapping.put("GMT+02:00#7", "Jerusalem(GMT+02:00)");
		GmtMapping.put("GMT+02:00#8", "Minsk(GMT+02:00)");
		GmtMapping.put("GMT+02:00#9", "Windhoek(GMT+02:00)");
		GmtMapping.put("GMT+03:00#1", "Baghdad(GMT+03:00)");
		GmtMapping.put("GMT+03:00#2", "Kuwait, Riyadh(GMT+03:00)");
		GmtMapping.put("GMT+03:00#3", "Moscow, St. Petersburg, Volgograd(GMT+03:00)");
		GmtMapping.put("GMT+03:00#4", "Nairobi(GMT+03:00)");
		GmtMapping.put("GMT+03:00#5", "Tbilisi(GMT+03:00)");
		GmtMapping.put("GMT+03:30#1", "Tehran(GMT+03:30)");
		GmtMapping.put("GMT+04:00#1", "Abu Dhabi, Muscat(GMT+04:00)");
		GmtMapping.put("GMT+04:00#2", "Baku(GMT+04:00)");
		GmtMapping.put("GMT+04:00#3", "Yerevan(GMT+04:00)");
		GmtMapping.put("GMT+04:30#1", "Kabul(GMT+04:30)");
		GmtMapping.put("GMT+05:00#1", "Ekaterinburg(GMT+05:00)");
		GmtMapping.put("GMT+05:00#2", "Islamabad, Karachi, Tashkent(GMT+05:00)");
		GmtMapping.put("GMT+05:30#1", "Chennai, Kolkata, Mumbai, New Delhi(GMT+05:30)");
		GmtMapping.put("GMT+05:30#2", "Sri Jayawardenepura(GMT+05:30)");
		GmtMapping.put("GMT+05:45#1", "Kathmandu(GMT+05:45)");
		GmtMapping.put("GMT+06:00#1", "Almaty, Novosibirsk(GMT+06:00)");
		GmtMapping.put("GMT+06:00#2", "Astana, Dhaka(GMT+06:00)");
		GmtMapping.put("GMT+06:30#1", "Yangon (Ranfoon)(GMT+06:30)");
		GmtMapping.put("GMT+07:00#1", "Bangkok, Hanoi, Jakarta(GMT+07:00)");
		GmtMapping.put("GMT+07:00#2", "Krasnoyarsk(GMT+07:00)");
		GmtMapping.put("GMT+08:00#1", "Beijing, Chongqing, Hong Kong, Urumqi(GMT+08:00)");
		GmtMapping.put("GMT+08:00#2", "Irkutsk, Ulaan, Bataar(GMT+08:00)");
		GmtMapping.put("GMT+08:00#3", "Kuala Lumpur, Singapore(GMT+08:00)");
		GmtMapping.put("GMT+08:00#4", "Perth(GMT+08:00)");
		GmtMapping.put("GMT+08:00#5", "Taipei(GMT+08:00)");
		GmtMapping.put("GMT+09:00#1", "Osaka, Sapporo, Tokyo(GMT+09:00) ");
		GmtMapping.put("GMT+09:00#2", "Seoul(GMT+09:00) ");
		GmtMapping.put("GMT+09:00#3", "Yakutsk(GMT+09:00) ");
		GmtMapping.put("GMT+09:30#1", "Adelaide(GMT+09:30)");
		GmtMapping.put("GMT+09:30#2", "Darwin(GMT+09:30)");
		GmtMapping.put("GMT+10:00#1", "Brisbane(GMT+10:00)");
		GmtMapping.put("GMT+10:00#2", "Canberra, Melbourne, Sydney(GMT+10:00)");
		GmtMapping.put("GMT+10:00#3", "Guam, Port Moresby(GMT+10:00)");
		GmtMapping.put("GMT+10:00#4", "Hobert(GMT+10:00)");
		GmtMapping.put("GMT+10:00#5", "Viadivostok(GMT+10:00)");
		GmtMapping.put("GMT+11:00#1", "Magadan, Soloman Is., New Caledonia(GMT+11:00)");
		GmtMapping.put("GMT+12:00#1", "Auckland, Wellington(GMT+12:00)");
		GmtMapping.put("GMT+12:00#2", "Fiji, Kamchatka, Marshall Is.(GMT+12:00)");
		GmtMapping.put("GMT+13:00#1", "Nuku'alofa(GMT+13:00)");
	}
}
