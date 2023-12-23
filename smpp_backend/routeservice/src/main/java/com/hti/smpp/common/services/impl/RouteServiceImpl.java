package com.hti.smpp.common.services.impl;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder.EntryObject;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.contacts.dto.GroupEntry;
import com.hti.smpp.common.contacts.repository.GroupEntryRepository;
import com.hti.smpp.common.dto.UserEntryExt;
import com.hti.smpp.common.exception.DataAccessError;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.ScheduledTimeException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.exception.WorkBookException;
import com.hti.smpp.common.login.dto.Role;
import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.UserRepository;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.network.repository.NetworkEntryRepository;
import com.hti.smpp.common.request.HlrEntryArrForm;
import com.hti.smpp.common.request.OptEntryArrForm;
import com.hti.smpp.common.request.RouteEntryArrForm;
import com.hti.smpp.common.request.RouteEntryForm;
import com.hti.smpp.common.request.RouteRequest;
import com.hti.smpp.common.request.SearchCriteria;
import com.hti.smpp.common.response.OptionRouteResponse;
import com.hti.smpp.common.response.RouteUserResponse;
import com.hti.smpp.common.route.dto.HlrEntryLog;
import com.hti.smpp.common.route.dto.HlrRouteEntry;
import com.hti.smpp.common.route.dto.OptionalEntryLog;
import com.hti.smpp.common.route.dto.OptionalRouteEntry;
import com.hti.smpp.common.route.dto.RouteEntry;
import com.hti.smpp.common.route.dto.RouteEntryExt;
import com.hti.smpp.common.route.dto.RouteEntryLog;
import com.hti.smpp.common.route.repository.HlrEntryLogRepository;
import com.hti.smpp.common.route.repository.HlrRouteEntryRepository;
import com.hti.smpp.common.route.repository.OptionalEntryLogRepository;
import com.hti.smpp.common.route.repository.OptionalRouteEntryRepository;
import com.hti.smpp.common.route.repository.RouteEntryLogRepository;
import com.hti.smpp.common.route.repository.RouteEntryRepository;
import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.sales.repository.SalesRepository;
import com.hti.smpp.common.schedule.dto.HlrRouteEntrySch;
import com.hti.smpp.common.schedule.dto.OptionalRouteEntrySchedule;
import com.hti.smpp.common.schedule.dto.RoutemasterSch;
import com.hti.smpp.common.schedule.repository.HlrRouteEntrySchRepository;
import com.hti.smpp.common.schedule.repository.OptionalRouteEntryScheduleRepository;
import com.hti.smpp.common.schedule.repository.RoutemasterSchRepository;
import com.hti.smpp.common.services.RouteServices;
import com.hti.smpp.common.smsc.dto.SmscEntry;
import com.hti.smpp.common.smsc.repository.SmscEntryRepository;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.UserSessionObject;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.InternalServerErrorException;

@Service
public class RouteServiceImpl implements RouteServices {

	private static final Logger logger = LoggerFactory.getLogger(RouteServiceImpl.class);

	@Autowired
	private RouteEntryRepository routeEntryRepository;
	@Autowired
	private HlrEntryLogRepository hlrEntryLogRepository;

	@Autowired
	private HlrRouteEntryRepository hlrRouteEntryRepository;

	@Autowired
	private OptionalEntryLogRepository optionalEntryLogRepository;

	@Autowired
	private OptionalRouteEntryRepository optionalRouteEntryRepository;

	@Autowired
	private RouteEntryLogRepository routeEntryLogRepository;

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private GroupEntryRepository groupEntryRepository;

	@Autowired
	private SmscEntryRepository SmscEntryRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private UserRepository loginRepository;

	@Autowired
	private OptionalRouteEntryScheduleRepository optionalRouteEntryScheduleRepository;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private NetworkEntryRepository networkEntryRepository;

	@Autowired
	private SalesRepository salesRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private RoutemasterSchRepository routemasterSchRepository;

	@Autowired
	private HlrRouteEntrySchRepository hlrRouteEntrySchRepository;

