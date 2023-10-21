package com.hti.smpp.common.sales.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sales_entry")
public class SalesEntry implements Comparable<SalesEntry> {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "master_id")
	private String masterId;
	@Column(name = "name", unique = true, nullable = false, updatable = false)
	private String username;
	@Column(name = "email")
	private String email;
	@Column(name = "number")
	private String number;
	@Column(name = "reporting")
	private String reporting;
	@Column(name = "createdon", insertable = false, updatable = false, nullable = false)
	private String createdOn;
	@Column(name = "remarks")
	private String remarks;
	@Column(name = "password")
	private String password;
	@Column(name = "expiredon")
	private String expiredOn;
	@Column(name = "role", updatable = false, nullable = false)
	private String role;
	@Column(name = "domain_email")
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

	public String toString() {
		return "SalesEntry: id=" + id + ",username=" + username + ",masterId=" + masterId;
	}

	@Override
	public int compareTo(SalesEntry o) {
		return this.getUsername().compareTo(o.getUsername());
	}
}
