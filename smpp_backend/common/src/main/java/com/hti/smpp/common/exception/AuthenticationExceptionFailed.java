package com.hti.smpp.common.exception;

public class AuthenticationExceptionFailed extends RuntimeException {
	private static final long serialVersionUID = 1106671598754207461L;

	public AuthenticationExceptionFailed(String message) {
		super(message);
	}
}
