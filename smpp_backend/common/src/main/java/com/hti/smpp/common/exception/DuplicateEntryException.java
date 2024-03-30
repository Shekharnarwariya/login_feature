package com.hti.smpp.common.exception;

public class DuplicateEntryException extends Exception {
	public DuplicateEntryException() {
	}

	public DuplicateEntryException(String message) {
		super(message);
	}

	public DuplicateEntryException(Throwable cause) {
		super(cause);
	}
}
