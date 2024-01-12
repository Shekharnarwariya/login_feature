package com.hti.smpp.common.response;

import org.springframework.stereotype.Component;

import com.hti.smpp.common.messages.dto.BulkListInfo;

@Component
public class SmsResponse {

	private String respMsgId;

	private String msgCount;

	private BulkListInfo BulkListInfo;

	private String credits;

	private String deductcredits;

	private String status;

	public String getRespMsgId() {
		return respMsgId;
	}

	public void setRespMsgId(String respMsgId) {
		this.respMsgId = respMsgId;
	}

	public String getMsgCount() {
		return msgCount;
	}

	public void setMsgCount(String msgCount) {
		this.msgCount = msgCount;
	}

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

}
