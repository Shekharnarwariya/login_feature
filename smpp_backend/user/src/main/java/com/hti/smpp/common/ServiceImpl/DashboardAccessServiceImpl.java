package com.hti.smpp.common.ServiceImpl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.DashboardAccess.Entity.DashboardAccess;
import com.hti.smpp.common.DashboardAccess.Repository.DashboardAccessRepository;
import com.hti.smpp.common.Service.DashboardAccessService;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NoDataFoundException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
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
	public void updateDashboardVisibility(String username, int userId, List<String> dashboardVisibilityList) {
	    Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
	    UserEntry user = null;
	    if (userOptional.isPresent()) {
	        user = userOptional.get();
	        if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
	            throw new UnauthorizedException(messageResourceBundle
	                    .getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
	        }
	    } else {
	        logger.error("User not found with system ID: {}", username);
	        throw new NotFoundException(
	                messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
	    }

	    if (dashboardVisibilityList == null) {
			throw new NoDataFoundException("List can't be null ");
		}

		try {
			Optional<DashboardAccess> dashboard = dashboardAccessRepository.findByUserId(userId);
			DashboardAccess dashboardAccess = dashboard.get();
			
			if (dashboardAccess == null) {
				throw new InternalServerException("Data Not Found For the User with UserID : " + userId);
			} else {
				dashboardAccess.setMsgStatus(dashboardVisibilityList.contains("msgStatus"));
				dashboardAccess.setCountryWiseSub(dashboardVisibilityList.contains("countryWiseSub"));
				dashboardAccess.setSenderWiseSub(dashboardVisibilityList.contains("senderWiseSub"));
				dashboardAccess.setUserWiseSt(dashboardVisibilityList.contains("userWiseSt"));
				dashboardAccess.setSmscWiseSub(dashboardVisibilityList.contains("smscWiseSub"));
				dashboardAccess.setSmscWiseDvy(dashboardVisibilityList.contains("smscWiseDvy"));
				dashboardAccess.setSmscWiseSpam(dashboardVisibilityList.contains("smscWiseSpam"));
				dashboardAccess.setUserWiseSpam(dashboardVisibilityList.contains("userWiseSpam"));
				dashboardAccessRepository.save(dashboardAccess);
			}
		} catch (Exception e) {
			throw new InternalServerException("Error While Updating !! Please Try again later");
		}
	}


	@Override
	public DashboardAccess saveDashboardAccess(String username) {

		return null;
	}
}
