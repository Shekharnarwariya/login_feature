package com.hti.smpp.common.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.report.dto.ProfitReportEntry;
import com.hti.smpp.common.report.dto.ReportCriteria;
import com.hti.smpp.common.request.ProfitReportRequest;
import com.hti.smpp.common.service.ProfitReportService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.MessageResourceBundle;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

@Service
public class ProfitReportServiceImpl implements ProfitReportService {
	@Autowired
	private UserEntryRepository userRepository;

	private Logger logger = LoggerFactory.getLogger(ProfitReportServiceImpl.class);

	@Autowired
	private DataSource dataSource;

	@Autowired
	private MessageResourceBundle messageResourceBundle;

	@Autowired
	private ReportDAOImpl reportDAOImpl;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	 @Transactional
    @Override
    public ResponseEntity<?> ProfitReportview(String username, ProfitReportRequest customReportForm, int page, int size) {
        UserEntry user = userRepository.findBySystemId(username)
                .orElseThrow(() -> new NotFoundException(messageResourceBundle
                        .getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[]{username})));

        if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
            throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION,
                    new Object[]{username}));
        }
        
        Pageable pageable = PageRequest.of(page, size);
        try {
            Page<ProfitReportEntry> profitReportPage = getProfitReportList(customReportForm, pageable, user);
            if (!profitReportPage.isEmpty()) {
                logger.info(messageResourceBundle.getLogMessage("report.size.view.message"), user.getSystemId(),
                        profitReportPage.getTotalElements());
                return ResponseEntity.ok().body(profitReportPage);
            } else {
                throw new NotFoundException(messageResourceBundle
                        .getExMessage(ConstantMessages.PROFIT_REPORT_NOT_FOUND_MESSAGE, new Object[]{username}));
            }
        } catch (NotFoundException ex) {
            throw new NotFoundException(ex.getMessage());
        } catch (Exception ex) {
            throw new InternalServerException(messageResourceBundle
                    .getExMessage(ConstantMessages.ERROR_GETTING_PROFIT_REPORT_MESSAGE, new Object[]{username}));
        }
    }

	
	private Page<ProfitReportEntry> getProfitReportList(ProfitReportRequest reportRequest, Pageable pageable, UserEntry user) {
	    LocalDate startDate = LocalDate.parse(reportRequest.getStartDate());
	    LocalDate endDate = LocalDate.parse(reportRequest.getEndDate()).plusDays(1); // Include the end day in the range

	    String startMsgId = startDate.format(DateTimeFormatter.ofPattern("yyMMdd")) + "0000000000000";
	    String endMsgId = endDate.format(DateTimeFormatter.ofPattern("yyMMdd")) + "0000000000000";

	    ReportCriteria criteria = new ReportCriteria();
	    criteria.setStartMsgId(Long.parseLong(startMsgId));
	    criteria.setEndMsgId(Long.parseLong(endMsgId));

	    if ("superadmin".equalsIgnoreCase(user.getRole()) && !"0".equals(reportRequest.getClientId())) {
	        criteria.setResellerId(Integer.parseInt(reportRequest.getClientId()));
	    } else {
	        criteria.setResellerId(user.getId());
	        if (!"0".equals(reportRequest.getClientId())) {
	            criteria.setUserId(Integer.parseInt(reportRequest.getClientId()));
	        }
	    }

	    try {
	        Page<ProfitReportEntry> reportEntries = reportDAOImpl.listProfitReport(criteria, pageable);
	        if (reportEntries.isEmpty()) {
	            logger.info(messageResourceBundle.getLogMessage("no.report.data.found.message"), user.getSystemId());
	            return new PageImpl<>(Collections.emptyList(), pageable, 0); // Return an empty page to maintain null safety
	        }
	        return reportEntries;
	    } catch (Exception e) {
	        logger.error("Error fetching profit report list", e);
	        // Return an empty page with the original pageable information
	        return new PageImpl<>(Collections.emptyList(), pageable, 0);
	    }
	}




	public Page<ProfitReportEntry> listProfitReport(ReportCriteria rc, Pageable pageable) {
	    Page<ProfitReportEntry> list = reportDAOImpl.listProfitReport(rc,  pageable);

	    list.forEach(entry -> {
	        Optional.ofNullable(GlobalVars.NetworkEntries.get(entry.getNetworkId()))
	                .ifPresent(networkEntry -> {
	                    entry.setCountry(networkEntry.getCountry());
	                    entry.setOperator(networkEntry.getOperator());
	                });

	        userRepository.findById(entry.getUserId())
	                .map(UserEntry::getSystemId)  // If UserEntry is present, get the system ID
	                .or(() -> Optional.ofNullable(getRemovedUsername(entry.getUserId()))) // If not, try getting removed username
	                .ifPresentOrElse(
	                        entry::setUsername,
	                        () -> entry.setUsername("-") // Otherwise, set username to "-"
	                );
	    });

	    return list;
	}


	public String getRemovedUsername(int userId) {
		String query = "select system_id from user_removed where id=?";
		String username = null;
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(query);
			pStmt.setInt(1, userId);
			rs = pStmt.executeQuery();
			if (rs.next()) {
				username = rs.getString("system_id");
			}
		} catch (SQLException sqle) {
			logger.error(messageResourceBundle.getLogMessage("sql.error.message"), userId, sqle);

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
		return username;
	}

}
