/*
 * SmscStatus.java
 *
 * Created on 06 April 2004, 14:14
 */
package com.hti.util;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArraySet;

import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;

import com.hazelcast.map.IMap;
import com.hti.database.ConnectionPool;
import com.hti.objects.LogPDU;
import com.hti.objects.PriorityQueue;
import com.hti.objects.RoutePDU;
import com.hti.objects.SmscLimit;
import com.hti.smsc.SpecialSMSCSetting;
import com.hti.smsc.dto.GroupEntry;
import com.hti.smsc.dto.SmscLooping;
import com.hti.thread.UserWiseMis;
import com.hti.user.dto.BalanceEntry;
import com.hti.user.dto.UserEntry;

/**
 *
 * 
 * @author administrator
 */
public class GlobalCache {
	public static ConnectionPool connnection_pool_1 = null;
	public static ConnectionPool connnection_pool_2 = null;
	// ---------- ehcache variables -----------
	public static PersistentCacheManager cacheManager = null;
	public static CacheConfigurationBuilder resp_config_Builder = null;
	public static CacheConfigurationBuilder resp_dlr_config_Builder = null;
	public static CacheConfigurationBuilder queue_config_Builder = null;
	// public static CacheConfigurationBuilder report_config_Builder = null;
	public static CacheConfigurationBuilder resend_config_Builder = null;
	// public static Cache<String, ReportLogObject> ReportCache = null; // for reporting
	public static Cache<String, LogPDU> ResponseLogCache = null; // log pdu map for submit response
	public static Cache<String, LogPDU> ResponseLogDlrCache = null; // log pdu map for deliver response
	public static Cache<String, PriorityQueue> SmscQueueCache = null; // pdu Queue for smsc Submission
	public static Cache<String, RoutePDU> ResendPDUCache = null; // pdu cache for enforce/signaling resend
	public static Cache<String, HashSet<String>> PartMappingForDlr = null; // Part description cache to create dlr for parts
	// -------------------------------------------------------------------
	public static Map<String, Integer> GroupWiseRepeatedNumbers = null;
	public static IMap<Integer, GroupEntry> SmscGroupEntries = null;
	public static Map<String, UserWiseMis> UserMisQueueObject = Collections
			.synchronizedMap(new HashMap<String, UserWiseMis>());
	// public static final Map<String, String> CountryMap = Collections.synchronizedMap(new HashMap<String, String>());
	public static Set<String> enforcedlist = new HashSet<String>();
	// ------------ ESME ERROR -----------------------
	public static Map<String, String> EsmeErrorCode = Collections.synchronizedMap(new HashMap<String, String>()); // Predefined Submit Error Codes to
																													// be resend if needed
	public static Map<String, String> EsmeErrorFlag = Collections.synchronizedMap(new HashMap<String, String>()); // Predefined Submit Error Flag to
																													// be resend if needed
	// public static Map<String, Map<String, Integer>> EsmeErrorResponse = Collections
	// .synchronizedMap(new HashMap<String, Map<String, Integer>>()); // Smsc Wise Submit Error Counter
	// ------------------ Signaling -----------------------------
	public static Map<String, Integer[]> SignalingErrorCriteria = Collections
			.synchronizedMap(new HashMap<String, Integer[]>());
	// ---------- Performance ------------------------
	public static Map<String, Set<String>> WorstDeliveryRoute = Collections
			.synchronizedMap(new HashMap<String, Set<String>>()); // Worst performance route as Key with network set as value
	public static Map<String, Set<String>> WorstResponseRoute = Collections
			.synchronizedMap(new HashMap<String, Set<String>>()); // Worst performance route as Key with network set as value
	// -------------- Other -------------------------------
	// public static Map<Integer, String> ExistRoutes = Collections.synchronizedMap(new HashMap<Integer, String>());
	public static Hashtable<String, Boolean> SMSCConnectionStatus = new Hashtable<String, Boolean>();
	// public static Hashtable<String, String> SmscConnectionIp = new Hashtable<String, String>();
	public static Set<String> SmscConnectionSet = Collections.synchronizedSet(new HashSet<String>());
	public static Map<String, Long> SmscDisconnection = Collections.synchronizedMap(new HashMap<String, Long>());
	public static Set<String> SmscDisconnectionAlert = Collections.synchronizedSet(new HashSet<String>());
	// public static Hashtable<String, Properties> SMSCtextHash = new Hashtable<String, Properties>(); // Added by Amit_vish
	// public static Set<String> ActiveRoutes = Collections.synchronizedSet(new HashSet<String>());
	// public static Map<String, Boolean> HexConvertSmscList = Collections.synchronizedMap(new HashMap<String, Boolean>());
	// public static Hashtable<String, String> ENFORCE_SMSC_Cache = new Hashtable<String, String>();
	public static Map<String, Date> SmscSubmitTime = Collections.synchronizedMap(new HashMap<String, Date>());
	// public static Map<String, Integer> SmscWiseNonRespond = Collections.synchronizedMap(new HashMap<String, Integer>());
	// public static Map<String, Integer> UserWiseNonRespond = Collections.synchronizedMap(new HashMap<String, Integer>());
	// public static Map<String, String> SubmittedMessageId = Collections.synchronizedMap(new HashMap<String, String>());
	public static Hashtable<String, String> SmscSenderId = new Hashtable<String, String>();// done by ashish 061206
	public static Hashtable<String, String> SenderIdList = new Hashtable<String, String>();
	// public static Set<String> smscEnforceDLR = Collections.synchronizedSet(new HashSet<String>());
	public static Set<String> smscGreekEncodeApply = new CopyOnWriteArraySet<String>();
	public static Map<String, Integer> DestinationSleepApply = Collections
			.synchronizedMap(new HashMap<String, Integer>());
	public static Map<String, String> CustomDlrTime = Collections.synchronizedMap(new HashMap<String, String>());
	public static Map<String, Integer> DelayedDlrRoute = Collections.synchronizedMap(new HashMap<String, Integer>());
	public static Map<RoutePDU, Long> DelaySubmition = Collections.synchronizedMap(new HashMap<RoutePDU, Long>());
	//public static Hashtable<String, SmscLooping> smscLoopApplyFlag = new Hashtable<String, SmscLooping>();
	public static Map<String, Integer> SmscSessionId = Collections.synchronizedMap(new HashMap<String, Integer>());
	public static Map<Integer, Set<String>> SessionIdSmscList = Collections
			.synchronizedMap(new HashMap<Integer, Set<String>>());
	// public static Hashtable<String, Set<String>> c_markedQueue = new Hashtable<String, Set<String>>();
	public static Vector<String> recievedDlrResponseId = new Vector<String>();
	public static Hashtable<String, SpecialSMSCSetting> specailSMSCsetting = new Hashtable<String, SpecialSMSCSetting>();
	public static Map<String, String> nonResponding = Collections.synchronizedMap(new HashMap<String, String>());
	public static Map<String, Set<String>> SmscBasedBSFM = Collections.synchronizedMap(new HashMap<String, Set<String>>());
	public static Map<String, Calendar> SmscLastDlrRecieve = Collections
			.synchronizedMap(new HashMap<String, Calendar>());
	public static Map<String, Map<Integer, String>> smscwisesequencemap = Collections
			.synchronizedMap(new HashMap<String, Map<Integer, String>>());
	public static Map<Integer, Map<String, String>> smscwiseResponseMap = Collections
			.synchronizedMap(new HashMap<Integer, Map<String, String>>());
	// public static Map<Integer, Boolean> ehCacheConfig = new HashMap<Integer, Boolean>();
	public static Map<String, String> SpecialEncoding = Collections.synchronizedMap(new HashMap<String, String>());
	public static Map<String, SmscLimit> SmscSubmitLimit = Collections
			.synchronizedMap(new HashMap<String, SmscLimit>());
	public static Set<Integer> SmscSubmitLimitNotified = new CopyOnWriteArraySet<Integer>();
	public static Map<Integer, Timer> SubmitLimitResetTime = Collections.synchronizedMap(new HashMap<Integer, Timer>());
	public static Map<String, List<RoutePDU>> WaitingForDeliveredPart = Collections
			.synchronizedMap(new HashMap<String, List<RoutePDU>>());
	public static Map<Integer, Integer> SmscSubmitCounter;
	public static Map<String, String> SmscPrioritySender = Collections.synchronizedMap(new HashMap<String, String>());
	public static Map<String, SmscLooping> SmscLoopingRules = Collections
			.synchronizedMap(new HashMap<String, SmscLooping>());
	public static Set<String> SkipLoopingUsers = new CopyOnWriteArraySet<String>();
	public static Map<String, String> LoopingRoutes = Collections
			.synchronizedMap(new HashMap<String, String>());
	public static Map<String, com.hti.objects.SubmittedObj> DndSourceMsgId = Collections
			.synchronizedMap(new HashMap<String, com.hti.objects.SubmittedObj>());
	// public static Map<String, SmscConnection> SmscConnections = Collections
	// .synchronizedMap(new HashMap<String, SmscConnection>());
	// ************** hazelcast **************************************
	// public static Map<String, NetworkEntry> NetworkEntries;
	// public static IMap<Integer, SmscEntry> SmscEntries;
	// public static Map<Integer, String> SmscNameMapping;
	// public static MultiMap<Integer, GroupMemberEntry> SmscGrouping;
	public static IMap<Integer, BalanceEntry> BalanceEntries;
	public static IMap<Integer, UserEntry> UserEntries;
	public static IMap<String, Integer> SystemIdMapping;

	public GlobalCache() {
	}
}
