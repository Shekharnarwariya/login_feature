package com.hti.smpp.common.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder.EntryObject;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.request.PaginationRequest;
import com.hti.smpp.common.request.UserDeliveryForm;
import com.hti.smpp.common.response.DeliveryDTO;
import com.hti.smpp.common.response.PaginatedResponse;
import com.hti.smpp.common.service.UserDeliveryReportService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.Customlocale;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;

import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

@Service
public class UserDeliveryReportServiceImpl implements UserDeliveryReportService {

	private String template_file = IConstants.FORMAT_DIR + "report//UserDeliveryStatus.jrxml";
	private Logger logger = LoggerFactory.getLogger(UserDeliveryReportServiceImpl.class);
	Locale locale = new Locale("en", "US");

	@Autowired
	private DataSource dataSource;
	@Autowired
	private UserEntryRepository userRepository;
	@Autowired
	private MessageResourceBundle messageResourceBundle;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public ResponseEntity<?> UserDeliveryReportView(String username, UserDeliveryForm customReportForm) {
	    String target = IConstants.FAILURE_KEY;
	    try {
	        Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
	        UserEntry user = userOptional.orElseThrow(() -> new NotFoundException(
	                messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[]{username})));

	        if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
	            throw new UnauthorizedException(messageResourceBundle
	                    .getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[]{username}));
	        }

	        List<DeliveryDTO> reportList = getReportList(customReportForm, username);
	        if (reportList == null || reportList.isEmpty()) {logger.info(user.getSystemId() + " ReportSize[View]:" + reportList.size());
			
			return ResponseEntity.ok(reportList);
			} else {
				throw new NotFoundException(
						messageResourceBundle.getExMessage(ConstantMessages.USER_DELIVERY_REPORT_NOT_FOUND_MESSAGE));

			}
	    } catch (NotFoundException | UnauthorizedException e) {
	       
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
	    } catch (Exception e) {
	       
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Process error occurred");
	    }
	}



	private List<DeliveryDTO> getJasperPrint(List<DeliveryDTO> reportList, boolean paging) throws JRException {

		Map<String, DeliveryDTO> key_map = new HashMap<String, DeliveryDTO>();
		for (DeliveryDTO chartDTO : reportList) {
			// ----------- report Data ----------------
			String key = chartDTO.getUsername() + chartDTO.getSender() + chartDTO.getCountry() + chartDTO.getOperator();
			DeliveryDTO reportDTO = null;
			if (key_map.containsKey(key)) {
				reportDTO = (DeliveryDTO) key_map.get(key);
			} else {
				reportDTO = new DeliveryDTO(chartDTO.getCountry(), chartDTO.getOperator(), chartDTO.getDate());
			}
			reportDTO.setUsername(chartDTO.getUsername());
			reportDTO.setSender(chartDTO.getSender());
			reportDTO.setCost(reportDTO.getCost() + chartDTO.getCost());
			int submitted = chartDTO.getStatusCount() + reportDTO.getSubmitted();
			reportDTO.setSubmitted(submitted);
			if (chartDTO.getStatus().startsWith("DELIV")) {
				int delivered = chartDTO.getStatusCount() + reportDTO.getDelivered();
				reportDTO.setDelivered(delivered);
			} else if (chartDTO.getStatus().startsWith("ATES")) {
				int pending = chartDTO.getStatusCount() + reportDTO.getPending();
				reportDTO.setPending(pending);
			} else {
				if (chartDTO.getStatus().startsWith("QUEUED")) {
					submitted = reportDTO.getSubmitted() - chartDTO.getStatusCount();
					reportDTO.setSubmitted(submitted);
				} else {
					int undelivered = chartDTO.getStatusCount() + reportDTO.getUndelivered();
					reportDTO.setUndelivered(undelivered);
				}
			}
			key_map.put(key, reportDTO);
		}
		List<DeliveryDTO> final_list = new ArrayList<DeliveryDTO>(key_map.values());
		final_list = sortListByCountry(final_list);
		// -------------------------------------------------------------
		logger.info(messageResourceBundle.getLogMessage("preparing.report.message"));


		return final_list;
	}

	private List<DeliveryDTO> getReportList(UserDeliveryForm customReportForm, String username)
			throws Exception {
		List<DeliveryDTO> list = null;
		String start_date = customReportForm.getStartDate();
		String last_date = customReportForm.getEndDate();
		String country = customReportForm.getCountry();
		String operator = customReportForm.getOperator();
		// String username = String.join("','", customReportForm.getUsername());
		if (start_date == null || start_date.length() == 0) {
			start_date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		}
		if (last_date == null || last_date.length() == 0) {
			last_date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		}
		String sql = "select total as count,username,oprCountry,sender,status,cost from report_summary where username in('"
				+ username + "') and time ";
		if (start_date.equalsIgnoreCase(last_date)) {
			sql += "='" + start_date + "' ";
		} else {
			sql += "between '" + start_date + "' and '" + last_date + "' ";
		}
		if (!country.equalsIgnoreCase("All")) {
			Predicate<Integer, NetworkEntry> p = null;
			String oprCountry = "";
			if (operator.equalsIgnoreCase("All")) {
				p = new PredicateBuilderImpl().getEntryObject().get("mcc").equal(country);
			} else {
				EntryObject e = new PredicateBuilderImpl().getEntryObject();
				p = e.get("mcc").equal(country).and(e.get("mnc").equal(operator));
			}
			// Map networkmap = dbService.getNetworkRecord(country, operator);
			// Iterator itr = networkmap.keySet().iterator();
			for (int cc : GlobalVars.NetworkEntries.keySet(p)) {
				oprCountry += "'" + cc + "',";
			}
			if (oprCountry.length() > 0) {
				oprCountry = oprCountry.substring(0, oprCountry.length() - 1);
				sql += "and oprCountry in (" + oprCountry + ") ";
			}
		}
		logger.info("SQL: " + sql);
		list = getUserDeliveryReport(sql);
		return list;
	}

	public List getUserDeliveryReport(String sql) {
		List list = new ArrayList();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		DeliveryDTO report = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(sql);
			rs = pStmt.executeQuery();
			while (rs.next()) {
				String username = rs.getString("username");
				String sender = rs.getString("sender");
				String oprCountry = rs.getString("oprCountry");
				String status = "QUEUED";
				if (rs.getString("status") != null) {
					if (!status.equalsIgnoreCase("Q")) {
						status = rs.getString("status");
					}
				}
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
				report = new DeliveryDTO(country, operator, null);
				report.setUsername(username);
				report.setSender(sender);
				report.setStatus(status);
				report.setCost(rs.getDouble("cost"));
				report.setStatusCount(rs.getInt("count"));
				list.add(report);
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
				if (con != null) {
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return list;
	}

	private static List<DeliveryDTO> sortListByCountry(List<DeliveryDTO> list) {
		Comparator<DeliveryDTO> comparator = null;
		comparator = Comparator.comparing(DeliveryDTO::getUsername).thenComparing(DeliveryDTO::getCountry)
				.thenComparing(DeliveryDTO::getOperator);
		Stream<DeliveryDTO> personStream = list.stream().sorted(comparator);
		List<DeliveryDTO> sortedlist = personStream.collect(Collectors.toList());
		return sortedlist;
	}

}