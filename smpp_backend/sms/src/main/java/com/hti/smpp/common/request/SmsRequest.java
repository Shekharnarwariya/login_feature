package com.hti.smpp.common.request;

import org.springframework.stereotype.Component;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Component
public class SmsRequest {

	@NotBlank(message = "Sender ID cannot be blank")
	private String senderId;

	@NotBlank(message = "From field cannot be blank")
	private String from;

	@NotBlank(message = "Destination number cannot be blank")
	private String destinationNumber;

	@NotBlank(message = "Message cannot be blank")
	private String message;

	@NotBlank(message = "Message type cannot be blank")
	private String messageType;

	@Min(value = 0, message = "Character count must be at least 0")
	private int charCount;

	@Min(value = 1, message = "SMS parts must be at least 1")
	private int smsParts;

	@Min(value = 1, message = "Character limit must be at least 1")
	private int charLimit;

	private long expiryHour;

	private boolean isSchedule;

	private String gmt = "";

	private String time;

	private String repeat;

	private String peId;

	private String templateId;

	private String telemarketerId;

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
	 * @return the destinationNumber
	 */
	public String getDestinationNumber() {
		return destinationNumber;
	}

	/**
	 * @param destinationNumber the destinationNumber to set
	 */
	public void setDestinationNumber(String destinationNumber) {
		this.destinationNumber = destinationNumber;
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
	 * @return the smsParts
	 */
	public int getSmsParts() {
		return smsParts;
	}

	/**
	 * @param smsParts the smsParts to set
	 */
	public void setSmsParts(int smsParts) {
		this.smsParts = smsParts;
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
	 * @return the isSchedule
	 */
	public boolean isSchedule() {
		return isSchedule;
	}

	/**
	 * @param isSchedule the isSchedule to set
	 */
	public void setSchedule(boolean isSchedule) {
		this.isSchedule = isSchedule;
	}

	/**
	 * @return the gmt
	 */
	public String getGmt() {
		return gmt;
	}

	/**
	 * @param gmt the gmt to set
	 */
	public void setGmt(String gmt) {
		this.gmt = gmt;
	}

	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * @return the repeat
	 */
	public String getRepeat() {
		return repeat;
	}

	/**
	 * @param repeat the repeat to set
	 */
	public void setRepeat(String repeat) {
		this.repeat = repeat;
	}

	/**
	 * @return the peId
	 */
	public String getPeId() {
		return peId;
	}

	/**
	 * @param peId the peId to set
	 */
	public void setPeId(String peId) {
		this.peId = peId;
	}

	/**
	 * @return the templateId
	 */
	public String getTemplateId() {
		return templateId;
	}

	/**
	 * @param templateId the templateId to set
	 */
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	/**
	 * @return the telemarketerId
	 */
	public String getTelemarketerId() {
		return telemarketerId;
	}

	/**
	 * @param telemarketerId the telemarketerId to set
	 */
	public void setTelemarketerId(String telemarketerId) {
		this.telemarketerId = telemarketerId;
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
