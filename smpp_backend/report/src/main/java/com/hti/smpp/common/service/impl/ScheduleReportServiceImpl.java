package com.hti.smpp.common.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.database.DataBase;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.request.ScheduleReportRequest;
import com.hti.smpp.common.schedule.dto.ScheduleEntry;
import com.hti.smpp.common.schedule.dto.ScheduleEntryExt;
import com.hti.smpp.common.service.ScheduleReportService;
import com.hti.smpp.common.service.UserDAService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.Customlocale;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;

import jakarta.servlet.http.HttpServlet;
import jakarta.ws.rs.BadRequestException;

@Service
public class ScheduleReportServiceImpl implements ScheduleReportService {
	@Autowired
	private DataBase dataBase;

	@Autowired
	private UserEntryRepository userRepository;
	@Autowired
	private MessageResourceBundle messageResourceBundle;

	@Autowired
	private UserDAService userService;
	private Logger logger = LoggerFactory.getLogger(ScheduleReportServiceImpl.class);

	@Autowired
	private DataSource dataSource;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	private String template_file = IConstants.FORMAT_DIR + "report//SmscDlrReport.jrxml";
	private final String summary_template_file = IConstants.FORMAT_DIR + "report//SmscDlrSummaryReport.jrxml";
	Locale locale = null;
	private String reportUser = null;

	@Override
	public ResponseEntity<?> ScheduleReport(String username, ScheduleReportRequest customReportForm) {
		String target = IConstants.SUCCESS_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional.orElseThrow(() -> new NotFoundException(
				messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username })));
		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION,
					new Object[] { username }));
		}
		try {
			List<ScheduleEntryExt> reportList = getReportList(customReportForm, username);
			if (reportList != null && !reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());
				return ResponseEntity.ok(reportList);
			} else {
				throw new NotFoundException(messageResourceBundle
						.getExMessage(ConstantMessages.SCHEDULE_REPORT_NOT_FOUND_MESSAGE, new Object[] { username }));
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (IllegalArgumentException e) {

			logger.error(messageResourceBundle.getLogMessage("invalid.argument"), e.getMessage(), e);
			throw new BadRequestException(messageResourceBundle
					.getExMessage(ConstantMessages.BAD_REQUEST_EXCEPTION_MESSAGE, new Object[] { e.getMessage() }));

		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("unexpected.error"), e.getMessage(), e);
			throw new InternalServerException(e.getMessage());
		}
	}

	private List<ScheduleEntryExt> getReportList(ScheduleReportRequest customReportForm, String username)
			throws SQLException {
		List<ScheduleEntryExt> list = new ArrayList<ScheduleEntryExt>();
		String sql = "select * from schedule_history where client_time between '" + customReportForm.getStartDate()
				+ " ' and '" + customReportForm.getEndDate() + "' ";
		if (customReportForm.getClientId() != null && customReportForm.getClientId().length() > 0) {
			if (!customReportForm.getClientId().contains("ALL")) {
				sql += " and username = '" + customReportForm.getClientId() + "' ";
			}
		}
		if (customReportForm.getSenderId() != null && customReportForm.getSenderId().length() > 0) {
			String sender = customReportForm.getSenderId();
			if (sender.contains("%")) {
				sql += " and sender_id like '" + sender + "' ";
			} else if (sender.contains(",")) {
				StringTokenizer strTokens = new StringTokenizer(sender, ",");
				String destlist = "";
				while (strTokens.hasMoreTokens()) {
					destlist += "'" + strTokens.nextToken() + "',";
				}
				destlist = destlist.substring(0, destlist.length() - 1);
				sql += " and sender_id in (" + destlist + ") ";
			} else {
				sql += " and sender_id = '" + sender + "' ";
			}
		}
		list = getScheduleReport(sql);
		return list;
	}

	public List<ScheduleEntryExt> getScheduleReport(String sql) {
		logger.info("Schedule History sql: " + sql);
		List<ScheduleEntryExt> scheduleList = new ArrayList<>();
		ScheduleEntryExt entryExt = null;
		Connection con=null;
		ResultSet rs=null;
		PreparedStatement pStmt=null;
		try { con = getConnection();
				 pStmt = con.prepareStatement(sql);
				 rs = pStmt.executeQuery(); 

			while (rs.next()) {
				int id = rs.getInt("id");
				int serverId = rs.getInt("server_id");
				String server_time = rs.getString("server_time");
				String system_id = rs.getString("username");
				ScheduleEntry entry = new ScheduleEntry(system_id, server_time, rs.getString("client_gmt"),
						rs.getString("client_time"), serverId, rs.getString("remarks"), null, rs.getString("repeated"),
						rs.getString("SchType"), rs.getString("web_id"));
				entry.setId(id);
				entry.setCreatedOn(rs.getString("createdOn"));
				entryExt = new ScheduleEntryExt(entry);
				entryExt.setMessageType(rs.getString("msg_type"));
				entryExt.setCampaign(rs.getString("campaign_name"));
				entryExt.setTotalNumbers(rs.getInt("total_number"));
				entryExt.setSenderId(rs.getString("sender_id"));
				scheduleList.add(entryExt);
			}
			logger.info(messageResourceBundle.getLogMessage("schedule.history.report.message"), scheduleList.size());

		} catch (SQLException sqle) {
			logger.error(messageResourceBundle.getLogMessage("sql.query.error.message"), sqle);

		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
				}
				if (rs != null) {
					rs.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}

		return scheduleList;
	}
}
