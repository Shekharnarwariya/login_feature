package com.hti.listener;

import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;

import com.hti.util.GlobalVar;

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
}
