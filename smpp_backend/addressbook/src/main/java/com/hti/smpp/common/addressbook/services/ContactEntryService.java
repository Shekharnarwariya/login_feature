package com.hti.smpp.common.addressbook.services;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.addressbook.request.ContactEntryRequest;
import com.hti.smpp.common.addressbook.request.UpdateContactRequest;
import com.hti.smpp.common.addressbook.response.ContactForBulk;
import com.hti.smpp.common.contacts.dto.ContactEntry;

/**
 * Interface for ContactEntryService defining contact-related operations.
 */
public interface ContactEntryService {

	public ResponseEntity<?> saveContactEntry(String request, MultipartFile file, String username);

	public ResponseEntity<?> contactForBulk(List<Long> numbers, int groupId, String username);

	public ResponseEntity<List<ContactEntry>> viewSearchContact(List<Integer> ids, String username);

	public List<ContactEntry> getContactByGroupId(int groupId,String start, String end, String search, String username);

	public ResponseEntity<ContactForBulk> proceedSearchContact(List<Integer> ids, String username);

	public ResponseEntity<?> modifyContactUpdate(UpdateContactRequest updateContactRequest, String username);

	public ResponseEntity<?> modifyContactDelete(List<Integer> ids, String username);

	public ResponseEntity<?> modifyContactExport(ContactEntryRequest request, String username);
}
