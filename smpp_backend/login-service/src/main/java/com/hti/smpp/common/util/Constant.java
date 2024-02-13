package com.hti.smpp.common.util;

/**
 * The {@code Constant} class defines constant values and configurations used throughout the application.
 */
public class Constant {

	public static final String LOGIN_SUBJECT = "Login alert for SMPP";
	public static final String GENERAL_TEMPLATE_PATH = "/template";
	public static final String LOGIN_TEMPLATE_PATH = "/loginalert";
    public static final String OTP_SUBJECT = "OTP Confirmation Alert for Your SMPP Account";
    public static final String TEMPLATE_PATH = "/index";
    public static final String MESSAGE_FOR_OTP = "Dear User,\n\n"
            + "It seems you're having trouble logging into your Dashboard. We received a request for password recovery. "
            + "If this request was made by you, please use the OTP provided below on the Dashboard to proceed.\n\n"
            + "Your One-Time Password (OTP) for password recovery is: ";

    public static final String SECOND_MESSAGE_FOR_OTP = "If you did not request this, please disregard this message.\n\n"
            + "Should you encounter any issues or did not initiate this request, please contact our support team for assistance.";

    public static final String PASSWORD_FORGOT_SUBJECT = "Password Reset Confirmation for Your SMPP Account";

    public static final String MESSAGE_FOR_FORGOT_PASSWORD = "Your password has been successfully reset as requested. "
            + "To access or modify your account information, please visit the Account section on our platform.\n\n"
            + "If this action was not initiated by you, it's crucial to contact us immediately to secure your account.";

    public static final String MESSAGE_FOR_PASSWORD_UPDATE = "Dear User,\n\n"
            + "We're confirming that your SMPP account password has been successfully updated. Your account's security "
            + "is our utmost priority, and this change helps ensure your data and messaging services remain protected.\n\n"
            + "Thank you for taking steps to maintain the security of your account.";

    public static final String PASSWORD_UPDATE_SUBJECT = "Confirmation: SMPP Account Password Update";

    public static final String OTP_FLAG_SUBJECT = "Password Recovery OTP Issued";

    public static final String FORGOT_FLAG_SUBJECT = "Password Reset Successful";

    public static final String UPDATE_FLAG_SUBJECT = "Password Update Successful";
}
