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


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hti.smpp.common.config.dto.DltEntry;
import com.hti.smpp.common.config.dto.DltTemplEntry;
import com.hti.smpp.common.config.repository.DltEntryRepository;
import com.hti.smpp.common.config.repository.DltTemplEntryRepository;

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

	
	/**
	 * Saves a DLT entry with the provided information.
	 *
	 * @param entry    The DltRequest containing information for the DLT entry.
	 * @param username The username of the user performing the operation.
	 * @return A ResponseEntity with the operation result (SUCCESS_KEY or FAILURE_KEY).
	 */
	
	@Override
	public ResponseEntity<?> saveDltEntry(DltRequest entry, String username) {

		// Retrieve user information based on the provided username
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

		// Check if the user is present and has the required roles

	

		 // Default target value
		String target = IConstants.FAILURE_KEY;

		// Log the request details
		logger.info(
				userEntry.getSystemId() + "[" + userEntry.getRole() + "] Add DltEntry Request: " + entry.getSender());
	
		try {

			 // Create a new DltEntry instance
			DltEntry dlt = new DltEntry();


			 // Set DltEntry properties from the provided DltRequest
			dlt.setUsername(entry.getUsername());
			dlt.setSender(entry.getSender());
			dlt.setPeId(entry.getPeId());
			dlt.setTelemarketerId(entry.getTelemarketerId());
			
			 // Call the saveDltEntry method to handle DltEntry saving

			saveDltEntry(dlt);
			logger.info(" DltEntry Added:" + entry);
			// Log success and update target
			target = IConstants.SUCCESS_KEY;
			logger.info("message.operation.success");
			 // Trigger a flag change for DLT processing
			MultiUtility.changeFlag(Constants.DLT_FLAG_FILE, "707");

		} catch (Exception e) {
			logger.error(userEntry.getSystemId(), e.fillInStackTrace());
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]", false);
		}
		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Add DltEntry Target: " + target);

		return new ResponseEntity<>(target, HttpStatus.CREATED);

	}

	/**
	 * Saves a DLT entry to the repository, handling null or empty values appropriately.
	 *
	 * @param entry The DltEntry instance to be saved.
	 * @throws Exception If an error occurs during the save operation.
	 */
	
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
		 // Save the DltEntry to the repository
		this.dltRepo.save(entry);
	}

	
	/**
	 * Adds DLT templates either from an uploaded file or from a provided JSON entry.
	 * Handles different formats: XLS and direct form entry.
	 *
	 * @param entryForm JSON entry representing DltTempRequest (for direct entry).
	 * @param file      Uploaded file containing DLT templates (Excel format).
	 * @param username  The username of the user performing the operation.
	 * @return A ResponseEntity with the operation result (SUCCESS_KEY or FAILURE_KEY).
	 */
	
	@Override
	public ResponseEntity<?> addDltTemplate(String entryForm, MultipartFile file, String username) {

		// Retrieve user information based on the provided username
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		// Check if the user is present and has the required roles
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		// Default target value
		String target = IConstants.FAILURE_KEY;

		 // Log user details and additional information
		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "]");
		


		DltTempRequest form;

		try {
			System.out.println(file);

			 // List to store DltTemplEntry instances for saving
			List<DltTemplEntry> list = new ArrayList<DltTemplEntry>();
			 // Check if the file is provided and has a name
			if (file != null && file.getName().length() > 0) {
				
				// Log the file name for XLS file processing
				logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Add Dlt Template Request: "
						+ file.getName());
				Workbook workbook = null;
				try {
					 // Determine the workbook type based on the file extension (XLS or XLSX)

					if (file.getOriginalFilename().indexOf(".xlsx") > 0) {
						System.out.println("165");
						workbook = new XSSFWorkbook(file.getInputStream());
					} else {
						workbook = new HSSFWorkbook(file.getInputStream());
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
						  // Check if the template is not null or empty, then add it to the list
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
					// Close the workbook after processing
					if (workbook != null) {
						try {
							workbook.close();
						} catch (Exception e) {
						}
					}
				}
			} else {
				 // Process JSON entry if the file is not provided
				ObjectMapper objectMapper = new ObjectMapper();
				form = objectMapper.readValue(entryForm, DltTempRequest.class);
//				form.setTemplateFile(file);
				// Log Dlt Template Request details
				logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Add Dlt Template Request: "
						+ form.getTemplateId());
				
				 // Check if necessary fields are not null, then proceed to add the template to the list
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

	/**
	 * Saves a list of DltTemplEntry instances to the database.
	 *
	 * @param list The list of DltTemplEntry instances to be saved.
	 * @return The number of entries successfully saved to the database.
	 * @throws Exception If an error occurs during the saving process.
	 */
	
	public int saveDltTemplate(List<DltTemplEntry> list) throws Exception {
		System.out.println(list);
		 // Counter to track the number of successfully saved entries
		int counter = 0;
		 // Iterate through the list and attempt to save each entry to the database
		for (DltTemplEntry entry : list) {
			try {
				  // Save the entry to the database
				dlttempRepo.save(entry);
				// Increment the counter for successful saves
				counter++;
			} catch (org.hibernate.exception.ConstraintViolationException ex) {
				logger.info("" + ex.getCause());
			}
		}
		// Return the total number of successfully saved entries
		return counter;
	}

	
	/**
	 * Retrieves a list of DltEntry instances based on the provided username.
	 *
	 * @param username The username for which to retrieve DltEntry instances.
	 * @return A ResponseEntity containing a list of DltResponse instances.
	 */
	
	@Override
	public ResponseEntity<List<DltResponse>> listDltEntry(String username) {
		// TODO Auto-generated method stub
		String target = IConstants.FAILURE_KEY;

		// Retrieve user information based on the provided username
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		 // Check user authorization
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
		// Retrieve the list of DltEntry instances
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
			target = IConstants.SUCCESS_KEY;
		} else {
			logger.error("error.record.unavailable");
		}
		// Return the ResponseEntity containing the list of DltResponse instances
		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] list DltEntry Target: " + target);

		return new ResponseEntity<>(responseList, HttpStatus.OK);
	}

	/**
	 * Retrieves a list of all DltEntry instances from the database.
	 *
	 * @return A list of DltEntry instances.
	 */
	public List<DltEntry> savelistDltEntry() {
		List<DltEntry> list = dltRepo.findAll();
		return list;
	}

	
	/**
	 * Retrieves a list of DltTemplEntry instances based on the provided username.
	 *
	 * @param username The username for which to retrieve DltTemplEntry instances.
	 * @return A ResponseEntity containing a list of DltTempResponse instances.
	 */
	
	@Override
	public ResponseEntity<List<DltTempResponse>> listDltTemplate(String username) {
		

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
		// Convert DltTemplEntry instances to DltTempResponse instances
       List<DltTempResponse> responseList= new ArrayList<DltTempResponse>();
		
		list.forEach(l -> {
			DltTempResponse response = new DltTempResponse();
			
			response.setId(l.getId());
			response.setPeId(l.getPeId());
			response.setTemplate(l.getTemplate());
			response.setTemplateId(l.getTemplateId());
			
			responseList.add(response);
		});
		
		
		if (list != null && !list.isEmpty()) {
			logger.info(" Dlt Template List: " + list.size());
			target = IConstants.SUCCESS_KEY;
		} else {
			logger.error("error.record.unavailable");
		}

		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] list Dlt Template Target: " + target);
		// Return the ResponseEntity containing the list of DltTempResponse instances
		return new ResponseEntity<>(responseList, HttpStatus.OK);
	}

	
	/**
	 * Converts a Unicode hexadecimal string to a human-readable string message.
	 *
	 * @param msg The Unicode hexadecimal string.
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
	 *
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
	 *
	 * @param entry    The DltRequest containing updated information.
	 * @param username The username of the user performing the operation.
	 * @return A ResponseEntity indicating the success or failure of the operation.
	 */
	
	@Override
	public ResponseEntity<?> updateDltEntry(DltRequest entry, String username) {
		
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
//			BeanUtils.copyProperties(entry, dlt);
			
			dlt.setId(entry.getId());
			dlt.setUsername(entry.getUsername());
			dlt.setSender(entry.getSender());
			dlt.setPeId(entry.getPeId());
			dlt.setTelemarketerId(entry.getTelemarketerId());
			
			
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

	/**
	 * Updates a DltEntry instance in the repository.
	 *
	 * @param entry The DltEntry instance to be updated.
	 * @throws Exception If an error occurs during the update operation.
	 */
	
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
		
		 // Initialize the target status to indicate failure
		String target = IConstants.FAILURE_KEY;

		// Retrieve the user information based on the provided username
		
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
		  // Log information about the update Dlt Template request
		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Update Dlt Template Request: "
				+ entry.getTemplateId());
		try {
			DltTemplEntry dlt = new DltTemplEntry();
			// Perform the update operation on the Dlt Template
			BeanUtils.copyProperties(entry, dlt);
			updateDltTemplate(dlt);
			 // Update the target status to indicate success
			target = IConstants.SUCCESS_KEY;
			logger.error("message.operation.success");
			MultiUtility.changeFlag(Constants.DLT_FLAG_FILE, "707");

		} catch (Exception e) {
			logger.error(userEntry.getSystemId(), e.fillInStackTrace());
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]", false);
		}
		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Update Dlt Template Target: " + target);

		// Return the ResponseEntity with the target status and HTTP status code indicating acceptance
		return new ResponseEntity<>(target, HttpStatus.ACCEPTED);
	}

	
	/**
	 * Update the Dlt Template entry with the provided content, ensuring proper encoding.
	 * 
	 * @param entry The DltTemplEntry containing the template content to be updated
	 * @throws Exception If an error occurs during the update operation
	 */
	
	public void updateDltTemplate(DltTemplEntry entry) throws Exception {
		// Extract the template content from the entry
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
		 // Save the updated Dlt Template entry
		dlttempRepo.save(entry);
	}

	
	@Override
	public ResponseEntity<?> deleteDltEntry(DltRequest entry, String username) {
		
		 // Initialize the target status to indicate failure
		String target = IConstants.FAILURE_KEY;

		 // Retrieve the user information based on the provided username
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			 // Check if the user has the required authorization roles; otherwise, throw an UnauthorizedException
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}



		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Remove DltEntry Request: "
				+ entry.getSender());
		try {
			 // Create a new DltEntry and copy properties from the provided entry
			DltEntry dlt = new DltEntry();
//			BeanUtils.copyProperties(entry, dlt);
			dlt.setId(entry.getId());
			dlt.setUsername(entry.getUsername());
			dlt.setSender(entry.getSender());
			dlt.setPeId(entry.getPeId());
			dlt.setTelemarketerId(entry.getTelemarketerId());
			// Perform the delete operation on the DltEntry
			
			 if (!dltRepo.existsById(entry.getId())) {
		            // Return a ResponseEntity indicating that the provided DltEntry ID is not present
		            return new ResponseEntity<>("User with ID " + entry.getId() + " not found.", HttpStatus.NOT_FOUND);
		        }
			 
			 
			deleteDltEntry(dlt);
			 // Update the target status to indicate success
			target = IConstants.SUCCESS_KEY;
			logger.info("message.operation.success");
			MultiUtility.changeFlag(Constants.DLT_FLAG_FILE, "707");

		} catch (Exception ex) {
			logger.error(userEntry.getSystemId(), ex.fillInStackTrace());
			logger.error("error.processError");
		}
		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Remove DltEntry Target: " + target);
		
		 // Return a ResponseEntity with a success message and HTTP status code indicating OK
		
		return new ResponseEntity<>("Deleted Successfully",HttpStatus.OK);

	}

	/**
	 * Delete the provided DltEntry from the repository.
	 * 
	 * @param entry The DltEntry to be deleted
	 */
	
	public void deleteDltEntry(DltEntry entry) {
		dltRepo.delete(entry);
		
	}

	@Override
	public ResponseEntity<?> deleteDltTemplate(DltTempRequest entry, String username) {
	

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

		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Remove Dlt Template Request: "
				+ entry.getTemplateId());
		try {

			 // Create a new DltTemplEntry and copy properties from the provided entry
			
			DltTemplEntry dlt = new DltTemplEntry();
			BeanUtils.copyProperties(entry, dlt);
			 // Perform the delete operation on the Dlt Template
			
			 if (!dlttempRepo.existsById(entry.getId())) {
		            // Return a ResponseEntity indicating that the provided DltEntry ID is not present
		            return new ResponseEntity<>("Template with ID " + entry.getId() + " not found.", HttpStatus.NOT_FOUND);
		        }
			 
			 
			deleteDltTemplate(dlt);
			 // Update the target status to indicate success
			target = IConstants.SUCCESS_KEY;
			// Log a success message
			logger.info("message.operation.success");
			MultiUtility.changeFlag(Constants.DLT_FLAG_FILE, "707");

		} catch (Exception ex) {
			logger.error(userEntry.getSystemId(), ex.fillInStackTrace());
			logger.error("error.processError");
		}
		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] Remove Dlt Template Target: " + target);
		
		return new ResponseEntity<>("Deleted Successfully",HttpStatus.OK);

	}

	
	/**
	 * Delete the provided DltTemplEntry from the repository.
	 * 
	 * @param entry The DltTemplEntry to be deleted
	 */
	
	public void deleteDltTemplate(DltTemplEntry entry) {
		dlttempRepo.delete(entry);
	}

	
	/**
	 * Retrieve the DltEntry information based on the provided ID.
	 * 
	 * @param id       The ID of the DltEntry to be retrieved
	 * @param username The username associated with the request
	 * @return A DltResponse containing the DltEntry information
	 * @throws NotFoundException If the DltEntry with the provided ID is not found
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

		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] View DltEntry Target:" + target);

		return responseEntry;
		} catch (NotFoundException ex) {
		    // NotFoundException: Rethrow the exception or handle it as needed
		    throw ex;
		} catch (Exception e) {
		    // Handle other exceptions
		    logger.error("An error occurred while processing DltEntry.", e);
		    // Optionally throw a custom exception or return an error response
		    throw new NotFoundException("Entry Not Found");
		}
		
//		if(responseEntry!=null) {
//			return responseEntry;
//		}else {
//			throw new NotFoundException("User not found with the provided ID.");
//		}
		
//		return entry;
	}

	
	/**
	 * Retrieve the DltEntry based on the provided ID.
	 * 
	 * @param id The ID of the DltEntry to be retrieved
	 * @return The DltEntry with the provided ID
	 * @throws NotFoundException If the DltEntry with the provided ID is not found
	 */
	
	public DltEntry getDltEntry(int id) {
		DltEntry entry = dltRepo.findById(id).get();
		// Return the retrieved DltEntry
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
	public DltTempResponse getDltTemplate(int id, String username) {
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

		try {
		DltTemplEntry entry = getDltTemplate(id);
		
		DltTempResponse responseList = new DltTempResponse();
		responseList.setId(entry.getId());
		responseList.setPeId(entry.getPeId());
		responseList.setTemplate(entry.getTemplate());
		responseList.setTemplateId(entry.getTemplateId());

		target = IConstants.SUCCESS_KEY;

		logger.info(userEntry.getSystemId() + "[" + userEntry.getRole() + "] View Dlt Template Target:" + target);

//		return entry;
		 return responseList;
		} catch (NotFoundException ex) {
		    // NotFoundException: Rethrow the exception or handle it as needed
		    throw ex;
		} catch (Exception e) {
		    // Handle other exceptions
		    logger.error("An error occurred while processing Dlt Template.", e);
		    // Optionally throw a custom exception or return an error response
		    throw new NotFoundException("Entry Not Found");
		    
		}
	}

	/**
	 * Retrieve the DltTemplEntry based on the provided ID, and decode the template content.
	 * 
	 * @param id The ID of the DltTemplEntry to be retrieved
	 * @return The DltTemplEntry with the provided ID, including decoded template content
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
	 * 
	 * @param msg The Unicode hexadecimal string to be converted
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

//	@Override
//	public ResponseEntity<?> saveDltTemplate(DltTempRequest entry, String username) {
//		
//		return null;
//	}

}
