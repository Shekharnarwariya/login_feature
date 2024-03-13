package com.hti.smpp.common.httpclient;

public class InvalidFormatException extends Exception {
	public InvalidFormatException() {
		super();
	}

	public InvalidFormatException(String message) {
		super(message);
	}

	public InvalidFormatException(String message, Exception e) {
		super(message, e);
	}
}
