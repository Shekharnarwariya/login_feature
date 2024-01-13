package com.hti.smpp.common.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class BulkContactRequest {

	@NotBlank(message = "Sender ID cannot be blank")
	private String senderId;

	@NotBlank(message = "Message cannot be blank")
	private String message;

	@NotBlank(message = "From field cannot be blank")
	private String from;

	@NotBlank(message = "Message type cannot be blank")
	private String messageType; // Encoding

	@Min(value = 1, message = "SMS parts must be at least 1")
	private int smsParts; // Sms Parts

	@Min(value = 0, message = "Character count must be at least 0")
	private int charCount; // Char Count

	@Min(value = 1, message = "Character limit must be at least 1")
	private int charLimit; // Char limit per sms

	private String gmt = "";

	@NotBlank(message = "sms count cannot be blank")
	private String smscount;

	private String destinationNumber;

	private String uploadedNumbers;

	private String totalContact;

	private double delay;

	private String repeat;

	private boolean alert;

	private boolean schedule;

	private String timestart;

	private long expiryHour;

	private String campaignName;

	private boolean tracking;

	private String[] weblink; // for tracking sms

	private String exclude; // excluded numbers from uploaded file

	private String peId;

	private String templateId;

	private String telemarketerId;

	private int groupId;

	/**
	 * @return the groupId
	 */
	public int getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(int groupId) {
		this.groupId = groupId;
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
	 * @return the uploadedNumbers
	 */
	public String getUploadedNumbers() {
		return uploadedNumbers;
	}

	/**
	 * @param uploadedNumbers the uploadedNumbers to set
	 */
	public void setUploadedNumbers(String uploadedNumbers) {
		this.uploadedNumbers = uploadedNumbers;
	}

	/**
	 * @return the totalContact
	 */
	public String isTotalContact() {
		return totalContact;
	}

	/**
	 * @param totalContact the totalContact to set
	 */
	public void setTotalContact(String customContent) {
		this.totalContact = customContent;
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
	 * @return the alert
	 */
	public boolean isAlert() {
		return alert;
	}

	/**
	 * @param alert the alert to set
	 */
	public void setAlert(boolean alert) {
		this.alert = alert;
	}

	/**
	 * @return the schedule
	 */
	public boolean isSchedule() {
		return schedule;
	}

	/**
	 * @param schedule the schedule to set
	 */
	public void setSchedule(boolean schedule) {
		this.schedule = schedule;
	}

	/**
	 * @return the timestart
	 */
	public String getTimestart() {
		return timestart;
	}

	/**
	 * @param timestart the timestart to set
	 */
	public void setTimestart(String timestart) {
		this.timestart = timestart;
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

	/**
	 * @return the exclude
	 */
	public String getExclude() {
		return exclude;
	}

	/**
	 * @param exclude the exclude to set
	 */
	public void setExclude(String exclude) {
		this.exclude = exclude;
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

}
