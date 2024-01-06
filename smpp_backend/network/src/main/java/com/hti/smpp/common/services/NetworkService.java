package com.hti.smpp.common.services;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.dto.MccMncDTO;
import com.hti.smpp.common.request.MccMncForm;
import com.hti.smpp.common.request.MccMncUpdateForm;
import com.hti.smpp.common.response.MncMccTokens;

public interface NetworkService {
	
	public ResponseEntity<String> addNewMccMnc(String formMccMnc, MultipartFile file, String username);
	public ResponseEntity<String> replace(MccMncUpdateForm form, String username);
	public ResponseEntity<String> delete(List<Integer> ids, String username);
	public ResponseEntity<List<MccMncDTO>> search(String ccReq, String mccReq, String mncReq, String checkCountryReq, String checkMccReq, String checkMncReq, String username);
	public ResponseEntity<byte[]> download(String ccReq, String mccReq, String mncReq, String checkCountryReq, String checkMccReq, String checkMncReq, String username);
	public ResponseEntity<?> editMccMnc(String username);
	public ResponseEntity<?> uploadUpdateMccMnc(String mccMncForm, MultipartFile file, String username);
	public ResponseEntity<MncMccTokens> findOption(String countryName, String mccParam, String username); 
}
