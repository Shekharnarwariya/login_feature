package com.hti.smpp.common.services;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.hti.smpp.common.request.AlertForm;
import com.hti.smpp.common.response.AlertEditResponse;
import com.hti.smpp.common.util.dto.AlertDTO;

public interface AlertService {

	public ResponseEntity<String> saveAlert(AlertForm form, String username);
	public ResponseEntity<AlertEditResponse> editAlert(int id, String username);
	public ResponseEntity<String> deleteAlert(int id, String username);
	public ResponseEntity<List<AlertDTO>> getAlerts(String username);
	public ResponseEntity<String> updateAlert(int id, AlertForm form, String username);
	public ResponseEntity<?>setupAlert(String username);
	
}
