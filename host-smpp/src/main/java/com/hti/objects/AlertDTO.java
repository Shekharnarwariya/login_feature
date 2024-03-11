/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.objects;

/**
 *
 * @author Administrator
 */
public class AlertDTO {
	private int id;
	private String email;
	private int percent;
	private int minlimit; // minimum submission to calculate performance
	private int resultedPercent;
	private String alertNumber;
	private int duration;
	private String countries;
	private String routes;
	private String remarks;
	private String status;
	private String from;
	private String to;
	private int submitted;
	private int statusCount;
	private boolean holdTraffic;
	private String senders;

	public AlertDTO(AlertDTO alert) {
		this.id = alert.getId();
		this.routes = alert.getRoutes();
		this.countries = alert.getCountries();
		this.duration = alert.getDuration();
		this.percent = alert.getPercent();
		this.minlimit = alert.getMinlimit();
		this.status = alert.getStatus();
		this.email = alert.getEmail();
		this.alertNumber = alert.getAlertNumber();
		this.remarks = alert.getRemarks();
		this.holdTraffic = alert.isHoldTraffic();
	}

	public AlertDTO(int id, String routes, String countries, int duration, int percent, String status, String email,
			String alertNumber, int minlimit, boolean holdTraffic, String senders) {
		this.id = id;
		this.routes = routes;
		this.countries = countries;
		this.duration = duration;
		this.percent = percent;
		this.status = status;
		this.email = email;
		this.alertNumber = alertNumber;
		this.minlimit = minlimit;
		this.holdTraffic = holdTraffic;
		this.senders = senders;
	}

	public int getMinlimit() {
		return minlimit;
	}

	public void setMinlimit(int minlimit) {
		this.minlimit = minlimit;
	}

	public int getSubmitted() {
		return submitted;
	}

	public void setSubmitted(int submitted) {
		this.submitted = submitted;
	}

	public int getStatusCount() {
		return statusCount;
	}

	public void setStatusCount(int statusCount) {
		this.statusCount = statusCount;
	}

	public int getResultedPercent() {
		return resultedPercent;
	}

	public void setResultedPercent(int resultedPercent) {
		this.resultedPercent = resultedPercent;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCountries() {
		return countries;
	}

	public void setCountries(String countries) {
		this.countries = countries;
	}

	public String getRoutes() {
		return routes;
	}

	public void setRoutes(String routes) {
		this.routes = routes;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getPercent() {
		return percent;
	}

	public void setPercent(int percent) {
		this.percent = percent;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
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
