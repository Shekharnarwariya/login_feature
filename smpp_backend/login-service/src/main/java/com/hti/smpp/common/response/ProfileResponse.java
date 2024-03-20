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
    private String balance;
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

    public ProfileResponse() {
        // Default constructor
    }

    public ProfileResponse(String userName, String email, String roles, String firstName, String lastName,
            String country, String balance, String contactNo, String currency,
            byte[] profilePath, String profileName, String companyName, String designation,
            String city, String state, int keepLogs, String referenceID,
            String companyAddress, String companyEmail, String taxID, String regID, String notes) {
this.userName = userName;
this.email = email;
this.roles = roles;
this.firstName = firstName;
this.lastName = lastName;
this.country = country;
this.balance = balance;
this.contactNo = contactNo;
this.currency = currency;
this.profilePath = profilePath;
this.profileName = profileName;
this.companyName = companyName;
this.designation = designation;
this.city = city;
this.state = state;
this.keepLogs = keepLogs;
this.referenceID = referenceID;
this.companyAddress = companyAddress;
this.companyEmail = companyEmail;
this.taxID = taxID;
this.regID = regID;
this.notes = notes;
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

	public String getBalance() {
		return balance;
	}

	public void setBalance(String balance) {
		this.balance = balance;
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
    

}

