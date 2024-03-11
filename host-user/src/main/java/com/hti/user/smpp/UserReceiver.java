/*
 * Copyright (c) 1996-2001
 * Logica Mobile Networks Limited
 * All rights reserved.
 *
 * This software is distributed under Logica Open Source License Version 1.0
 * ("Licence Agreement"). You shall use it and distribute only in accordance
 * with the terms of the License Agreement.
 *
 */
package com.hti.user.smpp;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.smpp.Connection;
import com.logica.smpp.Data;
import com.logica.smpp.ReceiverBase;
import com.logica.smpp.ServerPDUEventListener;
import com.logica.smpp.TimeoutException;
import com.logica.smpp.pdu.GenericNack;
import com.logica.smpp.pdu.InvalidPDUException;
import com.logica.smpp.pdu.PDU;
import com.logica.smpp.pdu.PDUException;
import com.logica.smpp.pdu.Request;
import com.logica.smpp.pdu.Response;
import com.logica.smpp.pdu.UnknownCommandIdException;
import com.logica.smpp.util.Queue;
import com.logica.smpp.util.Unprocessed;

/**
 * <code>Receiver</code> is class used for receiving PDUs from SMSC. It can be used two ways: it has methods for synchronous (blocking) receiving of PDUs and as it is derived from
 * <code>ReceiverBase</code> whic on turn is derived from <code>ProcessingThread</code> class, it can also receive PDUs on background and puts them into a queue.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version 1.2, 1 Oct 2001
 * @see ReceiverBase
 * @see Connection
 * @see Session
 * @see Queue
 */
/*
 * 13-07-01 ticp@logica.com start(), stop(), setQueueWaitTimeout() & getQueueWaitTimeout() made not synchronized; receive(long) & receive(PDU) made synchronized so the receiver no longer locks up
 * 13-07-01 ticp@logica.com bug fixed in tryReceivePDU which caused that the PDUs were never removed from the queue - now dequeue(expected) is now used instead of find(expected) 13-07-01
 * ticp@logica.com loads of debug lines corrected; some added 08-08-01 ticp@logica.com added support for Session's asynchronous processing capability 26-09-01 ticp@logica.com debug code categorized to
 * groups 01-10-01 ticp@logica.com added function getThreadName for ProcessingThread thread name initialisation. 02-10-01 ticp@logica.com instead of importing full packages only the used classes are
 * iported
 */
public class UserReceiver extends ReceiverBase {
	/*
	 * private int interCount = 0; private long receivedCount = 0;
	 */
	private Logger logger = LoggerFactory.getLogger("userLogger");
	private Logger pdulogger = LoggerFactory.getLogger("pduLogger");
	private Logger submitlogger = LoggerFactory.getLogger("submitLogger");
	private Logger dlrlogger = LoggerFactory.getLogger("dlrLogger");
	private String systemId;
	private long last_receive = 0;
	private boolean isConnect = true;
	private String sessionId;
	private String mode;
	private boolean pduLog;
	private boolean queueFull;

