package com.hti.smpp.common.controllers;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.email.EmailSender;
import com.hti.smpp.common.exception.AuthenticationExceptionFailed;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.InvalidOtpException;
import com.hti.smpp.common.exception.InvalidPasswordException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.login.dto.ERole;
import com.hti.smpp.common.login.dto.Role;
import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.RoleRepository;
import com.hti.smpp.common.login.repository.UserRepository;
import com.hti.smpp.common.payload.request.LoginRequest;
import com.hti.smpp.common.payload.request.PasswordUpdateRequest;
import com.hti.smpp.common.payload.request.SignupRequest;
import com.hti.smpp.common.payload.response.JwtResponse;
import com.hti.smpp.common.payload.response.MessageResponse;
import com.hti.smpp.common.payload.response.ProfileResponse;
import com.hti.smpp.common.security.jwt.JwtUtils;
import com.hti.smpp.common.security.services.UserDetailsImpl;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.BalanceEntryRepository;
import com.hti.smpp.common.user.repository.DlrSettingEntryRepository;
import com.hti.smpp.common.user.repository.ProfessionEntryRepository;
import com.hti.smpp.common.user.repository.RechargeEntryRepository;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.user.repository.WebMenuAccessEntryRepository;
import com.hti.smpp.common.util.Constant;
import com.hti.smpp.common.util.EmailValidator;
import com.hti.smpp.common.util.OTPGenerator;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
@OpenAPIDefinition(info = @Info(title = "SMPP login  API..", version = "1.0", description = "API for managing SMPP login..."))
public class AuthController {
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder encoder;

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private EmailSender emailSender;

	@Autowired
	private UserEntryRepository userEntryRepository;

	@Autowired
	private WebMasterEntryRepository webMasterEntryRepository;

	@Autowired
	private DlrSettingEntryRepository dlrSettingEntryRepository;

	@Autowired
	private ProfessionEntryRepository professionEntryRepository;

	@Autowired
	private BalanceEntryRepository balanceEntryRepository;

	@Autowired
	private RechargeEntryRepository rechargeEntryRepository;

