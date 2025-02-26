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
package com.hti.hlr.smpp;

import java.io.IOException;

import javax.sound.midi.Receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.smpp.Connection;
import com.logica.smpp.SmppObject;
import com.logica.smpp.pdu.PDU;
import com.logica.smpp.pdu.ValueNotSetException;

/**
 * Class <code>Transmitter</code> transmits PDUs over connection. It is used by <code>Session</code>.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version 1.1, 28 Sep 2001
 * @see Connection
 * @see Receiver
 * @see Session
 */
/*
 * 28-09-01 ticp@logica.com traces added
 */
public class SmscTransmitter extends SmppObject {
	private Logger pdulogger = LoggerFactory.getLogger("hlrPduLogger");
	private String smsc;
	/**
	 * The connection object. It is used for transmitting the PDUs. It's created outside of the <code>Transmitter</code> and passed to transmitter as a constructor parameter.
	 * 
	 * @see Connection
	 */
	private Connection connection = null;

	/**
	 * Default constructor made protected as it's not desirable to allow creation of <code>Transmitter</code> without providing <code>Connection</code>.
	 */
	protected SmscTransmitter() {
	}

	/**
	 * Creates <code>Transmitter</code> which uses provided <code>Connection</code>. Typically the <code>connection</code> parameter will be an instance of <code>TCPIPConnection</code> class.
	 *
	 * @param connection
	 *            connection used for transmitting the PDUs
	 */
	public SmscTransmitter(Connection c, String smsc) {
		this.smsc = smsc;
		connection = c;
	}

	/**
	 * Assigns unique sequence number to PDU, if necessary, and sends its data over connection.
	 *
	 * @param pdu
	 *            the PDU to send
	 *
	 * @exception IOException
	 *                exception during communication
	 * @exception ValueNotSetException
	 *                optional param not set but requested
	 */
	public void send(PDU pdu) throws ValueNotSetException, IOException {
		pdu.assignSequenceNumber();
		connection.send(pdu.getData());
		pdulogger.info(smsc + ":" + pdu.debugString());
	}
	/*
	 * public void setConnection(Connection getconnection){ connection=getconnection; }
	 */
}
