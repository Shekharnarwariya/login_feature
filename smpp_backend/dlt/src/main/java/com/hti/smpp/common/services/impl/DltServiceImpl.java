package com.hti.smpp.common.services.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.hti.smpp.common.config.dto.DltEntry;
import com.hti.smpp.common.config.dto.DltTemplEntry;
import com.hti.smpp.common.config.repository.DltEntryRepository;
import com.hti.smpp.common.config.repository.DltTemplEntryRepository;

import com.hti.smpp.common.exception.DataAccessError;
import com.hti.smpp.common.exception.InternalServerException;

import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.request.DltRequest;
import com.hti.smpp.common.request.DltTempRequest;
import com.hti.smpp.common.response.DltResponse;
import com.hti.smpp.common.response.DltTempResponse;
import com.hti.smpp.common.services.DltService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.Converters;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;
import com.hti.smpp.common.util.MultiUtility;

@Service

public class DltServiceImpl implements DltService {

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private DltEntryRepository dltRepo;

	@Autowired
	private DltTemplEntryRepository dlttempRepo;
	
	@Autowired
	private MessageResourceBundle messageResourceBundle;
	

	private static final Logger logger = LoggerFactory.getLogger(DltServiceImpl.class.getName());

	
	/**
	 * Saves a DLT entry with the provided information.
	 * @return A ResponseEntity with the operation result (SUCCESS_KEY or FAILURE_KEY).
	 */
	
	@Override
	public ResponseEntity<?> saveDltEntry(DltRequest dltEntry, String username) {
		// Retrieve user information based on the provided name
		
		logger.info(messageResourceBundle.getLogMessage("dlt.saveDltEntry.enter"));
		
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}
		
		String target = IConstants.FAILURE_KEY;

		// Log the request details
		logger.info(messageResourceBundle.getLogMessage("dlt.saveDltEntry.userid"),userEntry.getSystemId(),userEntry.getRole(),dltEntry.getSender());
		
		try {
			 // Create a new DltEntry instance
			DltEntry dltObj = new DltEntry();
			 // Set DltEntry properties from the provided DltRequest
			dltObj.setUsername(dltEntry.getUsername());
			dltObj.setSender(dltEntry.getSender());
			dltObj.setPeId(dltEntry.getPeId());
			dltObj.setTelemarketerId(dltEntry.getTelemarketerId());			
			 // Call the saveDltEntry method to handle DltEntry saving
			DltEntry dlt =  saveDltEntry(dltObj);			
			DltResponse dltResponse= new DltResponse();		
			dltResponse.setId(dlt.getId());
			dltResponse.setPeId(dlt.getPeId());
			dltResponse.setUsername(dlt.getUsername());
			dltResponse.setSender(dlt.getSender());
			dltResponse.setTelemarketerId(dlt.getTelemarketerId());	
//			logger.info(" DltEntry Added:" + dltEntry);
			logger.info(messageResourceBundle.getLogMessage("dlt.saveDltEntry.dltEntry"),dltEntry);
			// Log success and update target
			target = IConstants.SUCCESS_KEY;
//			logger.info("Dlt Entry Added Successfully");
			logger.info(messageResourceBundle.getLogMessage("dlt.saveDltEntry.success"));

			 // Trigger a flag change for DLT processing
			MultiUtility.changeFlag(Constants.DLT_FLAG_FILE, "707");
			
           return new ResponseEntity<>(dltResponse, HttpStatus.CREATED);
           
		}catch (Exception e) {
//    		logger.error("Error Occured While Processing");
    		logger.info(messageResourceBundle.getLogMessage("dlt.saveDltEntry.error"));
			 throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR, new Object[] {e.getMessage()}));
			
		}	
		
	}

	/**
	 * Saves a DLT entry to the repository, handling null or empty values appropriately.
	 * @throws Exception If an error occurs during the save operation.
	 */
	
	public DltEntry saveDltEntry(DltEntry entry)  {
		try {
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
			 // Save the DltEntry to the repository
			return this.dltRepo.save(entry);
		    		
		}catch (DataIntegrityViolationException e) {
//			logger.error("DataIntegrity Violation Exception error Occurred");
			logger.info(messageResourceBundle.getLogMessage("dlt.saveDltEntry.DataIntegrity.error"));
          throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.DUPLICATE_ENTRY_FOUND));    
       } catch (ConstraintViolationException e) {
          throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.DUPLICATE_ENTRY_FOUND));
       } catch (Exception e) {	
//			 logger.error("An error occurred while saving DltEntry to the repository", e);
			 logger.info(messageResourceBundle.getLogMessage("dlt.saveDltEntry.Exception.error"),e);
		  throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.FAILED_TO_ADD_ENTRY));		 
		}
		

	}

	
	/**
	 * Adds DLT templates either from an uploaded file or from a provided JSON entry.
	 * @param username  The username of the user performing the operation.
	 */
	
	@Override
	public ResponseEntity<?> addDltTemplate(String entryForm, MultipartFile file, String username) {

		logger.info(messageResourceBundle.getLogMessage("dlt.addDltTemplate.enter"));
		
		// Retrieve user information based on the provided username
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		// Check if the user is present and has the required roles
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}

		// Default target value
		String target = IConstants.FAILURE_KEY;

		 // Log user details and additional information
