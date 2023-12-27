package com.hti.smpp.common.twoway.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
/**
 * The `TwowayController` class serves as a controller in the Two-Way Application.
 */
public class TwowayController {
    
    @Autowired 
    private KeywordService keywordService;
   /**
    * The `TwowayController` class is a part of the Two-Way Application and handles 
    * @param form
    * @param username
    * @return
    */
    @Operation(summary = "Save Keyword Entry", description = "Save a new keyword entry")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "KeywordEntry Saved Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
	})
    @PostMapping("/addkeyword")
    public ResponseEntity<String> addKeyword(@Valid @RequestBody KeywordEntryForm form, @Parameter(description = "Username in header") @RequestHeader(value="username", required = true) String username){
    	return this.keywordService.addKeyword(form, username);
    }
   /**
    * The `TwowayController` class, a part of the Two-Way Application, handles HTTP
    * requests related to listing keyword entries. 
    * @param username
    * @return
    */
    @Operation(summary = "List KeywordEntry", description = "List the keyword entry")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "List response of Keyword Entry."),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
    })
    /**
     * The `TwowayController` class, a part of the Two-Way Application, handles HTTP
     * requests related to keyword entries.
     * @param username
     * @return
     */
    public ResponseEntity<List<KeywordEntry>> listKeyword(@RequestHeader(value="username", required = true) String username){
    	return this.keywordService.listKeyword(username);
    }
    
    @Operation(summary = "Update Keyword Entry", description = "Update an existing keyword entry")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "KeywordEntry Updated Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
	})
    /**
     * Updates an existing keyword entry.
     * @param form
     * @param username
     * @return
     */
    @PutMapping("/update-keyword")
    public ResponseEntity<String> updateKeyword(@Valid @RequestBody KeywordEntryForm form, @RequestHeader(value="username", required = true) String username){
    	return this.keywordService.updateKeyword(form, username);
    }
    
    @Operation(summary = "Delete Keyword Entry", description = "Delete an existing keyword entry")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "KeywordEntry Deleted Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
	})
    /**
     * Deletes a keyword entry by ID.
     * @param id
     * @param username
     * @return
     */
    @DeleteMapping("/delete-keyword/{id}")
    public ResponseEntity<String> deleteKeyword(@PathVariable(value = "id", required = true) int id, @RequestHeader(value="username", required = true) String username){
    	return this.keywordService.deleteKeyword(id, username);
    	
    }
    
    @Operation(summary = "Setup Keyword", description = "Returns the list as a response of UserEntry")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Setup Keyword Successful."),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
	})
    /**
     * Initializes keyword setup
     * @param username
     * @return
     */
    @GetMapping("/setupkeyword")
    public ResponseEntity<?> setupKeyword(@RequestHeader(value="username", required = true) String username){
    	return this.keywordService.setupKeyword(username);
    }
    
    @Operation(summary = "View Keyword", description = "Returns the KeywordEntry as a response by id")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "View KeywordEntry by id.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = KeywordEntry.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
	})
    /**
     * Retrieves details of a specific keyword entry by ID.
     * @param id
     * @param username
     * @return
     */
    @GetMapping("/viewkeyword/{id}")
    public ResponseEntity<KeywordEntry> viewKeyword(@PathVariable(value = "id", required = true) int id, @RequestHeader(value="username", required = true) String username){
    	return this.keywordService.viewKeyword(id, username);
    }
    
    @Operation(summary = "Generate Xls", description = "Creates the twoway report in .xlsx")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Twoway Report Creation Successful in .xlsx."),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Error Processing report", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request. Error Accessing data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))
    })
    /**
     *  Generates and returns an Excel file based on the provided report form and locale.
     * @param form
     * @param locale
     * @param username
     * @return
     */
    @PostMapping(value = "/generate/xls/{locale}")
    public ResponseEntity<StreamingResponseBody> generateXls(@Valid @RequestBody TwowayReportForm form, @PathVariable(value = "locale", required=true) String locale, @RequestHeader(value="username", required = true) String username){
    	return this.keywordService.generateXls(form, locale, username);
    }
    
    @Operation(summary = "Generate Pdf", description = "Creates the twoway report in .pdf")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Twoway Report Creation Successful in .pdf."),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Error Processing report", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request. Error Accessing data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))
    })
    /**
     * Generates and returns a PDF file based on the provided report form and locale.
     * @param form
     * @param locale
     * @param username
     * @return
     */
    @PostMapping("/generate/pdf/{locale}")
    public ResponseEntity<StreamingResponseBody> generatePdf(@Valid @RequestBody TwowayReportForm form, @PathVariable(value = "locale", required=true) String locale, @RequestHeader(value="username", required = true) String username){
    	return this.keywordService.generatePdf(form, locale, username);
    }
    
    @Operation(summary = "Generate Doc", description = "Creates the twoway report in .doc")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Twoway Report Creation Successful in .doc."),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Error Processing report", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request. Error Accessing data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))
    })
    /**
     *  Generates and returns a Word document based on the provided report form and locale
     * @param form
     * @param locale
     * @param username
     * @return
     */
    @PostMapping("/generate/doc/{locale}")
    public ResponseEntity<StreamingResponseBody> generateDoc(@Valid @RequestBody TwowayReportForm form, @PathVariable(value = "locale", required=true) String locale, @RequestHeader(value="username", required = true) String username){
    	return this.keywordService.generateDoc(form, locale, username);
    }
    
    @Operation(summary = "View Report", description = "Returns the response of View")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Twoway ViewReport."),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Error Processing report", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request. Error Accessing data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))
    })
    /**
     * Generates and returns a view for a report based on the provided form and locale.
     * @param form
     * @param locale
     * @param username
     * @return
     */
    @PostMapping("/view/{locale}")
    public ResponseEntity<?> viewReport(@Valid @RequestBody TwowayReportForm form, @PathVariable(value = "locale", required=true) String locale, @RequestHeader(value="username", required = true) String username){
    	return this.keywordService.view(form, locale, username);
    }
    
    @Operation(summary = "Setup TwowayReport", description = "Returns the list as a response of UserEntry")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Setup Twoway Report Successful."),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))
    })
    /**
     * Initializes setup for the Two-Way Report.
     * @param username
     * @return
     */
    @GetMapping("/setup-twowayreport")
    public ResponseEntity<?> setupTwowayReport(@RequestHeader(value="username", required = true) String username){
    	return this.keywordService.setupTwowayReport(username);
    }

}
