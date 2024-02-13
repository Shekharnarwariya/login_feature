package com.hti.smpp.common.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LookUpReportRequest {
	
	@NotBlank(message = "EndDate must not  be blank")
	@Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "endDate must be in the format yyyy-MM-dd HH:mm:ss")
	private String endDate;

	@NotBlank(message = "StartDate must not  be blank")
	 @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "endDate must be in the format yyyy-MM-dd HH:mm:ss")
	private String startDate;
	private String clientId;
	private String check_A;
	private String check_B;
	private String messageId;
	
	
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
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
	public String getCheck_A() {
		return check_A;
	}
	public void setCheck_A(String check_A) {
		this.check_A = check_A;
	}
	public String getCheck_B() {
		return check_B;
	}
	public void setCheck_B(String check_B) {
		this.check_B = check_B;
	}
	
	
	
	
	

}