	public void setPduLog(boolean pduLog) {
		this.pduLog = pduLog;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	/**
	 * Name of the thread created when starting the <code>ProcessingThread</code>.
	 *
	 * @see com.logica.smpp.util.ProcessingThread#start()
	 * @see com.logica.smpp.util.ProcessingThread#generateIndexedThreadName()
	 */
	public String RECEIVER_THREAD_NAME = "UserReceiver";
	/**
	 * The correspondent transmitter for transmitting PDUs. It's used for sending of generic negative acknowledges, if necessary. It is passed to the receiver as a parameter during construction.
	 *
	 * @see #receiveAsync()
	 */
	private UserTransmitter transmitter = null;
	/**
	 * The network connection which is used for receiving data. It is passed to the receiver as a parameter during construction.
	 */
	private Connection connection = null;
	/**
	 * The queue which holds the received PDUs. As the PDUs are received in asynchronnous manner, they are stored to a queue from which they can be get using method <code>receive</code>. PDUs are
	 * stored to the queue if and only if the <code>Receiver</code> is started as a separate thread using method <code>start</code>.
	 *
	 * @see #receive(long)
	 * @see #receive(PDU)
	 * @see #start()
	 * @see ReceiverBase#start()
	 */
	// private String temp_queue_folder = null;
	private Queue secondryQueue = new Queue(com.hti.util.GlobalVars.RECEIVER_QUEUE_SIZE);
	private Queue primaryQueue = new Queue();
	/**
	 * This timeout specifies for how long will go the receiving into wait if the PDU (expected or any) isn't in the <code>pduQueue</code> yet. After that the queue is probed again (etc.) until
	 * receiving timeout expires or the PDU is received.
	 *
	 * @see #tryReceivePDU(Connection,PDU)
	 */
	private long queueWaitTimeout = Data.QUEUE_TIMEOUT;
	/**
	 * Indication if the <code>Receiver</code> is receiving on background as an extra thread.
	 *
	 * @see #start()
	 * @see #tryReceivePDU(Connection,PDU)
	 */
	private boolean receiver = false;
	/**
	 * This object holds data received from connection which aren't complete PDU yet. If this situation occurs, it's likely that the data will be received the next time when another attempt to receive
	 * data from the connection occurs. Its used in <code>ReceiverBase</code>'s <code>receivePDUFromConnection</code> method.
	 *
	 * @see ReceiverBase#receivePDUFromConnection(Connection,Unprocessed)
	 */
	private Unprocessed unprocessed = new Unprocessed();
	/**
	 * Indicates that the sending of PDUs to the SMSC is asynchronous, i.e. the session doesn't wait for a response to the sent request as well as the <code>receive</code> functions will return null
	 * as all received PDUs are passed to the <code>pduListener</code> object in the <code>receiver</code>
	 *
	 * @see #pduListener
	 * @see #setServerPDUEventListener(ServerPDUEventListener)
	 */
	private boolean asynchronous = false;

	/**
	 * This constructor sets the connection to receive the messages from and a transmitter for sending generic negative acknowledges if necessary.
	 *
	 * @param transmitter
	 *            the transmitter to use for sending <code>GenericNack</code>
	 * @param connection
	 *            the connection to use for receiving and transmitting
	 * @see UserTransmitter
	 * @see Connection
	 * @see GenericNack
	 */
	public UserReceiver(UserTransmitter transmitter, Connection connection) {
		this.transmitter = transmitter;
		this.connection = connection;
	}

	/**
	 * Returns if the receiver receives PDUs on background as an extra thread.
	 *
	 * @see #receiver
	 */
	public boolean isReceiver() {
		return receiver;
	}

	/**
	 * Resets unprocessed data and starts receiving on the background.
	 *
	 * @see ReceiverBase#start()
	 */
	public void start() {
		try {
			receiver = true;
			unprocessed.reset();
			super.start();
		} catch (Exception e) {
			logger.error("start(" + systemId + ")", e);
		}
		// debug.write(DRXTX, "Receiver started");
	}

	/**
	 * Stops receiving on the background.
	 *
	 * @see ReceiverBase#stop()
	 */
	public void stop() {
		// debug.write(DRXTX, "Receiver stoping");
		if (isReceiver()) {
			super.stop();
			receiver = false;
		}
	}

	/**
	 * This method receives a PDU or returns PDU received on background, if there is any. It tries to receive a PDU for the specified timeout. If the receiver is asynchronous, then no attempt to
	 * receive a PDU and <code>null</code> is returned. The function calls are nested as follows:<br>
	 * <ul>
	 * <li>No background receiver thread<br>
	 * <code>
	 *       Receiver.receive(long)<br>
	 * ReceiverBase.tryReceivePDUWithTimeout(Connection,PDU,long)<br>
	 * Receiver.tryReceivePDU(Connection,PDU)<br>
	 * ReceiverBase.receivePDUFromConnection<br>
	 * Connection.receive()</code>
	 * <li>Has background receiver thread<br>
	 * <code>
	 *       Receiver.receive(long)<br>
	 * ReceiverBase.tryReceivePDUWithTimeout(Connection,PDU,long)<br>
	 * Receiver.tryReceivePDU(Connection,PDU)<br>
	 * Queue.dequeue(PDU)</code><br>
	 * and the ReceiverBase.run() function which actually receives the PDUs and stores them to a queue looks as follows:<br>
	 * <code>
	 *       ReceiverBase.run()<br>
	 * Receiver.receiveAsync()<br>
	 * ReceiverBase.receivePDUFromConnection<br>
	 * Connection.receive()</code>
	 *
	 * @param timeout
	 *            for how long is tried to receive a PDU
	 * @return the received PDU or null if none received for the spec. timeout
	 * @exception IOException
	 *                exception during communication
	 * @exception PDUException
	 *                incorrect format of PDU
	 * @exception TimeoutException
	 *                rest of PDU not received for too long time
	 * @exception UnknownCommandIdException
	 *                PDU with unknown id was received
	 * @see ReceiverBase#tryReceivePDUWithTimeout(Connection,PDU,long)
	 */
	public synchronized PDU receive(long timeout)
			throws UnknownCommandIdException, TimeoutException, PDUException, IOException {
		PDU pdu = null;
		// logger.info(systemId + " Trying to receive PDU timeout");
		if (!asynchronous) {
			pdu = tryReceivePDUWithTimeout(connection, null, timeout);
		}
		return pdu;
	}

	public synchronized PDU receive() throws UnknownCommandIdException, TimeoutException, PDUException, IOException {
		if (!isConnect) {
			throw new IOException("User Connection Breaked");
		}
		PDU pdu = null;
		// logger.info(systemId + " Trying to receive PDU timeout");
		if (!primaryQueue.isEmpty()) {
			pdu = (PDU) primaryQueue.dequeue();
		} else {
			if (!secondryQueue.isEmpty()) {
				// pdulogger.info(systemId + " Queue: " + secondryQueue.size());
				pdu = (PDU) secondryQueue.dequeue();
				// pdulogger.info(systemId + " dequeue: " + pdu.getCommandId() + " " + pdu.getSequenceNumber());
			} else {
				synchronized (secondryQueue) {
					try {
						secondryQueue.wait(getQueueWaitTimeout());
					} catch (InterruptedException e) {
						// submitlogger.info("<-- Interrupted Queue Wait -->");
					}
				}
				// submitlogger.info("<-- Checking Queue After Wait -> ");
				if (!secondryQueue.isEmpty()) {
					pdu = (PDU) secondryQueue.dequeue();
					// submitlogger.info("<-- Found Pdu After Wait: " + pdu.debugString());
				}
			}
		}
		return pdu;
	}

	/**
	 * Called from session to receive a response for previously sent request.
	 *
	 * @param expectedPDU
	 *            the template for expected PDU; the PDU returned must have the same sequence number
	 * @return the received PDU or null if none
	 * @see ReceiverBase#tryReceivePDUWithTimeout(Connection,PDU,long)
	 */
	public synchronized PDU receive(PDU expectedPDU)
			throws UnknownCommandIdException, TimeoutException, PDUException, IOException {
		PDU pdu = null;
		if (!asynchronous) {
			pdu = tryReceivePDUWithTimeout(connection, expectedPDU);
		}
		return pdu;
	}

	/**
	 * This method tries to receive one PDU from the connection. It is called in cycle from <code>tryReceivePDUWithTimeout</code> until timeout expires. <code>tryReceivePDUWithTimeout</code> is called
	 * either from <code>receiveAsync</code> as asynchronous receive on background or from <code>receive</code> as synchronous receive. It either gets pdu from the queue or tries to receive it from
	 * connection using <code>receivePDUFromConnection</code> depending on the value of the <code>receiver</code> flag. The method checks if the actualy received PUD is equal to
	 * <code>expectedPDU</code>.
	 *
	 * @exception IOException
	 *                exception during communication
	 * @exception PDUException
	 *                incorrect format of PDU
	 * @exception TimeoutException
	 *                rest of PDU not received for too long time
	 * @exception UnknownCommandIdException
	 *                PDU with unknown id was received
	 * @see ReceiverBase#tryReceivePDUWithTimeout(Connection,PDU,long)
	 * @see #receiveAsync()
	 * @see ReceiverBase#run()
	 */
	protected PDU tryReceivePDU(Connection connection, PDU expectedPDU)
			throws UnknownCommandIdException, TimeoutException, PDUException, IOException {
		PDU pdu = null;
		if (receiver) {
			if (!isConnect) {
				throw new IOException("User Connection Breaked");
			}
			if (expectedPDU == null) { // i.e. any pdu is acceptable
				if (!primaryQueue.isEmpty()) {
					pdu = (PDU) primaryQueue.dequeue();
				} else {
					if (!secondryQueue.isEmpty()) {
						pdu = (PDU) secondryQueue.dequeue();
					}
				}
			}
			if (pdu == null) {
				// wait sometime
				synchronized (primaryQueue) {
					try {
						primaryQueue.wait(getQueueWaitTimeout());
					} catch (InterruptedException e) {
					}
				}
			}
		} else {
			pdu = receivePDUFromConnection(connection, unprocessed);
			if (pdu != null) {
				if ((expectedPDU == null) || !pdu.equals(expectedPDU)) {
					// debug.write(DRXTX, "This is not the pdu we expect, processing" + pdu.debugString());
					enqueue(pdu);
					pdu = null;
				}
			}
		}
		return pdu;
	}

	/**
	 * This method receives a PDU from connection and stores it into <code>pduQueue</code>. It's called from the <code>ReceiverBase</code>'s p<code>process</code> method which is called in loop from
	 * <code>ProcessingThread</code>'s <code>run</code> method.
	 * <p>
	 * If an exception occurs during receiving, depending on type of the exception this method either just reports the exception to debug & event objects or stops processing to indicate that it isn't
	 * able to process the exception. The function <code>setTermException</code> is then called with the caught exception.
	 *
	 * @see ReceiverBase#run()
	 */
	protected void receiveAsync() {
		PDU pdu = null;
		try {
			pdu = receivePDUFromConnection(connection, unprocessed);
		} catch (InvalidPDUException e) {
			PDU expdu = e.getPDU();
			int seqNr = expdu == null ? 0 : expdu.getSequenceNumber();
			sendGenericNack(Data.ESME_RINVMSGLEN, seqNr);
			logger.error(systemId + "[" + seqNr + "]" + " <-- Invalid PDU Received.Sending GenerickNack --> ");
		} catch (UnknownCommandIdException e) {
			// if received unknown pdu, we must send generic nack
			// event.write(e, "Receiver.receiveAsync(): Unknown command id.");
			sendGenericNack(Data.ESME_RINVCMDID, e.getSequenceNumber());
			logger.error(
					systemId + "[" + e.getSequenceNumber() + "]" + " <-- Unknown Command ID.Sending GenerickNack --> ");
		} catch (TimeoutException e) {
			// too long had unprocessed data
			// debug.write(DRXTX, "Receiver.receiveAsync() too long had an uncomplete message.");
		} catch (PDUException e) {
			// something wrong with the PDU
			// event.write(e, "Receiver.receiveAsync()");
		} catch (Exception e) {
			// don't know what happen, let's end the show
			// event.write(e, "Receiver.receiveAsync()");
			// e.printStackTrace();
			stopProcessing(e);
			try {
				connection.close();
			} catch (IOException w) {
				// w.printStackTrace();
			}
			isConnect = false;
			logger.error(systemId + " <-- User Connection Breaked<" + e.getMessage() + "> --> Last Received: "
					+ new SimpleDateFormat("HH:mm:ss").format(new Date(last_receive)));
			// System.out.println("<--- User Recieving Socket Closing -->");
			// System.out.println("");
		}
		if (pdu != null) {
			/*
			 * if (pdu.isRequest()) { if (pdu.getCommandId() == Data.SUBMIT_SM) { GlobalAppVars.pduLogQueue.enqueue(systemId + ":" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + " :Request<"
			 * + connection.getIpAddress() + "> " + ": (submit: (seq: " + pdu.getSequenceNumber() + ")"); } else { GlobalAppVars.pduLogQueue .enqueue(systemId + ":" + new
			 * SimpleDateFormat("HH:mm:ss").format(new Date()) + " :Request<" + connection.getIpAddress() + ">: (seq: " + pdu.getSequenceNumber() + ")" + pdu.debugString()); } }
			 */
			last_receive = System.currentTimeMillis();
			enqueue(pdu);
		}
	}

	/**
	 * Puts the <code>pdu</code> into the <code>pduQueue</code>.
	 *
	 * @param pdu
	 *            the PDU to put into the queue
	 * @see Queue
	 */
	private void enqueue(PDU pdu) {
		try {
			if (pdu.getCommandId() == Data.BIND_RECEIVER || pdu.getCommandId() == Data.BIND_TRANSCEIVER
					|| pdu.getCommandId() == Data.BIND_TRANSMITTER) {
				pdulogger.info("[" + connection.getRemoteAddress() + "] BindRequest:"
						+ ((com.logica.smpp.pdu.BindRequest) pdu).getSystemId() + " seq:"
						+ ((com.logica.smpp.pdu.BindRequest) pdu).getSequenceNumber());
			}
			if (pduLog) {
				if (pdu.getCommandId() == Data.BIND_RECEIVER || pdu.getCommandId() == Data.BIND_TRANSCEIVER
						|| pdu.getCommandId() == Data.BIND_TRANSMITTER) {
					// pdulogger.info("[" + connection.getRemoteAddress() + "] " + pdu.debugString());
				} else if (pdu.getCommandId() == Data.SUBMIT_SM) {
					submitlogger.info(systemId + " " + sessionId + "-" + mode + " [" + connection.getLocalAddress()
							+ " : " + connection.getRemoteAddress() + "]: " + pdu.debugString());
				} else if (pdu.getCommandId() == Data.DELIVER_SM_RESP) {
					dlrlogger.info(systemId + " " + sessionId + "-" + mode + " [" + connection.getLocalAddress() + " : "
							+ connection.getRemoteAddress() + "]: " + pdu.debugString());
				} else {
					pdulogger.info(systemId + " " + sessionId + "-" + mode + " [" + connection.getLocalAddress() + " : "
							+ connection.getRemoteAddress() + "]: " + pdu.debugString());
				}
			}
			if (Data.BIND_RECEIVER == pdu.getCommandId() || Data.BIND_TRANSCEIVER == pdu.getCommandId()
					|| Data.BIND_TRANSMITTER == pdu.getCommandId() || Data.ENQUIRE_LINK == pdu.getCommandId()
					|| Data.UNBIND_RESP == pdu.getCommandId() || Data.UNBIND == pdu.getCommandId()) {
				primaryQueue.enqueue(pdu);
			} else {
				if (pdu.isRequest() && pdu.getCommandId() == Data.SUBMIT_SM) {
					if (queueFull) {
						if (((int) (((double) secondryQueue.size()
								/ (double) com.hti.util.GlobalVars.RECEIVER_QUEUE_SIZE) * 100)) >= 90) {
							// Return throttled error
							errorRespond((Request) pdu);
						} else {
							queueFull = false;
						}
					}
					if (!queueFull) {
						synchronized (secondryQueue) {
							try {
								secondryQueue.enqueue(pdu);
								secondryQueue.notify();
							} catch (IndexOutOfBoundsException e) {
								queueFull = true;
								// Return throttled error
								errorRespond((Request) pdu);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(systemId + "-" + sessionId + "-" + mode, e.getMessage());
		}
	}

	/**
	 * Sends <code>GenericNack</code> PDU via transmitter if there is one. The <code>GenericNack</code> is sent in case that the message is corrupted or has unknown command id. If the sending of
	 * <code>GenericNack</code> fails, this method calls <code>stopProcessing</code> and thus stops the receiving thread.
	 *
	 * @param commandStatus
	 *            the error code
	 * @param sequenceNumber
	 *            the sequence number of received wrong PDU
	 * @see GenericNack
	 * @see UserTransmitter
	 */
	private void sendGenericNack(int commandStatus, int sequenceNumber) {
		if (transmitter != null) {
			try {
				GenericNack gnack = new GenericNack(commandStatus, sequenceNumber);
				transmitter.send(gnack);
			} catch (IOException gnacke) {
				// event.write(gnacke, "Receiver.run(): IOException sending generic_nack.");
			} catch (Exception gnacke) {
				// event.write(gnacke, "Receiver.run(): an exception sending generic_nack.");
				stopProcessing(gnacke);
			}
		}
	}

	private void errorRespond(Request request) {
		System.out.println("< " + systemId + "-" + sessionId + "-" + mode + " > Submit Error < ESME_RTHROTTLED > "
				+ secondryQueue.size());
		if (transmitter != null) {
			try {
				Response response = request.getResponse();
				response.setCommandStatus(Data.ESME_RTHROTTLED);
				transmitter.send(response);
			} catch (Exception ex) {
				logger.error(systemId + "-" + sessionId + "-" + mode, ex.getMessage());
			}
		}
	}

	/**
	 * Sets queue waiting timeout.
	 *
	 * @param timeout
	 *            the new queue timeout
	 * @see #queueWaitTimeout
	 */
	public void setQueueWaitTimeout(long timeout) {
		queueWaitTimeout = timeout;
	}

	/**
	 * Returns current queue waiting timeout.
	 *
	 * @return the current queue timeout
	 * @see #queueWaitTimeout
	 */
	public long getQueueWaitTimeout() {
		return queueWaitTimeout;
	}

	// ProcessingThread's getThreadName override
	public String getThreadName() {
		logger.debug("UserReceiver.getThreadName()" + RECEIVER_THREAD_NAME);
		return RECEIVER_THREAD_NAME;
	}
	/**
	 * Convert object to byte array
	 * 
	 * @param object
	 * @return
	 */
	/*
	 * private byte[] objectToByteArray(Serializable object) { return SerializationUtils.serialize(object); }
	 */
	/**
	 * Convert byte array to object
	 * 
	 * @param bytes
	 * @return
	 */
	/*
	 * private Object byteArrayToObject(byte[] bytes) { return SerializationUtils.deserialize(bytes); }
	 */
	/*
	 * public void setConnection(Connection getconnection){ connection=getconnection; }
	 */
}
