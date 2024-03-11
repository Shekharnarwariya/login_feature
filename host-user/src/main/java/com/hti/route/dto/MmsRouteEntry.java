package com.hti.route.dto;

import java.io.Serializable;

public class MmsRouteEntry implements Serializable {
	private int routeId;
	private double cost;
	private int smscId;
	private String editBy;
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
