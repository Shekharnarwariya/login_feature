/* 
 **	Copyright 2004 High Tech InfoSystems. All Rights Reserved.
 **	Author		: Satya Prakash [satyaprakash@utils.net]
 **	Created on 	: 11/30/2004
 **	Modified on	: 11/30/2004
 **	Descritpion	:
 */
package com.hti.smpp.common.sms.util;

import java.net.InetAddress;

import org.springframework.stereotype.Service;

@Service
public final class IConstants {

//	public static final int DB_BATCH_SIZE = Integer.parseInt(ContextListener.property.getProperty("load.batch.size"));
//	// -------- path ------------------------------
	public static final String HOME_DIR = "C://";
//	public static final String CAAS_FLAG_DIR = HOME_DIR + ContextListener.property.getProperty("caas.flag.dir");
//	public static final String HLR_FLAG_DIR = HOME_DIR + ContextListener.property.getProperty("hlr.flag.dir");
	public static final String TOMCAT_DIR = HOME_DIR + "Tomcat//";
//	// public static final String BIN_DIR = TOMCAT_DIR +
//	// ContextListener.property.getProperty("tomcat.bin.dir");
//	public static final String WEBSMPP_EXT_DIR = TOMCAT_DIR + "websmpp//";
//	public static final String WEBAPP_DIR = TOMCAT_DIR + ContextListener.property.getProperty("tomcat.app.dir");
//	public static final String FORMAT_DIR = TOMCAT_DIR + ContextListener.property.getProperty("tomcat.format.dir");
	public static final String SCHEDULE_DIR = TOMCAT_DIR + "bin//schedule//";
//	// ------------ Prefix Settings ---------------
//	public static final int PREFIX_APPLY = Integer.parseInt(ContextListener.property.getProperty("load.prefix.apply"));
//	public static final int PREFIX_TO_APPLY = Integer
//			.parseInt(ContextListener.property.getProperty("load.prefix.value"));
//	public static final int NUMBER_LENGTH = Integer
//			.parseInt(ContextListener.property.getProperty("load.number.length"));
//	// ----------- balance multiply limit for reseller ------
//	public static final int BALANCE__MULTIPLY_LIMIT = Integer
//			.parseInt(ContextListener.property.getProperty("load.balance.multiply"));
//	// --------------------------------------------
//	public static final String DEFAULT_GMT = ContextListener.property.getProperty("load.gmt");
//	public static final String DEFAULT_GMT_VALUE = ContextListener.property.getProperty("load.gmt.value");
//	public static final String AccessIP = ContextListener.property.getProperty("load.accessIP");
//	public static final String CRM_ACCESS_IP = ContextListener.property.getProperty("load.crm_api_ip");
//	public static final String GLOBAl_ACCESS_IP = ContextListener.property.getProperty("load.global_ip");
//	public static final int LOGIN_OTP_VALIDITY = Integer
//			.parseInt(ContextListener.property.getProperty("load.login.otp.validity"));
//	public static final int PASSWORD_OTP_VALIDITY = Integer
//			.parseInt(ContextListener.property.getProperty("load.pwd.otp.validity"));
//	public static final int PASSWORD_LINK_VALIDITY = Integer
//			.parseInt(ContextListener.property.getProperty("load.pwd.link.validity"));
//	public static final int PASSWORD_EXPIRY_DAYS = Integer
//			.parseInt(ContextListener.property.getProperty("load.pwd.expire.days"));
//	public static final int LOGIN_ATTEMPT_LIMIT = Integer
//			.parseInt(ContextListener.property.getProperty("load.login.attempt"));
//	public static final String WEB_LINK_TRACKING_POST_URL = ContextListener.property
//			.getProperty("load.tracking.post.url");
//	public static final String WEB_LINK_TRACKING_URL = ContextListener.property.getProperty("load.tracking.url");
//	public static final String smsc = ContextListener.property.getProperty("load.smsc");
	public static final String SUCCESS_KEY = "success";
//	public static final String SHEDULE_MORE_KEY = ContextListener.property.getProperty("load.shedule_more");
	public static final String FAILURE_KEY = "failure";
//	public static final String INVALID_SESSION = ContextListener.property.getProperty("load.invalidSession");
	public static final String USER_SESSION_KEY = "userSessionObject";
//	public static final String WEB_APPLICATION_ERROR = ContextListener.property.getProperty("load.applicationError");
//	public static final String REMOTE_SERVER_UNAVAILABLE = ContextListener.property
//			.getProperty("load.remoteSmppServerUnavailable");
//	public static final long REMOTE_CONNECT_INTERVAL = Integer
//			.parseInt(ContextListener.property.getProperty("load.time"));
//	public static final String INSUFFICIENT_CREDITS = ContextListener.property.getProperty("load.insufficientCredits");
	public static final int SERVER_ID = 1;
//	public static String GATEWAY_NAME = ContextListener.property.getProperty("load.gateway");
	public static String ip = "122.168.122.77";
//	public static final String SMPP_IP = ContextListener.property.getProperty("load.smppip");
	public static String SMPP_PORT = "8899";
//	public static int SMPP_USER_PORT = Integer.parseInt(ContextListener.property.getProperty("load.smpp.port"));
//	public static final String HLR_IP = ContextListener.property.getProperty("load.hlrip");
//	public static int HLR_PORT = Integer.parseInt(ContextListener.property.getProperty("load.hlrport"));
//	public static final String WebUrl = ContextListener.property.getProperty("mail.sitename");
//	public static final String applicationName = ContextListener.property.getProperty("load.applicationName");
//	public static final String admin = ContextListener.property.getProperty("load.admin");
//	public static final String mediaupload = ContextListener.property.getProperty("load.mediaupload");
//	public static final String FTPServer = ContextListener.property.getProperty("load.ftpserver");
//	public static final String FTPUser = ContextListener.property.getProperty("load.ftpuser");
//	public static final String FTPPassword = ContextListener.property.getProperty("load.ftppassword");
//	public static final int SmppPortForWeb = Integer
//			.parseInt(ContextListener.property.getProperty("load.smppPortForWeb"));
//	public static int SleepTime = Integer.parseInt(ContextListener.property.getProperty("load.sleepTime"));
//	public static int ThroughPut = Integer.parseInt(ContextListener.property.getProperty("load.throughPut"));
//	public static int ALERT_INTERVAL = Integer.parseInt(ContextListener.property.getProperty("load.alert.interval"));
//	public static final int statusPort = Integer.parseInt(ContextListener.property.getProperty("load.statusPort"));
//	public static final int smtpPort = Integer.parseInt(ContextListener.property.getProperty("load.smtpport"));
//	public static final String mailHost = ContextListener.property.getProperty("load.mailHost");
//	public static final String mailPassword = ContextListener.property.getProperty("load.mailPassword");
//	public static final String mailId = ContextListener.property.getProperty("load.mailId");
//	public static final int timeout = Integer.parseInt(ContextListener.property.getProperty("load.timeout"));
//	public static final String DUPLICATE_REQUEST = ContextListener.property.getProperty("load.DUPLICATE_REQUEST");
//	public static final String[] SUPPORT_EMAIL = (ContextListener.property.getProperty("load.supportId")).split(",");
//	public static final String[] FINANCE_EMAIL = (ContextListener.property.getProperty("load.financeId")).split(",");
//	public static final String[] ROUTE_EMAIL = (ContextListener.property.getProperty("load.routeId")).split(",");
//	public static final String[] CC_EMAIL = (ContextListener.property.getProperty("load.mailCC")).split(",");
//	public static final String TO_EMAIl = ContextListener.property.getProperty("load.mailTo");
//	public static final String SENDER_MGMT_FROM_EMAIL = ContextListener.property.getProperty("sender.mgmt.from.email");
//	public static final String SENDER_MGMT_ALERT_SENDER = ContextListener.property
//			.getProperty("sender.mgmt.alert.sender");
//	// public static final String GMT =
//	// ContextListener.property.getProperty("load.GMT");
//	public static final int MaxRecordFetch = Integer
//			.parseInt(ContextListener.property.getProperty("load.MaxRecordFetch"));
//	public static final String ALERT_SENDER_ID = ContextListener.property.getProperty("load.senderID");
//	public static final String OTP_SENDER_ID = ContextListener.property.getProperty("load.otp.sender");
//	/*
//	 ** For HTTP URL SMS PUSH STATUS AND ERROR CODES added by sanjeev
//	 */
//	public static final String ERROR_HTTP01 = ContextListener.property.getProperty("load.ERROR_HTTP01");
//	public static final String ERROR_HTTP02 = ContextListener.property.getProperty("load.ERROR_HTTP02");
//	public static final String ERROR_HTTP03 = ContextListener.property.getProperty("load.ERROR_HTTP03");
//	public static final String ERROR_HTTP04 = ContextListener.property.getProperty("load.ERROR_HTTP04");
//	public static final String ERROR_HTTP05 = ContextListener.property.getProperty("load.ERROR_HTTP05");
//	public static final String ERROR_HTTP06 = ContextListener.property.getProperty("load.ERROR_HTTP06");
//	public static final String ERROR_HTTP07 = ContextListener.property.getProperty("load.ERROR_HTTP07");
//	public static final String ERROR_HTTP08 = ContextListener.property.getProperty("load.ERROR_HTTP08");
//	public static final String ERROR_HTTP09 = ContextListener.property.getProperty("load.ERROR_HTTP09");
//	public static final String ERROR_HTTP10 = ContextListener.property.getProperty("load.ERROR_HTTP10");
//	public static final String ERROR_HTTP11 = ContextListener.property.getProperty("load.ERROR_HTTP11");
//	public static final String ERROR_HTTP12 = ContextListener.property.getProperty("load.ERROR_HTTP12");
//	public static final String ERROR_HTTP13 = ContextListener.property.getProperty("load.ERROR_HTTP13");
//	public static final String ERROR_HTTP14 = ContextListener.property.getProperty("load.ERROR_HTTP14");
//	public static final String ERROR_HTTP15 = ContextListener.property.getProperty("load.ERROR_HTTP15");
//	public static final String ERROR_HTTP16 = ContextListener.property.getProperty("load.ERROR_HTTP16");
//	public static final String ERROR_HTTP17 = ContextListener.property.getProperty("load.ERROR_HTTP17");
//	public static final String ERROR_HTTP18 = ContextListener.property.getProperty("load.ERROR_HTTP18");
//	public static final String ERROR_HTTP19 = ContextListener.property.getProperty("load.ERROR_HTTP19");
//	public static final String ERROR_HTTP20 = ContextListener.property.getProperty("load.ERROR_HTTP20");
//	public static final String ERROR_HTTP21 = ContextListener.property.getProperty("load.ERROR_HTTP21");
//	public static final String ERROR_HTTP22 = ContextListener.property.getProperty("load.ERROR_HTTP22");
//	public static final String ERROR_HTTP23 = ContextListener.property.getProperty("load.ERROR_HTTP23");
//	public static final String ERROR_HTTP24 = ContextListener.property.getProperty("load.ERROR_HTTP24");
//	public static final String ERROR_HTTP25 = ContextListener.property.getProperty("load.ERROR_HTTP25");
//	public static final String ERROR_HTTP26 = ContextListener.property.getProperty("load.ERROR_HTTP26");
//	public static final String ERROR_HTTP27 = ContextListener.property.getProperty("load.ERROR_HTTP27");
//	public static final String ERROR_HTTP28 = ContextListener.property.getProperty("load.ERROR_HTTP28");
//	public static final String ACCEPTED = ContextListener.property.getProperty("load.ACCEPTED");
//	public static final String CP_STATUS = ContextListener.property.getProperty("load.CP_STATUS");
//	public static final String ATES_STATUS = ContextListener.property.getProperty("load.ATES_STATUS");
//	public static final String DELIVRD_STATUS = ContextListener.property.getProperty("load.DELIVRD_STATUS");
//	public static final String EXPIRED_STATUS = ContextListener.property.getProperty("load.EXPIRED_STATUS");
//	public static final String REJECTED_STATUS = ContextListener.property.getProperty("load.REJECTED_STATUS");
//	public static final String UNDELIVERED_STATUS = ContextListener.property.getProperty("load.UNDELIVERED_STATUS");
//	public static final String INVALID_STATUS = ContextListener.property.getProperty("load.INVALID_STATUS");
//	public static final String DELETED_STATUS = ContextListener.property.getProperty("load.DELETED_STATUS");
//	public static final String UNKNOWN_STATUS = ContextListener.property.getProperty("load.UNKNOWN_STATUS");
//	public static final String ERROR_HTTP_RESPONSE_01 = ContextListener.property
//			.getProperty("load.ERROR_HTTP_RESPONSE_01");
//	public static final String ERROR_HTTP_RESPONSE_02 = ContextListener.property
//			.getProperty("load.ERROR_HTTP_RESPONSE_02");
//	public static final int HTTPsmsCount = Integer.parseInt(ContextListener.property.getProperty("load.HTTPsmsCount"));
//	public static final int HTTPSleepTime = Integer
//			.parseInt(ContextListener.property.getProperty("load.HTTPSleepTime"));
	// ************************************************End Card
	// Menu******************************************
}
