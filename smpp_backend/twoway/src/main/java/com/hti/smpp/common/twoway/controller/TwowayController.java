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

import com.hti.smpp.common.twoway.dto.KeywordEntry;
import com.hti.smpp.common.twoway.request.KeywordEntryForm;
import com.hti.smpp.common.twoway.service.KeywordService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.util.IConstants;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/twoway")
public class TwowayController {
    
    @Autowired 
    private KeywordService keywordService;
    
    @PostMapping("/addkeyword")
    public ResponseEntity<String> addKeyword(@Valid @RequestBody KeywordEntryForm form, @RequestHeader(value="username", required = true) String username){
    	String response = this.keywordService.addKeyword(form, username);
    	if(response.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
    		return new ResponseEntity<>(response, HttpStatus.CREATED);
    	}else {
    		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    	}
    }
    
    @GetMapping("/listkeyword")
    public ResponseEntity<?> listKeyword(@RequestHeader(value="username", required = true) String username){
    	List<KeywordEntry> response = this.keywordService.listKeyword(username);
    	
    	if(response!=null && !response.isEmpty()) {
    		return new ResponseEntity<>(response, HttpStatus.OK);
    	}else {
    		return new ResponseEntity<>("No List Keyword Found.",HttpStatus.BAD_REQUEST);
    	}
    }
    
    @PutMapping("/update-keyword")
    public ResponseEntity<String> updateKeyword(@Valid @RequestBody KeywordEntryForm form, @RequestHeader(value="username", required = true) String username){
    	String response = this.keywordService.updateKeyword(form, username);
    	if(response.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
    		return new ResponseEntity<>(response, HttpStatus.CREATED);
    	}else {
    		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    	}
    }
    
    @DeleteMapping("/delete-keyword/{id}")
    public ResponseEntity<String> deleteKeyword(@PathVariable(value = "id", required = true) int id, @RequestHeader(value="username", required = true) String username){
    	String response = this.keywordService.deleteKeyword(id, username);
    	if(response.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
    		return new ResponseEntity<>(response, HttpStatus.OK);
    	}else {
    		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    	}
    }
    
    @GetMapping("/setupkeyword")
    public ResponseEntity<?> setupKeyword(@RequestHeader(value="username", required = true) String username){
    	Collection<UserEntry> response = this.keywordService.setupKeyword(username);
    	if(response!=null && response.isEmpty()) {
    		return new ResponseEntity<>(response,HttpStatus.OK);
    	}else {
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    	}
    }
    
    @GetMapping("/viewkeyword/{id}")
    public ResponseEntity<KeywordEntry> viewKeyword(@PathVariable(value = "id", required = true) int id, @RequestHeader(value="username", required = true) String username){
    	KeywordEntry response = this.keywordService.viewKeyword(id, username);
    	if(response != null) {
    		return new ResponseEntity<>(response, HttpStatus.OK);
    	}else {
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    	}
    }
    

}
