package com.hti.smpp.common.request;

import com.hti.smpp.common.route.dto.HlrRouteEntry;
import com.hti.smpp.common.route.dto.OptionalRouteEntry;

public class RouteEntryForm {
	private int id;
	private int userId;
	private int networkId;
	private int smscId;
	private double cost;
	private String smscType;
	private String remarks;
	// ------------ other props --------
	private String systemId;
	private String smsc;
	private int[] subUserId;
	private String mcc;
	private String[] mnc;
	private String margin;
	private boolean hlr;
	private boolean optional;
	private HlrRouteEntry hlrRouteEntry;
	private OptionalRouteEntry routeOptEntry;
	private boolean replaceExisting;

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

	public String getSmscType() {
		return smscType;
	}

	public void setSmscType(String smscType) {
		this.smscType = smscType;
	}

	public int[] getSubUserId() {
		return subUserId;
	}

	public void setSubUserId(int[] subUserId) {
		this.subUserId = subUserId;
	}

	public String getMargin() {
		return margin;
	}

	public void setMargin(String margin) {
		this.margin = margin;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getSmsc() {
		return smsc;
	}

	public void setSmsc(String smsc) {
		this.smsc = smsc;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public boolean isHlr() {
		return hlr;
	}

	public void setHlr(boolean hlr) {
		this.hlr = hlr;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public HlrRouteEntry getHlrRouteEntry() {
		if (hlrRouteEntry == null) {
			hlrRouteEntry = new HlrRouteEntry();
		}
		return hlrRouteEntry;
	}

	public void setHlrRouteEntry(HlrRouteEntry hlrRouteEntry) {
		this.hlrRouteEntry = hlrRouteEntry;
	}

	public OptionalRouteEntry getRouteOptEntry() {
		if (routeOptEntry == null) {
			routeOptEntry = new OptionalRouteEntry();
		}
		return routeOptEntry;
	}

	public void setRouteOptEntry(OptionalRouteEntry routeOptEntry) {
		this.routeOptEntry = routeOptEntry;
	}

	public String getMcc() {
		return mcc;
	}

	public void setMcc(String mcc) {
		this.mcc = mcc;
	}

	public String[] getMnc() {
		return mnc;
	}

	public void setMnc(String[] mnc) {
		this.mnc = mnc;
	}

	public boolean isReplaceExisting() {
		return replaceExisting;
	}

	public void setReplaceExisting(boolean replaceExisting) {
		this.replaceExisting = replaceExisting;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String toString() {
		return "Route: id=" + id + ",userId=" + userId + ",networkId=" + networkId + ",smscId=" + smscId + ",Cost="
				+ cost;
	}

}
