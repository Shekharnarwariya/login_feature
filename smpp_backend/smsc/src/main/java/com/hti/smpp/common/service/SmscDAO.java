package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.contacts.dto.GroupEntry;
import com.hti.smpp.common.contacts.dto.GroupMemberEntry;
import com.hti.smpp.common.request.CustomRequest;
import com.hti.smpp.common.request.GroupMemberRequest;
import com.hti.smpp.common.request.GroupRequest;
import com.hti.smpp.common.request.LimitRequest;
import com.hti.smpp.common.request.SmscBsfmEntryRequest;
import com.hti.smpp.common.request.SmscEntryRequest;
import com.hti.smpp.common.request.SmscLoopingRequest;
import com.hti.smpp.common.request.TrafficScheduleRequest;
import com.hti.smpp.common.smsc.dto.CustomEntry;
import com.hti.smpp.common.smsc.dto.LimitEntry;
import com.hti.smpp.common.smsc.dto.SmscBsfmEntry;
import com.hti.smpp.common.smsc.dto.SmscLooping;
import com.hti.smpp.common.smsc.dto.StatusEntry;
import com.hti.smpp.common.smsc.dto.TrafficScheduleEntry;

@Service
public interface SmscDAO {

	// =======================save============================
	public String smscEntrySave(SmscEntryRequest smscEntryRequest, String username);

	public String saveCustom(CustomRequest customRequest, String username);

	public String saveGroupMember(GroupMemberRequest groupMemberRequest, String username);

	public String saveLimit(LimitRequest limitRequest, String username);

	public String saveLoopingRule(SmscLoopingRequest smscLoopingRequest);

	public String saveSchedule(TrafficScheduleRequest trafficScheduleRequest);

	public String saveSmscBsfm(SmscBsfmEntryRequest smscBsfmEntryRequest, String username);

	public String saveGroup(GroupRequest groupRequest, String username);

	public String updateGroup(GroupRequest groupRequest, String username);

	public List<SmscLooping> listLoopingRule(String username);

	public List<TrafficScheduleEntry> listTrafficSchedule(String username);

	public List<SmscBsfmEntry> listSmscBsfm(String username);

	public List<GroupEntry> listGroup(String username);

	public String updateGroupMember(GroupMemberRequest groupMemberRequest, String username);

	public String deleteGroupMember(int groupMemberId, String username);

	public String updateLimit(int limitId, LimitRequest limitRequest, String username);

	public String deleteLimit(int limitId, String username);

	public String updateSchedule(TrafficScheduleRequest trafficScheduleRequest, String username);

	public String deleteSchedule(int scheduleId, String username);

	public String deleteGroup(int groupId, String username);
	// =================================================================================

	public String update(int smscId, SmscEntryRequest smscEntryRequest, String username);

	public String delete(int smscId, String username);

	public List<StatusEntry> listBound(boolean bound, String username);

	public List<CustomEntry> listCustom(String username);

	public CustomEntry getCustomEntry(int smscId, String username);

	public String updateCustom(int customId, CustomRequest customRequest);

	public String deleteCustom(int customId);

	public List<LimitEntry> listLimit();

	public List<GroupMemberEntry> listGroupMember(int groupId);
	// -------- Smsc Looping Rules -----------------------

	public String updateLoopingRule(SmscLoopingRequest smscLoopingRequest);

	public String deleteLoopingRule(int smscId);

	public SmscLooping getLoopingRule(int smscId);

}
