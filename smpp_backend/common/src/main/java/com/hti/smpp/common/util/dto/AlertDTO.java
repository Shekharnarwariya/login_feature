/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.smpp.common.util.dto;

import org.springframework.stereotype.Component;

/**
 *
 * @author Administrator
 */

@Component
public class AlertDTO {

	private String email;
	private int percent;
	private String alertNumber;
	private int duration;
	private String[] countries;
	private String[] routes;
	private String remarks;
	private String status;
	private int id;
	private int minlimit; // minimum submission to calculate performance
	private boolean holdTraffic;
	private String senders;

	public AlertDTO() {
	}

	public AlertDTO(int id, String[] countries, String[] routes, int percent, int duration, String status, String email,
			String alertNumber, String remarks, int minlimit, boolean holdTraffic, String senders) {
		this.id = id;
		this.countries = countries;
		this.routes = routes;
		this.percent = percent;
		this.duration = duration;
		this.status = status;
		this.email = email;
		this.alertNumber = alertNumber;
		this.remarks = remarks;
		this.minlimit = minlimit;
		this.holdTraffic = holdTraffic;
		this.senders = senders;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMinlimit() {
		return minlimit;
	}

	public void setMinlimit(int minlimit) {
		this.minlimit = minlimit;
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
