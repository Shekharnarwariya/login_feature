/*
 * TrackDeliverResponse.java
 *
 * Created on October 20, 2004, 12:12 PM
 */
package com.hti.smsc;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.DeliverObj;
import com.hti.objects.HTIQueue;
import com.hti.objects.ResponseObj;
import com.hti.objects.SerialQueue;
import com.hti.smsc.dto.SmscEntry;
import com.hti.util.Constants;
import com.hti.util.FileUtil;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.logica.smpp.Data;
import com.logica.smpp.pdu.DeliverSM;
import com.logica.smpp.pdu.ValueNotSetException;

/**
 * @author Administrator
 */
public class TrackDeliverResponse implements Runnable {
	private HTIQueue deliverQueue = null;
	private Map<String, String> response_map; // To Track Deliver_SM
	private String Smsc = null;
	SmscEntry entry = null;
	private boolean stop = false;
	private Logger logger = LoggerFactory.getLogger(TrackDeliverResponse.class);
	private Map<String, Long> waitingQueue = new HashMap<String, Long>();
	private Map<String, DeliverObj> waitingQueueObject = new HashMap<String, DeliverObj>();
	private Set<String> enqueuedSet = new HashSet<String>();
	// private String route;
	Iterator<Map.Entry<String, Long>> iterator;
	private int loop_counter = 0;
	private int processed_counter = 0;
	private int received_counter = 0; // total received
	private int waitingCounter = 0; // total put to waiting Queue
	private int waitingSuccess = 0; // Success Count after waiting
	private SerialQueue localQueue = new SerialQueue();
	// private boolean fixDecimal = false;
	// private boolean log = false;

	public TrackDeliverResponse() {
	}

	public TrackDeliverResponse(SmscEntry entry, HTIQueue deliverQueue, int session_id) {
		this.entry = entry;
		this.Smsc = entry.getName();
		this.deliverQueue = deliverQueue;
		this.response_map = GlobalCache.smscwiseResponseMap.get(session_id);
		logger.info(Smsc + "_TrackDeliverResponse Thread Starting: ");
	}

	@Override
	public void run() {
		checkBackFolder();
		logger.info(Smsc + "_TrackDeliverResponse Thread Started.");
		while (!stop) {
			if (++loop_counter >= 30) {
				loop_counter = 0;
				if (!waitingQueue.isEmpty()) {
					iterator = waitingQueue.entrySet().iterator();
					Map.Entry<String, Long> entry = null;
					String resp_id = null;
					while (iterator.hasNext()) {
						entry = iterator.next();
						resp_id = entry.getKey();
						if (System.currentTimeMillis() >= entry.getValue()) {
							if (waitingQueueObject.containsKey(resp_id)) {
								logger.info(Smsc + " Putting to DeliverQueue to Recheck: " + resp_id);
								localQueue.enqueue(waitingQueueObject.remove(resp_id));
							}
							enqueuedSet.add(resp_id);
							iterator.remove();
						}
					}
				}
			}
			if (deliverQueue.isEmpty() && localQueue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			} else {
				int counter = 0;
				DeliverSM deliver = null;
				while (!deliverQueue.isEmpty()) {
					try {
						deliver = (DeliverSM) deliverQueue.dequeue();
						if (deliver.getShortMessage() == null || !deliver.getShortMessage().contains("id:")) {
							logger.info(Smsc + " RespId missing: " + deliver.debugString());
							deliver.setSmsc(Smsc);
							GlobalQueue.TwoWayDeliverInsertQueue.enqueue(deliver);
							continue;
						}
						DeliverObj deliverObj = getDeliverObject(deliver);
						if (deliverObj != null) {
							localQueue.enqueue(deliverObj);
						}
						received_counter++;
						if (++processed_counter >= 25000) {
							processed_counter = 0;
							logger.info(Smsc + " Deliver Received Count: " + received_counter + " dlrQueue : "
									+ deliverQueue.size() + " processQueue: " + localQueue.size() + " WaitingQueue : "
									+ waitingQueue.size() + " WaitingCounter: " + waitingCounter + " WaitSuccess: "
									+ waitingSuccess);
						}
						if (++counter > 1000) {
							break;
						}
					} catch (Exception e) {
						logger.error(Smsc, deliver.debugString(), e);
					}
				}
			}
			while (!localQueue.isEmpty()) {
				processDeliver((DeliverObj) localQueue.dequeue());
				if (stop) {
					break;
				}
			}
		}
		logger.info(Smsc + " Deliver Received Count: " + received_counter + " dlrQueue : " + deliverQueue.size()
				+ " processQueue: " + localQueue.size() + " WaitingQueue : " + waitingQueue.size() + " WaitingCounter: "
				+ waitingCounter + " WaitSuccess: " + waitingSuccess);
		if (!deliverQueue.isEmpty() || !localQueue.isEmpty() || !waitingQueueObject.isEmpty()) {
			writeQueue();
		}
		logger.info(Smsc + "_TrackDeliverResponse Thread Stopped");
	}

