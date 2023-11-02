package com.hti.smpp.common.user.dto;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Entity bean with JPA annotations
 */
@Entity
@Table(name = "balance_master")
public class BalanceEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "system_id")
	private String systemId;

	@Column(name = "wallet_flag")
	private String walletFlag;

	@Column(name = "wallet")
	private Double walletAmount;

	@Column(name = "credits")
	private Long credits;

	@Column(name = "last_updated")
	private Date lastUpdated;

	@Transient
	private boolean active;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
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

	public Long getCredits() {
		return credits;
	}

	public void setCredits(Long credits) {
		this.credits = credits;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return "BalanceEntry{" + "userId=" + userId + ", systemId='" + systemId + '\'' + ", walletFlag='" + walletFlag
				+ '\'' + ", walletAmount=" + walletAmount + ", credits=" + credits + ", lastUpdated=" + lastUpdated
				+ ", active=" + active + '}';
	}
}
