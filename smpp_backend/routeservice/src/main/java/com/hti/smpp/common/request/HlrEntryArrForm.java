package com.hti.smpp.common.request;
/**
 * The HlrEntryArrForm class represents a form containing arrays of various parameters related to HLR routing.
 */
public class HlrEntryArrForm {
	private int[] routeId;
	private boolean[] hlr;
	private int[] smsc;
	private int[] hlrCache;
	private double[] cost;
	private boolean[] mnp;
	private int[] userId;
	// ---- optional -----------------
	private String criterionEntries; // all id fetched under selected criteria to view again
	private boolean schedule;
	private String scheduledOn;
   //Getter and setter
	public int[] getRouteId() {
		return routeId;
	}

	public void setRouteId(int[] routeId) {
		this.routeId = routeId;
	}

	public boolean[] getHlr() {
		return hlr;
	}

	public void setHlr(boolean[] hlr) {
		this.hlr = hlr;
	}

	public int[] getSmsc() {
		return smsc;
	}

	public void setSmsc(int[] smsc) {
		this.smsc = smsc;
	}

	public int[] getHlrCache() {
		return hlrCache;
	}

	public void setHlrCache(int[] hlrCache) {
		this.hlrCache = hlrCache;
	}

	public double[] getCost() {
		return cost;
	}

	public void setCost(double[] cost) {
		this.cost = cost;
	}

	public int[] getUserId() {
		return userId;
	}

	public void setUserId(int[] userId) {
		this.userId = userId;
	}

	public boolean[] getMnp() {
		return mnp;
	}

	public void setMnp(boolean[] mnp) {
		this.mnp = mnp;
	}

	public String getCriterionEntries() {
		return criterionEntries;
	}

	public void setCriterionEntries(String criterionEntries) {
		this.criterionEntries = criterionEntries;
	}

	public boolean isSchedule() {
		return schedule;
	}

	public void setSchedule(boolean schedule) {
		this.schedule = schedule;
	}

	public String getScheduledOn() {
		return scheduledOn;
	}

	public void setScheduledOn(String scheduledOn) {
		this.scheduledOn = scheduledOn;
	}
}
