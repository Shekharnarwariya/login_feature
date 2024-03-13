package com.hti.smpp.common.response;

import java.util.List;

/**
 * Represents a response containing a JWT token.
 */
public class JwtResponse {
	private String token;
	private String type = "Bearer";
	private int id;
	private String username;
	private List<String> roles;
	private boolean isOtpLogin;
	private String status;

	public boolean isOtpLogin() {
		return isOtpLogin;
	}

	public void setOtpLogin(boolean isOtpLogin) {
		this.isOtpLogin = isOtpLogin;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public JwtResponse(String accessToken, int id, String username, List<String> roles) {
		this.token = accessToken;
		this.id = id;
		this.username = username;
		this.roles = roles;
	}

//Getter and Setter
	public String getAccessToken() {
		return token;
	}

	public void setAccessToken(String accessToken) {
		this.token = accessToken;
	}

	public String getTokenType() {
		return type;
	}

	public void setTokenType(String tokenType) {
		this.type = tokenType;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

}
