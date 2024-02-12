package com.hti.smpp.common.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AbortBatchReportRequest {
	
	private String senderId;
    @Pattern(regexp = "yyyy-MM-dd HH:mm:ss", message = "endDate must be in the format yyyy-MM-dd hh:mm:ss")
	@NotBlank(message = "StartDate must not  be blank")
    private String endDate;

    @Pattern(regexp = "yyyy-MM-dd HH:mm:ss", message = "startDate must be in the format yyyy-MM-dd")
	@NotBlank(message = "StartDate must not  be blank")
    private String startDate;

    private String clientId;
	


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
	
	
	

}
