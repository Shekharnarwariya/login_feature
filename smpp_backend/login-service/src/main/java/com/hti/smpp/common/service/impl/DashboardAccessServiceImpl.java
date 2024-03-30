package com.hti.smpp.common.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.DashboardAccess.Entity.DashboardAccess;
import com.hti.smpp.common.DashboardAccess.Repository.DashboardAccessRepository;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.response.DashboardAccessResponse;
import com.hti.smpp.common.service.DashboardAccessService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.MessageResourceBundle;

@Service
public class DashboardAccessServiceImpl implements DashboardAccessService {

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private MessageResourceBundle messageResourceBundle;

	@Autowired
	private DashboardAccessRepository dashboardAccessRepository;

	private static final Logger logger = LoggerFactory.getLogger(DashboardAccessServiceImpl.class.getName());

	@Override
	public DashboardAccessResponse updateDashboardVisibility(String username, List<String> dashboardVisibilityList) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		int userId;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			userId = user.getId();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			logger.error("User not found with system ID: {}", username);
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}
		
		try {
			
			Optional<DashboardAccess> dashboardOptional = dashboardAccessRepository.findByUserId(userId);
			DashboardAccess dashboardAccess;
			if(dashboardOptional.isPresent() && dashboardVisibilityList.isEmpty() || dashboardVisibilityList==null) {
				DashboardAccessResponse response = new DashboardAccessResponse();
				response = getDashboardAccess(username);
				return response;
			}
			
			else if (dashboardOptional.isPresent() && !dashboardVisibilityList.isEmpty()) {
				dashboardAccess = dashboardOptional.get();
				dashboardAccess.setMsgStatus(dashboardVisibilityList.contains("msgStatus"));
				dashboardAccess.setCountryWiseSub(dashboardVisibilityList.contains("countryWiseSub"));
				dashboardAccess.setSenderWiseSub(dashboardVisibilityList.contains("senderWiseSub"));
				dashboardAccess.setUserWiseSt(dashboardVisibilityList.contains("userWiseSt"));
				dashboardAccess.setSmscWiseSub(dashboardVisibilityList.contains("smscWiseSub"));
				dashboardAccess.setSmscWiseDvy(dashboardVisibilityList.contains("smscWiseDvy"));
				dashboardAccess.setSmscWiseSpam(dashboardVisibilityList.contains("smscWiseSpam"));
				dashboardAccess.setUserWiseSpam(dashboardVisibilityList.contains("userWiseSpam"));
				dashboardAccess.setAccountSummary(dashboardVisibilityList.contains("accountSummary"));
				
				dashboardAccessRepository.save(dashboardAccess);
			} else {
				dashboardAccess = new DashboardAccess();
				dashboardAccess.setUserId(userId);
				dashboardAccess.setCountryWiseSub(true);
				dashboardAccess.setMsgStatus(true);
				dashboardAccess.setSenderWiseSub(true);
				dashboardAccess.setSmscWiseDvy(true);
				dashboardAccess.setSmscWiseSpam(true);
				dashboardAccess.setSmscWiseSub(true);
				dashboardAccess.setUserWiseSpam(true);
				dashboardAccess.setUserWiseSt(true);
				dashboardAccess.setAccountSummary(true);
				dashboardAccessRepository.save(dashboardAccess);
				}
			
		} catch (NoSuchElementException ex) {
			throw new NotFoundException("No Data found with the userId:" + userId);
		} catch (Exception ex) {
			throw new InternalServerException("Error While Updating Data !! Please Try again later");
		}

		DashboardAccessResponse response = new DashboardAccessResponse();
		response = getDashboardAccess(username);
		return response;
	}

	
	@Override
	public DashboardAccessResponse getDashboardAccess(String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		int userId;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			userId = user.getId();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			logger.error("User not found with system ID: {}", username);
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}

		DashboardAccessResponse response = new DashboardAccessResponse();

		Optional<DashboardAccess> dashboardAccessOptionalData = dashboardAccessRepository.findByUserId(userId);
		if (!dashboardAccessOptionalData.isPresent()) {
			throw new InternalServerException("Data Not Found For the User with UserID : " + userId);
		} else {
			DashboardAccess dashboardAccessData = dashboardAccessOptionalData.get();
			response.setUserId(userId);
			Map<String, Boolean> visibilityMap = new HashMap<>();
			visibilityMap.put("msgStatus", dashboardAccessData.getMsgStatus());
			visibilityMap.put("countryWiseSub", dashboardAccessData.getCountryWiseSub());
			visibilityMap.put("senderWiseSub", dashboardAccessData.getSenderWiseSub());
			visibilityMap.put("userWiseSt", dashboardAccessData.getUserWiseSt());
			visibilityMap.put("smscWiseSub", dashboardAccessData.getSmscWiseSub());
			visibilityMap.put("smscWiseDvy", dashboardAccessData.getSmscWiseDvy());
			visibilityMap.put("smscWiseSpam", dashboardAccessData.getSmscWiseSpam());
			visibilityMap.put("userWiseSpam", dashboardAccessData.getUserWiseSpam());
			visibilityMap.put("accountSummary", dashboardAccessData.getAccountSummary());
			response.setVisibilityMap(visibilityMap);
		}
		return response;
	}

}
