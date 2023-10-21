package com.hti.smpp.common.route.dto;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "hlr_routing")
public class HlrRouteEntry implements Serializable {
	@Id
	@Column(name = "route_id", unique = true, nullable = false)
	private int routeId;
	@Column(name = "isHlr")
	private boolean hlr;
	@Column(name = "hlr_smsc")
	private int smsc;
	@Column(name = "hlr_cache")
	private int hlrCache;
	@Column(name = "cost")
	private double cost;
	@Column(name = "editby")
	private String editBy;
	@Column(name = "edit_on")
	private String editOn;
	@Column(name = "is_mnp")
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
