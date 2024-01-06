package com.hti.smpp.common.response;

public class MncMccTokens {
	
	private String mccTokens;
	private String mncTokens;
	public MncMccTokens() {
		super();
	}
	public MncMccTokens(String mccTokens, String mncTokens) {
		super();
		this.mccTokens = mccTokens;
		this.mncTokens = mncTokens;
	}
	public String getMccTokens() {
		return mccTokens;
	}
	public void setMccTokens(String mccTokens) {
		this.mccTokens = mccTokens;
	}
	public String getMncTokens() {
		return mncTokens;
	}
	public void setMncTokens(String mncTokens) {
		this.mncTokens = mncTokens;
	}
	

}
