package com.hti.smpp.common.exception;

public class DataAccessError extends RuntimeException {

	private static final long serialVersionUID = 1106671598754207461L;

	public DataAccessError(String message) {
		super(message);
	}
}
