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
import java.util.stream.Collectors;

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
import com.hti.smpp.common.addressbook.request.GroupDataEntryRequest;
import com.hti.smpp.common.addressbook.request.SearchCriteria;
import com.hti.smpp.common.addressbook.response.ContactForBulk;
import com.hti.smpp.common.addressbook.response.EditGroupDataSearch;
import com.hti.smpp.common.addressbook.services.GroupDataEntryService;
import com.hti.smpp.common.addressbook.utils.Converters;
import com.hti.smpp.common.contacts.dto.GroupDataEntry;
import com.hti.smpp.common.contacts.repository.GroupDataEntryRepository;
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
/**
 * Implementation of the GroupDataEntryService interface.
 */
public class GroupDataEntryServiceImpl implements GroupDataEntryService {

	private static final Logger logger = LoggerFactory.getLogger(GroupDataEntryServiceImpl.class.getName());

	@Autowired
	private GroupDataEntryRepository groupDataEntryRepository;

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private TemplatesRepository tempRepository;

/**
 * Saves group data entries based on the provided request, file, and username.
 */
	@Override
	public ResponseEntity<?> saveGroupData(String request, MultipartFile file, String username) {

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

		GroupDataEntryRequest form;

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			form = objectMapper.readValue(request, GroupDataEntryRequest.class);
			form.setContactNumberFile(file);
		} catch (JsonProcessingException e) {
			throw new JsonProcessingError("JsonProccessingError: " + e.getLocalizedMessage());
		} catch (Exception ex) {
			throw new InternalServerException("Error: " + ex.getLocalizedMessage());
		}

		String target = IConstants.FAILURE_KEY;

		String systemId = user.getSystemId();

		logger.info(systemId + "[" + user.getRole() + "]" + " Adding GroupData To Group: " + form.getGroupId());

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
										logger.warn("Invalid Format For Entry[" + x + "]: " + entry);
										continue;
									} else {
										try {
											number = Long.parseLong(tokens[4]);
										} catch (Exception ex) {
											logger.warn("Invalid Number For Entry[" + x + "]: " + entry);
											continue;
										}
										int age = 0;
										try {
											age = Integer.parseInt(tokens[6]);
										} catch (Exception ex) {
											throw new InternalServerException("Error parsing age");
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
											throw new InternalServerException(
													"Error Processing: " + ex.getLocalizedMessage());
										}
									}
								}
							}
						} catch (Exception ex) {
							logger.error("Error: " + ex.getLocalizedMessage());
							throw new InternalServerException("Error processing: " + ex.getLocalizedMessage());
						} finally {
							if (bufferedReader != null) {
								try {
									bufferedReader.close();
								} catch (IOException ioe) {
									bufferedReader = null;
									logger.error(ioe.getLocalizedMessage());
									throw new InternalServerException("IOException: " + ioe.getLocalizedMessage());
								} catch (Exception ioe) {
									logger.error(ioe.getLocalizedMessage());
									throw new InternalServerException("Exception: " + ioe.getLocalizedMessage());
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
										logger.warn("Invalid Format For Xls File");
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
											logger.error("Invalid Number For Entry[" + nextRow.getRowNum() + "]: "
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
												logger.error("Error parsing age.");
												throw new InternalServerException("Error parsing age.");
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
											"Unexpected Exception: " + e.getLocalizedMessage());
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
					logger.warn("Invalid Number Found: " + form.getNumber());
					throw new NotFoundException("Number Not Found.");
				}
			}
			if (entry_list.isEmpty()) {
				logger.error("Error: Entry list is empty.No data Found" + ", Message: " + target);
				throw new NotFoundException("Entry list is empty.No data Found");
			} else {
				List<GroupDataEntry> groupData = this.groupDataEntryRepository.saveAll(entry_list);
				target = IConstants.SUCCESS_KEY;
				logger.info("GroupDataEntry Saved Successfully. Message: " + target);
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
 * Retrieves contact data for bulk processing based on provided numbers, group ID, and username.
 */
	@Override
	public ResponseEntity<?> groupDataForBulk(List<Long> numbers, int groupId, String username) {
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
		String systemId = user.getSystemId();
		logger.info("Proceed Contact For Bulk Request by " + systemId);
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
					templates = this.tempRepository.findByMasterId(systemId);
				} catch (Exception ex) {
					logger.error("Error: " + ex.getLocalizedMessage());
					throw new NotFoundException("Templates not found.");
				}
				if (templates != null) {
					response.setTemplates(templates);
				} else {
					logger.error("No templates exist.");
					throw new NotFoundException("No templates found!");
				}
				WebMasterEntry webMasterEntry = GlobalVars.WebmasterEntries.get(userOptional.get().getId());
				if (webMasterEntry != null) {
					if (webMasterEntry.getSenderId() != null && webMasterEntry.getSenderId().length() > 1) {
						Set<String> senders = new HashSet<String>(
								Arrays.asList(webMasterEntry.getSenderId().split(",")));
						logger.info(systemId + " Configured Senders: " + senders);
						response.setSenders(senders);
					} else {
						logger.warn(systemId + " No Senders Configured");
						throw new InternalServerException("No Senders Configured");
					}
				} else {
					logger.error(systemId + " Webmaster Entry Not Found");
					throw new NotFoundException("Webmaster Entry Not Found");
				}
				target = IConstants.SUCCESS_KEY;
			} else {
				logger.error(systemId + " No Record Found For Selected Criteria");
				throw new NotFoundException("No Record Found");
			}
			response.setStatus(target);
		} catch (InternalServerException ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]");
			logger.error(systemId, ex.getLocalizedMessage());
			throw new InternalServerException("Process Error: " + ex.getLocalizedMessage());
		} catch (NotFoundException ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]");
			logger.error(systemId, ex.getLocalizedMessage());
			throw new NotFoundException("Error: " + ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]");
			logger.error(systemId, ex.getLocalizedMessage());
			throw new InternalServerException("Process Error: " + ex.getLocalizedMessage());
		}

		return ResponseEntity.ok(response);
	}
