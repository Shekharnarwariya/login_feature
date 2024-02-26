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

	@Override
	public ResponseEntity<?> ProfitReportxls(String username, ProfitReportRequest customReportForm,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<?> ProfitReportpdf(String username, ProfitReportRequest customReportForm,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<?> ProfitReportdoc(String username, ProfitReportRequest customReportForm, 
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

	public Page<ProfitReportEntry> listProfitReport(ReportCriteria rc, Pageable pageable) {
	    // Retrieve the list of profit report entries from the DAO
	    Page<ProfitReportEntry> list = reportDAOImpl.listProfitReport(rc,  pageable);

	    // Enhanced loop with more robust null handling and leveraging Java 8 features
	    list.forEach(entry -> {
	        // Update country and operator based on network ID, if available
	        Optional.ofNullable(GlobalVars.NetworkEntries.get(entry.getNetworkId()))
	                .ifPresent(networkEntry -> {
	                    entry.setCountry(networkEntry.getCountry());
	                    entry.setOperator(networkEntry.getOperator());
	                });

	        // Attempt to set the username from the UserEntry if present; otherwise, use a removed username or default to "-"
	        userRepository.findById(entry.getUserId())
	                .map(UserEntry::getSystemId)  // If UserEntry is present, get the system ID
	                .or(() -> Optional.ofNullable(getRemovedUsername(entry.getUserId()))) // If not, try getting removed username
	                .ifPresentOrElse(
	                        entry::setUsername, // If a username is found, set it
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
