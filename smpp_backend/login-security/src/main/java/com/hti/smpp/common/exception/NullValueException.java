package com.hti.smpp.common.exception;

public class NullValueException extends RuntimeException {

	private static final long serialVersionUID = 1106671598754207461L;

	public NullValueException(String msg) {
		super(msg);
	}
}