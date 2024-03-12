/*
 * SmscConnection.java
 *
 * Created on 06 April 2004, 17:16
 */
package com.hti.smsc;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.objects.PriorityQueue;
import com.hti.objects.RoutePDU;
import com.hti.objects.SmscInObj;
import com.hti.smsc.dto.SmscEntry;
import com.hti.util.Constants;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;
import com.logica.smpp.Data;
import com.logica.smpp.Session;
import com.logica.smpp.TCPIPConnection;
import com.logica.smpp.pdu.AddressRange;
import com.logica.smpp.pdu.BindReceiver;
import com.logica.smpp.pdu.BindRequest;
import com.logica.smpp.pdu.BindResponse;
import com.logica.smpp.pdu.BindTransciever;
import com.logica.smpp.pdu.BindTransmitter;
import com.logica.smpp.pdu.EnquireLink;
import com.logica.smpp.pdu.UnbindResp;

//*****************************************
/**
 *
 * @author administrator Thread Responsible for Making Connection with SMSC and SendHtiPdu Thread Rebind if Connection Break,also SendHtiPDU Thread if get any Exception set the connection status Flag
 *         of SmscStatus.SMSCConnectionFlagHastable
 */
public class SmscConnection implements Runnable {
	private Session session;
	private boolean bound;
	private String ipAddress;
	private int port;
	private String primaryIpAddress;
	private int primaryPort;
	private String backupIpAddress;
	private String backupIpAddress1;
	private int backupPort;
	private int backupPort1;
	private SmscEntry smscEntry;
	private SendHtiPDU send_hti_pdu;
	private final boolean asynchronous = true;
	private HtiPDUEventListner pduListener;
	private PriorityQueue SMSC_IN;
	private TCPIPConnection connection;
	private int sessionId;
	private Logger logger = LoggerFactory.getLogger(SmscConnection.class);
	private Logger pdulogger = LoggerFactory.getLogger("pduLogger");
	private int retryCount;
	private Thread thread;
	private boolean stop;
	private boolean terminated;

	/**
	 * Creates a new instance of SmscConnection
	 */
	public SmscConnection(SmscEntry smscEntry) {
		setSmscEntry(smscEntry);
	}

	public SmscEntry getSmscEntry() {
		return smscEntry;
	}

	public void setSmscEntry(SmscEntry smscEntry) {
		this.smscEntry = smscEntry;
		// this.smscId = smscEntry.getId();
		this.ipAddress = smscEntry.getIp();
		this.port = smscEntry.getPort();
		this.primaryIpAddress = smscEntry.getIp();
		this.primaryPort = smscEntry.getPort();
		this.backupIpAddress = smscEntry.getBackupIp();
		this.backupPort = smscEntry.getBackupPort();
		this.backupIpAddress1 = smscEntry.getBackupIp1();
		this.backupPort1 = smscEntry.getBackupPort1();
		logger.debug(smscEntry.getName() + "-> Ip: " + ipAddress + " backup:" + backupIpAddress + " port:" + port
				+ " bindOption:" + smscEntry.getBindMode() + " pass:" + smscEntry.getPassword() + " type:"
				+ smscEntry.getSystemType());
		if (send_hti_pdu != null) {
			send_hti_pdu.setSmscEntry(smscEntry);
		}
	}

	public void resetLoopingRule() {
		if (send_hti_pdu != null) {
			send_hti_pdu.resetLoopingRule();
		}
	}
	
