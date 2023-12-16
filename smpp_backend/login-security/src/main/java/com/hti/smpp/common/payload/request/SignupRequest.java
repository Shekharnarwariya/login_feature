package com.hti.smpp.common.payload.request;

import java.util.Arrays;
import java.util.Set;

import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.DlrSettingEntry;
import com.hti.smpp.common.user.dto.ProfessionEntry;
import com.hti.smpp.common.user.dto.RechargeEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.dto.WebMenuAccessEntry;

public class SignupRequest {

	private String language;
	private String username;
	private String email;
	private Set<String> role;
	private String masterId;
	private String password;
	private boolean adminDepend;
	private String expiry;
	private boolean hlr;
	private String currency;
	private String systemType;
	private int sleep;
	private String remark;
	private int priority;
	private double forceDelay;
	private int timeout;
	private String createdOn;
	private String accessIp;
	private String[] accessCountries;
	private boolean logging;
	private int logDays;
	private int senderLength;
	private boolean senderTrim;
	private String dltDefaultSender;
	private boolean bindAlert;
	private int alertWaitDuration;
	private String alertNumber;
	private String alertEmail;
	private String alertUrl;
	private boolean fixLongSms;
	private String flagValue;
	private int[] userids;
	private boolean skipLoopingRule;
	private boolean skipContent;
	private int loopSmscId;
	private String passwordExpiresOn;
	private boolean forcePasswordChange;
	private boolean recordMnp;
	private boolean optOut;
	private String shortCode;
	private BalanceEntry balance;
	private WebMasterEntry webMasterEntry;
	private DlrSettingEntry dlrSettingEntry;
	private ProfessionEntry professionEntry;
	private RechargeEntry rechargeEntry;
	private WebMenuAccessEntry webMenuAccessEntry;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Set<String> getRole() {
		return role;
	}

	public void setRole(Set<String> role) {
		this.role = role;
	}

	public String getMasterId() {
		return masterId;
	}

