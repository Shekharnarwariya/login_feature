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
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hti.smpp.common.addressbook.request.GroupDataEntryRequest;
import com.hti.smpp.common.addressbook.response.ContactForBulk;
import com.hti.smpp.common.addressbook.services.GroupDataEntryService;
import com.hti.smpp.common.addressbook.utils.Converters;
import com.hti.smpp.common.contacts.dto.GroupDataEntry;
import com.hti.smpp.common.contacts.repository.GroupDataEntryRepository;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.templates.dto.TemplatesDTO;
import com.hti.smpp.common.templates.repository.TemplatesRepository;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;

@Service
public class GroupDataEntryServiceImpl implements GroupDataEntryService{
	
	private static final Logger logger = LoggerFactory.getLogger(GroupDataEntryServiceImpl.class.getName());
	
	@Autowired
	private GroupDataEntryRepository groupDataEntryRepository;
	
	@Autowired
	private UserEntryRepository userRepository;
	
	@Autowired
	private TemplatesRepository tempRepository;

	@Override
	public ResponseEntity<?> saveGroupData(String request, MultipartFile file,String username) {
		
		GroupDataEntryRequest form;

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			form = objectMapper.readValue(request, GroupDataEntryRequest.class);
			form.setContactNumberFile(file);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e.getMessage());
		} catch (Exception ex) {
			throw new InternalServerException(ex.getLocalizedMessage());
		}
		
		String target = IConstants.FAILURE_KEY;
		
		try {
			int groupId = form.getGroupId();
			String mode = form.getType();
			String format = null;
			List<GroupDataEntry> entry_list = new ArrayList<GroupDataEntry>();
			// -------------------------------------------
			if (mode.equalsIgnoreCase("multiple")) {
				MultipartFile uploadedFile = form.getContactNumberFile();
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
									new InputStreamReader(form.getContactNumberFile().getInputStream(), "UTF-8"));
							int x = 0;
							String entry = null;
							while ((entry = bufferedReader.readLine()) != null) {
								x++;
								logger.info(x + ": " + entry);
								if (entry != null && entry.length() > 0) {
									String[] tokens = entry.split(",");
									long number;
									if (tokens.length == 0) {
										logger.info("Invalid Format For Entry[" + x + "]: " + entry);
										continue;
									} else {
										try {
											number = Long.parseLong(tokens[4]);
										} catch (Exception ex) {
											logger.info("Invalid Number For Entry[" + x + "]: " + entry);
											continue;
										}
										int age = 0;
										try {
											age = Integer.parseInt(tokens[6]);
										} catch (Exception ex) {
										}
										try {
											String initials = null, first_name = null, middle_name = null,
													last_name = null;
											if (tokens[0] != null && tokens[0].length() > 0) {
												initials = new Converters().UTF16(tokens[0]);
											}
											if (tokens[1] != null && tokens[1].length() > 0) {
												first_name = new Converters().UTF16(tokens[1]);
											}
											if (tokens[2] != null && tokens[2].length() > 0) {
												middle_name = new Converters().UTF16(tokens[2]);
											}
											if (tokens[3] != null && tokens[3].length() > 0) {
												last_name = new Converters().UTF16(tokens[3]);
											}
											entry_list.add(new GroupDataEntry(groupId, initials, first_name,
													middle_name, last_name, number, tokens[5], age, tokens[7],
													tokens[8], tokens[9], tokens[10]));
										} catch (Exception ex) {
											logger.error(entry, ex.fillInStackTrace());
										}
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
								workbook = new XSSFWorkbook(form.getContactNumberFile().getInputStream());
							} else {
								workbook = new HSSFWorkbook(form.getContactNumberFile().getInputStream());
							}
							Sheet firstSheet = workbook.getSheetAt(0);
							logger.info("Sheet[0] Total Rows: " + firstSheet.getPhysicalNumberOfRows());
							int column_count = 0;
							for (Row nextRow : firstSheet) {
								if (nextRow.getRowNum() == 0) {
									column_count = nextRow.getPhysicalNumberOfCells();
									if (column_count == 0) {
										logger.info("Invalid Format For Xls File");
										break;
									}
								}
								String initial = null, firstname = null, middlename = null, lastname = null,
										email = null, profession = null, company = null, area = null, gender = null;
								int age = 0;
								long number = 0;
								String cell_value = null;
								logger.info("Row [" + nextRow.getRowNum() + "] Cell Counter: "
										+ nextRow.getPhysicalNumberOfCells());
								for (int cell_number = 0; cell_number < nextRow.getLastCellNum(); cell_number++) {
									Cell cell = nextRow.getCell(cell_number);
									cell_value = new DataFormatter().formatCellValue(cell);
									if (cell_number == 4) {
										try {
											number = Long.parseLong(cell_value);
										} catch (Exception ex) {
											logger.info("Invalid Number For Entry[" + nextRow.getRowNum() + "]: "
													+ cell_value);
											break;
										}
									}
									if (cell_value != null && cell_value.length() > 0) {
										if (cell_number == 0) {
											initial = new Converters().UTF16(cell_value);
										} else if (cell_number == 1) {
											firstname = new Converters().UTF16(cell_value);
										} else if (cell_number == 2) {
											middlename = new Converters().UTF16(cell_value);
										} else if (cell_number == 3) {
											lastname = new Converters().UTF16(cell_value);
										} else if (cell_number == 5) {
											if (cell_value.contains("@")) {
												email = cell_value;
											}
										} else if (cell_number == 6) {
											try {
												age = Integer.parseInt(cell_value);
											} catch (Exception ex) {
											}
										} else if (cell_number == 7) {
											profession = cell_value;
										} else if (cell_number == 8) {
											company = cell_value;
										} else if (cell_number == 9) {
											area = cell_value;
										} else if (cell_number == 10) {
											gender = cell_value;
										}
									}
									if (cell_number == 10) {
										break;
									}
								}
								logger.info(nextRow.getRowNum() + ": groupId=" + groupId + ",initial=" + initial
										+ ",firstname=" + firstname + ",middlename=" + middlename + ",lastname="
										+ lastname + ",number=" + number + ",email=" + email + ",age=" + age
										+ ",profession=" + profession + ",company=" + company + ",area=" + area
										+ ",gender=" + gender);
								if (number > 0) {
									entry_list.add(new GroupDataEntry(groupId, initial, firstname, middlename, lastname,
											number, email, age, profession, company, area, gender));
								}
							}
						} catch (Exception ex) {
							logger.info("Error: "+ex.getLocalizedMessage());
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
				if (form.getNumber() != null && form.getNumber()[0] > 0) {
					String initial = null, firstname = null, middlename = null, lastname = null;
					if (form.getInitials()[0] != null && form.getInitials()[0].length() > 0) {
						initial = new Converters().UTF16(form.getInitials()[0]);
					}
					if (form.getFirstName()[0] != null && form.getFirstName()[0].length() > 0) {
						firstname = new Converters().UTF16(form.getFirstName()[0]);
					}
					if (form.getMiddleName()[0] != null && form.getMiddleName()[0].length() > 0) {
						middlename = new Converters().UTF16(form.getMiddleName()[0]);
					}
					if (form.getLastName()[0] != null && form.getLastName()[0].length() > 0) {
						lastname = new Converters().UTF16(form.getLastName()[0]);
					}
					entry_list.add(new GroupDataEntry(groupId, initial, firstname, middlename, lastname,
							form.getNumber()[0], form.getEmail()[0], form.getAge()[0], form.getProfession()[0],
							form.getCompany()[0], form.getArea()[0], form.getGender()[0]));
				} else {
					logger.info("Invalid Number Found: " + form.getNumber());
				}
			}
			if (entry_list.isEmpty()) {
				return new ResponseEntity<>(entry_list, HttpStatus.NO_CONTENT);
			} else {
				List<GroupDataEntry> groupData = this.groupDataEntryRepository.saveAll(entry_list);
				target = IConstants.SUCCESS_KEY;
				logger.info("GroupDataEntry Saved Successfully. Message: "+target);
				return ResponseEntity.ok(groupData);
			}
		} catch (Exception e) {
			logger.error("Error: "+e.getLocalizedMessage()+", Message: "+target);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
	}

	@Override
	public ContactForBulk groupDataForBulk(GroupDataEntryRequest form, String username) {
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
				response.setStatus(target);
			}
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]");
			logger.error(systemId, ex.fillInStackTrace());
		}
	
		return response;
	}

}
