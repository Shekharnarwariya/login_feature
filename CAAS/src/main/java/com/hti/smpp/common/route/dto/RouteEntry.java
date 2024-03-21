package com.hti.smpp.common.route.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "routemaster")
public class RouteEntry implements Serializable {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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
	@Column(name = "editby")
	private String editBy;
	@Column(name = "edit_on")
	private String editOn;
	@Column(name = "remarks")
	private String remarks;

	public RouteEntry() {
	}

	public RouteEntry(int userId, String editBy, String editOn, String remarks) {
		this.userId = userId;
		this.editBy = editBy;
		this.editOn = editOn;
		this.remarks = remarks;
	}

	public RouteEntry(int userId, int networkId, int smscId, int groupId, double cost, String smscType, String editBy,
			String editOn, String remarks) {
		this.userId = userId;
		this.networkId = networkId;
		this.smscId = smscId;
		this.cost = cost;
		this.smscType = smscType;
		this.editBy = editBy;
		this.editOn = editOn;
		this.groupId = groupId;
		this.remarks = remarks;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEditOn() {
		return editOn;
	}

	public void setEditOn(String editOn) {
		this.editOn = editOn;
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

	public String getEditBy() {
		return editBy;
	}

	public void setEditBy(String editBy) {
		this.editBy = editBy;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String toString() {
		return "Route: id=" + id + ",userId=" + userId + ",networkId=" + networkId + ",smscId=" + smscId + ",groupId="
				+ groupId + ",Type=" + smscType + ",Cost=" + cost + ",remarks=" + remarks;
	}
}
