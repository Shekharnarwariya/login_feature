package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.request.SubscribeEntryForm;
import com.hti.smpp.common.util.dto.SubscribeEntry;

public interface SubscribeService {

	public ResponseEntity<?> saveSubscribe(String subscribeEntryForm, MultipartFile headerFile, MultipartFile footerFile, String username);
	public ResponseEntity<SubscribeEntry> viewSubscribeEntry(int id, String username);
	public ResponseEntity<List<SubscribeEntry>> listSubscribeEntry(String username);
	public ResponseEntity<?> updateSubscribe(String subscribeEntryForm, MultipartFile headerFile, MultipartFile footerFile, String username);
	public ResponseEntity<?> deleteSubscribe(int id, String username);
}
