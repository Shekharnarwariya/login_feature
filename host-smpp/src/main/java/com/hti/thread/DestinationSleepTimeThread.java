/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.RoutePDU;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;

class DestinationSleepTimeThread implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private boolean stop;

	public DestinationSleepTimeThread() {
		logger.info("DestinationSleepTimeThread Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			if (!GlobalCache.DelaySubmition.isEmpty()) {
				try {
					logger.info("Destination Delay Submission Queue: " + GlobalCache.DelaySubmition.size());
					Iterator<Map.Entry<RoutePDU, Long>> itr = GlobalCache.DelaySubmition.entrySet().iterator();
					Map.Entry<RoutePDU, Long> entry = null;
					while (itr.hasNext()) {
						entry = itr.next();
						if (System.currentTimeMillis() >= entry.getValue()) {
							GlobalQueue.interProcessManage.enqueue(entry.getKey());
							itr.remove();
						}
					}
				} catch (Exception e) {
					logger.error("", e.fillInStackTrace());
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
			}
		}
		logger.info("DestinationSleepTimeThread Stopped");
	}

	private void clear() {
		logger.info("Destination Delay Submission Clear Count: " + GlobalCache.DelaySubmition.size());
		try {
			GlobalCache.DelaySubmition.forEach((k, v) -> {
				GlobalQueue.interProcessManage.enqueue((RoutePDU) k);
			});
		} catch (Exception e) {
		}
	}

	public void stop() {
		logger.info("DestinationSleepTimeThread Stopping");
		clear();
		stop = true;
	}
}
