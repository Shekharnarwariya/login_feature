package com.hti.smpp.common.services;

import org.springframework.http.ResponseEntity;

import com.hti.smpp.common.request.MobileDbRequest;

public interface MobileDbUserService {

	
public ResponseEntity<?> mobileScheduleUpload(String username);
	
	public ResponseEntity<?> mobileShedulePartialUpload(String username);
	
	public ResponseEntity<?> mobileUserList(MobileDbRequest mobileDbRequest ,String username);
	
	public ResponseEntity<?> mobileUserListInfo(MobileDbRequest mobileDbRequest ,String username);
	
	public ResponseEntity<?> queryForMobileRecord(String username);
	
	
	public ResponseEntity<?> SendAreaWiseSms(MobileDbRequest mobileDbRequest,String username);
	
	
}
