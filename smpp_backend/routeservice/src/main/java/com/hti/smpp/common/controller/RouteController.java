package com.hti.smpp.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import com.hti.smpp.common.request.HlrEntryArrForm;
import com.hti.smpp.common.request.OptEntryArrForm;
import com.hti.smpp.common.request.RouteEntryArrForm;
import com.hti.smpp.common.request.RouteRequest;
import com.hti.smpp.common.response.OptionRouteResponse;
import com.hti.smpp.common.response.RouteUserResponse;
import com.hti.smpp.common.services.RouteServices;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;

@OpenAPIDefinition(info = @Info(title = "SMPP Route API", version = "1.0", description = "API for managing SMPP routes"))
@RestController
@RequestMapping("/routes")
public class RouteController {

	@Autowired
	private RouteServices routeService;

	// if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")) {
	@PostMapping("/save")
	@Operation(summary = "Save a route", description = "Save a new route entries")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Route saved successfully"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error. Route Not Saved."),
			@ApiResponse(responseCode = "401", description = "Unauthorized User"),
			@ApiResponse(responseCode = "404", description = "Content Not Found")
	})
	public ResponseEntity<String> saveRoute(
		    @RequestBody RouteRequest routeRequest,
			@Parameter(description = "Username in request header", required = true) @RequestHeader("username") String username) {
		String savedRoute = routeService.saveRoute(routeRequest, username);
		return new ResponseEntity<>(savedRoute, HttpStatus.CREATED);
	}

	@PutMapping("/updateOptionalRoute")
	@Operation(summary = "Update an optional route", description = "Api To Update an Optional Route ")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Update successful"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error. Update Failed."),
			@ApiResponse(responseCode = "401", description = "Unauthorized User"),
			@ApiResponse(responseCode = "404", description = "Content Not Found"),
			@ApiResponse(responseCode = "400", description = "Bad Request. ScheduleTime Exception."),
	})
	public ResponseEntity<String> updateOptionalRoute(
			@RequestBody OptEntryArrForm optEntryArrForm,
			@Parameter(description = "Username in request header", required = true) @RequestHeader("username") String username) {
		try {
			routeService.updateOptionalRoute(optEntryArrForm, username);
			return new ResponseEntity<>("Update successfully", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>("Error during update: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/undo")
	@Operation(summary = "Undo an operation", description = "API to undo an operation")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Undo operation successful"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "401", description = "Unauthorized User"),
			@ApiResponse(responseCode = "404", description = "Content Not Found")
	})
	public OptionRouteResponse undo(@RequestBody OptEntryArrForm optEntryArrForm,
			@RequestHeader("username") String username) {
		return routeService.UpdateOptionalRouteUndo(optEntryArrForm, username);
	}

	// if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")) {
	@GetMapping("/previous")
	@Operation(summary = "Get previous operation result", description = "API for Get previous operation result")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Previous operation result retrieved successfully"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "401", description = "Unauthorized User"),
			@ApiResponse(responseCode = "404", description = "Content Not Found")
	})
	public OptionRouteResponse previous(@RequestBody OptEntryArrForm optEntryArrForm,
			@RequestHeader("username") String username) {
		return routeService.UpdateOptionalRoutePrevious(optEntryArrForm, username);
	}

	@GetMapping("/basic")
	@Operation(summary = "Perform a basic operation", description = "This endpoint performs a basic operation.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Basic operation done successfully"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "401", description = "Unauthorized User"),
			@ApiResponse(responseCode = "404", description = "Content Not Found")
	})
	public OptionRouteResponse basic(@RequestBody OptEntryArrForm optEntryArrForm,
			@Parameter(description = "The username provided in the request header", required = true, example = "john_doe") @RequestHeader("username") String username) {
		return routeService.UpdateOptionalRouteBasic(optEntryArrForm, username);
	}

	@GetMapping("/check-existing")
	@Operation(summary = "Check Existing Route", description = "Check if a route already exists.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Check Existing Route Successful"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "401", description = "Unauthorized User"),
			@ApiResponse(responseCode = "404", description = "Content Not Found")
	})
	public OptionRouteResponse checkExisting(@RequestBody RouteEntryArrForm routeEntryArrForm, String username) {
		return routeService.checkExisting(routeEntryArrForm, username);
	}
	
	@PostMapping("/CopyRouting")
	@Operation(summary = "Copy Routing", description = "Check if copy route exists.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Copy routing Successful"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "401", description = "Unauthorized User"),
			@ApiResponse(responseCode = "404", description = "Content Not Found")
	})
	public String execute(@PathVariable String username) {
		return routeService.execute(username);
	}

	@Operation(summary = "Download Route", description = "Download route data for a user.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Download successful", content = @Content(mediaType = "text/plain")),
		@ApiResponse(responseCode = "500", description = "Internal Server Error"),
		@ApiResponse(responseCode = "401", description = "Unauthorized User"),
		@ApiResponse(responseCode = "404", description = "Content Not Found")
	})
	@GetMapping("/download")
	@ResponseBody
	public String downloadRoute(@RequestParam String username, @RequestParam RouteEntryArrForm routingForm,
			HttpServletResponse response) {
		// Implementation for downloadRoute method
		return routeService.downloadRoute(username, routingForm, response);
	}

	@Operation(summary = "Route User List", description = "Get the list of route users for a specific purpose.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Successful retrieval of route user list", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteUserResponse.class))),
		@ApiResponse(responseCode = "500", description = "Internal Server Error"),
		@ApiResponse(responseCode = "401", description = "Unauthorized User"),
		@ApiResponse(responseCode = "404", description = "Content Not Found")
	})
	@GetMapping("/userList")
	@ResponseBody
	public RouteUserResponse routeUserList(@RequestParam String username, @RequestParam String purpose) {
		// Implementation for RouteUserList method
		return routeService.RouteUserList(username, purpose);
	}

	@Operation(summary = "Search Routing (Basic)", description = "Search for routes using basic criteria.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Successful search for routes", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OptionRouteResponse.class))),
		@ApiResponse(responseCode = "500", description = "Internal Server Error"),
		@ApiResponse(responseCode = "401", description = "Unauthorized User"),
		@ApiResponse(responseCode = "404", description = "Content Not Found")
	})
	@GetMapping("/searchBasic")
	@ResponseBody
	public OptionRouteResponse searchRoutingBasic(@RequestParam String username,
			@RequestParam RouteEntryArrForm routingForm) {
		// Implementation for SearchRoutingBasic method
		return routeService.SearchRoutingBasic(username, routingForm);
	}

	@Operation(summary = "Search Routing (Optional)", description = "Search for routes using optional criteria.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Successful search for routes", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OptionRouteResponse.class))),
		@ApiResponse(responseCode = "500", description = "Internal Server Error"),
		@ApiResponse(responseCode = "401", description = "Unauthorized User"),
		@ApiResponse(responseCode = "404", description = "Content Not Found")
	})
	@GetMapping("/searchOptional")
	@ResponseBody
	public OptionRouteResponse searchRoutingOptional(@RequestParam String username,
			@RequestParam RouteEntryArrForm routingForm) {
		// Implementation for SearchRoutingOptional method
		return routeService.SearchRoutingOptional(username, routingForm);
	}

	@Operation(summary = "Search Routing (Lookup)", description = "Search for routes using lookup criteria.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Successful search for routes", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OptionRouteResponse.class))),
		@ApiResponse(responseCode = "500", description = "Internal Server Error"),
		@ApiResponse(responseCode = "401", description = "Unauthorized User"),
		@ApiResponse(responseCode = "404", description = "Content Not Found")
	})
	@GetMapping("/searchLookup")
	@ResponseBody
	public OptionRouteResponse searchRoutingLookup(@RequestParam String username,
			@RequestParam RouteEntryArrForm routingForm) {
		// Implementation for SearchRoutingLookup method
		return routeService.SearchRoutingLookup(username, routingForm);
	}

	@PostMapping("/basic")
	@Operation(summary = "Create Basic Route", description = "Create a basic route.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Route created successfully"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "401", description = "Unauthorized User"),
			@ApiResponse(responseCode = "404", description = "Content Not Found")
	})
	public OptionRouteResponse basicRoute(@RequestParam String username, @RequestBody RouteEntryArrForm routingForm) {
		// Implementation goes here
		return routeService.BasicRouteBasicRoute(username, routingForm);
	}

	@DeleteMapping("/delete-basic")
	@Operation(summary = "Delete Basic Route", description = "Delete a basic route.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Route deleted successfully"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "401", description = "Unauthorized User"),
			@ApiResponse(responseCode = "404", description = "Content Not Found")
	})
	public OptionRouteResponse deleteRouteBasicRoute(@RequestParam String username,
			@RequestBody RouteEntryArrForm routingForm) {
		// Implementation goes here
		return routeService.deleteRouteBasicRoute(username, routingForm);
	}

	@PostMapping("/undo")
	@Operation(summary = "Undo Basic Route", description = "Undo a basic route.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Undo successful"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "401", description = "Unauthorized User"),
			@ApiResponse(responseCode = "404", description = "Content Not Found")
	})
	public OptionRouteResponse undoRouteBasicRoute(@RequestParam String username,
			@RequestBody RouteEntryArrForm routingForm) {
		// Implementation goes here
		return routeService.undoRouteBasicRoute(username, routingForm);
	}

	@PostMapping("/previous")
	@Operation(summary = "Previous Basic Route", description = "Get the previous basic route.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Previous route retrieved successfully"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "401", description = "Unauthorized User"),
			@ApiResponse(responseCode = "404", description = "Content Not Found")
	})
	public OptionRouteResponse previousRouteBasicRoute(@RequestParam String username,
			@RequestBody RouteEntryArrForm routingForm) {
		// Implementation goes here
		return routeService.previousRouteBasicRoute(username, routingForm);
	}

	@PostMapping("/hlr")
	@Operation(summary = "HLR Basic Route", description = "Perform HLR routing.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "HLR routing successful"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "401", description = "Unauthorized User"),
			@ApiResponse(responseCode = "404", description = "Content Not Found")
	})
	public OptionRouteResponse hlrRouteBasicRoute(@RequestParam String username,
			@RequestBody RouteEntryArrForm routingForm) {
		// Implementation goes here
		return routeService.hlrRouteBasicRoute(username, routingForm);
	}

	@PostMapping("/optional")
	@Operation(summary = "Optional Basic Route", description = "Create an optional basic route.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Optional route created successfully"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "401", description = "Unauthorized User"),
			@ApiResponse(responseCode = "404", description = "Content Not Found")
	})
	public OptionRouteResponse optionalRouteBasicRoute(@RequestParam String username,
			@RequestBody RouteEntryArrForm routingForm) {
		// Implementation goes here
		return routeService.optionalRouteBasicRoute(username, routingForm);
	}

	@Operation(summary = "Update Optional Route HLR")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "401", description = "Unauthorized User"),
			@ApiResponse(responseCode = "404", description = "Content Not Found")		
	})
	@PostMapping("/updateOptionalRouteHlr")
	public OptionRouteResponse updateOptionalRouteHlr(@RequestBody OptEntryArrForm optEntryArrForm, String username) {
		return routeService.UpdateOptionalRouteHlr(optEntryArrForm, username);
	}

	@Operation(summary = "HLR Route Update")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "401", description = "Unauthorized User"),
			@ApiResponse(responseCode = "404", description = "Content Not Found")	
	})
	@PostMapping("/hlrRouteUpdate")
	public OptionRouteResponse hlrRouteUpdate(String username, @RequestBody HlrEntryArrForm hlrEntryArrForm) {
		return routeService.hlrRouteUpdate(username, hlrEntryArrForm);
	}

	@Operation(summary = "HLR Route Undo")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "401", description = "Unauthorized User"),
			@ApiResponse(responseCode = "404", description = "Content Not Found")	
	})
	@PostMapping("/hlrRouteUndo")
	public OptionRouteResponse hlrRouteUndo(String username, @RequestBody HlrEntryArrForm hlrEntryArrForm) {
		return routeService.hlrRouteUndo(username, hlrEntryArrForm);
	}

	@Operation(summary = "HLR Route Previous")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "401", description = "Unauthorized User"),
			@ApiResponse(responseCode = "404", description = "Content Not Found")
	})
	@PostMapping("/hlrRoutePrevious")
	public OptionRouteResponse hlrRoutePrevious(String username, @RequestBody HlrEntryArrForm hlrEntryArrForm) {
		return routeService.hlrRoutePrevious(username, hlrEntryArrForm);
	}

	@Operation(summary = "HLR Route Basic")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "401", description = "Unauthorized User"),
			@ApiResponse(responseCode = "404", description = "Content Not Found")	
	})
	@PostMapping("/hlrRouteBasic")
	public OptionRouteResponse hlrRouteBasic(String username, @RequestBody HlrEntryArrForm hlrEntryArrForm) {
		return routeService.hlrRouteBasic(username, hlrEntryArrForm);
	}

	@Operation(summary = "HLR Route Optional")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error"),
			@ApiResponse(responseCode = "401", description = "Unauthorized User"),
			@ApiResponse(responseCode = "404", description = "Content Not Found")	
	})
	@PostMapping("/hlrRouteOptional")
	public OptionRouteResponse hlrRouteOptional(String username, @RequestBody HlrEntryArrForm hlrEntryArrForm) {
		return routeService.hlrRouteOptional(username, hlrEntryArrForm);
	}
}
