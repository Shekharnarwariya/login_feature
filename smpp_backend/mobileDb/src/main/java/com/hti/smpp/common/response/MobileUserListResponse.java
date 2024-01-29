package com.hti.smpp.common.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MobileUserListResponse {

	private Map<String, Object> recordMap;
	private   Map<String, List<String>> numberMap;
	long mobileRecord;
	private String target;
	
	
	
	public Map<String, Object> getRecordMap() {
		return recordMap;
	}
	public void setRecordMap(Map<String, Object> recordMap) {
		this.recordMap = recordMap;
	}
	public Map<String, List<String>> getNumberMap() {
		return numberMap;
	}
	public void setNumberMap(Map<String, List<String>> numberMap) {
		this.numberMap = numberMap;
	}
	public long getMobileRecord() {
		return mobileRecord;
	}
	public void setMobileRecord(long mobileRecord) {
		this.mobileRecord = mobileRecord;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	
	
	
}