/**
 * Retrieves and returns a list of GroupDataEntry based on search criteria.
 */
	@Override
	public ResponseEntity<List<GroupDataEntry>> viewSearchGroupData(GroupDataEntryRequest request, String username) {

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

		SearchCriteria criteria = new SearchCriteria();
		criteria.setArea(request.getArea());
		criteria.setCompany(request.getCompany());
		criteria.setGender(request.getGender());
		criteria.setGroupId(request.getGroupId());
		criteria.setMaxAge(request.getMaxAge());
		criteria.setMinAge(request.getMinAge());
		criteria.setNumber(request.getNumber());
		criteria.setProfession(request.getProfession());
		String systemId = user.getSystemId();
		
		logger.info("List Group Data[" + criteria.getGroupId() + "] For Bulk Request by " + systemId);

		int groupId = criteria.getGroupId();
		int maxAge = criteria.getMaxAge();
		int minAge = criteria.getMinAge();
		long[] numberArray = criteria.getNumber();
		List<Long> number = Arrays.stream(numberArray).boxed().collect(Collectors.toList());
		List<String> gender = Arrays.asList(criteria.getGender());
		List<String> area = Arrays.asList(criteria.getArea());
		List<String> profession = Arrays.asList(criteria.getProfession());
		List<String> company = Arrays.asList(criteria.getCompany());

		List<GroupDataEntry> list = new ArrayList<GroupDataEntry>();
		try {
			// ContactDAService service = new ContactDAServiceImpl();
			List<GroupDataEntry> templist = null;
			try {
				templist = this.groupDataEntryRepository
						.findByGroupIdAndProfessionInAndCompanyInAndAreaInAndGenderInAndNumberInAndAgeBetween(groupId,
								profession, company, area, gender, number, minAge, maxAge);
			} catch (Exception e) {
				logger.error("Error: " + e.getLocalizedMessage());
				throw new NotFoundException(e.getLocalizedMessage());
			}

			if (templist != null && !templist.isEmpty()) {
				Converters cc = new Converters();

				for (GroupDataEntry entry : templist) {
					if (entry.getInitials() != null && entry.getInitials().length() > 0) {
						entry.setInitials(cc.uniHexToCharMsg(entry.getInitials()));
					}
					if (entry.getFirstName() != null && entry.getFirstName().length() > 0) {
						entry.setFirstName(cc.uniHexToCharMsg(entry.getFirstName()));
					}
					if (entry.getMiddleName() != null && entry.getMiddleName().length() > 0) {
						entry.setMiddleName(cc.uniHexToCharMsg(entry.getMiddleName()));
					}
					if (entry.getLastName() != null && entry.getLastName().length() > 0) {
						entry.setLastName(cc.uniHexToCharMsg(entry.getLastName()));
					}
					list.add(entry);
				}

			} else {
				logger.info(systemId + " No Record Found For Selected Criteria");
				throw new NotFoundException("No Record Found");
			}
		} catch (NotFoundException ex) {
			logger.error(systemId, ex.toString());
			throw new NotFoundException("Error: " + ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error(systemId, ex.toString());
			throw new InternalServerException("Error: " + ex.getLocalizedMessage());
		}

		return ResponseEntity.ok(list);
	}
/**
 * Processes a search request for group data based on specified criteria.
 */
	@Override
	public ResponseEntity<ContactForBulk> proceedSearchGroupData(GroupDataEntryRequest request, String username) {
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

		SearchCriteria criteria = new SearchCriteria();
		criteria.setArea(request.getArea());
		criteria.setCompany(request.getCompany());
		criteria.setGender(request.getGender());
		criteria.setGroupId(request.getGroupId());
		criteria.setMaxAge(request.getMaxAge());
		criteria.setMinAge(request.getMinAge());
		criteria.setNumber(request.getNumber());
		criteria.setProfession(request.getProfession());

		String systemId = user.getSystemId();

		logger.info("Send Group Data[" + request.getGroupId() + "] Request by " + systemId);

		String target = IConstants.FAILURE_KEY;
		String uploadedNumbers = "";
		ContactForBulk response = new ContactForBulk();
		try {
			// ContactDAService service = new ContactDAServiceImpl();
			int groupId = criteria.getGroupId();
			int maxAge = criteria.getMaxAge();
			int minAge = criteria.getMinAge();
			long[] numberArray = criteria.getNumber();
			List<Long> number = Arrays.stream(numberArray).boxed().collect(Collectors.toList());
			List<String> gender = Arrays.asList(criteria.getGender());
			List<String> area = Arrays.asList(criteria.getArea());
			List<String> profession = Arrays.asList(criteria.getProfession());
			List<String> company = Arrays.asList(criteria.getCompany());

			List<GroupDataEntry> list = this.groupDataEntryRepository
					.findByGroupIdAndProfessionInAndCompanyInAndAreaInAndGenderInAndNumberInAndAgeBetween(groupId,
							profession, company, area, gender, number, minAge, maxAge);

			if (list != null && !list.isEmpty()) {
				for (GroupDataEntry entry : list) {
					uploadedNumbers += entry.getNumber() + "\n";
				}
				response.setUploadedNumbers(uploadedNumbers);
				response.setTotalNumbers(list.size());
				response.setGroupId(criteria.getGroupId());
				List<TemplatesDTO> templates = null;
				try {
					templates = this.tempRepository.findByMasterId(systemId);
				} catch (Exception ex) {
					logger.error("Error: " + ex.getLocalizedMessage());
					throw new NotFoundException("Templates not found: " + ex.getLocalizedMessage());
				}
				if (templates != null) {
					response.setTemplates(templates);
				} else {
					logger.error("No templates exist.");
					throw new InternalServerException("Unable to process templates");
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
				response.setStatus(target);
				throw new NotFoundException("No Record Found");
			}
		} catch (InternalServerException ex) {
			logger.error(systemId, ex.fillInStackTrace());
			throw new InternalServerException("Error: " + ex.getLocalizedMessage());
		} catch (NotFoundException ex) {
			logger.error(systemId, ex.fillInStackTrace());
			throw new NotFoundException("Error: " + ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error(systemId, ex.fillInStackTrace());
			throw new InternalServerException("Error: " + ex.getLocalizedMessage());
		}

		return ResponseEntity.ok(response);
	}
/**
 * Modifies and updates GroupDataEntry records based on the provided form data.

 */
	@Override
	@Transactional
	public ResponseEntity<?> modifyGroupDataUpdate(GroupDataEntryRequest form, String username) {

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
		
		logger.info("Group Data Update Request by " + systemId);

		if (form.getId() != null && form.getId().length > 0) {
			int groupId = form.getGroupId();
			GroupDataEntry entry = null;
			List<GroupDataEntry> list = new ArrayList<GroupDataEntry>();
			try {
				int[] id = form.getId();
				String[] initials = form.getInitials();
				String[] firstName = form.getFirstName();
				String[] middleName = form.getMiddleName();
				String[] lastName = form.getLastName();
				int[] age = form.getAge();
				String[] gender = form.getGender();
				String[] email = form.getEmail();
				long[] number = form.getNumber();
				String[] company = form.getCompany();
				String[] profession = form.getProfession();
				String[] area = form.getArea();
				String first_name = null, middle_name = null, last_name = null, initial = null;
				for (int i = 0; i < id.length; i++) {
					initial = null;
					first_name = null;
					middle_name = null;
					last_name = null;
					if (initials[i] != null && initials[i].length() > 0) {
						initial = new Converters().UTF16(initials[i]);
					}
					if (firstName[i] != null && firstName[i].length() > 0) {
						first_name = new Converters().UTF16(firstName[i]);
					}
					if (middleName[i] != null && middleName[i].length() > 0) {
						middle_name = new Converters().UTF16(middleName[i]);
					}
					if (lastName[i] != null && lastName[i].length() > 0) {
						last_name = new Converters().UTF16(lastName[i]);
					}
					entry = new GroupDataEntry(groupId, initial, first_name, middle_name, last_name, number[i],
							email[i], age[i], profession[i], company[i], area[i], gender[i]);
					entry.setId(id[i]);
					list.add(entry);
				}
				if (!list.isEmpty()) {
					this.groupDataEntryRepository.saveAll(list);
					target = IConstants.SUCCESS_KEY;
				} else {
					throw new InternalServerException("Error saving GroupDataEntry.");
				}

			} catch (InternalServerException ex) {
				logger.error(systemId, ex.getLocalizedMessage());
				throw new InternalServerException(ex.getLocalizedMessage());
			} catch (Exception ex) {
				logger.error(systemId, ex.getLocalizedMessage());
				throw new InternalServerException(ex.getLocalizedMessage());
			}
		} else {
			logger.error(systemId + " No GroupData Records Found To Update");
			throw new NotFoundException(systemId + " No GroupData Records Found To Update");
		}
		logger.info(systemId + " Modify GroupDataEntryUpdate Target:" + target);
		return new ResponseEntity<>(target, HttpStatus.CREATED);
	}
/**
 *  Logs deleted group data entries along with the associated username.
 * @param username
 * @param deletedContactsIds
 */
	private void logDeletedGroupData(String username, List<Integer> deletedContactsIds) {
		if (!deletedContactsIds.isEmpty()) {
			logger.info("Deleted contacts by {}: {}", username, deletedContactsIds);
		}
	}
/**
 * Logs failed deletions of group data entries along with the associated username.
 * @param username
 * @param failedDeletionIds
 */
	private void logFailedDeletions(String username, List<Integer> failedDeletionIds) {
		if (!failedDeletionIds.isEmpty()) {
			logger.warn("Failed to delete contacts by {}: {}", username, failedDeletionIds);
		}
	}
/**
 * Deletes group data entries identified by the given list of IDs.
 */
	@Override
	@Transactional
	public ResponseEntity<?> modifyGroupDataDelete(List<Integer> ids, String username) {

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
				List<GroupDataEntry> groupdataToDelete = this.groupDataEntryRepository.findAllById(ids);

				if (groupdataToDelete.isEmpty()) {
					throw new NotFoundException("No GroupDataEntry Found!");
				}

				for (GroupDataEntry groupdata : groupdataToDelete) {
					try {
						this.groupDataEntryRepository.delete(groupdata);
						successfulDeletions.add(groupdata.getId());
					} catch (Exception e) {
						logger.error("Error deleting group data with ID {}: {}", groupdata.getId(), e.getMessage(), e);
						failedDeletionIds.add(groupdata.getId());
						throw new InternalServerException("Error deleting group data id: " + groupdata.getId());
					}
				}

				target = IConstants.SUCCESS_KEY;
				logDeletedGroupData(username, successfulDeletions);
				logFailedDeletions(username, failedDeletionIds);
			}
			return ResponseEntity.status(HttpStatus.OK).body(target);
		} catch (NotFoundException e) {
			logger.error("Error deleting group data: {}", e.getMessage(), e);
			target = IConstants.FAILURE_KEY;
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error("Error deleting group data: {}", e.getMessage(), e);
			target = IConstants.FAILURE_KEY;
			throw new InternalServerException(e.getLocalizedMessage());
		}

	}
/**
 * Creates and returns a workbook containing group data entries.
 * @param list
 * @return
 */
	private Workbook getWorkBook(List<GroupDataEntry> list) {
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
		String[] headers = { "Initials", "FirstName", "MiddleName", "LastName", "Gender", "Age", "Email", "Number",
				"Company", "Profession", "Area" };
		while (!list.isEmpty()) {
			int row_number = 0;
			sheet = workbook.createSheet("Sheet(" + sheet_number + ")");
			sheet.setDefaultColumnWidth(18);
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
					GroupDataEntry entry = list.remove(0);
					logger.debug("Add Row[" + row_number + "]: " + entry);
					Cell cell = row.createCell(0);
					cell.setCellValue(entry.getInitials());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(1);
					cell.setCellValue(entry.getFirstName());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(2);
					cell.setCellValue(entry.getMiddleName());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(3);
					cell.setCellValue(entry.getLastName());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(4);
					cell.setCellValue(entry.getGender());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(5);
					cell.setCellValue(entry.getAge());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(6);
					cell.setCellValue(entry.getEmail());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(7);
					cell.setCellValue(String.valueOf(entry.getNumber()));
					cell.setCellStyle(rowStyle);
					cell = row.createCell(8);
					cell.setCellValue(entry.getCompany());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(9);
					cell.setCellValue(entry.getProfession());
					cell.setCellStyle(rowStyle);
					cell = row.createCell(10);
					cell.setCellValue(entry.getArea());
					cell.setCellStyle(rowStyle);
				}
				if (++row_number > records_per_sheet) {
					logger.info("GroupData Sheet Created: " + sheet_number);
					break;
				}
			}
			sheet_number++;
		}
		logger.info("GroupData Workbook Created");
		return workbook;
	}
