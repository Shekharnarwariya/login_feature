package com.hti.smpp.common.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CampaignReportRequest {
	private PaginationRequest page;
	private String senderId;

	private String destinationNumber;

	@NotBlank(message = "country is required")
	private String country;

	@NotBlank(message = "clientId is required")
	private String clientId;

	// For date and time validations, assuming the format is yyyy-MM-dd HH:mm:ss
	@Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "startDate must be in the format yyyy-MM-dd HH:mm:ss")
	@NotBlank(message = "StartDate must not  be blank")
	private String startDate;

	@Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "endDate must be in the format yyyy-MM-dd HH:mm:ss")
	@NotBlank(message = "StartDate must not  be blank")
	private String endDate;

	// Assuming status could be anything, adjust validation as needed
	private String status;

	private String smscName;

	private String campaign;

	private String operator;

	private String groupBy;

	// For time components, assuming they are all numeric and have specific ranges
	private String emonth;

	private String smonth;

	private String sday;

	private String eday;

	private String syear;

	private String eyear;

	private String smin;

	private String emin;

	private String ssec;

	private String esec;

	private String shour;

	private String ehour;

	public PaginationRequest getPage() {
		return page;
	}

	public void setPage(PaginationRequest page) {
		this.page = page;
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

	public String getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
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

	public String getCampaign() {
		return campaign;
	}

	public void setCampaign(String campaign) {
		this.campaign = campaign;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSmscName() {
		return smscName;
	}

	public void setSmscName(String smscName) {
		this.smscName = smscName;
	}

}