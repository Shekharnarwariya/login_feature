package com.hti.smpp.common.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.contacts.dto.GroupEntry;
import com.hti.smpp.common.contacts.dto.GroupMemberEntry;
import com.hti.smpp.common.request.CustomRequest;
import com.hti.smpp.common.request.GroupMemberRequest;
import com.hti.smpp.common.request.GroupRequest;
import com.hti.smpp.common.request.LimitRequest;
import com.hti.smpp.common.request.SmscEntryRequest;
import com.hti.smpp.common.request.SmscLoopingRequest;
import com.hti.smpp.common.request.TrafficScheduleRequest;
import com.hti.smpp.common.service.SmscDAO;
import com.hti.smpp.common.smsc.dto.CustomEntry;
import com.hti.smpp.common.smsc.dto.LimitEntry;
import com.hti.smpp.common.smsc.dto.SmscLooping;
import com.hti.smpp.common.smsc.dto.StatusEntry;
import com.hti.smpp.common.smsc.dto.TrafficScheduleEntry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/smsc")
public class SmscController {

	@Autowired
	private SmscDAO smscDAOImpl;

	@Operation(summary = "Check Service Status", description = "Retrieve the status of the service.")
	@GetMapping("/status")
	public ResponseEntity<String> getStatus() {
		return new ResponseEntity<>("Service is up and running", HttpStatus.OK);
	}

