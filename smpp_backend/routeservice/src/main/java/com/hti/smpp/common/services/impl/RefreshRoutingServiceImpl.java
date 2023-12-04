package com.hti.smpp.common.services.impl;

import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.UserRepository;
import com.hti.smpp.common.user.dto.UserSessionObject;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.IConstants;

public class RefreshRoutingServiceImpl {

	@Autowired
	private UserRepository loginRepository;

	private org.slf4j.Logger logger = LoggerFactory.getLogger(RefreshRoutingServiceImpl.class);

	public Object execute(String username) {
		String target = IConstants.FAILURE_KEY;
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		//saveMessages(request, messages);
		
		return target;
	}
}
