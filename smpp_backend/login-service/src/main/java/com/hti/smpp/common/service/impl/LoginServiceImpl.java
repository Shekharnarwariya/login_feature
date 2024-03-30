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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.hazelcast.map.IMap;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.PredicateBuilderImpl;

import com.hti.smpp.common.contacts.dto.GroupEntry;
import com.hti.smpp.common.email.EmailSender;
import com.hti.smpp.common.exception.AuthenticationExceptionFailed;
import com.hti.smpp.common.exception.DuplicateEntryException;
import com.hti.smpp.common.exception.EntryNotFoundException;
import com.hti.smpp.common.exception.InsufficientBalanceException;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.InvalidOtpException;
import com.hti.smpp.common.exception.InvalidPropertyException;
import com.hti.smpp.common.exception.NoMultiUserFoundException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.ProcessErrorException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.jwt.JwtUtils;
import com.hti.smpp.common.messages.dto.BulkSmsDTO;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.request.LoginDTO;
import com.hti.smpp.common.request.LoginRequest;
import com.hti.smpp.common.request.MultiUserForm;
import com.hti.smpp.common.request.PasswordUpdateRequest;
import com.hti.smpp.common.request.ProfileUpdateRequest;
import com.hti.smpp.common.request.SignupRequest;
import com.hti.smpp.common.request.UserEntryForm;
import com.hti.smpp.common.request.UserLimitForm;
import com.hti.smpp.common.response.JwtResponse;
import com.hti.smpp.common.response.LoginResponse;
import com.hti.smpp.common.response.MultiUserResponse;
import com.hti.smpp.common.response.ProfileResponse;
import com.hti.smpp.common.route.dto.HlrRouteEntry;
import com.hti.smpp.common.route.dto.MmsRouteEntry;
import com.hti.smpp.common.route.dto.OptionalRouteEntry;
import com.hti.smpp.common.route.dto.RouteEntry;
import com.hti.smpp.common.route.dto.RouteEntryExt;

