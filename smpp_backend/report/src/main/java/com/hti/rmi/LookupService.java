package com.hti.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public interface LookupService extends Remote {
	
	List<LookupReport> getLookupReport(Map<String, String> params) throws RemoteException;

    int reCheckStatus(String sql) throws RemoteException;
}