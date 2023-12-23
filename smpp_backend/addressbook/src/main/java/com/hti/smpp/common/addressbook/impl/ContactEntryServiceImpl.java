package com.hti.smpp.common.addressbook.impl;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hti.smpp.common.addressbook.request.ContactEntryRequest;
import com.hti.smpp.common.addressbook.response.ContactForBulk;
import com.hti.smpp.common.addressbook.services.ContactEntryService;
import com.hti.smpp.common.addressbook.utils.Converters;
import com.hti.smpp.common.contacts.dto.ContactEntry;
import com.hti.smpp.common.contacts.repository.ContactRepository;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.JsonProcessingError;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.exception.WorkBookException;
import com.hti.smpp.common.templates.dto.TemplatesDTO;
import com.hti.smpp.common.templates.repository.TemplatesRepository;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;

import jakarta.transaction.Transactional;

@Service
public class ContactEntryServiceImpl implements ContactEntryService {

	private static final Logger logger = LoggerFactory.getLogger(ContactEntryServiceImpl.class.getName());

	@Autowired
	private ContactRepository contactRepo;

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private TemplatesRepository tempRepository;


/**
 *  Saves contact entries based on user authorization and input data
 */
	@Override
	public ResponseEntity<?> saveContactEntry(String reqdata, MultipartFile file, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		ContactEntryRequest form;

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			form = objectMapper.readValue(reqdata, ContactEntryRequest.class);
			form.setFile(file);
		} catch (JsonProcessingException e) {
			throw new JsonProcessingError("JsonProccessingError: " + e.getLocalizedMessage());
		} catch (Exception ex) {
			throw new InternalServerException("Error: " + ex.getLocalizedMessage());
		}

		String target = IConstants.FAILURE_KEY;

