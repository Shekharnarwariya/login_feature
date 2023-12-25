package com.hti.smpp.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.cluster.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Application context listener responsible for initializing properties and connecting to Hazelcast Cluster.
 */

public class ContextListener {
	private static final Logger logger = LoggerFactory.getLogger(ContextListener.class);

	  // Load properties file during class initialization
	
	public static Properties property = new Properties();

	static {
		initializeProperties();
		connectToHazelcastCluster();
	}

	/**
     * Initialize application properties from the "ApplicationResources.properties" file.
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
			ClientConfig config = new ClientConfig();
			// Configure Hazelcast client as needed

			GlobalVars.hazelInstance = HazelcastClient.newHazelcastClient(config);
			GlobalVars.hazelInstance.getCluster().addMembershipListener(new MemberEventListener());

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
				(memberId == IConstants.SERVER_ID) ? "(Master)" : "", memberId, member.getAddress().getHost(),
				member.getAddress().getPort(), member.getAttribute("db-cluster"));

		if (memberId == IConstants.SERVER_ID) {
			GlobalVars.MASTER_CLIENT = true;
			logger.info("Marked as Master Client");
		}
	}
}
