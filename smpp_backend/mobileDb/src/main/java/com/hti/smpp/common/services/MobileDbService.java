package com.hti.smpp.common.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.request.ChooseRequest;
import com.hti.smpp.common.request.DeleteMobDataRequest;
import com.hti.smpp.common.request.MobileDbRequest;
import com.hti.smpp.common.request.ShowMobileDataRequest;
import com.hti.smpp.common.request.UpdateMobileInfo;
import com.hti.smpp.common.request.UpdateSingleRequest;

public interface MobileDbService {

	public ResponseEntity<?> addMobileData(String mobileDb, MultipartFile file, String username);
	
	public ResponseEntity<?> showMobileData( ShowMobileDataRequest mobileDb, String username);
	
	
	public ResponseEntity<?> updateMobileDataList( UpdateMobileInfo mobileDb, String username);
	
	public ResponseEntity<?> deleteMobileDataList( DeleteMobDataRequest mobileData, String username);
	
	public ResponseEntity<?> chooseRequired(ChooseRequest mobileDbRequest , String username);
	
	public ResponseEntity<?> editData(String username);
	
	public ResponseEntity<?> getSubArea(String area ,String username);
	
	public ResponseEntity<?> mobileBulkUpload(String username);
	
	
	
//	-------------------------------------Separate API-------------------------------------------
	public ResponseEntity<?> updateMobileData( UpdateSingleRequest mobileDb, String username);
	
	public ResponseEntity<?> deleteMobileData( String mobileNumber, String username);
	
	
	
}