	public void setMasterId(String masterId) {
		this.masterId = masterId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isAdminDepend() {
		return adminDepend;
	}

	public void setAdminDepend(boolean adminDepend) {
		this.adminDepend = adminDepend;
	}

	public String getExpiry() {
		return expiry;
	}

	public void setExpiry(String expiry) {
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

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public double getForceDelay() {
		return forceDelay;
	}

	public void setForceDelay(double forceDelay) {
		this.forceDelay = forceDelay;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public String getAccessIp() {
		return accessIp;
	}

	public void setAccessIp(String accessIp) {
		this.accessIp = accessIp;
	}

	public String[] getAccessCountries() {
		return accessCountries;
	}

	public void setAccessCountries(String[] accessCountries) {
		this.accessCountries = accessCountries;
	}

	public boolean isLogging() {
		return logging;
	}

	public void setLogging(boolean logging) {
		this.logging = logging;
	}

	public int getLogDays() {
		return logDays;
	}

	public void setLogDays(int logDays) {
		this.logDays = logDays;
	}

	public int getSenderLength() {
		return senderLength;
	}

	public void setSenderLength(int senderLength) {
		this.senderLength = senderLength;
	}

	public boolean isSenderTrim() {
		return senderTrim;
	}

	public void setSenderTrim(boolean senderTrim) {
		this.senderTrim = senderTrim;
	}

	public String getDltDefaultSender() {
		return dltDefaultSender;
	}

	public void setDltDefaultSender(String dltDefaultSender) {
		this.dltDefaultSender = dltDefaultSender;
	}

	public boolean isBindAlert() {
		return bindAlert;
	}

	public void setBindAlert(boolean bindAlert) {
		this.bindAlert = bindAlert;
	}

	public int getAlertWaitDuration() {
		return alertWaitDuration;
	}

	public void setAlertWaitDuration(int alertWaitDuration) {
		this.alertWaitDuration = alertWaitDuration;
	}

	public String getAlertNumber() {
		return alertNumber;
	}

	public void setAlertNumber(String alertNumber) {
		this.alertNumber = alertNumber;
	}

	public String getAlertEmail() {
		return alertEmail;
	}

	public void setAlertEmail(String alertEmail) {
		this.alertEmail = alertEmail;
	}

	public String getAlertUrl() {
		return alertUrl;
	}

	public void setAlertUrl(String alertUrl) {
		this.alertUrl = alertUrl;
	}

	public boolean isFixLongSms() {
		return fixLongSms;
	}

	public void setFixLongSms(boolean fixLongSms) {
		this.fixLongSms = fixLongSms;
	}

	public String getFlagValue() {
		return flagValue;
	}

	public void setFlagValue(String flagValue) {
		this.flagValue = flagValue;
	}

	public int[] getUserids() {
		return userids;
	}

	public void setUserids(int[] userids) {
		this.userids = userids;
	}

	public boolean isSkipLoopingRule() {
		return skipLoopingRule;
	}

	public void setSkipLoopingRule(boolean skipLoopingRule) {
		this.skipLoopingRule = skipLoopingRule;
	}

	public boolean isSkipContent() {
		return skipContent;
	}

	public void setSkipContent(boolean skipContent) {
		this.skipContent = skipContent;
	}

	public int getLoopSmscId() {
		return loopSmscId;
	}

	public void setLoopSmscId(int loopSmscId) {
		this.loopSmscId = loopSmscId;
	}

	public String getPasswordExpiresOn() {
		return passwordExpiresOn;
	}

	public void setPasswordExpiresOn(String passwordExpiresOn) {
		this.passwordExpiresOn = passwordExpiresOn;
	}

	public boolean isForcePasswordChange() {
		return forcePasswordChange;
	}

	public void setForcePasswordChange(boolean forcePasswordChange) {
		this.forcePasswordChange = forcePasswordChange;
	}

	public boolean isRecordMnp() {
		return recordMnp;
	}

	public void setRecordMnp(boolean recordMnp) {
		this.recordMnp = recordMnp;
	}

	public boolean isOptOut() {
		return optOut;
	}

	public void setOptOut(boolean optOut) {
		this.optOut = optOut;
	}

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public BalanceEntry getBalance() {
		return balance;
	}

	public void setBalance(BalanceEntry balance) {
		this.balance = balance;
	}

	public WebMasterEntry getWebMasterEntry() {
		return webMasterEntry;
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

	public RechargeEntry getRechargeEntry() {
		return rechargeEntry;
	}

	public void setRechargeEntry(RechargeEntry rechargeEntry) {
		this.rechargeEntry = rechargeEntry;
	}

	public WebMenuAccessEntry getWebMenuAccessEntry() {
		return webMenuAccessEntry;
	}

	public void setWebMenuAccessEntry(WebMenuAccessEntry webMenuAccessEntry) {
		this.webMenuAccessEntry = webMenuAccessEntry;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public String toString() {
		return "SignupRequest [language=" + language + ", username=" + username + ", email=" + email + ", role=" + role
				+ ", masterId=" + masterId + ", password=" + password + ", adminDepend=" + adminDepend + ", expiry="
				+ expiry + ", hlr=" + hlr + ", currency=" + currency + ", systemType=" + systemType + ", sleep=" + sleep
				+ ", remark=" + remark + ", priority=" + priority + ", forceDelay=" + forceDelay + ", timeout="
				+ timeout + ", createdOn=" + createdOn + ", accessIp=" + accessIp + ", accessCountries="
				+ Arrays.toString(accessCountries) + ", logging=" + logging + ", logDays=" + logDays + ", senderLength="
				+ senderLength + ", senderTrim=" + senderTrim + ", dltDefaultSender=" + dltDefaultSender
				+ ", bindAlert=" + bindAlert + ", alertWaitDuration=" + alertWaitDuration + ", alertNumber="
				+ alertNumber + ", alertEmail=" + alertEmail + ", alertUrl=" + alertUrl + ", fixLongSms=" + fixLongSms
				+ ", flagValue=" + flagValue + ", userids=" + Arrays.toString(userids) + ", skipLoopingRule="
				+ skipLoopingRule + ", skipContent=" + skipContent + ", loopSmscId=" + loopSmscId
				+ ", passwordExpiresOn=" + passwordExpiresOn + ", forcePasswordChange=" + forcePasswordChange
				+ ", recordMnp=" + recordMnp + ", optOut=" + optOut + ", shortCode=" + shortCode + ", balance="
				+ balance + ", webMasterEntry=" + webMasterEntry + ", dlrSettingEntry=" + dlrSettingEntry
				+ ", professionEntry=" + professionEntry + ", rechargeEntry=" + rechargeEntry + ", webMenuAccessEntry="
				+ webMenuAccessEntry + "]";
	}

}