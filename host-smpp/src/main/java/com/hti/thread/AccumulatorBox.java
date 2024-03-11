/*
 * AccumulatorBox.java
 * Created on 08 April 2004, 14:23
 */
package com.hti.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;

public class AccumulatorBox implements Runnable {
	private static Logger logger = LoggerFactory.getLogger("ProcLogger");
	private BindAlert bindAlert;
	// private SubmittedLog submittedLog;
	private SignalRetryProcess signalRetryProcess;
	private ProcessResponseThread processResponseThread;
	private ProcessRemovedResponse processRemovedResponse;
	// private MappingLogWriteThread mappingLogWriteThread;
	private TrackMismatchedDeliver trackMismatchedDeliver;
	private MIS mis_thread;
	private SmscInDeleteThread smscInDeleteThread;
	private SmscInUpdateThread smscInUpdateThread;
	private SmscSubmitLog smscSubmitLog;
	private MismatchedDeliverInsert misInsertDlr;
	private CheckCpuCycle check_cpu_cycle;
	private HtiQueueLoader htiqueueloader;
	private HtiQueueManager htiqueuemanager_thread;
	private DestinationSleepTimeThread dstThread;
	private InsertIntoMappedId mapped_id_thread;
	private MappedIdDeletion mappedIdDeletion;
	private Report report_thread;
	private SmscQueueSelection thread_queue_selection;
	private RepeatedNumberInsert repeatedNumberInsert;
	private TwoWayDeliverInsert twoWayDeliverInsert;
	private DelayedDlrProcess delayedDlrProcess;
	private QueuedAlert queuedAlert;
	private DNDEntryLog dndEntryLogThread;
	private DeliverLogInsert deliverLogInsert;
	// private static Set<String> to_be_removed_resp = new HashSet<String>();
	private boolean stop;

	/**
	 * Creates a new instance of AccumulatorBox
	 */
	public AccumulatorBox() {
		logger.info("AccumulatorBox Starting");
		try {
			try {
				GlobalCache.SmscGroupEntries = GlobalVars.hazelInstance.getMap("smsc_group");
			} catch (Exception e) {
				logger.error("SmscGroupEntries", e);
			}
			htiqueueloader = new HtiQueueLoader();
			bindAlert = new BindAlert();
			new Thread(bindAlert, "BindAlert").start();
			signalRetryProcess = new SignalRetryProcess();
			new Thread(signalRetryProcess, "SignalRetryProcess").start();
			processResponseThread = new ProcessResponseThread();
			new Thread(processResponseThread, "ProcessResponseThread").start();
			delayedDlrProcess = new DelayedDlrProcess();
			new Thread(delayedDlrProcess, "DelayedDlrProcess").start();
			trackMismatchedDeliver = new TrackMismatchedDeliver();
			new Thread(trackMismatchedDeliver, "TrackMismatchedDeliver").start();
			processRemovedResponse = new ProcessRemovedResponse();
			new Thread(processRemovedResponse, "ProcessRemovedResponse").start();
			repeatedNumberInsert = new RepeatedNumberInsert();
			new Thread(repeatedNumberInsert, "RepeatedNumberInsert").start();
			htiqueuemanager_thread = new HtiQueueManager();
			new Thread(htiqueuemanager_thread, "HtiQueueManager").start();
			thread_queue_selection = new SmscQueueSelection();
			new Thread(thread_queue_selection, "SmscQueueSelection").start();
			report_thread = new Report();
			new Thread(report_thread, "Report").start();
			mis_thread = new MIS();
			new Thread(mis_thread, "MIS").start();
			smscInDeleteThread = new SmscInDeleteThread();
			new Thread(smscInDeleteThread, "SmscInDeleteThread").start();
			smscInUpdateThread = new SmscInUpdateThread();
			new Thread(smscInUpdateThread, "SmscInUpdateThread").start();
			misInsertDlr = new MismatchedDeliverInsert();
			new Thread(misInsertDlr, "MismatchedDeliverInsert").start();
			dstThread = new DestinationSleepTimeThread();
			new Thread(dstThread, "DestinationSleepTimeThread").start();
			check_cpu_cycle = new CheckCpuCycle();
			new Thread(check_cpu_cycle, "CheckCpuCycle").start();
			mapped_id_thread = new InsertIntoMappedId();
			new Thread(mapped_id_thread, "InsertIntoMappedId").start();
			mappedIdDeletion = new MappedIdDeletion();
			new Thread(mappedIdDeletion, "MappedIdDeletion").start();
			smscSubmitLog = new SmscSubmitLog();
			new Thread(smscSubmitLog, "SmscSubmitLog").start();
			twoWayDeliverInsert = new TwoWayDeliverInsert();
			new Thread(twoWayDeliverInsert, "TwoWayDeliverInsert").start();
			queuedAlert = new QueuedAlert();
			new Thread(queuedAlert, "QueuedAlert").start();
			dndEntryLogThread = new DNDEntryLog();
			new Thread(dndEntryLogThread, "DndEntryLogThread").start();
			deliverLogInsert = new DeliverLogInsert();
			new Thread(deliverLogInsert,"DeliverLogInsert").start();
		} catch (Exception e) {
			logger.error("AccumulatorBox()", e.fillInStackTrace());
		}
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException ie) {
				// no harm
			}
		}
		logger.info("AccumulatorBox Stopped");
	}

	public void StopQueueLoader() {
		htiqueueloader.stop();
	}

	public void startQueueLoader() {
		logger.info("Starting QueueLoader");
		new Thread(htiqueueloader, "HtiQueueLoader").start();
	}

	public void stop() {
		logger.info("AccumulatorBox Stopping");
		try {
			bindAlert.stop();
			queuedAlert.stop();
			trackMismatchedDeliver.stop();
			htiqueuemanager_thread.stop();
			dstThread.stop();
			thread_queue_selection.stop();
			check_cpu_cycle.stop();
			misInsertDlr.stop();
			mis_thread.stop();
			mapped_id_thread.stop();
			mappedIdDeletion.stop();
			smscInDeleteThread.stop();
			smscInUpdateThread.stop();
			report_thread.stop();
			delayedDlrProcess.stop();
			processResponseThread.stop();
			processRemovedResponse.stop();
			signalRetryProcess.stop();
			smscSubmitLog.stop();
			repeatedNumberInsert.stop();
			twoWayDeliverInsert.stop();
			dndEntryLogThread.stop();
			deliverLogInsert.stop();
		} catch (Exception ie) {
			logger.error("stop()", ie);
		}
		stop = true;
	}
}
