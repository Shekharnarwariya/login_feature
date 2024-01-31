package com.hti.smpp.common.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hti.smpp.common.user.dto.User;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.MessageResourceBundle;
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
	@Autowired
	private MessageResourceBundle messageResourceBundle;
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userEntryRepository.getUsers(username)
				.orElseThrow(() -> new UsernameNotFoundException((
						messageResourceBundle.getExMessage(ConstantMessages.AUTHENTICATION_FAILED_USERNAME + username))));
		return UserDetailsImpl.build(user);
	}

}
