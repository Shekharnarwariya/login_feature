package com.hti.smpp.common.schedule.dto;

public class ScheduleEntryExt extends ScheduleEntry {
	private String messageType;
	private int totalNumbers;
	private boolean customContent;
	private String senderId;
	private String campaign;
	private String firstNum;

	public ScheduleEntryExt(ScheduleEntry entry) {
		setId(entry.getId());
		setUsername(entry.getUsername());
		setFileName(entry.getFileName());
		setServerId(entry.getServerId());
		setServerTime(entry.getServerTime());
		setClientTime(entry.getClientTime());
		setClientGmt(entry.getClientGmt());
		setRepeated(entry.getRepeated());
		setScheduleType(entry.getScheduleType());
		setStatus(entry.getStatus());
		setCreatedOn(entry.getCreatedOn());
	}

	public String getFirstNum() {
		return firstNum;
	}

	public void setFirstNum(String firstNum) {
		this.firstNum = firstNum;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public int getTotalNumbers() {
		return totalNumbers;
	}

	public void setTotalNumbers(int totalNumbers) {
		this.totalNumbers = totalNumbers;
	}

	public boolean isCustomContent() {
		return customContent;
	}

	public void setCustomContent(boolean customContent) {
		this.customContent = customContent;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public String getCampaign() {
		return campaign;
	}

	public void setCampaign(String campaign) {
		this.campaign = campaign;
	}
}
