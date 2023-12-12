package com.hti.smpp.common.twoway.controller;


import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.hti.smpp.common.twoway.dto.KeywordEntry;
import com.hti.smpp.common.twoway.request.KeywordEntryForm;
import com.hti.smpp.common.twoway.request.TwowayReportForm;
import com.hti.smpp.common.twoway.service.KeywordService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.util.IConstants;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@OpenAPIDefinition(info = @Info(title = "SMPP Twoway API", version = "1.0", description = "API for managing SMPP Twoway"))
@RestController
@RequestMapping("/twoway")
@Tag(name = "TwowayController", description = "API's for Twoway")
public class TwowayController {
    
    @Autowired 
    private KeywordService keywordService;
    
    @Operation(summary = "Save Keyword Entry", description = "Save a new keyword entry")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "KeywordEntry Saved Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    @PostMapping("/addkeyword")
    public ResponseEntity<String> addKeyword(@Valid @RequestBody KeywordEntryForm form, @Parameter(description = "Username in header") @RequestHeader(value="username", required = true) String username){
    	String response = this.keywordService.addKeyword(form, username);
    	if(response.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
    		return new ResponseEntity<>(response, HttpStatus.CREATED);
    	}else {
    		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    	}
    }
    
    @Operation(summary = "List KeywordEntry", description = "List the keyword entry")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "List response of Keyword Entry."),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    public ResponseEntity<?> listKeyword(@RequestHeader(value="username", required = true) String username){
    	List<KeywordEntry> response = this.keywordService.listKeyword(username);
    	
    	if(response!=null && !response.isEmpty()) {
    		return new ResponseEntity<>(response, HttpStatus.OK);
    	}else {
    		return new ResponseEntity<>("No List Keyword Found.",HttpStatus.BAD_REQUEST);
    	}
    }
    
    @Operation(summary = "Update Keyword Entry", description = "Update an existing keyword entry")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "KeywordEntry Updated Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    @PutMapping("/update-keyword")
    public ResponseEntity<String> updateKeyword(@Valid @RequestBody KeywordEntryForm form, @RequestHeader(value="username", required = true) String username){
    	String response = this.keywordService.updateKeyword(form, username);
    	if(response.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
    		return new ResponseEntity<>(response, HttpStatus.CREATED);
    	}else {
    		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    	}
    }
    
    @Operation(summary = "Delete Keyword Entry", description = "Delete an existing keyword entry")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "KeywordEntry Deleted Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    @DeleteMapping("/delete-keyword/{id}")
    public ResponseEntity<String> deleteKeyword(@PathVariable(value = "id", required = true) int id, @RequestHeader(value="username", required = true) String username){
    	String response = this.keywordService.deleteKeyword(id, username);
    	if(response.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
    		return new ResponseEntity<>(response, HttpStatus.OK);
    	}else {
    		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    	}
    }
    
    @Operation(summary = "Setup Keyword", description = "Returns the list as a response of UserEntry")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Setup Keyword Successful."),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    @GetMapping("/setupkeyword")
    public ResponseEntity<?> setupKeyword(@RequestHeader(value="username", required = true) String username){
    	Collection<UserEntry> response = this.keywordService.setupKeyword(username);
    	if(response!=null && response.isEmpty()) {
    		return new ResponseEntity<>(response,HttpStatus.OK);
    	}else {
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    	}
    }
    
    @Operation(summary = "View Keyword", description = "Returns the KeywordEntry as a response by id")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "View KeywordEntry by id.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = KeywordEntry.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    @GetMapping("/viewkeyword/{id}")
    public ResponseEntity<KeywordEntry> viewKeyword(@PathVariable(value = "id", required = true) int id, @RequestHeader(value="username", required = true) String username){
    	KeywordEntry response = this.keywordService.viewKeyword(id, username);
    	if(response != null) {
    		return new ResponseEntity<>(response, HttpStatus.OK);
    	}else {
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    	}
    }
    
    @Operation(summary = "Generate Xls", description = "Creates the twoway report in .xlsx")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Twoway Report Creation Successful in .xlsx."),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Error Processing report", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request. Error Accessing data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    @PostMapping(value = "/generate/xls/{locale}")
    public ResponseEntity<StreamingResponseBody> generateXls(@Valid @RequestBody TwowayReportForm form, @PathVariable(value = "locale", required=true) String locale, @RequestHeader(value="username", required = true) String username){
    	return this.keywordService.generateXls(form, locale, username);
    }
    
    @Operation(summary = "Generate Pdf", description = "Creates the twoway report in .pdf")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Twoway Report Creation Successful in .pdf."),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Error Processing report", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request. Error Accessing data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    @PostMapping("/generate/pdf/{locale}")
    public ResponseEntity<StreamingResponseBody> generatePdf(@Valid @RequestBody TwowayReportForm form, @PathVariable(value = "locale", required=true) String locale, @RequestHeader(value="username", required = true) String username){
    	return this.keywordService.generatePdf(form, locale, username);
    }
    
    @Operation(summary = "Generate Doc", description = "Creates the twoway report in .doc")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Twoway Report Creation Successful in .doc."),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Error Processing report", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request. Error Accessing data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    @PostMapping("/generate/doc/{locale}")
    public ResponseEntity<StreamingResponseBody> generateDoc(@Valid @RequestBody TwowayReportForm form, @PathVariable(value = "locale", required=true) String locale, @RequestHeader(value="username", required = true) String username){
    	return this.keywordService.generateDoc(form, locale, username);
    }
    
    @Operation(summary = "View Report", description = "Returns the response of View")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Twoway ViewReport."),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Error Processing report", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request. Error Accessing data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    @PostMapping("/view/{locale}")
    public ResponseEntity<?> viewReport(@Valid @RequestBody TwowayReportForm form, @PathVariable(value = "locale", required=true) String locale, @RequestHeader(value="username", required = true) String username){
    	return this.keywordService.view(form, locale, username);
    }
    
    @Operation(summary = "Setup TwowayReport", description = "Returns the list as a response of UserEntry")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Setup Twoway Report Successful."),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    @GetMapping("/setup-twowayreport")
    public ResponseEntity<?> setupTwowayReport(@RequestHeader(value="username", required = true) String username){
    	Collection<UserEntry> response = this.keywordService.setupTwowayReport(username);
    	if(response!=null && response.isEmpty()) {
    		return new ResponseEntity<>(response,HttpStatus.OK);
    	}else {
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    	}
    }

}
