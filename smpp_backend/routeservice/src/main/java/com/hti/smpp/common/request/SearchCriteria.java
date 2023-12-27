package com.hti.smpp.common.request;
/**
 * The SearchCriteria class represents criteria for searching route entries.
 */
public class SearchCriteria {
	private int[] routeId;
	private int[] userId;
	private int[] networkId;
	private int[] smscId;
	private int[] groupId;
	private String[] smscType;
	private String[] currency;
	private String[] accountType;
	private boolean priceRange;
	private double minCost;
	private double maxCost;
	private boolean hlrEntry;
	private boolean optEntry;
//Getter and Setter
	public int[] getGroupId() {
		return groupId;
	}

	public void setGroupId(int[] groupId) {
		this.groupId = groupId;
	}

	public int[] getRouteId() {
		return routeId;
	}

	public void setRouteId(int[] routeId) {
		this.routeId = routeId;
	}

	public String[] getAccountType() {
		return accountType;
	}

	public void setAccountType(String[] accountType) {
		this.accountType = accountType;
	}

	public String[] getCurrency() {
		return currency;
	}

	public void setCurrency(String[] currency) {
		this.currency = currency;
	}

	public boolean isHlrEntry() {
		return hlrEntry;
	}

	public void setHlrEntry(boolean hlrEntry) {
		this.hlrEntry = hlrEntry;
	}

	public boolean isOptEntry() {
		return optEntry;
	}

	public void setOptEntry(boolean optEntry) {
		this.optEntry = optEntry;
	}

	public int[] getUserId() {
		return userId;
	}

	public void setUserId(int[] userId) {
		this.userId = userId;
	}

	public int[] getNetworkId() {
		return networkId;
	}

	public void setNetworkId(int[] networkId) {
		this.networkId = networkId;
	}

	public int[] getSmscId() {
		return smscId;
	}

	public void setSmscId(int[] smscId) {
		this.smscId = smscId;
	}

	public String[] getSmscType() {
		return smscType;
	}

	public void setSmscType(String[] smscType) {
		this.smscType = smscType;
	}

	public boolean isPriceRange() {
		return priceRange;
	}

	public void setPriceRange(boolean priceRange) {
		this.priceRange = priceRange;
	}

	public double getMinCost() {
		return minCost;
	}

	public void setMinCost(double minCost) {
		this.minCost = minCost;
	}

	public double getMaxCost() {
		return maxCost;
	}

	public void setMaxCost(double maxCost) {
		this.maxCost = maxCost;
	}
}