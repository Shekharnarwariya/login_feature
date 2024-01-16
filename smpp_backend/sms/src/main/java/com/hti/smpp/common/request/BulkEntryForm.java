package com.hti.smpp.common.request;

public class BulkEntryForm {
	private int id;
	private String systemId;
	private String senderId;
	private double delay;
	private String reqType;
	private int serverId;
	private int ston;
	private int snpi;
	private String messageType;
	private boolean alert;
	private long alertNumber;
	private long expiryHour;
	private String createdOn;
	private long total;
	private long firstNumber;
	private String content;
	private String campaignName;
	private long processed; 
	private long pending; 
	private boolean active; 

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public double getDelay() {
		return delay;
	}

	public void setDelay(double delay) {
		this.delay = delay;
	}

	public String getReqType() {
		return reqType;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getSton() {
		return ston;
	}

	public void setSton(int ston) {
		this.ston = ston;
	}

	public int getSnpi() {
		return snpi;
	}

	public void setSnpi(int snpi) {
		this.snpi = snpi;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public boolean isAlert() {
		return alert;
	}

	public void setAlert(boolean alert) {
		this.alert = alert;
	}

	public long getAlertNumber() {
		return alertNumber;
	}

	public void setAlertNumber(long alertNumber) {
		this.alertNumber = alertNumber;
	}

	public long getExpiryHour() {
		return expiryHour;
	}

	public void setExpiryHour(long expiryHour) {
		this.expiryHour = expiryHour;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getFirstNumber() {
		return firstNumber;
	}

	public void setFirstNumber(long firstNumber) {
		this.firstNumber = firstNumber;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCampaignName() {
		return campaignName;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	public long getProcessed() {
		return processed;
	}

	public void setProcessed(long processed) {
		this.processed = processed;
	}

	public long getPending() {
		return pending;
	}

	public void setPending(long pending) {
		this.pending = pending;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
