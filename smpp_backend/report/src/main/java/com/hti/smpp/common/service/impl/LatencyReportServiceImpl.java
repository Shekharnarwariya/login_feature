package com.hti.smpp.common.service.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.apache.catalina.Globals;
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
import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.sales.repository.SalesRepository;
import com.hti.smpp.common.service.LatencyReportService;
import com.hti.smpp.common.service.UserDAService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.util.Customlocale;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.logica.smpp.Session;

import java.io.OutputStream;

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
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

@Service
public class LatencyReportServiceImpl implements LatencyReportService {

	private String template_file = IConstants.FORMAT_DIR + "report//UserDeliveryStatus.jrxml";
	private Logger logger = LoggerFactory.getLogger(LatencyReportServiceImpl.class);
	Locale locale = new Locale("en", "US");

	@Autowired
	private DataSource dataSource;

	@Autowired
	private SalesRepository salesRepository;
	@Autowired
	private UserDAService userService;
	@Autowired
	private UserEntryRepository userRepository;
	@Autowired
	private WebMasterEntryRepository webMasterEntryRepository;

	String to_gmt;
	String from_gmt;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();

	}

	@Override
	public List<DeliveryDTO> LatencyReportView(String username, CustomReportForm customReportForm,String lang) {
		String target = IConstants.FAILURE_KEY;
		try {
			List<DeliveryDTO> reportList = null;
			if (customReportForm.getSmscnames() != null && customReportForm.getSmscnames().length > 0) {
				reportList = getSmscReportList(customReportForm, username);
			} else {
				reportList = getReportList(customReportForm, username);
			}
			if (reportList != null && !reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());
				JasperPrint print = null;
				if (customReportForm.getSmscnames() != null && customReportForm.getSmscnames().length > 0) {
					print = getJasperPrint(reportList, false,
							IConstants.FORMAT_DIR + "report//SmscLatencyReport.jrxml");
				} else {
					print = getJasperPrint(reportList, false, IConstants.FORMAT_DIR + "report//LatencyReport.jrxml");
				}

			} else {

				throw new NotFoundException("user delivery report not found with username {}" + username);
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			throw new InternalServerException("Error: getting error in delivery report with username {}" + username);
		}
		return null;

	}

	@Override
	public String LatencyReportxls(String username, CustomReportForm customReportForm, HttpServletResponse response,String lang) {
		String target = IConstants.FAILURE_KEY;
		try {
			 locale =locale = Customlocale.getLocaleByLanguage(lang);
			List<DeliveryDTO> reportList = null;
			if (customReportForm.getSmscnames() != null && customReportForm.getSmscnames().length > 0) {
				reportList = getSmscReportList(customReportForm, username);
			} else {
				reportList = getReportList(customReportForm, username);
			}
			if (reportList != null && !reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());
				JasperPrint print = null;
				if (customReportForm.getSmscnames() != null && customReportForm.getSmscnames().length > 0) {
					print = getJasperPrint(reportList, false,
							IConstants.FORMAT_DIR + "report//SmscLatencyReport.jrxml");
				} else {
					print = getJasperPrint(reportList, false, IConstants.FORMAT_DIR + "report//LatencyReport.jrxml");
				}
				System.out.println("<-- Preparing Outputstream --> ");
				String reportName = "latency_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0)) + ".xlsx";
				response.setContentType("text/html; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
				System.out.println("<-- Creating XLS --> ");
				OutputStream out = response.getOutputStream();
				JRExporter exporter = new JRXlsxExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
				exporter.exportReport();
				if (out != null) {
					try {
						out.close();
					} catch (IOException ioe) {
						System.out.println("XLS OutPutSream Closing Error");
					}
				}
			} else {

				throw new NotFoundException("user delivery report not found with username {}" + username);
			}
		} catch (Exception e) {
			throw new InternalServerException("Error: getting error in delivery report with username {}" + username);
		}
		return target;

	}

	@Override
	public String LatencyReportpdf(String username, CustomReportForm customReportForm, HttpServletResponse response,String lang) {
		String target = IConstants.FAILURE_KEY;
		try {
			 locale = locale = Customlocale.getLocaleByLanguage(lang); ;
			List<DeliveryDTO> reportList = null;
			if (customReportForm.getSmscnames() != null && customReportForm.getSmscnames().length > 0) {
				reportList = getSmscReportList(customReportForm, username);
			} else {
				reportList = getReportList(customReportForm, username);
			}
			if (reportList != null && !reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());
				JasperPrint print = null;
				if (customReportForm.getSmscnames() != null && customReportForm.getSmscnames().length > 0) {
					print = getJasperPrint(reportList, false,
							IConstants.FORMAT_DIR + "report//SmscLatencyReport.jrxml");
				} else {
					print = getJasperPrint(reportList, false, IConstants.FORMAT_DIR + "report//LatencyReport.jrxml");
				}
				System.out.println("<-- Preparing Outputstream --> ");
				String reportName = "latency_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0)) + ".pdf";
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
	public String LatencyReportdoc(String username, CustomReportForm customReportForm, HttpServletResponse response,
			String lang) {
		String target = IConstants.FAILURE_KEY;

		try {
			locale = Customlocale.getLocaleByLanguage(lang);
			List<DeliveryDTO> reportList = null;
			if (customReportForm.getSmscnames() != null && customReportForm.getSmscnames().length > 0) {
				reportList = getSmscReportList(customReportForm, username);
			} else {
				reportList = getReportList(customReportForm, username);
			}
			if (reportList != null && !reportList.isEmpty()) {
				System.out.println("Report Size: " + reportList.size());
				JasperPrint print = null;
				if (customReportForm.getSmscnames() != null && customReportForm.getSmscnames().length > 0) {
					print = getJasperPrint(reportList, false,
							IConstants.FORMAT_DIR + "report//SmscLatencyReport.jrxml");
				} else {
					print = getJasperPrint(reportList, false, IConstants.FORMAT_DIR + "report//LatencyReport.jrxml");
				}
				System.out.println("<-- Preparing Outputstream --> ");
				String reportName = "latency_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date(0)) + ".doc";
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
					}
				}
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

	private JasperPrint getJasperPrint(List<DeliveryDTO> reportList, boolean paging, String template_file)
			throws JRException {
		System.out.println("<-- Creating Design ---> ");
		JasperDesign design = JRXmlLoader.load(template_file);
		System.out.println("<-- Compiling Source Format file ---> ");
		JasperReport report = JasperCompileManager.compileReport(design);
		System.out.println("<-- Preparing Chart Data ---> ");
		List<DeliveryDTO> chart_list = new ArrayList<DeliveryDTO>();
		Map<String, Integer> chart_map = new HashMap<>();
		chart_map.put("0-5", 0);
		chart_map.put("6-15", 0);
		chart_map.put("16-30", 0);
		chart_map.put("31-45", 0);
		chart_map.put("46-60", 0);
		chart_map.put("> 60", 0);
		for (DeliveryDTO chartDTO : reportList) {
			int count = chart_map.get("0-5");
			count = count + chartDTO.getLatency1();
			chart_map.put("0-5", count);
			count = chart_map.get("6-15");
			count = count + chartDTO.getLatency2();
			chart_map.put("6-15", count);
			count = chart_map.get("16-30");
			count = count + chartDTO.getLatency3();
			chart_map.put("16-30", count);
			count = chart_map.get("31-45");
			count = count + chartDTO.getLatency4();
			chart_map.put("31-45", count);
			count = chart_map.get("46-60");
			count = count + chartDTO.getLatency5();
			chart_map.put("46-60", count);
			count = chart_map.get("> 60");
			count = count + chartDTO.getLatency6();
			chart_map.put("> 60", count);
		}
		for (Map.Entry<String, Integer> entry : chart_map.entrySet()) {
			chart_list.add(new DeliveryDTO(entry.getValue(), entry.getKey()));
		}
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(reportList);
		JRBeanCollectionDataSource piechartDataSource = new JRBeanCollectionDataSource(chart_list);
		Map parameters = new HashMap();
		parameters.put("piechartDataSource", piechartDataSource);
		if (reportList.size() > 20000) {
			JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(1000,
					new JRSwapFile(IConstants.WEBAPP_DIR + "temp//", 2048, 1024));
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
		}
		parameters.put(JRParameter.IS_IGNORE_PAGINATION, paging);
		ResourceBundle bundle = ResourceBundle.getBundle("JSReportLabels", locale);
		parameters.put("REPORT_RESOURCE_BUNDLE", bundle);
		JasperPrint print = JasperFillManager.fillReport(report, parameters, beanColDataSource);
		return print;
	}

	private List<DeliveryDTO> getReportList(CustomReportForm customReportForm, String username) {

		UserDAService userDAService = new UserDAServiceImpl();
		if (customReportForm.getClientId() == null) {
			return null;
		}
		Optional<UserEntry> usersOptional = userRepository.findBySystemId(username);
		if (!usersOptional.isPresent()) {
			throw new NotFoundException("user not fout with system id" + username);
		}

		UserEntry user = usersOptional.get();
		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(user.getId());
		if (webMasterEntry == null) {
			throw new NotFoundException("web MasterEntry  not fount username {}" + user.getId());
		}
		if (customReportForm.getClientId() == null) {
			return null;
		}
		logger.info(username + " Report Based On Criteria");
		List<String> users = null;
		String query = null;
		// IDatabaseService dbService = HtiSmsDB.getInstance();
		String senderId = customReportForm.getSenderId();
		String country = customReportForm.getCountry();
		String operator = customReportForm.getOperator();
		String startDate = customReportForm.getSday();
		String endDate = customReportForm.getEday();
		List<DeliveryDTO> list = null;
		List<DeliveryDTO> final_list = new ArrayList<DeliveryDTO>();
		if (!webMasterEntry.getGmt().equalsIgnoreCase(IConstants.DEFAULT_GMT)) {
			to_gmt = webMasterEntry.getGmt().replace("GMT", "");
			from_gmt = IConstants.DEFAULT_GMT.replace("GMT", "");
		}
		if (customReportForm.getClientId().equalsIgnoreCase("All")) {
			String role = user.getRole();
			if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")) {
				users = new ArrayList<String>(userService.listUsers().values());
			} else if (role.equalsIgnoreCase("admin")) {
				users = new ArrayList<String>(userService.listUsersUnderMaster(user.getSystemId()).values());
				users.add(username);
				Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
						.get("secondaryMaster").equal(username);
				for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(p)) {
					UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
					users.add(userEntry.getSystemId());
				}
			} else if (role.equalsIgnoreCase("seller")) {
				users = new ArrayList<String>(userService.listUsersUnderSeller(user.getId()).values());
			} else if (role.equalsIgnoreCase("manager")) {
				// SalesDAService salesService = new SalesDAServiceImpl();
				listUsernamesUnderManager(user.getSystemId()).values();
			}
			logger.info(username + " Under Users: " + users.size());
		} else {
			users = new ArrayList<String>();
			users.add(customReportForm.getClientId());
		}
		if (users != null && !users.isEmpty()) {
			while (!users.isEmpty()) {
				String report_user = (String) users.remove(0);
				logger.info(username + " Checking Report For " + report_user);
				query = "select count(msg_id) as count,source_no,oprcountry,status,TIME_TO_SEC(TIMEDIFF(deliver_time,submitted_time)) as latency from mis_"
						+ report_user + " where status not like 'ATES' and ";
				if (to_gmt != null) {
					SimpleDateFormat client_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					client_formatter.setTimeZone(TimeZone.getTimeZone(webMasterEntry.getGmt()));
					SimpleDateFormat local_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try {
						String start_msg_id = local_formatter.format(client_formatter.parse(startDate));
						String end_msg_id = local_formatter.format(client_formatter.parse(endDate));
						start_msg_id = start_msg_id.replaceAll("-", "");
						start_msg_id = start_msg_id.replaceAll(" ", "");
						start_msg_id = start_msg_id.replaceAll(":", "");
						start_msg_id = start_msg_id.substring(2);
						start_msg_id += "0000000";
						end_msg_id = end_msg_id.replaceAll("-", "");
						end_msg_id = end_msg_id.replaceAll(" ", "");
						end_msg_id = end_msg_id.replaceAll(":", "");
						end_msg_id = end_msg_id.substring(2);
						end_msg_id += "0000000";
						query += "msg_id between " + start_msg_id + " and " + end_msg_id;
					} catch (Exception e) {
						query += "submitted_time between CONVERT_TZ('" + startDate + "','" + to_gmt + "','" + from_gmt
								+ "') and CONVERT_TZ('" + endDate + "','" + to_gmt + "','" + from_gmt + "')";
					}
				} else {
					if (startDate.equalsIgnoreCase(endDate)) {
						String start_msg_id = startDate.substring(2);
						start_msg_id = start_msg_id.replaceAll("-", "");
						start_msg_id = start_msg_id.replaceAll(" ", "");
						start_msg_id = start_msg_id.replaceAll(":", "");
						query += "msg_id like '" + start_msg_id + "%'";
					} else {
						String start_msg_id = startDate.substring(2);
						start_msg_id = start_msg_id.replaceAll("-", "");
						start_msg_id = start_msg_id.replaceAll(" ", "");
						start_msg_id = start_msg_id.replaceAll(":", "");
						start_msg_id += "0000000";
						String end_msg_id = endDate.substring(2);
						end_msg_id = end_msg_id.replaceAll("-", "");
						end_msg_id = end_msg_id.replaceAll(" ", "");
						end_msg_id = end_msg_id.replaceAll(":", "");
						end_msg_id += "0000000";
						/*
						 * String[] end_date = endDate.split("-"); Calendar calendar =
						 * Calendar.getInstance(); calendar.set(Calendar.DATE,
						 * Integer.parseInt(end_date[2])); calendar.set(Calendar.MONTH,
						 * (Integer.parseInt(end_date[1])) - 1); calendar.set(Calendar.YEAR,
						 * Integer.parseInt(end_date[0])); calendar.add(Calendar.DATE, 1); String
						 * end_msg_id = new SimpleDateFormat("yyMMdd").format(calendar.getTime()) +
						 * "0000000000000";
						 */
						query += "msg_id between " + start_msg_id + " and " + end_msg_id + "";
					}
				}
				if (senderId != null && senderId.trim().length() > 0) {
					if (senderId.contains("%")) {
						query += " and source_no like \"" + senderId + "\" ";
					} else {
						query += " and source_no =\"" + senderId + "\" ";
					}
				}
				if (country != null && country.length() > 0) {
					Predicate<Integer, NetworkEntry> p = null;
					String oprCountry = "";
					if (operator.equalsIgnoreCase("All")) {
						p = new PredicateBuilderImpl().getEntryObject().get("mcc").equal(country);
					} else {
						EntryObject e = new PredicateBuilderImpl().getEntryObject();
						p = e.get("mcc").equal(country).and(e.get("mnc").equal(operator));
					}
					for (int cc : GlobalVars.NetworkEntries.keySet(p)) {
						oprCountry += "'" + cc + "',";
					}
					if (oprCountry.length() > 0) {
						oprCountry = oprCountry.substring(0, oprCountry.length() - 1);
						query += " and oprCountry in (" + oprCountry + ")";
					}
				}
				query += " group by source_no,oprcountry,status,latency";
				logger.info(username + " ReportSQL:" + query);
				try {
					list = getLatencyReport(report_user, query);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				logger.info(username + " list:" + list.size());
				if (list != null && !list.isEmpty()) {
					System.out.println(report_user + " Report List Size --> " + list.size());
					final_list.addAll(sortList(list));
					list.clear();
				}
			}
		}
		logger.info(username + " End Based On Criteria. Final Report Size: " + final_list.size());
		return final_list;
	}

	public List<DeliveryDTO> getLatencyReport(String username, String sql) throws SQLException {
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		DeliveryDTO report = null;
		System.out.println("SQL: " + sql);
		Map<String, DeliveryDTO> mapping = new HashMap<String, DeliveryDTO>();
		try {
			con = getConnection();
			pStmt = con.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			rs = pStmt.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					try {
						String oprCountry = rs.getString("oprCountry");
						String senderId = rs.getString("source_no");
						String status = rs.getString("status");
						int count = rs.getInt("count");
						if (mapping.containsKey(senderId + "#" + oprCountry)) {
							report = mapping.get(senderId + "#" + oprCountry);
						} else {
							report = new DeliveryDTO();
							report.setUsername(username);
							report.setSender(senderId);
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
							report.setCountry(country);
							report.setOperator(operator);
						}
						if (status != null && status.startsWith("DELIVRD")) {
							report.setDelivered(report.getDelivered() + count);
							int latency = rs.getInt("latency");
							if (latency <= 5) {
								report.setLatency1(report.getLatency1() + count);
							} else if (latency > 5 && latency <= 15) {
								report.setLatency2(report.getLatency2() + count);
							} else if (latency > 15 && latency <= 30) {
								report.setLatency3(report.getLatency3() + count);
							} else if (latency > 30 && latency <= 45) {
								report.setLatency4(report.getLatency4() + count);
							} else if (latency > 45 && latency <= 60) {
								report.setLatency5(report.getLatency5() + count);
							} else {
								report.setLatency6(report.getLatency6() + count);
							}
						}
						report.setSubmitted(report.getSubmitted() + count);
						mapping.put(senderId + "#" + oprCountry, report);
					} catch (Exception sqle) {
						logger.error("", sqle.fillInStackTrace());
					}
				}
				// logger.info(username + " Report List Final Count:--> " +
				// customReport.size());
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
				if (con != null) {
					// dbCon.releaseConnection(con);
				}
			} catch (SQLException sqle) {
			}
		}
		return new ArrayList<DeliveryDTO>(mapping.values());
	}

	public Map<Integer, String> listUsernamesUnderManager(String mgrId) {
		Map<Integer, String> map = listNamesUnderManager(mgrId);
		Map<Integer, String> users = new HashMap<Integer, String>();
		for (Integer seller_id : map.keySet()) {
			users.putAll(userService.listUsersUnderSeller(seller_id));
		}
		return users;
	}

	public Map<Integer, String> listNamesUnderManager(String mgrId) {
		Map<Integer, String> map = new HashMap<Integer, String>();
		List<SalesEntry> list = (List) listSellersUnderManager(mgrId);
		for (SalesEntry entry : list) {
			map.put(entry.getId(), entry.getUsername());
		}
		return map;
	}

	public List<SalesEntry> listSellersUnderManager(String mgrId) {
		return salesRepository.findByMasterIdAndRole(mgrId, "seller");
	}

	private List<DeliveryDTO> getSmscReportList(CustomReportForm customReportForm, String username)
			throws SQLException {

		UserDAService userDAService = new UserDAServiceImpl();
		if (customReportForm.getClientId() == null) {
			return null;
		}
		Optional<UserEntry> usersOptional = userRepository.findBySystemId(username);
		if (!usersOptional.isPresent()) {
			throw new NotFoundException("user not fout with system id" + username);
		}

		UserEntry user = usersOptional.get();
		WebMasterEntry webMasterEntry = webMasterEntryRepository.findByUserId(user.getId());
		if (webMasterEntry == null) {
			throw new NotFoundException("web MasterEntry  not fount username {}" + user.getId());
		}
		logger.info(username + " Report Based On Criteria");
		List<String> users = null;
		String query = null;
		// IDatabaseService dbService = HtiSmsDB.getInstance();
		String senderId = customReportForm.getSenderId();
		String country = customReportForm.getCountry();
		String operator = customReportForm.getOperator();
		String startDate = customReportForm.getSday();
		String endDate = customReportForm.getEday();
		List<DeliveryDTO> list = null;
		List<DeliveryDTO> final_list = new ArrayList<DeliveryDTO>();
		if (!webMasterEntry.getGmt().equalsIgnoreCase(IConstants.DEFAULT_GMT)) {
			to_gmt = webMasterEntry.getGmt().replace("GMT", "");
			from_gmt = IConstants.DEFAULT_GMT.replace("GMT", "");
		}
		// String role = userSessionObject.getRole();
		users = new ArrayList<String>(userService.listUsers().values());
		logger.info(username + " Report Users: " + users.size());
		if (users != null && !users.isEmpty()) {
			while (!users.isEmpty()) {
				String report_user = (String) users.remove(0);
				logger.info(username + " Checking Report For " + report_user);
				query = "select count(msg_id) as count,route_to_smsc,source_no,oprcountry,status,TIME_TO_SEC(TIMEDIFF(deliver_time,submitted_time)) as latency from mis_"
						+ report_user + " where status not like 'ATES' and ";
				if (to_gmt != null) {
					SimpleDateFormat client_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					client_formatter.setTimeZone(TimeZone.getTimeZone(webMasterEntry.getGmt()));
					SimpleDateFormat local_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try {
						String start_msg_id = local_formatter.format(client_formatter.parse(startDate));
						String end_msg_id = local_formatter.format(client_formatter.parse(endDate));
						start_msg_id = start_msg_id.replaceAll("-", "");
						start_msg_id = start_msg_id.replaceAll(" ", "");
						start_msg_id = start_msg_id.replaceAll(":", "");
						start_msg_id = start_msg_id.substring(2);
						start_msg_id += "0000000";
						end_msg_id = end_msg_id.replaceAll("-", "");
						end_msg_id = end_msg_id.replaceAll(" ", "");
						end_msg_id = end_msg_id.replaceAll(":", "");
						end_msg_id = end_msg_id.substring(2);
						end_msg_id += "0000000";
						query += "msg_id between " + start_msg_id + " and " + end_msg_id;
					} catch (Exception e) {
						query += "submitted_time between CONVERT_TZ('" + startDate + "','" + to_gmt + "','" + from_gmt
								+ "') and CONVERT_TZ('" + endDate + "','" + to_gmt + "','" + from_gmt + "')";
					}
				} else {
					if (startDate.equalsIgnoreCase(endDate)) {
						String start_msg_id = startDate.substring(2);
						start_msg_id = start_msg_id.replaceAll("-", "");
						start_msg_id = start_msg_id.replaceAll(" ", "");
						start_msg_id = start_msg_id.replaceAll(":", "");
						query += "msg_id like '" + start_msg_id + "%'";
					} else {
						String start_msg_id = startDate.substring(2);
						start_msg_id = start_msg_id.replaceAll("-", "");
						start_msg_id = start_msg_id.replaceAll(" ", "");
						start_msg_id = start_msg_id.replaceAll(":", "");
						start_msg_id += "0000000";
						String end_msg_id = endDate.substring(2);
						end_msg_id = end_msg_id.replaceAll("-", "");
						end_msg_id = end_msg_id.replaceAll(" ", "");
						end_msg_id = end_msg_id.replaceAll(":", "");
						end_msg_id += "0000000";
						/*
						 * String[] end_date = endDate.split("-"); Calendar calendar =
						 * Calendar.getInstance(); calendar.set(Calendar.DATE,
						 * Integer.parseInt(end_date[2])); calendar.set(Calendar.MONTH,
						 * (Integer.parseInt(end_date[1])) - 1); calendar.set(Calendar.YEAR,
						 * Integer.parseInt(end_date[0])); calendar.add(Calendar.DATE, 1); String
						 * end_msg_id = new SimpleDateFormat("yyMMdd").format(calendar.getTime()) +
						 * "0000000000000";
						 */
						query += "msg_id between " + start_msg_id + " and " + end_msg_id + "";
					}
				}
				query += " and route_to_smsc in ('" + String.join("','", customReportForm.getSmscnames()) + "')";
				if (senderId != null && senderId.trim().length() > 0) {
					if (senderId.contains("%")) {
						query += " and source_no like \"" + senderId + "\" ";
					} else {
						query += " and source_no =\"" + senderId + "\" ";
					}
				}
				if (country != null && country.length() > 0) {
					Predicate<Integer, NetworkEntry> p = null;
					String oprCountry = "";
					if (operator.equalsIgnoreCase("All")) {
						p = new PredicateBuilderImpl().getEntryObject().get("mcc").equal(country);
					} else {
						EntryObject e = new PredicateBuilderImpl().getEntryObject();
						p = e.get("mcc").equal(country).and(e.get("mnc").equal(operator));
					}
					for (int cc : GlobalVars.NetworkEntries.keySet(p)) {
						oprCountry += "'" + cc + "',";
					}
					if (oprCountry.length() > 0) {
						oprCountry = oprCountry.substring(0, oprCountry.length() - 1);
						query += " and oprCountry in (" + oprCountry + ")";
					}
				}
				query += " group by route_to_smsc,source_no,oprcountry,status,latency";
				logger.info(username + " ReportSQL:" + query);
				list = getLatencyReport(report_user, query, true);
				logger.info(username + " list:" + list.size());
				if (list != null && !list.isEmpty()) {
					System.out.println(report_user + " Report List Size --> " + list.size());
					final_list.addAll(sortList(list));
					list.clear();
				}
			}
		}
		logger.info(username + " End Based On Criteria. Final Report Size: " + final_list.size());
		return final_list;

	}

	private List<DeliveryDTO> sortList(List<DeliveryDTO> list) {
		Comparator<DeliveryDTO> comparator = null;
		comparator = Comparator.comparing(DeliveryDTO::getSender).thenComparing(DeliveryDTO::getCountry)
				.thenComparing(DeliveryDTO::getOperator);
		Stream<DeliveryDTO> personStream = list.stream().sorted(comparator);
		List<DeliveryDTO> sortedlist = personStream.collect(Collectors.toList());
		return sortedlist;
	}

	public List<DeliveryDTO> getLatencyReport(String username, String sql, boolean smsc) throws SQLException {
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		DeliveryDTO report = null;
		System.out.println("SQL: " + sql);
		Map<String, DeliveryDTO> mapping = new HashMap<String, DeliveryDTO>();
		try {

			con = getConnection();
			pStmt = con.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			pStmt.setFetchSize(Integer.MIN_VALUE);
			rs = pStmt.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					try {
						String smscname = rs.getString("route_to_smsc");
						String oprCountry = rs.getString("oprCountry");
						String senderId = rs.getString("source_no");
						String status = rs.getString("status");
						int count = rs.getInt("count");
						if (mapping.containsKey(smscname + "#" + senderId + "#" + oprCountry)) {
							report = mapping.get(smscname + "#" + senderId + "#" + oprCountry);
						} else {
							report = new DeliveryDTO();
							report.setUsername(username);
							report.setRoute(smscname);
							report.setSender(senderId);
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
							report.setCountry(country);
							report.setOperator(operator);
						}
						if (status != null && status.startsWith("DELIVRD")) {
							report.setDelivered(report.getDelivered() + count);
							int latency = rs.getInt("latency");
							if (latency <= 5) {
								report.setLatency1(report.getLatency1() + count);
							} else if (latency > 5 && latency <= 15) {
								report.setLatency2(report.getLatency2() + count);
							} else if (latency > 15 && latency <= 30) {
								report.setLatency3(report.getLatency3() + count);
							} else if (latency > 30 && latency <= 45) {
								report.setLatency4(report.getLatency4() + count);
							} else if (latency > 45 && latency <= 60) {
								report.setLatency5(report.getLatency5() + count);
							} else {
								report.setLatency6(report.getLatency6() + count);
							}
						}
						report.setSubmitted(report.getSubmitted() + count);
						mapping.put(smscname + "#" + senderId + "#" + oprCountry, report);
					} catch (Exception sqle) {
						logger.error("", sqle.fillInStackTrace());
					}
				}
				// logger.info(username + " Report List Final Count:--> " +
				// customReport.size());
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
				if (con != null) {
					// dbCon.releaseConnection(con);
				}
			} catch (SQLException sqle) {
			}
		}
		return new ArrayList<DeliveryDTO>(mapping.values());
	}

}
