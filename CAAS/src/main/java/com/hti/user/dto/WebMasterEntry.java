package com.hti.user.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.hti.util.PasswordConverter;

/**
 * Entity bean with JPA annotations
 */
@Entity
@Table(name = "web_master")
public class WebMasterEntry implements Serializable {
	@Id
	@Column(name = "user_id", unique = true, nullable = false)
	private int userId;
	@Column(name = "seller_id")
	private int executiveId;
	@Column(name = "email")
	private String email;
	@Column(name = "min_flag")
	private boolean minFlag;
	@Column(name = "min_balance")
	private double minBalance;
	@Column(name = "min_email")
	private String minBalEmail;
	@Column(name = "sms_alert")
	private boolean smsAlert;
	@Column(name = "min_mobile")
	private String minBalMobile;
	@Column(name = "access")
	private String access;
	@Column(name = "mis_detail")
	private boolean misReport;
	@Column(name = "mob_DB_flag")
	private boolean mobileDBAccess;
	@Column(name = "webAccess")
	private boolean webAccess;
	@Column(name = "sales_alert")
	private boolean salesAlert;
	@Column(name = "hidden")
	private boolean hidden;
	@Column(name = "dlrreport")
	private boolean dlrReport;
	@Column(name = "dlr_email")
	private String dlrEmail;
	@Column(name = "coverage_email")
	private String coverageEmail;
	@Column(name = "coverage_report")
	private String coverageReport;
	@Column(name = "invoice_email")
	private String invoiceEmail;
	@Column(name = "download")
	private boolean download;
	@Column(name = "hideNum")
	private boolean hideNum;
	@Column(name = "cost_display")
	private boolean displayCost;
	@Column(name = "sender_id")
	private String senderId;
	@Column(name = "sender_restrict_to")
	private String senderRestrictTo;
	@Column(name = "acc_type")
	private String accountType;
	@Column(name = "gmt")
	private String gmt;
	@Column(name = "otp_login")
	private boolean otpLogin;
	@Column(name = "otp_number")
	private String otpNumber;
	@Column(name = "otp_email")
	private String otpEmail;
	@Column(name = "prefix_apply")
	private boolean prefixApply;
	@Column(name = "prefix_to_apply")
	private int prefixToApply;
	@Column(name = "number_length")
	private int numberLength;
	@Column(name = "opt_param")
	private boolean optionalParam;
	@Column(name = "auto_copy_route")
	private boolean autoCopyRouting;
	@Column(name = "route_margin")
	private String routeMargin;
	@Column(name = "prov_code", updatable = false)
	@Convert(converter = PasswordConverter.class)
	private String provCode;
	@Column(name = "sender_mgmt")
	private boolean senderMgmt;
	@Column(name = "sender_act")
	private boolean senderAct;
	@Column(name = "bulk_mgmt")
	private boolean bulkMgmt;
	@Column(name = "bulk_act")
	private boolean bulkAct;
	@Column(name = "confirm_submit")
	private boolean confirmSubmit;
	@Column(name = "hide_content")
	private boolean hideContent;
	@Column(name = "sec_master")
	private String secondaryMaster;
	@Column(name = "apiAccess")
	private boolean apiAccess;
	@Column(name = "email_on_login")
	private boolean emailOnLogin;
	@Column(name = "otp_sender")
	private String otpSender;
	@Column(name = "api_key_only")
	private boolean apiKeyOnly;
	@Column(name = "multi_user")
	private boolean multiUserAccess;
	@Column(name = "err_code_display")
	private boolean displayErrCode;
	@Column(name = "bulk_on_approve")
	private boolean bulkOnApprove;
	@Column(name = "sender_act_alert_number")
	private String senderActAlertNumber;
	@Column(name = "sender_act_alert_email")
	private String senderActAlertEmail;
	@Column(name = "batch_alert_number")
	private String batchAlertNumber;
	@Transient
	private String executiveName;

