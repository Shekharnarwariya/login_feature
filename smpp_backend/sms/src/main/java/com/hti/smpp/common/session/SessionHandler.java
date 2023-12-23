package com.hti.smpp.common.session;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.logica.smpp.Connection;
import com.logica.smpp.Data;
import com.logica.smpp.Session;
import com.logica.smpp.TCPIPConnection;
import com.logica.smpp.pdu.BindRequest;
import com.logica.smpp.pdu.BindResponse;
import com.logica.smpp.pdu.BindTransmitter;
import com.logica.smpp.pdu.EnquireLinkResp;
import com.logica.smpp.pdu.Response;
import com.logica.smpp.pdu.UnbindResp;

@Service
public class SessionHandler implements Runnable {
	public SessionHandler() {
		super();
	}

	private String systemId;
	private String password;
	private int sessionLimit = 7;
	private Map<String, Integer> sessionCounter = Collections.synchronizedMap(new HashMap<String, Integer>());
	private List<UserSession> sessions = new ArrayList<UserSession>();
	private boolean stop;
	private Map<String, Long> LastSessionEnquire = Collections.synchronizedMap(new HashMap<String, Long>());
	private int timeout = 15;
	private Logger logger = LoggerFactory.getLogger("sessionLogger");
	private long lastActive = System.currentTimeMillis();
	private int next_allocation = 0;

	public SessionHandler(String systemId, String password) {
		this.systemId = systemId;
		this.password = password;
		logger.info(systemId + ": Session Handler Starting ");
		GlobalVars.UserSessionHandler.put(systemId + "#" + password, this);
		new Thread(this, "SessionHandler-" + systemId).start();
	}

	public synchronized UserSession getUserSession() {
		UserSession userSession = null;
		if (sessionCounter.size() < sessionLimit) {
			userSession = createConnection(systemId, password);
			if (userSession.getCommandStatus() == Data.ESME_ROK) {
				sessionCounter.put(userSession.getSessionId(), 0);
				synchronized (sessions) {
					sessions.add(userSession);
				}
			} else {
				userSession = new UserSession(systemId, password);
				userSession.setCommandStatus(Data.ESME_RBINDFAIL);
			}
		} else {
			logger.debug(systemId + " allocating Session Number: " + next_allocation);
			synchronized (sessions) {
				userSession = sessions.get(next_allocation);
			}
			if (next_allocation >= (sessionLimit - 1)) {
				next_allocation = 0;
			} else {
				next_allocation++;
			}
			sessionCounter.put(userSession.getSessionId(), 0); // reset enquire count
			logger.debug(systemId + " Cached Session Assigned: " + userSession.getSessionId());
		}
		return userSession;
	}

	public synchronized void putUserSession(UserSession userSession) {
		if (userSession != null && userSession.getSession() != null) {
			logger.debug(systemId + " Put Cached Session: " + userSession.getSessionId());
			LastSessionEnquire.put(userSession.getSessionId(), System.currentTimeMillis());
		}
	}

	private void removeSession(UserSession userSession) {
		sessionCounter.remove(userSession.getSessionId());
		logger.info(systemId + " <- Closing Session[" + userSession.getSessionId() + "] ->");
		try {
			userSession.getSession().close();
			logger.info(systemId + " <- Session Closed[" + userSession.getSessionId() + "] ->");
		} catch (Exception ex) {
			logger.info(systemId + " <- Closing Session Error[" + userSession.getSessionId() + "] ->");
		}
	}

	private void stopSession(UserSession userSession) {
		sessionCounter.remove(userSession.getSessionId());
		logger.info(systemId + " <- Stopping Session[" + userSession.getSessionId() + "] ->");
		try {
			UnbindResp unbind = userSession.getSession().unbind();
			logger.info(systemId + ":" + unbind.debugString());
			logger.info(systemId + " <- Session Stopped[" + userSession.getSessionId() + "] -> ");
		} catch (Exception ex) {
			logger.info(systemId + " <- Stop Session Error[" + userSession.getSessionId() + "] ->");
		}
	}

