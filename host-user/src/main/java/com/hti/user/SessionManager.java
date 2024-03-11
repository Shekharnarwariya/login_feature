/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.user;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.user.dto.UserEntry;
import com.hti.util.GlobalVars;

/**
 *
 * @author Administrator
 */
public class SessionManager {
	private String systemId;
	private volatile int sessionCount;
	private volatile int TxCount;
	private volatile int RxCount;
	private volatile int TRxCount;
	private final int sessionLimit;
	private final Map<String, UserSession> Receiver;
	private final Map<String, UserSession> Transmitter;
	private Logger logger = LoggerFactory.getLogger("userLogger");

	public SessionManager(String systemId) {
		this.systemId = systemId;
		sessionLimit = GlobalVars.NoOfSessionAllowed;
		Receiver = new HashMap<String, UserSession>();
		Transmitter = new HashMap<String, UserSession>();
		logger.info(systemId + " Creating UserSessionManager ");
	}

	public synchronized boolean isSessionUnderConstraint() {
		if (sessionCount < sessionLimit) {
			return true;
		} else {
			logger.error(systemId + " < Session Limit Exceeded > [" + sessionCount + "] [ Trx : " + TRxCount
					+ " ] [ Tx : " + TxCount + " ] [ Rx : " + RxCount + " ]");
			return false;
		}
	}

	public synchronized boolean add(UserSession session) {
		// logger.info(systemId +" Session Counter: "+sessionCount);
		if (sessionCount < sessionLimit) {
			sessionCount++;
			session.setSessionId(GlobalVars.getSessionId());
			logger.info("C R E A T E D  S E S S I O N [" + sessionCount + "] <" + session.getSessionId() + "-"
					+ session.getMode() + "> <" + systemId + ">");
			if (session.getMode().equalsIgnoreCase("reciever") || session.getMode().equalsIgnoreCase("tranciever")) {
				Receiver.put(session.getSessionId(), session);
				if (session.getMode().equalsIgnoreCase("reciever")) {
					RxCount++;
				} else {
					TRxCount++;
				}
			} else {
				Transmitter.put(session.getSessionId(), session);
				TxCount++;
			}
			return true;
		} else {
			logger.error(systemId + " < Session Limit Exceeded > [" + sessionCount + "] [ Trx : " + TRxCount
					+ " ] [ Tx : " + TxCount + " ] [ Rx : " + RxCount + " ]");
			return false;
		}
	}

	public synchronized void remove(UserSession session) {
		sessionCount--;
		if (session.getMode().equalsIgnoreCase("reciever") || session.getMode().equalsIgnoreCase("tranciever")) {
			Receiver.remove(session.getSessionId());
			if (session.getMode().equalsIgnoreCase("reciever")) {
				RxCount--;
			} else {
				TRxCount--;
			}
		} else {
			Transmitter.remove(session.getSessionId());
			TxCount--;
		}
		logger.info(systemId + " Remaining Session [" + sessionCount + "] [ Trx : " + TRxCount + " ] [ Tx : " + TxCount
				+ " ] [ Rx : " + RxCount + " ]");
	}

	public synchronized int getSessionCount() {
		return sessionCount;
	}

	public synchronized int getRxCount() {
		return RxCount;
	}

	public synchronized int getTRxCount() {
		return TRxCount;
	}

	public synchronized int getTxCount() {
		return TxCount;
	}

	public synchronized UserSession getReceiver() {
		Iterator itr = Receiver.values().iterator();
		while (itr.hasNext()) {
			UserSession receiver = (UserSession) itr.next();
			if (receiver.isKeepReceiving()) {
				return receiver;
			}
		}
		return null;
	}

	public synchronized void setUserEntry(UserEntry entry) {
		Receiver.forEach((k, v) -> {
			((UserSession) v).setUserEntry(entry);
		});
		Transmitter.forEach((k, v) -> {
			((UserSession) v).setUserEntry(entry);
		});
	}

	public synchronized void refresh() {
		Receiver.forEach((k, v) -> {
			((UserSession) v).reloadRouting();
		});
		Transmitter.forEach((k, v) -> {
			((UserSession) v).reloadRouting();
		});
	}

	public synchronized void refreshAdmin() {
		Receiver.forEach((k, v) -> {
			((UserSession) v).reloadAdminRouting();
		});
		Transmitter.forEach((k, v) -> {
			((UserSession) v).reloadAdminRouting();
		});
	}

	public synchronized void refreshNetwork() {
		Receiver.forEach((k, v) -> {
			((UserSession) v).reloadNetwork();
		});
		Transmitter.forEach((k, v) -> {
			((UserSession) v).reloadNetwork();
		});
	}
	
	public synchronized void refreshNetworkBsfm() {
		Receiver.forEach((k, v) -> {
			((UserSession) v).reloadNetworkBsfm();
		});
		Transmitter.forEach((k, v) -> {
			((UserSession) v).reloadNetworkBsfm();
		});
	}

	public synchronized void block() {
		logger.info(systemId + " Blocking Session [" + sessionCount + "] [ Trx/Rx : " + Receiver.size() + " ] [ Tx : "
				+ Transmitter.size() + " ]");
		Receiver.forEach((k, v) -> {
			((UserSession) v).stop();
		});
		Transmitter.forEach((k, v) -> {
			((UserSession) v).stop();
		});
	}

	public synchronized void unbind() {
		Receiver.forEach((k, v) -> {
			((UserSession) v).unbind();
		});
		Transmitter.forEach((k, v) -> {
			((UserSession) v).unbind();
		});
	}

	public synchronized void reset() {
		logger.info(systemId + " Reset Session [" + sessionCount + "] [ Trx/Rx : " + TRxCount + "/" + RxCount
				+ " ] [ Tx : " + TxCount + " ]");
		Receiver.clear();
		Transmitter.clear();
		sessionCount = 0;
		RxCount = 0;
		TRxCount = 0;
		TxCount = 0;
	}
}
