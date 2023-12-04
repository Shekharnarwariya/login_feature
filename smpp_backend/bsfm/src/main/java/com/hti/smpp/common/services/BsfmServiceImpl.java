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
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.UserRepository;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.network.repository.NetworkEntryRepository;
import com.hti.smpp.common.request.BsfmFilterFrom;
import com.hti.smpp.common.response.BSFMResponse;
import com.hti.smpp.common.response.DeleteProfileResponse;
import com.hti.smpp.common.smsc.dto.SmscEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.WebMenuAccessEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;

import jakarta.transaction.Transactional;

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
	private UserRepository loginRepository;

	@Override
	@Transactional
	public String addBsfmProfile(BsfmFilterFrom bsfmForm, String username) throws Exception {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		User user = null;
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystemAndAdmin(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String target = IConstants.FAILURE_KEY;
		String systemid = user.getSystemId();
		logger.info(systemid + "[" + user.getRoles() + "] Add Spam Profile: " + bsfmForm.getProfilename());
		Bsfm bdto = new Bsfm();
		BeanUtils.copyProperties(bdto, bsfmForm);
		boolean proceed = true;
		if (Access.isAuthorizedAdmin(user.getRoles())) {
			if (bsfmForm.getUsername() != null && bsfmForm.getUsername().length > 0) {
				bdto.setUsername(String.join(",", bsfmForm.getUsername()));
			} else {
				logger.error(systemid + "[" + user.getRoles() + "]  No User Selected.");
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
						e.printStackTrace();
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
					logger.info("message.operation.success");
					target = IConstants.SUCCESS_KEY;
					String bsfm_flag = MultiUtility.readFlag(Constants.BSFM_FLAG_FILE);
					if (bsfm_flag != null && bsfm_flag.equalsIgnoreCase("100")) {
						MultiUtility.changeFlag(Constants.BSFM_FLAG_FILE, "707");
					}
				} else {
					logger.error("message.operation.failed");
				}
			} catch (Exception ex) {
				logger.error("message.operation.failed");
			}
		} else {
			logger.error("error.noUserforSelectedmode");
		}
		return target;
	}

	private boolean addNewBsfmProfile(Bsfm bdto) {
		try {
			bsfmRepo.save(bdto);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

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

	public String byteToHex(byte data) {
		StringBuffer buf = new StringBuffer();
		buf.append(toHexChar((data >>> 4) & 0x0F));
		buf.append(toHexChar(data & 0x0F));
		return (buf.toString()).toUpperCase();
	}

	public char toHexChar(int i) {
		if ((i >= 0) && (i <= 9)) {
			return (char) ('0' + i);
		} else {
			return (char) ('a' + (i - 10));
		}
	}

	@Override
	public BSFMResponse checked(String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		User user = null;
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		BSFMResponse bSFMResponse = new BSFMResponse();
		String systemId = user.getSystemId();
		String target = IConstants.FAILURE_KEY;
		bSFMResponse.setSmscList(listNames());
		bSFMResponse.setUserlist(listUsers());
		bSFMResponse.setGroupDetail(listGroupNames());
		Map<String, String> networkmap = null;
		try {
			networkmap = getDistinctCountry();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Map<Integer, String> operatormap = new HashMap<Integer, String>();
		bSFMResponse.setNetworkmap(networkmap);
		for (NetworkEntry entry : GlobalVars.NetworkEntries.values()) {
			operatormap.put(entry.getId(),
					entry.getCountry() + "-" + entry.getOperator() + " [" + entry.getMnc() + "]");
		}
		bSFMResponse.setNetworkmap(networkmap);
		bSFMResponse.setOperatormap(operatormap);
		target = IConstants.SUCCESS_KEY;
		if (Access.isAuthorizedAdmin(user.getRoles())
				&& menuAccessEntryRepository.findById(user.getUserId().intValue()).get().isBsfm()) {
			Collection<String> values = listNames(systemId).values();
			bSFMResponse.setSmscList((Map<Integer, String>) values);
			List<String> users = new ArrayList<String>(listUsersUnderMaster(systemId).values());
			Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject().get("secondaryMaster")
					.equal(systemId);
			for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(p)) {
				UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
				users.add(userEntry.getSystemId());
			}
			try {
				networkmap = getDistinctCountry();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			Map<Integer, String> operatormap1 = new HashMap<Integer, String>();
			bSFMResponse.setNetworkmap(networkmap);
			for (NetworkEntry entry : GlobalVars.NetworkEntries.values()) {
				operatormap1.put(entry.getId(),
						entry.getCountry() + "-" + entry.getOperator() + " [" + entry.getMnc() + "]");
			}
			bSFMResponse.setNetworkmap(networkmap);
			bSFMResponse.setOperatormap(operatormap1);
			bSFMResponse.setUsers(users);
			target = IConstants.SUCCESS_KEY;
		}
		return bSFMResponse;
	}

	public Map<String, String> getDistinctCountry() throws SQLException {
		Map<String, String> countries = new LinkedHashMap<>();
		try {
			List<Object[]> results = networkRepository.findDistinctCountries();
			for (Object[] result : results) {
				countries.put((String) result[0], (String) result[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return countries;
	}

	public Map<Integer, String> listUsersUnderMaster(String master) {
		logger.debug("listUsersUnderMaster(" + master + ")");
		Map<Integer, String> map = new HashMap<Integer, String>();
		for (UserEntry entry : GlobalVars.UserEntries.values()) {
			if (entry.getMasterId().equalsIgnoreCase(master)) {
				map.put(entry.getId(), entry.getSystemId());
			}
		}
		Map<Integer, String> sortedMap = map.entrySet().stream()
				.sorted(Entry.comparingByValue(Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return sortedMap;
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

	public Map<Integer, String> listUsers() {
		logger.debug("listUsers()");
		Map<Integer, String> map = new HashMap<Integer, String>();
		for (UserEntry entry : GlobalVars.UserEntries.values()) {
			map.put(entry.getId(), entry.getSystemId());
		}
		Map<Integer, String> sortedMap = map.entrySet().stream()
				.sorted(Entry.comparingByValue(Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return sortedMap;
	}

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

	public Map<Integer, String> listNames(String masterId) {
		Map<Integer, String> names = new HashMap<Integer, String>();
		Predicate<Integer, SmscEntry> p = new PredicateBuilderImpl().getEntryObject().get("masterId").equal(masterId);
		for (SmscEntry entry : GlobalVars.SmscEntries.values(p)) {
			names.put(entry.getId(), entry.getName());
		}
		names = names.entrySet().stream().sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return names;
	}

	@Override
	public DeleteProfileResponse deleteProfile(String username, int id) {
		DeleteProfileResponse deleteProfileResponse = new DeleteProfileResponse();
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		User user = null;
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystemAndAdmin(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String target = IConstants.FAILURE_KEY;
		String systemid = user.getSystemId();
		System.out.println(user.getRoles() + " Edit Profile Request: " + id);
		try {
			Optional<Bsfm> bsfmOptional = bsfmRepo.findById(id);
			Bsfm bsfm = null;
			if (bsfmOptional.isPresent()) {
				bsfm = bsfmOptional.get();
			}
			if (bsfm != null) {
				if (bsfm.getSenderType() != null) {
					bsfm.setSenderType(hexCodePointsToCharMsg(bsfm.getSenderType()));
				}
				List<String> underUserList = null;
				Collection<String> smscList = null;
				if (Access.isAuthorizedAdmin(user.getRoles())) {
					smscList = listNames(systemid).values();
					underUserList = new ArrayList<String>(listUsersUnderMaster(systemid).values());
					Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
							.get("secondaryMaster").equal(systemid);
					for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(p)) {
						UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
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
				System.out.println(bsfm.getProfilename() + " isSchedule: " + bsfm.isSchedule());
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
				deleteProfileResponse.setSmscList(existSmscList);
				deleteProfileResponse.setExistUserList(existUserList);
				deleteProfileResponse.setExistSmscList(existSmscList);
				deleteProfileResponse.setGrouping(grouping);
				deleteProfileResponse.setNetworkmap(networkmap);
				deleteProfileResponse.setExistNetworks(existNetworks);
				target = IConstants.SUCCESS_KEY;
				System.out.println("finished: " + target);
			} else {
				logger.error("error.record.unavailable");
			}
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error("", ex.fillInStackTrace());
		}
		System.out.println("final: " + target);
		deleteProfileResponse.setStatus(target);
		return deleteProfileResponse;
	}

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

	public Map<Integer, String> listCountries() {
		Map<Integer, String> countries = new HashMap<Integer, String>();
		for (NetworkEntry entry : GlobalVars.NetworkEntries.values()) {
			countries.put(entry.getId(), entry.getCountry() + "-" + entry.getOperator());
		}
		return countries;
	}

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

	@Override
	public List<Bsfm> showBsfmProfile(String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);

		if (optionalUser.isPresent()) {
			User user = optionalUser.get();

			if (!Access.isAuthorizedSuperAdminAndSystemAndAdmin(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}

			if (menuAccessEntryRepository.findById(user.getUserId().intValue()).get().isBsfm()) {
				List<Bsfm> list;
				if (Access.isAuthorizedAdmin(user.getRoles())) {
					list = bsfmRepo.findByMasterIdOrderByPriority(user.getSystemId());
				} else {
					list = bsfmRepo.findByMasterIdOrderByPriority(null);
				}

				if (!list.isEmpty()) {
					return list;
				} else {
					logger.error("error.record.unavailable");
				}
			} else {
				throw new InternalServerException("Invalid request for user without Bsfm access.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		return Collections.emptyList(); // Return an empty list if no data is found
	}

	@Override
	public String updateBsfmProfil(BsfmFilterFrom bsfmForm, String username) {

		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		User user = null;
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystemAndAdmin(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String target = IConstants.FAILURE_KEY;
		String systemid = user.getSystemId();
		logger.info(systemid + "[" + user.getRoles() + "] Update Spam Profile: " + bsfmForm.getProfilename());
		Bsfm bdto = new Bsfm();
		BeanUtils.copyProperties(bdto, bsfmForm);
		boolean proceed = true;
		if (Access.isAuthorizedAdmin(user.getRoles())) {
			if (bsfmForm.getUsername() != null && bsfmForm.getUsername().length > 0) {
				bdto.setUsername(String.join(",", bsfmForm.getUsername()));
			} else {
				logger.error(systemid + "[" + user.getRoles() + "]  No User Selected.");
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
						logger.error(systemid + "[" + user.getRoles() + "]  [" + content_token + "]", e);
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
				logger.info("message.operation.success");
				target = IConstants.SUCCESS_KEY;
				String bsfm_flag = MultiUtility.readFlag(Constants.BSFM_FLAG_FILE);
				if (bsfm_flag != null && bsfm_flag.equalsIgnoreCase("100")) {
					MultiUtility.changeFlag(Constants.BSFM_FLAG_FILE, "707");
				}
			} else {
				logger.error("error.processError");
			}
		} else {
			logger.error("error.noUserforSelectedmode");
		}
		return target;
	}

	private boolean updatedBsfmProfile(Bsfm bdto) {
		try {
			bsfmRepo.save(bdto);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String delete(String username, BsfmFilterFrom bsfmFilterFrom) {
		String target = IConstants.FAILURE_KEY;
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		User user = null;
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystemAndAdmin(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		Bsfm bdto = new Bsfm();
		BeanUtils.copyProperties(bdto, bsfmFilterFrom);
		String profileName = bdto.getProfilename();
		boolean isDeleted = deleteBsfmActiveProfile(profileName);
		System.err.println("isdeleted valie" + isDeleted);
		if (isDeleted) {
			logger.info("message.operation.success");
			target = IConstants.SUCCESS_KEY;
			String bsfm_flag = MultiUtility.readFlag(Constants.BSFM_FLAG_FILE);
			if (bsfm_flag != null && bsfm_flag.equalsIgnoreCase("100")) {
				MultiUtility.changeFlag(Constants.BSFM_FLAG_FILE, "707");
			}
		} else {
			logger.error("error.processError");
			target = IConstants.FAILURE_KEY;
		}
		return target;
	}

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

	@Override
	public String updateBsfmProfileFlag(String username, BsfmFilterFrom filterFrom) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		User user = null;
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystemAndAdmin(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String target = IConstants.FAILURE_KEY;
		// System.err.println("inside of the action class ::::" + bdto.getFlagValue());
		String hostConfig = IConstants.CAAS_FLAG_DIR;
		String flagValue = filterFrom.getFlag();
		String text = "FLAG = " + flagValue;
		try {
			File file = new File(hostConfig + "BSFM.flag");
			Writer output = null;
			output = new BufferedWriter(new FileWriter(file));
			output.write(text);
			output.close();
			target = IConstants.SUCCESS_KEY;
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY)) {
			logger.info("message.ChangeFlagVaueSuccessfully");
		}
		if (target.equalsIgnoreCase(IConstants.FAILURE_KEY)) {
			logger.error("message.ChangeFlagFailure");
		}

		return target;
	}

}
