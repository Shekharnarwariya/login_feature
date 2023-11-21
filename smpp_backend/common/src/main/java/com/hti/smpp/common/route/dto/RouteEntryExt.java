package com.hti.smpp.common.route.dto;

import org.springframework.stereotype.Component;

@Component
public class RouteEntryExt {
	private RouteEntry basic;
	private String systemId;
	private String masterId;
	private String accountType;
	private String currency;
	private String smsc;
	private String group;
	private String country;
	private String operator;
	private String mcc;
	private String mnc;
	private double smscCost;
	private String oldCost;
	private String costStr;
	private String remarks;
	private String numSmsc;
	private String backupSmsc;
	private String regSmsc;
	private String regGroup;
	private double masterCost;
	private boolean optional;
	private HlrRouteEntry hlrRouteEntry;
	private OptionalRouteEntry routeOptEntry;

	public RouteEntryExt() {
	}

	public RouteEntryExt(RouteEntry basic, String systemId, String masterId, String accountType, String currency,
			String smsc, String group, String country, String operator, String mcc, String mnc, double smscCost,
			String oldCost, String costStr, String remarks, String numSmsc, String backupSmsc, String regSmsc,
			String regGroup, double masterCost, boolean optional, HlrRouteEntry hlrRouteEntry,
			OptionalRouteEntry routeOptEntry) {
		super();
		this.basic = basic;
		this.systemId = systemId;
		this.masterId = masterId;
		this.accountType = accountType;
		this.currency = currency;
		this.smsc = smsc;
		this.group = group;
		this.country = country;
		this.operator = operator;
		this.mcc = mcc;
		this.mnc = mnc;
		this.smscCost = smscCost;
		this.oldCost = oldCost;
		this.costStr = costStr;
		this.remarks = remarks;
		this.numSmsc = numSmsc;
		this.backupSmsc = backupSmsc;
		this.regSmsc = regSmsc;
		this.regGroup = regGroup;
		this.masterCost = masterCost;
		this.optional = optional;
		this.hlrRouteEntry = hlrRouteEntry;
		this.routeOptEntry = routeOptEntry;
	}

	public RouteEntryExt(RouteEntry basic) {
		this.basic = basic;
	}

	public RouteEntry getBasic() {
		return basic;
	}

	public void setBasic(RouteEntry basic) {
		this.basic = basic;
	}

	public double getMasterCost() {
		return masterCost;
	}

	public void setMasterCost(double masterCost) {
		this.masterCost = masterCost;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getMasterId() {
		return masterId;
	}

	public void setMasterId(String masterId) {
		this.masterId = masterId;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getSmsc() {
		return smsc;
	}

	public void setSmsc(String smsc) {
		this.smsc = smsc;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getMcc() {
		return mcc;
	}

	public void setMcc(String mcc) {
		this.mcc = mcc;
	}

	public String getMnc() {
		return mnc;
	}

	public void setMnc(String mnc) {
		this.mnc = mnc;
	}

	public String getOldCost() {
		return oldCost;
	}

	public void setOldCost(String oldCost) {
		this.oldCost = oldCost;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getNumSmsc() {
		return numSmsc;
	}

	public void setNumSmsc(String numSmsc) {
		this.numSmsc = numSmsc;
	}

	public String getBackupSmsc() {
		return backupSmsc;
	}

	public void setBackupSmsc(String backupSmsc) {
		this.backupSmsc = backupSmsc;
	}

	public String getRegSmsc() {
		return regSmsc;
	}

	public void setRegSmsc(String regSmsc) {
		this.regSmsc = regSmsc;
	}

	public String getRegGroup() {
		return regGroup;
	}

	public void setRegGroup(String regGroup) {
		this.regGroup = regGroup;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public HlrRouteEntry getHlrRouteEntry() {
		return hlrRouteEntry;
	}

	public void setHlrRouteEntry(HlrRouteEntry hlrRouteEntry) {
		this.hlrRouteEntry = hlrRouteEntry;
	}

	public OptionalRouteEntry getRouteOptEntry() {
		return routeOptEntry;
	}

	public void setRouteOptEntry(OptionalRouteEntry routeOptEntry) {
		this.routeOptEntry = routeOptEntry;
	}

	public double getSmscCost() {
		return smscCost;
	}

	public void setSmscCost(double smscCost) {
		this.smscCost = smscCost;
	}

	public String getCostStr() {
		return costStr;
	}

	public void setCostStr(String costStr) {
		this.costStr = costStr;
	}

	public String toString() {
		return "RouteEntryExt: " + basic.toString();
	}
	
}
