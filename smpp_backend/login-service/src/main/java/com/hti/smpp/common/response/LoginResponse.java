package com.hti.smpp.common.response;


public class LoginResponse {
	
	private JwtResponse jwtResponse;
	private boolean isOtpLogin;
	private String status;
	
	public LoginResponse() {
		super();
	}

	public LoginResponse(JwtResponse jwtResponse, boolean isOtpLogin, String status) {
		super();
		this.jwtResponse = jwtResponse;
		this.isOtpLogin = isOtpLogin;
		this.status = status;
	}

	public JwtResponse getJwtResponse() {
		return jwtResponse;
	}

	public void setJwtResponse(JwtResponse jwtResponse) {
		this.jwtResponse = jwtResponse;
	}

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
	
	

}
