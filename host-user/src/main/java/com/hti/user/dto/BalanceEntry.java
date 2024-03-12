package com.hti.user.dto;

import java.io.Serializable;

public class BalanceEntry implements Serializable {
	private int userId;
	private String systemId;
	private String walletFlag;
	private Double walletAmount;
	private long credits;
	private boolean active;

	public BalanceEntry() {
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getWalletFlag() {
		return walletFlag;
	}

	public void setWalletFlag(String walletFlag) {
		this.walletFlag = walletFlag;
	}

	public Double getWalletAmount() {
		return walletAmount;
	}

	public void setWalletAmount(Double walletAmount) {
		this.walletAmount = walletAmount;
	}

	public long getCredits() {
		return credits;
	}

	public void setCredits(long credits) {
		this.credits = credits;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String toString() {
		return "Balance: userid=" + userId + ",WalletFlag=" + walletFlag + ",WalletAmount=" + walletAmount + ",Credits="
				+ credits;
	}
}