//		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "]");
		logger.info(messageResourceBundle.getLogMessage("dlt.addDltTemplate.entry"),userEntry.getSystemId(),userEntry.getRole());
		
		DltTempRequest form;
		try {	
			 // List to store DltTemplEntry instances for saving
			List<DltTemplEntry> list = new ArrayList<DltTemplEntry>();
			 // Check if the file is provided and has a name
			if (file != null && file.getName().length() > 0) {
				
				// Log the file name for XLS file processing
//				logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Add Dlt Template Request: "
//						+ file.getName());
				logger.info(messageResourceBundle.getLogMessage("dlt.addDltTemplate.systemid"),userEntry.getSystemId(),userEntry.getRole(),file.getName());
				Workbook workbook = null;
				try {
					 // Determine the workbook type based on the file extension (XLS or XLSX)

					if (file.getOriginalFilename().indexOf(".xlsx") > 0) {
						workbook = new XSSFWorkbook(file.getInputStream());
					} else {
						workbook = new HSSFWorkbook(file.getInputStream());
					}
					Sheet firstSheet = workbook.getSheetAt(0);
//					logger.info(
//							userEntry.getSystemId() + " Sheet[0] Total Rows: " + firstSheet.getPhysicalNumberOfRows());

					logger.info(messageResourceBundle.getLogMessage("dlt.addDltTemplate.firstSheet"),userEntry.getSystemId(),firstSheet.getPhysicalNumberOfRows());
					
					int column_count = 0;
					for (Row nextRow : firstSheet) {
						// x++;
						if (nextRow.getRowNum() == 0) {
							column_count = nextRow.getPhysicalNumberOfCells();
							if (column_count == 0) {
//								logger.info("Invalid Format For Xls File");
								logger.info(messageResourceBundle.getLogMessage("dlt.addDltTemplate.Invalid"));
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
//											System.out.println(nextRow.getRowNum() + " [PE_ID]Invalid Char found[" + c
//													+ "]: " + cell_value.charAt(c));
											
											logger.info(messageResourceBundle.getLogMessage("dlt.addDltTemplate.getRowNum"),nextRow.getRowNum(), c,cell_value.charAt(c));
										}
									}
								} else {
//									logger.info("Invalid PE_ID For Entry[" + nextRow.getRowNum() + "]: " + cell_value);
									logger.info(messageResourceBundle.getLogMessage("dlt.addDltTemplate.PE_ID"),nextRow.getRowNum(),cell_value);
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
//											System.out.println(nextRow.getRowNum() + " [Template_ID]Invalid Char found["
//													+ c + "]: " + cell_value.charAt(c));
											logger.info(messageResourceBundle.getLogMessage("dlt.addDltTemplate.Template_ID]Invalid"),nextRow.getRowNum(),c,cell_value.charAt(c));
										}
									}
								} else {
//									logger.info("Invalid Template_ID For Entry[" + nextRow.getRowNum() + "]: "
//											+ cell_value);
									logger.info(messageResourceBundle.getLogMessage("dlt.addDltTemplate.Template_ID"),nextRow.getRowNum(),cell_value);
									continue;
								}
							}
							if (i == 2) {
								String cell_value = cell.getRichStringCellValue().getString();
								if (cell_value != null) {
									template = cell_value;
								} else {
//									logger.info(
//											"Invalid Template For Entry[" + nextRow.getRowNum() + "]: " + cell_value);
									logger.info(messageResourceBundle.getLogMessage("dlt.addDltTemplate.invalid.template"),nextRow.getRowNum(),cell_value);
									continue;
								}
							}
						}
