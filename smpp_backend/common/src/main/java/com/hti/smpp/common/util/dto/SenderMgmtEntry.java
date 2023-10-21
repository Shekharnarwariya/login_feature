package com.hti.smpp.common.util.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "send_mgm_entry")
public class SenderMgmtEntry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String sender;
	private String systemId;
	private String remarks;
	private String status;
	private String addedBy;
	private String addedOn;
	private String updateBy;
	private String updateOn;
	private String senderType;
	private String alertFlag;

	public SenderMgmtEntry() {
	}

	public SenderMgmtEntry(String sender, String systemId, String remarks, String addedBy, String addedOn,
			String senderType) {
		this.sender = sender;
		this.systemId = systemId;
		this.remarks = remarks;
		this.addedBy = addedBy;
		this.addedOn = addedOn;
		this.senderType = senderType;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAddedBy() {
		return addedBy;
	}

	public void setAddedBy(String addedBy) {
		this.addedBy = addedBy;
	}

	public String getAddedOn() {
		return addedOn;
	}

	public void setAddedOn(String addedOn) {
		this.addedOn = addedOn;
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

	public String getSenderType() {
		return senderType;
	}

	public void setSenderType(String senderType) {
		this.senderType = senderType;
	}

	public String getAlertFlag() {
		return alertFlag;
	}

	public void setAlertFlag(String alertFlag) {
		this.alertFlag = alertFlag;
	}

	public String toString() {
		return "SenderMgmtEntry: id=" + id + ",sender=" + sender + ",systemId=" + systemId + ",remarks=" + remarks;
	}
}
