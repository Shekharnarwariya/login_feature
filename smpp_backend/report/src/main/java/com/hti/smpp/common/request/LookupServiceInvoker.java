package com.hti.smpp.common.request;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;

import com.hti.rmi.LookupReport;
import com.hti.rmi.LookupService;
import com.hti.smpp.common.util.IConstants;

public class LookupServiceInvoker {

	private LookupService impl = null;

	public LookupServiceInvoker() throws IOException, NotBoundException {
		connectToRegistry(IConstants.HLR_IP, IConstants.HLR_RMI_PORT);
	}

	private void connectToRegistry(String address, int port) throws RemoteException, NotBoundException {
		Registry myRegistry = LocateRegistry.getRegistry(address, port);
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
