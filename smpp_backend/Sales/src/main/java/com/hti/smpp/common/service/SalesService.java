package com.hti.smpp.common.service;

import java.util.Collection;
import org.springframework.http.ResponseEntity;
import com.hti.smpp.common.request.SalesEntryForm;
import com.hti.smpp.common.sales.dto.SalesEntry;


public interface SalesService {

	public ResponseEntity<String> save(SalesEntryForm entry, String username);

	public ResponseEntity<String> update(SalesEntryForm form, String username);

	public ResponseEntity<String> delete(int id, String username);
	
	public ResponseEntity<Collection<SalesEntry>> listSalesUsers(String username);
	
	public ResponseEntity<?> viewSalesEntry(int id, String username);
	
	public ResponseEntity<Collection<SalesEntry>> setupSalesEntry(String username);
	
	

}
