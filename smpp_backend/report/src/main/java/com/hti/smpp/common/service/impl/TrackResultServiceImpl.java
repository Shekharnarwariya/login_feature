package com.hti.smpp.common.service.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.dto.MisDTO;
import com.hti.smpp.common.dto.SmscInDTO;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.TrackResultResponse;
import com.hti.smpp.common.service.TrackResultService;
import com.hti.smpp.common.util.Customlocale;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;

@Service
public class TrackResultServiceImpl implements TrackResultService {

	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private MessageResourceBundle messageResourceBundle;
	private Logger logger = LoggerFactory.getLogger(TrackResultServiceImpl.class);
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
	Locale locale =null;
	
	@Override
	public TrackResultResponse TrackResultReport(String username, CustomReportForm customReportForm,String lang) {
		TrackResultResponse trackResultResponse = new TrackResultResponse();
		String target = IConstants.SUCCESS_KEY;
		String req_msgid = "-", req_user = "-", req_time = "-", req_dest = "-", req_sender = "-", req_status = "-",
				req_smsc = "-";
		String msgid_temp = "", user_temp = "", smscin_dest_temp = "", mis_dest_temp = "", sender_temp = "",
				subtime_temp = "", time_temp = "", status_temp = "", smscin_smsc_temp = "", mis_smsc_temp = "";
		String smscinSQL = "";
		String misSQL = "";
		boolean and = false;
		try {
			locale = Customlocale.getLocaleByLanguage(lang); ;
			
			if (customReportForm.getMessageId() != null && customReportForm.getMessageId().length() > 0) {
				if (customReportForm.getMessageId().contains(",")) {
					String messageid = "";
					StringTokenizer strTokens = new StringTokenizer(customReportForm.getMessageId(), ",");
					while (strTokens.hasMoreTokens()) {
						messageid += "'" + strTokens.nextToken() + "',";
					}
					messageid = messageid.substring(0, messageid.length() - 1);
					msgid_temp = "msg_id in (" + messageid + ") ";
				} else {
					msgid_temp = "msg_id = '" + customReportForm.getMessageId() + "' ";
				}
				req_msgid = customReportForm.getMessageId();
			}
			if (customReportForm.getClientId() != null && customReportForm.getClientId().length() > 0) {
				user_temp = "username = '" + customReportForm.getClientId() + "' ";
				req_user = customReportForm.getClientId();
			}
			if (customReportForm.getDestinationNumber() != null
					&& customReportForm.getDestinationNumber().length() > 0) {
				String destination = customReportForm.getDestinationNumber();
				if (destination.contains("%")) {
					smscin_dest_temp = "destination_no like '" + destination + "' ";
					mis_dest_temp = "dest_no like '" + destination + "' ";
				} else if (destination.contains(",")) {
					StringTokenizer strTokens = new StringTokenizer(destination, ",");
					String destlist = "";
					while (strTokens.hasMoreTokens()) {
						destlist += "'" + strTokens.nextToken() + "',";
					}
					destlist = destlist.substring(0, destlist.length() - 1);
					smscin_dest_temp = "destination_no in (" + destlist + ") ";
					mis_dest_temp = "dest_no in (" + destlist + ") ";
				} else {
					smscin_dest_temp = "destination_no = '" + destination + "' ";
					mis_dest_temp = "dest_no = '" + destination + "' ";
				}
				req_dest = customReportForm.getDestinationNumber();
			}
			if (customReportForm.getSenderId() != null && customReportForm.getSenderId().length() > 0) {
				String sender = customReportForm.getSenderId();
				if (sender.contains("%")) {
					sender_temp = "source_no like '" + sender + "' ";
				} else if (sender.contains(",")) {
					StringTokenizer strTokens = new StringTokenizer(sender, ",");
					String destlist = "";
					while (strTokens.hasMoreTokens()) {
						destlist += "'" + strTokens.nextToken() + "',";
					}
					destlist = destlist.substring(0, destlist.length() - 1);
					sender_temp = "source_no in (" + destlist + ") ";
				} else {
					sender_temp = "source_no = '" + sender + "' ";
				}
				req_sender = customReportForm.getSenderId();
			}
			if (customReportForm.getSyear() != null && customReportForm.getSyear().length() > 0) {
				String date = customReportForm.getSyear() + "-" + customReportForm.getSmonth() + "-"
						+ customReportForm.getSday();
				if (customReportForm.getShour() != null && customReportForm.getShour().length() > 0) {
					date += " " + customReportForm.getShour() + ":" + customReportForm.getSmin();
				}
				subtime_temp = "submitted_time like '" + date + "%' ";
				time_temp = "time like '" + date + "%' ";
				req_time = date;
			}
			if (customReportForm.getMessageStatus() != null && customReportForm.getMessageStatus().length() > 0) {
				String status = customReportForm.getMessageStatus();
				if (status.contains("%")) {
					status_temp = "status like '" + status + "' ";
				} else if (status.contains(",")) {
					StringTokenizer strTokens = new StringTokenizer(status, ",");
					String destlist = "";
					while (strTokens.hasMoreTokens()) {
						destlist += "'" + strTokens.nextToken() + "',";
					}
					destlist = destlist.substring(0, destlist.length() - 1);
					status_temp = "status in (" + destlist + ") ";
				} else {
					status_temp = "status = '" + status + "' ";
				}
				req_status = customReportForm.getMessageStatus();
			}
			if (customReportForm.getSmscName() != null && customReportForm.getSmscName().length() > 0) {
				String smsc = customReportForm.getSmscName();
				if (smsc.contains("%")) {
					smscin_smsc_temp = "smsc like '" + smsc + "' ";
					mis_smsc_temp = "Route_to_smsc like '" + smsc + "' ";
				} else if (smsc.contains(",")) {
					StringTokenizer strTokens = new StringTokenizer(smsc, ",");
					String destlist = "";
					while (strTokens.hasMoreTokens()) {
						destlist += "'" + strTokens.nextToken() + "',";
					}
					destlist = destlist.substring(0, destlist.length() - 1);
					smscin_smsc_temp = "smsc in (" + destlist + ") ";
					mis_smsc_temp = "Route_to_smsc in (" + destlist + ") ";
				} else {
					smscin_smsc_temp = "smsc = '" + smsc + "' ";
					mis_smsc_temp = "Route_to_smsc = '" + smsc + "' ";
				}
				req_smsc = customReportForm.getSmscName();
			}
			// ------------- Creating Query ------------------------
			if (msgid_temp.length() > 0) {
				smscinSQL += msgid_temp;
				misSQL += msgid_temp;
				and = true;
			}
			if (user_temp.length() > 0) {
				if (and) {
					smscinSQL += "and ";
					// misSQL += "and ";
				} else {
					and = true;
				}
				smscinSQL += user_temp;
				// misSQL += user_temp;
			}
			if (smscin_dest_temp.length() > 0) {
				if (and) {
					smscinSQL += "and ";
					misSQL += "and ";
				} else {
					and = true;
				}
				smscinSQL += smscin_dest_temp;
				misSQL += mis_dest_temp;
			}
			if (sender_temp.length() > 0) {
				if (and) {
					smscinSQL += "and ";
					misSQL += "and ";
				} else {
					and = true;
				}
				smscinSQL += sender_temp;
				misSQL += sender_temp;
			}
			if (time_temp.length() > 0) {
				if (and) {
					smscinSQL += "and ";
					misSQL += "and ";
				} else {
					and = true;
				}
				smscinSQL += time_temp;
				misSQL += subtime_temp;
			}
			if (smscin_smsc_temp.length() > 0) {
				if (and) {
					smscinSQL += "and ";
					misSQL += "and ";
				} else {
					and = true;
				}
				smscinSQL += smscin_smsc_temp;
				misSQL += mis_smsc_temp;
			}
			if (status_temp.length() > 0) {
				if (and) {
					misSQL += "and ";
				}
				misSQL += status_temp;
			}
			Map userPrefix = new HashMap();
			// -------------- For Smsc_in Records ---------------------
			List smscinlist = new ArrayList();
			if (smscinSQL.length() > 0) {
				String smscin_stmt = "select * from smsc_in where " + smscinSQL + " order by time";
				String smscin_log_stmt = "select * from host_zlog.smsc_in_log where " + smscinSQL + " order by time";
				logger.info(messageResourceBundle.getMessage("smsc.in.message"), smscin_stmt);

				logger.info(messageResourceBundle.getMessage("smscin.log.message"), smscin_log_stmt);

				Map inmap = getSmscInRecord(smscin_stmt);
				Map inLogmap = getSmscInRecord(smscin_log_stmt);
				Iterator itr = inmap.keySet().iterator();
				while (itr.hasNext()) {
					String key = (String) itr.next();
					SmscInDTO smscin = (SmscInDTO) inmap.get(key);
					smscinlist.add(smscin);
					// --------------- Getting User's Disctinct Prefix Routing ---------
					Set prefixSet = null;
					if (userPrefix.containsKey(smscin.getUsername())) {
						prefixSet = (Set) userPrefix.get(smscin.getUsername());
					} else {
						prefixSet = new HashSet();
					}
					prefixSet.add(smscin.getOprCountry());
					userPrefix.put(smscin.getUsername(), prefixSet);
					// ---------------End Getting User's Disctinct Prefix Routing ---------
					inLogmap.remove(key);
				}
				itr = inLogmap.keySet().iterator();
				while (itr.hasNext()) {
					String key = (String) itr.next();
					SmscInDTO smscin = (SmscInDTO) inLogmap.get(key);
					smscin.setSflag("T");
					smscinlist.add(smscin);
					// --------------- Getting User's Disctinct Prefix Routing ---------
					Set prefixSet = null;
					if (userPrefix.containsKey(smscin.getUsername())) {
						prefixSet = (Set) userPrefix.get(smscin.getUsername());
					} else {
						prefixSet = new HashSet();
					}
					prefixSet.add(smscin.getOprCountry());
					userPrefix.put(smscin.getUsername(), prefixSet);
					// ---------------End Getting User's Disctinct Prefix Routing ---------
				}
				logger.info(messageResourceBundle.getMessage("smscin.record.size.message"), smscinlist.size());

			}
			// -------------- Finished For Smsc_in Records ---------------------
			// -------------- For Mis Records ---------------------
			List mislist = new ArrayList();
			if (misSQL.length() > 0) {
				String mis_stmt = "";
				if (!userPrefix.isEmpty()) {
					Iterator itr = userPrefix.keySet().iterator();
					while (itr.hasNext()) {
						String username1 = (String) itr.next();
						mis_stmt += "select * from mis_" + username1 + " where " + misSQL + " order by submitted_time";
						logger.info(messageResourceBundle.getMessage("mis.message"), mis_stmt);

						List temp_list = getMisRecord(mis_stmt);
						if (temp_list != null && !temp_list.isEmpty()) {
							mislist.addAll(temp_list);
						}
					}
				}
			}
			// -------------- Finished For Mis Records ---------------------
			// -------------- For Routing Records ---------------------
			List RoutingList = new ArrayList();
			if (!userPrefix.isEmpty()) {
				Iterator itr = userPrefix.keySet().iterator();
				String routingSQL = "";
				int i = 0;
				while (itr.hasNext()) {
					String username1 = (String) itr.next();
					String prefixStr = "";
					Set prefixSet = (Set) userPrefix.get(username1);
					Iterator setItr = prefixSet.iterator();
					while (setItr.hasNext()) {
						prefixStr += "'" + (String) setItr.next() + "',";
					}
					if (i > 0) {
						routingSQL += " UNION ";
					}
					routingSQL += "select * from routemaster where client_name = '" + username1 + "'";
					if (prefixStr.length() > 0) {
						prefixStr = prefixStr.substring(0, prefixStr.length() - 1);
						if (prefixStr.indexOf(",") > -1) {
							routingSQL += " and prefix_name in(" + prefixStr + ")";
						} else {
							routingSQL += " and prefix_name = " + prefixStr;
						}
					}
					i++;
				}
				logger.info(messageResourceBundle.getMessage("routing.sql.message"), routingSQL);

				if (routingSQL.length() > 0) {
					// RoutingList = dbService.getRoutingRecord(routingSQL);
				}
			}
			// -------------- Finished For Routing Records ---------------------
			// -------- Setting Requested Parameters ---------
			trackResultResponse.setReq_msgid(req_msgid);
			trackResultResponse.setReq_user(req_user);
			trackResultResponse.setReq_dest(req_dest);
			trackResultResponse.setReq_sender(req_sender);
			trackResultResponse.setReq_time(req_time);
			trackResultResponse.setReq_status(req_status);
			trackResultResponse.setReq_smsc(req_smsc);
			trackResultResponse.setSmscinlist(smscinlist);
			trackResultResponse.setMislist(mislist);
			trackResultResponse.setSmscinsize(smscinlist.size());
			trackResultResponse.setMissize(mislist.size());
			trackResultResponse.setRoutelist(RoutingList);
			trackResultResponse.setRoutesize(RoutingList.size());

		} catch (Exception ex) {
			ex.printStackTrace();
			target = IConstants.FAILURE_KEY;
		}
		trackResultResponse.setStatus(target);
		return trackResultResponse;
	}

