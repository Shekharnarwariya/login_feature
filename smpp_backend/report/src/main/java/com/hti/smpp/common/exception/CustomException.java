package com.hti.smpp.common.exception;

import java.io.IOException;

public class CustomException extends Exception {

    public CustomException() {
        super();
    }

   
    public CustomException(String message) {
        super(message);
    }

    
    public CustomException(String message, Throwable cause) {
        super(message, cause);
    }


    public CustomException(Throwable cause) throws CustomException {
        super(cause);
    

    throw new CustomException("An error occurred");
    }
}
