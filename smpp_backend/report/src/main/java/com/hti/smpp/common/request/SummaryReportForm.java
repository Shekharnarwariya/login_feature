package com.hti.smpp.common.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SummaryReportForm {

	
	private String reportType;
	private String clientId;
	private String senderId;
	@NotBlank(message = "EndDate must not  be blank")
	@Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "endDate must be in the format yyyy-MM-dd HH:mm:ss")
	
	private String endDate;
	@NotBlank(message = "StartDate must not  be blank")
	@Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "StartDate must be in the format yyyy-MM-dd HH:mm:ss")
	private String  startDate;
	private String country;
	private String check_A;
	private String groupBy;
	private String campaign;
	private String campaignType;
	
	public String getSenderId() {
		return senderId;
	}
	public void setSenderId(String senderId) {
		this.senderId = senderId;
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
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getCampaignType() {
		return campaignType;
		
		
		
	}
	public void setCampaignType(String campaignType) {
		this.campaignType = campaignType;
	}

	public String getReportType() {
		return reportType;
	}
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
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
}