	public List getMisRecord(String sql) throws SQLException {
		List misList = new ArrayList();
		Connection con = null;
		Statement pStmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			pStmt = con.createStatement();
			rs = pStmt.executeQuery(sql);
			MisDTO smscin = null;
			while (rs.next()) {
				smscin = new MisDTO();
				String msgid = rs.getString("msg_id");
				smscin.setMsgid(msgid);
				smscin.setUsername(rs.getString("username"));
				smscin.setTime(rs.getString("submitted_time"));
				// smscin.setDest_ton(rs.getInt("dton"));
				// smscin.setDest_npi(rs.getInt("dnpi"));
				smscin.setDestination(rs.getString("dest_no"));
				smscin.setOprCountry(rs.getString("oprCountry"));
				// smscin.setSour_ton(rs.getInt("ston"));
				// smscin.setSour_npi(rs.getInt("snpi"));
				smscin.setSourceno(rs.getString("source_no"));
				// smscin.setRegistered(rs.getString("registered"));
				// smscin.setEsm(rs.getInt("esm_class"));
				// smscin.setDcs(rs.getInt("dcs"));
				smscin.setCost(rs.getString("cost"));
				// smscin.setSflag(rs.getString("s_flag"));
				smscin.setSmsc(rs.getString("Route_to_SMSC"));
				// smscin.setSessionid(rs.getString("session_id"));
				smscin.setResponseid(rs.getString("response_id"));
				// smscin.setSub_ston(rs.getInt("sub_ston"));
				// smscin.setSub_snpi(rs.getInt("sub_snpi"));
				// smscin.setSub_dton(rs.getInt("sub_dton"));
				// smscin.setSub_dnpi(rs.getInt("sub_dnpi"));
				smscin.setDelivertime(rs.getString("deliver_time"));
				smscin.setStatus(rs.getString("Status"));
				smscin.setErrcode(rs.getString("Err_code"));
				// logger.info("RS-ESM: " + rs.getInt("esm_class") + " SessionID: " +
				// rs.getString("session_id"));
				// logger.info("ESM: " + smscin.getEsm() + " SessionID: " +
				// smscin.getSessionid());
				misList.add(smscin);
			}
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
		return misList;
	}

