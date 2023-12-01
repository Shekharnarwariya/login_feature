package com.hti.init;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.client.ClientService;
import com.hazelcast.cluster.Member;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hti.listener.ClientEventListener;
import com.hti.listener.MemberEventListener;
import com.hti.service.CacheService;
import com.hti.util.FileUtil;
import com.hti.util.GlobalVar;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

public class StartService implements Runnable {
	private Logger logger = LoggerFactory.getLogger(StartService.class);
	private FlagReader flagReader;
	private BearerBox bearerBox;
	private BalanceOperator balanceOperator;
	private CheckCpuCycle checkCpuCycle;

	public static void main(String[] args) {
		System.out.println("Starting Cache Server");
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.reset();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(loggerContext);
		InputStream configStream = null;
		try {
			configStream = FileUtils.openInputStream(new File("config//logConfig.xml"));
			configurator.doConfigure(configStream);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				configStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		StartService start = new StartService();
		try {
			start.startApplication();
		} catch (IOException e) {
			System.err.print(e);
			// System.exit(0);
		}
	}

	private void startApplication() throws FileNotFoundException, IOException {
		Config cfg = new XmlConfigBuilder("config/hazelcast.xml").build();
		GlobalVar.hazelInstance = Hazelcast.newHazelcastInstance(cfg);
		// -------- Add listener to check activity of Clients -----------
		ClientService clientService = GlobalVar.hazelInstance.getClientService();
		clientService.addClientListener(new ClientEventListener());
		// -------- Add listener to check activity of Members -----------
		GlobalVar.hazelInstance.getCluster().addMembershipListener(new MemberEventListener());
		Properties properties = FileUtil.readProperties("config//config.file");
		GlobalVar.DATABASE_URL = properties.getProperty("CONNECTION_URL");
		GlobalVar.DATABASE_DRIVER = properties.getProperty("DATABASE_DRIVER");
		GlobalVar.DATABASE_USER = properties.getProperty("USERNAME");
		GlobalVar.DATABASE_PASSWORD = properties.getProperty("PASSWORD");
		GlobalVar.MAX_CONNECTION = Integer.parseInt(properties.getProperty("MAX_CONNECTION"));
		GlobalVar.dbConnection = new com.hti.database.DBConnectionImpl();
		CacheService.init();
		CacheService.loadSmscSchedule();
		checkMasterMember();
		flagReader = new FlagReader();
		if (GlobalVar.MASTER_MEMBER) {
			CacheService.loadNetworkEntries();
			CacheService.loadSmscEntries();
			CacheService.loadSmscGroup();
			CacheService.loadSmscGroupMember();
			CacheService.loadUserConfig();
			CacheService.loadRouting();
		} else {
			flagReader.initFlagStatus();
		}
		new Thread(flagReader, "FlagReader").start();
		bearerBox = new BearerBox();
		Thread thread = new Thread(bearerBox);
		bearerBox.setThread(thread);
		thread.start();
		balanceOperator = new BalanceOperator();
		new Thread(balanceOperator, "BalanceOperator").start();
		checkCpuCycle = new CheckCpuCycle();
		new Thread(checkCpuCycle, "checkCpuCycle").start();
		new Thread(this, "StartService").start();
	}

	private void stopApplication() {
		logger.info("Cache Server is Stopping");
		bearerBox.stop();
		flagReader.stop();
		balanceOperator.stop();
		checkCpuCycle.stop();
		GlobalVar.dbConnection.removeAllConnections();
		CacheService.shutdown();
		logger.info("<-- Exiting --> ");
		System.exit(0);
	}

	private void checkMasterMember() {
		System.out.println(" *********** Members *****************");
		Set<Member> members = GlobalVar.hazelInstance.getCluster().getMembers();
		Iterator<Member> itr = members.iterator();
		int i = 1;
		while (itr.hasNext()) {
			Member member = itr.next();
			if (i == 1) {
				if (member.localMember()) {
					GlobalVar.MASTER_MEMBER = true;
				} else {
					GlobalVar.MASTER_MEMBER = false;
				}
				System.out.println("Master:-> " + member.getAddress().getHost() + ":" + member.getAddress().getPort());
			} else {
				System.out.println("Member:-> " + member.getAddress().getHost() + ":" + member.getAddress().getPort());
			}
			i++;
		}
	}

	@Override
	public void run() {
		try {
			while (!GlobalVar.APPLICATION_STOP) {
				System.out.println("Cache Server is Running");
				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
				}
			}
		} catch (Exception ie) {
			ie.printStackTrace();
		} finally {
			stopApplication();
		}
	}
}
