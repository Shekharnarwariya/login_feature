package com.hti.smpp.common.exception;

public class NotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1106671598754207461L;

	public NotFoundException(String msg) {
		super(msg);
	}
}