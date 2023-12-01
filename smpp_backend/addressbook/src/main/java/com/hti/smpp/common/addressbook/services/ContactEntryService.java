package com.hti.smpp.common.addressbook.services;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.addressbook.request.ContactEntryRequest;
import com.hti.smpp.common.addressbook.request.GroupEntryRequest;
import com.hti.smpp.common.addressbook.response.ContactForBulk;
import com.hti.smpp.common.contacts.dto.ContactEntry;

public interface ContactEntryService {
	
	public ResponseEntity<?> saveContactEntry(String request,MultipartFile file, String username);
	public ContactForBulk contactForBulk(ContactEntryRequest request, String username);
	public List<ContactEntry> viewSearchContact(GroupEntryRequest groupEntryRequest,String username);
	public ContactForBulk proceedSearchContact(GroupEntryRequest entryRequest, String username);
	public ResponseEntity<?> modifyContactUpdate(ContactEntryRequest request, String username);
	public ResponseEntity<?> modifyContactDelete(List<Integer> ids, String username);
	public ResponseEntity<?> modifyContactExport(ContactEntryRequest request, String username);
}
