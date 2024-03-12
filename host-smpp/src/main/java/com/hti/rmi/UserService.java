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
 * UserService Interface to request service from UserHost Server
 */
public interface UserService extends Remote {
	// boolean refundBalance(String to, double amount) throws RemoteException, Exception;
	// boolean reduceBalance(String to, double amount) throws RemoteException, Exception;
	boolean getUserHostStatus() throws RemoteException, Exception;

	void setSmppFlagStatus(boolean status) throws RemoteException, Exception;

	void onHoldTraffic(boolean status) throws RemoteException, Exception;

	Map getUserSessionStatus() throws RemoteException, Exception;
	// void loadSmscConfig() throws RemoteException, Exception;
}
