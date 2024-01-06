package com.hti.smpp.common.service.impl;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hti.smpp.common.user.dto.User;

/**
 * Implementation of the Spring Security UserDetails interface.
 */
public class UserDetailsImpl implements UserDetails {
	private static final long serialVersionUID = 1L;

	private int id;

	private String username;

	@JsonIgnore
	private String password;

	private Collection<? extends GrantedAuthority> authorities;
/**
 * Constructor to create an instance of UserDetailsImpl.
 * @param id
 * @param username
 * @param password
 * @param authorities
 */
	public UserDetailsImpl(int id, String username, String password,
			Collection<? extends GrantedAuthority> authorities) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.authorities = authorities;
	}
/**
 * Build a UserDetailsImpl instance based on a User entity.
 * @param user
 * @return
 */
	public static UserDetailsImpl build(User user) {
		List<GrantedAuthority> authorities = Stream.of(user.getRole()).map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		return new UserDetailsImpl(user.getUserId(), user.getSystemId(), user.getPassword(), authorities);
	}
/**
 * Returns the authorities granted to the user.
 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}
//Getter and setter
	public int getId() {
		return id;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UserDetailsImpl user = (UserDetailsImpl) o;
		return Objects.equals(id, user.id);
	}
}