//						logger.info(nextRow.getRowNum() + ": peId=" + peId + ",templateId=" + templateId + ",template="
//								+ template);
						
						logger.info(messageResourceBundle.getLogMessage("dlt.addDltTemplate.getRow.template"),nextRow.getRowNum(),peId,templateId,template);
						
						
						  // Check if the template is not null or empty, then add it to the list
						if (template != null && template.length() > 0) {
							if (template.contains("�")) {
								template = template.replaceAll("�", "'");
							}
							String encoded_content = new Converters().UTF16(template);
							list.add(new DltTemplEntry(peId, templateId, encoded_content));
						}
					}
				} catch (DataAccessError ex) {
						
					throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.DLT_DATA_ACCESS_SUCC));
				} finally {
					// Close the workbook after processing
					if (workbook != null) {
						try {
							workbook.close();
						} catch (Exception e) {
							throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR, new Object[] {e.getLocalizedMessage()}));
						}
					}
				}
			} else {
				 // Process JSON entry if the file is not provided
				try {
					ObjectMapper objectMapper = new ObjectMapper();
					form = objectMapper.readValue(entryForm, DltTempRequest.class);
				} catch (MismatchedInputException e) {
//					logger.error("An error occurred while processing DltEntry.");
					logger.info(messageResourceBundle.getLogMessage("dlt.addDltTemplate.error.dltentry"));
					throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.DLT_ERROR_JSON));
				}
				
				// Log Dlt Template Request details
//				logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Add Dlt Template Request: "
//						+ form.getTemplateId());
				
				logger.info(messageResourceBundle.getLogMessage("dlt.addDltTemplate.Request.template"),userEntry.getSystemId(), userEntry.getRole(),form.getTemplateId());

				 // Check if necessary fields are not null, then proceed to add the template to the list
				if (form.getTemplateId() != null && form.getTemplate() != null && form.getPeId() != null) {
//					System.out.println( form.getTemplateId() +  form.getTemplate() + form.getPeId() );
					DltTemplEntry entry = new DltTemplEntry();
					
					entry.setPeId(form.getPeId());
					entry.setTemplate(form.getTemplate());
					entry.setTemplateId(form.getTemplateId());
						
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
//				logger.info("<--- Dlt Templates empty --> ");
				logger.info(messageResourceBundle.getLogMessage("dlt.addDltTemplate.empty.template"));
				throw new NotFoundException("No Entries to Add");
			} else {
//				logger.info(" Dlt Template entries:" + list.size());
				logger.info(messageResourceBundle.getLogMessage("dlt.addDltTemplate.entries.template"),list.size());
				int counter = saveDltTemplate(list);
//				logger.info(" Dlt Template Inserted: " + counter);
				logger.info(messageResourceBundle.getLogMessage("dlt.addDltTemplate.Inserted.template"),counter);
				target = IConstants.SUCCESS_KEY;
//				logger.info("Data Added to List Successfully");
				logger.info(messageResourceBundle.getLogMessage("dlt.addDltTemplate.Successfully.template"));
				if (counter > 0) {
					MultiUtility.changeFlag(Constants.DLT_FLAG_FILE, "707");
				}
			}
		}catch (DataAccessError ex) {	
			throw new DataAccessError(messageResourceBundle.getExMessage(ConstantMessages.DLT_ERR_UNSUCC));
		}catch (ConstraintViolationException ex) {
			throw new ConstraintViolationException(ex.getMessage(), ex.getSQLException(), "Duplicate Entry");
		}catch (Exception ex) {
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.DLT_DUPLI_ENTY));
		}
