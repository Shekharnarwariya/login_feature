/*
 * GlobalAppVars.java
 *
 * Created on 08 April 2004, 17:24
 */
package com.hti.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import com.hti.database.ConnectionPool;
import com.hti.database.LogDBConnection;
import com.hti.objects.HTIQueue;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smsc.dto.GroupMemberEntry;
import com.hti.smsc.dto.SmscEntry;
import com.hti.thread.UserWiseContent;
import com.hti.user.RoutingThread;
import com.hti.user.SessionManager;
import com.hti.user.UserBalance;
import com.hti.user.UserDeliverForward;
import com.hti.user.WebDeliverProcess;
import com.hti.user.dto.BalanceEntry;
import com.hti.user.dto.DlrSettingEntry;
import com.hti.user.dto.ProfessionEntry;
import com.hti.user.dto.UserEntry;

/**
 * @author administrator
 */
public class GlobalCache {
	public static ConnectionPool connection_pool_user;
	public static ConnectionPool connection_pool_proc;
	public static LogDBConnection logConnectionPool;
	public static Map<String, HTIQueue> UserDeliverProcessQueue = Collections
			.synchronizedMap(new HashMap<String, HTIQueue>());
	public static Map<String, HTIQueue> WebDeliverProcessQueue = Collections
			.synchronizedMap(new HashMap<String, HTIQueue>());
	// public static final Map<String, Integer> Network = Collections.synchronizedMap(new HashMap<String, Integer>());
	// public static final Map<String, String> NNC = Collections.synchronizedMap(new HashMap<String, String>());
	// public static final Map<String, Integer> MCC_MNC = Collections.synchronizedMap(new HashMap<String, Integer>());
	public static Map<String, UserDeliverForward> UserRxObject = Collections
			.synchronizedMap(new HashMap<String, UserDeliverForward>());
	public static Map<String, WebDeliverProcess> UserWebObject = Collections
			.synchronizedMap(new HashMap<String, WebDeliverProcess>());
	public static Map<Integer, UserBalance> UserBalanceObject = Collections
			.synchronizedMap(new HashMap<Integer, UserBalance>());
	public static Map<String, SessionManager> UserSessionObject = Collections
			.synchronizedMap(new HashMap<String, SessionManager>());
	public static Map<String, RoutingThread> UserRoutingThread = Collections
			.synchronizedMap(new HashMap<String, RoutingThread>());
	// public static Map<String, UserDTO> UserObject = Collections.synchronizedMap(new HashMap<String, UserDTO>());
	// public static Map<String, Map<Integer, RoutingDTO>> UserRouting = Collections
	// .synchronizedMap(new HashMap<String, Map<Integer, RoutingDTO>>());
	public static Set<String> LowBalanceUser = Collections.synchronizedSet(new HashSet<String>());
	public static Set<String> BlockedUser = Collections.synchronizedSet(new HashSet<String>());
	public static Set<String> LongBrokenExpireRoute = Collections.synchronizedSet(new HashSet<String>());
	public static Map<String, Set<String>> SkipHlrSenderRoute = Collections
			.synchronizedMap(new HashMap<String, Set<String>>());
	// public static Map<String, DLRConfiguration> DLRSetting = Collections
	// .synchronizedMap(new HashMap<String, DLRConfiguration>()); // Deliver_sm setting for each user
	public static Map<String, UserWiseContent> UserContentQueueObject = Collections
			.synchronizedMap(new HashMap<String, UserWiseContent>());
	public static Map<String, Map<String, String>> SmscBasedReplacement = Collections
			.synchronizedMap(new HashMap<String, Map<String, String>>());
	public static Map<String, Integer> NncMapping = new ConcurrentHashMap<String, Integer>();
	public static Map<String, Integer> PrefixMapping = new ConcurrentHashMap<String, Integer>();
	public static Map<Integer, Set<String>> IdPrefixMapping = new ConcurrentHashMap<Integer, Set<String>>();
	public static Map<String, Long> UserDisconnectionAlert = new ConcurrentHashMap<String, Long>();
	public static Map<Integer, Map<String, Boolean>> NetworkBsfm = new ConcurrentHashMap<Integer, Map<String, Boolean>>();
	public static Set<Integer> UserBasedBsfm = Collections.synchronizedSet(new HashSet<Integer>());
	public static Map<String, Boolean> ContentWebLinks = new ConcurrentHashMap<String, Boolean>();
	public static Map<String, com.hti.user.dto.SubmitLimitEntry> UserSubmitLimitEntries = new ConcurrentHashMap<String, com.hti.user.dto.SubmitLimitEntry>();
	public static Set<Integer> SubmitLimitNotified = Collections.synchronizedSet(new HashSet<Integer>());
	public static Map<String, Set<Long>> OptOutFilter = new ConcurrentHashMap<String, Set<Long>>();
	public static Map<String, String> UnicodeReplacement = Collections.synchronizedMap(new HashMap<String, String>());
	// ---------- ehcache variables -----------
	// public static PersistentCacheManager cacheManager = null;
	// public static CacheConfigurationBuilder<String, HashMap> route_config_Builder = null;
	// public static Cache<String, HashMap> RoutingCache = null;
	// ************** hazelcast **************************************
	public static Map<String, NetworkEntry> NetworkEntries;
	public static IMap<Integer, SmscEntry> SmscEntries;
	public static IMap<String, Integer> SmscNameMapping;
	// public static IMap<String, String> SmscFlagStatus;
	public static MultiMap<Integer, GroupMemberEntry> SmscGroupMember;
	public static IMap<String, String> HttpDlrParam;
	// --------- user based cache --------------------
	public static IMap<String, Integer> SystemIdMapping;
	// public static Map<String, String> UserFlagStatus;
	public static IMap<Integer, UserEntry> UserEntries;
	public static IMap<Integer, BalanceEntry> BalanceEntries;
	public static IMap<Integer, DlrSettingEntry> DlrSettingEntries;
	public static IMap<Integer, ProfessionEntry> ProfessionEntries;
	public static IMap<String, Map<Long, Integer>> UserSubmitCounter;

	public GlobalCache() {
	}
}
