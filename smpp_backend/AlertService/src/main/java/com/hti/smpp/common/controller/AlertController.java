package com.hti.smpp.common.controller;

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

import com.hti.smpp.common.exception.ExceptionResponse;
import com.hti.smpp.common.request.AlertForm;
import com.hti.smpp.common.response.AlertEditResponse;
import com.hti.smpp.common.services.AlertService;
import com.hti.smpp.common.util.dto.AlertDTO;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


@OpenAPIDefinition(info = @Info(title = "SMPP Alert API", version = "1.0", description = "API for managing alerts"))
@RestController
@RequestMapping("/alerts")
@Tag(name = "AlertController", description = "APIs for Alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @Operation(summary = "Add Alert", description = "Add a new alert")
    @PostMapping("/add")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Alert added successfully."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
    })
    public ResponseEntity<String> addAlert(@Valid @RequestBody AlertForm alertForm,
                                          @RequestHeader(name = "username", required = true) String username) {
        return alertService.saveAlert(alertForm, username);
    }

    @Operation(summary = "Edit Alert", description = "Edit an existing alert by ID")
    @GetMapping("/edit/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Edit response of Alert."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
    })
    public ResponseEntity<AlertEditResponse> editAlert(@PathVariable("id") int id,
                                                       @RequestHeader(name = "username", required = true) String username) {
        return alertService.editAlert(id, username);
    }

    @Operation(summary = "Delete Alert", description = "Delete an existing alert by ID")
    @DeleteMapping("/delete/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alert deleted successfully."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
    })
    public ResponseEntity<String> deleteAlert(@PathVariable("id") int id,
                                              @RequestHeader(name = "username", required = true) String username) {
        return alertService.deleteAlert(id, username);
    }

    @Operation(summary = "Get Alerts", description = "Get a list of all alerts")
    @GetMapping("/get")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List response of Alerts."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
    })
    public ResponseEntity<List<AlertDTO>> getAlerts(@RequestHeader(name = "username", required = true) String username) {
        return alertService.getAlerts(username);
    }

    @Operation(summary = "Update Alert", description = "Update an existing alert by ID")
    @PutMapping("/update/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Alert Updated Successfully."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
    })
    public ResponseEntity<String> updateAlert(@PathVariable("id") int id,
                                              @Valid @RequestBody AlertForm alertForm,
                                              @RequestHeader(name = "username", required = true) String username) {
        return alertService.updateAlert(id, alertForm, username);
    }

    @Operation(summary = "Setup Alerts", description = "Setup alerts")
    @GetMapping("/setup")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alert setup successful."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
    })
    public ResponseEntity<?> setupAlert(@RequestHeader(name = "username", required = true) String username) {
        return alertService.setupAlert(username);
    }}



//@RestController
//@RequestMapping("/alert")
//public class AlertController {
//
//	@Autowired
//	private AlertService alertService;
//	
////	@Autowired
////	private AlertThread alertThread;
////	
//	
//	@PostMapping("/addAlert")
//	public ResponseEntity<String> addAlert(@RequestBody AlertForm alertForm, @RequestHeader(name = "username", required = true) String username){
//		
//		return this.alertService.saveAlert(alertForm, username);
//	}
//	
//	@GetMapping("/editAlert")
//	public ResponseEntity<AlertEditResponse> editAlert(@RequestParam("id") int id, @RequestHeader(name = "username", required = true) String username){
//		return this.alertService.editAlert(id, username);
//	}
//	
//	@DeleteMapping("/deleteAlert")
//	public ResponseEntity<String> deleteAlert(@RequestParam("id") int id, @RequestHeader(name = "username", required = true) String username){
//		return this.alertService.deleteAlert(id, username);
//	}
//	
//	@GetMapping("/getAlerts")
//	public ResponseEntity<List<AlertDTO>> getAlerts(@RequestHeader(name = "username", required = true) String username){
//		return this.alertService.getAlerts(username);
//	}
//	
//	@PutMapping("/updateAlert/{id}")
//	public ResponseEntity<String> updateAlert(@PathVariable("id") int id, @Valid @RequestBody AlertForm alertForm, @RequestHeader(name = "username", required = true) String username){
//		return this.alertService.updateAlert(id, alertForm, username);
//	}
//	
//	@GetMapping("/setupAlerts")
//	public ResponseEntity<?> setupAlert(@RequestHeader(name = "username", required = true) String username){
//		return this.alertService.setupAlert(username);
//	}
//	
////	 @GetMapping("/triggerAsyncTask")
////	    public String triggerAsyncTask() {
////		 alertThread.run();
////	     return "Async task triggered. Check console for logs.";
////	    }
////	 
//	 
//	
//	
//}