//		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Add Dlt Template Target: " + target);	
		logger.info(messageResourceBundle.getLogMessage("dlt.addDltTemplate.userRole.template"),userEntry.getSystemId(),userEntry.getRole(),target);
			return new ResponseEntity<>(messageResourceBundle.getExMessage(ConstantMessages.DLT_SUCC_ADD), HttpStatus.CREATED);
	}

	/**
	 * Saves a list of DltTemplEntry instances to the database.
	 * @throws Exception If an error occurs during the saving process.
	 */
	
	public int saveDltTemplate(List<DltTemplEntry> list) {
		

		 // Counter to track the number of successfully saved entries
		int counter = 0;
		 // Iterate through the list and attempt to save each entry to the database
		for (DltTemplEntry entry : list) {
			try {
				  // Save the entry to the database
				dlttempRepo.save(entry);

				counter++;			
			} catch (ConstraintViolationException ex) {
				throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.DLT_REQ_UNSUCC));

			}
		}
		// Return the total number of successfully saved entries
		return counter;
	}

	
	/**
	 * Retrieves a list of DltEntry instances based on the provided username.
	 * @return A ResponseEntity containing a list of DltResponse instances.
	 */
	
	@Override
	public ResponseEntity<List<DltResponse>> listDltEntry(String username) {

		logger.info(messageResourceBundle.getLogMessage("dlt.listDltEntry.enter"));
		
		String target = IConstants.FAILURE_KEY;

		// Retrieve user information based on the provided username
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		 // Check user authorization
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}

//		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] list DltEntry Request");
		logger.info(messageResourceBundle.getLogMessage("dlt.listDltEntry.DltEntry"),userEntry.getSystemId(),userEntry.getRole());

		try {

		List<DltEntry> list = savelistDltEntry();
		
		List<DltResponse> responseList= new ArrayList<DltResponse>();
		 // Convert DltEntry instances to DltResponse instances
		list.forEach(l -> {
			DltResponse response = new DltResponse();
			
			response.setId(l.getId());
			response.setUsername(l.getUsername());
			response.setPeId(l.getPeId());
			response.setSender(l.getSender());
			response.setTelemarketerId(l.getTelemarketerId());
			
			responseList.add(response);
		});
		
		if (list != null && !list.isEmpty()) {
			logger.info(" DltEntry List: " + list.size());
			logger.info(messageResourceBundle.getLogMessage("dlt.listDltEntry.List"),list.size());
			target = IConstants.SUCCESS_KEY;
//			logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] list DltEntry Target: " + target);
			logger.info(messageResourceBundle.getLogMessage("dlt.listDltEntry.DltEntry.target"),userEntry.getSystemId(),userEntry.getRole(),target);

			return new ResponseEntity<>(responseList, HttpStatus.OK);
		} else {
//			logger.error("No data is Avaliable in the template list");
			logger.info(messageResourceBundle.getLogMessage("dlt.listDltEntry.error"));
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.DLT_TEMP_EMPTY));
		}

		
		} catch (Exception e) {
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR));
		}
		
		

	}

	/**
	 * Retrieves a list of all DltEntry instances from the database.
	 * @return A list of DltEntry instances.
	 */
	public List<DltEntry> savelistDltEntry() {
		List<DltEntry> list =null;
		try {
		 list = dltRepo.findAll();		
		} catch (Exception e) {
			throw new NotFoundException("List is Empty");
		}
		return list;
	}

	
	/**
	 * Retrieves a list of DltTemplEntry instances based on the provided username.
	 * @return A ResponseEntity containing a list of DltTempResponse instances.
	 */
	
	@Override
	public ResponseEntity<List<DltTempResponse>> listDltTemplate(String username) {
		
		String target = IConstants.FAILURE_KEY;
		logger.info(messageResourceBundle.getLogMessage("dlt.listDltTemplate.enter"));

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}

