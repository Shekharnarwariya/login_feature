/*
 * HtiPDUEventListner.java
 *
 * Created on 13 March 2004, 11:20
 */
package com.hti.smsc;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.HTIQueue;
import com.hti.objects.SerialQueue;
import com.hti.smsc.dto.SmscEntry;
import com.hti.util.GlobalCache;
import com.logica.smpp.Data;
import com.logica.smpp.ServerPDUEvent;
import com.logica.smpp.ServerPDUEventListener;
import com.logica.smpp.pdu.DataSM;
import com.logica.smpp.pdu.DeliverSM;
import com.logica.smpp.pdu.EnquireLink;
import com.logica.smpp.pdu.PDU;
import com.logica.smpp.pdu.SubmitSMResp;

/**
 * @author administrator Get the response from SMSC , check type of Response Mapped the MSGID from HtiPDUHashtable, inqueue MappedMsgId Object in InterprosessResponse according to its Type Query
 *         responsed are Generated without HtiHostMSGID and HtiMSGID remove them from Hashtable no further use <Thread Pooling with Three Thread>
 */
public class HtiPDUEventListner implements ServerPDUEventListener {
	private PDU recieved_pdu;
	// private String entry.getName();
	private TrackDeliverResponse trackDeliverResponse;
	private TrackSubmitResponse trackSubmitResponse;
	private HTIQueue deliverQueue = null;
	private Logger logger = LoggerFactory.getLogger(HtiPDUEventListner.class);
	private boolean init = true, isFirstDLR = true;
	private int session_id;
	private Calendar dlr_Calendar = null;
	private boolean stop = false;
	private DeliverSM deliver_sm = null;
	private SerialQueue responseQueue;
	private SmscEntry entry;
	// private boolean log = false;
	public long lastReceivedOn = System.currentTimeMillis();

	public HtiPDUEventListner(SmscEntry entry, int session_id) {
		this.entry = entry;
		this.session_id = session_id;
	}

	private void setCalender() {
		if (GlobalCache.SmscLastDlrRecieve.containsKey(entry.getName())) {
			dlr_Calendar = GlobalCache.SmscLastDlrRecieve.get(entry.getName());
		} else {
			dlr_Calendar = Calendar.getInstance();
			GlobalCache.SmscLastDlrRecieve.put(entry.getName(), dlr_Calendar);
		}
	}

	@Override
	public void handleEvent(ServerPDUEvent getevent) {
		if (init) {
			logger.info(entry.getName() + " <- EventListner Started -> ");
			init = false;
		}
		if (!stop) {
			processPDU(getevent);
		}
	}

