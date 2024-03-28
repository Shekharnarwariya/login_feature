package com.hti.smpp.common.request;

public class LoginDTO {
	private String systemId;
	private String password;
	private String email;
	private String superAccess;
	private int otp;
	private String ipAddress;
	private String language;
	private String accessName;
	private String accessMobile;

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public int getOtp() {
		return otp;
	}

	public void setOtp(int otp) {
		this.otp = otp;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setSuperAccess(String superAccess) {
		this.superAccess = superAccess;
	}

	//////////////////// Getter Methods/////////////////////////
	public String getSuperAccess() {
		return superAccess;
	}

	public String getSystemId() {
		return systemId;
	}

	public String getPassword() {
		return password;
	}

	public String getEmail() {
		return email;
	}

	public String getAccessName() {
		return accessName;
	}

	public void setAccessName(String accessName) {
		this.accessName = accessName;
	}

	public String getAccessMobile() {
		return accessMobile;
	}

	public void setAccessMobile(String accessMobile) {
		this.accessMobile = accessMobile;
	}
}