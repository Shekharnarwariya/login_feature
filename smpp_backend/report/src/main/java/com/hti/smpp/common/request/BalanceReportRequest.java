package com.hti.smpp.common.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public class BalanceReportRequest {
	
	private PaginationRequest paginationRequest; 
	
	
	  @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "endDate must be in the format yyyy-MM-dd HH:mm:ss")
		
	  @NotBlank(message = "senderId must not be blank")
	    private String senderId;
	    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "endDate must be in the format yyyy-MM-dd HH:mm:ss")
		@NotBlank(message = "StartDate must not  be blank")
	    private String endDate;

	    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "startDate must be in the format yyyy-MM-dd HH:mm:ss")
		@NotBlank(message = "StartDate must not  be blank")
	    private String startDate;

	    @NotBlank(message = "clientId must not be blank")
	    private String clientId;

	    @NotBlank(message = "reportType must not be blank")
	    private String reportType;

	    private String country;

	    private String operator;

	    @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "emonth must be in the format MM (01-12)")
	    private String emonth;

	    @Pattern(regexp = "^\\d{4}$", message = "eyear must be a four-digit number")
	    private String eyear;

	
	public PaginationRequest getPaginationRequest() {
			return paginationRequest;
		}
		public void setPaginationRequest(PaginationRequest paginationRequest) {
			this.paginationRequest = paginationRequest;
		}
	public String getSenderId() {
		return senderId;
	}
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
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
	public String getReportType() {
		return reportType;
	}
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}
	
	

}
