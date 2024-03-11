package com.hti.thread;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.RoutePDU;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;

public class ProcessPDU implements Runnable {
	private boolean stop;
	private Logger logger = LoggerFactory.getLogger("omqLogger");
	private Session session;
	MessageProducer[] producer;
	Connection connection;
	// private boolean connect = false;
	private int total_Queue = 6;
	private int processed_counter;
	public static long submittedCounter = 0;

	public ProcessPDU() {
		logger.info("ProcessPDU Thread Starting");
		producer = new MessageProducer[total_Queue];
	}

	private boolean initConnection() {
		logger.info("<------  Trying to Connect OMQ Server to Submit PDUs ------->");
		try {
			connection = new com.sun.messaging.ConnectionFactory().createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			for (int i = 0; i < total_Queue; i++) {
				producer[i] = session.createProducer(session.createQueue("pduQueue_" + (i + 1)));
			}
			logger.info("<------  OMQ Server Connected to Submit PDUs ------->");
		} catch (JMSException e) {
			logger.error("<------  OMQ PDU Connection Error[" + e + "] ------->");
			return false;
		}
		return true;
	}

	private void closeConnection() {
		try {
			connection.close();
			logger.info("<------  OMQ Server PDU Connection Closed ------->");
		} catch (JMSException e) {
			logger.error("<---- OMQ Connection Close Error[" + e + "] ---->");
		}
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				if (!GlobalVars.OMQ_PDU_STATUS) {
					GlobalVars.OMQ_PDU_STATUS = initConnection();
				}
				boolean discard = false;
				if (GlobalVars.OMQ_PDU_STATUS) {
					if (!GlobalQueue.interProcessRequest.isEmpty()) {
						logger.debug("InterProcessRequest Queue:-> " + GlobalQueue.interProcessRequest.size());
						int submit_count = 0;
						int Queue_Number = 0;
						RoutePDU route;
						while (!GlobalQueue.interProcessRequest.isEmpty()) {
							route = (RoutePDU) GlobalQueue.interProcessRequest.dequeue();
							if (process(route, Queue_Number)) {
								processed_counter++;
								submittedCounter++;
								if (Queue_Number == (total_Queue - 1)) {
									Queue_Number = 0;
								} else {
									Queue_Number++;
								}
							} else {
								route = null;
								discard = true;
								GlobalVars.OMQ_PDU_STATUS = false;
								break;
							}
							if (++submit_count > 1000) {
								logger.debug("OMQ Submitted Counter:-> " + submittedCounter);
								break;
							}
						}
					}
				} else {
					discard = true;
				}
				if (discard) {
					RoutePDU route;
					int discard_count = 0;
					while (!GlobalQueue.interProcessRequest.isEmpty()) {
						route = (RoutePDU) GlobalQueue.interProcessRequest.dequeue();
						route = null;
						if (++discard_count > 1000) {
							break;
						}
					}
					logger.info("OMQ Discard Counter:=====> " + discard_count);
				} else {
					if (processed_counter > 10000) {
						processed_counter = 0;
						logger.info("OMQ Submitted :" + submittedCounter + " Queued: "
								+ GlobalQueue.interProcessRequest.size());
					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		closeConnection();
		logger.info("ProcessPDU Thread Stopped.Submitted:" + submittedCounter + " Queued: "
				+ GlobalQueue.interProcessRequest.size());
	}

	private boolean process(RoutePDU pdu, int Queue_number) {
		ObjectMessage objectMessage;
		try {
			objectMessage = session.createObjectMessage();
			objectMessage.setObject(pdu);
			producer[Queue_number].send(objectMessage);
			logger.info(pdu.getHtiMsgId() + " < pdu submitted > " + pdu.getUsername());
		} catch (Exception e) {
			logger.error("<------  OMQ PDU Submit Error(" + Queue_number + ")[" + e + "] ------->");
			return false;
		}
		objectMessage = null;
		return true;
	}

	public static String getStatistics() {
		return "OMQProcessQueue: " + GlobalQueue.interProcessRequest.size() + " Submitted: " + submittedCounter;
	}

	public void stop() {
		logger.info("ProcessPDU Thread Stopping");
		stop = true;
	}
}
