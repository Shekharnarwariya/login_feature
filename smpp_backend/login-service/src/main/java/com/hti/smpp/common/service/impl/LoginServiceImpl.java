package com.hti.smpp.common.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.hazelcast.map.IMap;
import com.hti.smpp.common.email.EmailSender;
import com.hti.smpp.common.exception.AuthenticationExceptionFailed;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.InvalidOtpException;
import com.hti.smpp.common.exception.InvalidPropertyException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.jwt.JwtUtils;
import com.hti.smpp.common.messages.dto.BulkSmsDTO;
import com.hti.smpp.common.request.LoginDTO;
import com.hti.smpp.common.request.LoginRequest;
import com.hti.smpp.common.request.PasswordUpdateRequest;
import com.hti.smpp.common.request.ProfileUpdateRequest;
import com.hti.smpp.common.request.SignupRequest;
import com.hti.smpp.common.response.JwtResponse;
import com.hti.smpp.common.response.ProfileResponse;
import com.hti.smpp.common.response.RecentActivityResponse;
import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.sales.repository.SalesRepository;
import com.hti.smpp.common.security.BCryptPasswordEncoder;
import com.hti.smpp.common.service.LoginService;
import com.hti.smpp.common.user.dto.AccessLogEntry;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.DlrSettingEntry;
import com.hti.smpp.common.user.dto.DriverInfo;
import com.hti.smpp.common.user.dto.MultiUserEntry;
import com.hti.smpp.common.user.dto.OTPEntry;
import com.hti.smpp.common.user.dto.ProfessionEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
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
import com.hti.smpp.common.util.FlagUtil;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MailUtility;
import com.hti.smpp.common.util.MessageResourceBundle;
import com.hti.smpp.common.util.MultiUtility;
import com.hti.smpp.common.util.OTPGenerator;
import com.hti.smpp.common.util.PasswordConverter;
import com.hti.smpp.common.util.SessionLogInsert;

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
	private RestTemplate restTemplate;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private MultiUserEntryRepository multiUserEntryRepository;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

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
	public ResponseEntity<?> loginjwt(LoginRequest loginRequest, HttpServletRequest request, WebMasterEntry webMaster,
			ProfessionEntry professionEntry, LoginDTO loginDTO, UserEntry userEntry) {
		String ipaddress = request.getRemoteAddr();
		String username = loginRequest.getUsername();
		try {
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
			SessionLogInsert.logQueue.enqueue(new AccessLogEntry(loginRequest.getUsername(),
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0, "failed",
					"wrong password"));
			int login_attempts = 0;
			if (GlobalVars.LoginAttempts.containsKey(username)) {
				login_attempts = GlobalVars.LoginAttempts.get(username);
			}
			login_attempts++;
			GlobalVars.LoginAttempts.put(username, login_attempts);
			logger.error("<-- Invalid User Login[" + username + "] --> Attempts:" + login_attempts);
			if (webMaster.getOtpEmail() != null) {
				String to = IConstants.TO_EMAIl;
				String from = IConstants.SUPPORT_EMAIL[0];
				String domainEmail = professionEntry.getDomainEmail();
				if (domainEmail != null && domainEmail.length() > 0 && domainEmail.contains("@")
						&& domainEmail.contains(".")) {
					from = domainEmail;
					logger.info(loginDTO.getSystemId() + " Domain-Email Found: " + from);
				} else {
					String master = userEntry.getMasterId();
					ProfessionEntry masterProfessionEntry = null;
					if (GlobalVars.UserMapping.containsKey(master)) {
						int masterid = GlobalVars.UserMapping.get(master);
						masterProfessionEntry = GlobalVars.ProfessionEntries.get(masterid);
					}
					String domainEmailMaster = masterProfessionEntry.getDomainEmail();
					if (domainEmailMaster != null && domainEmailMaster.length() > 0 && domainEmailMaster.contains("@")
							&& domainEmailMaster.contains(".")) {
						from = domainEmailMaster;
						logger.info(loginDTO.getSystemId() + " Domain-Email Found: " + from);
					} else {
						logger.info(loginDTO.getSystemId() + " Domain-Email Not Found");
					}
				}
				if (webMaster.getOtpEmail() != null && webMaster.getOtpEmail().contains("@")
						&& webMaster.getOtpEmail().contains(".")) {
					to = webMaster.getOtpEmail();
				}
				String mailContent = new MailUtility().mailOnLoginFailedContent(loginDTO.getSystemId(), ipaddress,
						login_attempts);
				try {
					MailUtility.send(to, mailContent, "Failed login alert", from, false);
					logger.error("failed login[" + loginDTO.getSystemId() + "] Email Sent From:" + from + " To:" + to);
				} catch (Exception ex) {
					logger.error(loginDTO.getSystemId() + " failed login email error", ex.fillInStackTrace());
				}
			} else {
				logger.info(loginDTO.getSystemId() + " OTP Email Not Found");
			}
			throw new AuthenticationExceptionFailed(
					messageResourceBundle.getExMessage(ConstantMessages.AUTHENTICATION_FAILED_PASSWORD));
		} catch (Exception e) {
			int login_attempts = 0;
			if (GlobalVars.LoginAttempts.containsKey(username)) {
				login_attempts = GlobalVars.LoginAttempts.get(username);
			}
			login_attempts++;
			GlobalVars.LoginAttempts.put(username, login_attempts);
			logger.error("<-- Invalid User Login[" + username + "] --> Attempts:" + login_attempts);
			logger.error(messageResourceBundle.getLogMessage("internal.server.error"), e.getMessage());
			SessionLogInsert.logQueue.enqueue(new AccessLogEntry(loginRequest.getUsername(),
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0, "failed",
					"system error"));
			if (webMaster.getOtpEmail() != null) {
				String to = IConstants.TO_EMAIl;
				String from = IConstants.SUPPORT_EMAIL[0];
				String domainEmail = professionEntry.getDomainEmail();
				if (domainEmail != null && domainEmail.length() > 0 && domainEmail.contains("@")
						&& domainEmail.contains(".")) {
					from = domainEmail;
					logger.info(loginDTO.getSystemId() + " Domain-Email Found: " + from);
				} else {
					String master = userEntry.getMasterId();
					ProfessionEntry masterProfessionEntry = null;
					if (GlobalVars.UserMapping.containsKey(master)) {
						int masterid = GlobalVars.UserMapping.get(master);
						masterProfessionEntry = GlobalVars.ProfessionEntries.get(masterid);
					}
					String domainEmailMaster = masterProfessionEntry.getDomainEmail();
					if (domainEmailMaster != null && domainEmailMaster.length() > 0 && domainEmailMaster.contains("@")
							&& domainEmailMaster.contains(".")) {
						from = domainEmailMaster;
						logger.info(loginDTO.getSystemId() + " Domain-Email Found: " + from);
					} else {
						logger.info(loginDTO.getSystemId() + " Domain-Email Not Found");
					}
				}
				if (webMaster.getOtpEmail() != null && webMaster.getOtpEmail().contains("@")
						&& webMaster.getOtpEmail().contains(".")) {
					to = webMaster.getOtpEmail();
				}
				String mailContent = new MailUtility().mailOnLoginFailedContent(loginDTO.getSystemId(), ipaddress,
						login_attempts);
				try {
					MailUtility.send(to, mailContent, "Failed login alert", from, false);
					logger.error("failed login[" + loginDTO.getSystemId() + "] Email Sent From:" + from + " To:" + to);
				} catch (Exception ex) {
					logger.error(loginDTO.getSystemId() + " failed login email error", ex.fillInStackTrace());
				}
			} else {
				logger.info(loginDTO.getSystemId() + " OTP Email Not Found");
			}
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
			WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(userEntry.getId());
			Optional<DlrSettingEntry> dlrOptional = dlrSettingEntryRepository.findById(userEntry.getId());
			DlrSettingEntry dlrSettingEntry = dlrOptional.get();

			ProfileResponse profileResponse = new ProfileResponse();
			profileResponse.setUserName(userEntry.getSystemId());
//			profileResponse.setBalance(String.valueOf(balanceEntry.getWalletAmount()));
			profileResponse.setCountry(professionEntry.getCountry());
			profileResponse.setEmail(professionEntry.getDomainEmail());
			profileResponse.setFirstName(professionEntry.getFirstName());
			profileResponse.setLastName(professionEntry.getLastName());
			profileResponse.setRoles(userEntry.getRole());
			profileResponse.setContactNo(professionEntry.getMobile());
			profileResponse.setCurrency(userEntry.getCurrency());
			profileResponse.setCompanyName(professionEntry.getCompany());
			profileResponse.setDesignation(professionEntry.getDesignation());
			profileResponse.setCity(professionEntry.getCity());
			profileResponse.setState(professionEntry.getState());
			profileResponse.setKeepLogs(userEntry.getLogDays());
			profileResponse.setReferenceID(professionEntry.getReferenceId());
			profileResponse.setCompanyAddress(professionEntry.getCompanyAddress());
			profileResponse.setCompanyEmail(professionEntry.getCompanyEmail());
			profileResponse.setTaxID(professionEntry.getTaxID());
			profileResponse.setRegID(professionEntry.getRegID());
			profileResponse.setNotes(professionEntry.getNotes());
			profileResponse.setCredits(balanceEntry.getCredits());
			profileResponse.setWallets(balanceEntry.getWalletAmount());
			profileResponse.setWalletFlag(balanceEntry.getWalletFlag());

//			--------------------------------------------Alert Setting ----------
			profileResponse.setAlertEmail(webMasterEntry.getMinBalEmail());
			profileResponse.setAlertMobile(webMasterEntry.getMinBalMobile());
			profileResponse.setInvoiceEmail(webMasterEntry.getInvoiceEmail());
			profileResponse.setDlrReport(webMasterEntry.isDlrReport());
			profileResponse.setDlrEmail(webMasterEntry.getDlrEmail());
			profileResponse.setCoverageReport(webMasterEntry.getCoverageReport());
			profileResponse.setCoverageEmail(webMasterEntry.getCoverageEmail());
			profileResponse.setLowAmount(webMasterEntry.getMinBalance());
			profileResponse.setSmsAlert(webMasterEntry.isSmsAlert());
			profileResponse.setWebUrl(dlrSettingEntry.getWebUrl());
			profileResponse.setDlrThroughWeb(dlrSettingEntry.isWebDlr());
			profileResponse.setLowBalanceAlert(webMasterEntry.isMinFlag());
			profileResponse.setMis(webMasterEntry.isMisReport());
//			-------------------------------------------------------------

			String profileImagePath = professionEntry.getImageFilePath();
			if (profileImagePath != null && !profileImagePath.isEmpty()) {
				try {
					String fileExtension = profileImagePath.substring(profileImagePath.lastIndexOf(".") + 1);
					profileResponse.setProfileName(fileExtension);
					Path imagePath = Paths.get(IConstants.PROFILE_DIR + "profile//" + profileImagePath);
					byte[] imageBytes = Files.readAllBytes(imagePath);
					profileResponse.setProfilePath(imageBytes);
				} catch (Exception e) {
					logger.error("Error while reading the image file" + e.getMessage());
				}
			}

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
		int duration = 2;
		try {
			Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);

			if (userOptional.isPresent()) {
				String generateOTP = null;
				UserEntry user = userOptional.get();
				Optional<OTPEntry> optionalOTP = otpEntryRepository.findBySystemId(username);
				if (optionalOTP.isPresent()) {
					OTPEntry existingOTP = optionalOTP.get();
					LocalTime expiresOn = LocalTime.parse(existingOTP.getExpiresOn());
					if (expiresOn.isAfter(LocalTime.now())) {
						generateOTP = String.valueOf(existingOTP.getOneTimePass());
					} else {
						generateOTP = OTPGenerator.generateOTP(6);
						existingOTP.setOneTimePass(Integer.parseInt(generateOTP));
						existingOTP.setExpiresOn(LocalTime.now() + "");
						otpEntryRepository.save(existingOTP);
					}
				} else {
					generateOTP = OTPGenerator.generateOTP(6);
					OTPEntry newOTP = new OTPEntry();
					newOTP.setOneTimePass(Integer.parseInt(generateOTP));
					newOTP.setExpiresOn(LocalTime.now() + "");
					newOTP.setSystemId(username);
					otpEntryRepository.save(newOTP);
				}
				ProfessionEntry professionEntry = professionEntryRepository.findById(user.getId())
						.orElseThrow(() -> new NotFoundException(
								messageResourceBundle.getExMessage(ConstantMessages.PROFESSION_ENTRY_ERROR)));

				emailSender.sendEmail(professionEntry.getDomainEmail(), Constant.OTP_SUBJECT, Constant.TEMPLATE_PATH,
						emailSender.createSourceMap(Constant.MESSAGE_FOR_OTP, generateOTP,
								Constant.SECOND_MESSAGE_FOR_OTP, Constant.OTP_FLAG_SUBJECT,
								professionEntry.getFirstName() + " " + professionEntry.getLastName()));
				WebMasterEntry webEntry = webMasterEntryRepository.findByUserId(user.getId());
				if (webEntry.getOtpNumber() != null && webEntry.getOtpNumber().length() > 0) {
					String valid_otp_numbers = "";
					for (String number : webEntry.getOtpNumber().split(",")) {
						logger.info(messageResourceBundle.getLogMessage("user.otp.number.info"), user.getSystemId(),
								number);

						try {
							Long.parseLong(number);
							valid_otp_numbers += number + ",";
						} catch (NumberFormatException ne) {
							logger.error(messageResourceBundle.getLogMessage("user.invalid.otp.number.error"),
									user.getSystemId(), number);
						}
						if (valid_otp_numbers.length() > 0) {
							int otp = 0;
							valid_otp_numbers = valid_otp_numbers.substring(0, valid_otp_numbers.length() - 1);
							logger.info(messageResourceBundle.getLogMessage("user.valid.otp.numbers.info"),
									user.getSystemId(), valid_otp_numbers);
						}
						UserEntry internalUser = this.userEntryRepository.findByRole("internal");
						DriverInfo driverInfo = driverInfoRepository.findById(internalUser.getId()).get();
						if (internalUser != null) {
							String content = null;
							try {
								content = MultiUtility.readContent(IConstants.FORMAT_DIR + "otp.txt");
							} catch (Exception ex) {
								logger.error(messageResourceBundle.getLogMessage("user.otp.format.error"),
										ex.getMessage());
							}
							if (content == null) {
								content = "Hello [system_id], [otp_pass] is your One-Time Password (OTP) on [url] valid for next [duration] minutes";
							}
							content = content.replace("[system_id]", user.getSystemId());
							content = content.replace("[otp_pass]", String.valueOf(generateOTP));
							content = content.replace("[url]", IConstants.WebUrl);
							content = content.replace("[duration]", String.valueOf(duration));
							ArrayList<String> list = new ArrayList<String>(Arrays.asList(valid_otp_numbers.split(",")));
							BulkSmsDTO smsDTO = new BulkSmsDTO();
							smsDTO.setSystemId(internalUser.getSystemId());
							smsDTO.setPassword(
									new PasswordConverter().convertToEntityAttribute(driverInfo.getDriver()));
							smsDTO.setMessage(content);
							smsDTO.setDestinationList(list);
							if (webEntry.getOtpSender() != null && webEntry.getOtpSender().length() > 1) {
								smsDTO.setSenderId(webEntry.getOtpSender());
							} else {
								smsDTO.setSenderId(IConstants.OTP_SENDER_ID);
							}
							ResponseEntity<?> response = MultiUtility.sendOtpSms(user.getSystemId(), smsDTO,
									restTemplate);
							logger.info("<OTP SMS: " + response.getBody().toString() + ">" + user.getSystemId() + "<"
									+ valid_otp_numbers + ">");
						}
					}
				}
				return ResponseEntity.ok("OTP Sent Successfully!");
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NOT_FOUND));
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
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
	public ResponseEntity<?> updateUserProfile(String username, String email, String firstName, String lastName,
			String contact, String companyName, String designation, String city, String country, String state,
			String keepLogs, String referenceID, String companyAddress, String companyEmail, String notes, String taxID,
			String regID, MultipartFile profileImageFile, String alertEmail, String alertMobile, String invoiceEmail,
			Boolean dlrReport, String dlrEmail, String coverageEmail, String coverageReport, Double lowAmount,
			Boolean smsAlert, String webUrl, Boolean dlrThroughWeb, Boolean mis, Boolean lowBalanceAlert) {
		Optional<UserEntry> optionalUser = userEntryRepository.findBySystemId(username);
		try {

			if (optionalUser.isPresent()) {
				UserEntry user = optionalUser.get();
				ProfessionEntry professionEntry = professionEntryRepository.findById(user.getId())
						.orElseThrow(() -> new NotFoundException(
								messageResourceBundle.getExMessage(ConstantMessages.PROFESSION_ENTRY_ERROR)));

				WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(user.getId());
				Optional<DlrSettingEntry> dlrOptional = dlrSettingEntryRepository.findById(user.getId());
				DlrSettingEntry dlrSettingEntry = dlrOptional.get();

				updateUserData(user, email, firstName, lastName, contact, companyName, designation, city, country,
						state, keepLogs, referenceID, companyAddress, companyEmail, notes, taxID, regID,
						professionEntry, profileImageFile);

				updateAlertSetting(alertEmail, alertMobile, invoiceEmail, dlrReport, dlrEmail, coverageEmail,
						coverageReport, lowAmount, smsAlert, webUrl, dlrThroughWeb, mis, lowBalanceAlert,
						webMasterEntry, dlrSettingEntry);

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				String formattedDateTime = LocalDateTime.now().format(formatter);
				user.setEditOn(formattedDateTime);

				user.setEditBy(username);
				if (keepLogs != null) {

					user.setLogDays(Integer.parseInt(keepLogs));
				}

				userEntryRepository.save(user);
				professionEntryRepository.save(professionEntry);
				webMasterEntryRepository.save(webMasterEntry);
				dlrSettingEntryRepository.save(dlrSettingEntry);
				
				 String flagVal = FlagUtil.readFlag(Constants.USER_FLAG_DIR + username + ".txt");
				 System.out.println(flagVal);
	                if (flagVal != null && !flagVal.equalsIgnoreCase("404")) {
	                	
	                    // Change flag
	                    FlagUtil.changeFlag(Constants.USER_FLAG_DIR + username + ".txt", "505");
	                    FlagUtil.changeFlag(Constants.CLIENT_FLAG_FILE, "707");
	                 
	             //     System.out.println(  FlagUtil.changeFlag(Constants.CLIENT_FLAG_FILE, "707") +" "+"yoyoyoyoy" + " "+FlagUtil.changeFlag(Constants.USER_FLAG_DIR + username + ".txt", "505"));
	                }

				return ResponseEntity.ok("Profile updated successfully");
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NOT_FOUND));
			}
		} catch (Exception e) {

			throw new InternalServerException("Something Went Wrong");
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
	private void updateUserData(UserEntry user, String email, String firstName, String lastName, String contact,
			String companyName, String designation, String city, String country, String state, String keepLogs,
			String referenceID, String companyAddress, String companyEmail, String notes, String taxID, String regID,
			ProfessionEntry professionEntry, MultipartFile profileImageFile) {
		if (email != null) {
			professionEntry.setDomainEmail(email);
		}
		if (firstName != null) {
			professionEntry.setFirstName(firstName);
		}
		if (lastName != null) {
			professionEntry.setLastName(lastName);
		}
		if (contact != null) {
			professionEntry.setMobile(contact);
		}
		if (companyName != null) {
			professionEntry.setCompany(companyName);
		}
		if (designation != null) {
			professionEntry.setDesignation(designation);
		}
		if (city != null) {
			professionEntry.setCity(city);
		}
		if (country != null) {
			professionEntry.setCountry(country);
		}
		if (state != null) {
			professionEntry.setState(state);
		}
		if (companyAddress != null) {
			professionEntry.setCompanyAddress(companyAddress);
		}
		if (companyEmail != null) {
			professionEntry.setCompanyEmail(companyEmail);
		}
		if (notes != null) {
			professionEntry.setNotes(notes);
		}
		if (taxID != null) {
			professionEntry.setTaxID(taxID);
		}
		if (regID != null) {
			professionEntry.setRegID(regID);
		}
		if (referenceID != null) {
			professionEntry.setReferenceId(referenceID);
		}

		if (profileImageFile != null && !profileImageFile.isEmpty()) {
			try {
				byte[] fileContent = profileImageFile.getBytes();
				String originalFileName = profileImageFile.getOriginalFilename();
				String filePath = IConstants.PROFILE_DIR + "profile//";

				File directory = new File(filePath);
				if (!directory.exists()) {
					directory.mkdirs();
				}
				String originalImagePath = filePath + originalFileName;
				File originalFile = new File(originalImagePath);
				FileOutputStream fos = new FileOutputStream(originalFile);
				fos.write(fileContent);
				fos.close();
				professionEntry.setImageFilePath(originalFileName);
			} catch (IOException e) {
				e.printStackTrace();
				throw new InternalServerException("Error while parsing the image");
			}
		}
	}

	private void updateAlertSetting(String alertEmail, String alertMobile, String invoiceEmail, Boolean dlrReport,
			String dlrEmail, String coverageEmail, String coverageReport, Double lowAmount, Boolean smsAlert,
			String webUrl, Boolean dlrThroughWeb, Boolean mis, Boolean lowBalanceAlert, WebMasterEntry webMasterEntry,
			DlrSettingEntry dlrSettingEntry) {
		if (alertMobile != null) {
			webMasterEntry.setMinBalMobile(alertMobile);
		}
		if (lowAmount != null) {
			webMasterEntry.setMinBalance(lowAmount);
		}
		if (alertEmail != null) {
			webMasterEntry.setMinBalEmail(alertEmail);
		}
		if (invoiceEmail != null) {
			webMasterEntry.setInvoiceEmail(invoiceEmail);
		}
		if (dlrReport != null) {
			webMasterEntry.setDlrReport(dlrReport);
		}
		if (dlrEmail != null) {
			webMasterEntry.setDlrEmail(dlrEmail);
		}
		if (coverageReport != null) {
			webMasterEntry.setCoverageReport(coverageReport);
		}
		if (coverageEmail != null) {
			webMasterEntry.setCoverageEmail(coverageEmail);
		}
		if (smsAlert != null) {
			webMasterEntry.setSmsAlert(smsAlert);
		}
		if (webUrl != null) {
			dlrSettingEntry.setWebUrl(webUrl);
		}
		if (dlrThroughWeb != null) {
			dlrSettingEntry.setWebDlr(dlrThroughWeb);
		}
		if (mis != null) {
			webMasterEntry.setMisReport(mis);
			;
		}
		if (lowBalanceAlert != null) {
			webMasterEntry.setMinFlag(lowBalanceAlert);
		}

	}

	@Override
	public ResponseEntity<?> userRecentActivity(String username) {

		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		if (userOptional.isEmpty()) {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NOT_FOUND));
		}
		UserEntry user = userOptional.get();
		String system_id = user.getSystemId();
		RecentActivityResponse recentActivityResponse = new RecentActivityResponse();
		try {
			AccessLogEntry successLogEntry = getLastSuccessLogin(system_id);

			AccessLogEntry failedLogEntry = getLastFailedLogin(system_id);

			recentActivityResponse.setSuccessLogEntry(successLogEntry);
			recentActivityResponse.setFailedLogEntry(failedLogEntry);
			recentActivityResponse.setGmt(IConstants.DEFAULT_GMT);
		} catch (Exception e) {
			throw new InternalServerException("Something Went Wrong");
		}

		return new ResponseEntity<>(recentActivityResponse, HttpStatus.ACCEPTED);
	}

	public AccessLogEntry getLastSuccessLogin(String systemId) {
		String query = "select time from web_access_log where status = 'login' and system_id=? order by time DESC limit 2";
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		AccessLogEntry logEntry = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(query);
			pStmt.setString(1, systemId);
			rs = pStmt.executeQuery();
			while (rs.next()) {
				System.out.println(systemId + " login time: " + rs.getString("time"));
				logEntry = new AccessLogEntry();
				logEntry.setTime(rs.getString("time"));
			}
		} catch (SQLException sqle) {
			logger.error(systemId + "", sqle);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}

		return logEntry;
	}

	public AccessLogEntry getLastFailedLogin(String systemId) {
		String query = "select MAX(time) as time,remarks from web_access_log where status = 'failed' and system_id=? GROUP BY remarks";
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		AccessLogEntry logEntry = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(query);
			pStmt.setString(1, systemId);
			rs = pStmt.executeQuery();
			if (rs.next()) {
				logEntry = new AccessLogEntry();
				logEntry.setTime(rs.getString("time"));
				logEntry.setRemarks(rs.getString("remarks"));
			}
		} catch (SQLException sqle) {
			logger.error(systemId + "", sqle);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return logEntry;
	}

	@Override
	public ResponseEntity<?> login(LoginRequest loginRequest, HttpServletRequest request) {
		LoginDTO loginDTO = new LoginDTO();
		loginDTO.setSystemId(loginRequest.getUsername());
		loginDTO.setPassword(loginRequest.getPassword());
		String target = IConstants.FAILURE_KEY;
		String username = loginRequest.getUsername();
		if (!userEntryRepository.existsBySystemId(username) && !salesRepository.existsByUsername(username)) {
			logger.error(messageResourceBundle.getLogMessage("auth.failed.userNotFound"), username);
			throw new AuthenticationExceptionFailed(
					messageResourceBundle.getExMessage(ConstantMessages.AUTHENTICATION_FAILED_USERNAME));
		}
		logger.info(messageResourceBundle.getLogMessage("log.attemptAuth"), username);
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(loginRequest.getUsername());
		UserEntry userEntry = userOptional.get();
		WebMasterEntry webMaster = webMasterEntryRepository.findByUserId(userEntry.getId());
		int login_attempts = 0;
		if (GlobalVars.LoginAttempts.containsKey(username)) {
			login_attempts = GlobalVars.LoginAttempts.get(username);
		}
		login_attempts++;
		if (login_attempts == IConstants.LOGIN_ATTEMPT_LIMIT) {
			System.out.println("run if..");
			webMaster.setWebAccess(false);
			webMasterEntryRepository.save(webMaster);
			target = "webAccessError";
			String flagVal = MultiUtility.readFlag(Constants.USER_FLAG_DIR + loginDTO.getSystemId() + ".txt");
			if ((flagVal != null) && (!flagVal.equalsIgnoreCase("404"))) {
				MultiUtility.changeFlag(Constants.USER_FLAG_DIR + loginDTO.getSystemId() + ".txt", "505");
				MultiUtility.changeFlag(Constants.CLIENT_FLAG_FILE, "707");
			}
			GlobalVars.LoginAttempts.remove(loginDTO.getSystemId());
		}
		ProfessionEntry professionEntry = null;
		if (GlobalVars.UserMapping.containsKey(userEntry.getSystemId())) {
			int userid = GlobalVars.UserMapping.get(userEntry.getSystemId());
			professionEntry = GlobalVars.ProfessionEntries.get(userid);
		}
		JwtResponse jwtResponse = (JwtResponse) loginjwt(loginRequest, request, webMaster, professionEntry, loginDTO,
				userEntry).getBody();
		boolean webAccess = true;
		String lang = "";
		String login_failed_remarks = null;

		try {
			String ipaddress = request.getRemoteAddr();
			logger.info("Web Login Requested by " + loginRequest.getUsername() + " Via IP: " + ipaddress);
			int userid = jwtResponse.getId();
			if (userid > 0 && Access.isAuthorized(jwtResponse.getRoles().get(0), "isAuthorizedAll")) {
				logger.info(userEntry.getSystemId() + " WebAccess: " + webMaster.isWebAccess());
				if (webMaster.isWebAccess()) {
					boolean isExpired = false;
					try {
						isExpired = new SimpleDateFormat("yyyy-MM-dd").parse(userEntry.getExpiry())
								.before(new java.util.Date());
					} catch (ParseException ex) {
						logger.error(userEntry.getSystemId(), ex.fillInStackTrace());
					}
					boolean isPasswordExpired = false;
					if (userEntry.isForcePasswordChange()) {
						try {
							isPasswordExpired = new SimpleDateFormat("yyyy-MM-dd")
									.parse(userEntry.getPasswordExpiresOn()).before(new java.util.Date());
						} catch (ParseException ex) {
							logger.error(userEntry.getSystemId(), ex.fillInStackTrace());
						}
					}
					if (isExpired) {
						logger.error("<- User Account Expired -> " + userEntry.getSystemId());
						target = "expired";
						SessionLogInsert.logQueue.enqueue(new AccessLogEntry(userEntry.getSystemId(),
								new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0, "failed",
								"Account Expired"));
						throw new UnauthorizedException("<- User Account Expired -> " + userEntry.getSystemId());
					} else if (isPasswordExpired) {
						logger.error("<- User Password Expired -> " + userEntry.getSystemId());
						target = "pwdexpired";
						SessionLogInsert.logQueue.enqueue(new AccessLogEntry(userEntry.getSystemId(),
								new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0, "failed",
								"Password Expired"));
						throw new UnauthorizedException("<- User Password Expired -> " + userEntry.getSystemId());
					} else {
						IMap<String, String> user_flag_status = GlobalVars.user_flag_status;
						String flagValue = user_flag_status.get(userEntry.getSystemId());
						if (flagValue != null) {
							if (flagValue.contains("404")) {
								webAccess = false;
								logger.error(userEntry.getSystemId() + " Blocked By Flag <404> ");
								SessionLogInsert.logQueue.enqueue(new AccessLogEntry(userEntry.getSystemId(),
										new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0,
										"failed", "Account Blocked"));
								throw new UnauthorizedException(userEntry.getSystemId() + " Blocked By Flag <404> ");
							} else {
								// userEntry.setFlagValue("100");
								logger.info(userEntry.getSystemId() + " Flag ====> " + flagValue);
							}
						} else {
							logger.info("<-- " + userEntry.getSystemId() + " Flag Read Error --> ");
							webAccess = false;
							SessionLogInsert.logQueue.enqueue(new AccessLogEntry(userEntry.getSystemId(),
									new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0,
									"failed", "Flag Read Error"));
							throw new UnauthorizedException("<-- " + userEntry.getSystemId() + " Flag Read Error --> ");
						}
						if (webAccess) {
							logger.info(userEntry.getSystemId() + "<-- Checking For AccessIP -->");
							if (userEntry.getRole().equalsIgnoreCase("superadmin")
									|| userEntry.getRole().equalsIgnoreCase("system")) {
								boolean matched = true;
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
										if (country != null) {
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
											logger.info(
													userEntry.getSystemId() + " Access IP Not Allowed: " + ipaddress);
										} else {
											logger.info(
													userEntry.getSystemId() + " Valid Access Country: " + ipaddress);
										}
									} else {
										webAccess = false;
										logger.info(userEntry.getSystemId() + " Access Countries Not Configured.");
									}
								} else {
									logger.info(userEntry.getSystemId() + " Valid Access IP: " + ipaddress);
								}
								webAccess = true;
							} else {
								if (userEntry.getAccessIp() != null && userEntry.getAccessIp().length() > 0) {
									boolean matched = false;
									if (ipaddress.equalsIgnoreCase("0:0:0:0:0:0:0:1")
											|| ipaddress.equalsIgnoreCase("127.0.0.1")) {
										logger.info(userEntry.getSystemId() + " Local AccessIp Matched: " + ipaddress);
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
											if (country != null) {
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
											} else {
												logger.info(userEntry.getSystemId() + " Valid Access Country: "
														+ ipaddress);
											}
										} else {
											webAccess = false;
											logger.info(userEntry.getSystemId() + " Access Countries Not Configured.");
										}
									} else {
										logger.info(userEntry.getSystemId() + " Valid Access IP: " + ipaddress);
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
											if (country != null) {
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
											/*
											 * SessionLogInsert.logQueue.enqueue(new
											 * AccessLogEntry(loginDTO.getSystemId(), new
											 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0,
											 * "failed", "Invalid AccessIp"));
											 */
										} else {
											logger.info(
													userEntry.getSystemId() + " Valid Access Country: " + ipaddress);
										}
									} else {
										logger.info(userEntry.getSystemId() + " Access Countries Not Configured.");
									}
								}
							}
							// -------------------------
							if (webAccess) {
								if (webMaster.isOtpLogin()) {

									boolean otplogin = true;
									if (webMaster.isMultiUserAccess()) {
										logger.info(userEntry.getSystemId() + " Multi User Access Enabled.");
										List<MultiUserEntry> list = listMultiUser(userEntry.getId());
										if (list.isEmpty()) {
											logger.info(userEntry.getSystemId() + " No Multi Access Name Found");
										} else {
											// forward to ask access name page.
											otplogin = false;
											target = "multiaccess";
											request.getSession().setAttribute("loginEntry", loginRequest);
										}
									}
									if (otplogin) {
										if (webMaster.getOtpNumber() != null && webMaster.getOtpNumber().length() > 0) {
											target = "otplogin";
											String valid_otp_numbers = "";
											for (String number : webMaster.getOtpNumber().split(",")) {
												logger.info(userEntry.getSystemId() + " OTP Number: " + number);
												try {
													Long.parseLong(number);
													valid_otp_numbers += number + ",";
												} catch (NumberFormatException ne) {
													logger.error(userEntry.getSystemId()
															+ " Invalid OTP Number Configured: " + number);
												}
											}
											if (valid_otp_numbers.length() > 0) {
												int otp = 0;
												valid_otp_numbers = valid_otp_numbers.substring(0,
														valid_otp_numbers.length() - 1);
												logger.info(userEntry.getSystemId() + " Valid OTP Numbers: "
														+ valid_otp_numbers);
												Optional<OTPEntry> otpOptional = otpEntryRepository
														.findBySystemId(userEntry.getSystemId());
												OTPEntry otpEntry = null;
												if (otpOptional.isPresent()) {
													otpEntry = otpOptional.get();
												}
												boolean generate_otp = true;
												if (otpEntry != null) {
													if (otpEntry.getExpiresOn() != null) {
														if (new Date().after(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
																.parse(otpEntry.getExpiresOn()))) {
															logger.info(userEntry.getSystemId() + " OTP ExpiredOn: "
																	+ otpEntry.getExpiresOn());
														} else {
															generate_otp = false;
															otp = otpEntry.getOneTimePass();
														}
													}
												}
												if (generate_otp) {
													otp = new Random().nextInt(999999 - 100000) + 100000;
													java.util.Calendar calendar = java.util.Calendar.getInstance();
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
														otpEntryRepository.save(otpEntry);
													} else {
														otpEntryRepository.save(
																new OTPEntry(userEntry.getSystemId(), otp, validity));
													}
													// --------------------- send to user ---------------
													UserEntry internalUser = this.userEntryRepository
															.findByRole("internal");
													if (internalUser != null) {
														String content = null;
														try {
															content = MultiUtility
																	.readContent(IConstants.FORMAT_DIR + "otp.txt");
														} catch (Exception ex) {
															logger.error("OTP_FORMAT", ex.fillInStackTrace());
														}
														if (content == null) {
															content = "hello [system_id], [otp_pass] is your One-Time Password (OTP) on [url] valid for next [duration] minutes";
														}
														content = content.replace("[system_id]",
																userEntry.getSystemId());
														content = content.replace("[otp_pass]", String.valueOf(otp));
														content = content.replace("[url]", IConstants.WebUrl);
														content = content.replace("[duration]",
																String.valueOf(duration));
														ArrayList<String> list = new ArrayList<String>(
																Arrays.asList(valid_otp_numbers.split(",")));
														BulkSmsDTO smsDTO = new BulkSmsDTO();
														smsDTO.setSystemId(internalUser.getSystemId());
														DriverInfo driverInfo = driverInfoRepository
																.findById(internalUser.getId()).get();
														smsDTO.setPassword(new PasswordConverter()
																.convertToEntityAttribute(driverInfo.getDriver()));
														// smsDTO.setMessageType("SpecialChar");
														smsDTO.setMessage(content);
														smsDTO.setDestinationList(list);
														// smsDTO.setFrom("Name");
														if (webMaster.getOtpSender() != null
																&& webMaster.getOtpSender().length() > 1) {
															smsDTO.setSenderId(webMaster.getOtpSender());
														} else {
															smsDTO.setSenderId(IConstants.OTP_SENDER_ID);
														}
														String Response = MultiUtility
																.sendOtpSms(userEntry.getSystemId(), smsDTO,
																		restTemplate)
																.getBody().toString();
														logger.info(
																"<OTP SMS: " + Response + ">" + userEntry.getSystemId()
																		+ "<" + valid_otp_numbers + ">");
														if (webMaster.getOtpEmail() != null
																&& webMaster.getOtpEmail().length() > 0) {
															String from = IConstants.SUPPORT_EMAIL[0];
															if (professionEntry.getDomainEmail() != null
																	&& professionEntry.getDomainEmail().length() > 0
																	&& professionEntry.getDomainEmail().contains("@")
																	&& professionEntry.getDomainEmail().contains(".")) {
																from = professionEntry.getDomainEmail();
																logger.info(userEntry.getSystemId()
																		+ " Domain-Email Found: " + from);
															} else {
																String master = userEntry.getMasterId();
																ProfessionEntry professionEntryMaster = null;
																if (GlobalVars.UserMapping.containsKey(master)) {
																	int masterid = GlobalVars.UserMapping.get(master);
																	professionEntryMaster = GlobalVars.ProfessionEntries
																			.get(masterid);
																}
																if (professionEntryMaster != null
																		&& professionEntryMaster
																				.getDomainEmail() != null
																		&& professionEntryMaster.getDomainEmail()
																				.length() > 0
																		&& professionEntryMaster.getDomainEmail()
																				.contains("@")
																		&& professionEntryMaster.getDomainEmail()
																				.contains(".")) {
																	from = professionEntryMaster.getDomainEmail();
																	logger.info(userEntry.getSystemId()
																			+ " Master Domain-Email Found: " + from);
																} else {
																	logger.info(userEntry.getSystemId()
																			+ " Domain-Email Not Found");
																}
															}
															logger.info(userEntry.getSystemId()
																	+ " Sending OTP Email From[" + from + "] on: "
																	+ webMaster.getOtpEmail());
															try {
																MailUtility.send(from, webMaster.getOtpEmail(),
																		"OTP Password", content);
															} catch (Exception e) {
																logger.error(userEntry.getSystemId()
																		+ " OTP Email Sending Error:" + e);
															}
														}
														loginDTO.setOtp(otp);
														loginDTO.setIpAddress(ipaddress);
														loginDTO.setLanguage(lang);
														loginDTO.setSystemId(loginRequest.getUsername());
														loginDTO.setPassword(loginRequest.getPassword());
														request.getSession().setAttribute("loginEntry", loginDTO);
														return ResponseEntity.ok("otp send successfully..");
													} else {
														logger.error("Internal User Not Configured For alerts.");
														target = "webAccessError";
														SessionLogInsert.logQueue
																.enqueue(new AccessLogEntry(loginDTO.getSystemId(),
																		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
																				.format(new Date()),
																		ipaddress, 0, "failed",
																		"internal user missing"));
														throw new UnauthorizedException(
																"Internal User Not Configured For alerts.");
													}
												} else {
													logger.info(
															userEntry.getSystemId() + " Login Request Via Recent OTP");
													loginDTO.setOtp(otp);
													loginDTO.setIpAddress(ipaddress);
													loginDTO.setLanguage(lang);
													loginDTO.setSystemId(loginRequest.getUsername());
													loginDTO.setPassword(loginRequest.getPassword());
													request.getSession().setAttribute("loginEntry", loginDTO);
													ResponseEntity.ok("Please Enter Recent OTP to proceed ");
												}
											} else {
												target = "webAccessError";
												SessionLogInsert.logQueue
														.enqueue(new AccessLogEntry(loginDTO.getSystemId(),
																new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
																		.format(new Date()),
																ipaddress, 0, "failed", "missing OTP numbers"));
												throw new UnauthorizedException("No Valid Number Found For OTP");
											}
										} else {
											logger.error(userEntry.getSystemId() + " OTP Number Not Configured.");
											target = "webAccessError";
											SessionLogInsert.logQueue.enqueue(new AccessLogEntry(loginDTO.getSystemId(),
													new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
													ipaddress, 0, "failed", "missing OTP numbers"));
											throw new UnauthorizedException("No Valid Number Found For OTP");
										}
									}
								} else {
									logger.info("Valid User -> " + loginDTO.getSystemId() + " Password -> "
											+ loginDTO.getPassword());
									logger.info(userEntry.getSystemId() + " Saving WebAccessLog Entry");
									SessionLogInsert.logQueue.enqueue(new AccessLogEntry(userEntry.getSystemId(),
											new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress,
											0, "login", null));
									if (webMaster.isEmailOnLogin()) {
										// send email for login
										String to = IConstants.TO_EMAIl;
										String from = IConstants.SUPPORT_EMAIL[0];
										String domainEmail = professionEntry.getDomainEmail();
										if (domainEmail != null && domainEmail.length() > 0 && domainEmail.contains("@")
												&& domainEmail.contains(".")) {
											from = domainEmail;
											logger.info(loginDTO.getSystemId() + " Domain-Email Found: " + from);
										} else {
											String master = userEntry.getMasterId();
											ProfessionEntry professionEntryMaster = null;
											if (GlobalVars.UserMapping.containsKey(master)) {
												int masterid = GlobalVars.UserMapping.get(master);
												professionEntryMaster = GlobalVars.ProfessionEntries.get(masterid);
											}
											String domainEmailMaster = professionEntryMaster.getDomainEmail();
											if (domainEmailMaster != null && domainEmailMaster.length() > 0
													&& domainEmailMaster.contains("@")
													&& domainEmailMaster.contains(".")) {
												from = domainEmailMaster;
												logger.info(loginDTO.getSystemId() + " Domain-Email Found: " + from);
											} else {
												logger.info(userEntry.getSystemId() + " Domain-Email Not Found");
											}
										}
										if (webMaster.getEmail() != null && webMaster.getEmail().contains("@")
												&& webMaster.getEmail().contains(".")) {
											to = webMaster.getEmail();
										}
										String mailContent = new MailUtility()
												.mailOnLoginContent(userEntry.getSystemId(), ipaddress);
										try {
											MailUtility.send(to, mailContent, "Login alert", from, false);
											logger.error("login[" + userEntry.getSystemId() + "] Email Sent From:"
													+ from + " To:" + to);
										} catch (Exception ex) {
											logger.error(userEntry.getSystemId() + " login email error",
													ex.fillInStackTrace());
										}
									}
									target = IConstants.SUCCESS_KEY;
								}
							} else {
								login_failed_remarks = "Invalid AccessIp";
								logger.info("Web Access Denied User -> " + loginRequest.getUsername() + " Password -> "
										+ loginRequest.getPassword());
								target = "webAccessError";
								throw new UnauthorizedException(
										"Web Access Denied User -> " + loginRequest.getUsername());
							}
						} else {
							logger.info("Web Access Denied User -> " + loginRequest.getUsername() + " Password -> ");
							target = "webAccessError";
							throw new UnauthorizedException("Web Access Denied User -> " + loginRequest.getUsername());
						}
					}
				} else {
					logger.info("Web Access Denied User -> " + loginRequest.getUsername() + " Password -> "
							+ loginRequest.getPassword());
					target = "webAccessError";
					SessionLogInsert.logQueue.enqueue(new AccessLogEntry(loginDTO.getSystemId(),
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0, "failed",
							"WebAccess Denied"));
					throw new UnauthorizedException("Web Access Denied User -> " + loginRequest.getUsername());
				}
			} else {
				int seller_id = jwtResponse.getId();
				SalesEntry salesEntry = null;
				if (seller_id > 0) {
					java.util.Date expired = null;
					try {
						Optional<SalesEntry> salesOptional = salesRepository.findById(seller_id);
						salesEntry = salesOptional.get();
						expired = new SimpleDateFormat("yyyy-MM-dd").parse(salesEntry.getExpiredOn());
					} catch (Exception ex) {
						expired = new java.util.Date();
						logger.error(salesEntry.getUsername() + ": " + ex);
					}
					boolean isExpired = false;
					try {
						java.util.Date checkDate = new SimpleDateFormat("yyyy-MM-dd").parse(expired.toString());
						java.util.Date currDate = new SimpleDateFormat("yyyy-MM-dd")
								.parse(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
						isExpired = checkDate.before(currDate);
					} catch (ParseException ex) {
					}
					if (isExpired) {
						logger.info("<- Executive Account Expired -> " + loginRequest.getUsername());
						target = "expired";
						throw new UnauthorizedException("Error: Account Expired");
					} else {
						target = IConstants.SUCCESS_KEY;
					}
				} else {
					target = "InvalidLogin";
					webAccess = false;
					login_failed_remarks = "invalid credentials";
				}
			}
			if (!webAccess && login_failed_remarks != null) {
				logger.error("<-- Invalid User Login[" + loginDTO.getSystemId() + ":" + loginDTO.getPassword() + "] -->"
						+ login_failed_remarks);
				if (webMaster != null) {
					if (GlobalVars.LoginAttempts.containsKey(loginDTO.getSystemId())) {
						login_attempts = GlobalVars.LoginAttempts.get(loginDTO.getSystemId());
					}
					GlobalVars.LoginAttempts.put(loginDTO.getSystemId(), login_attempts);
					logger.error("<-- Invalid User Login[" + loginDTO.getSystemId() + ":" + loginDTO.getPassword()
							+ "] --> Attempts:" + login_attempts);
					if (webMaster.getOtpEmail() != null) {
						String to = IConstants.TO_EMAIl;
						String from = IConstants.SUPPORT_EMAIL[0];
						String domainEmail = professionEntry.getDomainEmail();
						if (domainEmail != null && domainEmail.length() > 0 && domainEmail.contains("@")
								&& domainEmail.contains(".")) {
							from = domainEmail;
							logger.info(loginDTO.getSystemId() + " Domain-Email Found: " + from);
						} else {
							String master = userEntry.getMasterId();
							ProfessionEntry masterProfessionEntry = null;
							if (GlobalVars.UserMapping.containsKey(master)) {
								int masterid = GlobalVars.UserMapping.get(master);
								masterProfessionEntry = GlobalVars.ProfessionEntries.get(masterid);
							}
							String domainEmailMaster = masterProfessionEntry.getDomainEmail();
							if (domainEmailMaster != null && domainEmailMaster.length() > 0
									&& domainEmailMaster.contains("@") && domainEmailMaster.contains(".")) {
								from = domainEmailMaster;
								logger.info(loginDTO.getSystemId() + " Domain-Email Found: " + from);
							} else {
								logger.info(loginDTO.getSystemId() + " Domain-Email Not Found");
							}
						}
						if (webMaster.getOtpEmail() != null && webMaster.getOtpEmail().contains("@")
								&& webMaster.getOtpEmail().contains(".")) {
							to = webMaster.getOtpEmail();
						}
						String mailContent = new MailUtility().mailOnLoginFailedContent(loginDTO.getSystemId(),
								ipaddress, login_attempts);
						try {
							MailUtility.send(to, mailContent, "Failed login alert", from, false);
							logger.error("failed login[" + loginDTO.getSystemId() + "] Email Sent From:" + from + " To:"
									+ to);
						} catch (Exception ex) {
							logger.error(loginDTO.getSystemId() + " failed login email error", ex.fillInStackTrace());
						}
					} else {
						logger.info(loginDTO.getSystemId() + " OTP Email Not Found");
					}
					if (webMaster.getOtpNumber() != null && webMaster.getOtpNumber().length() > 7) {
						Set<String> valid_otp_numbers = new java.util.HashSet<String>();
						for (String number : webMaster.getOtpNumber().split(",")) {
							logger.info(loginDTO.getSystemId() + " OTP Number: " + number);
							try {
								long longnumber = Long.parseLong(number);
								if (longnumber > 0) {
									if (number.length() > 7) {
										valid_otp_numbers.add(number);
									} else {
										logger.error(
												loginDTO.getSystemId() + " Invalid OTP Number Configured: " + number);
									}
								} else {
									logger.error(loginDTO.getSystemId() + " Invalid OTP Number Configured: " + number);
								}
							} catch (NumberFormatException ne) {
								logger.error(loginDTO.getSystemId() + " Invalid OTP Number Configured: " + number);
							}
						}
						if (!valid_otp_numbers.isEmpty()) {
							UserEntry internalUser = this.userEntryRepository.findByRole("internal");
							if (internalUser != null) {
								String content = "Hello " + loginDTO.getSystemId() + ",\n" + login_attempts
										+ " failed login attempt(s) identified at " + IConstants.GATEWAY_NAME
										+ " via ip " + ipaddress + " on " + new Date() + " (" + IConstants.DEFAULT_GMT
										+ ")" + ".";
								BulkSmsDTO smsDTO = new BulkSmsDTO();
								smsDTO.setSystemId(internalUser.getSystemId());
								DriverInfo driverInfo = driverInfoRepository.findById(internalUser.getId()).get();
								smsDTO.setPassword(
										new PasswordConverter().convertToEntityAttribute(driverInfo.getDriver()));
								smsDTO.setMessage(content);
								smsDTO.setDestinationList(new ArrayList<String>(valid_otp_numbers));
								smsDTO.setSenderId("LOGIN-FAIL");
								String Response = MultiUtility.sendOtpSms(userEntry.getSystemId(), smsDTO, restTemplate)
										.getBody().toString();
								logger.info("<LOGIN FAILED ALERT SMS: " + Response + ">" + loginDTO.getSystemId() + "<"
										+ valid_otp_numbers + ">");
							}
						}
					} else {
						logger.info(loginDTO.getSystemId() + " OTP NUmber Not Found");
					}

				} else {
					logger.error("<-- WebmasterEntry Not Found [" + loginDTO.getSystemId() + ":"
							+ loginDTO.getPassword() + "] -->");

					SessionLogInsert.logQueue.enqueue(new AccessLogEntry(loginDTO.getSystemId(),
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0, "failed",
							login_failed_remarks));
					throw new UnauthorizedException(
							"<-- WebmasterEntry Not Found [" + loginDTO.getSystemId() + "] -->");
				}
			}
		} catch (UnauthorizedException ex) {
			throw new UnauthorizedException(ex.getMessage());
		} catch (Exception e) {
			target = IConstants.WEB_APPLICATION_ERROR;
			logger.error("Login Error: " + loginDTO.getSystemId(), e.fillInStackTrace());
			SessionLogInsert.logQueue.enqueue(new AccessLogEntry(loginDTO.getSystemId(),
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), request.getRemoteAddr(), 0,
					"failed", "Error " + e.getMessage()));
			throw new InternalServerException("Login Error: " + loginDTO.getSystemId() + " " + e.getMessage());
		}
		logger.info(loginDTO.getSystemId() + " login target: " + target);
		jwtResponse.setStatus(target);
		return ResponseEntity.ok(jwtResponse);

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

	public String getCountryname(String ip_address) {
		String country = null;
		String sql = "SELECT ip_location.country_name FROM ip_blocks JOIN ip_location ON ip_blocks.geoname_id = ip_location.geoname_id WHERE INET_ATON('"
				+ ip_address + "') BETWEEN ip_blocks.ip_from AND ip_blocks.ip_to";
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(sql);
			rs = pStmt.executeQuery();
			if (rs.next()) {
				country = rs.getString("country_name");
			}
		} catch (SQLException sqle) {
			logger.error(ip_address, sqle);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return country;
	}

	public List<MultiUserEntry> listMultiUser(int userId) throws Exception {
		logger.info("listing multiusers for userId:" + userId);
		// List<BulkMgmtEntry> list = new ArrayList<BulkMgmtEntry>();
		List<MultiUserEntry> list = new ArrayList<MultiUserEntry>();
		String query = "select * from multi_user_access where user_id = ?";
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		MultiUserEntry multiUserEntry = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(query);
			pStmt.setInt(1, userId);
			rs = pStmt.executeQuery();
			while (rs.next()) {
				multiUserEntry = new MultiUserEntry();
				multiUserEntry.setId(rs.getInt("id"));
				multiUserEntry.setAccessName(rs.getString("access_name"));
				multiUserEntry.setMobile(rs.getString("mobile"));
				multiUserEntry.setEmail(rs.getString("email"));
				list.add(multiUserEntry);
			}
		} catch (SQLException sqle) {
			logger.error(userId + "", sqle);
			throw new Exception(sqle.getMessage());
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}
		logger.info(userId + " multiuser list: " + list.size());
		return list;
	}

	public void falidEmail() {

	}

	@Override
	public ResponseEntity<?> loginOtp(LoginRequest loginRequest, HttpServletRequest request, String purpose) {
		String username = loginRequest.getUsername();
		if (!userEntryRepository.existsBySystemId(username) && !salesRepository.existsByUsername(username)) {
			logger.error(messageResourceBundle.getLogMessage("auth.failed.userNotFound"), username);
			throw new AuthenticationExceptionFailed(
					messageResourceBundle.getExMessage(ConstantMessages.AUTHENTICATION_FAILED_USERNAME));
		}
		String target = IConstants.FAILURE_KEY;
		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);
		UserEntry userEntry = userOptional.get();
		WebMasterEntry webMaster = webMasterEntryRepository.findByUserId(userEntry.getId());
		LoginDTO loginDTO = new LoginDTO();
		loginDTO.setSystemId(loginRequest.getUsername());
		loginDTO.setPassword(loginRequest.getPassword());
		loginDTO.setOtp(loginRequest.getOtp());
		String system_id = loginDTO.getSystemId();
		ProfessionEntry professionEntry = null;
		int otp = loginDTO.getOtp();
		boolean matched = false;
		try {
			logger.info(username + "[" + loginDTO.getAccessName() + "] Matching OTP Request[" + otp + " -> "
					+ loginDTO.getOtp() + "]");
			if (otp == loginDTO.getOtp()) {
				if (loginDTO.getAccessName() != null) {
					request.setAttribute("multiAccessUser", loginDTO.getAccessName());
				}
				logger.info(system_id + " Saving WebAccessLog Entry");
				SessionLogInsert.logQueue.enqueue(
						new AccessLogEntry(system_id, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
								loginDTO.getIpAddress(), 0, "login", null));
				matched = true;
				target = IConstants.SUCCESS_KEY;
				if (webMaster.isEmailOnLogin()) {
					if (webMaster.isEmailOnLogin()) {
						String to = IConstants.TO_EMAIl;
						String from = IConstants.SUPPORT_EMAIL[0];
						if (GlobalVars.UserMapping.containsKey(userEntry.getSystemId())) {
							int userid = GlobalVars.UserMapping.get(userEntry.getSystemId());
							professionEntry = GlobalVars.ProfessionEntries.get(userid);
						}
						if (professionEntry != null && professionEntry.getDomainEmail() != null
								&& professionEntry.getDomainEmail().length() > 0
								&& professionEntry.getDomainEmail().contains("@")
								&& professionEntry.getDomainEmail().contains(".")) {
							from = professionEntry.getDomainEmail();
							logger.info(system_id + " Domain-Email Found: " + from);
						} else {
							ProfessionEntry masterProfessionEntry = null;
							if (GlobalVars.UserMapping.containsKey(userEntry.getSystemId())) {
								int userid = GlobalVars.UserMapping.get(userEntry.getMasterId());
								masterProfessionEntry = GlobalVars.ProfessionEntries.get(userid);
							}
							if (masterProfessionEntry != null && masterProfessionEntry.getDomainEmail() != null
									&& masterProfessionEntry.getDomainEmail().length() > 0
									&& masterProfessionEntry.getDomainEmail().contains("@")
									&& masterProfessionEntry.getDomainEmail().contains(".")) {
								from = masterProfessionEntry.getDomainEmail();
								logger.info(userEntry.getMasterId() + " Master Domain-Email Found: " + from);
							} else {
								logger.info(system_id + " Domain-Email Not Found");
							}
						}
						if (webMaster.getEmail() != null && webMaster.getEmail().contains("@")
								&& webMaster.getEmail().contains(".")) {
							to = webMaster.getEmail();
						}
						String mailContent = new MailUtility().mailOnLoginContent(system_id, loginDTO.getIpAddress());
						try {
							MailUtility.send(to, mailContent, "Login alert", from, false);
							logger.error("login[" + system_id + "] Email Sent From:" + from + " To:" + to);
						} catch (Exception ex) {
							logger.error(system_id + " login email error", ex.fillInStackTrace());
						}
					}
				}
			} else {
				SessionLogInsert.logQueue.enqueue(
						new AccessLogEntry(system_id, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
								loginDTO.getIpAddress(), 0, "failed", "OTP mismatched"));
			}

			if (matched) {
				logger.info(system_id + " OTP Matched: " + otp);
			} else {
				if (purpose != null && purpose.equalsIgnoreCase("verify")) {
					target = "resetfail";
				}
				logger.error(system_id + " Invalid OTP Entered: " + otp);
				throw new InvalidOtpException("Invalid OTP Entered username: " + username);
			}
		} catch (Exception e) {
			target = IConstants.WEB_APPLICATION_ERROR;
			logger.error(system_id + " Login Error", e.fillInStackTrace());
			SessionLogInsert.logQueue.enqueue(
					new AccessLogEntry(system_id, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
							request.getRemoteAddr(), 0, "failed", "Error " + e.getMessage()));
			throw new InternalServerException(system_id + " Login Error" + e.getMessage());
		}
		logger.info(system_id + " OTP login target: " + target);
		JwtResponse jwtResponse = (JwtResponse) loginjwt(loginRequest, request, webMaster, professionEntry, loginDTO,
				userEntry).getBody();
		jwtResponse.setStatus(target);
		return ResponseEntity.ok(jwtResponse);
	}

	@Override
	public ResponseEntity<?> loginMultiUser(LoginRequest loginRequest, HttpServletRequest request, String purpose) {
		String username = loginRequest.getUsername();
		if (!userEntryRepository.existsBySystemId(username) && !salesRepository.existsByUsername(username)) {
			logger.error(messageResourceBundle.getLogMessage("auth.failed.userNotFound"), username);
			throw new AuthenticationExceptionFailed(
					messageResourceBundle.getExMessage(ConstantMessages.AUTHENTICATION_FAILED_USERNAME));
		}
		String target = IConstants.FAILURE_KEY;
		LoginDTO loginForm = new LoginDTO();
		loginForm.setSystemId(loginRequest.getUsername());
		loginForm.setPassword(loginRequest.getPassword());
		loginForm.setAccessName(loginRequest.getAccessName());
		String accessUser = loginForm.getAccessName();
		logger.info(loginForm.getSystemId() + " Access Login Requested By " + accessUser);
		try {
			Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(loginForm.getSystemId());
			UserEntry userEntry = userOptional.get();
			WebMasterEntry webMaster = webMasterEntryRepository.findByUserId(userEntry.getId());
			List<MultiUserEntry> accessUserList = multiUserEntryRepository.findByUserNative(userEntry.getId());
			boolean matched = false;
			for (MultiUserEntry multiUserEntry : accessUserList) {
				if (multiUserEntry.getAccessName().equals(accessUser)) {
					if (multiUserEntry.getMobile() != null && multiUserEntry.getMobile().length() > 0) {
						target = "otplogin";
						String valid_otp_numbers = "";
						for (String number : multiUserEntry.getMobile().split(",")) {
							logger.info(userEntry.getSystemId() + " OTP Number: " + number);
							try {
								Long.parseLong(number);
								valid_otp_numbers += number + ",";
							} catch (NumberFormatException ne) {
								logger.error(userEntry.getSystemId() + " Invalid OTP Number Configured: " + number);
							}
						}
						if (valid_otp_numbers.length() > 0) {
							valid_otp_numbers = valid_otp_numbers.substring(0, valid_otp_numbers.length() - 1);
							logger.info(userEntry.getSystemId() + " Valid OTP Numbers: " + valid_otp_numbers);
							// check otp exist & send to user in case of absent or expired
							int otp = new Random().nextInt(999999 - 100000) + 100000;
							java.util.Calendar calendar = java.util.Calendar.getInstance();
							int duration = 5;
							if (IConstants.LOGIN_OTP_VALIDITY > 0) {
								duration = IConstants.LOGIN_OTP_VALIDITY;
							}
							calendar.add(Calendar.MINUTE, duration);
							String validity = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
							OTPEntry otpEntry = otpEntryRepository.findBySystemId(username).get();
							if (otpEntry == null) {
								otpEntryRepository.save(new OTPEntry(userEntry.getSystemId(), otp, validity));
							} else {
								otpEntry.setExpiresOn(validity);
								otpEntry.setOneTimePass(otp);
								otpEntryRepository.save(otpEntry);
							}
							// --------------------- send to user ---------------
							UserEntry internalUser = this.userEntryRepository.findByRole("internal");
							DriverInfo driverInfo = driverInfoRepository.findById(internalUser.getId()).get();
							if (internalUser != null) {
								String content = null;
								try {
									content = MultiUtility.readContent(IConstants.FORMAT_DIR + "otp.txt");
								} catch (Exception ex) {
									logger.error("OTP_FORMAT", ex.fillInStackTrace());
								}
								if (content == null) {
									content = "hello [system_id], [otp_pass] is your One-Time Password (OTP) on [url] valid for next [duration] minutes";
								}
								content = content.replace("[system_id]", userEntry.getSystemId());
								content = content.replace("[otp_pass]", String.valueOf(otp));
								content = content.replace("[url]", IConstants.WebUrl);
								content = content.replace("[duration]", String.valueOf(duration));
								ArrayList<String> list = new ArrayList<String>(
										Arrays.asList(valid_otp_numbers.split(",")));
								BulkSmsDTO smsDTO = new BulkSmsDTO();
								smsDTO.setSystemId(internalUser.getSystemId());
								smsDTO.setPassword(
										new PasswordConverter().convertToEntityAttribute(driverInfo.getDriver()));
								// smsDTO.setMessageType("SpecialChar");
								smsDTO.setMessage(content);
								smsDTO.setDestinationList(list);
								// smsDTO.setFrom("Name");
								if (webMaster.getOtpSender() != null && webMaster.getOtpSender().length() > 1) {
									smsDTO.setSenderId(webMaster.getOtpSender());
								} else {
									smsDTO.setSenderId(IConstants.OTP_SENDER_ID);
								}
								String Response = MultiUtility.sendOtpSms(userEntry.getSystemId(), smsDTO, restTemplate)
										.toString();
								logger.info("<OTP SMS: " + Response + ">" + userEntry.getSystemId() + "<"
										+ valid_otp_numbers + ">");
								if (multiUserEntry.getEmail() != null && multiUserEntry.getEmail().length() > 0) {
									String from = IConstants.SUPPORT_EMAIL[0];
									ProfessionEntry professionEntry = null;
									if (GlobalVars.UserMapping.containsKey(userEntry.getSystemId())) {
										int userid = GlobalVars.UserMapping.get(userEntry.getSystemId());
										professionEntry = GlobalVars.ProfessionEntries.get(userid);
									}
									if (professionEntry.getDomainEmail() != null
											&& professionEntry.getDomainEmail().length() > 0
											&& professionEntry.getDomainEmail().contains("@")
											&& professionEntry.getDomainEmail().contains(".")) {
										from = professionEntry.getDomainEmail();
										logger.info(userEntry.getSystemId() + " Domain-Email Found: " + from);
									} else {
										String master = userEntry.getMasterId();
										ProfessionEntry professionEntryMaster = null;
										if (GlobalVars.UserMapping.containsKey(master)) {
											int userid = GlobalVars.UserMapping.get(master);
											professionEntryMaster = GlobalVars.ProfessionEntries.get(userid);
										}
										if (professionEntryMaster != null
												&& professionEntryMaster.getDomainEmail() != null
												&& professionEntryMaster.getDomainEmail().length() > 0
												&& professionEntryMaster.getDomainEmail().contains("@")
												&& professionEntryMaster.getDomainEmail().contains(".")) {
											from = professionEntryMaster.getDomainEmail();
											logger.info(
													userEntry.getSystemId() + " Master Domain-Email Found: " + from);
										} else {
											logger.info(userEntry.getSystemId() + " Domain-Email Not Found");
										}
									}
									logger.info(userEntry.getSystemId() + " Sending OTP Email From[" + from + "] on: "
											+ multiUserEntry.getEmail());
									try {
										MailUtility.send(from, multiUserEntry.getEmail(), "OTP Password", content);
									} catch (Exception e) {
										logger.error(userEntry.getSystemId() + " OTP Email Sending Error:" + e);
									}
								}
								loginForm.setOtp(otp);
								loginForm.setAccessName(accessUser);
								loginForm.setAccessMobile(valid_otp_numbers);
								request.getSession().setAttribute("loginEntry", loginForm);
								ResponseEntity
										.ok("An OTP has been sent to your registered number. Please Enter to verify");
							} else {
								logger.error("Internal User Not Configured For alerts.");
								target = "webAccessError";
								SessionLogInsert.logQueue.enqueue(new AccessLogEntry(loginForm.getSystemId(),
										new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
										loginForm.getIpAddress(), 0, "failed", "internal user missing"));
								throw new UnauthorizedException("Internal User Not Configured For alerts");
							}
						} else {
							target = "webAccessError";
							SessionLogInsert.logQueue.enqueue(new AccessLogEntry(loginForm.getSystemId(),
									new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
									loginForm.getIpAddress(), 0, "failed", "missing OTP numbers"));
							throw new NotFoundException(" No Valid Number Found For OTP");
						}
					} else {
						logger.error(userEntry.getSystemId() + " OTP Number Not Configured.");
						target = "webAccessError";
						SessionLogInsert.logQueue.enqueue(new AccessLogEntry(loginForm.getSystemId(),
								new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
								loginForm.getIpAddress(), 0, "failed", "missing OTP numbers"));
						throw new InternalServerException(userEntry.getSystemId() + " OTP Number Not Configured.");
					}
					matched = true;
					break;
				}
			}
			if (!matched) {
				logger.error(loginForm.getSystemId() + " mismatched access user");
				target = "invalidRequest";
			}
		} catch (Exception e) {
			target = IConstants.WEB_APPLICATION_ERROR;
			logger.error("Login Error: " + accessUser, e.fillInStackTrace());
			SessionLogInsert.logQueue.enqueue(new AccessLogEntry(loginForm.getSystemId(),
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), request.getRemoteAddr(), 0,
					"failed", "Error " + e.getMessage()));
		}
		return ResponseEntity.ok(target);

	}

	@Override
	public ResponseEntity<?> loginskip(LoginRequest loginRequest, HttpServletRequest request, String purpose) {
		String username = loginRequest.getUsername();
		if (!userEntryRepository.existsBySystemId(username) && !salesRepository.existsByUsername(username)) {
			logger.error(messageResourceBundle.getLogMessage("auth.failed.userNotFound"), username);
			throw new AuthenticationExceptionFailed(
					messageResourceBundle.getExMessage(ConstantMessages.AUTHENTICATION_FAILED_USERNAME));
		}
		String target = IConstants.FAILURE_KEY;
		LoginDTO loginDTO = new LoginDTO();
		loginDTO.setSystemId(loginRequest.getUsername());
		loginDTO.setPassword(loginRequest.getPassword());
		UserEntry userEntry = null;
		WebMasterEntry webMaster = null;
		ProfessionEntry professionEntry = null;
		try {
			Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(loginDTO.getSystemId());
			userEntry = userOptional.get();
			webMaster = webMasterEntryRepository.findByUserId(userEntry.getId());
			if (webMaster.getOtpNumber() != null && webMaster.getOtpNumber().length() > 0) {
				target = "otplogin";
				String valid_otp_numbers = "";
				for (String number : webMaster.getOtpNumber().split(",")) {
					logger.info(userEntry.getSystemId() + " OTP Number: " + number);
					try {
						Long.parseLong(number);
						valid_otp_numbers += number + ",";
					} catch (NumberFormatException ne) {
						logger.error(userEntry.getSystemId() + " Invalid OTP Number Configured: " + number);
					}
				}
				if (valid_otp_numbers.length() > 0) {
					int otp = 0;
					valid_otp_numbers = valid_otp_numbers.substring(0, valid_otp_numbers.length() - 1);
					logger.info(userEntry.getSystemId() + " Valid OTP Numbers: " + valid_otp_numbers);
					// check otp exist & send to user in case of absent or expired
					OTPEntry otpEntry = otpEntryRepository.findBySystemId(userEntry.getSystemId()).get();
					boolean generate_otp = true;
					if (otpEntry != null) {
						if (otpEntry.getExpiresOn() != null) {
							if (new Date().after(
									new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(otpEntry.getExpiresOn()))) {
								logger.info(userEntry.getSystemId() + " OTP ExpiredOn: " + otpEntry.getExpiresOn());
							} else {
								generate_otp = false;
								otp = otpEntry.getOneTimePass();
							}
						}
					}
					if (generate_otp) {
						otp = new Random().nextInt(999999 - 100000) + 100000;
						java.util.Calendar calendar = java.util.Calendar.getInstance();
						int duration = 5;
						if (IConstants.LOGIN_OTP_VALIDITY > 0) {
							duration = IConstants.LOGIN_OTP_VALIDITY;
						}
						calendar.add(Calendar.MINUTE, duration);
						String validity = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
						if (otpEntry != null) {
							otpEntry.setExpiresOn(validity);
							otpEntry.setOneTimePass(otp);
							otpEntryRepository.save(otpEntry);
						} else {
							otpEntryRepository.save(new OTPEntry(userEntry.getSystemId(), otp, validity));
						}
						// --------------------- send to user ---------------
						UserEntry internalUser = this.userEntryRepository.findByRole("internal");
						DriverInfo driverInfo = driverInfoRepository.findById(internalUser.getId()).get();
						if (internalUser != null) {
							String content = null;
							try {
								content = MultiUtility.readContent(IConstants.FORMAT_DIR + "otp.txt");
							} catch (Exception ex) {
								logger.error("OTP_FORMAT", ex.fillInStackTrace());
							}
							if (content == null) {
								content = "hello [system_id], [otp_pass] is your One-Time Password (OTP) on [url] valid for next [duration] minutes";
							}
							content = content.replace("[system_id]", userEntry.getSystemId());
							content = content.replace("[otp_pass]", String.valueOf(otp));
							content = content.replace("[url]", IConstants.WebUrl);
							content = content.replace("[duration]", String.valueOf(duration));
							ArrayList<String> list = new ArrayList<String>(Arrays.asList(valid_otp_numbers.split(",")));
							BulkSmsDTO smsDTO = new BulkSmsDTO();
							smsDTO.setSystemId(internalUser.getSystemId());
							smsDTO.setPassword(
									new PasswordConverter().convertToEntityAttribute(driverInfo.getDriver()));
							// smsDTO.setMessageType("SpecialChar");
							smsDTO.setMessage(content);
							smsDTO.setDestinationList(list);
							// smsDTO.setFrom("Name");
							if (webMaster.getOtpSender() != null && webMaster.getOtpSender().length() > 1) {
								smsDTO.setSenderId(webMaster.getOtpSender());
							} else {
								smsDTO.setSenderId(IConstants.OTP_SENDER_ID);
							}
							String Response = MultiUtility.sendOtpSms(userEntry.getSystemId(), smsDTO, restTemplate)
									.toString();
							logger.info("<OTP SMS: " + Response + ">" + userEntry.getSystemId() + "<"
									+ valid_otp_numbers + ">");
							if (webMaster.getOtpEmail() != null && webMaster.getOtpEmail().length() > 0) {
								String from = IConstants.SUPPORT_EMAIL[0];
								if (GlobalVars.UserMapping.containsKey(userEntry.getSystemId())) {
									int userid = GlobalVars.UserMapping.get(userEntry.getSystemId());
									professionEntry = GlobalVars.ProfessionEntries.get(userid);
								}
								if (professionEntry.getDomainEmail() != null
										&& professionEntry.getDomainEmail().length() > 0
										&& professionEntry.getDomainEmail().contains("@")
										&& professionEntry.getDomainEmail().contains(".")) {
									from = professionEntry.getDomainEmail();
									logger.info(userEntry.getSystemId() + " Domain-Email Found: " + from);
								} else {
									String master = userEntry.getMasterId();
									ProfessionEntry professionEntryMaster = null;
									if (GlobalVars.UserMapping.containsKey(master)) {
										int userid = GlobalVars.UserMapping.get(master);
										professionEntryMaster = GlobalVars.ProfessionEntries.get(userid);
									}
									if (professionEntryMaster != null && professionEntryMaster.getDomainEmail() != null
											&& professionEntryMaster.getDomainEmail().length() > 0
											&& professionEntryMaster.getDomainEmail().contains("@")
											&& professionEntryMaster.getDomainEmail().contains(".")) {
										from = professionEntryMaster.getDomainEmail();
										logger.info(userEntry.getSystemId() + " Master Domain-Email Found: " + from);
									} else {
										logger.info(userEntry.getSystemId() + " Domain-Email Not Found");
									}
								}
								logger.info(userEntry.getSystemId() + " Sending OTP Email From[" + from + "] on: "
										+ webMaster.getOtpEmail());
								try {
									MailUtility.send(from, webMaster.getOtpEmail(), "OTP Password", content);
								} catch (Exception e) {
									logger.error(userEntry.getSystemId() + " OTP Email Sending Error:" + e);
								}
							}
							loginDTO.setOtp(otp);
							request.getSession().setAttribute("loginEntry", loginDTO);
							return ResponseEntity
									.ok("An OTP has been sent to your registered number. Please Enter to verify");
						} else {
							logger.error("Internal User Not Configured For alerts.");
							target = "webAccessError";
							SessionLogInsert.logQueue.enqueue(new AccessLogEntry(loginDTO.getSystemId(),
									new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
									loginDTO.getIpAddress(), 0, "failed", "internal user missing"));
							throw new InternalServerException("Sorry !! No User Found To Proceed");
						}
					} else {
						logger.info(userEntry.getSystemId() + " Login Request Via Recent OTP");
						loginDTO.setOtp(otp);
						request.getSession().setAttribute("loginEntry", loginDTO);
						ResponseEntity.ok("Please Enter Recent OTP to proceed ");
					}
				} else {
					target = "webAccessError";
					SessionLogInsert.logQueue.enqueue(new AccessLogEntry(loginDTO.getSystemId(),
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), loginDTO.getIpAddress(), 0,
							"failed", "missing OTP numbers"));
					throw new NotFoundException("No Valid Number Found For OTP");
				}
			} else {
				logger.error(userEntry.getSystemId() + " OTP Number Not Configured.");
				target = "webAccessError";
				SessionLogInsert.logQueue.enqueue(new AccessLogEntry(loginDTO.getSystemId(),
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), loginDTO.getIpAddress(), 0,
						"failed", "missing OTP numbers"));
				throw new NotFoundException("No Valid Number Found For OTP");
			}
		} catch (Exception e) {
			target = IConstants.WEB_APPLICATION_ERROR;
			logger.error("Login Error: " + loginDTO.getSystemId(), e.fillInStackTrace());
			SessionLogInsert.logQueue.enqueue(new AccessLogEntry(loginDTO.getSystemId(),
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), request.getRemoteAddr(), 0,
					"failed", "Error " + e.getMessage()));
		}

		JwtResponse jwtResponse = (JwtResponse) loginjwt(loginRequest, request, webMaster, professionEntry, loginDTO,
				userEntry).getBody();
		jwtResponse.setStatus(target);
		return ResponseEntity.ok(jwtResponse);

	}

}
