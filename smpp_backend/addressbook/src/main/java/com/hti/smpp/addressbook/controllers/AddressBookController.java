package com.hti.smpp.addressbook.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.addressbook.request.ContactEntryRequest;
import com.hti.smpp.addressbook.request.GroupDataEntryRequest;
import com.hti.smpp.addressbook.request.GroupEntryRequest;
import com.hti.smpp.addressbook.response.ContactForBulk;
import com.hti.smpp.addressbook.services.ContactEntryService;
import com.hti.smpp.addressbook.services.GroupDataEntryService;
import com.hti.smpp.addressbook.services.GroupEntryService;
import com.hti.smpp.common.contacts.dto.ContactEntry;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/addressbook")
public class AddressBookController {

	@Autowired
	private ContactEntryService contactEntryService;
	
	@Autowired
	private GroupDataEntryService groupDataEntryService;
	
	@Autowired
	private GroupEntryService entryService;

	@PostMapping(value = "/save/contact-entry", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> saveContactEntry(@RequestPart("contactFile") MultipartFile contactFile,
			@RequestParam("contactEntryRequest") String contactEntryRequest,
			@RequestHeader("username") String username) {

		return this.contactEntryService.saveContactEntry(contactEntryRequest, contactFile, username);

	}

	@PostMapping(value = "/save/group-data-entry", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> saveGroupDataEntry(@RequestPart("contactNumberFile") MultipartFile contactNumberFile,
			@RequestParam("groupDataEntryRequest") String groupDataEntryRequest,
			@RequestHeader("username") String username) {

		return this.groupDataEntryService.saveGroupData(groupDataEntryRequest, contactNumberFile, username);
	}
	
	@PostMapping("/save/group-entry")
	public ResponseEntity<?> saveGroupEntry(@Valid @RequestBody GroupEntryRequest entryRequest,@RequestHeader("username") String username){
		return this.entryService.saveGroupEntry(entryRequest, username);
	}
	
	@GetMapping("/get/contact-for-bulk")
	public ResponseEntity<?> contactForBulk(@RequestBody ContactEntryRequest request, @RequestHeader("username") String username){
		ContactForBulk response = this.contactEntryService.contactForBulk(request, username);
		if(response!=null) {
			return ResponseEntity.ok(response);
		}else {
			return new ResponseEntity<>("No Contact for Bulk.",HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/get/groupdata-for-bulk")
	public ResponseEntity<?> groupDataForBulk(@RequestBody GroupDataEntryRequest request, @RequestHeader("username") String username){
		ContactForBulk response = this.groupDataEntryService.groupDataForBulk(request, username);
		if(response!=null) {
			return ResponseEntity.ok(response);
		}else {
			return new ResponseEntity<>("No Contact for Bulk.",HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/get/view-search-contact")
	public ResponseEntity<?> viewSearchContact(@RequestBody GroupEntryRequest groupEntryRequest, @RequestHeader("username") String username){
		List<ContactEntry> list = this.contactEntryService.viewSearchContact(groupEntryRequest, username);
		if(list.isEmpty()) {
			return new ResponseEntity<>("No Contact found.",HttpStatus.NOT_FOUND);
		}else if(!list.isEmpty()) {
			return ResponseEntity.ok(list);
		}else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
	
	@GetMapping("/get/proceed-search-contact")
	public ResponseEntity<?> proceedSearchContact(@RequestBody GroupEntryRequest entryRequest, @RequestHeader("username") String username ){
		ContactForBulk response = this.contactEntryService.proceedSearchContact(entryRequest, username);
		if(response!=null) {
			return ResponseEntity.ok(response);
		}else {
			return new ResponseEntity<>("No Contact for Bulk.",HttpStatus.BAD_REQUEST);
		}
	}

}
