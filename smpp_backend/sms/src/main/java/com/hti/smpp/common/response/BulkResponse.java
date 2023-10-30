package com.hti.smpp.common.response;

import com.hti.smpp.common.sms.service.impl.BulkListInfo;

public class BulkResponse {
	private BulkListInfo BulkListInfo;

	private String credits;

	private String deductcredits;

	private String status;

	private String bulkSessionId;

	public BulkListInfo getBulkListInfo() {
		return BulkListInfo;
	}

	public void setBulkListInfo(BulkListInfo bulkListInfo) {
		BulkListInfo = bulkListInfo;
	}

	public String getCredits() {
		return credits;
	}

	public void setCredits(String credits) {
		this.credits = credits;
	}

	public String getDeductcredits() {
		return deductcredits;
	}

	public void setDeductcredits(String deductcredits) {
		this.deductcredits = deductcredits;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getBulkSessionId() {
		return bulkSessionId;
	}

	public void setBulkSessionId(String bulkSessionId) {
		this.bulkSessionId = bulkSessionId;
	}

}
