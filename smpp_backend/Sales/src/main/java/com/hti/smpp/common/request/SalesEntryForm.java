package com.hti.smpp.common.request;

/**
 * Represents a form containing information for creating or updating a SalesEntry.
 * Includes fields such as ID, master ID, username, email, number, reporting, createdOn,
 * remarks, password, expiredOn, role, and domainEmail.
 */

public class SalesEntryForm {
	private int id;
	private String masterId;
	private String username;
	private String email;
	private String number;
	private String reporting;
	private String createdOn;
	private String remarks;
	private String password;
	private String expiredOn;
	private String role;
	private String domainEmail;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMasterId() {
		return masterId;
	}

	public void setMasterId(String masterId) {
		this.masterId = masterId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getReporting() {
		return reporting;
	}

	public void setReporting(String reporting) {
		this.reporting = reporting;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getExpiredOn() {
		return expiredOn;
	}

	public void setExpiredOn(String expiredOn) {
		this.expiredOn = expiredOn;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getDomainEmail() {
		return domainEmail;
	}

	public void setDomainEmail(String domainEmail) {
		this.domainEmail = domainEmail;
	}
}