		logger.info(user.getSystemId() + "[" + user.getRole() + "]" + " Adding Contact To Group: " + form.getGroupId());
		try {
			int groupId = form.getGroupId();
			String mode = form.getType();
			String format = null;
			List<ContactEntry> entry_list = new ArrayList<ContactEntry>();
			// -------------------------------------------
			if (mode.equalsIgnoreCase("multiple")) {
				MultipartFile uploadedFile = form.getFile();
				String file_name = uploadedFile.getName();
				if (file_name.indexOf(".xls") > 0) {
					format = "Excel";
				} else if (file_name.indexOf(".txt") > 0) {
					format = "Text";
				}
				if (format != null) {
					if (format.equalsIgnoreCase("Text")) {
						BufferedReader bufferedReader = null;
						try {
							bufferedReader = new BufferedReader(
									new InputStreamReader(form.getFile().getInputStream(), "UTF-8"));
							int x = 0;
							String entry = null;
							while ((entry = bufferedReader.readLine()) != null) {
								x++;
								logger.info(x + ": " + entry);
								if (entry != null && entry.length() > 0) {
									String[] tokens = entry.split(",");
									String name = null, email = null;
									long number;
									if (tokens.length == 0) {
										logger.warn("Invalid Format For Entry[" + x + "]: " + entry);
										continue;
									} else {
										try {
											tokens[0] = tokens[0].replaceAll("\\s+", ""); // Replace all the spaces in
																							// the String with empty
																							// character.
											number = Long.parseLong(tokens[0]);
										} catch (Exception ex) {
											logger.error("Invalid Number For Entry[" + x + "]: " + entry);
											continue;
										}
										if (tokens.length == 2) {
											if (tokens[1].contains("@")) {
												email = tokens[1];
											} else {
												name = tokens[1];
											}
										} else if (tokens.length == 3) {
											name = tokens[1];
											email = tokens[2];
										}
										if (name != null && name.length() > 0) {
											name = new Converters().UTF16(name);
										}
										if (email != null && email.length() > 40) {
											logger.info(x + " Email need to truncate: " + email.length());
											email = email.substring(0, 40);
										}
										entry_list.add(new ContactEntry(name, number, email, groupId));
									}
								}
							}
						} catch (Exception ex) {
							logger.error("Error: " + ex.getLocalizedMessage());
							throw new InternalServerException("Error Processing Data: " + ex.getLocalizedMessage());
						} finally {
							if (bufferedReader != null) {
								try {
									bufferedReader.close();
								} catch (IOException ioe) {
									bufferedReader = null;
									throw new InternalServerException("IOException: " + ioe.getLocalizedMessage());
								} catch (Exception e) {
									throw new InternalServerException("Exception Message: " + e.getLocalizedMessage());
								}
							}
						}
					} else if (format.equalsIgnoreCase("Excel")) {
						Workbook workbook = null;
						try {
							if (file_name.indexOf(".xlsx") > 0) {
								workbook = new XSSFWorkbook(form.getFile().getInputStream());
							} else {
								workbook = new HSSFWorkbook(form.getFile().getInputStream());
							}
							Sheet firstSheet = workbook.getSheetAt(0);
							logger.info("Sheet[0] Total Rows: " + firstSheet.getPhysicalNumberOfRows());
							// int x = 0;
							int column_count = 0;
							for (Row nextRow : firstSheet) {
								// x++;
								if (nextRow.getRowNum() == 0) {
									column_count = nextRow.getPhysicalNumberOfCells();
									if (column_count == 0) {
										logger.warn("Invalid Format For Xls File");
										break;
									}
								}
								String name = null, email = null;
								long number = 0;
								String cell_value = null;
								for (int i = 0; i < nextRow.getLastCellNum(); i++) {
									Cell cell = nextRow.getCell(i);
									cell_value = new DataFormatter().formatCellValue(cell);
									if (i == 0) {
										try {
											number = Long.parseLong(cell_value);
										} catch (Exception ex) {
											logger.warn("Invalid Number For Entry[" + nextRow.getRowNum() + "]: "
													+ cell_value);
											break;
										}
									}
									if (cell_value != null) {
										if (i == 1) {
											name = cell_value;
										} else if (i == 2) {
											if (cell_value.contains("@")) {
												email = cell_value;
											}
										}
									}
									if (i == 2) {
										break;
									}
								}
								logger.info(nextRow.getRowNum() + ": groupId=" + groupId + ",name=" + name + ",number="
										+ number + ",email=" + email);
								if (number > 0) {
									if (name != null && name.length() > 0) {
										name = new Converters().UTF16(name);
									}
									if (email != null && email.length() > 40) {
										logger.info(nextRow.getRowNum() + " Email need to truncate: " + email.length());
										email = email.substring(0, 40);
									}
									entry_list.add(new ContactEntry(name, number, email, groupId));
								}
							}
						} catch (Exception ex) {
							logger.error("Error: " + ex.getLocalizedMessage());
							throw new WorkBookException("Error Processing Workbook: " + ex.getLocalizedMessage());
						} finally {
							if (workbook != null) {
								try {
									workbook.close();
								} catch (WorkBookException ex) {
									logger.error("Error: " + ex.getLocalizedMessage());
									throw new WorkBookException(
											"Error Processing Workbook: " + ex.getLocalizedMessage());
								} catch (Exception e) {
									logger.error("Error: " + e.getLocalizedMessage());
									throw new InternalServerException(
											"Error Closing Workbook: " + e.getLocalizedMessage());
								}
							}
						}
					}
				}
			} else {
				String name = null, email = null;
				if (form.getNumber() != null && form.getNumber()[0] > 0) {
					if (form.getName() != null && form.getName()[0].length() > 0) {
						name = new Converters().UTF16(form.getName()[0]);
					}
					if (form.getEmail() != null && form.getEmail()[0].length() > 0) {
						if (form.getEmail()[0].contains("@")) {
							email = form.getEmail()[0];
						}
					}
					entry_list.add(new ContactEntry(name, form.getNumber()[0], email, groupId));
				} else {
					logger.error("Invalid Number Found: " + form.getNumber());
					throw new NotFoundException("Number not found");
				}
			}
			if (entry_list.isEmpty()) {
				logger.error("Entry List Empty.Data not found.");
				throw new NotFoundException("Entry List Empty.Data not found.");
			} else {
				List<ContactEntry> contacts = this.contactRepo.saveAll(entry_list);
				target = IConstants.SUCCESS_KEY;
				logger.info("ContactEntry Saved Successfully. Message: " + target);
				return new ResponseEntity<>(target, HttpStatus.CREATED);
			}
		} catch (NotFoundException e) {
			logger.error("Error: " + e.getLocalizedMessage() + ", Message: " + target);
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error("Error: " + e.getLocalizedMessage() + ", Message: " + target);
			throw new InternalServerException(e.getLocalizedMessage());
		}

	}
