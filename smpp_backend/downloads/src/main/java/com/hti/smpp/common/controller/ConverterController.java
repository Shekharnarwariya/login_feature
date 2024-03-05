package com.hti.smpp.common.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.request.ConversionData;
import com.hti.smpp.common.service.ConverterService;

@RestController
@RequestMapping("/")
public class ConverterController {

	@Autowired
	private ConverterService converterService;

	@PostMapping("converter")
	public ResponseEntity<?> convert(@RequestBody ConversionData conversionData) {
		List<MultipartFile> files = null;
		return converterService.Converter(files, conversionData);
	}

}
