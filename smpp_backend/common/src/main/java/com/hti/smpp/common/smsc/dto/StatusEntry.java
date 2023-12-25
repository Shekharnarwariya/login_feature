package com.hti.smpp.common.smsc.dto;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
/**
 * Entity class representing custom settings for a specific SMSC with JPA annotations.
 */
@Entity
@Table(name = "smsc_status")
public class StatusEntry implements Serializable {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "smsc_id")
	private int smscId;
	@Column(name = "server_id")
	private int serverId;
	@Column(name = "bound")
	private boolean bound;
	@Column(name = "updateOn")
	private String updateOn;
	@Column(name = "status_code")
	private int statusCode;
	@Transient
	private String statusRemark;

	public StatusEntry() {
	}

	public StatusEntry(int serverId, boolean bound, String updateOn, int statusCode) {
		this.serverId = serverId;
		this.bound = bound;
		this.updateOn = updateOn;
		this.statusCode = statusCode;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSmscId() {
		return smscId;
	}

	public void setSmscId(int smscId) {
		this.smscId = smscId;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public boolean isBound() {
		return bound;
	}

	public void setBound(boolean bound) {
		this.bound = bound;
	}

	public String getUpdateOn() {
		return updateOn;
	}

	public void setUpdateOn(String updateOn) {
		this.updateOn = updateOn;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusRemark() {
		return statusRemark;
	}

	public void setStatusRemark(String statusRemark) {
		this.statusRemark = statusRemark;
	}

	public String toString() {
		return "BoundStatus: id=" + id + ",serverId=" + serverId + ",smscId=" + smscId + ",bound=" + bound;
	}
}
