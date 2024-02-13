package com.hti.smpp.common.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SmscDlrReportRequest {

	private String senderId;
	private String country;
	private String operator;
	@NotBlank(message = "EndDate must not  be blank")
	@Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "endDate must be in the format yyyy-MM-dd HH:mm:ss")
	private String endDate;

	@NotBlank(message = "StartDate must not  be blank")
	 @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "endDate must be in the format yyyy-MM-dd HH:mm:ss")
	private String startDate;
	private String[] smscnames;
	
	private String reportType;
	@NotBlank(message = "EndDate must not be blank")
	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public String[] getSmscnames() {
		return smscnames;
	}

	public void setSmscnames(String[] smscnames) {
		this.smscnames = smscnames;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
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

}
