package com.hti.smpp.common.service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.rmi.dto.LookupReport;
@Service
public interface LookupService extends Remote {
	
	List<LookupReport> getLookupReport(Map<String, String> params) throws RemoteException;

	int reCheckStatus(String sql) throws RemoteException;
}