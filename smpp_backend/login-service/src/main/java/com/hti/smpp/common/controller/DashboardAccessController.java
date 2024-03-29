package com.hti.smpp.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.request.DashboardVisibilityRequest;
import com.hti.smpp.common.response.DashboardAccessResponse;
import com.hti.smpp.common.service.DashboardAccessService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/dashboard-access")
public class DashboardAccessController {
	
	@Autowired
	private DashboardAccessService dashboardAccessService;
	
	@PostMapping("/save")
	@Operation(summary = "Save/Update a Dashboard Access", description = "Save/Update a Existed dashboard Access")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Dashboard Acesss Updated Successfully !!"),
			@ApiResponse(responseCode = "404", description = "No content found"),
			@ApiResponse(responseCode = "502", description = "Error Updated Dashboard Access"),
			@ApiResponse(responseCode = "401", description = "User unauthorized request") })
	public ResponseEntity<?>updateDashboardAccess (@RequestHeader("username") String username,@RequestBody(required = false) DashboardVisibilityRequest request){
		DashboardAccessResponse response=null;
		if(request==null) {
			 response=dashboardAccessService.updateDashboardVisibility(username,null);
		}else {
		 response=dashboardAccessService.updateDashboardVisibility(username, request.getDashboardVisibility());
		}
		return new ResponseEntity<>(response,HttpStatus.OK);
	}
	
	@GetMapping("/fetch")
	@Operation(summary = "Get Dashboard Access", description = "Get Dashboard Access")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Get Dashboard Acesss !!"),
			@ApiResponse(responseCode = "404", description = "No content found"),
			@ApiResponse(responseCode = "502", description = "Error Getting Dashboard Access"),
			@ApiResponse(responseCode = "401", description = "User unauthorized request") })
	public ResponseEntity<?>saveDashboardAccess (@RequestHeader("username") String username){
	DashboardAccessResponse dashboardAccessResponse=dashboardAccessService.getDashboardAccess(username);
		return new ResponseEntity<>(dashboardAccessResponse,HttpStatus.OK);
	}
	
}
