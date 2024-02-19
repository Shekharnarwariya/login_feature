package com.hti.smpp.common.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

@Service
public class ProfitReportServiceImpl implements ProfitReportService {
	@Autowired
	private UserEntryRepository userRepository;

	private Logger logger = LoggerFactory.getLogger(UserDeliveryReportServiceImpl.class);

	@Autowired
	private DataSource dataSource;

	@Autowired
	private MessageResourceBundle messageResourceBundle;

	@Autowired
	private ReportDAOImpl reportDAOImpl;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public ResponseEntity<?> ProfitReportview(String username, ProfitReportRequest customReportForm, String lang) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (!userOptional.isPresent()) {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}
		UserEntry user = userOptional.get();
		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION,
					new Object[] { username }));
		}
		try {
			List<ProfitReportEntry> profitReportList = getProfitReportList(customReportForm, false, user);
			if (profitReportList != null && !profitReportList.isEmpty()) {
				logger.info(messageResourceBundle.getLogMessage("report.size.view.message"), user.getSystemId(),
						profitReportList.size());
				return new ResponseEntity<>(profitReportList, HttpStatus.OK);
			} else {
				throw new NotFoundException(messageResourceBundle
						.getExMessage(ConstantMessages.PROFIT_REPORT_NOT_FOUND_MESSAGE, new Object[] { username }));

			}
		} catch (NotFoundException ex) {
			throw new NotFoundException(ex.getMessage());
		} catch (Exception ex) {
			throw new InternalServerException(messageResourceBundle
					.getExMessage(ConstantMessages.ERROR_GETTING_PROFIT_REPORT_MESSAGE, new Object[] { username }));

		}
	}

	@Override
	public ResponseEntity<?> ProfitReportxls(String username, ProfitReportRequest customReportForm, String lang,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<?> ProfitReportpdf(String username, ProfitReportRequest customReportForm, String lang,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<?> ProfitReportdoc(String username, ProfitReportRequest customReportForm, String lang,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public ResponseEntity<?> ProfitReportxls(String username, ProfitReportRequest customReportForm, String lang,
//			HttpServletResponse response) {
//
//		String target = IConstants.FAILURE_KEY;
//		try {
//			locale = Customlocale.getLocaleByLanguage(lang);
//			JasperPrint print = null;// getProfitReportList(customReportForm, false, lang, username);
//			if (print != null) {
//				logger.info(messageResourceBundle.getLogMessage("preparing.outputstream.message"), username);
//
//				String reportName = "Profit_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0)) + ".xlsx";
//				response.setContentType("text/html; charset=utf-8");
//				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
//
//				logger.info(messageResourceBundle.getLogMessage("creating.xls.message"), username);
//
//				// OutputStream out = response.getOutputStream();
//				ByteArrayOutputStream out = new ByteArrayOutputStream();
//				JRExporter exporter = new JRXlsxExporter();
//				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
//				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
//				exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
//				exporter.setParameter(JRXlsExporterParameter.MAXIMUM_ROWS_PER_SHEET, 60000);
//				exporter.exportReport();
//				if (out != null) {
//					try {
//						out.close();
//					} catch (Exception ioe) {
//						logger.error(messageResourceBundle.getLogMessage("xls.outputstream.error.message"), username);
//
//					}
//				}
//				logger.error(messageResourceBundle.getLogMessage("finish.message"));
//				target = IConstants.SUCCESS_KEY;
//				return ResponseEntity.ok().body(out.toByteArray());
//			} else {
//				throw new NotFoundException(messageResourceBundle
//						.getExMessage(ConstantMessages.PROFIT_REPORT_XLS_NOT_FOUND_MESSAGE, new Object[] { username }));
//
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new InternalServerException(messageResourceBundle
//					.getExMessage(ConstantMessages.ERROR_GETTING_PROFIT_REPORT_XLS_MESSAGE, new Object[] { username }));
//
//		}
////		return target;
//	}
//
//	@Override
//	public ResponseEntity<?> ProfitReportpdf(String username, ProfitReportRequest customReportForm, String lang,
//			HttpServletResponse response) {
//		String target = IConstants.FAILURE_KEY;
//
//		try {
//			locale = Customlocale.getLocaleByLanguage(lang);
//
//			JasperPrint print = null;// getProfitReportList(customReportForm, false, lang, username);
//			if (print != null) {
//				logger.info(messageResourceBundle.getLogMessage("preparing.outputstream.message"), username);
//				String reportName = "profit_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0)) + ".pdf";
//				response.setContentType("text/html; charset=utf-8");
//				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
//				logger.info(messageResourceBundle.getLogMessage("creating.pdf.message"));
//
//				OutputStream out = response.getOutputStream();
//				JRExporter exporter = new JRPdfExporter();
//				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
//				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
//				exporter.exportReport();
//				if (out != null) {
//					try {
//						out.close();
//					} catch (Exception e) {
//						logger.error(messageResourceBundle.getLogMessage("pdf.outputstream.closing.error.message"),
//								username);
//
//					}
//				}
//				logger.error(messageResourceBundle.getLogMessage("finish.message"));
//				target = IConstants.SUCCESS_KEY;
//				// return new ResponseEntity<>(response, out, HttpStatus.OK);
//			} else {
//				throw new NotFoundException(messageResourceBundle
//						.getExMessage(ConstantMessages.PROFIT_REPORT_PDF_NOT_FOUND_MESSAGE, new Object[] { username }));
//
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new InternalServerException(messageResourceBundle
//					.getExMessage(ConstantMessages.ERROR_GETTING_PROFIT_REPORT_PDF_MESSAGE, new Object[] { username }));
//
//		}
//		return null;
//	}
//
//	@Override
//	public ResponseEntity<?> ProfitReportdoc(String username, ProfitReportRequest customReportForm, String lang,
//			HttpServletResponse response) {
//		String target = IConstants.FAILURE_KEY;
//
//		try {
//			locale = Customlocale.getLocaleByLanguage(lang);
//			JasperPrint print = null;// getProfitReportList(customReportForm, false, lang, username);
//			if (print != null) {
//				logger.info(messageResourceBundle.getLogMessage("preparing.outputstream.message"), username);
//				String reportName = "profit_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0)) + ".doc";
//				response.setContentType("text/html; charset=utf-8");
//				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
//				logger.info(messageResourceBundle.getLogMessage("creating.doc.message"));
//
//				OutputStream out = response.getOutputStream();
//				JRExporter exporter = new JRDocxExporter();
//				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
//				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
//				exporter.exportReport();
//				if (out != null) {
//					try {
//						out.close();
//					} catch (Exception ioe) {
//						logger.error(messageResourceBundle.getLogMessage("doc.outputstream.closing.error.message"),
//								username);
//
//					}
//				}
//				logger.error(messageResourceBundle.getLogMessage("finish.message"));
//				target = IConstants.SUCCESS_KEY;
//			} else {
//				throw new NotFoundException(messageResourceBundle
//						.getExMessage(ConstantMessages.PROFIT_REPORT_DOC_NOT_FOUND_MESSAGE, new Object[] { username }));
//
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new InternalServerException(messageResourceBundle
//					.getExMessage(ConstantMessages.ERROR_GETTING_PROFIT_REPORT_PDF_MESSAGE, new Object[] { username }));
//
//		}
//
//		return null;
//	}
//
	private List<ProfitReportEntry> getProfitReportList(ProfitReportRequest customReportForm, boolean paging,
			UserEntry user) throws Exception {
		String[] start_date = customReportForm.getStartDate().split("-");
		String[] end_date = customReportForm.getEndDate().split("-");
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DATE, Integer.parseInt(end_date[2]));
		calendar.set(Calendar.MONTH, (Integer.parseInt(end_date[1])) - 1);
		calendar.set(Calendar.YEAR, Integer.parseInt(end_date[0]));
		calendar.add(Calendar.DATE, 1);
		String end_date_str = new SimpleDateFormat("yyMMdd").format(calendar.getTime()) + "0000000000000";
		String start_date_str = (start_date[0].substring(2)) + "" + start_date[1] + "" + start_date[2]
				+ "0000000000000";
		ReportCriteria rc = new ReportCriteria();
		rc.setStartMsgId(Long.parseLong(start_date_str));
		rc.setEndMsgId(Long.parseLong(end_date_str));
		if (user.getRole().equalsIgnoreCase("superadmin")) {
			rc.setResellerId(Integer.parseInt(customReportForm.getClientId()));
		} else {
			rc.setResellerId(user.getId());
			if (!customReportForm.getClientId().equalsIgnoreCase("0")) {
				rc.setUserId(Integer.parseInt(customReportForm.getClientId()));
			}
		}

		List<ProfitReportEntry> list = listProfitReport(rc);
		if (list.isEmpty()) {
			logger.info(messageResourceBundle.getLogMessage("no.report.data.found.message"), user.getSystemId());

			return null;
		}
		return list;
	}

//
//	private static boolean isValidDate(int year, int month, int day) {
//		try {
//			LocalDate.of(year, month, day);
//			return true;
//		} catch (DateTimeException e) {
//			return false;
//		}
//	}
//
//	private <K, V extends Comparable<? super V>> Map<K, V> sortByDscValue(Map<K, V> map, int limit) {
//		Map<K, V> result = new LinkedHashMap<>();
//		Stream<Map.Entry<K, V>> st = map.entrySet().stream();
//		st.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(limit)
//				.forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
//		return result;
//	}
//
//	private List<ProfitReportEntry> sortListByUser(List<ProfitReportEntry> list) {
//		// logger.info(userSessionObject.getSystemId() + " sortListByUser ");
//		Comparator<ProfitReportEntry> comparator = null;
//		comparator = Comparator.comparing(ProfitReportEntry::getUsername);
//		Stream<ProfitReportEntry> personStream = list.stream().sorted(comparator);
//		List<ProfitReportEntry> sortedlist = personStream.collect(Collectors.toList());
//		return sortedlist;
//	}
//
//	private List<ProfitReportEntry> sortListByNetwork(List<ProfitReportEntry> list) {
//
//		System.out.println(list);
//		Comparator<ProfitReportEntry> comparator = null;
//		comparator = Comparator.comparing(ProfitReportEntry::getCountry).thenComparing(ProfitReportEntry::getOperator);
//		Stream<ProfitReportEntry> personStream = list.stream().sorted(comparator);
//		List<ProfitReportEntry> sortedlist = personStream.collect(Collectors.toList());
//		System.out.println("sortedlist" + sortedlist);
//
//		return sortedlist;
//	}
//
	public List<ProfitReportEntry> listProfitReport(ReportCriteria rc) {
		List<ProfitReportEntry> list = reportDAOImpl.listProfitReport(rc);
		for (ProfitReportEntry entry : list) {
			if (GlobalVars.NetworkEntries.containsKey(entry.getNetworkId())) {
				entry.setCountry(GlobalVars.NetworkEntries.get(entry.getNetworkId()).getCountry());
				entry.setOperator(GlobalVars.NetworkEntries.get(entry.getNetworkId()).getOperator());
			}
			UserEntry userEntry = userRepository.findById(entry.getUserId()).get();
			if (userEntry != null) {
				entry.setUsername(userEntry.getSystemId());

			} else {
				String username = getRemovedUsername(entry.getUserId());
				if (username != null) {
					entry.setUsername(username);
				} else {
					entry.setUsername("-");
				}
			}
		}

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
