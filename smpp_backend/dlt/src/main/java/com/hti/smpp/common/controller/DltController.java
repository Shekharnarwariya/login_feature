package com.hti.smpp.common.controller;

import java.util.List;

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

//import com.hti.smpp.common.config.dto.DltEntry;
//import com.hti.smpp.common.config.dto.DltTemplEntry;
import com.hti.smpp.common.exception.ExceptionResponse;
import com.hti.smpp.common.request.DltRequest;
import com.hti.smpp.common.request.DltTempRequest;
import com.hti.smpp.common.response.DltResponse;
import com.hti.smpp.common.response.DltTempResponse;
import com.hti.smpp.common.services.DltService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

//DltController handles endpoints related to DLT operations
@RestController
@RequestMapping("/dlt")
@OpenAPIDefinition(info = @Info(title = "SMPP DLT API", version = "1.0", description = "API for managing SMPP DLT"))
public class DltController {

	// Autowired DltService for handling DLT business logic
	@Autowired
	private DltService dltService;

	
	 // Endpoint to save DLT Entry
	@Operation(summary = "Save DLT Entry", description = "This endpoint allows users to Save DLT Entry.", tags = {
	"DLT" })
    @ApiResponses(value = {
	@ApiResponse(responseCode = "200", description = "DLT added successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DltRequest.class))),
	@ApiResponse(responseCode = "201", description = "DLT added successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DltRequest.class))),
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PostMapping("/add-dlt")
	public ResponseEntity<?> saveDltEntry(@Valid @RequestBody DltRequest entry, @RequestHeader("username") String username) {

		return this.dltService.saveDltEntry(entry, username);

	}

	
	// Endpoint to save DLT Template
	@Operation(summary = "Save DLT Template", description = "This endpoint allows users to save DLT Template.", tags = {
	"DLT" })
    @ApiResponses(value = {
	@ApiResponse(responseCode = "200", description = "DLT Template Saved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DltTempRequest.class))),
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PostMapping(value = "/add-dlt-template", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> addDltTemplate(@Valid @RequestParam(value = "entry" , required = false) String entry,
			@RequestPart(value = "file", required = false) MultipartFile file, @RequestHeader("username") String username) {
		
		return this.dltService.addDltTemplate(entry, file, username);
		
	}

	
    // Endpoint to list saved DLT Entries
	@Operation(summary = "List Saved DLT Entries", description = "This endpoint allows users to view the list of DLT Enrty.", tags = {
	"DLT" })
    @ApiResponses(value = {
	@ApiResponse(responseCode = "200", description = "Data fetched successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DltRequest.class))),
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PostMapping("/list-dlt")
	public ResponseEntity<List<DltResponse>> listDltEntry(@RequestHeader("username") String username) {
		return this.dltService.listDltEntry(username);

	}

	
	
	// Endpoint to list DLT Templates
	@Operation(summary = "List DLT Template", description = "This endpoint allows users to view the Listed DLT template.", tags = {
	"DLT" })
@ApiResponses(value = {
	@ApiResponse(responseCode = "200", description = "data fetched successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DltTempRequest.class))),
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PostMapping("/list-dlt-temp")
	public ResponseEntity<List<DltTempResponse>> listDltTemplate(@RequestHeader("username") String username) {
		return this.dltService.listDltTemplate(username);

	}

	
	
	// Endpoint to update DLT Entry
	@Operation(summary = "Update DLT Entry", description = "This endpoint allows users to Update DLT Entry.", tags = {
	"DLT" })
@ApiResponses(value = {
	@ApiResponse(responseCode = "200", description = "Entry Updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DltRequest.class))),
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PutMapping("/update-dlt")
	public ResponseEntity<?> updateDltEntry(@RequestBody DltRequest entry, @RequestHeader("username") String username) {

		return this.dltService.updateDltEntry(entry, username);
	}

	
	
	 // Endpoint to update DLT Template
	@Operation(summary = "Update DLT Template", description = "This endpoint allows users to update DLT template.", tags = {
	"DLT" })
@ApiResponses(value = {
	@ApiResponse(responseCode = "200", description = "DLT template Update successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DltTempRequest.class))),
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@PutMapping("/update-dlt-temp")
	public ResponseEntity<?> updateDltTemplate(@RequestBody DltTempRequest entry,
			@RequestHeader("username") String username) {

		return this.dltService.updateDltTemplate(entry, username);
	}

	
	
    // Endpoint to delete DLT Entry
	@Operation(summary = "Delete DLT Entry", description = "This endpoint allows users to Delete DLT Entry.", tags = {
	"DLT" })
@ApiResponses(value = {
	@ApiResponse(responseCode = "200", description = "Entry Deleted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DltRequest.class))),
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
	@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })

	@DeleteMapping("/delete-dlt/{id}")
	public ResponseEntity<?> deleteDltEntry(@PathVariable Integer id, @RequestHeader("username") String username) {

		return	this.dltService.deleteDltEntry(id, username);
		
//		return new ResponseEntity<>("Deleted Successfully",HttpStatus.OK);
	}

	
	
	 // Endpoint to delete DLT Template
	@Operation(summary = "Delete DLT Template", description = "This endpoint allows users to Delete DLT template.", tags = {
	"DLT" })
@ApiResponses(value = {
	@ApiResponse(responseCode = "200", description = "Template Deleted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DltTempRequest.class))),
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
	@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })

	@DeleteMapping("/delete-dlt-temp/{id}")
	public ResponseEntity<?> deleteDltTemplate(@PathVariable Integer id, @RequestHeader("username") String username) {

		return this.dltService.deleteDltTemplate(id, username);

//		return new ResponseEntity<>("Deleted Successfully",HttpStatus.OK);
		
	}

	
	
	// Endpoint to get a single DLT Entry by ID
	@Operation(summary = "Get Single DLT Entry", description = "This endpoint allows users to view a single dlt entry.", tags = {
	"DLT" })
@ApiResponses(value = {
	@ApiResponse(responseCode = "200", description = "Entry Fetched successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DltRequest.class))),
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
	@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@GetMapping("/get-dlt/{id}")
	public DltResponse getDltEntry( @PathVariable Integer id, @RequestHeader("username") String username) {

		return this.dltService.getDltEntry(id, username);
	}

	
	
	 // Endpoint to get a single DLT Template by ID
	@Operation(summary = "Get Single DLT Template", description = "This endpoint allows users to get single dlt template.", tags = {
	"DLT" })
@ApiResponses(value = {
	@ApiResponse(responseCode = "200", description = "Tempate Fetched successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DltTempRequest.class))),
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
	@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	@GetMapping("/get-dlt-temp/{id}")
	public DltTempResponse getDltTemplate(@PathVariable Integer id, @RequestHeader("username") String username) {

		return this.dltService.getDltTemplate(id, username);
	}

}
