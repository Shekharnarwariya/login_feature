package com.hti.smpp.common.util;

import com.logica.smpp.pdu.ValueNotSetException;
import com.logica.smpp.pdu.tlv.TLV;
import com.logica.smpp.pdu.tlv.TLVException;
import com.logica.smpp.util.ByteBuffer;
import com.logica.smpp.util.NotEnoughDataInByteBufferException;

public class TLVOctets extends TLV {
	private ByteBuffer value = null;

	public TLVOctets() {
		super();
	}

	public TLVOctets(short p_tag) {
		super(p_tag);
	}

	public TLVOctets(short p_tag, int min, int max) {
		super(p_tag, min, max);
	}

	public TLVOctets(short p_tag, ByteBuffer p_value) throws TLVException {
		super(p_tag);
		setValueData(p_value);
	}

	public TLVOctets(short p_tag, int min, int max, ByteBuffer p_value) throws TLVException {
		super(p_tag, min, max);
		setValueData(p_value);
	}

	protected void setValueData(ByteBuffer buffer) throws TLVException {
		checkLength(buffer);
		if (buffer != null) {
			try {
				value = buffer.removeBuffer(buffer.length());
			} catch (NotEnoughDataInByteBufferException e) {
				throw new Error("Removing buf.length() data from ByteBuffer buf "
						+ "reported too little data in buf, which shouldn't happen.");
			}
		} else {
			value = null;
		}
		markValueSet();
	}

	protected ByteBuffer getValueData() throws ValueNotSetException {
		ByteBuffer valueBuf = new ByteBuffer();
		valueBuf.appendBuffer(getValue());
		return valueBuf;
	}

	public void setValue(ByteBuffer p_value) {
		if (p_value != null) {
			try {
				value = p_value.removeBuffer(p_value.length());
			} catch (NotEnoughDataInByteBufferException e) {
				throw new Error("Removing buf.length() data from ByteBuffer buf "
						+ "reported too little data in buf, which shouldn't happen.");
			}
		} else {
			value = null;
		}
		markValueSet();
	}

	public ByteBuffer getValue() throws ValueNotSetException {
		if (hasValue()) {
			return value;
		} else {
			throw new ValueNotSetException();
		}
	}

	/**
	 * Returns the binary TLV created from tag, length and binary data value carried
	 * by this TLV.
	 */
	public ByteBuffer getData() throws ValueNotSetException {
		if (hasValue()) {
			ByteBuffer tlvBuf = new ByteBuffer();
			tlvBuf.appendShort(getTag());
			tlvBuf.appendShort(encodeUnsigned(getLength()));
			tlvBuf.appendBuffer(getValueData());
			return tlvBuf;
		} else {
			return null;
		}
	}

	protected static short encodeUnsigned(int positive) {
		if (positive < 32768) {
			// paolo@bulksms.com 2005-09-22: no, this isn't right! Casting the
			// short to a byte here overflows the byte, converts it back to a
			// short, and produces a bogus result. This was the cause of a bug
			// whereby invalid TLVs were produced: try creating a TLV longer than
			// 127 octets and see what happens...
			// return (byte)positive;
			return (short) positive;
		} else {
			return (short) (-(65536 - positive));
		}
	}

	public String debugString() {
		String dbgs = "(oct: ";
		dbgs += super.debugString();
		dbgs += value == null ? "" : value.getHexDump();
		dbgs += ") ";
		return dbgs;
	}
}
