package com.hti.smpp.common.user.dto;

import org.springframework.stereotype.Component;

@Component
public class UserSessionObject {
	private int id;
	private String systemId;
	private String masterId;
	private String password;
	private boolean adminDepend;
	private String expiry;
	private boolean hlr;
	private String currency;
	private String systemType;
	private String role;
	private int sleep;
	private String remark;
	private int priority;
	private double forceDelay;
	private int timeout;
	private String createdOn;
	private String accessIp;
	private int senderLength;
	private boolean skipContent;
	private BalanceEntry balance;
	private WebMasterEntry webMasterEntry;
	private DlrSettingEntry dlrSettingEntry;
	private ProfessionEntry professionEntry;
	private WebMenuAccessEntry webMenuAccessEntry;
	// ------------ other fields --------
	private BalanceEntry masterBalance;
	private String flagValue;
	private int masterUserId;
	private boolean smscOwner;
	// ------- user's timezone ----------
	private String defaultGmt;
	private String defaultGmtValue;
	// ----------- Counters --------------
	private int userCount;
	private int adminCount;
	private int exeCount;
	private int routeCount;
	private int otp;
	// ---- multi user --------
	private String multiUsername;

	public UserSessionObject() {
		super();
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public String getSystemType() {
		return systemType;
	}

	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public BalanceEntry getBalance() {
		if (balance == null) {
			balance = new BalanceEntry();
		}
		return balance;
	}

	public void setBalance(BalanceEntry balance) {
		this.balance = balance;
	}

	public WebMasterEntry getWebMasterEntry() {
		if (webMasterEntry == null) {
			webMasterEntry = new WebMasterEntry();
		}
		return webMasterEntry;
	}

	public void setWebMasterEntry(WebMasterEntry webMasterEntry) {
		this.webMasterEntry = webMasterEntry;
	}

	public boolean isAdminDepend() {
		return adminDepend;
	}

	public void setAdminDepend(boolean adminDepend) {
		this.adminDepend = adminDepend;
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

	public DlrSettingEntry getDlrSettingEntry() {
		if (dlrSettingEntry == null) {
			dlrSettingEntry = new DlrSettingEntry();
		}
		return dlrSettingEntry;
	}

	public void setDlrSettingEntry(DlrSettingEntry dlrSettingEntry) {
		this.dlrSettingEntry = dlrSettingEntry;
	}

	public ProfessionEntry getProfessionEntry() {
		if (professionEntry == null) {
			professionEntry = new ProfessionEntry();
		}
		return professionEntry;
	}

	public void setProfessionEntry(ProfessionEntry professionEntry) {
		this.professionEntry = professionEntry;
	}

	public String getFlagValue() {
		return flagValue;
	}

	public void setFlagValue(String flagValue) {
		this.flagValue = flagValue;
	}

	public int getUserCount() {
		return userCount;
	}

	public void setUserCount(int userCount) {
		this.userCount = userCount;
	}

	public int getAdminCount() {
		return adminCount;
	}

	public void setAdminCount(int adminCount) {
		this.adminCount = adminCount;
	}

	public int getExeCount() {
		return exeCount;
	}

	public void setExeCount(int exeCount) {
		this.exeCount = exeCount;
	}

	public int getRouteCount() {
		return routeCount;
	}

	public void setRouteCount(int routeCount) {
		this.routeCount = routeCount;
	}

	public BalanceEntry getMasterBalance() {
		return masterBalance;
	}

	public void setMasterBalance(BalanceEntry masterBalance) {
		this.masterBalance = masterBalance;
	}

	public int getMasterUserId() {
		return masterUserId;
	}

	public void setMasterUserId(int masterUserId) {
		this.masterUserId = masterUserId;
	}

	public String getAccessIp() {
		return accessIp;
	}

	public void setAccessIp(String accessIp) {
		this.accessIp = accessIp;
	}

	public String getDefaultGmt() {
		return defaultGmt;
	}

	public void setDefaultGmt(String defaultGmt) {
		this.defaultGmt = defaultGmt;
	}

	public String getDefaultGmtValue() {
		return defaultGmtValue;
	}

	public void setDefaultGmtValue(String defaultGmtValue) {
		this.defaultGmtValue = defaultGmtValue;
	}

	public int getOtp() {
		return otp;
	}

	public void setOtp(int otp) {
		this.otp = otp;
	}

	public int getSenderLength() {
		return senderLength;
	}

	public void setSenderLength(int senderLength) {
		this.senderLength = senderLength;
	}

	public boolean isSkipContent() {
		return skipContent;
	}

	public void setSkipContent(boolean skipContent) {
		this.skipContent = skipContent;
	}

	public boolean isSmscOwner() {
		return smscOwner;
	}

	public void setSmscOwner(boolean smscOwner) {
		this.smscOwner = smscOwner;
	}

	public String getMultiUsername() {
		return multiUsername;
	}

	public void setMultiUsername(String multiUsername) {
		this.multiUsername = multiUsername;
	}

	public WebMenuAccessEntry getWebMenuAccessEntry() {
		if (webMenuAccessEntry == null) {
			webMenuAccessEntry = new WebMenuAccessEntry();
		}
		return webMenuAccessEntry;
	}

	public void setWebMenuAccessEntry(WebMenuAccessEntry webMenuAccessEntry) {
		this.webMenuAccessEntry = webMenuAccessEntry;
	}

	public String toString() {
		return "id=" + id + ",SystemId=" + systemId + ",Role=" + role + ",MasterId=" + masterId + ",AdminDepend="
				+ adminDepend;
	}

	public UserSessionObject(int id, String systemId, String masterId, String password, boolean adminDepend,
			String expiry, boolean hlr, String currency, String systemType, String role, int sleep, String remark,
			int priority, double forceDelay, int timeout, String createdOn, String accessIp, int senderLength,
			boolean skipContent, BalanceEntry balance, WebMasterEntry webMasterEntry, DlrSettingEntry dlrSettingEntry,
			ProfessionEntry professionEntry, WebMenuAccessEntry webMenuAccessEntry, BalanceEntry masterBalance,
			String flagValue, int masterUserId, boolean smscOwner, String defaultGmt, String defaultGmtValue,
			int userCount, int adminCount, int exeCount, int routeCount, int otp, String multiUsername) {
		super();
		this.id = id;
		this.systemId = systemId;
		this.masterId = masterId;
		this.password = password;
		this.adminDepend = adminDepend;
		this.expiry = expiry;
		this.hlr = hlr;
		this.currency = currency;
		this.systemType = systemType;
		this.role = role;
		this.sleep = sleep;
		this.remark = remark;
		this.priority = priority;
		this.forceDelay = forceDelay;
		this.timeout = timeout;
		this.createdOn = createdOn;
		this.accessIp = accessIp;
		this.senderLength = senderLength;
		this.skipContent = skipContent;
		this.balance = balance;
		this.webMasterEntry = webMasterEntry;
		this.dlrSettingEntry = dlrSettingEntry;
		this.professionEntry = professionEntry;
		this.webMenuAccessEntry = webMenuAccessEntry;
		this.masterBalance = masterBalance;
		this.flagValue = flagValue;
		this.masterUserId = masterUserId;
		this.smscOwner = smscOwner;
		this.defaultGmt = defaultGmt;
		this.defaultGmtValue = defaultGmtValue;
		this.userCount = userCount;
		this.adminCount = adminCount;
		this.exeCount = exeCount;
		this.routeCount = routeCount;
		this.otp = otp;
		this.multiUsername = multiUsername;
	}

}
