package com.hti.route.dto;

import java.io.Serializable;

public class HlrRouteEntry implements Serializable {
	private int routeId;
	private boolean hlr;
	private int smsc;
	private int hlrCache;
	private double cost;
	private String editBy;
	private String editOn;
	private boolean mnp;

	public HlrRouteEntry(String editBy, String editOn) {
		this.editBy = editBy;
		this.editOn = editOn;
	}

	public HlrRouteEntry() {
	}

	public HlrRouteEntry(int routeId, boolean hlr, int smsc, int hlrCache, double cost, String editBy, String editOn,
			boolean mnp) {
		this.routeId = routeId;
		this.hlr = hlr;
		this.smsc = smsc;
		this.hlrCache = hlrCache;
		this.cost = cost;
		this.editBy = editBy;
		this.editOn = editOn;
		this.mnp = mnp;
	}

	public String getEditOn() {
		return editOn;
	}

	public void setEditOn(String editOn) {
		this.editOn = editOn;
	}

	public String getEditBy() {
		return editBy;
	}

	public void setEditBy(String editBy) {
		this.editBy = editBy;
	}

	public int getRouteId() {
		return routeId;
	}

	public void setRouteId(int routeId) {
		this.routeId = routeId;
	}

	public boolean isHlr() {
		return hlr;
	}

	public void setHlr(boolean hlr) {
		this.hlr = hlr;
	}

	public int getSmsc() {
		return smsc;
	}

	public void setSmsc(int smsc) {
		this.smsc = smsc;
	}

	public int getHlrCache() {
		return hlrCache;
	}

	public void setHlrCache(int hlrCache) {
		this.hlrCache = hlrCache;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public boolean isMnp() {
		return mnp;
	}

	public void setMnp(boolean mnp) {
		this.mnp = mnp;
	}

	public String toString() {
		return "hlr: Routeid=" + routeId + ",ishlr=" + hlr + ",hlrSmscId=" + smsc + ",Cache=" + hlrCache + ",Cost="
				+ cost;
	}
}
