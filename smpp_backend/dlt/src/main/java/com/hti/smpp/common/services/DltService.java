package com.hti.smpp.common.services;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
//
//import com.hti.smpp.common.config.dto.DltEntry;
//import com.hti.smpp.common.config.dto.DltTemplEntry;
import com.hti.smpp.common.request.DltRequest;
import com.hti.smpp.common.request.DltTempRequest;
import com.hti.smpp.common.response.DltResponse;
import com.hti.smpp.common.response.DltTempResponse;

//DltService interface defines the contract for handling DLT (Do Not Disturb) operations
//These operations include saving, updating, listing, and deleting DLT entries and templates
//It also provides methods to retrieve a single DLT entry or template by ID
//The interface is marked with the @Service annotation to indicate it as a service component


@Service
public interface DltService {

	// Method to save a DLT entry
	public ResponseEntity<?> saveDltEntry(DltRequest entry, String username);

	 // Method to add a DLT template
	public ResponseEntity<?> addDltTemplate(String entry , MultipartFile file, String username);
	
	 // Method to list DLT entries
	public ResponseEntity<List<DltResponse>> listDltEntry(String username);
	
	// Method to list DLT templates
	public ResponseEntity<List<DltTempResponse>> listDltTemplate(String username);

	// Method to update a DLT entry
	public ResponseEntity<?> updateDltEntry(DltRequest entry , String username);
	
	 // Method to update a DLT template
	public ResponseEntity<?> updateDltTemplate(DltTempRequest entry , String username);

	// Method to delete a DLT entry

	public ResponseEntity<?> deleteDltEntry(int id , String username);
	
	  // Method to delete a DLT template
	public ResponseEntity<?> deleteDltTemplate(int id , String username);


	 // Method to get a single DLT entry by ID
	public DltResponse getDltEntry(int id , String username);
	
	 // Method to get a single DLT template by ID
	public DltTempResponse getDltTemplate(int id , String username);


	
}
