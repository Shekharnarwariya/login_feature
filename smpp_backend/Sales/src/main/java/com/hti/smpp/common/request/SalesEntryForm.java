package com.hti.smpp.common.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents a form containing information for creating or updating a SalesEntry.
 * Includes fields such as ID, master ID, username, email, number, reporting, createdOn,
 * remarks, password, expiredOn, role, and domainEmail.
 */

public class SalesEntryForm {
	private int id;
	@NotBlank(message = "MasterId Can't Be Blank")
	private String masterId;
	@NotBlank(message = "Username Can't Be Blank")
	private String username;
	@NotBlank(message = "Email Can't Be Blank")
	private String email;
	@NotBlank(message = "Number Can't Be Blank")
	private String number;
	@NotBlank(message = "Reporting Can't Be Blank")
	private String reporting;
//	@NotBlank(message = "CreatedOn Can't Be Blank")
	private String createdOn;
	@NotBlank(message = "Remarks Can't Be Blank")
	private String remarks;
	@NotBlank(message = "Password Can't Be Blank")
	private String password;
	@NotBlank(message = "ExpiredOn Can't Be Blank")
	private String expiredOn;
	@NotBlank(message = "Role Can't Be Blank")
	private String role;
	@NotBlank(message = "DomainEmail Can't Be Blank")
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
