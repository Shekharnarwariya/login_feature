package com.hti.smpp.common.route.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "route_basic_log")
public class RouteEntryLog {
	@Id
	@Column(name = "id")
	private int id;
	@Column(name = "user_id", nullable = false, updatable = false)
	private int userId;
	@Column(name = "network_id", nullable = false, updatable = false)
	private int networkId;
	@Column(name = "smsc_id")
	private int smscId;
	@Column(name = "group_id")
	private int groupId;
	@Column(name = "cost")
	private double cost;
	@Column(name = "smsc_type")
	private String smscType = "W";
	@Column(name = "affectedOn", insertable = false, updatable = false, nullable = false)
	private String affectedOn;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getNetworkId() {
		return networkId;
	}

	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}

	public int getSmscId() {
		return smscId;
	}

	public void setSmscId(int smscId) {
		this.smscId = smscId;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public String getSmscType() {
		return smscType;
	}

	public void setSmscType(String smscType) {
		this.smscType = smscType;
	}

	public String getAffectedOn() {
		return affectedOn;
	}

	public void setAffectedOn(String affectedOn) {
		this.affectedOn = affectedOn;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public String toString() {
		return "RouteBasicLog: id=" + id + ",userId=" + userId + ",networkId=" + networkId + ",smscId=" + smscId
				+ ",groupId=" + groupId + ",Type=" + smscType + ",Cost=" + cost + ",AffectedOn=" + affectedOn;
	}
}
