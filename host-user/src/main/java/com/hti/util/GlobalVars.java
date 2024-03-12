package com.hti.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.hazelcast.core.HazelcastInstance;
import com.hti.dao.NetworkDAService;
import com.hti.dao.RouteDAService;
import com.hti.dao.SmscDAService;
import com.hti.dao.UserDAService;

public class GlobalVars {
	public static HazelcastInstance hazelInstance;
	public static SmscDAService smscService;
	public static NetworkDAService networkService;
	public static UserDAService userService;
	public static RouteDAService routeService;
	public static int SERVER_ID = 0;
	public static boolean DB_CLUSTER = false;
	public static boolean MASTER_CLIENT = false;
	public static boolean APPLICATION_STATUS = false;
	public static boolean SMPP_STATUS = false;
	public static boolean RELOAD_DGM_CONFIG = false;
	public static boolean RELOAD_NETWORK_CONFIG = false;
	public static boolean RELOAD_SMSC_CONFIG = false;
	public static boolean RELOAD_USER_CONFIG = false;
	public static boolean RELOAD_BSFM_CONFIG = false;
	public static boolean RELOAD_USER_BSFM_CONFIG = false;
	public static boolean RELOAD_USER_LIMIT_CONFIG = false;
	public static boolean RELOAD_NETWORK_BSFM_CONFIG = false;
	public static boolean RELOAD_DLT_CONFIG = false;
	public static boolean RELOAD_TW_FILTER = false;
	public static boolean RELOAD_UNICODE_ENCODING = false;
	public static boolean DISTRIBUTION = true;
	public static boolean OMQ_PDU_STATUS = false;
	public static boolean HOLD_ON_TRAFFIC = false;
	public static boolean OMQ_DLR_STATUS = false;
	public static int NoOfSessionAllowed = 30;
	private static int INCREMNT_NUMBER = 100;
	public static int sessionId = 1;
	public static int MIN_DESTINATION_LENGTH = 9;
	public static int RECEIVER_QUEUE_SIZE = 1000;
	public static String INVALID_DEST_SMSC = "test";
	public static String REJECT_SMSC = "GW997";
	public static String DELIVRD_SMSC = "GW998";
	public static String UNDELIV_SMSC = "GW999";
	public static int FIX_LONG_WAIT_TIME = 10;
	public static int URL_CHECK_WAIT_TIME = 1000; // ms
	// ------------ Database ------------------------------------
	public static String DB_URL = null;
	public static String LOG_DB_URL = null;
	public static String JDBC_DRIVER = null;
	public static String DB_USER = null;
	public static String DB_PASSWORD = null;
	public static String ALTER_DB_USER = null;
	public static String ALTER_DB_PASSWORD = null;
	public static int MAX_CONNECTIONS;

	public static synchronized String assignMessageId() {
		if (++INCREMNT_NUMBER > 999) {
			INCREMNT_NUMBER = 100;
		}
		return new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date()) + "" + INCREMNT_NUMBER + "" + SERVER_ID;
	}

	public static synchronized String getSessionId() {
		sessionId++;
		return Integer.toString(sessionId);
	}
}
