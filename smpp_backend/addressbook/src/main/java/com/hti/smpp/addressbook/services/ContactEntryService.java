package com.hti.smpp.addressbook.services;

import org.springframework.http.ResponseEntity;

import com.hti.smpp.addressbook.request.ContactEntryRequest;
import com.hti.smpp.addressbook.response.ContactForBulk;

public interface ContactEntryService {
	
	public ResponseEntity<?> saveContactEntry(ContactEntryRequest request, String username);
	public ContactForBulk contactForBulk(ContactEntryRequest request, String username);

}
