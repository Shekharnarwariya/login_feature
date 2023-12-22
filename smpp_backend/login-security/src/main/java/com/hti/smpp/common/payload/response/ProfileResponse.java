package com.hti.smpp.common.payload.response;

public class ProfileResponse {

	private String userName;

	private String email;

	private String roles;

	private String Base64Password;

	private String firstName;

	private String LastName;

	private String country;

	private String balance;

	private String contactNo;

	private String currency;

	public String getCurrency() {
		return currency;
	}

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

	public String getBase64Password() {
		return Base64Password;
	}

	public void setBase64Password(String base64Password) {
		Base64Password = base64Password;
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

	public ProfileResponse(String userName, String email, String roles, String base64Password, String firstName,
			String lastName, String country, String balance) {
		super();
		this.userName = userName;
		this.email = email;
		this.roles = roles;
		Base64Password = base64Password;
		this.firstName = firstName;
		LastName = lastName;
		this.country = country;
		this.balance = balance;
	}

	public ProfileResponse() {
		super();
	}

}
