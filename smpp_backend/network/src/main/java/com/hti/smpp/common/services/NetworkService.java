package com.hti.smpp.common.services;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.dto.MccMncDTO;
import com.hti.smpp.common.request.MccMncForm;
import com.hti.smpp.common.request.MccMncUpdateForm;
import com.hti.smpp.common.response.MncMccTokens;
import com.hti.smpp.common.response.SearchResponse;

/**
 * Service interface for managing SMPP Network entries.
 */
public interface NetworkService {
	
	//Adds a new network entry i.e., single or multiple.
	public ResponseEntity<?> addNewMccMnc(String formMccMnc, MultipartFile file, String username);
	
	//Update an existing Network entry
	public ResponseEntity<String> replace(MccMncUpdateForm form, String username);
	
	//Delete an existing network entry by ID
	public ResponseEntity<String> delete(List<Integer> ids, String username);
	
    //Retrieve a list of NetworkEntry based on specified parameters	
	public ResponseEntity<SearchResponse> search(String ccReq, String mccReq, String mncReq, String checkCountryReq, String checkMccReq, String checkMncReq, String username, Pageable pageable);
	
	//Download mccmnc_database.xls File based on specified parameters
	public ResponseEntity<byte[]> download(String ccReq, String mccReq, String mncReq, String checkCountryReq, String checkMccReq, String checkMncReq, String username);
	
	//Retrieve the NetworkMap Of Country And CC From All NetworkEntry
	public ResponseEntity<?> editMccMnc(String username);
	
	public ResponseEntity<?> findMcc(String cc, String username);
	
	public ResponseEntity<?> findMnc(String mcc, String username);
	
	//Update an existing network entry by uploading a file
	public ResponseEntity<?> uploadUpdateMccMnc(MultipartFile file, String username);
	
	// Retrieve MncMccTokens based on specified parameters
	public ResponseEntity<MncMccTokens> findOption(String countryName, String mccParam, String username); 
}
