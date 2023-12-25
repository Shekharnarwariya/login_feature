package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.hti.smpp.common.hlr.dto.HlrSmscEntry;
import com.hti.smpp.common.request.HlrSmscEntryRequest;

//Interface defining the contract for HLR SMS entry service operations
public interface HlrSmscService {

	  // Save a new HLR SMS entry
	public ResponseEntity<?> save(HlrSmscEntryRequest hlrSmscEntryRequest, String username);

	 // Update an existing HLR SMS entry
	public ResponseEntity<?> update(int id, HlrSmscEntryRequest hlrSmscEntryRequest, String username);

	// Delete an existing HLR SMS entry
	public ResponseEntity<?> delete(int id, String username);
	
	 // Get details of a specific HLR SMS entry
	public ResponseEntity<HlrSmscEntry> getEntry(int id, String username);

	// Get a list of all HLR SMS entries for a user
	public List<HlrSmscEntry> list(String username);
}
