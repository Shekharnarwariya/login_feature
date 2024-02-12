package com.hti.smpp.common.response;

import java.util.ArrayList;

public class ChooseRequestResponse {

	private ArrayList<?> mobileRecordList;
	private String  smsCount;
	private ArrayList<?> mobileRecord_s;
	private int smsCount_I;
	private  int partDay_I;
	private  int totalSmsParDay;
	private String target;
	
	
	public ArrayList<?> getMobileRecordList() {
		return mobileRecordList;
	}
	public void setMobileRecordList(ArrayList<?> mobileRecordList) {
		this.mobileRecordList = mobileRecordList;
	}
	public String getSmsCount() {
		return smsCount;
	}
	public void setSmsCount(String smsCount) {
		this.smsCount = smsCount;
	}
	public ArrayList<?> getMobileRecord_s() {
		return mobileRecord_s;
	}
	public void setMobileRecord_s(ArrayList<?> mobileRecord_s) {
		this.mobileRecord_s = mobileRecord_s;
	}
	public int getSmsCount_I() {
		return smsCount_I;
	}
	public void setSmsCount_I(int smsCount_I) {
		this.smsCount_I = smsCount_I;
	}
	public int getPartDay_I() {
		return partDay_I;
	}
	public void setPartDay_I(int partDay_I) {
		this.partDay_I = partDay_I;
	}
	public int getTotalSmsParDay() {
		return totalSmsParDay;
	}
	public void setTotalSmsParDay(int totalSmsParDay) {
		this.totalSmsParDay = totalSmsParDay;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	
	
}
