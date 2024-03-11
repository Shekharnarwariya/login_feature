package com.hti.hlr;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.SmscInObj;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;
import com.logica.smpp.util.Queue;

public class HlrRequestHandler implements Runnable {
	private Logger logger = LoggerFactory.getLogger("hlrLogger");
	// ----------- local used ------------------------
	private String systemId;
	private String password;
	private Queue hlrQueue;
	private boolean stop;
	private LookupSubmission[] lookupSubmission;
	private ResponseHandler responseHandler;
	private Map<String, Long> enqueueTime = new HashMap<String, Long>();
	private Queue[] lookupQueue;
	private String threadId = null;
	private int threadCount = 5;
	private int queueNumber = 0;

	public HlrRequestHandler(String systemId, String password) {
		this.systemId = systemId;
		this.password = password;
		this.threadId = new SimpleDateFormat("ddHHmmssSSS").format(new Date());
	}

	public void startHandler() {
		logger.info(systemId + "_" + threadId + " HlrRequestHandler Starting.Queue: " + hlrQueue.size());
		if (!GlobalVar.HlrResponeQueue.containsKey(systemId)) {
			GlobalVar.HlrResponeQueue.put(systemId, new Queue());
		}
		lookupQueue = new Queue[threadCount];
		lookupSubmission = new LookupSubmission[threadCount];
		for (int i = 0; i < threadCount; i++) {
			this.lookupQueue[i] = new Queue();
			if (i == 0) {
				lookupSubmission[i] = new LookupSubmission(systemId, password, lookupQueue[i], threadId + "[" + i + "]",
						true);
			} else {
				lookupSubmission[i] = new LookupSubmission(systemId, password, lookupQueue[i], threadId + "[" + i + "]",
						false);
			}
			new Thread(lookupSubmission[i], systemId + "_LookupSubmission").start();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		responseHandler = new ResponseHandler(systemId, threadId);
		new Thread(responseHandler, systemId + "_HlrResponseHandler").start();
		new Thread(this, systemId + "_HlrRequestHandler").start();
	}

	public Queue getHlrQueue() {
		return hlrQueue;
	}

	public void setHlrQueue(Queue hlrQueue) {
		this.hlrQueue = hlrQueue;
	}

	public void refreshUser() {
		this.password = GlobalVars.userService.getUserEntry(systemId).getPassword();
		if (lookupSubmission != null) {
			for (int i = 0; i < threadCount; i++) {
				lookupSubmission[i].refreshUserEntry(systemId, password);
			}
		}
	}

	public void refresh() {
		logger.info(systemId + "_" + threadId + " hlrRequestHandler Routing Refreshed");
		responseHandler.refresh();
	}

	@Override
	public void run() {
		int loopCounter = 0;
		while (!stop) {
			try {
				if (++loopCounter > 10) {
					loopCounter = 0;
					if (!enqueueTime.isEmpty()) {
						Iterator<Map.Entry<String, Long>> itr = enqueueTime.entrySet().iterator();
						while (itr.hasNext()) {
							Map.Entry<String, Long> entry = itr.next();
							if (System.currentTimeMillis() > (entry.getValue()
									+ (com.hti.util.Constants.HLR_STATUS_WAIT_DURATION * 1000))) {
								if (GlobalVar.EnqueueRouteObject.containsKey(entry.getKey())) {
									logger.debug(entry.getKey() + " time over.proceed further");
									RouteObject routeObject = GlobalVar.EnqueueRouteObject.remove(entry.getKey());
									GlobalQueue.smsc_in_temp_update_Queue.enqueue(new SmscInObj(routeObject.getMsgId(),
											"C", routeObject.getSmsc(), routeObject.getGroupId(), routeObject.getCost(),
											routeObject.getNetworkId()));
								} else {
									// logger.info(entry.getKey() + " already processed");
								}
								itr.remove();
							}
						}
					}
				}
				if (!hlrQueue.isEmpty()) {
					logger.debug(systemId + "_" + threadId + " hlrQueue: " + hlrQueue.size());
					RouteObject route = null;
					int counter = 0;
					while (!hlrQueue.isEmpty()) {
						route = (RouteObject) hlrQueue.dequeue();
						enqueueTime.put(route.getMsgId(), System.currentTimeMillis());
						GlobalVar.EnqueueRouteObject.put(route.getMsgId(), route);
						lookupQueue[queueNumber].enqueue(new HlrRequest(route.getMsgId(), route.getDestAddress()));
						if (++queueNumber > (threadCount - 1)) {
							queueNumber = 0;
						}
						if (++counter > 500) {
							counter = 0;
							break;
						}
					}
				}
			} catch (Exception e) {
				logger.error(systemId, e.fillInStackTrace());
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		for (int i = 0; i < threadCount; i++) {
			lookupSubmission[i].stop();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		responseHandler.stop();
		logger.info(systemId + "_" + threadId + " HlrRequestHandler Stopped.Queue: " + hlrQueue.size());
	}

	public void stop() {
		stop = true;
		logger.info(systemId + "_" + threadId + " HlrRequestHandler Stopping.Queue: " + hlrQueue.size());
	}
}
