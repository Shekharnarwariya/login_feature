package com.hti.smpp.common.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.contacts.dto.GroupEntry;
import com.hti.smpp.common.contacts.dto.GroupMemberEntry;
import com.hti.smpp.common.request.CustomRequest;
import com.hti.smpp.common.request.GroupMemberRequest;
import com.hti.smpp.common.request.GroupRequest;
import com.hti.smpp.common.request.LimitRequest;
import com.hti.smpp.common.request.SmscEntryRequest;
import com.hti.smpp.common.request.SmscLoopingRequest;
import com.hti.smpp.common.request.TrafficScheduleRequest;
import com.hti.smpp.common.smsc.dto.CustomEntry;
import com.hti.smpp.common.smsc.dto.LimitEntry;
import com.hti.smpp.common.smsc.dto.SmscLooping;
import com.hti.smpp.common.smsc.dto.StatusEntry;
import com.hti.smpp.common.smsc.dto.TrafficScheduleEntry;

@Service
public interface SmscDAO {

	public String save(SmscEntryRequest smscEntryRequest, String username);

	public String update(int smscId, SmscEntryRequest smscEntryRequest, String username);

	public String delete(int smscId, String username);

	public List<StatusEntry> listBound(boolean bound, String username);

	public List<CustomEntry> listCustom(String username);

	public CustomEntry getCustomEntry(int smscId, String username);

	public String saveCustom(CustomRequest customRequest, String username);

	public String updateCustom(int customId, CustomRequest customRequest);

	public String deleteCustom(int customId);

	public String saveLimit(LimitRequest limitRequest, String username);

	public String updateLimit(int limitId, LimitRequest limitRequest);

	public String deleteLimit(int limitId);

	public List<LimitEntry> listLimit();

	// --------- Grouping ---------------
	public String saveGroup(GroupRequest groupRequest, String username);

	public String updateGroup(GroupRequest groupRequest);

	public String deleteGroup(int groupId);

	public List<GroupEntry> listGroup();

	// ---------- Group Members ----------
	public String saveGroupMember(GroupMemberRequest groupMemberRequest, String username);

	public String updateGroupMember(GroupMemberRequest groupMemberRequest);

	public String deleteGroupMember(int groupMemberId);

	public List<GroupMemberEntry> listGroupMember(int groupId);

	// ------ traffic schedule -------------
	public String saveSchedule(TrafficScheduleRequest trafficScheduleRequest);

	public String updateSchedule(TrafficScheduleRequest trafficScheduleRequest);

	public String deleteSchedule(int scheduleId);

	public Map<String, TrafficScheduleEntry> listSchedule();

	// -------- Smsc Looping Rules -----------------------
	public String saveLoopingRule(SmscLoopingRequest smscLoopingRequest);

	public String updateLoopingRule(SmscLoopingRequest smscLoopingRequest);

	public String deleteLoopingRule(int smscId);

	public SmscLooping getLoopingRule(int smscId);

	public List<SmscLooping> listLoopingRule();
}