//		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] list Dlt Template Request");
		logger.info(messageResourceBundle.getLogMessage("dlt.listDltTemplate.Template.request"),userEntry.getSystemId(),userEntry.getRole());
		
			
		// Convert DltTemplEntry instances to DltTempResponse instances
       List<DltTempResponse> responseList= new ArrayList<DltTempResponse>();
		
		try {
			List<DltTemplEntry> list = listDltTemplate();
			list.forEach(l -> {
				DltTempResponse response = new DltTempResponse();				
				response.setId(l.getId());
				response.setPeId(l.getPeId());
				response.setTemplate(l.getTemplate());
				response.setTemplateId(l.getTemplateId());
				
				responseList.add(response);
			});
			
			
			if (list != null && !list.isEmpty()) {
//				logger.info(" Dlt Template List: " + list.size());
				logger.info(messageResourceBundle.getLogMessage("dlt.listDltTemplate.Template.list"),list.size());
				target = IConstants.SUCCESS_KEY;
//				logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] list Dlt Template Target: " + target);
				logger.info(messageResourceBundle.getLogMessage("dlt.listDltTemplate.Template.Target"),userEntry.getSystemId(),userEntry.getRole(),target);
				return new ResponseEntity<>(responseList, HttpStatus.OK);
			} else {
//				logger.error("No Record Avaliable");
				logger.info(messageResourceBundle.getLogMessage("dlt.listDltTemplate.noRecord"));
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.DLT_NOT_FOUND_ERROR));
			}
			
		} catch (Exception e) {
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.DLT_TEMP_EMPTY));
		}
		

	}

	
	/**
	 * Converts a Unicode hexadecimal string to a human-readable string message.
	 * @return The human-readable string message.
	 */
	
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

	
	/**
	 * Retrieves a list of DltTemplEntry instances from the repository and decodes template content.
	 * @return List of DltTemplEntry instances with decoded template content.
	 */
	
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

	
	/**
	 * Updates a DltEntry instance in the repository based on the provided DltRequest.
	 * @return A ResponseEntity indicating the success or failure of the operation.
	 */
	
	@Override

	public ResponseEntity<?> updateDltEntry(DltRequest dltEntry, String username) {

		logger.info(messageResourceBundle.getLogMessage("dlt.updateDltEntry.enter"));
		
		String target = IConstants.FAILURE_KEY;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}

