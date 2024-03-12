package com.hti.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

// Used to Request Services From SMPP server
public interface SmppService extends Remote {
	boolean getSmppFlagStatus() throws RemoteException;

	void setUserHostStatus(boolean status) throws RemoteException, Exception;
}
