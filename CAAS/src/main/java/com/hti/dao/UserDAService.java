package com.hti.dao;

import java.util.Map;

import com.hti.user.dto.BalanceEntry;
import com.hti.user.dto.DlrSettingEntry;
import com.hti.user.dto.ProfessionEntry;
import com.hti.user.dto.UserEntry;
import com.hti.user.dto.WebMasterEntry;

public interface UserDAService {
	public Map<Integer, UserEntry> listUser();
	
	public Map<Integer, UserEntry> listUser(Integer[] user_id);

	public Map<String, Integer> listUsernames();

	public Map<Integer, BalanceEntry> listBalance();

	//public void updateBalance(BalanceEntry entry);

	public Map<Integer, ProfessionEntry> listProfession();

	public Map<Integer, WebMasterEntry> listWebMaster();

	public Map<Integer, DlrSettingEntry> listDlrSetting();

	// --------- get single Entry ----------
	public UserEntry getUserEntry(int user_id);

	public BalanceEntry getBalance(int user_id);

	public ProfessionEntry getProfessionEntry(int user_id);

	public WebMasterEntry getWebMasterEntry(int user_id);

	public DlrSettingEntry getDlrSettingEntry(int user_id);
}
