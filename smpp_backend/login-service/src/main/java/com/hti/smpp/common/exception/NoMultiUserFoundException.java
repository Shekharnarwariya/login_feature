package com.hti.smpp.common.exception;

public class NoMultiUserFoundException extends RuntimeException {
    public NoMultiUserFoundException(String message) {
        super(message);
    }
}