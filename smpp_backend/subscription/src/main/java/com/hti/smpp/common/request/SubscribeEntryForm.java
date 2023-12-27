package com.hti.smpp.common.request;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

public class SubscribeEntryForm {
	private int id;
	private String pageName;
	private String sender;
	private String message;
	private String messageType;
	private int smsParts; // Sms Parts
	private int charCount; // Char Count
	private int charLimit; // Char limit per sms
	private String asciiList;
	private String countryCode;
	private String username;
	private String password;
	@Schema(hidden = true)
	private MultipartFile headerFile;
	@Schema(hidden = true)
	private MultipartFile footerFile;

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

	public String getAsciiList() {
		return asciiList;
	}

	public void setAsciiList(String asciiList) {
		this.asciiList = asciiList;
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
