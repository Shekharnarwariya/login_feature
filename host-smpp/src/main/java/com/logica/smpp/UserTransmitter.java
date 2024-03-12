package com.logica.smpp;

import java.io.IOException;

import com.logica.smpp.pdu.PDU;
import com.logica.smpp.pdu.ValueNotSetException;

public class UserTransmitter extends SmppObject {
	private String systemId;
	/**
	 * The connection object. It is used for transmitting the PDUs. It's created outside of the <code>Transmitter</code> and passed to transmitter as
	 * a constructor parameter.
	 * 
	 * @see Connection
	 */
	private Connection connection = null;

	/**
	 * Default constructor made protected as it's not desirable to allow creation of <code>Transmitter</code> without providing
	 * <code>Connection</code>.
	 */
	protected UserTransmitter() {
	}

	/**
	 * Creates <code>Transmitter</code> which uses provided <code>Connection</code>. Typically the <code>connection</code> parameter will be an
	 * instance of <code>TCPIPConnection</code> class.
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
	public void send(PDU pdu) throws ValueNotSetException, IOException {
		// debug.enter(DCOM,this,"send");
		pdu.assignSequenceNumber();
		try {
			// debug.write(DCOM,"going to send pdu's data over connection");
			connection.send(pdu.getData());
			/*
			 * GlobalAppVars.pduLogQueue.enqueue(systemId + ":" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + ": Response<" +
			 * connection.getIpAddress() + ">: (seq: " + pdu.getSequenceNumber() + ")" + pdu.debugString());
			 */
			// debug.write(DCOM,"successfully sent pdu's data over connection");
		} finally {
			// debug.exit(DCOM,this);
		}
	}
	/*
	 * public void setConnection(Connection getconnection){ connection=getconnection; }
	 */
}
