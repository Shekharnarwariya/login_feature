package com.hti.dao;

import java.util.List;

import com.hti.smpp.common.contacts.dto.GroupEntry;
import com.hti.smpp.common.contacts.dto.GroupMemberEntry;
import com.hti.smpp.common.smsc.dto.SmscEntry;
import com.hti.smpp.common.smsc.dto.TrafficScheduleEntry;

public interface SmscDAO {
	public List<SmscEntry> list();

	public List<SmscEntry> listNames();

	public SmscEntry getEntry(int smsc_id);

	public List<GroupMemberEntry> listGroupMember();

	public List<GroupEntry> listGroup();

	public List<TrafficScheduleEntry> listSchedule();
}
