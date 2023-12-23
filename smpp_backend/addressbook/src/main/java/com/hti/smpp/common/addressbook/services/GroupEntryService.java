package com.hti.smpp.common.addressbook.services;

import org.springframework.http.ResponseEntity;

import com.hti.smpp.common.addressbook.request.GroupEntryRequest;
/**
 *  Interface for GroupEntryService defining group-related operations.
 */
public interface GroupEntryService {

	public ResponseEntity<?> saveGroupEntry(GroupEntryRequest groupEntryRequest, String username);

	public ResponseEntity<?> modifyGroupEntryUpdate(GroupEntryRequest groupEntryRequest, String username);

	public ResponseEntity<?> modifyGroupEntryDelete(GroupEntryRequest groupEntryRequest, String username);

	public ResponseEntity<?> listGroup(String purpose, String groupData, String username);
}
