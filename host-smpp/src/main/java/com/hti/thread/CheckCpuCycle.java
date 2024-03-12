/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.PriorityQueue;
import com.hti.rmi.UserServiceInvoke;
import com.hti.util.Constants;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;

/**
 *
 * @author
 */
public class CheckCpuCycle implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private Calendar nextClearTime;
	public static boolean PRINT = false;
	public static boolean EXECUTE_GC = false;
	private long mb = 1024 * 1024;
	private Runtime runtime = Runtime.getRuntime();
	private long used_memory;
	private long max_memory;
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
				hold_on_processing();
				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
				}
				runGC();
			}
			if (used_memory < proc_limit) {
				resume_processing();
			}
			if (PRINT) {
				PRINT = false;
				printCacheSize();
			} else {
				if (nextClearTime.getTime().before(new Date())) {
					logger.info("*** Printing Cache Interval *******");
					printCacheSize();
					nextClearTime = Calendar.getInstance();
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
		proc_limit = (max_memory * 7) / 10; // 70% memeory
		logger.info("Memory Used:---> " + used_memory + " MB. Max Available: " + max_memory + " MB");
	}

	private void printCacheSize() {
		logger.info("*************** Start Print Cache *****************");
		try {
			logger.info("omqReceivedQueue Queue:---> " + GlobalQueue.omqReceivedQueue.size());
			logger.info("interProcessRequest Queue:---> " + GlobalQueue.interProcessRequest.size());
			logger.info("interProcessManage Queue :---> " + GlobalQueue.interProcessManage.size());
			logger.info("processResponseQueue :---> " + GlobalQueue.processResponseQueue.size());
			logger.info("processRemovedResponse :---> " + ProcessRemovedResponse.getStatistics());
			logger.info("MIS Queue:---> " + GlobalQueue.MIS_Dump_Queue.size());
			logger.info("smsc_in_delete :---> " + GlobalQueue.smsc_in_delete_Queue.size());
			logger.info("smsc_in_update :---> " + SmscInUpdateThread.getStatistics());
			logger.info("DeliverProcessQueue :---> " + GlobalQueue.DeliverProcessQueue.size());
			logger.info("DeliverSMTempQueue :---> " + GlobalQueue.DeliverSMTempQueue.size());
			logger.info("report :---> " + GlobalQueue.reportUpdateQueue.size());
			logger.info("reportLogQueue :---> " + GlobalQueue.reportLogQueue.size());
			logger.info("mismatchedDeliverQueue :---> " + GlobalQueue.mismatchedDeliverQueue.size());
			logger.info("DelaySubmition :---> " + GlobalCache.DelaySubmition.size());
			logger.info("MAPPED_ID_QUEUE :---> " + GlobalQueue.MAPPED_ID_QUEUE.size());
			logger.info("recievedDlrResponseId :---> " + GlobalCache.recievedDlrResponseId.size());
			logger.info("SmscSubmitTime :---> " + GlobalCache.SmscSubmitTime.size());
			logger.info("DelayResponse :---> " + GlobalCache.nonResponding.size());
		} catch (Exception e3) {
			logger.error("AllQueue: " + e3);
		}
		logger.info("*************** User MIS Queue Checking *****************");
		try {
			for (Map.Entry<String, UserWiseMis> entry : GlobalCache.UserMisQueueObject.entrySet()) {
				if (!(entry.getValue()).getQueue().isEmpty()) {
					logger.info(entry.getKey() + " Mis Queue:--> " + (entry.getValue()).getQueue().size());
				}
			}
		} catch (Exception e2) {
			logger.error("UserwisemisQueue: " + e2);
		}
		logger.info("*************** Smsc Queue Checking *****************");
		try {
			Set<String> names = GlobalVars.smscService.listNames();
			for (String smsc : names) {
				int smsc_live_queue_size = 0;
				if (GlobalCache.SmscQueueCache.containsKey(smsc)) {
					for (int j = 1; j <= Constants.noofqueue; j++) {
						smsc_live_queue_size += ((PriorityQueue) GlobalCache.SmscQueueCache.get(smsc)).PQueue[j].size();
					}
				}
				if (smsc_live_queue_size > 0) {
					logger.info(smsc + " Queue Size:---> " + smsc_live_queue_size);
				}
			}
		} catch (Exception e1) {
			logger.error("SmscQueue: " + e1);
		}
		logger.info("*************** SmscWise Sequence Checking *****************");
		try {
			for (Map.Entry<String, Map<Integer, String>> entry : GlobalCache.smscwisesequencemap.entrySet()) {
				if (!entry.getValue().isEmpty()) {
					logger.info(entry.getKey() + " SequenceCache: " + entry.getValue().size());
				}
			}
		} catch (Exception e2) {
			logger.error("SmscWiseSequenceCache: " + e2);
		}
		logger.info("*************** SmscWise Response Checking *****************");
		try {
			for (Map.Entry<Integer, Map<String, String>> entry : GlobalCache.smscwiseResponseMap.entrySet()) {
				if (!entry.getValue().isEmpty()) {
					if (GlobalCache.SessionIdSmscList.containsKey(entry.getKey())) {
						logger.info(GlobalCache.SessionIdSmscList.get(entry.getKey()) + " ResponseCache: "
								+ entry.getValue().size());
					}
				}
			}
		} catch (Exception e2) {
			logger.error("SmscWiseResponseCache: " + e2);
		}
		logger.info("*************** Print Cache Finished *****************");
	}

	private void hold_on_processing() {
		if (Constants.PROCESSING_STATUS) {
			logger.info(" <--- Holding on Queue Process --> ");
			Constants.PROCESSING_STATUS = false;
			try {
				new UserServiceInvoke().onHoldTraffic(true);
			} catch (Exception e) {
				logger.error("Error While Communicating To User Server: " + e);
			}
		}
	}

	private void resume_processing() {
		if (!Constants.PROCESSING_STATUS) {
			logger.info(" <--- Resuming Queue Processing --> ");
			Constants.PROCESSING_STATUS = true;
			try {
				new UserServiceInvoke().onHoldTraffic(false);
			} catch (Exception e) {
				logger.error("Error While Communicating To User Server: " + e);
			}
		}
	}

	public void stop() {
		logger.info("CheckCpuCycle Stopping");
		stop = true;
	}
}
