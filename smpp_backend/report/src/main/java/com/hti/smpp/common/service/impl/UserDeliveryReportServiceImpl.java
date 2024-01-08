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
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder.EntryObject;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.DeliveryDTO;
import com.hti.smpp.common.service.UserDeliveryReportService;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;

import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

@Service
public class UserDeliveryReportServiceImpl implements UserDeliveryReportService {

	private String template_file = IConstants.FORMAT_DIR + "report//UserDeliveryStatus.jrxml";
	private Logger logger = LoggerFactory.getLogger(UserDeliveryReportServiceImpl.class);
	Locale locale = new Locale("en", "US"); 

	@Autowired
	private DataSource dataSource;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public List<DeliveryDTO> UserDeliveryReportView(String username, CustomReportForm customReportForm) {
		String target = IConstants.FAILURE_KEY;

		try {

			List<DeliveryDTO> reportList = getReportList(customReportForm);
			if (!reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());
				target = IConstants.SUCCESS_KEY;
				return reportList;
			} else {
				throw new NotFoundException("user delivery report not found with username {}" + username);
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			throw new InternalServerException("Error: getting error in delivery report with username {}" + username);
		}
	}

	@Override
	public String UserDeliveryReportxls(String username, CustomReportForm customReportForm,
			HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;

		try {
			List<DeliveryDTO> reportList = getReportList(customReportForm);
			if (!reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());
				JasperPrint print = getJasperPrint(reportList, false);
				System.out.println("<-- Preparing Outputstream --> ");
				String reportName = "UserDlr_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".xlsx";
				response.setContentType("text/html; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				System.out.println("<-- Creating XLS --> ");
				OutputStream out = response.getOutputStream();
				JRExporter exporter = new JRXlsxExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
				exporter.setParameter(JRXlsExporterParameter.MAXIMUM_ROWS_PER_SHEET, 60000);
				exporter.exportReport();
				if (out != null) {
					try {
						out.close();
					} catch (IOException ioe) {
						System.out.println("XLS OutPutSream Closing Error");
					}
				}
				System.out.println("<-- Finished --> ");
				target = IConstants.SUCCESS_KEY;
			} else {
				throw new NotFoundException("user delivery report not found with username {}" + username);
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			throw new InternalServerException("Error: getting error in delivery report with username {}" + username);
		}
		return target;

	}

	@Override
	public String UserDeliveryReportPdf(String username, CustomReportForm customReportForm,
			HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;

		try {
			List<DeliveryDTO> reportList = getReportList(customReportForm);
			if (!reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());
				JasperPrint print = getJasperPrint(reportList, false);
				System.out.println("<-- Preparing Outputstream --> ");
				String reportName = "UserDlr_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".pdf";
				response.setContentType("text/html; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				System.out.println("<-- Creating PDF --> ");
				OutputStream out = response.getOutputStream();
				JRExporter exporter = new JRPdfExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.exportReport();
				if (out != null) {
					try {
						out.close();
					} catch (IOException ioe) {
						System.out.println("PDF OutPutSream Closing Error");
					}
				}
				System.out.println("<-- Finished --> ");
				target = IConstants.SUCCESS_KEY;
			} else {
				throw new NotFoundException("user delivery report not found with username {}" + username);
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			throw new InternalServerException("Error: getting error in delivery report with username {}" + username);
		}
		return target;
	}

	@Override
	public String UserDeliveryReportDoc(String username, CustomReportForm customReportForm,
			HttpServletResponse response) {
		String target = IConstants.FAILURE_KEY;

		try {
			List<DeliveryDTO> reportList = getReportList(customReportForm);
			if (!reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());
				JasperPrint print = getJasperPrint(reportList, false);
				System.out.println("<-- Preparing Outputstream --> ");
				String reportName = "UserDlr_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".doc";
				response.setContentType("text/html; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				System.out.println("<-- Creating DOC --> ");
				OutputStream out = response.getOutputStream();
				JRExporter exporter = new JRDocxExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.exportReport();
				if (out != null) {
					try {
						out.close();
					} catch (IOException ioe) {
						System.out.println("DOC OutPutSream Closing Error");
						throw new NotFoundException("user delivery report not found with username {}" + username);
					}
				}
				System.out.println("<-- Finished --> ");
				target = IConstants.SUCCESS_KEY;
			} else {
				throw new NotFoundException("user delivery report not found with username {}" + username);
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			throw new InternalServerException("Error: getting error in delivery report with username {}" + username);
		}
		return target;
	}

	private JasperPrint getJasperPrint(List<DeliveryDTO> reportList, boolean paging) throws JRException {
		System.out.println("Creating Design");
		JasperDesign design = JRXmlLoader.load(template_file);
		System.out.println("<--- Compiling -->");
		JasperReport report = JasperCompileManager.compileReport(design);
		// ------------- Preparing databeancollection for chart ------------------
		System.out.println("<-- Preparing Charts --> ");
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
		System.out.println("<-- Preparing report --> ");
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(final_list);
		Map parameters = new HashMap();
		parameters.put(JRParameter.IS_IGNORE_PAGINATION, paging);
		ResourceBundle bundle = ResourceBundle.getBundle("JSReportLabels", locale);
		parameters.put("REPORT_RESOURCE_BUNDLE", bundle);
		JasperPrint print = JasperFillManager.fillReport(report, parameters, beanColDataSource);
		return print;
	}

	private List<DeliveryDTO> getReportList(CustomReportForm customReportForm) throws Exception {
		List<DeliveryDTO> list = null;
		// int back_day = 1;
		String start_date = customReportForm.getSday();
		String last_date = customReportForm.getEday();
		String country = customReportForm.getCountry();
		String operator = customReportForm.getOperator();
		String usernames = String.join("','", customReportForm.getUsernames());
		if (start_date == null || start_date.length() == 0) {
			start_date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		}
		if (last_date == null || last_date.length() == 0) {
			last_date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		}
		String sql = "select total as count,username,oprCountry,sender,status,cost from report_summary where username in('"
				+ usernames + "') and time ";
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

	public List getUserDeliveryReport(String query) {
		List list = new ArrayList();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		DeliveryDTO report = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(query);
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