	@Autowired
	private WebMenuAccessEntryRepository webMenuAccessEntryRepository;

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
		this.authenticationManager = authenticationManager;
		this.jwtUtils = jwtUtils;
	}

	@Operation(summary = "Authenticate user", description = "Endpoint to authenticate a user.")
	@ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class)))
	@ApiResponse(responseCode = "500", description = "Internal server error")
	@PostMapping("/login")
	public ResponseEntity<JwtResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {
		try {
			log.info("Attempting to authenticate user: {}", loginRequest.getUsername());

			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

			SecurityContextHolder.getContext().setAuthentication(authentication);

			String jwt = jwtUtils.generateJwtToken(authentication);

			UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
			List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
					.collect(Collectors.toList());

			log.info("Authentication successful for user: {}", userDetails.getUsername());

			JwtResponse jwtResponse = new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(),
					userDetails.getEmail(), roles);

			return ResponseEntity.ok(jwtResponse);

		} catch (AuthenticationException e) {
			log.error("Authentication failed for user: {}", loginRequest.getUsername(), e);
			throw new AuthenticationExceptionFailed("Authentication failed" + e);

		} catch (Exception e) {
			log.error("Internal server error during authentication", e);
			throw new InternalServerException("Internal server error" + e);
		}
	}

	@Operation(summary = "Register a new user")
	@ApiResponse(responseCode = "200", description = "Successfully registered user")
	@ApiResponse(responseCode = "500", description = "Internal server error")
	@PostMapping("/signup")
	@Transactional
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		try {
			if (userRepository.existsBySystemId(signUpRequest.getUsername())) {
				return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
			}

			if (userRepository.existsByEmail(signUpRequest.getEmail())) {
				return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
			}
			if (!EmailValidator.isEmailValid(signUpRequest.getEmail())) {
				return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is not valid."));
			}
			// Create new user's account
			User user = new User();
			user.setEmail(signUpRequest.getEmail());
			user.setPassword(encoder.encode(signUpRequest.getPassword()));
			user.setSystemId(signUpRequest.getUsername());
			user.setBase64Password(signUpRequest.getPassword());
			user.setFirstName(signUpRequest.getFirstName());
			user.setLanguage(signUpRequest.getLanguage());
			user.setLastName(signUpRequest.getLastName());
			user.setCountry(signUpRequest.getCountry());
			Set<String> strRoles = signUpRequest.getRole();
			Set<Role> roles = new HashSet<>();

			if (strRoles == null) {
				Role userRole = roleRepository.findByName(ERole.ROLE_USER)
						.orElseThrow(() -> new NotFoundException("Error: Role is not found."));
				roles.add(userRole);
			} else {
				strRoles.forEach(role -> {
					switch (role) {
					case "admin":
						Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
								.orElseThrow(() -> new NotFoundException("Error: Role is not found."));
						roles.add(adminRole);

						break;

					case "superadmin":
						Role superAdminRole = roleRepository.findByName(ERole.ROLE_SUPERADMIN)
								.orElseThrow(() -> new NotFoundException("Error: Role is not found."));
						roles.add(superAdminRole);

						break;
					case "system":
						Role systemRole = roleRepository.findByName(ERole.ROLE_SYSTEM)
								.orElseThrow(() -> new NotFoundException("Error: Role is not found."));
						roles.add(systemRole);

					case "user":
						Role userRole = roleRepository.findByName(ERole.ROLE_USER)
								.orElseThrow(() -> new NotFoundException("Error: Role is not found."));
						roles.add(userRole);
					}
				});
			}

			user.setRoles(roles);
			User save = userRepository.save(user);
			userEntryRepository.save(ConvertRequert(signUpRequest));
			signUpRequest.getWebMasterEntry().setUserId(save.getUserId().intValue());
			signUpRequest.getDlrSettingEntry().setUserId(save.getUserId().intValue());
			signUpRequest.getProfessionEntry().setUserId(save.getUserId().intValue());
			signUpRequest.getBalance().setUserId(save.getUserId().intValue());
			signUpRequest.getBalance().setSystemId(save.getSystemId());
			signUpRequest.getRechargeEntry().setUserId(save.getUserId().intValue());
			signUpRequest.getRechargeEntry().setSystemId(save.getSystemId());
			signUpRequest.getWebMenuAccessEntry().setUserId(save.getUserId().intValue());

			webMasterEntryRepository.save(signUpRequest.getWebMasterEntry());
			rechargeEntryRepository.save(signUpRequest.getRechargeEntry());
			webMasterEntryRepository.save(signUpRequest.getWebMasterEntry());
			dlrSettingEntryRepository.save(signUpRequest.getDlrSettingEntry());
			professionEntryRepository.save(signUpRequest.getProfessionEntry());
			balanceEntryRepository.save(signUpRequest.getBalance());
			// return entry.getId();
			return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
		} catch (Exception e) {
			throw new InternalServerException("Internal server error: " + e.getMessage());
		}
	}

	public UserEntry ConvertRequert(SignupRequest signUpRequest) {
		UserEntry entry = new UserEntry();
		entry.setAccessCountry(String.join(",", signUpRequest.getAccessCountries()));
		entry.setAccessIp(signUpRequest.getAccessIp());
		entry.setAdminDepend(signUpRequest.isAdminDepend());
		entry.setAlertEmail(signUpRequest.getAlertEmail());
		entry.setAlertNumber(signUpRequest.getAlertNumber());
		entry.setAlertUrl(signUpRequest.getAlertUrl());
		entry.setAlertWaitDuration(signUpRequest.getAlertWaitDuration());
		entry.setBindAlert(signUpRequest.isBindAlert());
		entry.setCreatedBy(signUpRequest.getAccessIp());
		entry.setCreatedOn(signUpRequest.getCreatedOn());
		entry.setCurrency(signUpRequest.getCurrency());
		entry.setDltDefaultSender(signUpRequest.getDltDefaultSender());
		entry.setEditBy(signUpRequest.getSystemType());
		entry.setEditOn(signUpRequest.getCreatedOn());
		entry.setExpiry("" + signUpRequest.getExpiry());
		entry.setFixLongSms(signUpRequest.isFixLongSms());
		entry.setFlagStatus(signUpRequest.getFlagValue());
		entry.setForceDelay(signUpRequest.getForceDelay());
		entry.setForcePasswordChange(signUpRequest.isForcePasswordChange());
		entry.setHlr(signUpRequest.isHlr());
		entry.setLogDays(signUpRequest.getLogDays());
		entry.setLogging(signUpRequest.isLogging());
		entry.setLoopSmscId(signUpRequest.getLoopSmscId());
		entry.setMasterId(signUpRequest.getMasterId());
		entry.setPassword(signUpRequest.getPassword());
		entry.setPasswordExpiresOn(signUpRequest.getPasswordExpiresOn());
		entry.setPriority(signUpRequest.getPriority());
		entry.setRecordMnp(signUpRequest.isRecordMnp());
		entry.setRemark(signUpRequest.getRemark());
		entry.setSenderLength(signUpRequest.getSenderLength());
		entry.setSenderTrim(signUpRequest.isSenderTrim());
		entry.setSleep(signUpRequest.getSleep());
		entry.setSystemId(signUpRequest.getUsername());
		entry.setSystemType(signUpRequest.getSystemType());
		entry.setTimeout(signUpRequest.getTimeout());

		return entry;
	}

	@Operation(summary = "Validate OTP")
	@ApiResponse(responseCode = "200", description = "OTP validation successful. Please proceed")
	@ApiResponse(responseCode = "400", description = "Error: OTP has expired. Please request a new OTP")
	@ApiResponse(responseCode = "401", description = "Error: Invalid OTP. Please enter the correct OTP")
	@ApiResponse(responseCode = "404", description = "Error: User not found. Please check the username and try again")
	@PostMapping("/otp/validate")
	public ResponseEntity<String> validateOtp(@RequestParam String username, @RequestParam String otp) {
		System.out.println("username..........." + username);
		System.out.println("OTP ................" + otp);

		Optional<User> optionalUser = userRepository.findBySystemId(username);

		if (optionalUser.isPresent()) {
			User user = optionalUser.get();

			if (user.getOtpSecretKey().equals(otp)) {
				if (user.getOtpSendTime().isAfter(LocalTime.now().minusMinutes(2))) {
					// OTP validation successful
					return ResponseEntity.ok("OTP validation successful. Please proceed...........");
				} else {
					// OTP has expired
					throw new InvalidOtpException("Error: OTP has expired. Please request a new OTP.");
				}
			} else {
				// Invalid OTP
				throw new InvalidOtpException("Error: Invalid OTP. Please enter the correct OTP.");
			}
		} else {
			// User not found
			throw new NotFoundException("Error: User not found. Please check the username and try again.");
		}
	}

	@Operation(summary = "Get user profile")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved user profile")
	@ApiResponse(responseCode = "404", description = "Error: User not found")
	@GetMapping("/profile")
	public ResponseEntity<ProfileResponse> getUserProfile(@RequestHeader("username") String username) {
		Optional<User> userOptional = userRepository.findBySystemId(username);
		Optional<BalanceEntry> balanceOptional = balanceEntryRepository.findBySystemId(username);

		if (userOptional.isPresent() && balanceOptional.isPresent()) {
			User user = userOptional.get();
			BalanceEntry balanceEntry = balanceOptional.get();

			ProfileResponse profileResponse = new ProfileResponse();
			profileResponse.setUserName(user.getSystemId());
			profileResponse.setBalance(String.valueOf(balanceEntry.getWalletAmount()));
			profileResponse.setBase64Password(user.getBase64Password());
			profileResponse.setCountry(user.getCountry());
			profileResponse.setEmail(user.getEmail());
			profileResponse.setFirstName(user.getFirstName());
			profileResponse.setLanguage(user.getLanguage());
			profileResponse.setLastName(user.getLastName());
			profileResponse.setRoles(user.getRoles());

			return ResponseEntity.ok(profileResponse);
		} else {
			throw new NotFoundException("Error: User not found!");
		}
	}

	@Operation(summary = "Reset user password")
	@ApiResponse(responseCode = "200", description = "Password reset successfully")
	@ApiResponse(responseCode = "404", description = "Error: User not found")
	@PutMapping("/password/forgot")
	public ResponseEntity<?> forgotPassword(@RequestParam String newPassword, @RequestParam String username) {
		System.out.println("username..........." + username);
		System.out.println("newPassword ................" + newPassword);
		Optional<User> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isEmpty()) {
			throw new NotFoundException("Error: User Not Found!");
		}

		// Update User Password
		User user = userOptional.get();
		user.setPassword(encoder.encode(newPassword));
		userRepository.save(user);
		if (EmailValidator.isEmailValid(user.getEmail())) {
			emailSender.sendEmail(user.getEmail(), Constant.PASSWORD_FORGOT_SUBJECT, Constant.TEMPLATE_PATH,
					emailSender.createSourceMap(Constant.MESSAGE_FOR_FORGOT_PASSWORD,
							"username:- " + user.getSystemId() + "  password:- " + newPassword));
		}

		return ResponseEntity.ok(new MessageResponse("Password Reset Successfully!"));
	}

	@Operation(summary = "Send OTP to user email")
	@ApiResponse(responseCode = "200", description = "OTP sent successfully")
	@ApiResponse(responseCode = "404", description = "Error: User not found")
	@ApiResponse(responseCode = "500", description = "Internal server error")
	@PostMapping("/send/otp")
	public ResponseEntity<?> sendOTP(@RequestParam String username) {
		try {
			Optional<User> userOptional = userRepository.findBySystemId(username);

			if (userOptional.isPresent()) {
				// Generate OTP
				String generateOTP = OTPGenerator.generateOTP(6);

				// Set OTP Secret Key for User
				User user = userOptional.get();
				user.setOtpSecretKey(generateOTP);
				user.setOtpSendTime(LocalTime.now());

				// Send Email with OTP
				emailSender.sendEmail(user.getEmail(), Constant.OTP_SUBJECT, Constant.TEMPLATE_PATH,
						emailSender.createSourceMap(Constant.MESSAGE_FOR_OTP, generateOTP));

				// Save User with Updated OTP
				userRepository.save(user);

				return ResponseEntity.ok(new MessageResponse("OTP Sent Successfully!"));
			} else {
				throw new NotFoundException("Error: User Not Found!");
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			// Handle exceptions, log or return appropriate error response
			throw new InternalServerException("Error sending OTP: " + e.getMessage());
		}
	}

	@Operation(summary = "Update user password")
	@ApiResponse(responseCode = "200", description = "Password updated successfully")
	@ApiResponse(responseCode = "400", description = "Error: Invalid old password")
	@ApiResponse(responseCode = "404", description = "Error: User not found")
	@PutMapping("/update")
	public ResponseEntity<?> updatePassword(@Valid @RequestBody PasswordUpdateRequest passwordUpdateRequest) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentUsername = authentication.getName();
		Optional<User> optionalUser = userRepository.findBySystemId(currentUsername);

		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			String currentPassword = user.getPassword();

			if (encoder.matches(passwordUpdateRequest.getOldPassword(), currentPassword)) {
				// Valid old password, update the password
				user.setPassword(encoder.encode(passwordUpdateRequest.getNewPassword()));
				userRepository.save(user);
				if (EmailValidator.isEmailValid(user.getEmail())) {
					emailSender.sendEmail(user.getEmail(), Constant.PASSWORD_UPDATE_SUBJECT, Constant.TEMPLATE_PATH,
							emailSender.createSourceMap(Constant.MESSAGE_FOR_PASSWORD_UPDATE, "username:- "
									+ user.getSystemId() + "  password:- " + passwordUpdateRequest.getNewPassword()));
				}
				return ResponseEntity.ok(new MessageResponse("Password Updated Successfully!"));
			} else {
				throw new InvalidPasswordException("Error: Invalid old password");
			}
		} else {
			throw new NotFoundException("Error: User Not Found!");
		}
	}

}
