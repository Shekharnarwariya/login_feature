package com.hti.smpp.common.response;

import java.util.Date;

import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.UserEntry;

public class TransactionResponseCustome {
	private Integer userId;
	private String walletFlag;
	private Double walletAmount;
	private Long credits;
	private int id;
	private String systemId;
	private String masterId;
	private Date expiry;
	private boolean hlr;
	private String currency;
	private String systemType;
	private String role;
	private int sleep;
	private String remark;
	private BalanceEntry balance;
	private String time;
	private String particular;
	private long previousCredits;
	private long toBeAddedCedits;
	private long effectiveCredits;
	private double previousWallet;
	private double toBeAddedWallet;
	private double effectiveWallet;
	private String type;
	private String payType;
	private int payTerm;
	private String addedBy;
	private String paymethod;
	private String approveBy;
	private String effectiveUser;

	public TransactionResponseCustome(Integer userId, String walletFlag, Double walletAmount, Long credits, int id,
			String systemId, String masterId, Date expiry, boolean hlr, String currency, String systemType, String role,
			int sleep, String remark, BalanceEntry balance, String time, String particular, long previousCredits,
			long toBeAddedCedits, long effectiveCredits, double previousWallet, double toBeAddedWallet,
			double effectiveWallet, String type, String payType, int payTerm, String addedBy, String paymethod,
			String approveBy, String effectiveUser) {
		super();
		this.userId = userId;
		this.walletFlag = walletFlag;
		this.walletAmount = walletAmount;
		this.credits = credits;
		this.id = id;
		this.systemId = systemId;
		this.masterId = masterId;
		this.expiry = expiry;
		this.hlr = hlr;
		this.currency = currency;
		this.systemType = systemType;
		this.role = role;
		this.sleep = sleep;
		this.remark = remark;
		this.balance = balance;
		this.time = time;
		this.particular = particular;
		this.previousCredits = previousCredits;
		this.toBeAddedCedits = toBeAddedCedits;
		this.effectiveCredits = effectiveCredits;
		this.previousWallet = previousWallet;
		this.toBeAddedWallet = toBeAddedWallet;
		this.effectiveWallet = effectiveWallet;
		this.type = type;
		this.payType = payType;
		this.payTerm = payTerm;
		this.addedBy = addedBy;
		this.paymethod = paymethod;
		this.approveBy = approveBy;
		this.effectiveUser = effectiveUser;
	}

	public TransactionResponseCustome(UserEntry userEntry) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "TransactionResponseCustome [userId=" + userId + ", walletFlag=" + walletFlag + ", walletAmount="
				+ walletAmount + ", credits=" + credits + ", id=" + id + ", systemId=" + systemId + ", masterId="
				+ masterId + ", expiry=" + expiry + ", hlr=" + hlr + ", currency=" + currency + ", systemType="
				+ systemType + ", role=" + role + ", sleep=" + sleep + ", remark=" + remark + ", balance=" + balance
				+ ", time=" + time + ", particular=" + particular + ", previousCredits=" + previousCredits
				+ ", toBeAddedCedits=" + toBeAddedCedits + ", effectiveCredits=" + effectiveCredits
				+ ", previousWallet=" + previousWallet + ", toBeAddedWallet=" + toBeAddedWallet + ", effectiveWallet="
				+ effectiveWallet + ", type=" + type + ", payType=" + payType + ", payTerm=" + payTerm + ", addedBy="
				+ addedBy + ", paymethod=" + paymethod + ", approveBy=" + approveBy + ", effectiveUser=" + effectiveUser
				+ ", getUserId()=" + getUserId() + ", getWalletFlag()=" + getWalletFlag() + ", getWalletAmount()="
				+ getWalletAmount() + ", getCredits()=" + getCredits() + ", getId()=" + getId() + ", getSystemId()="
				+ getSystemId() + ", getMasterId()=" + getMasterId() + ", getExpiry()=" + getExpiry() + ", isHlr()="
				+ isHlr() + ", getCurrency()=" + getCurrency() + ", getSystemType()=" + getSystemType() + ", getRole()="
				+ getRole() + ", getSleep()=" + getSleep() + ", getRemark()=" + getRemark() + ", getBalance()="
				+ getBalance() + ", getTime()=" + getTime() + ", getParticular()=" + getParticular()
				+ ", getPreviousCredits()=" + getPreviousCredits() + ", getToBeAddedCedits()=" + getToBeAddedCedits()
				+ ", getEffectiveCredits()=" + getEffectiveCredits() + ", getPreviousWallet()=" + getPreviousWallet()
				+ ", getToBeAddedWallet()=" + getToBeAddedWallet() + ", getEffectiveWallet()=" + getEffectiveWallet()
				+ ", getType()=" + getType() + ", getPayType()=" + getPayType() + ", getPayTerm()=" + getPayTerm()
				+ ", getAddedBy()=" + getAddedBy() + ", getPaymethod()=" + getPaymethod() + ", getApproveBy()="
				+ getApproveBy() + ", getEffectiveUser()=" + getEffectiveUser() + ", getClass()=" + getClass()
				+ ", hashCode()=" + hashCode() + ", toString()=" + super.toString() + "]";
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
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

	public Long getCredits() {
		return credits;
	}

	public void setCredits(Long credits) {
		this.credits = credits;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public Date getExpiry() {
		return expiry;
	}

	public void setExpiry(Date expiry) {
		this.expiry = expiry;
	}

	public boolean isHlr() {
		return hlr;
	}

	public void setHlr(boolean hlr) {
		this.hlr = hlr;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getSystemType() {
		return systemType;
	}

	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public int getSleep() {
		return sleep;
	}

	public void setSleep(int sleep) {
		this.sleep = sleep;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public BalanceEntry getBalance() {
		return balance;
	}

	public void setBalance(BalanceEntry balance) {
		this.balance = balance;
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

	public String getPaymethod() {
		return paymethod;
	}

	public void setPaymethod(String paymethod) {
		this.paymethod = paymethod;
	}

	public String getApproveBy() {
		return approveBy;
	}

	public void setApproveBy(String approveBy) {
		this.approveBy = approveBy;
	}

	public String getEffectiveUser() {
		return effectiveUser;
	}

	public void setEffectiveUser(String effectiveUser) {
		this.effectiveUser = effectiveUser;
	}

}