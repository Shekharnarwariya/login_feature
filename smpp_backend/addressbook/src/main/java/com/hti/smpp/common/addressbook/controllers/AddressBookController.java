package com.hti.smpp.common.addressbook.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
	public ResponseEntity<?> saveGroupEntry(@RequestBody GroupEntryRequest entryRequest,@RequestHeader("username") String username){
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
	
	@GetMapping("/get/view-search-groupdata")
	public ResponseEntity<?> viewSearchGroupData(@RequestBody GroupDataEntryRequest request, @RequestHeader("username") String username){
		List<GroupDataEntry> list = this.groupDataEntryService.viewSearchGroupData(request, username);
		
		if(list.isEmpty()) {
			return new ResponseEntity<>("No data found!",HttpStatus.NOT_FOUND);
		}else if(!list.isEmpty()) {
			return ResponseEntity.ok(list);
		}else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
	
	@GetMapping("/get/proceed-search-groupdata")
	public ResponseEntity<?> proceedSearchGroupData(@RequestBody GroupDataEntryRequest request, @RequestHeader("username") String username){
		ContactForBulk response = this.groupDataEntryService.proceedSearchGroupData(request, username);
		if(response!=null) {
			return ResponseEntity.ok(response);
		}else {
			return new ResponseEntity<>("No Contact for Bulk.",HttpStatus.BAD_REQUEST);
		}
	}
	
	@PutMapping("/update/contact")
	public ResponseEntity<?> modifyContactUpdate(@RequestBody ContactEntryRequest request, @RequestHeader("username") String username){
		return this.contactEntryService.modifyContactUpdate(request, username);
	}
	
	@DeleteMapping("/delete/contact")
	public ResponseEntity<?> modifyContactDelete(@RequestBody List<Integer> ids, @RequestHeader("username") String username){
		return this.contactEntryService.modifyContactDelete(ids, username);
	}
	
	@GetMapping("/export/contact")
	public ResponseEntity<?> modifyContactExport(@RequestBody ContactEntryRequest request, @RequestHeader("username") String username){
		return this.contactEntryService.modifyContactExport(request, username);
	}
	
	@PutMapping("/update/group-data-entry")
	public ResponseEntity<?> modifyGroupDataUpdate(@RequestBody GroupDataEntryRequest request, @RequestHeader("username") String username){
		return this.groupDataEntryService.modifyGroupDataUpdate(request, username);
	}
	
	@DeleteMapping("/delete/group-data-entry")
	public ResponseEntity<?> modifyGroupDataDelete(@RequestBody List<Integer> ids, @RequestHeader("username") String username){
		return this.groupDataEntryService.modifyGroupDataDelete(ids, username);
	}
	
	@GetMapping("/export/group-data-entry")
	public ResponseEntity<?> modifyGroupDataExport(@RequestBody GroupDataEntryRequest request, @RequestHeader("username") String username){
		return this.groupDataEntryService.modifyGroupDataExport(request, username);
	}
	
	@PutMapping("/update/group-entry")
	public ResponseEntity<?> modifyGroupEntryUpdate(@RequestBody GroupEntryRequest groupEntryRequest, @RequestHeader("username") String username){
		return this.entryService.modifyGroupEntryUpdate(groupEntryRequest, username);
	}
	
	@DeleteMapping("/delete/group-entry")
	public ResponseEntity<?> modifyGroupEntryDelete(@RequestBody GroupEntryRequest groupEntryRequest, @RequestHeader("username") String username){
		return this.entryService.modifyGroupEntryDelete(groupEntryRequest, username);
	}

}
