package com.hti.util;

import com.hti.objects.HTIQueue;
import com.hti.objects.SerialQueue;
import com.logica.smpp.util.Queue;

public class GlobalQueue {
	public static SerialQueue interProcessRequest = new SerialQueue();
	public static SerialQueue processResponseQueue = new SerialQueue();
	public static SerialQueue DelayedDlrQueue = new SerialQueue();
	public static SerialQueue processRemovedResponseQueue = new SerialQueue(); // Process Response removed/not available in response cache
	public static SerialQueue omqReceivedQueue = new SerialQueue();
	public static SerialQueue interProcessManage = new SerialQueue();
	public static SerialQueue MIS_Dump_Queue = new SerialQueue();
	public static HTIQueue smsc_in_delete_Queue = new HTIQueue(); // delete pdu from smsc_in queue
	public static SerialQueue smsc_in_update_Queue = new SerialQueue();
	public static HTIQueue DeliverProcessQueue = new HTIQueue(); // processing received DLR from connection
	public static HTIQueue DeliverSMTempQueue = new HTIQueue(); // insert dlr on temp table if omq/user_host disconnected
	public static HTIQueue DeliverLogQueue = new HTIQueue(); // insert received DLR for log
	// public static HTIQueue MappingLogWriteQueue = new HTIQueue(); // log received Submit_SM_Resp from smsc
	// public static HTIQueue SubmittedQueue = new HTIQueue(); // log Submitted Submit_SM to smsc
	// public static SerialQueue pduStatusQueue = new SerialQueue();
	public static SerialQueue reportUpdateQueue = new SerialQueue();
	public static SerialQueue reportLogQueue = new SerialQueue();
	public static SerialQueue submitLogQueue = new SerialQueue();
	public static SerialQueue dndLogQueue = new SerialQueue();
	// public static SerialQueue dashboardQueue = new SerialQueue();
	public static HTIQueue mismatchedDeliverQueue = new HTIQueue();
	public static SerialQueue MAPPED_ID_QUEUE = new SerialQueue();
	public static HTIQueue deliverWaitingQueue = new HTIQueue();
	public static SerialQueue signalRetryQueue = new SerialQueue();
	public static SerialQueue signalWaitQueue = new SerialQueue();
	public static Queue RepeatedNumberQueue = new Queue();
	public static Queue TwoWayDeliverInsertQueue = new Queue();
}
