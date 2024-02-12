package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface MediaUploadService {

	public ResponseEntity<?> UploadMedia(String title, List<String> link_urls, List<MultipartFile> items);

}