	private UserSession createConnection(String username, String password) {
		logger.info("Creating Connection Using <" + username + " & " + password + "> @" + IConstants.ip + ":"
				+ IConstants.SMPP_PORT);
		UserSession userSession = new UserSession(username, password);
		Session session = null;
		InetAddress inetAddress = null;

		try {

			inetAddress = InetAddress.getByName(IConstants.SMPP_PORT + "");
			int ipAsInt = byteArrayToInt(inetAddress.getAddress());

			Connection connection = new TCPIPConnection(IConstants.ip, ipAsInt);

			session = new Session(connection);
			BindRequest breq = new BindTransmitter();
			breq.setSystemId(username);
			breq.setPassword(password);
			breq.setInterfaceVersion(Data.SMPP_V34);
			breq.setSystemType("BULK");
			Response response = session.bind(breq);
			if (response != null) {
				userSession.setCommandStatus(response.getCommandStatus());
				if (response.getCommandStatus() == Data.ESME_ROK) {
					userSession.setSession(session);
					userSession.setSessionId(((BindResponse) response).getSystemId());
					logger.info(username + " Connected Transmitter[" + userSession.getSessionId() + "] : "
							+ response.debugString());
				} else {
					userSession.setSession(null);
					if (response.getCommandStatus() == 1035) {
						logger.info(username + " Insufficient balance : " + response.debugString());
						session = null;
					} else if (response.getCommandStatus() == Data.ESME_RINVSYSID) {
						logger.info(username + " Connection Error <Invalid SystemId>");
						session = null;
					} else if (response.getCommandStatus() == Data.ESME_RINVPASWD) {
						logger.info(username + " Connection Error <Invalid SystemId/password> ");
					} else if (response.getCommandStatus() == Data.ESME_RINVEXPIRY) {
						logger.info(username + "Connection Error <Account Expired>");
					} else if (response.getCommandStatus() == Data.ESME_RBINDFAIL) {
						logger.info(username + " Bind Failed : " + response.debugString());
					} else {
						logger.error(username + " Connection Failed : " + response.debugString());
					}
				}
			} else {
				logger.error(username + " Connection Failed <No Response>");
				userSession.setCommandStatus(-1);
				userSession.setSession(null);
			}
		} catch (Exception ex) {
			logger.error(username + " Connection Failed <" + ex + ">");
			userSession.setCommandStatus(-1);
			userSession.setSession(null);
		}
		return userSession;
	}

	public static int byteArrayToInt(byte[] bytes) {
		int addr = 0;
		for (byte b : bytes) {
			addr = addr << 8 | (b & 0xFF);
		}
		return addr;
	}

	@Override
	public void run() {
		// replace into the user details
		UserEntry userEntry = new UserEntry();
		if (userEntry != null) {
			timeout = userEntry.getTimeout();
		}
		logger.info(systemId + ":--> Session Handler Started.Timeout: " + timeout);
		if (timeout > 30) {
			timeout = timeout / 2;
		}
		while (!stop) {
			if (!sessions.isEmpty()) {
				logger.debug(systemId + ": Sessions Counter: " + sessions.size());
				synchronized (sessions) {
					Iterator<UserSession> itr = sessions.iterator();
					while (itr.hasNext()) {
						UserSession userSession = itr.next();
						try {
							logger.debug(systemId + "[" + userSession.getSessionId() + "]: Checking Enquire");
							if (LastSessionEnquire.containsKey(userSession.getSessionId())) {
								int enquireCount = 0;
								if (sessionCounter.containsKey(userSession.getSessionId())) {
									enquireCount = sessionCounter.get(userSession.getSessionId());
								}
								if (enquireCount < 20) {
									if (userSession.getCommandStatus() == Data.ESME_ROK) {
										long lastEnquire = LastSessionEnquire.get(userSession.getSessionId());
										if ((System.currentTimeMillis() - lastEnquire) >= (timeout * 1000)) {
											enquireCount++;
											sessionCounter.put(userSession.getSessionId(), enquireCount);
											logger.info(systemId + "[" + userSession.getSessionId()
													+ "]: Enquiring Session.EnquireCount: " + enquireCount
													+ " sessionCounter:" + sessionCounter);
											try {
												EnquireLinkResp enquireLink_resp = userSession.getSession()
														.enquireLink();
												lastEnquire = System.currentTimeMillis();
												enquireCount++;
												logger.info(systemId + "[" + userSession.getSessionId() + "]:"
														+ enquireLink_resp.debugString());
												LastSessionEnquire.put(userSession.getSessionId(),
														System.currentTimeMillis());
											} catch (Exception ex) {
												logger.info(systemId + "[" + userSession.getSessionId()
														+ "]: <- Enquire Session Error <" + ex + ">");
												itr.remove();
												removeSession(userSession);
											}
										} else {
											logger.debug(systemId + "[" + userSession.getSessionId()
													+ "]: Waiting Enquire.EnquireCount: " + enquireCount
													+ " sessionCounter:" + sessionCounter);
										}
									} else {
										logger.info(systemId + " <- Session Not Ok ->");
										itr.remove();
										removeSession(userSession);
										// break;
									}
								} else {
									logger.info(systemId + "[" + userSession.getSessionId()
											+ "]: <- Enquire Limit Exceeded ->" + enquireCount);
									itr.remove();
									stopSession(userSession);
									// break;
								}
							} else {
								logger.debug(
										systemId + " [" + userSession.getSessionId() + "] Not Added To EnquireCache");
							}
						} catch (Exception e) {
							logger.error(systemId + " [" + userSession.getSessionId() + "]", e.fillInStackTrace());
						}
					}
				}
			} else {
				logger.debug(systemId + ": Sessions Empty.");
			}
			if (sessionCounter.isEmpty() && ((System.currentTimeMillis() - lastActive) >= (3 * 60 * 1000))) { // 3
																												// minutes
				logger.info(systemId + ": Session Handler Stopping.");
				stop = true;
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}
			}
		}
		if (!sessions.isEmpty()) {
			logger.info("Stopping All Sessions for " + systemId);
			for (UserSession session : sessions) {
				stopSession(session);
			}
		}
		GlobalVars.UserSessionHandler.remove(systemId + "#" + password);
		logger.info(systemId + ": Session Handler Stopped.");
	}

	public void stop() {
		logger.info("SessionHandler Stop Request for " + systemId);
		stop = true;
	}
}
