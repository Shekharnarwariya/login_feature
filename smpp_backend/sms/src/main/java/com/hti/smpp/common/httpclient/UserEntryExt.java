package com.hti.smpp.common.httpclient;

import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.BindErrorEntry;
import com.hti.smpp.common.user.dto.DlrSettingEntry;
import com.hti.smpp.common.user.dto.ProfessionEntry;
import com.hti.smpp.common.user.dto.RechargeEntry;
import com.hti.smpp.common.user.dto.SessionEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;

public class UserEntryExt {
	private UserEntry userEntry;
	private WebMasterEntry webMasterEntry;
	private DlrSettingEntry dlrSettingEntry;
	private ProfessionEntry professionEntry;
	private BalanceEntry balance;
	private RechargeEntry rechargeEntry;
	private SessionEntry sessionEntry;
	private BindErrorEntry bindErrorEntry;
	private String walletAmount;

	public UserEntryExt(UserEntry userEntry) {
		this.userEntry = userEntry;
	}

	public String getWalletAmount() {
		return walletAmount;
	}

	public void setWalletAmount(String walletAmount) {
		this.walletAmount = walletAmount;
	}

	public UserEntry getUserEntry() {
		return userEntry;
	}

	public void setUserEntry(UserEntry userEntry) {
		this.userEntry = userEntry;
	}

	public WebMasterEntry getWebMasterEntry() {
		if (webMasterEntry == null) {
			webMasterEntry = new WebMasterEntry();
		}
		return webMasterEntry;
	}

	public SessionEntry getSessionEntry() {
		return sessionEntry;
	}

	public void setSessionEntry(SessionEntry sessionEntry) {
		this.sessionEntry = sessionEntry;
	}

	public void setWebMasterEntry(WebMasterEntry webMasterEntry) {
		this.webMasterEntry = webMasterEntry;
	}

	public DlrSettingEntry getDlrSettingEntry() {
		return dlrSettingEntry;
	}

	public void setDlrSettingEntry(DlrSettingEntry dlrSettingEntry) {
		this.dlrSettingEntry = dlrSettingEntry;
	}

	public ProfessionEntry getProfessionEntry() {
		return professionEntry;
	}

	public void setProfessionEntry(ProfessionEntry professionEntry) {
		this.professionEntry = professionEntry;
	}

	public BalanceEntry getBalance() {
		return balance;
	}

	public void setBalance(BalanceEntry balance) {
		this.balance = balance;
	}

	public RechargeEntry getRechargeEntry() {
		return rechargeEntry;
	}

	public void setRechargeEntry(RechargeEntry rechargeEntry) {
		this.rechargeEntry = rechargeEntry;
	}

	public BindErrorEntry getBindErrorEntry() {
		return bindErrorEntry;
	}

	public void setBindErrorEntry(BindErrorEntry bindErrorEntry) {
		this.bindErrorEntry = bindErrorEntry;
	}

	public String toString() {
		return "UserEntryExt: [" + userEntry + "][" + webMasterEntry + "][" + dlrSettingEntry + "][" + professionEntry
				+ "][" + balance + "]";
	}
}
