package com.hti.smpp.common.user.dto;

public class User {

	private int userId;

	private String systemId;

	private String password;

	private String role;
	

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public User() {
		super();
	}

	@Override
	public String toString() {
		return "User [userId=" + userId + ", systemId=" + systemId + ", password=" + password + ", role=" + role + "]";
	}

	public User(int userId, String systemId, String password, String role) {
		super();
		this.userId = userId;
		this.systemId = systemId;
		this.password = password;
		this.role = role;
	}

}
