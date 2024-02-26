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
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;

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

	@Autowired
	private WebMasterEntryRepository webMasterRepo;

	@Autowired
	private MessageResourceBundle messageResourceBundle;

	/**
	 * Saves contact entries based on user authorization and input data
	 */
	@Override
	public ResponseEntity<?> saveContactEntry(String reqdata, MultipartFile file, String username) {
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

		ContactEntryRequest form;

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			form = objectMapper.readValue(reqdata, ContactEntryRequest.class);
			form.setFile(file);
		} catch (JsonProcessingException e) {
			throw new JsonProcessingError(messageResourceBundle.getExMessage(ConstantMessages.JSON_PROCESSING_ERROR,
					new Object[] { e.getMessage() }));
		} catch (Exception ex) {
			throw new InternalServerException(ex.getLocalizedMessage());
		}

		String target = IConstants.FAILURE_KEY;
		logger.info(messageResourceBundle.getLogMessage("addbook.contact.addreq"), user.getSystemId(), user.getRole(),
				form.getGroupId());
		try {
			int groupId = form.getGroupId();
			String mode = form.getType();
			String format = null;
			List<ContactEntry> entry_list = new ArrayList<ContactEntry>();
			// -------------------------------------------
			if (mode.equalsIgnoreCase("multiple")) {
				MultipartFile uploadedFile = form.getFile();
				if (uploadedFile == null) {
					throw new InternalServerException(
							messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_UPLOAD_FAILED));
				}
				String file_name = uploadedFile.getOriginalFilename();
				if (file_name.indexOf(".xls") > 0) {
					format = "Excel";
				} else if (file_name.indexOf(".txt") > 0) {
					format = "Text";
				}
				System.out.println("format: " + format);
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
										logger.warn(messageResourceBundle.getLogMessage("addbook.invalid.entryformat"),
												x, entry);
										continue;
									} else {
										try {
											tokens[0] = tokens[0].replaceAll("\\s+", ""); // Replace all the spaces in
																							// the String with empty
																							// character.
											number = Long.parseLong(tokens[0]);
										} catch (Exception ex) {
											logger.warn(
													messageResourceBundle.getLogMessage("addbook.invalid.entryformat"),
													x, entry);
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
											logger.info(
													messageResourceBundle.getLogMessage("addbook.email.truncate.info"),
													x, email.length());
											email = email.substring(0, 40);
										}
										entry_list.add(new ContactEntry(name, number, email, groupId));
									}
								}
							}
						} catch (Exception ex) {
							logger.error(messageResourceBundle.getLogMessage("addbook.error.message"),
									ex.getLocalizedMessage());
							throw new InternalServerException(messageResourceBundle.getExMessage(
									ConstantMessages.ADDBOOK_ERROR_MSG, new Object[] { ex.getMessage() }));
						} finally {
							if (bufferedReader != null) {
								try {
									bufferedReader.close();
								} catch (IOException ioe) {
									bufferedReader = null;
									throw new InternalServerException("IOException: " + ioe.getLocalizedMessage());
								} catch (Exception e) {
									throw new InternalServerException(messageResourceBundle.getExMessage(
											ConstantMessages.ADDBOOK_ERROR_MSG, new Object[] { e.getMessage() }));
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
							logger.info(messageResourceBundle.getLogMessage("addbooksheet.total.rows.info"),
									firstSheet.getPhysicalNumberOfRows());
							// int x = 0;
							int column_count = 0;
							for (Row nextRow : firstSheet) {
								// x++;
								if (nextRow.getRowNum() == 0) {
									column_count = nextRow.getPhysicalNumberOfCells();
									if (column_count == 0) {
										logger.warn(
												messageResourceBundle.getLogMessage("addbook.invalid.xls.format.warn"));
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
											logger.warn(
													messageResourceBundle
															.getLogMessage("addbook.invalid.number.entry.warn"),
													nextRow.getRowNum(), cell_value);
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
										logger.info(messageResourceBundle.getLogMessage("addbook.truncate.email.info"),
												nextRow.getRowNum(), email.length());
										email = email.substring(0, 40);
									}
									entry_list.add(new ContactEntry(name, number, email, groupId));
								}
							}
						} catch (Exception ex) {
							logger.error("Error: " + ex.getLocalizedMessage());
							throw new WorkBookException(
									messageResourceBundle.getExMessage(ConstantMessages.WORKBOOK_PROCESSING_ERROR,
											new Object[] { ex.getLocalizedMessage() }));
						} finally {
							if (workbook != null) {
								try {
									workbook.close();
								} catch (IOException ex) {
									logger.error("Error: " + ex.getLocalizedMessage());
									throw new WorkBookException(ex.getLocalizedMessage());
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
					logger.error(messageResourceBundle.getLogMessage("addbook.invalid.number.error"), form.getNumber());
					throw new NotFoundException(
							messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_NUMBER_NOT_FOUND_ERROR));
				}
			}
			if (entry_list.isEmpty()) {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_EMPTY_DATASET));
			} else {
				List<ContactEntry> contacts = this.contactRepo.saveAll(entry_list);
				target = IConstants.SUCCESS_KEY;
				logger.info(messageResourceBundle.getLogMessage("addbook.contactentry.saved.info"), target);
				return new ResponseEntity<>(messageResourceBundle.getMessage(ConstantMessages.ADDBOOK_CONTACT_SAVED),
						HttpStatus.CREATED);
			}
		} catch (NotFoundException e) {
			logger.error("Error: " + e.getLocalizedMessage() + ", Message: " + target);
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error("Error: " + e.getLocalizedMessage() + ", Message: " + target);
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_ERROR_MSG,
					new Object[] { e.getMessage() }));
		}

	}

	/**
	 * Retrieves contact information for bulk processing based on user authorization
	 * and input data.
	 */
	@Override
	public ResponseEntity<?> contactForBulk(List<Long> numbers, int groupId, String username) {

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
		List<Long> uploadedNumbers = new ArrayList<Long>();
		ContactForBulk response = new ContactForBulk();

		logger.info(messageResourceBundle.getLogMessage("addbook.proceed.contact.bulk.info"), user.getSystemId());

		try {
			if (numbers != null && numbers.size() > 0) {
				int number_count = 0;
				for (long number : numbers) {
					uploadedNumbers.add(number);
					number_count++;
				}
				response.setUploadedNumbers(uploadedNumbers);
				response.setTotalNumbers(number_count);
				response.setGroupId(groupId);
				List<TemplatesDTO> templates = new ArrayList<TemplatesDTO>();
				try {
					templates = this.tempRepository.findByMasterId(user.getMasterId());
					if (!templates.isEmpty()) {
						templates.forEach(t -> {
							if (t.getMessage() != null && t.getMessage().length() > 0) {
								t.setMessage(Converters.hexCodePointsToCharMsg(t.getMessage()));
							}
							if (t.getTitle() != null && t.getTitle().length() > 0) {
								t.setTitle(Converters.hexCodePointsToCharMsg(t.getTitle()));
							}
						});
					} else {
						logger.info(messageResourceBundle.getLogMessage("addbook.no.template.exist.info"));
					}
				} catch (Exception ex) {
					logger.error(ex.getLocalizedMessage());
					throw new NotFoundException(messageResourceBundle.getExMessage(
							ConstantMessages.NOT_FOUND_TEMPLATES_ERROR, new Object[] { ex.getLocalizedMessage() }));
				}

				response.setTemplates(templates);

				WebMasterEntry webMasterEntry = this.webMasterRepo.findByUserId(user.getId());
				if (webMasterEntry != null) {
					if (webMasterEntry.getSenderId() != null && webMasterEntry.getSenderId().length() > 1) {
						Set<String> senders = new HashSet<String>(
								Arrays.asList(webMasterEntry.getSenderId().split(",")));
						logger.info(messageResourceBundle.getLogMessage("addbook.configured.senders.info"),
								user.getSystemId(), senders);
						response.setSenders(senders);
					} else {
						logger.error(messageResourceBundle.getLogMessage("addbook.no.senders.configured.error"));
//						throw new InternalServerException("No Senders Configured");
					}
				} else {
					logger.error(messageResourceBundle.getLogMessage("addbook.webmaster.entry.notfound.error"),
							user.getSystemId());
					throw new NotFoundException(
							messageResourceBundle.getExMessage(ConstantMessages.NOT_FOUND_WEBMASTER_ERROR));
				}
				target = IConstants.SUCCESS_KEY;
			} else {
				logger.error(messageResourceBundle.getLogMessage("addbook.no.record.found.error"));
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_NORECORD));
			}
			response.setStatus(target);
		} catch (NotFoundException ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]");
			throw new NotFoundException(ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]");
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_ERROR_MSG,
					new Object[] { ex.getMessage() }));
		}

		return ResponseEntity.ok(response);
	}

	/**
	 * Retrieves contact entries based on group IDs for viewing in bulk, with user
	 * authorization.
	 */
	@Override
	public ResponseEntity<List<ContactEntry>> viewSearchContact(List<Integer> ids, String username) {

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

		String systemId = user.getSystemId();

		logger.info(messageResourceBundle.getLogMessage("addbook.list.contact.bulk.info"), systemId);

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
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_NO_CONTACT));
			}

		} catch (NotFoundException ex) {
			logger.error(systemId, ex.toString());
			throw new NotFoundException(ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error(systemId, ex.toString());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_ERROR_MSG,
					new Object[] { ex.getMessage() }));
		}

		return ResponseEntity.ok(list);
	}

	/**
	 * Proceeds with the search for contact entries based on group IDs, providing
	 * bulk processing details.
	 */
	@Override
	public ResponseEntity<ContactForBulk> proceedSearchContact(List<Integer> ids, String username) {

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

		String systemId = user.getSystemId();
		logger.info(messageResourceBundle.getLogMessage("addbook.proceed.contact.bulk.info"), systemId);
		String target = IConstants.FAILURE_KEY;
		List<Long> uploadedNumbers = new ArrayList<Long>();
		ContactForBulk response = new ContactForBulk();

		try {
			List<ContactEntry> list = new ArrayList<ContactEntry>();
			if (ids != null && ids.size() > 0) {
				for (int groupId : ids) {
					List<ContactEntry> part_list = null;

					part_list = this.contactRepo.findByGroupId(groupId);

					if (!part_list.isEmpty()) {
						list.addAll(part_list);
					}
				}
			}
			if (!list.isEmpty()) {
				for (ContactEntry entry : list) {
					uploadedNumbers.add(entry.getNumber());
				}
				response.setUploadedNumbers(uploadedNumbers);
				response.setTotalNumbers(list.size());
				List<TemplatesDTO> templates = new ArrayList<TemplatesDTO>();

				try {
					templates = this.tempRepository.findByMasterId(user.getMasterId());
					if (!templates.isEmpty()) {
						templates.forEach(t -> {
							if (t.getMessage() != null && t.getMessage().length() > 0) {
								t.setMessage(Converters.hexCodePointsToCharMsg(t.getMessage()));
							}
							if (t.getTitle() != null && t.getTitle().length() > 0) {
								t.setTitle(Converters.hexCodePointsToCharMsg(t.getTitle()));
							}
						});
					} else {
						logger.warn(messageResourceBundle.getLogMessage("addbook.no.template.exist.info"));
					}
				} catch (Exception ex) {
					logger.error(messageResourceBundle.getLogMessage("addbook.error.message"), ex.getMessage());
					throw new NotFoundException(
							messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_TEMPLATE_UNABLEFIND));
				}

				response.setTemplates(templates);

				WebMasterEntry webMasterEntry = this.webMasterRepo.findByUserId(user.getId());
				if (webMasterEntry != null) {
					if (webMasterEntry.getSenderId() != null && webMasterEntry.getSenderId().length() > 1) {
						Set<String> senders = new HashSet<String>(
								Arrays.asList(webMasterEntry.getSenderId().split(",")));
						logger.info(messageResourceBundle.getLogMessage("addbook.configured.senders.info"), systemId,
								senders);
						response.setSenders(senders);
					} else {
						logger.error(messageResourceBundle.getLogMessage("addbook.no.senders.configured.error"));
//						throw new InternalServerException("No Senders Configured");
					}
				} else {
					logger.error(messageResourceBundle.getLogMessage("addbook.webmaster.entry.notfound.error"),
							user.getSystemId());
					throw new NotFoundException(
							messageResourceBundle.getExMessage(ConstantMessages.NOT_FOUND_WEBMASTER_ERROR));
				}
				target = "proceed";
			} else {
				logger.error(messageResourceBundle.getLogMessage("addbook.no.record.found.error"));
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_NORECORD));
			}
			response.setStatus(target);
		} catch (NotFoundException ex) {
			logger.error(systemId, ex.toString());
			throw new NotFoundException(ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error(systemId, ex.toString());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_ERROR_MSG,
					new Object[] { ex.getMessage() }));
		}
		return ResponseEntity.ok(response);
	}

	/**
	 * Modifies and updates contact entries based on the provided
	 * ContactEntryRequest form.
	 */
	@Override
	@Transactional
	public ResponseEntity<?> modifyContactUpdate(ContactEntryRequest form, String username) {

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
		int groupId = form.getGroupId();
		logger.info(messageResourceBundle.getLogMessage("addbook.modify.contact.request.info"), systemId,
				user.getRole(), groupId);
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
				}
				if (!list.isEmpty()) {
					this.contactRepo.saveAll(list);
					target = IConstants.SUCCESS_KEY;
				} else {
					logger.error(messageResourceBundle.getLogMessage("addbook.update.list.empty.error"));
					throw new InternalServerException(
							messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_CONTACT_UPDATE_ERROR));
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				logger.error(systemId, e.getLocalizedMessage());
				throw new InternalServerException(
						messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_INCOMPLETE_DATA));
			} catch (Exception e) {
				logger.error(systemId, e.getLocalizedMessage());
				throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_ERROR_MSG,
						new Object[] { e.getMessage() }));
			}
		} else {
			logger.error(messageResourceBundle.getLogMessage("addbook.no.record.found.error"));
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_NORECORD));
		}
		logger.info(messageResourceBundle.getLogMessage("addbook.modify.contact.status.info"), systemId, target);
		return new ResponseEntity<>(messageResourceBundle.getMessage(ConstantMessages.ADDBOOK_CONTACT_UPDATED),
				HttpStatus.CREATED);
	}

	private void logDeletedContacts(String username, List<Integer> deletedContactsIds) {
		if (!deletedContactsIds.isEmpty()) {
			logger.info(messageResourceBundle.getLogMessage("addbook.contact.deleted.info"), username,
					deletedContactsIds);
		}
	}

	private void logFailedDeletions(String username, List<Integer> failedDeletionIds) {
		if (!failedDeletionIds.isEmpty()) {
			logger.warn(messageResourceBundle.getLogMessage("addbook.contact.failed.deletion.warn"), username,
					failedDeletionIds);
		}
	}

	/**
	 * Modifies and updates contact entries based on the provided
	 * ContactEntryRequest form.
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
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}

		String target = IConstants.FAILURE_KEY;
		List<Integer> successfulDeletions = new ArrayList<>();
		List<Integer> failedDeletionIds = new ArrayList<>();

		try {
			if (!ids.isEmpty()) {
				List<ContactEntry> contactsToDelete = null;
				contactsToDelete = this.contactRepo.findAllById(ids);
				if (contactsToDelete.isEmpty()) {
					throw new NotFoundException(
							messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_DELETECONTACT_NOTFOUND));
				}

				for (ContactEntry contact : contactsToDelete) {
					try {
						contactRepo.delete(contact);
						successfulDeletions.add(contact.getId());
					} catch (Exception e) {
						logger.error("Error deleting contact with ID {}: {}", contact.getId(), e.getMessage());
						failedDeletionIds.add(contact.getId());
						throw new InternalServerException(messageResourceBundle.getExMessage(
								ConstantMessages.ADDBOOK_ERROR_DELETE_CONTACT, new Object[] { contact.getId() }));
					}
				}

				target = IConstants.SUCCESS_KEY;
				logDeletedContacts(username, successfulDeletions);
				logFailedDeletions(username, failedDeletionIds);
			} else {
				throw new InternalServerException(messageResourceBundle.getExMessage(
						ConstantMessages.ADDBOOK_ERROR_DELETE_CONTACT, new Object[] { failedDeletionIds }));
			}
			return ResponseEntity.status(HttpStatus.OK).body(messageResourceBundle
					.getMessage(ConstantMessages.ADDBOOK_CONTACT_DELETED, new Object[] { successfulDeletions }));
		} catch (NotFoundException e1) {
			logger.error("Error Message: " + e1.getLocalizedMessage());
			throw new NotFoundException(e1.getLocalizedMessage());
		} catch (Exception e) {
			logger.error("Error deleting contacts: {}", e.getMessage());
			target = IConstants.FAILURE_KEY;
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_ERROR_MSG,
					new Object[] { e.getMessage() }));
		}
	}

	/**
	 * Generates a workbook containing contact entries with specified headers and
	 * styles.
	 * 
	 * @param list
	 * @return
	 */
	private Workbook getWorkBook(List<ContactEntry> list) {
		logger.info(messageResourceBundle.getLogMessage("addbook.start.creating.workbook.info"));
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
		logger.info(messageResourceBundle.getLogMessage("addbook.contact.workbook.created.info"));
		return workbook;
	}

	/**
	 * Exports contact entries to an Excel workbook and provides it as a
	 * downloadable file.
	 */
	@Override
	public ResponseEntity<?> modifyContactExport(ContactEntryRequest form, String username) {

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
		ContactEntry entry = null;
		String systemId = user.getSystemId();

		int groupId = form.getGroupId();
		logger.info(messageResourceBundle.getLogMessage("addbook.export.contact.request.info"), systemId,
				user.getRole(), groupId);
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
					logger.error(messageResourceBundle.getLogMessage("addbook.error.message"),
							e1.getLocalizedMessage());
					throw new WorkBookException(messageResourceBundle.getExMessage(
							ConstantMessages.WORKBOOK_PROCESSING_ERROR, new Object[] { e1.getMessage() }));
				}
				String filename = systemId + "_Contact[" + groupId + "]" + ".xlsx";
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				logger.info(messageResourceBundle.getLogMessage("addbook.creating.contact.xlsx.info"), systemId);
				workbook.write(bos);

				try (InputStream in = new ByteArrayInputStream(bos.toByteArray())) {
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
					headers.setContentDispositionFormData("attachment", filename);
					InputStreamResource resource = new InputStreamResource(in);
					target = "export";
					logger.info(messageResourceBundle.getLogMessage("addbook.export.contact.target.info"), systemId,
							target);
					return new ResponseEntity<>(resource, headers, HttpStatus.OK);
				} catch (IOException e) {
					logger.error(messageResourceBundle.getLogMessage("addbook.contact.xlsx.download.error"),
							e.getMessage());
					throw new InternalServerException(messageResourceBundle
							.getExMessage(ConstantMessages.ADDBOOK_ERROR_MSG, new Object[] { e.getMessage() }));
				} catch (Exception e) {
					logger.error(messageResourceBundle.getLogMessage("addbook.error.message"), e.getLocalizedMessage());
					throw new InternalServerException(messageResourceBundle
							.getExMessage(ConstantMessages.ADDBOOK_ERROR_MSG, new Object[] { e.getMessage() }));
				} finally {
					try {
						if (workbook != null) {
							workbook.close();
						}
					} catch (IOException e) {
						throw new WorkBookException(e.getLocalizedMessage());
					} catch (Exception e) {
						throw new InternalServerException(e.getLocalizedMessage());
					}
				}

			} catch (Exception e) {
				logger.error(systemId, e.toString());
				throw new InternalServerException(e.getLocalizedMessage());
			}

		} else {
			logger.warn(messageResourceBundle.getLogMessage("addbook.no.record.found.error"));
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_NORECORD));
		}

	}

	@Override
	public List<ContactEntry> getContactByGroupId(int groupId, String username) {
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
		try {
			List<ContactEntry> response = this.contactRepo.findByGroupId(groupId);
			response.forEach(entry -> {
				if (entry.getName() != null && entry.getName().length() > 0) {
					entry.setName(new Converters().uniHexToCharMsg(entry.getName()));
				}
			});
			if (response.isEmpty()) {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_NO_CONTACT));
			} else {
				return response;
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.ADDBOOK_ERROR_MSG,
					new Object[] { e.getMessage() }));
		}

	}

}
