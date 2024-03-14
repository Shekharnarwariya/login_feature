package com.hti.smpp.common.httpclient;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.util.GlobalVars;

@Service
public class IDatabaseService {

	private ConnectionService dataSource;

	private final Logger logger = LoggerFactory.getLogger(IDatabaseService.class);

	public IDatabaseService() {
		dataSource = new ConnectionService();
	}

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	public int removeApiStatus(String max_web_id, int limit) {
		logger.info("Remove ApiStatus Entries: " + max_web_id);
		int count = 0;
		String query = "delete from api_status where msg_id < " + max_web_id + " limit " + limit;
		Connection con = null;
		PreparedStatement pStmt = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(query);
			count = pStmt.executeUpdate();
			logger.info("ApiStatus deleted entries: " + count);
		} catch (SQLException sqle) {
			logger.error(max_web_id, sqle.fillInStackTrace());
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
		return count;
	}

	public void putApiStatus(ApiRequestDTO requestDTO) {
		String sql = "INSERT INTO api_status(web_id, status_code, msg_id, destination, username) VALUES (?, ?, ?, ?, ?)";
		Connection con = null;

		try {
			con = getConnection();
			con.setAutoCommit(false);
			try (PreparedStatement pStmt = con.prepareStatement(sql)) {
				for (String map : requestDTO.getResponselist()) {
					String[] str = map.split(",");
					pStmt.setString(1, requestDTO.getWebid());
					pStmt.setString(2, str[0]);
					pStmt.setString(3, str[1]);
					pStmt.setString(4, str[2]);
					pStmt.setString(5, requestDTO.getUsername());
					pStmt.addBatch();
				}

				int[] count = pStmt.executeBatch();
				con.commit();
				logger.info(requestDTO.getWebid() + " Api Status Insert Count: " + count.length);
			} catch (SQLException sqle) {
				logger.error("Error inserting API status for web_id: " + requestDTO.getWebid(), sqle);
				if (con != null) {
					try {
						con.rollback();
					} catch (SQLException ex) {
						logger.error("Error rolling back transaction", ex);
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error obtaining database connection or rolling back transaction for web_id: "
					+ requestDTO.getWebid(), ex);
		} finally {
			if (con != null) {
				try {
					con.setAutoCommit(true);
					con.close();
				} catch (SQLException e) {
					logger.error("Error closing database connection", e);
				}
			}
		}
	}

	public List<BaseApiDTO> listApiSchedule() {
		logger.info("Checking For Api Schedules");
		List<BaseApiDTO> list = new ArrayList<>();
		String query = "SELECT * FROM api_schedule";
		try (Connection con = getConnection();
				PreparedStatement pStmt = con.prepareStatement(query);
				ResultSet rs = pStmt.executeQuery()) {

			while (rs.next()) {
				BaseApiDTO entry = new BaseApiDTO();
				entry.setScheduleId(rs.getInt("id"));
				entry.setServerScheduleTime(rs.getString("scheduled_on"));
				entry.setUsername(rs.getString("system_id"));
				entry.setScheduleFile(rs.getString("filename"));
				list.add(entry);
			}

			logger.info("Total Api Scheduled: " + list.size());
		} catch (SQLException sqle) {
			logger.error("Error listing API schedules", sqle);
		}
		return list;
	}

	public boolean removeApiSchedule(int scheduleId) {
		logger.info("Removing Api Schedule: " + scheduleId);
		boolean result = false;
		String query = "DELETE FROM api_schedule WHERE id = ?";
		try (Connection con = getConnection(); PreparedStatement pStmt = con.prepareStatement(query)) {

			pStmt.setInt(1, scheduleId);
			int count = pStmt.executeUpdate();

			if (count > 0) {
				logger.info("Schedule Deleted: " + scheduleId);
				result = true;
			} else {
				logger.info("Schedule Not Found: " + scheduleId);
			}
		} catch (SQLException sqle) {
			logger.error("Error removing API schedule", sqle);
			throw new RuntimeException("ERROR: removeApiSchedule(" + scheduleId + ") - " + sqle.getMessage(), sqle);
		}

		return result;
	}

	public int removeHttpDlrParamLog(String sql) {
		logger.info("Remove Http Dlr Param Entries: " + sql);
		int count = 0;
		try (Connection con = getConnection(); PreparedStatement pStmt = con.prepareStatement(sql)) {

			count = pStmt.executeUpdate();
			logger.info("Http Dlr Param deleted entries: " + count);
		} catch (SQLException sqle) {
			logger.error("Error removing Http Dlr Param entries", sqle);
		}
		return count;
	}

	public int saveHttpDlrParamLog(List<HttpDlrParamEntry> entries) throws SQLException {
		int insertCounter = 0;
		String sql = "INSERT IGNORE INTO http_dlr_param (msg_id, param_name, param_value) VALUES (?, ?, ?)";
		try (Connection con = getConnection(); PreparedStatement pStmt = con.prepareStatement(sql)) {

			con.setAutoCommit(false);

			for (HttpDlrParamEntry entry : entries) {
				pStmt.setString(1, entry.getMsgId());
				pStmt.setString(2, entry.getParamName());
				pStmt.setString(3, entry.getParamValue());
				pStmt.addBatch();
			}

			int[] updateCounts = pStmt.executeBatch();
			con.commit();
			insertCounter += updateCounts.length;
			logger.info("HttpDlrParamEntry insert counter: " + insertCounter);
		} catch (SQLException e) {
			logger.error("Error saving Http Dlr Param Log", e);
			throw e;
		}

		return insertCounter;
	}

	public int removeHttpRequestLog(String sql) {
		logger.info("Remove Http Request Log Entries: " + sql);
		int count = 0;
		try (Connection con = getConnection(); PreparedStatement pStmt = con.prepareStatement(sql)) {

			count = pStmt.executeUpdate();
			logger.info("Http Request Log deleted entries: " + count);
		} catch (SQLException sqle) {
			logger.error("Error removing Http Request Log entries", sqle);
		}
		return count;
	}

	public int saveHttpRequestLog(List<HttpRequestEntry> list) throws SQLException {
		int insertCounter = 0;
		String sql = "INSERT IGNORE INTO http_request_log (request_ip, received_time, request_url, request_method) VALUES (?, ?, ?, ?)";
		try (Connection con = getConnection(); PreparedStatement pStmt = con.prepareStatement(sql)) {
			con.setAutoCommit(false);
			for (HttpRequestEntry entry : list) {
				pStmt.setString(1, entry.getRequestIp());
				pStmt.setString(2, entry.getReceivedTime());
				pStmt.setString(3, entry.getRequestUrl());
				pStmt.setString(4, entry.getRequestMethod());
				pStmt.addBatch();
			}
			int[] updateCounts = pStmt.executeBatch();
			con.commit();
			insertCounter = updateCounts.length;
			logger.info("HttpRequestLog insert counter: " + insertCounter);
		} catch (SQLException e) {
			throw new SQLException("saveHttpRequestLog Error", e);
		}
		return insertCounter;
	}

	public List<ApiResultDTO> getApiStatus(String webId, String username) {
		List<ApiResultDTO> result = new ArrayList<>();
		String sql = "SELECT * FROM api_status WHERE web_id = ? AND username = ?";
		try (Connection con = getConnection(); PreparedStatement pStmt = con.prepareStatement(sql)) {

			pStmt.setString(1, webId);
			pStmt.setString(2, username);
			try (ResultSet rs = pStmt.executeQuery()) {
				while (rs.next()) {
					String msgId = rs.getString("msg_id");
					String statusCode = rs.getString("status_code");
					String destination = rs.getString("destination");
					ApiResultDTO resultDTO = new ApiResultDTO((msgId != null && !"0".equals(msgId)) ? msgId : "",
							statusCode, "", destination);
					result.add(resultDTO);
				}
			}
		} catch (SQLException ex) {
			logger.error("Error fetching API status for web_id: " + webId + " and username: " + username, ex);
		}
		return result;
	}

	public Map<String, ApiResultDTO> getDeliveryStatus(String username, List<String> list) throws SQLException {
		if (list.isEmpty())
			return Collections.emptyMap();

		Map<String, ApiResultDTO> result = new HashMap<>();
		StringBuilder query = new StringBuilder(
				"SELECT msg_id, status, submitted_time, deliver_time, dest_no FROM mis_").append(username)
				.append(" WHERE msg_id IN (").append(String.join(",", Collections.nCopies(list.size(), "?")))
				.append(")");

		try (Connection con = getConnection(); PreparedStatement pStmt = con.prepareStatement(query.toString())) {

			int index = 1;
			for (String id : list) {
				pStmt.setString(index++, id);
			}

			try (ResultSet rs = pStmt.executeQuery()) {
				while (rs.next()) {
					ApiResultDTO resultEntry = new ApiResultDTO(rs.getString("msg_id"));
					resultEntry.setDlrStatus(rs.getString("status"));
					resultEntry.setSubmitOn(rs.getString("submitted_time"));
					resultEntry.setDeliverOn(rs.getString("deliver_time"));
					resultEntry.setRequestStatus(ResponseCode.NO_ERROR);
					resultEntry.setMsisdn(rs.getString("dest_no"));
					result.put(rs.getString("msg_id"), resultEntry);
				}
			}
		} catch (SQLException sqle) {
			throw sqle;
		}
		return result;
	}

	public BaseApiDTO getApiSchedule(String systemId, String batchId) {
		String query = "SELECT * FROM api_schedule WHERE system_id = ? AND batch_id = ?";
		BaseApiDTO entry = null;
		try (Connection con = getConnection(); PreparedStatement pStmt = con.prepareStatement(query)) {

			pStmt.setString(1, systemId);
			pStmt.setString(2, batchId);
			try (ResultSet rs = pStmt.executeQuery()) {
				if (rs.next()) {
					entry = new BaseApiDTO();
					entry.setScheduleId(rs.getInt("id"));
					entry.setScheduleFile(rs.getString("filename"));
					entry.setScheduleTime(rs.getString("created_on"));
					logger.info(systemId + " - Api Schedule Found: " + entry.getScheduleId());
				} else {
					logger.info(systemId + " - No Api Schedule found for batch_id: " + batchId);
				}
			}
		} catch (SQLException sqle) {
			logger.error("Error fetching API schedule for system_id: " + systemId + ", batch_id: " + batchId, sqle);
		}

		return entry;
	}

	public void updateVngStatus(String respId, String status, String statusCode, String remarks) throws SQLException {
		StringBuilder queryBuilder = new StringBuilder("UPDATE vng_status SET status = ?");
		int parameterIndex = 2;
		if (statusCode != null) {
			queryBuilder.append(", status_code = ?");
		}
		if (remarks != null) {
			queryBuilder.append(", remarks = ?");
		}
		queryBuilder.append(" WHERE resp_id = ?");
		String query = queryBuilder.toString();
		logger.info(query);
		try (Connection con = getConnection(); PreparedStatement pStmt = con.prepareStatement(query)) {
			pStmt.setString(1, status);
			if (statusCode != null) {
				pStmt.setString(parameterIndex++, statusCode);
			}
			if (remarks != null) {
				pStmt.setString(parameterIndex++, remarks);
			}
			pStmt.setString(parameterIndex, respId);
			pStmt.executeUpdate();
		} catch (SQLException sqle) {
			logger.error("Error updating VNG status.", sqle);
			throw sqle;
		}
	}

	public List<ApiResultDTO> listApiSchedule(String systemId) {
		logger.info("Checking Api Schedules For: " + systemId);
		List<ApiResultDTO> list = new ArrayList<>();
		String query = "SELECT * FROM api_schedule WHERE system_id = ?";
		try (Connection con = getConnection(); PreparedStatement pStmt = con.prepareStatement(query)) {

			pStmt.setString(1, systemId);

			try (ResultSet rs = pStmt.executeQuery()) {
				while (rs.next()) {
					String serverTime = rs.getString("scheduled_on");
					int id = rs.getInt("id");
					try {
						Date scheduledTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(serverTime);
						if (!scheduledTime.before(new Date())) {
							ApiResultDTO entry = new ApiResultDTO();
							entry.setBatchId(rs.getString("batch_id"));
							entry.setScheduleOn(rs.getString("client_time"));
							entry.setSender(rs.getString("sender"));
							entry.setCreatedOn(rs.getString("created_on"));
							list.add(entry);
						} else {
							logger.info(systemId + "[" + id + "] Schedule Expired: " + scheduledTime);
						}
					} catch (ParseException e) {
						logger.error("Error parsing date: " + serverTime, e);
					}
				}
			}
		} catch (SQLException sqle) {
			logger.error("SQL Error in listApiSchedule for systemId: " + systemId, sqle);
		}
		logger.info(systemId + " Total Api Scheduled: " + list.size());
		return list;
	}

	public int createApiSchedule(BaseApiDTO scheduledEntry) throws SQLException {
		int generatedID = 0;
		String insertQry = "INSERT IGNORE INTO api_schedule "
				+ "(system_id, filename, scheduled_on, client_time, gmt, sender, batch_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

		logger.info(scheduledEntry + " <-- Adding Schedule To Database -->");

		try (Connection con = getConnection();
				PreparedStatement pStmt = con.prepareStatement(insertQry, PreparedStatement.RETURN_GENERATED_KEYS)) {

			pStmt.setString(1, scheduledEntry.getUsername());
			pStmt.setString(2, scheduledEntry.getScheduleFile());
			pStmt.setString(3, scheduledEntry.getServerScheduleTime());
			pStmt.setString(4, scheduledEntry.getScheduleTime());
			pStmt.setString(5, scheduledEntry.getGmt());
			pStmt.setString(6, scheduledEntry.getSender());
			pStmt.setString(7, scheduledEntry.getWebid());

			pStmt.executeUpdate();
			try (ResultSet rs = pStmt.getGeneratedKeys()) {
				if (rs.next()) {
					generatedID = rs.getInt(1);
					logger.info(scheduledEntry + " Schedule Added: " + generatedID);
				}
			}
		} catch (SQLException sqle) {
			logger.error("Error in createApiSchedule", sqle);
			throw new SQLException("ERROR: createApiSchedule() - " + sqle.getMessage(), sqle);
		}

		return generatedID;
	}

	public void addLookupSummaryReport(LookupSummaryObj summary) throws SQLException {
		String sql = "insert into hlr_brd_log.lookup_summary(batch_id,username,time,cost,mode,numbercount)values(?,?,?,?,?,?)";
		Connection con = null;
		PreparedStatement pStmt = null;
		String usermode = summary.getUserMode();
		try {
			con = getConnection();
			pStmt = con.prepareStatement(sql);
			pStmt.setString(1, summary.getBatchid());
			pStmt.setString(2, summary.getSystemId());
			pStmt.setString(3, summary.getTime());
			pStmt.setString(4, summary.getCost());
			pStmt.setString(5, usermode);
			pStmt.setInt(6, summary.getNumberCount());
			pStmt.executeUpdate();
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}

			} catch (SQLException sqle) {
			}
		}
	}

