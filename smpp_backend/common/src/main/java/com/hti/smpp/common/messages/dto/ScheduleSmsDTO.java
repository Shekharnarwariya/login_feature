
package com.hti.smpp.common.messages.dto;

import org.springframework.stereotype.Component;
//import  com.hti.smpp.common.messages.dto.BulkListInfo;

//import org.apache.struts.upload.FormFile;
//import org.springframework.web.multipart.MultipartFile;

@Component
public class ScheduleSmsDTO {

	private int serverId;
	private String clientId;
	private String senderId;
	private String ston;
	private String snpi;
	// private MultipartFile destinationNumberFile;
	// private FormFile destinationNumberFile;
	private String dton;
	private String dnpi;
	private String messageType;
	private String message;
	private String charCount;
	private String esmClass;
	private String dcsValue;
	// private ArrayList destinationList;
	private String systemId;
	private String password;
	private String header;
	private String destinationNumber;
	private String from;
	private String tMonth;
	private String tYear;
	private String tDay;
	private String tHour;
	private String tMinute;
	private String tSecond;
	private String sched_Update;
	private String schedDate;
	private String schedTime;
	private String fileName;
	private String reqType;
	private boolean customContent;
	private int totalNumbers;

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
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

	public int getTotalNumbers() {
		return totalNumbers;
	}

	public void setTotalNumbers(int totalNumbers) {
		this.totalNumbers = totalNumbers;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public void setSchedDate(String schedDate) {
		this.schedDate = schedDate;
	}

	public void setSchedTime(String schedTime) {
		this.schedTime = schedTime;
	}

	public void setSton(String ston) {
		this.ston = ston;
	}

	public void setSnpi(String snpi) {
		this.snpi = snpi;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
		// System.out.println(" set fileName in dto "+fileName);
	}

	/*
	 * public void setDestinationNumberFile(MultipartFile destinationNumberFile) {
	 * this.destinationNumberFile = destinationNumberFile; }
	 */

	public void setSched_Update(String sched_Update) {
		this.sched_Update = sched_Update;
		// System.out.println(" set sched_update "+sched_Update);
	}

	public void setDton(String dton) {
		this.dton = dton;
	}

	public void setDnpi(String dnpi) {
		this.dnpi = dnpi;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setCharCount(String charCount) {
		this.charCount = charCount;
	}

	public void setEsmClass(String esmClass) {
		this.esmClass = esmClass;
	}

	public void setDcsValue(String dcsValue) {
		this.dcsValue = dcsValue;
	}

//	public void setDestinationList(ArrayList destinationList) {
//		this.destinationList = destinationList;
//	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setDestinationNumber(String destinationNumber) {
		this.destinationNumber = destinationNumber;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setTYear(String tYear) {
		this.tYear = tYear;
	}

	public void setTMonth(String tMonth) {
		this.tMonth = tMonth;
		;
	}

	public void setTDay(String tDay) {
		this.tDay = tDay;
	}

	public void setTHour(String tHour) {
		this.tHour = tHour;
	}

	public void setTMinute(String tMinute) {
		this.tMinute = tMinute;
	}

	public void setTSecond(String tSecond) {
		this.tSecond = tSecond;
	}

	public String getClientId() {
		return clientId;
	}

	public String getSenderId() {
		return senderId;
	}

	public String getSton() {
		return ston;
	}

	public String getSchedDate() {
		return schedDate;
	}

	public String getSnpi() {
		return snpi;
	}

	public String getSched_Update() {
		// System.out.println(" return sched_update "+sched_Update);
		return sched_Update;
	}

	public String getFileName() {
		// System.out.println(" get fileName in dto "+fileName);
		return fileName;
	}

	/*
	 * public MultipartFile getDestinationNumberFile() { return
	 * destinationNumberFile; }
	 */

	public String getSchedTime() {
		return schedTime;
	}

	public String getDton() {
		return dton;
	}

	public String getDnpi() {
		return dnpi;
	}

	public String getMessageType() {
		return messageType;
	}

	public String getMessage() {
		return message;
	}

	public String getCharCount() {
		return charCount;
	}

	public String getEsmClass() {
		return esmClass;
	}

	public String getDcsValue() {
		return dcsValue;
	}

//	public ArrayList getDestinationList() {
//		return destinationList;
//	}

	public String getSystemId() {
		return systemId;
	}

	public String getPassword() {
		return password;
	}

	public String getDestinationNumber() {
		return destinationNumber;
	}

	public String getHeader() {
		return header;
	}

	public String getFrom() {
		return from;
	}

	public String getTYear() {
		return tYear;
	}

	public String getTMonth() {
		return tMonth;
	}

	public String getTDay() {
		return tDay;
	}

	public String getTHour() {
		return tHour;
	}

	public String getTMinute() {
		return tMinute;
	}

	public String getTSecond() {
		return tSecond;
	}

}
