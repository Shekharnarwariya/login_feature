/*
 * Copyright (c) 1996-2001
 * Logica Mobile Networks Limited
 * All rights reserved.
 *
 * This software is distributed under Logica Open Source License Version 1.0
 * ("Licence Agreement"). You shall use it and distribute only in accordance
 * with the terms of the License Agreement.
 *
 */
package com.hti.user;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import org.slf4j.Logger;
//import com.logica.route.RoutePDU;
import org.slf4j.LoggerFactory;

import com.hti.user.dto.UserEntry;
import com.hti.user.smpp.UserReceiver;
import com.hti.user.smpp.UserTransmitter;
import com.hti.util.GlobalVars;
import com.logica.smpp.Connection;
import com.logica.smpp.Data;
import com.logica.smpp.TimeoutException;
import com.logica.smpp.pdu.PDU;
import com.logica.smpp.pdu.PDUException;
import com.logica.smpp.pdu.Request;
import com.logica.smpp.pdu.Response;
import com.logica.smpp.pdu.Unbind;
import com.logica.smpp.pdu.ValueNotSetException;

/**
 * This class represent one client connection to the server starting by accepting the connection, authenticating of the client, communication and finished by un binding. The <code>UserSession</code>
 * object is generated by <code>SMSCListener</code> which also sets the session's PDU processor. Session is run in separate thread; it reads PDUs from the connection and calls PDU processor's client
 * methods to process the received PDUs. PDU processor on turn can use the session to submit PDUs to the client. For receiving and sending of PDUs the session uses instances of <code>Receiver</code>
 * and <code>Transmitter</code>.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version 1.0, 21 Jun 2001
 * @see SMSCListener
 * @see PDUProcessor
 * @see Connection
 * @see Receiver
 * @see Transmitter
 */
public class UserSession implements Runnable {
	private Logger logger = LoggerFactory.getLogger("userLogger");
	// private Logger pdulogger = LoggerFactory.getLogger("pduLogger");
	private UserReceiver receiver;
	private UserTransmitter transmitter;
	private PDUProcessor pduProcessor;
	private Connection connection;
	private long receiveTimeout = Data.RECEIVER_TIMEOUT;
	public boolean keepReceiving = true;
	// private long next_time_to_cheak;
	public long closetime = System.currentTimeMillis();
	public String mode;
	public String sessionId;
	private String systemid;
	private String systemType;
	private long closeTime;
	private String requestIp;
	// private Logger pdulogger = LoggerFactory.getLogger("submitLogger");

	/**
	 * Initialises the session with the connection the session should communicate over.
	 *
	 * @param connection
	 *            the connection object for communication with client
	 */
	// private boolean masterRouting_status;
	public UserSession(Connection connection) {
		this.connection = connection;
		requestIp = connection.getRemoteAddress();
		transmitter = new UserTransmitter(connection);
		receiver = new UserReceiver(transmitter, connection);
		pduProcessor = new HtiPDUProcessor(this);
	}

	public UserSession() {
	}

	/**
	 * Signals the session's thread that it should stop. Doesn't wait for the thread to be completly finished. Note that it can take some time before the thread is completly stopped.
	 *
	 * @see #run()
	 */
	public void stop() {
		// logger.info("<- " + systemid +"<"+requestIp+">"+ " Session Stop Command -> ");
		keepReceiving = false;
	}

	public Connection getConnection() {
		return connection;
	}

	public boolean isKeepReceiving() {
		return keepReceiving;
	}

	/**
	 * Implements the logic of receiving of the PDUs from client and passing them to PDU processor. First starts receiver, then in cycle receives PDUs and passes them to the proper PDU processor's
	 * methods. After the function <code>stop</code> is called (externally) stops the receiver, exits the PDU processor and closes the connection, so no extry tidy-up routines are necessary.
	 *
	 * @see #stop()
	 * @see PDUProcessor#clientRequest(Request)
	 * @see PDUProcessor#clientResponse(Response)
	 */
	@Override
	public void run() {
		PDU pdu = null;
		receiver.start();
		closeTime = System.currentTimeMillis() + getReceiveTimeout();
		try {
			while (keepReceiving) {
				logger.trace("UserSession going to receive a PDU");
				try {
					pdu = receiver.receive();
				} catch (TimeoutException | PDUException | IOException e) {
					logger.error(systemid + "<" + requestIp + ">" + "[" + mode + "-" + sessionId
							+ "] Connection Error: " + e.getMessage());
					break;
				} catch (Exception ex) {
					logger.error(systemid + "<" + requestIp + ">" + "[" + mode + "-" + sessionId + "]Connection Error: "
							+ ex.getMessage());
					break;
				}
				if (pdu != null) {
					if (pdu.isRequest()) {
						if ((pdu.getCommandId() == Data.BIND_TRANSMITTER || pdu.getCommandId() == Data.BIND_RECEIVER
								|| pdu.getCommandId() == Data.BIND_TRANSCEIVER)) {
							systemid = ((com.logica.smpp.pdu.BindRequest) pdu).getSystemId();
							transmitter.setSystemId(systemid);
							receiver.setSystemId(systemid);
						}
						pduProcessor.clientRequest((Request) pdu);
					} else if (pdu.isResponse()) {
						pduProcessor.clientResponse((Response) pdu);
					} else {
						logger.debug("UserSession not request nor response => not doing anything.");
					}
					closeTime = System.currentTimeMillis() + getReceiveTimeout();
				} else {
					if (System.currentTimeMillis() > closeTime) {
						logger.info(systemid + "<" + requestIp + ">" + "[" + mode + "-" + sessionId
								+ "]:Session stopping due to inactivity[" + receiveTimeout + "]");
						stop();
					}
				}
			}
		} finally {
			pduProcessor.stopThread();
		}
		receiver.stop();
		// debug.write("UserSession exiting PDUProcessor");
		pduProcessor.exit();
		try {
			// debug.write("UserSession closing connection");
			connection.close();
		} catch (IOException e) {
			// event.write(e, "closing UserSession's connection.");
		} catch (NullPointerException ne) {
			// event.write(ne, "closing UserSession's connection.");
		}
		// logger.debug("Session Closed: " + systemid +"<"+requestIp+">"+ " Receive Time Out:" + receiveTimeout);
	}

