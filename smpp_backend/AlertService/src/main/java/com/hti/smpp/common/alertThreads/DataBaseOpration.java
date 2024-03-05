package com.hti.smpp.common.alertThreads;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
<<<<<<< HEAD
import java.util.Collections;
=======
>>>>>>> 96401f1d1d1a31c5e1b73c83ac974f4359502342
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
=======
import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.PredicateBuilderImpl;
>>>>>>> 96401f1d1d1a31c5e1b73c83ac974f4359502342
import com.hti.smpp.common.contacts.dto.GroupEntry;
import com.hti.smpp.common.contacts.repository.GroupEntryRepository;
import com.hti.smpp.common.dto.Network;
import com.hti.smpp.common.dto.UserEntryExt;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.route.dto.RouteEntry;
import com.hti.smpp.common.route.dto.RouteEntryExt;
import com.hti.smpp.common.route.repository.RouteEntryRepository;
import com.hti.smpp.common.smsc.dto.SmscEntry;
import com.hti.smpp.common.smsc.repository.SmscEntryRepository;
import com.hti.smpp.common.user.dto.DlrSettingEntry;
import com.hti.smpp.common.user.dto.ProfessionEntry;
import com.hti.smpp.common.user.dto.User;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.DlrSettingEntryRepository;
import com.hti.smpp.common.user.repository.ProfessionEntryRepository;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.util.GlobalVars;

@Component
public class DataBaseOpration {

	private static final Logger logger = LoggerFactory.getLogger(DataBaseOpration.class);

	@Autowired
	private DataSource dataSource;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Autowired
	private GroupEntryRepository groupEntryRepository;

	@Autowired
	private RouteEntryRepository routeEntryRepository;

	@Autowired
	private UserEntryRepository userEntryRepository;

	@Autowired
	private DlrSettingEntryRepository dlrSettingEntryRepository;

	@Autowired
	private WebMasterEntryRepository masterEntryRepository;

	@Autowired
	private ProfessionEntryRepository professionEntryRepository;

	@Autowired
	private SmscEntryRepository smscEntryRepository;

