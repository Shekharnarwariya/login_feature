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
/**
 * This class handles HTTP requests related to user authentication and profile management.
 */
@RestController
@RequestMapping("/login")
@Tag(name = "Login Controller", description = "APIs related to user authentication and profile management")
public class LoginController {

	@Autowired
	private LoginService loginService;
/**
 * RESTful endpoint for user authentication.
 * This endpoint receives a POST request with user credentials,
 * authenticates the user, and generates a JWT token for successful authentication.
 * @param loginRequest
 * @return
 */
	@PostMapping("/jwt")
	@Operation(summary = "Authenticate User", description = "Endpoint to authenticate a user and generate a JWT token.")
	public ResponseEntity<?> authenticateUser(@RequestBody @Valid LoginRequest loginRequest) {
		System.out.println("authenticate Username" + loginRequest.getUsername());
		return loginService.login(loginRequest);
	}
/**
 * RESTful endpoint for registering a new user.
 * This endpoint receives a POST request with user registration details,
 * validates the input, and registers a new user using the provided information.
 * @param signUpRequest
 * @return
 */
	@PostMapping("/register")
	@Operation(summary = "Register User", description = "Endpoint to register a new user.")
	public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
		return loginService.registerUser(signUpRequest);
	}
	
/**
 * RESTful endpoint for retrieving user profile data.
 * This endpoint receives a GET request with the username provided in the request header.
 * It retrieves and returns the profile data associated with the specified username.
 * @param username
 * @return
 */
	@GetMapping("/user/data")
	public ResponseEntity<?> getUserProfile(@RequestHeader("username") String username) {
		return loginService.profile(username);
	}
/**
 * RESTful endpoint for checking the status of the login service.
 * This endpoint receives a POST request with the username provided in the request header.
 * It returns a simple message indicating that the login service is running.
 * @param username
 * @return
 */
	@PostMapping("/status")
	public ResponseEntity<?> getStatus(@RequestHeader("username") String username) {
		return new ResponseEntity<>("login service running ..", HttpStatus.OK);
	}
/**
 * RESTful endpoint for validating an OTP (One-Time Password).
 * This endpoint receives a POST request with the username provided in the request header
 * and an OTP parameter. It validates the provided OTP for the specified username.
 * @param username
 * @param otp
 * @return
 */
	@PostMapping("/validateOtp")
	public ResponseEntity<?> validateOtp(@RequestHeader("username") String username, @RequestParam String otp) {
		return loginService.validateOtp(username, otp);
	}
/**
 * RESTful endpoint for handling forgot password requests.
 * This endpoint receives a PUT request with the new password as a request parameter
 * and the username provided in the request header. It processes the forgot password request
 * by updating the user's password with the new one.
 * @param newPassword
 * @param username
 * @return
 */
	@PutMapping("/forgotPassword")
	public ResponseEntity<?> forgotPassword(@RequestParam String newPassword,
			@RequestHeader("username") String username) {
		return loginService.forgotPassword(newPassword, username);
	}
/**
 * RESTful endpoint for sending an OTP (One-Time Password).
 * This endpoint receives a POST request with the username provided in the request header.
 * It initiates the process of sending an OTP to the specified user for authentication purposes.
 * @param username
 * @return
 */
	@PostMapping("/sendOTP")
	public ResponseEntity<?> sendOTP(@RequestHeader("username") String username) {
		return loginService.sendOTP(username);
	}
/**
 * RESTful endpoint for updating user password.
 * This endpoint receives a PUT request with a JSON body containing the new password details
 * and the username provided in the request header. It updates the user's password with the new one.
 * @param passwordUpdateRequest
 * @param username
 * @return
 */
	@PutMapping("/updatePassword")
	public ResponseEntity<?> updatePassword(@RequestBody PasswordUpdateRequest passwordUpdateRequest,
			@RequestHeader("username") String username) {
		return loginService.updatePassword(passwordUpdateRequest, username);
	}
/**
 * RESTful endpoint for updating user profile information.
 * This endpoint receives a PUT request with a JSON body containing the updated user profile details
 * and the username provided in the request header. It updates the user's profile information.
 * @param username
 * @param profileUpdateRequest
 * @return
 */
	@PutMapping("/updateProfile")
	public ResponseEntity<?> updateUserProfile(@RequestHeader("username") String username,
			@RequestBody ProfileUpdateRequest profileUpdateRequest) {
		return loginService.updateUserProfile(username, profileUpdateRequest);

	}
}
