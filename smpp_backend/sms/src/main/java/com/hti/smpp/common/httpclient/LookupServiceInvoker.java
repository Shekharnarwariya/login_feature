/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.smpp.common.httpclient;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;

import com.hti.rmi.LookupReport;

/**
 *
 * @author Administrator
 */
public class LookupServiceInvoker {
	
	LookupService impl = null;

	public LookupServiceInvoker() throws RemoteException, NotBoundException {
		// Connect HLR Server on port 1098
		Registry myRegistry = LocateRegistry.getRegistry("127.0.0.1", 1098);
		// search for service
		impl = (LookupService) myRegistry.lookup("lookupService");
	}

	public List<LookupReport> getLookupReport(Map<String, String> params) throws RemoteException {
		System.out.println("LookupReport Params: " + params);
		return impl.getLookupReport(params);
	}

	public int reCheckStatus(String sql) throws RemoteException {
		return impl.reCheckStatus(sql);
	}
}
