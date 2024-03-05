package com.hti.smpp.common.service;

<<<<<<< HEAD
import java.util.List;
=======
import java.io.IOException;
import java.nio.file.Files;
>>>>>>> 16fcd37 (misCounter)

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.request.ConversionData;
import com.hti.smpp.common.responce.ConverterResponse;


public interface DownloadService {
	
	ResponseEntity<?> downloadPricing(String format,String username);
	public ResponseEntity<List<Object>> downloadPricingInList(String username);

	

}
