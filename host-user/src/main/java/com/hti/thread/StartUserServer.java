/*
 * StartUserServer.java
 *
 * Created on 08 April 2004, 18:12
 */
package com.hti.thread;

import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.cluster.Member;
import com.hazelcast.topic.ITopic;
import com.hti.dao.impl.NetworkDAServiceImpl;
import com.hti.dao.impl.RouteDAServiceImpl;
import com.hti.dao.impl.SmscDAServiceImpl;
import com.hti.dao.impl.UserDAServiceImpl;
import com.hti.hlr.MismatchResponseHandler;
import com.hti.hlr.StatusInsertThread;
import com.hti.listener.FlagEventListener;
import com.hti.listener.MemberEventListener;
import com.hti.listener.TableEventListener;
import com.hti.rmi.SmppServiceInvoke;
import com.hti.rmi.UserService;
import com.hti.rmi.UserServiceImpl;
import com.hti.user.SessionManager;
import com.hti.user.UserListener;
import com.hti.util.Constants;
import com.hti.util.FileUtil;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;

public class StartUserServer implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private String LISTEN_IP_PORT[];
	private int RMI_LISTEN_POST = 1080;
	// ------------- objects -----------------------
	private UserService userService = null;
	private QueueConnection connection = null;
	private QueueSession session = null;
	// ---------- Threads ---------------------
	private BearerBox Bearerbox;
	private UserListener[] userConnectionListener;
	private SmscIn smsc_in_thread;
	private SmscInTemp smsc_in_temp;
	private SmscInBackup smsc_in_backup;
	private SmscInLogThread smscInLogThread;
	private RequestLogInsert requestLogInsert;
	private BackupResponseInsertThread backupResponseInsertThread;
	private BackupResponseLogThread backupResponseLogThread;
	private CheckCpuCycle check_cpu_cycle;
	private SmOptParamInsert smOptParamInsert;
	// private MessageIdTrace messageIdTrace;
	private StatusInsertThread hlrStatusInsertThread;
	private MismatchResponseHandler mismatchHlrResponseHandler;
	private ReportLog reportLog;
	private ProfitLog profitLog;
	private BindAlert bindAlert;
	private SpamReport spamReport;

	public StartUserServer() {
	}

	public void run() {
		try {
			while (GlobalVars.APPLICATION_STATUS) {
				if (readApplicationFlag()) {
					if (!GlobalVars.OMQ_DLR_STATUS) {
						GlobalVars.OMQ_DLR_STATUS = startJMSConnection();
					}
					if (!GlobalVars.SMPP_STATUS) {
						try {
							GlobalVars.SMPP_STATUS = new SmppServiceInvoke().getSmppFlagStatus();
						} catch (Exception e) {
							logger.error("<-- Error While Communicating SMPP Server --> ");
						}
					}
					try {
						Thread.sleep(10 * 1000);
					} catch (InterruptedException ie) {
					}
				} else {
					GlobalVars.APPLICATION_STATUS = false;
					break;
				}
			}
		} catch (Exception ie) {
			ie.printStackTrace();
		} finally {
			System.out.println("");
			System.out.println("***************************");
			logger.info("<-- Stopping Application --> ");
			System.out.println("***************************");
			try {
				bindAlert.stop();
				logger.info("<-- Stopping User Listeners --> ");
				for (int i = 0; i < userConnectionListener.length; i++) {
					try {
						userConnectionListener[i].stop();
					} catch (IOException ioe) {
						logger.error(ioe + " While Stopping Listener");
					}
				}
				try {
					new SmppServiceInvoke().setUserHostStatus(false);
				} catch (Exception e) {
					logger.error("Error While Communicating To SMPP Server: " + e);
				}
				logger.info("<-- Closing Deliver Request OMQ Connection --> ");
				closeJMSConnection();
				logger.info("<-- Stopping User Session --> ");
				try {
					GlobalCache.UserSessionObject.forEach((k, v) -> {
						((SessionManager) v).block();
						try {
							Thread.sleep(100);
						} catch (InterruptedException ex) {
						}
					});
				} catch (Exception ioe) {
					logger.error(ioe + " While Stopping User Session");
				}
				try {
					Thread.sleep(3 * 1000); // Waiting for All Users to be disconnected
				} catch (InterruptedException ex) {
				}
				Bearerbox.stop();
				try {
					Thread.sleep(1000); // Waiting for Bearer box to stop threads
				} catch (InterruptedException ex) {
				}
				stopProcessingThreads();
				try {
					Thread.sleep(10 * 1000); // Waiting for All Processing Thread to stop
				} catch (InterruptedException ex) {
				}
				logger.info("<-- Closing Database Connections --> ");
				GlobalCache.connection_pool_user.removeAllConnections();
				GlobalCache.connection_pool_proc.removeAllConnections();
				logger.info("<-- Closing Log Database Connections --> ");
				GlobalCache.logConnectionPool.closeConnections();
				GlobalVars.hazelInstance.shutdown();
				System.out.println(FileUtil.readContent(Constants.CONFIG_DIR + "Shutdown.fil"));
			} catch (Exception ex) {
				ex.fillInStackTrace();
			}
			logger.info("<-- Exiting --> ");
			System.exit(0);
		}
	}

	public void initGlobalVars() {
		// -------- hazelcast cache client --------------
		ClientConfig config = new ClientConfig();
		// GroupConfig groupConfig = config.getGroupConfig();
		// groupConfig.setName("dev");
		// groupConfig.setPassword("dev-pass");
		config.getNetworkConfig().setSmartRouting(false);
		// -------- Global vars -------------------------
		GlobalVars.hazelInstance = HazelcastClient.newHazelcastClient(config);
		ITopic<Map<String, String>> flag_topic = GlobalVars.hazelInstance.getTopic("flag_status");
		flag_topic.addMessageListener(new FlagEventListener());
		ITopic<Map<String, Boolean>> table_topic = GlobalVars.hazelInstance.getTopic("table_locked");
		table_topic.addMessageListener(new TableEventListener());
		GlobalVars.hazelInstance.getCluster().addMembershipListener(new MemberEventListener());
		System.out.println(" *********** Members *****************");
		Set<Member> members = GlobalVars.hazelInstance.getCluster().getMembers();
		Iterator<Member> itr = members.iterator();
		int i = 1;
		int member_id = 0;
		while (itr.hasNext()) {
			Member member = itr.next();
			member_id = Integer.parseInt(member.getAttribute("member-id"));
			if (i == 1) {
				String db_cluster = member.getAttribute("db-cluster");
				if (db_cluster != null && db_cluster.equalsIgnoreCase("true")) {
					GlobalVars.DB_CLUSTER = true;
				}
				System.out.println("Master:-> Id: " + member_id + " Address:" + member.getAddress().getHost() + ":"
						+ member.getAddress().getPort() + " DB_CLUSTER: " + GlobalVars.DB_CLUSTER);
				if (member_id == GlobalVars.SERVER_ID) {
					GlobalVars.MASTER_CLIENT = true;
					logger.info("******* Marked As Master Client ********");
				}
			} else {
				System.out.println("Member:-> " + member.getAddress().getHost() + ":" + member.getAddress().getPort());
			}
			i++;
		}
		GlobalVars.smscService = new SmscDAServiceImpl();
		GlobalVars.networkService = new NetworkDAServiceImpl();
		GlobalVars.userService = new UserDAServiceImpl();
		GlobalVars.routeService = new RouteDAServiceImpl();
	}

	public void ApplicationStart() {
		logger.info(" Cleaning temporary Queue folder");
		File persist_directory = new File("temp");
		if (persist_directory.exists()) {
			try {
				FileUtils.cleanDirectory(persist_directory);
				logger.info(" Cleaned temporary Queue folder");
			} catch (Exception e) {
				logger.error("temp Folder Cleanup Error", e);
			}
		}
		try {
			FileUtil.setDefaultFlag(Constants.APPLICATION_FLAG);
			// ********* Start cache Service *********************
			// GlobalVars.hazelInstance = Hazelcast.newHazelcastInstance(new Config());
			// ********* End cache Service *********************
			startProcessingThreads();
			try {
				Thread.sleep(1000); // Waiting For All thread to start proper
			} catch (InterruptedException ex) {
			}
			Bearerbox = new BearerBox();
			new Thread(Bearerbox, "BearerBox").start();
			try {
				Thread.sleep(3 * 1000); // Waiting For Bearer Box to start proper
			} catch (InterruptedException ex) {
			}
			GlobalVars.APPLICATION_STATUS = true;
			logger.info("<--- Checking SMPP Server Status ---->  ");
			try {
				GlobalVars.SMPP_STATUS = new SmppServiceInvoke().getSmppFlagStatus();
				logger.info("SMPP Server Status ----------->" + GlobalVars.SMPP_STATUS);
			} catch (RemoteException e) {
				logger.error("<-- RemoteException While Connecting to SMPP Server -->");
			} catch (NotBoundException e) {
				logger.error("<-- NotBoundException While Connecting to SMPP Server --> ");
			}
			Thread moniter = new Thread(this, "StartUserServer");
			moniter.start();
			startPDUListener(); // Start Listener To bind Users
			startRMIListener(); // RMI Listenet Start for Remote Use
			bindAlert = new BindAlert();
			new Thread(bindAlert, "BindAlert").start();
			Thread.yield();
		} catch (RemoteException re) {
			logger.error("RMI Listener Starting error: " + re);
			logger.error("", re.fillInStackTrace());
			GlobalVars.APPLICATION_STATUS = false;
		} catch (IOException ioe) {
			logger.error("User Listener Starting error: " + ioe);
			GlobalVars.APPLICATION_STATUS = false;
			logger.error("", ioe.fillInStackTrace());
		} catch (Exception ex) {
			logger.error("Application Starting error: " + ex);
			GlobalVars.APPLICATION_STATUS = false;
			logger.error("", ex.fillInStackTrace());
		}
	}

	private void startProcessingThreads() {
		logger.info("<-------- Starting Processing Threads -----> ");
		try {
			reportLog = new ReportLog();
			new Thread(reportLog, "ReportLog").start();
			profitLog = new ProfitLog();
			new Thread(profitLog, "ProfitLog").start();
			smOptParamInsert = new SmOptParamInsert();
			new Thread(smOptParamInsert, "SmOptParamInsert").start();
			smsc_in_thread = new SmscIn();
			new Thread(smsc_in_thread, "SmscIn").start();
			smsc_in_temp = new SmscInTemp();
			new Thread(smsc_in_temp, "SmscInTemp").start();
			smsc_in_backup = new SmscInBackup();
			new Thread(smsc_in_backup, "SmscInBackup").start();
			smscInLogThread = new SmscInLogThread();
			new Thread(smscInLogThread, "SmscInLogThread").start();
			requestLogInsert = new RequestLogInsert();
			new Thread(requestLogInsert, "RequestLogInsert").start();
			backupResponseInsertThread = new BackupResponseInsertThread();
			new Thread(backupResponseInsertThread, "BackupResponseInsertThread").start();
			backupResponseLogThread = new BackupResponseLogThread();
			new Thread(backupResponseLogThread, "BackupResponseLogThread").start();
			check_cpu_cycle = new CheckCpuCycle();
			new Thread(check_cpu_cycle, "CheckCpuCycle").start();
			spamReport = new SpamReport();
			new Thread(spamReport, "SpamReport").start();
			// messageIdTrace = new MessageIdTrace();
			// new Thread(messageIdTrace, "MessageIdTrace").start();
			hlrStatusInsertThread = new StatusInsertThread();
			new Thread(hlrStatusInsertThread, "StatusInsertThread").start();
			mismatchHlrResponseHandler = new MismatchResponseHandler();
			new Thread(mismatchHlrResponseHandler, "MismatchResponseHandler").start();
			logger.info("<-------- Started Processing Threads ----->");
		} catch (Exception e) {
			logger.error("", e.fillInStackTrace());
		}
	}

	private void stopProcessingThreads() {
		logger.info("<-------- Stopping Processing Threads ----->");
		try {
			// messageIdTrace.stop();
			check_cpu_cycle.stop();
			smsc_in_backup.stop();
			smsc_in_thread.stop();
			smsc_in_temp.stop();
			smscInLogThread.stop();
			requestLogInsert.stop();
			hlrStatusInsertThread.stop();
			mismatchHlrResponseHandler.stop();
			backupResponseInsertThread.stop();
			backupResponseLogThread.stop();
			reportLog.stop();
			profitLog.stop();
			smOptParamInsert.stop();
			spamReport.stop();
			logger.info("<-------- Stopped Processing Threads ----->");
		} catch (Exception ie) {
			logger.error("", ie.fillInStackTrace());
		}
	}

	private void startRMIListener() throws RemoteException {
		logger.info("RMI User Service Starting on Port:" + RMI_LISTEN_POST);
		userService = new UserServiceImpl();
		// create on port 1080
		Registry registry_user = LocateRegistry.createRegistry(RMI_LISTEN_POST);
		// create a new service named userService
		registry_user.rebind("userService", userService);
		logger.info("RMI User Service Started on Port:" + RMI_LISTEN_POST);
	}

	private void startPDUListener() throws Exception {
		userConnectionListener = new UserListener[LISTEN_IP_PORT.length];
		for (int i = 0; i < LISTEN_IP_PORT.length; i++) {
			String[] ip_port = LISTEN_IP_PORT[i].split(":");
			String local_ip = ip_port[0];
			int local_port = Integer.parseInt(ip_port[1]);
			logger.info("User Listener[" + (i + 1) + "] Starting[" + local_ip + ": " + local_port);
			userConnectionListener[i] = new UserListener(local_ip, local_port, true);
			userConnectionListener[i].start();
			logger.info("User Listener[" + (i + 1) + "] Started[" + local_ip + ": " + local_port);
			if (i == 0) {
				Constants.LOCAL_IP = local_ip;
				Constants.LOCAL_PORT = local_port;
			}
			try {
				Thread.sleep(5 * 1000);
			} catch (InterruptedException ie) {
			}
		}
	}

	private boolean startJMSConnection() {
		logger.info("<--- Trying to Connect OMQ Server To Receive DLRs -->");
		// creating a connection
		try {
			connection = new com.sun.messaging.QueueConnectionFactory().createQueueConnection();
			connection.start();
			session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			for (int i = 0; i < 3; i++) {
				Queue queue = session.createQueue("dlrQueue_" + (i + 1));
				QueueReceiver receiver = session.createReceiver(queue);
				QueueListener listener = new QueueListener(i + 1);
				receiver.setMessageListener(listener);
			}
		} catch (Exception ex) {
			logger.error("<-- OMQ DLR Connection Error[" + ex + "] --> ");
			return false;
		}
		logger.info("<--- OMQ Server Connected To Receive DLRs -->");
		return true;
	}

	private void closeJMSConnection() {
		try {
			session.close();
		} catch (JMSException e) {
			logger.error("", e.fillInStackTrace());
		}
		try {
			connection.close();
		} catch (JMSException e) {
			logger.error("closeJMSConnection()", e.fillInStackTrace());
		}
	}

	

	public boolean readApplicationFlag() {
		String flagValue = FileUtil.readFlag(Constants.APPLICATION_FLAG, true);
		if ((flagValue != null) && (flagValue.contains("404"))) {
			logger.info("<----------------- Got Command to Stop User Server --------> ");
			return false;
		}
		return true;
	}

	public boolean loadCofigration() {
		// boolean configFlag = true;
		try {
			Properties props = FileUtil.readProperties(Constants.APPLICATION_CONFIG_FILE);
			String ip_port_config = props.getProperty("SMPP-IP-PORT");
			LISTEN_IP_PORT = ip_port_config.split(",");
			GlobalVars.SERVER_ID = Integer.parseInt(props.getProperty("SERVER-ID"));
			// ------- database --------------------
			GlobalVars.DB_URL = props.getProperty("CONNECTION-URL");
			GlobalVars.LOG_DB_URL = props.getProperty("LOG-CONNECTION-URL");
			GlobalVars.MAX_CONNECTIONS = (Integer.parseInt(props.getProperty("MAX_CONNECTIONS")) / 2);
			GlobalVars.JDBC_DRIVER = props.getProperty("JDBC-DRIVER");
			GlobalVars.DB_USER = props.getProperty("DB_USER");
			GlobalVars.DB_PASSWORD = props.getProperty("DB_PASSWORD");
			GlobalVars.ALTER_DB_USER = props.getProperty("DB_ALTER_USER");
			GlobalVars.ALTER_DB_PASSWORD = props.getProperty("DB_ALTER_PASSWORD");
			// ------- others ----------------------
			GlobalVars.MIN_DESTINATION_LENGTH = Integer.parseInt(props.getProperty("MIN_DEST_LENGTH"));
			GlobalVars.FIX_LONG_WAIT_TIME = Integer.parseInt(props.getProperty("FIX_LONG_WAIT_TIME"));
			GlobalVars.URL_CHECK_WAIT_TIME = Integer.parseInt(props.getProperty("URL_CHECK_WAIT_TIME"));
			GlobalVars.INVALID_DEST_SMSC = props.getProperty("INVALID_DEST_SMSC");
			GlobalVars.REJECT_SMSC = props.getProperty("REJECT_SMSC");
			GlobalVars.DELIVRD_SMSC = props.getProperty("DELIVRD_SMSC");
			GlobalVars.UNDELIV_SMSC = props.getProperty("UNDELIV_SMSC");
			Constants.SERVER_NAME = props.getProperty("SERVER_NAME");
			GlobalVars.RECEIVER_QUEUE_SIZE = Integer.parseInt(props.getProperty("RECEIVER_QUEUE_SIZE"));
			GlobalVars.NoOfSessionAllowed = Integer.parseInt(props.getProperty("SESSION_LIMIT"));
			// --------- email ---------------------
			Constants.EMAIL_CC = props.getProperty("EMAIL_CC");
			Constants.EMAIL_FROM = props.getProperty("EMAIL_FROM");
			Constants.EMAIL_USER = props.getProperty("EMAIL_USER");
			Constants.EMAIL_PASSWORD = props.getProperty("EMAIL_PASSWORD");
			Constants.SMTP_HOST_NAME = props.getProperty("SMTP_HOST_NAME");
			Constants.SMTP_PORT = Integer.parseInt(props.getProperty("SMTP_PORT"));
			// --------- hlr ---------------------
			props = FileUtil.readProperties(Constants.HLR_CONFIG_FILE);
			Constants.HLR_DOWN_SMSC_1 = props.getProperty("HLR_DOWN_SMSC_1");
			Constants.HLR_DOWN_SMSC_2 = props.getProperty("HLR_DOWN_SMSC_2");
			Constants.HLR_DOWN_SMSC_3 = props.getProperty("HLR_DOWN_SMSC_3");
			Constants.HLR_DOWN_SMSC_4 = props.getProperty("HLR_DOWN_SMSC_4");
			Constants.HLR_DOWN_SMSC_5 = props.getProperty("HLR_DOWN_SMSC_5");
			Constants.HLR_STATUS_WAIT_DURATION = Integer.parseInt(props.getProperty("HLR_WAIT_DURATION"));
			Constants.HLR_SERVER_IP = props.getProperty("HLR_SERVER_IP").trim();
			Constants.HLR_SERVER_PORT = Integer.parseInt(props.getProperty("HLR_SERVER_PORT").trim());
			Constants.HLR_SESSION_LIMIT = Integer.parseInt(props.getProperty("HLR_SESSION_LIMIT").trim());
			Constants.PROMO_SENDER = props.getProperty("PROMO_SENDER");
			Constants.DND_SMSC = props.getProperty("DND_SMSC");
		} catch (Exception e) {
			logger.error("Configuration File Read Error: " + e.getMessage());
			return false;
		}
		return true;
	}
}
