package com.hti.smpp.common.twoway.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table(name = "2way_keyword")
public class KeywordEntry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "user_id")
	private int userId;
	@Column(name = "prefix")
	private String prefix;
	@Column(name = "suffix", length = 10)
	private String suffix;
	@Column(name = "type")
	private String type;
	@Column(name = "short_code", length = 15)
	private String shortCode;
	@Column(name = "expiresOn")
	private String expiresOn;
	@Column(name = "reply")
	private boolean reply;
	@Column(name = "success_msg")
	private String replyMessage;
	@Column(name = "failed_msg")
	private String replyOnFailed;
	@Column(name = "expire_msg")
	private String replyOnExpire;
	@Column(name = "reply_sender")
	private String replySender;
	@Column(name = "alert_number")
	private String alertNumber;
	@Column(name = "alert_email")
	private String alertEmail;
	@Column(name = "alert_url")
	private String alertUrl;
	@Column(name = "createdOn", updatable = false)
	private String createdOn;
	@Column(name = "createdBy", updatable = false)
	private String createdBy;
	@Column(name = "sources")
	private String sources;
	@Transient
	private String systemId;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public String getExpiresOn() {
		return expiresOn;
	}

	public void setExpiresOn(String expiresOn) {
		this.expiresOn = expiresOn;
	}

	public boolean isReply() {
		return reply;
	}

	public void setReply(boolean reply) {
		this.reply = reply;
	}

	public String getReplyMessage() {
		return replyMessage;
	}

	public void setReplyMessage(String replyMessage) {
		this.replyMessage = replyMessage;
	}

	public String getReplyOnFailed() {
		return replyOnFailed;
	}

	public void setReplyOnFailed(String replyOnFailed) {
		this.replyOnFailed = replyOnFailed;
	}

	public String getReplyOnExpire() {
		return replyOnExpire;
	}

	public void setReplyOnExpire(String replyOnExpire) {
		this.replyOnExpire = replyOnExpire;
	}

	public String getAlertNumber() {
		return alertNumber;
	}

	public void setAlertNumber(String alertNumber) {
		this.alertNumber = alertNumber;
	}

	public String getAlertEmail() {
		return alertEmail;
	}

	public void setAlertEmail(String alertEmail) {
		this.alertEmail = alertEmail;
	}

	public String getAlertUrl() {
		return alertUrl;
	}

	public void setAlertUrl(String alertUrl) {
		this.alertUrl = alertUrl;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getReplySender() {
		return replySender;
	}

	public void setReplySender(String replySender) {
		this.replySender = replySender;
	}

	public String getSources() {
		return sources;
	}

	public void setSources(String sources) {
		this.sources = sources;
	}

	public String toString() {
		return "{id: " + id + " ,UserId: " + userId + " ,Prefix: " + prefix + " ,Suffix: " + suffix + " ,Type: " + type
				+ " ,ShortCode: " + shortCode + " ,expiresOn: " + expiresOn + " ,isReply: " + reply + " ,Reply: "
				+ replyMessage + "}";
	}

}
