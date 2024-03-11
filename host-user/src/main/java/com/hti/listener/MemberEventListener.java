package com.hti.listener;

import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hti.util.GlobalVars;

public class MemberEventListener implements MembershipListener {
	private Logger logger = LoggerFactory.getLogger(MemberEventListener.class);

	@Override
	public void memberAdded(MembershipEvent membershipEvent) {
		logger.info("Member Connected: " + membershipEvent.getMember().getAddress().getHost());
		checkMasterMember();
	}

	@Override
	public void memberRemoved(MembershipEvent membershipEvent) {
		logger.info("Member Removed: " + membershipEvent.getMember().getAddress().getHost());
		checkMasterMember();
	}

	

	private void checkMasterMember() {
		System.out.println(" *********** Members *****************");
		Set<Member> members = GlobalVars.hazelInstance.getCluster().getMembers();
		Iterator<Member> itr = members.iterator();
		int i = 1;
		while (itr.hasNext()) {
			Member member = itr.next();
			if (i == 1) {
				String db_cluster = member.getAttribute("db-cluster");
				if (db_cluster != null && db_cluster.equalsIgnoreCase("true")) {
					GlobalVars.DB_CLUSTER = true;
				} else {
					GlobalVars.DB_CLUSTER = false;
				}
				System.out.println("Master:-> Id: " + member.getAttribute("member-id") + " Address:"
						+ member.getAddress().getHost() + ":" + member.getAddress().getPort() + " DB_CLUSTER: "
						+ GlobalVars.DB_CLUSTER);
				if (Integer.parseInt(member.getAttribute("member-id")) == GlobalVars.SERVER_ID) {
					GlobalVars.MASTER_CLIENT = true;
					logger.info("******* Marked As Master Client ********");
				} else {
					GlobalVars.MASTER_CLIENT = false;
					logger.info("******* Server is Not Master Client ********");
				}
			} else {
				System.out.println("Member:-> Id: " + member.getAttribute("member-id") + " Address:"
						+ member.getAddress().getHost() + ":" + member.getAddress().getPort());
			}
			i++;
		}
	}
}
