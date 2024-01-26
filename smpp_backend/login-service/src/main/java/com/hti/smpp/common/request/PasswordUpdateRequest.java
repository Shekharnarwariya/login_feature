package com.hti.smpp.common.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents a request to update the password in the application.
 */
public class PasswordUpdateRequest {

	@NotBlank(message = "Please enter your old password.")
	private String oldPassword;

	@NotBlank(message = "Please enter your new password.")
	private String newPassword;

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public PasswordUpdateRequest(String oldPassword, String newPassword) {
		super();
		this.oldPassword = oldPassword;
		this.newPassword = newPassword;
	}

	public PasswordUpdateRequest() {
		super();
	}

}
