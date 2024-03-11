package com.hti.dao.impl;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.dao.UserDAService;
import com.hti.exception.EntryNotFoundException;
import com.hti.hlr.GlobalVar;
import com.hti.hlr.HlrRequest;
import com.hti.hlr.HlrRequestHandler;
import com.hti.hlr.RouteObject;
import com.hti.objects.HTIQueue;
import com.hti.user.RoutingThread;
import com.hti.user.SessionManager;
import com.hti.user.UserBalance;
import com.hti.user.UserDeliverForward;
import com.hti.user.dto.DlrSettingEntry;
import com.hti.user.dto.ProfessionEntry;
import com.hti.user.dto.SessionEntry;
import com.hti.user.dto.UserEntry;
import com.hti.util.FlagStatus;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;
import com.logica.smpp.Data;
import com.logica.smpp.util.Queue;

public class UserDAServiceImpl implements UserDAService {
	private static Logger logger = LoggerFactory.getLogger("ProcLogger");
	// ----------- Hlr Caches ---------------------
	private Map<String, Queue> HlrSubmitQueues = new HashMap<String, Queue>();
	private Map<String, Map<String, RouteObject>> HlrRespMappings = new HashMap<String, Map<String, RouteObject>>();

	public UserDAServiceImpl() {
		GlobalCache.SystemIdMapping = GlobalVars.hazelInstance.getMap("user_mapping");
		GlobalCache.UserEntries = GlobalVars.hazelInstance.getMap("user_entries");
		GlobalCache.BalanceEntries = GlobalVars.hazelInstance.getMap("balance_entries");
		GlobalCache.DlrSettingEntries = GlobalVars.hazelInstance.getMap("dlrSetting_entries");
		GlobalCache.ProfessionEntries = GlobalVars.hazelInstance.getMap("profession_entries");
		logger.info("User Entries: " + GlobalCache.UserEntries.size());
	}

	@Override
	public UserEntry getUserEntry(int user_id) {
		if (GlobalCache.UserEntries.containsKey(user_id)) {
			return GlobalCache.UserEntries.get(user_id);
		} else {
			return null;
		}
	}

	@Override
	public UserEntry getUserEntry(String system_id) {
		if (GlobalCache.SystemIdMapping.containsKey(system_id)) {
			int user_id = GlobalCache.SystemIdMapping.get(system_id);
			return getUserEntry(user_id);
		} else {
			return null;
		}
	}

	@Override
	public UserEntry getInternalUser() {
		Predicate<Integer, UserEntry> predicate = new PredicateBuilderImpl().getEntryObject().get("role")
				.equal("internal");
		for (UserEntry entry : GlobalCache.UserEntries.values(predicate)) {
			return entry;
		}
		return null;
	}

	@Override
	public DlrSettingEntry getDlrSettingEntry(int user_id) {
		if (GlobalCache.DlrSettingEntries.containsKey(user_id)) {
			return GlobalCache.DlrSettingEntries.get(user_id);
		} else {
			return null;
		}
	}

