package com.hti.smpp.common.service.impl;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.database.DataBase;
import com.hti.smpp.common.database.DataNotFoundException;
import com.hti.smpp.common.database.ParameterMismatchException;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.messages.dto.BulkMapEntry;
import com.hti.smpp.common.request.CampaignReportRequest;
import com.hti.smpp.common.response.DeliveryDTO;
import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.sales.repository.SalesRepository;
import com.hti.smpp.common.service.CampaignReportService;
import com.hti.smpp.common.service.UserDAService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.Customlocale;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
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
public class CampaignReportImpl implements CampaignReportService {

	@Autowired
	private DataBase dataBase;

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private EntityManager entityManager;

	Locale locale = null;

	private static final Logger logger = LoggerFactory.getLogger(CampaignReportImpl.class);

	private String template_file = IConstants.FORMAT_DIR + "report//campaign_report.jrxml";

	@Autowired
	private DataSource dataSource;
	@Autowired
	private UserDAService userService;
	@Autowired
	private SalesRepository salesRepository;
	@Autowired
	private MessageResourceBundle messageResourceBundle;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public ResponseEntity<?> CampaignReportview(String username, CampaignReportRequest customReportForm) {
		try {
			Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
			UserEntry user = userOptional
					.orElseThrow(() ->new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username})));


			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));

			}

			
			
			List<DeliveryDTO> print = getReportList(customReportForm, username, false);
			
			
			if (print != null && !print.isEmpty()) {
				logger.info(messageResourceBundle.getLogMessage("report.size.view.message"), user.getSystemId(), print.size());
				logger.info(messageResourceBundle.getLogMessage("report.finished.message"), user.getSystemId());

				

				// Return ResponseEntity with the list in the response body
				return new ResponseEntity<>(print, HttpStatus.OK);
			} else {
				throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_EXCEPTION,new Object[] {username}));

			}
		//	return new ResponseEntity<>(print, HttpStatus.OK);
		} catch (DataNotFoundException | UnauthorizedException | ParameterMismatchException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error: No Campaign report data found for username " + username
							+ " within the specified date range.");
		}
	}
	private List<DeliveryDTO> getReportList(CampaignReportRequest customReportForm, String username, boolean paging) {
		if (customReportForm.getClientId() == null) {
			return null;
		}
		Optional<UserEntry> usersOptional = userRepository.findBySystemId(username);
		if (!usersOptional.isPresent()) {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}
		UserEntry user = usersOptional.get();

		List<DeliveryDTO> final_list = new ArrayList<DeliveryDTO>();
		List<DeliveryDTO> chart_list = new ArrayList<DeliveryDTO>();
		int final_pending = 0, final_deliv = 0, final_undeliv = 0, final_expired = 0, final_others = 0;
		// IDatabaseService dbService = HtiSmsDB.getInstance();
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
		List<String> users = null;
		if (customReportForm.getClientId().equalsIgnoreCase("All")) {
			String role = user.getRole();
			if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")) {
				users = new ArrayList<String>(userService.listUsers().values());
			} else if (role.equalsIgnoreCase("admin")) {
				users = new ArrayList<String>(userService.listUsersUnderMaster(user.getSystemId()).values());
				users.add(user.getSystemId());
				Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
						.get("secondaryMaster").equal(user.getSystemId());
				for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(p)) {
					UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
					users.add(userEntry.getSystemId());
				}
			} else if (role.equalsIgnoreCase("seller")) {
				users = new ArrayList<String>(userService.listUsersUnderSeller(user.getId()).values());
			} else if (role.equalsIgnoreCase("manager")) {
				// SalesDAService salesService = new SalesDAServiceImpl();
				users = new ArrayList<String>(listUsernamesUnderManager(user.getSystemId()).values());
			}
			logger.info(messageResourceBundle.getLogMessage("under.users.message"), user, users.size());

		} else {
			users = new ArrayList<String>();
			users.add(customReportForm.getClientId());
		}
		logger.info(messageResourceBundle.getLogMessage("preparing.report.data.message"), user.getSystemId());

		List<BulkMapEntry> list = list(users.toArray(new String[users.size()]), Long.parseLong(start_date_str),
				Long.parseLong(end_date_str));
		if (customReportForm.getGroupBy().equalsIgnoreCase("name")) {
			Map<String, Map<String, List<String>>> filtered_map = new HashMap<String, Map<String, List<String>>>();
			for (BulkMapEntry entry : list) {
				Map<String, List<String>> campaign_mapping = null;
				if (filtered_map.containsKey(entry.getSystemId())) {
					campaign_mapping = filtered_map.get(entry.getSystemId());
				} else {
					campaign_mapping = new HashMap<String, List<String>>();
				}
				List<String> msg_id_list = null;
				if (campaign_mapping.containsKey(entry.getName())) {
					msg_id_list = campaign_mapping.get(entry.getName());
				} else {
					msg_id_list = new ArrayList<String>();
				}
				msg_id_list.add(String.valueOf(entry.getMsgid()));
				campaign_mapping.put(entry.getName(), msg_id_list);
				filtered_map.put(entry.getSystemId(), campaign_mapping);
			}
			DeliveryDTO report = null;
			for (Map.Entry<String, Map<String, List<String>>> entry : filtered_map.entrySet()) {
				for (Map.Entry<String, List<String>> campaign_entry : entry.getValue().entrySet()) {
					System.out.println("Checking Report For " + entry.getKey() + " Campaign: " + campaign_entry.getKey()
							+ " Listed: " + campaign_entry.getValue().size());
					Map<String, Map<String, Map<String, Integer>>> report_map = null;
					try {
						report_map = getCampaignReport(entry.getKey(), campaign_entry.getValue());
					} catch (SQLException e) {
						e.printStackTrace();
						logger.info(messageResourceBundle.getLogMessage("fetching.campaign.report.error"), e);

						System.err.println(
								"Sorry, there was an issue retrieving the campaign report. Please try again later.");

					}
					for (Map.Entry<String, Map<String, Map<String, Integer>>> time_entry : report_map.entrySet()) {
						for (Map.Entry<String, Map<String, Integer>> source_entry : time_entry.getValue().entrySet()) {
							report = new DeliveryDTO();
							report.setUsername(entry.getKey());
							report.setDate(time_entry.getKey());
							report.setCampaign(campaign_entry.getKey());
							report.setSender(source_entry.getKey());
							int total_submitted = 0;
							int others = 0;
							for (Map.Entry<String, Integer> status_entry : source_entry.getValue().entrySet()) {
								if (status_entry.getKey() != null && status_entry.getKey().length() > 0) {
									if (status_entry.getKey().toLowerCase().startsWith("del")) {
										report.setDelivered(status_entry.getValue());
										final_deliv += status_entry.getValue();
									} else if (status_entry.getKey().toLowerCase().startsWith("und")) {
										report.setUndelivered(status_entry.getValue());
										final_undeliv += status_entry.getValue();
									} else if (status_entry.getKey().toLowerCase().startsWith("exp")) {
										report.setExpired(status_entry.getValue());
										final_expired += status_entry.getValue();
									} else if (status_entry.getKey().toLowerCase().startsWith("ates")) {
										report.setPending(status_entry.getValue());
										final_pending += status_entry.getValue();
									} else {
										others += status_entry.getValue();
									}
								} else {
									others += status_entry.getValue();
								}
								total_submitted += status_entry.getValue();
							}
							report.setSubmitted(total_submitted);
							report.setOthers(others);
							final_others += others;
							final_list.add(report);
						}
					}
				}
			}
		} else {
			Map<String, String> campaign_map = new HashMap<String, String>();
			for (BulkMapEntry entry : list) {
				campaign_map.put(String.valueOf(entry.getMsgid()), entry.getName());
			}
			if (!users.isEmpty()) {
				while (!users.isEmpty()) {
					String report_user = (String) users.remove(0);
					try {
						logger.info(messageResourceBundle.getLogMessage("checking.report.message"), user.getSystemId(), report_user);

						String sql = "select msg_id,DATE(submitted_time) as date,source_no,status from mis_"
								+ report_user + " where msg_id between " + start_date_str + " and " + end_date_str;
						List<DeliveryDTO> part_list = getCampaignReport(sql);
						logger.info(messageResourceBundle.getLogMessage("processing.entries.message"), report_user, part_list.size());

						Map<String, Map<String, Map<String, DeliveryDTO>>> date_wise_map = new HashMap<String, Map<String, Map<String, DeliveryDTO>>>();
						// int total_submitted = 0;
						for (DeliveryDTO dlrEntry : part_list) {
							if (campaign_map.containsKey(dlrEntry.getMsgid())) {
								dlrEntry.setCampaign(campaign_map.get(dlrEntry.getMsgid()));
							} else {
								dlrEntry.setCampaign("-");
							}
							Map<String, Map<String, DeliveryDTO>> campaign_wise_map = null;
							if (date_wise_map.containsKey(dlrEntry.getDate())) {
								campaign_wise_map = date_wise_map.get(dlrEntry.getDate());
							} else {
								campaign_wise_map = new HashMap<String, Map<String, DeliveryDTO>>();
							}
							Map<String, DeliveryDTO> source_wise_map = null;
							if (campaign_wise_map.containsKey(dlrEntry.getCampaign())) {
								source_wise_map = campaign_wise_map.get(dlrEntry.getCampaign());
							} else {
								source_wise_map = new HashMap<String, DeliveryDTO>();
							}
							DeliveryDTO dlrDTO = null;
							if (source_wise_map.containsKey(dlrEntry.getSender())) {
								dlrDTO = source_wise_map.get(dlrEntry.getSender());
							} else {
								dlrDTO = new DeliveryDTO();
								dlrDTO.setCampaign(dlrEntry.getCampaign());
								dlrDTO.setSender(dlrEntry.getSender());
								dlrDTO.setDate(dlrEntry.getDate());
								dlrDTO.setUsername(report_user);
							}
							if (dlrEntry.getStatus() != null) {
								if (dlrEntry.getStatus().toLowerCase().startsWith("deliv")) {
									dlrDTO.setDelivered(dlrDTO.getDelivered() + 1);
									final_deliv++;
								} else if (dlrEntry.getStatus().toLowerCase().startsWith("undel")) {
									dlrDTO.setUndelivered(dlrDTO.getUndelivered() + 1);
									final_undeliv++;
								} else if (dlrEntry.getStatus().toLowerCase().startsWith("expir")) {
									dlrDTO.setExpired(dlrDTO.getExpired() + 1);
									final_expired++;
								} else if (dlrEntry.getStatus().toLowerCase().startsWith("ates")) {
									dlrDTO.setPending(dlrDTO.getPending() + 1);
									final_pending++;
								} else {
									dlrDTO.setOthers(dlrDTO.getOthers() + 1);
									final_others++;
								}
							} else {
								dlrDTO.setOthers(dlrDTO.getOthers() + 1);
								final_others++;
							}
							dlrDTO.setSubmitted(dlrDTO.getSubmitted() + 1);
							// total_submitted++;
							source_wise_map.put(dlrDTO.getSender(), dlrDTO);
							campaign_wise_map.put(dlrDTO.getCampaign(), source_wise_map);
							date_wise_map.put(dlrDTO.getDate(), campaign_wise_map);
						}
						logger.info(messageResourceBundle.getLogMessage("end.processing.entries.message"), report_user);

						if (!date_wise_map.isEmpty()) {
							for (Map.Entry<String, Map<String, Map<String, DeliveryDTO>>> date_wise_entry : date_wise_map
									.entrySet()) {
								for (Map.Entry<String, Map<String, DeliveryDTO>> campaign_wise_entry : date_wise_entry
										.getValue().entrySet()) {
									for (Map.Entry<String, DeliveryDTO> source_wise_entry : campaign_wise_entry
											.getValue().entrySet()) {
										final_list.add(source_wise_entry.getValue());
									}
								}
							}
						}
					} catch (Exception ex) {
						logger.error(report_user + ":" + ex);
					}
				}
			}
		}
		chart_list.add(new DeliveryDTO("DELIVRD", final_deliv));
		chart_list.add(new DeliveryDTO("UNDELIV", final_undeliv));
		chart_list.add(new DeliveryDTO("EXPIRED", final_expired));
		chart_list.add(new DeliveryDTO("PENDING", final_pending));
		chart_list.add(new DeliveryDTO("OTHERS", final_others));
		List<DeliveryDTO> print = null;
		if (!final_list.isEmpty()) {
			logger.info(messageResourceBundle.getLogMessage("prepared.list.message"), user.getSystemId(), final_list.size());

			final_list = sortList(final_list);
//			JasperDesign design = null;
//			try {
//				design = JRXmlLoader.load(template_file);
//			} catch (JRException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			// List<DeliveryDTO> chart_list = new ArrayList<DeliveryDTO>();
//			JasperReport jasperreport = null;
//			try {
//				jasperreport = JasperCompileManager.compileReport(design);
//			} catch (JRException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			JRBeanCollectionDataSource piechartDataSource = new JRBeanCollectionDataSource(chart_list);
//			Map parameters = new HashMap();
//			parameters.put("piechartDataSource", piechartDataSource);
//			JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(final_list);
//			if (final_list.size() > 20000) {
//				logger.info(user.getSystemId() + " <-- Creating Virtualizer --> ");
//				JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(1000,
//						new JRSwapFile(IConstants.WEBAPP_DIR + "temp//", 2048, 1024));
//				parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
//			}
//			parameters.put(JRParameter.IS_IGNORE_PAGINATION, paging);
//			ResourceBundle bundle = ResourceBundle.getBundle("JSReportLabels", locale);
//			parameters.put("REPORT_RESOURCE_BUNDLE", bundle);
//			logger.info(user.getSystemId() + " <-- Filling Report Data --> ");
//			try {
//				print = (List<DeliveryDTO>) JasperFillManager.fillReport(jasperreport, parameters, beanColDataSource);
//			} catch (JRException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			logger.info(user.getSystemId() + " <-- Filling Finished --> ");
//		} else {
//			logger.info(user.getSystemId() + " <-- No Report Data Found --> ");
		}

		return final_list;

	}

	public List<BulkMapEntry> list(String[] systemId, long from, long to) {
		logger.info(messageResourceBundle.getLogMessage("checking.systemId.message"), systemId, from, to);


		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<BulkMapEntry> criteriaQuery = criteriaBuilder.createQuery(BulkMapEntry.class);
		Root<BulkMapEntry> root = criteriaQuery.from(BulkMapEntry.class);

		jakarta.persistence.criteria.Predicate predicate = criteriaBuilder.conjunction(); // Initialize with conjunction
																							// (AND)

		if (systemId != null && systemId.length > 0) {
			predicate = criteriaBuilder.and(predicate, root.get("systemId").in((Object[]) systemId));
		}

		if (from > 0 && to > 0) {
			predicate = criteriaBuilder.and(predicate, criteriaBuilder.between(root.get("msgid"), from, to));
		}

		criteriaQuery.select(root).where(predicate);

		List<BulkMapEntry> list = entityManager.createQuery(criteriaQuery).getResultList();

		logger.info(messageResourceBundle.getLogMessage("campaign.entries.message"), list.size());

		return list;
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
		List<SalesEntry> list = listSellersUnderManager(mgrId);
		for (SalesEntry entry : list) {
			map.put(entry.getId(), entry.getUsername());
		}
		return map;
	}

	public List<SalesEntry> listSellersUnderManager(String mgrId) {
		return salesRepository.findByMasterIdAndRole(mgrId, "seller");
	}

	public Map<String, Map<String, Map<String, Integer>>> getCampaignReport(String username, List<String> msg_id_list)
			throws SQLException {
		// logger.info("<--- checking For campaign report[" + username + "] --> ");
		Map<String, Map<String, Map<String, Integer>>> report_list = new HashMap<String, Map<String, Map<String, Integer>>>();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String sql = "select count(msg_id) as count,Date(submitted_time) as time,source_no,status from mis_" + username
				+ " where msg_id in(" + String.join(",", msg_id_list) + ") group by time,source_no,status";
		// System.out.println(sql);
		try {
			con = getConnection();
			pStmt = con.prepareStatement(sql);
			// pStmt.setString(1, String.join(",", msg_id_list));
			rs = pStmt.executeQuery();
			Map<String, Map<String, Integer>> source_map = null;
			Map<String, Integer> status_map = null;
			while (rs.next()) {
				System.out.println(rs.getString("time") + " " + rs.getString("source_no") + " " + rs.getString("status")
						+ " " + rs.getInt("count"));
				if (report_list.containsKey(rs.getString("time"))) {
					source_map = report_list.get(rs.getString("time"));
				} else {
					source_map = new HashMap<String, Map<String, Integer>>();
				}
				if (source_map.containsKey(rs.getString("source_no"))) {
					status_map = source_map.get(rs.getString("source_no"));
				} else {
					status_map = new HashMap<String, Integer>();
				}
				String status = rs.getString("status");
				if (status == null) {
					status = "";
				}
				int status_count = 0;
				if (status_map.containsKey(status)) {
					status_count = status_map.get(status);
				}
				status_count += rs.getInt("count");
				status_map.put(status, status_count);
				source_map.put(rs.getString("source_no"), status_map);
				report_list.put(rs.getString("time"), source_map);
				// System.out.println(report_list);
			}
			logger.info(messageResourceBundle.getLogMessage("campaign.report.message"), username, report_list.size());

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
		return report_list;
	}

	private List<DeliveryDTO> sortList(List<DeliveryDTO> list) {
		// logger.info(userSessionObject.getSystemId() + " sortListBySender ");
		Comparator<DeliveryDTO> comparator = null;
		comparator = Comparator.comparing(DeliveryDTO::getDate).thenComparing(DeliveryDTO::getCampaign);
		Stream<DeliveryDTO> personStream = list.stream().sorted(comparator);
		List<DeliveryDTO> sortedlist = personStream.collect(Collectors.toList());
		return sortedlist;
	}

	public List<DeliveryDTO> getCampaignReport(String sql) throws SQLException {
		List<DeliveryDTO> list = new ArrayList<DeliveryDTO>();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(sql);
			// pStmt.setString(1, String.join(",", msg_id_list));
			rs = pStmt.executeQuery();
			DeliveryDTO deliver = null;
			while (rs.next()) {
				deliver = new DeliveryDTO();
				deliver.setMsgid(rs.getString("msg_id"));
				deliver.setSender(rs.getString("source_no"));
				deliver.setDate(rs.getString("date"));
				deliver.setStatus(rs.getString("status"));
				list.add(deliver);
			}
		} catch (SQLException sqle) {
			throw new SQLException(sqle);
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

	
//	@Override
//	public ResponseEntity<?> CampaignReportxls(String username, CampaignReportRequest customReportForm,
//	        HttpServletResponse response, String lang) {
//	    List<DeliveryDTO> reportList = null;
//	    String target = IConstants.FAILURE_KEY;
//
//	    Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
//	    UserEntry user = userOptional.orElseThrow(() -> new NotFoundException("User not found with the provided username."));
//
//	    if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
//	        throw new UnauthorizedException("User does not have the required roles for this operation.");
//	    }
//
//	    try {
//	        locale = Customlocale.getLocaleByLanguage(lang);
//	        JasperPrint print = getReportList(customReportForm, username, false, lang);
//	        // Replace the following line with actual data retrieval logic if necessary
//	        // reportList = dataBase.getCampaignReportList(customReportForm, username, false, lang);
//
//	        if (print != null) {
//	            System.out.println("<-- Preparing Outputstream --> ");
//	            String reportName = "Campaign_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".xlsx";
//
//	            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//	            response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
//
//	            System.out.println("<-- Creating XLS --> ");
//	            ByteArrayOutputStream out = new ByteArrayOutputStream();
//	            JRExporter exporter = new JRXlsxExporter();
//	            exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
//	            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
//	            exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
//	            exporter.setParameter(JRXlsExporterParameter.MAXIMUM_ROWS_PER_SHEET, 60000);
//	            exporter.exportReport();
//
//	            System.out.println("<-- Finished --> ");
//	            target = IConstants.SUCCESS_KEY;
//
//	            return ResponseEntity.ok().body(out.toByteArray());
//	        } else {
//	            target = IConstants.FAILURE_KEY;
//	            throw new NotFoundException("No data found for the report");
//	        }
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//	    }
//	}

//	@Override
//	public ResponseEntity<?> CampaignReportPdf(String username, CampaignReportRequest customReportForm,
//			HttpServletResponse response, String lang) {
//		try {
//			Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
//			UserEntry user = userOptional
//					.orElseThrow(() -> new NotFoundException("User not found with the provided username."));
//
//			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
//				throw new UnauthorizedException("User does not have the required roles for this operation.");
//			}
//
//			locale = Customlocale.getLocaleByLanguage(lang);
//			JasperPrint print = getReportList(customReportForm, username, false, lang);
//			byte[] pdfReport = JasperExportManager.exportReportToPdf(print);
//				
//			HttpHeaders headers = new HttpHeaders();
//			headers.setContentType(MediaType.APPLICATION_PDF);
//			headers.setContentDispositionFormData("attachment", "campaign_report.pdf");
//
//			// Return the file in the ResponseEntity
//			return new ResponseEntity<>(pdfReport, headers, HttpStatus.OK);
//		} catch (NotFoundException e) {
//			throw new NotFoundException(e.getMessage());
//		} catch (UnauthorizedException e) {
//			throw new UnauthorizedException(e.getMessage());
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new InternalServerException(
//					"Error: An unexpected error occurred in the Campaign report: " + e.getMessage());
//		}
//	}
//
//	@Override
//	public ResponseEntity<?> CampaignReportDoc(String username, CampaignReportRequest customReportForm,
//	        HttpServletResponse response, String lang) {
//	    JasperPrint print = null;
//	    String target = IConstants.FAILURE_KEY;
//	    Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
//
//	    UserEntry user = userOptional.orElseThrow(() -> new NotFoundException("User not found with the provided username."));
//
//	    if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
//	        throw new UnauthorizedException("User does not have the required roles for this operation.");
//	    }
//
//	    try {
//	        locale = Customlocale.getLocaleByLanguage(lang);
//
//	        print = getReportList(customReportForm, username, false, lang);
//
//	        if (print != null) {
//	            System.out.println("<-- Preparing Outputstream --> ");
//	            String reportName = "Operator_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".docx";
//	            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
//	            response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + "\";");
//	            System.out.println("<-- Creating DOCX --> ");
//
//	            ByteArrayOutputStream out = new ByteArrayOutputStream();
//	            JRExporter exporter = new JRDocxExporter();
//	            exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
//	            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
//	            exporter.exportReport();
//
//	            System.out.println("<-- Finished --> ");
//	            target = IConstants.SUCCESS_KEY;
//
//	            return new ResponseEntity<>(out.toByteArray(), HttpStatus.OK);
//	        } else {
//	            throw new InternalServerException("No data found for the report");
//	        }
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//	    }
//	}

}
