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

import com.hti.smpp.common.request.ChooseRequest;
import com.hti.smpp.common.request.DeleteMobDataRequest;
import com.hti.smpp.common.request.MobileDbRequest;
import com.hti.smpp.common.request.MobileUserListInfoRequest;
import com.hti.smpp.common.request.MobileUserListRequest;
import com.hti.smpp.common.request.SendAreaSmsRequest;
import com.hti.smpp.common.request.ShowMobileDataRequest;
import com.hti.smpp.common.request.UpdateMobileInfo;
import com.hti.smpp.common.request.UpdateSingleRequest;
import com.hti.smpp.common.services.MobileDbService;
import com.hti.smpp.common.services.MobileDbUserService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/mobiledb")
@OpenAPIDefinition(info = @Info(title = "SMPP MobileDB  API..", version = "1.0", description = "API for managing SMPP  MobileDb..."))
public class MobileDbController {

	@Autowired
	private MobileDbService mobileDbService;
	
	@Autowired
	private MobileDbUserService mobileDbUserService;
	
	
	@Operation(summary = "Add Mobile Data Entry", description = "Add a new Mobile Data Entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "ContactDataEntry Saved Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), 
			@ApiResponse(responseCode = "400", description = "Bad Request.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) 
	})
	@PostMapping(value ="/add-mobiledata",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> addMobileData(@RequestParam(name = "mobileDbRequest", required = false) String  mobileDbRequest, @RequestPart(name = "file", required = false) MultipartFile file,
			@RequestHeader(name = "username", required = true) String username){
		
		return this.mobileDbService.addMobileData(mobileDbRequest,file, username);
	}
	
	
	
	
	@Operation(summary = "Show Mobile Data Entry", description = "Shows the Contact Data Entry Present")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Entry Fetched Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), 
			@ApiResponse(responseCode = "400", description = "Bad Request.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) 
	})

	@PostMapping("/show-mobiledata")
	public ResponseEntity<?> showMobileData(@RequestBody ShowMobileDataRequest MobileData ,@RequestHeader(name = "username", required = true) String username ){

		
		return this.mobileDbService.showMobileData(MobileData, username);
	}
	

	
	
	
	@Operation(summary = "Update MobileDb in Bulk Entry", description = "Update's an existing sales entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "ContactEntry Updated Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) 
	})

	@PutMapping("/update-mobiledata")

	public ResponseEntity<?> updateMobileDataList(@RequestBody UpdateMobileInfo updatedMobileData ,@RequestHeader(name = "username", required = true) String username ){
		
		return this.mobileDbService.updateMobileDataList(updatedMobileData, username);
	}
	
	
	
	
	
	@Operation(summary = "Delete MobileDb in Bulk Entry", description = "Delete's existing sales entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "ContactEntry Deleted Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) 
	})

	@DeleteMapping("/delete-mobiledata")
	public ResponseEntity<?> deleteMobileDataList(@RequestBody DeleteMobDataRequest mobileData ,@RequestHeader(name = "username", required = true) String username ){

		
		return this.mobileDbService.deleteMobileDataList(mobileData, username);
	}
	
	
	
	
	
	@Operation(summary = "Choose Request Type Entry", description = "Returns the Total Count And Type of sms based in action request entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Entry Fetched Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) 
	})

	@PostMapping("/choose-required")
	public ResponseEntity<?> chooseRequired(@RequestBody ChooseRequest mobileDbRequest ,@RequestHeader(name = "username", required = true) String username ){

		
		return this.mobileDbService.chooseRequired(mobileDbRequest,username);
	}
	
	
	
	
	
	@Operation(summary = "Provides the Different Fields present in Entry", description = "Returns all Distinct Entry Present in MobileDb")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Entry Fetched Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) 
	})

	@PostMapping("/editdata")

	public ResponseEntity<?> editData(@RequestHeader(name = "username", required = true) String username ){
		
		return this.mobileDbService.editData(username);
	}
	
	
	
	
	
	
	@Operation(summary = "Get Sub Area Entry", description = "Get Sub Area From an existing MobileDb entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Entry Fetched Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) 
	})

	@GetMapping("/get-subarea")

	public ResponseEntity<?> getSubArea(@RequestParam(required = false) String area, @RequestHeader(name = "username", required = true) String username ){
		
		return this.mobileDbService.getSubArea(area , username);
	}
	
	
	
	
	
	

