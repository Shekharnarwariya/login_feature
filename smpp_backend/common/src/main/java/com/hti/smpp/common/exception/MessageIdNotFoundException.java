package com.hti.smpp.common.exception;

public class MessageIdNotFoundException extends Exception {
    public MessageIdNotFoundException(String messageId) {
        super("MessageId " + messageId + " not found in the database.");
    }
}
