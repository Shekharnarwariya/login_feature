package com.hti.smpp.common.addressbook.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hti.smpp.common.addressbook.request.ContactEntryRequest;
import com.hti.smpp.common.addressbook.request.GroupEntryRequest;
import com.hti.smpp.common.addressbook.response.ContactForBulk;
import com.hti.smpp.common.addressbook.services.ContactEntryService;
import com.hti.smpp.common.addressbook.utils.Converters;
import com.hti.smpp.common.contacts.dto.ContactEntry;
import com.hti.smpp.common.contacts.repository.ContactRepository;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.login.dto.Role;
import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.UserRepository;
import com.hti.smpp.common.templates.dto.TemplatesDTO;
import com.hti.smpp.common.templates.repository.TemplatesRepository;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;

@Service
public class ContactEntryServiceImpl implements ContactEntryService{
	
	private static final Logger logger = LoggerFactory.getLogger(ContactEntryServiceImpl.class.getName());
	
	@Autowired
	private ContactRepository contactRepo;
	
	@Autowired
	private UserEntryRepository userRepository;
	
	@Autowired
	private TemplatesRepository tempRepository;
	
	@Autowired
	private UserRepository userLoginRepo;

	@Override
	public ResponseEntity<?> saveContactEntry(String reqdata, MultipartFile file, String username) {
		
		ContactEntryRequest form;

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			form = objectMapper.readValue(reqdata, ContactEntryRequest.class);
			form.setFile(file);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e.getMessage());
		} catch (Exception ex) {
			throw new InternalServerException(ex.getLocalizedMessage());
		}
		
		String target = IConstants.FAILURE_KEY;
		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		}
		
		Optional<User> user = userLoginRepo.findBySystemId(systemId);
		Set<Role> role = user.get().getRoles();
		
		logger.info(systemId + "[" + role + "]" + " Adding Contact To Group: " + form.getGroupId());
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
										logger.info("Invalid Format For Entry[" + x + "]: " + entry);
										continue;
									} else {
										try {
											tokens[0] = tokens[0].replaceAll("\\s+", ""); // Replace all the spaces in the String with empty character.
											number = Long.parseLong(tokens[0]);
										} catch (Exception ex) {
											logger.info("Invalid Number For Entry[" + x + "]: " + entry);
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
							logger.error("Error: "+ex.getLocalizedMessage());
						} finally {
							if (bufferedReader != null) {
								try {
									bufferedReader.close();
								} catch (IOException ioe) {
									bufferedReader = null;
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
										logger.info("Invalid Format For Xls File");
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
											logger.info("Invalid Number For Entry[" + nextRow.getRowNum() + "]: "
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
							logger.info("Error: " +ex.getLocalizedMessage());
						} finally {
							if (workbook != null) {
								try {
									workbook.close();
								} catch (Exception e) {
									logger.info("Error: "+e.getLocalizedMessage());
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
					logger.info("Invalid Number Found: " + form.getNumber());
				}
			}
			if (entry_list.isEmpty()) {
				return new ResponseEntity<>(entry_list, HttpStatus.NO_CONTENT);
			} else {
				List<ContactEntry> contacts = this.contactRepo.saveAll(entry_list);
				target = IConstants.SUCCESS_KEY;
				logger.info("ContactEntry Saved Successfully. Message: "+target);
				return ResponseEntity.ok(contacts);
			}
		} catch (Exception e) {
			logger.error("Error: "+e.getLocalizedMessage()+", Message: "+target);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Override
	public ContactForBulk contactForBulk(ContactEntryRequest form, String username) {
		String target = IConstants.FAILURE_KEY;
		String uploadedNumbers = "";
		ContactForBulk response = new ContactForBulk();
		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		}
		logger.info("Proceed Contact For Bulk Request by " + systemId);
		try {
			if (form.getNumber() != null && form.getNumber().length > 0) {
				int number_count = 0;
				for (long number : form.getNumber()) {
					uploadedNumbers += number + "\n";
					number_count++;
				}
				response.setUploadedNumbers(uploadedNumbers);
				response.setTotalNumbers(number_count);
				response.setGroupId(form.getGroupId());
				List<TemplatesDTO> templates = null;
				try {
					templates = this.tempRepository.findByMasterId(Long.parseLong(systemId));
				} catch (Exception ex) {
					logger.error("Error: "+ex.getLocalizedMessage());
					throw new NotFoundException("Templates not found.");
				}
				if (templates != null) {
					response.setTemplates(templates);
				} else {
					logger.info("No templates exist.");
				}
				WebMasterEntry webMasterEntry = GlobalVars.WebmasterEntries.get(userOptional.get().getId());
				if (webMasterEntry != null) {
					if (webMasterEntry.getSenderId() != null && webMasterEntry.getSenderId().length() > 1) {
						Set<String> senders = new HashSet<String>(
								Arrays.asList(webMasterEntry.getSenderId().split(",")));
						logger.info(systemId + " Configured Senders: " + senders);
						response.setSenders(senders);
					} else {
						logger.info(systemId + " No Senders Configured");
					}
				} else {
					logger.error(systemId + " Webmaster Entry Not Found");
				}
				target = IConstants.SUCCESS_KEY;
			} else {
				logger.info(systemId + " No Record Found For Selected Criteria");
			}
			response.setStatus(target);
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]");
			logger.error(systemId, ex.fillInStackTrace());
		}
	
		return response;
	}

	@Override
	public List<ContactEntry> viewSearchContact(GroupEntryRequest form, String username) {
		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		}
		
		logger.info("List Contact For Bulk Request by " + systemId);
		List<ContactEntry> list = new ArrayList<ContactEntry>();
		try {
			if (form.getId() != null && form.getId().length > 0) {
				for (int groupId : form.getId()) {
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
			
		} catch (Exception ex) {
			logger.error(systemId, ex.fillInStackTrace());
		}

		return list;
	}

	@Override
	public ContactForBulk proceedSearchContact(GroupEntryRequest form, String username) {
		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		}
		logger.info("Proceed Contact For Bulk Request by " + systemId);
		String target = IConstants.FAILURE_KEY;
		String uploadedNumbers = "";
		ContactForBulk response = new ContactForBulk();
		
		try {
			List<ContactEntry> list = new ArrayList<ContactEntry>();
			if (form.getId() != null && form.getId().length > 0) {
				for (int groupId : form.getId()) {
					List<ContactEntry> part_list = this.contactRepo.findByGroupId(groupId);
					if (!part_list.isEmpty()) {
						list.addAll(part_list);
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
					templates = this.tempRepository.findByMasterId(Long.parseLong(systemId));
				} catch (Exception ex) {
					logger.error("Error: "+ex.getLocalizedMessage());
					throw new NotFoundException("Templates not found.");
				}
				if (templates != null) {
					response.setTemplates(templates);
				} else {
					logger.info("NO template Exist");
				}
				WebMasterEntry webMasterEntry = GlobalVars.WebmasterEntries.get(userOptional.get().getId());
				if (webMasterEntry != null) {
					if (webMasterEntry.getSenderId() != null && webMasterEntry.getSenderId().length() > 1) {
						Set<String> senders = new HashSet<String>(
								Arrays.asList(webMasterEntry.getSenderId().split(",")));
						logger.info(systemId + " Configured Senders: " + senders);
						response.setSenders(senders);
					} else {
						logger.info(systemId + " No Senders Configured");
					}
				} else {
					logger.error(systemId + " Webmaster Entry Not Found");
				}
				target = "proceed";
			} else {
				logger.info(systemId + " No Record Found For Selected Criteria");
			}
			response.setStatus(target);
		} catch (Exception ex) {
			logger.error(systemId, ex.fillInStackTrace());
		}
		
		return response;
	}

	@Override
	public ResponseEntity<?> modifyContactUpdate(ContactEntryRequest form, String username) {
		String target = IConstants.FAILURE_KEY;
		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		}
		
		Optional<User> user = userLoginRepo.findBySystemId(systemId);
		Set<Role> role = user.get().getRoles();
		int groupId = form.getGroupId();
		logger.info(systemId + "[" + role + "]" + " Modify Contact Request For GroupId: " + groupId);
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
				if(!list.isEmpty()) {
					this.contactRepo.saveAll(list);
					target = IConstants.SUCCESS_KEY;
				}
			} catch (Exception e) {
				logger.error(systemId, e.getLocalizedMessage());
				throw new InternalServerException(e.getLocalizedMessage());
			}
		} else {
			logger.info(systemId + " No Records Selected");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		logger.info(systemId + " modify Contact Target:" + target);
		
		return new ResponseEntity<>(target,HttpStatus.CREATED);
	}
	
}
