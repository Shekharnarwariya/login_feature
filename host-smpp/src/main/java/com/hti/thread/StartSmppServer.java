/*
 * SMPPAPPLICATION.java
 *
 * Created on 08 April 2004, 18:12
 */
package com.hti.thread;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.cluster.Member;
import com.hazelcast.topic.ITopic;
import com.hti.dao.impl.NetworkDAServiceImpl;
import com.hti.dao.impl.SmscDAServiceImpl;
import com.hti.dao.impl.UserDAServiceImpl;
import com.hti.database.ConnectionPool;
import com.hti.listener.FlagEventListener;
import com.hti.listener.MemberEventListener;
import com.hti.listener.TableEventListener;
import com.hti.objects.LogPDU;
import com.hti.objects.PriorityQueue;
import com.hti.objects.ReportLogObject;
import com.hti.objects.RoutePDU;
import com.hti.objects.SmscInObj;
import com.hti.rmi.SmppService;
import com.hti.rmi.SmppServiceImpl;
import com.hti.rmi.UserServiceInvoke;
import com.hti.util.Constants;
import com.hti.util.FileUtil;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

/**
 * @author administrator Main class for Starting the Application take control of Accumulator Box and BearerBox Thread, stops Application if Application File set to 404
 */
public class StartSmppServer implements Runnable {
	private static Logger logger = LoggerFactory.getLogger("ProcLogger");
	private SmppService smppService = null;
	// private rdConfigFile CA_rdConFile = new rdConfigFile();
	private BearerBox Bearerbox;
	private AccumulatorBox acbox;
	public static String USER_SERVER_IP = "127.0.0.1";
	public static int USER_SERVER_PORT = 8899;
	private int RMI_LISTEN_POST = 1081;
	private DB repeatedNumberDB;
	// private FileUtil fileUtil = new FileUtil();

	public StartSmppServer() {
	}

	public void run() {
		try {
			int printcount = 0;
			while (Constants.APPLICATION_STATUS) {
				if (readApplicationFlag()) {
					if (!Constants.USER_HOST_STATUS) {
						try {
							Constants.USER_HOST_STATUS = new UserServiceInvoke().getUserHostStatus();
						} catch (Exception e) {
							logger.error("<--- Error While Communicating User Server ---> ",e);
						}
					}
					try {
						Thread.sleep(6 * 1000);
					} catch (InterruptedException e) {
					}
					if (++printcount == 10) {
						checkNonResponding();
						printcount = 0;
					}
				} else {
					Constants.APPLICATION_STATUS = false;
					Constants.PROCESSING_STATUS = false;
					break;
				}
			}
		} catch (Exception ie) {
			logger.error("run()", ie.fillInStackTrace());
		} finally {
			System.out.println("");
			System.out.println("***************************");
			logger.info("<-- Stopping Application --> ");
			System.out.println("***************************");
			logger.info("<-- Stopping User Session --> ");
			try {
				new UserServiceInvoke().setSmppFlagStatus(false);
			} catch (Exception e) {
				logger.error("Error While Communicating To User Server: " + e);
			}
			Constants.USER_HOST_STATUS = false;
			logger.info("<-- Stopping Queue Load Process --> ");
			acbox.StopQueueLoader();
			try {
				Thread.sleep(3 * 1000);
			} catch (InterruptedException ex) { // Waiting for Stop QueueLoader
			}
			Bearerbox.stop();
			try {
				Thread.sleep(10 * 1000); // Waiting for Bearer Box to stop
			} catch (InterruptedException ex) {
			}
			acbox.stop();
			try {
				Thread.sleep(30 * 1000); // Waiting for All Processing Thread to stop
			} catch (InterruptedException ex) {
			}
			logger.info("<-- Closing Database Connections --> ");
			GlobalCache.connnection_pool_1.removeAllConnections();
			GlobalCache.connnection_pool_2.removeAllConnections();
			closeCacheManager();
			try {
				System.out.println(FileUtil.readContent(Constants.CONFIG_DIR + "Shutdown.fil"));
			} catch (Exception e) {
				System.err.println(Constants.CONFIG_DIR + "Shutdown.fil" + " " + e.getMessage());
			}
			logger.info("<-- Exiting --> ");
			System.exit(0);
		}
	}

