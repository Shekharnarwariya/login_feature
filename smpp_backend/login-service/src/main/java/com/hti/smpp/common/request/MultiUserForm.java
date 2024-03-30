package com.hti.smpp.common.request;

public class MultiUserForm {
	private int[] id;
	private int userId;
	private String[] accessName;
	private String[] mobile;
	private String[] email;

	public int[] getId() {
		return id;
	}

	public void setId(int[] id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String[] getAccessName() {
		return accessName;
	}

	public void setAccessName(String[] accessName) {
		this.accessName = accessName;
	}

	public String[] getMobile() {
		return mobile;
	}

	public void setMobile(String[] mobile) {
		this.mobile = mobile;
	}

	public String[] getEmail() {
		return email;
	}

	public void setEmail(String[] email) {
		this.email = email;
	}

}
