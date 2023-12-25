package com.hti.smpp.common.request;

import java.util.ArrayList;

public class CustomReportDTO {
	private String sday;
	private String smonth;
	private String syear;
	private String shour;
	private String smin;
	private String ssec;
	private String eday = null;
	private String emonth = null;
	private String eyear = null;
	private String ehour;
	private String emin;
	private String esec;
	private String clientId;
	private String senderId;
	private String destinationNumber;
	private String messageStatus;
	private int startIndex;
	private int lastIndex;
	private String template;
	private String mode;
	private String status;
	private String clientName;
	private String smscName;
	private String country;
	private String operator;
	private String paidAmount;
	private String totalSmsSend;
	private String paidSms;
	private String smsVolume;
	private String reportType = null;
	private ArrayList userList = null;
	// private ArrayList dateList =null;
	private ArrayList countList = null;
	private ArrayList costList = null;
	private ArrayList destinationList = null;
	private String startdate;
	private String enddate;
	private String messageId;
	private String campaign;

	public String getCampaign() {
		return campaign;
	}

	public void setCampaign(String campaign) {
		this.campaign = campaign;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public void setSsec(String ssec) {
		this.ssec = ssec;
	}

	public void setSmin(String smin) {
		this.smin = smin;
	}

	public void setShour(String shour) {
		this.shour = shour;
	}

	public void setSday(String sday) {
		this.sday = sday;
	}

	public void setSmonth(String smonth) {
		this.smonth = smonth;
	}

	public void setSyear(String syear) {
		this.syear = syear;
	}

	public void setEsec(String esec) {
		this.esec = esec;
	}

	public void setEmin(String emin) {
		this.emin = emin;
	}

	public void setEhour(String ehour) {
		this.ehour = ehour;
	}

	public void setEday(String eday) {
		this.eday = eday;
	}

	public void setEmonth(String emonth) {
		this.emonth = emonth;
	}

	public void setEyear(String eyear) {
		this.eyear = eyear;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public void setSmscName(String smscName) {
		this.smscName = smscName;
	}

	public void setDestinationNumber(String destinationNumber) {
		this.destinationNumber = destinationNumber;
	}

	public void setMessageStatus(String messageStatus) {
		this.messageStatus = messageStatus;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public void setLastIndex(int lastIndex) {
		this.lastIndex = lastIndex;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public void setMode(String mode) {
		this.mode = mode;
		// System.out.println("set mode ::::::"+mode);
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public void setPaidAmount(String paidAmount) {
		this.paidAmount = paidAmount;
	}

	public void setTotalSmsSend(String totalSmsSend) {
		this.totalSmsSend = totalSmsSend;
	}

	public void setPaidSms(String paidSms) {
		this.paidSms = paidSms;
	}

	public void setSmsVolume(String smsVolume) {
		this.smsVolume = smsVolume;
	}

	public void setUserList(ArrayList userList) {
		this.userList = userList;
	}
	// public void setDateList(ArrayList dateList){
	// this.dateList = dateList;
	// }

	public void setCountList(ArrayList countList) {
		this.countList = countList;
	}

	public void setCostList(ArrayList costList) {
		this.costList = costList;
	}

	public void setDestinationList(ArrayList destinationList) {
		this.destinationList = destinationList;
	}

	public void setStartdate(String startdate) {
		this.startdate = startdate;
	}

	public void setEnddate(String enddate) {
		this.enddate = enddate;
	}

	public void setCountry(String country) {
		this.country = country;
	}
	////////////////////////////////////////////////////////////////////////////

	public String getCountry() {
		return country;
	}

	public String getStartdate() {
		return startdate;
	}

	public String getMessageId() {
		return messageId;
	}

	public String getEnddate() {
		return enddate;
	}

	public ArrayList getDestinationList() {
		return destinationList;
	}

	public ArrayList getUserList() {
		return userList;
	}
	// public ArrayList getDateList(){
	// return dateList;
	// }

	public ArrayList getCountList() {
		return countList;
	}

	public ArrayList getCostList() {
		return costList;
	}

	public String getReportType() {
		return reportType;
	}

	public String getSsec() {
		return ssec;
	}

	public String getSmin() {
		return smin;
	}

	public String getShour() {
		return shour;
	}

	public String getSday() {
		return sday;
	}

	public String getSmonth() {
		return smonth;
	}

	public String getSyear() {
		return syear;
	}

	public String getEsec() {
		return esec;
	}

	public String getEmin() {
		return emin;
	}

	public String getEhour() {
		return ehour;
	}

	public String getEday() {
		return eday;
	}

	public String getEmonth() {
		return emonth;
	}

	public String getEyear() {
		return eyear;
	}

	public String getClientId() {
		return clientId;
	}

	public String getSenderId() {
		return senderId;
	}

	public String getSmscName() {
		return smscName;
	}

	public String getDestinationNumber() {
		return destinationNumber;
	}

	public String getMessageStatus() {
		return messageStatus;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getLastIndex() {
		return lastIndex;
	}

	public String getStartDate() {
		String date1 = syear + "-" + smonth + "-" + sday + " " + shour + ":" + smin + ":01";
		// System.out.println("startDate"+date1);
		return date1;
	}

	public String getEndDate() {
		String date2 = eyear + "-" + emonth + "-" + eday + " " + ehour + ":" + emin + ":59";
		// System.out.println("EndDate"+date2);
		return date2;
	}

	public String getTemplate() {
		return template;
	}

	public String getMode() {
		// System.out.println("get mode ::::::"+mode);
		return mode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getClientName() {
		return clientName;
	}

	public String getPaidAmount() {
		return paidAmount;
	}

	public String getTotalSmsSend() {
		return totalSmsSend;
	}

	public String getPaidSms() {
		return paidSms;
	}

	public String getSmsVolume() {
		return smsVolume;
	}

	public String toString() {
		return "CustomReportCriteria: Username=" + clientId + ",messageId=" + messageId + ",status=" + messageStatus
				+ ",start=" + sday + ",End=" + eday + ",SenderId=" + senderId + ",Country=" + country + ",Operator="
				+ operator + ",ReportType=" + reportType;
	}
}
