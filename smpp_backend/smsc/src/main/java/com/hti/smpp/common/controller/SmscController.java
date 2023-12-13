package com.hti.smpp.common.controller;

import java.util.List;

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
import com.hti.smpp.common.request.CustomRequest;
import com.hti.smpp.common.request.GroupMemberRequest;
import com.hti.smpp.common.request.GroupRequest;
import com.hti.smpp.common.request.LimitRequest;
import com.hti.smpp.common.request.SmscBsfmEntryRequest;
import com.hti.smpp.common.request.SmscEntryRequest;
import com.hti.smpp.common.request.SmscLoopingRequest;
import com.hti.smpp.common.request.TrafficScheduleRequest;
import com.hti.smpp.common.service.SmscService;
import com.hti.smpp.common.smsc.dto.CustomEntry;
import com.hti.smpp.common.smsc.dto.LimitEntry;
import com.hti.smpp.common.smsc.dto.SmscBsfmEntry;
import com.hti.smpp.common.smsc.dto.SmscEntry;
import com.hti.smpp.common.smsc.dto.SmscLooping;
import com.hti.smpp.common.smsc.dto.TrafficScheduleEntry;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/smsc")
@OpenAPIDefinition(info = @Info(title = "SMPP  Smsc API", version = "1.0", description = "API for managing SMPP  Smsc"))
public class SmscController {

	@Autowired
	private SmscService smscDAOImpl;

	@Operation(summary = "Check Service Status", description = "Retrieve the status of the service.")
	@GetMapping("/status")
	public ResponseEntity<String> getStatus() {
		return new ResponseEntity<>("Service is up and running", HttpStatus.OK);
	}

