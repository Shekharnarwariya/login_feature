package com.hti.thread;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.util.GlobalQueue;

public class QueueListener implements MessageListener {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private int Queue_Number = 0;
	private int counter;
	private int total_counter;

	public QueueListener(int Queue_Number) {
		this.Queue_Number = Queue_Number;
		logger.info(" PDU Receiving Queue[" + Queue_Number + "] Listener Started ");
	}

	@Override
	public void onMessage(Message message) {
		GlobalQueue.omqReceivedQueue.enqueue(message);
		total_counter++;
		if (++counter == 10000) {
			counter = 0;
			logger.info(" PDU Received Counter[" + Queue_Number + "]: " + total_counter);
		}
	}
}
