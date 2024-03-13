package com.hti.smpp.common.httpclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.smpp.common.session.UserSession;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.util.GlobalVarsSms;
import com.hti.smpp.common.util.IConstants;
import com.logica.smpp.Connection;
import com.logica.smpp.Data;
import com.logica.smpp.Session;
import com.logica.smpp.TCPIPConnection;
import com.logica.smpp.pdu.BindRequest;
import com.logica.smpp.pdu.BindTransmitter;
import com.logica.smpp.pdu.EnquireLinkResp;
import com.logica.smpp.pdu.Response;
import com.logica.smpp.pdu.UnbindResp;

public class HlrSessionHandler implements Runnable {
	private Logger logger = LoggerFactory.getLogger("hlrLogger");
	private UserSession userSession;
	private boolean stop;
	private long lastEnquire;
	private int enquireCount;
	private int timeout = 15;

	public HlrSessionHandler(String username, String password) {
		logger.info(username + ":--> Hlr Session Handler Starting ");
		userSession = createConnection(username, password);
		if (userSession.getCommandStatus() == Data.ESME_ROK) {
			GlobalVarsSms.HlrUserSessionHandler.put(username + "#" + password, this);
			new Thread(this, "HlrSessionHandler-" + username).start();
			logger.info(userSession.getUsername() + " <- Hlr Session Connected -> ");
		} else {
			if (userSession.getCommandStatus() == Data.ESME_RINVSYSID) {
				logger.info(userSession.getUsername() + " <- Hlr Session Creation Failed < "
						+ userSession.getCommandStatus() + " > Invalid SystemId ");
			} else if (userSession.getCommandStatus() == Data.ESME_RINVPASWD) {
				logger.info(userSession.getUsername() + " <- Hlr Session Creation Failed < "
						+ userSession.getCommandStatus() + " > Invalid Password ");
			} else if (userSession.getCommandStatus() == 1035) {
				logger.info(userSession.getUsername() + " <- Hlr Session Creation Failed < "
						+ userSession.getCommandStatus() + " > Insufficient Balance ");
			} else if (userSession.getCommandStatus() == Data.ESME_RINVEXPIRY) {
				logger.info(userSession.getUsername() + " <- Hlr Session Creation Failed < "
						+ userSession.getCommandStatus() + " > Account Expired ");
			} else {
				logger.info(userSession.getUsername() + " <- Hlr Session Creation Failed < "
						+ userSession.getCommandStatus() + " > ");
			}
		}
		lastEnquire = System.currentTimeMillis();
	}

	public synchronized UserSession getUserSession() {
		enquireCount = 0;
		return userSession;
	}

	private void removeSession() {
		logger.info(userSession.getUsername() + " <- Hlr Closing Session ->");
		GlobalVarsSms.HlrUserSessionHandler.remove(userSession.getUsername() + "#" + userSession.getPassword());
		try {
			userSession.getSession().close();
			logger.info(userSession.getUsername() + " <- Hlr Session Closed ->");
		} catch (Exception ex) {
			logger.info(userSession.getUsername() + " <- Hlr Closing Session Error ->");
		}
	}

	private void stopSession() {
		logger.info(userSession.getUsername() + " <- Hlr Stopping Session ->");
		GlobalVarsSms.HlrUserSessionHandler.remove(userSession.getUsername() + "#" + userSession.getPassword());
		try {
			UnbindResp unbind = userSession.getSession().unbind();
			logger.info(userSession.getUsername() + ":" + unbind.debugString());
			logger.info(userSession.getUsername() + " <- Hlr Session Stopped -> ");
		} catch (Exception ex) {
			logger.info(userSession.getUsername() + " <- Hlr Stop Session Error ->");
		}
	}

	private UserSession createConnection(String username, String password) {
		logger.info("Creating Hlr Connection Using <" + username + " & " + password + "> @" + IConstants.HLR_IP + ":"
				+ IConstants.HLR_PORT);
		UserSession userSession = new UserSession(username, password);
		Session session = null;
		Connection connection = new TCPIPConnection(IConstants.HLR_IP, IConstants.HLR_PORT);
		session = new Session(connection);
		BindRequest breq = new BindTransmitter();
		try {
			breq.setSystemId(username);
			breq.setPassword(password);
			breq.setInterfaceVersion(Data.SMPP_V34);
			breq.setSystemType("BULK");
			Response response = session.bind(breq);
			if (response != null) {
				userSession.setCommandStatus(response.getCommandStatus());
				if (response.getCommandStatus() == Data.ESME_ROK) {
					logger.info(username + " Hlr Connected Transmitter : " + response.debugString());
					userSession.setSession(session);
				} else {
					userSession.setSession(null);
					if (response.getCommandStatus() == 1035) {
						logger.info(username + " Hlr Insufficient balance : " + response.debugString());
						session = null;
					} else if (response.getCommandStatus() == Data.ESME_RINVSYSID) {
						logger.info(username + " Hlr Connection Error <Invalid SystemId>");
						session = null;
					} else if (response.getCommandStatus() == Data.ESME_RINVPASWD) {
						logger.info(username + " Hlr Connection Error <Invalid SystemId/password> ");
					} else if (response.getCommandStatus() == Data.ESME_RBINDFAIL) {
						logger.info(username + " Hlr Bind Failed : " + response.debugString());
					} else {
						logger.info(username + " Hlr Connection Failed : " + response.debugString());
					}
				}
			} else {
				logger.info(username + " Hlr Connection Failed <No Response>");
				userSession.setCommandStatus(-1);
				userSession.setSession(null);
			}
		} catch (Exception ex) {
			logger.info(username + " Hlr Connection Failed <" + ex + ">");
			userSession.setCommandStatus(-1);
			userSession.setSession(null);
		}
		return userSession;
	}

	@Override
	public void run() {
		UserService userService = new UserService();
		UserEntry userEntry = userService.getUserEntry(userSession.getUsername());
		if (userEntry != null) {
			timeout = userEntry.getTimeout();
		}
		logger.info(userSession.getUsername() + ":--> Hlr Session Handler Started.Timeout: " + timeout);
		if (timeout > 30) {
			timeout = timeout / 2;
		}
		while (!stop) {
			if (enquireCount <= 12) {
				if (userSession.getCommandStatus() == Data.ESME_ROK) {
					if ((System.currentTimeMillis() - lastEnquire) >= (timeout * 1000)) {
						logger.info(userSession.getUsername() + ": Hlr Enquiring Session");
						// System.out.println(userSession.getUsername() + " <- Hlr Enquiring Session
						// ->");
						try {
							EnquireLinkResp enquireLink_resp = userSession.getSession().enquireLink();
							lastEnquire = System.currentTimeMillis();
							enquireCount++;
							logger.info(userSession.getUsername() + ":" + enquireLink_resp.debugString());
						} catch (Exception ex) {
							logger.info(userSession.getUsername() + " <- Hlr Enquire Session Error <" + ex + ">");
							removeSession();
							break;
						}
					}
				} else {
					logger.info(userSession.getUsername() + " <- Hlr Session Not Ok ->");
					removeSession();
					break;
				}
			} else {
				logger.info(userSession.getUsername() + " <- Hlr Enquire Limit Exceeded ->");
				stopSession();
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
			}
		}
		// GlobalVars.UserSessionHandler.remove(userSession.getUsername() + "#" +
		// userSession.getPassword());
		logger.info(userSession.getUsername() + ":--> Hlr Session Handler Stopped ");
	}
}
