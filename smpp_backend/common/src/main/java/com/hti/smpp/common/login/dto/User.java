package com.hti.smpp.common.login.dto;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
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
	@Column(name = "user_id")
	private Long userId;

	@NotBlank
	@Size(max = 20)
	@Column(name = "system_id")
	private String systemId;

	@NotBlank
	@Size(max = 50)
	@Email
	@Column(name = "email")
	private String email;

	@NotBlank
	@Size(max = 120)
	@Column(name = "password")
	private String password;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_master", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	@Column(name = "Base64Password")
	private String Base64Password;

	@Column(name = "otpSecretKey")
	private String otpSecretKey;

	@Column(name = "otpSendTime")
	private LocalTime otpSendTime;

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

	public User(Long userId, @NotBlank @Size(max = 20) String systemId, @NotBlank @Size(max = 50) @Email String email,
			@NotBlank @Size(max = 120) String password, Set<Role> roles, String base64Password, String otpSecretKey,
			LocalTime otpSendTime) {
		super();
		this.userId = userId;
		this.systemId = systemId;
		this.email = email;
		this.password = password;
		this.roles = roles;
		Base64Password = base64Password;
		this.otpSecretKey = otpSecretKey;
		this.otpSendTime = otpSendTime;
	}

	public User() {
		super();

	}

}
