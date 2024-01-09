package com.hti.smpp.common.services.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hti.smpp.common.config.dto.DltEntry;
import com.hti.smpp.common.config.dto.DltTemplEntry;
import com.hti.smpp.common.config.repository.DltEntryRepository;
import com.hti.smpp.common.config.repository.DltTemplEntryRepository;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.JsonProcessingError;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.request.DltRequest;
import com.hti.smpp.common.request.DltTempRequest;
import com.hti.smpp.common.services.DltService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.Converters;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;

@Service

public class DltServiceImpl implements DltService {

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private DltEntryRepository dltRepo;

	@Autowired
	private DltTemplEntryRepository dlttempRepo;

	private static final Logger logger = LoggerFactory.getLogger(DltServiceImpl.class.getName());

	@Override
	public ResponseEntity<?> saveDltEntry(DltRequest entry, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		// TODO Auto-generated method stub

		String target = IConstants.FAILURE_KEY;

		logger.info(
				userEntry.getSystemId() + "[" + userEntry.getRole() + "] Add DltEntry Request: " + entry.getSender());
		// IDatabaseService dbService = HtiSmsDB.getInstance();
		try {

			DltEntry dlt = new DltEntry();
			BeanUtils.copyProperties(entry, dlt);
			saveDltEntry(dlt);
			logger.info(" DltEntry Added:" + entry);
			target = IConstants.SUCCESS_KEY;
			logger.info("message.operation.success");
			MultiUtility.changeFlag(Constants.DLT_FLAG_FILE, "707");

		} catch (Exception e) {
			logger.error(userEntry.getSystemId(), e.fillInStackTrace());
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]", false);
		}
		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Add DltEntry Target: " + target);