	public boolean sendResponse(PDU pdu) {
		boolean flag = true;
		try {
			transmitter.send(pdu);
		} catch (ValueNotSetException e) {
		} catch (IOException e) {
			flag = false;
			keepReceiving = false;
		} catch (Exception nt) {
			flag = false;
			keepReceiving = false;
		}
		return flag;
	}

	public boolean sendPDU(PDU pdu) {
		boolean flag = true;
		try {
			// debug.write("UserSession going to send pdu over transmitter");
			transmitter.send(pdu);
			// debug.write("UserSession pdu sent over transmitter");
		} catch (ValueNotSetException e) {
		} catch (IOException e) {
			logger.error(systemid + "<" + requestIp + ">" + "[" + mode + "-" + sessionId
					+ "] : <-------- Session Breaked-------->");
			flag = false;
			keepReceiving = false;
		} catch (Exception nt) {
			flag = false;
			logger.error(systemid + "<" + requestIp + ">" + "[" + mode + "-" + sessionId
					+ "] : <-------- Session Breaked-------->");
			keepReceiving = false;
		}
		return flag;
	}

	/**
	 * Returns the current setting of receiving timeout.
	 *
	 * @return the current timeout value
	 */
	private long getReceiveTimeout() {
		return receiveTimeout;
	}

	public void setReceiveTimeout(long receiveTimeout) {
		this.receiveTimeout = receiveTimeout;
	}

	public void reloadRouting() {
		logger.info(systemid + "<" + requestIp + ">" + "[" + mode + "-" + sessionId + "]: reloadRouting()");
		pduProcessor.reloadRouting();
	}

	public void setUserEntry(UserEntry entry) {
		logger.info(systemid + "<" + requestIp + ">" + "[" + mode + "-" + sessionId + "]: reloadUser()");
		pduProcessor.setUserEntry(entry);
	}

	public void reloadAdminRouting() {
		logger.info(systemid + "<" + requestIp + ">" + " reloadAdminRouting()");
		pduProcessor.removeAdminRouting();
	}

	public void reloadNetwork() {
		logger.info(systemid + "<" + requestIp + ">" + " reloadNetwork()");
		pduProcessor.reloadNetwork();
	}
	
	public void reloadNetworkBsfm() {
		logger.info(systemid + "<" + requestIp + ">" + " reloadNetworkBsfm()");
		pduProcessor.reloadNetworkBsfm();
	}

	public boolean goAhead() {
		if (receiver != null && receiver.getTermException() != null) {
			logger.error(systemid + "<" + requestIp + ">" + " <- Receiver Error -> " + receiver.getTermException());
			return false;
		}
		if (!GlobalVars.APPLICATION_STATUS) {
			return false;
		}
		if (System.currentTimeMillis() > closetime) {
			logger.debug(systemid + "<" + requestIp + ">" + "<- Closing Session -> ");
			return false;
		}
		return true;
	}

	public void increaseCloseTime() {
		closetime = System.currentTimeMillis() + receiveTimeout;
	}

	public void setMode(String getMode) {
		transmitter.setMode(getMode);
		receiver.setMode(getMode);
		mode = getMode;
	}

	public String getMode() {
		return mode;
	}

	public void setPduLog(boolean pduLog) {
		transmitter.setPduLog(pduLog);
		receiver.setPduLog(pduLog);
	}

	public String getSystemid() {
		return systemid;
	}

	public String getSystemType() {
		return systemType;
	}

	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}

	public void setSystemid(String systemid) {
		this.systemid = systemid;
		transmitter.setSystemId(systemid);
		receiver.setSystemId(systemid);
		receiver.RECEIVER_THREAD_NAME = systemid + "<" + requestIp + ">" + "_UserReceiver";
	}

	public void setSessionId(String SessionId) {
		transmitter.setSessionId(SessionId);
		receiver.setSessionId(SessionId);
		sessionId = SessionId;
	}

	public synchronized String getSessionId() {
		return sessionId;
	}

	public void unbind() {
		logger.info("<- " + systemid + "<" + requestIp + ">" + " Sending Unbind Request -> ");
		keepReceiving = false;
		try {
			transmitter.send(new Unbind());
		} catch (ValueNotSetException e) {
		} catch (IOException e) {
			logger.error(systemid + "<" + requestIp + ">" + "[" + mode + "-" + sessionId
					+ "] : <-------- Session Breaked-------->");
		} catch (Exception nt) {
			logger.error(systemid + "<" + requestIp + ">" + "[" + mode + "-" + sessionId
					+ "] : <-------- Session Breaked-------->");
		}
	}
}
