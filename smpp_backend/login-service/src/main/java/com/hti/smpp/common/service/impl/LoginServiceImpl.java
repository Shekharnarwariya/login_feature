package com.hti.smpp.common.service.impl;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.hti.smpp.common.email.EmailSender;
import com.hti.smpp.common.exception.AuthenticationExceptionFailed;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.InvalidOtpException;
import com.hti.smpp.common.exception.InvalidPropertyException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.jwt.JwtUtils;
import com.hti.smpp.common.messages.dto.BulkSmsDTO;
import com.hti.smpp.common.request.LoginRequest;
import com.hti.smpp.common.request.PasswordUpdateRequest;
import com.hti.smpp.common.request.ProfileUpdateRequest;
import com.hti.smpp.common.request.SignupRequest;
import com.hti.smpp.common.response.JwtResponse;
import com.hti.smpp.common.response.LoginResponse;
import com.hti.smpp.common.response.ProfileResponse;
import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.sales.repository.SalesRepository;
import com.hti.smpp.common.security.BCryptPasswordEncoder;
import com.hti.smpp.common.service.LoginService;
import com.hti.smpp.common.user.dto.AccessLogEntry;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.DriverInfo;
import com.hti.smpp.common.user.dto.MultiUserEntry;
import com.hti.smpp.common.user.dto.OTPEntry;
import com.hti.smpp.common.user.dto.ProfessionEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.UserSessionObject;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.AccessLogEntryRepository;
import com.hti.smpp.common.user.repository.BalanceEntryRepository;
import com.hti.smpp.common.user.repository.DlrSettingEntryRepository;
import com.hti.smpp.common.user.repository.DriverInfoRepository;
import com.hti.smpp.common.user.repository.MultiUserEntryRepository;
import com.hti.smpp.common.user.repository.OtpEntryRepository;
import com.hti.smpp.common.user.repository.ProfessionEntryRepository;
import com.hti.smpp.common.user.repository.RechargeEntryRepository;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.user.repository.WebMenuAccessEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.Constant;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.EmailValidator;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;
import com.hti.smpp.common.util.MultiUtility;
import com.hti.smpp.common.util.OTPGenerator;
import com.hti.smpp.common.util.PasswordConverter;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Implementation of the LoginService interface for handling user authentication
 * and authorization.
 */
@Service
public class LoginServiceImpl implements LoginService {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private BCryptPasswordEncoder encoder;

	@Autowired
	private JwtUtils jwtUtils;
	
	@Autowired
	private SalesRepository salesRepository;

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

	@Autowired
	private MessageResourceBundle messageResourceBundle;

	@Autowired
	private AccessLogEntryRepository accessLog;

	@Autowired
	private MultiUserEntryRepository multiUserRepo;

	private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

	public enum UserRole {
		ADMIN, SUPERADMIN, SYSTEM, USER
	}

	/**
	 * Authenticates a user based on the provided login credentials. If
	 * authentication is successful, generates a JWT token and returns it along with
	 * user details.
	 */
	@Override
	public ResponseEntity<?> login(LoginRequest loginRequest) {
		String username = loginRequest.getUsername();

		try {
			logger.info(messageResourceBundle.getLogMessage("log.attemptAuth"), username);

			if (!userEntryRepository.existsBySystemId(username) && !salesRepository.existsByUsername(username)) {
				logger.error(messageResourceBundle.getLogMessage("auth.failed.userNotFound"), username);
				throw new AuthenticationExceptionFailed(
						messageResourceBundle.getExMessage(ConstantMessages.AUTHENTICATION_FAILED_USERNAME));
			}

			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword()));

			SecurityContextHolder.getContext().setAuthentication(authentication);

			String jwt = jwtUtils.generateJwtToken(authentication);
			UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
			List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
					.collect(Collectors.toList());

			logger.info(messageResourceBundle.getLogMessage("auth.successful"), username);