	private DeliverObj getDeliverObject(DeliverSM deliver) {
		String short_messege = null;
		try {
			short_messege = deliver.getShortMessage();
			// String source = deliver.getSourceAddr().getAddress();
			// String destination = deliver.getDestAddr().getAddress();
			String response_id = null;
			String receiptedMessageId = null;
			try {
				receiptedMessageId = deliver.getReceiptedMessageId();
				response_id = short_messege.substring(short_messege.indexOf("id:") + 3, short_messege.indexOf("sub"))
						.trim();
			} catch (ValueNotSetException e) {
				response_id = short_messege.substring(short_messege.indexOf("id:") + 3, short_messege.indexOf("sub"))
						.trim();
			}
			if (entry.isRlzRespId()) {
				if (receiptedMessageId != null) {
					receiptedMessageId = receiptedMessageId.replaceFirst("^0+(?!$)", "");
				}
				if (response_id != null) {
					response_id = response_id.replaceFirst("^0+(?!$)", "");
				}
			}
			if (receiptedMessageId != null) {
				if (!receiptedMessageId.equalsIgnoreCase(response_id)) {
					logger.info(Smsc + " ResponseId: " + response_id + " ReceiptedMessageId: " + receiptedMessageId);
					if (response_map.containsKey(receiptedMessageId)) {
						response_id = receiptedMessageId;
					}
				}
			}
			String status = short_messege.substring(short_messege.indexOf("stat:") + 5, short_messege.indexOf("err:"))
					.trim();
			/*
			 * String done_time = short_messege .substring(short_messege.indexOf("done date:") + 10, short_messege.indexOf("stat:")).trim();
			 */
			int textIndex = short_messege.indexOf("Text:");
			String error_code = "000";
			if (textIndex == -1) {
				textIndex = short_messege.indexOf("text:");
			}
			if (textIndex != -1) {
				try {
					error_code = short_messege.substring(short_messege.indexOf("err:") + 4, textIndex).trim();
					if (error_code.length() > 5) {
						error_code = error_code.substring(0, 5).trim();
					}
				} catch (Exception se) {
					logger.error(
							"Invalid Error Code " + error_code + " For Deliver_sm <" + response_id + "> From " + Smsc);
					error_code = "000";
				}
			} else {
				try {
					error_code = short_messege.substring(short_messege.indexOf("err:") + 4, short_messege.length())
							.trim();
					// System.out.println("error_code: "+error_code);
					if (error_code.length() > 5) {
						error_code = error_code.substring(0, 5).trim();
					}
				} catch (Exception se) {
					logger.error(
							"Invalid Error Code " + error_code + " For Deliver_sm <" + response_id + "> From " + Smsc);
					error_code = "000";
				}
			}
			GlobalQueue.DeliverLogQueue.enqueue(new com.hti.objects.DeliverLogObject(response_id,
					deliver.getReceivedOn(), deliver.getShortMessage(), Smsc, deliver.getSourceAddr().getAddress(),
					deliver.getDestAddr().getAddress()));
			return (new DeliverObj(response_id, Smsc, deliver.getReceivedOn(), deliver.getSourceAddr().getAddress(),
					deliver.getDestAddr().getAddress(), status, error_code));
		} catch (Exception e) {
			logger.error(Smsc, short_messege, e);
		}
		return null;
	}

