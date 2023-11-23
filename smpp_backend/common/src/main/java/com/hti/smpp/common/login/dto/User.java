package com.hti.smpp.common.login.dto;

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
	private Long system_id;

	@NotBlank
	@Size(max = 20)
	private String username;

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

	public String getOtpSecretKey() {
		return otpSecretKey;
	}

	public void setOtpSecretKey(String otpSecretKey) {
		this.otpSecretKey = otpSecretKey;
	}

	public Long getSystem_id() {
		return system_id;
	}

	public void setSystem_id(Long system_id) {
		this.system_id = system_id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	@Override
	public String toString() {
		return "User [system_id=" + system_id + ", username=" + username + ", email=" + email + ", password=" + password
				+ ", roles=" + roles + ", Base64Password=" + Base64Password + "]";
	}

	public User(@NotBlank @Size(max = 20) String username, @NotBlank @Size(max = 50) @Email String email,
			@NotBlank @Size(max = 120) String password, String base64Password) {
		super();
		this.username = username;
		this.email = email;
		this.password = password;
		Base64Password = base64Password;
	}

	public User() {
		super();
	}

}
