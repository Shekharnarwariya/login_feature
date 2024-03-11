/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Administrator
 */
public class UserServiceInvoke {
	UserService userService = null;
	private Logger logger = LoggerFactory.getLogger(UserServiceInvoke.class);

	public UserServiceInvoke() throws RemoteException, NotBoundException {
		// fire to localhost port 1080
		Registry myRegistry = LocateRegistry.getRegistry("127.0.0.1", 1080);
		// search for myMessage service
		userService = (UserService) myRegistry.lookup("userService");
	}

	/*
	 * public boolean refundBalance(String to, double amount) throws RemoteException, Exception { logger.debug(to + " refundBalance()"); return userService.refundBalance(to, amount); }
	 * 
	 * public boolean reduceBalance(String username, double cost) throws RemoteException, Exception { logger.debug(username + " reduceBalance()-> " + cost); return userService.reduceBalance(username,
	 * cost); }
	 */
	public boolean getUserHostStatus() throws RemoteException, Exception {
		return userService.getUserHostStatus();
	}

	public void setSmppFlagStatus(boolean status) throws RemoteException, Exception {
		userService.setSmppFlagStatus(status);
	}

	public void onHoldTraffic(boolean status) throws RemoteException, Exception {
		userService.onHoldTraffic(status);
	}

	public Map getUserSessionStatus() throws RemoteException, Exception {
		return userService.getUserSessionStatus();
	}
}
