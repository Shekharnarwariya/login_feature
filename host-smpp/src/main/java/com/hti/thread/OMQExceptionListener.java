package com.hti.thread;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OMQExceptionListener implements ExceptionListener {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private JMSException exception;

	public OMQExceptionListener() {
		logger.info("OMQExceptionListener Started");
	}

	@Override
	public void onException(JMSException exception) {
		this.exception = exception;
		logger.info(exception + " Received");
		logger.error("onException()", exception.fillInStackTrace());
	}

	public Logger getLogger() {
		return logger;
	}

	public JMSException getException() {
		return exception;
	}
}
