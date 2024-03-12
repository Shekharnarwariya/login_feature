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
public interface SmppService extends Remote {
	// --- Requested From Websmpp -------------------
	void resend(Map params) throws RemoteException;

	int resendCount(Map params) throws RemoteException;

	// --- Requested From UserHost Server -------------------
	void setUserHostStatus(boolean status) throws RemoteException, Exception;

	boolean getSmppFlagStatus() throws RemoteException;
}
