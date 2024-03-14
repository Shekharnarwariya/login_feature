package com.hti.smpp.common.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.messages.dto.BulkEntry;
import com.hti.smpp.common.request.AbortBatchReportRequest;
import com.hti.smpp.common.service.AbortBatchReportService;
import com.hti.smpp.common.service.UserDAService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;

import com.hti.smpp.common.util.Converters;
import com.hti.smpp.common.util.Customlocale;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;

import jakarta.persistence.EntityManager;
import jakarta.ws.rs.BadRequestException;

@Service
public class AbortBatchReportServiceImpl implements AbortBatchReportService {

	@Autowired
	private UserEntryRepository userRepository;
	@Autowired
	private WebMasterEntryRepository webMasterEntryRepository;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private MessageResourceBundle messageResourceBundle;

	Locale locale = null;
	private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public List<BulkEntry> abortBatchReport(String username, AbortBatchReportRequest customReportForm) {
	    Optional<UserEntry> userOptional = userRepository.findBySystemId(username);

	    UserEntry user = userOptional.orElseThrow(() -> new NotFoundException(
	            messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[]{username})));

	    if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
	        throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION,
	                new Object[]{username}));
	    }

	    try {
	        List<BulkEntry> reportList = getReportList(customReportForm, user.getId());

	        if (!reportList.isEmpty()) {
	            System.out.println("Report Size: " + reportList.size());
	            return reportList;
	        } else {
				throw new NotFoundException(
						messageResourceBundle.getExMessage(ConstantMessages.NO_DATA_FOUND_ABORT_REPORT));
 }
	    } 
	    catch (NotFoundException e) {
			// Log NotFoundException
			throw new NotFoundException(e.getMessage());
			
	    }catch (IllegalArgumentException e) {
	        // Log IllegalArgumentException
	        logger.error(messageResourceBundle.getLogMessage("invalid.argument"), e.getMessage(), e);
	        throw new BadRequestException(messageResourceBundle
	                .getExMessage(ConstantMessages.BAD_REQUEST_EXCEPTION_MESSAGE, new Object[]{e.getMessage()}));
	    } catch (Exception e) {
	        // Log other exceptions
	        logger.error(messageResourceBundle.getLogMessage("unexpected.error"), e.getMessage(), e);
	        throw new InternalServerException(messageResourceBundle
	                .getExMessage(ConstantMessages.INTERNAL_SERVER_EXCEPTION_MESSAGE, new Object[]{username}));
	    }
	}


	private List<BulkEntry> getReportList(AbortBatchReportRequest customReportForm, int id) throws SQLException {
		UserDAService userDAService = new UserDAServiceImpl();
		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(id);
		if (webMasterEntry == null) {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NOT_FOUND_EXCEPTION_MESSAGE,
					new Object[] { id }));

		}

		String to_gmt = null;
		String from_gmt = null;
		String sql = "select ";
		if (!webMasterEntry.getGmt().equalsIgnoreCase(IConstants.DEFAULT_GMT)) {
			to_gmt = webMasterEntry.getGmt().replace("GMT", "");
			from_gmt = IConstants.DEFAULT_GMT.replace("GMT", "");
			sql += "CONVERT_TZ (createdOn,'" + from_gmt + "','" + to_gmt + "') AS createdOn,";
		} else {
			sql += "createdOn,";
		}
		sql += "batch_id,system_id,sender_id,totalNum,pending,firstNum,delay,reqType,server_id,ston ,snpi ,alert,alert_number,expiry_hour,content,msg_type,campaign_name from batch_unprocess where";
		if (to_gmt != null) {
			sql += " createdOn between CONVERT_TZ('" + customReportForm.getStartDate() + " 00:00:00','" + to_gmt + "','"
					+ from_gmt + "')and CONVERT_TZ('" + customReportForm.getEndDate() + " 23:59:59','" + to_gmt + "','"
					+ from_gmt + "')";
		} else {
			if (customReportForm.getStartDate().equalsIgnoreCase(customReportForm.getEndDate())) {
				sql += " DATE(createdOn)= '" + customReportForm.getSenderId() + "'";
			} else {
				sql += " DATE(createdOn) between '" + customReportForm.getStartDate() + "' and '"
						+ customReportForm.getEndDate() + "'";
			}
		}
		if (customReportForm.getClientId() != null && customReportForm.getClientId().length() > 0) {
			if (!customReportForm.getClientId().contains("ALL")) {
				sql += " and system_id = '" + customReportForm.getClientId() + "' ";
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
		System.out.println("SQL: " + sql);
		List<BulkEntry> list = getAbortReport(sql);
		return list;
	}

	public List<BulkEntry> getAbortReport(String sql) throws SQLException {
		logger.info(messageResourceBundle.getLogMessage("batch.aborted.report.check"));
		List<BulkEntry> list = new ArrayList<BulkEntry>();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(sql);
			rs = pStmt.executeQuery();
			BulkEntry entry = null;
			while (rs.next()) {
				String content = new Converters().uniHexToCharMsg(rs.getString("content"));
				entry = new BulkEntry(rs.getInt("batch_id"), rs.getString("system_id"), rs.getString("sender_id"),
						rs.getDouble("delay"), rs.getString("reqType"), rs.getInt("server_id"),
						rs.getString("msg_type"), rs.getString("createdOn"), rs.getLong("totalNum"),
						rs.getLong("firstNum"), content, rs.getString("campaign_name"), rs.getLong("pending"));
				list.add(entry);
			}
			logger.info(messageResourceBundle.getLogMessage("batch.aborted.report.size"), list.size());

		} catch (SQLException sqle) {
			logger.error(" ", sqle.fillInStackTrace());
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
		return list;
	}

}
