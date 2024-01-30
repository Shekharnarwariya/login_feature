package com.hti.smpp.common.services;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.hti.smpp.common.bsfm.dto.Bsfm;
import com.hti.smpp.common.request.BsfmFilterFrom;
import com.hti.smpp.common.response.BSFMResponse;
import com.hti.smpp.common.response.DeleteProfileResponse;
/**
 * Service interface for Bsfm operations.
 */
public interface BsfmService {

	public ResponseEntity<String> addBsfmProfile(BsfmFilterFrom bsfmFilterFrom, String username);

	public ResponseEntity<BSFMResponse> checked(String username);

	public ResponseEntity<DeleteProfileResponse> deleteProfile(String username, int id);

	public ResponseEntity<List<Bsfm>> showBsfmProfile(String username);

	public ResponseEntity<String> updateBsfmProfile(BsfmFilterFrom bsfmFilterFrom, String username);

	public ResponseEntity<String> delete(String username, String profilename);

	public ResponseEntity<String> updateBsfmProfileFlag(String username, String flag);

}
