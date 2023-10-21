package com.hti.smpp.common.url.dto;

import org.springframework.stereotype.Component;

@Component
public class ApiResultDTO {

	private String messageid;
	private String requestStatus;
	private String dlrStatus;
	private String msisdn;
	private String submitOn;
	private String deliverOn;
	// schedules
	private String batchId;
	private String scheduleOn;
	private String createdOn;
	private String sender;

	public ApiResultDTO(String messageid) {
		this.messageid = messageid;
	}

	public ApiResultDTO(String messageid, String requestStatus, String dlrStatus, String msisdn) {
		this.messageid = messageid;
		this.requestStatus = requestStatus;
		this.dlrStatus = dlrStatus;
		this.msisdn = msisdn;
	}

	public ApiResultDTO() {
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getDlrStatus() {
		return dlrStatus;
	}

	public void setDlrStatus(String dlrStatus) {
		this.dlrStatus = dlrStatus;
	}

	public String getMessageid() {
		return messageid;
	}

	public void setMessageid(String messageid) {
		this.messageid = messageid;
	}

	public String getRequestStatus() {
		return requestStatus;
	}

	public void setRequestStatus(String requestStatus) {
		this.requestStatus = requestStatus;
	}

	public String getSubmitOn() {
		return submitOn;
	}

	public void setSubmitOn(String submitOn) {
		this.submitOn = submitOn;
	}

	public String getDeliverOn() {
		return deliverOn;
	}

	public void setDeliverOn(String deliverOn) {
		this.deliverOn = deliverOn;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getScheduleOn() {
		return scheduleOn;
	}

	public void setScheduleOn(String scheduleOn) {
		this.scheduleOn = scheduleOn;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}
}
