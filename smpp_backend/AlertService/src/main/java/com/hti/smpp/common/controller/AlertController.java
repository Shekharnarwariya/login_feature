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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.alertThreads.AlertThread;
import com.hti.smpp.common.request.AlertForm;
import com.hti.smpp.common.response.AlertEditResponse;
import com.hti.smpp.common.services.AlertService;
import com.hti.smpp.common.util.dto.AlertDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/alert")
public class AlertController {

	@Autowired
	private AlertService alertService;
	
//	@Autowired
//	private AlertThread alertThread;
//	
	
	@PostMapping("/addAlert")
	public ResponseEntity<String> addAlert(@RequestBody AlertForm alertForm, @RequestHeader(name = "username", required = true) String username){
		
		return this.alertService.saveAlert(alertForm, username);
	}
	
	@GetMapping("/editAlert")
	public ResponseEntity<AlertEditResponse> editAlert(@RequestParam("id") int id, @RequestHeader(name = "username", required = true) String username){
		return this.alertService.editAlert(id, username);
	}
	
	@DeleteMapping("/deleteAlert")
	public ResponseEntity<String> deleteAlert(@RequestParam("id") int id, @RequestHeader(name = "username", required = true) String username){
		return this.alertService.deleteAlert(id, username);
	}
	
	@GetMapping("/getAlerts")
	public ResponseEntity<List<AlertDTO>> getAlerts(@RequestHeader(name = "username", required = true) String username){
		return this.alertService.getAlerts(username);
	}
	
	@PutMapping("/updateAlert/{id}")
	public ResponseEntity<String> updateAlert(@PathVariable("id") int id, @Valid @RequestBody AlertForm alertForm, @RequestHeader(name = "username", required = true) String username){
		return this.alertService.updateAlert(id, alertForm, username);
	}
	
	@GetMapping("/setupAlerts")
	public ResponseEntity<?> setupAlert(@RequestHeader(name = "username", required = true) String username){
		return this.alertService.setupAlert(username);
	}
	
//	 @GetMapping("/triggerAsyncTask")
//	    public String triggerAsyncTask() {
//		 alertThread.run();
//	     return "Async task triggered. Check console for logs.";
//	    }
//	 
	 
	
	
}
