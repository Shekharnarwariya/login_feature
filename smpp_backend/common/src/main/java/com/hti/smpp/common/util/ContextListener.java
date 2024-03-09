package com.hti.smpp.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.cluster.Member;
/**
 * Application context listener responsible for initializing properties and
 * connecting to Hazelcast Cluster.
 */

@Component
public class ContextListener {
	private static final Logger logger = LoggerFactory.getLogger(ContextListener.class);

	// Load properties file during class initialization

	public static Properties property = new Properties();

	static {
		initializeProperties();
		connectToHazelcastCluster();
	}

	/**
	 * Initialize application properties from the "ApplicationResources.properties"
	 * file.
	 */

	private static void initializeProperties() {
		try (InputStream input = ContextListener.class.getClassLoader()
				.getResourceAsStream("ApplicationResources.properties")) {
			property.load(input);
		} catch (IOException e) {
			logger.error("Error loading properties file", e);
			// Handle the exception appropriately
		}
	}

	/**
	 * Connect to the Hazelcast Cluster and log cluster members.
	 */

	private static void connectToHazelcastCluster() {
		logger.info("Connecting to Hazelcast Cluster");

		try {

			System.out.println(property.getProperty("hazelcast.cluster.address"));
			ClientConfig config = new ClientConfig();
			config.getNetworkConfig().setSmartRouting(false);
			config.getNetworkConfig().addAddress(property.getProperty("hazelcast.cluster.address"));
			GlobalVars.hazelInstance = HazelcastClient.newHazelcastClient(config);
			GlobalVars.hazelInstance.getCluster().addMembershipListener(new MemberEventListener());
			GlobalVars.BatchQueue = GlobalVars.hazelInstance.getMap("batch_queue");
			GlobalVars.HlrBatchQueue = GlobalVars.hazelInstance.getMap("hlr_batch_queue");
			GlobalVars.SmscGroupEntries = GlobalVars.hazelInstance.getMap("smsc_group");
			GlobalVars.HttpDlrParam = GlobalVars.hazelInstance.getMap("http_dlr_param");
			GlobalVars.NetworkEntries = GlobalVars.hazelInstance.getMap("network_entries");
			GlobalVars.flag_write_Cache = GlobalVars.hazelInstance.getMap("flag_write_cache");
			GlobalVars.user_flag_status = GlobalVars.hazelInstance.getMap("user_flag_status");

			GlobalVars.SmscEntries = GlobalVars.hazelInstance.getMap("smsc_entries");

			logClusterMembers();

		} catch (Exception e) {
			logger.error("Error connecting to Hazelcast Cluster", e);
			// Handle the exception appropriately
		}
	}

	/**
	 * Log details of all cluster members.
	 */

	private static void logClusterMembers() {
		logger.info("Cluster Members:");
		Set<Member> members = GlobalVars.hazelInstance.getCluster().getMembers();
		int i = 1;

		for (Member member : members) {
			logMemberDetails(member, i);
			i++;
		}
	}

	/**
	 * Log details of a specific cluster member.
	 */

	private static void logMemberDetails(Member member, int memberIndex) {
		int memberId = Integer.parseInt(member.getAttribute("member-id"));
		logger.info("Member {}: {} Id: {} Address: {}:{} DB_CLUSTER: {}", memberIndex,
				(memberId == Integer.parseInt(ContextListener.property.getProperty("load.serverid"))) ? "(Master)" : "",
				memberId, member.getAddress().getHost(), member.getAddress().getPort(),
				member.getAttribute("db-cluster"));

		if (memberId == Integer.parseInt(ContextListener.property.getProperty("load.serverid"))) {
			GlobalVars.MASTER_CLIENT = true;
			logger.info("Marked as Master Client");
		}
	}
}
