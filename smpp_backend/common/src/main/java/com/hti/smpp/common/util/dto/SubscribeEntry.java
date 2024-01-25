package com.hti.smpp.common.util.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table (name = "subscription")
public class SubscribeEntry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name="page")
	private String pageName;
	
	@Column(name="sender")
	private String sender;
	
	@Column(name="message")
	private String message;
	
	@Column(name="orig_message")
	private String origMessage;
	
	@Column(name="msg_type")
	private String messageType;
	
	@Column(name="country_code")
	private String countryCode;
	
	@Column(name="username")
	private String username;
	
	@Column(name="password")
	private String password;
	
	@Column(name="header_file")
	private String headerFileName;
	
	@Column(name="footer_file")
	private String footerFileName;
	
	@Column(name="created_by")
	private String createdBy;
	
	@Column(name="group_id")
	private int groupId;

	public SubscribeEntry() {
	}

	public SubscribeEntry(int id, String pageName, String sender, String message, String origMessage,
			String messageType, String countryCode, String username, String password, String headerFileName,
			String footerFileName, String createdBy, int groupId) {
		super();
		this.id = id;
		this.pageName = pageName;
		this.sender = sender;
		this.message = message;
		this.origMessage = origMessage;
		this.messageType = messageType;
		this.countryCode = countryCode;
		this.username = username;
		this.password = password;
		this.headerFileName = headerFileName;
		this.footerFileName = footerFileName;
		this.createdBy = createdBy;
		this.groupId = groupId;
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

	public String getOrigMessage() {
		return origMessage;
	}

	public void setOrigMessage(String origMessage) {
		this.origMessage = origMessage;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getCountryCode() {
		return countryCode;
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

	
}
