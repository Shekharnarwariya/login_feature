
package com.hti.smpp.common.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CustomizedReportRequest {
	private String smin;
	private String shour;
	private String sday;
	private String smonth;
	private String syear;
	private String ssec;
	private String mode;
	@NotBlank(message = "Status must not be blank")
	private String status;
	private String esec;
	private String emin;
	private String ehour;
	private String eday;
	private String emonth;
	private String eyear;
	private String senderId;
	
	private String destinationNumber;

	@NotBlank(message = "ReportType must not be blank")
	private String reportType;

	@NotBlank(message = "ClientId must not be blank")
	private String clientId;
	private String country;
	private String operator;
	private String messageId;
	private String check_A;

	@NotBlank(message = "GroupBy must not be blank")
	private String groupBy;
	private String campaign;
	private String campaignType;

	@NotBlank(message = "EndDate must not be blank")
	private String endDate;

	@NotBlank(message = "StartDate must not be blank")
	private String startDate;
	private String Content;
	private String ContentType;
	private String Check_F;
	private String SmscName;
	
	
	public String getSmscName() {
		return SmscName;
	}
	public void setSmscName(String smscName) {
		SmscName = smscName;
	}
	public String getContent() {
		return Content;
	}
	public void setContent(String content) {
		Content = content;
	}
	public String getSmin() {
		return smin;
	}
	public void setSmin(String smin) {
		this.smin = smin;
	}
	public String getShour() {
		return shour;
	}
	public void setShour(String shour) {
		this.shour = shour;
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
	public String getSyear() {
		return syear;
	}
	public void setSyear(String syear) {
		this.syear = syear;
	}
	public String getSsec() {
		return ssec;
	}
	public void setSsec(String ssec) {
		this.ssec = ssec;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getEsec() {
		return esec;
	}
	public void setEsec(String esec) {
		this.esec = esec;
	}
	public String getEmin() {
		return emin;
	}
	public void setEmin(String emin) {
		this.emin = emin;
	}
	public String getEhour() {
		return ehour;
	}
	public void setEhour(String ehour) {
		this.ehour = ehour;
	}
	public String getEday() {
		return eday;
	}
	public void setEday(String eday) {
		this.eday = eday;
	}
	public String getEmonth() {
		return emonth;
	}
	public void setEmonth(String emonth) {
		this.emonth = emonth;
	}
	public String getEyear() {
		return eyear;
	}
	public void setEyear(String eyear) {
		this.eyear = eyear;
	}
	public String getSenderId() {
		return senderId;
	}
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
	public String getDestinationNumber() {
		return destinationNumber;
	}
	public void setDestinationNumber(String destinationNumber) {
		this.destinationNumber = destinationNumber;
	}
	public String getReportType() {
		return reportType;
	}
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public String getCheck_A() {
		return check_A;
	}
	public void setCheck_A(String check_A) {
		this.check_A = check_A;
	}
	public String getGroupBy() {
		return groupBy;
	}
	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}
	public String getCampaign() {
		return campaign;
	}
	public void setCampaign(String campaign) {
		this.campaign = campaign;
	}
	public String getCampaignType() {
		return campaignType;
	}
	public void setCampaignType(String campaignType) {
		this.campaignType = campaignType;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getContentType() {
		return ContentType;
	}
	public void setContentType(String contentType) {
		ContentType = contentType;
	}
	public String getCheck_F() {
		return Check_F;
	}
	public void setCheck_F(String check_F) {
		Check_F = check_F;
	}
	
	

}
