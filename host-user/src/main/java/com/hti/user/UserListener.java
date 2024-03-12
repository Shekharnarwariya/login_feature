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
package com.hti.user;

import java.io.IOException;
import java.io.InterruptedIOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.user.smpp.UserTCPIPConnection;
import com.logica.smpp.Connection;
import com.logica.smpp.SmppObject;

/**
 * This class accepts client connection on given port. When the connection is accepted, the listener creates an instance of <code>UserSession</code>, generates new <code>PDUProcessor</code> using
 * object derived from <code>PDUProcessorFactory</code>, passes the processor to the smsc session and starts the session as a standalone thread.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version 1.1, 26 Sep 2001
 * @see UserSession
 * @see PDUProcessor
 * @see PDUProcessorFactory
 */
public class UserListener extends SmppObject implements Runnable {
	private Connection serverConn = null;
	private String localip;
	private int port;
	private long acceptTimeout = com.logica.smpp.Data.ACCEPT_TIMEOUT;
	private boolean keepReceiving = true;
	private boolean isReceiving = false;
	private boolean asynchronous = false;
	private Logger logger = LoggerFactory.getLogger("userLogger");

	/**
	 * Construct synchronous listener listening on the given port.
	 *
	 * @param port
	 *            the port to listen on
	 * @see #SMSCListener(int,boolean)
	 */
	public UserListener(String localip, int port) {
		this.localip = localip;
		this.port = port;
	}

	/**
	 * Constructor with control if the listener starts as a separate thread. If <code>asynchronous</code> is true, then the listener is started as a separate thread, i.e. the creating thread can
	 * continue after calling of method <code>start</code>. If it's false, then the caller blocks while the listener does it's work, i.e. listening.
	 *
	 * @param port
	 *            the port to listen on
	 * @param asynchronous
	 *            if the listening will be performed as separate thread
	 * @see #start()
	 */
	public UserListener(String localip, int port, boolean asynchronous) {
		this.localip = localip;
		this.port = port;
		this.asynchronous = asynchronous;
	}

	/**
	 * Starts the listening. If the listener is asynchronous (reccomended), then new thread is created which listens on the port and the <code>start</code> method returns to the caller. Otherwise the
	 * caller is blocked in the start method.
	 *
	 * @see #stop()
	 */
	public synchronized void start() throws IOException {
		if (!isReceiving) {
			if (localip != null) {
				serverConn = new UserTCPIPConnection(localip, port, false);
			} else {
				serverConn = new UserTCPIPConnection(port);
			}
			serverConn.setReceiveTimeout(getAcceptTimeout());
			serverConn.open();
			keepReceiving = true;
			logger.info("Userlistener[" + serverConn.getLocalAddress() + ":" + serverConn.getLocalPort() + "]");
			if (asynchronous) {
				Thread serverThread = new Thread(this, "UserListener");
				serverThread.start();
			} else {
				run();
			}
		} else {
		}
	}

	/**
	 * Signals the listener that it should stop listening and wait until the listener stops. Note that based on the timeout settings it can take some time before this method is finished -- the
	 * listener can be blocked on i/o operation and only after exiting i/o it can detect that it should stop.
	 *
	 * @see #start()
	 */
	public void stop() throws IOException {
		keepReceiving = false;
		serverConn.close();
		logger.info("<-- User Listener[" + localip + ":" + port + "] Stopped -->");
	}

	/**
	 * The actual listening code which is run either from the thread (for async listener) or called from <code>start</code> method (for sync listener). The method can be exited by calling of method
	 * <code>stop</code>.
	 *
	 * @see #start()
	 * @see #stop()
	 */
	public void run() {
		// debug.enter(this, "run of UserListener on port " + port);
		isReceiving = true;
		try {
			while (keepReceiving) {
				// if (com.logica.hti.SMPPAPPLICATION.APPLICATION_STATUS) {
				listen();
				Thread.yield();
			}
		} finally {
			isReceiving = false;
		}
		// debug.exit(this);
	}

	/**
	 * The "one" listen attempt called from <code>run</code> method. The listening is atomicised to allow contoled stopping of the listening. The length of the single listen attempt is defined by
	 * <code>acceptTimeout</code>. If a connection is accepted, then new session is created on this connection, new PDU processor is generated using PDU processor factory and the new session is
	 * started in separate thread.
	 *
	 * @see #run()
	 * @see com.logica.smpp.Connection
	 * @see UserSession
	 * @see PDUProcessor
	 * @see PDUProcessorFactory
	 */
	private void listen() {
		try {
			Connection connection = null;
			serverConn.setReceiveTimeout(getAcceptTimeout());
			connection = serverConn.accept();
			if (connection != null) {
				logger.info("UserListener Connection Accepted[" + connection.getRemoteAddress() + "] On["
						+ connection.getLocalAddress() + ":" + connection.getLocalPort() + "]");
				new Thread(new UserSession(connection), "UserSession").start();
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
			}
		} catch (InterruptedIOException e) {
			// thrown when the timeout expires => it's ok, we just didn't
			// receive anything
			// debug.write("InterruptedIOException accepting, timeout? -> " + e);
		} catch (IOException e) {
			// accept can throw this from various reasons
			// and we don't want to continue then (?)
			// event.write(e, "IOException accepting connection");
			logger.error("UserListener IOError", e);
			keepReceiving = false;
		}
		// debug.exit(Simulator.DSIMD2, this);
	}

	/**
	 * Sets a PDU processor factory to use for generating PDU processors.
	 *
	 * @param processorFactory
	 *            the new PDU processor factory
	 */
	/*
	 * public void setPDUProcessorFactory(PDUProcessorFactory processorFactory) { this.processorFactory = processorFactory; }
	 */
	/**
	 * Sets new timeout for accepting new connection. The listening blocks the for maximum this time, then it exits regardless the connection was acctepted or not.
	 *
	 * @param value
	 *            the new value for accept timeout
	 */
	public void setAcceptTimeout(int value) {
		acceptTimeout = value;
	}

	/**
	 * Returns the current setting of accept timeout.
	 *
	 * @return the current accept timeout
	 * @see #setAcceptTimeout(int)
	 */
	public long getAcceptTimeout() {
		return acceptTimeout;
	}

	public boolean isKeepReceiving() {
		return keepReceiving;
	}
}