	private void processDeliver(DeliverObj deliver) {
		// String response_id = deliver.getResponseId();
		// String status = deliver.getStatus();
		String message_id = null;
		try {
			if (response_map.containsKey(deliver.getResponseId())) {
				message_id = response_map.get(deliver.getResponseId());
				if (deliver.getStatus() != null && !deliver.getStatus().startsWith("ACCEP")) {
					response_map.remove(deliver.getResponseId());
				}
			}
			boolean proceed = false;
			if (message_id == null) {
				if (enqueuedSet.contains(deliver.getResponseId())) // already removed from waitingQueue.
				{
					enqueuedSet.remove(deliver.getResponseId());
					GlobalQueue.deliverWaitingQueue.enqueue(deliver);
				} else { // put to waitingQueue
					waitingCounter++;
					waitingQueue.put(deliver.getResponseId(), System.currentTimeMillis() + (60 * 1000));
					waitingQueueObject.put(deliver.getResponseId(), deliver);
				}
			} else {
				if (enqueuedSet.contains(deliver.getResponseId())) {
					waitingSuccess++;
					enqueuedSet.remove(deliver.getResponseId());
				}
				proceed = true;
			}
			if (proceed) {
				System.out.println(
						"<" + message_id + ":" + deliver.getResponseId() + ">< DLR ><" + deliver.getDestination() + "> "
								+ deliver.getStatus() + ":-> " + Smsc + "-> " + deliver.getSource());
				GlobalQueue.processResponseQueue.enqueue(new ResponseObj(message_id, deliver.getResponseId(),
						deliver.getStatus(), deliver.getTime(), deliver.getErrorCode(), Data.DELIVER_SM));
			}
		} catch (Exception e) {
			logger.error(Smsc + " Resp: " + deliver.getResponseId(), e.fillInStackTrace());
		}
	}

	private void writeQueue() {
		DeliverSM deliver = null;
		while (!deliverQueue.isEmpty()) {
			deliver = (DeliverSM) deliverQueue.dequeue();
			DeliverObj deliverObj = getDeliverObject(deliver);
			if (deliverObj != null) {
				localQueue.enqueue(deliverObj);
			}
		}
		if (!waitingQueueObject.isEmpty()) {
			Iterator<DeliverObj> itr = waitingQueueObject.values().iterator();
			while (itr.hasNext()) {
				localQueue.enqueue(itr.next());
			}
		}
		waitingQueueObject.clear();
		waitingQueue.clear();
		logger.info(Smsc + " Deliver Process Queue : " + localQueue.size());
		try {
			FileUtil.writeObject(Constants.deliver_backup_dir + Smsc + "-deliverQueue.ser", localQueue);
		} catch (Exception ex) {
			logger.error(ex + " While Writing DeliverQueue Object of " + Smsc);
		}
	}

	private void checkBackFolder() {
		logger.info("<- Checking for Deliver Backup File -> " + Smsc);
		File file = new File(Constants.deliver_backup_dir + Smsc + "-deliverQueue.ser");
		if (file.exists()) {
			logger.info("<- Deliver Backup File Found -> " + Smsc);
			try {
				SerialQueue tempQueue = (SerialQueue) FileUtil
						.readObject(Constants.deliver_backup_dir + Smsc + "-deliverQueue.ser", true);
				if (tempQueue != null && !tempQueue.isEmpty()) {
					logger.info(Smsc + " Deliver Backup Queue Size ---> " + tempQueue.size());
					while (!tempQueue.isEmpty()) {
						localQueue.enqueue((DeliverObj) tempQueue.dequeue());
					}
				}
			} catch (Exception e) {
				logger.error(e + " While Reading DeliverQueue Object of " + Smsc);
			}
		}
	}

	public void stop() {
		logger.info(Smsc + "_TrackDeliverResponse Thread Stopping");
		stop = true;
	}
}
