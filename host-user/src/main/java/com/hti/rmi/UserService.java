/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public interface UserService extends Remote {
	// ------------- Requested From SMPP --------------------------
	boolean getUserHostStatus() throws RemoteException, Exception;

	void setSmppFlagStatus(boolean status) throws RemoteException, Exception;

	void onHoldTraffic(boolean status) throws RemoteException, Exception;

	Map getUserSessionStatus() throws RemoteException, Exception;
}