//		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Update DltEntry Request: "
//				+ dltEntry.getSender());
		logger.info(messageResourceBundle.getLogMessage("dlt.updateDltEntry.Update.userDetail"),userEntry.getSystemId(),userEntry.getRole(),dltEntry.getSender());
		
		try {

			DltEntry dlt = new DltEntry();	
			dlt.setId(dltEntry.getId());
			dlt.setUsername(dltEntry.getUsername());
			dlt.setSender(dltEntry.getSender());
			dlt.setPeId(dltEntry.getPeId());
			dlt.setTelemarketerId(dltEntry.getTelemarketerId());
					

			updateDltEntry(dlt);
			target = IConstants.SUCCESS_KEY;
//			logger.info("message.operation.success");
			logger.info(messageResourceBundle.getLogMessage("dlt.updateDltEntry.success"));
			MultiUtility.changeFlag(Constants.DLT_FLAG_FILE, "707");
//			logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Update DltEntry Target: " + target);
			logger.info(messageResourceBundle.getLogMessage("dlt.updateDltEntry.Target"),userEntry.getSystemId(),userEntry.getRole(),target);
			return new ResponseEntity<>(messageResourceBundle.getMessage(ConstantMessages.DLT_UPDATE_SUCCESS), HttpStatus.OK);

		} catch (Exception e) {
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR, new Object[] {e.getMessage()}));
		}	
			
	}

	/**
	 * Updates a DltEntry instance in the repository.
	 * @throws Exception If an error occurs during the update operation.
	 */
	
	public void updateDltEntry(DltEntry dltEntry)  {
		
		try {
			if (dltEntry.getUsername() != null && dltEntry.getUsername().length() > 0) {
			} else {
				dltEntry.setUsername(null);
			}
			if (dltEntry.getSender() != null && dltEntry.getSender().length() > 0) {
			} else {
				dltEntry.setSender(null);
			}
			if (dltEntry.getTelemarketerId() != null && dltEntry.getTelemarketerId().length() > 0) {
			} else {
				dltEntry.setTelemarketerId(null);
			}
			dltRepo.save(dltEntry);
		} catch (Exception e) {
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.DLT_ENTRY_NOTFOUND));

		}
		
	}

	
	@Override
	public ResponseEntity<?> updateDltTemplate(DltTempRequest dltTemplate, String username) {
		
		String target = IConstants.FAILURE_KEY;
		logger.info(messageResourceBundle.getLogMessage("dlt.updateDltTemplate.enter"));
		
		// Retrieve the user information based on the provided username
		
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}