		return new ResponseEntity<>(target, HttpStatus.CREATED);

	}

	public void saveDltEntry(DltEntry entry) throws Exception {
		if (entry.getUsername() != null && entry.getUsername().length() > 0) {
		} else {
			entry.setUsername(null);
		}
		if (entry.getSender() != null && entry.getSender().length() > 0) {
		} else {
			entry.setSender(null);
		}
		if (entry.getPeId() != null && entry.getPeId().length() > 0) {
		} else {
			entry.setPeId(null);
		}
		if (entry.getTelemarketerId() != null && entry.getTelemarketerId().length() > 0) {
		} else {
			entry.setTelemarketerId(null);
		}
		this.dltRepo.save(entry);
	}

	@Override
	public ResponseEntity<?> addDltTemplate(String entryForm, MultipartFile file, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		String target = IConstants.FAILURE_KEY;

		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "]");
		// IDatabaseService dbService = HtiSmsDB.getInstance();

		DltTempRequest form;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			form = objectMapper.readValue(entryForm, DltTempRequest.class);
			form.setTemplateFile(file);
		} catch (JsonProcessingException e) {
			throw new JsonProcessingError("JsonProccessingError: " + e.getLocalizedMessage());
		} catch (Exception ex) {
			throw new InternalServerException("Error: " + ex.getLocalizedMessage());
		}
       
		try {
			System.out.println(file);
			List<DltTemplEntry> list = new ArrayList<DltTemplEntry>();
			if (file != null && form.getTemplateFile().getName().length() > 0) {
				
				logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Add Dlt Template Request: "
						+ form.getTemplateFile().getName());
				Workbook workbook = null;
				try {

					if (file.getOriginalFilename().indexOf(".xlsx") > 0) {
						System.out.println("165");
						workbook = new XSSFWorkbook(form.getTemplateFile().getInputStream());
					} else {
						workbook = new HSSFWorkbook(form.getTemplateFile().getInputStream());
					}
					Sheet firstSheet = workbook.getSheetAt(0);
					logger.info(
							userEntry.getSystemId() + " Sheet[0] Total Rows: " + firstSheet.getPhysicalNumberOfRows());

					int column_count = 0;
					for (Row nextRow : firstSheet) {
						// x++;
						if (nextRow.getRowNum() == 0) {
							column_count = nextRow.getPhysicalNumberOfCells();
							if (column_count == 0) {
								logger.info("Invalid Format For Xls File");
								break;
							} else {
								continue;
							}
						}
						String peId = null, template = null, templateId = null;
						for (int i = 0; i < nextRow.getLastCellNum(); i++) {
							Cell cell = nextRow.getCell(i);
							// System.out.println("cellStr: " + cell.getStringCellValue());
							// System.out.println("cellForm: " + cell_value);
							if (i == 0) {
								String cell_value = new DataFormatter().formatCellValue(cell);
								if (cell_value != null) {
									peId = "";
									for (int c = 0; c < cell_value.length(); c++) {
										if (Character.isLetterOrDigit(cell_value.charAt(c))) {
											peId += cell_value.charAt(c) + "";
										} else {
											System.out.println(nextRow.getRowNum() + " [PE_ID]Invalid Char found[" + c
													+ "]: " + cell_value.charAt(c));
										}
									}
								} else {
									logger.info("Invalid PE_ID For Entry[" + nextRow.getRowNum() + "]: " + cell_value);
									continue;
								}
							}
							if (i == 1) {
								String cell_value = new DataFormatter().formatCellValue(cell);
								if (cell_value != null) {
									templateId = "";
									for (int c = 0; c < cell_value.length(); c++) {
										if (Character.isLetterOrDigit(cell_value.charAt(c))) {
											templateId += cell_value.charAt(c) + "";
										} else {
											System.out.println(nextRow.getRowNum() + " [Template_ID]Invalid Char found["
													+ c + "]: " + cell_value.charAt(c));
										}
									}
								} else {
									logger.info("Invalid Template_ID For Entry[" + nextRow.getRowNum() + "]: "
											+ cell_value);
									continue;
								}
							}
							if (i == 2) {
								String cell_value = cell.getRichStringCellValue().getString();
								if (cell_value != null) {
									template = cell_value;
								} else {
									logger.info(
											"Invalid Template For Entry[" + nextRow.getRowNum() + "]: " + cell_value);
									continue;
								}
							}
						}
						logger.info(nextRow.getRowNum() + ": peId=" + peId + ",templateId=" + templateId + ",template="
								+ template);
						if (template != null && template.length() > 0) {
							if (template.contains("�")) {
								template = template.replaceAll("�", "'");
							}
							String encoded_content = new Converters().UTF16(template);
							list.add(new DltTemplEntry(peId, templateId, encoded_content));
						}
					}
				} catch (Exception ex) {
					logger.info(userEntry.getSystemId(), ex.fillInStackTrace());
				} finally {
					if (workbook != null) {
						try {
							workbook.close();
						} catch (Exception e) {
						}
					}
				}
			} else {
				logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Add Dlt Template Request: "
						+ form.getTemplateId());
				
				if (form.getTemplateId() != null && form.getTemplate() != null && form.getPeId() != null) {
					System.out.println( form.getTemplateId() +  form.getTemplate() + form.getPeId() );
					DltTemplEntry entry = new DltTemplEntry();
					BeanUtils.copyProperties(form, entry);
					String template = entry.getTemplate();
					if (template != null && template.length() > 0) {
						if (template.contains("�")) {
							template = template.replaceAll("�", "'");
						}
						String encoded_content = new Converters().UTF16(template);
						entry.setTemplate(encoded_content);
						list.add(entry);
					}
				}
			}
			if (list.isEmpty()) {
				logger.info("<--- Dlt Templates empty --> ");
				logger.error("error.record.unavailable");
			} else {
				logger.info(" Dlt Template entries:" + list.size());
				int counter = saveDltTemplate(list);
				logger.info(" Dlt Template Inserted: " + counter);
				target = IConstants.SUCCESS_KEY;
				logger.info("message.operation.success");
				if (counter > 0) {
					MultiUtility.changeFlag(Constants.DLT_FLAG_FILE, "707");
				}
			}
		} catch (Exception ex) {
			logger.error(userEntry.getSystemId(), ex.fillInStackTrace());
			logger.error("error.processError");
		}
		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Add Dlt Template Target: " + target);

		return new ResponseEntity<>(target, HttpStatus.CREATED);

	}

	public int saveDltTemplate(List<DltTemplEntry> list) throws Exception {
		System.out.println(list);
		int counter = 0;
		for (DltTemplEntry entry : list) {
			try {
				dlttempRepo.save(entry);
				counter++;
			} catch (org.hibernate.exception.ConstraintViolationException ex) {
				logger.info("" + ex.getCause());
			}
		}
		return counter;
	}

	@Override
	public ResponseEntity<List<DltEntry>> listDltEntry(String username) {
		// TODO Auto-generated method stub
		String target = IConstants.FAILURE_KEY;

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		// TODO Auto-generated method stub

		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] list DltEntry Request");
		List<DltEntry> list = savelistDltEntry();
		if (list != null && !list.isEmpty()) {
			logger.info(" DltEntry List: " + list.size());
			target = IConstants.SUCCESS_KEY;
		} else {
			logger.error("error.record.unavailable");
		}

		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] list DltEntry Target: " + target);

		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	public List<DltEntry> savelistDltEntry() {
		List<DltEntry> list = dltRepo.findAll();
		return list;
	}

	@Override
	public ResponseEntity<List<DltTemplEntry>> listDltTemplate(String username) {
		// TODO Auto-generated method stub

		String target = IConstants.FAILURE_KEY;

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] list Dlt Template Request");
		List<DltTemplEntry> list = listDltTemplate();
		if (list != null && !list.isEmpty()) {
			logger.info(" Dlt Template List: " + list.size());
			target = IConstants.SUCCESS_KEY;
		} else {
			logger.error("error.record.unavailable");
		}

		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] list Dlt Template Target: " + target);

		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	public String uniHexToCharMsg(String msg) {
		if (msg == null || msg.length() == 0) {
			msg = "0020";
		}
		boolean reqNULL = false;
		byte[] charsByt, var;
		int x = 0;
		try {
			if (msg.substring(0, 2).compareTo("00") == 0) {
				reqNULL = true;
			}
			charsByt = new BigInteger(msg, 16).toByteArray();
			if (charsByt[0] == '\0') {
				var = new byte[charsByt.length - 1];
				for (int q = 1; q < charsByt.length; q++) {
					var[q - 1] = charsByt[q];
				}
				charsByt = var;
			}
			if (reqNULL) {
				var = new byte[charsByt.length + 1];
				x = 0;
				var[0] = '\0';
				reqNULL = false;
			} else {
				var = new byte[charsByt.length];
				x = -1;
			}
			for (int l = 0; l < charsByt.length; l++) {
				var[++x] = charsByt[l];
			}
			msg = new String(var, "UTF-16");
		} catch (Exception ex) {
			logger.error(msg, ex);
		}
		return msg;
	}

	public List<DltTemplEntry> listDltTemplate() {
		List<DltTemplEntry> list = dlttempRepo.findAll();
		for (DltTemplEntry entry : list) {
			if (entry.getTemplate() != null && entry.getTemplate().trim().length() > 0) {
				String decoded_content = uniHexToCharMsg(entry.getTemplate());
				if (decoded_content.length() > 0) {
					entry.setTemplate(decoded_content);
				} else {
					entry.setTemplate(null);
				}
			} else {
				entry.setTemplate(null);
			}
		}
		return list;
	}

	@Override
	public ResponseEntity<?> updateDltEntry(DltRequest entry, String username) {
		// TODO Auto-generated method stub
		String target = IConstants.FAILURE_KEY;

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

//		DltEntryForm entryForm = (DltEntryForm) actionForm;
		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Update DltEntry Request: "
				+ entry.getSender());
		try {
			DltEntry dlt = new DltEntry();
			BeanUtils.copyProperties(entry, dlt);
			updateDltEntry(dlt);
			target = IConstants.SUCCESS_KEY;
			logger.info("message.operation.success");
			MultiUtility.changeFlag(Constants.DLT_FLAG_FILE, "707");

		} catch (Exception e) {
			logger.error(userEntry.getSystemId(), e.fillInStackTrace());
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]", false);
		}
		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Update DltEntry Target: " + target);

		return new ResponseEntity<>(target, HttpStatus.ACCEPTED);

	}

	public void updateDltEntry(DltEntry entry) throws Exception {
		if (entry.getUsername() != null && entry.getUsername().length() > 0) {
		} else {
			entry.setUsername(null);
		}
		if (entry.getSender() != null && entry.getSender().length() > 0) {
		} else {
			entry.setSender(null);
		}
		if (entry.getTelemarketerId() != null && entry.getTelemarketerId().length() > 0) {
		} else {
			entry.setTelemarketerId(null);
		}
		/*
		 * if (entry.getContent() != null && entry.getContent().trim().length() > 0) {
		 * String encoded_content = Converter.getUTF8toHexDig(entry.getContent()); if
		 * (encoded_content.length() > 0) { entry.setContent(encoded_content); } else {
		 * entry.setContent(null); } } else { entry.setContent(null); }
		 */
		dltRepo.save(entry);
	}

	@Override
	public ResponseEntity<?> updateDltTemplate(DltTempRequest entry, String username) {
		// TODO Auto-generated method stub

		String target = IConstants.FAILURE_KEY;

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

//		DltTemplForm entryForm = (DltTemplForm) actionForm;

		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Update Dlt Template Request: "
				+ entry.getTemplateId());
		try {
			DltTemplEntry dlt = new DltTemplEntry();
			BeanUtils.copyProperties(entry, dlt);
			updateDltTemplate(dlt);
			target = IConstants.SUCCESS_KEY;
			logger.error("message.operation.success");
			MultiUtility.changeFlag(Constants.DLT_FLAG_FILE, "707");

		} catch (Exception e) {
			logger.error(userEntry.getSystemId(), e.fillInStackTrace());
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]", false);
		}
		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Update Dlt Template Target: " + target);

		return new ResponseEntity<>(target, HttpStatus.ACCEPTED);
	}

	public void updateDltTemplate(DltTemplEntry entry) throws Exception {
		if (entry.getTemplate() != null && entry.getTemplate().trim().length() > 0) {
			String encoded_content = new Converters().UTF16(entry.getTemplate());
			if (encoded_content.length() > 0) {
				entry.setTemplate(encoded_content);
			} else {
				entry.setTemplate(null);
			}
		} else {
			entry.setTemplate(null);
		}
		dlttempRepo.save(entry);
	}

	@Override
	public void deleteDltEntry(DltEntry entry, String username) {
		// TODO Auto-generated method stub
		String target = IConstants.FAILURE_KEY;

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

//		ActionMessages messages = new ActionMessages();
//		ActionMessage message = null;
//		UserSessionObject userSessionObject = (UserSessionObject) SessionHelper.getSessionObject(request,
//				IConstants.USER_SESSION_KEY);
//		String systemid = userSessionObject.getSystemId();
//		String role = userSessionObject.getRole();
//		DltEntryForm entryForm = (DltEntryForm) actionForm;

		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Remove DltEntry Request: "
				+ entry.getSender());
		try {
			DltEntry dlt = new DltEntry();
			BeanUtils.copyProperties(entry, dlt);
			deleteDltEntry(dlt);
			target = IConstants.SUCCESS_KEY;
			logger.info("message.operation.success");
			MultiUtility.changeFlag(Constants.DLT_FLAG_FILE, "707");

		} catch (Exception ex) {
			logger.error(userEntry.getSystemId(), ex.fillInStackTrace());
			logger.error("error.processError");
		}
		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Remove DltEntry Target: " + target);

	}

	public void deleteDltEntry(DltEntry entry) {
		dltRepo.delete(entry);
	}

	@Override
	public void deleteDltTemplate(DltTemplEntry entry, String username) {
		// TODO Auto-generated method stub

		String target = IConstants.FAILURE_KEY;
//		ActionMessages messages = new ActionMessages();
//		ActionMessage message = null;
//		UserSessionObject userSessionObject = (UserSessionObject) SessionHelper.getSessionObject(request,
//				IConstants.USER_SESSION_KEY);
//		String systemid = userSessionObject.getSystemId();
//		String role = userSessionObject.getRole();
//		DltTemplForm entryForm = (DltTemplForm) actionForm;

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Remove Dlt Template Request: "
				+ entry.getTemplateId());
		try {

			DltTemplEntry dlt = new DltTemplEntry();
			BeanUtils.copyProperties(entry, dlt);
			deleteDltTemplate(dlt);
			target = IConstants.SUCCESS_KEY;
			logger.info("message.operation.success");
			MultiUtility.changeFlag(Constants.DLT_FLAG_FILE, "707");

		} catch (Exception ex) {
			logger.error(userEntry.getSystemId(), ex.fillInStackTrace());
			logger.error("error.processError");
		}
		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Remove Dlt Template Target: " + target);

	}

	public void deleteDltTemplate(DltTemplEntry entry) {
		dlttempRepo.delete(entry);
	}

	@Override
	public DltEntry getDltEntry(int id, String username) {
		// TODO Auto-generated method stub
		String target = IConstants.FAILURE_KEY;

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
	
		}

