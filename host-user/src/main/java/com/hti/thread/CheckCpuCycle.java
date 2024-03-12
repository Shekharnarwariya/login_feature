/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.hlr.GlobalVar;
import com.hti.objects.HTIQueue;
import com.hti.user.WebDeliverProcess;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;

/**
 *
 * @author
 */
public class CheckCpuCycle implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private Calendar nextClearTime;
	// private long gc_limit = 1024
	public static boolean PRINT = false;
	public static boolean EXECUTE_GC = false;
	private long mb = 1024 * 1024;
	private Runtime runtime = Runtime.getRuntime();
	private long used_memory;
	private long max_memory;
	// long gc_limit;
	private long proc_limit;
	private boolean stop;

	public CheckCpuCycle() {
		logger.info("CheckCpuCycle Starting");
		nextClearTime = Calendar.getInstance();
		nextClearTime.add(Calendar.MINUTE, +30);
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				Thread.sleep(1 * 60 * 1000); // 1 minutes
			} catch (Exception ex) {
			}
			checkMemoryUsage();
			if (used_memory >= proc_limit) {
				EXECUTE_GC = true;
			}
			if (EXECUTE_GC) {
				EXECUTE_GC = false;
				PRINT = false;
				printCacheSize();
				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
				}
				runGC();
			}
			if (PRINT) {
				PRINT = false;
				printCacheSize();
			} else {
				if (nextClearTime.getTime().before(new Date())) {
					logger.info("*** Printing Cache Interval *******");
					printCacheSize();
					nextClearTime.add(Calendar.MINUTE, +30);
				}
			}
		}
		logger.info("CheckCpuCycle Stopped");
	}

	private void runGC() {
		logger.warn("Excuting Garbage Collector. Used Memory:-> " + used_memory + " MB"); // Print used memory
		System.gc();
		used_memory = ((runtime.totalMemory() - runtime.freeMemory()) / mb);
		logger.info("After Garbage Collection. Used Memory:-> " + used_memory + " MB");
	}

	private void checkMemoryUsage() {
		used_memory = (runtime.totalMemory() - runtime.freeMemory()) / mb;
		max_memory = (runtime.maxMemory() / mb);
		proc_limit = (max_memory * 8) / 10; // 80% memeory
		logger.info("Memory Used:---> " + used_memory + " MB. Max Available: " + max_memory + " MB");
	}

	private void printCacheSize() {
		logger.info("*************** Start Print Cache *****************");
		try {
			logger.info(ProcessPDU.getStatistics());
			logger.info("smsc_in_Queue :---> " + GlobalQueue.smsc_in_Queue.size());
			logger.info("smsc_in_temp_Queue :---> " + GlobalQueue.smsc_in_temp_Queue.size());
			logger.info("smsc_in_temp_update_Queue :---> " + GlobalQueue.smsc_in_temp_update_Queue.size());
			logger.info("smsc_in_log_Queue:---> " + GlobalQueue.smsc_in_log_Queue.size());
			logger.info("DeliverProcess Queue  :---> " + GlobalQueue.DeliverProcessQueue.size());
			logger.info("DLRInsertQueue :---> " + GlobalQueue.DLRInsertQueue.size());
			logger.info("backupRespLogQueue :---> " + GlobalQueue.backupRespLogQueue.size());
			logger.info("reportQueue :---> " + GlobalQueue.reportLogQueue.size());
			logger.info("lookupStatusInsertQueue Queue :---> " + GlobalVar.lookupStatusInsertQueue.size());
		} catch (Exception e) {
			logger.error("AllQueue: " + e);
		}
		logger.info("*************** User Content Queue Checking *****************");
		try {
			for (Map.Entry<String, UserWiseContent> entry : GlobalCache.UserContentQueueObject.entrySet()) {
				if (!entry.getValue().getQueue().isEmpty()) {
					logger.info(entry.getKey() + " Content Queue:--> " + entry.getValue().getQueue().size());
				}
			}
		} catch (Exception e) {
			logger.error("UserwiseDeliverQueue: " + e);
		}
		logger.info("*************** User Deliver Process Queue Checking *****************");
		try {
			for (Map.Entry<String, HTIQueue> entry : GlobalCache.UserDeliverProcessQueue.entrySet()) {
				if (!entry.getValue().isEmpty()) {
					logger.info(entry.getKey() + " Deliver Queue:--> " + entry.getValue().size());
				}
			}
		} catch (Exception e) {
			logger.error("UserwiseDeliverQueue: " + e);
		}
		logger.info("*************** Web Deliver Process Queue Checking *****************");
		try {
			for (Map.Entry<String, HTIQueue> entry : GlobalCache.WebDeliverProcessQueue.entrySet()) {
				if (!entry.getValue().isEmpty()) {
					logger.info(entry.getKey() + " Web Deliver Queue:--> " + entry.getValue().size());
				}
			}
			for (Map.Entry<String, WebDeliverProcess> entry : GlobalCache.UserWebObject.entrySet()) {
				if (entry.getValue().getWorkerQueueSize() > 0) {
					logger.info(entry.getKey() + " WebDlr WorkerQueue:-> " + entry.getValue().getWorkerQueueSize());
				}
			}
		} catch (Exception e) {
			logger.error("UserWebDeliverQueue: " + e);
		}
		logger.info("*************** Print Cache Finished *****************");
	}

	public void stop() {
		logger.info("CheckCpuCycle Stopping");
		stop = true;
	}
}
