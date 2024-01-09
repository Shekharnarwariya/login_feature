package com.hti.smpp.common.services;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.config.dto.DltEntry;
import com.hti.smpp.common.config.dto.DltTemplEntry;
import com.hti.smpp.common.request.DltRequest;
import com.hti.smpp.common.request.DltTempRequest;


@Service
public interface DltService {

	public ResponseEntity<?> saveDltEntry(DltRequest entry, String username);

	public ResponseEntity<?> addDltTemplate(String entry , MultipartFile file, String username);
	
	public ResponseEntity<List<DltEntry>> listDltEntry(String username);
	
	public ResponseEntity<List<DltTemplEntry>> listDltTemplate(String username);

	public ResponseEntity<?> updateDltEntry(DltRequest entry , String username);
	
	public ResponseEntity<?> updateDltTemplate(DltTempRequest entry , String username);

	public void deleteDltEntry(DltEntry entry , String username);
	
	public void deleteDltTemplate(DltTemplEntry entry , String username);

	public DltEntry getDltEntry(int id , String username);
	
	public DltTemplEntry getDltTemplate(int id , String username);


	
}
