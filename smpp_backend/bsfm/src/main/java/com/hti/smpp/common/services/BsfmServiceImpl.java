package com.hti.smpp.common.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.bsfm.dto.Bsfm;
import com.hti.smpp.common.bsfm.repository.BsfmProfileRepository;
import com.hti.smpp.common.contacts.dto.GroupEntry;
import com.hti.smpp.common.contacts.repository.GroupEntryRepository;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.network.repository.NetworkEntryRepository;
import com.hti.smpp.common.request.BsfmFilterFrom;
import com.hti.smpp.common.response.BSFMResponse;
import com.hti.smpp.common.response.DeleteProfileResponse;
import com.hti.smpp.common.smsc.dto.SmscEntry;
import com.hti.smpp.common.smsc.repository.SmscEntryRepository;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.user.repository.WebMenuAccessEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;

import jakarta.transaction.Transactional;
/**
 * Service interface for Bsfm operations.
 */
@Service
public class BsfmServiceImpl implements BsfmService {

	private static final Logger logger = LoggerFactory.getLogger(BsfmServiceImpl.class.getName());

	@Autowired
	private BsfmProfileRepository bsfmRepo;

	@Autowired
	private NetworkEntryRepository networkRepository;

	@Autowired
	private WebMenuAccessEntryRepository menuAccessEntryRepository;

	@Autowired
	private GroupEntryRepository groupEntryRepository;
	
	@Autowired
	private UserEntryRepository userRepository;
	
	@Autowired
	private SmscEntryRepository smscRepo;
	
