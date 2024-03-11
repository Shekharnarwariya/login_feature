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
package com.logica.smpp.pdu;

import com.logica.smpp.Data;
import com.logica.smpp.util.ByteBuffer;
import com.logica.smpp.util.NotEnoughDataInByteBufferException;
import com.logica.smpp.util.TerminatingZeroNotFoundException;

/**
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version 1.0, 11 Jun 2001
 */
public class AlertNotification extends Request {
	private Address sourceAddr = new Address();
	private Address esmeAddr = new Address();

	public AlertNotification() {
		super(Data.ALERT_NOTIFICATION);
	}

	protected Response createResponse() {
		return null;
	}

	public boolean canResponse() {
		return false;
	}

	public void setBody(ByteBuffer buffer)
			throws NotEnoughDataInByteBufferException, TerminatingZeroNotFoundException, PDUException {
	}

	public ByteBuffer getBody() {
		return null;
	}

	public void setSourceAddr(byte ton, byte npi, String address) throws WrongLengthOfStringException {
		setSourceAddr(new Address(ton, npi, address));
	}

	public void setSourceAddr(Address value) {
		sourceAddr = value;
	}

	public Address getSourceAddr() {
		return sourceAddr;
	}

	public Address getEsmeAddr() {
		return esmeAddr;
	}

	public void setEsmeAddr(byte ton, byte npi, String address) throws WrongLengthOfStringException {
		setEsmeAddr(new Address(ton, npi, address));
	}

	public void setEsmeAddr(Address value) {
		esmeAddr = value;
	}

	public String debugString() {
		String dbgs = "(alertnotification: ";
		dbgs += super.debugString();
		dbgs += ") ";
		return dbgs;
	}
}