/**
 *  Retrieves contact information for bulk processing based on user authorization and input data.
 */
	@Override
	public ResponseEntity<?> contactForBulk(List<Long> numbers, int groupId, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		String target = IConstants.FAILURE_KEY;
		String uploadedNumbers = "";
		ContactForBulk response = new ContactForBulk();

		logger.info("Proceed Contact For Bulk Request by " + user.getSystemId());
		try {
			if (numbers != null && numbers.size() > 0) {
				int number_count = 0;
				for (long number : numbers) {
					uploadedNumbers += number + "\n";
					number_count++;
				}
				response.setUploadedNumbers(uploadedNumbers);
				response.setTotalNumbers(number_count);
				response.setGroupId(groupId);
				List<TemplatesDTO> templates = null;
				try {
					templates = this.tempRepository.findByMasterId(user.getSystemId());
				} catch (Exception ex) {
					logger.error("Error: " + ex.getLocalizedMessage());
					throw new NotFoundException("Templates not found: " + ex.getLocalizedMessage());
				}
				if (templates != null) {
					response.setTemplates(templates);
				} else {
					logger.error("No templates exist.");
					throw new InternalServerException("No templates exist.");
				}
				WebMasterEntry webMasterEntry = GlobalVars.WebmasterEntries.get(userOptional.get().getId());
				if (webMasterEntry != null) {
					if (webMasterEntry.getSenderId() != null && webMasterEntry.getSenderId().length() > 1) {
						Set<String> senders = new HashSet<String>(
								Arrays.asList(webMasterEntry.getSenderId().split(",")));
						logger.info(user.getSystemId() + " Configured Senders: " + senders);
						response.setSenders(senders);
					} else {
						logger.error(user.getSystemId() + " No Senders Configured");
						throw new InternalServerException("No Senders Configured");
					}
				} else {
					logger.error(user.getSystemId() + " Webmaster Entry Not Found");
					throw new NotFoundException("Webmaster Entry Not Found");
				}
				target = IConstants.SUCCESS_KEY;
			} else {
				logger.error(user.getSystemId() + " No Record Found For Selected Criteria");
				throw new NotFoundException("No Record Found For Selected Criteria");
			}
			response.setStatus(target);
		} catch (NotFoundException ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]");
			logger.error(user.getSystemId(), ex.getLocalizedMessage());
			throw new NotFoundException("Exception: " + ex.getLocalizedMessage());
		} catch (InternalServerException ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]");
			logger.error(user.getSystemId(), ex.getLocalizedMessage());
			throw new InternalServerException("Process Error: " + ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]");
			logger.error(user.getSystemId(), ex.getLocalizedMessage());
			throw new InternalServerException("Process Error: " + ex.getLocalizedMessage());
		}

		return ResponseEntity.ok(response);
	}
