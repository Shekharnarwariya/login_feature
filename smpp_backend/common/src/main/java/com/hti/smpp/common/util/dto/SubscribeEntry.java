package com.hti.smpp.common.util.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table (name = "subscribe_entry")
public class SubscribeEntry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String pageName;
	private String sender;
	private String message;
	private String origMessage;
	private String messageType;
	private String countryCode;
	private String username;
	private String password;
	private String headerFileName;
	private String footerFileName;
	private String createdBy;
	private int groupId;

	public SubscribeEntry() {
	}

	public SubscribeEntry(String pageName, String sender, String message, String messageType, String countryCode,
			String username, String password, String headerFileName, String footerFileName, String origMessage) {
		this.pageName = pageName;
		this.sender = sender;
		this.message = message;
		this.messageType = messageType;
		this.countryCode = countryCode;
		this.username = username;
		this.password = password;
		this.headerFileName = headerFileName;
		this.footerFileName = footerFileName;
		this.origMessage = origMessage;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPageName() {
		return pageName;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHeaderFileName() {
		return headerFileName;
	}

	public void setHeaderFileName(String headerFileName) {
		this.headerFileName = headerFileName;
	}

	public String getFooterFileName() {
		return footerFileName;
	}

	public void setFooterFileName(String footerFileName) {
		this.footerFileName = footerFileName;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public String getOrigMessage() {
		return origMessage;
	}

	public void setOrigMessage(String origMessage) {
		this.origMessage = origMessage;
	}

	public String toString() {
		return "SubscribeEntry: id=" + id + ",pageName=" + pageName + ",sender=" + sender + ",message=" + message
				+ ",username=" + username + ",countryCode=" + countryCode + ",headerFileName=" + headerFileName
				+ ",footerFileName=" + footerFileName;
	}
}
