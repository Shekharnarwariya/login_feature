package com.hti.smpp.common.management.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.hti.smpp.common.messages.dto.BulkEntry;

@Entity
@Table(name = "bulk_mgmt_entry")
public class BulkMgmtEntry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private BulkEntry bulkEntry;
	private String userMode;
	private long msgCount;
	private double totalCost;
	private String origMessage;
	private String campaignType;
	private String status;
	private String remarks;
	private String updateBy;
	private String updateOn;

	public BulkMgmtEntry() {
	}

	public BulkMgmtEntry(BulkEntry bulkEntry) {
		this.bulkEntry = bulkEntry;
	}

	public BulkEntry getBulkEntry() {
		if (bulkEntry == null) {
			bulkEntry = new BulkEntry();
		}
		return bulkEntry;
	}

	public void setBulkEntry(BulkEntry bulkEntry) {
		this.bulkEntry = bulkEntry;
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

	public String getOrigMessage() {
		return origMessage;
	}

	public void setOrigMessage(String origMessage) {
		this.origMessage = origMessage;
	}

	public String getCampaignType() {
		return campaignType;
	}

	public void setCampaignType(String campaignType) {
		this.campaignType = campaignType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}

	public String getUpdateOn() {
		return updateOn;
	}

	public void setUpdateOn(String updateOn) {
		this.updateOn = updateOn;
	}
}
