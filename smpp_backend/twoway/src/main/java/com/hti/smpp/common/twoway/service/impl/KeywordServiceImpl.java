package com.hti.smpp.common.twoway.service.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
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
import com.hti.smpp.common.response.PaginationResponse;
import com.hti.smpp.common.twoway.dto.KeywordEntry;
import com.hti.smpp.common.twoway.dto.ReportEntry;
import com.hti.smpp.common.twoway.repository.KeywordEntryRepository;
import com.hti.smpp.common.twoway.request.KeywordEntryForm;
import com.hti.smpp.common.twoway.request.SearchCriteria;
import com.hti.smpp.common.twoway.request.TwowayReportForm;
import com.hti.smpp.common.twoway.service.KeywordService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMenuAccessEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMenuAccessEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;
import com.hti.smpp.common.util.MultiUtility;
import com.netflix.discovery.converters.Auto;

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

/**
 * The `KeywordServiceImpl` class implements the `KeywordService` interface and
 * provides the implementation for keyword-related operations.
 */
@Service
public class KeywordServiceImpl implements KeywordService {

	private static final Logger logger = LoggerFactory.getLogger(KeywordServiceImpl.class.getName());

	@Autowired
	private WebMenuAccessEntryRepository webMenuRepo;

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private KeywordEntryRepository keywordRepo;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private MessageResourceBundle messageResourceBundle;

	@Value("${twoway.report.path}")
	private String template_file;

	private Locale locale = null;

