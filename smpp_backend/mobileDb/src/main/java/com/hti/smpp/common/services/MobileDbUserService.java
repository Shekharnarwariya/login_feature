package com.hti.smpp.common.services;

import org.springframework.http.ResponseEntity;

import com.hti.smpp.common.request.MobileDbRequest;
import com.hti.smpp.common.request.MobileUserListInfoRequest;
import com.hti.smpp.common.request.MobileUserListRequest;
import com.hti.smpp.common.request.SendAreaSmsRequest;

public interface MobileDbUserService {

	
public ResponseEntity<?> mobileScheduleUpload(String username);
	
	public ResponseEntity<?> mobileShedulePartialUpload(String username);
	
	public ResponseEntity<?> mobileUserList(MobileUserListRequest mobileDbRequest ,String username);
	
	public ResponseEntity<?> mobileUserListInfo(MobileUserListInfoRequest mobileDbRequest ,String username);
	
	public ResponseEntity<?> queryForMobileRecord(String username);
	
	
	public ResponseEntity<?> SendAreaWiseSms(SendAreaSmsRequest mobileDbRequest,String username);
	
	
}
