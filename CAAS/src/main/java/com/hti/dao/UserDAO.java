package com.hti.dao;

import java.util.List;

import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.DlrSettingEntry;
import com.hti.smpp.common.user.dto.ProfessionEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;

public interface UserDAO {
	public List<UserEntry> listUser();
	
	public List<UserEntry> listUser(Integer[] user_id);

	public List<UserEntry> listUsernames();

	public List<BalanceEntry> listBalance();

	public void updateBalance(BalanceEntry entry);

	public List<ProfessionEntry> listProfession();

	public List<WebMasterEntry> listWebMaster();

	public List<DlrSettingEntry> listDlrSetting();

	// --------- get single Entry ----------
	public UserEntry getUserEntry(int user_id);

	public BalanceEntry getBalance(int user_id);

	public ProfessionEntry getProfessionEntry(int user_id);

	public WebMasterEntry getWebMasterEntry(int user_id);

	public DlrSettingEntry getDlrSettingEntry(int user_id);
}
