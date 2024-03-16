package com.hti.smpp.common.response;
/**
 * Represents a response containing user profile information.
 */
public class ProfileResponse {

	private String userName;

	private String email;

	private String roles;

	private String firstName;

	private String LastName;

	private String country;

	private String balance;

	private String contactNo;

	private String currency;
	
	private byte[] profilePath;
	
	private String profileName;
	
	

	public String getProfileName() {
		return profileName;
	}
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	public String getCurrency() {
		return currency;
	}
//Getter and Setter 
	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getContactNo() {
		return contactNo;
	}

	public void setContactNo(String contactNo) {
		this.contactNo = contactNo;
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
		return LastName;
	}

	public void setLastName(String lastName) {
		LastName = lastName;
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

	public ProfileResponse(String userName, String email, String roles, String firstName, String lastName,
			String country, String balance) {
		super();
		this.userName = userName;
		this.email = email;
		this.roles = roles;
		this.firstName = firstName;
		LastName = lastName;
		this.country = country;
		this.balance = balance;
	}

	public ProfileResponse() {
		super();
	}
	public byte[] getProfilePath() {
		return profilePath;
	}
	public void setProfilePath(byte[] profilePath) {
		this.profilePath = profilePath;
	}
	
}