//	@PostMapping("/mobileBulkUpload")
//	public ResponseEntity<?> mobileBulkUpload(@RequestHeader(name = "username", required = true) String username ){
//		
//		return this.mobileDbService.mobileBulkUpload(username);
//	}
	
	
	
//
//	@PostMapping("/mobileScheduleUpload")
//	public ResponseEntity<?> mobileScheduleUpload(@RequestHeader(name = "username", required = true) String username ){
//		
//		return this.mobileDbUserService.mobileScheduleUpload(username);
//	}
	
	
	
	
	

//	@PostMapping("/mobileShedulePartialUpload")
//	public ResponseEntity<?> mobileShedulePartialUpload(@RequestHeader(name = "username", required = true) String username ){
//		
//		return this.mobileDbUserService.mobileShedulePartialUpload(username);
//	}
	
	
	
	
	
	@Operation(summary = "Mobile User List Entry", description = "Mobile User List Filter based on provided data entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Entry Fetched Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) 
	})

	@PostMapping("/mob-userlist")
	public ResponseEntity<?> mobileUserList(@Valid @RequestBody MobileUserListRequest mobileDbRequest ,@RequestHeader(name = "username", required = true) String username ){

		
		return this.mobileDbUserService.mobileUserList(mobileDbRequest,username);
	}
	
	
	
	
	
	@Operation(summary = "Mobile User List Information Entry", description = "Mobile User List Filter based on provided data entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Entry Fetched Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) 
	})

	@PostMapping("/mob-userlistinfo")
	public ResponseEntity<?> mobileUserListInfo(@RequestBody MobileUserListInfoRequest mobileDbRequest ,@RequestHeader(name = "username", required = true) String username ){

		
		return this.mobileDbUserService.mobileUserListInfo(mobileDbRequest,username);
	}
	
	
	
	
	
	
	@Operation(summary = "Query For Mobile Record Entry", description = "Query For Mobile Record from MObile DB entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Entry Fetched Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) 
	})

	@PostMapping("/query-mobile")

	public ResponseEntity<?> queryForMobileRecord(@RequestHeader(name = "username", required = true) String username ){
		
		return this.mobileDbUserService.queryForMobileRecord(username);
		
	}
	
	
	
	
	
	
	@Operation(summary = "Send Area Wise Sms Entry", description = "Send Area Wise Sms from Mobile Db entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Entry Fetched Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) 
	})

	@PostMapping("/send-areawise-sms")
	public ResponseEntity<?> SendAreaWiseSms(@RequestBody SendAreaSmsRequest mobileDbRequest ,@RequestHeader(name = "username", required = true) String username ){

		
		return this.mobileDbUserService.SendAreaWiseSms(mobileDbRequest,username);
		
	}
	
	
	
	
	
	
	
	
	
//	----------------------------------separate API----------------------------------------------------------------
	
	
	
	@Operation(summary = "Update Single MobileDb Entry", description = "Update's an existing MobileDb entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Entry Updated Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) 
	})

	@PutMapping("/update-mobsingle")
	public ResponseEntity<?> updateMobileData(@RequestBody UpdateSingleRequest updatedMobileData ,@RequestHeader(name = "username", required = true) String username ){

		
		return this.mobileDbService.updateMobileData(updatedMobileData, username);
	}
	
	
	
	@Operation(summary = "Delete Single MobileDb Entry", description = "Deleted's an existing sales entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Entry Deleted Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), 
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) 
	})

	@DeleteMapping("/delete-mobsingle")
	public ResponseEntity<?> deleteMobileData(@RequestParam(required = false) String mobileNumber ,@RequestHeader(name = "username", required = true) String username ){

		
		return this.mobileDbService.deleteMobileData(mobileNumber, username);
	}
	
	
}