	@Override
	public DlrSettingEntry getDlrSettingEntry(String system_id) {
		if (GlobalCache.SystemIdMapping.containsKey(system_id)) {
			int user_id = GlobalCache.SystemIdMapping.get(system_id);
			DlrSettingEntry entry = getDlrSettingEntry(user_id);
			if (entry != null) {
				return entry;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public void initializeStatus() {
		initHlrPendings();
		Map<String, String> UserFlagStatus = GlobalVars.hazelInstance.getMap("user_flag_status");
		for (Entry<String, String> map_entry : UserFlagStatus.entrySet()) {
			String system_id = map_entry.getKey();
			try {
				String flag = map_entry.getValue();
				if (flag != null && flag.equalsIgnoreCase(FlagStatus.BLOCKED)) {
					GlobalCache.BlockedUser.add(system_id);
					logger.info("Blocked User -> " + system_id);
				} else {
					UserEntry entry = getUserEntry(system_id);
					if (entry.isHlr()) {
						initHlrHandler(system_id, entry.getPassword());
					}
				}
			} catch (Exception e) {
				logger.error(system_id, e);
			}
		}
		HlrSubmitQueues.clear();
		HlrRespMappings.clear();
	}

	@Override
	public void reloadUserFlagStatus() {
		logger.info("******** Start Reload User Flag Status *****");
		try {
			Map<String, Set<String>> ParentAdmin = new HashMap<String, Set<String>>();
			Predicate<Integer, UserEntry> predicate = new PredicateBuilderImpl().getEntryObject().is("adminDepend");
			for (UserEntry entry : GlobalCache.UserEntries.values(predicate)) {
				Set<String> childs = null;
				if (ParentAdmin.containsKey(entry.getMasterId())) {
					childs = ParentAdmin.get(entry.getMasterId());
				} else {
					childs = new HashSet<String>();
				}
				childs.add(entry.getSystemId());
				ParentAdmin.put(entry.getMasterId(), childs);
			}
			logger.info("ParentAdmin: " + ParentAdmin);
			Map<String, String> UserFlagStatus = GlobalVars.hazelInstance.getMap("user_flag_status");
			for (Entry<String, String> map_entry : UserFlagStatus.entrySet()) {
				try {
					String system_id = map_entry.getKey();
					String flag = UserFlagStatus.get(system_id);
					if (flag.equalsIgnoreCase(FlagStatus.ACCOUNT_REMOVED)
							|| flag.equalsIgnoreCase(FlagStatus.BLOCKED)) {
						if (flag.equalsIgnoreCase(FlagStatus.BLOCKED)) {
							if (!GlobalCache.BlockedUser.contains(system_id)) {
								GlobalCache.BlockedUser.add(system_id);
								logger.info(" Blocked User <404> " + system_id);
							}
						} else {
							logger.info(" Removed User <303> " + system_id);
						}
						Set<String> to_be_blocked = null;
						if (ParentAdmin.containsKey(system_id)) {
							to_be_blocked = ParentAdmin.get(system_id);
						} else {
							to_be_blocked = new HashSet<String>();
						}
						to_be_blocked.add(system_id);
						for (String block_user : to_be_blocked) {
							if (GlobalCache.UserSessionObject.containsKey(block_user)) {
								((SessionManager) GlobalCache.UserSessionObject.get(block_user)).block();
							} else {
								logger.info(block_user + " No Active Session Found");
							}
							// ----------- stop loolkup session ----------------
							logger.info(block_user + " Checking For HLR sessions to Block");
							if (GlobalVar.HlrRequestHandlers.containsKey(block_user)) {
								logger.info(block_user + " HLR RequestHandler removing");
								GlobalVar.HlrRequestHandlers.remove(block_user).stop();
							} else {
								logger.info(block_user + " No HLR sessions to Block");
							}
							// --------- remove balance object -------------
							if (GlobalCache.UserBalanceObject.containsKey(block_user)) {
								logger.info(block_user + " Removing User Balance Object");
								UserBalance balance = GlobalCache.UserBalanceObject.remove(block_user);
								try {
									balance.setActive(false);
								} catch (EntryNotFoundException ne) {
									logger.info(block_user + " BalanceEntry Not Found");
								}
							}
							// ------- remove web dlr process ---------------
							if (GlobalCache.WebDeliverProcessQueue.containsKey(block_user)) {
								GlobalCache.WebDeliverProcessQueue.remove(block_user);
							}
							if (GlobalCache.UserWebObject.containsKey(block_user)) {
								(GlobalCache.UserWebObject.remove(block_user)).stop();
							}
						}
					} else {
						if (flag.equalsIgnoreCase(FlagStatus.ACCOUNT_REFRESH)) {
							UserEntry entry = getUserEntry(system_id);
							if (entry != null) {
								DlrSettingEntry dlr_entry = getDlrSettingEntry(system_id);
								if (GlobalCache.UserSessionObject.containsKey(system_id)) {
									((SessionManager) GlobalCache.UserSessionObject.get(system_id)).setUserEntry(entry);
								}
								if (GlobalCache.UserRoutingThread.containsKey(system_id)) {
									((RoutingThread) GlobalCache.UserRoutingThread.get(system_id)).setUserEntry(entry);
									((RoutingThread) GlobalCache.UserRoutingThread.get(system_id))
											.setDlrSettingEntry(dlr_entry);
								}
								logger.info(system_id + " Checking For HLR sessions to Account refresh");
								if (entry.isHlr()) {
									logger.info(system_id + " HLR is Enabled");
									if (GlobalVar.HlrRequestHandlers.containsKey(system_id)) {
										GlobalVar.HlrRequestHandlers.get(system_id).refreshUser();
									}
								} else {
									logger.info(system_id + " HLR is Disabled");
									if (GlobalVar.HlrRequestHandlers.containsKey(system_id)) {
										logger.info(system_id + " HLR RequestHandler removing");
										GlobalVar.HlrRequestHandlers.remove(system_id).stop();
									} else {
										logger.info(system_id + " No Active HLR session");
									}
								}
								if (dlr_entry.isWebDlr()) {
									if (GlobalCache.UserWebObject.containsKey(system_id)) {
										(GlobalCache.UserWebObject.get(system_id)).setWebUrl(dlr_entry.getWebUrl());
									}
								} else {
									if (GlobalCache.WebDeliverProcessQueue.containsKey(system_id)) {
										GlobalCache.WebDeliverProcessQueue.remove(system_id);
									}
									if (GlobalCache.UserWebObject.containsKey(system_id)) {
										(GlobalCache.UserWebObject.remove(system_id)).stop();
									}
								}
							}
						} else if (flag.equalsIgnoreCase(FlagStatus.BALANCE_REFRESH)) {
							logger.info(" Balance Refreshed <" + FlagStatus.BALANCE_REFRESH + "> " + system_id);
							if (GlobalCache.LowBalanceUser.contains(system_id)) {
								GlobalCache.LowBalanceUser.remove(system_id);
							}
						} else if (flag.equalsIgnoreCase(FlagStatus.REFRESH)) {
							logger.info(" Routing Refreshed <707> " + system_id);
							if (GlobalCache.UserRoutingThread.containsKey(system_id)) {
								((RoutingThread) GlobalCache.UserRoutingThread.get(system_id)).refresh();
							}
							if (GlobalCache.UserSessionObject.containsKey(system_id)) {
								((SessionManager) GlobalCache.UserSessionObject.get(system_id)).refresh();
							}
							if (GlobalVar.HlrRequestHandlers.containsKey(system_id)) {
								HlrRequestHandler entry = GlobalVar.HlrRequestHandlers.get(system_id);
								entry.refresh();
							}
							if (ParentAdmin.containsKey(system_id)) {
								Set<String> to_be_refresh = ParentAdmin.get(system_id);
								for (String child : to_be_refresh) {
									if (GlobalCache.UserSessionObject.containsKey(child)) {
										((SessionManager) GlobalCache.UserSessionObject.get(child)).refreshAdmin();
									}
								}
							}
						}
						if (GlobalCache.BlockedUser.contains(system_id)) {
							checkHlrHandler(getUserEntry(system_id));
							GlobalCache.BlockedUser.remove(system_id);
						}
						// GlobalCache.UserFlagStatus.put(system_id, FlagStatus.DEFAULT);
					}
				} catch (Exception e) {
					logger.error(map_entry.getKey(), e.fillInStackTrace());
				}
			}
		} catch (Exception e) {
			logger.error("2", e.fillInStackTrace());
		}
		logger.info("******** End Reload User Flag Status *****");
	}

	@Override
	public synchronized RoutingThread getRoutingThread(String systemId) throws Exception {
		RoutingThread routingThread = null;
		if (GlobalCache.UserRoutingThread.containsKey(systemId)) {
			routingThread = GlobalCache.UserRoutingThread.get(systemId);
		} else {
			routingThread = new RoutingThread(new HTIQueue(), systemId);
			GlobalCache.UserRoutingThread.put(systemId, routingThread);
			new Thread(routingThread, "RoutingThread_" + systemId).start();
		}
		return routingThread;
	}

	@Override
	public synchronized SessionManager getSessionManager(String systemId) {
		SessionManager userSession = null;
		if (GlobalCache.UserSessionObject.containsKey(systemId)) {
			userSession = GlobalCache.UserSessionObject.get(systemId);
		} else {
			userSession = new SessionManager(systemId);
			GlobalCache.UserSessionObject.put(systemId, userSession);
		}
		return userSession;
	}

	@Override
	public synchronized UserBalance getUserBalance(int user_id) throws EntryNotFoundException {
		if (GlobalCache.UserBalanceObject.containsKey(user_id)) {
			return GlobalCache.UserBalanceObject.get(user_id);
		} else {
			if (GlobalCache.BalanceEntries.containsKey(user_id)) {
				UserBalance userBalance = new UserBalance(user_id);
				GlobalCache.UserBalanceObject.put(user_id, userBalance);
				return userBalance;
			} else {
				logger.error(user_id + " <Balance Entry Not Found >");
				return null;
			}
		}
	}

	@Override
	public synchronized UserBalance getUserBalance(String systemId) throws EntryNotFoundException {
		if (GlobalCache.SystemIdMapping.containsKey(systemId)) {
			return getUserBalance(GlobalCache.SystemIdMapping.get(systemId));
		} else {
			return null;
		}
	}

	@Override
	public synchronized UserDeliverForward checkDeliverForward(String systemId) {
		UserDeliverForward userDeliverForward = null;
		HTIQueue dlrQueue = null;
		if (GlobalCache.UserDeliverProcessQueue.containsKey(systemId)) {
			dlrQueue = (HTIQueue) GlobalCache.UserDeliverProcessQueue.get(systemId);
		} else {
			dlrQueue = new HTIQueue();
			GlobalCache.UserDeliverProcessQueue.put(systemId, dlrQueue);
		}
		if (!GlobalCache.UserRxObject.containsKey(systemId)) {
			userDeliverForward = new UserDeliverForward(systemId, dlrQueue);
			GlobalCache.UserRxObject.put(systemId, userDeliverForward);
		} else {
			userDeliverForward = (UserDeliverForward) GlobalCache.UserRxObject.get(systemId);
		}
		return userDeliverForward;
	}

	@Override
	public Collection<UserEntry> listUserEntries() {
		return GlobalCache.UserEntries.values();
	}
	// ----------- pendings to proceed ------------------

	private void initHlrPendings() {
		try {
			Map<String, List<RouteObject>> submitPendings = loadHlrSubmitPendings();
			Map<String, Map<String, String>> respPendings = loadHlrRespPendings();
			for (Map.Entry<String, List<RouteObject>> entry : submitPendings.entrySet()) {
				String username = entry.getKey();
				List<RouteObject> submit_list = entry.getValue();
				Map<String, String> resp_list = null;
				if (respPendings.containsKey(username)) {
					resp_list = respPendings.get(username);
				}
				logger.info(username + " Checking For Hlr Submission");
				for (RouteObject route : submit_list) {
					String messageid = route.getMsgId();
					if (resp_list != null && resp_list.containsKey(messageid)) {
						logger.debug(username + " [" + messageid + "] Already Submitted.");
						String resp_id = resp_list.get(messageid);
						Map<String, RouteObject> respMapping = null;
						if (HlrRespMappings.containsKey(username)) {
							respMapping = HlrRespMappings.get(username);
						} else {
							respMapping = new HashMap<String, RouteObject>();
						}
						respMapping.put(resp_id, route);
						HlrRespMappings.put(username, respMapping);
					} else {
						// put to hlrQueue for submission
						Queue hlrQueue = null;
						if (HlrSubmitQueues.containsKey(username)) {
							hlrQueue = HlrSubmitQueues.get(username);
						} else {
							hlrQueue = new Queue();
							HlrSubmitQueues.put(username, hlrQueue);
						}
						hlrQueue.enqueue(route);
					}
				}
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private void initHlrHandler(String systemId, String password) {
		try {
			if (HlrSubmitQueues.containsKey(systemId) || HlrRespMappings.containsKey(systemId)) {
				Queue hlrQueue = null;
				if (HlrSubmitQueues.containsKey(systemId)) {
					hlrQueue = HlrSubmitQueues.get(systemId);
					logger.info(systemId + " HlrQueue: " + hlrQueue.size());
				} else {
					hlrQueue = new Queue();
				}
				if (HlrRespMappings.containsKey(systemId)) {
					for (Map.Entry<String, RouteObject> entry : HlrRespMappings.get(systemId).entrySet()) {
						GlobalVar.HlrResponseMapping.put(entry.getKey(), entry.getValue().getMsgId());
						GlobalVar.EnqueueRouteObject.put(entry.getValue().getMsgId(), entry.getValue());
					}
				}
				HlrRequestHandler hlrRequestHandler = new HlrRequestHandler(systemId, password);
				hlrRequestHandler.setHlrQueue(hlrQueue);
				GlobalVar.HlrRequestHandlers.put(systemId, hlrRequestHandler);
				hlrRequestHandler.startHandler();
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private void checkHlrHandler(UserEntry user) {
		try {
			HlrRequestHandler hlrRequestHandler = null;
			if (user.isHlr()) {
				if (!GlobalVar.HlrRequestHandlers.containsKey(user.getSystemId())) {
					logger.info(user.getSystemId() + " Hlr Service Enabled");
					// ------ initialize submit & response cache -------
					Queue hlrSubmitQueue = new Queue();
					Map<String, HlrRequest> respMapping = new HashMap<String, HlrRequest>();
					List<RouteObject> submitPendings = loadHlrSubmitPendings(user.getSystemId());
					Map<String, String> respPendings = loadHlrRespPendings(user.getSystemId());
					if (!submitPendings.isEmpty() || !respMapping.isEmpty()) {
						for (RouteObject route : submitPendings) {
							String messageid = route.getMsgId();
							if (respPendings.containsKey(messageid)) {
								logger.debug(user.getSystemId() + " [" + messageid + "] Already Submitted.");
								String resp_id = respPendings.get(messageid);
								// --------- put to response mapping ----------
								GlobalVar.HlrResponseMapping.put(resp_id, messageid);
								GlobalVar.EnqueueRouteObject.put(messageid, route);
							} else {
								// put to hlrQueue for submission
								hlrSubmitQueue.enqueue(route);
							}
						}
						// ------- manipulate part mapping ---------------
						if (hlrSubmitQueue.size() > 0 || respMapping.size() > 0) {
							hlrRequestHandler = new HlrRequestHandler(user.getSystemId(), user.getPassword());
							hlrRequestHandler.setHlrQueue(hlrSubmitQueue);
							GlobalVar.HlrRequestHandlers.put(user.getSystemId(), hlrRequestHandler);
							logger.info(user.getSystemId() + " HlrQueue: " + hlrSubmitQueue.size() + " ResponseCache: "
									+ respMapping.size());
							hlrRequestHandler.startHandler();
						} else {
							logger.info(user.getSystemId() + " HlrRequestHandler Not Created.");
						}
					}
				}
			} else {
				if (GlobalVar.HlrRequestHandlers.containsKey(user.getSystemId())) {
					logger.info(user.getSystemId() + " Hlr Service Disabled");
					hlrRequestHandler = GlobalVar.HlrRequestHandlers.remove(user.getSystemId());
					hlrRequestHandler.stop();
				}
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private Map<String, List<RouteObject>> loadHlrSubmitPendings() {
		logger.info("Checking For Lookup Submit Pendings");
		Statement statement = null;
		ResultSet rs = null;
		String sql = null;
		Connection con = null;
		Map<String, List<RouteObject>> userWiseSubmitPending = new HashMap<String, List<RouteObject>>();
		sql = "select * from smsc_in_temp where s_flag='H' and server_id=" + GlobalVars.SERVER_ID
				+ " order by username,msg_id,destination_no";
		try {
			con = GlobalCache.connection_pool_user.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				String msg_id = rs.getString("msg_id");
				String destAddress = rs.getString("destination_no");
				String sourceAddress = rs.getString("source_no");
				double cost = rs.getDouble("cost");
				String smsc = rs.getString("smsc");
				int groupId = rs.getInt("group_id");
				String username = rs.getString("username");
				int esm_class = Integer.parseInt(rs.getString("esm_class"));
				int dcs = Integer.parseInt(rs.getString("dcs"));
				int part_number = 0;
				if (esm_class == 64 || esm_class == 67) { // multipart
					// System.out.println(msg_id + " : " + rs.getString("content"));
					if (dcs == 8) {
						part_number = getPartNumber(rs.getString("content"));
					} else {
						part_number = getPartNumber(getHexDump(rs.getString("content")));
					}
				}
				int network_id = 0;
				try {
					network_id = Integer.parseInt(rs.getString("oprCountry"));
				} catch (Exception ex) {
					logger.error(msg_id + " " + rs.getString("oprCountry"), ex);
				}
				List<RouteObject> pdu_list = null;
				if (userWiseSubmitPending.containsKey(username)) {
					pdu_list = userWiseSubmitPending.get(username);
				} else {
					pdu_list = new ArrayList<RouteObject>();
				}
				pdu_list.add(new RouteObject(msg_id, smsc, groupId, cost, part_number, sourceAddress, destAddress,
						false, false, network_id,false));
				userWiseSubmitPending.put(username, pdu_list);
			}
		} catch (Exception ex) {
			logger.error("loadHlrSubmitPendings()", ex.fillInStackTrace());
		} finally {
			GlobalCache.connection_pool_user.putConnection(con);
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
				}
			}
		}
		logger.info("Lookup Submit Pendings Users:" + userWiseSubmitPending.size());
		return userWiseSubmitPending;
	}

	public void initializeUserBsfm() {
		GlobalCache.UserBasedBsfm.clear();
		Statement statement = null;
		ResultSet rs = null;
		String sql = "select user_id from bsfm_user where scan_url=true";
		Connection con = null;
		try {
			con = GlobalCache.connection_pool_user.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				GlobalCache.UserBasedBsfm.add(rs.getInt("user_id"));
			}
		} catch (Exception ex) {
			logger.error("initializeUserBsfm()", ex.fillInStackTrace());
		} finally {
			GlobalCache.connection_pool_user.putConnection(con);
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
				}
			}
		}
		logger.info("User Based Bsfm Users: " + GlobalCache.UserBasedBsfm);
	}

	public String getCountryname(String ip_address) {
		String country = null;
		Statement statement = null;
		ResultSet rs = null;
		String sql = "SELECT ip_location.country_name FROM ip_blocks JOIN ip_location ON ip_blocks.geoname_id = ip_location.geoname_id WHERE INET_ATON('"
				+ ip_address + "') BETWEEN ip_blocks.ip_from AND ip_blocks.ip_to";
		Connection con = null;
		try {
			con = GlobalCache.connection_pool_user.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			if (rs.next()) {
				country = rs.getString("country_name");
			}
		} catch (Exception ex) {
			logger.error("getCountryname()", ex.fillInStackTrace());
		} finally {
			GlobalCache.connection_pool_user.putConnection(con);
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
				}
			}
		}
		return country;
	}

	public String getWebDlrParam(String msg_id) {
		logger.info("Checking For Http Dlr Param: " + msg_id);
		String param = null;
		Statement statement = null;
		ResultSet rs = null;
		String sql = "SELECT param_name,param_value from http_dlr_param where msg_id = " + msg_id;
		Connection con = null;
		try {
			con = GlobalCache.connection_pool_user.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			if (rs.next()) {
				if (rs.getString("param_name") != null && rs.getString("param_value") != null) {
					param = rs.getString("param_name") + "=" + rs.getString("param_value");
				}
			}
		} catch (Exception ex) {
			logger.error("getWebDlrParam()", ex.fillInStackTrace());
		} finally {
			GlobalCache.connection_pool_user.putConnection(con);
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
				}
			}
		}
		return param;
	}

	private List<RouteObject> loadHlrSubmitPendings(String systemId) {
		logger.info("Checking For Lookup Submit Pendings[" + systemId + "]");
		Statement statement = null;
		ResultSet rs = null;
		String sql = null;
		Connection con = null;
		List<RouteObject> pduList = new ArrayList<RouteObject>();
		sql = "select msg_id,destination_no,source_no,smsc,group_id,cost,esm_class,content,dcs from smsc_in_temp where server_id = "
				+ GlobalVars.SERVER_ID + " and s_flag='H' and username='" + systemId
				+ "' order by msg_id,destination_no";
		try {
			con = GlobalCache.connection_pool_user.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				String msg_id = rs.getString("msg_id");
				int network_id = 0;
				try {
					network_id = Integer.parseInt(rs.getString("oprCountry"));
				} catch (Exception ex) {
					logger.error(msg_id + " " + rs.getString("oprCountry"), ex);
				}
				String destAddress = rs.getString("destination_no");
				String sourceAddress = rs.getString("source_no");
				double cost = rs.getDouble("cost");
				String smsc = rs.getString("smsc");
				int groupId = rs.getInt("group_id");
				int esm_class = Integer.parseInt(rs.getString("esm_class"));
				int dcs = Integer.parseInt(rs.getString("dcs"));
				int part_number = 0;
				if (esm_class == 64 || esm_class == 67) { // multipart
					// System.out.println(msg_id + " : " + rs.getString("content"));
					if (dcs == 8) {
						part_number = getPartNumber(rs.getString("content"));
					} else {
						part_number = getPartNumber(getHexDump(rs.getString("content")));
					}
				}
				pduList.add(new RouteObject(msg_id, smsc, groupId, cost, part_number, sourceAddress, destAddress, false,
						false, network_id, false));
			}
		} catch (Exception ex) {
			logger.error("loadHlrSubmitPendings(" + systemId + ")", ex.fillInStackTrace());
		} finally {
			GlobalCache.connection_pool_user.putConnection(con);
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
				}
			}
		}
		logger.info(systemId + " Lookup Submit Pendings:" + pduList.size());
		return pduList;
	}

	private Map<String, Map<String, String>> loadHlrRespPendings() {
		logger.info("Checking For Lookup Response Pendings");
		Statement statement = null;
		ResultSet rs = null;
		Connection con = null;
		Map<String, Map<String, String>> userRespPendings = new HashMap<String, Map<String, String>>();
		String sql = "select msg_id,hlr_id,username from lookup_status where server_id = " + GlobalVars.SERVER_ID
				+ " and flag = 'S' order by username";
		try {
			con = GlobalCache.connection_pool_user.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				String msgid = rs.getString("msg_id");
				String username = rs.getString("username");
				String resp_id = rs.getString("hlr_id");
				Map<String, String> list = null;
				if (userRespPendings.containsKey(username)) {
					list = userRespPendings.get(username);
				} else {
					list = new HashMap<String, String>();
				}
				list.put(msgid, resp_id);
				userRespPendings.put(username, list);
			}
		} catch (Exception ex) {
			logger.error("loadHlrRespPendings()", ex.fillInStackTrace());
		} finally {
			GlobalCache.connection_pool_user.putConnection(con);
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
				}
			}
		}
		logger.info("Lookup Response Waiting Users: " + userRespPendings.size());
		return userRespPendings;
	}

	private Map<String, String> loadHlrRespPendings(String systemId) {
		logger.info("Checking For Lookup Response Pendings[" + systemId + "]");
		Statement statement = null;
		ResultSet rs = null;
		Connection con = null;
		Map<String, String> respPendings = new HashMap<String, String>();
		String sql = "select msg_id,hlr_id from lookup_status where server_id = " + GlobalVars.SERVER_ID
				+ " and username='" + systemId + "' and flag='S'";
		try {
			con = GlobalCache.connection_pool_user.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				String msgid = rs.getString("msg_id");
				String resp_id = rs.getString("hlr_id");
				respPendings.put(msgid, resp_id);
			}
		} catch (Exception ex) {
			logger.error("loadHlrRespPendings(" + systemId + ")", ex.fillInStackTrace());
		} finally {
			GlobalCache.connection_pool_user.putConnection(con);
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
				}
			}
		}
		logger.info(systemId + " Lookup Response Waiting: " + respPendings.size());
		return respPendings;
	}

	@Override
	public void updateSession(SessionEntry entry) {
		logger.debug("update: " + entry);
		Connection connection = null;
		PreparedStatement stmt = null;
		String sql = "insert into user_session(system_id,server_id,request_ip,total,rx,tx,trx) values(?,?,?,?,?,?,?) on duplicate key update total=?,rx=?,tx=?,trx=?";
		try {
			connection = GlobalCache.connection_pool_user.getConnection();
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, entry.getSystemId());
			stmt.setInt(2, entry.getServerId());
			stmt.setString(3, entry.getRequestIp());
			stmt.setInt(4, entry.getSessionCount());
			stmt.setInt(5, entry.getReceiver());
			stmt.setInt(6, entry.getTransmitter());
			stmt.setInt(7, entry.getTranciever());
			stmt.setInt(8, entry.getSessionCount());
			stmt.setInt(9, entry.getReceiver());
			stmt.setInt(10, entry.getTransmitter());
			stmt.setInt(11, entry.getTranciever());
			stmt.executeUpdate();
		} catch (Exception ex) {
			logger.error(entry.toString(), ex);
		} finally {
			GlobalCache.connection_pool_user.putConnection(connection);
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ex) {
				}
			}
		}
	}

	@Override
	public void updateBindError(SessionEntry entry) {
		logger.info("update: " + entry);
		Connection connection = null;
		PreparedStatement stmt = null;
		String sql = "insert into user_bind_error(system_id,server_id,request_ip,status_code) values(?,?,?,?) on duplicate key update request_ip=?,status_code=?,updateOn=?";
		try {
			connection = GlobalCache.connection_pool_user.getConnection();
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, entry.getSystemId());
			stmt.setInt(2, entry.getServerId());
			stmt.setString(3, entry.getRequestIp());
			stmt.setInt(4, entry.getCommandStatus());
			stmt.setString(5, entry.getRequestIp());
			stmt.setInt(6, entry.getCommandStatus());
			stmt.setString(7, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			stmt.executeUpdate();
		} catch (Exception ex) {
			logger.error(entry.toString(), ex);
		} finally {
			GlobalCache.connection_pool_user.putConnection(connection);
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ex) {
				}
			}
		}
	}

	private String getHexDump(String getString) throws UnsupportedEncodingException {
		String dump = "";
		byte[] buffer = getString.getBytes(Data.ENC_UTF16_BE);
		for (int i = 0; i < buffer.length; i++) {
			dump += Character.forDigit((buffer[i] >> 4) & 0x0f, 16);
			dump += Character.forDigit(buffer[i] & 0x0f, 16);
		}
		// System.out.println("hexdump:" + dump);
		buffer = null;
		return dump;
	}

	private int getPartNumber(String hex_dump) {
		int part_number = 0;
		try {
			int header_length = Integer.parseInt(hex_dump.substring(0, 2));
			// System.out.println("Header Length:" + header_length);
			if (header_length == 0) {
				header_length = Integer.parseInt(hex_dump.substring(0, 4));
				if (header_length == 5) {
					try {
						part_number = Integer.parseInt(hex_dump.substring(20, 24));
					} catch (Exception ex) {
					}
				} else if (header_length == 6) {
					try {
						part_number = Integer.parseInt(hex_dump.substring(24, 28));
					} catch (Exception ex) {
					}
				} else {
					System.out.println("Unknown Header Found:" + hex_dump.substring(0, 14));
				}
			} else {
				if (header_length == 5) {
					try {
						part_number = Integer.parseInt(hex_dump.substring(10, 12));
					} catch (Exception ex) {
					}
				} else if (header_length == 6) {
					try {
						part_number = Integer.parseInt(hex_dump.substring(12, 14));
					} catch (Exception ex) {
					}
				} else {
					System.out.println("Unknown Header Found:" + hex_dump.substring(0, 14));
				}
			}
		} catch (Exception une) {
		}
		// System.out.println("part_number: " + part_number);
		return part_number;
	}

	@Override
	public ProfessionEntry getProfessionEntry(String systemId) {
		if (GlobalCache.SystemIdMapping.containsKey(systemId)) {
			int user_id = GlobalCache.SystemIdMapping.get(systemId);
			return GlobalCache.ProfessionEntries.get(user_id);
		} else {
			return null;
		}
	}
}