/**
 * Handles the request to export group data entries based on the provided criteria.
 */
	@Override
	public ResponseEntity<?> modifyGroupDataExport(GroupDataEntryRequest form, String username) {

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
		logger.info("Group Data Export Request by " + systemId);
		String target = IConstants.FAILURE_KEY;
		if (form.getId() != null && form.getId().length > 0) {
			int groupId = form.getGroupId();
			GroupDataEntry entry = null;
			List<GroupDataEntry> list = new ArrayList<GroupDataEntry>();
			Converters cc = new Converters();
			try {
				int[] id = form.getId();
				String[] initials = form.getInitials();
				String[] firstName = form.getFirstName();
				String[] middleName = form.getMiddleName();
				String[] lastName = form.getLastName();
				int[] age = form.getAge();
				String[] gender = form.getGender();
				String[] email = form.getEmail();
				long[] number = form.getNumber();
				String[] company = form.getCompany();
				String[] profession = form.getProfession();
				String[] area = form.getArea();
				String first_name = null, middle_name = null, last_name = null, initial = null;
				for (int i = 0; i < id.length; i++) {
					initial = null;
					first_name = null;
					middle_name = null;
					last_name = null;
					if (initials[i] != null && initials[i].length() > 0) {
						initial = new Converters().UTF16(initials[i]);
					}
					if (firstName[i] != null && firstName[i].length() > 0) {
						first_name = new Converters().UTF16(firstName[i]);
					}
					if (middleName[i] != null && middleName[i].length() > 0) {
						middle_name = new Converters().UTF16(middleName[i]);
					}
					if (lastName[i] != null && lastName[i].length() > 0) {
						last_name = new Converters().UTF16(lastName[i]);
					}
					entry = new GroupDataEntry(groupId, cc.uniHexToCharMsg(initial), cc.uniHexToCharMsg(first_name),
							cc.uniHexToCharMsg(middle_name), cc.uniHexToCharMsg(last_name), number[i], email[i], age[i],
							profession[i], company[i], area[i], gender[i]);
					entry.setId(id[i]);
					list.add(entry);
				}
				Workbook workbook = null;
				try {
					workbook = getWorkBook(list);
				} catch (Exception e1) {
					logger.error("WorkBook Error: " + e1.getLocalizedMessage());
					throw new WorkBookException("WorkBook Exception: " + e1.getLocalizedMessage());
				}
				String filename = systemId + "_GroupData[" + groupId + "]" + ".xlsx";
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				logger.info(systemId + " Creating GroupData XLSx ");
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
					throw new InternalServerException(e.getLocalizedMessage());
				} catch (Exception e) {
					logger.error("Unexpected exception: " + e.getLocalizedMessage());
					throw new InternalServerException(e.getLocalizedMessage());
				}

			} catch (Exception ex) {
				logger.error(systemId, ex.toString());
				throw new InternalServerException(ex.getLocalizedMessage());
			}
		} else {
			logger.error(systemId + " No GroupData Records Found To Export");
			throw new NotFoundException(systemId + " No GroupData Records Found To Export");
		}
	}
