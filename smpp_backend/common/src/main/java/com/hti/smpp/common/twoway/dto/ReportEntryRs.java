package com.hti.smpp.common.twoway.dto;

import org.springframework.stereotype.Component;

@Component
public class ReportEntryRs {
	
	private int userId;
	private String source;
	private String shortCode;
	private String receivedText;
	private String receivedOn;
	private boolean reply;
	private String replyMessage;
	private String messageId;
	private String remarks;
	private String systemId;
	private String prefix;
	private String suffix;
	public ReportEntryRs() {
		
	}
	public ReportEntryRs(int userId, String source, String shortCode, String receivedText, String receivedOn,
			boolean reply, String replyMessage, String messageId, String remarks, String systemId, String prefix,
			String suffix) {
		super();
		this.userId = userId;
		this.source = source;
		this.shortCode = shortCode;
		this.receivedText = receivedText;
		this.receivedOn = receivedOn;
		this.reply = reply;
		this.replyMessage = replyMessage;
		this.messageId = messageId;
		this.remarks = remarks;
		this.systemId = systemId;
		this.prefix = prefix;
		this.suffix = suffix;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getShortCode() {
		return shortCode;
	}
	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}
	public String getReceivedText() {
		return receivedText;
	}
	public void setReceivedText(String receivedText) {
		this.receivedText = receivedText;
	}
	public String getReceivedOn() {
		return receivedOn;
	}
	public void setReceivedOn(String receivedOn) {
		this.receivedOn = receivedOn;
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
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getSystemId() {
		return systemId;
	}
	public void setSystemId(String systemId) {
		this.systemId = systemId;
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
	
	

}
