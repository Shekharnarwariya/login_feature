package com.hti.smpp.common.response;

import com.hti.smpp.common.user.dto.AccessLogEntry;

public class RecentActivityResponse {

	private AccessLogEntry successLogEntry;
	private AccessLogEntry failedLogEntry;
	private  String gmt;
	
	
	@Override
	public String toString() {
		return "RecentActivityResponse [successLogEntry=" + successLogEntry + ", failedLogEntry=" + failedLogEntry
				+ ", gmt=" + gmt + "]";
	}
	
	public RecentActivityResponse(AccessLogEntry successLogEntry, AccessLogEntry failedLogEntry, String gmt) {
		super();
		this.successLogEntry = successLogEntry;
		this.failedLogEntry = failedLogEntry;
		this.gmt = gmt;
	}
	
	

	public RecentActivityResponse() {
		super();
	}

	public AccessLogEntry getSuccessLogEntry() {
		return successLogEntry;
	}
	public void setSuccessLogEntry(AccessLogEntry successLogEntry) {
		this.successLogEntry = successLogEntry;
	}
	public AccessLogEntry getFailedLogEntry() {
		return failedLogEntry;
	}
	public void setFailedLogEntry(AccessLogEntry failedLogEntry) {
		this.failedLogEntry = failedLogEntry;
	}
	public String getGmt() {
		return gmt;
	}
	public void setGmt(String gmt) {
		this.gmt = gmt;
	}
	
	
	
	
}
