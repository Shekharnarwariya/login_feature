package com.hti.smpp.common.request;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

public class SubscribeEntryForm {
	private int id;// Unique identifier for the subscription entry
	private String pageName;// Name of the page associated with the subscription
	private String sender; // Sender information for the subscription
	private String message;// Message content for the subscription
	private String messageType;// Type of the message (e.g., text, multimedia)
	private int smsParts; // Sms Parts
	private int charCount; // Char Count
	private int charLimit; // Char limit per sms
	private String countryCode;// Country code associated with the subscription
	private String username;// Username associated with the subscription
	private String password;// Password associated with the subscription
	@Schema(hidden = true)
	private MultipartFile headerFile;// Header file attached to the subscription
	@Schema(hidden = true)
	private MultipartFile footerFile;// Footer file attached to the subscription
  
   // Getters and setters for each field...
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

	public int getSmsParts() {
		return smsParts;
	}

	public void setSmsParts(int smsParts) {
		this.smsParts = smsParts;
	}

	public int getCharCount() {
		return charCount;
	}

	public void setCharCount(int charCount) {
		this.charCount = charCount;
	}

	public int getCharLimit() {
		return charLimit;
	}

	public void setCharLimit(int charLimit) {
		this.charLimit = charLimit;
	}

	public void setMessage(String message) {
		this.message = message;
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

	public MultipartFile getHeaderFile() {
		return headerFile;
	}

	public void setHeaderFile(MultipartFile headerFile) {
		this.headerFile = headerFile;
	}

	public MultipartFile getFooterFile() {
		return footerFile;
	}

	public void setFooterFile(MultipartFile footerFile) {
		this.footerFile = footerFile;
	}

}
