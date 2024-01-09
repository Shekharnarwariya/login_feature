package com.hti.smpp.common.request;

public class BulkContactUploadForm {
	private int id;
	private String senderId;
	private String ston;
	private String snpi;
	private String destinationNumber;
	private String dton;
	private String dnpi;
	private String message;
	private String esmClass;
	private String header;
	private String dcsValue;
	private String from;
	private String syear;
	private String sday;
	private String smonth;
	private String hours;
	private String minutes;
	private String time;
	private String date;
	private String greet;
	private String temp;
	private int hideBulk;
	private String complete;
	private String schedTime;
	private String schedDate;
	// private boolean boolvalue;
	// private int processedFigure;
	private String[] numberlist;
	private String totalNumbers;
	private String uploadedNumbers;
	private String noOfMessage;
	private String[] grouplist;
	private String asciiList;
	private String fileName;
	// private String schType = "contact";
	private String reCheck = "no";
	private String reqType = "contact";
	private boolean customContent;
	private String gmt = "";
	private String smscount;
	// private String addGroup;
	private int groupId;
	private String masterId;
	private double delay;
	private String repeat;
	private boolean isAlert;
	private boolean isSchedule;
	private String timestart;
	// ------------ Modified on 27-Feb-2016 ---------------
	private String origMessage; // For summary
	private String messageType; // Encoding
	private int smsParts; // Sms Parts
	private int charCount; // Char Count
	private int charLimit; // Char limit per sms
	private String username;
	private long expiryHour;
	private String campaignName;
	private boolean tracking;
	private String[] weblink; // for tracking sms
	private String exclude; // excluded numbers from uploaded file
	// ------ optional parameters for submit_sm ------
	private String peId;
	private String templateId;
	private String telemarketerId;

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
	 * @return the ston
	 */
	public String getSton() {
		return ston;
	}

	/**
	 * @param ston the ston to set
	 */
	public void setSton(String ston) {
		this.ston = ston;
	}

	/**
	 * @return the snpi
	 */
	public String getSnpi() {
		return snpi;
	}

