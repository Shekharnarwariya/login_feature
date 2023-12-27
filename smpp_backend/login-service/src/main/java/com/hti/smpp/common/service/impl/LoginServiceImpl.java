package com.hti.smpp.common.service.impl;

import java.sql.Types;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.email.EmailSender;
import com.hti.smpp.common.exception.AuthenticationExceptionFailed;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.InvalidOtpException;
import com.hti.smpp.common.exception.InvalidPasswordException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.jwt.JwtUtils;
import com.hti.smpp.common.request.LoginRequest;
import com.hti.smpp.common.request.PasswordUpdateRequest;
import com.hti.smpp.common.request.ProfileUpdateRequest;
import com.hti.smpp.common.request.SignupRequest;
import com.hti.smpp.common.response.JwtResponse;
import com.hti.smpp.common.response.ProfileResponse;
import com.hti.smpp.common.service.LoginService;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.DriverInfo;
import com.hti.smpp.common.user.dto.OTPEntry;
import com.hti.smpp.common.user.dto.ProfessionEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.BalanceEntryRepository;
import com.hti.smpp.common.user.repository.DlrSettingEntryRepository;
import com.hti.smpp.common.user.repository.DriverInfoRepository;
import com.hti.smpp.common.user.repository.OtpEntryRepository;
import com.hti.smpp.common.user.repository.ProfessionEntryRepository;
import com.hti.smpp.common.user.repository.RechargeEntryRepository;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.user.repository.WebMenuAccessEntryRepository;
import com.hti.smpp.common.util.Constant;
import com.hti.smpp.common.util.EmailValidator;
import com.hti.smpp.common.util.OTPGenerator;
import com.hti.smpp.common.util.PasswordConverter;

@Service
public class LoginServiceImpl implements LoginService {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private PasswordEncoder encoder;

	@Autowired
	private JwtUtils jwtUtils;

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

	@Autowired
	private OtpEntryRepository otpEntryRepository;

	@Autowired
	private DriverInfoRepository driverInfoRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private EmailSender emailSender;

	private static final Logger log = LoggerFactory.getLogger(LoginServiceImpl.class);

	public enum UserRole {
		ADMIN, SUPERADMIN, SYSTEM, USER
	}

