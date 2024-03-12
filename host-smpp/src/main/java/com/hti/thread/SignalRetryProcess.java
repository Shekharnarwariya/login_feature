package com.hti.thread;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.SignalRetryObj;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;

public class SignalRetryProcess implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private Set<SignalRetryObj> waitingQueue = new HashSet<SignalRetryObj>();
	private boolean stop;

	public SignalRetryProcess() {
		logger.info("SignalRetryProcess Thread Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			if (!waitingQueue.isEmpty()) {
				Iterator<SignalRetryObj> itr = waitingQueue.iterator();
				SignalRetryObj retryObj = null;
				while (itr.hasNext()) {
					retryObj = itr.next();
					if (retryObj.getProcessTime() <= System.currentTimeMillis()) {
						if (GlobalCache.ResendPDUCache.containsKey(retryObj.getMessageId())) {
							GlobalQueue.interProcessManage
									.enqueue(GlobalCache.ResendPDUCache.get(retryObj.getMessageId()));
							logger.debug(retryObj.getMessageId() + " Released from Retry Waiting Queue");
						} else {
							logger.debug(retryObj.getMessageId() + " Retry PDU not Found ");
						}
						itr.remove();
					}
				}
			}
			if (GlobalQueue.signalRetryQueue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			} else {
				SignalRetryObj retry_obj = null;
				while (!GlobalQueue.signalRetryQueue.isEmpty()) {
					retry_obj = (SignalRetryObj) GlobalQueue.signalRetryQueue.dequeue();
					if (retry_obj.getProcessTime() <= System.currentTimeMillis()) {
						if (GlobalCache.ResendPDUCache.containsKey(retry_obj.getMessageId())) {
							GlobalQueue.interProcessManage
									.enqueue(GlobalCache.ResendPDUCache.get(retry_obj.getMessageId()));
						} else {
							logger.debug(retry_obj.getMessageId() + " Retry PDU not Found ");
						}
					} else {
						waitingQueue.add(retry_obj);
						logger.debug(retry_obj.getMessageId() + " Added to Retry Waiting Queue");
					}
				}
			}
		}
		logger.info("SignalRetryProcess Thread Stopped.Queue: " + GlobalQueue.signalRetryQueue.size());
	}

	public void stop() {
		stop = true;
		logger.info("SignalRetryProcess Thread Stopping.Queue: " + GlobalQueue.signalRetryQueue.size());
	}
}
