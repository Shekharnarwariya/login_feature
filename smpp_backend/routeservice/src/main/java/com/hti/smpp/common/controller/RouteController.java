package com.hti.smpp.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.request.OptEntryArrForm;
import com.hti.smpp.common.request.RouteRequest;
import com.hti.smpp.common.responce.OptionRouteResponse;
import com.hti.smpp.common.services.RouteServices;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@OpenAPIDefinition(info = @Info(title = "SMPP Route API", version = "1.0", description = "API for managing SMPP routes"))
@RestController
@RequestMapping("/api/routes")
public class RouteController {

	@Autowired
	private RouteServices routeService;

	// if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")) {
	@PostMapping("/save")
	@Operation(summary = "Save a route")
	@ApiResponse(responseCode = "201", description = "Route saved successfully")
	@ApiResponse(responseCode = "500", description = "Internal Server Error")
	public ResponseEntity<String> saveRoute(
			@RequestBody(description = "Route request object") RouteRequest routeRequest,
			@Parameter(description = "Username in request header", required = true) @RequestHeader("username") String username) {
		String savedRoute = routeService.saveRoute(routeRequest, username);
		return new ResponseEntity<>(savedRoute, HttpStatus.CREATED);
	}

	@PostMapping("/updateOptionalRoute")
	@Operation(summary = "Update an optional route")
	@ApiResponse(responseCode = "200", description = "Update successful")
	@ApiResponse(responseCode = "500", description = "Internal Server Error")
	public ResponseEntity<String> updateOptionalRoute(
			@RequestBody(description = "Optional entry array form") OptEntryArrForm optEntryArrForm,
			@Parameter(description = "Username in request header", required = true) @RequestHeader("username") String username) {
		try {
			routeService.updateOptionalRoute(optEntryArrForm, username);
			return new ResponseEntity<>("Update successfully", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>("Error during update: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/undo")
	@Operation(summary = "Undo an operation")
	@ApiResponse(responseCode = "200", description = "Undo operation successful")
	@ApiResponse(responseCode = "500", description = "Internal Server Error")
	public OptionRouteResponse undo(@RequestBody OptEntryArrForm optEntryArrForm,
			@RequestHeader("username") String username) {
		return routeService.undo(optEntryArrForm, username);
	}

	// if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")) {
	@PostMapping("/previous")
	@Operation(summary = "Get previous operation result")
	@ApiResponse(responseCode = "200", description = "Previous operation result retrieved successfully")
	@ApiResponse(responseCode = "500", description = "Internal Server Error")
	public OptionRouteResponse previous(@RequestBody OptEntryArrForm optEntryArrForm,
			@RequestHeader("username") String username) {
		return routeService.previous(optEntryArrForm, username);
	}
}
