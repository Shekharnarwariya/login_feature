package com.hti.smpp.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.request.LoginRequest;
import com.hti.smpp.common.request.PasswordUpdateRequest;
import com.hti.smpp.common.request.ProfileUpdateRequest;
import com.hti.smpp.common.request.SignupRequest;
import com.hti.smpp.common.service.LoginService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/login")
@Tag(name = "Login Controller", description = "APIs related to user authentication and profile management")
public class LoginController {

	@Autowired
	private LoginService loginService;

	@PostMapping("/jwt")
	@Operation(summary = "Authenticate User", description = "Endpoint to authenticate a user and generate a JWT token.")
	public ResponseEntity<?> authenticateUser(@RequestBody @Valid LoginRequest loginRequest) {
		System.out.println("authenticate Username" + loginRequest.getUsername());
		return loginService.login(loginRequest);
	}

	@PostMapping("/register")
	@Operation(summary = "Register User", description = "Endpoint to register a new user.")
	public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
		return loginService.registerUser(signUpRequest);
	}

	@GetMapping("/user/data")
	public ResponseEntity<?> getUserProfile(@RequestHeader("username") String username) {
		return loginService.profile(username);
	}

	@PostMapping("/status")
	public ResponseEntity<?> getStatus(@RequestHeader("username") String username) {
		return new ResponseEntity<>("login service running ..", HttpStatus.OK);
	}

	@PostMapping("/validateOtp")
	public ResponseEntity<?> validateOtp(@RequestHeader("username") String username, @RequestParam String otp) {
		return loginService.validateOtp(username, otp);
	}

	@PutMapping("/forgotPassword")
	public ResponseEntity<?> forgotPassword(@RequestParam String newPassword,
			@RequestHeader("username") String username) {
		return loginService.forgotPassword(newPassword, username);
	}

	@PostMapping("/sendOTP")
	public ResponseEntity<?> sendOTP(@RequestHeader("username") String username) {
		return loginService.sendOTP(username);
	}

	@PutMapping("/updatePassword")
	public ResponseEntity<?> updatePassword(@RequestBody PasswordUpdateRequest passwordUpdateRequest,
			@RequestHeader("username") String username) {
		return loginService.updatePassword(passwordUpdateRequest, username);
	}

	@PutMapping("/updateProfile")
	public ResponseEntity<?> updateUserProfile(@RequestHeader("username") String username,
			@RequestBody ProfileUpdateRequest profileUpdateRequest) {
		return loginService.updateUserProfile(username, profileUpdateRequest);

	}
}
