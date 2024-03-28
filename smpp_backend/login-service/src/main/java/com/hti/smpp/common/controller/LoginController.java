package com.hti.smpp.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.exception.ExceptionResponse;
import com.hti.smpp.common.request.LoginRequest;
import com.hti.smpp.common.request.PasswordUpdateRequest;
import com.hti.smpp.common.request.SignupRequest;
import com.hti.smpp.common.response.JwtResponse;
import com.hti.smpp.common.response.LoginResponse;
import com.hti.smpp.common.response.ProfileResponse;
import com.hti.smpp.common.service.LoginService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/login")
@OpenAPIDefinition(info = @Info(title = "SMPP Login API", version = "1.0", description = "API for managing SMPP Login"))
@Tag(name = "Login Controller", description = "APIs related to user authentication and profile management")
public class LoginController {

	@Autowired
	private LoginService loginService;

	/**
	 * RESTful endpoint for user authentication. This endpoint receives a POST
	 * request with user credentials, authenticates the user, and generates a JWT
	 * token for successful authentication.
	 * 
	 * @param loginRequest
	 * @return
	 */
	@PostMapping("/jwt")
	@Operation(summary = "Authenticate User", description = "Endpoint to authenticate a user and generate a JWT token.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User authenticated successfully. JWT token generated.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid or malformed request. Unable to authenticate user.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error during authentication process.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> authenticateUser(@RequestBody @Valid LoginRequest loginRequest,
			HttpServletRequest request) {
		System.out.println("authenticate Username: " + loginRequest.getUsername());
		return loginService.login(loginRequest, request);
	}