	public Map<String, String> checkCustomSettings() {
		Map<String, String> custom = new HashMap<String, String>();
		Connection con = null;
		PreparedStatement pStmt1 = null;
		ResultSet res = null;
		try {
			con = getConnection();
			String query1 = "select username,price_change_subject from custom_setting";
			pStmt1 = con.prepareStatement(query1);
			res = pStmt1.executeQuery();
			while (res.next()) {
				String username = res.getString("username");
				String price_change_subject = res.getString("price_change_subject");
				if (price_change_subject != null && price_change_subject.length() > 0) {
					custom.put(username, price_change_subject);
				}
			}
			// logger.info("CUSTOM_SUBJECT_DB: ==>" + custom);
		} catch (SQLException sqle) {
			logger.error(" ", sqle.fillInStackTrace());
		} finally {
			try {
				if (res != null) {
					res.close();
					res = null;
				}
				if (pStmt1 != null) {
					pStmt1.close();
					pStmt1 = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return custom;
	}

	public Map<Integer, UserEntryExt> listUserEntries() {
		List<UserEntry> listUser = userEntryRepository.findAll();
		logger.debug("listUserEntries():" + listUser.size());
		Map<Integer, UserEntryExt> map = new LinkedHashMap<Integer, UserEntryExt>();
		for (UserEntry user : listUser) {
			UserEntryExt entry = getUserEntryExt(user);
			map.put(user.getId(), entry);
		}
		return map;
	}

	public UserEntryExt getUserEntryExt(UserEntry user) {
		logger.debug("getUserEntry(" + user.getId() + ")");
		Optional<DlrSettingEntry> dltOptional = dlrSettingEntryRepository.findById(user.getId());
		WebMasterEntry webMasterOptional = masterEntryRepository.findByUserId(user.getId());
		Optional<ProfessionEntry> professionalOptional = professionEntryRepository.findByUserId(user.getId());
		if (dltOptional.isPresent() && webMasterOptional != null && professionalOptional.isPresent()) {
			UserEntryExt entry = new UserEntryExt(user);
			entry.setDlrSettingEntry(dltOptional.get());
			entry.setWebMasterEntry(webMasterOptional);
			entry.setProfessionEntry(professionalOptional.get());
			logger.debug("end getUserEntry(" + user.getId() + ")");
			return entry;
		} else {
			return null;
		}
	}

	public Map<Integer, List<RouteEntryExt>> checkForPriceChange() throws SQLException {
		Map<Integer, List<RouteEntryExt>> routing = new HashMap<Integer, List<RouteEntryExt>>();
		Connection con = null;
		PreparedStatement pStmt1 = null;
		ResultSet res = null;
		try {
			con = getConnection();
			String query1 = "select id,user_id,network_id,CAST(cost_old AS CHAR) AS cost_old,CAST(cost_new AS CHAR) AS cost_new,smsc_old_id,smsc_new_id,affected_date from routemaster_updates where flag='false'";
			pStmt1 = con.prepareStatement(query1);
			res = pStmt1.executeQuery();
			RouteEntryExt routingDTO = null;
			RouteEntry entry = null;
			while (res.next()) {
				double cost_old = res.getDouble("cost_old");
				double cost_new = res.getDouble("cost_new");
				String smsc_old = res.getString("smsc_old_id");
				String smsc_new = res.getString("smsc_new_id");
				String remarks = "-";
				if (smsc_old != null && smsc_old.length() > 0) {
					if (cost_old != cost_new) {
						if (cost_new > cost_old) {
							remarks = "Increased";
						} else {
							remarks = "Decreased";
						}
					} else if (!smsc_old.equalsIgnoreCase(smsc_new)) {
						remarks = "Updated";
					}
				} else {
					remarks = "Added";
				}
				entry = new RouteEntry();
				routingDTO = new RouteEntryExt(entry);
				int networkId = res.getInt("network_id");
				entry.setId(res.getInt("id"));
				entry.setUserId(res.getInt("user_id"));
				entry.setNetworkId(networkId);
				entry.setEditOn(res.getString("affected_date"));
				routingDTO.setOldCost(res.getString("cost_old"));
				routingDTO.setCostStr(res.getString("cost_new"));
				routingDTO.setRemarks(remarks);
				List<RouteEntryExt> list = null;
				if (routing.containsKey(networkId)) {
					list = routing.get(networkId);
				} else {
					list = new ArrayList<RouteEntryExt>();
				}
				list.add(routingDTO);
				routing.put(networkId, list);
			}
		} catch (SQLException sqle) {
			throw new SQLException(sqle.getMessage());
		} finally {
			try {
				if (res != null) {
					res.close();
					res = null;
				}
				if (pStmt1 != null) {
					pStmt1.close();
					pStmt1 = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return routing;
	}

	public Map<Integer, List<RouteEntryExt>> checkForPriceChangeSch() throws SQLException {
		Map<Integer, List<RouteEntryExt>> routing = new HashMap<Integer, List<RouteEntryExt>>();
		Connection con = null;
		PreparedStatement pStmt1 = null;
		ResultSet res = null;
		try {
			con = getConnection();
			String query1 = "select A.id,A.user_id,A.network_id,A.schedule_on,CAST(B.cost AS CHAR) AS cost_old,CAST(A.cost AS CHAR) AS cost_new from routemaster_sch A,routemaster B where A.flag='false' and A.id=B.id";
			pStmt1 = con.prepareStatement(query1);
			res = pStmt1.executeQuery();
			RouteEntryExt routingDTO = null;
			RouteEntry entry = null;
			while (res.next()) {
				double cost_old = res.getDouble("cost_old");
				double cost_new = res.getDouble("cost_new");
				String remarks = "-";
				if (cost_old != cost_new) {
					if (cost_new > cost_old) {
						remarks = "Increased";
					} else {
						remarks = "Decreased";
					}
				} else {
					continue;
				}
				entry = new RouteEntry();
				routingDTO = new RouteEntryExt(entry);
				int networkId = res.getInt("A.network_id");
				entry.setId(res.getInt("A.id"));
				entry.setUserId(res.getInt("A.user_id"));
				entry.setNetworkId(networkId);
				entry.setEditOn(res.getString("A.schedule_on"));
				routingDTO.setOldCost(res.getString("cost_old"));
				routingDTO.setCostStr(res.getString("cost_new"));
				routingDTO.setRemarks(remarks);
				List<RouteEntryExt> list = null;
				if (routing.containsKey(networkId)) {
					list = routing.get(networkId);
				} else {
					list = new ArrayList<RouteEntryExt>();
				}
				list.add(routingDTO);
				routing.put(networkId, list);
			}
		} catch (SQLException sqle) {
			throw new SQLException(sqle.getMessage());
		} finally {
			try {
				if (res != null) {
					res.close();
					res = null;
				}
				if (pStmt1 != null) {
					pStmt1.close();
					pStmt1 = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return routing;
	}

	public Map<Integer, Network> getNetworkRecord(Set<Integer> idSet) throws SQLException {
		Map<Integer, Network> networklist = new HashMap<Integer, Network>();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String ids = "";
		String sql = "";
		for (int next : idSet) {
			ids += next + ",";
		}
		if (ids.length() > 1) {
			ids = ids.substring(0, ids.length() - 1);
		}
		// logger.info("ID ====> " + ids);
		sql = "select * from network where id in(" + ids + ")";
		try {
			con = getConnection();
			pStmt = con.prepareStatement(sql);
			// pStmt.setString(1, ids);
			rs = pStmt.executeQuery();
			Network network = null;
			while (rs.next()) {
				network = new Network();
				network.setId(rs.getInt("id"));
				network.setCc(rs.getString("cc"));
				network.setMcc(rs.getString("mcc"));
				network.setMnc(rs.getString("mnc"));
				network.setCountry(rs.getString("country"));
				network.setOperator(rs.getString("operator"));
				networklist.put(rs.getInt("id"), network);
			}
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqle) {
				}
			}
			if (pStmt != null) {
				try {
					pStmt.close();
				} catch (SQLException sqle) {
				}
			}
			if (con != null) {
				con.close();
			}
		}
		return networklist;
	}

	public Map<Integer, RouteEntryExt> listCoverage(String systemId, boolean display, boolean cached) {
		User user = userEntryRepository.getUsers(systemId).get();
		int userId = user.getUserId();
		return listCoverage(userId, display, cached);
	}

	public Map<Integer, RouteEntryExt> listCoverage(int userId, boolean display, boolean cached) {
		Map<Integer, RouteEntryExt> list = new LinkedHashMap<Integer, RouteEntryExt>();
		Map<Integer, String> smsc_name_mapping = null;
		Map<Integer, String> group_name_mapping = null;
		if (display) {
			smsc_name_mapping = listNames();
			group_name_mapping = listGroupNames();
		}
		if (cached) {
			for (RouteEntry basic : routeEntryRepository.findAll()) {
				RouteEntryExt entry = new RouteEntryExt(basic);
				if (display) {
					Optional<UserEntry> userOptional = userEntryRepository.findById(basic.getUserId());
					// ------ set user values -----------------
					if (userOptional.isPresent()) {
						entry.setSystemId(userOptional.get().getSystemId());
						entry.setMasterId(userOptional.get().getMasterId());
						entry.setCurrency(userOptional.get().getCurrency());
						entry.setAccountType(masterEntryRepository.findByUserId(basic.getUserId()).getAccountType());
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
			List<RouteEntry> db_list = routeEntryRepository.findByUserId(userId);
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

	public Map<Integer, String> listNames() {
		Map<Integer, String> names = new HashMap<Integer, String>();
		List<SmscEntry> listSmsc = smscEntryRepository.findAll();
		for (SmscEntry entry : listSmsc) {
			names.put(entry.getId(), entry.getName());
		}
		names = names.entrySet().stream().sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return names;
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

	public boolean updateRoutingFlagSch(Map list) {
		boolean isUpdate = false;
		Connection con = null;
		String sql = "update routemaster_sch set flag=? where id=?";
		PreparedStatement pStmt = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(sql);
			con.setAutoCommit(false);
			Iterator itr = list.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry entry = (Map.Entry) itr.next();
				pStmt.setString(1, (String) entry.getValue());
				pStmt.setInt(2, (Integer) entry.getKey());
				pStmt.addBatch();
			}
			int[] executeBatch = pStmt.executeBatch();
			con.commit();
			if (executeBatch.length > 0) {
				isUpdate = true;
			}
			logger.info("Total Records Updated (Routing Flag) : " + executeBatch.length);
		} catch (SQLException sqle) {
			logger.error(" ", sqle.fillInStackTrace());
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
		return isUpdate;
	}

	public boolean updateRoutingFlag(Map list) {
		boolean isUpdate = false;
		Connection con = null;
		String sql = "update routemaster_updates set flag=? where id=?";
		PreparedStatement pStmt = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(sql);
			con.setAutoCommit(false);
			Iterator itr = list.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry entry = (Map.Entry) itr.next();
				pStmt.setString(1, (String) entry.getValue());
				pStmt.setInt(2, (Integer) entry.getKey());
				pStmt.addBatch();
			}
			int[] executeBatch = pStmt.executeBatch();
			con.commit();
			if (executeBatch.length > 0) {
				isUpdate = true;
			}
			logger.info("Total Records Updated (Routing Flag) : " + executeBatch.length);
		} catch (SQLException sqle) {
			logger.error(" ", sqle.fillInStackTrace());
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
		return isUpdate;
	}

<<<<<<< HEAD
	public List<WebMasterEntry> findWebMaster() {
		return masterEntryRepository.findByMinFlag(true).orElseGet(() -> {
			logger.info("Web master is null; find min flag returned null.");
			return Collections.emptyList();
		});
	}

	public List<WebMasterEntry> findAllWebMaster() {
		try {
			List<WebMasterEntry> webMasterEntries = masterEntryRepository.findAll();
			if (webMasterEntries == null) {
				logger.warn("findAllWebMaster: No web master entries found (result is null).");
				return Collections.emptyList();
			}
			return webMasterEntries;
		} catch (Exception e) {
			logger.error("Exception occurred in findAllWebMaster", e);
			return Collections.emptyList();
		}
	}

	public List<ProfessionEntry> professionData() {
		try {
			List<ProfessionEntry> entries = professionEntryRepository.findByDomainEmailIsNotNull();
			System.out.println(entries);
			if (entries == null) {
				logger.warn("professionData: No ProfessionEntry found (result is null).");
				return Collections.emptyList();
			}
			return entries;
		} catch (Exception e) {
			logger.error("Exception occurred in professionData", e);
			return Collections.emptyList();
		}
	}

	public List<WebMasterEntry> findAllById(Set<Integer> sentMinBalAlertEmail) {
		try {
			List<WebMasterEntry> entries = masterEntryRepository.findAllById(sentMinBalAlertEmail);
			if (entries == null) {
				logger.warn("findAllById: No WebMasterEntry found for provided IDs.");
				return Collections.emptyList();
			}
			return entries;
		} catch (Exception e) {
			logger.error("Exception occurred in findAllById", e);
			return Collections.emptyList();
		}
	}

	public List<WebMasterEntry> findDlrReportUsersWithValidEmail() {
		try {
			List<WebMasterEntry> entries = masterEntryRepository.findDlrReportUsersWithValidEmail();
			return Optional.ofNullable(entries).orElseGet(Collections::emptyList);
		} catch (Exception e) {
			logger.error("Exception occurred in findDlrReportUsersWithValidEmail", e);
			return Collections.emptyList();
		}
	}
=======
>>>>>>> 96401f1d1d1a31c5e1b73c83ac974f4359502342
}
