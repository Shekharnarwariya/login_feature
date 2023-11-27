package com.hti.smpp.common.util;

public class Constant {

	public static final String OTP_SUBJECT = "OTP confirmation alert for SMPP";

	public static final String TEMPLATE_PATH = "/index";
	public static final String MESSAGE_FOR_OTP = "Dear User,\r\n"
			+ "\r\n"
			+ "Please use the following OTP (One-Time Password) to validate your email for survey participation.\r\n"
			+ " \r\n"
			+ " ";
	public static final String PASSWORD_FORGOT_SUBJECT="Your SMPP Account Password Has Been Reset";
	public static final String MESSAGE_FOR_FORGOT_PASSWORD="Dear User,\r\n"
			+ "\r\n"
			+ "I hope this message finds you well. We would like to inform you that your SMPP (Short Message Peer-to-Peer) account password has been reset as per your request. Your account security is our top priority, and we have taken this action to ensure the continued protection of your data and messaging services.\r\n"
			+ "\r\n"
			+ "Here are the details of your updated SMPP account:";
   public static final String MESSAGE_FOR_PASSWORD_UPDATE="Dear User,\r\n"
   		+ "\r\n"
   		+ "I hope this message finds you well. We would like to inform you that your SMPP (Short Message Peer-to-Peer) account password has been updated as per your request. Your account security is our top priority, and we have taken this action to ensure the continued protection of your data and messaging services.\r\n"
   		+ "\r\n"
   		+ "Here are the details of your updated SMPP account:\r\n";
   public static final String PASSWORD_UPDATE_SUBJECT="Your SMPP Account Password Has Been Updated";
}
