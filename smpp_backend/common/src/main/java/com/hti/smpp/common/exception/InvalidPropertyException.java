package com.hti.smpp.common.exception;

public class InvalidPropertyException extends RuntimeException {

	private static final long serialVersionUID = 1106671598754207461L;

	public InvalidPropertyException(String msg) {
		super(msg);
	}
}