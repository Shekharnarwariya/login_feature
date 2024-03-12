package com.hti.util;

public class Constants {
	// *********** Files & Dir ****************
	public static final String APPLICATION_CONFIG_FILE = "config//UserConfiguration.file";
	public static final String HLR_CONFIG_FILE = "config//HlrConfiguration.file";
	public static final String CONFIG_DIR = "config//";
	// public static final String Resend_DLRS_Flag_file = "config//R_DLR.flag";
	public static final String webdeliverResponse_log = "log//webdeliverResponse.log";
	public static final String APPLICATION_FLAG = "config//UserApplication.flag";
	public static final String TEMP_QUEUE_FLAG = "config//TempQueue.flag";
	public static final String CACHE_PRINT = "config//UserPrint.flag";
	public static final String HLR_CONFIG_FLAG = "config//HlrConfig.flag";
	public static final String UnicodeEncodingFile = "config//UnicodeReplacement.txt";
	// ******** email ***************
	public static String EMAIL_CC;
	public static String EMAIL_FROM;
	public static String EMAIL_USER;
	public static String EMAIL_PASSWORD;
	public static String SMTP_HOST_NAME;
	public static int SMTP_PORT;
	// ----------- others --------
	public static String LOCAL_IP = "localhost";
	public static int LOCAL_PORT = 8899;
	public static String SERVER_NAME = "GWX";
	// ----------- hlr config --------------
	public static String HLR_SERVER_IP = "localhost";
	public static int HLR_SERVER_PORT = 2775;
	public static int HLR_SESSION_LIMIT = 6;
	public static int HLR_STATUS_WAIT_DURATION = 120; // wait for lookup status
	public static String PROMO_SENDER = "";
	public static String DND_SMSC = "GW_DND";
	public static String HLR_DOWN_SMSC_1 = "GW_HLR_1";
	public static String HLR_DOWN_SMSC_2 = "GW_HLR_2";
	public static String HLR_DOWN_SMSC_3 = "GW_HLR_3";
	public static String HLR_DOWN_SMSC_4 = "GW_HLR_4";
	public static String HLR_DOWN_SMSC_5 = "GW_HLR_5";

	public Constants() {
	}
}
