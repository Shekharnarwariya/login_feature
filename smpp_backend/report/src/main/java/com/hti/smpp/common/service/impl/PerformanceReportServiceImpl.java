package com.hti.smpp.common.service.impl;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TreeMap;
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
import com.hti.smpp.common.database.DataBase;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.request.PerformanceReportRequest;
import com.hti.smpp.common.response.DeliveryDTO;
import com.hti.smpp.common.service.PerformanceReportService;
import com.hti.smpp.common.smsc.dto.SmscEntry;
import com.hti.smpp.common.smsc.repository.SmscEntryRepository;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.Customlocale;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;

import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

@Service
public class PerformanceReportServiceImpl implements PerformanceReportService {

	private Logger logger = LoggerFactory.getLogger(PerformanceReportServiceImpl.class);

	private String template_file_smsc = IConstants.FORMAT_DIR + "report//perform_smsc.jrxml";
	private String template_file_opr = IConstants.FORMAT_DIR + "report//perform_operator.jrxml";
	private String template_file_cat = IConstants.FORMAT_DIR + "report//perform_category.jrxml";
	private Locale locale = null;

	@Autowired
	private DataBase dataBase;

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private SmscEntryRepository smscEntryRepository;

	@Autowired
	private MessageResourceBundle messageResourceBundle;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public ResponseEntity<?> PerformanceReportview(String username, PerformanceReportRequest customReportForm) {
		String target = IConstants.FAILURE_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = userOptional.orElseThrow(() -> new NotFoundException(
				messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username })));
		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
			throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION,
					new Object[] { username }));
		}
		try {


			List<DeliveryDTO> reportList = getReportList(customReportForm, username);
			if (!reportList.isEmpty()) {
				logger.info(messageResourceBundle.getLogMessage("report.size"), reportList.size());

				// JasperPrint print = getJasperPrint(reportList, false,
				// customReportForm.getGroupBy());
				System.out.println("Report Size: " + reportList.size());
				// List<DeliveryDTO> print = getJasperPrint(reportList, false,
				// customReportForm.getGroupBy());
				target = IConstants.SUCCESS_KEY;
				return new ResponseEntity<>(reportList, HttpStatus.OK);
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(
						ConstantMessages.PERFORMANCE_REPORT_NOT_FOUND_MESSAGE, new Object[] { username }));

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalServerException(messageResourceBundle.getExMessage(
					ConstantMessages.ERROR_GETTING_PERFORMANCE_REPORT_MESSAGE, new Object[] { username }));

		}

	}
	private List<DeliveryDTO> getReportList(PerformanceReportRequest customReportForm, String username)
			throws Exception {
		List<DeliveryDTO> list = null;
		// int back_day = 1;
		String start_date = customReportForm.getStartDate();
		String last_date = customReportForm.getEndDate();
		String country = customReportForm.getCountry();
		String operator = customReportForm.getOperator();
		if (start_date == null || start_date.length() == 0) {
			start_date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(0));
		}
		if (last_date == null || last_date.length() == 0) {
			last_date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(0));
		}
		String sql = "select total as count,smsc,time as date,oprCountry,status,cost from report_summary where time ";
		if (start_date.equalsIgnoreCase(last_date)) {
			sql += "='" + start_date + "' ";
		} else {
			sql += "between '" + start_date + "' and '" + last_date + "' ";
		}
		String[] smsc = customReportForm.getSmscnames();
		if (smsc != null && smsc.length > 0) {
			sql += "and smsc in('" + String.join("','", smsc) + "') ";
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
			// Coll<Integer, Network> networkmap = dbService.getNetworkRecord(country,
			// operator);
			for (int cc : GlobalVars.NetworkEntries.keySet(p)) {
				oprCountry += "'" + cc + "',";
			}
			if (oprCountry.length() > 0) {
				oprCountry = oprCountry.substring(0, oprCountry.length() - 1);
				sql += "and oprCountry in (" + oprCountry + ")";
			}
		}
		logger.info(messageResourceBundle.getLogMessage("sql.message"), sql);

		list = getSmscStatusReport(sql);
		return list;
	}

	public List getSmscStatusReport(String query) throws Exception {
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
				String date = rs.getString("date");
				String oprCountry = rs.getString("oprCountry");
				String status = "QUEUED";
				if (rs.getString("status") != null) {
					status = rs.getString("status");
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
				report = new DeliveryDTO(country, operator, rs.getDouble("cost"), date, status, rs.getInt("count"));
				report.setRoute(rs.getString("smsc"));
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

	private List<DeliveryDTO> getJasperPrint(List<DeliveryDTO> reportList, boolean paging, String groupBy) {
		System.out.println("Creating Design");

		Map<String, SmscEntry> smscEntries = list();

		List<DeliveryDTO> pi_chart_list = new ArrayList<DeliveryDTO>();
		List<DeliveryDTO> bar_chart_list = new ArrayList<DeliveryDTO>();
		List<DeliveryDTO> final_list = new ArrayList<DeliveryDTO>();
		JasperDesign design = null;
		logger.info(messageResourceBundle.getLogMessage("creating.design.message"));

		if (groupBy.equalsIgnoreCase("Smsc")) {
			// design = JRXmlLoader.load(template_file_smsc);
			Map<String, Map<String, DeliveryDTO>> smsc_key_map = new TreeMap<String, Map<String, DeliveryDTO>>();
			Map<String, Integer> total_status_map = new TreeMap<String, Integer>(); // track all status counts for pi
																					// chart
			Map<String, DeliveryDTO> smsc_status_map = new HashMap<String, DeliveryDTO>(); // track submit & deliver
																							// count per smsc for bar
																							// chart
			for (DeliveryDTO deliver : reportList) {
				String smsc = deliver.getRoute();
				Map<String, DeliveryDTO> key_map = null;
				if (smsc_key_map.containsKey(smsc)) {
					key_map = smsc_key_map.get(smsc);
				} else {
					key_map = new HashMap<String, DeliveryDTO>();
				}
				DeliveryDTO key_entry = null;
				if (key_map.containsKey(
						deliver.getDate() + deliver.getCountry() + deliver.getOperator() + deliver.getCost())) {
					key_entry = key_map
							.get(deliver.getDate() + deliver.getCountry() + deliver.getOperator() + deliver.getCost());
				} else {
					key_entry = new DeliveryDTO(deliver.getCountry(), deliver.getOperator(), deliver.getDate());
					key_entry.setRoute(smsc);
					key_entry.setCost(deliver.getCost());
					if (smscEntries.containsKey(smsc)) {
						key_entry.setCategory(smscEntries.get(smsc).getCategory());
					} else {
						key_entry.setCategory("Wholesale");
					}
				}
				if (deliver.getStatus().startsWith("DELIV")) {
					key_entry.setDelivered(key_entry.getDelivered() + deliver.getStatusCount());
				}
				key_entry.setSubmitted(key_entry.getSubmitted() + deliver.getStatusCount());
				key_map.put(deliver.getDate() + deliver.getCountry() + deliver.getOperator() + deliver.getCost(),
						key_entry);
				smsc_key_map.put(smsc, key_map);
				// ------- total pi_chart --------------------
				int total_status_count = 0;
				if (total_status_map.containsKey(deliver.getStatus())) {
					total_status_count = total_status_map.get(deliver.getStatus());
				}
				total_status_map.put(deliver.getStatus(), total_status_count + deliver.getStatusCount());
				// ------- smsc bar chart --------------------
				DeliveryDTO smsc_status_entry = null;
				if (smsc_status_map.containsKey(smsc)) {
					smsc_status_entry = smsc_status_map.get(smsc);
				} else {
					smsc_status_entry = new DeliveryDTO();
					smsc_status_entry.setRoute(smsc);
				}
				smsc_status_entry.setSubmitted(smsc_status_entry.getSubmitted() + deliver.getStatusCount());
				if (deliver.getStatus().startsWith("DELIV")) {
					smsc_status_entry.setDelivered(smsc_status_entry.getDelivered() + deliver.getStatusCount());
				}
				smsc_status_map.put(smsc, smsc_status_entry);
			}
			for (Map.Entry<String, Integer> total_map_entry : total_status_map.entrySet()) {
				pi_chart_list.add(new DeliveryDTO(total_map_entry.getKey(), total_map_entry.getValue()));
			}
			List<DeliveryDTO> smsc_top_submit_list = sortListBySubmission(smsc_status_map.values());
			for (DeliveryDTO bar_chart_entry : smsc_top_submit_list) {
				DeliveryDTO bar_chart_dto = new DeliveryDTO("DELIVRD", bar_chart_entry.getDelivered());
				bar_chart_dto.setRoute(bar_chart_entry.getRoute());
				bar_chart_list.add(bar_chart_dto);
				bar_chart_dto = new DeliveryDTO("SUBMITTED", bar_chart_entry.getSubmitted());
				bar_chart_dto.setRoute(bar_chart_entry.getRoute());
				bar_chart_list.add(bar_chart_dto);
			}
			for (Map<String, DeliveryDTO> map_entry : smsc_key_map.values()) {
				Comparator<DeliveryDTO> comparator = Comparator.comparing(DeliveryDTO::getDate)
						.thenComparing(DeliveryDTO::getCountry).thenComparing(DeliveryDTO::getOperator);
				Stream<DeliveryDTO> personStream = map_entry.values().stream().sorted(comparator);
				final_list.addAll(personStream.collect(Collectors.toList()));
			}
		} else if (groupBy.equalsIgnoreCase("Operator")) {
			// design = JRXmlLoader.load(template_file_opr);
			Map<String, Map<String, DeliveryDTO>> opr_key_map = new TreeMap<String, Map<String, DeliveryDTO>>();
			Map<String, Integer> total_status_map = new TreeMap<String, Integer>(); // track all status counts for pi
																					// chart
			Map<String, DeliveryDTO> opr_status_map = new HashMap<String, DeliveryDTO>(); // track submit & deliver
																							// count per smsc for bar
																							// chart
			for (DeliveryDTO deliver : reportList) {
				// String opr = deliver.getCountry();
				Map<String, DeliveryDTO> key_map = null;
				if (opr_key_map.containsKey(deliver.getCountry())) {
					key_map = opr_key_map.get(deliver.getCountry());
				} else {
					key_map = new HashMap<String, DeliveryDTO>();
				}
				DeliveryDTO key_entry = null;
				if (key_map.containsKey(
						deliver.getDate() + deliver.getRoute() + deliver.getOperator() + deliver.getCost())) {
					key_entry = key_map
							.get(deliver.getDate() + deliver.getRoute() + deliver.getOperator() + deliver.getCost());
				} else {
					key_entry = new DeliveryDTO(deliver.getCountry(), deliver.getOperator(), deliver.getDate());
					key_entry.setRoute(deliver.getRoute());
					key_entry.setCost(deliver.getCost());
					if (smscEntries.containsKey(deliver.getRoute())) {
						key_entry.setCategory(smscEntries.get(deliver.getRoute()).getCategory());
					} else {
						key_entry.setCategory("Wholesale");
					}
				}
				if (deliver.getStatus().startsWith("DELIV")) {
					key_entry.setDelivered(key_entry.getDelivered() + deliver.getStatusCount());
				}
				key_entry.setSubmitted(key_entry.getSubmitted() + deliver.getStatusCount());
				key_map.put(deliver.getDate() + deliver.getRoute() + deliver.getOperator() + deliver.getCost(),
						key_entry);
				opr_key_map.put(deliver.getCountry(), key_map);
				// ------- total pi_chart --------------------
				int total_status_count = 0;
				if (total_status_map.containsKey(deliver.getStatus())) {
					total_status_count = total_status_map.get(deliver.getStatus());
				}
				total_status_map.put(deliver.getStatus(), total_status_count + deliver.getStatusCount());
				// ------- smsc bar chart --------------------
				DeliveryDTO opr_status_entry = null;
				if (opr_status_map.containsKey(deliver.getCountry())) {
					opr_status_entry = opr_status_map.get(deliver.getCountry());
				} else {
					opr_status_entry = new DeliveryDTO();
					opr_status_entry.setCountry(deliver.getCountry());
				}
				opr_status_entry.setSubmitted(opr_status_entry.getSubmitted() + deliver.getStatusCount());
				if (deliver.getStatus().startsWith("DELIV")) {
					opr_status_entry.setDelivered(opr_status_entry.getDelivered() + deliver.getStatusCount());
				}
				opr_status_map.put(deliver.getCountry(), opr_status_entry);
			}
			for (Map.Entry<String, Integer> total_map_entry : total_status_map.entrySet()) {
				pi_chart_list.add(new DeliveryDTO(total_map_entry.getKey(), total_map_entry.getValue()));
			}
			List<DeliveryDTO> smsc_top_submit_list = sortListBySubmission(opr_status_map.values());
			for (DeliveryDTO bar_chart_entry : smsc_top_submit_list) {
				DeliveryDTO bar_chart_dto = new DeliveryDTO("DELIVRD", bar_chart_entry.getDelivered());
				bar_chart_dto.setCountry(bar_chart_entry.getCountry());
				bar_chart_list.add(bar_chart_dto);
				bar_chart_dto = new DeliveryDTO("SUBMITTED", bar_chart_entry.getSubmitted());
				bar_chart_dto.setCountry(bar_chart_entry.getCountry());
				bar_chart_list.add(bar_chart_dto);
			}
			for (Map<String, DeliveryDTO> map_entry : opr_key_map.values()) {
				Comparator<DeliveryDTO> comparator = Comparator.comparing(DeliveryDTO::getDate)
						.thenComparing(DeliveryDTO::getOperator).thenComparing(DeliveryDTO::getRoute);
				Stream<DeliveryDTO> personStream = map_entry.values().stream().sorted(comparator);
				final_list.addAll(personStream.collect(Collectors.toList()));
			}
		} else if (groupBy.equalsIgnoreCase("Category")) {
			// design = JRXmlLoader.load(template_file_cat);
			Map<String, Map<String, DeliveryDTO>> cat_key_map = new TreeMap<String, Map<String, DeliveryDTO>>();
			Map<String, Integer> total_status_map = new TreeMap<String, Integer>(); // track all status counts for pi
																					// chart
			Map<String, DeliveryDTO> cat_status_map = new HashMap<String, DeliveryDTO>(); // track submit & deliver
																							// count per smsc for bar
																							// chart
			for (DeliveryDTO deliver : reportList) {
				// String smsc = deliver.getRoute();
				String category = "Wholesale";
				if (smscEntries.containsKey(deliver.getRoute())) {
					category = smscEntries.get(deliver.getRoute()).getCategory();
				}
				Map<String, DeliveryDTO> key_map = null;
				if (cat_key_map.containsKey(category)) {
					key_map = cat_key_map.get(category);
				} else {
					key_map = new HashMap<String, DeliveryDTO>();
				}
				DeliveryDTO key_entry = null;
				if (key_map.containsKey(deliver.getDate() + deliver.getRoute() + deliver.getCountry()
						+ deliver.getOperator() + deliver.getCost())) {
					key_entry = key_map.get(deliver.getDate() + deliver.getRoute() + deliver.getCountry()
							+ deliver.getOperator() + deliver.getCost());
				} else {
					key_entry = new DeliveryDTO(deliver.getCountry(), deliver.getOperator(), deliver.getDate());
					key_entry.setRoute(deliver.getRoute());
					key_entry.setCost(deliver.getCost());
					key_entry.setCategory(category);
				}
				if (deliver.getStatus().startsWith("DELIV")) {
					key_entry.setDelivered(key_entry.getDelivered() + deliver.getStatusCount());
				}
				key_entry.setSubmitted(key_entry.getSubmitted() + deliver.getStatusCount());
				key_map.put(deliver.getDate() + deliver.getRoute() + deliver.getCountry() + deliver.getOperator()
						+ deliver.getCost(), key_entry);
				cat_key_map.put(category, key_map);
				// ------- total pi_chart --------------------
				int total_status_count = 0;
				if (total_status_map.containsKey(deliver.getStatus())) {
					total_status_count = total_status_map.get(deliver.getStatus());
				}
				total_status_map.put(deliver.getStatus(), total_status_count + deliver.getStatusCount());
				// ------- smsc bar chart --------------------
				DeliveryDTO cat_status_entry = null;
				if (cat_status_map.containsKey(category)) {
					cat_status_entry = cat_status_map.get(category);
				} else {
					cat_status_entry = new DeliveryDTO();
					cat_status_entry.setCategory(category);
				}
				cat_status_entry.setSubmitted(cat_status_entry.getSubmitted() + deliver.getStatusCount());
				if (deliver.getStatus().startsWith("DELIV")) {
					cat_status_entry.setDelivered(cat_status_entry.getDelivered() + deliver.getStatusCount());
				}
				cat_status_map.put(category, cat_status_entry);
			}
			for (Map.Entry<String, Integer> total_map_entry : total_status_map.entrySet()) {
				pi_chart_list.add(new DeliveryDTO(total_map_entry.getKey(), total_map_entry.getValue()));
			}
			List<DeliveryDTO> cat_top_submit_list = sortListBySubmission(cat_status_map.values());
			for (DeliveryDTO bar_chart_entry : cat_top_submit_list) {
				DeliveryDTO bar_chart_dto = new DeliveryDTO("DELIVRD", bar_chart_entry.getDelivered());
				bar_chart_dto.setCategory(bar_chart_entry.getCategory());
				bar_chart_list.add(bar_chart_dto);
				bar_chart_dto = new DeliveryDTO("SUBMITTED", bar_chart_entry.getSubmitted());
				bar_chart_dto.setCategory(bar_chart_entry.getCategory());
				bar_chart_list.add(bar_chart_dto);
			}
			for (Map<String, DeliveryDTO> map_entry : cat_key_map.values()) {
				Comparator<DeliveryDTO> comparator = Comparator.comparing(DeliveryDTO::getDate)
						.thenComparing(DeliveryDTO::getRoute).thenComparing(DeliveryDTO::getCountry)
						.thenComparing(DeliveryDTO::getOperator);
				Stream<DeliveryDTO> personStream = map_entry.values().stream().sorted(comparator);
				final_list.addAll(personStream.collect(Collectors.toList()));
			}
		} else {
			logger.info(messageResourceBundle.getLogMessage("invalid.groupby.message"), groupBy);

		}

		// JasperReport report = JasperCompileManager.compileReport(design);

		System.out.println("<--- Compiling -->");
		// JasperReport report = JasperCompileManager.compileReport(design);

		// -------------------------------------------------------------
		JRBeanCollectionDataSource piechartDataSource = new JRBeanCollectionDataSource(pi_chart_list);
		JRBeanCollectionDataSource barchart1DataSource = new JRBeanCollectionDataSource(bar_chart_list);
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(final_list);
		Map parameters = new HashMap();
		parameters.put(JRParameter.IS_IGNORE_PAGINATION, paging);
		parameters.put("piechartDataSource", piechartDataSource);
		parameters.put("barchart1DataSource", barchart1DataSource);
		ResourceBundle bundle = ResourceBundle.getBundle("JSReportLabels", locale);
		parameters.put("REPORT_RESOURCE_BUNDLE", bundle);

		logger.info(messageResourceBundle.getLogMessage("processing.report.data.message"));

		// JasperPrint print = JasperFillManager.fillReport(report, parameters,
		// beanColDataSource);
		// return print;

		System.out.println("<-- filling report data --> ");
		// JasperPrint print = JasperFillManager.fillReport(report, parameters,
		// beanColDataSource);
		return final_list;

	}

	public Map<String, SmscEntry> list() {

		Map<String, SmscEntry> list = new HashMap<String, SmscEntry>();
		for (SmscEntry entry : smscEntryRepository.findAll()) {
			list.put(entry.getName(), entry);
		}

		return list;
	}

	private static List<DeliveryDTO> sortListBySubmission(Collection<DeliveryDTO> list) {
		Comparator<DeliveryDTO> comparator = Comparator.comparing(DeliveryDTO::getSubmitted);
		Stream<DeliveryDTO> personStream = list.stream().sorted(comparator.reversed()).limit(10);
		List<DeliveryDTO> sortedlist = personStream.collect(Collectors.toList());
		return sortedlist;
	}

}