	public WebMasterEntry() {
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getExecutiveId() {
		return executiveId;
	}

	public void setExecutiveId(int executiveId) {
		this.executiveId = executiveId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isMinFlag() {
		return minFlag;
	}

	public void setMinFlag(boolean minFlag) {
		this.minFlag = minFlag;
	}

	public double getMinBalance() {
		return minBalance;
	}

	public void setMinBalance(double minBalance) {
		this.minBalance = minBalance;
	}

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public boolean isMisReport() {
		return misReport;
	}

	public void setMisReport(boolean misReport) {
		this.misReport = misReport;
	}

	public boolean isMobileDBAccess() {
		return mobileDBAccess;
	}

	public void setMobileDBAccess(boolean mobileDBAccess) {
		this.mobileDBAccess = mobileDBAccess;
	}

	public boolean isWebAccess() {
		return webAccess;
	}

	public void setWebAccess(boolean webAccess) {
		this.webAccess = webAccess;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isDownload() {
		return download;
	}

	public void setDownload(boolean download) {
		this.download = download;
	}

	public boolean isHideNum() {
		return hideNum;
	}

	public void setHideNum(boolean hideNum) {
		this.hideNum = hideNum;
	}

	public String getMinBalEmail() {
		return minBalEmail;
	}

	public void setMinBalEmail(String minBalEmail) {
		this.minBalEmail = minBalEmail;
	}

	public boolean isSmsAlert() {
		return smsAlert;
	}

	public void setSmsAlert(boolean smsAlert) {
		this.smsAlert = smsAlert;
	}

	public String getMinBalMobile() {
		return minBalMobile;
	}

	public void setMinBalMobile(String minBalMobile) {
		this.minBalMobile = minBalMobile;
	}

	public boolean isSalesAlert() {
		return salesAlert;
	}

	public void setSalesAlert(boolean salesAlert) {
		this.salesAlert = salesAlert;
	}

	public boolean isDlrReport() {
		return dlrReport;
	}

	public void setDlrReport(boolean dlrReport) {
		this.dlrReport = dlrReport;
	}

	public String getDlrEmail() {
		return dlrEmail;
	}

	public void setDlrEmail(String dlrEmail) {
		this.dlrEmail = dlrEmail;
	}

	public String getCoverageEmail() {
		return coverageEmail;
	}

	public void setCoverageEmail(String coverageEmail) {
		this.coverageEmail = coverageEmail;
	}

	public String getCoverageReport() {
		return coverageReport;
	}

	public void setCoverageReport(String coverageReport) {
		this.coverageReport = coverageReport;
	}

	public String getInvoiceEmail() {
		return invoiceEmail;
	}

	public void setInvoiceEmail(String invoiceEmail) {
		this.invoiceEmail = invoiceEmail;
	}

	public boolean isDisplayCost() {
		return displayCost;
	}

	public void setDisplayCost(boolean displayCost) {
		this.displayCost = displayCost;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public String getExecutiveName() {
		return executiveName;
	}

	public void setExecutiveName(String executiveName) {
		this.executiveName = executiveName;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getGmt() {
		return gmt;
	}

	public void setGmt(String gmt) {
		this.gmt = gmt;
	}

	public boolean isOtpLogin() {
		return otpLogin;
	}

	public void setOtpLogin(boolean otpLogin) {
		this.otpLogin = otpLogin;
	}

	public String getOtpNumber() {
		return otpNumber;
	}

	public void setOtpNumber(String otpNumber) {
		this.otpNumber = otpNumber;
	}

	public boolean isPrefixApply() {
		return prefixApply;
	}

	public void setPrefixApply(boolean prefixApply) {
		this.prefixApply = prefixApply;
	}

	public int getPrefixToApply() {
		return prefixToApply;
	}

	public void setPrefixToApply(int prefixToApply) {
		this.prefixToApply = prefixToApply;
	}

	public int getNumberLength() {
		return numberLength;
	}

	public void setNumberLength(int numberLength) {
		this.numberLength = numberLength;
	}

	public String getOtpEmail() {
		return otpEmail;
	}

	public void setOtpEmail(String otpEmail) {
		this.otpEmail = otpEmail;
	}

	public boolean isOptionalParam() {
		return optionalParam;
	}

	public void setOptionalParam(boolean optionalParam) {
		this.optionalParam = optionalParam;
	}

	public boolean isAutoCopyRouting() {
		return autoCopyRouting;
	}

	public void setAutoCopyRouting(boolean autoCopyRouting) {
		this.autoCopyRouting = autoCopyRouting;
	}

	public String getRouteMargin() {
		return routeMargin;
	}

	public void setRouteMargin(String routeMargin) {
		this.routeMargin = routeMargin;
	}

	public String getProvCode() {
		return provCode;
	}

	public void setProvCode(String provCode) {
		this.provCode = provCode;
	}

	public boolean isSenderMgmt() {
		return senderMgmt;
	}

	public void setSenderMgmt(boolean senderMgmt) {
		this.senderMgmt = senderMgmt;
	}

	public boolean isSenderAct() {
		return senderAct;
	}

	public void setSenderAct(boolean senderAct) {
		this.senderAct = senderAct;
	}

	public boolean isBulkMgmt() {
		return bulkMgmt;
	}

	public void setBulkMgmt(boolean bulkMgmt) {
		this.bulkMgmt = bulkMgmt;
	}

	public boolean isBulkAct() {
		return bulkAct;
	}

	public void setBulkAct(boolean bulkAct) {
		this.bulkAct = bulkAct;
	}

	public boolean isConfirmSubmit() {
		return confirmSubmit;
	}

	public void setConfirmSubmit(boolean confirmSubmit) {
		this.confirmSubmit = confirmSubmit;
	}

	public boolean isHideContent() {
		return hideContent;
	}

	public void setHideContent(boolean hideContent) {
		this.hideContent = hideContent;
	}

	public String getSecondaryMaster() {
		return secondaryMaster;
	}

	public void setSecondaryMaster(String secondaryMaster) {
		this.secondaryMaster = secondaryMaster;
	}

	public boolean isApiAccess() {
		return apiAccess;
	}

	public void setApiAccess(boolean apiAccess) {
		this.apiAccess = apiAccess;
	}

	public boolean isEmailOnLogin() {
		return emailOnLogin;
	}

	public void setEmailOnLogin(boolean emailOnLogin) {
		this.emailOnLogin = emailOnLogin;
	}

	public String getOtpSender() {
		return otpSender;
	}

	public void setOtpSender(String otpSender) {
		this.otpSender = otpSender;
	}

	public boolean isApiKeyOnly() {
		return apiKeyOnly;
	}

	public void setApiKeyOnly(boolean apiKeyOnly) {
		this.apiKeyOnly = apiKeyOnly;
	}

	public boolean isMultiUserAccess() {
		return multiUserAccess;
	}

	public void setMultiUserAccess(boolean multiUserAccess) {
		this.multiUserAccess = multiUserAccess;
	}

	public String getSenderRestrictTo() {
		return senderRestrictTo;
	}

	public void setSenderRestrictTo(String senderRestrictTo) {
		this.senderRestrictTo = senderRestrictTo;
	}

	public boolean isDisplayErrCode() {
		return displayErrCode;
	}

	public void setDisplayErrCode(boolean displayErrCode) {
		this.displayErrCode = displayErrCode;
	}

	public boolean isBulkOnApprove() {
		return bulkOnApprove;
	}

	public void setBulkOnApprove(boolean bulkOnApprove) {
		this.bulkOnApprove = bulkOnApprove;
	}

	public String getSenderActAlertNumber() {
		return senderActAlertNumber;
	}

	public void setSenderActAlertNumber(String senderActAlertNumber) {
		this.senderActAlertNumber = senderActAlertNumber;
	}

	public String getSenderActAlertEmail() {
		return senderActAlertEmail;
	}

	public void setSenderActAlertEmail(String senderActAlertEmail) {
		this.senderActAlertEmail = senderActAlertEmail;
	}

	public String getBatchAlertNumber() {
		return batchAlertNumber;
	}

	public void setBatchAlertNumber(String batchAlertNumber) {
		this.batchAlertNumber = batchAlertNumber;
	}

	public String toString() {
		return "web: userId=" + userId + ",Executiveid=" + executiveId + ",MinimumBalAlert=" + minFlag + ",MISReport="
				+ misReport + ",WebAccess=" + webAccess;
	}
}