	/**
	 * Adds a new keyword entry.
	 */
	@Override
	public ResponseEntity<String> addKeyword(KeywordEntryForm entryForm, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}
		int userId = user.getId();
		WebMenuAccessEntry webMenu = null;
		Optional<WebMenuAccessEntry> webEntryOptional = this.webMenuRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.TWOWAY_WEBMENU_ACCESS_ENTRY_NOT_FOUND));
		}

		String systemId = user.getSystemId();

		String target = IConstants.FAILURE_KEY;
		logger.info(messageResourceBundle.getLogMessage("twoway.setup.keyword.request.info"),
				systemId + "[" + user.getRole() + "]", entryForm.getPrefix());

		try {
			if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem") || webMenu.isTwoWay()) {
				KeywordEntry entry = new KeywordEntry();
				BeanUtils.copyProperties(entryForm, entry);
				try {
					entry.setCreatedBy(systemId);
					entry.setCreatedOn(LocalDate.now() + "");
					this.keywordRepo.save(entry);
					target = IConstants.SUCCESS_KEY;
					logger.info(messageResourceBundle.getLogMessage("twoway.keyword.entry.saved.successfully.info"));
					MultiUtility.changeFlag(Constants.KEYWORD_FLAG_FILE, "707");
				} catch (Exception e) {
					logger.error(messageResourceBundle.getLogMessage("twoway.keyword.already.exist.error"),
							entryForm.getPrefix());
					throw new InternalServerException(messageResourceBundle.getExMessage(
							ConstantMessages.TWOWAY_KEYWORD_EXIST, new Object[] { entryForm.getPrefix() }));
				}
			} else {
				target = "invalidRequest";
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}

		} catch (UnauthorizedException e) {
			logger.error(messageResourceBundle.getLogMessage("twoway.unauthorized.access.error"), e.getMessage(),
					e.getCause());
			throw new UnauthorizedException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("twoway.process.error"), e.getMessage(), e.getCause());
			throw new InternalServerException(e.getLocalizedMessage());
		}
		logger.info(messageResourceBundle.getLogMessage("twoway.add.keyword.target.info"),
				systemId + "[" + user.getRole() + "]", target);

		return new ResponseEntity<>(messageResourceBundle.getMessage(ConstantMessages.TWOWAY_KEYWORD_ADD_SUCCESS),
				HttpStatus.CREATED);
	}

	/**
	 * Retrieves a list of all keyword entries.
	 * 
	 * @return
	 */
	public Page<KeywordEntry> listKeyWord(Pageable p) {
		Page<KeywordEntry> list = null;
		try {
			list = this.keywordRepo.findAll(p);
		} catch (Exception e) {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.TWOWAY_KEYWORD_NOT_FOUND));
		}
		for (KeywordEntry entry : list) {
			entry.setSystemId(entry.getCreatedBy());
		}
		return list;
	}

	/**
	 * Retrieves a list of keyword entries for specified user identifiers.
	 * 
	 * @param users
	 * @return
	 */
	public Page<KeywordEntry> listKeyWord(Integer[] users, Pageable p) {
		Page<KeywordEntry> list = null;
		try {
			list = this.keywordRepo.findByUserIdIn(users, p);
		} catch (Exception e) {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.TWOWAY_KEYWORD_NOT_FOUND));
		}
		for (KeywordEntry entry : list) {
			entry.setSystemId(entry.getCreatedBy());
		}
		return list;
	}

	/**
	 * Lists keyword entries based on user authorization and role.
	 */
	@Override
	public ResponseEntity<?> listKeyword(String search, String start, String end, String type,Pageable pageable, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}

		String target = IConstants.FAILURE_KEY;
		int userId = user.getId();
		WebMenuAccessEntry webMenu = null;
		Optional<WebMenuAccessEntry> webEntryOptional = this.webMenuRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.TWOWAY_WEBMENU_ACCESS_ENTRY_NOT_FOUND));
		}

		String systemId = user.getSystemId();
		logger.info(messageResourceBundle.getLogMessage("twoway.list.keyword.request.info"),
				systemId + "[" + user.getRole() + "]");
		Page<KeywordEntry> list = null;
		List<KeywordEntry> content = null;
		PaginationResponse pr = null;
		ArrayList<Object> response = new ArrayList<>();

		try {
			if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				if (search != null && search.length()>0) {
					list = this.keywordRepo.searchKeyword(search, pageable);
					pr = new PaginationResponse(list.getNumber(), list.getSize(), list.getTotalPages(),
							list.getTotalElements(), list.isLast(), list.isFirst());
					content = list.getContent();
					for (KeywordEntry entry : content) {
						entry.setSystemId(entry.getCreatedBy());
					}
				} else if ((start != null && start.length() > 0) && (end != null && end.length()>0) && (type != null && type.length()>0)) {
					list = this.keywordRepo.searchByDate(start, end, type, pageable);
					pr = new PaginationResponse(list.getNumber(), list.getSize(), list.getTotalPages(),
							list.getTotalElements(), list.isLast(), list.isFirst());
					content = list.getContent();
					for (KeywordEntry entry : content) {
						entry.setSystemId(entry.getCreatedBy());
					}
				} else {
					list = listKeyWord(pageable);
					pr = new PaginationResponse(list.getNumber(), list.getSize(), list.getTotalPages(),
							list.getTotalElements(), list.isLast(), list.isFirst());
					content = list.getContent();
					for (KeywordEntry entry : content) {
						entry.setSystemId(entry.getCreatedBy());
					}
				}

				if (list.isEmpty()) {
					throw new NotFoundException(
							messageResourceBundle.getExMessage(ConstantMessages.TWOWAY_KEYWORD_NOT_FOUND));
				} else {
					logger.info(messageResourceBundle.getLogMessage("twoway.keyword.list.info"),
							systemId + "[" + user.getRole() + "]", content.size());
					target = IConstants.SUCCESS_KEY;
					response.add(content);
					response.add(pr);
					return ResponseEntity.ok(response);
				}

			} else if (webMenu.isTwoWay()) {
				Integer[] users = null;
				if (Access.isAuthorized(user.getRole(), "isAuthorizedAdmin")) {
					Set<Integer> userKey = this.userRepository.getAllIds();
					Set<Integer> set = new HashSet<Integer>(userKey);
					set.add(userOptional.get().getId());
					users = set.toArray(new Integer[0]);
				} else {
					users = new Integer[1];
					users[0] = user.getId();
				}
				if (search != null && search.length()>0) {
					list = this.keywordRepo.searchKeywordAndFindByUserIdIn(search, users, pageable);
					pr = new PaginationResponse(list.getNumber(), list.getSize(), list.getTotalPages(),
							list.getTotalElements(), list.isLast(), list.isFirst());
					content = list.getContent();
					for (KeywordEntry entry : content) {
						entry.setSystemId(entry.getCreatedBy());
					}
				} else if ((start != null && start.length() > 0) && (end != null && end.length()>0) && (type != null && type.length()>0)) {
					list = this.keywordRepo.searchByDateAndFindByUserIdIn(start, end, type,users, pageable);
					pr = new PaginationResponse(list.getNumber(), list.getSize(), list.getTotalPages(),
							list.getTotalElements(), list.isLast(), list.isFirst());
					content = list.getContent();
					for (KeywordEntry entry : content) {
						entry.setSystemId(entry.getCreatedBy());
					}
				} else {
					list = listKeyWord(users, pageable);
					pr = new PaginationResponse(list.getNumber(), list.getSize(), list.getTotalPages(),
							list.getTotalElements(), list.isLast(), list.isFirst());
					content = list.getContent();
					for (KeywordEntry entry : content) {
						entry.setSystemId(entry.getCreatedBy());
					}
				}

				if (list.isEmpty()) {
					logger.error(messageResourceBundle.getLogMessage("twoway.keyword.list.empty.error"),
							systemId + "[" + user.getRole() + "]");
					throw new NotFoundException(
							messageResourceBundle.getExMessage(ConstantMessages.TWOWAY_KEYWORD_NOT_FOUND));
				} else {
					logger.info(messageResourceBundle.getLogMessage("twoway.keyword.list.info"),
							systemId + "[" + user.getRole() + "]", content.size());
					target = IConstants.SUCCESS_KEY;
					response.add(content);
					response.add(pr);
					return ResponseEntity.ok(response);
				}

			} else {
				target = "invalidRequest";
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} catch (UnauthorizedException e) {
			logger.error(messageResourceBundle.getLogMessage("twoway.unauthorized.access.error"), e.getMessage(),
					e.getCause());
			throw new UnauthorizedException(e.getLocalizedMessage());
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("twoway.process.error"), e.getMessage(), e.getCause());
			throw new InternalServerException(messageResourceBundle.getExMessage(
					ConstantMessages.TWOWAY_INTERNALSERVER_ERROR, new Object[] { e.getLocalizedMessage() }));
		}

	}

	/**
	 * Updates an existing keyword entry.
	 */
	@Override
	public ResponseEntity<?> updateKeyword(KeywordEntryForm entryForm, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}
		String target = IConstants.FAILURE_KEY;
		int userId = user.getId();
		WebMenuAccessEntry webMenu = null;
		Optional<WebMenuAccessEntry> webEntryOptional = this.webMenuRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.TWOWAY_WEBMENU_ACCESS_ENTRY_NOT_FOUND));
		}

		String systemId = user.getSystemId();

		logger.info(messageResourceBundle.getLogMessage("twoway.update.keyword.request.info"),
				systemId + "[" + user.getRole() + "]", entryForm.getId());

		try {
			if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndAdmin") || webMenu.isTwoWay()) {
				KeywordEntry entry = new KeywordEntry();
				BeanUtils.copyProperties(entryForm, entry);

				if (this.keywordRepo.existsById(entry.getId())) {
					this.keywordRepo.save(entry);
				} else {
					throw new NotFoundException(
							messageResourceBundle.getExMessage(ConstantMessages.TWOWAY_KEYWORD_NOT_FOUND));
				}

				target = IConstants.SUCCESS_KEY;
				MultiUtility.changeFlag(Constants.KEYWORD_FLAG_FILE, "707");
			} else {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} catch (UnauthorizedException e) {
			logger.error(messageResourceBundle.getLogMessage("twoway.unauthorized.access.error"), e.getMessage(),
					e.getCause());
			throw new UnauthorizedException(e.getLocalizedMessage());
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			throw new InternalServerException(messageResourceBundle.getExMessage(
					ConstantMessages.TWOWAY_INTERNALSERVER_ERROR, new Object[] { e.getLocalizedMessage() }));
		}
		return new ResponseEntity<>(messageResourceBundle.getMessage(ConstantMessages.TWOWAY_KEYWORD_UPDATED_SUCCESS),
				HttpStatus.CREATED);
	}

	/**
	 * Deletes a keyword entry by its identifier.
	 */
	@Override
	public ResponseEntity<?> deleteKeyword(int id, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}
		String target = IConstants.FAILURE_KEY;
		String systemId = user.getSystemId();
		int userId = user.getId();
		WebMenuAccessEntry webMenu = null;
		Optional<WebMenuAccessEntry> webEntryOptional = this.webMenuRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.TWOWAY_WEBMENU_ACCESS_ENTRY_NOT_FOUND));
		}
		logger.info(messageResourceBundle.getLogMessage("twoway.delete.keyword.request.info"),
				systemId + "[" + user.getRole() + "]", id);

		try {
			if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem") || webMenu.isTwoWay()) {
				if (this.keywordRepo.existsById(id)) {
					this.keywordRepo.deleteById(id);
				} else {
					throw new NotFoundException(
							messageResourceBundle.getExMessage(ConstantMessages.TWOWAY_KEYWORD_NOT_FOUND));
				}

				target = IConstants.SUCCESS_KEY;
				MultiUtility.changeFlag(Constants.KEYWORD_FLAG_FILE, "707");
			} else {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} catch (UnauthorizedException e) {
			logger.error(messageResourceBundle.getLogMessage("twoway.unauthorized.access.error"), e.getMessage(),
					e.getCause());
			throw new UnauthorizedException(e.getLocalizedMessage());
		} catch (Exception e) {
			throw new InternalServerException(messageResourceBundle.getExMessage(
					ConstantMessages.TWOWAY_INTERNALSERVER_ERROR, new Object[] { e.getLocalizedMessage() }));
		}

		return new ResponseEntity<>(messageResourceBundle.getMessage(ConstantMessages.TWOWAY_KEYWORD_DELETED_SUCCESS),
				HttpStatus.OK);
	}

	/**
	 * Retrieves a keyword entry by its identifier.
	 * 
	 * @param id
	 * @return
	 */
	public KeywordEntry getEntry(int id) {
		Optional<KeywordEntry> entryOptional = this.keywordRepo.findById(id);
		KeywordEntry entry = null;
		if (entryOptional.isPresent()) {
			entry = entryOptional.get();
			entry.setSystemId(entry.getCreatedBy());
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.TWOWAY_KEYWORD_NOT_FOUND));
		}

		return entry;
	}

	/**
	 * Retrieves details of a keyword entry by its identifier for viewing purposes.
	 */
	@Override
	public ResponseEntity<KeywordEntry> viewKeyword(int id, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}
		String target = IConstants.FAILURE_KEY;
		String systemId = user.getSystemId();
		int userId = user.getId();
		WebMenuAccessEntry webMenu = null;
		Optional<WebMenuAccessEntry> webEntryOptional = this.webMenuRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.TWOWAY_WEBMENU_ACCESS_ENTRY_NOT_FOUND));
		}
		logger.info(messageResourceBundle.getLogMessage("twoway.view.keyword.request.info"),
				systemId + "[" + user.getRole() + "]", id);
		KeywordEntry entry = null;
		try {
			if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem") || webMenu.isTwoWay()) {
				if (this.keywordRepo.existsById(id)) {
					entry = getEntry(id);
				} else {
					throw new NotFoundException(
							messageResourceBundle.getExMessage(ConstantMessages.TWOWAY_KEYWORD_NOT_FOUND));
				}

				target = IConstants.SUCCESS_KEY;

			} else {
				target = "invalidRequest";
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}

		} catch (UnauthorizedException e) {
			throw new UnauthorizedException(e.getLocalizedMessage());
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(systemId, e.toString());
			throw new InternalServerException(messageResourceBundle.getExMessage(
					ConstantMessages.TWOWAY_INTERNALSERVER_ERROR, new Object[] { e.getLocalizedMessage() }));
		}
		return ResponseEntity.ok(entry);
	}

	/**
	 * Retrieves the JasperPrint object for the 2-way report based on the provided
	 * form and paging parameter.
	 * 
	 * @param reportForm
	 * @param paging
	 * @return
	 * @throws JRException
	 * @throws DataAccessException
	 * @throws IOException
	 */
	private JasperPrint getReportList(TwowayReportForm reportForm, boolean paging)
			throws JRException, DataAccessException, IOException {

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
			this.jdbcTemplate.query(sql, (rs) -> {
				try {
					ReportEntry entry = new ReportEntry(rs.getString("A.source"), rs.getString("A.short_code"),
							rs.getString("A.received_text"), rs.getString("A.receivedOn"), rs.getBoolean("A.reply"),
							rs.getString("A.reply_msg"), rs.getString("A.msg_id"), rs.getString("A.remarks"));

					String prefix = rs.getString("C.prefix");
					String suffix = rs.getString("C.suffix");

					if (suffix != null) {
						entry.setKeyword(prefix + " " + suffix);
					} else {
						entry.setKeyword(prefix);
					}

					entry.setSystemId(rs.getString("B.system_id"));

					list.add(entry);
				} catch (SQLException e) {
					logger.error("SQL ERROR: " + e.toString());
					throw new InternalServerException("SQL ERROR: " + e.getLocalizedMessage());
				} catch (Exception e) {
					logger.error("ERROR: " + e.toString());
					throw new InternalServerException("ERROR: " + e.getLocalizedMessage());
				}
			});
		} catch (DataAccessException e) {
			logger.error("DataAccess ERROR: " + e.toString());
			throw new InternalServerException("ERROR: " + e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error("ERROR: " + e.toString());
			throw new InternalServerException("ERROR: " + e.getLocalizedMessage());
		}

		List<ReportEntry> final_list = list;
		logger.info(" 2WayReport: " + sql);
		logger.info(" Prepared List: " + final_list.size());
		// template_file = IConstants.FORMAT_DIR + "report//twoway_report.jrxml";

		JasperPrint print = null;
		if (!final_list.isEmpty()) {
			final_list = sortList(final_list);

			String templateFilePath = this.template_file + "twoway_report.jrxml";
			ClassPathResource resource = new ClassPathResource(templateFilePath);
			JasperDesign design = JRXmlLoader.load(resource.getInputStream());
//			JasperDesign design = JRXmlLoader.load(temp_file);
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

	/**
	 * Sorts the list of ReportEntry objects by systemId, keyword, and receivedOn.
	 * 
	 * @param list
	 * @return
	 */
	private List<ReportEntry> sortList(List<ReportEntry> list) {
		logger.info(" sortListBySender ");
		Comparator<ReportEntry> comparator = null;
		comparator = Comparator.comparing(ReportEntry::getSystemId).thenComparing(ReportEntry::getKeyword)
				.thenComparing(ReportEntry::getReceivedOn);
		Stream<ReportEntry> personStream = list.stream().sorted(comparator);
		List<ReportEntry> sortedlist = personStream.collect(Collectors.toList());
		return sortedlist;
	}

	/**
	 * Generates an Excel (XLSX) report based on the provided TwowayReportForm
	 * parameters. The generated report is streamed as a response.
	 */
	@Override
	public ResponseEntity<StreamingResponseBody> generateXls(TwowayReportForm reportForm, String locale,
			String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}

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
					} else {
						logger.error(messageResourceBundle.getLogMessage("twoway.report.not.found.error"));
						throw new NotFoundException(
								messageResourceBundle.getExMessage(ConstantMessages.TWOWAY_RECORD_UNAVAILABLE));
					}

				} catch (JRException e) {
					logger.error(e.toString());
					throw new JasperReportException(e.getLocalizedMessage());
				} catch (NotFoundException e) {
					logger.error(e.toString());
					throw new NotFoundException(e.getLocalizedMessage());
				} catch (DataAccessException e) {
					logger.error(e.toString());
					throw new AccessDataException(e.getLocalizedMessage());
				} catch (Exception e) {
					logger.error(e.toString());
					throw new InternalServerException(e.getLocalizedMessage());
				}finally {
					outputStream.close();
				}
			};
			isDone = true;
		} catch (NotFoundException e) {
			logger.error(e.toString());
			isDone = false;
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (DataAccessException e) {
			logger.error(e.toString());
			isDone = false;
			throw new AccessDataException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(e.toString());
			isDone = false;
			throw new InternalServerException(e.getLocalizedMessage());
		}

		if (isDone) {
			target = IConstants.SUCCESS_KEY;
			logger.info("<-- Finished --> Message: " + target);
			return ResponseEntity.ok().headers(headers).body(responseBody);
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

	}

	/**
	 * Generates a PDF report based on the provided TwowayReportForm parameters.
	 */
	@Override
	public ResponseEntity<StreamingResponseBody> generatePdf(TwowayReportForm reportForm, String locale,
			String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}
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
					} else {
						logger.error(messageResourceBundle.getLogMessage("twoway.report.not.found.error"));
						throw new NotFoundException(
								messageResourceBundle.getExMessage(ConstantMessages.TWOWAY_RECORD_UNAVAILABLE));
					}

				} catch (JRException e) {
					logger.error(e.toString());
					throw new JasperReportException(e.getLocalizedMessage());
				} catch (NotFoundException e) {
					logger.error(e.toString());
					throw new NotFoundException(e.getLocalizedMessage());
				} catch (DataAccessException e) {
					logger.error(e.toString());
					throw new AccessDataException(e.getLocalizedMessage());
				} catch (Exception e) {
					logger.error(e.toString());
					throw new InternalServerException(e.getLocalizedMessage());
				}finally {
					outputStream.close();
				}
			};

			isDone = true;

		} catch (NotFoundException e) {
			logger.error(e.toString());
			isDone = false;
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (DataAccessException e) {
			logger.error(e.toString());
			isDone = false;
			throw new AccessDataException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(e.toString());
			isDone = false;
			throw new InternalServerException(e.getLocalizedMessage());
		}

		if (isDone) {
			target = IConstants.SUCCESS_KEY;
			logger.info("<-- Finished --> Message: " + target);
			return ResponseEntity.ok().headers(headers).body(responseBody);
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}

	/**
	 * Generates a DOC report based on the provided TwowayReportForm parameters. The
	 * generated report is streamed as a response.
	 */
	@Override
	public ResponseEntity<StreamingResponseBody> generateDoc(TwowayReportForm reportForm, String locale,
			String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}
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
					} else {
						logger.error(messageResourceBundle.getLogMessage("twoway.report.not.found.error"));
						throw new NotFoundException(
								messageResourceBundle.getExMessage(ConstantMessages.TWOWAY_RECORD_UNAVAILABLE));
					}

				} catch (JRException e) {
					logger.error(e.toString());
					throw new JasperReportException(e.getLocalizedMessage());
				} catch (NotFoundException e) {
					logger.error(e.toString());
					throw new NotFoundException(e.getLocalizedMessage());
				} catch (DataAccessException e) {
					logger.error(e.toString());
					throw new AccessDataException(e.getLocalizedMessage());
				} catch (Exception e) {
					logger.error(e.toString());
					throw new InternalServerException(e.getLocalizedMessage());
				}finally {

					outputStream.close();
				}
			};
			isDone = true;
		} catch (NotFoundException e) {
			logger.error(e.toString());
			isDone = false;
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (DataAccessException e) {
			logger.error(e.toString());
			isDone = false;
			throw new AccessDataException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(e.toString());
			isDone = false;
			throw new InternalServerException(e.getLocalizedMessage());
		}

		if (isDone) {
			target = IConstants.SUCCESS_KEY;
			logger.info("<-- Finished --> Message: " + target);
			return ResponseEntity.ok().headers(headers).body(responseBody);
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}

	/**
	 * Generates a report based on the provided TwowayReportForm parameters and
	 * returns it.
	 */
//	@Override
//	public ResponseEntity<?> view(TwowayReportForm reportForm, int page, int size, String username) {
//		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
//		UserEntry user = null;
//		if (userOptional.isPresent()) {
//			user = userOptional.get();
//			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
//				throw new UnauthorizedException(messageResourceBundle
//						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
//			}
//		} else {
//			throw new NotFoundException(
//					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
//		}
//		String target = IConstants.SUCCESS_KEY;
//		PaginationResponse pr = null;
//		ArrayList<Object> response = new ArrayList<>();
//		String sql = "select A.user_id,A.source,A.short_code,A.received_text,A.receivedOn,A.reply,A.reply_msg,A.msg_id,A.remarks,B.system_id,C.prefix,C.suffix"
//				+ " from 2way_report A,usermaster B,2way_keyword C where A.user_id=B.id and A.keyword_id = C.id";
//		String countQuery = "select count(*) from 2way_report A,usermaster B,2way_keyword C where A.user_id=B.id and A.keyword_id = C.id";
//
//		if (reportForm.getUserId() != null && reportForm.getUserId().length > 0) {
//
//			sql += " and A.user_id in("
//					+ Arrays.stream(reportForm.getUserId()).mapToObj(String::valueOf).collect(Collectors.joining(","))
//					+ ")";
//			countQuery += " and A.user_id in("
//					+ Arrays.stream(reportForm.getUserId()).mapToObj(String::valueOf).collect(Collectors.joining(","))
//					+ ")";
//		}
//		if (reportForm.getType() != null && reportForm.getType().length > 0) {
//			sql += " and C.type in('" + String.join("','", reportForm.getType()) + "')";
//			countQuery += " and C.type in('" + String.join("','", reportForm.getType()) + "')";
//		}
//		if (reportForm.getKeyword() != null && reportForm.getKeyword().length() > 0) {
//			sql += " and C.prefix in ('" + String.join("','", reportForm.getKeyword().split(",")) + "')";
//			countQuery += " and C.prefix in ('" + String.join("','", reportForm.getKeyword().split(",")) + "')";
//		}
//		if (reportForm.getShortCode() != null && reportForm.getShortCode().length() > 0) {
//			sql += " and A.short_code in ('" + String.join("','", reportForm.getShortCode().split(",")) + "')";
//			countQuery += " and A.short_code in ('" + String.join("','", reportForm.getShortCode().split(",")) + "')";
//		}
//		if (reportForm.getStartTime() != null && reportForm.getEndTime() != null) {
//			sql += " and A.receivedOn between '" + reportForm.getStartTime() + "' and '" + reportForm.getEndTime()
//					+ "'";
//			countQuery += " and A.receivedOn between '" + reportForm.getStartTime() + "' and '"
//					+ reportForm.getEndTime() + "'";
//		}
//		int offset = (page - 1) * size;
//		sql += " LIMIT " + size + " OFFSET " + offset;
//		List<ReportEntry> list = new ArrayList<ReportEntry>();
//
//		try {
//			this.jdbcTemplate.query(sql, (rs) -> {
//				try {
//					ReportEntry entry = new ReportEntry(rs.getString("A.source"), rs.getString("A.short_code"),
//							rs.getString("A.received_text"), rs.getString("A.receivedOn"), rs.getBoolean("A.reply"),
//							rs.getString("A.reply_msg"), rs.getString("A.msg_id"), rs.getString("A.remarks"));
//
//					String prefix = rs.getString("C.prefix");
//					String suffix = rs.getString("C.suffix");
//
//					if (suffix != null) {
//						entry.setKeyword(prefix + " " + suffix);
//					} else {
//						entry.setKeyword(prefix);
//					}
//
//					entry.setSystemId(rs.getString("B.system_id"));
//
//					list.add(entry);
//				} catch (SQLException e) {
//					logger.error("SQL ERROR: " + e.toString());
//					throw new InternalServerException("SQL ERROR: " + e.getLocalizedMessage());
//				} catch (Exception e) {
//					logger.error("ERROR: " + e.toString());
//					throw new InternalServerException("ERROR: " + e.getLocalizedMessage());
//				}
//			});
//			int totalCount = jdbcTemplate.queryForObject(countQuery, Integer.class);
//			Page<ReportEntry> pageReport = new PageImpl<>(list, PageRequest.of(page - 1, size), totalCount);
//			pr = new PaginationResponse(pageReport.getNumber(), pageReport.getSize(), pageReport.getTotalPages(),
//					pageReport.getTotalElements(), pageReport.isLast(), pageReport.isFirst());
//			response.add(pageReport.getContent());
//			response.add(pr);
//
//			if (!response.isEmpty()) {
//				return new ResponseEntity<>(response, HttpStatus.OK);
//			} else {
//				return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
//			}
//
//		} catch (DataAccessException e) {
//			logger.error("DataAccess ERROR: " + e.toString());
//			throw new InternalServerException("SQL ERROR!");
//		} catch (Exception e) {
//			logger.error("ERROR: " + e.toString());
//			throw new InternalServerException("ERROR: " + e.getLocalizedMessage());
//		}
//	}
	
	
	
	@Override
		public ResponseEntity<?> view(int[] userId,String shortCode,String keyword,String startTime,String endTime, String[] type, int page, int size, String username) {
			Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
			UserEntry user = null;
			if (userOptional.isPresent()) {
				user = userOptional.get();
				if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
					throw new UnauthorizedException(messageResourceBundle
							.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
				}
			} else {
				throw new NotFoundException(
						messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
			}
			String target = IConstants.SUCCESS_KEY;
			PaginationResponse pr = null;
			ArrayList<Object> response = new ArrayList<>();
			String sql = "select A.user_id,A.source,A.short_code,A.received_text,A.receivedOn,A.reply,A.reply_msg,A.msg_id,A.remarks,B.system_id,C.prefix,C.suffix"
					+ " from 2way_report A,usermaster B,2way_keyword C where A.user_id=B.id and A.keyword_id = C.id";
			String countQuery = "select count(*) from 2way_report A,usermaster B,2way_keyword C where A.user_id=B.id and A.keyword_id = C.id";
	
			int user_ID=user.getId();
			if (userId != null && userId.length > 0) {
				
				sql += " and A.user_id in("
						+ Arrays.stream(userId).mapToObj(String::valueOf).collect(Collectors.joining(","))
						+ ")";
				countQuery += " and A.user_id in("
						+ Arrays.stream(userId).mapToObj(String::valueOf).collect(Collectors.joining(","))
						+ ")";
				//System.out.println(" inside the if condition"+userId[0]);
			}else {
				//System.out.println(" inside the else condition"+user_ID);
					sql += " and A.user_id = '" + user_ID + "'";
			        countQuery += " and A.user_id = '" + user_ID + "'";
			}
			
			if (type != null && type.length > 0) {
				
				sql += " and C.type in('" + String.join("','", type) + "')";
				countQuery += " and C.type in('" + String.join("','", type) + "')";
			}
			
			if (keyword != null && keyword.length() > 0) {
				
				sql += " and C.prefix in ('" + String.join("','", keyword.split(",")) + "')";
				countQuery += " and C.prefix in ('" + String.join("','", keyword.split(",")) + "')";
			}
			if (shortCode != null && shortCode.length() > 0) {
			
				sql += " and A.short_code in ('" + String.join("','", shortCode.split(",")) + "')";
				countQuery += " and A.short_code in ('" + String.join("','", shortCode.split(",")) + "')";
			}
			if (startTime != null && startTime != null) {
				
				sql += " and A.receivedOn between '" + startTime + "' and '" + endTime
						+ "'";
				countQuery += " and A.receivedOn between '" + startTime + "' and '"
						+ endTime + "'";
			}
			int offset = (page - 1) * size;
			sql += " LIMIT " + size + " OFFSET " + offset;
			//System.out.println("sql query"+sql);
			//System.out.println("Countsql query"+countQuery);
			List<ReportEntry> list = new ArrayList<ReportEntry>();
	
			try {
				this.jdbcTemplate.query(sql, (rs) -> {
					try {
						//System.out.println(rs);
						ReportEntry entry = new ReportEntry(rs.getString("A.source"), rs.getString("A.short_code"),
								rs.getString("A.received_text"), rs.getString("A.receivedOn"), rs.getBoolean("A.reply"),
								rs.getString("A.reply_msg"), rs.getString("A.msg_id"), rs.getString("A.remarks"));
	
						String prefix = rs.getString("C.prefix");
						String suffix = rs.getString("C.suffix");
	
						if (suffix != null) {
							entry.setKeyword(prefix + " " + suffix);
						} else {
							entry.setKeyword(prefix);
						}
	
						entry.setSystemId(rs.getString("B.system_id"));
						//System.out.println(entry);
						list.add(entry);
						
					} catch (SQLException e) {
						logger.error("SQL ERROR: " + e.toString());
						throw new InternalServerException("SQL ERROR: " + e.getLocalizedMessage());
					} catch (Exception e) {
						logger.error("ERROR: " + e.toString());
						throw new InternalServerException("ERROR: " + e.getLocalizedMessage());
					}
				});
				int totalCount = jdbcTemplate.queryForObject(countQuery, Integer.class);
				Page<ReportEntry> pageReport = new PageImpl<>(list, PageRequest.of(page - 1, size), totalCount);
				pr = new PaginationResponse(pageReport.getNumber(), pageReport.getSize(), pageReport.getTotalPages(),
						pageReport.getTotalElements(), pageReport.isLast(), pageReport.isFirst());
				response.add(pageReport.getContent());
				response.add(pr);
	
				if (!response.isEmpty()) {
					return new ResponseEntity<>(response, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
				}
	
			} catch (DataAccessException e) {
				logger.error("DataAccess ERROR: " + e.toString());
				throw new InternalServerException("SQL ERROR!");
			} catch (Exception e) {
				logger.error("ERROR: " + e.toString());
				throw new InternalServerException("ERROR: " + e.getLocalizedMessage());
			}
		}

//--------------------------------------------------------------------------------------------------------------------------------	

	/**
	 * Sets up a collection of UserEntry objects for TwoWay Report based on the
	 * provided username.
	 */
	@Override
	public ResponseEntity<Collection<UserEntry>> setupTwowayReport(String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystemAndAdmin")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}

		String systemId = user.getSystemId();

		String target = IConstants.FAILURE_KEY;
		logger.info(systemId + "[" + user.getRole() + "] TwoWay Report Request");
		Collection<UserEntry> list = null;

		try {
			if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				if (Access.isAuthorized(user.getRole(), "isAuthorizedSystem")) {
					EntryObject e = new PredicateBuilderImpl().getEntryObject();
					Predicate p = e.get("role").in("admin", "user").or(e.get("id").equal(userOptional.get().getId()));
					list = GlobalVars.UserEntries.values(p);
				} else {
					list = GlobalVars.UserEntries.values();
				}

			} else {
				if (Access.isAuthorized(user.getRole(), "isAuthorizedAdmin")) {
					EntryObject e = new PredicateBuilderImpl().getEntryObject();
					Predicate p = e.get("masterId").equal(userOptional.get().getSystemId())
							.or(e.get("id").equal(userOptional.get().getId()));
					list = GlobalVars.UserEntries.values(p);
				} else {
					throw new UnauthorizedException("Unauthorized User!");
				}
			}
			target = IConstants.SUCCESS_KEY;
		} catch (UnauthorizedException e) {
			logger.error(e.toString());
			throw new UnauthorizedException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}
		return ResponseEntity.ok(list);
	}

	/**
	 * Sets up keyword configuration by providing a collection of user entries.
	 */
	@Override
	public ResponseEntity<Collection<UserEntry>> setupKeyword(String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}
		String target = IConstants.FAILURE_KEY;
		String systemId = user.getSystemId();
		int userId = user.getId();
		WebMenuAccessEntry webMenu = null;
		Optional<WebMenuAccessEntry> webEntryOptional = this.webMenuRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException("WebMenuAccessEntry not found.");
		}
		logger.info(systemId + "[" + user.getRole() + "] Setup Keyword Request");
		Collection<UserEntry> users = null;
		try {
			if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				if (Access.isAuthorized(user.getRole(), "isAuthorizedSystem")) {
					EntryObject e = new PredicateBuilderImpl().getEntryObject();
					Predicate p = e.get("role").in("admin", "user").or(e.get("id").equal(user.getId()));
					users = GlobalVars.UserEntries.values(p);
				} else {
					users = GlobalVars.UserEntries.values();
				}
				target = IConstants.SUCCESS_KEY;
				return ResponseEntity.ok(users);
			} else if (webMenu.isTwoWay()) {
				if (Access.isAuthorized(user.getRole(), "isAuthorizedAdmin")) {
					Predicate<Integer, UserEntry> p = new PredicateBuilderImpl().getEntryObject().get("masterId")
							.equal(systemId);
					users = new ArrayList<UserEntry>(GlobalVars.UserEntries.values(p));
					users.add(GlobalVars.UserEntries.get(userOptional.get().getId()));
					return ResponseEntity.ok(users);
				}
				target = IConstants.SUCCESS_KEY;
			} else {
				logger.error("Authorization Failed :" + systemId);
				target = "invalidRequest";
				throw new UnauthorizedException("Authorization Failed");
			}
		} catch (UnauthorizedException e) {
			logger.error(systemId, e.toString());
			throw new UnauthorizedException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(systemId, e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}

		return ResponseEntity.ok(users);
	}

	@Override
	public ResponseEntity<String> deleteAllKeyWordByID(List<Integer> ids, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
			}
		
		String target = IConstants.FAILURE_KEY;
		String systemId = user.getSystemId();
		int userId = user.getId();
		WebMenuAccessEntry webMenu = null;
		Optional<WebMenuAccessEntry> webEntryOptional = this.webMenuRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.TWOWAY_WEBMENU_ACCESS_ENTRY_NOT_FOUND));
		}
		logger.info(messageResourceBundle.getLogMessage("twoway.delete.keyword.request.info"),
				systemId + "[" + user.getRole() + "]", ids);

		try {
			if (!ids.isEmpty()) {
				for (Integer id : ids) {
					if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem") || webMenu.isTwoWay()) {
						this.keywordRepo.deleteById(id);
						target = IConstants.SUCCESS_KEY;
						MultiUtility.changeFlag(Constants.KEYWORD_FLAG_FILE, "707");
					} else {
						throw new UnauthorizedException(messageResourceBundle
								.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
					}
				}
			}

		} catch (UnauthorizedException e) {
			logger.error(messageResourceBundle.getLogMessage("twoway.unauthorized.access.error"), e.getMessage(),
					e.getCause());
			throw new UnauthorizedException(e.getLocalizedMessage());
		} catch (Exception e) {
			throw new InternalServerException(messageResourceBundle.getExMessage(
					ConstantMessages.TWOWAY_INTERNALSERVER_ERROR, new Object[] { e.getLocalizedMessage() }));
		}

		return new ResponseEntity<>(messageResourceBundle.getMessage(ConstantMessages.TWOWAY_KEYWORD_DELETED_SUCCESS),
				HttpStatus.OK);
	}

}
