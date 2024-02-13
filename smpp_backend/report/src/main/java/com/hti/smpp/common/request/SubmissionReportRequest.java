package com.hti.smpp.common.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SubmissionReportRequest {
	
	
	private String sday;
	private String smonth;
	private String syear;
	private String shour;
	private String smin;
	private String ssec;
	private String eday;
	private String emonth;
	private String eyear;
	private String ehour;
	private String emin;
	private String esec;
	@NotBlank(message = "StartDate must not  be blank")
	@Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "StartDate must be in the format yyyy-MM-dd HH:mm:ss")
	private String startdate;
	@NotBlank(message = "EndDate must not  be blank")
	@Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "endDate must be in the format yyyy-MM-dd HH:mm:ss")
	private String enddate;
	private String senderId;
	private String country;
	private String reportType;
	private String status;
	private String messageStatus;
	
	
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
	public String getShour() {
		return shour;
	}
	public void setShour(String shour) {
		this.shour = shour;
	}
	public String getSmin() {
		return smin;
	}
	public void setSmin(String smin) {
		this.smin = smin;
	}
	public String getSsec() {
		return ssec;
	}
	public void setSsec(String ssec) {
		this.ssec = ssec;
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
	public String getEhour() {
		return ehour;
	}
	public void setEhour(String ehour) {
		this.ehour = ehour;
	}
	public String getEmin() {
		return emin;
	}
	public void setEmin(String emin) {
		this.emin = emin;
	}
	public String getEsec() {
		return esec;
	}
	public void setEsec(String esec) {
		this.esec = esec;
	}
	public String getMessageStatus() {
		return messageStatus;
	}
	public void setMessageStatus(String messageStatus) {
		this.messageStatus = messageStatus;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getStartdate() {
		return startdate;
	}
	public void setStartdate(String startdate) {
		this.startdate = startdate;
	}
	public String getEnddate() {
		return enddate;
	}
	public void setEnddate(String enddate) {
		this.enddate = enddate;
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
	public String getReportType() {
		return reportType;
	}
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}
	




	
	
	
	

}
