package com.hti.smpp.common.user.dto;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "usermaster")
public class UserEntry implements Serializable, Comparable<UserEntry> {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "system_id")
	private String systemId;
	@Column(name = "master_id")
	private String masterId;
	@Column(name = "password")
	private String password;
	@Column(name = "admindepend")
	private boolean adminDepend;
	@Column(name = "expiryday")
	private String expiry;
	@Column(name = "hlr")
	private boolean hlr;
	@Column(name = "currency")
	private String currency;
	@Column(name = "system_type")
	private String systemType;
	@Column(name = "role")
	private String role;
	@Column(name = "sleep_time")
	private int sleep;
	@Column(name = "Remark")
	private String remark;
	@Column(name = "Priority")
	private int priority;
	@Column(name = "force_delay")
	private double forceDelay;
	@Column(name = "timeout")
	private int timeout = 60;
	@Column(name = "createdon")
	private String createdOn;
	@Column(name = "access_ip")
	private String accessIp;
	@Column(name = "pdu_log")
	private boolean logging;
	@Column(name = "log_days")
	private int logDays;
	@Column(name = "createdby")
	private String createdBy;
	@Column(name = "editby")
	private String editBy;
	@Column(name = "editOn")
	private String editOn;
	@Column(name = "sender_length")
	private int senderLength;
	@Column(name = "sender_trim")
	private boolean senderTrim;
	@Column(name = "dlt_default_sender")
	private String dltDefaultSender;
	@Column(name = "fix_long_sms")
	private boolean fixLongSms;
	@Column(name = "bind_alert")
	private boolean bindAlert;
	@Column(name = "alert_wait_time")
	private int alertWaitDuration;
	@Column(name = "alert_number")
	private String alertNumber;
	@Column(name = "alert_email")
	private String alertEmail;
	@Column(name = "alert_url")
	private String alertUrl;
	@Column(name = "skip_loop_rule")
	private boolean skipLoopingRule;
	@Column(name = "skip_content")
	private boolean skipContent;
	@Column(name = "loop_smsc_id")
	private int loopSmscId;
	@Column(name = "pwd_expires_on")
	private String passwordExpiresOn;
	@Column(name = "force_pwd_change")
	private boolean forcePasswordChange;
	@Column(name = "access_country")
	private String accessCountry;
	@Column(name = "record_mnp")
	private boolean recordMnp;
	@Transient
	private String flagStatus;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UserEntry entry = (UserEntry) o;
		return (id == entry.id) && (systemId == null ? entry.systemId == null : systemId.equals(entry.systemId))
				&& (masterId == null ? entry.masterId == null : masterId.equals(entry.masterId))
				&& (password == null ? entry.password == null : password.equals(entry.password))
				&& (adminDepend == entry.adminDepend) && (hlr == entry.hlr) && (priority == entry.priority)
				&& (systemType == null ? entry.systemType == null : systemType.equals(entry.systemType))
				&& (expiry == null ? entry.expiry == null : expiry.equals(entry.expiry)) && (sleep == entry.sleep)
				&& (timeout == entry.timeout)
				&& (currency == null ? entry.currency == null : currency.equals(entry.currency))
				&& (role == null ? entry.role == null : role.equals(entry.role)) && (forceDelay == entry.forceDelay)
				&& (logging == entry.logging) && (logDays == entry.logDays)
				&& (accessIp == null ? entry.accessIp == null : accessIp.equals(entry.accessIp));
	}

	public UserEntry() {
	}

	public UserEntry(int id, String systemId) {
		this.id = id;
		this.systemId = systemId;
	}

	public UserEntry(int id, String systemId, String masterId, String currency) {
		this.id = id;
		this.systemId = systemId;
		this.masterId = masterId;
		this.currency = currency;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMasterId() {
		return masterId;
	}

	public void setMasterId(String masterId) {
		this.masterId = masterId;
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

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getAccessIp() {
		return accessIp;
	}

	public void setAccessIp(String accessIp) {
		this.accessIp = accessIp;
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

	public String getEditBy() {
		return editBy;
	}

	public void setEditBy(String editBy) {
		this.editBy = editBy;
	}

	public String getEditOn() {
		return editOn;
	}

	public void setEditOn(String editOn) {
		this.editOn = editOn;
	}

	public String getFlagStatus() {
		return flagStatus;
	}

	public void setFlagStatus(String flagStatus) {
		this.flagStatus = flagStatus;
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

	public boolean isFixLongSms() {
		return fixLongSms;
	}

	public void setFixLongSms(boolean fixLongSms) {
		this.fixLongSms = fixLongSms;
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

	public String getAccessCountry() {
		return accessCountry;
	}

	public void setAccessCountry(String accessCountry) {
		this.accessCountry = accessCountry;
	}

	public boolean isRecordMnp() {
		return recordMnp;
	}

	public void setRecordMnp(boolean recordMnp) {
		this.recordMnp = recordMnp;
	}

	public String toString() {
		return "id=" + id + ",SystemId=" + systemId + ",Password=" + password + ",Role=" + role + ",MasterId="
				+ masterId + ",AdminDepend=" + adminDepend + ",Currency=" + currency + ",hlr=" + hlr + ",timeout="
				+ timeout + ",CreatedOn:" + createdOn;
	}

	@Override
	public int compareTo(UserEntry o) {
		return this.getSystemId().compareTo(o.getSystemId());
	}
}