	public Map getSmscInRecord(String sql) throws SQLException {
		Map map = new HashMap();
		Connection con = null;
		Statement pStmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			pStmt = con.createStatement();
			rs = pStmt.executeQuery(sql);
			SmscInDTO smscin = null;
			while (rs.next()) {
				smscin = new SmscInDTO();
				String msgid = rs.getString("msg_id");
				smscin.setMsgid(msgid);
				smscin.setUsername(rs.getString("username"));
				smscin.setTime(rs.getString("time"));
				smscin.setSeqno(rs.getString("seq_no"));
				smscin.setContent(rs.getString("content"));
				smscin.setDest_ton(rs.getInt("dest_ton"));
				smscin.setDest_npi(rs.getInt("dest_npi"));
				smscin.setDestination(rs.getString("destination_no"));
				smscin.setOprCountry(rs.getString("oprCountry"));
				smscin.setSour_ton(rs.getInt("sour_ton"));
				smscin.setSour_npi(rs.getInt("sour_npi"));
				smscin.setSourceno(rs.getString("source_no"));
				smscin.setRegistered(rs.getString("registered"));
				smscin.setEsm(rs.getInt("esm_class"));
				smscin.setDcs(rs.getInt("dcs"));
				smscin.setCost(rs.getString("cost"));
				smscin.setSflag(rs.getString("s_flag"));
				smscin.setSmsc(rs.getString("smsc"));
				smscin.setPriority(rs.getString("Priority"));
				smscin.setSessionid(rs.getString("session_id"));
				smscin.setSecondrysmscid(rs.getString("Secondry_smsc_id"));
				map.put(msgid, smscin);
			}
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
		return map;
	}

}