//		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Update Dlt Template Request: "
//				+ dltTemplate.getTemplateId());
		
		logger.info(messageResourceBundle.getLogMessage("dlt.updateDltTemplate.Template.user"),userEntry.getSystemId(), userEntry.getRole(),dltTemplate.getTemplateId());
		
		
		try {
			DltTemplEntry dlt = new DltTemplEntry();
		
			dlt.setId(dltTemplate.getId());
			dlt.setPeId(dltTemplate.getPeId());
			dlt.setTemplate(dltTemplate.getTemplate());
			dlt.setTemplateId(dltTemplate.getTemplateId());
			
			updateDltTemplate(dlt);
			 // Update the target status to indicate success
			target = IConstants.SUCCESS_KEY;
//			logger.error("message.operation.success");
			logger.info(messageResourceBundle.getLogMessage("dlt.updateDltTemplate.success"));
			MultiUtility.changeFlag(Constants.DLT_FLAG_FILE, "707");
//			logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Update Dlt Template Target: " + target);			
			logger.info(messageResourceBundle.getLogMessage("dlt.updateDltTemplate.Target"),userEntry.getSystemId(),userEntry.getRole(),target);
			
			return new ResponseEntity<>(messageResourceBundle.getExMessage(ConstantMessages.DLT_TEMP_UPDATE_SUCCES), HttpStatus.OK);

		} catch (Exception e) {
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR, new Object[] {e.getLocalizedMessage()}));
		}
	}

	
	/**
	 * Update the Dlt Template entry with the provided content, ensuring proper encoding.
	 * @throws Exception If an error occurs during the update operation
	 */
	
	public void updateDltTemplate(DltTemplEntry dltTemplate) {
		// Extract the template content from the entry
		try {
			if (dltTemplate.getTemplate() != null && dltTemplate.getTemplate().trim().length() > 0) {
				String encoded_content = new Converters().UTF16(dltTemplate.getTemplate());
				if (encoded_content.length() > 0) {
					dltTemplate.setTemplate(encoded_content);
				} else {
					dltTemplate.setTemplate(null);
				}

			} else {
				dltTemplate.setTemplate(null);
			}
			 // Save the updated Dlt Template entry
			dlttempRepo.save(dltTemplate);
		} catch (Exception e) {
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.DLT_TEMP_NOTFOUND));
		}
		
	}

	
	@Override
	public ResponseEntity<?> deleteDltEntry(int id, String username) {
		
		
		String target = IConstants.FAILURE_KEY;
		 // Retrieve the user information based on the provided username
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}


		try {
			
			 if (!dltRepo.existsById(id)) {
		            // Return a ResponseEntity indicating that the provided DltEntry ID is not present
				 throw new NotFoundException("User Not Found with given Id");
		        }
			 			 
			deleteDltEntry(id);
			 // Update the target status to indicate success
			target = IConstants.SUCCESS_KEY;
//			logger.info("message.operation.success");
			logger.info(messageResourceBundle.getLogMessage("dlt.deleteDltEntry.success"));
			MultiUtility.changeFlag(Constants.DLT_FLAG_FILE, "707");

		} catch (Exception ex) {
			
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.DLT_RESOURCE_NOTFOUND, new Object[] {ex.getLocalizedMessage()}));
		}		
			return new ResponseEntity<>(messageResourceBundle.getExMessage(ConstantMessages.DLT_DELETE_ENTRY), HttpStatus.OK);
	}

	/**
	 * Delete the provided DltEntry from the repository.
	 */
	
	public void deleteDltEntry(int id) {
		dltRepo.deleteById(id);	

	}

	
	@Override

	public ResponseEntity<?> deleteDltTemplate(int id, String username) {
	
		String target = IConstants.FAILURE_KEY;

		 // Retrieve the user information based on the provided username
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}

		try {

			
			 if (!dlttempRepo.existsById(id)) {
		            // Return a ResponseEntity indicating that the provided DltEntry ID is not present
				 throw new NotFoundException("Template not found with given id");
		        }
			 
			 
			deleteDltTemplate(id);

			 // Update the target status to indicate success
			target = IConstants.SUCCESS_KEY;
			// Log a success message
//			logger.info("message.operation.success");
			logger.info(messageResourceBundle.getLogMessage("dlt.deleteDltTemplate.success"));
			MultiUtility.changeFlag(Constants.DLT_FLAG_FILE, "707");

		} catch (Exception ex) {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.DLT_RESOURCE_NOTFOUND, new Object[] {ex.getLocalizedMessage()}));
		}
//		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Remove Dlt Template Target: " + target);
		logger.info(messageResourceBundle.getLogMessage("dlt.deleteDltTemplate.Target"),userEntry.getSystemId(),userEntry.getRole(),target);
		


			return new ResponseEntity<>(messageResourceBundle.getExMessage(ConstantMessages.DLT_TEMP_DELETED), HttpStatus.OK);
	}

	
	/**
	 * Delete the provided DltTemplEntry from the repository.
	 */
	
	public void deleteDltTemplate(int id) {
		dlttempRepo.deleteById(id);
	}

	
	/**
	 * Retrieve the DltEntry information based on the provided ID.
	 * @throws UnauthorizedException If the user does not have the required roles for this operation
	 */
	
	@Override
	public DltResponse getDltEntry(int id, String username) {

		 // Initialize the target status to indicate failure
		String target = IConstants.FAILURE_KEY;

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}

