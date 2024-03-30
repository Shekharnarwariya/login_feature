package com.hti.smpp.common.exception;

public class EntryNotFoundException extends Exception {
	public EntryNotFoundException() {
		super("EntryNotFoundException");
	}

	public EntryNotFoundException(String message) {
		super(message);
	}
}

