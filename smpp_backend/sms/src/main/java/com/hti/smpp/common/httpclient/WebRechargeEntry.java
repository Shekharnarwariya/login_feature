package com.hti.smpp.common.httpclient;

public class WebRechargeEntry {
	public String master;
	public String password;
	public String targetUser;
	public String operation;
	public String amount;
	public String balanceMode;
	public String remoteAddr;
	public String role;

	public WebRechargeEntry() {
	}

	public WebRechargeEntry(String master, String password, String targetUser, String operation, String amount,
			String remoteAddr, String balanceMode) {
		this.master = master;
		this.password = password;
		this.targetUser = targetUser;
		this.operation = operation;
		this.amount = amount;
		this.remoteAddr = remoteAddr;
		this.balanceMode = balanceMode;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getRemoteAddr() {
		return remoteAddr;
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		this.master = master;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTargetUser() {
		return targetUser;
	}

	public void setTargetUser(String targetUser) {
		this.targetUser = targetUser;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getBalanceMode() {
		return balanceMode;
	}

	public void setBalanceMode(String balanceMode) {
		this.balanceMode = balanceMode;
	}

	public String toString() {
		return "apirecharge: master=" + master + ",targetUser=" + targetUser + ",Amount=" + amount + ",Operation="
				+ operation + ",balance_mode=" + balanceMode;
	}
}
