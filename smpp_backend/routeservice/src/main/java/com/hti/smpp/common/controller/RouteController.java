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
import com.hti.smpp.common.services.RouteServices;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

	@Autowired
	private RouteServices routeService;

	@PostMapping("/save")
	public ResponseEntity<String> saveRoute(@RequestBody RouteRequest routeRequest,
			@RequestHeader("username") String username) {
		String savedRoute = routeService.saveRoute(routeRequest, username);
		return new ResponseEntity<>(savedRoute, HttpStatus.CREATED);
	}

	@PostMapping("/updateOptionalRoute")
	public ResponseEntity<String> updateOptionalRoute(@RequestBody OptEntryArrForm optEntryArrForm,
			@RequestHeader("username") String username) {
		try {
			routeService.updateOptionalRoute(optEntryArrForm, username);
			return new ResponseEntity<>("Update successfully", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>("Error during update: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