	public void ApplicationStart() {
		try {
			System.out.println(FileUtil.readContent(Constants.CONFIG_DIR + "welcome.fil"));
		} catch (Exception e) {
			System.err.println(Constants.CONFIG_DIR + "welcome.fil" + " " + e.getMessage());
		}
		try {
			FileUtil.setDefaultFlag(Constants.ApplicationFlag);
			intializeCacheManager();
			acbox = new AccumulatorBox();
			new Thread(acbox, "AccumulatorBox").start();
			Bearerbox = new BearerBox();
			new Thread(Bearerbox, "BearerBox").start();
			try {
				Thread.sleep(10 * 1000); // Waiting For All Smsc to Connect
			} catch (InterruptedException ex) {
			}
			acbox.startQueueLoader();
			try {
				Thread.sleep(3 * 1000); // Wait before User Listener to start
			} catch (InterruptedException ex) {
			}
			startRMIListener(); // RMI Listenet Start for Remote Use
			Constants.APPLICATION_STATUS = true;
			Constants.PROCESSING_STATUS = true;
			Thread moniter = new Thread(this, "StartSmppServer");
			moniter.start();
			Thread.yield();
		} catch (Exception ex) {
			logger.error("Application Starting error", ex.fillInStackTrace());
			Constants.APPLICATION_STATUS = false;
			Constants.PROCESSING_STATUS = false;
			// closeCacheManager();
			ex.fillInStackTrace();
		}
	}

	
	private void closeCacheManager() {
		logger.info("<-- Closing Cache Manager --> ");
		try {
			if (repeatedNumberDB != null) {
				repeatedNumberDB.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (GlobalCache.cacheManager != null) {
			GlobalCache.cacheManager.close();
		}
	}

	public void initGlobalVars() {
		// -------- hazelcast cache client --------------
		ClientConfig config = new ClientConfig();
		//GroupConfig groupConfig = config.getGroupConfig();
		//groupConfig.setName("dev");
		//groupConfig.setPassword("dev-pass");
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
	}

	private void intializeCacheManager() throws Exception {
		File persist_directory = new File("backup//repeatedNumbers.db");
		if (persist_directory.exists()) {
			try {
				FileUtils.forceDelete(persist_directory);
				logger.info(" Deleted repeated Numbers dbfile");
			} catch (IOException e) {
				logger.error(" Unable to delete dbfile: " + persist_directory.getName());
			}
		}
		try {
			repeatedNumberDB = DBMaker.fileDB("backup//repeatedNumbers.db").closeOnJvmShutdown().checksumHeaderBypass()
					.make();
			GlobalCache.GroupWiseRepeatedNumbers = repeatedNumberDB
					.hashMap("repeated", Serializer.STRING, Serializer.INTEGER).expireAfterCreate(48, TimeUnit.HOURS)
					.expireAfterGet(72, TimeUnit.HOURS).expireExecutor(Executors.newSingleThreadScheduledExecutor())
					.createOrOpen();
		} catch (Exception ex) {
			logger.error("", ex);
		}
		logger.info("Queue Cache:-> Heap: " + Constants.QUEUE_HEAP_SIZE + " Disk: " + Constants.QUEUE_DISK_SIZE
				+ " Expired: " + Constants.QUEUE_EXPIRED_ON);
		logger.info("Response Cache:-> Heap: " + Constants.RESPONSE_HEAP_SIZE + " Disk: " + Constants.RESPONSE_DISK_SIZE
				+ " Expired: " + Constants.RESPONSE_EXPIRED_ON + " DlrExpired: " + Constants.RESPONSE_DLR_EXPIRED_ON);
		logger.info("Resend pdu Cache:-> Heap: " + Constants.RESEND_PDU_HEAP_SIZE + " Disk: "
				+ Constants.RESEND_PDU_DISK_SIZE + " Expired: " + Constants.RESEND_PDU_EXPIRED_ON);
		GlobalCache.cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
				.with(CacheManagerBuilder.persistence(Constants.persist_dir)).build(true);
		// ************************** Part Mapping *************************
		CacheConfigurationBuilder part_config_builder = CacheConfigurationBuilder
				.newCacheConfigurationBuilder(String.class, HashSet.class,
						ResourcePoolsBuilder.newResourcePoolsBuilder().heap(25, MemoryUnit.MB)
								.offheap(25 * 2, MemoryUnit.MB).disk(1000, MemoryUnit.MB, true))
				.withExpiry(Expirations.timeToLiveExpiration(Duration.of(72, TimeUnit.HOURS)));
		GlobalCache.PartMappingForDlr = GlobalCache.cacheManager.createCache("part_map_cache", part_config_builder);
		// ************************** Queue Cache **************************
		GlobalCache.queue_config_Builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class,
				PriorityQueue.class,
				ResourcePoolsBuilder.newResourcePoolsBuilder().heap(Constants.QUEUE_HEAP_SIZE, MemoryUnit.MB)
						.offheap(Constants.QUEUE_HEAP_SIZE * 2, MemoryUnit.MB)
						.disk(Constants.QUEUE_DISK_SIZE, MemoryUnit.MB, false));
		GlobalCache.SmscQueueCache = GlobalCache.cacheManager.createCache("smsc_queue_cache",
				GlobalCache.queue_config_Builder);
		// ************************** Response Cache **************************
		GlobalCache.resp_config_Builder = CacheConfigurationBuilder
				.newCacheConfigurationBuilder(String.class, LogPDU.class,
						ResourcePoolsBuilder.newResourcePoolsBuilder().heap(Constants.RESPONSE_HEAP_SIZE, MemoryUnit.MB)
								.offheap(Constants.RESPONSE_HEAP_SIZE * 2, MemoryUnit.MB)
								.disk(Constants.RESPONSE_DISK_SIZE, MemoryUnit.MB, true))
				.withExpiry(
						Expirations.timeToLiveExpiration(Duration.of(Constants.RESPONSE_EXPIRED_ON, TimeUnit.MINUTES)));
		GlobalCache.ResponseLogCache = GlobalCache.cacheManager.createCache("resp_log_cache",
				GlobalCache.resp_config_Builder);
		// ************************** Response Dlr Cache **************************
		GlobalCache.resp_dlr_config_Builder = CacheConfigurationBuilder
				.newCacheConfigurationBuilder(String.class, LogPDU.class,
						ResourcePoolsBuilder.newResourcePoolsBuilder().heap(Constants.RESPONSE_HEAP_SIZE, MemoryUnit.MB)
								.offheap(Constants.RESPONSE_HEAP_SIZE * 2, MemoryUnit.MB)
								.disk(Constants.RESPONSE_DISK_SIZE * 3, MemoryUnit.MB, false))
				.withExpiry(Expirations
						.timeToLiveExpiration(Duration.of(Constants.RESPONSE_DLR_EXPIRED_ON, TimeUnit.MINUTES)));
		GlobalCache.ResponseLogDlrCache = GlobalCache.cacheManager.createCache("resp_log_dlr_cache",
				GlobalCache.resp_dlr_config_Builder);
		// ************************** Resend Pdu Cache **************************
		GlobalCache.resend_config_Builder = CacheConfigurationBuilder
				.newCacheConfigurationBuilder(String.class, RoutePDU.class,
						ResourcePoolsBuilder.newResourcePoolsBuilder()
								.heap(Constants.RESEND_PDU_HEAP_SIZE, MemoryUnit.MB)
								.offheap(Constants.RESEND_PDU_HEAP_SIZE * 2, MemoryUnit.MB)
								.disk(Constants.RESEND_PDU_DISK_SIZE, MemoryUnit.MB, false))
				.withExpiry(Expirations
						.timeToLiveExpiration(Duration.of(Constants.RESEND_PDU_EXPIRED_ON, TimeUnit.MINUTES)));
		GlobalCache.ResendPDUCache = GlobalCache.cacheManager.createCache("resend_pdu_cache",
				GlobalCache.resend_config_Builder);
		// ****************** mapdb groupwise repeated numbers *************************
	}

	private void startRMIListener() throws RemoteException {
		logger.info("RMI Smpp Service Starting on Port:" + RMI_LISTEN_POST);
		smppService = new SmppServiceImpl();
		// create on port 1080
		Registry registry_user = LocateRegistry.createRegistry(RMI_LISTEN_POST);
		// create a new service named userService
		registry_user.rebind("smppService", smppService);
		logger.info("RMI Smpp Service Started on Port:" + RMI_LISTEN_POST);
	}

	public void initializeDistVar() {
		Statement statement = null;
		ResultSet rs = null;
		String crossQuery = "delete from smsc_in where s_flag in('C','F','Q') and msg_id in(select msg_id from mis_table where status not like 'ERR_RESP')";
		Connection con = null;
		// Set deleteSet = new HashSet();
		try {
			con = GlobalCache.connnection_pool_1.getConnection();
			if (con != null) {
				logger.info("Checking Records to be deleted From smsc_in");
				statement = con.createStatement();
				int count = statement.executeUpdate(crossQuery);
				logger.info("Records deleted from smsc_in: " + count);
				crossQuery = "update smsc_in set s_flag='Q' where s_flag='C'";
				count = statement.executeUpdate(crossQuery);
				logger.info("Records updated smsc_in:: " + count);
			}
		} catch (Exception ex) {
			logger.error("initializeDistVar()", ex.fillInStackTrace());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
				}
			}
			GlobalCache.connnection_pool_1.putConnection(con);
		}
	}

	/////////////////////////////// ERROR CODE FROM SMSC////////////////////////////////
	private void checkNonResponding() {
		System.out.println("<- Checking For Non Responding ->");
		try {
			Map<String, java.util.Date> tempMap = null;
			try {
				tempMap = new HashMap<String, java.util.Date>(GlobalCache.SmscSubmitTime); // To avoid Concurrent Modification Error
			} catch (ConcurrentModificationException ce) {
				// ConcurrentModificationException if the specified map is modified while the operation is in progress.
			}
			if (tempMap != null && !tempMap.isEmpty()) {
				int nonrespondduration = Integer.parseInt(Constants.NON_RESP_WAIT_TIME);
				Iterator<String> enumeration = tempMap.keySet().iterator();
				String msgid = null;
				java.util.Date submit_time = null;
				while (enumeration.hasNext()) {
					msgid = enumeration.next();
					submit_time = tempMap.get(msgid);
					long nonrespondmillis = submit_time.getTime() + (nonrespondduration * 60000);
					if (System.currentTimeMillis() > nonrespondmillis) {
						GlobalCache.SmscSubmitTime.remove(msgid);
						if (GlobalCache.ResponseLogCache.containsKey(msgid)) {
							LogPDU log_pdu = GlobalCache.ResponseLogCache.get(msgid);
							GlobalQueue.smsc_in_update_Queue.enqueue(new SmscInObj(log_pdu.getMsgid(), "F",
									log_pdu.getRoute(), log_pdu.getGroupId(), log_pdu.getUsername()));
							GlobalQueue.reportUpdateQueue.enqueue(new ReportLogObject(log_pdu.getMsgid(),
									log_pdu.getRoute(), "NO_RESP", log_pdu.getSource()));
						}
					}
				}
				tempMap = null;
			}
		} catch (Exception e) {
			logger.error("checkNonResponding()", e.fillInStackTrace());
		}
	}

	private boolean readApplicationFlag() {
		String flagValue = null;
		try {
			flagValue = FileUtil.readFlag(Constants.ApplicationFlag, true);
		} catch (IOException ioe) {
			logger.error("readApplicationFlag()", ioe);
		}
		if ((flagValue != null) && (flagValue.contains("404"))) {
			logger.info("<----------------- Got Command to Stop SMPP Server --------> ");
			return false;
		}
		return true;
	}

	public boolean loadCofigration() {
		boolean configFlag = true;
		try {
			Properties props = FileUtil.readProperties(Constants.APPLICATION_CONFIG_FILE);
			Constants.DCS = Integer.parseInt(props.getProperty("DCS"));
			String ip_port_config = props.getProperty("SMPP-IP-PORT");
			String[] user_server_ip_port = (ip_port_config.split(",")[0]).split(":");
			USER_SERVER_IP = user_server_ip_port[0];
			USER_SERVER_PORT = Integer.parseInt(user_server_ip_port[1]);
			logger.info("User Server Bind Configuration:-> " + USER_SERVER_IP + ":" + USER_SERVER_PORT);
			GlobalVars.SERVER_ID = Integer.parseInt(props.getProperty("SERVER-ID"));
			// --------- database ---------------------
			Constants.LOG_DB = props.getProperty("LOG-DB");
			Constants.DB_URL = props.getProperty("CONNECTION-URL");
			Constants.LOG_DB_URL = props.getProperty("LOG-CONNECTION-URL");
			Constants.MAX_CONNECTIONS = (Integer.parseInt(props.getProperty("MAX_CONNECTIONS")) / 2);
			Constants.JDBC_DRIVER = props.getProperty("JDBC-DRIVER");
			Constants.DB_USER = props.getProperty("DB_USER");
			Constants.DB_PASSWORD = props.getProperty("DB_PASSWORD");
			Constants.ALTER_DB_USER = props.getProperty("DB_ALTER_USER");
			Constants.ALTER_DB_PASSWORD = props.getProperty("DB_ALTER_PASSWORD");
			// --------- email ---------------------
			Constants.EMAIL_CC = props.getProperty("EMAIL_CC");
			Constants.EMAIL_FROM = props.getProperty("EMAIL_FROM");
			Constants.EMAIL_USER = props.getProperty("EMAIL_USER");
			Constants.EMAIL_PASSWORD = props.getProperty("EMAIL_PASSWORD");
			Constants.SMTP_HOST_NAME = props.getProperty("SMTP_HOST_NAME");
			Constants.SMTP_PORT = Integer.parseInt(props.getProperty("SMTP_PORT"));
			// --------- cache ---------------------
			Constants.QUEUE_HEAP_SIZE = Long.parseLong(props.getProperty("queue.heap.size"));
			Constants.QUEUE_DISK_SIZE = Long.parseLong(props.getProperty("queue.disk.size"));
			Constants.QUEUE_EXPIRED_ON = Long.parseLong(props.getProperty("queue.expired.time"));
			Constants.RESPONSE_HEAP_SIZE = Long.parseLong(props.getProperty("resp.heap.size"));
			Constants.RESPONSE_DISK_SIZE = Long.parseLong(props.getProperty("resp.disk.size"));
			Constants.RESPONSE_EXPIRED_ON = Long.parseLong(props.getProperty("resp.expired.time"));
			Constants.RESPONSE_DLR_EXPIRED_ON = Long.parseLong(props.getProperty("resp.dlr.expired.time"));
			Constants.RESEND_PDU_HEAP_SIZE = Long.parseLong(props.getProperty("resd.pdu.heap.size"));
			Constants.RESEND_PDU_DISK_SIZE = Long.parseLong(props.getProperty("resd.pdu.disk.size"));
			Constants.RESEND_PDU_EXPIRED_ON = Long.parseLong(props.getProperty("resd.pdu.expired.time"));
			// --------- other ---------------------
			Constants.L_DUMP_SMSC = props.getProperty("L_DUMP_SMSC");
			Constants.DLT_UNDELIV_SMSC = props.getProperty("DLT_UNDELIV_SMSC");
			Constants.DLT_PREFIX = props.getProperty("DLT_PREFIX");
			Constants.TW_DUMP_SMSC = props.getProperty("TW_DUMP_SMSC");
			Constants.NON_RESP_WAIT_TIME = props.getProperty("NON_RESP_WAIT_TIME");
			BindAlert.WAIT_DURATION = Integer.parseInt(props.getProperty("WAIT_DURATION"));
			Constants.SERVER_NAME = props.getProperty("SERVER_NAME");
			Constants.BSFM_DUMP_SMSC = props.getProperty("B_DUMP_SMSC");
			//------------ queued alert ----------------
			Constants.QUEUED_ALERT_COUNT = Integer.parseInt(props.getProperty("QUEUED_ALERT_COUNT"));
			Constants.QUEUED_ALERT_DURATION = Integer.parseInt(props.getProperty("QUEUED_ALERT_DURATION"));
			Constants.QUEUED_ALERT_EMAILS = props.getProperty("QUEUED_ALERT_EMAILS");
			Constants.QUEUED_ALERT_NUMBERS = props.getProperty("QUEUED_ALERT_NUMBERS");
		} catch (Exception e) {
			// e.printStackTrace();
			configFlag = false;
		}
		return configFlag;
	}
}
