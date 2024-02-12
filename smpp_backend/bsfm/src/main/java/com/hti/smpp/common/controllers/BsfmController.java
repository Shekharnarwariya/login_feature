package com.hti.smpp.common.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.bsfm.dto.Bsfm;
import com.hti.smpp.common.exception.ExceptionResponse;
import com.hti.smpp.common.request.BsfmFilterFrom;
import com.hti.smpp.common.response.BSFMResponse;
import com.hti.smpp.common.response.DeleteProfileResponse;
import com.hti.smpp.common.services.BsfmService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * RESTful controller for managing SMPP Bsfm API.
 */
@RestController
@RequestMapping("/bsfm")
@OpenAPIDefinition(info = @Info(title = "SMPP Bsfm  API", version = "1.0", description = "API for managing SMPP  Bsfm..."))
@Tag(name = "BSFMController", description = "API's for BSFM Service.")
public class BsfmController {

	@Autowired
	private BsfmService bsfmService;

	/**
	 * Adds a new Bsfm profile based on the provided filter and username
	 * 
	 * @param bsfmFilterFrom
	 * @param username
	 * @return
	 * @throws Exception
	 */
	@Operation(summary = "Add Bsfm Profile", description = "Add a new Bsfm profile")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "201", description = "Bsfm Profile Added Successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PostMapping("/add")
	public ResponseEntity<String> addBsfmProfile(@RequestBody BsfmFilterFrom bsfmFilterFrom,
			@Parameter(description = "Username of the profile owner", required = true) @RequestHeader(name = "username", required = true) String username)
			throws Exception {
		return bsfmService.addBsfmProfile(bsfmFilterFrom, username);
	}
	
	/**
	 * Retrieves all Bsfm profiles for a given username.
	 * 
	 * @param username
	 * @return
	 */
	@Operation(summary = "Show Bsfm Profiles", description = "Show all Bsfm profiles for a given username")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Bsfm Profile Fetched Successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@GetMapping("/show")
	public ResponseEntity<List<Bsfm>> showBsfmProfile(
			@Parameter(description = "Username of the profile owner", required = true) @RequestHeader(name = "username", required = true) String username) {
		return bsfmService.showBsfmProfile(username);
	}
	
	/**
	 * Deletes a Bsfm profile based on the provided username and profile details.
	 * 
	 * @param username
	 * @param bsfmFilterFrom
	 * @return
	 */
	@Operation(summary = "Delete Bsfm Profile", description = "Delete Bsfm profile by profilename")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Bsfm Profile Deleted Successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@DeleteMapping("/delete")
	public ResponseEntity<String> delete(
			@Parameter(description = "Username of the profile owner", required = true) @RequestHeader(name = "username", required = true) String username,
			@RequestParam("profilename") String profilename) {
		return bsfmService.delete(username, profilename);
	}
	
	/**
	 * Updates an existing Bsfm profile.
	 * 
	 * @param bsfmFilterFrom
	 * @param username
	 * @return
	 */
	@Operation(summary = "Update Bsfm Profile", description = "Update an existing Bsfm profile")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "201", description = "Bsfm Profile Updated Successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PutMapping("/update")
	public ResponseEntity<String> updateBsfmProfile(@RequestBody BsfmFilterFrom bsfmFilterFrom,
			@Parameter(description = "Username of the profile owner", required = true) @RequestHeader(name = "username", required = true) String username) {
		return bsfmService.updateBsfmProfile(bsfmFilterFrom, username);
	}
	
	/**
	 * Updates the flag of an existing Bsfm profile.
	 * 
	 * @param username
	 * @param bsfmFilterFrom
	 * @return
	 */
	@Operation(summary = "Update Bsfm Profile Flag", description = "Update flag of an existing Bsfm profile")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Bsfm Flag Updated Successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PutMapping("/update-flag")
	public ResponseEntity<String> updateBsfmProfileFlag(
			@Parameter(description = "Username of the profile owner", required = true) @RequestHeader(name = "username", required = true) String username,
			@RequestParam(name="flag",required = true) String flag) {
		return bsfmService.updateBsfmProfileFlag(username, flag);
	}
	
	/**
	 * Return a delete bsfm profile response based on the provided username and profile ID.
	 * 
	 * @param username
	 * @param id
	 * @return
	 */
	@Operation(summary = "Delete Bsfm Profile Response", description = "Delete response of Bsfm profile by username and profile ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Delete Profile Response Fetched Successfully.",content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeleteProfileResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class)))
	})
	@GetMapping("/get/delete-profile")
	public ResponseEntity<DeleteProfileResponse> deleteProfile(
			@Parameter(description = "Username of the profile owner", required = true) @RequestHeader(name = "username", required = true) String username,
			@Parameter(description = "ID of the profile to be deleted", required = true) @RequestParam(name = "id", required = true) int id) {
		return bsfmService.deleteProfile(username, id);
	}

	/**
	 * Checks the existence of a Bsfm profile based on the provided username.
	 * 
	 * @param username
	 * @return
	 */
	@Operation(summary = "Check Bsfm Profile", description = "Check Bsfm profile by username")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Checked Bsfm Response Successful.",content = @Content(mediaType = "application/json", schema = @Schema(implementation = BSFMResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class)))
	})
	@GetMapping("/check")
	public ResponseEntity<BSFMResponse> checked(
			@Parameter(description = "Username of the profile owner", required = true) @RequestHeader(name = "username", required = true) String username) {
		return bsfmService.checked(username);
	}

}