	@Operation(summary = "Save SMS Entry", description = "Save the SMS entry to the system.")
	@PostMapping("/smsc")
	public ResponseEntity<String> saveSmscEntry(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request body for SMS Entry", required = true, content = @Content(schema = @Schema(implementation = SmscEntryRequest.class))) @RequestBody SmscEntryRequest smscEntryRequest,
			@RequestHeader("username") String username) {
		String result = smscDAOImpl.save(smscEntryRequest, username);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@Operation(summary = "Save Custom Entry", description = "Save the custom entry to the system.")
	@PostMapping("/custom")
	public ResponseEntity<String> saveCustom(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request body for Custom Entry", required = true, content = @Content(schema = @Schema(implementation = CustomRequest.class))) @RequestBody CustomRequest customRequest) {
		String result = smscDAOImpl.saveCustom(customRequest);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@Operation(summary = "Save Group", description = "Save the group in the system.")
	@PostMapping("/group")
	public ResponseEntity<String> saveGroup(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request body for Group", required = true, content = @Content(schema = @Schema(implementation = GroupRequest.class))) @RequestBody GroupRequest groupRequest) {
		String result = smscDAOImpl.saveGroup(groupRequest);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@Operation(summary = "Save Group Member", description = "Save the group member in the system.")
	@PostMapping("/groupMember")
	public ResponseEntity<String> saveGroupMember(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request body for Group Member", required = true, content = @Content(schema = @Schema(implementation = GroupMemberRequest.class))) @RequestBody GroupMemberRequest groupMemberRequest) {
		String result = smscDAOImpl.saveGroupMember(groupMemberRequest);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@Operation(summary = "Save Limit", description = "Save the limit in the system.")
	@PostMapping("/limit")
	public ResponseEntity<String> saveLimit(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request body for Limit", required = true, content = @Content(schema = @Schema(implementation = LimitRequest.class))) @RequestBody LimitRequest limitRequest) {
		String result = smscDAOImpl.saveLimit(limitRequest);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@Operation(summary = "Update SMS Entry", description = "Update the SMS entry in the system.")
	@PutMapping("/smsc/{smscId}")
	public ResponseEntity<String> updateSmscEntry(@PathVariable int smscId,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request body for SMS Entry", required = true, content = @Content(schema = @Schema(implementation = SmscEntryRequest.class))) @RequestBody SmscEntryRequest smscEntryRequest) {
		String result = smscDAOImpl.update(smscId, smscEntryRequest);
		if (result == null) {
			return new ResponseEntity<>("Smsc entry not found for id: " + smscId, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Operation(summary = "Delete SMS Entry", description = "Delete the SMS entry from the system.")
	@DeleteMapping("smsc/{id}")
	public ResponseEntity<String> deleteSmscEntry(@PathVariable int id) {
		String result = smscDAOImpl.delete(id);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Operation(summary = "List Bound Status", description = "List status entries based on the bound parameter.")
	@GetMapping("/status/listBound")
	public ResponseEntity<List<StatusEntry>> listBound(@RequestParam boolean bound) {
		List<StatusEntry> statusEntries = smscDAOImpl.listBound(bound);
		return new ResponseEntity<>(statusEntries, HttpStatus.OK);
	}

	@Operation(summary = "List Custom Entries", description = "List all custom entries.")
	@GetMapping("/custom/listCustom")
	public ResponseEntity<List<CustomEntry>> listCustom() {
		List<CustomEntry> customEntries = smscDAOImpl.listCustom();
		return new ResponseEntity<>(customEntries, HttpStatus.OK);
	}

	@Operation(summary = "Get Custom Entry", description = "Get a custom entry based on the provided ID.")
	@GetMapping("/custom/{smscId}")
	public ResponseEntity<CustomEntry> getCustomEntry(@PathVariable int smscId) {
		CustomEntry customEntry = smscDAOImpl.getCustomEntry(smscId);
		if (customEntry != null) {
			return new ResponseEntity<>(customEntry, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "Update Custom Entry", description = "Update the custom entry with the provided ID.")
	@PutMapping("/custom/{customId}")
	public ResponseEntity<String> updateCustom(@PathVariable int customId, @RequestBody CustomRequest customRequest) {
		smscDAOImpl.updateCustom(customId, customRequest);
		return new ResponseEntity<>("CustomEntry with ID " + customId + " successfully updated.", HttpStatus.OK);
	}

	@Operation(summary = "Delete Custom Entry", description = "Delete the custom entry with the provided ID.")
	@DeleteMapping("/customs/{customId}")
	public ResponseEntity<String> deleteCustom(@PathVariable int customId) {
		String result = smscDAOImpl.deleteCustom(customId);
		return new ResponseEntity<>("Custom with ID " + customId + " successfully deleted", HttpStatus.OK);
	}

	@Operation(summary = "Update Limit", description = "Update the limit with the provided ID.")
	@PostMapping("/updateLimit/{limitId}")
	public String updateLimit(@PathVariable int limitId, @RequestBody LimitRequest limitRequest) {
		String updateLimitResult = smscDAOImpl.updateLimit(limitId, limitRequest);
		if (updateLimitResult != null) {
			return "Limit with ID " + limitId + " updated successfully";
		} else {
			return "Failed to update limit with ID " + limitId;
		}
	}

	@Operation(summary = "Delete Limit Entry", description = "Delete the limit entry with the provided ID.")
	@DeleteMapping("/deleteLimit/{limitId}")
	public String deleteLimit(@PathVariable int limitId) {
		smscDAOImpl.deleteLimit(limitId);
		return "LimitEntry with ID " + limitId + " deleted successfully";
	}

	@Operation(summary = "List Limit Entries", description = "Get a list of all limit entries.")
	@GetMapping("/list")
	public List<LimitEntry> listLimit() {
		List<LimitEntry> limitEntries = smscDAOImpl.listLimit();
		return limitEntries;
	}

	@Operation(summary = "Update Group", description = "Update the group with the provided request.")
	@PutMapping("group/update")
	public ResponseEntity<String> updateGroup(@RequestBody GroupRequest groupRequest) {
		String result = smscDAOImpl.updateGroup(groupRequest);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "List Groups", description = "Get a list of all groups.")
	@GetMapping("group/list")
	public ResponseEntity<List<GroupEntry>> listGroups() {
		List<GroupEntry> groupEntries = smscDAOImpl.listGroup();
		return ResponseEntity.ok(groupEntries);
	}

	@Operation(summary = "Update Group Member", description = "Update the group member with the provided request.")
	@PutMapping("groupmember/update")
	public ResponseEntity<String> updateGroupMember(@RequestBody GroupMemberRequest groupMemberRequest) {
		String result = smscDAOImpl.updateGroupMember(groupMemberRequest);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Delete Group Member", description = "Delete the group member with the provided ID.")
	@DeleteMapping("groupmember/delete/{groupMemberId}")
	public ResponseEntity<String> deleteGroupMember(@PathVariable int groupMemberId) {
		String result = smscDAOImpl.deleteGroupMember(groupMemberId);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "List Group Members", description = "Get a list of all group members for the provided group ID.")
	@GetMapping("groupmember/list/{groupId}")
	public ResponseEntity<List<GroupMemberEntry>> listGroupMember(@PathVariable int groupId) {
		List<GroupMemberEntry> groupMembers = smscDAOImpl.listGroupMember(groupId);
		return ResponseEntity.ok(groupMembers);
	}

	@Operation(summary = "Save Schedule", description = "Save the traffic schedule with the provided request.")
	@PostMapping("schedule/save")
	public ResponseEntity<String> saveSchedule(@RequestBody TrafficScheduleRequest trafficScheduleRequest) {
		String result = smscDAOImpl.saveSchedule(trafficScheduleRequest);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Operation(summary = "Update Schedule", description = "Update the schedule with the provided request.")
	@PostMapping("schedule/updateSchedule")
	public ResponseEntity<String> updateSchedule(@RequestBody TrafficScheduleRequest trafficScheduleRequest) {
		String result = smscDAOImpl.updateSchedule(trafficScheduleRequest);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Operation(summary = "Delete Schedule", description = "Delete the schedule with the provided ID.")
	@DeleteMapping("schedule/deleteSchedule/{scheduleId}")
	public ResponseEntity<String> deleteSchedule(@PathVariable int scheduleId) {
		String result = smscDAOImpl.deleteSchedule(scheduleId);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Operation(summary = "List Schedules", description = "Get a list of all traffic schedules.")
	@GetMapping("traffic/listSchedule")
	public ResponseEntity<Map<String, TrafficScheduleEntry>> listSchedule() {
		Map<String, TrafficScheduleEntry> scheduleMap = smscDAOImpl.listSchedule();
		return new ResponseEntity<>(scheduleMap, HttpStatus.OK);
	}

	@Operation(summary = "Save Looping Rule", description = "Save the looping rule with the provided request.")
	@PostMapping("smsclooping/saveLoopingRule")
	public String saveLoopingRule(@RequestBody SmscLoopingRequest smscLoopingRequest) {
		// Implementation
		return smscDAOImpl.saveLoopingRule(smscLoopingRequest);
	}

	@Operation(summary = "Update Looping Rule", description = "Update the looping rule with the provided request.")
	@PutMapping("smsclooping/updateLoopingRule")
	public String updateLoopingRule(@RequestBody SmscLoopingRequest smscLoopingRequest) {
		// Implementation
		return smscDAOImpl.updateLoopingRule(smscLoopingRequest);
	}

	@Operation(summary = "Delete Looping Rule", description = "Delete the looping rule with the provided ID.")
	@DeleteMapping("smsclooping/deleteLoopingRule/{smscId}")
	public String deleteLoopingRule(@PathVariable int smscId) {
		// Implementation
		return smscDAOImpl.deleteLoopingRule(smscId);
	}

	@Operation(summary = "Get Looping Rule", description = "Get the looping rule with the provided ID.")
	@GetMapping("smsclooping/getLoopingRule/{smscId}")
	public ResponseEntity<?> getLoopingRule(@PathVariable int smscId) {
		SmscLooping loopingRule = smscDAOImpl.getLoopingRule(smscId);
		return ResponseEntity.ok(loopingRule);
	}

	@Operation(summary = "List Looping Rules", description = "Get a list of all looping rules.")
	@GetMapping("smsclooping/listLoopingRule")
	public ResponseEntity<?> listLoopingRule() {
		List<SmscLooping> loopingRules = smscDAOImpl.listLoopingRule();
		return ResponseEntity.ok(loopingRules);
	}

}
