package com.hti.dao;

import java.util.Collection;

import com.hti.exception.EntryNotFoundException;
import com.hti.user.RoutingThread;
import com.hti.user.SessionManager;
import com.hti.user.UserBalance;
import com.hti.user.UserDeliverForward;
import com.hti.user.dto.DlrSettingEntry;
import com.hti.user.dto.ProfessionEntry;
import com.hti.user.dto.SessionEntry;
import com.hti.user.dto.UserEntry;

public interface UserDAService {
	public Collection<UserEntry> listUserEntries();

	public UserEntry getUserEntry(int user_id);

	public UserEntry getUserEntry(String system_id);
	
	public UserEntry getInternalUser();

	public DlrSettingEntry getDlrSettingEntry(String system_id);

	public DlrSettingEntry getDlrSettingEntry(int user_id);
	
	public ProfessionEntry getProfessionEntry(String systemId);

	public void reloadUserFlagStatus();
	
	public void initializeUserBsfm();

	public void initializeStatus();

	public RoutingThread getRoutingThread(String systemId) throws Exception;

	public SessionManager getSessionManager(String systemId);

	public void updateSession(SessionEntry entry);

	public void updateBindError(SessionEntry entry);

	public UserBalance getUserBalance(int user_id) throws EntryNotFoundException;

	public UserBalance getUserBalance(String systemId) throws EntryNotFoundException;

	public UserDeliverForward checkDeliverForward(String systemId);
	
	public String getCountryname(String ip_address);
	
	public String getWebDlrParam(String msg_id);
	
	
}
