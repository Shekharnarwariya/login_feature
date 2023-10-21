package com.hti.smpp.common.messages.dto;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Entity bean with JPA annotations
 */
@Entity
@Table(name = "batch_process")
public class BulkEntry implements Serializable {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "system_id", updatable = false)
	private String systemId;
	@Column(name = "sender_id")
	private String senderId;
	@Column(name = "delay")
	private double delay;
	@Column(name = "reqType")
	private String reqType;
	@Column(name = "server_id", updatable = false)
	private int serverId;
	@Column(name = "ston")
	private int ston;
	@Column(name = "snpi")
	private int snpi;
	@Column(name = "msg_type")
	private String messageType;
	@Column(name = "alert")
	private boolean alert;
	@Column(name = "alert_number")
	private String alertNumbers;
	@Column(name = "expiry_hour")
	private long expiryHour;
	@Column(name = "createdOn", updatable = false)
	private String createdOn;
	@Column(name = "totalnum")
	private long total;
	@Column(name = "firstnum")
	private long firstNumber;
	@Column(name = "content")
	private String content;
	@Column(name = "campaign_name", updatable = false)
	private String campaignName;
	// ------ optional parameters for submit_sm ------
	@Column(name = "peId", updatable = false)
	private String peId;
	@Column(name = "templateId", updatable = false)
	private String templateId;
	@Column(name = "telemarketerId", updatable = false)
	private String telemarketerId;
	@Transient
	private long processed; // count of processed numbers
	@Transient
	private long pending; // count of processed numbers
	@Transient
	private boolean active; // status

	public BulkEntry() {
	}

	public BulkEntry(int id, String systemId, String senderId, double delay, String reqType, int serverId,
			String messageType, String createdOn, long total, long firstNumber, String content, String campaignName,
			long pending) {
		this.id = id;
		this.systemId = systemId;
		this.senderId = senderId;
		this.delay = delay;
		this.reqType = reqType;
		this.serverId = serverId;
		this.messageType = messageType;
		this.createdOn = createdOn;
		this.total = total;
		this.firstNumber = firstNumber;
		this.content = content;
		this.campaignName = campaignName;
		this.pending = pending;
	}

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

	public String getAlertNumbers() {
		return alertNumbers;
	}

	public void setAlertNumbers(String alertNumbers) {
		this.alertNumbers = alertNumbers;
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

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public long getPending() {
		return pending;
	}

	public void setPending(long pending) {
		this.pending = pending;
	}

	public String getPeId() {
		return peId;
	}

	public void setPeId(String peId) {
		this.peId = peId;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getTelemarketerId() {
		return telemarketerId;
	}

	public void setTelemarketerId(String telemarketerId) {
		this.telemarketerId = telemarketerId;
	}

	@Override
	public String toString() {
		return "id=" + id + ",serverId=" + serverId + ",systemId=" + systemId + ",firstNumber=" + firstNumber
				+ ",messageType" + messageType + ",totalNumbers=" + total + ",processed=" + processed;
	}
}
