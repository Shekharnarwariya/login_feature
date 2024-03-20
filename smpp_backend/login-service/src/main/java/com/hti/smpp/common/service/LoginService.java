package com.hti.smpp.common.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.request.LoginRequest;
import com.hti.smpp.common.request.PasswordUpdateRequest;
import com.hti.smpp.common.request.SignupRequest;
/**
 * Configures and provides a DaoAuthenticationProvider bean.
 */
@Service
public interface LoginService {

	public ResponseEntity<?> login(LoginRequest loginRequest);

	public ResponseEntity<?> profile(String username);

	public ResponseEntity<?> registerUser(SignupRequest signUpRequest);

	public ResponseEntity<?> validateOtp(String username, String otp);

	public ResponseEntity<?> forgotPassword(String newPassword, String username);

	public ResponseEntity<?> sendOTP(String username);
	
	public ResponseEntity<?> updatePassword(PasswordUpdateRequest passwordUpdateRequest,String username);
	
	public ResponseEntity<?> updateUserProfile(String username,String email,String firstName,String lastName,String contact,MultipartFile profileImageFile);
	
	public ResponseEntity<?> validateUserIpAccess(LoginRequest loginRequest, String language);
	
	public ResponseEntity<?> sellerValidation(String username, String password);
	
	public ResponseEntity<?> userIpOtpValidate(LoginRequest loginRequest, int otp);
}
