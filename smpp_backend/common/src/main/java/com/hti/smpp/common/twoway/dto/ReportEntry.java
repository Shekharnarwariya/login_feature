package com.hti.smpp.common.twoway.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table(name = "2way_report")
public class ReportEntry implements Cloneable {
	@Id
	@Column(name = "deliver_id")
	private int deliverId;
	@Column(name = "user_id")
	private int userId;
	@Column(name = "keyword_id")
	private int keywordId;
	@Column(name = "source")
	private String source;
	@Column(name = "short_code")
	private String shortCode;
	@Column(name = "received_text")
	private String receivedText;
	@Column(name = "receivedOn")
	private String receivedOn;
	@Column(name = "reply")
	private boolean reply;
	@Column(name = "reply_msg")
	private String replyMessage;
	@Column(name = "msg_id")
	private String messageId;
	@Column(name = "remarks")
	private String remarks;
	@Transient
	private String systemId;
	@Transient
	private String keyword;

	public ReportEntry() {
	}

	public ReportEntry(String source, String shortCode, String receivedText, String receivedOn, boolean reply,
			String replyMessage, String messageId, String remarks) {
		this.source = source;
		this.shortCode = shortCode;
		this.receivedText = receivedText;
		this.receivedOn = receivedOn;
		this.reply = reply;
		this.replyMessage = replyMessage;
		this.messageId = messageId;
		this.remarks = remarks;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getDeliverId() {
		return deliverId;
	}

	public void setDeliverId(int deliverId) {
		this.deliverId = deliverId;
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

	public int getKeywordId() {
		return keywordId;
	}

	public void setKeywordId(int keywordId) {
		this.keywordId = keywordId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public String toString() {
		return "Report: id=" + deliverId + ",userId=" + userId + ",shortCode=" + shortCode;
	}
}
