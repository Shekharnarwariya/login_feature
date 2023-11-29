package com.hti.smpp.addressbook.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.addressbook.request.GroupDataEntryRequest;
import com.hti.smpp.addressbook.response.ContactForBulk;

public interface GroupDataEntryService {
	
	public ResponseEntity<?> saveGroupData(String request, MultipartFile file, String username);
	public ContactForBulk groupDataForBulk(GroupDataEntryRequest groupDataEntry, String username);

}
