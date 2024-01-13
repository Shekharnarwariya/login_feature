package com.hti.smpp.common.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.dto.MccMncDTO;
import com.hti.smpp.common.exception.ExceptionResponse;
import com.hti.smpp.common.request.MccMncForm;
import com.hti.smpp.common.request.MccMncUpdateForm;
import com.hti.smpp.common.response.AddResponse;
import com.hti.smpp.common.response.MncMccTokens;
import com.hti.smpp.common.response.SearchResponse;
import com.hti.smpp.common.services.NetworkService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

//Controller for managing SMPP Network entries
@OpenAPIDefinition(info = @Info(title = "SMPP Network API", version = "1.0", description = "API for managing SMPP Network"))
@RestController
@RequestMapping("/network")
@Tag(name = "NetworkController", description = "API's for network")
public class NetworkController {

	@Autowired
	private NetworkService networkService;

	// Add a new Network entry
	@Operation(summary = "Add New MccMnc", description = "Save a new mcc mnc")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Network Entry Saved Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AddResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "409", description = "Duplicate Entry. Confilict!.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AddResponse.class))) })
	@PostMapping(value = "/addNewMccMnc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> addNewMccMnc(
			@Parameter(description = "Network Entry request", content = @Content(schema = @Schema(implementation = MccMncForm.class))) @RequestParam(name = "formMccMnc", required = false) String formMccMnc,
			@RequestPart(name = "listFile", required = false) MultipartFile file,
			@RequestHeader(name = "username", required = true) String username) {
		return this.networkService.addNewMccMnc(formMccMnc, file, username);
	}

	// Update an existing Network entry
	@Operation(summary = "Update MccMnc", description = "Update existing mcc mnc")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Network Entry Updated Successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PutMapping("/replace")
	public ResponseEntity<String> replace(@RequestBody MccMncUpdateForm form,
			@RequestHeader(name = "username", required = true) String username) {
		return this.networkService.replace(form, username);
	}

	// Delete an existing network entry by ID
	@Operation(summary = "Delete MccMnc", description = "Delete existing mcc mnc by id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Network Entry Deleted Successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@DeleteMapping("/delete")
	public ResponseEntity<String> delete(@RequestParam(value = "ids", required = true) List<Integer> ids,
			@RequestHeader(name = "username", required = true) String username) {
		return this.networkService.delete(ids, username);
	}

	// Retrieve a list of NetworkEntry based on specified parameters
	@Operation(summary = "Search list of NetworkEntry", description = "Retrieve the list of NetworkEntry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Network Entry List Fetched Successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@GetMapping("/search")
	public ResponseEntity<SearchResponse> search(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, @RequestParam(name = "cc") String ccReq,
			@RequestParam(name = "mcc") String mccReq, @RequestParam(name = "mnc") String mncReq,
			@RequestParam(name = "checkCountry") String checkCountryReq,
			@RequestParam(name = "checkMcc") String checkMccReq, @RequestParam(name = "checkMnc") String checkMncReq,
			@RequestHeader(name = "username", required = true) String username) {
		return this.networkService.search(ccReq, mccReq, mncReq, checkCountryReq, checkMccReq, checkMncReq, username,
				PageRequest.of(page, size));
	}

	// Download mccmnc_database.xls File based on specified parameters
	@Operation(summary = "Download mccmnc_database.xls File", description = "Download the mccmnc_database.xls")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "mccmnc_database.xls File Downloaded Successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@GetMapping("/download")
	public ResponseEntity<byte[]> download(@RequestParam(name = "cc") String ccReq,
			@RequestParam(name = "mcc") String mccReq, @RequestParam(name = "mnc") String mncReq,
			@RequestParam(name = "checkCountry") String checkCountryReq,
			@RequestParam(name = "checkMcc") String checkMccReq, @RequestParam(name = "checkMnc") String checkMncReq,
			@RequestHeader(name = "username", required = true) String username) {
		return this.networkService.download(ccReq, mccReq, mncReq, checkCountryReq, checkMccReq, checkMncReq, username);
	}

	// Retrieve the NetworkMap Of Country And CC From All NetworkEntry
	@Operation(summary = "Get Country Name Map", description = "Retrieve the NetworkMap Of Country And CC From All NetworkEntry")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Networkmap retrieved successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@GetMapping("/getCountry")
	public ResponseEntity<?> editMccMnc(@RequestHeader(name = "username", required = true) String username) {
		return this.networkService.editMccMnc(username);
	}

	// Retrieve Distinct MCC By CC From All NetworkEntry
	@Operation(summary = "Get MCC By CC", description = "Retrieve Distinct MCC By CC From All NetworkEntry")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "MCC retrieved successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@GetMapping("/getMCC")
	public ResponseEntity<?> findMccByCC(@RequestParam(name = "cc", required = true) String cc,
			@RequestHeader(name = "username", required = true) String username) {
		return this.networkService.findMcc(cc, username);
	}

	// Retrieve Distinct MNC By MCC From All NetworkEntry
	@Operation(summary = "Get MNC By MCC", description = "Retrieve Distinct MNC By MCC From All NetworkEntry")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Networkmap retrieved successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@GetMapping("/getMNC")
	public ResponseEntity<?> findMncByMcc(@RequestParam(name = "mcc", required = true) String mcc,
			@RequestHeader(name = "username", required = true) String username) {
		return this.networkService.findMnc(mcc, username);
	}

	// Update an existing network entry by uploading a file
	@Operation(summary = "Update mcc mnc", description = "Update an existing network by upload")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Network updated successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PutMapping(value = "/uploadUpdateMccMnc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> uploadUpdateMccMnc(@RequestPart(name = "listFile", required = true) MultipartFile file,
			@RequestHeader(name = "username", required = true) String username) {
		return this.networkService.uploadUpdateMccMnc(file, username);
	}

	// Retrieve MncMccTokens based on specified parameters
	@Operation(summary = "FindOption MncMccTokens", description = "To retrieve MncMcctokens")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "MncMccTokens retrieved successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MncMccTokens.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@GetMapping("/findOption")
	public ResponseEntity<MncMccTokens> findOption(
			@RequestParam(name = "countryName", required = true) String countryName,
			@RequestParam(name = "mccParam", required = true) String mccParam,
			@RequestHeader(name = "username", required = true) String username) {
		return this.networkService.findOption(countryName, mccParam, username);
	}

}
