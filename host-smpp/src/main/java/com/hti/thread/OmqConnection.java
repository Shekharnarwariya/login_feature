package com.hti.thread;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OmqConnection implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private QueueConnection connection = null;
	private QueueSession session = null;
	private boolean stop;
	private int numberOfQueue = 6;
	private OMQExceptionListener exceptionListener;
	private boolean OMQ_PDU_STATUS;

	public OmqConnection() {
		logger.info("OmqConnection Thread Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			if (!OMQ_PDU_STATUS) {
				OMQ_PDU_STATUS = initConnection();
			} else {
				OMQ_PDU_STATUS = isConnected();
			}
			try {
				Thread.sleep(3 * 1000);
			} catch (InterruptedException e) {
			}
		}
		closeConnection();
		logger.info("OmqConnection Thread Stopped");
	}

	private boolean isConnected() {
		if (connection != null && exceptionListener != null) {
			if (exceptionListener.getException() != null) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	private boolean initConnection() {
		logger.info("<--- Trying to Connect OMQ Server To Receive PDUs -->");
		// creating a connection
		try {
			connection = new com.sun.messaging.QueueConnectionFactory().createQueueConnection();
			exceptionListener = new OMQExceptionListener();
			connection.setExceptionListener(exceptionListener);
			connection.start();
			session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			for (int i = 1; i <= numberOfQueue; i++) {
				QueueReceiver receiver = session.createReceiver(session.createQueue("pduQueue_" + (i)));
				receiver.setMessageListener(new QueueListener(i));
				logger.info("Creating Message Receiving Queue: " + i);
			}
		} catch (Exception ex) {
			logger.error("<-- OMQ PDU Connection Error[" + ex + "] --> ");
			return false;
		}
		logger.info("<--- OMQ Server Connected To Receive PDUs -->");
		return true;
	}

	private void closeConnection() {
		logger.error("<--- Closing OMQ PDU Session -->");
		try {
			session.close();
		} catch (JMSException e) {
			logger.error("<--- OMQ PDU Session Close Error " + e + " -->");
		}
		try {
			connection.close();
		} catch (JMSException e) {
			logger.error("<--- OMQ PDU Connection Close Error " + e + " -->");
		}
	}

	public void stop() {
		logger.info("OmqConnection Thread Stopping");
		stop = true;
	}
}
