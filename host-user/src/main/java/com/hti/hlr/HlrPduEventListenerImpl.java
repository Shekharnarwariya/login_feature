/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.hlr;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.smpp.Data;
import com.logica.smpp.ServerPDUEvent;
import com.logica.smpp.ServerPDUEventListener;
import com.logica.smpp.pdu.DeliverSM;
import com.logica.smpp.pdu.PDU;
import com.logica.smpp.pdu.SubmitSMResp;
import com.logica.smpp.pdu.ValueNotSetException;
import com.logica.smpp.util.Queue;

/**
 *
 * @author Administrator
 */
public class HlrPduEventListenerImpl implements ServerPDUEventListener {
	private String systemid;
	private Logger logger = LoggerFactory.getLogger("hlrLogger");
	private Map<Integer, String> sequenceMapping;
	private Queue HlrResponeQueue;

	public HlrPduEventListenerImpl(String systemid) {
		this.systemid = systemid;
		this.HlrResponeQueue = GlobalVar.HlrResponeQueue.get(systemid);
		logger.debug("HLR PduEventListener Started For : " + systemid);
	}

	public void setSequenceMapping(Map<Integer, String> sequenceMapping) {
		this.sequenceMapping = sequenceMapping;
	}

	@Override
	public void handleEvent(ServerPDUEvent event) {
		PDU pdu = event.getPDU();
		if (pdu.isResponse()) {
			if (pdu.getCommandId() == Data.SUBMIT_SM_RESP) {
				SubmitSMResp resp = (SubmitSMResp) pdu;
				// if (resp.getCommandStatus() == Data.ESME_ROK) {
				logger.debug(systemid + " [HLR] : " + pdu.debugString());
				if (sequenceMapping.containsKey(resp.getSequenceNumber())) {
					String msgid = sequenceMapping.remove(resp.getSequenceNumber());
					if (resp.getCommandStatus() == Data.ESME_ROK) {
						GlobalVar.HlrResponseMapping.put(resp.getMessageId(), msgid);
					} else {
						logger.error(systemid + " [HLR] : " + pdu.debugString());
					}
					HlrResponeQueue.enqueue(
							new HlrRequest(msgid, Data.SUBMIT_SM_RESP, resp.getCommandStatus(), resp.getMessageId()));
				} else {
					logger.info(systemid + " [HLR]mismatchResp: " + pdu.debugString());
				}
			} else if (pdu.getCommandId() == Data.ENQUIRE_LINK_RESP) {
				logger.debug(systemid + " [HLR] : " + pdu.debugString());
			} else {
				logger.info(systemid + " [HLR] : " + pdu.debugString());
			}
		} else if (pdu.isRequest()) {
			if (pdu.getCommandId() == Data.DELIVER_SM) {
				try {
					event.getConnection().send(((DeliverSM) pdu).getResponse().getData());
				} catch (java.io.IOException io) {
					logger.error(systemid + " <--- HLR Socket Closed While DLR Ack --> ");
				} catch (ValueNotSetException e) {
					logger.error(systemid + " <--- HLR Value Set Error While DLR Ack --> ");
				}
				logger.debug(systemid + " [HLR] : " + pdu.debugString());
				DeliverSM deliver = (DeliverSM) pdu;
				String response_id = null;
				try {
					response_id = deliver.getReceiptedMessageId();
				} catch (ValueNotSetException ex) {
					logger.error(systemid, ex);
				}
				if (response_id != null && GlobalVar.HlrResponseMapping.containsKey(response_id)) {
					try {
						String messageId = GlobalVar.HlrResponseMapping.remove(response_id);
						HlrResponse result = new HlrResponse();
						result.setResponseId(response_id);
						// ------------- parsing result ------------------
						String short_messege = deliver.getShortMessage();
						String status = short_messege
								.substring(short_messege.indexOf("stat:") + 5, short_messege.indexOf("err:")).trim();
						String error = short_messege
								.substring(short_messege.indexOf("err:") + 4, short_messege.indexOf("imsi:")).trim();
						String nnc = short_messege
								.substring(short_messege.indexOf("nnc:") + 4, short_messege.indexOf("mccmnc:")).trim();
						String isPorted = short_messege
								.substring(short_messege.indexOf("isPorted:") + 9, short_messege.indexOf("p_nnc:"))
								.trim();
						String ported_nnc = short_messege
								.substring(short_messege.indexOf("p_nnc:") + 6, short_messege.indexOf("isRoaming:"))
								.trim();
						String isRoaming = short_messege
								.substring(short_messege.indexOf("isRoaming:") + 10, short_messege.indexOf("r_cc:"))
								.trim();
						String roaming_nnc = short_messege
								.substring(short_messege.indexOf("r_nnc:") + 6, short_messege.indexOf("prmnt:")).trim();
						String permanent = short_messege
								.substring(short_messege.indexOf("prmnt:") + 6, short_messege.indexOf("dnd:")).trim();
						String dnd = short_messege
								.substring(short_messege.indexOf("dnd:") + 4, short_messege.indexOf("text:")).trim();
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
						HlrResponeQueue.enqueue(new HlrRequest(messageId, result));
					} catch (Exception e) {
						logger.error(systemid + " " + response_id, e);
					}
				} else {
					logger.info(systemid + " [HLR]mismatchedDLR : " + pdu.debugString());
					deliver.setClientName(systemid);
					GlobalVar.MismatchedWaiting.enqueue(deliver);
				}
				// -----------------------------------------------
			} else {
				logger.debug(systemid + " [HLR] : " + pdu.debugString());
			}
		}
	}

	public void stop() {
		logger.debug("HLR PduEventListener Stopping For : " + systemid);
	}
}