	@Override
	public ResponseEntity<?> login(LoginRequest loginRequest) {
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

			JwtResponse jwtResponse = new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), roles);

			return ResponseEntity.ok(jwtResponse);

		} catch (AuthenticationException e) {
			log.error("Authentication failed for user: {}", loginRequest.getUsername(), e.getMessage());
			System.out.println("error authentication........");
			throw new AuthenticationExceptionFailed("Authentication failed" + e);

		} catch (Exception e) {
			log.error("Internal server error during authentication", e);
			throw new InternalServerException("Internal server error" + e);
		}
	}

	@Override
	public ResponseEntity<?> profile(String username) {
		System.out.println("get profile method call username" + username);
		Optional<BalanceEntry> balanceOptional = balanceEntryRepository.findBySystemId(username);
		Optional<UserEntry> userEntityOptional = userEntryRepository.findBySystemId(username);

		if (balanceOptional.isPresent() && userEntityOptional.isPresent()) {
			BalanceEntry balanceEntry = balanceOptional.get();
			UserEntry userEntry = userEntityOptional.get();

			// Use map to simplify getting profession entry
			ProfessionEntry professionEntry = professionEntryRepository.findById(userEntry.getId())
					.orElseThrow(() -> new NotFoundException("Error:getting error professionEntry.."));
			ProfileResponse profileResponse = new ProfileResponse();
			profileResponse.setUserName(userEntry.getSystemId());
			profileResponse.setBalance(String.valueOf(balanceEntry.getWalletAmount()));
			profileResponse.setCountry(professionEntry.getCountry());
			profileResponse.setEmail(professionEntry.getDomainEmail());
			profileResponse.setFirstName(professionEntry.getFirstName());
			profileResponse.setLastName(professionEntry.getLastName());
			profileResponse.setRoles(userEntry.getRole());
			profileResponse.setContactNo(professionEntry.getMobile());
			profileResponse.setCurrency(userEntry.getCurrency());

			return ResponseEntity.ok(profileResponse);
		} else {
			throw new NotFoundException("Error: User not found!");
		}
	}

	@Override
	public ResponseEntity<?> registerUser(SignupRequest signUpRequest) {
		try {
			if (userEntryRepository.existsBySystemId(signUpRequest.getUsername())) {
				return ResponseEntity.badRequest().body("Error: Username is already taken!");
			}

			if (professionEntryRepository.existsByDomainEmail(signUpRequest.getProfessionEntry().getDomainEmail())) {
				return ResponseEntity.badRequest().body("Error: Email is already in use!");
			}
			if (!EmailValidator.isEmailValid(signUpRequest.getProfessionEntry().getDomainEmail())) {
				return ResponseEntity.badRequest().body("Error: Email is not valid.");
			}
			PasswordConverter passwordConverter = new PasswordConverter();
			UserEntry convertRequert = ConvertRequert(signUpRequest);
			UserEntry user = userEntryRepository.save(convertRequert);
			driverInfoRepository.save(new DriverInfo(user.getId(),
					passwordConverter.convertToDatabaseColumn(signUpRequest.getPassword()), LocalDateTime.now()));
			signUpRequest.getWebMasterEntry().setUserId(user.getId());
			signUpRequest.getDlrSettingEntry().setUserId(user.getId());
			signUpRequest.getProfessionEntry().setUserId(user.getId());
			signUpRequest.getBalance().setUserId(user.getId());
			signUpRequest.getBalance().setSystemId(user.getSystemId());
			signUpRequest.getRechargeEntry().setUserId(user.getId());
			signUpRequest.getRechargeEntry().setSystemId(user.getSystemId());
			signUpRequest.getWebMenuAccessEntry().setUserId(user.getId());

			webMenuAccessEntryRepository.save(signUpRequest.getWebMenuAccessEntry());
			rechargeEntryRepository.save(signUpRequest.getRechargeEntry());
			webMasterEntryRepository.save(signUpRequest.getWebMasterEntry());
			dlrSettingEntryRepository.save(signUpRequest.getDlrSettingEntry());
			professionEntryRepository.save(signUpRequest.getProfessionEntry());
			balanceEntryRepository.save(signUpRequest.getBalance());

			return ResponseEntity.ok("User registered successfully!");
		} catch (Exception e) {
			throw new InternalServerException("Internal server error: " + e.getMessage());
		}
	}

	public UserEntry ConvertRequert(SignupRequest signUpRequest) {
		UserEntry entry = new UserEntry();
		String strRoles = signUpRequest.getRole().toUpperCase();
		try {
			UserRole userRole = UserRole.valueOf(strRoles);
			entry.setRole(userRole.name());
		} catch (IllegalArgumentException e) {
			throw new NotFoundException("Error: Role is not found. " + strRoles);
		}
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
		entry.setEditBy(signUpRequest.getUsername());
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
		entry.setPassword(encoder.encode(signUpRequest.getPassword()));
		return entry;
	}

	@Override
	public ResponseEntity<?> validateOtp(String username, String otp) {
		Optional<OTPEntry> optionalOtp = otpEntryRepository.findBySystemId(username);
		if (optionalOtp.isPresent()) {
			OTPEntry otpEntry = optionalOtp.get();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
			LocalTime localTime = LocalTime.parse(otpEntry.getExpiresOn().subSequence(0, 8), formatter);
			if (String.valueOf(otpEntry.getOneTimePass()).equals(otp)) {
				if (localTime.isAfter(LocalTime.now().minusMinutes(2))) {
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

	@Override
	public ResponseEntity<?> forgotPassword(String newPassword, String username) {
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		if (userOptional.isEmpty()) {
			throw new NotFoundException("Error: User Not Found!");
		}
		// Update User Password
		UserEntry user = userOptional.get();
		ProfessionEntry professionEntry = professionEntryRepository.findById(user.getId())
				.orElseThrow(() -> new NotFoundException("Error:getting error professionEntry.."));

		String updateQuery = "UPDATE usermaster SET password = ?, editOn = CURRENT_TIMESTAMP, editby = ? WHERE system_id = ?";
		jdbcTemplate.update(updateQuery, new Object[] { encoder.encode(newPassword), username, username },
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR });

		driverInfoRepository.save(new DriverInfo(user.getId(),
				new PasswordConverter().convertToDatabaseColumn(newPassword), LocalDateTime.now()));
		if (EmailValidator.isEmailValid(professionEntry.getDomainEmail())) {
			emailSender.sendEmail(professionEntry.getDomainEmail(), Constant.PASSWORD_FORGOT_SUBJECT,
					Constant.TEMPLATE_PATH,
					emailSender.createSourceMap(Constant.MESSAGE_FOR_FORGOT_PASSWORD,
							professionEntry.getFirstName() + " " + professionEntry.getLastName(),
							Constant.FORGOT_FLAG_SUBJECT));
		}

		return ResponseEntity.ok("Password Reset Successfully!");
	}

	@Override
	public ResponseEntity<?> sendOTP(String username) {
		try {
			Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);

			if (userOptional.isPresent()) {
				// Generate OTP
				String generateOTP = OTPGenerator.generateOTP(6);

				// Set OTP Secret Key for User
				UserEntry user = userOptional.get();
				Optional<OTPEntry> optionalOTP = otpEntryRepository.findBySystemId(username);
				if (!optionalOTP.isPresent()) {
					OTPEntry OTP = new OTPEntry();
					OTP.setOneTimePass(Integer.parseInt(generateOTP));
					OTP.setExpiresOn(LocalTime.now() + "");
					OTP.setSystemId(username);
					otpEntryRepository.save(OTP);
				} else {

					OTPEntry otpEntry = optionalOTP.get();
					otpEntry.setOneTimePass(Integer.parseInt(generateOTP));
					otpEntry.setExpiresOn(LocalTime.now() + "");
					otpEntry.setSystemId(username);
					otpEntryRepository.save(otpEntry);
				}

				ProfessionEntry professionEntry = professionEntryRepository.findById(user.getId())
						.orElseThrow(() -> new NotFoundException("Error:getting error professionEntry.."));
				// Send Email with OTP
				emailSender.sendEmail(professionEntry.getDomainEmail(), Constant.OTP_SUBJECT, Constant.TEMPLATE_PATH,
						emailSender.createSourceMap(Constant.MESSAGE_FOR_OTP, generateOTP,
								Constant.SECOND_MESSAGE_FOR_OTP, Constant.OTP_FLAG_SUBJECT,
								professionEntry.getFirstName() + " " + professionEntry.getLastName()));
				return ResponseEntity.ok("OTP Sent Successfully!");
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

	@Override
	public ResponseEntity<?> updatePassword(PasswordUpdateRequest passwordUpdateRequest, String username) {
		Optional<UserEntry> optionalUser = userEntryRepository.findBySystemId(username);

		if (optionalUser.isPresent()) {
			UserEntry userEntry = optionalUser.get();
			String currentPassword = userEntry.getPassword();

			if (encoder.matches(passwordUpdateRequest.getOldPassword(), currentPassword)) {
				// Valid old password, update the password
				String updateQuery = "UPDATE usermaster SET password = ?, editOn = CURRENT_TIMESTAMP, editby = ? WHERE system_id = ?";
				jdbcTemplate.update(updateQuery,
						new Object[] { encoder.encode(passwordUpdateRequest.getNewPassword()), username, username },
						new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR });

				driverInfoRepository.save(new DriverInfo(userEntry.getId(),
						new PasswordConverter().convertToDatabaseColumn(passwordUpdateRequest.getNewPassword()),
						LocalDateTime.now()));
				ProfessionEntry professionEntry = professionEntryRepository.findById(userEntry.getId())
						.orElseThrow(() -> new NotFoundException("Error:getting error professionEntry.."));
				if (EmailValidator.isEmailValid(professionEntry.getDomainEmail())) {
					emailSender.sendEmail(professionEntry.getDomainEmail(), Constant.PASSWORD_UPDATE_SUBJECT,
							Constant.TEMPLATE_PATH,
							emailSender.createSourceMap(Constant.MESSAGE_FOR_PASSWORD_UPDATE,
									professionEntry.getFirstName() + " " + professionEntry.getLastName(),
									Constant.UPDATE_FLAG_SUBJECT));
				}
				return ResponseEntity.ok("Password Updated Successfully!");
			} else {
				throw new InvalidPasswordException("Error: Invalid old password");
			}
		} else {
			throw new NotFoundException("Error: User Not Found!");
		}
	}

	@Override
	public ResponseEntity<?> updateUserProfile(String username, ProfileUpdateRequest profileUpdateRequest) {
		Optional<UserEntry> optionalUser = userEntryRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			UserEntry user = optionalUser.get();
			ProfessionEntry professionEntry = professionEntryRepository.findById(user.getId())
					.orElseThrow(() -> new NotFoundException("Error:getting error professionEntry.."));
			updateUserData(user, profileUpdateRequest, professionEntry);
			user.setEditOn(LocalDateTime.now() + "");
			user.setEditBy(username);
			userEntryRepository.save(user);
			professionEntryRepository.save(professionEntry);
			return ResponseEntity.ok("Profile updated successfully");
		} else {
			throw new NotFoundException("User not found!");
		}
	}

	private void updateUserData(UserEntry user, ProfileUpdateRequest profileUpdateRequest,
			ProfessionEntry professionEntry) {
		// Use null checks to update only non-null fields
		if (profileUpdateRequest.getEmail() != null) {
			professionEntry.setDomainEmail(profileUpdateRequest.getEmail());
		}
		if (profileUpdateRequest.getFirstName() != null) {
			professionEntry.setFirstName(profileUpdateRequest.getFirstName());
		}
		if (profileUpdateRequest.getLastName() != null) {
			professionEntry.setLastName(profileUpdateRequest.getLastName());
		}
		if (profileUpdateRequest.getContact() != null) {
			professionEntry.setMobile(profileUpdateRequest.getContact());
		}
	}

}
