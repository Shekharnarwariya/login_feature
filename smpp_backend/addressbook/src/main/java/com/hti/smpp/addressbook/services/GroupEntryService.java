package com.hti.smpp.addressbook.services;

import org.springframework.http.ResponseEntity;

import com.hti.smpp.addressbook.request.GroupEntryRequest;

public interface GroupEntryService {
	
	public ResponseEntity<?> saveGroupEntry(GroupEntryRequest groupEntryRequest, String username);

}
