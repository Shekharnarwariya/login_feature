/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.user.SessionManager;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;

/**
 *
 * @author Administrator
 */
public class UserServiceImpl extends UnicastRemoteObject implements UserService {
	private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	public UserServiceImpl() throws RemoteException {
	}

	@Override
	public boolean getUserHostStatus() throws RemoteException, Exception {
		return GlobalVars.APPLICATION_STATUS;
	}

	@Override
	public Map getUserSessionStatus() throws RemoteException, Exception {
		Map userSession = new HashMap();
		GlobalCache.UserSessionObject.forEach((k, v) -> {
			SessionManager sessionObject = ((SessionManager) v);
			if (sessionObject.getSessionCount() > 0) {
				int sessionCount[] = new int[3];
				sessionCount[0] = sessionObject.getTRxCount();
				sessionCount[1] = sessionObject.getTxCount();
				sessionCount[2] = sessionObject.getRxCount();
				userSession.put((String) k, sessionCount);
			}
		});
		return userSession;
	}

	@Override
	public void setSmppFlagStatus(boolean status) throws RemoteException, Exception {
		GlobalVars.SMPP_STATUS = status;
	}

	@Override
	public void onHoldTraffic(boolean status) throws RemoteException, Exception {
		if (status) {
			logger.info("<--- Traffic Hold On Request From SMPP Server --> ");
		} else {
			logger.info("<--- Traffic Resume Request From SMPP Server --> ");
		}
		GlobalVars.HOLD_ON_TRAFFIC = status;
	}
}
