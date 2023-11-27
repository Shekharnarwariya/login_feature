package com.hti.smpp.common.exception;

public class InvalidOtpException extends RuntimeException {

	private static final long serialVersionUID = 1106671598754207461L;

	public InvalidOtpException(String msg) {
		super(msg);
	}
}