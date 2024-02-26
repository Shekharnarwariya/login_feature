package com.hti.smpp.common.request;

public class AlertForm {

	private int id;
	private String email;
	private double percent;
	private String alertNumber;
	private int duration;
	private String[] countries;
	private String[] routes;
	private String remarks;
	private String status;
	private int minlimit;
	private boolean holdTraffic;
	private String senders;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public double getPercent() {
		return percent;
	}
	public void setPercent(double percent) {
		this.percent = percent;
	}
	public String getAlertNumber() {
		return alertNumber;
	}
	public void setAlertNumber(String alertNumber) {
		this.alertNumber = alertNumber;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public String[] getCountries() {
		return countries;
	}
	public void setCountries(String[] countries) {
		this.countries = countries;
	}
	public String[] getRoutes() {
		return routes;
	}
	public void setRoutes(String[] routes) {
		this.routes = routes;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getMinlimit() {
		return minlimit;
	}
	public void setMinlimit(int minlimit) {
		this.minlimit = minlimit;
	}
	public boolean isHoldTraffic() {
		return holdTraffic;
	}
	public void setHoldTraffic(boolean holdTraffic) {
		this.holdTraffic = holdTraffic;
	}
	public String getSenders() {
		return senders;
	}
	public void setSenders(String senders) {
		this.senders = senders;
	}
	
	
	
}
