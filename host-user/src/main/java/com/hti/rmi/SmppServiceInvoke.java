package com.hti.rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class SmppServiceInvoke {
	SmppService impl = null;

	public SmppServiceInvoke() throws RemoteException, NotBoundException {
		// Connect SMPP Server on port 1081
		Registry myRegistry = LocateRegistry.getRegistry("127.0.0.1", 1081);
		// search for service
		impl = (SmppService) myRegistry.lookup("smppService");
	}

	/*
	 * public void userFlagAction(String username, String flag) throws RemoteException { impl.userFlagAction(username, flag); }
	 */
	public boolean getSmppFlagStatus() throws RemoteException {
		return impl.getSmppFlagStatus();
	}

	public void setUserHostStatus(boolean status) throws RemoteException, Exception {
		impl.setUserHostStatus(status);
	}
}