	/*public void resetDestSourceContentLoopingRule() {
		if (send_hti_pdu != null) {
			send_hti_pdu.resetDestSourceContentLoopingRule();
		}
	}*/

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
		this.thread.setName("SmscConnection-" + smscEntry.getName());
	}

	public SmscConnection() {
	}

	private void bind() {
		logger.debug("bind() " + smscEntry.getName());
		if (bound) {
			logger.info("Already bound, unbind first.");
		} else {
			int command_status = Data.ESME_ROK;
			try {
				BindRequest request = null;
				BindResponse response = null;
				// String syncMode = (asynchronous ? "a" : "s");
				if (smscEntry.getBindMode().compareToIgnoreCase("tr") == 0) {
					request = new BindTransciever();
				} else if (smscEntry.getBindMode().compareToIgnoreCase("t") == 0) {
					request = new BindTransmitter();
				} else if (smscEntry.getBindMode().compareToIgnoreCase("r") == 0) {
					request = new BindReceiver();
				} else {
					request = new BindTransciever();
				}
				connection = new TCPIPConnection(ipAddress, port);
				// connection.setReceiveTimeout(20 * 1000);
				session = new Session(connection, smscEntry.getName());
				request.setSystemId(smscEntry.getSystemId());
				request.setPassword(smscEntry.getPassword());
				request.setSystemType(smscEntry.getSystemType());
				request.setInterfaceVersion(Data.SMPP_V34);
				request.setAddressRange(new AddressRange());
				if (asynchronous) {
					if (pduListener == null) {
						pduListener = new HtiPDUEventListner(smscEntry, sessionId);// thread_count
					}
					response = session.bind(request, pduListener);
				} else {
					response = session.bind(request);
				}
				if (response != null) {
					pdulogger.info(smscEntry.getName() + ": " + response.debugString());
					if (response.getCommandStatus() == Data.ESME_ROK) {
						System.out.println("*************************************");
						logger.info("Bound to " + smscEntry.getName() + " SMSC as " + getMode(smscEntry.getBindMode()));
						System.out.println("*************************************");
						// fileUtil.writeLog(AccumulatorBox.SmscConnectionLog, smscEntry.getName() + " : Connected");
						if (GlobalCache.SmscDisconnection.containsKey(smscEntry.getName())) {
							GlobalCache.SmscDisconnection.remove(smscEntry.getName());
						}
						if (smscEntry.getBindMode().compareToIgnoreCase("r") != 0) {
							GlobalCache.SMSCConnectionStatus.put(smscEntry.getName(), true);
							GlobalCache.SmscConnectionSet.add(smscEntry.getName());
						}
						bound = true;
					} else {
						command_status = response.getCommandStatus();
						logger.error(smscEntry.getName() + " Negative Bind Response -> " + response.getCommandStatus()
								+ " : " + displayStatus(response.getCommandStatus())); // done by rajniprabha09032007
					}
				} else {
					logger.error(smscEntry.getName() + " <- No Response For Bind Request ->");
					command_status = Data.ESME_RBINDFAIL;
				}
			} catch (Exception e) {
				if (GlobalCache.SMSCConnectionStatus.containsKey(smscEntry.getName())) {
					GlobalCache.SMSCConnectionStatus.put(smscEntry.getName(), false);
				}
				if (GlobalCache.SmscConnectionSet.contains(smscEntry.getName())) {
					GlobalCache.SmscConnectionSet.remove(smscEntry.getName());
				}
				System.out.println(smscEntry.getName() + " Bind failed <" + e + ">");
				command_status = Data.ESME_RBINDFAIL;
			}
			changeBindStatus(bound, command_status);
			if (!bound) {
				stopListener();
			}
		}
	}

	private void stopListener() {
		if (pduListener != null) {
			pduListener.stop();
			pduListener = null;
		}
	}

	public void setSMSC_IN(PriorityQueue getSMSC_IN) {
		SMSC_IN = getSMSC_IN;
	}

	private String getMode(String get) {
		String toReturn = "Tranciever Mode";
		if (get.compareToIgnoreCase("t") == 0) {
			toReturn = "Transmiter Mode";
		} else if (get.compareToIgnoreCase("r") == 0) {
			toReturn = "Reciever Mode";
		} else if (get.compareToIgnoreCase("tr") == 0) {
			toReturn = "Tranciever Mode";
		}
		return toReturn;
	}

	@Override
	public void run() {
		logger.info("<-- SmscConnection Started For: " + smscEntry.getName() + " -->");
		while (!stop) {
			terminated = false;
			if (bound) {
				if (smscEntry.getBindMode().compareToIgnoreCase("r") != 0) {
					if (send_hti_pdu == null) {
						send_hti_pdu = new SendHtiPDU(session, SMSC_IN, smscEntry);
						send_hti_pdu.start();
					}
					if (send_hti_pdu.getTermException() != null) {
						bound = false;
						if (send_hti_pdu != null) {
							send_hti_pdu.stop();
						}
						send_hti_pdu = null;
						// stop();
						if (GlobalCache.SMSCConnectionStatus.containsKey(smscEntry.getName())) {
							GlobalCache.SMSCConnectionStatus.put(smscEntry.getName(), false);
						}
						GlobalCache.SmscConnectionSet.remove(smscEntry.getName());
						stopListener();
						changeBindStatus(false, Data.ESME_ROK);
					} else {
						try {
							Thread.sleep(3 * 1000);
						} catch (InterruptedException ie) {
							logger.debug(smscEntry.getName() + "<" + smscEntry.getBindMode() + "> Exited From Sleep ");
						}
						if (pduListener != null) {
							if (System.currentTimeMillis() > pduListener.lastReceivedOn + (60 * 1000)) {
								logger.warn(smscEntry.getName() + "<" + smscEntry.getBindMode()
										+ "> Packet Not Received since long time.");
								clear();
							}
						}
					}
				} else {
					if (sendEnquireLink() != null) {
						bound = false;
						changeBindStatus(false, Data.ESME_ROK);
						// logger.info("<-- Connection Breaked --> " + smscEntry.getName() + " <" + NickName + " >");
					} else {
						try {
							Thread.sleep(Constants.interEnquireTime * 1000);
						} catch (InterruptedException ie) {
							logger.debug(smscEntry.getName() + "<" + smscEntry.getBindMode()
									+ "> Exited From Enquire Sleep ");
						}
					}
				}
			} else {
				System.out.println("Trying To Make Connection  With SMSC  " + smscEntry.getName() + " <" + ipAddress
						+ ":" + port + " >");
				bind();
				/*
				 * synchronized (SmscConnection.class) {
				 * 
				 * try { Thread.sleep(100); } catch (InterruptedException ie) { } }
				 */
				if (!bound) {
					if (++retryCount >= 60) {
						releaseQueue();
						retryCount = 0;
					}
					if (backupIpAddress != null && backupIpAddress.length() > 0) {
						if (ipAddress.equalsIgnoreCase(primaryIpAddress)) {
							ipAddress = backupIpAddress;
							if (backupPort > 0) {
								port = backupPort;
							}
						} else {
							if (ipAddress.equalsIgnoreCase(backupIpAddress)) {
								if (backupIpAddress1 != null && backupIpAddress1.length() > 0) {
									ipAddress = backupIpAddress1;
									if (backupPort1 > 0) {
										port = backupPort1;
									}
								} else {
									ipAddress = primaryIpAddress;
									port = primaryPort;
								}
							} else {
								ipAddress = primaryIpAddress;
								port = primaryPort;
							}
						}
					}
					try {
						Thread.sleep(5 * 1000);
					} catch (InterruptedException ie) {
						logger.debug(smscEntry.getName() + "<" + smscEntry.getBindMode() + "> Exited From Sleep ");
					}
				} else {
					retryCount = 0;
				}
			}
		}
		clear();
		logger.info("<-- SmscConnection Stopped For: " + smscEntry.getName() + " -->");
	}
	/*
	 * public boolean isActive() { return active; }
	 */

	/*
	 * public void setActive(boolean active) { this.active = active; }
	 */
	public void stop() {
		logger.info("Got Command To Stop Connection: " + smscEntry.getName());
		stop = true;
		thread.interrupt();
	}

	public void clear() {
		if (send_hti_pdu != null) {
			send_hti_pdu.stop();
		}
		stopListener();
		GlobalCache.SMSCConnectionStatus.remove(smscEntry.getName());
		GlobalCache.SmscConnectionSet.remove(smscEntry.getName());
		// GlobalCache.ActiveRoutes.remove(smscEntry.getName());
		changeBindStatus(false, Data.ESME_ROK);
		if (bound) {
			try {
				UnbindResp unbind_response = session.unbind();
				if (unbind_response != null && unbind_response.getCommandStatus() == Data.ESME_ROK) {
					logger.info("<-- Unbind To Smsc: " + smscEntry.getName() + " -->");
				} else {
					logger.warn("<-- Unbind Response Not Recieved from " + smscEntry.getName() + " --> ");
				}
				// fileUtil.writeLog(AccumulatorBox.SmscConnectionLog, smscEntry.getName() + " : Unbind");
			} catch (Exception e) {
				logger.error("Unbind Error <" + smscEntry.getName() + "> -->" + e);
			}
			if (GlobalCache.SmscDisconnectionAlert.contains(smscEntry.getName())) {
				if (!GlobalCache.SmscDisconnection.containsKey(smscEntry.getName())) {
					GlobalCache.SmscDisconnection.put(smscEntry.getName(), System.currentTimeMillis());
				}
			}
		}
		if (smscEntry.getBindMode().compareToIgnoreCase("r") != 0) {
			releaseQueue();
		}
		send_hti_pdu = null;
		bound = false;
		terminated = true;
	}

	private void releaseQueue() {
		try {
			int downCounter = 0;
			if (GlobalCache.SmscQueueCache.containsKey(smscEntry.getName())) {
				logger.info(smscEntry.getName() + " <-- Checking for Queue Release -->");
				for (int i = 1; i <= Constants.noofqueue; i++) {
					int queueSize = (GlobalCache.SmscQueueCache.get(smscEntry.getName())).PQueue[i].size();
					if (queueSize > 0) {
						for (int j = 0; j < queueSize; j++) {
							RoutePDU route_pdu = (RoutePDU) (GlobalCache.SmscQueueCache
									.get(smscEntry.getName())).PQueue[i].dequeue();
							if (route_pdu.getRequestPDU().getCommandId() == Data.SUBMIT_SM) {
								downCounter++;
								GlobalQueue.smsc_in_update_Queue.enqueue(new SmscInObj(route_pdu.getHtiMsgId(), "Q",
										route_pdu.getSmsc(), route_pdu.getGroupId(), route_pdu.getUsername()));
							}
							route_pdu = null;
						}
					}
				}
				if (downCounter > 0) {
					logger.info(smscEntry.getName() + " < Released Queue Size: " + downCounter + " >");
				}
			}
		} catch (Exception ex) {
			logger.error(smscEntry.getName(), ex);
		}
		// SmscStatus.SMSCQueueHashTable.remove(smscEntry.getName()); // gives NULLException HtiSmsManager
	}

	private Exception sendEnquireLink() {
		logger.debug(smscEntry.getName() + " sendEnquireLink() ");
		Exception toReturn = null;
		try {
			session.enquireLink(new EnquireLink());
		} catch (Exception e) {
			toReturn = e;
		}
		return toReturn;
	}

	private void changeBindStatus(boolean flag, int status_code) {
		java.sql.PreparedStatement statement = null;
		java.sql.Connection con = null;
		String sql = "insert ignore into smsc_status(server_id,smsc_id,bound,status_code) values(?,?,?,?) on duplicate key update bound=?,status_code=?";
		try {
			con = GlobalCache.connnection_pool_1.getConnection();
			statement = con.prepareStatement(sql);
			statement.setInt(1, GlobalVars.SERVER_ID);
			statement.setInt(2, smscEntry.getId());
			statement.setBoolean(3, flag);
			statement.setInt(4, status_code);
			statement.setBoolean(5, flag);
			statement.setInt(6, status_code);
			statement.executeUpdate();
		} catch (Exception q) {
			logger.error(smscEntry.getName(), q.fillInStackTrace());
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			GlobalCache.connnection_pool_1.putConnection(con);
		}
	}

	// ***********************************************************************************
	private String displayStatus(int status) {
		String error = "Unknown";
		switch (status) {
		case Data.ESME_RINVMSGLEN:
			error = "Message Length is Invalid";
			break;
		case Data.ESME_RINVCMDLEN:
			error = "Command Lenth is Invalid";
			break;
		case Data.ESME_RINVCMDID:
			error = "Invalid Command Id";
			break;
		case Data.ESME_RINVBNDSTS:
			error = "Incorrect Bind Status for Given Command";
			break;
		case Data.ESME_RALYBND:
			error = "ESMS Already in Bound Status";
			break;
		case Data.ESME_RINVPRTFLG:
			error = "Invalid Priorty Flag";
			break;
		case Data.ESME_RINVREGDLVFLG:
			error = "Invalid Registered Flag";
			break;
		case Data.ESME_RSYSERR:
			error = "System Error";
			break;
		case Data.ESME_RINVSRCADR:
			error = "Invalid Source Address";
			break;
		case Data.ESME_RINVDSTADR:
			error = "Invalid Destination Address";
			break;
		case Data.ESME_RINVMSGID:
			error = "Invalid Massage Id";
			break;
		case Data.ESME_RBINDFAIL:
			error = "Bind Failed";
			break;
		case Data.ESME_RINVPASWD:
			error = "Invalid Password";
			break;
		case Data.ESME_RINVSYSID:
			error = "Invalid System Id";
			break;
		case Data.ESME_RINVSERTYP:
			error = "Invalid Service Type";
			break;
		case Data.ESME_RINVSYSTYP:
			error = "Invalid System Type";
			break;
		}
		return error;
	}
}