	@Override
	@Transactional
	public String saveRoute(RouteRequest RouteRequest, String username) {

		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		String target = IConstants.FAILURE_KEY;
		// Logging the username
		System.out.println("Username: " + username);
		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		}
		List<RouteEntry> removeList = new ArrayList<RouteEntry>();
		List<RouteEntryExt> routingList = new ArrayList<>();
		Map<Integer, List<RouteEntryExt>> userWiseRouting = new HashMap<>();
		int[] id = RouteRequest.getId();
		if (id.length > 0) {
			String editOn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			int[] userId = RouteRequest.getUserId();
			int[] networkId = RouteRequest.getNetworkId();
			int[] smscId = RouteRequest.getSmscId();
			int[] groupId = RouteRequest.getGroupId();
			double[] cost = RouteRequest.getCost();
			String[] smscType = RouteRequest.getSmscType();
			String[] remarks = RouteRequest.getRemarks();
			RouteEntry routing;
			RouteEntryExt routeExt;

			for (int i = 0; i < id.length; i++) {
				routing = new RouteEntry(userId[i], systemId, editOn, remarks[i]);
				routing.setNetworkId(networkId[i]);
				routing.setSmscId(smscId[i]);
				routing.setGroupId(groupId[i]);

				if (smscType[i] != null && smscType[i].length() == 1) {
					routing.setSmscType(smscType[i]);
				}

				routing.setCost(cost[i]);
				routeExt = new RouteEntryExt(routing);
				routeExt.setHlrRouteEntry(new HlrRouteEntry(systemId, editOn));
				routeExt.setRouteOptEntry(new OptionalRouteEntry(systemId, editOn));

				List<RouteEntryExt> routingListForUser = userWiseRouting.getOrDefault(userId[i], new ArrayList<>());
				routingListForUser.add(routeExt);
				userWiseRouting.put(userId[i], routingListForUser);

			}
			if (userWiseRouting.isEmpty()) {
				logger.error("error: record unavailable");
			} else {
				try {
					// ----- check for auto_copy_routing users ------------
					Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
							.is("autoCopyRouting");
					Set<Integer> auto_copy_route_users = GlobalVars.WebmasterEntries.keySet(p);
					if (!auto_copy_route_users.isEmpty()) {
						EntryObject e = new PredicateBuilderImpl().getEntryObject();
						Predicate<Integer, UserEntry> pu = e.get("role").equal("admin")
								.and(e.get("id").in(userWiseRouting.keySet().stream().toArray(Integer[]::new)));
						Collection<UserEntry> resellers = GlobalVars.UserEntries.values(pu);
						for (UserEntry reseller : resellers) {
							pu = new PredicateBuilderImpl().getEntryObject().get("masterId")
									.equal(reseller.getSystemId());
							for (int subUserId : GlobalVars.UserEntries.keySet(pu)) {
								logger.info(reseller.getSystemId() + " Checking Copy Route for SubUser: " + subUserId);
								if (auto_copy_route_users.contains(subUserId)) {
									logger.info(reseller.getSystemId() + " Auto Copy Route Enabled For SubUser: "
											+ subUserId);
									if (userWiseRouting.containsKey(subUserId)) {
										userWiseRouting.remove(subUserId);
									}
									WebMasterEntry webEntry = GlobalVars.WebmasterEntries.get(subUserId);
									double margin = 0;
									boolean isPercent = false;
									if (webEntry.getRouteMargin() != null && webEntry.getRouteMargin().length() > 0) {
										String margin_str = webEntry.getRouteMargin();
										if (margin_str.contains("%")) {
											isPercent = true;
											margin_str = margin_str.substring(0, margin_str.indexOf("%")).trim();
										}
										try {
											margin = Double.parseDouble(margin_str);
										} catch (Exception ex) {
											logger.error(subUserId + "Invalid margin: " + margin_str);
											throw new InternalServerException("Parse Exception: "+ex.getLocalizedMessage());
										}
									}
									logger.info(
											reseller.getSystemId() + " SubUser[" + subUserId + "] Margin: " + margin);
									List<RouteEntryExt> child_routing = new ArrayList<RouteEntryExt>();
									Map<Integer, RouteEntry> child_network_routing = getNetworkRouting(subUserId);
									RouteEntry childRouteEntry = null;
									RouteEntryExt childRouteEntryExt = null;
									for (RouteEntryExt ext : userWiseRouting.get(reseller.getId())) {
										logger.info(reseller.getSystemId() + " SubUser[" + subUserId + "] NetworkId: "
												+ ext.getBasic());
										childRouteEntry = new RouteEntry(subUserId, ext.getBasic().getNetworkId(),
												ext.getBasic().getSmscId(), ext.getBasic().getGroupId(),
												ext.getBasic().getCost(), ext.getBasic().getSmscType(),
												ext.getBasic().getEditBy(), ext.getBasic().getEditOn(),
												ext.getBasic().getRemarks());
										if (margin > 0) {
											double child_cost = childRouteEntry.getCost();
											if (isPercent) {
												child_cost = child_cost + ((child_cost * margin) / 100);
											} else {
												child_cost = child_cost + margin;
											}
											childRouteEntry.setCost(child_cost);
										}
										if (child_network_routing.containsKey(ext.getBasic().getNetworkId())) {
											logger.info(reseller.getSystemId() + " SubUser[" + subUserId
													+ "] Already Has Network: " + ext.getBasic().getNetworkId());
											// put to remove list
											removeList.add(child_network_routing.get(ext.getBasic().getNetworkId()));
										}
										childRouteEntryExt = new RouteEntryExt(childRouteEntry);
										childRouteEntryExt.setHlrRouteEntry(ext.getHlrRouteEntry());
										childRouteEntryExt.setRouteOptEntry(ext.getRouteOptEntry());
										child_routing.add(childRouteEntryExt);
										logger.info(reseller.getSystemId() + " SubUser[" + subUserId + "] AEntry: "
												+ childRouteEntry);
									}
									logger.info(reseller.getSystemId() + " SubUser[" + subUserId + "] Routings: "
											+ child_routing.size());
									userWiseRouting.put(subUserId, child_routing);
								}
							}
						}
					}
					logger.info(systemId + " Add Routing Users: " + userWiseRouting.keySet());
					for (List<RouteEntryExt> user_wise_entries : userWiseRouting.values()) {
						routingList.addAll(user_wise_entries);
					}
					logger.info(systemId + " Add Routing Entries: " + routingList.size());
					if (!removeList.isEmpty()) {
						logger.info(systemId + " Remove Routing Entries: " + removeList.size());
						deleteRouteEntries(removeList);
					}

					saveEntries(routingList);

					for (int user : userWiseRouting.keySet()) {
						MultiUtility.refreshRouting(user);
					}
					MultiUtility.changeFlag(Constants.CLIENT_FLAG_FILE, "707");
				} catch (Exception ex) {
					logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
					throw new InternalServerException("Unexpected Exception: "+ex.getLocalizedMessage());
				}
			}
		} else {
			logger.error("error: record unavailable");
			throw new NotFoundException("Exception: Record Unavailable!");
		}
		return target;
	}

	@Override
	public void saveDefaultEntries(RouteEntry entry) {
		RouteEntryExt defaultEntryExt = new RouteEntryExt(entry);
		defaultEntryExt.setHlrRouteEntry(new HlrRouteEntry(entry.getEditBy(), entry.getEditOn()));
		defaultEntryExt.setRouteOptEntry(new OptionalRouteEntry(entry.getEditBy(), entry.getEditOn()));
		saveRouteEntry(defaultEntryExt);
	}

	@Override
	public void deleteRouteEntries(int userId) {
		routeEntryRepository.deleteByUserId(userId);

	}

	@Override
	public void deleteRouteEntries(int userId, Set<Integer> networks) {
		routeEntryRepository.deleteByUserIdAndNetworkIdIn(userId, new ArrayList<>(networks));
	}

	@Override
	@Transactional
	public void deleteRouteEntries(List<RouteEntry> list) {
		for (RouteEntry entry : list) {
			try {
				routeEntryRepository.delete(entry);
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
				throw new InternalServerErrorException("Error in Deletion: "+e.getLocalizedMessage());
			}

			HlrRouteEntry hlr = new HlrRouteEntry();
			hlr.setRouteId(entry.getId());
			try {
				hlrRouteEntryRepository.delete(hlr);
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
				throw new InternalServerErrorException("Error in Deletion: "+e.getLocalizedMessage());
			}

			OptionalRouteEntry opt = new OptionalRouteEntry();
			opt.setRouteId(entry.getId());
			try {
				optionalRouteEntryRepository.delete(opt);
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
				throw new InternalServerErrorException("Error in Deletion: "+e.getLocalizedMessage());
			}
		}
	}

	@Override
	@Transactional
	public void updateRouteEntries(List<RouteEntry> list) {
		try {
			routeEntryRepository.saveAll(list);
		} catch (Exception e) {
			throw new InternalServerException("Exception: "+e.getLocalizedMessage());
		}
	}

	@Override
	@Transactional
	public void updateOptionalRouteEntries(List<OptionalRouteEntry> list) {
		optionalRouteEntryRepository.saveAll(list);
	}

	@Override
	@Transactional
	public void updateHlrRouteEntries(List<HlrRouteEntry> list) {
		try {
			hlrRouteEntryRepository.saveAll(list);
		} catch (Exception e) {
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}
	}

	@Override
	@Transactional
	public void saveEntries(List<RouteEntryExt> list) {
		for (RouteEntryExt entry : list) {
			RouteEntry basic = entry.getBasic();
			HlrRouteEntry hlr = entry.getHlrRouteEntry();
			OptionalRouteEntry opt = entry.getRouteOptEntry();

			try {
				// Save basic entry
				routeEntryRepository.save(basic);

				// Set routeId and save HlrRouteEntry
				hlr.setRouteId(basic.getId());
				hlrRouteEntryRepository.save(hlr);

				// Set routeId and save OptionalRouteEntry
				opt.setRouteId(basic.getId());
				optionalRouteEntryRepository.save(opt);

			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
				throw new InternalServerErrorException("Error in Saving: "+e.getLocalizedMessage());
			}
		}

	}

	@Override
	public Map<Integer, RouteEntryExt> listRouteEntries(int userId, boolean hlr, boolean optional, boolean display) {

		List<RouteEntry> basicEntries = null;
		try {
			basicEntries = routeEntryRepository.findByUserId(userId);
		} catch (Exception e) {
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}

		if (basicEntries.isEmpty()) {
			logger.info("Routing Entries Not Found For " + userId);
			return new HashMap<>();
		}

		Map<Integer, RouteEntryExt> routeEntries = basicEntries.stream()
				.collect(Collectors.toMap(RouteEntry::getId, basic -> {
					RouteEntryExt entry = new RouteEntryExt(basic);
					// Additional processing logic...

					if (hlr) {
						HlrRouteEntry hlrEntry = null;
						try {
							hlrEntry = hlrRouteEntryRepository.findByRouteId(basic.getId());
						} catch (Exception e) {
							logger.error(e.getLocalizedMessage());
							throw new InternalServerException(e.getLocalizedMessage());
						}
						entry.setHlrRouteEntry(hlrEntry);
					}

					if (optional) {
						OptionalRouteEntry optionalEntry = null;
						try {
							optionalEntry = optionalRouteEntryRepository.findByRouteId(basic.getId());
						} catch (Exception e) {
							logger.error(e.getLocalizedMessage());
							throw new InternalServerException(e.getLocalizedMessage());
						}
						entry.setRouteOptEntry(optionalEntry);
						// Additional processing logic...
					}

					return entry;
				}));

		logger.info(userId + " RouteEntries: " + routeEntries.size());
		return routeEntries;
	}

	@Override
	public Map<Integer, RouteEntry> getNetworkRouting(int userId) {
		Map<Integer, RouteEntry> networkRouting = null;
		try {
			networkRouting = new HashMap<Integer, RouteEntry>();
			Predicate<Integer, RouteEntry> p = new PredicateBuilderImpl().getEntryObject().get("userId").equal(userId);
			for (RouteEntry entry : GlobalVars.BasicRouteEntries.values(p)) {
				networkRouting.put(entry.getNetworkId(), entry);
			}
		} catch (Exception e) {
			throw new InternalServerException(e.getLocalizedMessage());
		}
		return networkRouting;
	}

	@Override
	public Map<Integer, RouteEntryExt> listCoverage(int userId, boolean display, boolean cached) {
		Map<Integer, RouteEntryExt> list = new LinkedHashMap<Integer, RouteEntryExt>();
		Map<Integer, String> smsc_name_mapping = null;
		Map<Integer, String> group_name_mapping = null;
		if (display) {
			smsc_name_mapping = listNames();
			group_name_mapping = listGroupNames();
		}
		if (cached) {
			Predicate<Integer, RouteEntry> p = new PredicateBuilderImpl().getEntryObject().get("userId").equal(userId);
			for (RouteEntry basic : GlobalVars.BasicRouteEntries.values(p)) {
				RouteEntryExt entry = new RouteEntryExt(basic);
				if (display) {
					// ------ set user values -----------------
					if (GlobalVars.UserEntries.containsKey(basic.getUserId())) {
						entry.setSystemId(GlobalVars.UserEntries.get(basic.getUserId()).getSystemId());
						entry.setMasterId(GlobalVars.UserEntries.get(basic.getUserId()).getMasterId());
						entry.setCurrency(GlobalVars.UserEntries.get(basic.getUserId()).getCurrency());
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
				list.put(entry.getBasic().getNetworkId(), entry);
			}
		} else {
			logger.info("listing RouteEntries From Database: " + userId);
			List<RouteEntry> db_list = listRoute(userId);
			for (RouteEntry basic : db_list) {
				RouteEntryExt entry = new RouteEntryExt(basic);
				if (display) {
					// ------ set user values -----------------
					if (GlobalVars.UserEntries.containsKey(entry.getBasic().getUserId())) {
						entry.setSystemId(GlobalVars.UserEntries.get(basic.getUserId()).getSystemId());
						entry.setMasterId(GlobalVars.UserEntries.get(basic.getUserId()).getMasterId());
						entry.setCurrency(GlobalVars.UserEntries.get(basic.getUserId()).getCurrency());
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
				list.put(entry.getBasic().getNetworkId(), entry);
			}
		}
		return list;
	}

	public List<RouteEntry> listRoute(int userId) {
		return routeEntryRepository.findByUserId(userId);
	}

	@Override
	public Map<Integer, RouteEntryExt> listCoverage(String systemId, boolean display, boolean cached) {
		int userId = GlobalVars.UserMapping.get(systemId);
		return listCoverage(userId, display, cached);
	}

	@Override
	public Map<Integer, RouteEntryLog> listBasicLogEntries(int[] routeId) {
		Map<Integer, RouteEntryLog> map = null;
		try {
			List<RouteEntryLog> list = listBasicLog(routeId);
			map = new LinkedHashMap<Integer, RouteEntryLog>();
			for (RouteEntryLog entryLog : list) {
				if (!map.containsKey(entryLog.getId())) {
					map.put(entryLog.getId(), entryLog);
				}
			}
		} catch (Exception e) {
			throw new InternalServerException(e.getLocalizedMessage());
		}
		return map;
	}

	public List<RouteEntryLog> listBasicLog(int[] routeId) {
		return routeEntryLogRepository.findByIdInOrderByAffectedOnDesc(routeId);
	}

	@Override
	public Map<Integer, HlrEntryLog> listHlrLog(int[] routeId) {
		List<HlrEntryLog> list = hlrEntryLogRepository.findByRouteIdInOrderByAffectedOnDesc(routeId);
		Map<Integer, HlrEntryLog> map = new LinkedHashMap<Integer, HlrEntryLog>();
		for (HlrEntryLog entryLog : list) {
			if (!map.containsKey(entryLog.getRouteId())) {
				map.put(entryLog.getRouteId(), entryLog);
			}
		}
		return map;
	}

	@Override
	public Map<Integer, OptionalEntryLog> listOptLog(int[] routeId) {
		List<OptionalEntryLog> list = optionalEntryLogRepository.findByRouteIdInOrderByAffectedOnDesc(routeId);
		;
		Map<Integer, OptionalEntryLog> map = new LinkedHashMap<Integer, OptionalEntryLog>();
		for (OptionalEntryLog entryLog : list) {
			if (!map.containsKey(entryLog.getRouteId())) {
				map.put(entryLog.getRouteId(), entryLog);
			}
		}
		return map;
	}

	@Override
	public Set<String> distinctSmscTypes() {
		Set<String> smscTypes = new TreeSet<String>();
		for (RouteEntry entry : GlobalVars.BasicRouteEntries.values()) {
			if (entry.getSmscType() != null && entry.getSmscType().length() > 0) {
				smscTypes.add(entry.getSmscType().toUpperCase());
			}
		}
		return smscTypes;
	}

	@Override
	public double calculateLookupCost(int userId, List<String> numbers) {
		Map<String, Integer> prefix_mapping = new HashMap<String, Integer>(GlobalVars.PrefixMapping);
		double totalcost = 0;
		Map<Integer, RouteEntryExt> routingEntries = getNetworkRouting(userId, true);
		for (String destination : numbers) {
			int networkId = 0;
			int length = destination.length();
			for (int i = length; i >= 1; i--) {
				if (prefix_mapping.containsKey(destination.substring(0, i))) {
					networkId = prefix_mapping.get(destination.substring(0, i));
					break;
				}
			}
			RouteEntryExt route = null;
			if (routingEntries.containsKey(networkId)) {
				route = routingEntries.get(networkId);
			} else {
				route = routingEntries.get(0); // get default routing
			}
			if (route != null && route.getHlrRouteEntry().isHlr()) {
				totalcost += route.getHlrRouteEntry().getCost();
				logger.info(userId + " Dest: " + destination + "[" + networkId + "], HlrCost= "
						+ route.getHlrRouteEntry().getCost());
			} else {
				logger.info(userId + " Dest: " + destination + "[" + networkId + "] Route Not Configured");
			}
		}
		return totalcost;
	}

	private Map<Integer, RouteEntryExt> getNetworkRouting(int userId, boolean hlr) {
		Map<Integer, RouteEntryExt> networkRouting = new HashMap<Integer, RouteEntryExt>();
		Map<Integer, RouteEntryExt> routingEntries = listRouteEntries(userId, hlr, false, false);
		for (RouteEntryExt entry : routingEntries.values()) {
			networkRouting.put(entry.getBasic().getNetworkId(), entry);
		}
		return networkRouting;
	}

	@Override
	public double calculateRoutingCost(int userId, List<String> numbers, int msgParts) {
		Map<String, Integer> prefix_mapping = new HashMap<String, Integer>(GlobalVars.PrefixMapping);
		double totalcost = 0;
		Map<Integer, RouteEntryExt> routingEntries = getNetworkRouting(userId, false);
		for (String destination : numbers) {
			int networkId = 0;
			int length = destination.length();
			for (int i = length; i >= 1; i--) {
				if (prefix_mapping.containsKey(destination.substring(0, i))) {
					networkId = prefix_mapping.get(destination.substring(0, i));
					break;
				}
			}
			RouteEntryExt route = null;
			if (routingEntries.containsKey(networkId)) {
				route = routingEntries.get(networkId);
			} else {
				route = routingEntries.get(0); // get default routing
			}
			double cost = 0;
			if (route != null) {
				cost = route.getBasic().getCost();
			}
			totalcost = totalcost + (cost * msgParts);
			// System.out.println("Number: " + destination + " cost: " + cost + " total:" +
			// totalcost + " route: " + route);
		}
		return totalcost;
	}

	@Override
	public double calculateRoutingCost(int userId, Map<String, Integer> numbersParts) {
		Map<String, Integer> prefix_mapping = new HashMap<String, Integer>(GlobalVars.PrefixMapping);
		double totalcost = 0;
		Map<Integer, RouteEntryExt> routingEntries = getNetworkRouting(userId, false);
		for (Map.Entry<String, Integer> entry : numbersParts.entrySet()) {
			String destination = entry.getKey();
			int msgParts = entry.getValue();
			int networkId = 0;
			int length = destination.length();
			for (int i = length; i >= 1; i--) {
				if (prefix_mapping.containsKey(destination.substring(0, i))) {
					networkId = prefix_mapping.get(destination.substring(0, i));
					break;
				}
			}
			RouteEntryExt route = null;
			if (routingEntries.containsKey(networkId)) {
				route = routingEntries.get(networkId);
			} else {
				route = routingEntries.get(0); // get default routing
			}
			double cost = 0;
			if (route != null) {
				cost = route.getBasic().getCost();
			}
			totalcost = totalcost + (cost * msgParts);
			// System.out.println("Number: " + destination + " cost: " + cost + " total:" +
			// totalcost + " route: " + route);
		}
		return totalcost;
	}

	@Override
	@Transactional
	public void saveRouteEntry(RouteEntryExt entry) {
		try {
			RouteEntry basic = entry.getBasic();
			HlrRouteEntry hlr = entry.getHlrRouteEntry();
			OptionalRouteEntry opt = entry.getRouteOptEntry();

			routeEntryRepository.save(basic);

			hlr.setRouteId(basic.getId());
			hlrRouteEntryRepository.save(hlr);

			opt.setRouteId(basic.getId());
			optionalRouteEntryRepository.save(opt);

		} catch (Exception e) {
			logger.error("Error in saving: "+e.getLocalizedMessage());
			throw new InternalServerErrorException("Exception: "+e.getLocalizedMessage());
		}
	}

	@Override
	public Map<Integer, RouteEntryExt> listRouteEntries(SearchCriteria searchCriteria) {
		Map<Integer, RouteEntryExt> result = new HashMap<Integer, RouteEntryExt>();
		Map<Integer, String> smsc_name_mapping = listNames();
		Map<Integer, String> group_name_mapping = listGroupNames();

		try {
			Map<Integer, Set<String>> smsc_group_mapping = getSmscGroupMapping();
			if (searchCriteria.getRouteId() != null && searchCriteria.getRouteId().length > 0) {
				for (int id : searchCriteria.getRouteId()) {
					if (GlobalVars.BasicRouteEntries.containsKey(id)) {
						RouteEntry basic = GlobalVars.BasicRouteEntries.get(id);
						if (GlobalVars.UserEntries.containsKey(basic.getUserId())) {
							if (expired(GlobalVars.UserEntries.get(basic.getUserId()))) {
								continue;
							}
						} else {
							logger.error("UserEntry Not Found: " + basic.getUserId());
							continue;
						}
						RouteEntryExt entry = new RouteEntryExt(basic);
						// ------ set user values -----------------
						if (GlobalVars.UserEntries.containsKey(basic.getUserId())) {
							entry.setSystemId(GlobalVars.UserEntries.get(basic.getUserId()).getSystemId());
							entry.setMasterId(GlobalVars.UserEntries.get(basic.getUserId()).getMasterId());
							entry.setCurrency(GlobalVars.UserEntries.get(basic.getUserId()).getCurrency());
							entry.setAccountType(GlobalVars.WebmasterEntries.get(basic.getUserId()).getAccountType());
						}
						// ------ set network values -----------------
						// NetworkEntry network = CacheService.getNetworkEntry(entry.getNetworkId());
						if (GlobalVars.NetworkEntries.containsKey(entry.getBasic().getNetworkId())) {
							NetworkEntry network = GlobalVars.NetworkEntries.get(entry.getBasic().getNetworkId());
							entry.setCountry(network.getCountry());
							entry.setOperator(network.getOperator());
							if (entry.getBasic().getNetworkId() == 0) {
								entry.setMcc("000");
								entry.setMnc("000");
							} else {
								entry.setMcc(network.getMcc());
								entry.setMnc(network.getMnc());
							}
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
						// ------------------- end set dynamic values --------------------
						if (searchCriteria.isHlrEntry()) {
							if (GlobalVars.HlrRouteEntries.containsKey(id)) {
								entry.setHlrRouteEntry(GlobalVars.HlrRouteEntries.get(id));
							}
						}
						if (searchCriteria.isOptEntry()) {
							if (GlobalVars.OptionalRouteEntries.containsKey(id)) {
								OptionalRouteEntry optEntry = GlobalVars.OptionalRouteEntries.get(id);
								if (optEntry.getNumSmscId() == 0) {
									entry.setNumSmsc("DOWN [NONE]");
								} else {
									if (smsc_name_mapping.containsKey(optEntry.getNumSmscId())) {
										entry.setNumSmsc(smsc_name_mapping.get(optEntry.getNumSmscId()) + " "
												+ smsc_group_mapping.get(optEntry.getNumSmscId()));
									}
								}
								if (optEntry.getBackupSmscId() == 0) {
									entry.setBackupSmsc("DOWN [NONE]");
								} else {
									if (smsc_name_mapping.containsKey(optEntry.getBackupSmscId())) {
										entry.setBackupSmsc(smsc_name_mapping.get(optEntry.getBackupSmscId()) + " "
												+ smsc_group_mapping.get(optEntry.getBackupSmscId()));
									}
								}
								if (optEntry.getRegSmscId() == 0) {
									entry.setRegSmsc("DOWN [NONE]");
								} else {
									if (smsc_name_mapping.containsKey(optEntry.getRegSmscId())) {
										entry.setRegSmsc(smsc_name_mapping.get(optEntry.getRegSmscId()) + " "
												+ smsc_group_mapping.get(optEntry.getRegSmscId()));
									}
								}
								if (group_name_mapping.containsKey(optEntry.getRegGroupId())) {
									entry.setRegGroup(group_name_mapping.get(optEntry.getRegGroupId()));
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
								entry.setRouteOptEntry(optEntry);
								// --------------------------------------
							}
						}
						result.put(basic.getId(), entry);
					} else {
						logger.error(id + " Cached Entry Not Found ");
					}
				}
			} else {
				List<RouteEntry> list = listRoute(searchCriteria);
				Set<String> currencies = new HashSet<String>();
				Set<String> accountTypes = new HashSet<String>();
				if (searchCriteria.getCurrency() != null) {
					currencies.addAll(Arrays.asList(searchCriteria.getCurrency()));
					logger.info("Currencies: " + currencies);
				}
				if (searchCriteria.getAccountType() != null) {
					accountTypes.addAll(Arrays.asList(searchCriteria.getAccountType()));
					logger.info("AccountTypes: " + accountTypes);
				}
				for (RouteEntry db_entry : list) {
					int id = db_entry.getId();
					if (GlobalVars.BasicRouteEntries.containsKey(id)) {
						RouteEntry basic = GlobalVars.BasicRouteEntries.get(id);
						if (GlobalVars.UserEntries.containsKey(basic.getUserId())) {
							if (expired(GlobalVars.UserEntries.get(basic.getUserId()))) {
								continue;
							}
						} else {
							logger.error("UserEntry Not Found: " + basic.getUserId());
							continue;
						}
						if (currencies.isEmpty() && accountTypes.isEmpty()) {
							RouteEntryExt entry = new RouteEntryExt(basic);
							// ------ set user values -----------------
							if (GlobalVars.UserEntries.containsKey(basic.getUserId())) {
								entry.setSystemId(GlobalVars.UserEntries.get(basic.getUserId()).getSystemId());
								entry.setMasterId(GlobalVars.UserEntries.get(basic.getUserId()).getMasterId());
								entry.setCurrency(GlobalVars.UserEntries.get(basic.getUserId()).getCurrency());
								entry.setAccountType(
										GlobalVars.WebmasterEntries.get(basic.getUserId()).getAccountType());
							}
							// ------ set network values -----------------
							// NetworkEntry network = CacheService.getNetworkEntry(entry.getNetworkId());
							if (GlobalVars.NetworkEntries.containsKey(basic.getNetworkId())) {
								NetworkEntry network = GlobalVars.NetworkEntries.get(basic.getNetworkId());
								entry.setCountry(network.getCountry());
								entry.setOperator(network.getOperator());
								if (basic.getNetworkId() == 0) {
									entry.setMcc("000");
									entry.setMnc("000");
								} else {
									entry.setMcc(network.getMcc());
									entry.setMnc(network.getMnc());
								}
							}
							// ------ set Smsc values -----------------
							if (entry.getBasic().getSmscId() == 0) {
								entry.setSmsc("Down");
							} else {
								if (smsc_name_mapping.containsKey(basic.getSmscId())) {
									entry.setSmsc(smsc_name_mapping.get(basic.getSmscId()));
								}
							}
							if (group_name_mapping.containsKey(entry.getBasic().getGroupId())) {
								entry.setGroup(group_name_mapping.get(entry.getBasic().getGroupId()));
							}
							// ------------------- end set dynamic values --------------------
							if (searchCriteria.isHlrEntry()) {
								if (GlobalVars.HlrRouteEntries.containsKey(id)) {
									entry.setHlrRouteEntry(GlobalVars.HlrRouteEntries.get(id));
								}
							}
							if (searchCriteria.isOptEntry()) {
								if (GlobalVars.OptionalRouteEntries.containsKey(id)) {
									OptionalRouteEntry optEntry = GlobalVars.OptionalRouteEntries.get(id);
									if (optEntry.getNumSmscId() == 0) {
										entry.setNumSmsc("DOWN [NONE]");
									} else {
										if (smsc_name_mapping.containsKey(optEntry.getNumSmscId())) {
											entry.setNumSmsc(smsc_name_mapping.get(optEntry.getNumSmscId()) + " "
													+ smsc_group_mapping.get(optEntry.getNumSmscId()));
										}
									}
									if (optEntry.getBackupSmscId() == 0) {
										entry.setBackupSmsc("DOWN [NONE]");
									} else {
										if (smsc_name_mapping.containsKey(optEntry.getBackupSmscId())) {
											entry.setBackupSmsc(smsc_name_mapping.get(optEntry.getBackupSmscId()) + " "
													+ smsc_group_mapping.get(optEntry.getBackupSmscId()));
										}
									}
									if (optEntry.getRegSmscId() == 0) {
										entry.setRegSmsc("DOWN [NONE]");
									} else {
										if (smsc_name_mapping.containsKey(optEntry.getRegSmscId())) {
											entry.setRegSmsc(smsc_name_mapping.get(optEntry.getRegSmscId()) + " "
													+ smsc_group_mapping.get(optEntry.getRegSmscId()));
										}
									}
									if (group_name_mapping.containsKey(optEntry.getRegGroupId())) {
										entry.setRegGroup(group_name_mapping.get(optEntry.getRegGroupId()));
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
									entry.setRouteOptEntry(optEntry);
									// --------------------------------------
								}
							}
							result.put(basic.getId(), entry);
						} else {
							String currency = null;
							String accountType = null;
							if (GlobalVars.WebmasterEntries.containsKey(basic.getUserId())) {
								accountType = GlobalVars.WebmasterEntries.get(basic.getUserId()).getAccountType();
							}
							if (!currencies.isEmpty()) {
								if (GlobalVars.UserEntries.containsKey(basic.getUserId())) {
									currency = GlobalVars.UserEntries.get(basic.getUserId()).getCurrency();
								}
								if (currency != null && currencies.contains(currency)) {
									// criteria matched
								} else {
									continue;
								}
							}
							if (!accountTypes.isEmpty()) {
								if (GlobalVars.WebmasterEntries.containsKey(basic.getUserId())) {
									accountType = GlobalVars.WebmasterEntries.get(basic.getUserId()).getAccountType();
								}
								if (accountType != null && accountTypes.contains(accountType)) {
									// criteria matched
								} else {
									continue;
								}
							}
							RouteEntryExt entry = new RouteEntryExt(basic);
							// ------ set user values -----------------
							if (GlobalVars.UserEntries.containsKey(basic.getUserId())) {
								entry.setSystemId(GlobalVars.UserEntries.get(basic.getUserId()).getSystemId());
								entry.setMasterId(GlobalVars.UserEntries.get(basic.getUserId()).getMasterId());
								entry.setCurrency(GlobalVars.UserEntries.get(basic.getUserId()).getCurrency());
								entry.setAccountType(
										GlobalVars.WebmasterEntries.get(basic.getUserId()).getAccountType());
							}
							// ------ set network values -----------------
							// NetworkEntry network = CacheService.getNetworkEntry(entry.getNetworkId());
							if (GlobalVars.NetworkEntries.containsKey(basic.getNetworkId())) {
								NetworkEntry network = GlobalVars.NetworkEntries.get(basic.getNetworkId());
								entry.setCountry(network.getCountry());
								entry.setOperator(network.getOperator());
								entry.setMcc(network.getMcc());
								entry.setMnc(network.getMnc());
							}
							// ------ set Smsc values -----------------
							if (entry.getBasic().getSmscId() == 0) {
								entry.setSmsc("Down");
							} else {
								if (smsc_name_mapping.containsKey(basic.getSmscId())) {
									entry.setSmsc(smsc_name_mapping.get(basic.getSmscId()));
								}
							}
							if (group_name_mapping.containsKey(entry.getBasic().getGroupId())) {
								entry.setGroup(group_name_mapping.get(entry.getBasic().getGroupId()));
							}
							// ------------------- end set dynamic values --------------------
							if (searchCriteria.isHlrEntry()) {
								if (GlobalVars.HlrRouteEntries.containsKey(id)) {
									entry.setHlrRouteEntry(GlobalVars.HlrRouteEntries.get(id));
								}
							}
							if (searchCriteria.isOptEntry()) {
								if (GlobalVars.OptionalRouteEntries.containsKey(id)) {
									OptionalRouteEntry optEntry = GlobalVars.OptionalRouteEntries.get(id);
									if (optEntry.getNumSmscId() == 0) {
										entry.setNumSmsc("DOWN [NONE]");
									} else {
										if (smsc_name_mapping.containsKey(optEntry.getNumSmscId())) {
											entry.setNumSmsc(smsc_name_mapping.get(optEntry.getNumSmscId()) + " "
													+ smsc_group_mapping.get(optEntry.getNumSmscId()));
										}
									}
									if (optEntry.getBackupSmscId() == 0) {
										entry.setBackupSmsc("DOWN [NONE]");
									} else {
										if (smsc_name_mapping.containsKey(optEntry.getBackupSmscId())) {
											entry.setBackupSmsc(smsc_name_mapping.get(optEntry.getBackupSmscId()) + " "
													+ smsc_group_mapping.get(optEntry.getBackupSmscId()));
										}
									}
									if (optEntry.getRegSmscId() == 0) {
										entry.setRegSmsc("DOWN [NONE]");
									} else {
										if (smsc_name_mapping.containsKey(optEntry.getRegSmscId())) {
											entry.setRegSmsc(smsc_name_mapping.get(optEntry.getRegSmscId()) + " "
													+ smsc_group_mapping.get(optEntry.getRegSmscId()));
										}
									}
									if (group_name_mapping.containsKey(optEntry.getRegGroupId())) {
										entry.setRegGroup(group_name_mapping.get(optEntry.getRegGroupId()));
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
									entry.setRouteOptEntry(optEntry);
									// --------------------------------------
								}
							}
							result.put(basic.getId(), entry);
						}
					} else {
						logger.error(id + " Cached Entry Not Found ");
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception", e.fillInStackTrace());
			throw new InternalServerException("Exception: "+e.getLocalizedMessage());
		}
		return result;
	}

	public Map<Integer, Set<String>> getSmscGroupMapping() {
		Map<Integer, Set<String>> groupMapping = new HashMap<>();
		List<SmscEntry> results = null;
		try {
			results = SmscEntryRepository.findAll();
		} catch (Exception e) {
			logger.error("Exception: "+e.toString());
			throw new InternalServerException("Unexpected Exception: "+e.getLocalizedMessage());
		}

		for (SmscEntry result : results) {
			int smscId = result.getId(); // Assuming SmscEntry has a getSmscId() method
			String groupName = result.getName(); // Assuming SmscEntry has a getGroupName() method

			Set<String> names = groupMapping.getOrDefault(smscId, new HashSet<>());
			names.add(groupName);
			groupMapping.put(smscId, names);
		}

		return groupMapping;
	}

	public Map<Integer, String> listNames() {
		Map<Integer, String> names = new HashMap<Integer, String>();

		List<SmscEntry> smscEntry = null;
		try {
			smscEntry = SmscEntryRepository.findAll();
		} catch (Exception e) {
			logger.error(e.toString());
			throw new InternalServerException("Error: "+e.getLocalizedMessage());
		}

		for (SmscEntry entry : smscEntry) {
			names.put(entry.getId(), entry.getName());
		}
		names = names.entrySet().stream().sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return names;
	}

	public Map<Integer, String> listGroupNames() {
		Map<Integer, String> names = new HashMap<Integer, String>();
		names.put(0, "NONE");
		List<GroupEntry> groups = null;
		try {
			groups = groupEntryRepository.findAll();
		} catch (Exception e) {
			logger.error(e.toString());
			throw new InternalServerException("Exception: "+e.getLocalizedMessage());
		}
		for (GroupEntry entry : groups) {
			names.put(entry.getId(), entry.getName());
		}
		names = names.entrySet().stream().sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return names;
	}

	private boolean expired(UserEntry entry) {
		boolean expired = true;
		try {
			Date expiry_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(entry.getExpiry() + " 23:59:59");
			if (expiry_date.after(new Date())) {
				expired = false;
			} else {
				System.out.println(entry.getSystemId() + " Account Expired: " + entry.getExpiry());
			}
		} catch (ParseException e) {
			logger.error(entry.getSystemId(), "Expiry Parse Error: " + entry.getExpiry());
		}
		return expired;
	}

	public List<RouteEntry> listRoute(SearchCriteria searchCriteria) {
		List<RouteEntry> routeEntries = new ArrayList<>();

		try {
			int[] userIds = searchCriteria.getUserId();
			int[] smscIds = searchCriteria.getSmscId();
			int[] groupIds = searchCriteria.getGroupId();
			int[] networkIds = searchCriteria.getNetworkId();
			String[] smscTypes = searchCriteria.getSmscType();
			double minCost = searchCriteria.getMinCost();
			double maxCost = searchCriteria.getMaxCost();

			routeEntries = routeEntryRepository
					.findByUserIdInAndSmscIdInAndGroupIdInAndNetworkIdInAndSmscTypeInAndCostBetween(userIds, smscIds,
							groupIds, networkIds, smscTypes, minCost, maxCost);

			return routeEntries;
		} catch (Exception ex) {
			logger.error("Exception: "+ex.toString());
			throw new InternalServerException("Exception: "+ex.getLocalizedMessage());
		}
	}

	@Override
	public OptionRouteResponse updateOptionalRoute(OptEntryArrForm optRouteEntry, String username) {

		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		OptionRouteResponse responce = new OptionRouteResponse();
		System.out.println("Username: " + username);
		String target = IConstants.FAILURE_KEY;
		String masterid = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			masterid = userOptional.get().getMasterId();
		}
		Map<Integer, List<OptionalRouteEntry>> userWiseRouting = new HashMap<Integer, List<OptionalRouteEntry>>();
		String editOn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		try {
			List<OptionalRouteEntry> list = new ArrayList<OptionalRouteEntry>();
			int[] id = optRouteEntry.getRouteId();
			int[] userId = optRouteEntry.getUserId();
			int[] numSmscId = optRouteEntry.getNumSmscId();
			int[] backupSmscId = optRouteEntry.getBackupSmscId();
			int[] regSmscId = optRouteEntry.getRegSmscId();
			int[] regGroupId = optRouteEntry.getRegGroupId();
			String[] regSender = optRouteEntry.getRegSender();
			String[] forceSIDNum = optRouteEntry.getForceSenderNum();
			String[] forceSIDAlpha = optRouteEntry.getForceSenderAlpha();
			int[] smsLength = optRouteEntry.getSmsLength();
			int[] codeLength = optRouteEntry.getCodeLength();
			boolean[] replaceContent = optRouteEntry.getReplaceContent();
			String[] replacement = optRouteEntry.getReplacement();
			String[] expiredOn = optRouteEntry.getExpiredOn();
			boolean[] refund = optRouteEntry.getRefund();
			String[] msgAppender = optRouteEntry.getMsgAppender();
			String[] sourceAppender = optRouteEntry.getSourceAppender();
			String[] senderReplFrom = optRouteEntry.getSenderReplFrom();
			String[] senderReplTo = optRouteEntry.getSenderReplTo();
			OptionalRouteEntry routingDTO = null;
			for (int i = 0; i < id.length; i++) {
				routingDTO = new OptionalRouteEntry(masterid, editOn);
				routingDTO.setRouteId(id[i]);
				routingDTO.setNumSmscId(numSmscId[i]);
				routingDTO.setBackupSmscId(backupSmscId[i]);
				routingDTO.setRegGroupId(regGroupId[i]);
				routingDTO.setRegSmscId(regSmscId[i]);
				routingDTO.setRegSender(regSender[i]);
				// logger.info(id[i] + " " + " regSmscId: " + regSmscId[i] + " -> " +
				// routingDTO.getRegSmscId());
				routingDTO.setRefund(refund[i]);
				routingDTO.setExpiredOn(expiredOn[i]);
				routingDTO.setCodeLength(codeLength[i]);
				String msgAppend = msgAppender[i];
				if (msgAppend != null && msgAppend.length() > 0) {
					routingDTO.setMsgAppender(UTF16(msgAppend));
				}
				routingDTO.setSourceAppender(sourceAppender[i]);
				if (forceSIDAlpha[i] == null || forceSIDAlpha[i].trim().length() < 1) {
					routingDTO.setForceSenderAlpha(null);
				} else {
					routingDTO.setForceSenderAlpha(forceSIDAlpha[i]);
				}
				if (forceSIDNum[i] == null || forceSIDNum[i].trim().length() < 1) {
					routingDTO.setForceSenderNum(null);
				} else {
					routingDTO.setForceSenderNum(forceSIDNum[i]);
				}
				routingDTO.setSenderReplFrom(senderReplFrom[i]);
				routingDTO.setSenderReplTo(senderReplTo[i]);
				routingDTO.setSmsLength(smsLength[i]);
				if (replaceContent[i]) {
					if (replacement[i] != null && replacement[i].trim().length() > 0 && replacement[i].contains("|")) {
						String replacement_text = "";
						StringTokenizer tokens = new StringTokenizer(replacement[i], ",");
						while (tokens.hasMoreTokens()) {
							String next_token = tokens.nextToken();
							if (next_token.contains("|")) {
								String key = next_token.substring(0, next_token.indexOf("|"));
								String value = next_token.substring(next_token.indexOf("|") + 1, next_token.length());
								// System.out.println(key + " : " + value);
								replacement_text += UTF16(key) + "|" + UTF16(value) + ",";
								logger.debug(UTF16(key) + " : " + UTF16(value));
							}
						}
						if (replacement_text.length() > 0) {
							replacement_text = replacement_text.substring(0, replacement_text.length() - 1);
							routingDTO.setReplaceContent(true);
							routingDTO.setReplacement(replacement_text);
						}
					} else {
						routingDTO.setReplaceContent(false);
						routingDTO.setReplacement(null);
					}
				} else {
					routingDTO.setReplaceContent(false);
					routingDTO.setReplacement(null);
				}
				List<OptionalRouteEntry> opt_list = null;
				if (userWiseRouting.containsKey(userId[i])) {
					opt_list = userWiseRouting.get(userId[i]);
				} else {
					opt_list = new ArrayList<OptionalRouteEntry>();
				}
				opt_list.add(routingDTO);
				userWiseRouting.put(userId[i], opt_list);
			}
			if (!userWiseRouting.isEmpty()) {
				// ----- check for auto_copy_routing users ------------
				Predicate<Integer, WebMasterEntry> pw = new PredicateBuilderImpl().getEntryObject()
						.is("autoCopyRouting");
				Set<Integer> auto_copy_route_users = GlobalVars.WebmasterEntries.keySet(pw);
				if (!auto_copy_route_users.isEmpty()) {
					EntryObject e = new PredicateBuilderImpl().getEntryObject();
					Predicate<Integer, UserEntry> pu = e.get("role").equal("admin")
							.and(e.get("id").in(userWiseRouting.keySet().stream().toArray(Integer[]::new)));
					Collection<UserEntry> resellers = GlobalVars.UserEntries.values(pu);
					for (UserEntry reseller : resellers) {
						pu = new PredicateBuilderImpl().getEntryObject().get("masterId").equal(reseller.getSystemId());
						for (int subUserId : GlobalVars.UserEntries.keySet(pu)) {
							logger.info(reseller.getSystemId() + " Checking Copy Route for SubUser: " + subUserId);
							if (auto_copy_route_users.contains(subUserId)) {
								logger.info(
										reseller.getSystemId() + " Auto Copy Route Enabled For SubUser: " + subUserId);
								if (userWiseRouting.containsKey(subUserId)) {
									userWiseRouting.remove(subUserId);
								}
								Set<Integer> reseller_route_id = new HashSet<Integer>();
								for (OptionalRouteEntry hlrEntry : userWiseRouting.get(reseller.getId())) {
									reseller_route_id.add(hlrEntry.getRouteId());
								}
								e = new PredicateBuilderImpl().getEntryObject();
								Predicate<Integer, RouteEntry> p = e.get("userId").equal(reseller.getId())
										.and(e.get("id").in(reseller_route_id.stream().toArray(Integer[]::new)));
								Map<Integer, Integer> reseller_network_id = new HashMap<Integer, Integer>();
								for (RouteEntry basicEntry : GlobalVars.BasicRouteEntries.values(p)) {
									reseller_network_id.put(basicEntry.getId(), basicEntry.getNetworkId());
								}
								e = new PredicateBuilderImpl().getEntryObject();
								p = e.get("userId").equal(subUserId).and(e.get("networkId")
										.in(reseller_network_id.values().stream().toArray(Integer[]::new)));
								Map<Integer, Integer> child_network_id = new HashMap<Integer, Integer>();
								for (RouteEntry basicEntry : GlobalVars.BasicRouteEntries.values(p)) {
									child_network_id.put(basicEntry.getNetworkId(), basicEntry.getId());
								}
								List<OptionalRouteEntry> child_routing = new ArrayList<OptionalRouteEntry>();
								// child_routing.addAll(userWiseRouting.get(reseller.getId()));
								// Iterator<OptionalRouteEntry> itr = child_routing.iterator();
								OptionalRouteEntry childRouteEntry = null;
								for (OptionalRouteEntry masterEntry : userWiseRouting.get(reseller.getId())) {
									childRouteEntry = new OptionalRouteEntry(0, masterEntry.getNumSmscId(),
											masterEntry.getBackupSmscId(), masterEntry.getForceSenderNum(),
											masterEntry.getForceSenderAlpha(), masterEntry.getExpiredOn(),
											masterEntry.getSmsLength(), masterEntry.isRefund(),
											masterEntry.getRegSender(), masterEntry.getRegSmscId(),
											masterEntry.getCodeLength(), masterEntry.isReplaceContent(),
											masterEntry.getReplacement(), masterEntry.getMsgAppender(),
											masterEntry.getSourceAppender(), masterEntry.getEditBy(),
											masterEntry.getEditOn());
									childRouteEntry.setRegGroupId(masterEntry.getRegGroupId());
									int network_id = reseller_network_id.get(masterEntry.getRouteId());
									if (child_network_id.containsKey(network_id)) {
										childRouteEntry.setRouteId(child_network_id.get(network_id));
									} else {
										logger.info(reseller.getSystemId() + " SubUser[" + subUserId + "] Network["
												+ network_id + "] Skipped");
										continue;
									}
									child_routing.add(childRouteEntry);
								}
								logger.info(reseller.getSystemId() + " SubUser[" + subUserId + "] Routings: "
										+ child_routing.size());
								userWiseRouting.put(subUserId, child_routing);
							}
						}
					}
				}
				logger.info(masterid + " Edit Optional Routing Users: " + userWiseRouting.keySet());
				for (List<OptionalRouteEntry> user_wise_entries : userWiseRouting.values()) {
					list.addAll(user_wise_entries);
				}
				// ---------------- end ------------------------------
				logger.info("Optional Route Update Size: " + list.size());
				if (optRouteEntry.isSchedule()) {
					String[] scheduledOn = optRouteEntry.getScheduledOn().split(" ");
					DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
					Date scheduledDate = df.parse(scheduledOn[0]);
					Date currentDate = df.parse(df.format(new Date()));
					if (scheduledDate.before(currentDate)) {
						logger.error(masterid + " Optional Routing Scheduled Date is Before Current Date");
					} else {
						addOptRouteSchEntry(list, optRouteEntry.getScheduledOn());
						if (scheduledDate.after(currentDate)) {
							logger.error(masterid + " Optional Routing Not Scheduled For Today");
						} else {
							logger.info(masterid + " Optional Routing Scheduled For Today");
							Timer t = new Timer();
							t.schedule(new TimerTask() {
								public void run() {

									try {
										updateOptRouteSch(optRouteEntry.getScheduledOn());
									} catch (DataAccessException e) {
										logger.error("Data Access Exception: "+e.getLocalizedMessage());
										throw new DataAccessError("Data Access Exception: "+e.getLocalizedMessage());
									} catch (Exception e) {
										logger.error("Unexpected Error: "+e.getLocalizedMessage());
										throw new InternalServerException("Exception: "+e.getLocalizedMessage());
									}
									for (int user : userWiseRouting.keySet()) {
										MultiUtility.refreshRouting(user);
									}
									MultiUtility.changeFlag(Constants.CLIENT_FLAG_FILE, "707");

								}
							}, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(optRouteEntry.getScheduledOn()));
						}
						target = "schedule";
						logger.info("Schedule Succesful.");
					}
				} else {
					updateOptionalRouteEntries(list);
					List<RouteEntryExt> routinglist = getUpdateOptionalRoutingList(optRouteEntry.getCriterionEntries());
					if (!routinglist.isEmpty()) {
						Map<Integer, String> groupDetail = new HashMap<Integer, String>(listGroupNames());
						Map<Integer, String> smsclist = new HashMap<Integer, String>();
						Map<Integer, Set<String>> group_mapping = getSmscGroupMapping();
						Map<Integer, String> smscnames = listNames();
						for (int smsc_id : smscnames.keySet()) {
							String smsc_name = smscnames.get(smsc_id);
							if (group_mapping.containsKey(smsc_id)) {
								smsclist.put(smsc_id, smsc_name + " " + group_mapping.get(smsc_id));
							} else {
								smsclist.put(smsc_id, smsc_name + " [NONE]");
							}
						}
						smsclist.put(0, "DOWN [NONE]");

						responce.setGroupDetail(groupDetail);
						responce.setRoutinglist(routinglist);
						responce.setSmsclist(smsclist);
						target = "view";
					} else {
						target = IConstants.SUCCESS_KEY;
						logger.info("Routing Configured Successful!");
					}
				}
			} else {
				logger.error("error: record unavailable");
				throw new NotFoundException("Error: No Record Found");
			}
		} catch (NotFoundException ex) {
			logger.error(masterid, ex.toString());
			throw new NotFoundException("NotFoundException: "+ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error(masterid, ex.toString());
			throw new InternalServerException("InternalServerException: "+ex.getLocalizedMessage());
		}
		if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY) || target.equalsIgnoreCase("view")) {
			for (int user : userWiseRouting.keySet()) {
				MultiUtility.refreshRouting(user);
			}
			MultiUtility.changeFlag(Constants.CLIENT_FLAG_FILE, "707");
		}
		responce.setStatus(target);
		return responce;

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

	/*
	 * Converting hex to its character/symbol/number.
	 */
	public char toHexChar(int i) {
		if ((i >= 0) && (i <= 9)) {
			return (char) ('0' + i);
		} else {
			return (char) ('a' + (i - 10));
		}
	}

	@Transactional
	public void addOptRouteSchEntry(List<OptionalRouteEntry> list, String scheduleOn) throws Exception {
		try {

			list.forEach(entry -> {
				OptionalRouteEntrySchedule oproute = new OptionalRouteEntrySchedule();
				oproute.setRouteId(entry.getRouteId());
				oproute.setNumSmscId(entry.getNumSmscId());
				oproute.setBackupSmscId(entry.getBackupSmscId());
				oproute.setForceSenderNum(entry.getForceSenderNum());
				oproute.setForceSenderAlpha(entry.getForceSenderAlpha());
				oproute.setExpiredOn(entry.getExpiredOn());
				oproute.setSmsLength(entry.getSmsLength());
				oproute.setRefund(entry.isRefund());
				oproute.setRegSender(entry.getRegSender());
				oproute.setRegSmscId(entry.getRegSmscId());
				oproute.setRegGroupId(entry.getRegGroupId());
				oproute.setCodeLength(entry.getCodeLength());
				oproute.setReplaceContent(entry.isReplaceContent());
				oproute.setReplacement(entry.getReplacement());
				oproute.setMsgAppender(entry.getMsgAppender());
				oproute.setSourceAppender(entry.getSourceAppender());
				oproute.setEditBy(entry.getEditBy());
				oproute.setEditOn(entry.getEditOn());
				oproute.setSenderReplFrom(entry.getSenderReplFrom());
				oproute.setSenderReplTo(entry.getSenderReplTo());
				oproute.setScheduleOn(scheduleOn);

				optionalRouteEntryScheduleRepository.save(oproute);
			});

			// Log or return success message
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			throw new Exception("Failed to add Optional Route Entries", e);
		}
	}

	public void updateOptRouteSch(String scheduledOn) {
		routeEntryRepository.updateOptRouteSchAndDelete(scheduledOn);
	}

	@Override
	public OptionRouteResponse UpdateOptionalRouteUndo(OptEntryArrForm optRouteEntry, String username) {

		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		String target = IConstants.FAILURE_KEY;
		OptionRouteResponse responce = new OptionRouteResponse();
		String masterid = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			masterid = userOptional.get().getMasterId();
		}
		Set<Integer> refreshUsers = new HashSet<Integer>();
		logger.info("OptionalRoute Undo Requested By " + masterid);
		try {
			List<OptionalRouteEntry> list = new ArrayList<OptionalRouteEntry>();
			int[] id = optRouteEntry.getRouteId();
			int[] userId = optRouteEntry.getUserId();
			if (id != null && id.length > 0) {
				Map<Integer, OptionalEntryLog> map = listOptLog(id);
				logger.info("OptionalRoute Undo Records: " + map.size());
				String editOn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				OptionalRouteEntry routingDTO = null;
				for (int i = 0; i < id.length; i++) {
					if (map.containsKey(id[i])) {
						OptionalEntryLog logEntry = map.get(id[i]);
						routingDTO = new OptionalRouteEntry(logEntry.getRouteId(), logEntry.getNumSmscId(),
								logEntry.getBackupSmscId(), logEntry.getForceSenderNum(),
								logEntry.getForceSenderAlpha(), logEntry.getExpiredOn(), logEntry.getSmsLength(),
								logEntry.isRefund(), logEntry.getRegSender(), logEntry.getRegSmscId(),
								logEntry.getCodeLength(), logEntry.isReplaceContent(), logEntry.getReplacement(),
								logEntry.getMsgAppender(), logEntry.getSourceAppender(), masterid, editOn);
						routingDTO.setSenderReplFrom(logEntry.getSenderReplFrom());
						routingDTO.setSenderReplTo(logEntry.getSenderReplTo());
						routingDTO.setRegGroupId(logEntry.getRegGroupId());
						list.add(routingDTO);
						refreshUsers.add(userId[i]);
					}
				}
				if (!list.isEmpty()) {
					logger.info("Optional Route Update Size: " + list.size());
					try {
						updateOptionalRouteEntries(list);
					} catch (Exception e) {
						logger.error("Error: "+e.getLocalizedMessage());
						throw new InternalServerException("Error: "+e.getLocalizedMessage());
					}
					List<RouteEntryExt> routinglist = null;
					try {
						routinglist = getUpdateOptionalRoutingList(optRouteEntry.getCriterionEntries());
					} catch (SQLException e) {
						logger.error("Error: "+e.getLocalizedMessage());
						throw new DataAccessError("SQL Error: "+e.getLocalizedMessage());
					} catch (Exception e) {
						logger.error("Error: "+e.getLocalizedMessage());
						throw new InternalServerException("Error: "+e.getLocalizedMessage());
					}
					if (!routinglist.isEmpty()) {
						Map<Integer, String> smsclist = listNames();
						smsclist.put(0, "Down");
						responce.setRoutinglist(routinglist);
						responce.setGroupDetail(smsclist);
						target = "view";
					} else {
						target = IConstants.SUCCESS_KEY;
						logger.info("Routing Configured Successfully!");
					}
				} else {
					logger.error("error: List is empty");
					throw new NotFoundException("Record not found! List is empty");
				}
			} else {
				logger.error("error: record unavailable");
				throw new NotFoundException("Record Unavailable!");
			}
		} catch (NotFoundException ex) {
			logger.error("NotFound Exception: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.toString());
			throw new NotFoundException("NotFound Exception: "+ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.toString());
			throw new InternalServerException("Unexpected Exception: "+ex.getLocalizedMessage());
		}
		
		if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY) || target.equalsIgnoreCase("view")) {
			for (int user : refreshUsers) {
				MultiUtility.refreshRouting(user);
			}
			MultiUtility.changeFlag(Constants.CLIENT_FLAG_FILE, "707");
		}
		responce.setStatus(target);
		return responce;
	}

	@Override
	public OptionRouteResponse UpdateOptionalRoutePrevious(OptEntryArrForm routingForm, String username) {

		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		OptionRouteResponse response = new OptionRouteResponse();
		String target = IConstants.FAILURE_KEY;

		String masterid = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			masterid = userOptional.get().getMasterId();
		}

		logger.info("Optional Route Log Requested By " + masterid);
		// List<RouteEntry> list = new ArrayList<RouteEntry>();
		int[] id = routingForm.getRouteId();
		// String editOn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new
		// Date());
		try {
			if (id != null && id.length > 0) {

				List<Long> ids = Arrays.stream(id).asLongStream().boxed().collect(Collectors.toList());

				// Making a request to the repository method
				List<RouteEntryExt> list = null;
				try {
					list = routeEntryRepository.getOptRoutingLog(ids);
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage());
					throw new InternalServerException(e.getLocalizedMessage());
				}
				if (!list.isEmpty()) {
					response.setRoutinglist(list);
					target = "previous";
				} else {
					logger.error("error: record unavailable");
					throw new NotFoundException("Record Not Found!");
				}
			} else {
				logger.error("error: record unavailable");
				throw new NotFoundException("Record Unavailble. Id Unavailable.");
			}

		} catch (NotFoundException ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.toString());
			throw new NotFoundException("NotFoundException: "+ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.toString());
			throw new InternalServerException("Unexpected Exception: "+ex.getLocalizedMessage());
		}
		response.setStatus(target);
		return response;
	}

	@Override
	public OptionRouteResponse UpdateOptionalRouteBasic(OptEntryArrForm routingForm, String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String target = IConstants.FAILURE_KEY;
		OptionRouteResponse optionRouteResponse = new OptionRouteResponse();
		int[] id = routingForm.getRouteId();
		try {

			if (id != null && id.length > 0) {
				List<RouteEntryExt> list = routeEntryRepository.findAllByCustomQuery(routingForm.getCriterionEntries());
				if (!list.isEmpty()) {
					Map<Integer, String> smscnames = listNames();
					Map<Integer, String> smsclist = new HashMap<Integer, String>();
					Map<Integer, Set<String>> group_mapping = getSmscGroupMapping();
					for (int smsc_id : smscnames.keySet()) {
						String smsc_name = smscnames.get(smsc_id);
						if (group_mapping.containsKey(smsc_id)) {
							smsclist.put(smsc_id, smsc_name + " " + group_mapping.get(smsc_id));
						} else {
							smsclist.put(smsc_id, smsc_name + " [NONE]");
						}
					}
					smsclist.put(0, "DOWN [NONE]");
					optionRouteResponse.setRoutinglist(list);
					optionRouteResponse.setSmsclist(smsclist);
					optionRouteResponse.setGroupDetail(listGroupNames());
					target = "basic";
				} else {
					logger.error("error: record unavailable");
					throw new NotFoundException("Record Not Found! List is empty");
				}
			} else {
				logger.error("error: record unavailable");
				throw new NotFoundException("Record not found! Id is null or zero!");
			}
		} catch (NotFoundException ex) {
			logger.error("NotFoundException: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			throw new NotFoundException("NotFoundException: " + ex.getMessage());
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			throw new InternalServerErrorException("Process Error: " + ex.getMessage());
		} 
		optionRouteResponse.setStatus(target);

		return optionRouteResponse;
	}

	@Override
	public OptionRouteResponse checkExisting(RouteEntryArrForm routeEntryArrForm, String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		String target = IConstants.FAILURE_KEY;
		OptionRouteResponse optionRouteResponse = null;
		try {
			RouteEntryArrForm routingForm = new RouteEntryArrForm();
			optionRouteResponse = new OptionRouteResponse();

			Map<Integer, String> groupDetail = new HashMap<>(listGroupNames());
			Collection<NetworkEntry> networks = null;
			if (routingForm.isCountryWise()) {
				logger.info("Countries: " + String.join(",", routingForm.getCriteria().getMcc()));
				if (routingForm.getCriteria().getMcc() != null && routingForm.getCriteria().getMcc().length > 0) {
					Predicate p = new PredicateBuilderImpl().getEntryObject().get("mcc")
							.in(routingForm.getCriteria().getMcc());
					networks = GlobalVars.NetworkEntries.values(p);
				} else {
					networks = GlobalVars.NetworkEntries.values();
				}
			} else {
				Predicate p = new PredicateBuilderImpl().getEntryObject().get("id")
						.in(Arrays.stream(routingForm.getNetworkId()).boxed().toArray(Integer[]::new));
				networks = GlobalVars.NetworkEntries.values(p);
			}

			logger.info("Total: " + networks.size());
			Set<Integer> user_id_list = new HashSet<>();
			if (routingForm.getCriteria().getAccountType() != null) {
				EntryObject entryObject = new PredicateBuilderImpl().getEntryObject();
				Predicate p = new PredicateBuilderImpl().getEntryObject().get("id")
						.in(Arrays.stream(routingForm.getNetworkId()).boxed().toArray(Integer[]::new));
				networks = GlobalVars.NetworkEntries.values(p);
				user_id_list.addAll(GlobalVars.WebmasterEntries.keySet(p));
			} else {
				user_id_list.addAll(Arrays.stream(routingForm.getUserId()).boxed().collect(Collectors.toSet()));
			}
			Map<Integer, String> smscnames = listNames();
			int smscId = 0;
			if (routingForm.getSmscId() != null) {
				smscId = routingForm.getSmscId()[0];
			}
			int groupId = routingForm.getGroupId()[0];
			double cost = routingForm.getCost()[0];
			String smscType = routingForm.getSmscType()[0];
			String remarks = routingForm.getRemarks()[0];
			String smsc = smscnames.get(smscId);
			String group = groupDetail.get(groupId);
			Set<Integer> network_id_list = new HashSet<>();
			for (NetworkEntry networkEntry : networks) {
				logger.info("Adding Network: " + networkEntry.getId());
				network_id_list.add(networkEntry.getId());
			}
			Map<Integer, Double> smsc_pricing = getSmscPricing(smsc,
					network_id_list.stream().map(String::valueOf).collect(Collectors.toSet()));
			EntryObject entryObj = new PredicateBuilderImpl().getEntryObject();
			Predicate<Integer, RouteEntry> p = entryObj.get("userId")
					.in(user_id_list.toArray(new Integer[user_id_list.size()]))
					.and(entryObj.get("networkId").in(network_id_list.toArray(new Integer[network_id_list.size()])));
			Map<Integer, Set<Integer>> existUserRoutes = new HashMap<>();
			for (RouteEntry entry : GlobalVars.BasicRouteEntries.values(p)) {
				Set<Integer> set = existUserRoutes.computeIfAbsent(entry.getUserId(), k -> new HashSet<>());
				set.add(entry.getNetworkId());
				existUserRoutes.put(entry.getUserId(), set);
			}
			Map<Integer, Set<String>> group_mapping = getSmscGroupMapping();
			if (smscId > 0) {
				if (group_mapping.containsKey(smscId)) {
					smsc += " " + group_mapping.get(smscId);
				} else {
					smsc += " [NONE]";
				}
			} else {
				smsc += " [NONE]";
			}
			Map<Integer, String> smsclist = new HashMap<>();
			for (int smsc_id : smscnames.keySet()) {
				String smsc_name = smscnames.get(smsc_id);
				if (group_mapping.containsKey(smsc_id)) {
					smsclist.put(smsc_id, smsc_name + " " + group_mapping.get(smsc_id));
				} else {
					smsclist.put(smsc_id, smsc_name + " [NONE]");
				}
			}
			smsclist.put(0, "DOWN [NONE]");
			List<RouteEntryExt> routelist = new ArrayList<>();
			int auto_incr_id = 0;
			for (int user : user_id_list) {
				String systemId = GlobalVars.UserEntries.get(user).getSystemId();
				String accountType = GlobalVars.WebmasterEntries.get(user).getAccountType();
				Set<Integer> existNetworks = existUserRoutes.get(user);
				logger.info(systemId + " Configured Networks: " + existNetworks);
				for (NetworkEntry networkEntry : networks) {
					int networkId = networkEntry.getId();
					if (existNetworks != null) {
						if (!existNetworks.contains(networkId)) {
							RouteEntry entry = new RouteEntry(user, networkId, smscId, groupId, cost, smscType, null, null,
									remarks);
							entry.setId(++auto_incr_id);
							RouteEntryExt ext = new RouteEntryExt(entry);
							ext.setSystemId(systemId);
							ext.setCountry(networkEntry.getCountry());
							ext.setOperator(networkEntry.getOperator());
							ext.setMcc(networkEntry.getMcc());
							ext.setMnc(networkEntry.getMnc());
							ext.setSmsc(smsc);
							ext.setGroup(group);
							ext.setAccountType(accountType);
							if (smsc_pricing.containsKey(networkId)) {
								ext.setSmscCost(smsc_pricing.get(networkId));
							}
							routelist.add(ext);
						} else {
							logger.info(systemId + " Already Has Network: " + networkId);
						}
					} else {
						RouteEntry entry = new RouteEntry(user, networkId, smscId, groupId, cost, smscType, null, null,
								remarks);
						entry.setId(++auto_incr_id);
						RouteEntryExt ext = new RouteEntryExt(entry);
						ext.setSystemId(systemId);
						ext.setCountry(networkEntry.getCountry());
						ext.setOperator(networkEntry.getOperator());
						ext.setMcc(networkEntry.getMcc());
						ext.setMnc(networkEntry.getMnc());
						ext.setSmsc(smsc);
						ext.setGroup(group);
						ext.setAccountType(accountType);
						if (smsc_pricing.containsKey(networkId)) {
							ext.setSmscCost(smsc_pricing.get(networkId));
						}
						routelist.add(ext);
					}
				}

				optionRouteResponse.setRoutinglist(routelist);
				optionRouteResponse.setSmsclist(smsclist);
				optionRouteResponse.setGroupDetail(listGroupNames());
				target = IConstants.SUCCESS_KEY;

			}
			optionRouteResponse.setStatus(target);
		} catch (Exception e) {
			logger.error("Exception: "+e.toString());
			throw new InternalServerException("Exception: "+e.getLocalizedMessage());
		}

		return optionRouteResponse;

	}

	public Map<Integer, Double> getSmscPricing(String smsc, Set<String> networks) {
		Map<Integer, Double> smscPricing = new HashMap<>();
		List<Object[]> results = optionalRouteEntryRepository.findSmscPricing(smsc, networks);
		for (Object[] result : results) {
			Integer networkId = (Integer) result[0];
			Double newCost = (Double) result[1];
			smscPricing.put(networkId, newCost);
		}

		return smscPricing;
	}

/////////////CopyRouting
	@Override
	public String execute(String username) {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		User user = null;

		UserSessionObject userSessionObject = new UserSessionObject();

		NetworkEntry listExistNetwork = null;
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		String target = IConstants.FAILURE_KEY;
		boolean proceed = true;
		RouteEntryForm routingForm = new RouteEntryForm();
		boolean includeHlr, includeOpt;
		boolean replaceExist = routingForm.isReplaceExisting();
		int from = routingForm.getUserId();
		int[] to_user = routingForm.getSubUserId();
		String margin_str = routingForm.getMargin();
		String systemId = user.getSystemId();

		try {
			if (!Access.isAuthorizedAdminAndUser(user.getRoles())) {
				Map<Integer, String> users = listUsersUnderMaster(systemId);
				Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject().get("secondaryMaster")
						.equal(systemId);
				for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(p)) {
					UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
					users.put(userEntry.getId(), userEntry.getSystemId());
				}
				if (from != user.getUserId()) {
					if (!users.containsKey(from)) {

						proceed = false;
					}
				}
				if (proceed) {
					for (int i = 0; i < to_user.length; i++) {
						if (!users.containsKey(to_user[i])) {
							logger.info(systemId + "[" + userSessionObject.getRole() + "] Invalid To User [" + to_user[i]
									+ "]");
							proceed = false;
							break;
						}
					}
				}
			}
			if (proceed) {
				boolean isPercent = false;
				double margin = 0;
				if (margin_str.contains("%")) {
					isPercent = true;
					margin_str = margin_str.substring(0, margin_str.indexOf("%")).trim();
				}
				try {
					margin = Double.parseDouble(margin_str);
				} catch (Exception ex) {
					logger.error(from + "Invalid margin: " + margin_str);
					throw new InternalServerException("Exception: "+ex.getLocalizedMessage());
				}
				if (Access.isAuthorizedAdminAndUser(user.getRoles())) {
					includeHlr = true;
					includeOpt = true;
					if (userSessionObject.getBalance().getWalletFlag().equalsIgnoreCase("yes")) {
						if (margin > 0) {
							logger.error(systemId + " valid Margin: " + margin);
						} else {
							logger.error(systemId + " Invalid Margin: " + margin);
							proceed = false;
						}
					}
				} else {
					includeHlr = routingForm.isHlr();
					includeOpt = routingForm.isOptional();
				}
				if (proceed) {
					logger.info(from + " Checking For Routing Entries");
					Map<Integer, RouteEntryExt> map = listRouteEntries(from, includeHlr, includeOpt, false);
					logger.info(from + " Routing Entries Found: " + map.size());
					String country = routingForm.getMcc();
					Set<Integer> networks = new HashSet<Integer>();
					logger.info("MCC: " + country);
					logger.info("MNC: " + Arrays.toString(routingForm.getMnc()));
					if (!country.equalsIgnoreCase("%")) {
						Set<String> operators = new HashSet<String>();
						for (String mnc : routingForm.getMnc()) {
							if (mnc.equalsIgnoreCase("%")) {
								break;
							} else {
								operators.add(mnc);
							}
						}
						Predicate<Integer, NetworkEntry> p = new PredicateBuilderImpl().getEntryObject().get("mcc")
								.equal(country);
						for (NetworkEntry network : GlobalVars.NetworkEntries.values(p)) {
							if (!operators.isEmpty()) {
								if (operators.contains(network.getMnc())) {
									networks.add(network.getId());
								}
							} else {
								networks.add(network.getId());
							}
						}
					}
					logger.info("networks: " + networks);
					// database.IDatabaseService dbService = com.hti.webems.database.HtiSmsDB
					for (int i = 0; i < to_user.length; i++) {
						logger.info("Processing For " + to_user[i]);
						String editOn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
						List<RouteEntryExt> routinglist = new ArrayList<RouteEntryExt>();
						Set<Integer> exist_networks = null, to_be_replaced = null;
						if (!replaceExist) {
							// exist_networks = NetworkEntryRepository.listExistNetwork(to_user[i]);
							to_be_replaced = new HashSet<Integer>();
						}
						double cost = 0;
						for (RouteEntryExt toRouteExt : map.values()) {
							RouteEntry toRoute = toRouteExt.getBasic();
							if (!networks.isEmpty()) {
								if (!networks.contains(toRoute.getNetworkId())) {
									logger.info("Skipping Copy Routing For: " + toRoute.getNetworkId());
									continue;
								}
							}
							if (!replaceExist) {
								if (exist_networks.contains(toRoute.getNetworkId())) {
									logger.info("Skipping Copy Routing For Exist: " + toRoute.getNetworkId());
									continue;
								} else {
									to_be_replaced.add(toRoute.getNetworkId());
								}
							}
							RouteEntry route = new RouteEntry(toRoute.getUserId(), toRoute.getNetworkId(),
									toRoute.getSmscId(), toRoute.getGroupId(), toRoute.getCost(), toRoute.getSmscType(),
									systemId, editOn, null);
							cost = route.getCost();
							if (isPercent) {
								cost = cost + ((cost * margin) / 100);
							} else {
								cost = cost + margin;
							}
							route.setId(0);
							route.setCost(Double.valueOf(new DecimalFormat("#.#####").format(cost)));
							route.setUserId(to_user[i]);
							RouteEntryExt ext = new RouteEntryExt(route);
							if (!includeHlr) {
								ext.setHlrRouteEntry(new HlrRouteEntry(systemId, editOn));
							} else {
								if (toRouteExt.getHlrRouteEntry() != null) {
									HlrRouteEntry toHlr = toRouteExt.getHlrRouteEntry();
									ext.setHlrRouteEntry(new HlrRouteEntry(0, toHlr.isHlr(), toHlr.getSmsc(),
											toHlr.getHlrCache(), toHlr.getCost(), systemId, editOn, toHlr.isMnp()));
								} else {
									ext.setHlrRouteEntry(new HlrRouteEntry(systemId, editOn));
								}
							}
							if (!includeOpt) {
								ext.setRouteOptEntry(new OptionalRouteEntry(systemId, editOn));
							} else {
								if (toRouteExt.getRouteOptEntry() != null) {
									OptionalRouteEntry toOptRouteEntry = toRouteExt.getRouteOptEntry();
									String msgAppender = toOptRouteEntry.getMsgAppender();
									String msgAppenderConverted = null;
									if (msgAppender != null && msgAppender.length() > 0) {
										// logger.info("msgAppender: " + msgAppender);
										if (msgAppender.contains("^") || msgAppender.contains("$")) {
											// convert to hex
											msgAppenderConverted = (msgAppender);
										} else {
											msgAppenderConverted = null;
										}
									} else {
										msgAppenderConverted = null;
									}
									ext.setRouteOptEntry(new OptionalRouteEntry(0, toOptRouteEntry.getNumSmscId(),
											toOptRouteEntry.getBackupSmscId(), toOptRouteEntry.getForceSenderNum(),
											toOptRouteEntry.getForceSenderAlpha(), toOptRouteEntry.getExpiredOn(),
											toOptRouteEntry.getSmsLength(), toOptRouteEntry.isRefund(),
											toOptRouteEntry.getRegSender(), toOptRouteEntry.getRegSmscId(),
											toOptRouteEntry.getCodeLength(), toOptRouteEntry.isReplaceContent(),
											toOptRouteEntry.getReplacement(), msgAppenderConverted,
											toOptRouteEntry.getSourceAppender(), systemId, editOn));
									ext.getRouteOptEntry().setRegGroupId(toOptRouteEntry.getRegGroupId());
								} else {
									ext.setRouteOptEntry(new OptionalRouteEntry(systemId, editOn));
								}
							}
							routinglist.add(ext);
						}
						logger.info(to_user[i] + " To be Copied Routing Entries: " + routinglist.size());
						if (replaceExist) {
							if (networks.isEmpty()) {
								deleteRouteEntries(to_user[i]);
							} else {
								deleteRouteEntries(to_user[i], networks);
							}
						} else {
							// skip existing networks
							deleteRouteEntries(to_user[i], to_be_replaced);
						}
						logger.info(to_user[i] + " All Routing Entries Deleted");
						saveEntries(routinglist);
						logger.info(to_user[i] + " All Routing Copied");
						MultiUtility.refreshRouting(to_user[i]);
					}
					MultiUtility.changeFlag(Constants.CLIENT_FLAG_FILE, "707");
					target = IConstants.SUCCESS_KEY;
				}
			} else {
				target = "invalidRequest";
				throw new UnauthorizedException("Unauthorized User");
			}
		} catch (UnauthorizedException e) {
			logger.error(e.toString());
			throw new UnauthorizedException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}

		return target;

	}

	public Map<Integer, String> listUsersUnderMaster(String systemId) {
		Map<Integer, String> sortedMap = GlobalVars.UserEntries.values().stream()
				.filter(entry -> entry.getMasterId().equalsIgnoreCase(systemId))
				.collect(Collectors.toMap(UserEntry::getId, UserEntry::getSystemId, (e1, e2) -> e1, // Merge function in
																									// case of duplicate
																									// keys
						LinkedHashMap::new));

		// Sorting by value (case-insensitive)
		sortedMap = sortedMap.entrySet().stream().sorted(Map.Entry.comparingByValue(String.CASE_INSENSITIVE_ORDER))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		return sortedMap;
	}

	@Override
	public String downloadRoute(String username, RouteEntryArrForm routingForm, HttpServletResponse response) {

		Optional<User> userOptional = userRepo.findBySystemId(username);
		User user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		Optional<UserEntry> userEntityOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userEntityOptional.isPresent()) {
			userEntry = userEntityOptional.get();
		}else {
			throw new NotFoundException("UserEntry not found");
		}
		String target = IConstants.FAILURE_KEY;
		String masterid = userEntry.getMasterId();
		logger.info("Download Routing Requested By " + masterid + " [" + user.getRoles() + "]");
		try {
			if (!Access.isAuthorizedUser(user.getRoles())) {
				boolean proceed = true;
				// RouteDAService routeService = new RouteDAServiceImpl();
				List<RouteEntryExt> routinglist = new ArrayList<RouteEntryExt>();
				int[] useridarr = routingForm.getUserId();
				if (useridarr != null && useridarr.length > 0) {
					if (Access.isAuthorizedAdmin(user.getRoles())) {
						Map<Integer, String> users = listUsersUnderMaster(masterid);
						Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
								.get("secondaryMaster").equal(masterid);
						for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(p)) {
							UserEntry userEntry1 = GlobalVars.UserEntries.get(webEntry.getUserId());
							users.put(userEntry1.getId(), userEntry1.getSystemId());
						}
						for (int user_id : useridarr) {
							if (!users.containsKey(user_id)) {
								logger.info(masterid + "[" + user.getRoles() + "] Invalid User[" + user_id
										+ "] Download Routing Request");
								proceed = false;
								break;
							}
						}
					}
					if (proceed) {
						for (int user1 : useridarr) {
							logger.info("Listing Routing For " + user1);
							Map<Integer, RouteEntryExt> routingmap = listRouteEntries(user1, false, false, true);
							routinglist.addAll(routingmap.values());
						}
						logger.info(masterid + " Download Routinglist Size: " + routinglist.size());
						if (!routinglist.isEmpty()) {
							Workbook workbook = null;
							try {
								workbook = getWorkBook(routinglist);
							} catch (Exception e) {
								logger.error(e.getLocalizedMessage());
								throw new WorkBookException("WorkBook Exception: "+e.getLocalizedMessage());
							}
							String filename = "Routing_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date())
									+ ".xlsx";
							response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\";");
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							logger.info(masterid + " Creating Routing XLSx ");
							workbook.write(bos);
							InputStream is = null;
							OutputStream out = null;
							try {
								is = new ByteArrayInputStream(bos.toByteArray());
								// byte[] buffer = new byte[8789];
								int curByte = -1;
								out = response.getOutputStream();
								logger.info(masterid + " Starting Routing xlsx Download ");
								while ((curByte = is.read()) != -1) {
									out.write(curByte);
								}
								out.flush();
							} catch (IOException ex) {
								logger.error(masterid + " Routing XLSx Download Error: " + ex.toString());
								throw new InternalServerException("IO Exception: "+ex.getLocalizedMessage());
							} catch (Exception ex) {
								logger.error(masterid + " Routing XLSx Download Error: " + ex.toString());
								throw new InternalServerException("Exception: "+ex.getLocalizedMessage());
							} finally {
								try {
									if (is != null) {
										is.close();
									}
									if (out != null) {
										out.close();
									}
								} catch (IOException ex) {
									logger.error("IOException: "+ex.getLocalizedMessage());
									throw new InternalServerException("IO Exception: "+ex.getLocalizedMessage());
								} catch (Exception ex) {
									logger.error("Exception: "+ex.getLocalizedMessage());
									throw new InternalServerException("Exception: "+ex.getLocalizedMessage());
								}
							}
							target = IConstants.SUCCESS_KEY;
						} else {
							logger.error("error: record unavailable. Routing list empty.");
							throw new NotFoundException("Routing list empty!");
						}
					} else {
						target = "invalidRequest";
						throw new UnauthorizedException("Invalid Request!");
					}
				} else {
					logger.error("error: record unavailable");
					throw new NotFoundException("Id not found!");
				}
			} else {
				logger.error("Authorization Failed[" + user.getRoles() + "] :" + masterid);
				target = "invalidRequest";
				throw new UnauthorizedException("Unauthorized User!");
			}
		} catch (NotFoundException ex) {
			logger.error("NotFound Exception: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.toString());
			throw new NotFoundException("NotFound Exception: "+ex.getLocalizedMessage());
			
		} catch (UnauthorizedException ex) {
			logger.error("Unauthorized Exception: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.toString());
			throw new UnauthorizedException("Unauthorized Exception: "+ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.toString());
			throw new InternalServerException("Unexpected Exception: "+ex.getLocalizedMessage());
		}
		return target;

	}

	private Workbook getWorkBook(List<RouteEntryExt> routinglist) {
		logger.info("Start Creating WorkBook.");
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		int records_per_sheet = 500000;
		int sheet_number = 0;
		Sheet sheet = null;
		Row row = null;
		XSSFFont headerFont = (XSSFFont) workbook.createFont();
		headerFont.setFontName("Arial");
		headerFont.setFontHeightInPoints((short) 10);
		headerFont.setColor(new XSSFColor(Color.WHITE));
		XSSFCellStyle headerStyle = (XSSFCellStyle) workbook.createCellStyle();
		headerStyle.setFont(headerFont);
		headerStyle.setFillForegroundColor(new XSSFColor(Color.GRAY));
		headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		headerStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
		headerStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderBottom((short) 1);
		headerStyle.setBottomBorderColor(new XSSFColor(Color.WHITE));
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderTop((short) 1);
		headerStyle.setTopBorderColor(new XSSFColor(Color.WHITE));
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderLeft((short) 1);
		headerStyle.setLeftBorderColor(new XSSFColor(Color.WHITE));
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setBorderRight((short) 1);
		headerStyle.setRightBorderColor(new XSSFColor(Color.WHITE));
		XSSFFont rowFont = (XSSFFont) workbook.createFont();
		rowFont.setFontName("Arial");
		rowFont.setFontHeightInPoints((short) 9);
		rowFont.setColor(new XSSFColor(Color.BLACK));
		XSSFCellStyle rowStyle = (XSSFCellStyle) workbook.createCellStyle();
		rowStyle.setFont(rowFont);
		rowStyle.setFillForegroundColor(new XSSFColor(Color.LIGHT_GRAY));
		rowStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		rowStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
		rowStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
		rowStyle.setBorderBottom(BorderStyle.THIN);
		rowStyle.setBorderBottom((short) 1);
		rowStyle.setBottomBorderColor(new XSSFColor(Color.WHITE));
		rowStyle.setBorderTop(BorderStyle.THIN);
		rowStyle.setBorderTop((short) 1);
		rowStyle.setTopBorderColor(new XSSFColor(Color.WHITE));
		rowStyle.setBorderLeft(BorderStyle.THIN);
		rowStyle.setBorderLeft((short) 1);
		rowStyle.setLeftBorderColor(new XSSFColor(Color.WHITE));
		rowStyle.setBorderRight(BorderStyle.THIN);
		rowStyle.setBorderRight((short) 1);
		rowStyle.setRightBorderColor(new XSSFColor(Color.WHITE));
		String[] headers = { "SystemId", "Country", "Operator", "Mcc", "Mnc", "Cost" };
		while (!routinglist.isEmpty()) {
			int row_number = 0;
			sheet = workbook.createSheet("Sheet(" + sheet_number + ")");
			sheet.setDefaultColumnWidth(14);
			logger.info("Creating Sheet: " + sheet_number);
			while (!routinglist.isEmpty()) {
				row = sheet.createRow(row_number);
				if (row_number == 0) {
					int cell_number = 0;
					for (String header : headers) {
						Cell cell = row.createCell(cell_number);
						cell.setCellValue(header);
						cell.setCellStyle(headerStyle);
						cell_number++;
					}
				} else {
					RouteEntryExt routeEntry = routinglist.remove(0);
					logger.debug("Add Row[" + row_number + "]: " + routeEntry.getSystemId() + " -> " + routeEntry);
					Cell cell = row.createCell(0);
					cell.setCellValue(routeEntry.getSystemId());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(1);
					cell.setCellValue(routeEntry.getCountry());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(2);
					cell.setCellValue(routeEntry.getOperator());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(3);
					cell.setCellValue(routeEntry.getMcc());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(4);
					cell.setCellValue(routeEntry.getMnc());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(5);
					cell.setCellValue(routeEntry.getBasic().getCost());
					cell.setCellStyle(rowStyle);
				}
				if (++row_number > records_per_sheet) {
					logger.info("Routing Sheet Created: " + sheet_number);
					break;
				}
			}
			sheet_number++;
		}
		logger.info("Routing Workbook Created");
		return workbook;
	}

	@Override
	public RouteUserResponse RouteUserList(String username, String purpose) {
		RouteUserResponse routeUserResponse = new RouteUserResponse();
		Optional<User> userOptional = userRepo.findBySystemId(username);
		User user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		SalesEntry salesEntry = null;
		try {
			salesEntry = salesRepository.findByMasterId(user.getSystemId());
		} catch (Exception e1) {
			logger.error("NotFoundException: "+e1.toString());
			throw new NotFoundException("Unable to found SalesEntry. Exception: "+e1.getLocalizedMessage());
		}
		String target = null;
		try {
			// String usernames="";
			String masterId = user.getSystemId();
			logger.info("Routing User list Request For " + purpose + " By " + masterId);
			if (Access.isAuthorizedUser(user.getRoles())) {
				logger.info("Authorized User :" + masterId);
				if (Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
					// List<UserEntry> usernames = new ArrayList<UserEntry>();
					if (purpose.equalsIgnoreCase("add") || purpose.equalsIgnoreCase("search")) {
						Map<String, String> networkmap = getDistinctCountry();
						Map<Integer, String> operatormap = new HashMap<Integer, String>();
						routeUserResponse.setNetworkmap(networkmap);
						for (NetworkEntry entry : GlobalVars.NetworkEntries.values()) {
							operatormap.put(entry.getId(),
									entry.getCountry() + "-" + entry.getOperator() + " [" + entry.getMnc() + "]");
						}
						routeUserResponse.setOperatormap(operatormap);
						Map<Integer, String> smscnames = listNames();
						Map<Integer, String> smsclist = new HashMap<Integer, String>();
						Map<Integer, Set<String>> group_mapping = getSmscGroupMapping();
						for (int smsc_id : smscnames.keySet()) {
							String smsc_name = smscnames.get(smsc_id);
							if (group_mapping.containsKey(smsc_id)) {
								smsclist.put(smsc_id, smsc_name + " " + group_mapping.get(smsc_id));
							} else {
								smsclist.put(smsc_id, smsc_name + " [NONE]");
							}
						}
						smsclist.put(0, "DOWN [NONE]");
						routeUserResponse.setSmsclist(smsclist);
						routeUserResponse.getListGroupNames();
						routeUserResponse.setTreeSet(new TreeSet<String>(GlobalVars.currencies.keySet()));
						if (purpose.equalsIgnoreCase("add")) {
							routeUserResponse.setSmscTypes(GlobalVars.smscTypes);
						} else {
							Set<String> set = distinctSmscTypes();
							routeUserResponse.setSmscTypes(set);
						}
					}
					Collection<UserEntry> users = null;
					if (Access.isAuthorizedSystem(user.getRoles())) {
						EntryObject e = new PredicateBuilderImpl().getEntryObject();
						Predicate p = e.get("role").in("admin", "user").or(e.get("id").equal(user.getUserId()));
						users = GlobalVars.UserEntries.values(p);
					} else {
						users = GlobalVars.UserEntries.values();
					}
					if (purpose.equalsIgnoreCase("download")) {
						List<UserEntryExt> usernames = new ArrayList<UserEntryExt>();
						Map<Integer, String> sales = listNames();
						for (UserEntry userEntry : users) {
							if (expired(userEntry)) {
								continue;
							}
							UserEntryExt entry = new UserEntryExt(userEntry);
							WebMasterEntry webEntry = GlobalVars.WebmasterEntries.get(userEntry.getId());
							if (webEntry.getExecutiveId() > 0) {
								if (sales.containsKey(webEntry.getExecutiveId())) {
									webEntry.setExecutiveName(sales.get(webEntry.getExecutiveId()));
								}
							}
							usernames.add(entry);
						}
						routeUserResponse.setUsernames(usernames);
					} else {
						routeUserResponse.setFilter(filter(users));
					}
				} else if (Access.isAuthorizedAdmin(user.getRoles())) {
					EntryObject e = new PredicateBuilderImpl().getEntryObject();
					if (purpose.equalsIgnoreCase("download")) {
						Predicate<Integer, UserEntry> p = e.get("masterId").equal(user.getSystemId())
								.or(e.get("id").equal(user.getUserId()));
						List<UserEntryExt> usernames = new ArrayList<UserEntryExt>();
						Map<Integer, String> sales = listNames();
						for (UserEntry userEntry : GlobalVars.UserEntries.values(p)) {
							if (expired(userEntry)) {
								continue;
							}
							UserEntryExt entry = new UserEntryExt(userEntry);
							WebMasterEntry webEntry = GlobalVars.WebmasterEntries.get(userEntry.getId());
							if (webEntry.getExecutiveId() > 0) {
								if (sales.containsKey(webEntry.getExecutiveId())) {
									entry.getWebMasterEntry().setExecutiveName(sales.get(webEntry.getExecutiveId()));
								}
							}
							usernames.add(entry);
						}
						Predicate<Integer, WebMasterEntry> pw = new PredicateBuilderImpl().getEntryObject()
								.get("secondaryMaster").equal(user.getSystemId());
						for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(pw)) {
							UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
							if (expired(userEntry)) {
								continue;
							}
							UserEntryExt entry = new UserEntryExt(userEntry);
							if (webEntry.getExecutiveId() > 0) {
								if (sales.containsKey(webEntry.getExecutiveId())) {
									entry.getWebMasterEntry().setExecutiveName(sales.get(webEntry.getExecutiveId()));
								}
							}
							usernames.add(entry);
						}
						routeUserResponse.setUsernames(usernames);
					} else if (purpose.equalsIgnoreCase("copy")) {
						Predicate p = e.get("masterId").equal(user.getSystemId())
								.or(e.get("id").equal(user.getUserId()));
						List<UserEntry> list = filter(GlobalVars.UserEntries.values(p));
						Predicate<Integer, WebMasterEntry> pw = new PredicateBuilderImpl().getEntryObject()
								.get("secondaryMaster").equal(user.getSystemId());
						for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(pw)) {
							UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
							if (expired(userEntry)) {
								continue;
							}
							list.add(userEntry);
						}
						routeUserResponse.setFilter(list);
					} else {
						Predicate p = e.get("masterId").equal(user.getSystemId());
						List<UserEntry> list = filter(GlobalVars.UserEntries.values(p));
						Predicate<Integer, WebMasterEntry> pw = new PredicateBuilderImpl().getEntryObject()
								.get("secondaryMaster").equal(user.getSystemId());
						for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(pw)) {
							UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
							if (expired(userEntry)) {
								continue;
							}
							list.add(userEntry);
						}
						if (purpose.equalsIgnoreCase("search")) {
							Map<String, String> networkmap = getDistinctCountry();
							routeUserResponse.setNetworkmap(networkmap);
						}
						routeUserResponse.setFilter(list);
					}

				} else if (salesEntry.getRole().equalsIgnoreCase("seller")) {
					List<UserEntryExt> usernames = new ArrayList<UserEntryExt>();
					Map<Integer, UserEntryExt> userEntries = listUserEntryUnderSeller(user.getUserId().intValue(),
							user);
					for (UserEntryExt userEntry : userEntries.values()) {
						if (expired(userEntry.getUserEntry())) {
							continue;
						}
						userEntry.getWebMasterEntry().setExecutiveName(user.getSystemId());
						usernames.add(userEntry);
					}
					routeUserResponse.setUsernames(usernames);
				} else if (salesEntry.getRole().equalsIgnoreCase("manager")) {
					Map<Integer, UserEntryExt> userEntries = listUserEntryUnderManager(user.getSystemId());
					routeUserResponse.setUserEntries(userEntries);
				}
				if (purpose.equalsIgnoreCase("add")) {
					target = "add";
				} else if (purpose.equalsIgnoreCase("copy")) {
					target = "copy";
					Map<String, String> networkmap = getDistinctCountry();
					routeUserResponse.setNetworkmap(networkmap);
				} else if (purpose.equalsIgnoreCase("search")) {
					target = "search";
				} else if (purpose.equalsIgnoreCase("download")) {
					target = "download";
				}
				// usernames.sort(Comparator.comparing(UserEntry::getSystemId,
				// String.CASE_INSENSITIVE_ORDER));
			} else {
				logger.error("Authorization Failed :" + masterId);
				target = "invalidRequest";
				throw new UnauthorizedException("Authorization Failed :" + masterId);
			}
		} catch (UnauthorizedException ex) {
			logger.error(user.getSystemId(), ex.fillInStackTrace());
			target = IConstants.FAILURE_KEY;
			throw new UnauthorizedException("Unauthorized user: "+ex.getLocalizedMessage());
		} catch (Exception e) {
			logger.error("Unexpected Exception: "+e.toString());
			throw new InternalServerException("Unexpected Exception: "+e.getLocalizedMessage());
		}
		logger.info("Routing User list Request Target: " + target);
		routeUserResponse.setStatus(target);
		return routeUserResponse;
	}

	public Map<Integer, UserEntryExt> listUserEntryUnderManager(String mgrId) {
		Map<Integer, String> map = listNamesUnderManager(mgrId);
		Map<Integer, UserEntryExt> users = new HashMap<Integer, UserEntryExt>();
		try {
			for (int seller_id : map.keySet()) {
				Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject().get("executiveId")
						.equal(seller_id);
				for (WebMasterEntry entry : GlobalVars.WebmasterEntries.values(p)) {
					UserEntry userEntry = GlobalVars.UserEntries.get(entry.getUserId());
					UserEntryExt ext = new UserEntryExt(userEntry);
					entry.setExecutiveName(map.get(seller_id));
					ext.setWebMasterEntry(entry);
					users.put(userEntry.getId(), ext);
				}
			}
		} catch (Exception e) {
			logger.error("Exception: "+e.toString());
			throw new InternalServerException("Exception: "+e.getLocalizedMessage());
		}
		return users;
	}

	public Map<Integer, String> listNamesUnderManager(String mgrId) {
		Map<Integer, String> map = new HashMap<Integer, String>();
		try {
			List<SalesEntry> list = listSellersUnderManager(mgrId);
			for (SalesEntry entry : list) {
				map.put(entry.getId(), entry.getUsername());
			}
		} catch (Exception e) {
			logger.error("Exception: "+e.toString());
			throw new InternalServerException("Exception: "+e.getLocalizedMessage());
		}
		return map;
	}

	private List<SalesEntry> listSellersUnderManager(String mgrId) {
		return salesRepository.findByMasterIdAndRole(mgrId, "seller");
	}

	public Map<Integer, UserEntryExt> listUserEntryUnderSeller(int seller, User user) {
		logger.info("listing UserEntries Under Seller(" + seller + ")");
		Map<Integer, UserEntryExt> map = new HashMap<Integer, UserEntryExt>();
		try {
			for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values()) {
				if (webEntry.getExecutiveId() == seller) {
					UserEntryExt entry = getUserEntryExt(webEntry.getUserId());
					if (Access.isAuthorizedAdmin(user.getRoles())) {
						Map<Integer, UserEntryExt> under_users = listUserEntryUnderMaster(
								entry.getUserEntry().getSystemId());
						map.putAll(under_users);
					}
					map.put(webEntry.getUserId(), entry);
				}
			}
		} catch (Exception e) {
			logger.error("Exception: "+e.toString());
			throw new InternalServerException("Unexpected Exception: "+e.getLocalizedMessage());
		}
		logger.info("UserEntries Found Under Seller(" + seller + "): " + map.size());
		return map;
	}

	private Map<String, String> getDistinctCountry() {
		Map<String, String> countries = new HashMap<>();
		List<Object[]> resultList = null;
		try {
			resultList = networkEntryRepository.findDistinctCountries();
		} catch (Exception e) {
			logger.error("NotFound Exception: "+e.toString());
			throw new NotFoundException("Unable to find Distinct Countries. Exception: "+e.getLocalizedMessage());
		}

		for (Object[] result : resultList) {
			countries.put((String) result[0], (String) result[1]);
		}

		return countries;
	}

	private List<UserEntry> filter(Collection<UserEntry> users) {
		List<UserEntry> filter = new ArrayList<UserEntry>();
		for (UserEntry entry : users) {
			try {
				Date expiry_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(entry.getExpiry() + " 23:59:59");
				if (expiry_date.after(new Date())) {
					filter.add(entry);
				} else {
					logger.error(entry.getSystemId() + " Account Expired: " + entry.getExpiry());
					throw new InternalServerException(entry.getSystemId() + " Account Expired: " + entry.getExpiry());
				}
			} catch (ParseException e) {
				logger.error(entry.getSystemId(), "Expiry Parse Error: " + entry.getExpiry());
				throw new InternalServerException("Parse Exception: "+e.getLocalizedMessage());
			} catch (Exception e) {
				logger.error("Exception: "+e.toString());
				throw new InternalServerException("Unexpected Exception: "+e.getLocalizedMessage());
			}
		}
		return filter;
	}

	public UserEntryExt getUserEntryExt(int userid) {
		logger.debug("getUserEntry(" + userid + ")");
		UserEntryExt entry = null;
		try {
			if (GlobalVars.UserEntries.containsKey(userid)) {
				entry = new UserEntryExt(GlobalVars.UserEntries.get(userid));
				entry.setDlrSettingEntry(GlobalVars.DlrSettingEntries.get(userid));
				WebMasterEntry webEntry = GlobalVars.WebmasterEntries.get(userid);
				entry.setWebMasterEntry(webEntry);
				entry.setProfessionEntry(GlobalVars.ProfessionEntries.get(userid));
				logger.debug("end getUserEntry(" + userid + ")");
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("Exception: "+e.toString());
			throw new InternalServerException("Unexpected Exception: "+e.getLocalizedMessage());
		}
		return entry;
	}

	public Map<Integer, UserEntryExt> listUserEntryUnderMaster(String master) {
		logger.debug("listUserEntryUnderMaster(" + master + ")");
		Map<Integer, UserEntryExt> map = new LinkedHashMap<Integer, UserEntryExt>();
		try {
			for (UserEntry userEntry : GlobalVars.UserEntries.values()) {
				if (userEntry.getMasterId().equalsIgnoreCase(master)) {
					UserEntryExt entry = new UserEntryExt(userEntry);
					entry.setDlrSettingEntry(GlobalVars.DlrSettingEntries.get(userEntry.getId()));
					entry.setWebMasterEntry(GlobalVars.WebmasterEntries.get(userEntry.getId()));
					entry.setProfessionEntry(GlobalVars.ProfessionEntries.get(userEntry.getId()));
					map.put(userEntry.getId(), entry);
				}
			}
		} catch (Exception e) {
			logger.error("Excpetion: "+e.toString());
			throw new InternalServerException("Unexpected Exception: "+e.getLocalizedMessage());
		}
		return map;
	}

	@Override
	public OptionRouteResponse SearchRoutingBasic(String username, RouteEntryArrForm routingForm) {
		OptionRouteResponse optionRouteResponse = new OptionRouteResponse();
		Optional<User> userOptional = userRepo.findBySystemId(username);
		User user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String target = "basic";
		String masterid = user.getSystemId();
		logger.info(masterid + "[" + user.getRoles() + "] RouteEntries Search Request Using Advanced Criteria");
		try {
			if (Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				List<RouteEntryExt> routinglist = getRoutingList(routingForm);
				if (routinglist != null && !routinglist.isEmpty()) {
					Map<Integer, String> smscnames = listNames();
					Map<Integer, String> smsclist = new HashMap<Integer, String>();
					Map<Integer, Set<String>> group_mapping = getSmscGroupMapping();
					for (int smsc_id : smscnames.keySet()) {
						String smsc_name = smscnames.get(smsc_id);
						if (group_mapping.containsKey(smsc_id)) {
							smsclist.put(smsc_id, smsc_name + " " + group_mapping.get(smsc_id));
						} else {
							smsclist.put(smsc_id, smsc_name + " [NONE]");
						}
					}
					smsclist.put(0, "DOWN [NONE]");
					optionRouteResponse.setSmsclist(smsclist);
					optionRouteResponse.setRoutinglist(routinglist);
					optionRouteResponse.setGroupDetail(listGroupNames());
				} else {
					Set<Integer> user_id_list = new HashSet<Integer>();
					user_id_list.addAll(Arrays.stream(routingForm.getUserId()).boxed().collect(Collectors.toSet()));
					Collection<NetworkEntry> networks = null;
					if (routingForm.isCountryWise()) {
						System.out.println("Countries: " + String.join(",", routingForm.getCriteria().getMcc()));
						if (routingForm.getCriteria().getMcc() != null
								&& routingForm.getCriteria().getMcc().length > 0) {
							Predicate p = new PredicateBuilderImpl().getEntryObject().get("mcc")
									.in(routingForm.getCriteria().getMcc());
							networks = GlobalVars.NetworkEntries.values(p);
						} else {
							networks = GlobalVars.NetworkEntries.values();
						}
					} else {
						Predicate p = new PredicateBuilderImpl().getEntryObject().get("id")
								.in(ArrayUtils.toObject(routingForm.getNetworkId()));
						networks = GlobalVars.NetworkEntries.values(p);
					}
					System.out.println("Total: " + networks.size());
					Set<Integer> network_id_list = new HashSet<Integer>();
					for (NetworkEntry networkEntry : networks) {
						logger.info("Adding Network: " + networkEntry.getId());
						network_id_list.add(networkEntry.getId());
					}
					EntryObject entryObj = new PredicateBuilderImpl().getEntryObject();
					Predicate<Integer, RouteEntry> p = entryObj.get("userId")
							.in(user_id_list.toArray(new Integer[user_id_list.size()])).and(entryObj.get("networkId")
									.in(network_id_list.toArray(new Integer[network_id_list.size()])));
					Map<Integer, Set<Integer>> existUserRoutes = new HashMap<Integer, Set<Integer>>();
					for (RouteEntry entry : GlobalVars.BasicRouteEntries.values(p)) {
						Set<Integer> set = null;
						if (existUserRoutes.containsKey(entry.getUserId())) {
							set = existUserRoutes.get(entry.getUserId());
						} else {
							set = new HashSet<Integer>();
						}
						set.add(entry.getNetworkId());
						existUserRoutes.put(entry.getUserId(), set);
					}
					List<RouteEntryExt> routelist = new ArrayList<RouteEntryExt>();
					int auto_incr_id = 0;
					for (int user1 : user_id_list) {
						String systemId = GlobalVars.UserEntries.get(user1).getSystemId();
						String accountType = GlobalVars.WebmasterEntries.get(user1).getAccountType();
						Set<Integer> existNetworks = existUserRoutes.get(user1);
						logger.info(systemId + " Configured Networks: " + existNetworks);
						for (NetworkEntry networkEntry : networks) {
							int networkId = networkEntry.getId();
							if (existNetworks != null) {
								if (!existNetworks.contains(networkId)) {
									RouteEntry entry = new RouteEntry(user1, networkId, 0, 0, 0, "W", null, null,
											"new entry");
									entry.setId(++auto_incr_id);
									RouteEntryExt ext = new RouteEntryExt(entry);
									ext.setSystemId(systemId);
									ext.setCountry(networkEntry.getCountry());
									ext.setOperator(networkEntry.getOperator());
									ext.setMcc(networkEntry.getMcc());
									ext.setMnc(networkEntry.getMnc());
									ext.setSmsc("DOWN [NONE]");
									ext.setGroup("NONE");
									ext.setAccountType(accountType);
									routelist.add(ext);
								} else {
									logger.info(systemId + " Already Has Network: " + networkId);
								}
							} else {
								RouteEntry entry = new RouteEntry(user1, networkId, 0, 0, 0, "W", null, null,
										"new entry");
								entry.setId(++auto_incr_id);
								RouteEntryExt ext = new RouteEntryExt(entry);
								ext.setSystemId(systemId);
								ext.setCountry(networkEntry.getCountry());
								ext.setOperator(networkEntry.getOperator());
								ext.setMcc(networkEntry.getMcc());
								ext.setMnc(networkEntry.getMnc());
								ext.setSmsc("DOWN [NONE]");
								ext.setGroup("NONE");
								ext.setAccountType(accountType);
								routelist.add(ext);
							}
						}
					}
					Map<Integer, String> groupDetail = new HashMap<Integer, String>(listGroupNames());
					Map<Integer, String> smsclist = new HashMap<Integer, String>();
					Map<Integer, Set<String>> group_mapping = getSmscGroupMapping();
					Map<Integer, String> smscnames = listNames();
					for (int smsc_id : smscnames.keySet()) {
						String smsc_name = smscnames.get(smsc_id);
						if (group_mapping.containsKey(smsc_id)) {
							smsclist.put(smsc_id, smsc_name + " " + group_mapping.get(smsc_id));
						} else {
							smsclist.put(smsc_id, smsc_name + " [NONE]");
						}
					}
					smsclist.put(0, "DOWN [NONE]");
					optionRouteResponse.setSmsclist(smsclist);
					optionRouteResponse.setRoutinglist(routelist);
					optionRouteResponse.setGroupDetail(groupDetail);
					target = "addroute";
					logger.error("error.record.unavailable");
					// messages.add(ActionMessages.GLOBAL_MESSAGE, message);
				}
			} else if (Access.isAuthorizedAdmin(user.getRoles())) {
				if (routingForm.getUserId() != null) {
					List<Integer> user_under_master = new ArrayList<Integer>(listUsersUnderMaster(masterid).keySet());
					Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
							.get("secondaryMaster").equal(user.getSystemId());
					for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(p)) {
						user_under_master.add(webEntry.getUserId());
					}
					Set<Integer> selected_user_list = new HashSet<Integer>();
					for (int user_id : routingForm.getUserId()) {
						if (user_under_master.contains(user_id)) {
							selected_user_list.add(user_id);
						} else {
							logger.info(masterid + " Invalid User Routing Request: " + user_id);
							selected_user_list.clear();
							break;
						}
					}
					if (selected_user_list.isEmpty()) {
						logger.info(masterid + "[" + user.getRoles() + "] Authorization Failed");
						target = "invalidRequest";
					} else {
						List<RouteEntryExt> routinglist = getRoutingList(routingForm, false, false);
						if (!routinglist.isEmpty()) {
							Map<Integer, RouteEntry> master_routes = getNetworkRouting(user.getUserId().intValue());
							Iterator<RouteEntryExt> itr = routinglist.iterator();
							while (itr.hasNext()) {
								RouteEntryExt ext = itr.next();
								if (master_routes.containsKey(ext.getBasic().getNetworkId())) { // admin can edit only
																								// self allocated
																								// networks
									ext.setMasterCost(master_routes.get(ext.getBasic().getNetworkId()).getCost());
								} else {
									itr.remove();
								}
							}
							optionRouteResponse.setRoutinglist(routinglist);
							target = "partial";
						} else {
							target = IConstants.FAILURE_KEY;
							logger.error("error.record.unavailable");
						}
					}
				} else {
					logger.info(masterid + "[" + user.getRoles() + "] Authorization Failed");
					target = "invalidRequest";
				}
			} else {
				logger.info(masterid + "[" + user.getRoles() + "] Authorization Failed");
				target = "invalidRequest";
			}
		} catch (Exception ex) {
			logger.error("", ex);
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
		}
		optionRouteResponse.setStatus(target);
		return optionRouteResponse;

	}

	private List<RouteEntryExt> getRoutingList(RouteEntryArrForm routingForm) {
		String sql = "select A.id,A.user_id,A.smsc_id,A.group_id,A.network_id,CAST(A.cost AS CHAR) AS cost,A.smsc_type,A.editBy,A.edit_on,A.remarks,"
				+ "B.country,B.operator,B.mcc,B.mnc,C.name,D.system_id,D.master_id,D.currency,E.acc_type,F.name"
				+ " from routemaster A,network B,smscmaster C,usermaster D,web_master E,smsc_group F";
		boolean and = false;
		if (routingForm.getUserId() != null) {
			sql += " where A.user_id in("
					+ Arrays.stream(routingForm.getUserId()).mapToObj(String::valueOf).collect(Collectors.joining(","))
					+ ")";
			and = true;
		}
		if (routingForm.getSmscId() != null) {
			if (and) {
				sql += " and ";
			} else {
				sql += " where ";
				and = true;
			}
			sql += "A.smsc_id in("
					+ Arrays.stream(routingForm.getSmscId()).mapToObj(String::valueOf).collect(Collectors.joining(","))
					+ ")";
		}
		if (routingForm.getGroupId() != null) {
			if (and) {
				sql += " and ";
			} else {
				sql += " where ";
				and = true;
			}
			sql += "A.group_id in("
					+ Arrays.stream(routingForm.getGroupId()).mapToObj(String::valueOf).collect(Collectors.joining(","))
					+ ")";
		}
		if (routingForm.getSmscType() != null) {
			if (and) {
				sql += " and ";
			} else {
				sql += " where ";
				and = true;
			}
			sql += "A.smsc_type in('" + String.join("','", routingForm.getSmscType()) + "')";
		}
		if (routingForm.getCriteria().isPriceRange()) {
			if (and) {
				sql += " and ";
			} else {
				sql += " where ";
				and = true;
			}
			sql += "A.cost between " + routingForm.getCriteria().getMinCost() + " and "
					+ routingForm.getCriteria().getMaxCost();
		}
		System.out.println("CountryWise: " + routingForm.isCountryWise());
		System.out.println("mcc: " + routingForm.getCriteria().getMcc());
		System.out.println("networks: " + routingForm.getNetworkId());
		if (routingForm.isCountryWise()) {
			if (routingForm.getCriteria().getMcc() != null && routingForm.getCriteria().getMcc().length > 0) {
				boolean includeCountry = true;
				for (String mcc : routingForm.getCriteria().getMcc()) {
					if (mcc.equals("0")) {
						includeCountry = false;
						break;
					}
				}
				if (includeCountry) {
					if (and) {
						sql += " and ";
					} else {
						sql += " where ";
						and = true;
					}
					sql += "B.mcc in('" + String.join("','", routingForm.getCriteria().getMcc()) + "')";
				}
			}
		} else {
			if (routingForm.getNetworkId() != null && routingForm.getNetworkId().length > 0) {
				if (and) {
					sql += " and ";
				} else {
					sql += " where ";
					and = true;
				}
				sql += "A.network_id in(" + Arrays.stream(routingForm.getNetworkId()).mapToObj(String::valueOf)
						.collect(Collectors.joining(",")) + ")";
			}
		}
		if (routingForm.getCriteria().getCurrency() != null) {
			if (and) {
				sql += " and ";
			} else {
				sql += " where ";
				and = true;
			}
			sql += "D.currency in('" + String.join("','", routingForm.getCriteria().getCurrency()) + "')";
		}
		if (routingForm.getCriteria().getAccountType() != null) {
			if (and) {
				sql += " and ";
			} else {
				sql += " where ";
				and = true;
			}
			sql += "E.acc_type in('" + String.join("','", routingForm.getCriteria().getAccountType()) + "')";
		}
		if (and) {
			sql += " and ";
		} else {
			sql += " where ";
		}
		sql += "A.user_id = D.id and A.smsc_id=C.id and A.network_id = B.id and A.group_id = F.id and A.user_id = E.user_id";
		System.out.println(sql);
		List<RouteEntryExt> routinglist = null;
		try {
			routinglist = getBasicRouting(sql);
		} catch (Exception e) {
			logger.error("Exception: "+e.toString());
			throw new InternalServerErrorException("Exception: "+e.getLocalizedMessage());
		}
		return routinglist;
	}

	private List<RouteEntryExt> getBasicRouting(String sql) {
		List<RouteEntryExt> basicRouting = new ArrayList<>();

		try {
			Map<String, Map<Integer, Double>> smscPricing = getSmscPricing();
			Map<Integer, Set<String>> groupMapping = getSmscGroupMapping();

			basicRouting = jdbcTemplate.query(sql, (rs, rowNum) -> {
				RouteEntry entry = new RouteEntry(rs.getInt("A.user_id"), rs.getInt("A.network_id"),
						rs.getInt("A.smsc_id"), rs.getInt("A.group_id"), rs.getDouble("cost"),
						rs.getString("A.smsc_type"), rs.getString("A.editBy"), rs.getString("A.edit_on"),
						rs.getString("A.remarks"));
				entry.setId(rs.getInt("A.id"));

				RouteEntryExt ext = new RouteEntryExt(entry);
				ext.setSystemId(rs.getString("D.system_id"));
				ext.setMasterId(rs.getString("D.master_id"));
				ext.setCurrency(rs.getString("D.currency"));
				ext.setAccountType(rs.getString("E.acc_type"));
				ext.setCostStr(rs.getString("cost"));

				if (rs.getInt("A.network_id") == 0) {
					ext.setCountry("Default");
					ext.setOperator("Default");
					ext.setMcc("000");
					ext.setMnc("000");
				} else {
					ext.setCountry(rs.getString("B.country"));
					ext.setOperator(rs.getString("B.operator"));
					ext.setMcc(rs.getString("B.mcc"));
					ext.setMnc(rs.getString("B.mnc"));
				}

				if (rs.getInt("A.smsc_id") == 0) {
					ext.setSmsc("DOWN [NONE]");
				} else {
					if (groupMapping.containsKey(rs.getInt("A.smsc_id"))) {
						ext.setSmsc(rs.getString("c.name") + " " + groupMapping.get(rs.getInt("A.smsc_id")));
					} else {
						ext.setSmsc(rs.getString("c.name") + " [NONE]");
					}
				}

				if (rs.getInt("A.group_id") == 0) {
					ext.setGroup("NONE");
				} else {
					ext.setGroup(rs.getString("F.name"));
				}

				if (smscPricing.containsKey(rs.getString("c.name"))) {
					if (smscPricing.get(rs.getString("c.name")).containsKey(rs.getInt("A.network_id"))) {
						ext.setSmscCost(smscPricing.get(rs.getString("c.name")).get(rs.getInt("A.network_id")));
					}
				}

				return ext;
			});
		} catch (Exception e) {
			logger.error("Exception: "+e.toString());
			throw new InternalServerException("Exception: "+e.getLocalizedMessage());
		}

		return basicRouting;
	}

	public Map<String, Map<Integer, Double>> getSmscPricing() {
		Map<String, Map<Integer, Double>> smscPricing = new HashMap<>();

		try {
			String sql = "SELECT smsc_name, network_id, new_cost FROM crm_prices";
			jdbcTemplate.query(sql, (rs, rowNum) -> {
				String smsc = rs.getString("smsc_name");
				Map<Integer, Double> networkCost = smscPricing.getOrDefault(smsc, new HashMap<>());
				networkCost.put(rs.getInt("network_id"), rs.getDouble("new_cost"));
				smscPricing.put(smsc, networkCost);
				return null; // Not used since we are building the result incrementally
			});
		} catch (Exception e) {
			logger.error("Exception: "+e.toString());
			throw new InternalServerException("Exception: "+e.getLocalizedMessage());
		}

		return smscPricing;
	}

	private List<RouteEntryExt> getRoutingList(RouteEntryArrForm routingForm, boolean hlrEntry, boolean optEntry) {
		List<RouteEntryExt> routinglist = new ArrayList<RouteEntryExt>();
		String logText = "Routing Search Criteria: ";
		if (routingForm.getUserId() != null) {
			logText += " Users: " + routingForm.getUserId().length + ",";
		}
		if (routingForm.getSmscId() != null) {
			logText += " Routes: " + routingForm.getSmscId().length + ",";
		}
		if (routingForm.getGroupId() != null) {
			logText += " RouteGroups: " + routingForm.getGroupId().length + ",";
		}
		if (routingForm.getSmscType() != null) {
			logText += " SmscTypes: " + routingForm.getSmscType().length + ",";
		}
		if (routingForm.getCriteria().getCurrency() != null) {
			logText += " Currencies: " + routingForm.getCriteria().getCurrency().length + ",";
		}
		if (routingForm.getCriteria().getAccountType() != null) {
			logText += " AccountType: " + routingForm.getCriteria().getAccountType().length + ",";
		}
		try {
			// IDatabaseService dbService = HtiSmsDB.getInstance();
			// Map<Integer, Network> networkmap = null;
			SearchCriteria searchCriteria = new SearchCriteria();
			searchCriteria.setUserId(routingForm.getUserId());
			searchCriteria.setSmscId(routingForm.getSmscId());
			searchCriteria.setGroupId(routingForm.getGroupId());
			searchCriteria.setSmscType(routingForm.getSmscType());
			searchCriteria.setCurrency(routingForm.getCriteria().getCurrency());
			searchCriteria.setAccountType(routingForm.getCriteria().getAccountType());
			boolean priceRange = routingForm.getCriteria().isPriceRange();
			if (priceRange) {
				searchCriteria.setPriceRange(true);
				double minCost = routingForm.getCriteria().getMinCost();
				double maxCost = routingForm.getCriteria().getMaxCost();
				searchCriteria.setMinCost(minCost);
				searchCriteria.setMaxCost(maxCost);
				logText += " Price Range: " + minCost + " & " + maxCost;
			}
			/*
			 * logger.info("Search Criteria:-> Users:" + userSet.size() + " Smsc:" +
			 * smscSet.size() + " SmscType: " + smscTypeSet.size() + " Mcc:" + country +
			 * " Mnc:" + operator);
			 */
			if (routingForm.isCountryWise()) {
				String[] country = routingForm.getCriteria().getMcc();
				if (country != null && country.length > 0) {
					boolean includeCountry = true;
					for (String mcc : country) {
						if (mcc.equals("0")) {
							includeCountry = false;
							break;
						}
					}
					if (includeCountry) {
						Predicate<Integer, NetworkEntry> p = new PredicateBuilderImpl().getEntryObject().get("mcc")
								.in(country);
						Set<Integer> networks = new HashSet<Integer>();
						for (NetworkEntry network : GlobalVars.NetworkEntries.values(p)) {
							networks.add(network.getId());
						}
						int[] networkId = networks.stream().mapToInt(Integer::intValue).toArray();
						searchCriteria.setNetworkId(networkId);
						logText += " Networks: " + networks;
					}
				}
			} else {
				if (routingForm.getNetworkId() != null && routingForm.getNetworkId().length > 0) {
					searchCriteria.setNetworkId(routingForm.getNetworkId());
					logText += " Networks: " + Arrays.stream(routingForm.getNetworkId()).mapToObj(String::valueOf)
							.collect(Collectors.joining(","));
				}
			}
			searchCriteria.setHlrEntry(hlrEntry);
			searchCriteria.setOptEntry(optEntry);
			logger.info(logText);
			Map<Integer, RouteEntryExt> map = listRouteEntries(searchCriteria);
			routinglist.addAll(map.values());
			logger.info("Requested Routing List: " + routinglist.size());
		} catch (Exception e) {
			logger.error("Exception: ", e.toString());
			throw new InternalServerException("Exception: "+e.getLocalizedMessage());
		}
		return routinglist;
	}

	@Override
	public OptionRouteResponse SearchRoutingLookup(String username, RouteEntryArrForm routingForm) {
		OptionRouteResponse optionRouteResponse = new OptionRouteResponse();
		Optional<User> userOptional = userRepo.findBySystemId(username);
		User user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String target = "lookup";
		String masterid = user.getSystemId();
		logger.info(masterid + "[" + user.getRoles() + "] Lookup RouteEntries Search Request Using Advanced Criteria");
		try {
			if (Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				List<RouteEntryExt> routinglist = getRoutingList(routingForm, true, false);
				if (!routinglist.isEmpty()) {
					optionRouteResponse.setRoutinglist(routinglist);
				} else {
					target = IConstants.FAILURE_KEY;
					logger.error("error: record unavailable");
					throw new NotFoundException("Routing list not found!");
				}
			} else {
				logger.info(masterid + "[" + user.getRoles() + "] Authorization Failed");
				target = "invalidRequest";
				throw new UnauthorizedException("Unauthorized User!");
			}
			optionRouteResponse.setStatus(target);
		} catch (NotFoundException e) {
			logger.error("NotFoundException: "+e.toString());
			throw new NotFoundException("NotFoundException: "+e.getLocalizedMessage());
		} catch (UnauthorizedException e) {
			logger.error("UnauthorizedException: "+e.toString());
			throw new NotFoundException("UnauthorizedException: "+e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error("Exception: "+e.toString());
			throw new NotFoundException("Exception: "+e.getLocalizedMessage());
		}
		
		return optionRouteResponse;
	}

	public OptionRouteResponse SearchRoutingOptional(String username, RouteEntryArrForm routingForm) {
		OptionRouteResponse optionRouteResponse = new OptionRouteResponse();
		Optional<User> userOptional = userRepo.findBySystemId(username);
		User user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String target = IConstants.FAILURE_KEY;
		String masterid = user.getSystemId();
		logger.info(masterid + "[" + user.getRoles() + "] Lookup RouteEntries Search Request Using Advanced Criteria");
		try {
			if (Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				List<RouteEntryExt> routinglist = getRoutingList(routingForm, false, true);
				if (!routinglist.isEmpty()) {
					Map<Integer, String> groupDetail = new HashMap<Integer, String>(listGroupNames());
					Map<Integer, String> smsclist = new HashMap<Integer, String>();
					Map<Integer, Set<String>> group_mapping = getSmscGroupMapping();
					Map<Integer, String> smscnames = listNames();
					for (int smsc_id : smscnames.keySet()) {
						String smsc_name = smscnames.get(smsc_id);
						if (group_mapping.containsKey(smsc_id)) {
							smsclist.put(smsc_id, smsc_name + " " + group_mapping.get(smsc_id));
						} else {
							smsclist.put(smsc_id, smsc_name + " [NONE]");
						}
					}
					smsclist.put(0, "DOWN [NONE]");
					optionRouteResponse.setGroupDetail(groupDetail);
					optionRouteResponse.setRoutinglist(routinglist);
					optionRouteResponse.setSmsclist(smsclist);
				} else {
					target = IConstants.FAILURE_KEY;
					logger.error("error: record unavailable");
					throw new NotFoundException("NotFound Exception: Routing List Not Found");
				}
			} else {
				logger.error(masterid + "[" + user.getRoles() + "] Authorization Failed");
				target = "invalidRequest";
				throw new UnauthorizedException("Unauthorized User Exception!");
			}
			optionRouteResponse.setStatus(target);
		} catch (NotFoundException e) {
			logger.error("NotFound Exception: "+e.toString());
			throw new NotFoundException("NotFound Exception: "+e.getLocalizedMessage());
		} catch (UnauthorizedException e) {
			logger.error("Unauthorized Exception: "+e.toString());
			throw new NotFoundException("Unauthorized Exception: "+e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error("Unexpected Exception: "+e.toString());
			throw new NotFoundException("Exception: "+e.getLocalizedMessage());
		}
		return optionRouteResponse;
	}

	@Override
	public OptionRouteResponse BasicRouteBasicRoute(String username, RouteEntryArrForm routingForm) {
		Optional<User> userOptional = userRepo.findBySystemId(username);
		User user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		OptionRouteResponse optionRouteResponse = new OptionRouteResponse();
		String target = IConstants.FAILURE_KEY;
		String masterid = user.getSystemId();
		logger.info("Route Update Requested By " + masterid + " [" + user.getRoles() + "]");
		Set<Integer> refreshUsers = new HashSet<Integer>();
		List<RouteEntry> list = new ArrayList<RouteEntry>();
		String editOn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		try {
			int[] id = routingForm.getId();
			double[] cost = routingForm.getCost();
			if (Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				Map<Integer, List<RouteEntry>> userWiseRouting = new HashMap<Integer, List<RouteEntry>>();
				int[] userid = routingForm.getUserId();
				int[] networkId = routingForm.getNetworkId();
				int[] smscId = routingForm.getSmscId();
				int[] groupId = routingForm.getGroupId();
				String[] smscType = routingForm.getSmscType();
				String[] remarks = routingForm.getRemarks();
				for (int i = 0; i < id.length; i++) {
					RouteEntry routingDTO = new RouteEntry(userid[i], masterid, editOn, remarks[i]);
					routingDTO.setUserId(userid[i]);
					routingDTO.setId(id[i]);
					routingDTO.setSmscId(smscId[i]);
					routingDTO.setGroupId(groupId[i]);
					routingDTO.setNetworkId(networkId[i]);
					routingDTO.setCost(cost[i]);
					if (smscType[i] == null || smscType[i].trim().length() < 1) {
						routingDTO.setSmscType("D");
					} else {
						routingDTO.setSmscType(smscType[i]);
					}
					List<RouteEntry> route_list = null;
					if (userWiseRouting.containsKey(userid[i])) {
						route_list = userWiseRouting.get(userid[i]);
					} else {
						route_list = new ArrayList<RouteEntry>();
					}
					route_list.add(routingDTO);
					userWiseRouting.put(userid[i], route_list);
					// refreshUsers.add(userid[i]);
				}
				if (userWiseRouting.isEmpty()) {
					logger.error("error: record unavailable. user wise routing is empty!");
				} else {
					// ----- check for auto_copy_routing users ------------
					// EntryObject e = new PredicateBuilder().getEntryObject();
					Predicate<Integer, WebMasterEntry> pw = new PredicateBuilderImpl().getEntryObject()
							.is("autoCopyRouting");
					Set<Integer> auto_copy_route_users = GlobalVars.WebmasterEntries.keySet(pw);
					if (!auto_copy_route_users.isEmpty()) {
						EntryObject e = new PredicateBuilderImpl().getEntryObject();
						Predicate<Integer, UserEntry> p = e.get("role").equal("admin")
								.and(e.get("id").in(userWiseRouting.keySet().stream().toArray(Integer[]::new)));
						Collection<UserEntry> resellers = GlobalVars.UserEntries.values(p);
						for (UserEntry reseller : resellers) {
							p = new PredicateBuilderImpl().getEntryObject().get("masterId")
									.equal(reseller.getSystemId());
							for (int subUserId : GlobalVars.UserEntries.keySet(p)) {
								logger.info(reseller.getSystemId() + " Checking Copy Route for SubUser: " + subUserId);
								if (auto_copy_route_users.contains(subUserId)) {
									logger.info(reseller.getSystemId() + " Auto Copy Route Enabled For SubUser: "
											+ subUserId);
									if (userWiseRouting.containsKey(subUserId)) {
										userWiseRouting.remove(subUserId);
									}
									WebMasterEntry webEntry = GlobalVars.WebmasterEntries.get(subUserId);
									double margin = 0;
									boolean isPercent = false;
									if (webEntry.getRouteMargin() != null && webEntry.getRouteMargin().length() > 0) {
										String margin_str = webEntry.getRouteMargin();
										if (margin_str.contains("%")) {
											isPercent = true;
											margin_str = margin_str.substring(0, margin_str.indexOf("%")).trim();
										}
										try {
											margin = Double.parseDouble(margin_str);
										} catch (Exception ex) {
											logger.error(subUserId + "Invalid margin: " + margin_str);
											throw new InternalServerException("Parse Exception: "+ex.getLocalizedMessage());
										}
									}
									logger.info(
											reseller.getSystemId() + " SubUser[" + subUserId + "] Margin: " + margin);
									List<RouteEntry> child_routing = new ArrayList<RouteEntry>();
									// child_routing.addAll(userWiseRouting.get(reseller.getId()));
									Map<Integer, RouteEntry> child_network_routing = getNetworkRouting(subUserId);
									// Iterator<RouteEntry> itr = child_routing.iterator();
									RouteEntry childRouteEntry = null;
									for (RouteEntry ext : userWiseRouting.get(reseller.getId())) {
										childRouteEntry = new RouteEntry(subUserId, ext.getNetworkId(), ext.getSmscId(),
												ext.getGroupId(), ext.getCost(), ext.getSmscType(), ext.getEditBy(),
												ext.getEditOn(), ext.getRemarks());
										if (child_network_routing.containsKey(childRouteEntry.getNetworkId())) {
											childRouteEntry.setId(
													child_network_routing.get(childRouteEntry.getNetworkId()).getId());
										} else {
											logger.info(reseller.getSystemId() + " SubUser[" + subUserId + "] Network["
													+ childRouteEntry.getNetworkId() + "] Skipped");
											continue;
										}
										if (margin > 0) {
											double child_cost = childRouteEntry.getCost();
											if (isPercent) {
												child_cost = child_cost + ((child_cost * margin) / 100);
											} else {
												child_cost = child_cost + margin;
											}
											childRouteEntry.setCost(child_cost);
										}
										child_routing.add(childRouteEntry);
									}
									logger.info(reseller.getSystemId() + " SubUser[" + subUserId + "] Routings: "
											+ child_routing.size());
									userWiseRouting.put(subUserId, child_routing);
								}
							}
						}
					}
					logger.info(masterid + "[" + user.getRoles() + "] Edit Routing Users: " + userWiseRouting.keySet());
					for (List<RouteEntry> user_wise_entries : userWiseRouting.values()) {
						list.addAll(user_wise_entries);
					}
					refreshUsers.addAll(userWiseRouting.keySet());
				}
			} else {
				Map<Integer, RouteEntry> master_routes = getNetworkRouting(user.getUserId().intValue());
				for (int i = 0; i < id.length; i++) {
					if (GlobalVars.BasicRouteEntries.containsKey(id[i])) {
						RouteEntry basic = GlobalVars.BasicRouteEntries.get(id[i]);
						if (master_routes.containsKey(basic.getNetworkId())) {
							RouteEntry master_route_entry = master_routes.get(basic.getNetworkId());
							if (cost[i] < master_route_entry.getCost()) {
								logger.error(masterid + " RouteId[" + id[i] + "] Cost[" + cost[i]
										+ "] less than Purchase[" + master_route_entry.getCost() + "]");
							} else {
								RouteEntry routingDTO = new RouteEntry(basic.getUserId(), masterid, editOn,
										basic.getRemarks());
								routingDTO.setId(id[i]);
								routingDTO.setSmscId(basic.getSmscId());
								routingDTO.setGroupId(basic.getGroupId());
								routingDTO.setNetworkId(basic.getNetworkId());
								routingDTO.setCost(cost[i]);
								routingDTO.setSmscType(basic.getSmscType());
								list.add(routingDTO);
								refreshUsers.add(basic.getUserId());
								logger.info(masterid + " Added To Update: " + routingDTO);
							}
						} else {
							logger.error(masterid + " RouteId[" + id[i] + "] Invalid Network[" + basic.getNetworkId()
									+ "] Update Request");
							throw new InternalServerException(masterid + " RouteId[" + id[i] + "] Invalid Network[" + basic.getNetworkId()
							+ "] Update Request");
						}
					} else {
						logger.error(masterid + " Invalid RouteId[" + id[i] + "] Update Request");
						throw new InternalServerException(masterid + " Invalid RouteId[" + id[i] + "] Update Request");
					}
				}
			}
			if (!list.isEmpty()) {
				if (routingForm.isSchedule()) {
					String[] scheduledOn = routingForm.getScheduledOn().split(" ");
					DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
					Date scheduledDate = df.parse(scheduledOn[0]);
					Date currentDate = df.parse(df.format(new Date()));
					if (scheduledDate.before(currentDate)) {
						logger.error(masterid + " Basic Routing Scheduled Date is Before Current Date");
					} else {
						addRouteSchEntry(list, routingForm.getScheduledOn());
						if (scheduledDate.after(currentDate)) {
							logger.info(masterid + " Basic Routing Not Scheduled For Today");
						} else {
							logger.info(masterid + " Basic Routing Scheduled For Today");
							Timer t = new Timer();
							t.schedule(new TimerTask() {
								public void run() {
									System.out.println(masterid + " Routemaster Scheduled Update Task Starting");
									try {
										updateRouteSch(routingForm.getScheduledOn());
									} catch (Exception e) {
										logger.error(masterid + " " + e.fillInStackTrace());
										throw new InternalServerErrorException(e.getLocalizedMessage());
									}
									for (int user : refreshUsers) {
										MultiUtility.refreshRouting(user);
									}
									MultiUtility.changeFlag(Constants.CLIENT_FLAG_FILE, "707");
									logger.info(masterid + " Routemaster Scheduled Update Task End");
								}
							}, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(routingForm.getScheduledOn()));
						}
						target = "schedule";
						logger.info("Schedule configured successfully!");
					}
				} else {
					updateRouteEntries(list);
					if (Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
						List<RouteEntryExt> routinglist = null;
						try {
							routinglist = getUpdateBasicRoutingList(routingForm.getCriterionEntries());
						} catch (Exception e) {
							logger.error("Exception: "+e.toString());
							throw new InternalServerException("Exception: "+e.getLocalizedMessage());
						}
						if (routinglist != null && !routinglist.isEmpty()) {
							Map<Integer, String> smscnames = listNames();
							Map<Integer, String> smsclist = new HashMap<Integer, String>();
							Map<Integer, Set<String>> group_mapping = getSmscGroupMapping();
							for (int smsc_id : smscnames.keySet()) {
								String smsc_name = smscnames.get(smsc_id);
								if (group_mapping.containsKey(smsc_id)) {
									smsclist.put(smsc_id, smsc_name + " " + group_mapping.get(smsc_id));
								} else {
									smsclist.put(smsc_id, smsc_name + " [NONE]");
								}
							}
							smsclist.put(0, "DOWN [NONE]");
							optionRouteResponse.setGroupDetail(listGroupNames());
							optionRouteResponse.setRoutinglist(routinglist);
							optionRouteResponse.setSmsclist(smsclist);
							target = "view";
						} else {
							target = IConstants.SUCCESS_KEY;
							logger.info("Routing Configured");
						}
					} else {
						target = IConstants.SUCCESS_KEY;
						logger.info("Routing Configured");
					}
				}
			} else {
				logger.info("error: record unavailable");
				throw new NotFoundException("No data in list Exception!");
			}
		} catch (InternalServerException ex) {
			logger.info("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new InternalServerException("Process Error: "+ex.getLocalizedMessage());
		} catch (NotFoundException ex) {
			logger.info("NotFound Exception: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new NotFoundException("NotFound Exception: "+ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.info("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new InternalServerException("Exception: "+ex.getLocalizedMessage());
		}
		if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY) || target.equalsIgnoreCase("view")) {
			for (int user1 : refreshUsers) {
				MultiUtility.refreshRouting(user1);
			}
			MultiUtility.changeFlag(Constants.CLIENT_FLAG_FILE, "707");
		}
		optionRouteResponse.setStatus(target);
		return optionRouteResponse;
	}

	protected void updateRouteSch(String scheduledOn) {
		try {
			// Update routemaster based on routemaster_sch
			String updateQRY = "UPDATE RouteMaster A, RouteMasterSch B "
					+ "SET A.userId = B.userId, A.networkId = B.networkId, A.smscId = B.smscId, "
					+ "A.groupId = B.groupId, A.cost = B.cost, A.smscType = B.smscType, "
					+ "A.editBy = B.scheduleBy, A.editOn = B.scheduleOn, A.remarks = B.remarks "
					+ "WHERE A.id = B.id AND B.scheduleOn = :scheduledOn";

			Query updateQuery = entityManager.createQuery(updateQRY);
			updateQuery.setParameter("scheduledOn", scheduledOn);
			int updateCount = updateQuery.executeUpdate();
			System.out.println("Routemaster Schedule Update Counter: " + updateCount);

			// Delete entries from routemaster_sch
			Query deleteQuery = entityManager.createQuery("DELETE FROM RouteMasterSch WHERE scheduleOn = :scheduledOn");
			deleteQuery.setParameter("scheduledOn", scheduledOn);
			int deleteCount = deleteQuery.executeUpdate();
			System.out.println("Routemaster Schedule Delete Counter: " + deleteCount);
		} catch (Exception e) {
			throw new InternalServerErrorException("ERROR: updateRouteSch()" + e);
		}
	}

	private void addRouteSchEntry(List<RouteEntry> list, String scheduledOn) {

		try {
			List<RoutemasterSch> list1 = new ArrayList<>();
			for (RouteEntry entry : list) {
				RoutemasterSch routemasterSch = new RoutemasterSch();

				routemasterSch.setScheduleOn(scheduledOn);
				routemasterSch.setCost(entry.getCost());
				routemasterSch.setGroupId(entry.getGroupId());
				routemasterSch.setId(entry.getId());
				routemasterSch.setNetworkId(entry.getNetworkId());
				routemasterSch.setRemarks(entry.getRemarks());
				routemasterSch.setScheduleBy(entry.getEditBy());
				routemasterSch.setSmscId(entry.getSmscId());
				routemasterSch.setSmscType(entry.getSmscType());
				routemasterSch.setUserId(entry.getUserId());
				list1.add(routemasterSch);
			}
			routemasterSchRepository.saveAll(list1);

			// Logging
			int batchSize = list.size();
			System.out.println("Scheduled Basic Route Entries Added: " + batchSize);
		} catch (Exception e) {
			// Exception handling
			throw new RuntimeException("Error adding Route Entries", e);
		}
	}

	@Override
	public OptionRouteResponse deleteRouteBasicRoute(String username, RouteEntryArrForm routingForm) {
		Set<Integer> refreshUsers = new HashSet<Integer>();
		OptionRouteResponse optionRouteResponse = new OptionRouteResponse();
		String target = IConstants.FAILURE_KEY;
		Optional<User> userOptional = userRepo.findBySystemId(username);
		User user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String masterid = user.getSystemId();
		logger.info("Route Delete Requested By " + masterid + " [" + user.getRoles() + "]");
		List<RouteEntry> list = new ArrayList<RouteEntry>();
		int[] id = routingForm.getId();
		String editOn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		try {
			if (Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				Map<Integer, List<RouteEntry>> userWiseRouting = new HashMap<Integer, List<RouteEntry>>();
				int[] userid = routingForm.getUserId();
				int[] networkId = routingForm.getNetworkId();
				String[] remarks = routingForm.getRemarks();
				if (id != null && id.length > 0) {
					for (int i = 0; i < id.length; i++) {
						RouteEntry entry = new RouteEntry(userid[i], masterid, editOn, remarks[i]);
						entry.setId(id[i]);
						entry.setNetworkId(networkId[i]);
						List<RouteEntry> route_list = null;
						if (userWiseRouting.containsKey(userid[i])) {
							route_list = userWiseRouting.get(userid[i]);
						} else {
							route_list = new ArrayList<RouteEntry>();
						}
						route_list.add(entry);
						userWiseRouting.put(userid[i], route_list);
						refreshUsers.add(userid[i]);
					}
				}
				if (userWiseRouting.isEmpty()) {
					logger.error("error: record unavailable!");
				} else {
					// ----- check for auto_copy_routing users ------------
					// EntryObject e = new PredicateBuilder().getEntryObject();
					Predicate<Integer, WebMasterEntry> pw = new PredicateBuilderImpl().getEntryObject()
							.is("autoCopyRouting");
					Set<Integer> auto_copy_route_users = GlobalVars.WebmasterEntries.keySet(pw);
					if (!auto_copy_route_users.isEmpty()) {
						EntryObject e = new PredicateBuilderImpl().getEntryObject();
						Predicate<Integer, UserEntry> p = e.get("role").equal("admin")
								.and(e.get("id").in(userWiseRouting.keySet().stream().toArray(Integer[]::new)));
						Collection<UserEntry> resellers = GlobalVars.UserEntries.values(p);
						for (UserEntry reseller : resellers) {
							p = new PredicateBuilderImpl().getEntryObject().get("masterId")
									.equal(reseller.getSystemId());
							for (int subUserId : GlobalVars.UserEntries.keySet(p)) {
								logger.info(
										reseller.getSystemId() + " Checking Remove Route for SubUser: " + subUserId);
								if (auto_copy_route_users.contains(subUserId)) {
									logger.info(reseller.getSystemId() + " Auto Remove Route Enabled For SubUser: "
											+ subUserId);
									if (userWiseRouting.containsKey(subUserId)) {
										userWiseRouting.remove(subUserId);
									}
									List<RouteEntry> child_routing = new ArrayList<RouteEntry>();
									// child_routing.addAll(userWiseRouting.get(reseller.getId()));
									Map<Integer, RouteEntry> child_network_routing = getNetworkRouting(subUserId);
									RouteEntry childRouteEntry = null;
									for (RouteEntry ext : userWiseRouting.get(reseller.getId())) {
										childRouteEntry = new RouteEntry(subUserId, ext.getEditBy(), ext.getEditOn(),
												ext.getRemarks());
										childRouteEntry.setNetworkId(ext.getNetworkId());
										if (child_network_routing.containsKey(childRouteEntry.getNetworkId())) {
											childRouteEntry.setId(
													child_network_routing.get(childRouteEntry.getNetworkId()).getId());
										} else {
											logger.info(reseller.getSystemId() + " SubUser[" + subUserId + "] Network["
													+ childRouteEntry.getNetworkId() + "] Skipped");
											continue;
										}
										child_routing.add(childRouteEntry);
									}
									logger.info(reseller.getSystemId() + " SubUser[" + subUserId + "] Routings: "
											+ child_routing.size());
									userWiseRouting.put(subUserId, child_routing);
								}
							}
						}
					}
					logger.info(
							masterid + "[" + user.getRoles() + "] Remove Routing Users: " + userWiseRouting.keySet());
					for (List<RouteEntry> user_wise_entries : userWiseRouting.values()) {
						list.addAll(user_wise_entries);
					}
					refreshUsers.addAll(userWiseRouting.keySet());
				}
			} else {
				Set<Integer> master_routes = getNetworkRouting(user.getUserId().intValue()).keySet();
				for (int i = 0; i < id.length; i++) {
					if (GlobalVars.BasicRouteEntries.containsKey(id[i])) {
						RouteEntry basic = GlobalVars.BasicRouteEntries.get(id[i]);
						if (master_routes.contains(basic.getNetworkId())) {
							RouteEntry entry = new RouteEntry(basic.getUserId(), masterid, editOn, basic.getRemarks());
							entry.setId(id[i]);
							entry.setNetworkId(basic.getNetworkId());
							list.add(entry);
							refreshUsers.add(basic.getUserId());
							logger.info(masterid + " Added To Remove: " + entry);
						} else {
							logger.error(masterid + " RouteId[" + id[i] + "] Invalid Network[" + basic.getNetworkId()
									+ "] remove Request");
							throw new InternalServerException(masterid + " RouteId[" + id[i] + "] Invalid Network[" + basic.getNetworkId()
							+ "] remove Request");
						}
					} else {
						logger.error(masterid + " Invalid RouteId[" + id[i] + "] remove Request");
						throw new InternalServerException(masterid + " Invalid RouteId[" + id[i] + "] remove Request");
					}
				}
			}
			if (!list.isEmpty()) {
				// logger.info("deleting records");
				deleteRouteEntries(list);
				if (Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
					List<RouteEntryExt> routinglist = null;
					try {
						routinglist = getUpdateBasicRoutingList(routingForm.getCriterionEntries());
					} catch (Exception e) {
						throw new InternalServerException(e.getLocalizedMessage());
					}
					if (routinglist != null && !routinglist.isEmpty()) {
						Map<Integer, String> smscnames = listNames();
						Map<Integer, String> smsclist = new HashMap<Integer, String>();
						Map<Integer, Set<String>> group_mapping = getSmscGroupMapping();
						for (int smsc_id : smscnames.keySet()) {
							String smsc_name = smscnames.get(smsc_id);
							if (group_mapping.containsKey(smsc_id)) {
								smsclist.put(smsc_id, smsc_name + " " + group_mapping.get(smsc_id));
							} else {
								smsclist.put(smsc_id, smsc_name + " [NONE]");
							}
						}
						smsclist.put(0, "DOWN [NONE]");
						optionRouteResponse.setGroupDetail(listGroupNames());
						optionRouteResponse.setRoutinglist(routinglist);
						optionRouteResponse.setSmsclist(smsclist);
						target = "view";
					} else {
						target = IConstants.SUCCESS_KEY;
						logger.info("Routing Configured!");
					}
				} else {
					target = IConstants.SUCCESS_KEY;
					logger.info("Routing Configured!");
				}
			} else {
				logger.error("error: record unavailable");
				throw new NotFoundException("Record Not Found!");
			}
		} catch (InternalServerException ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new InternalServerException("Process Error: "+ex.getLocalizedMessage());
		} catch (NotFoundException ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new NotFoundException("NotFound Exception: "+ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new InternalServerException("Exception: "+ex.getLocalizedMessage());
		}
		if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY) || target.equalsIgnoreCase("view")) {
			for (int user1 : refreshUsers) {
				MultiUtility.refreshRouting(user1);
			}
			MultiUtility.changeFlag(Constants.CLIENT_FLAG_FILE, "707");
		}

		optionRouteResponse.setStatus(target);
		return optionRouteResponse;
	}

	@Override
	public OptionRouteResponse undoRouteBasicRoute(String username, RouteEntryArrForm routingForm) {
		OptionRouteResponse optionRouteResponse = new OptionRouteResponse();
		String target = IConstants.FAILURE_KEY;
		Optional<User> userOptional = userRepo.findBySystemId(username);
		User user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String masterid = user.getSystemId();
		// RouteDAService routeService = new RouteDAServiceImpl();
		Set<Integer> refreshUsers = new HashSet<Integer>();
		logger.info("Route Undo Requested By " + masterid + " [" + user.getRoles() + "]");
		List<RouteEntry> list = new ArrayList<RouteEntry>();
		int[] id = routingForm.getId();
		try {
			if (id != null && id.length > 0) {
				String editOn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				Collection<RouteEntryLog> log_list = listBasicLogEntries(id).values();
				if (log_list != null && !log_list.isEmpty()) {
					logger.info("Entries For Undo:-> " + log_list.size());
					for (RouteEntryLog logEntry : log_list) {
						logger.debug(logEntry.toString());
						RouteEntry entry = new RouteEntry(logEntry.getUserId(), logEntry.getNetworkId(),
								logEntry.getSmscId(), logEntry.getGroupId(), logEntry.getCost(), logEntry.getSmscType(),
								masterid, editOn, null);
						entry.setId(logEntry.getId());
						list.add(entry);
						refreshUsers.add(logEntry.getUserId());
					}
					updateRouteEntries(list);
					if (Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
						List<RouteEntryExt> routinglist = getUpdateBasicRoutingList(routingForm.getCriterionEntries());
						if (routinglist != null && !routinglist.isEmpty()) {
							Map<Integer, String> smscnames = listNames();
							Map<Integer, String> smsclist = new HashMap<Integer, String>();
							Map<Integer, Set<String>> group_mapping = getSmscGroupMapping();
							for (int smsc_id : smscnames.keySet()) {
								String smsc_name = smscnames.get(smsc_id);
								if (group_mapping.containsKey(smsc_id)) {
									smsclist.put(smsc_id, smsc_name + " " + group_mapping.get(smsc_id));
								} else {
									smsclist.put(smsc_id, smsc_name + " [NONE]");
								}
							}
							smsclist.put(0, "DOWN [NONE]");
							optionRouteResponse.setGroupDetail(listGroupNames());
							optionRouteResponse.setRoutinglist(routinglist);
							optionRouteResponse.setSmsclist(smsclist);
							target = "view";
						} else {
							target = IConstants.SUCCESS_KEY;
							logger.info("Routing Configured!");
						}
					} else {
						target = IConstants.SUCCESS_KEY;
						logger.info("Routing Configured!");
					}
				} else {
					logger.error("error: record unavailable");
					throw new NotFoundException("Log List Not Found!");
				}
			} else {
				logger.error("error: record unavailable");
				throw new NotFoundException("Id is null or not found!");
			}
		} catch (NotFoundException ex) {
			logger.error("NotFound Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new NotFoundException("NotFound Exception: "+ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new InternalServerException("Exception: "+ex.getLocalizedMessage());
		}
		if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY) || target.equalsIgnoreCase("view")) {
			for (int user1 : refreshUsers) {
				MultiUtility.refreshRouting(user1);
			}
			MultiUtility.changeFlag(Constants.CLIENT_FLAG_FILE, "707");
		}
		optionRouteResponse.setStatus(target);
		return optionRouteResponse;
	}

	@Override
	public OptionRouteResponse previousRouteBasicRoute(String username, RouteEntryArrForm routingForm) {
		OptionRouteResponse optionRouteResponse = new OptionRouteResponse();
		Optional<User> userOptional = userRepo.findBySystemId(username);
		User user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String target = IConstants.FAILURE_KEY;

		String masterid = user.getSystemId();
		logger.info("Route Log Requested By " + masterid + " [" + user.getRoles() + "]");
		// List<RouteEntry> list = new ArrayList<RouteEntry>();
		int[] id = routingForm.getId();
		// String editOn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new
		// Date());
		try {
			if (Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				if (id != null && id.length > 0) {
					String sql = "select A.id,A.user_id,A.smsc_id,A.group_id,A.network_id,A.cost,A.smsc_type,A.editBy,A.affectedOn,"
							+ "B.country,B.operator,C.name,D.system_id,D.master_id,D.currency,E.acc_type,F.name"
							+ " from route_basic_log A,network B,smscmaster C,usermaster D,web_master E,smsc_group F where"
							+ " A.id in(" + Arrays.stream(id).mapToObj(String::valueOf).collect(Collectors.joining(","))
							+ ") and A.user_id = D.id and A.smsc_id=C.id and A.network_id = B.id and A.group_id = F.id and A.user_id = E.user_id"
							+ " order by A.affectedOn DESC";
					Query nativeQuery = entityManager.createNativeQuery(sql, "RouteEntryExtMapping");
					List<RouteEntryExt> list = nativeQuery.getResultList();
					if (!list.isEmpty()) {
						optionRouteResponse.setRoutinglist(list);
						target = "previous";
					} else {
						logger.error("error: record unavailable");
						throw new NotFoundException("No data found in RouteEntryExt List.");
					}
				} else {
					logger.error("error: record unavailable");
					throw new NotFoundException("Id not found or is null!");
				}
			} else {
				logger.info(masterid + "[" + user.getRoles() + "] Authorization Failed");
				target = "invalidRequest";
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} catch (NotFoundException ex) {
			logger.error("NotFoundException: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new NotFoundException("NotFoundException: "+ex.getLocalizedMessage());
		} catch (UnauthorizedException ex) {
			logger.error("UnauthorizedException: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new UnauthorizedException("Unauthorized Exception: "+ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new InternalServerException("Exception: "+ex.getLocalizedMessage());
		}
		optionRouteResponse.setStatus(target);
		return optionRouteResponse;
	}

	@Override
	public OptionRouteResponse hlrRouteBasicRoute(String username, RouteEntryArrForm routingForm) {
		OptionRouteResponse optionRouteResponse = new OptionRouteResponse();
		String target = IConstants.FAILURE_KEY;
		Optional<User> userOptional = userRepo.findBySystemId(username);
		User user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String masterid = user.getSystemId();
		logger.info("Route Log Requested By " + masterid + " [" + user.getRoles() + "]");
		// List<RouteEntry> list = new ArrayList<RouteEntry>();
		int[] id = routingForm.getId();
		// String editOn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new
		// Date());
		try {
			if (Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				if (id != null && id.length > 0) {
					List<RouteEntryExt> list = getHlrRoutingList(routingForm.getCriterionEntries());
					if (!list.isEmpty()) {
						optionRouteResponse.setRoutinglist(list);
						target = "hlr";
					} else {
						logger.error("error: record unavailable");
						throw new NotFoundException("No data found in RouteEntryExt List.");
					}
				} else {
					logger.error("error: record unavailable");
					throw new NotFoundException("Id not found or is null!");
				}
			} else {
				logger.info(masterid + "[" + user.getRoles() + "] Authorization Failed");
				target = "invalidRequest";
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} catch (NotFoundException ex) {
			logger.error("NotFoundException: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new NotFoundException("NotFoundException: "+ex.getLocalizedMessage());
		} catch (UnauthorizedException ex) {
			logger.error("UnauthorizedException: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new UnauthorizedException("Unauthorized Exception: "+ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new InternalServerException("Exception: "+ex.getLocalizedMessage());
		}
		optionRouteResponse.setStatus(target);
		return optionRouteResponse;
	}

	@Override
	public OptionRouteResponse optionalRouteBasicRoute(String username, RouteEntryArrForm routingForm) {
		OptionRouteResponse optionRouteResponse = new OptionRouteResponse();
		String target = IConstants.FAILURE_KEY;
		Optional<User> userOptional = userRepo.findBySystemId(username);
		User user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String masterid = user.getSystemId();
		logger.info("Route Log Requested By " + masterid + " [" + user.getRoles() + "]");
		// List<RouteEntry> list = new ArrayList<RouteEntry>();
		int[] id = routingForm.getId();
		// String editOn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new
		// Date());
		try {
			if (Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				if (id != null && id.length > 0) {
					List<RouteEntryExt> list = getOptionalList(routingForm.getCriterionEntries());
					if (!list.isEmpty()) {
						Map<Integer, String> groupDetail = new HashMap<Integer, String>(listGroupNames());
						Map<Integer, String> smsclist = new HashMap<Integer, String>();

						Map<Integer, Set<String>> group_mapping = getSmscGroupMapping();
						Map<Integer, String> smscnames = listNames();
						for (int smsc_id : smscnames.keySet()) {
							String smsc_name = smscnames.get(smsc_id);
							if (group_mapping.containsKey(smsc_id)) {
								smsclist.put(smsc_id, smsc_name + " " + group_mapping.get(smsc_id));
							} else {
								smsclist.put(smsc_id, smsc_name + " [NONE]");
							}
						}
						smsclist.put(0, "DOWN [NONE]");
						optionRouteResponse.setGroupDetail(groupDetail);
						optionRouteResponse.setRoutinglist(list);
						optionRouteResponse.setSmsclist(smsclist);
						target = "optional";
					} else {
						logger.error("error: record unavailable");
						throw new NotFoundException("No data found in RouteEntryExt List.");
					}
				} else {
					logger.error("error: record unavailable");
					throw new NotFoundException("Id not found or is null!");
				}
			} else {
				logger.info(masterid + "[" + user.getRoles() + "] Authorization Failed");
				target = "invalidRequest";
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} catch (NotFoundException ex) {
			logger.error("NotFoundException: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new NotFoundException("NotFoundException: "+ex.getLocalizedMessage());
		} catch (UnauthorizedException ex) {
			logger.error("UnauthorizedException: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new UnauthorizedException("Unauthorized Exception: "+ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new InternalServerException("Exception: "+ex.getLocalizedMessage());
		}
		optionRouteResponse.setStatus(target);
		return optionRouteResponse;
	}

	@Override
	public OptionRouteResponse hlrRouteUpdate(String username, HlrEntryArrForm hlrRouteEntry) {
		OptionRouteResponse optionRouteResponse = new OptionRouteResponse();
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		User user = null;
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String target = IConstants.FAILURE_KEY;
		String masterid = user.getSystemId();
		// Set<Integer> refreshUsers = new HashSet<Integer>();
		Map<Integer, List<HlrRouteEntry>> userWiseRouting = new HashMap<Integer, List<HlrRouteEntry>>();
		logger.info("HlrRoute Update Requested By " + masterid + " [" + user.getRoles() + "]");
		String editOn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		try {
			List<HlrRouteEntry> list = new ArrayList<HlrRouteEntry>();
			int[] id = hlrRouteEntry.getRouteId();
			int[] userId = hlrRouteEntry.getUserId();
			boolean[] hlr = hlrRouteEntry.getHlr();
			boolean[] mnp = hlrRouteEntry.getMnp();
			int[] smsc = hlrRouteEntry.getSmsc();
			int[] hlrCache = hlrRouteEntry.getHlrCache();
			double[] cost = hlrRouteEntry.getCost();
			HlrRouteEntry routingDTO = null;
			for (int i = 0; i < id.length; i++) {
				routingDTO = new HlrRouteEntry(masterid, editOn);
				routingDTO.setRouteId(id[i]);
				routingDTO.setHlr(hlr[i]);
				routingDTO.setCost(cost[i]);
				routingDTO.setSmsc(smsc[i]);
				routingDTO.setHlrCache(hlrCache[i]);
				routingDTO.setMnp(mnp[i]);
				List<HlrRouteEntry> hlr_list = null;
				if (userWiseRouting.containsKey(userId[i])) {
					hlr_list = userWiseRouting.get(userId[i]);
				} else {
					hlr_list = new ArrayList<HlrRouteEntry>();
				}
				hlr_list.add(routingDTO);
				userWiseRouting.put(userId[i], hlr_list);
				// refreshUsers.add(userId[i]);
			}
			if (!userWiseRouting.isEmpty()) {
				// ----- check for auto_copy_routing users ------------
				// EntryObject e = new PredicateBuilder().getEntryObject();
				Predicate<Integer, WebMasterEntry> pw = new PredicateBuilderImpl().getEntryObject()
						.is("autoCopyRouting");
				Set<Integer> auto_copy_route_users = GlobalVars.WebmasterEntries.keySet(pw);
				if (!auto_copy_route_users.isEmpty()) {
					EntryObject e = new PredicateBuilderImpl().getEntryObject();
					Predicate<Integer, UserEntry> pu = e.get("role").equal("admin")
							.and(e.get("id").in(userWiseRouting.keySet().stream().toArray(Integer[]::new)));
					Collection<UserEntry> resellers = GlobalVars.UserEntries.values(pu);
					for (UserEntry reseller : resellers) {
						pu = new PredicateBuilderImpl().getEntryObject().get("masterId").equal(reseller.getSystemId());
						for (int subUserId : GlobalVars.UserEntries.keySet(pu)) {
							logger.info(reseller.getSystemId() + " Checking Copy Route for SubUser: " + subUserId);
							if (auto_copy_route_users.contains(subUserId)) {
								logger.info(
										reseller.getSystemId() + " Auto Copy Route Enabled For SubUser: " + subUserId);
								if (userWiseRouting.containsKey(subUserId)) {
									userWiseRouting.remove(subUserId);
								}
								Set<Integer> reseller_route_id = new HashSet<Integer>();
								for (HlrRouteEntry hlrEntry : userWiseRouting.get(reseller.getId())) {
									reseller_route_id.add(hlrEntry.getRouteId());
								}
								e = new PredicateBuilderImpl().getEntryObject();
								Predicate<Integer, RouteEntry> p = e.get("userId").equal(reseller.getId())
										.and(e.get("id").in(reseller_route_id.stream().toArray(Integer[]::new)));
								Map<Integer, Integer> reseller_network_id = new HashMap<Integer, Integer>();
								for (RouteEntry basicEntry : GlobalVars.BasicRouteEntries.values(p)) {
									reseller_network_id.put(basicEntry.getId(), basicEntry.getNetworkId());
								}
								e = new PredicateBuilderImpl().getEntryObject();
								p = e.get("userId").equal(subUserId).and(e.get("networkId")
										.in(reseller_network_id.values().stream().toArray(Integer[]::new)));
								Map<Integer, Integer> child_network_id = new HashMap<Integer, Integer>();
								for (RouteEntry basicEntry : GlobalVars.BasicRouteEntries.values(p)) {
									child_network_id.put(basicEntry.getNetworkId(), basicEntry.getId());
								}
								List<HlrRouteEntry> child_routing = new ArrayList<HlrRouteEntry>();
								// child_routing.addAll(userWiseRouting.get(reseller.getId()));
								// Iterator<HlrRouteEntry> itr = child_routing.iterator();
								HlrRouteEntry childRouteEntry = null;
								for (HlrRouteEntry masterEntry : userWiseRouting.get(reseller.getId())) {
									childRouteEntry = new HlrRouteEntry(0, masterEntry.isHlr(), masterEntry.getSmsc(),
											masterEntry.getHlrCache(), masterEntry.getCost(), masterEntry.getEditBy(),
											masterEntry.getEditOn(), masterEntry.isMnp());
									int network_id = reseller_network_id.get(masterEntry.getRouteId());
									if (child_network_id.containsKey(network_id)) {
										childRouteEntry.setRouteId(child_network_id.get(network_id));
									} else {
										logger.info(reseller.getSystemId() + " SubUser[" + subUserId + "] Network["
												+ network_id + "] Skipped");
										continue;
									}
									child_routing.add(childRouteEntry);
								}
								logger.info(reseller.getSystemId() + " SubUser[" + subUserId + "] Routings: "
										+ child_routing.size());
								userWiseRouting.put(subUserId, child_routing);
							}
						}
					}
				}
				logger.info(masterid + "[" + user.getRoles() + "] Edit Hlr Routing Users: " + userWiseRouting.keySet());
				for (List<HlrRouteEntry> user_wise_entries : userWiseRouting.values()) {
					list.addAll(user_wise_entries);
				}
				// ---------------- end ------------------------------
				logger.info("HlrRoute Update Size: " + list.size());
				if (hlrRouteEntry.isSchedule()) {
					String[] scheduledOn = hlrRouteEntry.getScheduledOn().split(" ");
					DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
					Date scheduledDate = df.parse(scheduledOn[0]);
					Date currentDate = df.parse(df.format(new Date()));
					if (scheduledDate.before(currentDate)) {
						logger.error(masterid + " Hlr Routing Scheduled Date is Before Current Date");
					} else {
						addHlrRouteSchEntry(list, hlrRouteEntry.getScheduledOn());
						if (scheduledDate.after(currentDate)) {
							logger.info(masterid + " Hlr Routing Not Scheduled For Today");
						} else {
							logger.info(masterid + " Hlr Routing Scheduled For Today");
							Timer t = new Timer();
							t.schedule(new TimerTask() {
								public void run() {
									System.out.println(masterid + " Hlr Routing Scheduled Update Task Starting");
									try {
										updateHlrRouteSch(hlrRouteEntry.getScheduledOn());
									} catch (Exception e) {
										System.out.println(masterid + " " + e.fillInStackTrace());
									}
									for (int user : userWiseRouting.keySet()) {
										MultiUtility.refreshRouting(user);
									}
									MultiUtility.changeFlag(Constants.CLIENT_FLAG_FILE, "707");
									System.out.println(masterid + " Hlr Routing Scheduled Update Task End");
								}

							}, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(hlrRouteEntry.getScheduledOn()));
						}
						target = "schedule";
						logger.info("Schedule Successful!");
					}
				} else {
					updateHlrRouteEntries(list);
					List<RouteEntryExt> routinglist = null;
					try {
						routinglist = getUpdateHlrRoutingList(hlrRouteEntry.getCriterionEntries());
					} catch (Exception e) {
						logger.error("Exception: "+e.toString());
						throw new InternalServerException("Exception: "+e.getLocalizedMessage());
					}
					if (!routinglist.isEmpty()) {
						optionRouteResponse.setRoutinglist(routinglist);
						target = "view";
					} else {
						target = IConstants.SUCCESS_KEY;
						logger.info("Routing Configured");
					}
				}
			} else {
				logger.error("No Records Found to Proceed");
				throw new NotFoundException("No Records Found to Proceed");
			}
		} catch (NotFoundException ex) {
			logger.error("NotFound Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new NotFoundException("NotFoundException: "+ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new InternalServerException("Exception: "+ex.getLocalizedMessage());
		}
		if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY) || target.equalsIgnoreCase("view")) {
			for (int user1 : userWiseRouting.keySet()) {
				MultiUtility.refreshRouting(user1);
			}
			MultiUtility.changeFlag(Constants.CLIENT_FLAG_FILE, "707");
		}
		optionRouteResponse.setStatus(target);
		return optionRouteResponse;
	}

	private void addHlrRouteSchEntry(List<HlrRouteEntry> list, String scheduledOn) {
		try {
			list.forEach(entry -> {
				HlrRouteEntrySch hlrRouteEntrySch = new HlrRouteEntrySch();

				hlrRouteEntrySch.setCost(entry.getCost());
				hlrRouteEntrySch.setEditBy(entry.getEditBy());
				hlrRouteEntrySch.setEditOn(entry.getEditOn());
				hlrRouteEntrySch.setHlr(entry.isHlr());
				hlrRouteEntrySch.setHlrCache(entry.getHlrCache());
				hlrRouteEntrySch.setMnp(entry.isMnp());
				hlrRouteEntrySch.setRouteId(entry.getRouteId());
				hlrRouteEntrySch.setScheduleOn(scheduledOn);
				hlrRouteEntrySch.setSmsc(entry.getSmsc());
				hlrRouteEntrySchRepository.save(hlrRouteEntrySch);
			});
		} catch (Exception e) {
			throw new InternalServerErrorException("Failed to add Hlr Route Entries" + e);

		}
	}

	@Transactional
	public void updateHlrRouteSch(String scheduledOn) {
		try {
			// Fetch the HlrRouteEntrySch entity by schedule_on
			HlrRouteEntrySch hlrRouteEntrySch = hlrRouteEntrySchRepository.findByScheduleOn(scheduledOn);
			if (hlrRouteEntrySch != null) {
				// Create a new HlrRouteEntry entity and copy values from HlrRouteEntrySch
				HlrRouteEntry hlrRouteEntry = new HlrRouteEntry();
				hlrRouteEntry.setHlr(hlrRouteEntrySch.isHlr());
				hlrRouteEntry.setMnp(hlrRouteEntrySch.isMnp());
				hlrRouteEntry.setSmsc(hlrRouteEntrySch.getSmsc());
				hlrRouteEntry.setHlrCache(hlrRouteEntrySch.getHlrCache());
				hlrRouteEntry.setCost(hlrRouteEntrySch.getCost());
				hlrRouteEntry.setEditBy(hlrRouteEntrySch.getEditBy());
				hlrRouteEntry.setEditOn(hlrRouteEntrySch.getEditOn());

				// Save the new HlrRouteEntry entity
				hlrRouteEntryRepository.save(hlrRouteEntry);

				// Delete the HlrRouteEntrySch entity
				hlrRouteEntrySchRepository.delete(hlrRouteEntrySch);
			}
		} catch (Exception e) {
			throw new InternalServerErrorException("Failed to update Hlr Route Entries", e);
		}
	}

	@Override
	public OptionRouteResponse hlrRouteUndo(String username, HlrEntryArrForm hlrRouteEntry) {
		OptionRouteResponse optionRouteResponse = new OptionRouteResponse();
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		User user = null;
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String target = IConstants.FAILURE_KEY;
		String masterid = user.getSystemId();
		Set<Integer> refreshUsers = new HashSet<Integer>();
		logger.info("HlrRoute Undo Requested By " + masterid + " [" + user.getRoles() + "]");
		try {
			List<HlrRouteEntry> list = new ArrayList<HlrRouteEntry>();
			int[] id = hlrRouteEntry.getRouteId();
			int[] userId = hlrRouteEntry.getUserId();
			if (id != null && id.length > 0) {
				String editOn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				Map<Integer, HlrEntryLog> map = listHlrLog(id);
				logger.info("HlrRoute Undo Records: " + map.size());
				HlrRouteEntry routingDTO = null;
				for (int i = 0; i < id.length; i++) {
					if (map.containsKey(id[i])) {
						HlrEntryLog logEntry = map.get(id[i]);
						routingDTO = new HlrRouteEntry(logEntry.getRouteId(), logEntry.isHlr(), logEntry.getSmsc(),
								logEntry.getHlrCache(), logEntry.getCost(), masterid, editOn, logEntry.isMnp());
						list.add(routingDTO);
						refreshUsers.add(userId[i]);
					}
				}
				if (!list.isEmpty()) {
					logger.info("HlrRoute Update Size: " + list.size());
					updateHlrRouteEntries(list);
					List<RouteEntryExt> routinglist = null;
					try {
						routinglist = getUpdateHlrRoutingList(hlrRouteEntry.getCriterionEntries());
					} catch (Exception e) {
						logger.error(e.toString());
						throw new InternalServerException("Exception: "+e.getLocalizedMessage());
					}
					if (!routinglist.isEmpty()) {
						optionRouteResponse.setRoutinglist(routinglist);
						target = "view";
					} else {
						target = IConstants.SUCCESS_KEY;
						logger.info("Routing Configured");
					}
				} else {
					logger.error("No Records Found to Proceed");
					throw new NotFoundException("No Records Found to Proceed. List is empty!");
				}
			} else {
				logger.error("No Records Found to Proceed");
				throw new NotFoundException("No Records Found to Proceed. Id not found or is null!");
			}
		} catch (NotFoundException ex) {
			logger.error("NotFound Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new NotFoundException("NotFoundException: "+ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new InternalServerException("Exception: "+ex.getLocalizedMessage());
		}
		if (target.equalsIgnoreCase(IConstants.SUCCESS_KEY) || target.equalsIgnoreCase("view")) {
			for (int user1 : refreshUsers) {
				MultiUtility.refreshRouting(user1);
			}
			MultiUtility.changeFlag(Constants.CLIENT_FLAG_FILE, "707");
		}
		optionRouteResponse.setStatus(target);
		return optionRouteResponse;
	}

	@Override
	public OptionRouteResponse hlrRoutePrevious(String username, HlrEntryArrForm routingForm) {
		OptionRouteResponse optionRouteResponse = new OptionRouteResponse();
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
		String target = IConstants.FAILURE_KEY;
		String masterid = user.getSystemId();
		Set<Role> role = user.getRoles();

		logger.info("Hlr Route Log Requested By " + masterid + " [" + role + "]");
		// List<RouteEntry> list = new ArrayList<RouteEntry>();
		int[] id = routingForm.getRouteId();
		// String editOn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new
		// Date());
		try {
			if (id != null && id.length > 0) {
				String sql = "select A.user_id,A.network_id,B.route_id,B.isHlr,B.hlr_smsc,B.hlr_cache,B.cost,B.affectedOn,B.is_mnp,C.country,C.operator,D.system_id "
						+ "from routemaster A,route_hlr_log B,network C,usermaster D where " + "B.route_id in("
						+ Arrays.stream(id).mapToObj(String::valueOf).collect(Collectors.joining(","))
						+ ") and A.id = B.route_id and A.user_id = D.id and A.network_id = C.id "
						+ " order by B.affectedOn DESC";
				Query nativeQuery = entityManager.createNativeQuery(sql, "RouteEntryExtMapping");
				List<RouteEntryExt> list = nativeQuery.getResultList();
				if (!list.isEmpty()) {
					optionRouteResponse.setRoutinglist(list);
					target = "previous";
				} else {
					logger.error("No Records Found to Proceed");
					throw new NotFoundException("No Records Found to Proceed. List is empty.");
				}
			} else {
				logger.error("No Records Found to Proceed");
				throw new NotFoundException("No Records Found to Proceed. Id not found or is null!");
			}
		} catch (NotFoundException ex) {
			logger.error("NotFound Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new NotFoundException("NotFoundException: "+ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new InternalServerException("Exception: "+ex.getLocalizedMessage());
		}
		optionRouteResponse.setStatus(target);
		return optionRouteResponse;
	}

	@Override
	public OptionRouteResponse hlrRouteBasic(String username, HlrEntryArrForm routingForm) {
		OptionRouteResponse optionRouteResponse = new OptionRouteResponse();
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
		String target = IConstants.FAILURE_KEY;
		String masterid = user.getSystemId();
		Set<Role> role = user.getRoles();
		logger.info("Route Log Requested By " + masterid + " [" + role + "]");
		// List<RouteEntry> list = new ArrayList<RouteEntry>();
		int[] id = routingForm.getRouteId();
		// String editOn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new
		// Date());
		try {
			if (id != null && id.length > 0) {
				List<RouteEntryExt> list = getBasicRoutingList(routingForm.getCriterionEntries());
				if (!list.isEmpty()) {
					Map<Integer, String> smscnames = listNames();
					Map<Integer, String> smsclist = new HashMap<Integer, String>();
					Map<Integer, Set<String>> group_mapping = getSmscGroupMapping();
					for (int smsc_id : smscnames.keySet()) {
						String smsc_name = smscnames.get(smsc_id);
						if (group_mapping.containsKey(smsc_id)) {
							smsclist.put(smsc_id, smsc_name + " " + group_mapping.get(smsc_id));
						} else {
							smsclist.put(smsc_id, smsc_name + " [NONE]");
						}
					}
					smsclist.put(0, "DOWN [NONE]");
					optionRouteResponse.setGroupDetail(listGroupNames());
					optionRouteResponse.setRoutinglist(list);
					optionRouteResponse.setSmsclist(smsclist);
					target = "basic";
				} else {
					logger.error("No Records Found to Proceed");
					throw new NotFoundException("No Records Found to Proceed. List is empty.");
				}
			} else {
				logger.error("No Records Found to Proceed");
				throw new NotFoundException("No Records Found to Proceed. Id not found or is null!");
			}
		} catch (NotFoundException ex) {
			logger.error("NotFound Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new NotFoundException("NotFoundException: "+ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new InternalServerException("Exception: "+ex.getLocalizedMessage());
		}
		optionRouteResponse.setStatus(target);
		return optionRouteResponse;
	}

	@Override
	public OptionRouteResponse hlrRouteOptional(String username, HlrEntryArrForm routingForm) {
		OptionRouteResponse optionRouteResponse = new OptionRouteResponse();
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
		String target = IConstants.FAILURE_KEY;
		String masterid = user.getSystemId();
		Set<Role> role = user.getRoles();
		logger.info("Route Log Requested By " + masterid + " [" + role + "]");
		// List<RouteEntry> list = new ArrayList<RouteEntry>();
		int[] id = routingForm.getRouteId();
		// String editOn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new
		// Date());
		try {
			if (Access.isAuthorizedSuperAdminAndSystem(role)) {
				if (id != null && id.length > 0) {
					List<RouteEntryExt> list = getOptionalList(routingForm.getCriterionEntries());
					if (!list.isEmpty()) {
						Map<Integer, String> groupDetail = new HashMap<Integer, String>(listGroupNames());
						Map<Integer, String> smsclist = new HashMap<Integer, String>();
						Map<Integer, Set<String>> group_mapping = getSmscGroupMapping();
						Map<Integer, String> smscnames = listNames();
						for (int smsc_id : smscnames.keySet()) {
							String smsc_name = smscnames.get(smsc_id);
							if (group_mapping.containsKey(smsc_id)) {
								smsclist.put(smsc_id, smsc_name + " " + group_mapping.get(smsc_id));
							} else {
								smsclist.put(smsc_id, smsc_name + " [NONE]");
							}
						}
						smsclist.put(0, "DOWN [NONE]");
						optionRouteResponse.setGroupDetail(groupDetail);
						optionRouteResponse.setRoutinglist(list);
						optionRouteResponse.setSmsclist(smsclist);
						target = "optional";
					} else {
						logger.error("error: record unavailable");
						throw new NotFoundException("No data found in RouteEntryExt List.");
					}
				} else {
					logger.error("error: record unavailable");
					throw new NotFoundException("Id not found or is null!");
				}
			} else {
				logger.info(masterid + "[" + user.getRoles() + "] Authorization Failed");
				target = "invalidRequest";
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} catch (NotFoundException ex) {
			logger.error("NotFoundException: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new NotFoundException("NotFoundException: "+ex.getLocalizedMessage());
		} catch (UnauthorizedException ex) {
			logger.error("UnauthorizedException: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new UnauthorizedException("Unauthorized Exception: "+ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new InternalServerException("Exception: "+ex.getLocalizedMessage());
		}
		optionRouteResponse.setStatus(target);
		return optionRouteResponse;
	}

	@Override
	public OptionRouteResponse UpdateOptionalRouteHlr(OptEntryArrForm routingForm, String username) {
		OptionRouteResponse optionRouteResponse = new OptionRouteResponse();
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
		String target = IConstants.FAILURE_KEY;
		String masterid = user.getSystemId();
		logger.info("Route Log Requested By " + masterid + " [" + user.getRoles() + "]");
		// List<RouteEntry> list = new ArrayList<RouteEntry>();
		int[] id = routingForm.getRouteId();
		// String editOn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new
		// Date());
		try {
			if (id != null && id.length > 0) {
				List<RouteEntryExt> list = getHlrRoutingList(routingForm.getCriterionEntries());
				if (!list.isEmpty()) {
					optionRouteResponse.setRoutinglist(list);
					target = "hlr";
				} else {
					logger.error("error: record unavailable");
					throw new NotFoundException("No Data Found in RouteEntryExt List!");
				}
			} else {
				logger.error("error: record unavailable");
				throw new NotFoundException("Id not found or is null!");
			}

		} catch (NotFoundException ex) {
			logger.error("NotFoundException: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new NotFoundException("NotFoundException: "+ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(masterid, ex.fillInStackTrace());
			throw new InternalServerException("Exception: "+ex.getLocalizedMessage());
		}
		optionRouteResponse.setStatus(target);
		return optionRouteResponse;

	}

	private List<RouteEntryExt> getUpdateOptionalRoutingList(String criterianEntries) throws SQLException {
		String sql = "select A.user_id,A.network_id,"
				+ "B.route_id,B.isReplaceContent,B.content_replace,B.backup_smsc_id,B.num_smsc_id,B.reg_smsc_id,B.reg_group_id,B.reg_sender_id,B.forceSIDNum,B.forceSIDAlpha,B.set_expiry,"
				+ "B.sms_length,B.code_length,B.refund,B.edit_on,B.msgAppender,B.sourceAppender,B.editBy,B.sender_repl_from,B.sender_repl_to,"
				+ "C.country,C.operator,D.name as backup_smsc,F.name as num_smsc,G.name as reg_smsc,E.system_id,H.name as reg_group_name "
				+ "from routemaster A,route_opt B,network C,smscmaster D,smscmaster F,smscmaster G,usermaster E,smsc_group H where "
				+ "B.route_id in(" + criterianEntries
				+ ") and A.id = B.route_id and B.backup_smsc_id = D.id and B.num_smsc_id=F.id and B.reg_smsc_id=G.id and A.user_id = E.id and A.network_id = C.id and B.reg_group_id=H.id ";
		System.out.println(sql);
		Query nativeQuery = entityManager.createNativeQuery(sql, "RouteEntryExtMapping");
		List<RouteEntryExt> routinglist = nativeQuery.getResultList();
		return routinglist;
	}

	private List<RouteEntryExt> getUpdateHlrRoutingList(String criterianEntries) throws SQLException {
		String sql = "select A.user_id,A.network_id,B.route_id,B.isHlr,B.hlr_smsc,B.hlr_cache,B.cost,B.edit_on,B.editBy,B.is_mnp,C.country,C.operator,C.mcc,C.mnc,D.system_id "
				+ "from routemaster A,hlr_routing B,network C,usermaster D where " + "B.route_id in(" + criterianEntries
				+ ") and A.id = B.route_id and A.user_id = D.id and A.network_id = C.id ";
		System.out.println(sql);
		Query nativeQuery = entityManager.createNativeQuery(sql, "RouteEntryExtMapping");
		List<RouteEntryExt> routinglist = nativeQuery.getResultList();
		return routinglist;
	}

	private List<RouteEntryExt> getUpdateBasicRoutingList(String criterianEntries) throws SQLException {
		String sql = "select A.id,A.user_id,A.smsc_id,A.group_id,A.network_id,CAST(A.cost AS CHAR) AS cost,A.smsc_type,A.editBy,A.edit_on,A.remarks,"
				+ "B.country,B.operator,B.mcc,B.mnc,C.name,D.system_id,D.master_id,D.currency,E.acc_type,F.name"
				+ " from routemaster A,network B,smscmaster C,usermaster D,web_master E,smsc_group F where A.id in("
				+ criterianEntries + ") and ";
		sql += "A.user_id = D.id and A.smsc_id=C.id and A.network_id = B.id and A.group_id = F.id and A.user_id = E.user_id";
		System.out.println(sql);
		Query nativeQuery = entityManager.createNativeQuery(sql, "RouteEntryExtMapping");
		List<RouteEntryExt> routinglist = nativeQuery.getResultList();
		return routinglist;
	}

	private List<RouteEntryExt> getBasicRoutingList(String criterianEntries) throws SQLException {
		String sql = "select A.id,A.user_id,A.smsc_id,A.group_id,A.network_id,CAST(A.cost AS CHAR) AS cost,A.smsc_type,A.editBy,A.edit_on,A.remarks,"
				+ "B.country,B.operator,B.mcc,B.mnc,C.name,D.system_id,D.master_id,D.currency,E.acc_type,F.name"
				+ " from routemaster A,network B,smscmaster C,usermaster D,web_master E,smsc_group F where A.id in("
				+ criterianEntries + ") and ";
		sql += "A.user_id = D.id and A.smsc_id=C.id and A.network_id = B.id and A.group_id = F.id and A.user_id = E.user_id";
		System.out.println(sql);
		Query nativeQuery = entityManager.createNativeQuery(sql, "RouteEntryExtMapping");
		List<RouteEntryExt> routinglist = nativeQuery.getResultList();
		return routinglist;
	}

	private List<RouteEntryExt> getOptionalList(String criterianEntries) {
		String sql = "select A.user_id,A.network_id,"
				+ "B.route_id,B.isReplaceContent,B.content_replace,B.backup_smsc_id,B.num_smsc_id,B.reg_smsc_id,B.reg_group_id,B.reg_sender_id,B.forceSIDNum,B.forceSIDAlpha,B.set_expiry,"
				+ "B.sms_length,B.code_length,B.refund,B.edit_on,B.msgAppender,B.sourceAppender,B.editBy,B.sender_repl_from,B.sender_repl_to,"
				+ "C.country,C.operator,D.name as backup_smsc,F.name as num_smsc,G.name as reg_smsc,E.system_id,H.name as reg_group_name "
				+ "from routemaster A,route_opt B,network C,smscmaster D,smscmaster F,smscmaster G,usermaster E,smsc_group H where "
				+ "B.route_id in(" + criterianEntries
				+ ") and A.id = B.route_id and B.backup_smsc_id = D.id and B.num_smsc_id=F.id and B.reg_smsc_id=G.id and A.user_id = E.id and A.network_id = C.id and B.reg_group_id=H.id ";
		System.out.println(sql);
		Query nativeQuery = entityManager.createNativeQuery(sql, "RouteEntryExtMapping");
		List<RouteEntryExt> routinglist = nativeQuery.getResultList();
		return routinglist;
	}

	private List<RouteEntryExt> getHlrRoutingList(String criterianEntries) {
		String sql = "select A.user_id,A.network_id,B.route_id,B.isHlr,B.hlr_smsc,B.hlr_cache,B.cost,B.edit_on,B.editBy,B.is_mnp,C.country,C.operator,C.mcc,C.mnc,D.system_id "
				+ "from routemaster A,hlr_routing B,network C,usermaster D where " + "B.route_id in(" + criterianEntries
				+ ") and A.id = B.route_id and A.user_id = D.id and A.network_id = C.id ";
		System.out.println(sql);
		Query nativeQuery = entityManager.createNativeQuery(sql, "RouteEntryExtMapping");
		List<RouteEntryExt> routinglist = nativeQuery.getResultList();
		return routinglist;
	}

}
