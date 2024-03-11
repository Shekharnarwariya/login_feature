package com.hti.objects;

public class ProfitLogObject {
	private String msgId;
	private int userId;
	private int resellerId;
	private int networkId;
	private double purchaseCost;
	private double sellingCost;
	private boolean wallet;
	private boolean adminDepend;

	public ProfitLogObject() {
	}

	public ProfitLogObject(String msgId, int userId, int resellerId, int networkId, double purchaseCost,
			double sellingCost, boolean wallet, boolean adminDepend) {
		this.msgId = msgId;
		this.userId = userId;
		this.resellerId = resellerId;
		this.networkId = networkId;
		this.purchaseCost = purchaseCost;
		this.sellingCost = sellingCost;
		this.wallet = wallet;
		this.adminDepend = adminDepend;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
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

	public String toString() {
		return "profitLog: msgId=" + msgId + ",userId=" + userId + ",resellerId=" + resellerId + ",networkId="
				+ networkId + ",purchaseCost=" + purchaseCost + ",sellingCost=" + sellingCost;
	}
}