	@Operation(summary = "Save SMS Entry", description = "Save the SMS entry to the system.")
	@PostMapping("/save")
	public ResponseEntity<String> saveSmscEntry(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request body for SMS Entry", required = true, content = @Content(schema = @Schema(implementation = SmscEntryRequest.class))) @RequestBody SmscEntryRequest smscEntryRequest,
			@RequestHeader("username") String username) {
		String result = smscDAOImpl.smscEntrySave(smscEntryRequest, username);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@Operation(summary = "Save Custom Entry", description = "Save the custom entry to the system.")
	@PostMapping("/custom/save")
	public ResponseEntity<String> saveCustom(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request body for Custom Entry", required = true, content = @Content(schema = @Schema(implementation = CustomRequest.class))) @RequestBody CustomRequest customRequest,
			@RequestHeader("username") String username) {
		String result = smscDAOImpl.saveCustom(customRequest, username);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@Operation(summary = "Save SmscBsfmEntry ", description = "Save the SmscBsfmEntry  to the system.")
	@PostMapping("/bsfm/save")
	public ResponseEntity<String> saveSmscBsfm(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request body for SmscBsfmEntry ", required = true, content = @Content(schema = @Schema(implementation = SmscBsfmEntryRequest.class))) @RequestBody SmscBsfmEntryRequest smscBsfmEntryRequest,
			@RequestHeader("username") String username) {
		String result = smscDAOImpl.saveSmscBsfm(smscBsfmEntryRequest, username);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@Operation(summary = "Save Group", description = "Save the group in the system.")
	@PostMapping("/group/save")
	public ResponseEntity<String> saveGroup(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request body for Group", required = true, content = @Content(schema = @Schema(implementation = GroupRequest.class))) @RequestBody GroupRequest groupRequest,
			@RequestHeader("username") String username) {
		String result = smscDAOImpl.saveGroup(groupRequest, username);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@Operation(summary = "Save Group Member", description = "Save the group member in the system.")
	@PostMapping("/groupMember/save")
	public ResponseEntity<String> saveGroupMember(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request body for Group Member", required = true, content = @Content(schema = @Schema(implementation = GroupMemberRequest.class))) @RequestBody GroupMemberRequest groupMemberRequest,
			@RequestHeader("username") String username) {
		String result = smscDAOImpl.saveGroupMember(groupMemberRequest, username);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@Operation(summary = "Save Limit", description = "Save the limit in the system.")
	@PostMapping("/limit/save")
	public ResponseEntity<String> saveLimit(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request body for Limit", required = true, content = @Content(schema = @Schema(implementation = LimitRequest.class))) @RequestBody LimitRequest limitRequest,
			@RequestHeader("username") String username) {
		String result = smscDAOImpl.saveLimit(limitRequest, username);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@Operation(summary = "Update SMS Entry", description = "Update the SMS entry in the system.")
	@PutMapping("/update/{smscId}")
	public ResponseEntity<String> updateSmscEntry(@PathVariable int smscId,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request body for SMS Entry", required = true, content = @Content(schema = @Schema(implementation = SmscEntryRequest.class))) @RequestBody SmscEntryRequest smscEntryRequest,
			@RequestHeader("username") String username) {
		String result = smscDAOImpl.smscupdate(smscId, smscEntryRequest, username);
		if (result == null) {
			return new ResponseEntity<>("Smsc entry not found for id: " + smscId, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Operation(summary = "Delete SMS Entry", description = "Delete the SMS entry from the system.")
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<String> deleteSmscEntry(@PathVariable int id, @RequestHeader("username") String username) {
		String result = smscDAOImpl.smscdelete(id, username);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Operation(summary = "Get Custom Entry", description = "Get a custom entry based on the provided ID.")
	@GetMapping("/custom/{smscId}")
	public ResponseEntity<CustomEntry> getCustomEntry(@PathVariable int smscId,
			@RequestHeader("username") String username) {
		CustomEntry customEntry = smscDAOImpl.getCustomEntry(smscId, username);
		if (customEntry != null) {
			return new ResponseEntity<>(customEntry, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "Update Custom Entry", description = "Update the custom entry with the provided ID.")
	@PutMapping("/custom/{customId}")
	public ResponseEntity<String> updateCustom(@PathVariable int customId, @RequestBody CustomRequest customRequest,
			@RequestHeader("username") String username) {
		smscDAOImpl.updateCustom(customId, customRequest, username);
		return new ResponseEntity<>("CustomEntry with ID " + customId + " successfully updated.", HttpStatus.OK);
	}

	@Operation(summary = "Delete Custom Entry", description = "Delete the custom entry with the provided ID.")
	@DeleteMapping("/custom/{customId}")
	public ResponseEntity<String> deleteCustom(@PathVariable int customId, @RequestHeader("username") String username) {
		String result = smscDAOImpl.deleteCustom(customId, username);
		return new ResponseEntity<>("Custom with ID " + customId + " successfully deleted", HttpStatus.OK);
	}

	@Operation(summary = "Update Limit", description = "Update the limit with the provided ID.")
	@PutMapping("/updateLimit/{limitId}")
	public String updateLimit(@PathVariable int limitId, @RequestBody LimitRequest limitRequest,
			@RequestHeader("username") String username) {
		String updateLimitResult = smscDAOImpl.updateLimit(limitId, limitRequest, username);
		if (updateLimitResult != null) {
			return "Limit with ID " + limitId + " updated successfully";
		} else {
			return "Failed to update limit with ID " + limitId;
		}
	}

	@Operation(summary = "Delete Limit Entry", description = "Delete the limit entry with the provided ID.")
	@DeleteMapping("/deleteLimit/{limitId}")
	public String deleteLimit(@PathVariable int limitId, @RequestHeader("username") String username) {
		smscDAOImpl.deleteLimit(limitId, username);
		return "LimitEntry with ID " + limitId + " deleted successfully";
	}

	@Operation(summary = "List Limit Entries", description = "Get a list of all limit entries.")
	@GetMapping("/list")
	public List<LimitEntry> listLimit(@RequestHeader("username") String username) {
		List<LimitEntry> limitEntries = smscDAOImpl.listLimit(username);
		return limitEntries;
	}

	@Operation(summary = "Update Group", description = "Update the group with the provided request.")
	@PutMapping("/group/update")
	public ResponseEntity<String> updateGroup(@RequestBody GroupRequest groupRequest,
			@RequestHeader("username") String username) {
		String result = smscDAOImpl.updateGroup(groupRequest, username);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "List Groups", description = "Get a list of all groups.")
	@GetMapping("/group/list")
	public ResponseEntity<List<GroupEntry>> listGroups(@RequestHeader("username") String username) {
		List<GroupEntry> groupEntries = smscDAOImpl.listGroup(username);
		return ResponseEntity.ok(groupEntries);
	}

	@Operation(summary = "Update Group Member", description = "Update the group member with the provided request.")
	@PutMapping("/groupmember/update")
	public ResponseEntity<String> updateGroupMember(@RequestBody GroupMemberRequest groupMemberRequest,
			@RequestHeader("username") String username) {
		String result = smscDAOImpl.updateGroupMember(groupMemberRequest, username);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Delete Group Member", description = "Delete the group member with the provided ID.")
	@DeleteMapping("/groupmember/delete/{groupMemberId}")
	public ResponseEntity<String> deleteGroupMember(@RequestParam int groupMemberId,
			@RequestHeader("username") String username) {
		String result = smscDAOImpl.deleteGroupMember(groupMemberId, username);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Save Schedule", description = "Save the traffic schedule with the provided request.")
	@PostMapping("/schedule/save")
	public ResponseEntity<String> saveSchedule(@RequestBody TrafficScheduleRequest trafficScheduleRequest) {
		String result = smscDAOImpl.saveSchedule(trafficScheduleRequest);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Operation(summary = "Update Schedule", description = "Update the schedule with the provided request.")
	@PutMapping("/schedule/update")
	public ResponseEntity<String> updateSchedule(@RequestBody TrafficScheduleRequest trafficScheduleRequest,
			@RequestHeader("username") String username) {
		String result = smscDAOImpl.updateSchedule(trafficScheduleRequest, username);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Operation(summary = "Delete Schedule", description = "Delete the schedule with the provided ID.")
	@DeleteMapping("/schedule/delete/{scheduleId}")
	public ResponseEntity<String> deleteSchedule(@PathVariable int scheduleId,
			@RequestHeader("username") String username) {
		String result = smscDAOImpl.deleteSchedule(scheduleId, username);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Operation(summary = "Save Looping Rule", description = "Save the looping rule with the provided request.")
	@PostMapping("/looping/save")
	public String saveLoopingRule(@RequestBody SmscLoopingRequest smscLoopingRequest) {
		return smscDAOImpl.saveLoopingRule(smscLoopingRequest);
	}

	@Operation(summary = "Update Looping Rule", description = "Update the looping rule with the provided request.")
	@PutMapping("/looping/update")
	public String updateLoopingRule(@RequestBody SmscLoopingRequest smscLoopingRequest,
			@RequestHeader("username") String username) {
		return smscDAOImpl.loopingRuleupdate(smscLoopingRequest, username);
	}

	@Operation(summary = "Delete Looping Rule", description = "Delete the looping rule with the provided ID.")
	@DeleteMapping("smsclooping/deleteLoopingRule/{smscId}")
	public String deleteLoopingRule(@PathVariable int smscId, @RequestHeader("username") String username) {
		return smscDAOImpl.loopingRuledelete(smscId, username);
	}

	@Operation(summary = "List Looping Rules", description = "Get a list of all looping rules.")
	@GetMapping("/looping/list")
	public ResponseEntity<?> listLoopingRule(@RequestHeader("username") String username) {
		List<SmscLooping> loopingRules = smscDAOImpl.listLoopingRule(username);
		return ResponseEntity.ok(loopingRules);
	}

	@Operation(summary = "List Traffic Schedule", description = "Get a list of traffic schedule entries.")
	@GetMapping("/trafficSchedule/list")
	public ResponseEntity<?> listTrafficSchedule(@RequestHeader("username") String username) {
		List<TrafficScheduleEntry> trafficScheduleEntries = smscDAOImpl.listTrafficSchedule(username);
		return ResponseEntity.ok(trafficScheduleEntries);
	}

	@Operation(summary = "List SMSB BSFM Entries", description = "Get a list of SMSB BSFM entries.")
	@GetMapping("/bsfm/list")
	public ResponseEntity<?> listSmscBsfm(@RequestHeader("username") String username) {
		List<SmscBsfmEntry> smscBsfmEntries = smscDAOImpl.listSmscBsfm(username);
		return ResponseEntity.ok(smscBsfmEntries);
	}

	@Operation(summary = "Delete Group", description = "Delete a group by ID.")
	@DeleteMapping("/group/delete/{groupId}")
	public ResponseEntity<String> deleteGroup(@PathVariable int groupId, @RequestHeader("username") String username) {
		String resultMessage = smscDAOImpl.deleteGroup(groupId, username);
		return ResponseEntity.ok(resultMessage);
	}

	@Operation(summary = "Update an SMS entry", description = "Update an existing SMS entry by providing the entry details.")
	@PutMapping("/update")
	public ResponseEntity<String> bsfmUpdate(@RequestBody SmscBsfmEntryRequest smscBsfmEntryRequest,
			@RequestHeader("username") String username) {
		return smscDAOImpl.bsfmupdate(smscBsfmEntryRequest, username);
	}

	@Operation(summary = "Delete an SMS entry", description = "Delete an existing SMS entry by providing its ID.")
	@DeleteMapping("/bsfm/delete/{id}")
	public ResponseEntity<String> bsfmDelete(
			@Parameter(description = "ID of the SMS entry to be deleted", required = true) @PathVariable int id,
			@RequestHeader("username") String username) {
		return smscDAOImpl.bsfmdelete(id, username);
	}

	@Operation(summary = "Get an SMS entry by ID", description = "Retrieve an SMS entry by providing its ID.")
	@GetMapping("/get")
	public ResponseEntity<SmscEntry> getSmscEntry(
			@Parameter(description = "ID of the SMS entry to retrieve", required = true) @RequestParam int id,
			@Parameter(description = "Username making the request", required = true) @RequestHeader("username") String username) {
		return smscDAOImpl.getSmscEntry(id, username);
	}

	@Operation(summary = "Get a group member by ID", description = "Retrieve a group member by providing its ID.")
	@GetMapping("/groupMember/{id}")
	public ResponseEntity<?> getGroupMember(
			@Parameter(description = "ID of the group member to retrieve", required = true) @PathVariable int id,
			@Parameter(description = "Username making the request", required = true) @RequestHeader("username") String username) {
		return smscDAOImpl.getGroupMember(id, username);
	}
}
