package com.hti.smpp.common.user.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Entity bean with JPA annotations
 */
@Entity
@Table(name = "creditmaster")
public class RechargeEntry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "user_id")
	private int userId;
	@Column(name = "time")
	private String time;
	@Column(name = "particular")
	private String particular;
	@Column(name = "current_credit")
	private long previousCredits;
	@Column(name = "credits")
	private long toBeAddedCedits;
	@Column(name = "balance")
	private long effectiveCredits;
	@Column(name = "current_wallet")
	private double previousWallet;
	@Column(name = "wallet")
	private double toBeAddedWallet;
	@Column(name = "wall_balance")
	private double effectiveWallet;
	@Column(name = "remark")
	private String remark;
	// ------------------
	@Column(name = "type")
	private String type;
	@Column(name = "pay_type")
	private String payType;
	@Column(name = "pay_term")
	private int payTerm;
	@Column(name = "addedby")
	private String addedBy;
	@Column(name = "pay_method")
	private String paymethod;
	@Column(name = "approveby")
	private String approveBy;
	// --------------------
	@Transient
	private String operation;
	@Transient
	private String systemId;
	@Transient
	private String effectiveUser;
	@Transient
	private String walletFlag;

	public RechargeEntry() {
	}

	public RechargeEntry(int userId, String time, String particular, long previousCredits, long toBeAddedCedits,
			double previousWallet, double toBeAddedWallet, String remark, String type, String addedBy) {
		this.userId = userId;
		this.time = time;
		this.particular = particular;
		this.previousCredits = previousCredits;
		this.toBeAddedCedits = toBeAddedCedits;
		this.previousWallet = previousWallet;
		this.toBeAddedWallet = toBeAddedWallet;
		this.remark = remark;
		this.type = type;
		this.addedBy = addedBy;
	}

	public String getPaymethod() {
		return paymethod;
	}

	public void setPaymethod(String paymethod) {
		this.paymethod = paymethod;
	}

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

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getParticular() {
		return particular;
	}

	public void setParticular(String particular) {
		this.particular = particular;
	}

	public long getPreviousCredits() {
		return previousCredits;
	}

	public void setPreviousCredits(long previousCredits) {
		this.previousCredits = previousCredits;
	}

	public long getToBeAddedCedits() {
		return toBeAddedCedits;
	}

	public void setToBeAddedCedits(long toBeAddedCedits) {
		this.toBeAddedCedits = toBeAddedCedits;
	}

	public long getEffectiveCredits() {
		return effectiveCredits;
	}

	public void setEffectiveCredits(long effectiveCredits) {
		this.effectiveCredits = effectiveCredits;
	}

	public double getPreviousWallet() {
		return previousWallet;
	}

	public void setPreviousWallet(double previousWallet) {
		this.previousWallet = previousWallet;
	}

	public double getToBeAddedWallet() {
		return toBeAddedWallet;
	}

	public void setToBeAddedWallet(double toBeAddedWallet) {
		this.toBeAddedWallet = toBeAddedWallet;
	}

	public double getEffectiveWallet() {
		return effectiveWallet;
	}

	public void setEffectiveWallet(double effectiveWallet) {
		this.effectiveWallet = effectiveWallet;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getEffectiveUser() {
		return effectiveUser;
	}

	public void setEffectiveUser(String effectiveUser) {
		this.effectiveUser = effectiveUser;
	}

	public String getWalletFlag() {
		return walletFlag;
	}

	public void setWalletFlag(String walletFlag) {
		this.walletFlag = walletFlag;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public int getPayTerm() {
		return payTerm;
	}

	public void setPayTerm(int payTerm) {
		this.payTerm = payTerm;
	}

	public String getAddedBy() {
		return addedBy;
	}

	public void setAddedBy(String addedBy) {
		this.addedBy = addedBy;
	}

	public String getApproveBy() {
		return approveBy;
	}

	public void setApproveBy(String approveBy) {
		this.approveBy = approveBy;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String toString() {
		return "recharge: id=" + id + ",Userid=" + userId + ",Particular=" + particular + ",Remark=" + remark;
	}
}
