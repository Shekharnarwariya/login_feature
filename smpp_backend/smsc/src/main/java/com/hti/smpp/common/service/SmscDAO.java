package com.hti.smpp.common.service;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.contacts.dto.GroupEntry;
import com.hti.smpp.common.contacts.dto.GroupMemberEntry;
import com.hti.smpp.common.request.CustomRequest;
import com.hti.smpp.common.request.GroupMemberRequest;
import com.hti.smpp.common.request.GroupRequest;
import com.hti.smpp.common.request.SmscEntryRequest;
import com.hti.smpp.common.smsc.dto.CustomEntry;
import com.hti.smpp.common.smsc.dto.LimitEntry;
import com.hti.smpp.common.smsc.dto.SmscEntry;
import com.hti.smpp.common.smsc.dto.SmscLooping;
import com.hti.smpp.common.smsc.dto.StatusEntry;
import com.hti.smpp.common.smsc.dto.TrafficScheduleEntry;

@Service
public interface SmscDAO {

	public String save(SmscEntryRequest smscEntryRequest, String username);

	public void update(SmscEntry entry);

	public void delete(SmscEntry entry);

	public List<StatusEntry> listBound(boolean bound);

	public List<CustomEntry> listCustom();

	public CustomEntry getCustomEntry(int smscId);

	public String saveCustom(CustomRequest customRequest);

	public void updateCustom(CustomEntry entry);

	public void deleteCustom(CustomEntry entry);

	public void saveLimit(LimitEntry entry);

	public void updateLimit(List<LimitEntry> list);

	public void deleteLimit(List<LimitEntry> list);

	public List<LimitEntry> listLimit();

	// --------- Grouping ---------------
	public String saveGroup(GroupRequest groupRequest);

	public void updateGroup(List<GroupEntry> list);

	public void deleteGroup(GroupEntry entry);

	public List<GroupEntry> listGroup();

	// ---------- Group Members ----------
	public String saveGroupMember(GroupMemberRequest groupMemberRequest);

	public void updateGroupMember(List<GroupMemberEntry> list);

	public void deleteGroupMember(Collection<GroupMemberEntry> list);

	public List<GroupMemberEntry> listGroupMember(int groupId);

	// ------ traffic schedule -------------
	public void saveSchedule(List<TrafficScheduleEntry> list);

	public void updateSchedule(List<TrafficScheduleEntry> list);

	public void deleteSchedule(List<TrafficScheduleEntry> list);

	public List<TrafficScheduleEntry> listSchedule();

	// -------- Smsc Looping Rules -----------------------
	public void saveLoopingRule(SmscLooping entry);

	public void updateLoopingRule(SmscLooping entry);

	public void deleteLoopingRule(SmscLooping entry);

	public SmscLooping getLoopingRule(int smscId);

	public List<SmscLooping> listLoopingRule();
}
