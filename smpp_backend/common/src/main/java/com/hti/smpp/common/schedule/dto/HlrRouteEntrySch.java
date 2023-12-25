package com.hti.smpp.common.schedule.dto;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
/**
 * Entity class representing HLR (Home Location Register) routing entries with JPA annotations.
 */
@Entity
@Table(name = "hlr_routing")
public class HlrRouteEntrySch implements Serializable {
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
	@Column(name = "schedule_on")
	private String scheduleOn;

	public String getScheduleOn() {
		return scheduleOn;
	}

	public void setScheduleOn(String scheduleOn) {
		this.scheduleOn = scheduleOn;
	}

	public HlrRouteEntrySch(String editBy, String editOn) {
		this.editBy = editBy;
		this.editOn = editOn;
	}

	public HlrRouteEntrySch() {
	}

	public HlrRouteEntrySch(int routeId, boolean hlr, int smsc, int hlrCache, double cost, String editBy, String editOn,
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
	 // Getters and setters for all fields...

    /**
     * Custom toString method for displaying HLR route details.
     */
	public String toString() {
		return "hlr: Routeid=" + routeId + ",ishlr=" + hlr + ",hlrSmscId=" + smsc + ",Cache=" + hlrCache + ",Cost="
				+ cost;
	}
}