	/**
	 * @param snpi the snpi to set
	 */
	public void setSnpi(String snpi) {
		this.snpi = snpi;
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
	 * @return the dton
	 */
	public String getDton() {
		return dton;
	}

	/**
	 * @param dton the dton to set
	 */
	public void setDton(String dton) {
		this.dton = dton;
	}

	/**
	 * @return the dnpi
	 */
	public String getDnpi() {
		return dnpi;
	}

	/**
	 * @param dnpi the dnpi to set
	 */
	public void setDnpi(String dnpi) {
		this.dnpi = dnpi;
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
	 * @return the esmClass
	 */
	public String getEsmClass() {
		return esmClass;
	}

	/**
	 * @param esmClass the esmClass to set
	 */
	public void setEsmClass(String esmClass) {
		this.esmClass = esmClass;
	}

	/**
	 * @return the header
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * @param header the header to set
	 */
	public void setHeader(String header) {
		this.header = header;
	}

	/**
	 * @return the dcsValue
	 */
	public String getDcsValue() {
		return dcsValue;
	}

	/**
	 * @param dcsValue the dcsValue to set
	 */
	public void setDcsValue(String dcsValue) {
		this.dcsValue = dcsValue;
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
	 * @return the syear
	 */
	public String getSyear() {
		return syear;
	}

	/**
	 * @param syear the syear to set
	 */
	public void setSyear(String syear) {
		this.syear = syear;
	}

	/**
	 * @return the sday
	 */
	public String getSday() {
		return sday;
	}

	/**
	 * @param sday the sday to set
	 */
	public void setSday(String sday) {
		this.sday = sday;
	}

	/**
	 * @return the smonth
	 */
	public String getSmonth() {
		return smonth;
	}

	/**
	 * @param smonth the smonth to set
	 */
	public void setSmonth(String smonth) {
		this.smonth = smonth;
	}

	/**
	 * @return the hours
	 */
	public String getHours() {
		return hours;
	}

	/**
	 * @param hours the hours to set
	 */
	public void setHours(String hours) {
		this.hours = hours;
	}

	/**
	 * @return the minutes
	 */
	public String getMinutes() {
		return minutes;
	}

	/**
	 * @param minutes the minutes to set
	 */
	public void setMinutes(String minutes) {
		this.minutes = minutes;
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
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * @return the greet
	 */
	public String getGreet() {
		return greet;
	}

	/**
	 * @param greet the greet to set
	 */
	public void setGreet(String greet) {
		this.greet = greet;
	}

	/**
	 * @return the temp
	 */
	public String getTemp() {
		return temp;
	}

	/**
	 * @param temp the temp to set
	 */
	public void setTemp(String temp) {
		this.temp = temp;
	}

	/**
	 * @return the hideBulk
	 */
	public int getHideBulk() {
		return hideBulk;
	}

	/**
	 * @param hideBulk the hideBulk to set
	 */
	public void setHideBulk(int hideBulk) {
		this.hideBulk = hideBulk;
	}

	/**
	 * @return the complete
	 */
	public String getComplete() {
		return complete;
	}

	/**
	 * @param complete the complete to set
	 */
	public void setComplete(String complete) {
		this.complete = complete;
	}

	/**
	 * @return the schedTime
	 */
	public String getSchedTime() {
		return schedTime;
	}

	/**
	 * @param schedTime the schedTime to set
	 */
	public void setSchedTime(String schedTime) {
		this.schedTime = schedTime;
	}

	/**
	 * @return the schedDate
	 */
	public String getSchedDate() {
		return schedDate;
	}

	/**
	 * @param schedDate the schedDate to set
	 */
	public void setSchedDate(String schedDate) {
		this.schedDate = schedDate;
	}

	/**
	 * @return the numberlist
	 */
	public String[] getNumberlist() {
		return numberlist;
	}

	/**
	 * @param numberlist the numberlist to set
	 */
	public void setNumberlist(String[] numberlist) {
		this.numberlist = numberlist;
	}

	/**
	 * @return the totalNumbers
	 */
	public String getTotalNumbers() {
		return totalNumbers;
	}

	/**
	 * @param totalNumbers the totalNumbers to set
	 */
	public void setTotalNumbers(String totalNumbers) {
		this.totalNumbers = totalNumbers;
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
	 * @return the noOfMessage
	 */
	public String getNoOfMessage() {
		return noOfMessage;
	}

	/**
	 * @param noOfMessage the noOfMessage to set
	 */
	public void setNoOfMessage(String noOfMessage) {
		this.noOfMessage = noOfMessage;
	}

	/**
	 * @return the grouplist
	 */
	public String[] getGrouplist() {
		return grouplist;
	}

	/**
	 * @param grouplist the grouplist to set
	 */
	public void setGrouplist(String[] grouplist) {
		this.grouplist = grouplist;
	}

	/**
	 * @return the asciiList
	 */
	public String getAsciiList() {
		return asciiList;
	}

	/**
	 * @param asciiList the asciiList to set
	 */
	public void setAsciiList(String asciiList) {
		this.asciiList = asciiList;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the reCheck
	 */
	public String getReCheck() {
		return reCheck;
	}

	/**
	 * @param reCheck the reCheck to set
	 */
	public void setReCheck(String reCheck) {
		this.reCheck = reCheck;
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
	 * @return the customContent
	 */
	public boolean isCustomContent() {
		return customContent;
	}

	/**
	 * @param customContent the customContent to set
	 */
	public void setCustomContent(boolean customContent) {
		this.customContent = customContent;
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
	 * @return the masterId
	 */
	public String getMasterId() {
		return masterId;
	}

	/**
	 * @param masterId the masterId to set
	 */
	public void setMasterId(String masterId) {
		this.masterId = masterId;
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
	 * @return the origMessage
	 */
	public String getOrigMessage() {
		return origMessage;
	}

	/**
	 * @param origMessage the origMessage to set
	 */
	public void setOrigMessage(String origMessage) {
		this.origMessage = origMessage;
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
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
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
