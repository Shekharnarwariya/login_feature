package com.hti.smpp.common.exception;

public class UnauthorizedException extends RuntimeException {

	private static final long serialVersionUID = 1106671598754207461L;

	public UnauthorizedException(String message) {
		super(message);
	}
}
