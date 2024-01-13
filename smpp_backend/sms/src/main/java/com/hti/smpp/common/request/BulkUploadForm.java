package com.hti.smpp.common.request;

public class BulkUploadForm {
	private String senderId;
	private String message;
	private String from;
	private String gmt = "";
	private String smscount;
	private String timestart;
	private String destinationNumber;
	private String repeat;
	private boolean isSchedule;
	private boolean isAlert;
	private boolean allowDuplicate;
	// ------------ Modified on 27-Feb-2016 ---------------
	private String messageType; // Encoding
	private int smsParts; // Sms Parts
	private int charCount; // Char Count
	private int charLimit; // Char limit per sms
	private String exclude; // excluded numbers from uploaded file
	private long expiryHour;
	private boolean tracking;
	private String[] weblink; // for tracking sms
	private String campaignName;
	// ------ optional parameters for submit_sm ------
	private String peId;
	private String templateId;
	private String telemarketerId;
	// -------- mms optional param -------
	private String caption;
	private String mmsType; // audio/video/vcard/image

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
	 * @return the isAlert
	 */
	public boolean isAlert() {
		return isAlert;
	}

	/**
	 * @param isAlert the isAlert to set
	 */
	public void setAlert(boolean isAlert) {
		this.isAlert = isAlert;
	}

	/**
	 * @return the allowDuplicate
	 */
	public boolean isAllowDuplicate() {
		return allowDuplicate;
	}

	/**
	 * @param allowDuplicate the allowDuplicate to set
	 */
	public void setAllowDuplicate(boolean allowDuplicate) {
		this.allowDuplicate = allowDuplicate;
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
	 * @return the caption
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * @param caption the caption to set
	 */
	public void setCaption(String caption) {
		this.caption = caption;
	}

	/**
	 * @return the mmsType
	 */
	public String getMmsType() {
		return mmsType;
	}

	/**
	 * @param mmsType the mmsType to set
	 */
	public void setMmsType(String mmsType) {
		this.mmsType = mmsType;
	}

}
