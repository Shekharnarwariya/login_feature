package com.hti.smpp.common.report.dto;



public class ReportCriteria {
private String startTime;
private String endTime;
private long startMsgId;
private long endMsgId;
private int userId;
private int resellerId;

public String getStartTime() {
	return startTime;
}

public void setStartTime(String startTime) {
	this.startTime = startTime;
}

public String getEndTime() {
	return endTime;
}

public void setEndTime(String endTime) {
	this.endTime = endTime;
}

public long getStartMsgId() {
	return startMsgId;
}

public void setStartMsgId(long startMsgId) {
	this.startMsgId = startMsgId;
}

public long getEndMsgId() {
	return endMsgId;
}

public void setEndMsgId(long endMsgId) {
	this.endMsgId = endMsgId;
}

public int getUserId() {
	return userId;
}

public void setUserId(int userId) {
	this.userId = userId;
}

public int getResellerId() {
	return resellerId;
}

public void setResellerId(int resellerId) {
	this.resellerId = resellerId;

}
}
