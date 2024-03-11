package com.hti.thread;

import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Iterator;
import com.hti.objects.ResponseObj;
import com.hti.util.GlobalQueue;

public class DelayedDlrProcess implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private Map<ResponseObj, Long> waitingQueue = new HashMap<ResponseObj, Long>();
	private boolean stop;

	public DelayedDlrProcess() {
		logger.info(" DelayedDlrProcess Starting ");
	}

	@Override
	public void run() {
		logger.info(" DelayedDlrProcess Started ");
		while (!stop) {
			try {
				if (!waitingQueue.isEmpty()) {
					// logger.info("DelayedDlr WaitingQueue: " + waitingQueue.size());
					Iterator<Map.Entry<ResponseObj, Long>> itr = waitingQueue.entrySet().iterator();
					while (itr.hasNext()) {
						Map.Entry<ResponseObj, Long> entry = itr.next();
						if (System.currentTimeMillis() > entry.getValue()) {
							entry.getKey().setTime(new java.util.Date());
							GlobalQueue.processResponseQueue.enqueue(entry.getKey());
							logger.info(entry.getKey().getMsgid() + " DelayedDlr Added To ProcessQueue.");
							itr.remove();
						}
					}
				}
				if (GlobalQueue.DelayedDlrQueue.isEmpty()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				} else {
					ResponseObj resp_obj = null;
					int counter = 0;
					while (!GlobalQueue.DelayedDlrQueue.isEmpty()) {
						// logger.info("DelayedDlrQueue: " + GlobalQueue.DelayedDlrQueue.size());
						resp_obj = (ResponseObj) GlobalQueue.DelayedDlrQueue.dequeue();
						waitingQueue.put(resp_obj, System.currentTimeMillis() + (resp_obj.getDelayDlr() * 1000));
						if (++counter > 1000) {
							break;
						}
					}
				}
			} catch (Exception ex) {
				logger.error("", ex);
			}
		}
		if (!waitingQueue.isEmpty()) {
			logger.info("Clear DelayedDlr WaitingQueue: " + waitingQueue.size());
			Iterator<Map.Entry<ResponseObj, Long>> itr = waitingQueue.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<ResponseObj, Long> entry = itr.next();
				entry.getKey().setTime(new java.util.Date());
				GlobalQueue.processResponseQueue.enqueue(entry.getKey());
				logger.info(entry.getKey().getMsgid() + " DelayedDlr Added To ProcessQueue.");
				itr.remove();
			}
		}
		logger.info(" DelayedDlrProcess Stopped ");
	}

	public void stop() {
		stop = true;
		logger.info(" DelayedDlrProcess Stopping ");
	}
}
