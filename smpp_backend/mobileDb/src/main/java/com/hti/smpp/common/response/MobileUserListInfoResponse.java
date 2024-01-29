package com.hti.smpp.common.response;

import java.util.Collection;

public class MobileUserListInfoResponse {
	
	private Collection mobileRecord;
	private long size;
	private String div_setting;
	private String param_value;
	private String target;
	
	
	public Collection getMobileRecord() {
		return mobileRecord;
	}
	public void setMobileRecord(Collection mobileRecord) {
		this.mobileRecord = mobileRecord;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getDiv_setting() {
		return div_setting;
	}
	public void setDiv_setting(String div_setting) {
		this.div_setting = div_setting;
	}
	public String getParam_value() {
		return param_value;
	}
	public void setParam_value(String param_value) {
		this.param_value = param_value;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	
	

}