import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.sales.repository.SalesRepository;
import com.hti.smpp.common.security.BCryptPasswordEncoder;
import com.hti.smpp.common.service.LoginService;
import com.hti.smpp.common.smsc.dto.SmscEntry;
import com.hti.smpp.common.user.dto.AccessLogEntry;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.DriverInfo;
import com.hti.smpp.common.user.dto.MultiUserEntry;
import com.hti.smpp.common.user.dto.OTPEntry;
import com.hti.smpp.common.user.dto.ProfessionEntry;
import com.hti.smpp.common.user.dto.RechargeEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.UserSessionObject;
import com.hti.smpp.common.user.dto.UserlimitEntry;
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
import com.hti.smpp.common.util.FlagStatus;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MailUtility;
import com.hti.smpp.common.util.MessageResourceBundle;
import com.hti.smpp.common.util.MultiUtility;
import com.hti.smpp.common.util.OTPGenerator;
import com.hti.smpp.common.util.PasswordConverter;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

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

	@Autowired
	private RestTemplate restTemplate;

	private final SessionFactory sessionFactory = null;

	@Autowired
	private DataSource dataSource;

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

			if (user.getRole().equalsIgnoreCase("user")) {
				if (signUpRequest.getBalance().getWalletFlag().equalsIgnoreCase("No")) {
					signUpRequest.getWebMasterEntry().setRouteMargin(null);
				}
				int master_id = 0;
				Collection<RouteEntryExt> master_routing = listRouteEntries(master_id, true, true, false).values();
				List<RouteEntryExt> user_routing = new ArrayList<RouteEntryExt>();
				user_routing.addAll(master_routing);
				boolean isPercent = false;
				double margin = 0;
				int generatedId = 0;
				if (signUpRequest.getWebMasterEntry().getRouteMargin() != null) {
					if (signUpRequest.getWebMasterEntry().getRouteMargin().length() > 0) {
						String margin_str = signUpRequest.getWebMasterEntry().getRouteMargin();
						if (margin_str.contains("%")) {
							isPercent = true;
							margin_str = margin_str.substring(0, margin_str.indexOf("%")).trim();
						}
						try {
							margin = Double.parseDouble(margin_str);
						} catch (Exception ex) {
							// logger.info(userSessionObject.getSystemId() + "Invalid margin: " +
							// margin_str);
							signUpRequest.getWebMasterEntry().setRouteMargin(null);
						}
					} else {
						signUpRequest.getWebMasterEntry().setRouteMargin(null);
					}
				}
				for (RouteEntryExt ext : user_routing) {
					ext.getBasic().setUserId(generatedId);
					ext.getBasic().setId(0);
					ext.getBasic().setEditBy(user.getSystemId());
					ext.getBasic().setEditOn(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
					ext.getHlrRouteEntry().setRouteId(0);
					ext.getRouteOptEntry().setRouteId(0);
					if (signUpRequest.getBalance().getWalletFlag().equalsIgnoreCase("No")) {
						ext.getBasic().setCost(0);
						ext.getHlrRouteEntry().setCost(0);
					} else {
						if (margin > 0) {
							double cost = ext.getBasic().getCost();
							if (isPercent) {
								cost = cost + ((cost * margin) / 100);
							} else {
								cost = cost + margin;
							}
							ext.getBasic().setCost(cost);
						}
					}
					ext.setMmsRouteEntry(new MmsRouteEntry(user.getSystemId(),
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
				}
//				logger.info(user.getSystemId() + "[" + generatedId + "] Master Routing Entries: "
//						+ user_routing.size());
				saveEntries(user_routing);
			} else {
				signUpRequest.getWebMasterEntry().setRouteMargin(null);
				signUpRequest.getWebMasterEntry().setAutoCopyRouting(false);
				int generatedId = 0;
				RouteEntry routeEntry = new RouteEntry(generatedId, user.getSystemId(),
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), "default");
				saveDefaultEntries(routeEntry);
				// logger.info(user.getSystemId() + "[" + generatedId + "] Default Routing
				// Added");
			}

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

	public Map<Integer, RouteEntryExt> listRouteEntries(int userId, boolean hlr, boolean optional, boolean display) {
		logger.info("listing RouteEntries For " + userId + " hlr: " + hlr + " optional: " + optional);
		Map<Integer, RouteEntryExt> list = new HashMap<Integer, RouteEntryExt>();
		Predicate<Integer, RouteEntry> p = new PredicateBuilderImpl().getEntryObject().get("userId").equal(userId);
		Map<Integer, String> smsc_name_mapping = null;
		Map<Integer, String> group_name_mapping = null;
		if (display) {
			smsc_name_mapping = listNames();
			group_name_mapping = listGroupNames();
		}
		for (RouteEntry basic : GlobalVars.BasicRouteEntries.values(p)) {
			RouteEntryExt entry = new RouteEntryExt(basic);
			if (display) {
				// ------ set user values -----------------
				if (GlobalVars.UserEntries.containsKey(entry.getBasic().getUserId())) {
					entry.setSystemId(GlobalVars.UserEntries.get(entry.getBasic().getUserId()).getSystemId());
					entry.setMasterId(GlobalVars.UserEntries.get(entry.getBasic().getUserId()).getMasterId());
					entry.setCurrency(GlobalVars.UserEntries.get(entry.getBasic().getUserId()).getCurrency());
					entry.setAccountType(GlobalVars.WebmasterEntries.get(basic.getUserId()).getAccountType());
				}
				// ------ set network values -----------------
				// NetworkEntry network = CacheService.getNetworkEntry(entry.getNetworkId());
				if (GlobalVars.NetworkEntries.containsKey(entry.getBasic().getNetworkId())) {
					NetworkEntry network = GlobalVars.NetworkEntries.get(entry.getBasic().getNetworkId());
					entry.setCountry(network.getCountry());
					entry.setOperator(network.getOperator());
					entry.setMcc(network.getMcc());
					entry.setMnc(network.getMnc());
				}
				// ------ set Smsc values -----------------
				if (entry.getBasic().getSmscId() == 0) {
					entry.setSmsc("Down");
				} else {
					if (smsc_name_mapping.containsKey(entry.getBasic().getSmscId())) {
						entry.setSmsc(smsc_name_mapping.get(entry.getBasic().getSmscId()));
					}
				}
				if (group_name_mapping.containsKey(entry.getBasic().getGroupId())) {
					entry.setGroup(group_name_mapping.get(entry.getBasic().getGroupId()));
				}
			}
			if (hlr) {
				if (GlobalVars.HlrRouteEntries.containsKey(basic.getId())) {
					entry.setHlrRouteEntry(GlobalVars.HlrRouteEntries.get(basic.getId()));
				} else {
					logger.info(basic.getId() + " Hlr Entry Not Found For: " + userId);
				}
			}
			if (optional) {
				if (GlobalVars.OptionalRouteEntries.containsKey(basic.getId())) {
					OptionalRouteEntry optEntry = GlobalVars.OptionalRouteEntries.get(basic.getId());
					// OptionalRouteEntryExt optEntryExt = new OptionalRouteEntryExt(optEntry);
					if (display) {
						if (optEntry.getNumSmscId() == 0) {
							entry.setNumSmsc("Down");
						} else {
							if (smsc_name_mapping.containsKey(optEntry.getNumSmscId())) {
								entry.setNumSmsc(smsc_name_mapping.get(optEntry.getNumSmscId()));
							}
						}
						if (optEntry.getBackupSmscId() == 0) {
							entry.setBackupSmsc("Down");
						} else {
							if (smsc_name_mapping.containsKey(optEntry.getBackupSmscId())) {
								entry.setBackupSmsc(smsc_name_mapping.get(optEntry.getBackupSmscId()));
							}
						}
						if (optEntry.getRegSmscId() == 0) {
							entry.setRegSmsc("Down");
						} else {
							if (smsc_name_mapping.containsKey(optEntry.getRegSmscId())) {
								entry.setRegSmsc(smsc_name_mapping.get(optEntry.getRegSmscId()));
							}
						}
						// ----------- Configure Content Replacement ----------------------
						if (optEntry.isReplaceContent()) {
							Map<String, String> content_map = optEntry.getReplaceContentMap();
							String converted_text = "";
							for (Map.Entry<String, String> map_entry : content_map.entrySet()) {
								converted_text += map_entry.getKey() + "|" + map_entry.getValue() + ",";
							}
							if (converted_text.length() > 0) {
								converted_text = converted_text.substring(0, converted_text.length() - 1);
								optEntry.setReplacement(converted_text);
							}
						}
					}
					entry.setRouteOptEntry(optEntry);
					// --------------------------------------
				} else {
					logger.info(basic.getId() + " Optional Entry Not Found For: " + userId);
				}
			}
			list.put(basic.getId(), entry);
		}
		if (list.isEmpty()) {
			logger.info("Routing Entries Not Found For " + userId);
		} else {
			logger.info(userId + " RouteEntries: " + list.size());
		}
		return list;
	}

	public Map<Integer, String> listGroupNames() {
		Map<Integer, String> names = new HashMap<Integer, String>();
		names.put(0, "NONE");
		List<GroupEntry> groups = listGroup();
		for (GroupEntry entry : groups) {
			names.put(entry.getId(), entry.getName());
		}
		names = names.entrySet().stream().sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return names;
	}

	public List<GroupEntry> listGroup() {
		try (Session session = sessionFactory.openSession()) {
			Transaction transaction = session.beginTransaction();
			logger.info("Listing Group Entries");

			try {
				org.hibernate.query.Query<GroupEntry> query = session.createQuery("FROM GroupEntry", GroupEntry.class);
				List<GroupEntry> groupEntries = query.getResultList();
				logger.info("GroupEntry list size: {}", groupEntries.size());
				transaction.commit();
				return groupEntries;
			} catch (Exception e) {
				if (transaction != null) {
					transaction.rollback();
				}
				throw e;
			}
		}
	}

	public Map<Integer, String> listNames() {
		Map<Integer, String> names = new HashMap<Integer, String>();
		for (SmscEntry entry : GlobalVars.SmscEntries.values()) {
			names.put(entry.getId(), entry.getName());
		}
		names = names.entrySet().stream().sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return names;
	}

	public void saveEntries(List<RouteEntryExt> list) {
		for (RouteEntryExt entry : list) {
			try {
				saveRouteEntry(entry);
			} catch (Exception ex) {
				logger.error(ex + ":-> " + entry.toString());
			}
		}
	}

	public void saveDefaultEntries(RouteEntry defaultEntry) {
		RouteEntryExt defaultEntryExt = new RouteEntryExt(defaultEntry);
		defaultEntryExt.setHlrRouteEntry(
				new com.hti.smpp.common.route.dto.HlrRouteEntry(defaultEntry.getEditBy(), defaultEntry.getEditOn()));
		defaultEntryExt.setRouteOptEntry(new com.hti.smpp.common.route.dto.OptionalRouteEntry(defaultEntry.getEditBy(),
				defaultEntry.getEditOn()));
		defaultEntryExt.setMmsRouteEntry(new MmsRouteEntry(defaultEntry.getEditBy(), defaultEntry.getEditOn()));
		saveRouteEntry(defaultEntryExt);
	}

	public void saveRouteEntry(RouteEntryExt entry) {
		RouteEntry basic = entry.getBasic();
		HlrRouteEntry hlr = entry.getHlrRouteEntry();
		OptionalRouteEntry opt = entry.getRouteOptEntry();
		MmsRouteEntry mms = entry.getMmsRouteEntry();
		try (Session session = sessionFactory.openSession()) {
			Transaction transaction = session.beginTransaction();

			try {
				logger.info("Saving RouteEntry: {}", entry);
				session.save(entry.getBasic());
				session.save(entry.getHlrRouteEntry());
				session.save(entry.getRouteOptEntry());
				session.save(entry.getMmsRouteEntry());
				transaction.commit();
				logger.info("RouteEntry saved successfully: {}", entry);
			} catch (Exception e) {
				transaction.rollback();
				throw new Exception("Error saving RouteEntry: " + e.getMessage(), e);
			}
		} catch (Exception e) {
			throw new RuntimeException("Error occurred during session handling: " + e.getMessage(), e);
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
				// Generate OTP
				String generateOTP = null;
				// Set OTP Secret Key for User
				UserEntry user = userOptional.get();
				Optional<OTPEntry> optionalOTP = otpEntryRepository.findBySystemId(username);
				if (optionalOTP.isPresent()) {
					// If OTP exists, check if it's expired
					OTPEntry existingOTP = optionalOTP.get();
					LocalTime expiresOn = LocalTime.parse(existingOTP.getExpiresOn());
					if (expiresOn.isAfter(LocalTime.now())) {
						// OTP not expired, use the existing one
						generateOTP = String.valueOf(existingOTP.getOneTimePass());
					} else {
						// OTP expired, generate a new one
						generateOTP = OTPGenerator.generateOTP(6);
						existingOTP.setOneTimePass(Integer.parseInt(generateOTP));
						existingOTP.setExpiresOn(LocalTime.now() + "");
						otpEntryRepository.save(existingOTP);
					}
				} else {
					// No OTP exists, generate a new one
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

				// Send Email with OTP
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
			String regID, MultipartFile profileImageFile) {
		Optional<UserEntry> optionalUser = userEntryRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			UserEntry user = optionalUser.get();
			ProfessionEntry professionEntry = professionEntryRepository.findById(user.getId())
					.orElseThrow(() -> new NotFoundException(
							messageResourceBundle.getExMessage(ConstantMessages.PROFESSION_ENTRY_ERROR)));
			updateUserData(user, email, firstName, lastName, contact, companyName, designation, city, country, state,
					keepLogs, referenceID, companyAddress, companyEmail, notes, taxID, regID, professionEntry,
					profileImageFile);
			user.setEditOn(LocalDateTime.now() + "");
			user.setEditBy(username);
			user.setLogDays(Integer.parseInt(keepLogs));
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

// -----------------------------------------------------------------------------------------------------------------------

	private UserEntry getUser(String username) {

		UserEntry userEntry = null;
		Optional<UserEntry> user = this.userEntryRepository.findBySystemId(username);
		if (user.isPresent()) {
			userEntry = user.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}

		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}
		return userEntry;
	}

	private WebMasterEntry getWebMasterEntry(int userId) {
		WebMasterEntry webEntry = null;
		webEntry = this.webMasterEntryRepository.findByUserId(userId);
		if (webEntry != null) {
			return webEntry;
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NOT_FOUND_WEBMASTER_ERROR));
		}
	}

	private ProfessionEntry getProfessionEntry(int userId) {
		ProfessionEntry proEntry = this.professionEntryRepository.findByUserId(userId)
				.orElseThrow(() -> new NotFoundException(
						messageResourceBundle.getExMessage(ConstantMessages.PROFESSION_ENTRY_ERROR)));
		return proEntry;
	}

	@Override
	public ResponseEntity<?> validateUserIpAccess(LoginRequest loginRequest, String language) {

		// user validation
		int userId = 0;
		String systemId = loginRequest.getUsername();
		String password = encoder.encode(loginRequest.getPassword());
		UserEntry userEntry = getUser(loginRequest.getUsername());
		if (userEntry.getPassword().equals(password)) {
			userId = userEntry.getId();
		} else {
			logger.error(messageResourceBundle.getLogMessage("auth.failed.password"), systemId);
			userId = 0;
		}

		boolean webAccess = true;
		String ipaddress = getClientIp();
		if (userId > 0) {
			WebMasterEntry webEntry = getWebMasterEntry(userId);
			if (webEntry != null) {
				if (webEntry.isWebAccess()) {
					boolean isExpired = false;
					try {
						isExpired = new SimpleDateFormat("yyyy-MM-dd").parse(userEntry.getExpiry())
								.before(new java.util.Date());
					} catch (ParseException ex) {
						logger.error(userEntry.getSystemId(), ex.fillInStackTrace());
						throw new InternalServerException(
								messageResourceBundle.getExMessage(ConstantMessages.DATE_PARSE_ERROR));
					}
					boolean isPasswordExpired = false;
					if (userEntry.isForcePasswordChange()) {
						try {
							isPasswordExpired = new SimpleDateFormat("yyyy-MM-dd")
									.parse(userEntry.getPasswordExpiresOn()).before(new java.util.Date());
						} catch (ParseException ex) {
							logger.error(userEntry.getSystemId(), ex.fillInStackTrace());
							throw new InternalServerException(
									messageResourceBundle.getExMessage(ConstantMessages.PASSWORD_EXPIRES_PARSE_ERROR));
						}
					}
					if (isExpired) {
						logger.error(messageResourceBundle.getLogMessage("user.account.expired"),
								userEntry.getSystemId());
						this.accessLog.save(new AccessLogEntry(userEntry.getSystemId(),
								new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0, "failed",
								"Account Expired"));

						throw new InternalServerException(messageResourceBundle.getExMessage(
								ConstantMessages.ACCOUNT_EXPIRED, new Object[] { userEntry.getSystemId() }));
					} else if (isPasswordExpired) {
						logger.error(messageResourceBundle.getLogMessage("user.password.expired"),
								userEntry.getSystemId());
						this.accessLog.save(new AccessLogEntry(userEntry.getSystemId(),
								new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0, "failed",
								"Password Expired"));
						throw new InternalServerException(messageResourceBundle.getExMessage(
								ConstantMessages.PASSWORD_EXPIRED, new Object[] { userEntry.getSystemId() }));
					} else {
						IMap<String, String> user_flag_status = GlobalVars.user_flag_status;
						String flagValue = user_flag_status.get(userEntry.getSystemId());
						if (flagValue != null) {
							if (flagValue.contains("404")) {
								webAccess = false;
								logger.error(messageResourceBundle.getLogMessage("user.account.blocked"),
										userEntry.getSystemId());
								this.accessLog.save(new AccessLogEntry(userEntry.getSystemId(),
										new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0,
										"failed", "Account Blocked"));
								throw new InternalServerException(messageResourceBundle.getExMessage(
										ConstantMessages.ACCOUNT_BLOCKED, new Object[] { userEntry.getSystemId() }));
							} else {
								logger.info(messageResourceBundle.getLogMessage("user.flag.value"),
										userEntry.getSystemId(), flagValue);
							}
						} else {
							logger.info(messageResourceBundle.getLogMessage("user.flag.read.error"),
									userEntry.getSystemId());
							webAccess = false;
							this.accessLog.save(new AccessLogEntry(userEntry.getSystemId(),
									new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0,
									"failed", "Flag Read Error"));
							throw new InternalServerException(
									messageResourceBundle.getExMessage(ConstantMessages.FLAG_READ_ERROR));
						}
						if (webAccess) {
							logger.info(messageResourceBundle.getLogMessage("user.check.ipaddress"),
									userEntry.getSystemId());
							if (Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {

								if (ipaddress != null && !ipaddress.isEmpty()) {
									boolean matched = false;
									if (ipaddress.equalsIgnoreCase("0:0:0:0:0:0:0:1")
											|| ipaddress.equalsIgnoreCase("127.0.0.1")) {
										logger.info(messageResourceBundle.getLogMessage("user.local.ip.match"),
												userEntry.getSystemId(), ipaddress);
										matched = true;
									} else {

										logger.info(
												messageResourceBundle
														.getLogMessage("user.matching.super.access.ip.info"),
												userEntry.getSystemId(), ipaddress);

										String[] IPs = IConstants.AccessIP.split(",");
										for (String allowedip : IPs) {
											if (allowedip.indexOf("/") > 0 && isInRange(allowedip, ipaddress)) {
												logger.info(
														messageResourceBundle.getLogMessage("user.range.matched.info"),
														userEntry.getSystemId(), allowedip, ipaddress);
												matched = true;
												break;
											} else {
												if (ipaddress.equalsIgnoreCase(allowedip)) {
													logger.info(
															messageResourceBundle
																	.getLogMessage("user.configured.ip.matched.info"),
															userEntry.getSystemId(), allowedip, ipaddress);
													matched = true;
													break;
												}
											}

										}
										if (!matched) {

											logger.info(
													messageResourceBundle
															.getLogMessage("user.matching.global.access.ip.info"),
													userEntry.getSystemId(), ipaddress);
											IPs = IConstants.GLOBAl_ACCESS_IP.split(",");
											for (String allowedip : IPs) {
												if (allowedip.indexOf("/") > 0 && isInRange(allowedip, ipaddress)) {
													logger.info(
															messageResourceBundle
																	.getLogMessage("user.range.matched.info"),
															userEntry.getSystemId(), allowedip, ipaddress);
													matched = true;
													break;
												} else {
													if (ipaddress.equalsIgnoreCase(allowedip)) {
														logger.info(
																messageResourceBundle.getLogMessage(
																		"user.configured.ip.matched.info"),
																userEntry.getSystemId(), allowedip, ipaddress);
														matched = true;
														break;
													}
												}
											}
										}
									}
									if (!matched) {
										logger.info(
												messageResourceBundle.getLogMessage("user.access.ip.not.matched.info"),
												userEntry.getSystemId(), ipaddress);
										if (userEntry.getAccessCountry() != null
												&& userEntry.getAccessCountry().length() > 0) {

											logger.info(
													messageResourceBundle
															.getLogMessage("user.matching.allowed.country.info"),
													userEntry.getSystemId(), ipaddress);
											String country = getCountryname(ipaddress);
											if (country != null && !country.isEmpty()) {

												logger.info(
														messageResourceBundle.getLogMessage("user.country.found.info"),
														userEntry.getSystemId(), ipaddress, country);

												for (String allowedCountry : userEntry.getAccessCountry().split(",")) {
													if (allowedCountry.equalsIgnoreCase(country)) {
														matched = true;
														break;
													}
												}
											} else {
												logger.info(
														messageResourceBundle
																.getLogMessage("user.country.not.found.info"),
														userEntry.getSystemId(), ipaddress);
											}
											if (!matched) {
												webAccess = false;
												logger.info(
														messageResourceBundle
																.getLogMessage("user.access.ip.not.allowed.info"),
														userEntry.getSystemId(), ipaddress);
												throw new InternalServerException(messageResourceBundle.getExMessage(
														ConstantMessages.USER_ACCESS_IPNOTALLOWED,
														new Object[] { userEntry.getSystemId(), ipaddress }));
											} else {
												logger.info(
														messageResourceBundle
																.getLogMessage("user.valid.access.country.info"),
														userEntry.getSystemId(), ipaddress);
											}

										} else {
											webAccess = false;
											logger.info(
													messageResourceBundle
															.getLogMessage("user.access.countries.not.configured.info"),
													userEntry.getSystemId());
//											throw new InternalServerException(messageResourceBundle.getExMessage(
//													ConstantMessages.ACCESS_COUNTRY_NOTCONFIGURED,
//													new Object[] { userEntry.getSystemId() }));
										}
									} else {
										logger.info(messageResourceBundle.getLogMessage("user.valid.access.ip.info"),
												userEntry.getSystemId(), ipaddress);
									}

								} else {
									throw new NotFoundException(
											messageResourceBundle.getExMessage(ConstantMessages.IPADDRESS_NOTFOUND));
								}

							} else {

								System.out.println("****************876");
								if (userEntry.getAccessIp() != null && userEntry.getAccessIp().length() > 0) {
									boolean matched = false;
									if (ipaddress != null && !ipaddress.isEmpty()) {
										if (ipaddress.equalsIgnoreCase("0:0:0:0:0:0:0:1")
												|| ipaddress.equalsIgnoreCase("127.0.0.1")) {

											logger.info(messageResourceBundle.getLogMessage("user.local.ip.match"),
													userEntry.getSystemId(), ipaddress);
											matched = true;
										} else {
											String[] allowed_list = userEntry.getAccessIp().split(",");
											for (String allowedip : allowed_list) {
												if (allowedip.indexOf("/") > 0 && isInRange(allowedip, ipaddress)) {
//													if (isInRange(allowedip, ipaddress)) {
													logger.info(
															messageResourceBundle
																	.getLogMessage("user.range.matched.info"),
															userEntry.getSystemId(), allowedip, ipaddress);
													matched = true;
													break;
//													}
												} else {
													if (ipaddress.equalsIgnoreCase(allowedip)) {

														logger.info(
																messageResourceBundle.getLogMessage(
																		"user.configured.ip.matched.info"),
																userEntry.getSystemId(), allowedip, ipaddress);
														matched = true;
														break;
													}
												}
											}

											if (!matched) {

												logger.info(
														messageResourceBundle
																.getLogMessage("user.matching.global.access.ip.info"),
														userEntry.getSystemId(), ipaddress);
												allowed_list = IConstants.GLOBAl_ACCESS_IP.split(",");
												for (String allowedip : allowed_list) {
													if (allowedip.indexOf("/") > 0) {
														if (isInRange(allowedip, ipaddress)) {

															logger.info(
																	messageResourceBundle
																			.getLogMessage("user.range.matched.info"),
																	userEntry.getSystemId(), allowedip, ipaddress);
															matched = true;
															break;
														}
													} else {
														if (ipaddress.equalsIgnoreCase(allowedip)) {

															logger.info(
																	messageResourceBundle.getLogMessage(
																			"user.configured.ip.matched.info"),
																	userEntry.getSystemId(), allowedip, ipaddress);
															matched = true;
															break;
														}
													}
												}
											}
										}
										if (!matched) {

											logger.info(
													messageResourceBundle
															.getLogMessage("user.access.ip.not.matched.info"),
													userEntry.getSystemId(), ipaddress);
											if (userEntry.getAccessCountry() != null
													&& userEntry.getAccessCountry().length() > 0) {

												logger.info(
														messageResourceBundle
																.getLogMessage("user.matching.allowed.country.info"),
														userEntry.getSystemId(), ipaddress);
												String country = getCountryname(ipaddress);
												if (country != null && !country.isEmpty()) {

													logger.info(
															messageResourceBundle
																	.getLogMessage("user.country.found.info"),
															userEntry.getSystemId(), ipaddress, country);
													for (String allowedCountry : userEntry.getAccessCountry()
															.split(",")) {
														if (allowedCountry.equalsIgnoreCase(country)) {
															matched = true;
															break;
														}
													}
												} else {
													logger.info(
															messageResourceBundle
																	.getLogMessage("user.country.not.found.info"),
															userEntry.getSystemId(), ipaddress);
												}
												if (!matched) {
													webAccess = false;

													logger.info(
															messageResourceBundle
																	.getLogMessage("user.access.ip.not.allowed.info"),
															userEntry.getSystemId(), ipaddress);

													throw new InternalServerException(messageResourceBundle
															.getExMessage(ConstantMessages.USER_ACCESS_IPNOTALLOWED,
																	new Object[] { userEntry.getSystemId(),
																			ipaddress }));
												} else {

													logger.info(
															messageResourceBundle
																	.getLogMessage("user.valid.access.country.info"),
															userEntry.getSystemId(), ipaddress);
												}
											} else {
												webAccess = false;
												logger.info(
														messageResourceBundle.getLogMessage(
																"user.access.countries.not.configured.info"),
														userEntry.getSystemId());
//												throw new InternalServerException(messageResourceBundle.getExMessage(
//														ConstantMessages.ACCESS_COUNTRY_NOTCONFIGURED,
//														new Object[] { userEntry.getSystemId() }));
											}
										} else {
											logger.info(
													messageResourceBundle.getLogMessage("user.valid.access.ip.info"),
													userEntry.getSystemId(), ipaddress);
										}

									} else {
//										throw new NotFoundException(messageResourceBundle
//												.getExMessage(ConstantMessages.IPADDRESS_NOTFOUND));
									}

								} else {

									System.out.println(webAccess);
									logger.info(
											messageResourceBundle.getLogMessage("user.access.ip.not.configured.info"),
											userEntry.getSystemId());

									if (userEntry.getAccessCountry() != null
											&& userEntry.getAccessCountry().length() > 0) {
										boolean matched = false;
										if (ipaddress.equalsIgnoreCase("0:0:0:0:0:0:0:1")
												|| ipaddress.equalsIgnoreCase("127.0.0.1")) {
											logger.info(messageResourceBundle.getLogMessage("user.local.ip.match"),
													userEntry.getSystemId(), ipaddress);
											matched = true;
										} else {

											logger.info(
													messageResourceBundle
															.getLogMessage("user.matching.allowed.country.info"),
													userEntry.getSystemId(), ipaddress);
											String country = getCountryname(ipaddress);
											System.out.println(country);
											if (country != null && !country.isEmpty()) {
												logger.info(
														messageResourceBundle.getLogMessage("user.country.found.info"),
														userEntry.getSystemId(), ipaddress, country);
												for (String allowedCountry : userEntry.getAccessCountry().split(",")) {
													if (allowedCountry.equalsIgnoreCase(country)) {
														matched = true;
														break;
													}
												}
											} else {
												logger.info(
														messageResourceBundle
																.getLogMessage("user.country.not.found.info"),
														userEntry.getSystemId(), ipaddress);
											}
										}

										if (!matched) {
											webAccess = false;

											logger.info(
													messageResourceBundle
															.getLogMessage("user.access.ip.not.allowed.info"),
													userEntry.getSystemId(), ipaddress);
//											throw new InternalServerException(messageResourceBundle.getExMessage(
//													ConstantMessages.USER_ACCESS_IPNOTALLOWED,
//													new Object[] { userEntry.getSystemId(), ipaddress }));

										} else {
											logger.info(
													messageResourceBundle
															.getLogMessage("user.valid.access.country.info"),
													userEntry.getSystemId(), ipaddress);
										}

									} else {

										logger.info(
												messageResourceBundle
														.getLogMessage("user.access.countries.not.configured.info"),
												userEntry.getSystemId());
//										throw new InternalServerException(messageResourceBundle.getExMessage(
//												ConstantMessages.ACCESS_COUNTRY_NOTCONFIGURED,
//												new Object[] { userEntry.getSystemId() }));
									}
								}

							}

							System.out.println("1088" + webAccess);
							if (webAccess) {
								System.out.println("*********************1085");

								if (webEntry.isOtpLogin()) {

									boolean otplogin = true;

									if (webEntry.isMultiUserAccess()) {
										logger.info(
												messageResourceBundle
														.getLogMessage("user.multi.user.access.enabled.info"),
												userEntry.getSystemId());
										List<MultiUserEntry> list = this.multiUserRepo
												.findByUserIdEquals(userEntry.getId());
										if (list.isEmpty()) {
											logger.info(
													messageResourceBundle
															.getLogMessage("user.no.multi.access.name.found.info"),
													userEntry.getSystemId());

										} else {
											otplogin = false;

										}

									}
									if (otplogin) {

										if (webEntry.getOtpNumber() != null && webEntry.getOtpNumber().length() > 0) {
											String valid_otp_numbers = "";

											for (String number : webEntry.getOtpNumber().split(",")) {
												logger.info(messageResourceBundle.getLogMessage("user.otp.number.info"),
														userEntry.getSystemId(), number);

												try {
													Long.parseLong(number);
													valid_otp_numbers += number + ",";
												} catch (NumberFormatException ne) {
													logger.error(
															messageResourceBundle
																	.getLogMessage("user.invalid.otp.number.error"),
															userEntry.getSystemId(), number);
												}

												if (valid_otp_numbers.length() > 0) {

													int otp = 0;
													valid_otp_numbers = valid_otp_numbers.substring(0,
															valid_otp_numbers.length() - 1);
													logger.info(
															messageResourceBundle
																	.getLogMessage("user.valid.otp.numbers.info"),
															userEntry.getSystemId(), valid_otp_numbers);
													// check otp exist & send to user in case of absent or expired
													Optional<OTPEntry> optionalOtp = this.otpEntryRepository
															.findBySystemId(userEntry.getSystemId());
													OTPEntry otpEntry = null;

													if (optionalOtp.isPresent()) {
														otpEntry = optionalOtp.get();
													} else {
														logger.info(messageResourceBundle
																.getLogMessage("user.no.otp.entry.found.info"));
													}

													boolean generate_otp = true;
													if (otpEntry != null) {
														if (otpEntry.getExpiresOn() != null) {

															try {

																System.out.println(new Date().after(
																		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
																				.parse(otpEntry.getExpiresOn())));

																if (new Date().after(
																		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
																				.parse(otpEntry.getExpiresOn()))) {

																	logger.info(
																			messageResourceBundle.getLogMessage(
																					"user.otp.expired.on.info"),
																			userEntry.getSystemId(),
																			otpEntry.getExpiresOn());
																} else {
																	generate_otp = false;
																	otp = otpEntry.getOneTimePass();
																}
															} catch (ParseException e) {
																logger.error(e.getLocalizedMessage());
																throw new InternalServerException(
																		messageResourceBundle.getExMessage(
																				ConstantMessages.OTPEXPIRYDATE_PARSE_ERROR));
															}
														}

													}

													if (generate_otp) {

														otp = new Random().nextInt(999999 - 100000) + 100000;
														Calendar calendar = Calendar.getInstance();

														int duration = (IConstants.LOGIN_OTP_VALIDITY > 0)
																? IConstants.LOGIN_OTP_VALIDITY
																: 5;

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
															this.otpEntryRepository.save(new OTPEntry(
																	userEntry.getSystemId(), otp, validity));
														}
														// --------------------- send to user ---------------
														UserEntry internalUser = this.userEntryRepository
																.findByRole("internal");
														DriverInfo driverInfo = driverInfoRepository
																.findById(internalUser.getId()).get();

														if (internalUser != null) {

															String content = null;
															try {
																content = MultiUtility
																		.readContent(IConstants.FORMAT_DIR + "otp.txt");
															} catch (Exception ex) {
																logger.error(
																		messageResourceBundle
																				.getLogMessage("user.otp.format.error"),
																		ex.getMessage());
															}

															if (content == null) {
																content = "Hello [system_id], [otp_pass] is your One-Time Password (OTP) on [url] valid for next [duration] minutes";
															}

															content = content.replace("[system_id]",
																	userEntry.getSystemId());
															content = content.replace("[otp_pass]",
																	String.valueOf(otp));
															content = content.replace("[url]", IConstants.WebUrl);
															content = content.replace("[duration]",
																	String.valueOf(duration));
															ArrayList<String> list = new ArrayList<String>(
																	Arrays.asList(valid_otp_numbers.split(",")));
															BulkSmsDTO smsDTO = new BulkSmsDTO();
															smsDTO.setSystemId(internalUser.getSystemId());
															smsDTO.setPassword(new PasswordConverter()
																	.convertToEntityAttribute(driverInfo.getDriver()));
															smsDTO.setMessage(content);
															smsDTO.setDestinationList(list);
															if (webEntry.getOtpSender() != null
																	&& webEntry.getOtpSender().length() > 1) {
																smsDTO.setSenderId(webEntry.getOtpSender());
															} else {
																smsDTO.setSenderId(IConstants.OTP_SENDER_ID);
															}
															ResponseEntity<?> response = MultiUtility.sendOtpSms(
																	userEntry.getSystemId(), smsDTO, restTemplate);
															logger.info("<OTP SMS: " + response.getBody().toString()
																	+ ">" + userEntry.getSystemId() + "<"
																	+ valid_otp_numbers + ">");
															if (webEntry.getOtpEmail() != null
																	&& webEntry.getOtpEmail().length() > 0
																	&& isValidEmail(webEntry.getOtpEmail())) {
																String from = IConstants.SUPPORT_EMAIL[0];
																ProfessionEntry proEntry = getProfessionEntry(
																		userEntry.getId());
//																
																if (proEntry.getDomainEmail() != null
																		&& isValidEmail(proEntry.getDomainEmail())) {
																	from = proEntry.getDomainEmail();
																	logger.info(
																			messageResourceBundle.getLogMessage(
																					"user.domain.email.found.info"),
																			userEntry.getSystemId(), from);
																} else {
																	String master = userEntry.getMasterId();
																	ProfessionEntry professionEntry = getProfessionEntry(
																			master);
																	if (professionEntry != null && isValidEmail(
																			professionEntry.getDomainEmail())) {
																		from = professionEntry.getDomainEmail();
																		logger.info(messageResourceBundle.getLogMessage(
																				"user.master.domain.email.found.info"),
																				userEntry.getSystemId(), from);
																	} else {
																		logger.info(messageResourceBundle.getLogMessage(
																				"user.domain.email.not.found.info"),
																				userEntry.getSystemId());
																	}
																	logger.info(
																			messageResourceBundle.getLogMessage(
																					"user.sending.otp.email.info"),
																			userEntry.getSystemId(), from,
																			webEntry.getOtpEmail());
																}
																emailSender.sendEmail(webEntry.getOtpEmail(),
																		Constant.OTP_SUBJECT,
																		Constant.GENERAL_TEMPLATE_PATH,
																		emailSender.createSourceMap(content));
															} else {
																throw new InternalServerException(
																		"Invalid Email Address Provided!");
															}

														}
													} else {
														logger.info(messageResourceBundle
																.getLogMessage("user.otp.already.generated.info"));
													}

												} else {
													this.accessLog.save(new AccessLogEntry(userEntry.getSystemId(),
															new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
																	.format(new Date()),
															ipaddress, 0, "failed", "missing OTP numbers"));
													throw new InternalServerException("Missing OTP numbers!");
												}
											}
										} else {
											logger.error(
													messageResourceBundle
															.getLogMessage("user.otp.number.not.configured.error"),
													userEntry.getSystemId());
											this.accessLog.save(new AccessLogEntry(userEntry.getSystemId(),
													new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
													ipaddress, 0, "failed", "missing OTP numbers"));
											throw new InternalServerException(
													userEntry.getSystemId() + " OTP Number Not Configured.");
										}
									} else {
										// otp login is false and user has multi access
										ResponseEntity<?> userLogin = login(loginRequest);
										JwtResponse jwtResp = (JwtResponse) userLogin.getBody();
										jwtResp.setOtpLogin(false);
										jwtResp.setStatus("User Has Multi User Access!");
										System.out.println("User Has Multi User Access!!-----1385");
										return ResponseEntity.ok(jwtResp);
									}

									// otp sent successfully
									LoginResponse loginResponse = new LoginResponse();
									loginResponse.setStatus("Otp sent successfully!");
									loginResponse.setOtpLogin(true);
									return ResponseEntity.ok(loginResponse);

								} else if (webEntry.isEmailOnLogin()) {
									System.out.println(webEntry.isEmailOnLogin());
									// if not otp login send email to user of login alert
									// send email for login
									String to = IConstants.TO_EMAIl;
									String from = IConstants.SUPPORT_EMAIL[0];
									ProfessionEntry proEntry = getProfessionEntry(userEntry.getId());

									if (isValidEmail(proEntry.getDomainEmail())) {

										from = proEntry.getDomainEmail();
										logger.info(messageResourceBundle.getLogMessage("user.domain.email.found.info"),
												userEntry.getSystemId(), from);

									} else {
										ProfessionEntry professionEntry = getProfessionEntry(userEntry.getMasterId());
										if (professionEntry != null && isValidEmail(professionEntry.getDomainEmail())) {
											from = professionEntry.getDomainEmail();
											logger.info(
													messageResourceBundle
															.getLogMessage("user.master.domain.email.found.info"),
													userEntry.getSystemId(), from);
										} else {
											logger.info(
													messageResourceBundle
															.getLogMessage("user.domain.email.not.found.info"),
													userEntry.getSystemId());
										}
									}

									System.out.println(webEntry.getEmail());

									if (webEntry.getEmail() != null && webEntry.getEmail().contains("@")
											&& webEntry.getEmail().contains(".")) {
										to = webEntry.getEmail();
									}

									emailSender.sendEmail(to, Constant.LOGIN_SUBJECT, Constant.LOGIN_TEMPLATE_PATH,
											emailSender.createCustomSourceMap(userEntry.getSystemId(),
													IConstants.GATEWAY_NAME, ipaddress, new Date().toString(),
													IConstants.DEFAULT_GMT));

									ResponseEntity<?> userLogin = login(loginRequest);
									JwtResponse jwtResp = (JwtResponse) userLogin.getBody();
									jwtResp.setOtpLogin(false);
									jwtResp.setStatus("Login Alert Email Sent Successfully!");
									return ResponseEntity.ok(jwtResp);

								} else {
									ResponseEntity<?> userLogin = login(loginRequest);
									JwtResponse jwtResp = (JwtResponse) userLogin.getBody();
									jwtResp.setOtpLogin(false);
									jwtResp.setStatus("Jwt Token Generated Successfully!");
									return ResponseEntity.ok(jwtResp);
								}

							} else {
								throw new InternalServerException(messageResourceBundle.getExMessage(
										ConstantMessages.WEBACCESS_DENIED_USER,
										new Object[] { loginRequest.getUsername(), loginRequest.getPassword() }));
							}

						} else {
							logger.info(messageResourceBundle.getLogMessage("user.web.access.denied.info"),
									loginRequest.getUsername(), loginRequest.getPassword());
							throw new InternalServerException(
									messageResourceBundle.getExMessage(ConstantMessages.WEBACCESS_DENIED_USER,
											new Object[] { loginRequest.getUsername(), loginRequest.getPassword() }));
						}
					}
				} else {
					logger.info(messageResourceBundle.getLogMessage("user.web.access.denied.info"),
							loginRequest.getUsername(), loginRequest.getPassword());
					this.accessLog.save(new AccessLogEntry(userEntry.getSystemId(),
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), ipaddress, 0, "failed",
							"WebAccess Denied"));
					throw new InternalServerException(
							messageResourceBundle.getExMessage(ConstantMessages.WEBACCESS_DENIED_USER,
									new Object[] { loginRequest.getUsername(), loginRequest.getPassword() }));
				}
			} else {
				throw new NotFoundException(
						messageResourceBundle.getExMessage(ConstantMessages.NOT_FOUND_WEBMASTER_ERROR));
			}

		} else {
			logger.error(messageResourceBundle.getLogMessage("user.invalid.credentials.error"));
			throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.INVALID_CREDENTIALS));
		}

	}

	@Override
	public ResponseEntity<?> userIpOtpValidate(LoginRequest loginRequest, int otp) {
		int userId = 0;
		String systemId = loginRequest.getUsername();
		String password = encoder.encode(loginRequest.getPassword());
		UserEntry userEntry = getUser(loginRequest.getUsername());
		if (userEntry.getPassword().equals(password)) {
			userId = userEntry.getId();
		} else {
			logger.error(messageResourceBundle.getLogMessage("auth.failed.password"), systemId);
			userId = 0;
		}

		int otp1 = 0;
		Optional<OTPEntry> optionalOtp = this.otpEntryRepository.findBySystemId(userEntry.getSystemId());
		OTPEntry otpEntry = null;

		if (optionalOtp.isPresent()) {
			otpEntry = optionalOtp.get();
		} else {
			logger.info(messageResourceBundle.getLogMessage("user.no.otp.entry.found.info"));
		}

		if (otpEntry.getExpiresOn() != null) {

			try {

				System.out.println(
						new Date().after(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(otpEntry.getExpiresOn())));

				if (new Date().after(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(otpEntry.getExpiresOn()))) {

					logger.info(messageResourceBundle.getLogMessage("user.otp.expired.on.info"),
							userEntry.getSystemId(), otpEntry.getExpiresOn());
				} else {
					otp1 = otpEntry.getOneTimePass();
				}
			} catch (ParseException e) {
				logger.error(e.getLocalizedMessage());
				throw new InternalServerException(
						messageResourceBundle.getExMessage(ConstantMessages.OTPEXPIRYDATE_PARSE_ERROR));
			}
		}
		if (otp == otp1) {

			ResponseEntity<?> userLogin = login(loginRequest);
			JwtResponse jwtResp = (JwtResponse) userLogin.getBody();
			jwtResp.setOtpLogin(true);
			jwtResp.setStatus("Otp Matched successfully!");
			return ResponseEntity.ok(jwtResp);
		} else {
			throw new InvalidOtpException("OTP Expired Or Invalid OTP Entered!");
		}

	}

	private boolean isValidEmail(String email) {
		return email != null && email.length() > 0 && email.contains("@") && email.contains(".");
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
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes();
		if (requestAttributes != null) {
			HttpServletRequest request = requestAttributes.getRequest();
			String clientIp = request.getHeader("X-Forwarded-For");
			if (checkIPisEmptyOrUnknown(clientIp)) {
				clientIp = request.getHeader("Proxy-Client-IP");
			}
			if (checkIPisEmptyOrUnknown(clientIp)) {
				clientIp = request.getHeader("WL-Proxy-Client-IP");
			}
			if (checkIPisEmptyOrUnknown(clientIp)) {
				clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
			}
			if (checkIPisEmptyOrUnknown(clientIp)) {
				clientIp = request.getRemoteAddr();
			}
			return clientIp;
		} else {
			return null;
		}
	}

	private boolean checkIPisEmptyOrUnknown(String value) {
		return value == null || value.isEmpty() || "unknown".equalsIgnoreCase(value);
	}

	public ProfessionEntry getProfessionEntry(String systemId) {
		ProfessionEntry entry = null;
		if (GlobalVars.UserMapping.containsKey(systemId)) {
			int userid = GlobalVars.UserMapping.get(systemId);
			entry = GlobalVars.ProfessionEntries.get(userid);
		}
		return entry;
	}
