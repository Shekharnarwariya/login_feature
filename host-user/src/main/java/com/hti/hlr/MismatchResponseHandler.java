package com.hti.hlr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.logica.smpp.util.Queue;
import com.logica.smpp.Data;
import com.logica.smpp.pdu.DeliverSM;
import com.logica.smpp.pdu.ValueNotSetException;

public class MismatchResponseHandler implements Runnable {
	private Logger logger = LoggerFactory.getLogger("hlrLogger");
	private boolean stop;
	private Queue waitingQueue = new Queue();

	public MismatchResponseHandler() {
		logger.info("MismatchResponseHandler Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				if (!GlobalVar.MismatchedWaiting.isEmpty()) {
					while (!GlobalVar.MismatchedWaiting.isEmpty()) {
						waitingQueue.enqueue((DeliverSM) GlobalVar.MismatchedWaiting.dequeue());
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				if (!waitingQueue.isEmpty()) {
					logger.info("Processing WaitingQueue: " + waitingQueue.size());
					while (!waitingQueue.isEmpty()) {
						DeliverSM deliver = (DeliverSM) waitingQueue.dequeue();
						String systemid = deliver.getClientName();
						logger.info("Processing[" + systemid + "]: " + deliver.debugString());
						String response_id = null;
						try {
							response_id = deliver.getReceiptedMessageId();
							if (GlobalVar.HlrResponseMapping.containsKey(response_id)) {
								String messageId = GlobalVar.HlrResponseMapping.remove(response_id);
								HlrResponse result = new HlrResponse();
								result.setResponseId(response_id);
								// ------------- parsing result ------------------
								String short_messege = deliver.getShortMessage();
								String status = short_messege
										.substring(short_messege.indexOf("stat:") + 5, short_messege.indexOf("err:"))
										.trim();
								String error = short_messege
										.substring(short_messege.indexOf("err:") + 4, short_messege.indexOf("imsi:"))
										.trim();
								String nnc = short_messege
										.substring(short_messege.indexOf("nnc:") + 4, short_messege.indexOf("mccmnc:"))
										.trim();
								String isPorted = short_messege.substring(short_messege.indexOf("isPorted:") + 9,
										short_messege.indexOf("p_nnc:")).trim();
								String ported_nnc = short_messege.substring(short_messege.indexOf("p_nnc:") + 6,
										short_messege.indexOf("isRoaming:")).trim();
								String isRoaming = short_messege.substring(short_messege.indexOf("isRoaming:") + 10,
										short_messege.indexOf("r_cc:")).trim();
								String roaming_nnc = short_messege
										.substring(short_messege.indexOf("r_nnc:") + 6, short_messege.indexOf("prmnt:"))
										.trim();
								String permanent = short_messege
										.substring(short_messege.indexOf("prmnt:") + 6, short_messege.indexOf("dnd:"))
										.trim();
								String dnd = short_messege
										.substring(short_messege.indexOf("dnd:") + 4, short_messege.indexOf("text:"))
										.trim();
								if (ported_nnc.length() == 0) {
									ported_nnc = null;
								}
								if (roaming_nnc.length() == 0) {
									roaming_nnc = null;
								}
								if (permanent != null && permanent.length() > 0) {
									try {
										result.setPermanent(Integer.parseInt(permanent));
									} catch (Exception ex) {
										logger.info(response_id + " invalid permanent value: " + permanent);
									}
								}
								result.setError(error);
								result.setNnc(nnc);
								if (isPorted.equalsIgnoreCase("true") || isRoaming.equalsIgnoreCase("true")) {
									if (isPorted.equalsIgnoreCase("true") && ported_nnc != null) {
										result.setPorted(true);
										result.setPortedNNC(ported_nnc);
									}
									if (isRoaming.equalsIgnoreCase("true") && roaming_nnc != null) {
										result.setRoaming(true);
										result.setRoamingNNC(roaming_nnc);
									}
								}
								if (status.equalsIgnoreCase("UNDELIV")) {
									if (error.equalsIgnoreCase(LookupStatus.SYSTEM_FAILURE)) {
										result.setStatus("FAILED");
									} else if (result.getPermanent() > 0) {
										result.setStatus("UNDELIV");
									} else {
										result.setStatus("DELIVRD");
									}
								} else {
									result.setStatus("DELIVRD");
								}
								if (dnd.equalsIgnoreCase("1")) {
									result.setDnd(true);
								}
								result.setCommandId(Data.DELIVER_SM);
								GlobalVar.HlrResponeQueue.get(systemid).enqueue(new HlrRequest(messageId, result));
							} else {
								logger.info(response_id + " mismatchDlr");
							}
						} catch (ValueNotSetException ex) {
							logger.error(deliver.debugString(), ex);
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		logger.info("MismatchResponseHandler Stopped");
	}

	public void stop() {
		stop = true;
		logger.info("MismatchResponseHandler Stopping");
	}
}
