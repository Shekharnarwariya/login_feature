package com.hti.smpp.common.addressbook.services;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.hti.smpp.common.addressbook.request.GroupEntryRequest;
/**
 *  Interface for GroupEntryService defining group-related operations.
 */
public interface GroupEntryService {

	public ResponseEntity<?> saveGroupEntry(GroupEntryRequest groupEntryRequest, String username);

	public ResponseEntity<?> modifyGroupEntryUpdate(GroupEntryRequest groupEntryRequest, String username);

	public ResponseEntity<?> modifyGroupEntryDelete(int id, String username);

	public ResponseEntity<?> listGroup(String purpose, String groupData, String username,Pageable pageable);
}