//-------------------------------------------------------------------------------------------------------------------

	public int validateEntry(String username, String password) {
		SalesEntry entry = this.salesRepository.findByUsername(username);
		if (entry == null) {
			return 0;
		} else {
			if (entry.getPassword().equals(password)) {
				return entry.getId();
			} else {
				return 0;
			}
		}
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
		int seller_id = validateEntry(systemId, password);
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
				logger.error(messageResourceBundle.getLogMessage("user.account.expired"), systemId);
				throw new InternalServerException("Executive " + systemId + " Account Expired!");
			} else {
				userSessionObject.setFlagValue("100");
				userSessionObject.getWebMasterEntry().setDisplayCost(true);
				target = IConstants.SUCCESS_KEY;
			}

		} else {
			throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION,
					new Object[] { systemId }));
		}
		return ResponseEntity.ok(target);
	}

	@Override

	public ResponseEntity<?> addMultiUser(MultiUserForm entryForm) {
		SignupRequest signUpRequest = new SignupRequest();
		UserEntry convertRequest = ConvertRequert(signUpRequest);

		UserEntry user = userEntryRepository.save(convertRequest);

		String target = IConstants.FAILURE_KEY;
		String systemId = user.getSystemId();
		String role = user.getRole();

		int multiUserId = 0;
		try {
			multiUserId = entryForm.getUserId();
			String requestedSystemId = null;
			if (GlobalVars.UserEntries.containsKey(multiUserId)) {
				requestedSystemId = GlobalVars.UserEntries.get(multiUserId).getSystemId();
			}
			logger.info(systemId + "[" + role + "] Add Multi User Request For " + requestedSystemId + "[" + multiUserId
					+ "]");

			if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system") || role.equalsIgnoreCase("admin")
					|| signUpRequest.getWebMasterEntry().isMultiUserAccess()) {
				if (entryForm.getAccessName() != null && entryForm.getAccessName().length > 0) {
					List<MultiUserEntry> list = new ArrayList<>();
					for (int i = 0; i < entryForm.getAccessName().length; i++) {
						MultiUserEntry entry = new MultiUserEntry(entryForm.getUserId(), entryForm.getAccessName()[i],
								entryForm.getMobile()[i], entryForm.getEmail()[i]);
						list.add(entry);
					}

					if (list.isEmpty()) {
						logger.info(requestedSystemId + " No multi access names found");
						return ResponseEntity.badRequest().body("No multi access names found");
					} else {
						addMultiUserEntry(list);
						target = IConstants.SUCCESS_KEY;
					}

					// Create a MultiUserResponse object
					MultiUserResponse response = new MultiUserResponse(systemId, multiUserId, list);
					return ResponseEntity.ok(response);
				} else {
					logger.info(requestedSystemId + " No multi access names found");
					return ResponseEntity.badRequest().body("No multi access names found");
				}
			}
		} catch (Exception e) {
			logger.error("" + multiUserId, e.fillInStackTrace());
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred");
	}

	public void addMultiUserEntry(List<MultiUserEntry> list) throws Exception {
		String insertQry = "insert into multi_user_access " + "(user_id,access_name,mobile,email)"
				+ " values (?,?,?,?)";
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			con.setAutoCommit(false);
			pStmt = con.prepareStatement(insertQry);
			for (MultiUserEntry entry : list) {
				pStmt.setInt(1, entry.getUserId());
				pStmt.setString(2, entry.getAccessName());
				pStmt.setString(3, entry.getMobile());
				pStmt.setString(4, entry.getEmail());
				pStmt.addBatch();
			}
			pStmt.executeBatch();
			con.commit();
		} catch (SQLException sqle) {
			logger.error(" ", sqle.fillInStackTrace());
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
	}

	@Override
	public ResponseEntity<?> addUserLimit(UserLimitForm userLimitForm) {
		SignupRequest signUpRequest = new SignupRequest();
		UserEntry convertRequest = ConvertRequert(signUpRequest);

		UserEntry user = userEntryRepository.save(convertRequest);

		String masterId = user.getSystemId();
		String role = user.getRole();
		logger.info(masterId + "[" + role + "] Add User Limit Request: " + userLimitForm.getUserId());
		try {
			if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")) {
				UserlimitEntry entry = new UserlimitEntry();
				BeanUtils.copyProperties(entry, userLimitForm);
				try {
					saveLimit(entry);
					MultiUtility.changeFlag(Constants.USER_LT_FLAG_FILE, "707");
					return ResponseEntity.ok("User limit added successfully"); // Return success response
				} catch (DuplicateEntryException e) {
					logger.error(userLimitForm.getUserId() + " Entry Already Exist");
					return ResponseEntity.badRequest().body("Entry Already Exist"); // Return bad request response
				}
			} else {
				logger.error("Authorization Failed :" + masterId);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization Failed"); // Return forbidden
																									// response
			}
		} catch (Exception ex) {
			logger.error(masterId, ex.fillInStackTrace());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred"); // Return internal
																									// server error
																									// response
		}
	}

	public void saveLimit(UserlimitEntry entry) throws DuplicateEntryException {
		saveLimit1(entry);
	}

	public void saveLimit1(UserlimitEntry entry) throws DuplicateEntryException {
		try (Session session = sessionFactory.openSession()) {
			session.beginTransaction();
			session.save(entry);
			session.getTransaction().commit();
		} catch (PersistenceException e) {
			if (e.getCause() instanceof ConstraintViolationException) {
				throw new DuplicateEntryException("Duplicate entry: " + e.getCause().getMessage());
			} else {
				e.printStackTrace();
			}
		}
	}

	@Override
	public ResponseEntity<?> listMultiUser(int id) {
		// Assume UserSessionObject is correctly instantiated or injected
		UserSessionObject userSessionObject = new UserSessionObject();
		String masterId = userSessionObject.getSystemId();
		String role = userSessionObject.getRole();
		List<UserEntry> selected = new java.util.ArrayList<>();

		try {
			if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")
					|| role.equalsIgnoreCase("admin")) {
				Predicate<Integer, WebMasterEntry> pw = new PredicateBuilderImpl().getEntryObject()
						.is("multiUserAccess");
				for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(pw)) {
					UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
					if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")
							|| userEntry.getMasterId().equals(masterId)) {
						selected.add(userEntry);
					}
				}
				if (selected.isEmpty()) {
					return ResponseEntity.ok(Map.of("message", "No User Found For MultiUserAccess"));
				} else {
					userSessionObject.setMultiUsername(masterId);
					List<Map<String, Object>> response = selected.stream().map(userEntry -> {
						Map<String, Object> entryMap = new HashMap<>();
						entryMap.put("userId", userEntry.getId());
						entryMap.put("masterId", userEntry.getMasterId());
						return entryMap;
					}).collect(Collectors.toList());

					return ResponseEntity.ok(response);
				}
			} else if (userSessionObject.getWebMasterEntry().isMultiUserAccess()) {
				UserEntry userEntry = GlobalVars.UserEntries.get(userSessionObject.getId());
				selected.add(userEntry);
				userSessionObject.setMultiUsername(masterId);
				// Similar DTO conversion as above
				List<Map<String, Object>> response = selected.stream().map(userEntry1 -> {
					Map<String, Object> entryMap = new HashMap<>();
					entryMap.put("userId", userEntry.getId());
					entryMap.put("masterId", userEntry.getMasterId());
					return entryMap;
				}).collect(Collectors.toList());

				return ResponseEntity.ok(response);
			} else {
				return ResponseEntity.badRequest().body(Map.of("error", "Authorization Failed"));
			}
		} catch (Exception ex) {
			return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred"));
		}
	}

	@Override
	public ResponseEntity<?> ModifyMultiUserUpdate(MultiUserForm entryForm) {
		String target = IConstants.FAILURE_KEY;
		UserSessionObject userSessionObject = new UserSessionObject();
		String systemid = userSessionObject.getSystemId();
		String role = userSessionObject.getRole();
		int multiUserId = entryForm.getUserId();
		String requested_system_id = null;
		if (GlobalVars.UserEntries.containsKey(multiUserId)) {
			requested_system_id = GlobalVars.UserEntries.get(multiUserId).getSystemId();
		}
		logger.info(systemid + "[" + role + "] Update Multi User Request For " + requested_system_id + "[" + multiUserId
				+ "]");
		if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system") || role.equalsIgnoreCase("admin")
				|| userSessionObject.getWebMasterEntry().isMultiUserAccess()) {
			if (entryForm.getId() != null && entryForm.getId().length > 0) {
				MultiUserEntry entry = null;
				List<MultiUserEntry> list = new java.util.ArrayList<MultiUserEntry>();
				for (int i = 0; i < entryForm.getId().length; i++) {
					entry = new MultiUserEntry(entryForm.getId()[i], entryForm.getUserId(),
							entryForm.getAccessName()[i], entryForm.getMobile()[i], entryForm.getEmail()[i]);
					list.add(entry);
				}
				try {
					if (list.isEmpty()) {
						String errorMessage = requested_system_id + " No multi access names found";
						logger.info(errorMessage);

						logger.error("Error: " + errorMessage);
						target = IConstants.ERROR_HTTP01;
					} else {
						updateMultiUser(list);
						target = IConstants.SUCCESS_KEY;
					}
				} catch (Exception e) {
					logger.error("An error occurred: " + e.getMessage());
					target = IConstants.ERROR_HTTP01;
				}

			} else {
				logger.info(requested_system_id + " No multi access names found");
			}
		} else {
		}
		return null;
	}

	public void updateMultiUser(List<MultiUserEntry> list) throws Exception {
		String insertQry = "update multi_user_access set access_name=?,mobile=?,email=? where id = ?";
		Connection con = null;
		PreparedStatement pStmt = null;
		try {
			con = getConnection();
			con.setAutoCommit(false);
			pStmt = con.prepareStatement(insertQry);
			for (MultiUserEntry entry : list) {
				pStmt.setString(1, entry.getAccessName());
				pStmt.setString(2, entry.getMobile());
				pStmt.setString(3, entry.getEmail());
				pStmt.setInt(4, entry.getId());
				pStmt.addBatch();
			}
			pStmt.executeBatch();
			con.commit();
		} catch (SQLException sqle) {
			logger.error(" ", sqle.fillInStackTrace());
			throw new Exception(sqle.getMessage());
		} finally {
			try {
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
	}

	@Override
	public ResponseEntity<?> modifyMultiUserDelete(MultiUserForm entryForm) {
		UserSessionObject userSessionObject = new UserSessionObject();
		String systemId = userSessionObject.getSystemId();
		String role = userSessionObject.getRole();
		int multiUserId = entryForm.getUserId();
		String requestedSystemId = null;
		if (GlobalVars.UserEntries.containsKey(multiUserId)) {
			requestedSystemId = GlobalVars.UserEntries.get(multiUserId).getSystemId();
		}
		logger.info(systemId + "[" + role + "] Delete Multi User Request For " + requestedSystemId + "[" + multiUserId
				+ "]");

		if (!role.equalsIgnoreCase("superadmin") && !role.equalsIgnoreCase("system") && !role.equalsIgnoreCase("admin")
				&& !userSessionObject.getWebMasterEntry().isMultiUserAccess()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}

		if (entryForm.getId() == null || entryForm.getId().length == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No multi access names found");
		}

		List<MultiUserEntry> list = new ArrayList<>();
		for (int id : entryForm.getId()) {
			MultiUserEntry entry = new MultiUserEntry(id);
			list.add(entry);
		}

		if (list.isEmpty()) {
			logger.info(requestedSystemId + " No multi access names found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No multi access names found");
		} else {
			deleteMultiUser(list); // Assuming this method deletes the users and handles exceptions internally
			return ResponseEntity.ok("Successfully deleted multi users");
		}
	}

	public boolean deleteMultiUser(List<MultiUserEntry> list) {
		String deleteQry = "delete from multi_user_access where id = ?";
		Connection con = null;
		PreparedStatement pStmt = null;
		boolean isSuccess = false;
		try {
			con = getConnection();
			con.setAutoCommit(false);
			pStmt = con.prepareStatement(deleteQry);
			for (MultiUserEntry entry : list) {
				pStmt.setInt(1, entry.getId());
				pStmt.addBatch();
			}
			pStmt.executeBatch();
			con.commit();
			isSuccess = true; // Set success to true if all operations complete successfully
		} catch (SQLException sqle) {
			logger.error("SQL Error: ", sqle);
			if (con != null) {
				try {
					con.rollback(); // Rollback changes if exception occurs
				} catch (SQLException e) {
					logger.error("Rollback failed: ", e);
				}
			}
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException sqle) {
				logger.error("Error closing resources: ", sqle);
			}
		}
		return isSuccess; // Return the success status
	}

	@Override
	public ResponseEntity<?> updateBalance(UserEntryForm userForm) throws ProcessErrorException {

		String target = IConstants.FAILURE_KEY;
		boolean isSufficient = true;

		UserSessionObject userSessionObject = new UserSessionObject();
		boolean proceed = true;
		String operation = userForm.getRechargeEntry().getOperation();
		logger.info("User: " + userForm.getId() + " WalletFlag: " + userForm.getBalance().getWalletFlag()
				+ " Operation: " + operation);
		String role = userSessionObject.getRole();
		// UserEntryExt userEntryExt =
		// GlobalVars.userService.getUserBalanceEntry(userForm.getId());
		UserEntry userEntry = GlobalVars.UserEntries.get(userForm.getId());
		if (userSessionObject.getRole().equalsIgnoreCase("admin")) {
			if (!userEntry.getMasterId().equalsIgnoreCase(userSessionObject.getSystemId())) {
				logger.info(userSessionObject.getSystemId() + "[" + userSessionObject.getRole() + "] Invalid Request");
				proceed = false;
			}
		}
		if (proceed) {
			long toBeAddedCredits = 0;
			double toBeAddedBalance = 0;
			if (userForm.getBalance().getWalletFlag().equalsIgnoreCase("No")) {
				toBeAddedCredits = userForm.getBalance().getCredits();
				if (toBeAddedCredits < 0) {
					proceed = false;
					logger.error(
							userSessionObject.getSystemId() + " Invalid Credit Amount entered: " + toBeAddedCredits);
				}
			} else {
				toBeAddedBalance = userForm.getBalance().getWalletAmount();
				if (toBeAddedBalance < 0) {
					proceed = false;
					logger.error(
							userSessionObject.getSystemId() + " Invalid Wallet Amount entered: " + toBeAddedBalance);
				}
			}
			if (proceed) {
				if (userForm.getRemark() == null || userForm.getRemark().equalsIgnoreCase("")) {
					userForm.setRemark("Account Updated");
				}
				RechargeEntry rechargeEntryUser = null;
				logger.info("Processing For Balance Update Request");
				if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")) {
					if (role.equalsIgnoreCase("superadmin")
							&& userEntry.getSystemId().equalsIgnoreCase(userSessionObject.getSystemId())) {// own
																											// account
																											// recharge
																											// by
																											// superadmin
						try {
							logger.info("Superadmin Recharging his Own Account: Wallet: " + toBeAddedBalance
									+ " Credits: " + toBeAddedCredits);
							double effectiveBalance = userSessionObject.getBalance().getWalletAmount();
							long effectiveCredits = userSessionObject.getBalance().getCredits();
							double preBalance = userSessionObject.getBalance().getWalletAmount();
							long preCredits = userSessionObject.getBalance().getCredits();
							boolean isProcessed = true;
							if (userForm.getBalance().getWalletFlag().equalsIgnoreCase("yes")) {
								isProcessed = addBalance(userSessionObject.getId(), toBeAddedBalance);
								effectiveBalance = effectiveBalance + toBeAddedBalance;
								logger.info(userSessionObject.getSystemId() + " Balance Added. Effective Balance: "
										+ effectiveBalance);
							} else {
								isProcessed = addCredits(userSessionObject.getId(), (int) toBeAddedCredits);
								effectiveCredits = effectiveCredits + toBeAddedCredits;
								logger.info(userSessionObject.getSystemId() + " Credits Added. Effective Credits: "
										+ effectiveCredits);
							}
							if (isProcessed) {
								logger.info(userSessionObject.getSystemId() + " isProcessed: " + isProcessed);
								userSessionObject.getBalance().setWalletAmount(effectiveBalance);
								userSessionObject.getBalance().setCredits(effectiveCredits);
								// ------------------ First Entry For Admin --------------------
								String particular = null;
								if (operation.equalsIgnoreCase("plus")) {
									particular = "cr_" + userEntry.getSystemId();
								} else {
									particular = "dr_" + userEntry.getSystemId();
								}
								logger.debug(userSessionObject.getSystemId() + "Form : " + userForm.getRechargeEntry());
								rechargeEntryUser = new RechargeEntry(userSessionObject.getId(),
										new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), particular,
										preCredits, toBeAddedCredits, preBalance, toBeAddedBalance,
										userForm.getRemark(), userForm.getRechargeEntry().getType(),
										userSessionObject.getSystemId());
								logger.debug(userSessionObject.getSystemId() + "[1] : " + rechargeEntryUser);
								rechargeEntryUser.setEffectiveCredits(userSessionObject.getBalance().getCredits());
								rechargeEntryUser.setEffectiveWallet(userSessionObject.getBalance().getWalletAmount());
								logger.debug(userSessionObject.getSystemId() + "[2] : " + rechargeEntryUser);
								if (userForm.getRechargeEntry().getType().equalsIgnoreCase("international")) {
									rechargeEntryUser.setPayTerm(userForm.getRechargeEntry().getPayTerm());
									rechargeEntryUser.setPayType(userForm.getRechargeEntry().getPayType());
									rechargeEntryUser.setApproveBy(userForm.getRechargeEntry().getApproveBy());
								}
								logger.debug(userSessionObject.getSystemId() + "[3] : " + rechargeEntryUser);
								saveRechargeEntry(rechargeEntryUser);
								target = IConstants.SUCCESS_KEY;
							} else {
								logger.info(userSessionObject.getSystemId() + " Balance Processing Error");
							}
						} catch (Exception e) {
							logger.info(userSessionObject.getSystemId(), e.fillInStackTrace());

						}
					} else { // deduct from master's account
						if (userEntry.getMasterId().equalsIgnoreCase(userSessionObject.getSystemId())) {
							try {
								// super/system is master account
								logger.info("Balance " + userForm.getRechargeEntry().getOperation() + " Request From "
										+ userSessionObject.getSystemId() + " To: " + userEntry.getSystemId()
										+ " Wallet: " + toBeAddedBalance + " Credit: " + toBeAddedCredits);
								String user_particular = null;
								if (operation.equalsIgnoreCase("plus")) {
									user_particular = "cr_" + userSessionObject.getSystemId();
								} else {
									user_particular = "dr_" + userSessionObject.getSystemId();
								}
								String admin_particular = null;
								if (operation.equalsIgnoreCase("plus")) {
									admin_particular = "dr_" + userEntry.getSystemId();
								} else {
									admin_particular = "cr_" + userEntry.getSystemId();
								}
								BalanceEntry target_balance = GlobalVars.BalanceEntries.get(userEntry.getId());
								rechargeEntryUser = new RechargeEntry(userEntry.getId(),
										new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), user_particular,
										target_balance.getCredits(), toBeAddedCredits, target_balance.getWalletAmount(),
										toBeAddedBalance, userForm.getRemark(), userForm.getRechargeEntry().getType(),
										userSessionObject.getSystemId());
								RechargeEntry rechargeEntryAdmin = new RechargeEntry(userSessionObject.getId(),
										new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
										admin_particular, userSessionObject.getBalance().getCredits(), toBeAddedCredits,
										userSessionObject.getBalance().getWalletAmount(), toBeAddedBalance,
										userForm.getRemark(), userForm.getRechargeEntry().getType(),
										userSessionObject.getSystemId());
								// ---------------------------------------------------------------
								double adminWallet = userSessionObject.getBalance().getWalletAmount();
								double userWallet = target_balance.getWalletAmount();
								long adminCredits = userSessionObject.getBalance().getCredits();
								long userCredits = target_balance.getCredits();
								if (userForm.getBalance().getWalletFlag().equalsIgnoreCase("yes")) {
									if (userForm.getRechargeEntry().getOperation().equalsIgnoreCase("plus")) {
										if (userEntry.isAdminDepend()) { // inherited user
											double allocatedBalance = 0;
											Predicate p = new PredicateBuilderImpl().getEntryObject().get("masterId")
													.equal(userEntry.getMasterId());
											Collection<UserEntry> users = GlobalVars.UserEntries.values(p);
											logger.info(
													userSessionObject.getSystemId() + " Depend Users: " + users.size());
											for (UserEntry child : users) {
												BalanceEntry child_balance = GlobalVars.BalanceEntries
														.get(child.getId());
												allocatedBalance += child_balance.getWalletAmount();
											}
											logger.info(userSessionObject.getSystemId() + "[" + adminWallet
													+ "] Previous Allocated Balance: " + allocatedBalance);
											if ((toBeAddedBalance + allocatedBalance) <= (adminWallet
													* IConstants.BALANCE__MULTIPLY_LIMIT)) {
												logger.info("Adding " + toBeAddedBalance + " From: "
														+ userSessionObject.getSystemId() + " To Child: "
														+ userEntry.getSystemId() + " [" + userWallet + "]");
												isSufficient = addBalance(userEntry.getId(), toBeAddedBalance);
											} else {
												logger.info(userSessionObject.getSystemId()
														+ " <Admin> has Insufficient Amount: " + adminWallet);
												isSufficient = false;
											}
										} else {
											if (adminWallet > toBeAddedBalance) {
												logger.info("Transfer " + toBeAddedBalance + " From: "
														+ userSessionObject.getSystemId() + "[" + adminWallet + "] To: "
														+ userEntry.getSystemId() + "[" + userWallet + "]");
												boolean isProcessed = transferBalance(userSessionObject.getId(),
														userEntry.getId(), toBeAddedBalance);
												if (isProcessed) {
													userWallet += toBeAddedBalance;
													adminWallet -= toBeAddedBalance;
												} else {
													logger.info(userSessionObject.getSystemId()
															+ " <Admin> has Insufficient Amount");
													isSufficient = false;
												}
											} else {
												logger.info(userSessionObject.getSystemId()
														+ " <Admin> has Insufficient Amount: " + adminWallet);
												isSufficient = false;
											}
										}
									} else {
										if (userEntry.isAdminDepend()) { // inherited user
											logger.info("Deduction " + toBeAddedBalance + " From: "
													+ userSessionObject.getSystemId() + "[" + adminWallet
													+ "] To Child : " + userEntry.getSystemId() + "[" + userWallet
													+ "]");
											if ((userWallet - toBeAddedBalance) >= 0) {
												boolean isProcessed = deductBalance(userEntry.getId(),
														toBeAddedBalance);
												if (isProcessed) {
													userWallet -= toBeAddedBalance;
												} else {
													logger.info(userEntry.getSystemId()
															+ "<User> has Insufficient Amount to Deduct");
													isSufficient = false;
												}
											} else {
												logger.info(userEntry.getSystemId()
														+ "<User> has Insufficient Amount to Deduct");
												isSufficient = false;
											}
										} else {
											logger.info("Deduction " + toBeAddedBalance + " From: "
													+ userSessionObject.getSystemId() + "[" + adminWallet + "] To : "
													+ userEntry.getSystemId() + "[" + userWallet + "]");
											if ((userWallet - toBeAddedBalance) >= 0) {
												boolean isProcessed = transferBalance(userEntry.getId(),
														userSessionObject.getId(), toBeAddedBalance);
												if (isProcessed) {
													userWallet -= toBeAddedBalance;
													adminWallet += toBeAddedBalance;
												} else {
													logger.info(userEntry.getSystemId()
															+ "<User> has Insufficient Amount to Deduct");
													isSufficient = false;
												}
											} else {
												logger.info(userEntry.getSystemId()
														+ "<User> has Insufficient Amount to Deduct");
												isSufficient = false;
											}
										}
									}
								} else {
									if (userForm.getRechargeEntry().getOperation().equalsIgnoreCase("plus")) {
										if (userEntry.isAdminDepend()) { // inherited user
											long allocatedCredits = 0;
											Predicate p = new PredicateBuilderImpl().getEntryObject().get("masterId")
													.equal(userEntry.getMasterId());
											Collection<UserEntry> users = GlobalVars.UserEntries.values(p);
											logger.info(
													userSessionObject.getSystemId() + " Depend Users: " + users.size());
											for (UserEntry child : users) {
												BalanceEntry child_balance = GlobalVars.BalanceEntries
														.get(child.getId());
												allocatedCredits += child_balance.getCredits();
											}
											logger.info(userSessionObject.getSystemId() + "[" + adminCredits
													+ "] Previous Allocated Credits: " + allocatedCredits);
											if (((toBeAddedCredits + allocatedCredits) <= (adminCredits
													* IConstants.BALANCE__MULTIPLY_LIMIT))) {
												logger.info("Credit Addition " + toBeAddedCredits + "  From: "
														+ userSessionObject.getSystemId() + "[" + adminCredits
														+ "] To Child: " + userEntry.getSystemId() + "[" + userCredits
														+ "]");
												boolean isProcessed = addCredits(userEntry.getId(),
														(int) toBeAddedCredits);
												if (isProcessed) { // Processing from smpp server
													userCredits += toBeAddedCredits;
												}
											} else {
												logger.info(userSessionObject.getSystemId()
														+ " <Admin> has Insufficient Credit");
												isSufficient = false;
											}
										} else {
											logger.info("Credits " + toBeAddedCredits + " Transfer From: "
													+ userSessionObject.getSystemId() + "[" + adminCredits + "] To: "
													+ userEntry.getSystemId() + "[" + userCredits + "]");
											if ((adminCredits > toBeAddedCredits)) {
												boolean isProcessed = transferCredits(userSessionObject.getId(),
														userEntry.getId(), (int) toBeAddedCredits);
												if (isProcessed) { // Processing from smpp server
													userCredits += toBeAddedCredits;
													adminCredits -= toBeAddedCredits;
												} else {
													logger.info(userSessionObject.getSystemId()
															+ " <Admin> has Insufficient Credit");
													isSufficient = false;
												}
											} else {
												logger.info(userSessionObject.getSystemId()
														+ " <Admin> has Insufficient Credit");
												isSufficient = false;
											}
										}
									} else {
										if (userEntry.isAdminDepend()) { // inherited user
											logger.info("Credits " + toBeAddedCredits + " Deduction From: "
													+ userSessionObject.getSystemId() + "[" + adminCredits
													+ "] To Child: " + userEntry.getSystemId() + "[" + userCredits
													+ "]");
											if ((userCredits - toBeAddedCredits) >= 0) {
												boolean isProcessed = deductCredits(userEntry.getId(),
														(int) toBeAddedCredits);
												if (isProcessed) { // Processing from smpp server
													userCredits -= toBeAddedCredits;
												} else {
													logger.info(userEntry.getSystemId()
															+ " <User> has Insufficient Credit to deduct");
													isSufficient = false;
												}
											} else {
												logger.info(userEntry.getSystemId()
														+ " <User> has Insufficient Credit to deduct");
												isSufficient = false;
											}
										} else {
											logger.info("Credits " + toBeAddedCredits + " Deduction From: "
													+ userSessionObject.getSystemId() + "[" + adminCredits + "] To: "
													+ userEntry.getSystemId() + "[" + userCredits + "]");
											if ((userCredits - toBeAddedCredits) >= 0) {
												boolean isProcessed = transferCredits(userEntry.getId(),
														userSessionObject.getId(), (int) toBeAddedCredits);
												if (isProcessed) { // Processing from smpp server
													userCredits -= toBeAddedCredits;
													adminCredits += toBeAddedCredits;
												} else {
													logger.info(userEntry.getSystemId()
															+ " <User> has Insufficient Credit to deduct");
													isSufficient = false;
												}
											} else {
												logger.info(userEntry.getSystemId()
														+ " <User> has Insufficient Credit to deduct");
												isSufficient = false;
											}
										}
									}
								}
								if (isSufficient) {
									logger.info(userSessionObject.getSystemId() + " Setting Balance Updation Changes");
									// -------------------- set Session Changes ----------------------
									userSessionObject.getBalance().setWalletAmount(adminWallet);
									userSessionObject.getBalance().setCredits(adminCredits);
									// ----------- save Recharge Entry -------------------------------
									rechargeEntryAdmin.setEffectiveWallet(adminWallet);
									rechargeEntryAdmin.setEffectiveCredits(adminCredits);
									if (userForm.getRechargeEntry().getType().equalsIgnoreCase("international")) {
										rechargeEntryAdmin.setPayTerm(userForm.getRechargeEntry().getPayTerm());
										rechargeEntryAdmin.setPayType(userForm.getRechargeEntry().getPayType());
										rechargeEntryAdmin.setApproveBy(userForm.getRechargeEntry().getApproveBy());
										rechargeEntryAdmin.setPaymethod(userForm.getRechargeEntry().getPaymethod());
									}
									saveRechargeEntry(rechargeEntryAdmin);
									rechargeEntryUser.setEffectiveWallet(userWallet);
									rechargeEntryUser.setEffectiveCredits(userCredits);
									if (userForm.getRechargeEntry().getType().equalsIgnoreCase("international")) {
										rechargeEntryUser.setPayTerm(userForm.getRechargeEntry().getPayTerm());
										rechargeEntryUser.setPayType(userForm.getRechargeEntry().getPayType());
										rechargeEntryUser.setApproveBy(userForm.getRechargeEntry().getApproveBy());
										rechargeEntryUser.setPaymethod(userForm.getRechargeEntry().getPaymethod());
									}
									saveRechargeEntry(rechargeEntryUser);
									target = IConstants.SUCCESS_KEY;
								} else {
									throw new InsufficientBalanceException("exception.insufficientBalance");
								}
							} catch (Exception e) {
								logger.info(userSessionObject.getSystemId(), e.fillInStackTrace());
								String errorMessage = "Process Error: " + e.getMessage() + "[" + e.getCause() + "]";
								throw new ProcessErrorException(errorMessage, e);
							}
						} else {// super/system recharging other's child
							try {
								// ---------------------------------------------------------------------
								logger.info("Balance " + userForm.getRechargeEntry().getOperation() + " Request From "
										+ userSessionObject.getSystemId() + " To: " + userEntry.getSystemId() + " C/O "
										+ userEntry.getMasterId() + " Wallet: " + toBeAddedBalance + " Credit: "
										+ toBeAddedCredits);
								String master_system_id = userEntry.getMasterId();
								int master_id = GlobalVars.UserMapping.get(master_system_id);
								// UserEntry masterEntry = GlobalVars.UserEntries.get(master_id);
								BalanceEntry master_balance = GlobalVars.BalanceEntries.get(master_id);
								String user_particular = null;
								if (operation.equalsIgnoreCase("plus")) {
									user_particular = "cr_" + userEntry.getMasterId();
								} else {
									user_particular = "dr_" + userEntry.getMasterId();
								}
								String admin_particular = null;
								if (operation.equalsIgnoreCase("plus")) {
									admin_particular = "dr_" + userEntry.getSystemId();
								} else {
									admin_particular = "cr_" + userEntry.getSystemId();
								}
								BalanceEntry target_balance = GlobalVars.BalanceEntries.get(userEntry.getId());
								rechargeEntryUser = new RechargeEntry(userEntry.getId(),
										new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), user_particular,
										target_balance.getCredits(), toBeAddedCredits, target_balance.getWalletAmount(),
										toBeAddedBalance, userForm.getRemark(), userForm.getRechargeEntry().getType(),
										userSessionObject.getSystemId());
								RechargeEntry rechargeEntryAdmin = new RechargeEntry(master_id,
										new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
										admin_particular, master_balance.getCredits(), toBeAddedCredits,
										master_balance.getWalletAmount(), toBeAddedBalance, userForm.getRemark(),
										userForm.getRechargeEntry().getType(), userSessionObject.getSystemId());
								// ---------------------------------------------------------------
								double adminWallet = master_balance.getWalletAmount();
								double userWallet = target_balance.getWalletAmount();
								long adminCredits = master_balance.getCredits();
								long userCredits = target_balance.getCredits();
								if (userForm.getBalance().getWalletFlag().equalsIgnoreCase("yes")) {
									if (userForm.getRechargeEntry().getOperation().equalsIgnoreCase("plus")) {
										if (userEntry.isAdminDepend()) { // inherited user
											double allocatedBalance = 0;
											Predicate p = new PredicateBuilderImpl().getEntryObject().get("masterId")
													.equal(userEntry.getMasterId());
											Collection<UserEntry> users = GlobalVars.UserEntries.values(p);
											logger.info(master_system_id + " Depend Users: " + users.size());
											for (UserEntry child : users) {
												BalanceEntry child_balance = GlobalVars.BalanceEntries
														.get(child.getId());
												allocatedBalance += child_balance.getWalletAmount();
											}
											logger.info(master_system_id + "[" + adminWallet
													+ "] Previous Allocated Balance: " + allocatedBalance);
											if ((toBeAddedBalance + allocatedBalance) <= (adminWallet
													* IConstants.BALANCE__MULTIPLY_LIMIT)) {
												logger.info("Adding " + toBeAddedBalance + " From: " + master_system_id
														+ " To Child: " + userEntry.getSystemId() + " [" + userWallet
														+ "]");
												boolean isProcessed = addBalance(userEntry.getId(), toBeAddedBalance);
												if (isProcessed) {
													logger.info(master_system_id + " <Admin> has Sufficient Amount");
													userWallet += toBeAddedBalance;
												} else {
													logger.info(master_system_id + " <Admin> has Insufficient Amount");
													isSufficient = false;
												}
											} else {
												logger.info(master_system_id + " <Admin> has Insufficient Amount: "
														+ adminWallet);
												isSufficient = false;
											}
										} else {
											if (adminWallet > toBeAddedBalance) {
												logger.info("Transfer " + toBeAddedBalance + " From: "
														+ master_system_id + "[" + adminWallet + "] To: "
														+ userEntry.getSystemId() + "[" + userWallet + "]");
												boolean isProcessed = transferBalance(master_id, userEntry.getId(),
														toBeAddedBalance);
												if (isProcessed) {
													userWallet += toBeAddedBalance;
													adminWallet -= toBeAddedBalance;
												} else {
													logger.info(master_system_id + " <Admin> has Insufficient Amount");
													isSufficient = false;
												}
											} else {
												logger.info(master_system_id + " <Admin> has Insufficient Amount: "
														+ adminWallet);
												isSufficient = false;
											}
										}
									} else {
										if (userEntry.isAdminDepend()) { // inherited user
											logger.info("Deduction " + toBeAddedBalance + " From: " + master_system_id
													+ "[" + adminWallet + "] To Child : " + userEntry.getSystemId()
													+ "[" + userWallet + "]");
											if ((userWallet - toBeAddedBalance) >= 0) {
												boolean isProcessed = deductBalance(userEntry.getId(),
														toBeAddedBalance);
												if (isProcessed) {
													userWallet -= toBeAddedBalance;
												} else {
													logger.info(userEntry.getSystemId()
															+ "<User> has Insufficient Amount to Deduct");
													isSufficient = false;
												}
											} else {
												logger.info(userEntry.getSystemId()
														+ "<User> has Insufficient Amount to Deduct");
												isSufficient = false;
											}
										} else {
											logger.info("Deduction " + toBeAddedBalance + " From: " + master_system_id
													+ "[" + adminWallet + "] To : " + userEntry.getSystemId() + "["
													+ userWallet + "]");
											if ((userWallet - toBeAddedBalance) >= 0) {
												boolean isProcessed = transferBalance(userEntry.getId(), master_id,
														toBeAddedBalance);
												if (isProcessed) {
													userWallet -= toBeAddedBalance;
													adminWallet += toBeAddedBalance;
												} else {
													logger.info(userEntry.getSystemId()
															+ "<User> has Insufficient Amount to Deduct");
													isSufficient = false;
												}
											} else {
												logger.info(userEntry.getSystemId()
														+ "<User> has Insufficient Amount to Deduct");
												isSufficient = false;
											}
										}
									}
								} else {
									if (userForm.getRechargeEntry().getOperation().equalsIgnoreCase("plus")) {
										if (userEntry.isAdminDepend()) { // inherited user
											long allocatedCredits = 0;
											Predicate p = new PredicateBuilderImpl().getEntryObject().get("masterId")
													.equal(userEntry.getMasterId());
											Collection<UserEntry> users = GlobalVars.UserEntries.values(p);
											logger.info(master_system_id + " Depend Users: " + users.size());
											for (UserEntry child : users) {
												BalanceEntry child_balance = GlobalVars.BalanceEntries
														.get(child.getId());
												allocatedCredits += child_balance.getCredits();
											}
											logger.info(master_system_id + "[" + adminCredits
													+ "] Previous Allocated Credits: " + allocatedCredits);
											if (((toBeAddedCredits + allocatedCredits) <= (adminCredits
													* IConstants.BALANCE__MULTIPLY_LIMIT))) {
												logger.info("Credit Addition " + toBeAddedCredits + "  From: "
														+ master_system_id + "[" + adminCredits + "] To Child: "
														+ userEntry.getSystemId() + "[" + userCredits + "]");
												boolean isProcessed = addCredits(userEntry.getId(),
														(int) toBeAddedCredits);
												if (isProcessed) { // Processing from smpp server
													userCredits += toBeAddedCredits;
												} else {
													logger.info(master_system_id + " <Admin> has Insufficient Credit");
													isSufficient = false;
												}
											} else {
												logger.info(master_system_id + " <Admin> has Insufficient Credit");
												isSufficient = false;
											}
										} else {
											logger.info("Credits " + toBeAddedCredits + " Transfer From: "
													+ master_system_id + "[" + adminCredits + "] To: "
													+ userEntry.getSystemId() + "[" + userCredits + "]");
											if ((adminCredits > toBeAddedCredits)) {
												boolean isProcessed = transferCredits(master_id, userEntry.getId(),
														(int) toBeAddedCredits);
												if (isProcessed) { // Processing from smpp server
													userCredits += toBeAddedCredits;
													adminCredits -= toBeAddedCredits;
												} else {
													logger.info(master_system_id + " <Admin> has Insufficient Credit");
													isSufficient = false;
												}
											} else {
												logger.info(master_system_id + " <Admin> has Insufficient Credit");
												isSufficient = false;
											}
										}
									} else {
										if (userEntry.isAdminDepend()) { // inherited user
											logger.info("Credits " + toBeAddedCredits + " Deduction From: "
													+ master_system_id + "[" + adminCredits + "] To depend Child: "
													+ userEntry.getSystemId() + "[" + userCredits + "]");
											if ((userCredits - toBeAddedCredits) >= 0) {
												boolean isProcessed = deductCredits(userEntry.getId(),
														(int) toBeAddedCredits);
												if (isProcessed) { // Processing from smpp server
													userCredits -= toBeAddedCredits;
												} else {
													logger.info(userEntry.getSystemId()
															+ " <User> has Insufficient Credit to deduct");
													isSufficient = false;
												}
											} else {
												logger.info(userEntry.getSystemId()
														+ " <User> has Insufficient Credit to deduct");
												isSufficient = false;
											}
										} else {
											logger.info("Credits " + toBeAddedCredits + " Deduction From: "
													+ master_system_id + "[" + adminCredits + "] To: "
													+ userEntry.getSystemId() + "[" + userCredits + "]");
											if ((userCredits - toBeAddedCredits) >= 0) {
												boolean isProcessed = transferCredits(userEntry.getId(), master_id,
														(int) toBeAddedCredits);
												if (isProcessed) { // Processing from smpp server
													userCredits -= toBeAddedCredits;
													adminCredits += toBeAddedCredits;
												} else {
													logger.info(userEntry.getSystemId()
															+ " <User> has Insufficient Credit to deduct");
													isSufficient = false;
												}
											} else {
												logger.info(userEntry.getSystemId()
														+ " <User> has Insufficient Credit to deduct");
												isSufficient = false;
											}
										}
									}
								}
								if (isSufficient) {
									logger.info(master_system_id + " Setting Balance Updation Changes");
									// ----------- save Recharge Entry -------------------------------
									rechargeEntryAdmin.setEffectiveWallet(adminWallet);
									rechargeEntryAdmin.setEffectiveCredits(adminCredits);
									if (userForm.getRechargeEntry().getType().equalsIgnoreCase("international")) {
										rechargeEntryAdmin.setPayTerm(userForm.getRechargeEntry().getPayTerm());
										rechargeEntryAdmin.setPayType(userForm.getRechargeEntry().getPayType());
										rechargeEntryAdmin.setApproveBy(userForm.getRechargeEntry().getApproveBy());
										rechargeEntryAdmin.setPaymethod(userForm.getRechargeEntry().getPaymethod());
									}
									saveRechargeEntry(rechargeEntryAdmin);
									rechargeEntryUser.setEffectiveWallet(userWallet);
									rechargeEntryUser.setEffectiveCredits(userCredits);
									if (userForm.getRechargeEntry().getType().equalsIgnoreCase("international")) {
										rechargeEntryUser.setPayTerm(userForm.getRechargeEntry().getPayTerm());
										rechargeEntryUser.setPayType(userForm.getRechargeEntry().getPayType());
										// rechargeEntryUser.setAddedBy(userForm.getRechargeEntry().getAddedBy());
										rechargeEntryUser.setApproveBy(userForm.getRechargeEntry().getApproveBy());
										rechargeEntryUser.setPaymethod(userForm.getRechargeEntry().getPaymethod());
									}
									saveRechargeEntry(rechargeEntryUser);
									target = IConstants.SUCCESS_KEY;
								} else {
									throw new InsufficientBalanceException("exception.insufficientBalance");
								}
								// ----------------------------- super/system recharge other End --------
							} catch (Exception e) {
								logger.info(userSessionObject.getSystemId(), e.fillInStackTrace());
								String errorMessage = "Process Error: " + e.getMessage() + "[" + e.getCause() + "]";
								throw new ProcessErrorException(errorMessage, e);
							}
						}
					}
				} else {
					if (userEntry.getMasterId().equalsIgnoreCase(userSessionObject.getSystemId())) { // primary master
						try {
							logger.info("Balance " + userForm.getRechargeEntry().getOperation() + " Request From "
									+ userSessionObject.getSystemId() + " To: " + userEntry.getSystemId() + " Wallet: "
									+ toBeAddedBalance + " Credit: " + toBeAddedCredits);
							String user_particular = null;
							if (operation.equalsIgnoreCase("plus")) {
								user_particular = "cr_" + userSessionObject.getSystemId();
							} else {
								user_particular = "dr_" + userSessionObject.getSystemId();
							}
							String admin_particular = null;
							if (operation.equalsIgnoreCase("plus")) {
								admin_particular = "dr_" + userEntry.getSystemId();
							} else {
								admin_particular = "cr_" + userEntry.getSystemId();
							}
							BalanceEntry target_balance = GlobalVars.BalanceEntries.get(userEntry.getId());
							rechargeEntryUser = new RechargeEntry(userEntry.getId(),
									new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), user_particular,
									target_balance.getCredits(), toBeAddedCredits, target_balance.getWalletAmount(),
									toBeAddedBalance, userForm.getRemark(), userForm.getRechargeEntry().getType(),
									userSessionObject.getSystemId());
							RechargeEntry rechargeEntryAdmin = new RechargeEntry(userSessionObject.getId(),
									new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), admin_particular,
									userSessionObject.getBalance().getCredits(), toBeAddedCredits,
									userSessionObject.getBalance().getWalletAmount(), toBeAddedBalance,
									userForm.getRemark(), userForm.getRechargeEntry().getType(),
									userSessionObject.getSystemId());
							// ---------------------------------------------------------------
							double adminWallet = userSessionObject.getBalance().getWalletAmount();
							double userWallet = target_balance.getWalletAmount();
							long adminCredits = userSessionObject.getBalance().getCredits();
							long userCredits = target_balance.getCredits();
							if (userForm.getBalance().getWalletFlag().equalsIgnoreCase("yes")) {
								if (userForm.getRechargeEntry().getOperation().equalsIgnoreCase("plus")) {
									if (userEntry.isAdminDepend()) { // inherited user
										double allocatedBalance = 0;
										Predicate p = new PredicateBuilderImpl().getEntryObject().get("masterId")
												.equal(userEntry.getMasterId());
										Collection<UserEntry> users = GlobalVars.UserEntries.values(p);
										logger.info(userSessionObject.getSystemId() + " Depend Users: " + users.size());
										for (UserEntry child : users) {
											BalanceEntry child_balance = GlobalVars.BalanceEntries.get(child.getId());
											allocatedBalance += child_balance.getWalletAmount();
										}
										logger.info(userSessionObject.getSystemId() + "[" + adminWallet
												+ "] Previous Allocated Balance: " + allocatedBalance);
										if ((toBeAddedBalance + allocatedBalance) <= (adminWallet
												* IConstants.BALANCE__MULTIPLY_LIMIT)) {
											logger.info("Adding " + toBeAddedBalance + " From: "
													+ userSessionObject.getSystemId() + " To Child: "
													+ userEntry.getSystemId() + " [" + userWallet + "]");
											boolean isProcessed = addBalance(userEntry.getId(), toBeAddedBalance);
											if (isProcessed) {
												logger.info(userSessionObject.getSystemId()
														+ " <Admin> has Sufficient Amount");
												userWallet += toBeAddedBalance;
											} else {
												logger.info(userSessionObject.getSystemId()
														+ " <Admin> has Insufficient Amount");
												isSufficient = false;
											}
										} else {
											logger.info(userSessionObject.getSystemId()
													+ " <Admin> has Insufficient Amount: " + adminWallet);
											isSufficient = false;
										}
									} else {
										if (adminWallet > toBeAddedBalance) {
											logger.info("Transfer " + toBeAddedBalance + " From: "
													+ userSessionObject.getSystemId() + "[" + adminWallet + "] To: "
													+ userEntry.getSystemId() + "[" + userWallet + "]");
											boolean isProcessed = transferBalance(userSessionObject.getId(),
													userEntry.getId(), toBeAddedBalance);
											if (isProcessed) {
												userWallet += toBeAddedBalance;
												adminWallet -= toBeAddedBalance;
											} else {
												logger.info(userSessionObject.getSystemId()
														+ " <Admin> has Insufficient Amount");
												isSufficient = false;
											}
										} else {
											logger.info(userSessionObject.getSystemId()
													+ " <Admin> has Insufficient Amount: " + adminWallet);
											isSufficient = false;
										}
									}
								} else {
									if (userEntry.isAdminDepend()) { // inherited user
										logger.info("Deduction " + toBeAddedBalance + " From: "
												+ userSessionObject.getSystemId() + "[" + adminWallet + "] To Child : "
												+ userEntry.getSystemId() + "[" + userWallet + "]");
										if ((userWallet - toBeAddedBalance) >= 0) {
											boolean isProcessed = deductBalance(userEntry.getId(), toBeAddedBalance);
											if (isProcessed) {
												userWallet -= toBeAddedBalance;
											} else {
												logger.info(userEntry.getSystemId()
														+ "<User> has Insufficient Amount to Deduct");
												isSufficient = false;
											}
										} else {
											logger.info(userEntry.getSystemId()
													+ "<User> has Insufficient Amount to Deduct");
											isSufficient = false;
										}
									} else {
										logger.info("Deduction " + toBeAddedBalance + " From: "
												+ userSessionObject.getSystemId() + "[" + adminWallet + "] To : "
												+ userEntry.getSystemId() + "[" + userWallet + "]");
										if ((userWallet - toBeAddedBalance) >= 0) {
											boolean isProcessed = transferBalance(userEntry.getId(),
													userSessionObject.getId(), toBeAddedBalance);
											if (isProcessed) {
												userWallet -= toBeAddedBalance;
												adminWallet += toBeAddedBalance;
											} else {
												logger.info(userEntry.getSystemId()
														+ "<User> has Insufficient Amount to Deduct");
												isSufficient = false;
											}
										} else {
											logger.info(userEntry.getSystemId()
													+ "<User> has Insufficient Amount to Deduct");
											isSufficient = false;
										}
									}
								}
							} else {
								if (userForm.getRechargeEntry().getOperation().equalsIgnoreCase("plus")) {
									if (userEntry.isAdminDepend()) { // inherited user
										long allocatedCredits = 0;
										Predicate p = new PredicateBuilderImpl().getEntryObject().get("masterId")
												.equal(userEntry.getMasterId());
										Collection<UserEntry> users = GlobalVars.UserEntries.values(p);
										logger.info(userSessionObject.getSystemId() + " Depend Users: " + users.size());
										for (UserEntry child : users) {
											BalanceEntry child_balance = GlobalVars.BalanceEntries.get(child.getId());
											allocatedCredits += child_balance.getCredits();
										}
										logger.info(userSessionObject.getSystemId() + "[" + adminCredits
												+ "] Previous Allocated Credits: " + allocatedCredits);
										if (((toBeAddedCredits + allocatedCredits) <= (adminCredits
												* IConstants.BALANCE__MULTIPLY_LIMIT))) {
											logger.info("Credit Addition " + toBeAddedCredits + "  From: "
													+ userSessionObject.getSystemId() + "[" + adminCredits
													+ "] To Child: " + userEntry.getSystemId() + "[" + userCredits
													+ "]");
											boolean isProcessed = addCredits(userEntry.getId(), (int) toBeAddedCredits);
											if (isProcessed) { // Processing from smpp server
												userCredits += toBeAddedCredits;
											} else {
												logger.info(userSessionObject.getSystemId()
														+ " <Admin> has Insufficient Credit");
												isSufficient = false;
											}
										} else {
											logger.info(userSessionObject.getSystemId()
													+ " <Admin> has Insufficient Credit");
											isSufficient = false;
										}
									} else {
										logger.info("Credits " + toBeAddedCredits + " Transfer From: "
												+ userSessionObject.getSystemId() + "[" + adminCredits + "] To: "
												+ userEntry.getSystemId() + "[" + userCredits + "]");
										if ((adminCredits > toBeAddedCredits)) {
											boolean isProcessed = transferCredits(userSessionObject.getId(),
													userEntry.getId(), (int) toBeAddedCredits);
											if (isProcessed) { // Processing from smpp server
												userCredits += toBeAddedCredits;
												adminCredits -= toBeAddedCredits;
											} else {
												logger.info(userSessionObject.getSystemId()
														+ " <Admin> has Insufficient Credit");
												isSufficient = false;
											}
										} else {
											logger.info(userSessionObject.getSystemId()
													+ " <Admin> has Insufficient Credit");
											isSufficient = false;
										}
									}
								} else {
									if (userEntry.isAdminDepend()) { // inherited user
										logger.info("Credits " + toBeAddedCredits + " Deduction From: "
												+ userSessionObject.getSystemId() + "[" + adminCredits + "] To Child: "
												+ userEntry.getSystemId() + "[" + userCredits + "]");
										if ((userCredits - toBeAddedCredits) >= 0) {
											boolean isProcessed = deductCredits(userEntry.getId(),
													(int) toBeAddedCredits);
											if (isProcessed) { // Processing from smpp server
												userCredits -= toBeAddedCredits;
											} else {
												logger.info(userEntry.getSystemId()
														+ " <User> has Insufficient Credit to deduct");
												isSufficient = false;
											}
										} else {
											logger.info(userEntry.getSystemId()
													+ " <User> has Insufficient Credit to deduct");
											isSufficient = false;
										}
									} else {
										logger.info("Credits " + toBeAddedCredits + " Deduction From: "
												+ userSessionObject.getSystemId() + "[" + adminCredits + "] To: "
												+ userEntry.getSystemId() + "[" + userCredits + "]");
										if ((userCredits - toBeAddedCredits) >= 0) {
											boolean isProcessed = transferCredits(userEntry.getId(),
													userSessionObject.getId(), (int) toBeAddedCredits);
											if (isProcessed) { // Processing from smpp server
												userCredits -= toBeAddedCredits;
												adminCredits += toBeAddedCredits;
											} else {
												logger.info(userEntry.getSystemId()
														+ " <User> has Insufficient Credit to deduct");
												isSufficient = false;
											}
										} else {
											logger.info(userEntry.getSystemId()
													+ " <User> has Insufficient Credit to deduct");
											isSufficient = false;
										}
									}
								}
							}
							if (isSufficient) {
								logger.info(userSessionObject.getSystemId() + " Setting Balance Updation Changes");
								// -------------------- set Session Changes ----------------------
								userSessionObject.getBalance().setWalletAmount(adminWallet);
								userSessionObject.getBalance().setCredits(adminCredits);
								// ----------- save Recharge Entry -------------------------------
								rechargeEntryAdmin.setEffectiveWallet(adminWallet);
								rechargeEntryAdmin.setEffectiveCredits(adminCredits);
								if (userForm.getRechargeEntry().getType().equalsIgnoreCase("international")) {
									rechargeEntryAdmin.setPayTerm(userForm.getRechargeEntry().getPayTerm());
									rechargeEntryAdmin.setPayType(userForm.getRechargeEntry().getPayType());
									rechargeEntryAdmin.setApproveBy(userForm.getRechargeEntry().getApproveBy());
									rechargeEntryAdmin.setPaymethod(userForm.getRechargeEntry().getPaymethod());
								}
								saveRechargeEntry(rechargeEntryAdmin);
								rechargeEntryUser.setEffectiveWallet(userWallet);
								rechargeEntryUser.setEffectiveCredits(userCredits);
								if (userForm.getRechargeEntry().getType().equalsIgnoreCase("international")) {
									rechargeEntryUser.setPayTerm(userForm.getRechargeEntry().getPayTerm());
									rechargeEntryUser.setPayType(userForm.getRechargeEntry().getPayType());
									rechargeEntryUser.setApproveBy(userForm.getRechargeEntry().getApproveBy());
									rechargeEntryUser.setPaymethod(userForm.getRechargeEntry().getPaymethod());
								}
								saveRechargeEntry(rechargeEntryUser);
								target = IConstants.SUCCESS_KEY;
							} else {
								throw new InsufficientBalanceException("exception.insufficientBalance");
							}
						} catch (Exception e) {
							logger.info(userSessionObject.getSystemId(), e.fillInStackTrace());
							String errorMessage = "Process Error: " + e.getMessage() + "[" + e.getCause() + "]";
							throw new ProcessErrorException(errorMessage, e);
						}
					} else { // secondry master
						WebMasterEntry webEntry = GlobalVars.WebmasterEntries.get(userEntry.getId());
						if (webEntry.getSecondaryMaster() != null
								&& webEntry.getSecondaryMaster().equalsIgnoreCase(userSessionObject.getSystemId())) {
							try {
								// ---------------------------------------------------------------------
								logger.info("Balance " + userForm.getRechargeEntry().getOperation() + " Request From "
										+ userSessionObject.getSystemId() + " To: " + userEntry.getSystemId() + " C/O "
										+ userEntry.getMasterId() + " Wallet: " + toBeAddedBalance + " Credit: "
										+ toBeAddedCredits);
								String master_system_id = userEntry.getMasterId();
								int master_id = GlobalVars.UserMapping.get(master_system_id);
								// UserEntry masterEntry = GlobalVars.UserEntries.get(master_id);
								BalanceEntry master_balance = GlobalVars.BalanceEntries.get(master_id);
								String user_particular = null;
								if (operation.equalsIgnoreCase("plus")) {
									user_particular = "cr_" + userEntry.getMasterId();
								} else {
									user_particular = "dr_" + userEntry.getMasterId();
								}
								String admin_particular = null;
								if (operation.equalsIgnoreCase("plus")) {
									admin_particular = "dr_" + userEntry.getSystemId();
								} else {
									admin_particular = "cr_" + userEntry.getSystemId();
								}
								BalanceEntry target_balance = GlobalVars.BalanceEntries.get(userEntry.getId());
								rechargeEntryUser = new RechargeEntry(userEntry.getId(),
										new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), user_particular,
										target_balance.getCredits(), toBeAddedCredits, target_balance.getWalletAmount(),
										toBeAddedBalance, userForm.getRemark(), userForm.getRechargeEntry().getType(),
										userSessionObject.getSystemId());
								RechargeEntry rechargeEntryAdmin = new RechargeEntry(master_id,
										new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
										admin_particular, master_balance.getCredits(), toBeAddedCredits,
										master_balance.getWalletAmount(), toBeAddedBalance, userForm.getRemark(),
										userForm.getRechargeEntry().getType(), userSessionObject.getSystemId());
								// ---------------------------------------------------------------
								double adminWallet = master_balance.getWalletAmount();
								double userWallet = target_balance.getWalletAmount();
								long adminCredits = master_balance.getCredits();
								long userCredits = target_balance.getCredits();
								if (userForm.getBalance().getWalletFlag().equalsIgnoreCase("yes")) {
									if (userForm.getRechargeEntry().getOperation().equalsIgnoreCase("plus")) {
										if (userEntry.isAdminDepend()) { // inherited user
											double allocatedBalance = 0;
											Predicate p = new PredicateBuilderImpl().getEntryObject().get("masterId")
													.equal(userEntry.getMasterId());
											Collection<UserEntry> users = GlobalVars.UserEntries.values(p);
											logger.info(master_system_id + " Depend Users: " + users.size());
											for (UserEntry child : users) {
												BalanceEntry child_balance = GlobalVars.BalanceEntries
														.get(child.getId());
												allocatedBalance += child_balance.getWalletAmount();
											}
											logger.info(master_system_id + "[" + adminWallet
													+ "] Previous Allocated Balance: " + allocatedBalance);
											if ((toBeAddedBalance + allocatedBalance) <= (adminWallet
													* IConstants.BALANCE__MULTIPLY_LIMIT)) {
												logger.info("Adding " + toBeAddedBalance + " From: " + master_system_id
														+ " To Child: " + userEntry.getSystemId() + " [" + userWallet
														+ "]");
												boolean isProcessed = addBalance(userEntry.getId(), toBeAddedBalance);
												if (isProcessed) {
													logger.info(master_system_id + " <Admin> has Sufficient Amount");
													userWallet += toBeAddedBalance;
												} else {
													logger.info(master_system_id + " <Admin> has Insufficient Amount");
													isSufficient = false;
												}
											} else {
												logger.info(master_system_id + " <Admin> has Insufficient Amount: "
														+ adminWallet);
												isSufficient = false;
											}
										} else {
											if (adminWallet > toBeAddedBalance) {
												logger.info("Transfer " + toBeAddedBalance + " From: "
														+ master_system_id + "[" + adminWallet + "] To: "
														+ userEntry.getSystemId() + "[" + userWallet + "]");
												boolean isProcessed = transferBalance(master_id, userEntry.getId(),
														toBeAddedBalance);
												if (isProcessed) {
													userWallet += toBeAddedBalance;
													adminWallet -= toBeAddedBalance;
												} else {
													logger.info(master_system_id + " <Admin> has Insufficient Amount");
													isSufficient = false;
												}
											} else {
												logger.info(master_system_id + " <Admin> has Insufficient Amount: "
														+ adminWallet);
												isSufficient = false;
											}
										}
									} else {
										if (userEntry.isAdminDepend()) { // inherited user
											logger.info("Deduction " + toBeAddedBalance + " From: " + master_system_id
													+ "[" + adminWallet + "] To Child : " + userEntry.getSystemId()
													+ "[" + userWallet + "]");
											if ((userWallet - toBeAddedBalance) >= 0) {
												boolean isProcessed = deductBalance(userEntry.getId(),
														toBeAddedBalance);
												if (isProcessed) {
													userWallet -= toBeAddedBalance;
												} else {
													logger.info(userEntry.getSystemId()
															+ "<User> has Insufficient Amount to Deduct");
													isSufficient = false;
												}
											} else {
												logger.info(userEntry.getSystemId()
														+ "<User> has Insufficient Amount to Deduct");
												isSufficient = false;
											}
										} else {
											logger.info("Deduction " + toBeAddedBalance + " From: " + master_system_id
													+ "[" + adminWallet + "] To : " + userEntry.getSystemId() + "["
													+ userWallet + "]");
											if ((userWallet - toBeAddedBalance) >= 0) {
												boolean isProcessed = transferBalance(userEntry.getId(), master_id,
														toBeAddedBalance);
												if (isProcessed) {
													userWallet -= toBeAddedBalance;
													adminWallet += toBeAddedBalance;
												} else {
													logger.info(userEntry.getSystemId()
															+ "<User> has Insufficient Amount to Deduct");
													isSufficient = false;
												}
											} else {
												logger.info(userEntry.getSystemId()
														+ "<User> has Insufficient Amount to Deduct");
												isSufficient = false;
											}
										}
									}
								} else {
									if (userForm.getRechargeEntry().getOperation().equalsIgnoreCase("plus")) {
										if (userEntry.isAdminDepend()) { // inherited user
											long allocatedCredits = 0;
											Predicate p = new PredicateBuilderImpl().getEntryObject().get("masterId")
													.equal(userEntry.getMasterId());
											Collection<UserEntry> users = GlobalVars.UserEntries.values(p);
											logger.info(master_system_id + " Depend Users: " + users.size());
											for (UserEntry child : users) {
												BalanceEntry child_balance = GlobalVars.BalanceEntries
														.get(child.getId());
												allocatedCredits += child_balance.getCredits();
											}
											logger.info(master_system_id + "[" + adminCredits
													+ "] Previous Allocated Credits: " + allocatedCredits);
											if (((toBeAddedCredits + allocatedCredits) <= (adminCredits
													* IConstants.BALANCE__MULTIPLY_LIMIT))) {
												logger.info("Credit Addition " + toBeAddedCredits + "  From: "
														+ master_system_id + "[" + adminCredits + "] To Child: "
														+ userEntry.getSystemId() + "[" + userCredits + "]");
												boolean isProcessed = addCredits(userEntry.getId(),
														(int) toBeAddedCredits);
												if (isProcessed) { // Processing from smpp server
													userCredits += toBeAddedCredits;
												} else {
													logger.info(master_system_id + " <Admin> has Insufficient Credit");
													isSufficient = false;
												}
											} else {
												logger.info(master_system_id + " <Admin> has Insufficient Credit");
												isSufficient = false;
											}
										} else {
											logger.info("Credits " + toBeAddedCredits + " Transfer From: "
													+ master_system_id + "[" + adminCredits + "] To: "
													+ userEntry.getSystemId() + "[" + userCredits + "]");
											if ((adminCredits > toBeAddedCredits)) {
												boolean isProcessed = transferCredits(master_id, userEntry.getId(),
														(int) toBeAddedCredits);
												if (isProcessed) { // Processing from smpp server
													userCredits += toBeAddedCredits;
													adminCredits -= toBeAddedCredits;
												} else {
													logger.info(master_system_id + " <Admin> has Insufficient Credit");
													isSufficient = false;
												}
											} else {
												logger.info(master_system_id + " <Admin> has Insufficient Credit");
												isSufficient = false;
											}
										}
									} else {
										if (userEntry.isAdminDepend()) { // inherited user
											logger.info("Credits " + toBeAddedCredits + " Deduction From: "
													+ master_system_id + "[" + adminCredits + "] To depend Child: "
													+ userEntry.getSystemId() + "[" + userCredits + "]");
											if ((userCredits - toBeAddedCredits) >= 0) {
												boolean isProcessed = deductCredits(userEntry.getId(),
														(int) toBeAddedCredits);
												if (isProcessed) { // Processing from smpp server
													userCredits -= toBeAddedCredits;
												} else {
													logger.info(userEntry.getSystemId()
															+ " <User> has Insufficient Credit to deduct");
													isSufficient = false;
												}
											} else {
												logger.info(userEntry.getSystemId()
														+ " <User> has Insufficient Credit to deduct");
												isSufficient = false;
											}
										} else {
											logger.info("Credits " + toBeAddedCredits + " Deduction From: "
													+ master_system_id + "[" + adminCredits + "] To: "
													+ userEntry.getSystemId() + "[" + userCredits + "]");
											if ((userCredits - toBeAddedCredits) >= 0) {
												boolean isProcessed = transferCredits(userEntry.getId(), master_id,
														(int) toBeAddedCredits);
												if (isProcessed) { // Processing from smpp server
													userCredits -= toBeAddedCredits;
													adminCredits += toBeAddedCredits;
												} else {
													logger.info(userEntry.getSystemId()
															+ " <User> has Insufficient Credit to deduct");
													isSufficient = false;
												}
											} else {
												logger.info(userEntry.getSystemId()
														+ " <User> has Insufficient Credit to deduct");
												isSufficient = false;
											}
										}
									}
								}
								if (isSufficient) {
									logger.info(master_system_id + " Setting Balance Updation Changes");
									// ----------- save Recharge Entry -------------------------------
									rechargeEntryAdmin.setEffectiveWallet(adminWallet);
									rechargeEntryAdmin.setEffectiveCredits(adminCredits);
									if (userForm.getRechargeEntry().getType().equalsIgnoreCase("international")) {
										rechargeEntryAdmin.setPayTerm(userForm.getRechargeEntry().getPayTerm());
										rechargeEntryAdmin.setPayType(userForm.getRechargeEntry().getPayType());
										rechargeEntryAdmin.setApproveBy(userForm.getRechargeEntry().getApproveBy());
										rechargeEntryAdmin.setPaymethod(userForm.getRechargeEntry().getPaymethod());
									}
									saveRechargeEntry(rechargeEntryAdmin);
									rechargeEntryUser.setEffectiveWallet(userWallet);
									rechargeEntryUser.setEffectiveCredits(userCredits);
									if (userForm.getRechargeEntry().getType().equalsIgnoreCase("international")) {
										rechargeEntryUser.setPayTerm(userForm.getRechargeEntry().getPayTerm());
										rechargeEntryUser.setPayType(userForm.getRechargeEntry().getPayType());
										// rechargeEntryUser.setAddedBy(userForm.getRechargeEntry().getAddedBy());
										rechargeEntryUser.setApproveBy(userForm.getRechargeEntry().getApproveBy());
										rechargeEntryUser.setPaymethod(userForm.getRechargeEntry().getPaymethod());
									}
									saveRechargeEntry(rechargeEntryUser);
									target = IConstants.SUCCESS_KEY;
								} else {
									throw new InsufficientBalanceException("exception.insufficientBalance");
								}
								// ----------------------------- super/system recharge other End --------
							} catch (Exception e) {
								logger.info(userSessionObject.getSystemId(), e.fillInStackTrace());
								String errorMessage = "Process Error: " + e.getMessage() + "[" + e.getCause() + "]";
								throw new ProcessErrorException(errorMessage, e);
							}
						} else {
							target = "invalidRequest";
						}
					}
				}
				if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
					try {
						String flagVal = GlobalVars.UserFlagStatus.get(userEntry.getSystemId());
						if ((flagVal != null) && (!flagVal.equalsIgnoreCase(FlagStatus.BLOCKED))) {
							MultiUtility.changeFlag(Constants.USER_FLAG_DIR + userEntry.getSystemId() + ".txt",
									FlagStatus.BALANCE_REFRESH); // reload user account details
						}
						if (!userEntry.getSystemId().equalsIgnoreCase(userEntry.getMasterId())) {
							String masterflagVal = GlobalVars.UserFlagStatus.get(userEntry.getMasterId());
							if (masterflagVal != null && !masterflagVal.equalsIgnoreCase(FlagStatus.BLOCKED)) {
								MultiUtility.changeFlag(Constants.USER_FLAG_DIR + userEntry.getMasterId() + ".txt",
										FlagStatus.BALANCE_REFRESH); // reload user account details
							}
						}
						MultiUtility.changeFlag(Constants.CLIENT_FLAG_FILE, FlagStatus.REFRESH); // command to reload
																									// users flag files
					} catch (Exception e) {
						logger.error(userSessionObject.getSystemId(), e);
					}
				}
				if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY) && rechargeEntryUser != null) {
					try {
						Integer[] userid = new Integer[1];
						userid[0] = userForm.getId();
						List<RechargeEntry> txnlist = new ArrayList<RechargeEntry>();
						Collection<List<RechargeEntry>> transactions = listTransactions(userid).values();
						for (List<RechargeEntry> list : transactions) {
							txnlist.addAll(list);
						}
						List<RechargeEntry> sortedTxnList = sortList(txnlist);

						return (ResponseEntity<?>) sortedTxnList;
					} catch (Exception e) {
						logger.info(userSessionObject.getSystemId(), e.fillInStackTrace());
						String errorMessage = "Process Error: " + e.getMessage() + "[" + e.getCause() + "]";
						throw new ProcessErrorException(errorMessage, e);
					}
				}
			} else {
				throw new InsufficientBalanceException("exception.insufficientBalance");
			}
	 
		} else {
			target = "invalidRequest";
		}
		logger.info(userSessionObject.getSystemId() + "[" + userSessionObject.getRole() + "] Update Balance Target: "
				+ target);

	}

	public Map<Integer, List<RechargeEntry>> listTransactions(Integer[] userid) {
		return listTransactions(userid, null, null, null);
	}

	public Map<Integer, List<RechargeEntry>> listTransactions(Integer[] userid, String txnType, String startTime,
			String endTime) {
		logger.debug("listTransactions(" + userid + ")");
		// Map<Integer, UserEntryExt> usermap = listUserBalance();
		Map<Integer, List<RechargeEntry>> map = new HashMap<Integer, List<RechargeEntry>>();
		List<RechargeEntry> list = listTransactions1(userid, txnType, startTime, endTime);
		for (RechargeEntry entry : list) {
			String particular = entry.getParticular();
			String[] particular_arr = particular.split("_");
			entry.setParticular(particular_arr[0]);
			String effectiveUser = particular_arr[1];
			entry.setEffectiveUser(effectiveUser);
			if (GlobalVars.UserEntries.containsKey(entry.getUserId())) {
				UserEntry userEntry = GlobalVars.UserEntries.get(entry.getUserId());
				BalanceEntry balance = GlobalVars.BalanceEntries.get(entry.getUserId());
				logger.debug(userEntry.getSystemId() + "[" + userEntry.getRole() + "]: " + entry);
				entry.setSystemId(userEntry.getSystemId());
				if (userEntry.getRole().equalsIgnoreCase("superadmin")
						|| userEntry.getRole().equalsIgnoreCase("system")) {
					if (GlobalVars.UserMapping.containsKey(effectiveUser)) {
						int effective_user_id = GlobalVars.UserMapping.get(effectiveUser);
						if (GlobalVars.BalanceEntries.containsKey(effective_user_id)) {
							BalanceEntry effectiveBalanceEntry = GlobalVars.BalanceEntries.get(effective_user_id);
							entry.setWalletFlag(effectiveBalanceEntry.getWalletFlag());
						} else {
							entry.setWalletFlag(balance.getWalletFlag());
						}
					} else {
						entry.setWalletFlag(balance.getWalletFlag());
					}
				} else {
					entry.setWalletFlag(balance.getWalletFlag());
				}
			}
			List<RechargeEntry> entrylist = null;
			if (map.containsKey(entry.getUserId())) {
				entrylist = map.get(entry.getUserId());
			} else {
				entrylist = new ArrayList<RechargeEntry>();
			}
			entrylist.add(entry);
			map.put(entry.getUserId(), entrylist);
		}
		return map;
	}

	public List<RechargeEntry> listTransactions1(Integer[] userId, String txnType, String startTime, String endTime) {
		Transaction transaction = null;
		List<RechargeEntry> results = new ArrayList<>();

		try (Session session = sessionFactory.openSession()) {
			transaction = session.beginTransaction();

			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<RechargeEntry> query = builder.createQuery(RechargeEntry.class);
			Root<RechargeEntry> root = query.from(RechargeEntry.class);

//		            Configuration configuration = new Configuration().configure(); // Loads hibernate.cfg.xml
//		            StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
//		                .applySettings(configuration.getProperties());
//		            SessionFactory sessionFactory = configuration.buildSessionFactory(builder.build());

			if (userId != null && userId.length > 0) {
				query.where(root.get("userId").in((Object[]) userId));
			}
			if (txnType != null) {
				query.where(builder.like(root.get("txnType"), "%" + txnType + "%"));
			}
			if (startTime != null && endTime != null) {
				query.where(builder.between(root.get("time"), startTime, endTime));
			}
			results = session.createQuery(query).getResultList();
			transaction.commit();

			logger.info("Transactions fetched: {}", results.size());
		} catch (Exception e) {
			if (transaction != null) {
				transaction.rollback();
			}
			logger.error("Error retrieving transactions: {}", e.getMessage());
			return Collections.emptyList();
		}
		return results;
	}

	private List<RechargeEntry> sortList(List<RechargeEntry> list) {
		try {
			List<RechargeEntry> sortedlist = list.stream()
					.sorted(Comparator.comparing(RechargeEntry::getTime).reversed()).collect(Collectors.toList());
			return sortedlist;
		} catch (Exception ex) {
			logger.info("Transaction list Sorting Error: " + ex);
			return list;
		}
	}

	private boolean deductCredits(int userId, int amount) throws EntryNotFoundException {
		if (GlobalVars.BalanceEntries.containsKey(userId)) {
			try {
				GlobalVars.BalanceEntries.lock(userId);
				BalanceEntry balanceEntry = GlobalVars.BalanceEntries.get(userId);
				if (balanceEntry.getCredits() >= amount) {
					balanceEntry.setCredits(balanceEntry.getCredits() - amount);
					balanceEntry.setActive(true);
					logger.info(userId + " Effective Credits -->" + balanceEntry.getCredits());
				} else {
					logger.error(userId + " Insufficient Credits -->" + balanceEntry.getCredits());
				}
				GlobalVars.BalanceEntries.put(userId, balanceEntry);
			} finally {
				GlobalVars.BalanceEntries.unlock(userId);
			}
			return true;
		} else {
			throw new EntryNotFoundException(userId + " BalanceEntry Not Found");
		}
	}

	public boolean transferCredits(int from, int to, int amount) throws EntryNotFoundException {
		logger.info("Transfer From: " + from + " To: " + to + " Credits:" + amount);
		boolean proceed = false;
		if (!GlobalVars.BalanceEntries.containsKey(to)) {
			throw new EntryNotFoundException(to + " BalanceEntry");
		}
		if (!GlobalVars.BalanceEntries.containsKey(from)) {
			throw new EntryNotFoundException(from + " BalanceEntry");
		}
		try {
			GlobalVars.BalanceEntries.lock(from);
			BalanceEntry from_balance = GlobalVars.BalanceEntries.get(from);
			if (from_balance.getCredits() > amount) {
				from_balance.setCredits(from_balance.getCredits() - amount);
				logger.info(from + " Remaining Credits -->" + from_balance.getCredits());
				from_balance.setActive(true);
				proceed = true;
			} else {
				logger.error(from + " Insufficient Credits -->" + from_balance.getCredits());
			}
			GlobalVars.BalanceEntries.put(from, from_balance);
		} finally {
			GlobalVars.BalanceEntries.unlock(from);
		}
		if (proceed) {
			try {
				GlobalVars.BalanceEntries.lock(to);
				BalanceEntry to_balance = GlobalVars.BalanceEntries.get(to);
				to_balance.setCredits(to_balance.getCredits() + amount);
				to_balance.setActive(true);
				GlobalVars.BalanceEntries.put(to, to_balance);
				logger.info(to + " Effective Credits -->" + to_balance.getCredits());
			} finally {
				GlobalVars.BalanceEntries.unlock(to);
			}
		}
		return proceed;
	}

	private boolean deductBalance(int userId, double amount) throws EntryNotFoundException {
		if (GlobalVars.BalanceEntries.containsKey(userId)) {
			try {
				GlobalVars.BalanceEntries.lock(userId);
				BalanceEntry balanceEntry = GlobalVars.BalanceEntries.get(userId);
				if (balanceEntry.getWalletAmount() >= amount) {
					balanceEntry.setWalletAmount(balanceEntry.getWalletAmount() - amount);
					if (balanceEntry.getWalletFlag().equalsIgnoreCase("MIN")) {
						balanceEntry.setWalletFlag("Yes");
					}
					balanceEntry.setActive(true);
					logger.info(userId + " Effective Balance -->" + balanceEntry.getWalletAmount());
				} else {
					logger.error(userId + " Insufficient Balance -->" + balanceEntry.getWalletAmount());
				}
				GlobalVars.BalanceEntries.put(userId, balanceEntry);
			} finally {
				GlobalVars.BalanceEntries.unlock(userId);
			}
			return true;
		} else {
			throw new EntryNotFoundException(userId + " BalanceEntry Not Found");
		}
	}

	public boolean transferBalance(int from, int to, double amount) throws EntryNotFoundException {
		logger.info("Transfer From: " + from + " To: " + to + " Balance:" + amount);
		boolean proceed = false;
		if (!GlobalVars.BalanceEntries.containsKey(to)) {
			throw new EntryNotFoundException(to + " BalanceEntry");
		}
		if (!GlobalVars.BalanceEntries.containsKey(from)) {
			throw new EntryNotFoundException(from + " BalanceEntry");
		}
		try {
			GlobalVars.BalanceEntries.lock(from);
			BalanceEntry from_balance = GlobalVars.BalanceEntries.get(from);
			if (from_balance.getWalletAmount() > amount) {
				from_balance.setWalletAmount(from_balance.getWalletAmount() - amount);
				if (from_balance.getWalletFlag().equalsIgnoreCase("MIN")) {
					from_balance.setWalletFlag("Yes");
				}
				logger.info(from + " Remaining Balance -->" + from_balance.getWalletAmount());
				from_balance.setActive(true);
				proceed = true;
			} else {
				logger.error(from + " Insufficient Balance -->" + from_balance.getWalletAmount());
			}
			GlobalVars.BalanceEntries.put(from, from_balance);
		} finally {
			GlobalVars.BalanceEntries.unlock(from);
		}
		if (proceed) {
			try {
				GlobalVars.BalanceEntries.lock(to);
				BalanceEntry to_balance = GlobalVars.BalanceEntries.get(to);
				to_balance.setWalletAmount(to_balance.getWalletAmount() + amount);
				if (to_balance.getWalletFlag().equalsIgnoreCase("MIN")) {
					to_balance.setWalletFlag("Yes");
				}
				to_balance.setActive(true);
				GlobalVars.BalanceEntries.put(to, to_balance);
				logger.info(to + " Effective Balance --> " + to_balance.getWalletAmount());
			} finally {
				GlobalVars.BalanceEntries.unlock(to);
			}
		}
		return proceed;
	}

	public int saveRechargeEntry(RechargeEntry entry) {
		logger.debug("saveRechargeEntry(" + entry + ")");
		return saveRechargeEntry1(entry);
	}

	@Transactional
	public int saveRechargeEntry1(RechargeEntry entry) {
		int returnId = 0;
		try (Session session = sessionFactory.openSession()) {
			session.beginTransaction();
			returnId = (Integer) session.save(entry);
			session.getTransaction().commit();
			return returnId;
		} catch (Exception ex) {
			// Ideally, log the exception
			throw new RuntimeException("Failed to save recharge entry", ex);
		}
	}

	////////////////////
	private boolean addCredits(int userId, long amount) throws EntryNotFoundException {
		if (!GlobalVars.BalanceEntries.containsKey(userId)) {
			throw new EntryNotFoundException(userId + " BalanceEntry");
		}
		try {
			GlobalVars.BalanceEntries.lock(userId);
			BalanceEntry balanceEntry = GlobalVars.BalanceEntries.get(userId);
			balanceEntry.setCredits(balanceEntry.getCredits() + amount);
			balanceEntry.setActive(true);
			GlobalVars.BalanceEntries.put(userId, balanceEntry);
		} finally {
			GlobalVars.BalanceEntries.unlock(userId);
		}
		return true;
	}

	private boolean addBalance(int userId, double amount) throws EntryNotFoundException {
		if (!GlobalVars.BalanceEntries.containsKey(userId)) {
			throw new EntryNotFoundException(userId + " BalanceEntry");
		}
		try {
			GlobalVars.BalanceEntries.lock(userId);
			BalanceEntry balanceEntry = GlobalVars.BalanceEntries.get(userId);
			balanceEntry.setWalletAmount(balanceEntry.getWalletAmount() + amount);
			if (balanceEntry.getWalletFlag().equalsIgnoreCase("MIN")) {
				balanceEntry.setWalletFlag("Yes");
			}
			balanceEntry.setActive(true);
			GlobalVars.BalanceEntries.put(userId, balanceEntry);
		} finally {
			GlobalVars.BalanceEntries.unlock(userId);
		}
		return true;
	}

	

}
