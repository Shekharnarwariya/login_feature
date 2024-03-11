package com.hti.user.dto;

import java.io.Serializable;

public class ProfessionEntry implements Serializable {
	private int userId;
	private String firstName;
	private String lastName;
	private String company;
	private String mobile;
	private String domainEmail;
	private String designation;
	private String city;
	private String state;
	private String country;
	private String referenceId;
	private String logoFileName;
	private byte[] logoFile;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
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

	public String getDomainEmail() {
		return domainEmail;
	}

	public void setDomainEmail(String domainEmail) {
		this.domainEmail = domainEmail;
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

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public String getLogoFileName() {
		return logoFileName;
	}

	public void setLogoFileName(String logoFileName) {
		this.logoFileName = logoFileName;
	}

	public byte[] getLogoFile() {
		return logoFile;
	}

	public void setLogoFile(byte[] logoFile) {
		this.logoFile = logoFile;
	}

	public String toString() {
		return "professionEntry: Userid=" + userId + ",Name=" + firstName + " " + lastName + ",Mobile=" + mobile
				+ ",company=" + company + ",Designation=" + designation + ",DomainEmail=" + domainEmail + ",city="
				+ city + ",State=" + state + ",Country=" + country;
	}
}
