package com.hti.thread;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.DeliverSMExt;
import com.hti.util.GlobalQueue;

public class QueueListener implements MessageListener {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private int Queue_Number = 0;
	private int counter;
	private int total_counter;

	public QueueListener(int Queue_Number) {
		this.Queue_Number = Queue_Number;
		logger.info(" DLR Receiving Queue[" + Queue_Number + "] Listener Started ");
	}

	@Override
	public void onMessage(Message message) {
		try {
			if (message instanceof ObjectMessage) {
				Object object = null;
				try {
					object = ((ObjectMessage) message).getObject();
				} catch (Exception e) {
					logger.error("onMessage(" + Queue_Number + ")", e.fillInStackTrace());
				}
				GlobalQueue.DeliverProcessQueue.enqueue((DeliverSMExt) object);
				total_counter++;
				if (++counter == 10000) {
					counter = 0;
					logger.info(" DLR Received Counter[" + Queue_Number + "]: " + total_counter);
				}
			}
		} catch (Exception ex) {
			logger.error("Queue:" + Queue_Number, ex.fillInStackTrace());
		}
	}
}
