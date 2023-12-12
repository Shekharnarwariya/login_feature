package com.hti.smpp.common.twoway.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder.EntryObject;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.exception.AccessDataException;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.JasperReportException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.login.dto.Role;
import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.UserRepository;
import com.hti.smpp.common.twoway.dto.KeywordEntry;
import com.hti.smpp.common.twoway.dto.ReportEntry;
import com.hti.smpp.common.twoway.dto.ReportEntryRs;
import com.hti.smpp.common.twoway.repository.KeywordEntryRepository;
import com.hti.smpp.common.twoway.request.KeywordEntryForm;
import com.hti.smpp.common.twoway.request.TwowayReportForm;
import com.hti.smpp.common.twoway.service.KeywordService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMenuAccessEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMenuAccessEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
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
public class KeywordServiceImpl implements KeywordService {

	private static final Logger logger = LoggerFactory.getLogger(KeywordServiceImpl.class.getName());

	@Autowired
	private UserRepository loginRepo;

	@Autowired
	private WebMenuAccessEntryRepository webMenuRepo;

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private KeywordEntryRepository keywordRepo;

	@PersistenceContext
	private EntityManager entityManager;

	private String template_file = null;
	private Locale locale = null;

	@Override
	public String addKeyword(KeywordEntryForm entryForm, String username) {

		Optional<User> optionalUser = loginRepo.findBySystemId(username);
		User user = null;
		Set<Role> role = new HashSet<>();
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			role = user.getRoles();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}

		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		int userId = user.getUserId().intValue();
		WebMenuAccessEntry webMenu = null;
		Optional<WebMenuAccessEntry> webEntryOptional = this.webMenuRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException("WebMenuAccessEntry not found.");
		}

		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found.");
		}

		String target = IConstants.FAILURE_KEY;
		logger.info(systemId + "[" + role + "] Setup Keyword Request: " + entryForm.getPrefix());

		try {
			if (Access.isAuthorizedSuperAdminAndSystem(role) || webMenu.isTwoWay()) {
				KeywordEntry entry = new KeywordEntry();
				BeanUtils.copyProperties(entryForm, entry);
				try {
					entry.setCreatedBy(systemId);
					this.keywordRepo.save(entry);
					target = IConstants.SUCCESS_KEY;
					logger.info("message.operation.success");
					MultiUtility.changeFlag(Constants.KEYWORD_FLAG_FILE, "707");
				} catch (Exception e) {
					logger.error(entryForm.getPrefix() + " Keyword Already Exist");
					logger.error("error.record.duplicate");
					throw new InternalServerException(e.toString());
				}
			} else {
				logger.error("Authorization Failed :" + systemId);
				target = "invalidRequest";
			}

		} catch (Exception e) {
			logger.error(systemId, e.fillInStackTrace());
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]");
			throw new InternalServerException(e.toString());
		}
		logger.info(systemId + "[" + role + "] Add Keyword Target: " + target);

		return target;
	}

	public List<KeywordEntry> listKeyWord() {
		List<KeywordEntry> list = null;
		try {
			list = this.keywordRepo.findAll();
		} catch (Exception e) {
			logger.error(e.toString());
			throw new NotFoundException("No KeywordEntry Found.");
		}
		for (KeywordEntry entry : list) {
			if (GlobalVars.UserEntries.containsKey(entry.getUserId())) {
				entry.setSystemId(GlobalVars.UserEntries.get(entry.getUserId()).getSystemId());
			}
		}
		return list;
	}

	public List<KeywordEntry> listKeyWord(Integer[] users) {
		List<KeywordEntry> list = null;
		try {
			list = this.keywordRepo.findByUserIdIn(users);
		} catch (Exception e) {
			logger.error(e.toString());
			throw new NotFoundException("No KeywordEntry Found.");
		}
		for (KeywordEntry entry : list) {
			if (GlobalVars.UserEntries.containsKey(entry.getUserId())) {
				entry.setSystemId(GlobalVars.UserEntries.get(entry.getUserId()).getSystemId());
			}
		}
		return list;
	}

	@Override
	public List<KeywordEntry> listKeyword(String username) {
		String target = IConstants.FAILURE_KEY;
		Optional<User> optionalUser = loginRepo.findBySystemId(username);
		User user = null;
		Set<Role> role = new HashSet<>();
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			role = user.getRoles();
			if (!Access.isAuthorizedSuperAdminAndSystemAndAdmin(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}

		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		int userId = user.getUserId().intValue();
		WebMenuAccessEntry webMenu = null;
		Optional<WebMenuAccessEntry> webEntryOptional = this.webMenuRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException("WebMenuAccessEntry not found.");
		}

		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found.");
		}

		logger.info(systemId + "[" + role + "] List Keyword Request");

		List<KeywordEntry> list = null;
		try {
			if (Access.isAuthorizedSuperAdminAndSystem(role)) {
				list = listKeyWord();
				if (list.isEmpty()) {
					logger.info(systemId + "[" + role + "] Keyword List Empty");
				} else {
					logger.info(systemId + "[" + role + "] Keyword List:" + list.size());
					target = IConstants.SUCCESS_KEY;
					return list;
				}

			} else if (webMenu.isTwoWay()) {
				Integer[] users = null;
				if (Access.isAuthorizedAdmin(role)) {
					Predicate<Integer, UserEntry> p = new PredicateBuilderImpl().getEntryObject().get("masterId")
							.equal(systemId);
					Set<Integer> set = new HashSet<Integer>(GlobalVars.UserEntries.keySet());
					set.add(userOptional.get().getId());
					users = set.toArray(new Integer[0]);
				} else {
					users = new Integer[1];
					users[0] = user.getUserId().intValue();
				}
				list = listKeyWord(users);
				if (list.isEmpty()) {
					logger.info(systemId + "[" + role + "] Keyword List Empty");
					logger.error("error.record.unavailable");
				} else {
					logger.info(systemId + "[" + role + "] Keyword List:" + list.size());
					target = IConstants.SUCCESS_KEY;
					return list;
				}

			} else {
				logger.error("Authorization Failed :" + systemId);
				target = "invalidRequest";
			}
		} catch (Exception e) {
			logger.error(systemId, e.toString());
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]");
		}
		logger.info(systemId + "[" + role + "] List Keyword Target: " + target);
		return list;
	}

	@Override
	public String updateKeyword(KeywordEntryForm entryForm, String username) {
		String target = IConstants.FAILURE_KEY;
		Optional<User> optionalUser = loginRepo.findBySystemId(username);
		User user = null;
		Set<Role> role = new HashSet<>();
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			role = user.getRoles();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}

		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		int userId = user.getUserId().intValue();
		WebMenuAccessEntry webMenu = null;
		Optional<WebMenuAccessEntry> webEntryOptional = this.webMenuRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException("WebMenuAccessEntry not found.");
		}

		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found.");
		}
		logger.info(systemId + "[" + role + "] Update Keyword Request: " + entryForm.getId() + "]");

		try {
			if (Access.isAuthorizedSuperAdminAndAdmin(role) || webMenu.isTwoWay()) {
				KeywordEntry entry = new KeywordEntry();
				BeanUtils.copyProperties(entryForm, entry);
				KeywordEntry updateEntry = this.keywordRepo.findById(entry.getId())
						.orElseThrow(() -> new NotFoundException("KeywordEntry not found."));
				this.keywordRepo.save(updateEntry);
				target = IConstants.SUCCESS_KEY;
				MultiUtility.changeFlag(Constants.KEYWORD_FLAG_FILE, "707");
			} else {
				logger.info(systemId + "[" + role + "] Unauthorized Request");
				logger.error("error.unauthorized");
			}
		} catch (Exception e) {
			logger.error(systemId, e.toString());
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]");
			throw new InternalServerException("Process Error: " + e.getLocalizedMessage());
		}
		return target;
	}

	@Override
	public String deleteKeyword(int id, String username) {
		String target = IConstants.FAILURE_KEY;

		Optional<User> optionalUser = loginRepo.findBySystemId(username);
		User user = null;
		Set<Role> role = new HashSet<>();
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			role = user.getRoles();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}

		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found.");
		}
		int userId = user.getUserId().intValue();
		WebMenuAccessEntry webMenu = null;
		Optional<WebMenuAccessEntry> webEntryOptional = this.webMenuRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException("WebMenuAccessEntry not found.");
		}
		logger.info(systemId + "[" + role + "] Delete Keyword Request: " + id + "]");

		try {
			if (Access.isAuthorizedSuperAdminAndSystem(role) || webMenu.isTwoWay()) {
				this.keywordRepo.deleteById(id);
				target = IConstants.SUCCESS_KEY;
				MultiUtility.changeFlag(Constants.KEYWORD_FLAG_FILE, "707");
			} else {
				logger.info(systemId + "[" + role + "] Unauthorized Request");
				logger.error("error.unauthorized");
			}
		} catch (Exception e) {
			logger.error(systemId, e.toString());
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]");
			throw new InternalServerException("Process Error: " + e.toString());
		}

		return target;
	}

	@Override
	public Collection<UserEntry> setupKeyword(String username) {
		String target = IConstants.FAILURE_KEY;
		Optional<User> optionalUser = loginRepo.findBySystemId(username);
		User user = null;
		Set<Role> role = new HashSet<>();
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			role = user.getRoles();
			if (!Access.isAuthorizedSuperAdminAndSystemAndAdmin(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}

		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found.");
		}
		int userId = user.getUserId().intValue();
		WebMenuAccessEntry webMenu = null;
		Optional<WebMenuAccessEntry> webEntryOptional = this.webMenuRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException("WebMenuAccessEntry not found.");
		}
		logger.info(systemId + "[" + role + "] Setup Keyword Request");
		Collection<UserEntry> users = null;
		try {
			if (Access.isAuthorizedSuperAdminAndSystem(role)) {
				if (Access.isAuthorizedSystem(role)) {
					EntryObject e = new PredicateBuilderImpl().getEntryObject();
					Predicate p = e.get("role").in("admin", "user").or(e.get("id").equal(userOptional.get().getId()));
					users = GlobalVars.UserEntries.values(p);
				} else {
					users = GlobalVars.UserEntries.values();
				}
				target = IConstants.SUCCESS_KEY;
				return users;
			} else if (webMenu.isTwoWay()) {
				if (Access.isAuthorizedAdmin(role)) {
					Predicate<Integer, UserEntry> p = new PredicateBuilderImpl().getEntryObject().get("masterId")
							.equal(systemId);
					users = new ArrayList<UserEntry>(GlobalVars.UserEntries.values(p));
					users.add(GlobalVars.UserEntries.get(userOptional.get().getId()));
					return users;
				}
				target = IConstants.SUCCESS_KEY;
			} else {
				logger.error("Authorization Failed :" + systemId);
				target = "invalidRequest";
			}
		} catch (Exception e) {
			logger.error(systemId, e.toString());
			throw new InternalServerException(e.toString());
		}

		return users;
	}

	public KeywordEntry getEntry(int id) {
		Optional<KeywordEntry> entryOptional = this.keywordRepo.findById(id);
		KeywordEntry entry = null;
		if (entryOptional.isPresent()) {
			entry = entryOptional.get();
		} else {
			throw new NotFoundException("KeywordEntry not found.");
		}
		if (GlobalVars.UserEntries.containsKey(entry.getUserId())) {
			entry.setSystemId(GlobalVars.UserEntries.get(entry.getUserId()).getSystemId());
		}
		return entry;
	}

	@Override
	public KeywordEntry viewKeyword(int id, String username) {
		String target = IConstants.FAILURE_KEY;
		
		Optional<User> optionalUser = loginRepo.findBySystemId(username);

		User user = null;
		Set<Role> role = new HashSet<>();
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			role = user.getRoles();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}

		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		
		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found.");
		}
		
		int userId = user.getUserId().intValue();
		WebMenuAccessEntry webMenu = null;
		Optional<WebMenuAccessEntry> webEntryOptional = this.webMenuRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException("WebMenuAccessEntry not found.");
		}
		logger.info(systemId + "[" + role + "] View Keyword Request: " + id);
		KeywordEntry entry = null;
		try {
			if (Access.isAuthorizedSuperAdminAndSystem(role) || webMenu.isTwoWay()) {
				entry = getEntry(id);
				target = IConstants.SUCCESS_KEY;
				if (entry != null) {
					logger.info("error.record.unavailable");
					return entry;
				}
			} else {
				logger.error("Authorization Failed :" + systemId);
				target = "invalidRequest";
			}

		} catch (Exception e) {
			logger.error(systemId, e.toString());
			throw new InternalServerException(e.toString());
		}
		return entry;
	}

	private JasperPrint getReportList(TwowayReportForm reportForm, boolean paging)
			throws JRException, AccessDataException {

		String sql = "select A.user_id,A.source,A.short_code,A.received_text,A.receivedOn,A.reply,A.reply_msg,A.msg_id,A.remarks,B.system_id,C.prefix,C.suffix"
				+ " from 2way_report A,usermaster B,2way_keyword C where A.user_id=B.id and A.keyword_id = C.id";
		if (reportForm.getUserId() != null && reportForm.getUserId().length > 0) {
			sql += " and A.user_id in("
					+ Arrays.stream(reportForm.getUserId()).mapToObj(String::valueOf).collect(Collectors.joining(","))
					+ ")";
		}
		if (reportForm.getType() != null && reportForm.getType().length > 0) {
			sql += " and C.type in('" + String.join("','", reportForm.getType()) + "')";
		}
		if (reportForm.getKeyword() != null && reportForm.getKeyword().length() > 0) {
			sql += " and C.prefix in ('" + String.join("','", reportForm.getKeyword().split(",")) + "')";
		}
		if (reportForm.getShortCode() != null && reportForm.getShortCode().length() > 0) {
			sql += " and A.short_code in ('" + String.join("','", reportForm.getShortCode().split(",")) + "')";
		}
		if (reportForm.getStartTime() != null && reportForm.getEndTime() != null) {
			sql += " and A.receivedOn between '" + reportForm.getStartTime() + "' and '" + reportForm.getEndTime()
					+ "'";
		}
		List<ReportEntry> list = new ArrayList<ReportEntry>();

		try {

			Query nativeQuery = entityManager.createNativeQuery(sql, "ReportEntryRsMapping");
			List<ReportEntryRs> result = nativeQuery.getResultList();

			result.forEach(rs -> {
				ReportEntry entry = new ReportEntry(rs.getSource(), rs.getShortCode(), rs.getReceivedText(),
						rs.getReceivedOn(), rs.isReply(), rs.getReplyMessage(), rs.getMessageId(), rs.getRemarks());
				if (rs.getSuffix() != null) {
					entry.setKeyword(rs.getPrefix() + " " + rs.getSuffix());
				} else {
					entry.setKeyword(rs.getPrefix());
				}
				entry.setSystemId(rs.getSystemId());

				list.add(entry);

			});

		} catch (Exception e) {
			logger.error(e.toString());
			throw new AccessDataException("Error fetching data.");
		}

		List<ReportEntry> final_list = list;
		logger.info(" 2WayReport: " + sql);
		logger.info(" Prepared List: " + final_list.size());
		this.template_file = IConstants.FORMAT_DIR + "report//twoway_report.jrxml";
		JasperPrint print = null;
		if (!final_list.isEmpty()) {
			final_list = sortList(final_list);
			JasperDesign design = JRXmlLoader.load(template_file);
			JasperReport jasperreport = JasperCompileManager.compileReport(design);
			Map parameters = new HashMap();
			JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(final_list);
			if (final_list.size() > 20000) {
				logger.info(" <-- Creating Virtualizer --> ");
				JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(1000,
						new JRSwapFile(IConstants.WEBAPP_DIR + "temp//", 2048, 1024));
				parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
			}
			parameters.put(JRParameter.IS_IGNORE_PAGINATION, paging);
			ResourceBundle bundle = ResourceBundle.getBundle("JSReportLabels", this.locale);
			parameters.put("REPORT_RESOURCE_BUNDLE", bundle);
			logger.info(" <-- Filling Report Data --> ");
			print = JasperFillManager.fillReport(jasperreport, parameters, beanColDataSource);
			logger.info(" <-- Filling Finished --> ");
		} else {
			logger.info(" <-- No Report Data Found --> ");
		}
		return print;

	}

	private List<ReportEntry> sortList(List<ReportEntry> list) {
		logger.info(" sortListBySender ");
		Comparator<ReportEntry> comparator = null;
		comparator = Comparator.comparing(ReportEntry::getSystemId).thenComparing(ReportEntry::getKeyword)
				.thenComparing(ReportEntry::getReceivedOn);
		Stream<ReportEntry> personStream = list.stream().sorted(comparator);
		List<ReportEntry> sortedlist = personStream.collect(Collectors.toList());
		return sortedlist;
	}

	@Override
	public ResponseEntity<StreamingResponseBody> generateXls(TwowayReportForm reportForm, String locale,
			String username) {
		String target = IConstants.FAILURE_KEY;
		logger.info("<-- Preparing Outputstream --> ");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		String reportName = "twoway_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".xlsx";
		headers.setContentDispositionFormData("attachment", reportName);
		boolean isDone = false;
		StreamingResponseBody responseBody = null;
		try {
			logger.info("<-- Creating XLS -->");
			responseBody = outputStream -> {
				try {
					this.locale = locale != null ? new Locale(locale) : Locale.getDefault();
					JasperPrint print = getReportList(reportForm, false);
					if (print != null) {

						JRExporter exporter = new JRXlsxExporter();
						exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
						exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
						exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
						exporter.setParameter(JRXlsExporterParameter.MAXIMUM_ROWS_PER_SHEET, 60000);
						exporter.exportReport();
					}else {
						logger.error("error.record.unavailable");
					}

				} catch (JRException e) {
					logger.error(e.toString());
					throw new JasperReportException(e.toString());
				} catch (Exception e) {
					logger.error(e.toString());
					throw new AccessDataException(e.toString());
				}
			};
			isDone = true;
		} catch (Exception e) {
			logger.error(e.toString());
			isDone = false;
			throw new InternalServerException(e.toString());
		}

		if (isDone) {
			target = IConstants.SUCCESS_KEY;
			logger.info("<-- Finished --> Message: " + target);
			return ResponseEntity.ok().headers(headers).body(responseBody);
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

	}

	@Override
	public ResponseEntity<StreamingResponseBody> generatePdf(TwowayReportForm reportForm, String locale,
			String username) {
		String target = IConstants.FAILURE_KEY;
		logger.info("<-- Preparing Outputstream --> ");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		String reportName = "twoway_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".pdf";
		headers.setContentDispositionFormData("attachment", reportName);
		boolean isDone = false;
		StreamingResponseBody responseBody = null;
		try {
			logger.info("<-- Creating PDF -->");
			responseBody = outputStream -> {
				try {
					this.locale = locale != null ? new Locale(locale) : Locale.getDefault();
					JasperPrint print = getReportList(reportForm, false);
					if (print != null) {
						JRExporter exporter = new JRPdfExporter();
						exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
						exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
						exporter.exportReport();
					}else {
						logger.error("error.record.unavailable");
					}

				} catch (JRException e) {
					logger.error(e.toString());
					throw new JasperReportException(e.toString());
				} catch (Exception e) {
					logger.error(e.toString());
					throw new AccessDataException(e.toString());
				}
			};
			isDone = true;
		} catch (Exception e) {
			logger.error(e.toString());
			isDone = false;
			throw new InternalServerException(e.toString());
		}

		if (isDone) {
			target = IConstants.SUCCESS_KEY;
			logger.info("<-- Finished --> Message: " + target);
			return ResponseEntity.ok().headers(headers).body(responseBody);
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}

	@Override
	public ResponseEntity<StreamingResponseBody> generateDoc(TwowayReportForm reportForm, String locale,
			String username) {
		String target = IConstants.FAILURE_KEY;
		logger.info("<-- Preparing Outputstream --> ");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		String reportName = "twoway_" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".doc";
		headers.setContentDispositionFormData("attachment", reportName);
		boolean isDone = false;
		StreamingResponseBody responseBody = null;
		try {
			logger.info("<-- Creating DOC -->");
			responseBody = outputStream -> {
				try {
					this.locale = locale != null ? new Locale(locale) : Locale.getDefault();
					JasperPrint print = getReportList(reportForm, false);
					if (print != null) {
						JRExporter exporter = new JRDocxExporter();
						exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
						exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
						exporter.exportReport();
					}else {
						logger.error("error.record.unavailable");
					}

				} catch (JRException e) {
					logger.error(e.toString());
					throw new JasperReportException(e.toString());
				} catch (Exception e) {
					logger.error(e.toString());
					throw new AccessDataException(e.toString());
				}
			};
			isDone = true;
		} catch (Exception e) {
			logger.error(e.toString());
			isDone = false;
			throw new InternalServerException(e.toString());
		}

		if (isDone) {
			target = IConstants.SUCCESS_KEY;
			logger.info("<-- Finished --> Message: " + target);
			return ResponseEntity.ok().headers(headers).body(responseBody);
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}

	@Override
	public ResponseEntity<?> view(TwowayReportForm reportForm, String locale, String username) {
		String target = IConstants.SUCCESS_KEY;
		JasperPrint print = null;
		try {
			this.locale = locale != null ? new Locale(locale) : Locale.getDefault();
			print = getReportList(reportForm, false);

		} catch (Exception e) {
			logger.error(e.toString());
			throw new JasperReportException(e.toString());
		}

		if (print != null) {
			return new ResponseEntity<>(print, HttpStatus.OK);
		} else {
			target = IConstants.FAILURE_KEY;
			logger.error("error.record.unavailable");
			return new ResponseEntity<>(target, HttpStatus.BAD_REQUEST);
		}

	}

	@Override
	public Collection<UserEntry> setupTwowayReport(String username) {
		Optional<User> optionalUser = loginRepo.findBySystemId(username);
		User user = null;
		Set<Role> role = new HashSet<>();
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			role = user.getRoles();
			if (!Access.isAuthorizedSuperAdminAndSystemAndAdmin(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}

		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found.");
		}
		String target = IConstants.FAILURE_KEY;
		logger.info(systemId + "[" + role + "] TwoWay Report Request");
		Collection<UserEntry> list = null;

		try {
			if (Access.isAuthorizedSuperAdminAndSystem(role)) {
				if (Access.isAuthorizedSystem(role)) {
					EntryObject e = new PredicateBuilderImpl().getEntryObject();
					Predicate p = e.get("role").in("admin", "user").or(e.get("id").equal(userOptional.get().getId()));
					list = GlobalVars.UserEntries.values(p);
				} else {
					list = GlobalVars.UserEntries.values();
				}

			}else {
				if(Access.isAuthorizedAdmin(role)) {
					EntryObject e = new PredicateBuilderImpl().getEntryObject();
					Predicate p = e.get("masterId").equal(userOptional.get().getSystemId())
							.or(e.get("id").equal(userOptional.get().getId()));
					list = GlobalVars.UserEntries.values(p);
				}
			}
			target = IConstants.SUCCESS_KEY;
		} catch (Exception e) {
			logger.error(e.toString());
			throw new InternalServerException(e.toString());
		}
		return list;
	}
		

}
