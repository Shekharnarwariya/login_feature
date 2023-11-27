package com.hti.smpp.common.exception;

public class InvalidPasswordException extends RuntimeException {

	private static final long serialVersionUID = 1106671598754207461L;

	public InvalidPasswordException(String msg) {
		super(msg);
	}
}