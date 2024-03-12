package com.hti.util;

import java.util.List;
import java.util.Vector;

import com.hti.objects.HTIQueue;
import com.logica.smpp.pdu.DeliverSM;

public class GlobalQueue {
	public static HTIQueue DeliverProcessQueue = new HTIQueue();
	public static HTIQueue smsc_in_Queue = new HTIQueue(); // received pdu from users insertion queue
	public static HTIQueue smsc_in_temp_Queue = new HTIQueue(); // received pdu from users insertion queue
	public static HTIQueue smsc_in_temp_update_Queue = new HTIQueue(); // received pdu from users updation queue for temp table
	public static HTIQueue smsc_in_log_Queue = new HTIQueue(); // received pdu from users log insertion queue
	public static HTIQueue request_log_Queue = new HTIQueue(); // received pdu from users as it is log insertion queue
	public static List<DeliverSM> DLRInsertQueue = new Vector<DeliverSM>();
	public static List<DeliverSM> backupRespLogQueue = new Vector<DeliverSM>();
	public static HTIQueue reportLogQueue = new HTIQueue();
	public static HTIQueue spamReportQueue = new HTIQueue();
	public static HTIQueue profitReportQueue = new HTIQueue();
	public static HTIQueue interProcessRequest = new HTIQueue();
}
