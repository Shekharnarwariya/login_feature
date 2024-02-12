package com.hti.smpp.common.database;

@SuppressWarnings("serial")
public class DataNotFoundException extends RuntimeException {
    public DataNotFoundException(String message) {
        super(message);
    }
}


