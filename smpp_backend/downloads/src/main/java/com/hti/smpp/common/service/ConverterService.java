package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.request.ConversionData;
@Service
public interface ConverterService {
	public ResponseEntity<?> Converter(List<MultipartFile> files, ConversionData request);
  

}