/**
 * Retrieves contact entries based on group IDs for viewing in bulk, with user authorization.
 */
	@Override
	public ResponseEntity<List<ContactEntry>> viewSearchContact(List<Integer> ids, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		String systemId = user.getSystemId();
	
		logger.info("List Contact For Bulk Request by " + systemId);
		List<ContactEntry> list = new ArrayList<ContactEntry>();
		try {
			if (ids != null && ids.size() > 0) {
				for (int groupId : ids) {
					List<ContactEntry> part_list = this.contactRepo.findByGroupId(groupId);
					if (!part_list.isEmpty()) {
						for (ContactEntry entry : part_list) {
							if (entry.getName() != null && entry.getName().length() > 0) {
								entry.setName(new Converters().uniHexToCharMsg(entry.getName()));
							}
							list.add(entry);
						}
					}
				}
			}
			if (list.isEmpty()) {
				throw new NotFoundException("No Contact Found");
			}

		} catch (InternalServerException ex) {
			logger.error(systemId, ex.toString());
			throw new InternalServerException(ex.getLocalizedMessage());
		} catch (NotFoundException ex) {
			logger.error(systemId, ex.toString());
			throw new NotFoundException(ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error(systemId, ex.toString());
			throw new InternalServerException(ex.getLocalizedMessage());
		}

		return ResponseEntity.ok(list);
	}
/**
 * Proceeds with the search for contact entries based on group IDs, providing bulk processing details.
 */
	@Override
	public ResponseEntity<ContactForBulk> proceedSearchContact(List<Integer> ids, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		String systemId = user.getSystemId();
		
		logger.info("Proceed Contact For Bulk Request by " + systemId);
		String target = IConstants.FAILURE_KEY;
		String uploadedNumbers = "";
		ContactForBulk response = new ContactForBulk();

		try {
			List<ContactEntry> list = new ArrayList<ContactEntry>();
			if (ids != null && ids.size() > 0) {
				for (int groupId : ids) {
					List<ContactEntry> part_list = null;
					try {
						part_list = this.contactRepo.findByGroupId(groupId);
					} catch (Exception e) {
						logger.error("Error: " + e.getLocalizedMessage());
						throw new InternalServerException(e.getLocalizedMessage());
					}
					if (!part_list.isEmpty()) {
						list.addAll(part_list);
					} else {
						logger.error("part_list is empty. unable to process.");
						throw new NotFoundException("List Not Found. Empty List Exception.");
					}
				}
			}
			if (!list.isEmpty()) {
				for (ContactEntry entry : list) {
					uploadedNumbers += entry.getNumber() + "\n";
				}
				response.setUploadedNumbers(uploadedNumbers);
				response.setTotalNumbers(list.size());
				List<TemplatesDTO> templates = null;
				try {
					templates = this.tempRepository.findByMasterId(systemId);
				} catch (Exception ex) {
					logger.error("Error: " + ex.getLocalizedMessage());
					throw new NotFoundException("Templates not found.");
				}
				if (templates != null) {
					response.setTemplates(templates);
				} else {
					logger.error("NO template Exist");
					throw new NotFoundException("NO template found");
				}
				WebMasterEntry webMasterEntry = GlobalVars.WebmasterEntries.get(userOptional.get().getId());
				if (webMasterEntry != null) {
					if (webMasterEntry.getSenderId() != null && webMasterEntry.getSenderId().length() > 1) {
						Set<String> senders = new HashSet<String>(
								Arrays.asList(webMasterEntry.getSenderId().split(",")));
						logger.info(systemId + " Configured Senders: " + senders);
						response.setSenders(senders);
					} else {
						logger.error(systemId + " No Senders Configured");
						throw new InternalServerException("No Senders Configured");
					}
				} else {
					logger.error(systemId + " Webmaster Entry Not Found");
					throw new NotFoundException("Webmaster Entry Not Found");
				}
				target = "proceed";
			} else {
				logger.error(systemId + " No Record Found For Selected Criteria");
				throw new NotFoundException("No Record Found");
			}
			response.setStatus(target);
		} catch (InternalServerException ex) {
			logger.error(systemId, ex.toString());
			throw new InternalServerException("Process Error: " + ex.getLocalizedMessage());
		} catch (NotFoundException ex) {
			logger.error(systemId, ex.toString());
			throw new NotFoundException("Error: " + ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error(systemId, ex.toString());
			throw new InternalServerException("Process Error: " + ex.getLocalizedMessage());
		}
		return ResponseEntity.ok(response);
	}
/**
 * Modifies and updates contact entries based on the provided ContactEntryRequest form.
 */
	@Override
	@Transactional
	public ResponseEntity<?> modifyContactUpdate(ContactEntryRequest form, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		String target = IConstants.FAILURE_KEY;
		String systemId = user.getSystemId();
		int groupId = form.getGroupId();
		logger.info(systemId + "[" + user.getRole() + "]" + " Modify Contact Request For GroupId: " + groupId);
		ContactEntry entry = null;
		List<ContactEntry> list = new ArrayList<ContactEntry>();
		int[] id = form.getId();
		String[] names = form.getName();
		String[] emails = form.getEmail();
		long[] numbers = form.getNumber();

		if (id != null && id.length > 0) {
			try {
				for (int i = 0; i < id.length; i++) {
					if (names[i] != null && names[i].length() > 0) {
						entry = new ContactEntry(new Converters().UTF16(names[i]), numbers[i], emails[i], groupId);
					} else {
						entry = new ContactEntry(null, numbers[i], emails[i], groupId);
					}
					entry.setId(id[i]);
					list.add(entry);
					logger.info(entry.toString());
				}
				if (!list.isEmpty()) {
					this.contactRepo.saveAll(list);
					target = IConstants.SUCCESS_KEY;
				} else {
					logger.error("Error: List is Empty. Error while saving data.");
					throw new InternalServerException("Error saving ContactEntry List");
				}
			} catch (InternalServerException e) {
				logger.error(systemId, e.getLocalizedMessage());
				throw new InternalServerException("target: " + target + " Message:" + e.getLocalizedMessage());
			} catch (Exception e) {
				logger.error(systemId, e.getLocalizedMessage());
				throw new InternalServerException("target: " + target + " Message:" + e.getLocalizedMessage());
			}
		} else {
			logger.error(systemId + " No Records Selected");
			throw new NotFoundException(systemId + " No Records Selected");
		}
		logger.info(systemId + " modify Contact Target:" + target);
		return new ResponseEntity<>(target, HttpStatus.CREATED);
	}

	private void logDeletedContacts(String username, List<Integer> deletedContactsIds) {
		if (!deletedContactsIds.isEmpty()) {
			logger.info("Deleted contacts by {}: {}", username, deletedContactsIds);
		}
	}

	private void logFailedDeletions(String username, List<Integer> failedDeletionIds) {
		if (!failedDeletionIds.isEmpty()) {
			logger.warn("Failed to delete contacts by {}: {}", username, failedDeletionIds);
		}
	}
/**
 * Modifies and updates contact entries based on the provided ContactEntryRequest form.
 *
 */
	@Override
	@Transactional
	public ResponseEntity<?> modifyContactDelete(List<Integer> ids, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		String target = IConstants.FAILURE_KEY;
		List<Integer> successfulDeletions = new ArrayList<>();
		List<Integer> failedDeletionIds = new ArrayList<>();

		try {
			if (!ids.isEmpty()) {
				List<ContactEntry> contactsToDelete = null;
				try {
					contactsToDelete = contactRepo.findAllById(ids);
				} catch (InternalServerException e1) {
					logger.error(e1.getLocalizedMessage());
					throw new InternalServerException(e1.getLocalizedMessage());
				} catch (Exception e1) {
					logger.error(e1.getLocalizedMessage());
					throw new InternalServerException(e1.getLocalizedMessage());
				}
				for (ContactEntry contact : contactsToDelete) {
					try {
						contactRepo.delete(contact);
						successfulDeletions.add(contact.getId());
					} catch (InternalServerException e) {
						logger.error("Error deleting contact with ID {}: {}", contact.getId(), e.getMessage(), e);
						failedDeletionIds.add(contact.getId());
						throw new InternalServerException("Error deleting contact id: " + contact.getId());
					} catch (Exception e) {
						logger.error("Error deleting contact with ID {}: {}", contact.getId(), e.getMessage(), e);
						failedDeletionIds.add(contact.getId());
						throw new InternalServerException("Error deleting contact id: " + contact.getId());
					}
				}

				target = IConstants.SUCCESS_KEY;
				logDeletedContacts(username, successfulDeletions);
				logFailedDeletions(username, failedDeletionIds);
			} else {
				throw new InternalServerException("Error Processing Deletion.");
			}
			return ResponseEntity.status(HttpStatus.OK).body(target);
		} catch (InternalServerException e) {
			logger.error("Error deleting contacts: {}", e.getMessage(), e);
			target = IConstants.FAILURE_KEY;
			throw new InternalServerException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error("Error deleting contacts: {}", e.getMessage(), e);
			target = IConstants.FAILURE_KEY;
			throw new InternalServerException(e.getLocalizedMessage());
		}
	}
/**
 * Generates a workbook containing contact entries with specified headers and styles.
 * @param list
 * @return
 */
	private Workbook getWorkBook(List<ContactEntry> list) {
		logger.info("Start Creating WorkBook.");
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		int records_per_sheet = 500000;
		int sheet_number = 0;
		Sheet sheet = null;
		Row row = null;
		XSSFFont headerFont = (XSSFFont) workbook.createFont();
		headerFont.setFontName("Arial");
		headerFont.setFontHeightInPoints((short) 10);
		headerFont.setColor(new XSSFColor(Color.WHITE));
		XSSFCellStyle headerStyle = (XSSFCellStyle) workbook.createCellStyle();
		headerStyle.setFont(headerFont);
		headerStyle.setFillForegroundColor(new XSSFColor(Color.GRAY));
		headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		headerStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
		headerStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderBottom((short) 1);
		headerStyle.setBottomBorderColor(new XSSFColor(Color.WHITE));
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderTop((short) 1);
		headerStyle.setTopBorderColor(new XSSFColor(Color.WHITE));
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderLeft((short) 1);
		headerStyle.setLeftBorderColor(new XSSFColor(Color.WHITE));
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setBorderRight((short) 1);
		headerStyle.setRightBorderColor(new XSSFColor(Color.WHITE));
		XSSFFont rowFont = (XSSFFont) workbook.createFont();
		rowFont.setFontName("Arial");
		rowFont.setFontHeightInPoints((short) 9);
		rowFont.setColor(new XSSFColor(Color.BLACK));
		XSSFCellStyle rowStyle = (XSSFCellStyle) workbook.createCellStyle();
		rowStyle.setFont(rowFont);
		rowStyle.setFillForegroundColor(new XSSFColor(Color.LIGHT_GRAY));
		rowStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		rowStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
		rowStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
		rowStyle.setBorderBottom(BorderStyle.THIN);
		rowStyle.setBorderBottom((short) 1);
		rowStyle.setBottomBorderColor(new XSSFColor(Color.WHITE));
		rowStyle.setBorderTop(BorderStyle.THIN);
		rowStyle.setBorderTop((short) 1);
		rowStyle.setTopBorderColor(new XSSFColor(Color.WHITE));
		rowStyle.setBorderLeft(BorderStyle.THIN);
		rowStyle.setBorderLeft((short) 1);
		rowStyle.setLeftBorderColor(new XSSFColor(Color.WHITE));
		rowStyle.setBorderRight(BorderStyle.THIN);
		rowStyle.setBorderRight((short) 1);
		rowStyle.setRightBorderColor(new XSSFColor(Color.WHITE));
		String[] headers = { "Name", "Number", "Email" };
		while (!list.isEmpty()) {
			int row_number = 0;
			sheet = workbook.createSheet("Sheet(" + sheet_number + ")");
			sheet.setDefaultColumnWidth(20);
			logger.info("Creating Sheet: " + sheet_number);
			while (!list.isEmpty()) {
				row = sheet.createRow(row_number);
				if (row_number == 0) {
					int cell_number = 0;
					for (String header : headers) {
						Cell cell = row.createCell(cell_number);
						cell.setCellValue(header);
						cell.setCellStyle(headerStyle);
						cell_number++;
					}
				} else {
					ContactEntry entry = list.remove(0);
					logger.debug("Add Row[" + row_number + "]: " + entry);
					Cell cell = row.createCell(0);
					cell.setCellValue(entry.getName());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(1);
					cell.setCellValue(String.valueOf(entry.getNumber()));
					cell.setCellStyle(rowStyle);
					cell = row.createCell(2);
					cell.setCellValue(entry.getEmail());
					cell.setCellStyle(rowStyle);
				}
				if (++row_number > records_per_sheet) {
					logger.info("Contact Sheet Created: " + sheet_number);
					break;
				}
			}
			sheet_number++;
		}
		logger.info("Contact Workbook Created");
		return workbook;
	}
/**
 * Exports contact entries to an Excel workbook and provides it as a downloadable file.
 */
	@Override
	public ResponseEntity<?> modifyContactExport(ContactEntryRequest form, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		String target = IConstants.FAILURE_KEY;
		ContactEntry entry = null;
		String systemId = user.getSystemId();

		int groupId = form.getGroupId();
		logger.info(systemId + "[" + user.getRole() + "]" + " Export Contact Request For GroupId: " + groupId);
		List<ContactEntry> list = new ArrayList<ContactEntry>();
		int[] id = form.getId();
		String[] names = form.getName();
		String[] emails = form.getEmail();
		long[] numbers = form.getNumber();
		Converters cc = new Converters();

		if (id != null && id.length > 0) {
			try {
				for (int i = 0; i < id.length; i++) {
					if (names[i] != null && names[i].length() > 0) {
						String hex = new Converters().UTF16(names[i]);
						entry = new ContactEntry(cc.uniHexToCharMsg(hex), numbers[i], emails[i], groupId);
					} else {
						entry = new ContactEntry(null, numbers[i], emails[i], groupId);
					}
					list.add(entry);
					logger.debug(entry.toString());
				}
				Workbook workbook = null;
				try {
					workbook = getWorkBook(list);
				} catch (Exception e1) {
					logger.error("WorkBook Exception: " + e1.toString());
					throw new WorkBookException("WorkBook Error: " + e1.getLocalizedMessage());
				}
				String filename = systemId + "_Contact[" + groupId + "]" + ".xlsx";
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				logger.info(systemId + " Creating Contact XLSx ");
				workbook.write(bos);

				try (InputStream in = new ByteArrayInputStream(bos.toByteArray())) {
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
					headers.setContentDispositionFormData("attachment", filename);
					InputStreamResource resource = new InputStreamResource(in);
					target = "export";
					logger.info(systemId + " Export Contact Target:" + target);
					return new ResponseEntity<>(resource, headers, HttpStatus.OK);
				} catch (IOException e) {
					logger.error("Contact XLSx Download Error: {}", e.toString());
					throw new InternalServerException("IOException: " + e.getLocalizedMessage());
				} catch (Exception e) {
					logger.error("Unexpected Exception: " + e.getLocalizedMessage());
					throw new InternalServerException("Unexpected Exception: " + e.getLocalizedMessage());
				}

			} catch (Exception e) {
				logger.error(systemId, e.toString());
				throw new InternalServerException(e.getLocalizedMessage());
			}

		} else {
			logger.warn(systemId + " No Records Selected");
			throw new NotFoundException("No Records Found.");
		}

	}

}
