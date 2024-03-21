package com.hti.smpp.common.user.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "profession_info")
public class ProfessionEntry implements Serializable {
	@Id
	@Column(name = "user_id", unique = true, nullable = false)
	private int userId;
	@Column(name = "first_name")
	private String firstName;
	@Column(name = "last_name")
	private String lastName;
	@Column(name = "company")
	private String company;
	@Column(name = "mobile")
	private String mobile;
	@Column(name = "domain_email")
	private String domainEmail;
	@Column(name = "designation")
	private String designation;
	@Column(name = "city")
	private String city;
	@Column(name = "state")
	private String state;
	@Column(name = "country")
	private String country;
	@Column(name = "ref_id")
	private String referenceId;
	@Column(name = "logo_file_type")
	private String logoFileName;
	@Column(name = "logo_file")
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