			JwtResponse jwtResponse = new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), roles);

			return ResponseEntity.ok(jwtResponse);

		} catch (BadCredentialsException e) {
			logger.error(messageResourceBundle.getLogMessage("auth.failed.password"), username, e.getMessage());
			throw new AuthenticationExceptionFailed(
					messageResourceBundle.getExMessage(ConstantMessages.AUTHENTICATION_FAILED_PASSWORD));
		} catch (AuthenticationExceptionFailed e) {
			logger.error(messageResourceBundle.getLogMessage("auth.failed.userNotFound"), username, e.getMessage());
			throw new AuthenticationExceptionFailed(e.getMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("internal.server.error"), e.getMessage());
			throw new InternalServerException(
					messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR));
		}
	}

	/**
	 * Retrieves and returns the profile information for the specified user.
	 */
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
					.orElseThrow(() -> new NotFoundException(
							messageResourceBundle.getExMessage(ConstantMessages.PROFESSION_ENTRY_ERROR)));

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
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NOT_FOUND));
		}
	}

	/**
	 * Registers a new user based on the provided signup request.
	 */
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
			throw new InternalServerException(
					messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR));
		}
	}

	/**
	 * Converts a SignupRequest object to a UserEntry object.
	 * 
	 * @param signUpRequest
	 * @return
	 */
	public UserEntry ConvertRequert(SignupRequest signUpRequest) {
		UserEntry entry = new UserEntry();
		String strRoles = signUpRequest.getRole().toUpperCase();
		try {
			UserRole userRole = UserRole.valueOf(strRoles);
			entry.setRole(userRole.name());
		} catch (IllegalArgumentException e) {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.ROLE_NOT_FOUND_ERROR + strRoles));
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

	/**
	 * Validates a one-time passcode (OTP) for a given user.
	 */
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
					throw new InvalidOtpException(messageResourceBundle.getExMessage(ConstantMessages.OTP_EXPIRED));
				}
			} else {
				// Invalid OTP
				throw new InvalidOtpException(messageResourceBundle.getExMessage(ConstantMessages.INVALID_OTP));
			}
		} else {
			// User not found
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NOT_FOUND));
		}
	}

	/**
	 * Resets the password for a user and sends a notification email.
	 */

	@Override
	public ResponseEntity<?> forgotPassword(String newPassword, String username) {
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		if (userOptional.isEmpty()) {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NOT_FOUND));
		}
		// Update User Password
		UserEntry user = userOptional.get();
		ProfessionEntry professionEntry = professionEntryRepository.findById(user.getId())
				.orElseThrow(() -> new NotFoundException(
						messageResourceBundle.getExMessage(ConstantMessages.PROFESSION_ENTRY_ERROR)));

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

	/**
	 * Sends a One-Time Password (OTP) to the user's registered email address for
	 * authentication.
	 */
	@Override
	public ResponseEntity<?> sendOTP(String username) {
		System.out.println("send otp method called  username{}" + username);
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
						.orElseThrow(() -> new NotFoundException(
								messageResourceBundle.getExMessage(ConstantMessages.PROFESSION_ENTRY_ERROR)));
				// Send Email with OTP
				emailSender.sendEmail(professionEntry.getDomainEmail(), Constant.OTP_SUBJECT, Constant.TEMPLATE_PATH,
						emailSender.createSourceMap(Constant.MESSAGE_FOR_OTP, generateOTP,
								Constant.SECOND_MESSAGE_FOR_OTP, Constant.OTP_FLAG_SUBJECT,
								professionEntry.getFirstName() + " " + professionEntry.getLastName()));
				return ResponseEntity.ok("OTP Sent Successfully!");
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NOT_FOUND));
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			// Handle exceptions, log or return appropriate error response
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.NOT_FOUND));
		}
	}

	/**
	 * Updates the password for the user associated with the given username.
	 */
	@Override
	public ResponseEntity<?> updatePassword(PasswordUpdateRequest passwordUpdateRequest, String username) {
		System.out.println("called update password username{}" + username);
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
						.orElseThrow(() -> new NotFoundException(
								messageResourceBundle.getExMessage(ConstantMessages.PROFESSION_ENTRY_ERROR)));
				if (EmailValidator.isEmailValid(professionEntry.getDomainEmail())) {
					emailSender.sendEmail(professionEntry.getDomainEmail(), Constant.PASSWORD_UPDATE_SUBJECT,
							Constant.TEMPLATE_PATH,
							emailSender.createSourceMap(Constant.MESSAGE_FOR_PASSWORD_UPDATE,
									professionEntry.getFirstName() + " " + professionEntry.getLastName(),
									Constant.UPDATE_FLAG_SUBJECT));
				}
				return ResponseEntity.ok("Password Updated Successfully!");
			} else {
				throw new InvalidPropertyException(
						messageResourceBundle.getExMessage(ConstantMessages.INVALID_OLD_PASSWORD));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NOT_FOUND));
		}
	}

	/**
	 * Updates the user profile information for the specified username.
	 */
	@Override
	public ResponseEntity<?> updateUserProfile(String username, ProfileUpdateRequest profileUpdateRequest) {
		Optional<UserEntry> optionalUser = userEntryRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			UserEntry user = optionalUser.get();
			ProfessionEntry professionEntry = professionEntryRepository.findById(user.getId())
					.orElseThrow(() -> new NotFoundException(
							messageResourceBundle.getExMessage(ConstantMessages.PROFESSION_ENTRY_ERROR)));
			updateUserData(user, profileUpdateRequest, professionEntry);
			user.setEditOn(LocalDateTime.now() + "");
			user.setEditBy(username);
			userEntryRepository.save(user);
			professionEntryRepository.save(professionEntry);
			return ResponseEntity.ok("Profile updated successfully");
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NOT_FOUND));
		}
	}

	/**
	 * Updates the user-related data (such as email, first name, last name, and
	 * contact) based on the provided {@link ProfileUpdateRequest}. Only non-null
	 * fields in the request will be updated.
	 * 
	 * @param user
	 * @param profileUpdateRequest
	 * @param professionEntry
	 */
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

	// ------------------------------------------------------------------------------------------------

	private UserEntry getUser(String username) {

		UserEntry userEntry = null;
		Optional<UserEntry> user = this.userEntryRepository.findBySystemId(username);
		if (user.isPresent()) {
			userEntry = user.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException("Unauthorised User!");
			}

		} else {
			throw new NotFoundException("User Entry Not Found With Username: " + username);
		}
		return userEntry;
	}

	private WebMasterEntry getWebMasterEntry(int userId) {
		WebMasterEntry webEntry = null;
		webEntry = this.webMasterEntryRepository.findByUserId(userId);
		if (webEntry != null) {
			return webEntry;
		} else {
			throw new NotFoundException("Web Master Entry Not Found!");
		}
	}

	private ProfessionEntry getProfessionEntry(int userId) {
		ProfessionEntry proEntry = this.professionEntryRepository.findByUserId(userId)
				.orElseThrow(() -> new NotFoundException("No Profession Entry Found For UserId: " + userId));
		return proEntry;
	}

	private int validateUser(LoginRequest loginRequest) {
		String systemId = loginRequest.getUsername();
		String password = encoder.encode(loginRequest.getPassword());

		UserEntry userEntry = getUser(loginRequest.getUsername());
		if (userEntry.getPassword().equals(password)) {
			return userEntry.getId();
		} else {
			logger.info(systemId + " Invalid Password: " + password);
			return 0;
		}
	}

	@Override
	public ResponseEntity<?> validateUserIpAccess(LoginRequest loginRequest, String language) {
		int userId = validateUser(loginRequest);
		boolean webAccess = true;
		String ipaddress = getClientIp();
		if (userId > 0) {
			UserEntry userEntry = getUser(loginRequest.getUsername());
			WebMasterEntry webEntry = getWebMasterEntry(userId);
			if (webEntry != null) {
				if (webEntry.isWebAccess()) {

					boolean isExpired = false;
					try {
						isExpired = new SimpleDateFormat("yyyy-MM-dd").parse(userEntry.getExpiry())
								.before(new java.util.Date());
					} catch (ParseException ex) {
						logger.error(userEntry.getSystemId(), ex.fillInStackTrace());
						throw new InternalServerException("Unable to parse user entry expiry date!");
					}
					boolean isPasswordExpired = false;
					if (userEntry.isForcePasswordChange()) {
						try {
							isPasswordExpired = new SimpleDateFormat("yyyy-MM-dd")
									.parse(userEntry.getPasswordExpiresOn()).before(new java.util.Date());
						} catch (ParseException ex) {
							logger.error(userEntry.getSystemId(), ex.fillInStackTrace());
							throw new InternalServerException("Unable to parse user entry password expires on!");
						}
					}
					if (isExpired) {
						logger.info("<- User Account Expired -> " + userEntry.getSystemId());
						this.accessLog.save(new AccessLogEntry(userEntry.getSystemId(),
								new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0, "failed",
								"Account Expired"));

						throw new InternalServerException("User " + userEntry.getSystemId() + " Account Expired!");
					} else if (isPasswordExpired) {
						logger.info("<- User Password Expired -> " + userEntry.getSystemId());
						this.accessLog.save(new AccessLogEntry(userEntry.getSystemId(),
								new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0, "failed",
								"Password Expired"));
						throw new InternalServerException("User " + userEntry.getSystemId() + " Password Expired!");
					} else {
						String fileName = Constants.USER_FLAG_DIR + userEntry.getSystemId() + ".txt";
						String flagValue = MultiUtility.readFlag(fileName);
						if (flagValue != null) {
							if (flagValue.contains("404")) {
								webAccess = false;
								logger.info(userEntry.getSystemId() + " Blocked By Flag <404> ");
								this.accessLog.save(new AccessLogEntry(userEntry.getSystemId(),
										new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0,
										"failed", "Account Blocked"));
								throw new InternalServerException(userEntry.getSystemId() + " Account Blocked!");
							} else {
								// userEntry.setFlagValue("100");
								logger.info(userEntry.getSystemId() + " Flag ====> " + flagValue);

							}
						} else {
							logger.info("<-- " + userEntry.getSystemId() + " Flag Read Error --> ");
							webAccess = false;
							this.accessLog.save(new AccessLogEntry(userEntry.getSystemId(),
									new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0,
									"failed", "Flag Read Error"));
							throw new InternalServerException("Error While Reading Flag!");
						}
						if (webAccess) {
							// ip validation starts
							logger.info(userEntry.getSystemId() + "<-- Checking For AccessIP -->");
							if (Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {

								if (ipaddress != null && !ipaddress.isEmpty()) {
									boolean matched = false;
									if (ipaddress.equalsIgnoreCase("0:0:0:0:0:0:0:1")
											|| ipaddress.equalsIgnoreCase("127.0.0.1")) {
										logger.info(userEntry.getSystemId() + " Local AccessIp Matched: " + ipaddress);
										matched = true;
									} else {
										logger.info(userEntry.getSystemId() + " Matching[" + ipaddress
												+ "] From Super Access IP list");
										String[] IPs = IConstants.AccessIP.split(",");
										for (String allowedip : IPs) {
											if (allowedip.indexOf("/") > 0) {
												if (isInRange(allowedip, ipaddress)) {
													logger.info(userEntry.getSystemId() + " Range[" + allowedip
															+ "] Matched: " + ipaddress);
													matched = true;
													break;
												}
											} else {
												if (ipaddress.equalsIgnoreCase(allowedip)) {
													logger.info(userEntry.getSystemId() + " Configured[" + allowedip
															+ "] Ip Matched: " + ipaddress);
													matched = true;
													break;
												}
											}

										}
										if (!matched) {
											logger.info(userEntry.getSystemId() + " Matching[" + ipaddress
													+ "] From Global Access IP list");
											IPs = IConstants.GLOBAl_ACCESS_IP.split(",");
											for (String allowedip : IPs) {
												if (allowedip.indexOf("/") > 0) {
													if (isInRange(allowedip, ipaddress)) {
														logger.info(userEntry.getSystemId() + " Range[" + allowedip
																+ "] Matched: " + ipaddress);
														matched = true;
														break;
													}
												} else {
													if (ipaddress.equalsIgnoreCase(allowedip)) {
														logger.info(userEntry.getSystemId() + " Configured[" + allowedip
																+ "] Ip Matched: " + ipaddress);
														matched = true;
														break;
													}
												}
											}
										}
									}
									if (!matched) {
										logger.info(userEntry.getSystemId() + " Access IP Not Matched: " + ipaddress);
										if (userEntry.getAccessCountry() != null
												&& userEntry.getAccessCountry().length() > 0) {
											logger.info(userEntry.getSystemId() + " Matching[" + ipaddress
													+ "] From Allowed Country list");
											String country = getCountryname(ipaddress);
											if (country != null && !country.isEmpty()) {
												logger.info(userEntry.getSystemId() + " Country [" + ipaddress
														+ "] found in database: " + country);
												for (String allowedCountry : userEntry.getAccessCountry().split(",")) {
													if (allowedCountry.equalsIgnoreCase(country)) {
														matched = true;
														break;
													}
												}
											} else {
												logger.info(userEntry.getSystemId() + " Country [" + ipaddress
														+ "] not found in database.");
											}
											if (!matched) {
												webAccess = false;
												logger.info(userEntry.getSystemId() + " Access IP Not Allowed: "
														+ ipaddress);
												throw new InternalServerException(userEntry.getSystemId()
														+ " Access IP Not Allowed: " + ipaddress);
											} else {
												logger.info(userEntry.getSystemId() + " Valid Access Country: "
														+ ipaddress);
											}

										} else {
											webAccess = false;
											logger.info(userEntry.getSystemId() + " Access Countries Not Configured.");
											throw new InternalServerException(
													userEntry.getSystemId() + " Access Countries Not Configured.");
										}
									} else {
										logger.info(userEntry.getSystemId() + " Valid Access IP: " + ipaddress);
									}

								} else {
									throw new NotFoundException("Unable to found Ip address!");
								}

							} else {
								if (userEntry.getAccessIp() != null && userEntry.getAccessIp().length() > 0) {
									boolean matched = false;

									if (ipaddress != null && !ipaddress.isEmpty()) {
										if (ipaddress.equalsIgnoreCase("0:0:0:0:0:0:0:1")
												|| ipaddress.equalsIgnoreCase("127.0.0.1")) {
											logger.info(
													userEntry.getSystemId() + " Local AccessIp Matched: " + ipaddress);
											matched = true;
										} else {
											String[] allowed_list = userEntry.getAccessIp().split(",");
											for (String allowedip : allowed_list) {
												if (allowedip.indexOf("/") > 0) {
													if (isInRange(allowedip, ipaddress)) {
														logger.info(userEntry.getSystemId() + " Range[" + allowedip
																+ "] Matched: " + ipaddress);
														matched = true;
														break;
													}
												} else {
													if (ipaddress.equalsIgnoreCase(allowedip)) {
														logger.info(userEntry.getSystemId() + " Configured[" + allowedip
																+ "] Ip Matched: " + ipaddress);
														matched = true;
														break;
													}
												}
											}

											if (!matched) {
												logger.info(userEntry.getSystemId() + " Matching[" + ipaddress
														+ "] From Global Access IP list");
												allowed_list = IConstants.GLOBAl_ACCESS_IP.split(",");
												for (String allowedip : allowed_list) {
													if (allowedip.indexOf("/") > 0) {
														if (isInRange(allowedip, ipaddress)) {
															logger.info(userEntry.getSystemId() + " Range[" + allowedip
																	+ "] Matched: " + ipaddress);
															matched = true;
															break;
														}
													} else {
														if (ipaddress.equalsIgnoreCase(allowedip)) {
															logger.info(userEntry.getSystemId() + " Configured["
																	+ allowedip + "] Ip Matched: " + ipaddress);
															matched = true;
															break;
														}
													}
												}
											}
										}
										if (!matched) {
											logger.info(
													userEntry.getSystemId() + " Access IP Not Matched: " + ipaddress);
											if (userEntry.getAccessCountry() != null
													&& userEntry.getAccessCountry().length() > 0) {
												logger.info(userEntry.getSystemId() + " Matching[" + ipaddress
														+ "] From Allowed Country list");
												String country = getCountryname(ipaddress);
												if (country != null && !country.isEmpty()) {
													logger.info(userEntry.getSystemId() + " Country [" + ipaddress
															+ "] found in database: " + country);
													for (String allowedCountry : userEntry.getAccessCountry()
															.split(",")) {
														if (allowedCountry.equalsIgnoreCase(country)) {
															matched = true;
															break;
														}
													}
												} else {
													logger.info(userEntry.getSystemId() + " Country [" + ipaddress
															+ "] not found in database.");
												}
												if (!matched) {
													webAccess = false;
													logger.info(userEntry.getSystemId() + " Access IP Not Allowed: "
															+ ipaddress);

													throw new InternalServerException(userEntry.getSystemId()
															+ " Access IP Not Allowed: " + ipaddress);
												} else {
													logger.info(userEntry.getSystemId() + " Valid Access Country: "
															+ ipaddress);
												}
											} else {
												webAccess = false;
												logger.info(
														userEntry.getSystemId() + " Access Countries Not Configured.");
												throw new InternalServerException(
														userEntry.getSystemId() + " Access Countries Not Configured.");
											}
										} else {
											logger.info(userEntry.getSystemId() + " Valid Access IP: " + ipaddress);
										}

									} else {
										throw new NotFoundException("Unable to found Ip Address!");
									}

								} else {
									logger.info(userEntry.getSystemId() + " Access IP Address Not Configured.");
									if (userEntry.getAccessCountry() != null
											&& userEntry.getAccessCountry().length() > 0) {
										boolean matched = false;
										if (ipaddress.equalsIgnoreCase("0:0:0:0:0:0:0:1")
												|| ipaddress.equalsIgnoreCase("127.0.0.1")) {
											logger.info(
													userEntry.getSystemId() + " Local AccessIp Matched: " + ipaddress);
											matched = true;
										} else {
											logger.info(userEntry.getSystemId() + " Matching[" + ipaddress
													+ "] From Allowed Country list");
											String country = getCountryname(ipaddress);
											if (country != null && !country.isEmpty()) {
												logger.info(userEntry.getSystemId() + " Country [" + ipaddress
														+ "] found in database: " + country);
												for (String allowedCountry : userEntry.getAccessCountry().split(",")) {
													if (allowedCountry.equalsIgnoreCase(country)) {
														matched = true;
														break;
													}
												}
											} else {
												logger.info(userEntry.getSystemId() + " Country [" + ipaddress
														+ "] not found in database.");
											}
										}

										if (!matched) {
											webAccess = false;
											logger.info(
													userEntry.getSystemId() + " Access IP Not Allowed: " + ipaddress);
											throw new InternalServerException(
													userEntry.getSystemId() + " Access IP Not Allowed: " + ipaddress);

										} else {
											logger.info(
													userEntry.getSystemId() + " Valid Access Country: " + ipaddress);
										}

									} else {
										logger.info(userEntry.getSystemId() + " Access Countries Not Configured.");
										throw new InternalServerException(
												userEntry.getSystemId() + " Access Countries Not Configured.");
									}
								}

							}

						}
					}
				} else {
					logger.info("Web Access Denied User -> " + userEntry.getSystemId() + " Password -> "
							+ userEntry.getPassword());
					this.accessLog.save(new AccessLogEntry(userEntry.getSystemId(),
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0, "failed",
							"WebAccess Denied"));
					throw new InternalServerException("Web Access Denied User -> " + loginRequest.getUsername()
							+ " Password -> " + loginRequest.getPassword());
				}
			} else {
				throw new NotFoundException("WebMaster Entry not found!");
			}

			if (webAccess) {
				System.out.println(webEntry.isOtpLogin());
				if (webEntry.isOtpLogin()) {

					boolean otplogin = true;
					if (webEntry.isMultiUserAccess()) {
						logger.info(userEntry.getSystemId() + " Multi User Access Enabled.");
						List<MultiUserEntry> list = this.multiUserRepo.findByUserIdEquals(userEntry.getId());
						if (list.isEmpty()) {
							logger.info(userEntry.getSystemId() + " No Multi Access Name Found");
						} else {
							// forward to ask access name page.
							otplogin = false;
//							target = "multiaccess";
							// TODO ask Vinod Sir about this line
//							request.getSession().setAttribute("loginEntry", loginDTO);
						}

					}
					if (otplogin) {
						if (webEntry.getOtpNumber() != null && webEntry.getOtpNumber().length() > 0) {
//							target = "otplogin";
							String valid_otp_numbers = "";
							for (String number : webEntry.getOtpNumber().split(",")) {
								logger.info(userEntry.getSystemId() + " OTP Number: " + number);
								try {
									Long.parseLong(number);
									valid_otp_numbers += number + ",";
								} catch (NumberFormatException ne) {
									logger.error(userEntry.getSystemId() + " Invalid OTP Number Configured: " + number);
								}
								if (valid_otp_numbers.length() > 0) {
									int otp = 0;
									valid_otp_numbers = valid_otp_numbers.substring(0, valid_otp_numbers.length() - 1);
									logger.info(userEntry.getSystemId() + " Valid OTP Numbers: " + valid_otp_numbers);
									// check otp exist & send to user in case of absent or expired
									Optional<OTPEntry> optionalOtp = this.otpEntryRepository.findBySystemId(userEntry.getSystemId());
									OTPEntry otpEntry = null;
									if(optionalOtp.isPresent()) {
										otpEntry = optionalOtp.get();
									}else {
										logger.info("no otp entry found for the user!");
									}
									boolean generate_otp = true;
									if (otpEntry != null) {
										if (otpEntry.getExpiresOn() != null) {
											try {
												if (new Date().after(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
														.parse(otpEntry.getExpiresOn()))) {
													logger.info(userEntry.getSystemId() + " OTP ExpiredOn: "
															+ otpEntry.getExpiresOn());
												} else {
													generate_otp = false;
													otp = otpEntry.getOneTimePass();
												}
											} catch (ParseException e) {
												logger.error(e.getLocalizedMessage());
												throw new InternalServerException("Unable to parse otp expiry date!");
											}
										}

									}
									if (generate_otp) {
										otp = new Random().nextInt(999999 - 100000) + 100000;
										Calendar calendar = Calendar.getInstance();
										int duration = 5;
										if (IConstants.LOGIN_OTP_VALIDITY > 0) {
											duration = IConstants.LOGIN_OTP_VALIDITY;
										}
										calendar.add(Calendar.MINUTE, duration);
										String validity = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
												.format(calendar.getTime());
										if (otpEntry != null) {
											otpEntry.setExpiresOn(validity);
											otpEntry.setOneTimePass(otp);
											// updateOTPEntry in database
											this.otpEntryRepository.save(otpEntry);
										} else {
											// save otp entry
											this.otpEntryRepository
													.save(new OTPEntry(userEntry.getSystemId(), otp, validity));
										}
										// --------------------- send to user ---------------
										UserEntry internalUser = this.userEntryRepository.findByRole("internal");
										if (internalUser != null) {
											String content = null;
											try {
												content = MultiUtility.readContent(IConstants.FORMAT_DIR + "otp.txt");
											} catch (Exception ex) {
												logger.error("OTP_FORMAT (unable to read otp.txt file): ", ex.getLocalizedMessage());
											}
											if (content == null) {
												content = "Hello [system_id], [otp_pass] is your One-Time Password (OTP) on [url] valid for next [duration] minutes";
											}
											content = content.replace("[system_id]", userEntry.getSystemId());
											content = content.replace("[otp_pass]", String.valueOf(otp));
											content = content.replace("[url]", IConstants.WebUrl);
											content = content.replace("[duration]", String.valueOf(duration));
											ArrayList<String> list = new ArrayList<String>(
													Arrays.asList(valid_otp_numbers.split(",")));
											BulkSmsDTO smsDTO = new BulkSmsDTO();
											smsDTO.setSystemId(internalUser.getSystemId());
											smsDTO.setPassword(internalUser.getPassword());
											smsDTO.setMessage(content);
											smsDTO.setDestinationList(list);
											if (webEntry.getOtpSender() != null
													&& webEntry.getOtpSender().length() > 1) {
												smsDTO.setSenderId(webEntry.getOtpSender());
											} else {
												smsDTO.setSenderId(IConstants.OTP_SENDER_ID);
											}
											/*
											 * To Be Implemented By Vinod Sir To Send Bulk SMS String Response = new
											 * SendSmsService().sendAlert(smsDTO);
											 * 
											 * logger.info( "<OTP SMS: " + Response + ">" + userEntry.getSystemId() +
											 * "<" + valid_otp_numbers + ">");
											 */
											System.out.println(content);
											if (webEntry.getOtpEmail() != null && webEntry.getOtpEmail().length() > 0) {
												String from = IConstants.SUPPORT_EMAIL[0];
												ProfessionEntry proEntry = getProfessionEntry(userEntry.getId());
												if (proEntry.getDomainEmail() != null
														&& proEntry.getDomainEmail().length() > 0
														&& proEntry.getDomainEmail().contains("@")
														&& proEntry.getDomainEmail().contains(".")) {
													from = proEntry.getDomainEmail();
													logger.info(
															userEntry.getSystemId() + " Domain-Email Found: " + from);
												} else {
													String master = userEntry.getMasterId();
													ProfessionEntry professionEntry = getProfessionEntry(master);
													if (professionEntry != null
															&& professionEntry.getDomainEmail() != null
															&& professionEntry.getDomainEmail().length() > 0
															&& professionEntry.getDomainEmail().contains("@")
															&& professionEntry.getDomainEmail().contains(".")) {
														from = professionEntry.getDomainEmail();
														logger.info(userEntry.getSystemId()
																+ " Master Domain-Email Found: " + from);
													} else {
														logger.info(
																userEntry.getSystemId() + " Domain-Email Not Found");
													}
													logger.info(userEntry.getSystemId() + " Sending OTP Email From["
															+ from + "] on: " + webEntry.getOtpEmail());
													

												}
												emailSender.sendEmail(webEntry.getOtpEmail(), Constant.OTP_SUBJECT, Constant.GENERAL_TEMPLATE_PATH,
														emailSender.createSourceMap(content));
											}
											
											
										}
									}else {
										logger.info("Otp already generated!");
									}

								} else {
									this.accessLog.save(new AccessLogEntry(userEntry.getSystemId(),
											new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress,
											0, "failed", "missing OTP numbers"));
									throw new InternalServerException("Missing OTP numbers!");
								}
							}
						} else {
							logger.error(userEntry.getSystemId() + " OTP Number Not Configured.");
							this.accessLog.save(new AccessLogEntry(userEntry.getSystemId(),
									new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0,
									"failed", "missing OTP numbers"));
							throw new InternalServerException(userEntry.getSystemId() + " OTP Number Not Configured.");
						}
					}
					ResponseEntity<?> userLogin = login(loginRequest);
					JwtResponse jwtResp = (JwtResponse) userLogin.getBody();
					LoginResponse loginResponse = new LoginResponse();
					loginResponse.setJwtResponse(jwtResp);
					loginResponse.setOtpLogin(true);
					loginResponse.setStatus("Otp sent successfully!");
					return ResponseEntity.ok(loginResponse);

				} else {
					// if not otp login send email to user of login alert
					if (webEntry.isEmailOnLogin()) {
						// send email for login
						String to = IConstants.TO_EMAIl;
						String from = IConstants.SUPPORT_EMAIL[0];
						ProfessionEntry proEntry = getProfessionEntry(userEntry.getId());
						if (proEntry.getDomainEmail() != null && proEntry.getDomainEmail().length() > 0
								&& proEntry.getDomainEmail().contains("@") && proEntry.getDomainEmail().contains(".")) {
							from = proEntry.getDomainEmail();
							logger.info(userEntry.getSystemId() + " Domain-Email Found: " + from);
						} else {
							ProfessionEntry professionEntry = getProfessionEntry(userEntry.getMasterId());
							if (professionEntry != null && professionEntry.getDomainEmail() != null
									&& professionEntry.getDomainEmail().length() > 0
									&& professionEntry.getDomainEmail().contains("@")
									&& professionEntry.getDomainEmail().contains(".")) {
								from = professionEntry.getDomainEmail();
								logger.info(userEntry.getSystemId() + " Master Domain-Email Found: " + from);
							} else {
								logger.info(userEntry.getSystemId() + " Domain-Email Not Found");
							}
						}
						if (webEntry.getEmail() != null && webEntry.getEmail().contains("@")
								&& webEntry.getEmail().contains(".")) {
							to = webEntry.getEmail();
						}
						
						emailSender.sendEmail(webEntry.getEmail(), Constant.LOGIN_SUBJECT, Constant.LOGIN_TEMPLATE_PATH,
								emailSender.createCustomSourceMap(userEntry.getSystemId(),IConstants.GATEWAY_NAME,ipaddress,new Date().toString(),IConstants.DEFAULT_GMT));
					}
					ResponseEntity<?> userLogin = login(loginRequest);
					JwtResponse jwtResp = (JwtResponse) userLogin.getBody();
					LoginResponse loginResponse = new LoginResponse();
					loginResponse.setJwtResponse(jwtResp);
					loginResponse.setOtpLogin(false);
					loginResponse.setStatus("Login Alert Email Sent Successfully!");
					return ResponseEntity.ok(loginResponse);

				}
				

			} else {
				throw new InternalServerException("Web Access Denied User -> " + loginRequest.getUsername()
						+ " Password -> " + loginRequest.getPassword());
			}

		} else {
			logger.error("Invalid Creadentials! Unable to validate User.");
			throw new UnauthorizedException("Invalid Creadentials! Unable to validate User.");
		}

	}

	public String getCountryname(String ip_address) {

		String sql = "SELECT ip_location.country_name FROM ip_blocks JOIN ip_location ON ip_blocks.geoname_id = ip_location.geoname_id WHERE INET_ATON('"
				+ ip_address + "') BETWEEN ip_blocks.ip_from AND ip_blocks.ip_to";
		try {
			return this.jdbcTemplate.query(sql, (rs) -> {
				if (rs.next()) {
					return rs.getString("country_name");
				} else {
					return ""; // Return empty string if no country name found
				}
			});

		} catch (Exception e) {
			throw new InternalServerException(e.getMessage());
		}
	}

	public boolean isInRange(String range, String requestip) {
		boolean inRange = false;
		String[] parts = range.split("/");
		String ip = parts[0];
		int prefix;
		if (parts.length < 2) {
			prefix = 0;
		} else {
			prefix = Integer.parseInt(parts[1]);
		}
		Inet4Address a = null;
		Inet4Address a1 = null;
		try {
			a = (Inet4Address) InetAddress.getByName(ip);
			a1 = (Inet4Address) InetAddress.getByName(requestip);
		} catch (UnknownHostException e) {
			System.out.println("UnknownHostException : " + e);
		}
		byte[] b = a.getAddress();
		int ipInt = ((b[0] & 0xFF) << 24) | ((b[1] & 0xFF) << 16) | ((b[2] & 0xFF) << 8) | ((b[3] & 0xFF) << 0);
		byte[] b1 = a1.getAddress();
		int ipInt1 = ((b1[0] & 0xFF) << 24) | ((b1[1] & 0xFF) << 16) | ((b1[2] & 0xFF) << 8) | ((b1[3] & 0xFF) << 0);
		int mask = ~((1 << (32 - prefix)) - 1);
		if ((ipInt & mask) == (ipInt1 & mask)) {
			inRange = true;
		}
		return inRange;
	}

	public String getClientIp() {
		// Retrieve the current request attributes
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes();
		if (requestAttributes != null) {
			HttpServletRequest request = requestAttributes.getRequest();
			// Retrieve client's IP address
			String clientIp = request.getHeader("X-Forwarded-For");
			if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
				clientIp = request.getHeader("Proxy-Client-IP");
			}
			if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
				clientIp = request.getHeader("WL-Proxy-Client-IP");
			}
			if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
				clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
			}
			if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
				clientIp = request.getRemoteAddr();
			}
			return clientIp;
		} else {
			return null;
		}
	}

	public ProfessionEntry getProfessionEntry(String systemId) {
		ProfessionEntry entry = null;
		if (GlobalVars.UserMapping.containsKey(systemId)) {
			int userid = GlobalVars.UserMapping.get(systemId);
			entry = GlobalVars.ProfessionEntries.get(userid);
		}
		return entry;
	}

	public int validateEntry(String username, String password) {
		SalesEntry entry = this.salesRepository.findByUsername(username);
		if (entry == null) {
			logger.info("Invalid Sales Username: " + username);
			return 0;
		} else {
			if (entry.getPassword().equals(password)) {
				return entry.getId();
			} else {
				logger.info(username + " Invalid password: " + password);
				return 0;
			}
		}
	}

	public int validateExecutive(String systemId, String password) {
		int seller_id = 0;
		try {
			seller_id = validateEntry(systemId, password);
		} catch (Exception e) {
			logger.error(systemId, e);
		}
		return seller_id;
	}

	private Map<Integer, String> listNamesUnderManager(String mgrId) {
		Map<Integer, String> map = new HashMap<Integer, String>();
		List<SalesEntry> list = null;
		try {
			list = this.salesRepository.findByMasterIdAndRole(mgrId, "seller");
			if (!list.isEmpty()) {
				for (SalesEntry entry : list) {
					map.put(entry.getId(), entry.getUsername());
				}
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SALES_NOTFOUND));
			}
		} catch (NotFoundException e) {
			logger.error(messageResourceBundle.getLogMessage("sales.msg.error"), e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("sales.msg.error"), e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SALES_MSG_ERROR,
					new Object[] { e.getMessage() }));
		}

		return map;
	}

	public UserSessionObject getExecutiveSessionObject(int id) {
		UserSessionObject userSessionObject = new UserSessionObject();
		SalesEntry entry = this.salesRepository.findById(id)
				.orElseThrow(() -> new InternalServerException("No Seller User Found!"));
		userSessionObject.setId(entry.getId());
		userSessionObject.setSystemId(entry.getUsername());
		userSessionObject.setPassword(entry.getPassword());
		userSessionObject.setRole(entry.getRole());
		userSessionObject.getBalance().setWalletFlag("No");
		userSessionObject.getBalance().setWalletAmount(0.0);
		userSessionObject.getBalance().setCredits(0L);
		userSessionObject.setMasterId(entry.getMasterId());
		userSessionObject.getWebMasterEntry().setHideNum(false);
		userSessionObject.setCreatedOn(entry.getCreatedOn());
		userSessionObject.setExpiry(entry.getExpiredOn());
		userSessionObject.getWebMasterEntry().setEmail(entry.getEmail());
		userSessionObject.getWebMasterEntry().setAccess("PARTIAL");
		if (entry.getRole().equalsIgnoreCase("manager")) {
			Map<Integer, String> map = listNamesUnderManager(entry.getUsername());
			int userCount = 0;
			for (Integer seller_id : map.keySet()) {
				userCount += this.webMasterEntryRepository.countUsersUnderSeller(seller_id);
			}
			userSessionObject.setExeCount(map.size());
			userSessionObject.setUserCount(userCount);
		} else {
			userSessionObject.setUserCount((int) this.webMasterEntryRepository.countUsersUnderSeller(id));
		}
		return userSessionObject;
	}

	@Override
	public ResponseEntity<?> sellerValidation(String systemId, String password) {
		String target = IConstants.FAILURE_KEY;
		int seller_id = validateExecutive(systemId, password);
		if (seller_id > 0) {
			Date expired = null;
			UserSessionObject userSessionObject = getExecutiveSessionObject(seller_id);
			logger.info("<- Session Object Created -> " + userSessionObject.getSystemId());
			try {
				expired = new SimpleDateFormat("yyyy-MM-dd").parse(userSessionObject.getExpiry());
			} catch (Exception ex) {
				expired = new Date();
				logger.error(userSessionObject.getSystemId() + ": " + ex);
			}
			boolean isExpired = false;
			try {
				Date checkDate = new SimpleDateFormat("yyyy-MM-dd").parse(expired.toString());
				Date currDate = new SimpleDateFormat("yyyy-MM-dd")
						.parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
				isExpired = checkDate.before(currDate);
			} catch (ParseException ex) {
			}
			if (isExpired) {
				logger.info("<- Executive Account Expired -> " + systemId);
				throw new InternalServerException("User " + systemId + " Account Expired!");
			} else {
				userSessionObject.setFlagValue("100");
				userSessionObject.getWebMasterEntry().setDisplayCost(true);
				target = IConstants.SUCCESS_KEY;
			}

		} else {
			throw new UnauthorizedException("Unauthorised Executive!");
		}
		return ResponseEntity.ok(target);
	}

}
