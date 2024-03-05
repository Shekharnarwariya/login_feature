package com.hti.smpp.common.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.request.ConversionData;
import com.hti.smpp.common.responce.ConverterResponse;
import com.hti.smpp.common.service.ConverterService;
import com.hti.smpp.common.service.DownloadService;

@RestController
@RequestMapping("/download")
public class DownloadController {
	
	@Autowired
	private DownloadService downloadService;
	

	
	@GetMapping("/pricing")
	public ResponseEntity<?> downloadPricingFormat(@RequestParam("format") String format, @RequestHeader("username") String username){
		return this.downloadService.downloadPricing(format, username);
	}
<<<<<<< HEAD
	@GetMapping("/pricing-list")
	public ResponseEntity<List<Object>> downloadPricingInList(@RequestHeader("username") String username){
		return this.downloadService.downloadPricingInList(username);
	}
=======
	

>>>>>>> 16fcd37 (misCounter)
}