	private void processPDU(ServerPDUEvent event) {
		lastReceivedOn = System.currentTimeMillis();
		try {
			recieved_pdu = event.getPDU();
			int command_id = recieved_pdu.getCommandId();
			if (recieved_pdu.isRequest()) {
				if (command_id == Data.ENQUIRE_LINK) {
					// System.err.println("command_id>>>>>>" + command_id);
					EnquireLink enq_link = (EnquireLink) recieved_pdu;
					try {
						event.getConnection().send(enq_link.getResponse().getData());
					} catch (Exception io) {
						logger.error(entry.getName() + " <-- Socket Closed[" + io + "] --> ");
					}
				} else if (command_id == Data.DELIVER_SM) {
					try {
						// if get Delivery_PDU
						deliver_sm = (DeliverSM) recieved_pdu;
						try {
							event.getConnection().send(deliver_sm.getResponse().getData());
						} catch (java.io.IOException io) {
							logger.error(entry.getName() + " <--- Socket Closed While DLR Ack --> ");
						}
						deliver_sm.setSmsc(entry.getName());
						deliver_sm.setReceivedOn(new Date());
						// ----------- Added By Amit_vish --------
						if (deliverQueue == null) {
							deliverQueue = new HTIQueue();
						}
						deliverQueue.enqueue(deliver_sm);
						if (trackDeliverResponse == null) {
							trackDeliverResponse = new TrackDeliverResponse(entry, deliverQueue, session_id);
							new Thread(trackDeliverResponse, entry.getName() + "_TrackDeliverResponse").start();
						}
						if (isFirstDLR) {
							isFirstDLR = false;
							setCalender();
						}
						dlr_Calendar.setTime(new Date());
					} catch (Exception e) {
						logger.error(entry.getName() + "[Deliver]", e.fillInStackTrace());
					}
				} else if (command_id == Data.DATA_SM) {
					try {
						// if get Delivery_PDU
						DataSM data_sm = (DataSM) recieved_pdu;
						try {
							event.getConnection().send(data_sm.getResponse().getData());
						} catch (java.io.IOException io) {
							logger.error(entry.getName() + " <--- Socket Closed While DLR Ack --> ");
						}
						deliver_sm = convert(data_sm);
						if (deliver_sm != null) {
							logger.info(entry.getName() + " converetdDLR: " + deliver_sm.debugString());
							deliver_sm.setSmsc(entry.getName());
							deliver_sm.setReceivedOn(new Date());
							// ----------- Added By Amit_vish --------
							if (deliverQueue == null) {
								deliverQueue = new HTIQueue();
							}
							deliverQueue.enqueue(deliver_sm);
							if (trackDeliverResponse == null) {
								trackDeliverResponse = new TrackDeliverResponse(entry, deliverQueue,
										session_id);
								new Thread(trackDeliverResponse, entry.getName() + "_TrackDeliverResponse").start();
							}
							if (isFirstDLR) {
								isFirstDLR = false;
								setCalender();
							}
							dlr_Calendar.setTime(new Date());
						}
					} catch (Exception e) {
						logger.error(entry.getName() + "[Deliver]", e.fillInStackTrace());
					}
				}
			} else if (recieved_pdu.isResponse()) {
				if (command_id == Data.SUBMIT_SM_RESP) {
					try {
						// if get SUBMIT_RESPONSE
						if (responseQueue == null) {
							responseQueue = new SerialQueue();
						}
						if (trackSubmitResponse == null) {
							trackSubmitResponse = new TrackSubmitResponse(entry, session_id, responseQueue);
							new Thread(trackSubmitResponse, entry.getName() + "_TrackSubmitResponse").start();
						}
						responseQueue.enqueue((SubmitSMResp) recieved_pdu);
					} catch (Exception e) {
						logger.error(entry.getName() + "[Response]", e.fillInStackTrace());
					}
				}
			}
		} catch (Exception es) {
			logger.error(entry.getName(), es.fillInStackTrace());
		}
	}

	private DeliverSM convert(DataSM dataSm) {
		DeliverSM deliver = null;
		try {
			String text = null;
			if (dataSm.getOptional((short) 0x424) != null) {
				text = new String(dataSm.getOptional((short) 0x424).getData().getBuffer());
				text = text.substring(text.indexOf("id:"));
				text = text.substring(text.indexOf("id:"), text.indexOf(" submit date")) + " sub:1 dlvrd:1 "
						+ text.substring(text.indexOf("submit date")) + " text: ";
				deliver = new DeliverSM();
				deliver.setSourceAddr(dataSm.getSourceAddr());
				deliver.setDestAddr(dataSm.getDestAddr());
				deliver.setEsmClass((byte) Data.SM_SMSC_DLV_RCPT_TYPE);
				deliver.setDataCoding((byte) 0x03);
				deliver.setShortMessage(text);
			} else {
				logger.error(entry.getName() + " Invalid DataSm: " + dataSm.debugString());
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		return deliver;
	}

	public void stop() {
		stop = true;
		if (trackSubmitResponse != null) {
			trackSubmitResponse.stop();
		}
		if (trackDeliverResponse != null) {
			trackDeliverResponse.stop();
		}
	}
}
