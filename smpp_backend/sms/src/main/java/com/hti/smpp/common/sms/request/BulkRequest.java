package com.hti.smpp.common.sms.request;

public class BulkRequest {
	private String senderId;
	private String ston;
	private String snpi;
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
	private String asciiList;
	private String temp;
	private String smscName;
	private String gmt = "";
	private String[] smscList;
	private String[] numberlist;
	private String smscount;
	private String reqType = "bulk";
	private boolean customContent;
	private String totalSmsParDay;
	// private int processedFigure;
	private String totalNumbers;
	private String uploadedNumbers;
	// private String schType;
	private String timestart;
	private String destinationNumber;
	private String clientId;
	private int[] id;
	private String[] user;
	private double delay;
	private String repeat;
	// private int fileid;
	private int batchId;
	private String fileName;
	private boolean isSchedule;
	private boolean isAlert;
	private boolean allowDuplicate;
	// ------------ Modified on 27-Feb-2016 ---------------
	private String origMessage; // For summary
	private String messageType; // Encoding
	private int smsParts; // Sms Parts
	private int charCount; // Char Count
	private int charLimit; // Char limit per sms
	private String exclude; // excluded numbers from uploaded file
	private String status;
	private long expiryHour;
	private boolean tracking;
	private String[] weblink; // for tracking sms
	private String campaignName;
	// ------ optional parameters for submit_sm ------
	private String peId;
	private String templateId;
	private String telemarketerId;

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public String getSton() {
		return ston;
	}

	public void setSton(String ston) {
		this.ston = ston;
	}

	public String getSnpi() {
		return snpi;
	}

	public void setSnpi(String snpi) {
		this.snpi = snpi;
	}

	public String getDton() {
		return dton;
	}

	public void setDton(String dton) {
		this.dton = dton;
	}

	public String getDnpi() {
		return dnpi;
	}

	public void setDnpi(String dnpi) {
		this.dnpi = dnpi;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getEsmClass() {
		return esmClass;
	}

	public void setEsmClass(String esmClass) {
		this.esmClass = esmClass;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getDcsValue() {
		return dcsValue;
	}

	public void setDcsValue(String dcsValue) {
		this.dcsValue = dcsValue;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSyear() {
		return syear;
	}

	public void setSyear(String syear) {
		this.syear = syear;
	}

	public String getSday() {
		return sday;
	}

	public void setSday(String sday) {
		this.sday = sday;
	}

	public String getSmonth() {
		return smonth;
	}

	public void setSmonth(String smonth) {
		this.smonth = smonth;
	}

	public String getHours() {
		return hours;
	}

	public void setHours(String hours) {
		this.hours = hours;
	}

	public String getMinutes() {
		return minutes;
	}

	public void setMinutes(String minutes) {
		this.minutes = minutes;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getGreet() {
		return greet;
	}

	public void setGreet(String greet) {
		this.greet = greet;
	}

	public String getAsciiList() {
		return asciiList;
	}

	public void setAsciiList(String asciiList) {
		this.asciiList = asciiList;
	}

	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}

	public String getSmscName() {
		return smscName;
	}

	public void setSmscName(String smscName) {
		this.smscName = smscName;
	}

	public String getGmt() {
		return gmt;
	}

	public void setGmt(String gmt) {
		this.gmt = gmt;
	}

	public String[] getSmscList() {
		return smscList;
	}

	public void setSmscList(String[] smscList) {
		this.smscList = smscList;
	}

	public String[] getNumberlist() {
		return numberlist;
	}

	public void setNumberlist(String[] numberlist) {
		this.numberlist = numberlist;
	}

	public String getSmscount() {
		return smscount;
	}

	public void setSmscount(String smscount) {
		this.smscount = smscount;
	}

	public String getReqType() {
		return reqType;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
	}

	public boolean isCustomContent() {
		return customContent;
	}

	public void setCustomContent(boolean customContent) {
		this.customContent = customContent;
	}

	public String getTotalSmsParDay() {
		return totalSmsParDay;
	}

	public void setTotalSmsParDay(String totalSmsParDay) {
		this.totalSmsParDay = totalSmsParDay;
	}

	public String getTotalNumbers() {
		return totalNumbers;
	}

	public void setTotalNumbers(String totalNumbers) {
		this.totalNumbers = totalNumbers;
	}

	public String getUploadedNumbers() {
		return uploadedNumbers;
	}

	public void setUploadedNumbers(String uploadedNumbers) {
		this.uploadedNumbers = uploadedNumbers;
	}

	public String getTimestart() {
		return timestart;
	}

	public void setTimestart(String timestart) {
		this.timestart = timestart;
	}

	public String getDestinationNumber() {
		return destinationNumber;
	}

	public void setDestinationNumber(String destinationNumber) {
		this.destinationNumber = destinationNumber;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public int[] getId() {
		return id;
	}

	public void setId(int[] id) {
		this.id = id;
	}

	public String[] getUser() {
		return user;
	}

	public void setUser(String[] user) {
		this.user = user;
	}

	public double getDelay() {
		return delay;
	}

	public void setDelay(double delay) {
		this.delay = delay;
	}

	public String getRepeat() {
		return repeat;
	}

	public void setRepeat(String repeat) {
		this.repeat = repeat;
	}

	public int getBatchId() {
		return batchId;
	}

	public void setBatchId(int batchId) {
		this.batchId = batchId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean isSchedule() {
		return isSchedule;
	}

	public void setSchedule(boolean isSchedule) {
		this.isSchedule = isSchedule;
	}

	public boolean isAlert() {
		return isAlert;
	}

	public void setAlert(boolean isAlert) {
		this.isAlert = isAlert;
	}

	public boolean isAllowDuplicate() {
		return allowDuplicate;
	}

	public void setAllowDuplicate(boolean allowDuplicate) {
		this.allowDuplicate = allowDuplicate;
	}

	public String getOrigMessage() {
		return origMessage;
	}

	public void setOrigMessage(String origMessage) {
		this.origMessage = origMessage;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
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

	public String getExclude() {
		return exclude;
	}

	public void setExclude(String exclude) {
		this.exclude = exclude;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getExpiryHour() {
		return expiryHour;
	}

	public void setExpiryHour(long expiryHour) {
		this.expiryHour = expiryHour;
	}

	public boolean isTracking() {
		return tracking;
	}

	public void setTracking(boolean tracking) {
		this.tracking = tracking;
	}

	public String[] getWeblink() {
		return weblink;
	}

	public void setWeblink(String[] weblink) {
		this.weblink = weblink;
	}

	public String getCampaignName() {
		return campaignName;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	public String getPeId() {
		return peId;
	}

	public void setPeId(String peId) {
		this.peId = peId;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getTelemarketerId() {
		return telemarketerId;
	}

	public void setTelemarketerId(String telemarketerId) {
		this.telemarketerId = telemarketerId;
	}
}