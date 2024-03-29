package com.hti.smpp.common.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.Request.DashboardVisibilityRequest;
import com.hti.smpp.common.Service.DashboardAccessService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/dashboard-access")
public class DashboardAccessController {
	
	@Autowired
	private DashboardAccessService dashboardAccessService;
	
	@PutMapping("/update")
	@Operation(summary = "Update a new Dashboard Access", description = "Update a Existed dashboard Access")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Dashboard Acesss Updated Successfully !!"),
			@ApiResponse(responseCode = "404", description = "No content found"),
			@ApiResponse(responseCode = "502", description = "Error Updated Dashboard Access"),
			@ApiResponse(responseCode = "401", description = "User unauthorized request") })
	public ResponseEntity<?>updateDashboardAccess (@RequestHeader("username") String username,@RequestBody DashboardVisibilityRequest request){
		dashboardAccessService.updateDashboardVisibility(username,request.getUserId(), request.getDashboardVisibility());
		return new ResponseEntity<>("Update Sucessfully",HttpStatus.OK);
	}
	
//	@PostMapping("/save")
//	@Operation(summary = "Create a new Dashboard Access", description = "Save A new Dashboard Access")
//	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Dashboard Acesss Created Successfully !!"),
//			@ApiResponse(responseCode = "404", description = "No content found"),
//			@ApiResponse(responseCode = "502", description = "Error Creating Dashboard Access"),
//			@ApiResponse(responseCode = "401", description = "User unauthorized request") })
//	public ResponseEntity<?>saveDashboardAccess (@RequestHeader("username") String username,@RequestBody DashboardAccessRequest dashboardAccessRequest){
//		DashboardAccess dashboardAccess=dashboardAccessService.saveDashboardAccess(username, dashboardAccessRequest);
//		return new ResponseEntity<>(dashboardAccess,HttpStatus.OK);
//	}
	
}
