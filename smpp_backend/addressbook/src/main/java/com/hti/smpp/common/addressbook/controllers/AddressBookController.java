package com.hti.smpp.common.addressbook.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.addressbook.request.ContactEntryRequest;
import com.hti.smpp.common.addressbook.request.GroupDataEntryRequest;
import com.hti.smpp.common.addressbook.request.GroupEntryRequest;
import com.hti.smpp.common.addressbook.response.ContactForBulk;
import com.hti.smpp.common.addressbook.services.ContactEntryService;
import com.hti.smpp.common.addressbook.services.GroupDataEntryService;
import com.hti.smpp.common.addressbook.services.GroupEntryService;
import com.hti.smpp.common.contacts.dto.ContactEntry;
import com.hti.smpp.common.contacts.dto.GroupDataEntry;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@OpenAPIDefinition(info = @Info(title = "SMPP AddressBook API", version = "1.0", description = "API for managing SMPP AddressBook..."))
@RestController
@RequestMapping("/api/addressbook")
@Tag(name = "AddressBookController", description = "API's for address book")
public class AddressBookController {

	@Autowired
	private ContactEntryService contactEntryService;

	@Autowired
	private GroupDataEntryService groupDataEntryService;

	@Autowired
	private GroupEntryService entryService;

	@Operation(summary = "Save Contact Entry", description = "Save a new contact entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "ContactEntry Saved Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "204", description = "No Content added to list.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/save/contact-entry", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> saveContactEntry(
			@RequestPart(value = "contactFile", required = true) MultipartFile contactFile,
			@Parameter(description = "Contact Entry request", content = @Content(schema = @Schema(implementation = ContactEntryRequest.class))) @RequestParam(value = "contactEntryRequest", required = true) String contactEntryRequest,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {

		return this.contactEntryService.saveContactEntry(contactEntryRequest, contactFile, username);

	}

	@Operation(summary = "Save Group Data Entry", description = "Save a new group data entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "GroupDataEntry Saved Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "204", description = "No Content added to list.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/save/group-data-entry", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> saveGroupDataEntry(
			@RequestPart(value = "contactNumberFile", required = true) MultipartFile contactNumberFile,
			@Parameter(description = "Group Data Entry request", content = @Content(schema = @Schema(implementation = GroupDataEntryRequest.class))) @RequestParam("groupDataEntryRequest") String groupDataEntryRequest,
			@Parameter(description = "Username in header") @RequestHeader("username") String username) {

		return this.groupDataEntryService.saveGroupData(groupDataEntryRequest, contactNumberFile, username);
	}
	
	@Operation(summary = "Save Group Entry", description = "Save a new group entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "GroupEntry Saved Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "204", description = "No Content added to list.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))
	})
	@PostMapping("/save/group-entry")
	public ResponseEntity<?> saveGroupEntry(@Valid @RequestBody GroupEntryRequest entryRequest,
			@Parameter(description = "Username in header") @RequestHeader("username") String username) {
		return this.entryService.saveGroupEntry(entryRequest, username);
	}
	
	//TODO
	@GetMapping("/get/contact-for-bulk")
	public ResponseEntity<?> contactForBulk(@RequestBody ContactEntryRequest request,
			@RequestHeader("username") String username) {
		ContactForBulk response = this.contactEntryService.contactForBulk(request, username);
		if (response != null) {
			return ResponseEntity.ok(response);
		} else {
			return new ResponseEntity<>("No Contact for Bulk.", HttpStatus.BAD_REQUEST);
		}
	}
	
	//TODO
	@GetMapping("/get/groupdata-for-bulk")
	public ResponseEntity<?> groupDataForBulk(@RequestBody GroupDataEntryRequest request,
			@RequestHeader("username") String username) {
		ContactForBulk response = this.groupDataEntryService.groupDataForBulk(request, username);
		if (response != null) {
			return ResponseEntity.ok(response);
		} else {
			return new ResponseEntity<>("No Contact for Bulk.", HttpStatus.BAD_REQUEST);
		}
	}
	
	//TODO
	@GetMapping("/get/view-search-contact")
	public ResponseEntity<?> viewSearchContact(@RequestBody GroupEntryRequest groupEntryRequest,
			@RequestHeader("username") String username) {
		List<ContactEntry> list = this.contactEntryService.viewSearchContact(groupEntryRequest, username);
		if (list.isEmpty()) {
			return new ResponseEntity<>("No Contact found.", HttpStatus.NOT_FOUND);
		} else if (!list.isEmpty()) {
			return ResponseEntity.ok(list);
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
	
	//TODO
	@GetMapping("/get/proceed-search-contact")
	public ResponseEntity<?> proceedSearchContact(@RequestBody GroupEntryRequest entryRequest,
			@RequestHeader("username") String username) {
		ContactForBulk response = this.contactEntryService.proceedSearchContact(entryRequest, username);
		if (response != null) {
			return ResponseEntity.ok(response);
		} else {
			return new ResponseEntity<>("No Contact for Bulk.", HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/get/view-search-groupdata")
	public ResponseEntity<?> viewSearchGroupData(@RequestBody GroupDataEntryRequest request,
			@RequestHeader("username") String username) {
		List<GroupDataEntry> list = this.groupDataEntryService.viewSearchGroupData(request, username);

		if (list.isEmpty()) {
			return new ResponseEntity<>("No data found!", HttpStatus.NOT_FOUND);
		} else if (!list.isEmpty()) {
			return ResponseEntity.ok(list);
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}

	@GetMapping("/get/proceed-search-groupdata")
	public ResponseEntity<?> proceedSearchGroupData(@RequestBody GroupDataEntryRequest request,
			@RequestHeader("username") String username) {
		ContactForBulk response = this.groupDataEntryService.proceedSearchGroupData(request, username);
		if (response != null) {
			return ResponseEntity.ok(response);
		} else {
			return new ResponseEntity<>("No Contact for Bulk.", HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping("/update/contact")
	public ResponseEntity<?> modifyContactUpdate(@RequestBody ContactEntryRequest request,
			@RequestHeader("username") String username) {
		return this.contactEntryService.modifyContactUpdate(request, username);
	}

	@DeleteMapping("/delete/contact")
	public ResponseEntity<?> modifyContactDelete(@RequestBody List<Integer> ids,
			@RequestHeader("username") String username) {
		return this.contactEntryService.modifyContactDelete(ids, username);
	}

	@GetMapping("/export/contact")
	public ResponseEntity<?> modifyContactExport(@RequestBody ContactEntryRequest request,
			@RequestHeader("username") String username) {
		return this.contactEntryService.modifyContactExport(request, username);
	}

	@PutMapping("/update/group-data-entry")
	public ResponseEntity<?> modifyGroupDataUpdate(@RequestBody GroupDataEntryRequest request,
			@RequestHeader("username") String username) {
		return this.groupDataEntryService.modifyGroupDataUpdate(request, username);
	}

	@DeleteMapping("/delete/group-data-entry")
	public ResponseEntity<?> modifyGroupDataDelete(@RequestBody List<Integer> ids,
			@RequestHeader("username") String username) {
		return this.groupDataEntryService.modifyGroupDataDelete(ids, username);
	}

	@GetMapping("/export/group-data-entry")
	public ResponseEntity<?> modifyGroupDataExport(@RequestBody GroupDataEntryRequest request,
			@RequestHeader("username") String username) {
		return this.groupDataEntryService.modifyGroupDataExport(request, username);
	}

	@PutMapping("/update/group-entry")
	public ResponseEntity<?> modifyGroupEntryUpdate(@RequestBody GroupEntryRequest groupEntryRequest,
			@RequestHeader("username") String username) {
		return this.entryService.modifyGroupEntryUpdate(groupEntryRequest, username);
	}

	@DeleteMapping("/delete/group-entry")
	public ResponseEntity<?> modifyGroupEntryDelete(@RequestBody GroupEntryRequest groupEntryRequest,
			@RequestHeader("username") String username) {
		return this.entryService.modifyGroupEntryDelete(groupEntryRequest, username);
	}

	@GetMapping("/search/group-data/{groupId}")
	public ResponseEntity<?> editGroupDataSearch(@PathVariable("groupId") int groupId,
			@RequestHeader("username") String username) {
		return this.groupDataEntryService.editGroupDataSearch(groupId, username);
	}

	@GetMapping("/listgroup")
	public ResponseEntity<?> listGroup(@RequestParam("purpose") String purpose,
			@RequestParam("groupData") String groupData, @RequestHeader("username") String username) {
		return this.entryService.listGroup(purpose, groupData, username);
	}

}
