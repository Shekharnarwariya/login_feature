package com.hti.smpp.common.request;

import jakarta.validation.constraints.Min;

public class BulkAutoScheduleRequest {

	private String senderId;

	private String message;

	private String from;

	@Min(value = 0, message = "Character count must be at least 0")
	private int charCount;

	@Min(value = 1, message = "Character limit must be at least 1")
	private int charLimit;

	private String smscount;

	private String messageType;

	private boolean tracking;

	private String[] weblink;

	private String campaignName;

	/**
	 * @return the campaignName
	 */
	public String getCampaignName() {
		return campaignName;
	}

	/**
	 * @param campaignName the campaignName to set
	 */
	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
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
	 * @return the charCount
	 */
	public int getCharCount() {
		return charCount;
	}

	/**
	 * @param charCount the charCount to set
	 */
	public void setCharCount(int charCount) {
		this.charCount = charCount;
	}

	/**
	 * @return the charLimit
	 */
	public int getCharLimit() {
		return charLimit;
	}

	/**
	 * @param charLimit the charLimit to set
	 */
	public void setCharLimit(int charLimit) {
		this.charLimit = charLimit;
	}

	/**
	 * @return the smscount
	 */
	public String getSmscount() {
		return smscount;
	}

	/**
	 * @param smscount the smscount to set
	 */
	public void setSmscount(String smscount) {
		this.smscount = smscount;
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
	 * @return the tracking
	 */
	public boolean isTracking() {
		return tracking;
	}

	/**
	 * @param tracking the tracking to set
	 */
	public void setTracking(boolean tracking) {
		this.tracking = tracking;
	}

	/**
	 * @return the weblink
	 */
	public String[] getWeblink() {
		return weblink;
	}

	/**
	 * @param weblink the weblink to set
	 */
	public void setWeblink(String[] weblink) {
		this.weblink = weblink;
	}

}
