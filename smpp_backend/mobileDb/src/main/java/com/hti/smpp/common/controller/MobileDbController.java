package com.hti.smpp.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.request.MobileDbRequest;
import com.hti.smpp.common.request.UpdateMobileInfo;
import com.hti.smpp.common.services.MobileDbService;
import com.hti.smpp.common.services.MobileDbUserService;

@RestController
@RequestMapping("/MobileDb")
public class MobileDbController {

	@Autowired
	private MobileDbService mobileDbService;
	
	@Autowired
	private MobileDbUserService mobileDbUserService;
	
	@PostMapping(value ="/addMobileData",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> addMobileData(@RequestParam(name = "mobileDbRequest", required = false) String  mobileDbRequest, @RequestPart(name = "file", required = false) MultipartFile file,
			@RequestHeader(name = "username", required = true) String username){
		
		return this.mobileDbService.addMobileData(mobileDbRequest,file, username);
	}
	
	
	@GetMapping("/showMobileData")
	public ResponseEntity<?> showMobileData(@RequestBody MobileDbRequest MobileData ,@RequestHeader(name = "username", required = true) String username ){
		
		return this.mobileDbService.showMobileData(MobileData, username);
	}
	

	
	@PutMapping("/updateMobileDataList")
	public ResponseEntity<?> updateMobileDataList(@RequestBody UpdateMobileInfo updatedMobileData ,@RequestHeader(name = "username", required = true) String username ){
		
		return this.mobileDbService.updateMobileDataList(updatedMobileData, username);
	}
	
	
	@DeleteMapping("/deleteMobileDataList")
	public ResponseEntity<?> deleteMobileDataList(@RequestBody UpdateMobileInfo mobileData ,@RequestHeader(name = "username", required = true) String username ){
		
		return this.mobileDbService.deleteMobileDataList(mobileData, username);
	}
	
	
	@PostMapping("/chooseRequired")
	public ResponseEntity<?> chooseRequired(@RequestBody MobileDbRequest mobileDbRequest ,@RequestHeader(name = "username", required = true) String username ){
		
		return this.mobileDbService.chooseRequired(mobileDbRequest,username);
	}
	
	@PostMapping("/editData")
	public ResponseEntity<?> editData(@RequestHeader(name = "username", required = true) String username ){
		
		return this.mobileDbService.editData(username);
	}
	
	
	@GetMapping("/getSubArea")
	public ResponseEntity<?> getSubArea(@RequestParam(required = false) String area, @RequestHeader(name = "username", required = true) String username ){
		
		return this.mobileDbService.getSubArea(area , username);
	}
	
	
	@PostMapping("/mobileBulkUpload")
	public ResponseEntity<?> mobileBulkUpload(@RequestHeader(name = "username", required = true) String username ){
		
		return this.mobileDbService.mobileBulkUpload(username);
	}
	@PostMapping("/mobileScheduleUpload")
	public ResponseEntity<?> mobileScheduleUpload(@RequestHeader(name = "username", required = true) String username ){
		
		return this.mobileDbUserService.mobileScheduleUpload(username);
	}
	@PostMapping("/mobileShedulePartialUpload")
	public ResponseEntity<?> mobileShedulePartialUpload(@RequestHeader(name = "username", required = true) String username ){
		
		return this.mobileDbUserService.mobileShedulePartialUpload(username);
	}
	@PostMapping("/mobileUserList")
	public ResponseEntity<?> mobileUserList(@RequestBody MobileDbRequest mobileDbRequest ,@RequestHeader(name = "username", required = true) String username ){
		
		return this.mobileDbUserService.mobileUserList(mobileDbRequest,username);
	}
	@PostMapping("/mobileUserListInfo")
	public ResponseEntity<?> mobileUserListInfo(@RequestBody MobileDbRequest mobileDbRequest ,@RequestHeader(name = "username", required = true) String username ){
		
		return this.mobileDbUserService.mobileUserListInfo(mobileDbRequest,username);
	}
	
	@PostMapping("/queryForMobileRecord")
	public ResponseEntity<?> queryForMobileRecord(@RequestHeader(name = "username", required = true) String username ){
		
		return this.mobileDbUserService.queryForMobileRecord(username);
		
	}
	
	@PostMapping("/SendAreaWiseSms")
	public ResponseEntity<?> SendAreaWiseSms(@RequestBody MobileDbRequest mobileDbRequest ,@RequestHeader(name = "username", required = true) String username ){
		
		return this.mobileDbUserService.SendAreaWiseSms(mobileDbRequest,username);
		
	}
	
	
	
	
	
	
	
	
	
//	----------------------------------separate API----------------------------------------------------------------
	
	
	@PutMapping("/updateMobileData")
	public ResponseEntity<?> updateMobileData(@RequestBody MobileDbRequest updatedMobileData ,@RequestHeader(name = "username", required = true) String username ){
		
		return this.mobileDbService.updateMobileData(updatedMobileData, username);
	}
	
	@DeleteMapping("/deleteMobileData")
	public ResponseEntity<?> deleteMobileData(@RequestBody MobileDbRequest mobileData ,@RequestHeader(name = "username", required = true) String username ){
		
		return this.mobileDbService.deleteMobileData(mobileData, username);
	}
	
	
}
