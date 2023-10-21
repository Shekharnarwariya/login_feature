package com.hti.smpp.common.route.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "route_hlr_log")
public class HlrEntryLog {
	@Id
	@Column(name = "route_id")
	private int routeId;
	@Column(name = "isHlr")
	private boolean hlr;
	@Column(name = "hlr_smsc")
	private int smsc;
	@Column(name = "hlr_cache")
	private int hlrCache;
	@Column(name = "cost")
	private double cost;
	@Column(name = "affectedOn", insertable = false, updatable = false, nullable = false)
	private String affectedOn;
	@Column(name = "is_mnp")
	private boolean mnp;

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

	public String getAffectedOn() {
		return affectedOn;
	}

	public void setAffectedOn(String affectedOn) {
		this.affectedOn = affectedOn;
	}

	public boolean isMnp() {
		return mnp;
	}

	public void setMnp(boolean mnp) {
		this.mnp = mnp;
	}

	public String toString() {
		return "hlr: Routeid=" + routeId + ",ishlr=" + hlr + ",hlrSmscId=" + smsc + ",Cache=" + hlrCache + ",Cost="
				+ cost + ",affectedOn=" + affectedOn;
	}
}
