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
import com.hti.smpp.common.request.UserDeliveryForm;
import com.hti.smpp.common.response.DeliveryDTO;
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
//			locale = Customlocale.getLocaleByLanguage(lang);
			Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
			UserEntry user = userOptional.orElseThrow(() -> new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username })));

			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}

			List<DeliveryDTO> reportList = getReportList(customReportForm, username);
			if (reportList != null && !reportList.isEmpty()) {

				logger.info(messageResourceBundle.getLogMessage("report.size.view.message"), user.getSystemId(),
						reportList.size());

				logger.info(messageResourceBundle.getLogMessage("report.size.view.message"), user.getSystemId(),
						reportList.size());

				List<DeliveryDTO> print = getJasperPrint(reportList, false);
				logger.info(messageResourceBundle.getLogMessage("report.finished.message"), user.getSystemId());

				target = IConstants.SUCCESS_KEY;
				return new ResponseEntity<>(reportList, HttpStatus.OK);
				// Return the file in the ResponseEntity
				// return new ResponseEntity<>(pdfReport, headers, HttpStatus.OK);

			} else {
				throw new Exception(messageResourceBundle
						.getExMessage(ConstantMessages.NO_USER_DELIVERY_NOT_REPORT_DATA_FOUND_MESSAGE));

			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalServerException(messageResourceBundle
					.getExMessage(ConstantMessages.NO_DELIVERY_REPORT_DATA_FOUND_MESSAGE, new Object[] { username }));

		}
	}

	@Override
	public ResponseEntity<?> UserDeliveryReportxls(String username, UserDeliveryForm customReportForm,
			HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;

		try {
			

			List<DeliveryDTO> reportList = getReportList(customReportForm, username);
			if (!reportList.isEmpty()) {
				logger.info(messageResourceBundle.getLogMessage("report.size"), reportList.size());

				List<DeliveryDTO> print = getJasperPrint(reportList, false);

				// Update content type for Excel file
				response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				response.setHeader("Content-Disposition", "attachment; filename=UserDlr_"
						+ new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".xlsx");

				OutputStream out = response.getOutputStream();
				JRExporter exporter = new JRXlsxExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
				exporter.setParameter(JRXlsExporterParameter.MAXIMUM_ROWS_PER_SHEET, 60000);
				exporter.exportReport();

				// Close the output stream
				if (out != null) {
					try {
						out.flush(); // Flush before closing
						out.close();
					} catch (IOException ioe) {
						logger.error(messageResourceBundle.getLogMessage("xls.outputstream.error.message"));

					}
				}

				target = IConstants.SUCCESS_KEY;
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(
						ConstantMessages.USER_DELIVERY_REPORT_NOT_FOUND_MESSAGE, new Object[] { username }));

			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			throw new InternalServerException(messageResourceBundle
					.getExMessage(ConstantMessages.ERROR_GETTING_DELIVERY_REPORT_MESSAGE, new Object[] { username }));

		}
		return ResponseEntity.ok(target);
	}

	@Override
	public ResponseEntity<?> UserDeliveryReportPdf(String username, UserDeliveryForm customReportForm,
	        HttpServletResponse response) {
	    String target = IConstants.FAILURE_KEY;

	    try {
	        //Locale locale = Customlocale.getLocaleByLanguage(lang);
	        List<DeliveryDTO> reportList = getReportList(customReportForm, username);
	        if (!reportList.isEmpty()) {
	            logger.info(messageResourceBundle.getMessage("report.size"), reportList.size());

	            List<DeliveryDTO>  print = getJasperPrint(reportList, false);

	            // Update content type for PDF file
	            response.setContentType("application/pdf");
	            response.setHeader("Content-Disposition", "attachment; filename=UserDlr_"
	                    + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".pdf");

	            logger.info(messageResourceBundle.getMessage("creating.pdf.message"));

	            OutputStream out = response.getOutputStream();
	            JRExporter exporter = new JRPdfExporter();
	            exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
	            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
	            exporter.exportReport();

	            // Close the output stream
	            out.flush(); // Flush before closing
	            out.close();

	            target = IConstants.SUCCESS_KEY;
	        } else {
	            throw new NotFoundException(messageResourceBundle.getExMessage(
	                    ConstantMessages.USER_DELIVERY_REPORT_NOT_FOUND_MESSAGE, new Object[]{username}));
	        }
	    } catch (NotFoundException e) {
	        throw new NotFoundException(e.getMessage());
	    } catch (IOException e) {
	        logger.error(messageResourceBundle.getMessage("pdf.outputstream.closing.error.message"), e);
	        // Handle IOException for OutputStream operations
	    } catch (Exception e) {
	        throw new InternalServerException(messageResourceBundle
	                .getExMessage(ConstantMessages.ERROR_GETTING_DELIVERY_REPORT_MESSAGE, new Object[]{username}));
	    }
	    return ResponseEntity.ok(target);
	}

	@Override
	public ResponseEntity<?> UserDeliveryReportDoc(String username, UserDeliveryForm customReportForm,
			HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;

		try {
			//locale = Customlocale.getLocaleByLanguage(lang);

			List<DeliveryDTO> reportList = getReportList(customReportForm, username);
			if (!reportList.isEmpty()) {
				logger.info(messageResourceBundle.getLogMessage("report.size"), reportList.size());
				List<DeliveryDTO>  print = getJasperPrint(reportList, false);

				// Update content type for DOCX file
				response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
				response.setHeader("Content-Disposition", "attachment; filename=UserDlr_"
						+ new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".docx");

				logger.info(messageResourceBundle.getMessage("creating.doc.message"), username);
				logger.info(messageResourceBundle.getLogMessage("creating.doc.message"), username);

				OutputStream out = response.getOutputStream();
				JRExporter exporter = new JRDocxExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.exportReport();

				// Close the output stream
				if (out != null) {
					try {
						out.flush(); // Flush before closing
						out.close();
					} catch (IOException ioe) {
						logger.error(messageResourceBundle.getLogMessage("docx.outputstream.closing.error.message"));

					}
				}

				target = IConstants.SUCCESS_KEY;
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(
						ConstantMessages.USER_DELIVERY_REPORT_NOT_FOUND_MESSAGE, new Object[] { username }));
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			throw new InternalServerException(messageResourceBundle
					.getExMessage(ConstantMessages.ERROR_GETTING_DELIVERY_REPORT_MESSAGE, new Object[] { username }));
		}
		return ResponseEntity.ok(target);
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

//		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(final_list);
//		Map parameters = new HashMap();
//		parameters.put(JRParameter.IS_IGNORE_PAGINATION, paging);
//		ResourceBundle bundle = ResourceBundle.getBundle("JSReportLabels", locale);
//		parameters.put("REPORT_RESOURCE_BUNDLE", bundle);
//		JasperPrint print = JasperFillManager.fillReport(report, parameters, beanColDataSource);
		return final_list;
	}

	private List<DeliveryDTO> getReportList(UserDeliveryForm customReportForm, String username)
			throws Exception {
		List<DeliveryDTO> list = null;
		// int back_day = 1;
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
				// String date = rs.getString("date");
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