	public boolean deleteschedule(String web_id, String username) throws SQLException {
		logger.info("Delete Schedule(" + username + ") WebId: " + web_id);
		boolean result = false;
		String query = "delete from schedulesms where web_id = ? and username=?";
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(query);
			pStmt.setString(1, web_id);
			pStmt.setString(2, username);
			int count = pStmt.executeUpdate();
			if (count > 0) {
				logger.info("Schedule Deleted (" + username + ") WebId: " + web_id);
				result = true;
			} else {
				logger.info("Schedule Not Found (" + username + ") WebId: " + web_id);
			}
		} catch (SQLException sqle) {
			throw new SQLException("ERROR: deleteschedule()", sqle);
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
			} catch (SQLException sqle) {
			}
		}
		return result;
	}

	public Map getDlrReport(String username, String query, boolean hideNum) throws SQLException {
		Map customReport = new HashMap();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String dest = "";
		System.out.println("SQL: " + query);
		DeliveryDTO report = null;
		String msg_id = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(query, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			rs = pStmt.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					msg_id = rs.getString("msg_id");
					try {
						dest = rs.getString("dest_no");
						if (hideNum) {
							String newDest = dest.substring(0, dest.length() - 2);
							dest = newDest + "**";
						}
						String oprCountry = rs.getString("oprCountry");
						int network_id = 0;
						try {
							network_id = Integer.parseInt(oprCountry);
						} catch (Exception ex) {
						}
						String[] time = rs.getString("submitted_time").split(" ");
						String country = null, operator = null;
						if (GlobalVars.NetworkEntries.containsKey(network_id)) {
							NetworkEntry network = GlobalVars.NetworkEntries.get(network_id);
							country = network.getCountry();
							operator = network.getOperator();
						} else {
							country = oprCountry;
							operator = oprCountry;
						}
						report = new DeliveryDTO(msg_id, country, operator, dest, rs.getString("source_no"),
								rs.getDouble("cost"), time[0], time[1], rs.getString("status"));
						report.setDeliverOn(rs.getString("deliver_time"));
						report.setUsername(username);
						report.setResponseId(rs.getString("response_id"));
						if (rs.getString("err_code") == null) {
							report.setErrCode("000");
						} else {
							report.setErrCode(rs.getString("err_code"));
						}
						customReport.put(rs.getString("msg_id"), report);
					} catch (Exception sqle) {
						logger.error(msg_id, sqle.fillInStackTrace());
					}
				}
			}
		} catch (SQLException ex) {
			if (ex.getMessage().contains("Table") && ex.getMessage().contains("doesn't exist")) {
				logger.info("<-- " + username + " Mis & Content Table Doesn't Exist -->");
			} else {
				logger.error(" ", ex.fillInStackTrace());
			}
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
				}
				if (rs != null) {
					rs.close();
				}

			} catch (SQLException sqle) {
			}
		}
		return customReport;
	}

	public List getMessageContent(Map map, String username) throws SQLException {
		List list = new ArrayList();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = null;
		try {
			String keys = map.keySet().toString();
			keys = keys.substring(1, keys.length() - 1);
			sql = "select msg_id,dcs,content from content_" + username + " where msg_id in(" + keys + ")";
			con = getConnection();
			pStmt = con.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			rs = pStmt.executeQuery();
			while (rs.next()) {
				int dcs = rs.getInt("dcs");
				String msg_id = rs.getString("msg_id");
				String message = null;
				if (dcs == 0 || dcs == 240) {
					message = rs.getString("content");
				} else {
					try {
						String content = rs.getString("content").trim();
						if (content.contains("0000")) {
							content = content.replaceAll("0000", "0040");
						}
						message = hexCodePointsToCharMsg(content);
					} catch (Exception ex) {
						message = "Conversion Failed";
					}
				}
				if (map.containsKey(msg_id)) {
					DeliveryDTO report = (DeliveryDTO) map.remove(msg_id);
					report.setContent(message);
					list.add(report);
				}
			}
			logger.info("Objects without Content: " + map.size());
			list.addAll(map.values());
		} catch (Exception sqle) {
			if (sqle.getMessage().contains("doesn't exist")) {
				logger.info("content_" + username + " Table Doesn't Exist. Creating New");
			}
			throw new SQLException("ERROR: getMessageContent()", sqle);
		} finally {

			if (pStmt != null) {
				pStmt.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		return list;
	}

	public List getCustomizedReport(String username, String query, boolean hideNum) throws SQLException {
		List customReport = new ArrayList();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String dest = "";
		DeliveryDTO report = null;
		System.out.println("SQL: " + query);
		String msg_id = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(query, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			rs = pStmt.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					msg_id = rs.getString("msg_id");
					try {
						dest = rs.getString("dest_no");
						if (hideNum) {
							String newDest = dest.substring(0, dest.length() - 2);
							dest = newDest + "**";
						}
						String[] time = rs.getString("submitted_time").split(" ");
						String oprCountry = rs.getString("oprCountry");
						int network_id = 0;
						try {
							network_id = Integer.parseInt(oprCountry);
						} catch (Exception ex) {
						}
						String country = null, operator = null;
						if (GlobalVars.NetworkEntries.containsKey(network_id)) {
							NetworkEntry network = GlobalVars.NetworkEntries.get(network_id);
							country = network.getCountry();
							operator = network.getOperator();
						} else {
							country = oprCountry;
							operator = oprCountry;
						}
						report = new DeliveryDTO(msg_id, country, operator, dest, rs.getString("source_no"),
								rs.getDouble("cost"), time[0], time[1], rs.getString("status"));
						report.setDeliverOn(rs.getString("deliver_time"));
						report.setResponseId(rs.getString("response_id"));
						report.setUsername(username);
						report.setRoute(rs.getString("route_to_smsc"));
						report.setErrCode(rs.getString("err_code"));
						customReport.add(report);
					} catch (Exception sqle) {
						logger.error(msg_id, sqle.fillInStackTrace());
					}
				}
			}
		} catch (SQLException ex) {
			if (ex.getMessage().contains("Table") && ex.getMessage().contains("doesn't exist")) {
				logger.info("<-- " + username + " Mis & Content Table Doesn't Exist -->");
			} else {
				logger.error(" ", ex.fillInStackTrace());
			}
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
				}
				if (rs != null) {
					rs.close();
				}

			} catch (SQLException sqle) {
			}
		}
		return customReport;
	}

	public List getUnprocessedReport(String query, boolean hide_number, boolean isContent) throws SQLException {
		List customReport = new ArrayList();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String dest = "";
		DeliveryDTO report = null;
		System.out.println("UnprocessedReport:" + query);
		String msg_id = null;
		try {
			Set<String> flags = getSmscErrorFlagSymbol();
			con = getConnection();
			pStmt = con.prepareStatement(query, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			rs = pStmt.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					msg_id = rs.getString("msg_id");
					String status = rs.getString("s_flag");
					if (status != null) {
						if (status.equalsIgnoreCase("B")) {
							status = "BLOCKED";
						} else if (status.equalsIgnoreCase("M")) {
							status = "MINCOST";
						} else if (status.equalsIgnoreCase("F")) {
							status = "NONRESP";
						} else if (status.equalsIgnoreCase("Q")) {
							status = "QUEUED";
						} else {
							if (status.equalsIgnoreCase("E")) {
								continue;
							} else if (flags.contains(status.toUpperCase())) {
								continue;
							} else {
								status = "UNPROCD";
							}
						}
					}
					try {
						dest = rs.getString("destination_no");
						if (hide_number) {
							String newDest = dest.substring(0, dest.length() - 2);
							dest = newDest + "**";
						}
						String[] time = rs.getString("time").split(" ");
						String oprCountry = rs.getString("oprCountry");
						int network_id = 0;
						try {
							network_id = Integer.parseInt(oprCountry);
						} catch (Exception ex) {
						}
						String country = null, operator = null;
						if (GlobalVars.NetworkEntries.containsKey(network_id)) {
							NetworkEntry network = GlobalVars.NetworkEntries.get(network_id);
							country = network.getCountry();
							operator = network.getOperator();
						} else {
							country = oprCountry;
							operator = oprCountry;
						}
						report = new DeliveryDTO(msg_id, country, operator, dest, rs.getString("source_no"),
								rs.getDouble("cost"), time[0], time[1], status);
						report.setUsername(rs.getString("username"));
						report.setRoute(rs.getString("smsc"));
						if (isContent) {
							String message = null;
							if (rs.getInt("dcs") == 0) {
								message = rs.getString("content").trim();
							} else {
								try {
									String content = rs.getString("content").trim();
									if (content.contains("0000")) {
										content = content.replaceAll("0000", "0040");
									}
									message = hexCodePointsToCharMsg(content);
								} catch (Exception ex) {
									message = "Conversion Failed";
								}
							}
							report.setContent(message);
						}
						customReport.add(report);
					} catch (Exception sqle) {
						logger.error(msg_id, sqle.fillInStackTrace());
					}
				}
			}
		} catch (SQLException ex) {
			logger.error(" ", ex.fillInStackTrace());
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sqle) {
			}
		}
		logger.info(" Report List Count:--> " + customReport.size());
		return customReport;
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
			} catch (SQLException sqle) {
			}
		}
		return country;
	}

	public String[] getDeliveryStatus(String username, String msgid) throws SQLException {
		String[] result = new String[3];
		Connection con = null;
		Statement pStmt = null;
		ResultSet rs = null;
		String query = null;
		if (username != null) {
			query = "select status,deliver_time,err_code from mis_" + username + " where msg_id = " + msgid;
		} else {
			query = "select status from mis_table where msg_id = " + msgid;
		}
		try {
			con = getConnection();
			pStmt = con.createStatement();
			rs = pStmt.executeQuery(query);
			if (rs.next()) {
				result[0] = rs.getString("status");
				if (username != null) {
					result[1] = rs.getString("deliver_time");
					result[2] = rs.getString("err_code");
				}
			} else {
				rs.close();
				pStmt.close();
				query = "select status from mis_table_log where msg_id = " + msgid;
				pStmt = con.createStatement();
				rs = pStmt.executeQuery(query);
				if (rs.next()) {
					result[0] = rs.getString("status");
				}
			}
		} catch (Exception sqle) {
			throw new SQLException(" Delivery Status Get Error ", sqle);
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
			} catch (SQLException sqle) {
			}
		}
		return result;
	}

	public String hexCodePointsToCharMsg(String msg) {
		boolean reqNULL = false;
		byte[] charsByt, var;
		int x = 0;
		if (msg.substring(0, 2).compareTo("00") == 0) {
			reqNULL = true;
		}
		charsByt = new BigInteger(msg, 16).toByteArray();
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
			msg = new String(var, "UTF-16");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return msg;
	}

	private Set<String> getSmscErrorFlagSymbol() {
		Set<String> map = new HashSet<String>();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = "select distinct(Flag_symbol) from smsc_error_code";
		try {
			con = getConnection();
			pStmt = con.prepareStatement(sql);
			rs = pStmt.executeQuery();
			while (rs.next()) {
				String flag_symbol = rs.getString("Flag_symbol");
				if (flag_symbol != null && flag_symbol.length() > 0) {
					map.add(flag_symbol.toUpperCase());
				}
			}
		} catch (SQLException sqle) {
			logger.error(" ", sqle.fillInStackTrace());
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return map;
	}

}
