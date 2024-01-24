package com.hti.smpp.common.service;

import java.util.Collection;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.SalesEntryForm;
import com.hti.smpp.common.response.ViewSalesEntry;
import com.hti.smpp.common.sales.dto.SalesEntry;

/**
 * Interface defining methods for Sales Service operations.
 */

@Service
public interface SalesService {

	public ResponseEntity<String> save(SalesEntryForm entry, String username);

	public ResponseEntity<String> update(SalesEntryForm form, String username);

	public ResponseEntity<String> delete(int id, String username);
	
	public ResponseEntity<Collection<SalesEntry>> listSalesUsers(String username);
	
	public ResponseEntity<ViewSalesEntry> viewSalesEntry(int id, String username);
	
	public ResponseEntity<Collection<SalesEntry>> setupSalesEntry(String username);
	
	
	
	

}
