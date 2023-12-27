package com.hti.smpp.common.request;

import jakarta.validation.constraints.NotBlank;

public class PasswordUpdateRequest {
	@NotBlank(message = "PLEASE ENTER OLD PASSWORD ")
	private String oldPassword;

	@NotBlank(message = "PLEASE ENTER  NEW PASSWORD")
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
