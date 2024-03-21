package com.hti.smpp.common.response;

import java.util.List;

import com.hti.smpp.common.dto.UserEntryExt;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.RechargeEntry;
import com.hti.smpp.common.user.dto.UserEntry;

public class TransactionResponse {

	private BalanceEntry balanceEntry;
	private List<RechargeEntry> rechargeEntries;
	private double totalCreditAmount;
	private double totalDebitAmount;
	private String currency;
	
	
	  public String getCurrency() {
	        return currency;
	    }

	    public void setCurrency(String currency) {
	        this.currency = currency;
	    }
	
	

	public double getTotalCreditAmount() {
		return totalCreditAmount;
	}

	public void setTotalCreditAmount(double totalCreditAmount) {
		this.totalCreditAmount = totalCreditAmount;
	}

	public double getTotalDebitAmount() {
		return totalDebitAmount;
	}

	public void setTotalDebitAmount(double totalDebitAmount) {
		this.totalDebitAmount = totalDebitAmount;
	}

	public List<RechargeEntry> getRechargeEntries() {
		return rechargeEntries;
	}

	public void setRechargeEntries(List<RechargeEntry> rechargeEntries) {
		this.rechargeEntries = rechargeEntries;
	}

	


	public BalanceEntry getBalanceEntry() {
		return balanceEntry;
	}

	public void setBalanceEntry(BalanceEntry balanceEntry) {
		this.balanceEntry = balanceEntry;
	}




}
