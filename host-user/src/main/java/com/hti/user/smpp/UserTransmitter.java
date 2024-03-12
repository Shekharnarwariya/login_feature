package com.hti.user.smpp;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.smpp.Connection;
import com.logica.smpp.Data;
import com.logica.smpp.SmppObject;
import com.logica.smpp.pdu.PDU;
import com.logica.smpp.pdu.ValueNotSetException;

public class UserTransmitter extends SmppObject {
	private Logger pdulogger = LoggerFactory.getLogger("pduLogger");
	private Logger submitlogger = LoggerFactory.getLogger("submitLogger");
	private Logger dlrlogger = LoggerFactory.getLogger("dlrLogger");
	private String systemId;
	private String sessionId;
	private String mode;
	private boolean pduLog;
	/**
	 * The connection object. It is used for transmitting the PDUs. It's created outside of the <code>Transmitter</code> and passed to transmitter as a constructor parameter.
	 * 
	 * @see Connection
	 */
	private Connection connection = null;

	/**
	 * Default constructor made protected as it's not desirable to allow creation of <code>Transmitter</code> without providing <code>Connection</code>.
	 */
	protected UserTransmitter() {
	}

	/**
	 * Creates <code>Transmitter</code> which uses provided <code>Connection</code>. Typically the <code>connection</code> parameter will be an instance of <code>TCPIPConnection</code> class.
	 *
	 * @param connection
	 *            connection used for transmitting the PDUs
	 */
	public UserTransmitter(Connection c) {
		connection = c;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setPduLog(boolean pduLog) {
		this.pduLog = pduLog;
	}

	/**
	 * Assigns unique sequence number to PDU, if necessary, and sends its data over connection.
	 *
	 * @param pdu
	 *            the PDU to send
	 * @exception IOException
	 *                exception during communication
	 * @exception ValueNotSetException
	 *                optional param not set but requested
	 */
	public void send(PDU pdu) throws ValueNotSetException, IOException, Exception {
		pdu.assignSequenceNumber();
		try {
			connection.send(pdu.getData());
			if (pduLog) {
				if (pdu.getCommandId() == Data.SUBMIT_SM_RESP) {
					submitlogger.info(systemId + " " + sessionId + "-" + mode + " [" + connection.getLocalAddress()
							+ " : " + connection.getRemoteAddress() + "]: " + pdu.debugString());
				} else if (pdu.getCommandId() == Data.DELIVER_SM) {
					dlrlogger.info(systemId + " " + sessionId + "-" + mode + " [" + connection.getLocalAddress() + " : "
							+ connection.getRemoteAddress() + "]: " + pdu.debugString());
				} else {
					pdulogger.info(systemId + " " + sessionId + "-" + mode + " [" + connection.getLocalAddress() + " : "
							+ connection.getRemoteAddress() + "]: " + pdu.debugString());
				}
			}
		} catch (ValueNotSetException ve) {
			if (pduLog) {
				if (pdu.getCommandId() == Data.SUBMIT_SM_RESP) {
					submitlogger.error(systemId + " " + sessionId + "-" + mode + " [" + connection.getLocalAddress()
							+ " : " + connection.getRemoteAddress() + "]: " + "[ValueNotSetException]"
							+ pdu.debugString());
				} else if (pdu.getCommandId() == Data.DELIVER_SM) {
					dlrlogger.error(systemId + " " + sessionId + "-" + mode + " [" + connection.getLocalAddress()
							+ " : " + connection.getRemoteAddress() + "]: " + "[ValueNotSetException]"
							+ pdu.debugString());
				} else {
					pdulogger.error(systemId + " " + sessionId + "-" + mode + " [" + connection.getLocalAddress()
							+ " : " + connection.getRemoteAddress() + "]: " + "[ValueNotSetException]"
							+ pdu.debugString());
				}
			}
			throw new ValueNotSetException();
		} catch (IOException ioe) {
			if (pduLog) {
				if (pdu.getCommandId() == Data.SUBMIT_SM_RESP) {
					submitlogger.error(systemId + " " + sessionId + "-" + mode + " [" + connection.getLocalAddress()
							+ " : " + connection.getRemoteAddress() + "]: " + "[IOError]" + pdu.debugString());
				} else if (pdu.getCommandId() == Data.DELIVER_SM) {
					dlrlogger.error(systemId + " " + sessionId + "-" + mode + " [" + connection.getLocalAddress()
							+ " : " + connection.getRemoteAddress() + "]: " + "[IOError]" + pdu.debugString());
				} else {
					pdulogger.error(systemId + " " + sessionId + "-" + mode + " [" + connection.getLocalAddress()
							+ " : " + connection.getRemoteAddress() + "]: " + "[IOError]" + pdu.debugString());
				}
			}
			throw new IOException(ioe.getCause());
		} catch (Exception e) {
			if (pduLog) {
				if (pdu.getCommandId() == Data.SUBMIT_SM_RESP) {
					submitlogger.error(systemId + " " + sessionId + "-" + mode + " [" + connection.getLocalAddress()
							+ " : " + connection.getRemoteAddress() + "]: " + "[Error: " + e + "]" + pdu.debugString());
				} else if (pdu.getCommandId() == Data.DELIVER_SM) {
					dlrlogger.error(systemId + " " + sessionId + "-" + mode + " [" + connection.getLocalAddress()
							+ " : " + connection.getRemoteAddress() + "]: " + "[Error: " + e + "]" + pdu.debugString());
				} else {
					pdulogger.error(systemId + " " + sessionId + "-" + mode + " [" + connection.getLocalAddress()
							+ " : " + connection.getRemoteAddress() + "]: " + "[Error: " + e + "]" + pdu.debugString());
				}
			}
			throw new Exception(e.getCause());
		}
	}
	/*
	 * public void setConnection(Connection getconnection){ connection=getconnection; }
	 */
}
