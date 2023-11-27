package com.hti.smpp.common.exception;

public class InternalServerException extends RuntimeException {

	private static final long serialVersionUID = 1106671598754207461L;

	public InternalServerException(String message) {
		super(message);
	}
}
