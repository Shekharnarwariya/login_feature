package com.hti.smpp.common.request;

import jakarta.validation.constraints.NotBlank;

public class BulkUpdateRequest {

	private int id;
	
	private String reqType = "bulk";

	@NotBlank(message = "Sender ID cannot be blank")
	private String senderId;

	@NotBlank(message = "Message cannot be blank")
	private String message;

	@NotBlank(message = "From field cannot be blank")
	private String from;

	@NotBlank(message = "Message type cannot be blank")
	private String messageType; // Encoding

	private double delay;

	private long expiryHour;

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the reqType
	 */
	public String getReqType() {
		return reqType;
	}

	/**
	 * @param reqType the reqType to set
	 */
	public void setReqType(String reqType) {
		this.reqType = reqType;
	}

	/**
	 * @return the senderId
	 */
	public String getSenderId() {
		return senderId;
	}

	/**
	 * @param senderId the senderId to set
	 */
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @param from the from to set
	 */
	public void setFrom(String from) {
		this.from = from;
	}

	/**
	 * @return the messageType
	 */
	public String getMessageType() {
		return messageType;
	}

	/**
	 * @param messageType the messageType to set
	 */
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	/**
	 * @return the delay
	 */
	public double getDelay() {
		return delay;
	}

	/**
	 * @param delay the delay to set
	 */
	public void setDelay(double delay) {
		this.delay = delay;
	}

	/**
	 * @return the expiryHour
	 */
	public long getExpiryHour() {
		return expiryHour;
	}

	/**
	 * @param expiryHour the expiryHour to set
	 */
	public void setExpiryHour(long expiryHour) {
		this.expiryHour = expiryHour;
	}


}