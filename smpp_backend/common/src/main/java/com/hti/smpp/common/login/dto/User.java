package com.hti.smpp.common.login.dto;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	@NotBlank
	@Size(max = 20)
	private String systemId;

	@NotBlank
	@Size(max = 50)
	@Email
	private String email;

	@NotBlank
	@Size(max = 120)
	private String password;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_master", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	private String Base64Password;

	private String otpSecretKey;

	private LocalTime otpSendTime;

	private String firstName;

	private String LastName;

	private String country;

	private String language;

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

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public String getBase64Password() {
		return Base64Password;
	}

	public void setBase64Password(String base64Password) {
		Base64Password = base64Password;
	}

	public String getOtpSecretKey() {
		return otpSecretKey;
	}

	public void setOtpSecretKey(String otpSecretKey) {
		this.otpSecretKey = otpSecretKey;
	}

	public LocalTime getOtpSendTime() {
		return otpSendTime;
	}

	public void setOtpSendTime(LocalTime otpSendTime) {
		this.otpSendTime = otpSendTime;
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

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public User(Long userId, @NotBlank @Size(max = 20) String systemId, @NotBlank @Size(max = 50) @Email String email,
			@NotBlank @Size(max = 120) String password, Set<Role> roles, String base64Password, String otpSecretKey,
			LocalTime otpSendTime, String firstName, String lastName, String country, String language,
			String contactNo) {
		super();
		this.userId = userId;
		this.systemId = systemId;
		this.email = email;
		this.password = password;
		this.roles = roles;
		Base64Password = base64Password;
		this.otpSecretKey = otpSecretKey;
		this.otpSendTime = otpSendTime;
		this.firstName = firstName;
		LastName = lastName;
		this.country = country;
		this.language = language;
		this.contactNo = contactNo;
	}

	public User() {
		super();
	}

}
