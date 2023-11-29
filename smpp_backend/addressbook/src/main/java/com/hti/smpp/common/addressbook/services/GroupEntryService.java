package com.hti.smpp.common.addressbook.services;

import org.springframework.http.ResponseEntity;

import com.hti.smpp.common.addressbook.request.GroupEntryRequest;

public interface GroupEntryService {
	
	public ResponseEntity<?> saveGroupEntry(GroupEntryRequest groupEntryRequest, String username);

}
