package com.hti.smpp.common.services;


import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.user.dto.RechargeEntry;
import com.hti.smpp.common.user.dto.UserEntry;

@Service
public interface UserDAService   {
	
	// public int saveUserEntry(UserEntryExt entry);

//	public int saveRechargeEntry(RechargeEntry entry);
//
//	public void updateUserEntry(UserEntry entry);
//
//	public void updateWebMasterEntry(WebMasterEntry entry);
//
//	public void updateProfessionEntry(ProfessionEntry entry);
//
//	//public void updateUserEntryExt(UserEntryExt entry);
//
//	//public void deleteUserEntry(UserEntryExt entry);
//
//	public int validateUser(String systemId, String password);
//
//	public int validateUser(String accessKey);
//
//	public void saveWebMenuAccessEntry(WebMenuAccessEntry entry);
//
//	public void updateWebMenuAccessEntry(WebMenuAccessEntry entry);
//
//	// ------------ Get Entries ------------------------
//	public UserSessionObject getUserSessionObject(String systemId);
//
//	public UserEntry getUserEntry(int userid);
//
//	public UserEntry getUserEntry(String systemid);
//
////	public UserEntryExt getUserEntryExt(int userid);
////
////	public UserEntryExt getUserEntryExt(String systemid);
//	// public UserEntryExt getUserBalanceEntry(int userid);
//	// public UserEntryExt getUserBalanceEntry(String systemId);
//
//	// public BalanceEntry getBalanceEntry(int userid);
//	// public BalanceEntry getBalanceEntry(String systemId);
	
public UserEntry getInternUserEntry();
//
//	public WebMenuAccessEntry getWebMenuAccessEntry(int userId);
//
//	public ProfessionEntry getProfessionEntry(String systemId);
//
//	public WebMasterEntry getWebmasterEntry(String systemId);
//
//	// ------------- list names -----------------------
	public Map<Integer, String> listUsers();

//
//	public Map<Integer, String> listUsers(Set<String> roles);
//
//	public Map<Integer, String> listUsers(Integer[] useridarr);
//
	public Map<Integer, String> listUsersUnderMaster(String master);

	public Map<Integer, String> listUsersUnderSeller(int seller);
//
//	// ----------- list Entries -----------------------
////	public Map<Integer, UserEntryExt> listUserEntries();
////
////	public Map<Integer, UserEntryExt> listUserEntries(Set<String> roles);
////
////	public Map<Integer, UserEntryExt> listUserEntryUnderMaster(String master);
////
//	public Map<Integer, UserEntryExt> listUserEntryUnderSeller(int seller);
//
//
//	// -------------- Recharge Entries -------------------
	public Map<Integer, RechargeEntry> listRecentRecharges(Integer[] userid);

	public Map<Integer, List<RechargeEntry>> listTransactions(Integer[] userid, String txnType, String startTime,
			String endTime);

	public Map<Integer, List<RechargeEntry>> listTransactions(Integer[] userid);
	
	
	
	
//
//	// ----------- Web Access Log entries -----------------
//	public void saveAccessLogEntry(AccessLogEntry entry);
//
//	public List<AccessLogEntry> listAccessLog(int[] userid);
//
//	// --------- smpp session log -------------------------
//	public Map<String, SessionEntry> listSessionLog();
//
//	public Map<String, BindErrorEntry> listBindErrorLog();
//
//	// ---------- otp entry -------------------------------
//	public void saveOTPEntry(OTPEntry entry);
//
//	public void updateOTPEntry(OTPEntry entry);
//
//	public OTPEntry getOTPEntry(String systemId);

	public UserEntry getUserEntry(int userId);


}



