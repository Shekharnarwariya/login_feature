package com.hti.smpp.common.sms.request;

import org.springframework.stereotype.Component;

@Component
public class SmsRequest {

	private String senderId;
	private String messageType;
	private String message;
	private String from;
	private String destinationNumber;

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getDestinationNumber() {
		return destinationNumber;
	}

	public void setDestinationNumber(String destinationNumber) {
		this.destinationNumber = destinationNumber;
	}

	@Override
	public String toString() {
		return "SmsRequest [senderId=" + senderId + ", messageType=" + messageType + ", message=" + message + ", from="
				+ from + ", destinationNumber=" + destinationNumber + "]";
	}

}
