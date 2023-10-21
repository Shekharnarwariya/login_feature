package com.hti.smpp.common.user.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "otp_master")
public class OTPEntry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "system_id", unique = true, nullable = false, updatable = false)
	private String systemId;
	@Column(name = "otp")
	private int oneTimePass;
	@Column(name = "expiresOn")
	private String expiresOn;

	public OTPEntry() {
	}

	public OTPEntry(String systemId, int oneTimePass, String expiresOn) {
		this.systemId = systemId;
		this.oneTimePass = oneTimePass;
		this.expiresOn = expiresOn;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public int getOneTimePass() {
		return oneTimePass;
	}

	public void setOneTimePass(int oneTimePass) {
		this.oneTimePass = oneTimePass;
	}

	public String getExpiresOn() {
		return expiresOn;
	}

	public void setExpiresOn(String expiresOn) {
		this.expiresOn = expiresOn;
	}

	public String toString() {
		return "otpEntry: id=" + id + ",system_id=" + systemId + ",otp=" + oneTimePass + ",expiresOn=" + expiresOn;
	}
}
