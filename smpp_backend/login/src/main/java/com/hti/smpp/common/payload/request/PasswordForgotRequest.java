package com.hti.smpp.common.payload.request;

import jakarta.validation.constraints.NotBlank;

public class PasswordForgotRequest {

	@NotBlank(message = "PLEASE ENTER Email")
	private String email;

	@NotBlank(message = "PLEASE ENTER  PASSWORD")
	private String newPassword;

	@NotBlank(message = "PLEASE ENTER  OTP")
	private String otp;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public PasswordForgotRequest(String email, String newPassword, String otp) {
		super();
		this.email = email;
		this.newPassword = newPassword;
		this.otp = otp;
	}

	public PasswordForgotRequest() {
		super();
	}

}
