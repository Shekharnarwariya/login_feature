package com.hti.smpp.common.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents a login request in the application.
 */
public class LoginRequest {
	/**
	 * Represents a login request in the application.
	 */
	@NotBlank(message = "Please enter a username. It cannot be blank.")
	private String username;

	@NotBlank(message = "Please enter a password. It cannot be blank.")
	private String password;

	private int otp;

	public int getOtp() {
		return otp;
	}

	private String accessName;

	public String getAccessName() {
		return accessName;
	}

	public void setAccessName(String accessName) {
		this.accessName = accessName;
	}

	public void setOtp(int otp) {
		this.otp = otp;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "LoginRequest [username=" + username + ", password=" + password + "]";
	}

}
