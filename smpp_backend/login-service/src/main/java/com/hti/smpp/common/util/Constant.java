package com.hti.smpp.common.util;

public class Constant {

	public static final String OTP_SUBJECT = "OTP confirmation alert for SMPP";

	public static final String TEMPLATE_PATH = "/index";
	public static final String MESSAGE_FOR_OTP = "\r\n"
			+ "We're sorry to hear that you're having trouble with logging in to Dashboard. We've received a message that you've forgotten your password. If this was you, Please enter the mentioned OTP in the field listed on the Dashboard.\r\n"
			+ "\r\n" + "Your One Time Password (OTP) for forgot password request.";
	public static final String SECOND_MESSAGE_FOR_OTP = "If you didn't request a login link or password reset, you can ignore this message.\r\n"
			+ "\r\n" + "Do let us know if you face any problem in resetting your password.;";
	public static final String PASSWORD_FORGOT_SUBJECT = "Your SMPP Account Password Has Been Reset";
	public static final String MESSAGE_FOR_FORGOT_PASSWORD = "\r\n"
			+ "We’ve changed your password, as you asked. To view or change your account information, visit your Account.\r\n"
			+ "\r\n"
			+ "If you did not ask to change your password, we are here to help secure your account – just contact us.";
	public static final String MESSAGE_FOR_PASSWORD_UPDATE = "Dear User,\r\n" + "\r\n"
			+ "I hope this message finds you well. We would like to inform you that your SMPP (Short Message Peer-to-Peer) account password has been updated as per your request. Your account security is our top priority, and we have taken this action to ensure the continued protection of your data and messaging services.\r\n"
			+ "\r\n" + "Here are the details of your updated SMPP account:\r\n";
	public static final String PASSWORD_UPDATE_SUBJECT = "Your SMPP Account Password Has Been Updated";

	public static final String OTP_FLAG_SUBJECT = "FORGOT PASSWORD OTP";

	public static final String FORGOT_FLAG_SUBJECT = "FORGOT PASSWORD SUCCESSFULLY";

	public static final String UPDATE_FLAG_SUBJECT = "UPDATE PASSWORD SUCCESSFULLY";
}
