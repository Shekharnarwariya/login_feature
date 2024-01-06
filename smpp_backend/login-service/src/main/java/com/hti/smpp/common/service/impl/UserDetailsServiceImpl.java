package com.hti.smpp.common.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hti.smpp.common.user.dto.User;
import com.hti.smpp.common.user.repository.UserEntryRepository;
/**
 * Implementation of the Spring Security {@code UserDetailsService} interface.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UserEntryRepository userEntryRepository;
/**
 * Loads a user by the provided username and constructs a UserDetails object.
 */
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userEntryRepository.getUsers(username)
				.orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
		return UserDetailsImpl.build(user);
	}

}
