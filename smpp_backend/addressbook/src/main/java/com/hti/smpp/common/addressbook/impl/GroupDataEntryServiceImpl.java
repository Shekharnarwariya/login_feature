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
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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

import jakarta.transaction.Transactional;

@Service
public class GroupDataEntryServiceImpl implements GroupDataEntryService {

	private static final Logger logger = LoggerFactory.getLogger(GroupDataEntryServiceImpl.class.getName());

	@Autowired
	private GroupDataEntryRepository groupDataEntryRepository;

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private TemplatesRepository tempRepository;

	@Autowired
	private UserRepository userLoginRepo;

	@Override
	@Transactional
	public ResponseEntity<?> saveGroupData(String request, MultipartFile file, String username) {

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

		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		}

		Optional<User> user = userLoginRepo.findBySystemId(systemId);
		Set<Role> role = user.get().getRoles();

		logger.info(systemId + "[" + role + "]" + " Adding GroupData To Group: " + form.getGroupId());

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
							logger.error("Error: " + ex.getLocalizedMessage());
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
							logger.info("Error: " + ex.getLocalizedMessage());
						} finally {
							if (workbook != null) {
								try {
									workbook.close();
								} catch (Exception e) {
									logger.info("Error: " + e.getLocalizedMessage());
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
				return new ResponseEntity<>(target, HttpStatus.NO_CONTENT);
			} else {
				List<GroupDataEntry> groupData = this.groupDataEntryRepository.saveAll(entry_list);
				target = IConstants.SUCCESS_KEY;
				logger.info("GroupDataEntry Saved Successfully. Message: " + target);
				return new ResponseEntity<>(target, HttpStatus.CREATED);
			}
		} catch (Exception e) {
			logger.error("Error: " + e.getLocalizedMessage() + ", Message: " + target);
			return new ResponseEntity<>(target, HttpStatus.INTERNAL_SERVER_ERROR);
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
					logger.error("Error: " + ex.getLocalizedMessage());
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
	public List<GroupDataEntry> viewSearchGroupData(GroupDataEntryRequest request, String username) {

		SearchCriteria criteria = new SearchCriteria();
		criteria.setArea(request.getArea());
		criteria.setCompany(request.getCompany());
		criteria.setGender(request.getGender());
		criteria.setGroupId(request.getGroupId());
		criteria.setMaxAge(request.getMaxAge());
		criteria.setMinAge(request.getMinAge());
		criteria.setNumber(request.getNumber());
		criteria.setProfession(request.getProfession());
		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		}
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
			List<GroupDataEntry> templist = this.groupDataEntryRepository
					.findByGroupIdAndProfessionInAndCompanyInAndAreaInAndGenderInAndNumberInAndAgeBetween(groupId,
							profession, company, area, gender, number, minAge, maxAge);

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
			}
		} catch (Exception ex) {
			logger.error(systemId, ex.fillInStackTrace());
			throw new InternalServerException("Error: " + ex.getLocalizedMessage());
		}

		return list;
	}

	@Override
	public ContactForBulk proceedSearchGroupData(GroupDataEntryRequest request, String username) {

		SearchCriteria criteria = new SearchCriteria();
		criteria.setArea(request.getArea());
		criteria.setCompany(request.getCompany());
		criteria.setGender(request.getGender());
		criteria.setGroupId(request.getGroupId());
		criteria.setMaxAge(request.getMaxAge());
		criteria.setMinAge(request.getMinAge());
		criteria.setNumber(request.getNumber());
		criteria.setProfession(request.getProfession());

		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		}
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
					templates = this.tempRepository.findByMasterId(Long.parseLong(systemId));
				} catch (Exception ex) {
					logger.error("Error: " + ex.getLocalizedMessage());
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
				target = "proceed";
			} else {
				logger.info(systemId + " No Record Found For Selected Criteria");
				response.setStatus(target);
			}
		} catch (Exception ex) {
			logger.error(systemId, ex.fillInStackTrace());
			throw new InternalServerException("Error: " + ex.getLocalizedMessage());
		}

		return response;
	}

	@Override
	public ResponseEntity<?> modifyGroupDataUpdate(GroupDataEntryRequest form, String username) {
		String target = IConstants.FAILURE_KEY;
		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		}
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
				}

			} catch (Exception ex) {
				logger.error(systemId, ex.getLocalizedMessage());
				return new ResponseEntity<>(target, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			logger.info(systemId + " No GroupData Records Found To Update");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		logger.info(systemId + " Modify GroupDataEntryUpdate Target:" + target);

		return new ResponseEntity<>(target, HttpStatus.CREATED);
	}

	private void logDeletedGroupData(String username, List<Integer> deletedContactsIds) {
		if (!deletedContactsIds.isEmpty()) {
			logger.info("Deleted contacts by {}: {}", username, deletedContactsIds);
		}
	}

	private void logFailedDeletions(String username, List<Integer> failedDeletionIds) {
		if (!failedDeletionIds.isEmpty()) {
			logger.warn("Failed to delete contacts by {}: {}", username, failedDeletionIds);
		}
	}

	@Override
	public ResponseEntity<?> modifyGroupDataDelete(List<Integer> ids, String username) {
		String target = IConstants.FAILURE_KEY;
		List<Integer> successfulDeletions = new ArrayList<>();
		List<Integer> failedDeletionIds = new ArrayList<>();

		try {
			if (!ids.isEmpty()) {
				List<GroupDataEntry> groupdataToDelete = this.groupDataEntryRepository.findAllById(ids);
				for (GroupDataEntry groupdata : groupdataToDelete) {
					try {
						this.groupDataEntryRepository.delete(groupdata);
						successfulDeletions.add(groupdata.getId());
					} catch (Exception e) {
						logger.error("Error deleting group data with ID {}: {}", groupdata.getId(), e.getMessage(), e);
						failedDeletionIds.add(groupdata.getId());
					}
				}

				target = IConstants.SUCCESS_KEY;
				logDeletedGroupData(username, successfulDeletions);
				logFailedDeletions(username, failedDeletionIds);
			}
			return ResponseEntity.status(HttpStatus.OK).body(target);
		} catch (Exception e) {
			logger.error("Error deleting group data: {}", e.getMessage(), e);
			target = IConstants.FAILURE_KEY;
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(target);
		}
	}

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

	@Override
	public ResponseEntity<?> modifyGroupDataExport(GroupDataEntryRequest form, String username) {

		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		}
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
				Workbook workbook = getWorkBook(list);
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
					return new ResponseEntity<>(target, HttpStatus.INTERNAL_SERVER_ERROR);
				}

			} catch (Exception ex) {
				logger.error(systemId, ex.toString());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			}
		} else {
			logger.info(systemId + " No GroupData Records Found To Export");
		}
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	private String capitalize(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	private String getProperty(GroupDataEntry entry, String property) {
		try {
			return (String) entry.getClass().getMethod("get" + capitalize(property)).invoke(entry);
		} catch (Exception e) {
			throw new RuntimeException("Error retrieving property value", e);
		}
	}

	public List<String> distinctGroupData(int groupId, String property) {
		List<GroupDataEntry> entries = groupDataEntryRepository.findByGroupId(groupId);
		
		 List<String> distinctValues = entries.stream()
	                .map(entry -> getProperty(entry, property))
	                .distinct()
	                .collect(Collectors.toList());

	        return distinctValues;

	}

	@Override
	public ResponseEntity<?> editGroupDataSearch(int groupId, String username) {
		String target = "search";
		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		}
		Optional<User> user = userLoginRepo.findBySystemId(systemId);
		Set<Role> role = user.get().getRoles();
		logger.info(systemId + "[" + role + "]" + " Search GroupData Request For Group: " + groupId);

		EditGroupDataSearch response = new EditGroupDataSearch();
		Set<String> professions = new HashSet<String>();
		Set<String> companies = new HashSet<String>();
		Set<String> areas = new HashSet<String>();
		
		List<String> list = distinctGroupData(groupId, "profession");
		
		professions.addAll(list);
		response.setProfessions(professions);
		
		list = distinctGroupData(groupId, "company");
		
		companies.addAll(list);
		response.setCompanies(companies);
		
		list = distinctGroupData(groupId, "area");
		
		areas.addAll(list);
		response.setAreas(areas);
		
		response.setGroupId(groupId);
		response.setTarget(target);
		
		if(response.getAreas().isEmpty() || response.getCompanies().isEmpty() || response.getProfessions().isEmpty() || response == null) {
			return new ResponseEntity<>("Unable to search Group Data.",HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(response,HttpStatus.OK);
	}

}
