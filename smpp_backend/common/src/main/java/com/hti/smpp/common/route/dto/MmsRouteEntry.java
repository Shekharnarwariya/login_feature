package com.hti.smpp.common.route.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mms_routing")
public class MmsRouteEntry  {
	@Id
	@Column(name = "route_id", unique = true, nullable = false)
	private int routeId;
	@Column(name = "smsc_id")
	private int smscId;
	@Column(name = "cost")
	private double cost;
	@Column(name = "editby")
	private String editBy;
	@Column(name = "edit_on")
	private String editOn;

	public MmsRouteEntry() {
	}

	public MmsRouteEntry(String editBy, String editOn) {
		this.editBy = editBy;
		this.editOn = editOn;
	}

	public int getRouteId() {
		return routeId;
	}

	public void setRouteId(int routeId) {
		this.routeId = routeId;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public int getSmscId() {
		return smscId;
	}

	public void setSmscId(int smscId) {
		this.smscId = smscId;
	}

	public String getEditBy() {
		return editBy;
	}

	public void setEditBy(String editBy) {
		this.editBy = editBy;
	}

	public String getEditOn() {
		return editOn;
	}

	public void setEditOn(String editOn) {
		this.editOn = editOn;
	}

	@Override
	public String toString() {
		return "MmsRouteEntry [routeId=" + routeId + ", cost=" + cost + ", smscId=" + smscId + ", editBy=" + editBy
				+ ", editOn=" + editOn + "]";
	}
}
