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
