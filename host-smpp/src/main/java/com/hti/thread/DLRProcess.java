package com.hti.thread;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.DeliverSMExt;
import com.hti.util.Constants;
import com.hti.util.GlobalQueue;

public class DLRProcess implements Runnable {
	private boolean stop;
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private Session session;
	MessageProducer[] producer;
	Connection connection;
	// private boolean connect = false;
	private int total_Queue = 3;
	private DeliverTempInsert deliverTempInsert;

	public DLRProcess() {
		logger.info("DLRProcess Thread Starting");
		producer = new MessageProducer[total_Queue];
		deliverTempInsert = new DeliverTempInsert();
		new Thread(deliverTempInsert, "DeliverTempInsert").start();
	}

	private boolean initConnection() {
		try {
			connection = new com.sun.messaging.ConnectionFactory().createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			for (int i = 0; i < total_Queue; i++) {
				producer[i] = session.createProducer(session.createQueue("dlrQueue_" + (i + 1)));
			}
			connection.start();
			logger.info("<------  OMQ Server Connected to submit DLRs ------->");
		} catch (JMSException e) {
			logger.error("<------  OMQ DLR Connection Error[" + e + "] ------->");
			return false;
		}
		return true;
	}

	private void closeConnection() {
		try {
			connection.close();
			logger.info("<------  OMQ Server Connection Closed ------->");
		} catch (JMSException e) {
			logger.error("<---- OMQ Connection Close Error[" + e + "] ---->");
		}
	}

	@Override
	public void run() {
		while (!stop) {
			if (!Constants.OMQ_DELIVER_STATUS) {
				Constants.OMQ_DELIVER_STATUS = initConnection();
			}
			if (GlobalQueue.DeliverProcessQueue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			} else {
				boolean discard = false;
				if (Constants.OMQ_DELIVER_STATUS && Constants.USER_HOST_STATUS) {
					int Queue_Number = 0;
					int submit_count = 0;
					DeliverSMExt deliver_sm;
					while (!GlobalQueue.DeliverProcessQueue.isEmpty()) {
						deliver_sm = (DeliverSMExt) GlobalQueue.DeliverProcessQueue.dequeue();
						if (process(deliver_sm, Queue_Number)) {
							if (++submit_count > 1000) {
								break;
							}
							if (Queue_Number == 2) {
								Queue_Number = 0;
							} else {
								Queue_Number++;
							}
						} else {
							discard = true;
							Constants.OMQ_DELIVER_STATUS = false;
							GlobalQueue.DeliverSMTempQueue.enqueue(deliver_sm); // put to temp dlr table
							break;
						}
					}
				} else {
					discard = true;
				}
				if (discard) {
					int discard_count = 0;
					while (!GlobalQueue.DeliverProcessQueue.isEmpty()) {
						GlobalQueue.DeliverSMTempQueue
								.enqueue((DeliverSMExt) GlobalQueue.DeliverProcessQueue.dequeue());
						if (++discard_count > 1000) {
							break;
						}
					}
				}
			}
		}
		closeConnection();
		if (!GlobalQueue.DeliverProcessQueue.isEmpty()) {
			logger.info("Copying DLRProcess Queue to temp Queue :-> " + GlobalQueue.DeliverProcessQueue.size());
			while (!GlobalQueue.DeliverProcessQueue.isEmpty()) {
				GlobalQueue.DeliverSMTempQueue.enqueue((DeliverSMExt) GlobalQueue.DeliverProcessQueue.dequeue());
			}
		}
		logger.info("DLRProcess Thread Stopped.Queue: " + GlobalQueue.DeliverProcessQueue.size());
		deliverTempInsert.stop();
	}

	private boolean process(DeliverSMExt pdu, int i) {
		ObjectMessage objectMessage;
		try {
			objectMessage = session.createObjectMessage();
			objectMessage.setObject(pdu);
			producer[i].send(objectMessage);
			// logger.infopdu.getMsgid() + " < Deliver Request Sent > " + pdu.getUsername());
		} catch (Exception e) {
			logger.error("<------  OMQ Deliver Submit Error(" + i + ")[" + e + "] ------->");
			return false;
		}
		return true;
	}

	public void stop() {
		logger.info("DLRProcess Thread Stopping.Queue: " + GlobalQueue.DeliverProcessQueue.size());
		stop = true;
	}
}
