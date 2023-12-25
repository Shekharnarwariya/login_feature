package com.hti.smpp.common.config.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
/**
 * Represents the DLT (Do Not Disturb) entry entity with attributes such as ID, username, sender, peId, and telemarketerId.
 */

@Entity
@Table(name = "dlt_config")
public class DltEntry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "username")
	private String username;
	@Column(name = "sender")
	private String sender;
	@Column(name = "pe_id")
	private String peId;
	@Column(name = "tm_id")
	private String telemarketerId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getPeId() {
		return peId;
	}

	public void setPeId(String peId) {
		this.peId = peId;
	}

	public String getTelemarketerId() {
		return telemarketerId;
	}

	public void setTelemarketerId(String telemarketerId) {
		this.telemarketerId = telemarketerId;
	}

	public String toString() {
		return "DLTEntry: id=" + id + ",Username=" + username + ",PeId=" + peId + ",TelemarketerId=" + telemarketerId;
	}
}
