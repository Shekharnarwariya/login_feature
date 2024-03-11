package com.hti.util;

public class Constants {
	public static int noofqueue = 3;
	public static int interEnquireTime = 15;
	public static int DCS;
	public static boolean OMQ_DELIVER_STATUS = false;
	public static boolean USER_HOST_STATUS = false;
	// public static boolean REPORT_STATUS = true;
	public static boolean APPLICATION_STATUS = false;
	public static boolean PROCESSING_STATUS = false;
	public static boolean RELOAD_DGM_CONFIG = false;
	public static boolean RELOAD_DLT_CONFIG = false;
	public static boolean RELOAD_SMSC_CONFIG = false;
	public static boolean RELOAD_SMSC_LT_CONFIG = false;
	public static boolean RELOAD_SMSC_LOOP_CONFIG = false;
	// public static boolean RELOAD_SMSC_SH_CONFIG = false;
	public static boolean RELOAD_USER_CONFIG = false;
	public static boolean RELOAD_ESME_ERROR_CONFIG = false;
	public static boolean RELOAD_SIGNAL_ERROR_CONFIG = false;
	public static boolean RELOAD_SMSC_SPCL_SETTING = false;
	public static boolean RELOAD_SMSC_BSFM = false;
	public static boolean RELOAD_SPCL_ENCODING = false;
	public static boolean RELOAD_TW_FILTER = false;
	public static boolean DISTRIBUTION = true;
	// public volatile static int totalDownCount;
	// public volatile static int totalNonRespond;
	public static int SMSC_QUEUE_LIMIT = 10000;
	public static int MIN_QUEUE_LEVEL = 14;
	public static String INIT_PARAM = "INIT_HTI";
	public static String NON_RESP_WAIT_TIME = "04";
	public static String SERVER_NAME = "GWX";
	// Looping Dump Smsc
	public static String L_DUMP_SMSC = "L_DUMP";
	public static String BSFM_DUMP_SMSC = "B_DUMP";
	public static String DLT_UNDELIV_SMSC = "GW_DLT";
	public static String TW_DUMP_SMSC = "GW_TWDUMP";
	public static String DLT_PREFIX = "91";
	// ************* Cache Configuration **************
	public static long QUEUE_HEAP_SIZE = 100;
	public static long QUEUE_DISK_SIZE = 1000;
	public static long QUEUE_EXPIRED_ON = 0;
	public static long RESPONSE_HEAP_SIZE = 100;
	public static long RESPONSE_DISK_SIZE = 1000;
	public static long RESPONSE_EXPIRED_ON = 15;
	public static long RESPONSE_DLR_EXPIRED_ON = 180; // 3 hours
	public static long RESEND_PDU_HEAP_SIZE = 100;
	public static long RESEND_PDU_DISK_SIZE = 1000;
	public static long RESEND_PDU_EXPIRED_ON = 60; // 1 hour
	// ********************* Files & Dir ************************
	public static final String persist_dir = "backup//cache//";
	public static final String resp_process_backup_dir = "backup//resp_process//";
	public static final String resp_backup_dir = "backup//response//";
	public static final String deliver_backup_dir = "backup//deliver//";
	public static final String BACKUP_FOLDER = "backup//";
	public static final String CACHE_PRINT = "config//Print.flag";
	public static final String Dashboard_flag_file = "config//dashboard.flag";
	public static final String CONFIG_DIR = "config//";
	public static final String SMSC_DIR = "smsc//";
	public static final String SpecialEncodingFile = "SpecialGreekEncoding//replacement.txt";
	public static final String ApplicationFlag = "config//Application.flag";
	public static final String APPLICATION_CONFIG_FILE = "config//SmppConfiguration.file";
	// -------- database ----------------------
	public static String DB_URL = null;
	public static String LOG_DB = null;
	public static String LOG_DB_URL = null;
	public static String JDBC_DRIVER = null;
	public static String DB_USER = null;
	public static String DB_PASSWORD = null;
	public static String ALTER_DB_USER = null;
	public static String ALTER_DB_PASSWORD = null;
	public static int MAX_CONNECTIONS;
	// ---------- email -----------------------
	public static String EMAIL_CC;
	public static String EMAIL_FROM;
	public static String EMAIL_USER;
	public static String EMAIL_PASSWORD;
	public static String SMTP_HOST_NAME;
	public static int SMTP_PORT;
	// ----------- queued alert -------
	public static int QUEUED_ALERT_COUNT = 100;
	public static int QUEUED_ALERT_DURATION = 10;  // minutes
	public static String QUEUED_ALERT_EMAILS;
	public static String QUEUED_ALERT_NUMBERS;
}
