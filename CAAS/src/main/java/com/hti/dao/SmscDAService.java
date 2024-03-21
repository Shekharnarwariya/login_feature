package com.hti.dao;

import java.util.List;
import java.util.Map;

import com.hti.smpp.common.contacts.dto.GroupEntry;
import com.hti.smpp.common.contacts.dto.GroupMemberEntry;
import com.hti.smpp.common.smsc.dto.SmscEntry;

public interface SmscDAService {
	public Map<Integer, SmscEntry> list();

	public SmscEntry getEntry(int smsc_id);

	public Map<String, Integer> listNames();

	public List<GroupEntry> listGroup();

	public List<GroupMemberEntry> listGroupMember();

	public Map<Integer, Map<Integer, Map<String, String>>> listSchedule();
}
