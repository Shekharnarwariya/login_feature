package com.hti.smpp.common.response;
/**
 * Represents a response containing user profile information.
 */


public class ProfileResponse {

    private String userName;
    private String email;
    private String roles;
    private String firstName;
    private String lastName;
    private String country;
    private String contactNo;
    private String currency;
    private byte[] profilePath;
    private String profileName;
    private String companyName; 
    private String designation; 
    private String city; 
    private String state; 
    private int keepLogs; 
    private String referenceID; 
    private String companyAddress; 
    private String companyEmail; 
    private String taxID; 
    private String regID; 
    private String notes; 
    private long credits;
    private double wallets;
    private String walletFlag;
    
    private Boolean dlrReport;
    private String coverageReport;
    private Boolean dlrThroughWeb;
    private String alertEmail;
    private String alertMobile;
    private Boolean mis;
    private Boolean smsAlert;
    private double lowAmount;
    private String webUrl;
    private String dlrEmail;
    private String coverageEmail;
    private String invoiceEmail;
    private Boolean lowBalanceAlert;
    
    
    
    

    
	
	
//  ----------------------------------------------
	
	public Boolean getLowBalanceAlert() {
		return lowBalanceAlert;
	}
	public void setLowBalanceAlert(Boolean lowBalanceAlert) {
		this.lowBalanceAlert = lowBalanceAlert;
	}
	public Boolean getDlrReport() {
		return dlrReport;
	}
	public void setDlrReport(Boolean dlrReport) {
		this.dlrReport = dlrReport;
	}
	public String getCoverageReport() {
		return coverageReport;
	}
	public void setCoverageReport(String coverageReport) {
		this.coverageReport = coverageReport;
	}
	public Boolean getDlrThroughWeb() {
		return dlrThroughWeb;
	}
	public void setDlrThroughWeb(Boolean dlrThroughWeb) {
		this.dlrThroughWeb = dlrThroughWeb;
	}
	public String getAlertEmail() {
		return alertEmail;
	}
	public void setAlertEmail(String alertEmail) {
		this.alertEmail = alertEmail;
	}
	public String getAlertMobile() {
		return alertMobile;
	}
	public void setAlertMobile(String alertMobile) {
		this.alertMobile = alertMobile;
	}
	public Boolean getMis() {
		return mis;
	}
	public void setMis(Boolean mis) {
		this.mis = mis;
	}
	public Boolean getSmsAlert() {
		return smsAlert;
	}
	public void setSmsAlert(Boolean smsAlert) {
		this.smsAlert = smsAlert;
	}
	public double getLowAmount() {
		return lowAmount;
	}
	public void setLowAmount(double lowAmount) {
		this.lowAmount = lowAmount;
	}

	public String getWebUrl() {
		return webUrl;
	}
	public void setWebUrl(String webUrl) {
		this.webUrl = webUrl;
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
	public String getInvoiceEmail() {
		return invoiceEmail;
	}
	public void setInvoiceEmail(String invoiceEmail) {
		this.invoiceEmail = invoiceEmail;
	}
	
	
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getRoles() {
		return roles;
	}
	public void setRoles(String roles) {
		this.roles = roles;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getContactNo() {
		return contactNo;
	}
	public void setContactNo(String contactNo) {
		this.contactNo = contactNo;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public byte[] getProfilePath() {
		return profilePath;
	}
	public void setProfilePath(byte[] profilePath) {
		this.profilePath = profilePath;
	}
	public String getProfileName() {
		return profileName;
	}
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getDesignation() {
		return designation;
	}
	public void setDesignation(String designation) {
		this.designation = designation;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public int getKeepLogs() {
		return keepLogs;
	}
	public void setKeepLogs(int keepLogs) {
		this.keepLogs = keepLogs;
	}
	public String getReferenceID() {
		return referenceID;
	}
	public void setReferenceID(String referenceID) {
		this.referenceID = referenceID;
	}
	public String getCompanyAddress() {
		return companyAddress;
	}
	public void setCompanyAddress(String companyAddress) {
		this.companyAddress = companyAddress;
	}
	public String getCompanyEmail() {
		return companyEmail;
	}
	public void setCompanyEmail(String companyEmail) {
		this.companyEmail = companyEmail;
	}
	public String getTaxID() {
		return taxID;
	}
	public void setTaxID(String taxID) {
		this.taxID = taxID;
	}
	public String getRegID() {
		return regID;
	}
	public void setRegID(String regID) {
		this.regID = regID;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public long getCredits() {
		return credits;
	}
	public void setCredits(long credits) {
		this.credits = credits;
	}
	public double getWallets() {
		return wallets;
	}
	public void setWallets(double wallets) {
		this.wallets = wallets;
	}
	public String getWalletFlag() {
		return walletFlag;
	}
	public void setWalletFlag(String walletFlag) {
		this.walletFlag = walletFlag;
	}
   

}

