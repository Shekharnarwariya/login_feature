package com.hti.hlr;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.hlr.smpp.Session;
import com.hti.hlr.smpp.SmscTCPIPConnection;
import com.logica.smpp.Connection;
import com.logica.smpp.Data;
import com.logica.smpp.TimeoutException;
import com.logica.smpp.WrongSessionStateException;
import com.logica.smpp.pdu.BindRequest;
import com.logica.smpp.pdu.BindTransciever;
import com.logica.smpp.pdu.BindTransmitter;
import com.logica.smpp.pdu.PDUException;
import com.logica.smpp.pdu.Response;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.ValueNotSetException;
import com.logica.smpp.pdu.WrongLengthOfStringException;
import com.logica.smpp.util.Queue;

public class LookupSubmission implements Runnable {
	private Logger logger = LoggerFactory.getLogger("hlrLogger");
	private String systemid;
	private String password;
	private Session session;
	private boolean isConnect;
	private long next_enquire = System.currentTimeMillis() + (15 * 1000);
	private Queue lookupQueue;
	// private Queue respQueue;
	private boolean stop;
	private HlrPduEventListenerImpl eventListener;
	private Map<Integer, String> sequenceMapping;
	private String threadId;
	private boolean receiver = false;

	public LookupSubmission(String systemId, String password, Queue lookupQueue, String threadId, boolean receiver) {
		this.systemid = systemId;
		this.password = password;
		this.lookupQueue = lookupQueue;
		this.threadId = threadId;
		this.receiver = receiver;
		this.sequenceMapping = new HashMap<Integer, String>();
		logger.info(systemId + "_" + threadId + " LookupSubmission Starting.Queue: " + lookupQueue.size());
	}

	public void refreshUserEntry(String systemId, String password) {
		this.systemid = systemId;
		this.password = password;
	}

	@Override
	public void run() {
		int loopCounter = 0;
		while (!stop) {
			try {
				checkConnection(loopCounter);
				if (++loopCounter > 10) {
					loopCounter = 0;
				}
				if (isConnect) {
					if (!lookupQueue.isEmpty()) {
						logger.debug(systemid + "_" + threadId + " HlrSubmitQueue: " + lookupQueue.size());
						HlrRequest hlrRequest = null;
						SubmitSM msg = null;
						while (!lookupQueue.isEmpty()) {
							hlrRequest = (HlrRequest) lookupQueue.dequeue();
							msg = new SubmitSM();
							try {
								msg.assignSequenceNumber();
								msg.setSourceAddr((byte) 5, (byte) 0, "HLR");
								msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164,
										hlrRequest.getDestination());
								msg.setShortMessage("HLR Request");
								msg.setRegisteredDelivery((byte) 1);
								msg.setDataCoding((byte) 0);
								sequenceMapping.put(msg.getSequenceNumber(), hlrRequest.getMessageId());
								session.submit(msg);
								LookupDTO lookupDTO = new LookupDTO(hlrRequest.getMessageId(), systemid,
										hlrRequest.getDestination(), "S",
										new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
										msg.getSequenceNumber(), "insert");
								GlobalVar.lookupStatusInsertQueue.enqueue(lookupDTO);
							} catch (WrongLengthOfStringException ex) {
								logger.error(systemid + " -> HlrSubmitSM Creation Error " + ex);
							} catch (Exception ex) {
								logger.error(systemid + " -> HlrSubmit Error " + ex);
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error(systemid + "_" + threadId, e);
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		stopSession();
		logger.info(systemid + "_" + threadId + " LookupSubmission Stopped.Queue: " + lookupQueue.size());
	}

	public void stop() {
		logger.info(systemid + "_" + threadId + " LookupSubmission Stopping.Queue: " + lookupQueue.size());
		stop = true;
	}

	private Session connect() throws WrongLengthOfStringException, ValueNotSetException, IOException,
			WrongSessionStateException, TimeoutException, PDUException {
		int commandStatus = Data.ESME_RBINDFAIL;
		logger.debug(systemid + " connect() " + password);
		Connection connection = new SmscTCPIPConnection(com.hti.util.Constants.HLR_SERVER_IP, com.hti.util.Constants.HLR_SERVER_PORT);
		Session local_session = new Session(connection, systemid);
		BindRequest breq = null;
		if (receiver) {
			breq = new BindTransciever();
		} else {
			breq = new BindTransmitter();
		}
		breq.setSystemId(systemid);
		breq.setPassword(password);
		breq.setInterfaceVersion(Data.SMPP_V34);
		breq.setSystemType("BRD_SMPP");
		eventListener = new HlrPduEventListenerImpl(systemid);
		Response response = local_session.bind(breq, eventListener);
		if (response != null) {
			commandStatus = response.getCommandStatus();
			logger.debug(systemid + " " + response.debugString());
			if (commandStatus == Data.ESME_ROK) {
				eventListener.setSequenceMapping(sequenceMapping);
				logger.info(systemid + "_" + threadId + " HLR Connected : " + response.debugString());
				return local_session;
			} else {
				logger.error(systemid + "_" + threadId + " HLR Connection Failed : " + response.debugString());
				return null;
			}
		} else {
			logger.error(systemid + "_" + threadId + " HLR Connection Failed < No Response >");
			return null;
		}
	}

	private void checkConnection(int loopCounter) {
		if (loopCounter == 0) {
			// logger.info(systemid + "_" + threadId + " Checking Hlr Connection: " + isConnect);
			if (isConnect) {
				if (next_enquire <= System.currentTimeMillis()) {
					logger.debug(systemid + "_" + threadId + " <- Enquire Hlr Session -> ");
					try {
						session.enquireLink();
						next_enquire = System.currentTimeMillis() + (15 * 1000);
					} catch (Exception ex) {
						logger.error(systemid + "_" + threadId + " -> HLR Server Connection Error: " + ex);
						isConnect = false;
						session = null;
					}
				}
			}
			if (!isConnect) {
				if (session != null) {
					if (session.isBound()) {
						isConnect = true;
					}
				}
				if (!isConnect) {
					try {
						session = connect();
						if (session != null) {
							isConnect = true;
						}
					} catch (Exception ex) {
						logger.error(systemid + "_" + threadId + " -> HLR Server Connection Error: " + ex);
					}
				}
			}
		}
	}

	private void stopSession() {
		logger.info(systemid + "_" + threadId + " HLR Session Closing");
		try {
			logger.info(systemid + "_" + threadId + " HLR: " + session.unbind().debugString());
		} catch (Exception ex) {
			logger.error(systemid + "_" + threadId + " -> HLR Session Closing Error: " + ex);
		}
	}
}
