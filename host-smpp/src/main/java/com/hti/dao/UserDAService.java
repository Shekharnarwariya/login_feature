package com.hti.dao;

import java.util.Set;

import com.hti.exception.EntryNotFoundException;
import com.hti.user.UserBalance;
import com.hti.user.dto.UserEntry;

public interface UserDAService {
	public void reloadStatus(boolean init);

	public UserBalance getUserBalance(int user_id) throws EntryNotFoundException;

	public UserBalance getUserBalance(String system_id) throws EntryNotFoundException;
	
	public UserEntry getInternalUser();
}
