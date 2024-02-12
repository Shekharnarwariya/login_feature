package com.hti.smpp.common.database;

@SuppressWarnings("serial")
public class ParameterMismatchException extends RuntimeException {
    public ParameterMismatchException(String message) {
        super(message);
    }
}