/**
 * Capitalizes the first letter of the input string.
 * @param str
 * @return
 */
	private String capitalize(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
/**
 * Retrieves the value of the specified property from a GroupDataEntry object using reflection.
 * @param entry
 * @param property
 * @return
 */
	private String getProperty(GroupDataEntry entry, String property) {
		try {
			return (String) entry.getClass().getMethod("get" + capitalize(property)).invoke(entry);
		} catch (Exception e) {
			throw new RuntimeException("Error retrieving property value", e);
		}
	}
/**
 * Retrieves distinct values of a specified property for GroupDataEntry objects with the given group ID.
 * @param groupId
 * @param property
 * @return
 */
	public List<String> distinctGroupData(int groupId, String property) {
		List<GroupDataEntry> entries = groupDataEntryRepository.findByGroupId(groupId);

		List<String> distinctValues = entries.stream().map(entry -> getProperty(entry, property)).distinct()
				.collect(Collectors.toList());

		return distinctValues;

	}
/**
 * Searches for GroupData with specified options for editing.
 */
	@Override
	public ResponseEntity<?> editGroupDataSearch(int groupId, String username) {

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
		String target = "search";
		String systemId = user.getSystemId();
		
		logger.info(systemId + "[" + user.getRole() + "]" + " Search GroupData Request For Group: " + groupId);

		EditGroupDataSearch response = new EditGroupDataSearch();
		Set<String> professions = new HashSet<String>();
		Set<String> companies = new HashSet<String>();
		Set<String> areas = new HashSet<String>();

		List<String> list = null;
		try {
			list = distinctGroupData(groupId, "profession");
		} catch (Exception e) {
			logger.error("Error in adding profession to list: " + e.getLocalizedMessage());
			throw new InternalServerException("Exception in adding profession to list: " + e.getLocalizedMessage());
		}

		professions.addAll(list);
		response.setProfessions(professions);

		try {
			list = distinctGroupData(groupId, "company");
		} catch (Exception e) {
			logger.error("Error in adding company to list: " + e.getLocalizedMessage());
			throw new InternalServerException("Exception in adding company to list: " + e.getLocalizedMessage());
		}

		companies.addAll(list);
		response.setCompanies(companies);

		try {
			list = distinctGroupData(groupId, "area");
		} catch (Exception e) {
			logger.error("Error in adding area to list: " + e.getLocalizedMessage());
			throw new InternalServerException("Exception in adding area to list: " + e.getLocalizedMessage());
		}

		areas.addAll(list);
		response.setAreas(areas);

		response.setGroupId(groupId);
		response.setTarget(target);

		if (response.getAreas().isEmpty() || response.getCompanies().isEmpty() || response.getProfessions().isEmpty()
				|| response == null) {
			throw new InternalServerException("Unable to search Group Data.");
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
