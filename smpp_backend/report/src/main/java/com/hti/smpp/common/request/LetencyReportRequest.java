package com.hti.smpp.common.request;

import jakarta.validation.constraints.NotBlank;

public class LetencyReportRequest {
	
	@NotBlank(message = "SenderId must not be blank")
	private String senderId;
	private String destinationNumber;
	private String country;
	private String operator;
	private String campaign;

	@NotBlank(message = "EndDate must not be blank")
	private String endDate;

	@NotBlank(message = "StartDate must not be blank")
	private String startDate;
	private String[] smscnames;

	@NotBlank(message = "ClientId must not be blank")
	private String clientId;
	private String status;
	private String smscName;
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSmscName() {
		return smscName;
	}

	public void setSmscName(String smscName) {
		this.smscName = smscName;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String[] getSmscnames() {
		return smscnames;
	}

	public void setSmscnames(String[] smscnames) {
		this.smscnames = smscnames;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public String getDestinationNumber() {
		return destinationNumber;
	}

	public void setDestinationNumber(String destinationNumber) {
		this.destinationNumber = destinationNumber;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getCampaign() {
		return campaign;
	}

	public void setCampaign(String campaign) {
		this.campaign = campaign;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
}