	@Autowired
	private WebMasterEntryRepository webmasterRepo;
/**
 * Adds a new Bsfm profile based on the provided filter and username.
 */
	@Override
//	@Transactional
	public ResponseEntity<String> addBsfmProfile(BsfmFilterFrom bsfmForm, String username){
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystemAndAdmin")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
	
		String target = IConstants.FAILURE_KEY;
		String systemid = user.getSystemId();
		logger.info(systemid + "[" + user.getRole() + "] Add Spam Profile: " + bsfmForm.getProfilename());
		Bsfm bdto = new Bsfm();
		BeanUtils.copyProperties(bsfmForm, bdto);
		boolean proceed = true;
		if (Access.isAuthorized(user.getRole(),"isAuthorizedAdmin")) {
			if (bsfmForm.getUsername() != null && bsfmForm.getUsername().length > 0) {
				bdto.setUsername(String.join(",", bsfmForm.getUsername()));
			} else {
				logger.error(systemid + "[" + user.getRole() + "]  No User Selected.");
				proceed = false;
			}
		} else {
			if (bsfmForm.getUsername() != null && bsfmForm.getUsername().length > 0) {
				bdto.setUsername(String.join(",", bsfmForm.getUsername()));
			}
		}
		if (proceed) {
			if (bsfmForm.getSenderType() != null && bsfmForm.getSenderType().length() > 0) {
				bdto.setSenderType(UTF16(bsfmForm.getSenderType()));
			} else {
				bdto.setSenderType(null);
			}
			if (!bdto.isSchedule()) {
				bdto.setDayTime(null);
			}
			if (bsfmForm.getSmsc() != null && bsfmForm.getSmsc().length > 0) {
				bdto.setSmsc(String.join(",", bsfmForm.getSmsc()));
			}
			if (bsfmForm.isCountryWise()) {
				if (bsfmForm.getMcc() != null && bsfmForm.getMcc().length > 0) {
					Predicate p = new PredicateBuilderImpl().getEntryObject().get("mcc").in(bsfmForm.getMcc());
					Set<Integer> set = com.hti.smpp.common.util.GlobalVars.NetworkEntries.keySet(p);
					String networks = set.stream().map(i -> i.toString()).collect(Collectors.joining(","));
					System.out.println("Networks:" + networks);
					bdto.setNetworks(networks);
				}
			} else {
				if (bsfmForm.getNetworks() != null && bsfmForm.getNetworks().length > 0) {
					bdto.setNetworks(String.join(",", bsfmForm.getNetworks()));
				}
			}
			if (bsfmForm.getContent() != null && bsfmForm.getContent().trim().length() > 0) {
				// StringTokenizer tokens = new StringTokenizer(getContent(), ",");
				String encoded_content = "";
				System.out.println("Content: " + bsfmForm.getContent());
				for (String content_token : bsfmForm.getContent().split(",")) {
					System.out.println("Token: " + content_token);
					try {
						encoded_content += UTF16(content_token) + ",";
					} catch (Exception e) {
						logger.error(e.getLocalizedMessage());
						throw new InternalServerException(e.getLocalizedMessage());
					}
				}
				if (encoded_content.length() > 0) {
					encoded_content = encoded_content.substring(0, encoded_content.length() - 1);
					bdto.setContent(encoded_content);
				} else {
					bdto.setContent(null);
				}
			} else {
				bdto.setContent(null);
			}
			bdto.setMasterId(systemid);
			try {
				int priority = bsfmRepo.findMaxPriority();
				System.out.println("Bsfmaster Max Priority: " + priority);
				if (bdto.getReroute() != null && bdto.getReroute().trim().length() == 0) {
					bdto.setReroute(null);
				}
				bdto.setPriority(++priority);
				bdto.setEditBy(systemid);
				if (addNewBsfmProfile(bdto)) {
					logger.info("Bsfm Profile added successfully: " + bsfmForm.getProfilename());
					target = IConstants.SUCCESS_KEY;
					String bsfm_flag = MultiUtility.readFlag(Constants.BSFM_FLAG_FILE);
					if (bsfm_flag != null && bsfm_flag.equalsIgnoreCase("100")) {
						MultiUtility.changeFlag(Constants.BSFM_FLAG_FILE, "707");
					}
				} else {
					logger.error("Failed to add Bsfm Profile: " + bsfmForm.getProfilename()+". Duplicate Entry Found!");
					throw new InternalServerException("Failed to add Bsfm Profile: " + bsfmForm.getProfilename()+". Duplicate Entry Found!");
				}
			} catch (Exception ex) {
				logger.error("Failed to add Bsfm Profile: " + bsfmForm.getProfilename(), ex.toString());
				throw new InternalServerException("Error while adding profile. Msg: "+ex.getLocalizedMessage());
			}
		} else {
			logger.error("Failed to add Bsfm Profile: " + bsfmForm.getProfilename() + ". Rollback.");
			throw new InternalServerException("Error while processing the request.");
		}
		return new ResponseEntity<String>("Bsfm Profile added successfully: " + bsfmForm.getProfilename(), HttpStatus.CREATED);
	}
/**
 * Adds a new Bsfm profile to the database.
 * @param bdto
 * @return
 */
	private boolean addNewBsfmProfile(Bsfm bdto) {
		try {
			bsfmRepo.save(bdto);
			return true;
		} catch (Exception e) {
			  logger.error("Failed to save Bsfm Profile to the database.", e);
			return false;
		}
	}
/**
 * Converts a UTF-16 encoded string to its hexadecimal representation.
 * @param utf16TA
 * @return
 */
	public String UTF16(String utf16TA) {
		byte[] byteBuff;
		StringBuffer strBuff = new StringBuffer();
		String tempS;
		try {
			utf16TA = new String(utf16TA.getBytes("UTF-16"), "UTF-16");
			if (utf16TA != null && utf16TA.compareTo("") != 0) {
				byteBuff = utf16TA.getBytes("UTF-16");
				for (int l = 0; l < byteBuff.length; l++) {
					tempS = byteToHex(byteBuff[l]);
					if (!tempS.equalsIgnoreCase("0D")) {
						strBuff.append(tempS);
					} else {
						strBuff.delete(strBuff.length() - 2, strBuff.length());
					}
				}
				utf16TA = strBuff.toString();
				utf16TA = utf16TA.substring(4, utf16TA.length());
				strBuff = null;
			}
		} catch (Exception ex) {
			System.out.println("EXCEPTION FROM UTF16 method :: " + ex);
		}
		return utf16TA;
	}
/**
 * Converts a byte to its hexadecimal representation.
 * @param data
 * @return
 */
	public String byteToHex(byte data) {
		StringBuffer buf = new StringBuffer();
		buf.append(toHexChar((data >>> 4) & 0x0F));
		buf.append(toHexChar(data & 0x0F));
		return (buf.toString()).toUpperCase();
	}
/**
 * Converts an integer to its corresponding hexadecimal character.
 * @param i
 * @return
 */
	public char toHexChar(int i) {
		if ((i >= 0) && (i <= 9)) {
			return (char) ('0' + i);
		} else {
			return (char) ('a' + (i - 10));
		}
	}
/**
 *  Retrieves information for Bsfm profiles based on the provided username.
 */
	@Override
	public ResponseEntity<BSFMResponse> checked(String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystemAndAdmin")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		logger.info("Checked Request By username: "+username+" userId: "+user.getId());
		BSFMResponse bSFMResponse = new BSFMResponse();
		String systemId = user.getSystemId();
		String target = IConstants.FAILURE_KEY;
		bSFMResponse.setSmscList(listNames().values());
		bSFMResponse.setUserlist(listUsers().values());
		bSFMResponse.setGroupDetail(listGroupNames());
		Map<String, String> networkmap = null;
		try {
			networkmap = getDistinctCountry();
		} catch (SQLException e) {
			logger.error("An error occured in line {}: {}", Thread.currentThread().getStackTrace()[1].getLineNumber(), e.getMessage());
			throw new InternalServerException("Error: "+e.getLocalizedMessage());
		}
		Map<Integer, String> operatormap = new HashMap<Integer, String>();
		bSFMResponse.setNetworkmap(networkmap);
		List<NetworkEntry> networkEntries = this.networkRepository.findAll();
		for (NetworkEntry entry : networkEntries) {
			operatormap.put(entry.getId(),
					entry.getCountry() + "-" + entry.getOperator() + " [" + entry.getMnc() + "]");
		}
		bSFMResponse.setNetworkmap(networkmap);
		bSFMResponse.setOperatormap(operatormap);
		target = IConstants.SUCCESS_KEY;
		
		if (Access.isAuthorized(user.getRole(),"isAuthorizedAdmin")
				&& menuAccessEntryRepository.findById(user.getId()).get().isBsfm()) {
			bSFMResponse.setSmscList(listNames(systemId).values());
			List<String> users = new ArrayList<String>(listUsersUnderMaster(systemId).values());

			List<WebMasterEntry> webEntries = this.webmasterRepo.findBySecondaryMaster(systemId);
			for (WebMasterEntry webEntry : webEntries) {
				UserEntry userEntry = this.userRepository.findById(webEntry.getUserId()).get();
				users.add(userEntry.getSystemId());
			}
			try {
				networkmap = getDistinctCountry();
			} catch (SQLException e) {
				logger.error("An error occured in line {}: {}", Thread.currentThread().getStackTrace()[1].getLineNumber(), e.getMessage());
				throw new InternalServerException("Error: "+e.getLocalizedMessage());
			}
			Map<Integer, String> operatormap1 = new HashMap<Integer, String>();
			bSFMResponse.setNetworkmap(networkmap);
			List<NetworkEntry> networkEntriesAdmin = this.networkRepository.findAll();
			for (NetworkEntry entry : networkEntriesAdmin) {
				operatormap1.put(entry.getId(),
						entry.getCountry() + "-" + entry.getOperator() + " [" + entry.getMnc() + "]");
			}
			bSFMResponse.setNetworkmap(networkmap);
			bSFMResponse.setOperatormap(operatormap1);
			bSFMResponse.setUserlist(users);
			target = IConstants.SUCCESS_KEY;
		}
		return ResponseEntity.ok(bSFMResponse);
	}
/**
 * Retrieves distinct countries and their codes from the network repository.
 * @return
 * @throws SQLException
 */
	public Map<String, String> getDistinctCountry() throws SQLException {
		Map<String, String> countries = new LinkedHashMap<>();
		try {
			List<Object[]> results = networkRepository.findDistinctCountries();
			for (Object[] result : results) {
				countries.put((String) result[0], (String) result[1]);
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			throw new SQLException("Error retrieving distinct countries. Msg: "+e.getLocalizedMessage());
		}
		return countries;
	}
/**
 * Retrieves a map of user IDs and usernames under the specified master ID.
 * @param master
 * @return
 */
	public Map<Integer, String> listUsersUnderMaster(String master) {
		logger.info("listUsersUnderMaster(" + master + ")");
		Map<Integer, String> map = new HashMap<Integer, String>();
		List<UserEntry> userEntries = this.userRepository.findAll();
		for (UserEntry entry : userEntries) {
			if (entry.getMasterId().equalsIgnoreCase(master)) {
				map.put(entry.getId(), entry.getSystemId());
			}
		}
		Map<Integer, String> sortedMap = map.entrySet().stream()
				.sorted(Entry.comparingByValue(Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return sortedMap;
	}
/**
 * Retrieves a sorted map of SMS center IDs and names.
 * @return
 */
	public Map<Integer, String> listNames() {
		Map<Integer, String> names = new HashMap<Integer, String>();
		List<SmscEntry> smscEntries = this.smscRepo.findAll();
		for (SmscEntry entry : smscEntries) {
			names.put(entry.getId(), entry.getName());
		}
		names = names.entrySet().stream().sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return names;
	}
/**
 * Retrieves a sorted map of user IDs and usernames.
 * @return
 */
	public Map<Integer, String> listUsers() {
		logger.debug("listUsers()");
		Map<Integer, String> map = new HashMap<Integer, String>();
		List<UserEntry> userEntries = this.userRepository.findAll();
		for (UserEntry entry : userEntries) {
			map.put(entry.getId(), entry.getSystemId());
		}
		Map<Integer, String> sortedMap = map.entrySet().stream()
				.sorted(Entry.comparingByValue(Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return sortedMap;
	}
/**
 * Retrieves a sorted map of group IDs and names.
 * @return
 */
	public Map<Integer, String> listGroupNames() {
		Map<Integer, String> names = new HashMap<Integer, String>();
		names.put(0, "NONE");
		List<GroupEntry> groups = groupEntryRepository.findAll();
		for (GroupEntry entry : groups) {
			names.put(entry.getId(), entry.getName());
		}
		names = names.entrySet().stream().sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return names;
	}
/**
 * Retrieves a sorted map of SMS center IDs and names for a specific master ID.
 * @param masterId
 * @return
 */
	public Map<Integer, String> listNames(String masterId) {
		Map<Integer, String> names = new HashMap<Integer, String>();
		List<SmscEntry> smscEntries = this.smscRepo.findByMasterId(masterId);
		for (SmscEntry entry : smscEntries) {
			names.put(entry.getId(), entry.getName());
		}
		names = names.entrySet().stream().sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return names;
	}
/**
 * Deletes a Bsfm profile based on the provided username and profile ID.
 */
	@Override
	public ResponseEntity<DeleteProfileResponse> deleteProfile(String username, int id) {
		DeleteProfileResponse deleteProfileResponse = new DeleteProfileResponse();
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystemAndAdmin")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String target = IConstants.FAILURE_KEY;
		String systemid = user.getSystemId();
		logger.info(user.getRole() + " Edit Profile Request: " + id);
		try {
			Optional<Bsfm> bsfmOptional = bsfmRepo.findById(id);
			Bsfm bsfm = null;
			if (bsfmOptional.isPresent()) {
				bsfm = bsfmOptional.get();
			}else {
				throw new NotFoundException("No Bsfm Profile Found With Id: "+id);
			}
			if (bsfm != null) {
				if (bsfm.getSenderType() != null) {
					bsfm.setSenderType(hexCodePointsToCharMsg(bsfm.getSenderType()));
				}
				List<String> underUserList = null;
				Collection<String> smscList = null;
				if (Access.isAuthorized(user.getRole(),"isAuthorizedAdmin")) {
					smscList = listNames(systemid).values();
					underUserList = new ArrayList<String>(listUsersUnderMaster(systemid).values());
					List<WebMasterEntry> webMasterEntries = this.webmasterRepo.findBySecondaryMaster(systemid);
					for (WebMasterEntry webEntry : webMasterEntries) {
						UserEntry userEntry = this.userRepository.findById(webEntry.getUserId()).get();
						underUserList.add(userEntry.getSystemId());
					}
				} else {
					smscList = listNames().values();
					underUserList = new ArrayList<String>(listUsers().values());
				}
				List<String> existUserList = new ArrayList<String>();
				List<String> existSmscList = new ArrayList<String>();
				if (bsfm.getUsername() != null && bsfm.getUsername().length() > 0) {
					for (String username1 : bsfm.getUsername().split(",")) {
						if (underUserList.contains(username1)) {
							underUserList.remove(username1);
							existUserList.add(username1);
						}
					}
				}
				if (bsfm.getSmsc() != null && bsfm.getSmsc().length() > 0) {
					for (String smsc : bsfm.getSmsc().split(",")) {
						if (smscList.contains(smsc)) {
							smscList.remove(smsc);
							existSmscList.add(smsc);
						}
					}
				}
				Map<Integer, String> networkmap = listCountries();
				networkmap.remove(0);
				Map<Integer, String> existNetworks = new java.util.HashMap<Integer, String>();
				if (bsfm.getNetworks() != null && bsfm.getNetworks().length() > 0) {
					for (String network : bsfm.getNetworks().split(",")) {
						try {
							int network_id = Integer.parseInt(network);
							if (networkmap.containsKey(network_id)) {
								existNetworks.put(network_id, networkmap.remove(network_id));
							}
						} catch (Exception e) {
							logger.error(bsfm.getId() + "[" + bsfm.getProfilename() + "] Invalid Network: " + network);
						}
					}
				}
				String content = bsfm.getContent();
				String decoded_content = "";
				if (content != null && content.trim().length() > 0) {
					StringTokenizer tokens = new StringTokenizer(content, ",");
					while (tokens.hasMoreTokens()) {
						String content_token = tokens.nextToken();
						decoded_content += uniHexToCharMsg(content_token) + ",";
					}
					if (decoded_content.length() > 0) {
						decoded_content = decoded_content.substring(0, decoded_content.length() - 1);
						bsfm.setContent(decoded_content);
					} else {
						bsfm.setContent(null);
					}
				} else {
					bsfm.setContent(null);
				}
				logger.info(bsfm.getProfilename() + " isSchedule: " + bsfm.isSchedule());
				if (bsfm.isSchedule()) {
					List<String[]> daytimelist = new ArrayList<String[]>();
					for (String day_time_token : bsfm.getDayTime().split(",")) {
						if (day_time_token != null && day_time_token.length() == 19) {
							logger.info(bsfm.getProfilename() + " day_time_token: " + day_time_token);
							try {
								int day = Integer.parseInt(day_time_token.substring(0, day_time_token.indexOf("F")));
								String from = day_time_token.substring(day_time_token.indexOf("F") + 1,
										day_time_token.indexOf("T"));
								String to = day_time_token.substring(day_time_token.indexOf("T") + 1,
										day_time_token.length());
								String day_name = "Everyday";
								if (day > 7) {
									logger.error(bsfm.getId() + " Invalid Day: " + day);
									continue;
								} else {
									if (day == 1) {
										day_name = "Sunday";
									} else if (day == 2) {
										day_name = "Monday";
									} else if (day == 3) {
										day_name = "Tuesday";
									} else if (day == 4) {
										day_name = "Wednesday";
									} else if (day == 5) {
										day_name = "Thursday";
									} else if (day == 6) {
										day_name = "Friday";
									} else if (day == 7) {
										day_name = "Saturday";
									}
								}
								// logger.info("day_time_token Adding: " + day_time_token);
								daytimelist.add(new String[] { day + "", day_name, from, to });
								// daytimelist.add(day + " " + day_name + " " + from + " " + to);
							} catch (Exception ex) {
								logger.error(bsfm.getId() + " Invalid Schedule token: " + day_time_token, ex);
								continue;
							}
						} else {
							logger.error(bsfm.getId() + " Invalid Schedule token: " + day_time_token);
							continue;
						}
					}
					// System.out.println("schedules: " + daytimelist);
					if (daytimelist.isEmpty()) {
						bsfm.setSchedule(false);
					} else {
						deleteProfileResponse.setDaytimelist(daytimelist);
					}
				}
				Map<Integer, String> grouping = listGroupNames();
				if (grouping.containsKey(bsfm.getRerouteGroupId())) {
					bsfm.setRerouteGroupName(grouping.get(bsfm.getRerouteGroupId()));
				}
				// request.setAttribute("message", content);
				deleteProfileResponse.setBsfm(bsfm);
				deleteProfileResponse.setUnderUserList(underUserList);
				deleteProfileResponse.setSmscList(smscList);
				deleteProfileResponse.setExistUserList(existUserList);
				deleteProfileResponse.setExistSmscList(existSmscList);
				deleteProfileResponse.setGrouping(grouping);
				deleteProfileResponse.setNetworkmap(networkmap);
				deleteProfileResponse.setExistNetworks(existNetworks);
				target = IConstants.SUCCESS_KEY;
				logger.info("finished: " + target);
			} else {
				logger.error("Requested Bsfm profile is unavailable.");         //
			}
		} catch (NotFoundException ex) {
			logger.error("Not Found Error: "+ex.getLocalizedMessage());
			throw new NotFoundException(ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error("An error occured in line {}: {}", Thread.currentThread().getStackTrace()[1].getLineNumber(), ex.getMessage());
			throw new InternalServerException("Error: "+ex.getLocalizedMessage());
		}
		logger.info("final target value: " + target);             //
		deleteProfileResponse.setStatus(target);
		return ResponseEntity.ok(deleteProfileResponse);
	}
/**
 *  Converts a string containing hex values of Unicode to Unicode characters.
 * @param msg
 * @return
 */
	public static String hexCodePointsToCharMsg(String msg)// Implemented by Abhishek Sahu
	{
		// this mthd made decreasing codes, only.
		//// This mthd will take msg who contain hex values of unicode, then it will
		// convert this msg to Unicode from hex.
		boolean reqNULL = false;
		byte[] charsByt, var;
		int x = 0;
		if (msg.substring(0, 2).compareTo("00") == 0) // if true means first byte is null, then null is required in
														// first byte, after header.
		{
			reqNULL = true;
		}
		charsByt = new BigInteger(msg, 16).toByteArray(); // this won't give null value in first byte if occured, so i
															// have to append it .
		if (charsByt[0] == '\0') // cut this null.
		{
			var = new byte[charsByt.length - 1];
			for (int q = 1; q < charsByt.length; q++) {
				var[q - 1] = charsByt[q];
			}
			charsByt = var;
		}
		if (reqNULL) {
			var = new byte[charsByt.length + 1];
			x = 0;
			var[0] = '\0';
			reqNULL = false;
		} else {
			var = new byte[charsByt.length];
			x = -1;
		}
		for (int l = 0; l < charsByt.length; l++) {
			var[++x] = charsByt[l];
		}
		try {
			msg = new String(var, "UTF-16"); // charsTA msg Setted.
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return msg;
	}
/**
 *  Retrieves a map of network entries with their corresponding country and operator information.
 * @return
 */
	public Map<Integer, String> listCountries() {
		Map<Integer, String> countries = new HashMap<Integer, String>();
		List<NetworkEntry> networkEntries = this.networkRepository.findAll();
		for (NetworkEntry entry : networkEntries) {
			countries.put(entry.getId(), entry.getCountry() + "-" + entry.getOperator());
		}
		return countries;
	}
/**
 * Converts a Unicode hex string to a character message.
 * @param msg
 * @return
 */
	public String uniHexToCharMsg(String msg) {
		if (msg == null || msg.length() == 0) {
			msg = "0020";
		}
		boolean reqNULL = false;
		byte[] charsByt, var;
		int x = 0;
		try {
			if (msg.substring(0, 2).compareTo("00") == 0) {
				reqNULL = true;
			}
			charsByt = new BigInteger(msg, 16).toByteArray();
			if (charsByt[0] == '\0') {
				var = new byte[charsByt.length - 1];
				for (int q = 1; q < charsByt.length; q++) {
					var[q - 1] = charsByt[q];
				}
				charsByt = var;
			}
			if (reqNULL) {
				var = new byte[charsByt.length + 1];
				x = 0;
				var[0] = '\0';
				reqNULL = false;
			} else {
				var = new byte[charsByt.length];
				x = -1;
			}
			for (int l = 0; l < charsByt.length; l++) {
				var[++x] = charsByt[l];
			}
			msg = new String(var, "UTF-16");
		} catch (Exception ex) {
			logger.error(msg, ex);
		}
		return msg;
	}
/**
 * Retrieves a list of Bsfm profiles for the specified user.
 */
	@Override
	public ResponseEntity<List<Bsfm>> showBsfmProfile(String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystemAndAdmin")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

			if (menuAccessEntryRepository.findById(user.getId()).get().isBsfm()) {
				List<Bsfm> list = null;
				if (Access.isAuthorized(user.getRole(),"isAuthorizedAdmin")) {
					list = bsfmRepo.findByMasterIdOrderByPriority(user.getSystemId());
				} else {
					list = bsfmRepo.findByMasterIdOrderByPriority(null);
				}

				if (!list.isEmpty()) {
					 logger.info("Bsfm profiles listed successfully for user: {}", username);       //
					return ResponseEntity.ok(list);
				} else {
					  logger.warn("No Bsfm profiles available for user: {}", username);       //
					  return ResponseEntity.ok(Collections.emptyList()); // Return an empty list if no data is found
				}
			} else {
				throw new InternalServerException("Invalid request for user without Bsfm access.");
			}
	}
/**
 * Updates a Bsfm profile based on the provided form data and the user's authorization.
 */
	@Override
	@Transactional
	public ResponseEntity<String> updateBsfmProfile(BsfmFilterFrom bsfmForm, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystemAndAdmin")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String target = IConstants.FAILURE_KEY;
		String systemid = user.getSystemId();
		logger.info(systemid + "[" + user.getRole() + "] Update Spam Profile: " + bsfmForm.getProfilename());
		
		Bsfm bdto = new Bsfm();
		BeanUtils.copyProperties(bsfmForm, bdto);
		boolean proceed = true;
		if (Access.isAuthorized(user.getRole(),"isAuthorizedAdmin")) {
			if (bsfmForm.getUsername() != null && bsfmForm.getUsername().length > 0) {
				bdto.setUsername(String.join(",", bsfmForm.getUsername()));
			} else {
				logger.error(systemid + "[" + user.getRole() + "]  No User Selected.");
				proceed = false;
			}
		} else {
			if (bsfmForm.getUsername() != null && bsfmForm.getUsername().length > 0) {
				bdto.setUsername(String.join(",", bsfmForm.getUsername()));
			}
		}
		if (proceed) {
			if (bsfmForm.getSmsc() != null && bsfmForm.getSmsc().length > 0) {
				bdto.setSmsc(String.join(",", bsfmForm.getSmsc()));
			}
			if (bsfmForm.getNetworks() != null && bsfmForm.getNetworks().length > 0) {
				bdto.setNetworks(String.join(",", bsfmForm.getNetworks()));
			}
			if (bsfmForm.getSenderType() != null && bsfmForm.getSenderType().length() > 0) {
				bdto.setSenderType(UTF16(bsfmForm.getSenderType()));
			} else {
				bdto.setSenderType(null);
			}
			if (bsfmForm.getContent() != null && bsfmForm.getContent().trim().length() > 0) {
				// StringTokenizer tokens = new StringTokenizer(getContent(), ",");
				String encoded_content = "";
				for (String content_token : bsfmForm.getContent().split(",")) {
					try {
						encoded_content += UTF16(content_token) + ",";
					} catch (Exception e) {
						logger.error(systemid + "[" + user.getRole() + "]  [" + content_token + "]", e.getMessage());
						throw new InternalServerException("Error: "+e.getLocalizedMessage());
					}
				}
				if (encoded_content.length() > 0) {
					encoded_content = encoded_content.substring(0, encoded_content.length() - 1);
					bdto.setContent(encoded_content);
				} else {
					bdto.setContent(null);
				}
			} else {
				bdto.setContent(null);
			}
			if (bdto.getReroute() != null && bdto.getReroute().trim().length() == 0) {
				bdto.setReroute(null);
			}
			if (bdto.isSchedule()) {
				if (bdto.getDayTime() == null || bdto.getDayTime().length() < 19) {
					logger.info(bdto.getProfilename() + " Invalid DayTime Configured: " + bdto.getDayTime());
					bdto.setSchedule(false);
				}
			}
			bdto.setEditBy(systemid);
			boolean isUpdated = updatedBsfmProfile(bdto);
			if (isUpdated) {
				logger.info("Spam Profile updated successfully: {}", bsfmForm.getProfilename());
				target = IConstants.SUCCESS_KEY;
				String bsfm_flag = MultiUtility.readFlag(Constants.BSFM_FLAG_FILE);
				if (bsfm_flag != null && bsfm_flag.equalsIgnoreCase("100")) {
					MultiUtility.changeFlag(Constants.BSFM_FLAG_FILE, "707");
				}
			} else {
				logger.error("Failed to update Spam Profile: {}", bsfmForm.getProfilename()); 
				throw new InternalServerException("Failed to update Spam Profile: "+bsfmForm.getProfilename());//
			}
		} else {
			logger.error("No user selected for the update.");  
			throw new InternalServerException("No user selected for the update.");//
		}
		return new ResponseEntity<String>("Bsfm profile updated successfully!",HttpStatus.CREATED);
	}
	
	// Method to check if the profilename already exists for a different record
	private boolean profileAlreadyExists(String profilename, int currentId) {
	    Optional<Bsfm> existingProfile = bsfmRepo.findByProfilenameAndIdNot(profilename, currentId);
	    return existingProfile.isPresent();
	}
	
/**
 * Updates the Bsfm profile in the repository.
 * @param bdto
 * @return
 */
	private boolean updatedBsfmProfile(Bsfm bdto) {
		try {
			Bsfm update = bsfmRepo.findById(bdto.getId()).get();
			if (update!=null) {
	            // Check if the profilename already exists before updating
	            if (profileAlreadyExists(bdto.getProfilename(), bdto.getId())) {
	                logger.error("Duplicate Bsfm Profile with profilename: " + bdto.getProfilename());
	                throw new InternalServerException("Duplicate Bsfm Profile with profilename: " + bdto.getProfilename());
	            } else {
	                // Update the existing record
	            	update.setProfilename(bdto.getProfilename());
	            	update.setUsername(bdto.getUsername());
	            	update.setContent(bdto.getContent());
	            	update.setSmsc(bdto.getSmsc());
	            	update.setPrefixes(bdto.getPrefixes());
	            	update.setSourceid(bdto.getSourceid());
	            	update.setActive(bdto.isActive());
	            	update.setReverse(bdto.isReverse());
	            	update.setReroute(bdto.getReroute());
	            	update.setSchedule(bdto.isSchedule());
	            	update.setDayTime(bdto.getDayTime());
	            	update.setActiveOnScheduleTime(bdto.isActiveOnScheduleTime());
	            	update.setSenderType(bdto.getSenderType());
	            	update.setForceSenderId(bdto.getForceSenderId());
	            	update.setRerouteGroupId(bdto.getRerouteGroupId());
	            	update.setMsgLength(bdto.getMsgLength());
	            	update.setLengthOpr(bdto.getLengthOpr());
	            	update.setNetworks(bdto.getNetworks());
	            	update.setEditBy(bdto.getEditBy());
	                bsfmRepo.save(update);
	                return true;
	            }
	        } else {
	            logger.error("No Bsfm Profile exists with Id: " + bdto.getId());
	            throw new NoSuchElementException("No Bsfm Profile exists with Id: " + bdto.getId());
	        }
			
		} catch (NoSuchElementException e) {
			logger.error("Failed to update Bsfm Profile. "+e.getLocalizedMessage());            //
			throw new NotFoundException("No Bsfm Profile exists with Id: " + bdto.getId());
		} catch (InternalServerException e) {
			logger.error("Failed to update Bsfm Profile. "+ e.getLocalizedMessage());            //
			throw new InternalServerException(e.getMessage());
		} catch (Exception e) {
			logger.error("Failed to update Bsfm Profile.", e);            //
			return false;
		}   
	}
/**
 * Deletes the Bsfm profile based on the provided filter parameters.
 */
	@Override
	public ResponseEntity<String> delete(String username, String profilename) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystemAndAdmin")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String target = IConstants.FAILURE_KEY;
		logger.info("Delete request by username: "+username);
		boolean isDeleted = deleteBsfmActiveProfile(profilename);
		logger.info("isDeleted value: " + isDeleted);
		if (isDeleted) {
			logger.info("Spam Profile deleted successfully!");
			target = IConstants.SUCCESS_KEY;
			String bsfm_flag = MultiUtility.readFlag(Constants.BSFM_FLAG_FILE);
			if (bsfm_flag != null && bsfm_flag.equalsIgnoreCase("100")) {
				MultiUtility.changeFlag(Constants.BSFM_FLAG_FILE, "707");
			}
		} else {
			logger.error("Failed to delete Spam Profile");
			target = IConstants.FAILURE_KEY;
			logger.error("An error occured while deleting the spam profile");
			throw new InternalServerException("Unable to delete the spam profile name: "+profilename);
		}
		return ResponseEntity.ok("Spam Profile Deleted Successfully!");
	}
/**
 * 
 * @param profileName
 * @return
 */
	@Transactional
	public boolean deleteBsfmActiveProfile(String profileName) {
		try {
			long count = bsfmRepo.deleteByProfilename(profileName);

			return count > 0;
		} catch (Exception e) {
			logger.error("Error deleting BsfmActiveProfile", e);
			return false;
		}
	}
/**
 * Updates the Bsfm profile flag based on the provided information.
 */
	@Override
	public ResponseEntity<String> updateBsfmProfileFlag(String username, String flag) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystemAndAdmin")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		logger.info("Update Bsfm profile flag requested by: "+username);
		String target = IConstants.FAILURE_KEY;
		// System.err.println("inside of the action class ::::" + bdto.getFlagValue());
		String hostConfig = IConstants.CAAS_FLAG_DIR;
		String text = "FLAG = " + flag;
		try {
			File file = new File(hostConfig + "BSFM.flag");
			Writer output = null;
			output = new BufferedWriter(new FileWriter(file));
			output.write(text);
			output.close();
			target = IConstants.SUCCESS_KEY;
		} catch (Exception e) {
			logger.error("An error occured in line {}: {}", Thread.currentThread().getStackTrace()[1].getLineNumber(), e.getMessage());
			throw new InternalServerException("Error: "+e.getLocalizedMessage());
		}
		if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
			logger.info("Flag value change: Success");
		}
		if (target.equalsIgnoreCase(IConstants.FAILURE_KEY)) {
			logger.error("Flag value change: Failure");
		}
		return ResponseEntity.ok("Flag Value Change: "+target);
	}

}
