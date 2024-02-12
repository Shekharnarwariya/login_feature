package com.hti.smpp.common.database;

public class DBException extends Exception {
	public DBException() {
		System.out.println("****************DBException1****************");
	}

	public DBException(String message) {
		System.out.println("****************DBException2****************");
	}
				
	public DBException(String message, Exception e) {
		// super(message);
		System.out.println(message + " -> " + e);
	}
}