//		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] View DltEntry Request:" + id);
		logger.info(messageResourceBundle.getLogMessage("dlt.getDltEntry.DltEntry"),userEntry.getSystemId(),userEntry.getRole(),id);

		try {
			 // Retrieve the DltEntry based on the provided ID
		DltEntry entry = getDltEntry(id);
		
		 // Create a DltResponse to hold the response information
		DltResponse responseEntry = new DltResponse();
		responseEntry.setId(entry.getId());
		responseEntry.setUsername(entry.getUsername());
		responseEntry.setPeId(entry.getPeId());
		responseEntry.setSender(entry.getSender());
		responseEntry.setTelemarketerId(entry.getTelemarketerId());

		// Update the target status to indicate success
		target = IConstants.SUCCESS_KEY;

//		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] View DltEntry Target:" + target);
		logger.info(messageResourceBundle.getLogMessage("dlt.getDltEntry.target"),userEntry.getSystemId(),userEntry.getRole(),target);
		

		return responseEntry;
		} catch (NoSuchElementException ex) {
		    // NotFoundException: Rethrow the exception or handle it as needed
		    throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.DLT_ENTRY_NOTFOUND));
		} catch (Exception e) {
		    // Handle other exceptions
//		    logger.error("An error occurred while processing DltEntry.", e);
		    // Optionally throw a custom exception or return an error response
		    throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR, new Object[] {e.getLocalizedMessage()}));
		}
	}

	
	/**
	 * Retrieve the DltEntry based on the provided ID.
	 * @throws NotFoundException If the DltEntry with the provided ID is not found
	 */
	
	public DltEntry getDltEntry(int id) {
		DltEntry entry = dltRepo.findById(id).get();
		// Return the retrieved DltEntry
		return entry;
	}

	@Override
	public DltTempResponse getDltTemplate(int id, String username) {

		String target = IConstants.FAILURE_KEY;

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}

//		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] View Dlt Template Request:" + id);
		logger.info(messageResourceBundle.getLogMessage("dlt.getDltTemplate.DltEntry"),userEntry.getSystemId(),userEntry.getRole(),id);

		try {
		DltTemplEntry entry = getDltTemplate(id);
		
		DltTempResponse responseList = new DltTempResponse();
		responseList.setId(entry.getId());
		responseList.setPeId(entry.getPeId());
		responseList.setTemplate(entry.getTemplate());
		responseList.setTemplateId(entry.getTemplateId());

		target = IConstants.SUCCESS_KEY;

		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] View Dlt Template Target:" + target);
		logger.info(messageResourceBundle.getLogMessage("dlt.getDltTemplate.target"),userEntry.getSystemId(),userEntry.getRole(),target);


		 return responseList;
		} catch (NoSuchElementException ex) {
		    throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.DLT_TEMP_NOTFOUND));
		} catch (Exception e) {
//			logger.error("An error occurred while processing DltEntry.", e);
		    throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR, new Object[] {e.getLocalizedMessage()}));
		}
	}

	/**
	 * Retrieve the DltTemplEntry based on the provided ID, and decode the template content
	 * @throws NotFoundException If the DltTemplEntry with the provided ID is not found
	 */
	
	public DltTemplEntry getDltTemplate(int id) {
		DltTemplEntry entry = dlttempRepo.findById(id).get();
		 // Check and decode the template content if it exists
		
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
		// Return the retrieved DltTemplEntry
		return entry;
	}

	
	/**
	 * Convert Unicode hexadecimal string to a UTF-16 encoded string.
	 * @return The UTF-16 encoded string resulting from the conversion
	 */
	
	public String uniHexToCharMsg1(String msg) {
		if (msg == null || msg.length() == 0) {
			msg = "0020";
		}
		// Initialize variables for processing the message
		boolean reqNULL = false;
		byte[] charsByt, var;
		int x = 0;
		try {
			if (msg.substring(0, 2).compareTo("00") == 0) {
				reqNULL = true;
			}
			 // Convert the hexadecimal string to a byte array
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
			 // Copy the bytes to the new byte array
			for (int l = 0; l < charsByt.length; l++) {
				var[++x] = charsByt[l];
			}
			msg = new String(var, "UTF-16");
		} catch (Exception ex) {
			logger.error(msg, ex);
		}
		// Return the resulting UTF-16 encoded string
		return msg;
	}

}
