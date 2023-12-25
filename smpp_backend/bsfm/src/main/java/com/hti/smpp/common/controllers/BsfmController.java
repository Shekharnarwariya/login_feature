package com.hti.smpp.common.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.bsfm.dto.Bsfm;
import com.hti.smpp.common.request.BsfmFilterFrom;
import com.hti.smpp.common.response.BSFMResponse;
import com.hti.smpp.common.response.DeleteProfileResponse;
import com.hti.smpp.common.services.BsfmService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
/**
 * RESTful controller for managing SMPP Bsfm API.
 */
@RestController
@RequestMapping("/bsfm")
@OpenAPIDefinition(info = @Info(title = "SMPP Bsfm  API..", version = "1.0", description = "API for managing SMPP  Bsfm..."))
public class BsfmController {

	@Autowired
	private BsfmService bsfmService;
    /**
     *  Adds a new Bsfm profile based on the provided filter and username
     * @param bsfmFilterFrom
     * @param username
     * @return
     * @throws Exception
     */
	@Operation(summary = "Add Bsfm Profile", description = "Add a new Bsfm profile")
	@PostMapping("/add")
	public String addBsfmProfile(@RequestBody BsfmFilterFrom bsfmFilterFrom,
			@Parameter(description = "Username of the profile owner", required = true) @RequestParam String username)
			throws Exception {
		return bsfmService.addBsfmProfile(bsfmFilterFrom, username);
	}
/**
 * Checks the existence of a Bsfm profile based on the provided username.
 * @param username
 * @return
 */
	@Operation(summary = "Check Bsfm Profile", description = "Check Bsfm profile by username")
	@GetMapping("/check/{username}")
	public BSFMResponse checked(
			@Parameter(description = "Username of the profile owner", required = true) @PathVariable String username) {
		return bsfmService.checked(username);
	}
/**
 * Deletes a Bsfm profile based on the provided username and profile ID.
 * @param username
 * @param id
 * @return
 */
	@Operation(summary = "Delete Bsfm Profile", description = "Delete Bsfm profile by username and profile ID")
	@ApiResponse(responseCode = "200", description = "Profile deleted successfully")
	@ApiResponse(responseCode = "404", description = "Profile not found")
	@DeleteMapping("/delete/{username}/{id}")
	public DeleteProfileResponse deleteProfile(
			@Parameter(description = "Username of the profile owner", required = true) @PathVariable String username,
			@Parameter(description = "ID of the profile to be deleted", required = true) @PathVariable int id) {
		return bsfmService.deleteProfile(username, id);
	}
/**
 * Retrieves all Bsfm profiles for a given username.
 * @param username
 * @return
 */
	@Operation(summary = "Show Bsfm Profiles", description = "Show all Bsfm profiles for a given username")
	@GetMapping("/show/{username}")
	public List<Bsfm> showBsfmProfile(
			@Parameter(description = "Username of the profile owner", required = true) @PathVariable String username) {
		return bsfmService.showBsfmProfile(username);
	}
/**
 * Updates an existing Bsfm profile.
 * @param bsfmFilterFrom
 * @param username
 * @return
 */
	@Operation(summary = "Update Bsfm Profile", description = "Update an existing Bsfm profile")
	@PutMapping("/update")
	public String updateBsfmProfile(@RequestBody BsfmFilterFrom bsfmFilterFrom,
			@Parameter(description = "Username of the profile owner", required = true) @RequestParam String username) {
		return bsfmService.updateBsfmProfil(bsfmFilterFrom, username);
	}
/**
 * Deletes a Bsfm profile based on the provided username and profile details.
 * @param username
 * @param bsfmFilterFrom
 * @return
 */
	@Operation(summary = "Delete Bsfm Profile", description = "Delete Bsfm profile by username and profile details")
	@DeleteMapping("/delete")
	public String delete(
			@Parameter(description = "Username of the profile owner", required = true) @RequestParam String username,
			@RequestBody BsfmFilterFrom bsfmFilterFrom) {
		return bsfmService.delete(username, bsfmFilterFrom);
	}
/**
 * Updates the flag of an existing Bsfm profile.
 * @param username
 * @param bsfmFilterFrom
 * @return
 */
	@Operation(summary = "Update Bsfm Profile Flag", description = "Update flag of an existing Bsfm profile")
	@PutMapping("/updateFlag/{username}")
	public String updateBsfmProfileFlag(
			@Parameter(description = "Username of the profile owner", required = true) @PathVariable String username,
			@RequestBody BsfmFilterFrom bsfmFilterFrom) {
		return bsfmService.updateBsfmProfileFlag(username, bsfmFilterFrom);
	}
}