	/**
	 * RESTful endpoint for registering a new user. This endpoint receives a POST
	 * request with user registration details, validates the input, and registers a
	 * new user using the provided information.
	 * 
	 * @param signUpRequest
	 * @return
	 */
	@PostMapping("/register")
	@Operation(summary = "Register User", description = "Endpoint to register a new user. This API allows users to sign up by providing necessary registration information.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "User registered successfully. A confirmation may be required.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid or malformed request. Registration failed.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error during the registration process.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> registerUser(@RequestBody @Valid SignupRequest signUpRequest) {
		return loginService.registerUser(signUpRequest);
	}

	/**
	 * RESTful endpoint for retrieving user profile data. This endpoint receives a
	 * GET request with the username provided in the request header. It retrieves
	 * and returns the profile data associated with the specified username.
	 * 
	 * @param username
	 * @return
	 */
	@GetMapping("/user/data")
	@Operation(summary = "Get User Profile", description = "Endpoint to retrieve user profile data.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User profile data retrieved successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid or malformed request. Unable to retrieve user profile data.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "404", description = "User not found. Profile data not available.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error during the profile data retrieval process.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> getUserProfile(@RequestHeader(value = "username", required = true) String username) {
		return loginService.profile(username);
	}

	/**
	 * RESTful endpoint for checking the status of the login service. This endpoint
	 * receives a POST request with the username provided in the request header. It
	 * returns a simple message indicating that the login service is running.
	 * 
	 * @param username
	 * @return
	 */
	@GetMapping("/status")
	@Operation(summary = "Get Service Status", description = "Endpoint to check the status of the login service.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Service status retrieved successfully.", content = @Content(mediaType = "text/plain")),
			@ApiResponse(responseCode = "400", description = "Invalid or malformed request. Unable to retrieve service status.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error during the service status retrieval process.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> getStatus(@RequestHeader(value = "username", required = true) String username) {
		return new ResponseEntity<>("Login service running.", HttpStatus.OK);
	}

	/**
	 * RESTful endpoint for validating an OTP (One-Time Password). This endpoint
	 * receives a POST request with the username provided in the request header and
	 * an OTP parameter. It validates the provided OTP for the specified username.
	 * 
	 * @param username
	 * @param otp
	 * @return
	 */
	@PostMapping("/validateOtp")
	@Operation(summary = "Validate OTP", description = "Endpoint to validate the one-time password (OTP) for a user.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OTP validated successfully. User authenticated.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid or malformed request. Unable to validate OTP.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized. OTP validation failed.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error during OTP validation process.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> validateOtp(@RequestHeader(value = "username", required = true) String username,
			@RequestParam(value = "otp", required = true) String otp) {
		return loginService.validateOtp(username, otp);
	}

	/**
	 * RESTful endpoint for handling forgot password requests. This endpoint
	 * receives a PUT request with the new password as a request parameter and the
	 * username provided in the request header. It processes the forgot password
	 * request by updating the user's password with the new one.
	 * 
	 * @param newPassword
	 * @param username
	 * @return
	 */
	@PutMapping("/forgotPassword")
	@Operation(summary = "Forgot Password", description = "Endpoint to reset the password for a user who has forgotten their password.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Password reset successfully. No content returned."),
			@ApiResponse(responseCode = "400", description = "Invalid or malformed request. Unable to reset password.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized. Password reset failed.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error during password reset process.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> forgotPassword(@RequestParam(value = "newPassword", required = true) String newPassword,
			@RequestHeader(value = "username", required = true) String username) {
		return loginService.forgotPassword(newPassword, username);
	}

	/**
	 * RESTful endpoint for sending an OTP (One-Time Password). This endpoint
	 * receives a POST request with the username provided in the request header. It
	 * initiates the process of sending an OTP to the specified user for
	 * authentication purposes.
	 * 
	 * @param username
	 * @return
	 */
	@PostMapping("/sendOTP")
	@Operation(summary = "Send One-Time Password (OTP)", description = "Endpoint to send a one-time password (OTP) to a user for authentication or verification.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OTP sent successfully. Check your registered email or phone for the OTP.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid or malformed request. Unable to send OTP.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error during OTP sending process.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> sendOTP(@RequestHeader(value = "username", required = true) String username) {
		return loginService.sendOTP(username);
	}

	/**
	 * RESTful endpoint for updating user password. This endpoint receives a PUT
	 * request with a JSON body containing the new password details and the username
	 * provided in the request header. It updates the user's password with the new
	 * one.
	 * 
	 * @param passwordUpdateRequest
	 * @param username
	 * @return
	 */
	@PutMapping("/updatePassword")
	@Operation(summary = "Update Password", description = "Endpoint to update the password for a user.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Password updated successfully. No content returned."),
			@ApiResponse(responseCode = "400", description = "Invalid or malformed request. Unable to update password.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized. Password update failed.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error during password update process.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> updatePassword(@RequestBody @Valid PasswordUpdateRequest passwordUpdateRequest,
			@RequestHeader(value = "username", required = true) String username) {
		return loginService.updatePassword(passwordUpdateRequest, username);
	}

	/**
	 * RESTful endpoint for updating user profile information. This endpoint
	 * receives a PUT request with a JSON body containing the updated user profile
	 * details and the username provided in the request header. It updates the
	 * user's profile information.
	 * 
	 * @param username
	 * @param profileUpdateRequest
	 * @return
	 */
	@PutMapping(value = "/updateProfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Update User Profile", description = "Endpoint to update the profile information for a user.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "User profile updated successfully. No content returned."),
			@ApiResponse(responseCode = "400", description = "Invalid or malformed request. Unable to update user profile.", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized. User profile update failed.", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error during user profile update process.", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> updateUserProfile(@RequestHeader(value = "username", required = true) String username,
			@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "firstName", required = false) String firstName,
			@RequestParam(value = "lastName", required = false) String lastName,
			@RequestParam(value = "contact", required = false) String contact,
			@RequestParam(value = "companyName", required = false) String companyName,
			@RequestParam(value = "designation", required = false) String designation,
			@RequestParam(value = "city", required = false) String city,
			@RequestParam(value = "country", required = false) String country,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "keepLogs", required = false) String keepLogs,
			@RequestParam(value = "referenceID", required = false) String referenceID,
			@RequestParam(value = "companyAddress", required = false) String companyAddress,
			@RequestParam(value = "companyEmail", required = false) String companyEmail,
			@RequestParam(value = "notes", required = false) String notes,
			@RequestParam(value = "taxID", required = false) String taxID,
			@RequestParam(value = "regID", required = false) String regID,
			@RequestParam(value = "image", required = false) MultipartFile image,
			@RequestParam(value = "alertEmail", required = false) String alertEmail,
			@RequestParam(value = "alertMobile", required = false) String alertMobile,
			@RequestParam(value = "invoiceEmail", required = false) String invoiceEmail,
			@RequestParam(value = "dlrReport", required = false) Boolean dlrReport,
			@RequestParam(value = "dlrEmail", required = false) String dlrEmail,
			@RequestParam(value = "coverageEmail", required = false) String coverageEmail,
			@RequestParam(value = "coverageReport", required = false) String coverageReport,
			@RequestParam(value = "lowAmount", required = false) Double lowAmount,
			@RequestParam(value = "smsAlert", required = false) Boolean smsAlert,
			@RequestParam(value = "webUrl", required = false) String webUrl,
			@RequestParam(value = "dlrThroughWeb", required = false) Boolean dlrThroughWeb,
			@RequestParam(value = "mis", required = false) Boolean mis,
			@RequestParam(value = "lowBalanceAlert", required = false) Boolean lowBalanceAlert) {
		return loginService.updateUserProfile(username, email, firstName, lastName, contact, companyName, designation,
				city, country, state, keepLogs, referenceID, companyAddress, companyEmail, notes, taxID, regID, image,
				alertEmail, alertMobile, invoiceEmail, dlrReport, dlrEmail, coverageEmail, coverageReport, lowAmount,
				smsAlert, webUrl, dlrThroughWeb, mis, lowBalanceAlert);
	}

	@PostMapping("/validate/user-ip")
	@Operation(summary = "Validate User Ip", description = "Endpoint to validate user ip address and send otp in sms and email if user is enabled to get otp else the user will be notified by email alert when the user logged in.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User ip authenticated successfully. JWT token generated.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid or malformed request. Unable to authenticate user.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error during authentication process.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> validateIpAccess(@Valid @RequestBody LoginRequest loginRequest,
			@RequestParam(value = "language") String language) {
		return this.loginService.validateUserIpAccess(loginRequest, language);
	}

	@PostMapping("/validate/otp-user-ip")
	@Operation(summary = "Validate OTP User Ip", description = "Endpoint to validate user ip address and send otp in sms and email if user is enabled to get otp else the user will be notified by email alert when the user logged in.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OTP authenticated successfully. JWT token generated.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
			@ApiResponse(responseCode = "504", description = "Invalid or malformed request. Unable to authenticate user.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error during authentication process.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> userIpOtpValidate(@Valid @RequestBody LoginRequest loginRequest,
			@RequestParam(value = "OTP") int otp) {
		return this.loginService.userIpOtpValidate(loginRequest, otp);
	}

	@PostMapping("/recent-activity")
	@Operation(summary = "Recent Activity", description = "Endpoint to check Recent Activity.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Recent Activites Fetched Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
			@ApiResponse(responseCode = "504", description = "Invalid or malformed request. Unable to authenticate user.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error during authentication process.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))) })
	public ResponseEntity<?> recentActivity(@RequestHeader(value = "username", required = true) String username) {
		return this.loginService.userRecentActivity(username);
	}

}
