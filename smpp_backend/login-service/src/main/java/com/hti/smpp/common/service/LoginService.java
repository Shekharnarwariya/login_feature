package com.hti.smpp.common.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.request.LoginDTO;
import com.hti.smpp.common.request.LoginRequest;
import com.hti.smpp.common.request.PasswordUpdateRequest;
import com.hti.smpp.common.request.SignupRequest;
import com.hti.smpp.common.user.dto.ProfessionEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Configures and provides a DaoAuthenticationProvider bean.
 */
@Service
public interface LoginService {

	public ResponseEntity<?> loginjwt(LoginRequest loginRequest, HttpServletRequest request, WebMasterEntry webMaster,
			ProfessionEntry professionEntry, LoginDTO loginDTO, UserEntry userEntry);

	public ResponseEntity<?> profile(String username);

	public ResponseEntity<?> registerUser(SignupRequest signUpRequest);

	public ResponseEntity<?> validateOtp(String username, String otp);

	public ResponseEntity<?> forgotPassword(String newPassword, String username);

	public ResponseEntity<?> sendOTP(String username);

	public ResponseEntity<?> updatePassword(PasswordUpdateRequest passwordUpdateRequest, String username);

	public ResponseEntity<?> updateUserProfile(String username, String email, String firstName, String lastName,
			String contact, String companyName, String designation, String city, String country, String state,
			String keepLogs, String referenceID, String companyAddress, String companyEmail, String notes, String taxID,
			String regID, MultipartFile profileImageFile, String alertEmail, String alertMobile, String invoiceEmail,
			Boolean dlrReport, String dlrEmail, String coverageEmail, String coverageReport, Double lowAmount,
			Boolean smsAlert, String webUrl, Boolean dlrThroughWeb, Boolean mis, Boolean lowBalanceAlert);

	public ResponseEntity<?> userRecentActivity(String username);

	public ResponseEntity<?> login(LoginRequest loginRequest, HttpServletRequest request);

}
