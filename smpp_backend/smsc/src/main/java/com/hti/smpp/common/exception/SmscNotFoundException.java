package com.hti.smpp.common.exception;

public class SmscNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1106671598754207461L;

	public SmscNotFoundException(String msg) {
		super(msg);
	}
}