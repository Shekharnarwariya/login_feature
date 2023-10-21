package com.hti.smpp.common.report.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "profit_report")
public class ProfitReportEntry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "msg_id")
	private long msgId;
	@Column(name = "user_id")
	private int userId;
	@Column(name = "reseller_id")
	private int resellerId;
	@Column(name = "network_id")
	private int networkId;
	@Column(name = "purchase_cost")
	private double purchaseCost;
	@Column(name = "selling_cost")
	private double sellingCost;
	@Column(name = "wallet")
	private boolean wallet;
	@Column(name = "admindepend")
	private boolean adminDepend;
	@Transient
	private String reseller;
	@Transient
	private String username;
	@Transient
	private String country;
	@Transient
	private String operator;
	@Transient
	private double profit;

	public ProfitReportEntry() {
		// TODO Auto-generated constructor stub
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getMsgId() {
		return msgId;
	}

	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getResellerId() {
		return resellerId;
	}

	public void setResellerId(int resellerId) {
		this.resellerId = resellerId;
	}

	public int getNetworkId() {
		return networkId;
	}

	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}

	public double getPurchaseCost() {
		return purchaseCost;
	}

	public void setPurchaseCost(double purchaseCost) {
		this.purchaseCost = purchaseCost;
	}

	public double getSellingCost() {
		return sellingCost;
	}

	public void setSellingCost(double sellingCost) {
		this.sellingCost = sellingCost;
	}

	public String getReseller() {
		return reseller;
	}

	public void setReseller(String reseller) {
		this.reseller = reseller;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public double getProfit() {
		if (profit <= 0) {
			return (sellingCost - purchaseCost);
		} else {
			return profit;
		}
	}

	public boolean isWallet() {
		return wallet;
	}

	public void setWallet(boolean wallet) {
		this.wallet = wallet;
	}

	public boolean isAdminDepend() {
		return adminDepend;
	}

	public void setAdminDepend(boolean adminDepend) {
		this.adminDepend = adminDepend;
	}

	public void setProfit(double profit) {
		this.profit = profit;
	}

	public String toString() {
		return "profitLog: msgId=" + msgId + ",userId=" + userId + ",username=" + username + ",resellerId=" + resellerId
				+ ",networkId=" + networkId + ",purchaseCost=" + purchaseCost + ",sellingCost=" + sellingCost;
	}
}
