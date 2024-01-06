package com.hti.smpp.common.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.dto.UserEntryExt;
import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.user.dto.UserSessionObject;
@Service
public interface SalesDAService {
	
	
	public int save(SalesEntry entry) throws  Exception;

	public void update(SalesEntry entry);

	public void delete(SalesEntry entry);

	public SalesEntry getEntry(int id);

	public SalesEntry getEntry(String username);

	public List<SalesEntry> list();

	public List<SalesEntry> list(String role);

	//public List<SalesEntry> listSellersUnderManager(String mgrId);
	public Map<Integer, SalesEntry> listSellersUnderManager(String mgrId) ;
	public Map<Integer, SalesEntry> saleslist() ;
	
	//public Map<Integer, SalesEntry> listSellersUnderManager(String mgrId);
//	
//	public int save(SalesEntry entry) throws DuplicateEntryException, Exception;
//
//	public void update(SalesEntry entry);
//
//	public void delete(SalesEntry entry);
//
//	public Map<Integer, SalesEntry> list();
//
	public Map<Integer, SalesEntry> Rolelist(String role);

	public Map<Integer, String> listNames();

	public Map<Integer, String> listNames(String role);

	public int validateEntry(String systemId, String password);

//	public SalesEntry getEntry(int id);
//
//	public SalesEntry getEntry(String username);

	public UserSessionObject getSessionObject(int id);

	public Map<Integer, String> listNamesUnderManager(String mgrId);

	//public Map<Integer, SalesEntry> listSellersUnderManager(String mgrId);

	public Map<Integer, String> listUsernamesUnderManager(String mgrId);

	public Map<Integer, SalesEntry> listSellerMappedManager();

	public Map<Integer, UserEntryExt> listUserEntryUnderManager(String mgrId);
	
;
}

