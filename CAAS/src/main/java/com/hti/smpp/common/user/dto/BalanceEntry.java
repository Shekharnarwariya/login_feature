package com.hti.smpp.common.user.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Entity bean with JPA annotations
 */
@Entity
@Table(name = "balance_master")
public class BalanceEntry implements Serializable {
	@Id
	@Column(name = "user_id", unique = true, nullable = false)
	private int userId;
	@Column(name = "system_id", nullable = false, updatable = false)
	private String systemId;
	@Column(name = "wallet_flag")
	private String walletFlag;
	@Column(name = "wallet", nullable = false)
	private Double walletAmount;
	@Column(name = "credits", nullable = false)
	private long credits;
	@Transient
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
