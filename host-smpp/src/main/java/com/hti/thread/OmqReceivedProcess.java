package com.hti.thread;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.RoutePDU;
import com.hti.objects.SmscInObj;
import com.hti.util.Constants;
import com.hti.util.GlobalQueue;

public class OmqReceivedProcess implements Runnable {
	private boolean stop;
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	//private Logger tracklogger = LoggerFactory.getLogger("trackLogger");
	private Object object = null;
	public static Set<String> processedCQueue = Collections.synchronizedSet(new HashSet<String>());
	private RoutePDU route = null;
	private int counter;
	private int total_counter;

	public OmqReceivedProcess() {
		logger.info("OmqReceivedProcess Thread Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			if (GlobalQueue.omqReceivedQueue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			} else {
				int local_counter = 0;
				while (!GlobalQueue.omqReceivedQueue.isEmpty()) {
					onMessage((Message) GlobalQueue.omqReceivedQueue.dequeue());
					total_counter++;
					if (++counter == 25000) {
						counter = 0;
						logger.info("OMQ ReceivedQueue:" + GlobalQueue.omqReceivedQueue.size() + " Processed Counter: "
								+ total_counter);
					}
					if (++local_counter > 1000) {
						break;
					}
				}
			}
		}
		logger.info("OmqReceivedProcess Thread Stopped.Queue: " + GlobalQueue.omqReceivedQueue.size());
	}

	public void onMessage(Message message) {
		if (message instanceof ObjectMessage) {
			try {
				object = ((ObjectMessage) message).getObject();
			} catch (Exception e) {
				logger.error("onMessage()", e.fillInStackTrace());
			}
			route = (RoutePDU) object;
			if (processedCQueue.contains(route.getHtiMsgId())) {
				//tracklogger.debug(route.getHtiMsgId() + " < Processed pdu received > " + route.getUsername());
				processedCQueue.remove(route.getHtiMsgId());
			} else {
				//tracklogger.debug(route.getHtiMsgId() + " received: " + route.getUsername() + " [" + route.getSmsc() + "]");
				if (Constants.PROCESSING_STATUS) {
					GlobalQueue.interProcessRequest.enqueue(route);
				} else {
					GlobalQueue.smsc_in_update_Queue.enqueue(new SmscInObj(route.getHtiMsgId(), "Q", route.getSmsc(),
							route.getGroupId(), route.getUsername()));
					//tracklogger.debug(route.getHtiMsgId() + " downQueueOnReceive: " + route.getUsername() + " ["+ route.getSmsc() + "]");
				}
			}
		}
		object = null;
		route = null;
	}

	public void stop() {
		logger.info("OmqReceivedProcess Thread Stopping");
		stop = true;
	}
}
