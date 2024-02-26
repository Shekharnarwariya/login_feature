package com.hti.smpp.common.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public class BlockedReportRequest {
	
	private PaginationRequest paginationRequest; 
	
	    private String senderId;

	    private String clientId;

	    @Size(min = 10, max = 15, message = "destinationNumber must be between 10 and 15 characters long")
	    private String destinationNumber;

	    private String country;

	    private String operator;

	    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "startDate must be in the format yyyy-MM-dd HH:mm:ss")
	    @NotBlank(message = "StartDate must not  be blank")
	    private String startDate;

	    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "endDate must be in the format yyyy-MM-dd HH:mm:ss")
	    @NotBlank(message = "StartDate must not  be blank")
	    private String endDate;

	   
 private Integer bsfmRule; // Use Integer instead of int to allow null validation
	   
	
	
	public PaginationRequest getPaginationRequest() {
	return paginationRequest;
}
public void setPaginationRequest(PaginationRequest paginationRequest) {
	this.paginationRequest = paginationRequest;
}
public void setBsfmRule(Integer bsfmRule) {
	this.bsfmRule = bsfmRule;
}
	public int getBsfmRule() {
		return bsfmRule;
	}
	public void setBsfmRule(int bsfmRule) {
		this.bsfmRule = bsfmRule;
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
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
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
	@Override
	public String toString() {
		return "BlockedReportRequest [operator=" + operator + ", endDate=" + endDate +   ", startDate=" + startDate + ", clientId=" + clientId + ", senderId=" + senderId
				+ ", destinationNumber=" + destinationNumber + ", country=" + country + ", bsfmRule=" + bsfmRule + "]";
	}
	
	

}