//		UserSessionObject userSessionObject = (UserSessionObject) SessionHelper.getSessionObject(request,
//				IConstants.USER_SESSION_KEY);
//		String systemid = userSessionObject.getSystemId();
//		String role = userSessionObject.getRole();
//		DltEntryForm entryForm = (DltEntryForm) actionForm;

		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] View DltEntry Request:" + id);

		DltEntry entry = getDltEntry(id);

		target = IConstants.SUCCESS_KEY;

		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] View DltEntry Target:" + target);

		if(entry!=null) {
			return entry;
		}else {
			throw new NotFoundException("User not found with the provided ID.");
		}
//		return entry;
	}

	public DltEntry getDltEntry(int id) {
		DltEntry entry = dltRepo.findById(id).get();
		return entry;
//		Optional<DltEntry> optionalEntry = dltRepo.findById(id);
//
//	    DltEntry entry = null;
//	    if (optionalEntry.isPresent()) {
//	        entry = optionalEntry.get();
//	    }
//
//	    return entry;
	}

	@Override
	public DltTemplEntry getDltTemplate(int id, String username) {
		// TODO Auto-generated method stub

		String target = IConstants.FAILURE_KEY;
//		UserSessionObject userSessionObject = (UserSessionObject) SessionHelper.getSessionObject(request,
//				IConstants.USER_SESSION_KEY);
//		String systemid = userSessionObject.getSystemId();
//		String role = userSessionObject.getRole();
//		DltTemplForm entryForm = (DltTemplForm) actionForm;

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] View Dlt Template Request:" + id);

		DltTemplEntry entry = getDltTemplate(id);

		target = IConstants.SUCCESS_KEY;

		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] View Dlt Template Target:" + target);

		return entry;
	}

	public DltTemplEntry getDltTemplate(int id) {
		DltTemplEntry entry = dlttempRepo.findById(id).get();
		if (entry.getTemplate() != null && entry.getTemplate().trim().length() > 0) {
			String decoded_content = uniHexToCharMsg1(entry.getTemplate());
			if (decoded_content.length() > 0) {
				entry.setTemplate(decoded_content);
			} else {
				entry.setTemplate(null);
			}
		} else {
			entry.setTemplate(null);
		}
		return entry;
	}

	public String uniHexToCharMsg1(String msg) {
		if (msg == null || msg.length() == 0) {
			msg = "0020";
		}
		boolean reqNULL = false;
		byte[] charsByt, var;
		int x = 0;
		try {
			if (msg.substring(0, 2).compareTo("00") == 0) {
				reqNULL = true;
			}
			charsByt = new BigInteger(msg, 16).toByteArray();
			if (charsByt[0] == '\0') {
				var = new byte[charsByt.length - 1];
				for (int q = 1; q < charsByt.length; q++) {
					var[q - 1] = charsByt[q];
				}
				charsByt = var;
			}
			if (reqNULL) {
				var = new byte[charsByt.length + 1];
				x = 0;
				var[0] = '\0';
				reqNULL = false;
			} else {
				var = new byte[charsByt.length];
				x = -1;
			}
			for (int l = 0; l < charsByt.length; l++) {
				var[++x] = charsByt[l];
			}
			msg = new String(var, "UTF-16");
		} catch (Exception ex) {
			logger.error(msg, ex);
		}
		return msg;
	}

//	@Override
//	public ResponseEntity<?> saveDltTemplate(DltTempRequest entry, String username) {
//		
//		return null;
//	}

}
