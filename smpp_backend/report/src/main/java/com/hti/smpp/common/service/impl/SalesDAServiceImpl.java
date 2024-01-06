package com.hti.smpp.common.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.dto.UserEntryExt;
import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.service.SalesDAService;
import com.hti.smpp.common.service.UserDAService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.UserSessionObject;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.util.GlobalVars;

public class SalesDAServiceImpl implements SalesDAService {

	@Override
	public int save(SalesEntry entry) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void update(SalesEntry entry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(SalesEntry entry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SalesEntry getEntry(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SalesEntry getEntry(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SalesEntry> list() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SalesEntry> list(String role) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, SalesEntry> listSellersUnderManager(String mgrId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, SalesEntry> saleslist() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, SalesEntry> Rolelist(String role) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, String> listNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, String> listNames(String role) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int validateEntry(String systemId, String password) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public UserSessionObject getSessionObject(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, String> listNamesUnderManager(String mgrId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, String> listUsernamesUnderManager(String mgrId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, SalesEntry> listSellerMappedManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, UserEntryExt> listUserEntryUnderManager(String mgrId) {
		// TODO Auto-generated method stub
		return null;
	}
	


}
