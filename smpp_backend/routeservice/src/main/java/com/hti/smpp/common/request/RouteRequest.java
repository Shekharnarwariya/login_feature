package com.hti.smpp.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
/**
 * The RouteRequest class represents a request DTO (Data Transfer Object) for managing route information.
 */
@Schema(description = "RouteDTO class")
public class RouteRequest {
	private int[] id;
	private int[] userId;
	private int[] networkId;
	private int[] smscId;
	private int[] groupId;
	private double[] cost;
	private double[] masterCost;
	private String[] smscType;
	private String[] remarks;
	// -------- optional fields -------
	private EditCriteria criteria;
	private String criterionEntries; // all id fetched under selected criteria to view again
	private boolean countryWise;
	private boolean schedule;
	private String scheduledOn;
/**
 *  The EditCriteria class represents criteria for editing route entries.
 */
	public class EditCriteria {
		// private String purpose;
		private String[] mcc;
		// private String[] mnc;
		private boolean priceRange;
		private double minCost;
		private double maxCost;
		private String[] currency;
		private String[] accountType;
//Getter and Setter
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

		public String[] getMcc() {
			return mcc;
		}

		public void setMcc(String[] mcc) {
			this.mcc = mcc;
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

	public int[] getId() {
		return id;
	}

	public void setId(int[] id) {
		this.id = id;
	}

	public boolean isCountryWise() {
		return countryWise;
	}

	public void setCountryWise(boolean countryWise) {
		this.countryWise = countryWise;
	}

	public EditCriteria getCriteria() {
		if (criteria == null) {
			criteria = new EditCriteria();
		}
		return criteria;
	}

	public void setCriteria(EditCriteria criteria) {
		this.criteria = criteria;
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

	public double[] getCost() {
		return cost;
	}

	public void setCost(double[] cost) {
		this.cost = cost;
	}

	public double[] getMasterCost() {
		return masterCost;
	}

	public void setMasterCost(double[] masterCost) {
		this.masterCost = masterCost;
	}

	public String[] getSmscType() {
		return smscType;
	}

	public void setSmscType(String[] smscType) {
		this.smscType = smscType;
	}

	public int[] getGroupId() {
		return groupId;
	}

	public void setGroupId(int[] groupId) {
		this.groupId = groupId;
	}

	public String getCriterionEntries() {
		return criterionEntries;
	}

	public void setCriterionEntries(String criterionEntries) {
		this.criterionEntries = criterionEntries;
	}

	public String[] getRemarks() {
		return remarks;
	}

	public void setRemarks(String[] remarks) {
		this.remarks = remarks;
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
