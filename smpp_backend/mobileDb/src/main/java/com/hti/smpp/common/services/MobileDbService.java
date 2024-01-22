package com.hti.smpp.common.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.request.MobileDbRequest;
import com.hti.smpp.common.request.UpdateMobileInfo;

public interface MobileDbService {

	public ResponseEntity<?> addMobileData(String mobileDb, MultipartFile file, String username);
	
	public ResponseEntity<?> showMobileData( MobileDbRequest mobileDb, String username);
	
	
	public ResponseEntity<?> updateMobileDataList( UpdateMobileInfo mobileDb, String username);
	
	public ResponseEntity<?> deleteMobileDataList( UpdateMobileInfo mobileData, String username);
	
	public ResponseEntity<?> chooseRequired(MobileDbRequest mobileDbRequest , String username);
	
	public ResponseEntity<?> editData(String username);
	
	public ResponseEntity<?> getSubArea(String area ,String username);
	
	public ResponseEntity<?> mobileBulkUpload(String username);
	
	
	
//	-------------------------------------Separate API-------------------------------------------
	public ResponseEntity<?> updateMobileData( MobileDbRequest mobileDb, String username);
	
	public ResponseEntity<?> deleteMobileData( MobileDbRequest mobileData, String username);
	
	
	
}
