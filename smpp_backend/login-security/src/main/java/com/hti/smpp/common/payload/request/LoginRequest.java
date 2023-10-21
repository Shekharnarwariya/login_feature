package com.hti.smpp.common.payload.request;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

	@NotBlank(message = "PLEASE ENTER USER NAME")
	private String username;

	@NotBlank(message = "PLEASE ENTER  PASSWORD")
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
}
