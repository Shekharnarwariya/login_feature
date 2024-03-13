package com.hti.smpp.common.httpclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.user.dto.OTPEntry;
import com.hti.smpp.common.user.dto.ProfessionEntry;
import com.hti.smpp.common.user.dto.RechargeEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.util.GlobalVars;

@Service
public class UserService {

	@Autowired
	private UserDAO userDAO;

	private Logger logger = LoggerFactory.getLogger(UserService.class);

	public UserEntryExt getUserEntryExt(String systemid) {
		logger.debug("getUserEntry(" + systemid + ")");
		if (GlobalVars.UserMapping.containsKey(systemid)) {
			int userid = GlobalVars.UserMapping.get(systemid);
			return getUserEntryExt(userid);
		} else {
			return null;
		}
	}

	public UserEntryExt getUserEntryExt(int userid) {
		logger.debug("getUserEntry(" + userid + ")");
		if (GlobalVars.UserEntries.containsKey(userid)) {
			UserEntryExt entry = new UserEntryExt(GlobalVars.UserEntries.get(userid));
			entry.setDlrSettingEntry(GlobalVars.DlrSettingEntries.get(userid));
			WebMasterEntry webEntry = GlobalVars.WebmasterEntries.get(userid);
			entry.setWebMasterEntry(webEntry);
			entry.setProfessionEntry(GlobalVars.ProfessionEntries.get(userid));
			logger.debug("end getUserEntry(" + userid + ")");
			return entry;
		} else {
			return null;
		}
	}

	public UserEntry getUserEntry(String systemid) {
		logger.debug("getUserEntry(" + systemid + ")");
		if (GlobalVars.UserMapping.containsKey(systemid)) {
			int userid = GlobalVars.UserMapping.get(systemid);
			return getUserEntry(userid);
		} else {
			return null;
		}
	}

	public UserEntry getUserEntry(int userid) {
		logger.debug("getUserEntry(" + userid + ")");
		if (GlobalVars.UserEntries.containsKey(userid)) {
			return GlobalVars.UserEntries.get(userid);
		} else {
			return null;
		}
	}

	public OTPEntry getOTPEntry(String systemId) {
		return userDAO.getOTPEntry(systemId);
	}

	public void updateOTPEntry(OTPEntry entry) {
		userDAO.updateOTPEntry(entry);
	}

	public void saveOTPEntry(OTPEntry entry) {
		userDAO.saveOTPEntry(entry);
	}

	public ProfessionEntry getProfessionEntry(String systemId) {
		ProfessionEntry entry = null;
		if (GlobalVars.UserMapping.containsKey(systemId)) {
			int userid = GlobalVars.UserMapping.get(systemId);
			entry = GlobalVars.ProfessionEntries.get(userid);
		}
		return entry;
	}

	public UserEntry getInternUserEntry() {
		logger.debug("Checking For User Internal User ");
		Predicate<Integer, UserEntry> p = new PredicateBuilderImpl().getEntryObject().get("role").equal("internal");
		for (UserEntry entry : GlobalVars.UserEntries.values(p)) {
			return entry;
		}
		return null;
	}

	public int validateUser(String systemId, String password) {
		logger.debug("Checking For User Validation: " + systemId + ":" + password);
		if (GlobalVars.UserMapping.containsKey(systemId)) {
			int userid = GlobalVars.UserMapping.get(systemId);
			UserEntry entry = GlobalVars.UserEntries.get(userid);
			if (entry.getPassword().equals(password)) {
				return entry.getId();
			} else {
				logger.info(systemId + " Invalid Password: " + password);
				return 0;
			}
		} else {
			logger.info(systemId + ": Invalid SystemId");
			return 0;
		}
	}

	public int validateUser(String accessKey) {
		logger.debug("Checking For User Validation AccessKey: " + accessKey);
		Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject().get("provCode")
				.equal(accessKey);
		for (WebMasterEntry webEntryItr : GlobalVars.WebmasterEntries.values(p)) {
			return webEntryItr.getUserId();
		}
		logger.info("Invalid accessKey: " + accessKey);
		return 0;
	}

	public int saveRechargeEntry(RechargeEntry entry) {
		logger.debug("saveRechargeEntry(" + entry + ")");
		return userDAO.saveRechargeEntry(entry);
	}
}
