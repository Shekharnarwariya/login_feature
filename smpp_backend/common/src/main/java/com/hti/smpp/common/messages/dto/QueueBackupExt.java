package com.hti.smpp.common.messages.dto;

import org.springframework.stereotype.Component;

@Component
public class QueueBackupExt {
	private BulkEntry bulkEntry;
	private String userMode;
	private long msgCount;
	private double totalCost;
	private String origMessage;
	private String campaignType;

	public BulkEntry getBulkEntry() {
		return bulkEntry;
	}

	public void setBulkEntry(BulkEntry bulkEntry) {
		this.bulkEntry = bulkEntry;
	}

	public String getCampaignType() {
		return campaignType;
	}

	public void setCampaignType(String campaignType) {
		this.campaignType = campaignType;
	}

	public String getOrigMessage() {
		return origMessage;
	}

	public void setOrigMessage(String origMessage) {
		this.origMessage = origMessage;
	}

	public String getUserMode() {
		return userMode;
	}

	public void setUserMode(String userMode) {
		this.userMode = userMode;
	}

	public long getMsgCount() {
		return msgCount;
	}

	public void setMsgCount(long msgCount) {
		this.msgCount = msgCount;
	}

	public double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}
}
