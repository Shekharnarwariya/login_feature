package com.hti.smpp.common.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hti.smpp.common.contacts.dto.GroupEntryDTO;
import com.hti.smpp.common.contacts.repository.GroupEntryDTORepository;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.JsonProcessingError;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.request.SubscribeEntryForm;
import com.hti.smpp.common.service.SubscribeService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.Converter;
import com.hti.smpp.common.util.Converters;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;
import com.hti.smpp.common.util.SevenBitChar;
import com.hti.smpp.common.util.dto.SubscribeEntry;
import com.hti.smpp.common.util.repository.SubscribeEntryRepository;

@Service
public class SubscribeServiceImpl implements SubscribeService {

	@Autowired
	private GroupEntryDTORepository groupRepository;

	@Autowired
	private SubscribeEntryRepository subscribeEntryRepository;

	@Autowired
	private UserEntryRepository userRepository;
	
	@Autowired
	private MessageResourceBundle messageResourceBundle;

	private static final Logger logger = LoggerFactory.getLogger(SubscribeServiceImpl.class);

	private static String getAsciiList(String msg) {
		StringBuilder asciiValues = new StringBuilder();
		for (int i = 0; i < msg.length(); i++) {
			int asciiNum = (int) msg.charAt(i);
			asciiValues.append(asciiNum).append(',');
		}
		// System.out.println(asciiValues.toString());
		return asciiValues.toString();
	}

	public static String getHexValue(String msg) {
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < msg.length(); i++) {
			char c = msg.charAt(i);
			String hex = Integer.toHexString(c);
			result.append(String.format("%04x", (int) c));
		}

		return result.toString();
	}

	@Override
	public ResponseEntity<?> saveSubscribe(String request, MultipartFile headerFile, MultipartFile footerFile,
			String username) {
		// Validate user authorization
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}

		// Initialize variables
		SubscribeEntryForm form;

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			form = objectMapper.readValue(request, SubscribeEntryForm.class);
			form.setHeaderFile(headerFile);
			form.setFooterFile(footerFile);
		} catch (JsonProcessingException e) {
			throw new JsonProcessingError(messageResourceBundle.getExMessage(ConstantMessages.JSON_PROCESSING_ERROR,new Object[] {e.getMessage()}));
		} catch (Exception ex) {
			throw new InternalServerException(ex.getLocalizedMessage());
		}
		String target = IConstants.FAILURE_KEY;
		String masterId = user.getMasterId();
		logger.info(messageResourceBundle.getLogMessage("subscription.add.req"),masterId,form.getPageName(),form.getSender());
		SubscribeEntry entry = new SubscribeEntry();
		String header_file_name = null;
		String footer_file_name = null;
		try {
			// Process message content
			if (form.getMessage() != null && form.getMessage().length() > 0) {
				if (form.getMessageType().equalsIgnoreCase("7bit")) {
					if (getAsciiList(form.getMessage()) != null) {
						entry.setOrigMessage(new Converters().UTF16(form.getMessage()));
						String hexmsg = SevenBitChar.getHexValue(getAsciiList(form.getMessage()));
						System.out.println("hex: " + hexmsg);
						String content = Converter.getContent(hexmsg.toCharArray());
						System.out.println("content: " + content);
						if (hexmsg != null) {
							entry.setMessage(new Converters().UTF16(content));
							logger.info(entry.getMessage());
						}
					}
				} else {
					entry.setOrigMessage(new Converters().UTF16(form.getMessage()));
					entry.setMessage(getHexValue(form.getMessage()));
				}
				entry.setMessageType(form.getMessageType());
			}
			// Process header file
			if (form.getHeaderFile() != null && form.getHeaderFile().getOriginalFilename() != null
					&& form.getHeaderFile().getOriginalFilename().length() > 0) {
				logger.info(messageResourceBundle.getLogMessage("subscription.header.upload"),masterId,form.getHeaderFile().getOriginalFilename());
				String header_file_ext = form.getHeaderFile().getOriginalFilename()
						.substring(form.getHeaderFile().getOriginalFilename().lastIndexOf(".") + 1);
				if (form.getUsername().length() > 4) {
					header_file_name = form.getUsername().substring(0, 4);
				} else {
					header_file_name = form.getUsername();
				}
				header_file_name += "h" + new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date()) + "."
						+ header_file_ext;
				try {
					if (writeToFile(header_file_name, form.getHeaderFile())) {
						logger.info(messageResourceBundle.getLogMessage("subscription.header.created"),masterId,header_file_name);
					} else {
						header_file_name = null;
					}
				} catch (Exception ex) {
					logger.error(masterId + "-" + header_file_name, ex);
					header_file_name = null;
				}
				entry.setHeaderFileName(header_file_name);
			}
			// Process footer file
			if (form.getFooterFile() != null && form.getFooterFile().getOriginalFilename() != null
					&& form.getFooterFile().getOriginalFilename().length() > 0) {
				logger.info(messageResourceBundle.getLogMessage("subscription.footer.upload"),masterId,form.getFooterFile().getOriginalFilename());
				String footer_file_ext = form.getFooterFile().getOriginalFilename()
						.substring(form.getFooterFile().getOriginalFilename().lastIndexOf(".") + 1);
				if (form.getUsername().length() > 4) {
					footer_file_name = form.getUsername().substring(0, 4);
				} else {
					footer_file_name = form.getUsername();
				}
				footer_file_name += "f" + new java.text.SimpleDateFormat("yyMMddHHmmssSSS").format(new Date()) + "."
						+ footer_file_ext;
				try {
					if (writeToFile(footer_file_name, form.getFooterFile())) {
						logger.info(messageResourceBundle.getLogMessage("subscription.footer.created"),masterId,footer_file_name);
					} else {
						footer_file_name = null;
					}
				} catch (Exception ex) {
					logger.error(masterId + "-" + footer_file_name, ex);
					footer_file_name = null;
				}
				entry.setFooterFileName(footer_file_name);
			}
			// Set other attributes
			entry.setUsername(form.getUsername());
			entry.setPassword(form.getPassword());
			entry.setPageName(form.getPageName());
			entry.setSender(form.getSender());
			entry.setCreatedBy(masterId);
			if (form.getCountryCode() != null) {
				try {
					Integer.parseInt(form.getCountryCode());
					entry.setCountryCode(form.getCountryCode());
				} catch (Exception e) {
					logger.error(messageResourceBundle.getLogMessage("subscription.invalid.cc"),masterId,form.getCountryCode());
					// throw new InternalServerException(masterId + " Invalid Country Code: " +
					// form.getCountryCode());
				}
			}
			// Process country code
			// Save GroupEntryDTO
			GroupEntryDTO groupEntry = new GroupEntryDTO();
			groupEntry.setName(new Converters().UTF16(form.getPageName()));
			groupEntry.setMasterId(masterId);
			int groupId = groupRepository.save(groupEntry).getId();
			// Save SubscribeEntry
			if (groupId > 0) {
				entry.setGroupId(groupId);
				int generatedId = subscribeEntryRepository.save(entry).getId();
				if (generatedId > 0) {
					logger.info(messageResourceBundle.getLogMessage("subscription.saved.success"));
					target = IConstants.SUCCESS_KEY;
				} else {
					logger.error(messageResourceBundle.getLogMessage("subscription.saved.failure"));
					throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SUBSCRIPTION_ADD_ERROR));
				}
			} else {
				logger.error(messageResourceBundle.getLogMessage("subscription.contact.failure"));
				throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SUBSCRIPTION_ADD_ERROR));
			}
		} catch (DataIntegrityViolationException ex) {
			logger.error(masterId, ex.getMessage());
			if (form.getHeaderFile() != null && form.getFooterFile() != null) {
				deleteFile(header_file_name);
				deleteFile(footer_file_name);
			}
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SUBSCRIPTION_DUPLICATE_ENTRY, new Object[] {form.getPageName(),form.getUsername()}));
		} catch (Exception ex) {
			logger.error(masterId, ex.getMessage());
			if (form.getHeaderFile() != null && form.getFooterFile() != null) {
				deleteFile(header_file_name);
				deleteFile(footer_file_name);
			}
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SUBSCRIPTION_MSG_ERROR, new Object[] {ex.getMessage()}));
		}
		return new ResponseEntity<>(messageResourceBundle.getMessage(ConstantMessages.SUBSCRIPTION_ADD_SUCCESS), HttpStatus.CREATED);
	}

	public boolean writeToFile(String fileName, MultipartFile file) {
		try {
			Path directoryPath = Path.of(IConstants.WEBAPP_DIR + "images//subscription");
			if (!Files.exists(directoryPath)) {
				Files.createDirectories(directoryPath);
				logger.info(messageResourceBundle.getLogMessage("subscription.dir.created"));
			}
			Path filePath = directoryPath.resolve(fileName);
			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
			logger.info(messageResourceBundle.getLogMessage("subscription.file.saved"),filePath);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteFile(String fileName) {
		try {
			Path filePath = Paths.get(IConstants.WEBAPP_DIR + "images//subscription", fileName);
			if (Files.exists(filePath)) {
				Files.delete(filePath);
				logger.info(messageResourceBundle.getLogMessage("subscription.file.deleted"),filePath);
				return true;
			} else {
				logger.info(messageResourceBundle.getLogMessage("subscription.file.notexist"),filePath);
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public ResponseEntity<SubscribeEntry> viewSubscribeEntry(int id, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}
		String target = IConstants.FAILURE_KEY;
		logger.info(messageResourceBundle.getLogMessage("subscription.view.req"),id,user.getMasterId());
		SubscribeEntry entry = null;
		try {
			Optional<SubscribeEntry> optionalEntry = this.subscribeEntryRepository.findById(id);
			if (optionalEntry.isPresent()) {
				entry = optionalEntry.get();
			} else {
				logger.error(messageResourceBundle.getLogMessage("subscription.msg.noentry"),user.getMasterId());
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SUBSCRIPTION_MSG_NOTFOUND));
			}
			Converters convert = new Converters();
			entry.setMessage(convert.HexCodePointsToCharMsg(entry.getMessage()));
			entry.setOrigMessage(convert.HexCodePointsToCharMsg(entry.getOrigMessage()));
			logger.info(messageResourceBundle.getLogMessage("subscription.msg.found"),user.getMasterId(),entry.getUsername());
			target = IConstants.SUCCESS_KEY;
		} catch (NotFoundException e) {
			logger.error(messageResourceBundle.getLogMessage("subscription.msg.error"),e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("subscription.msg.error"),e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SUBSCRIPTION_MSG_ERROR,new Object[] {e.getMessage()}));
		}
		return ResponseEntity.ok(entry);
	}

	@Override
	public ResponseEntity<List<SubscribeEntry>> listSubscribeEntry(String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}
		String target = IConstants.FAILURE_KEY;
		String masterId = user.getMasterId();
		logger.info(messageResourceBundle.getLogMessage("subscription.req.list"),masterId);
		List<SubscribeEntry> listEntry = null;
		try {
			listEntry = this.subscribeEntryRepository.findByCreatedBy(masterId);
			if (listEntry.isEmpty()) {
				logger.info(messageResourceBundle.getLogMessage("subscription.msg.noentry"),masterId);
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SUBSCRIPTION_MSG_NOTFOUND ));
			} else {
				logger.info(messageResourceBundle.getLogMessage("subscription.entries"),masterId,listEntry.size());
				target = IConstants.SUCCESS_KEY;
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("subscription.msg.error"),e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SUBSCRIPTION_MSG_ERROR,new Object[] {e.getMessage()}));
		}
		return ResponseEntity.ok(listEntry);
	}

	@Override
	public ResponseEntity<?> updateSubscribe(String request, MultipartFile headerFile, MultipartFile footerFile,
			String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}

		SubscribeEntryForm form;

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			form = objectMapper.readValue(request, SubscribeEntryForm.class);
			form.setHeaderFile(headerFile);
			form.setFooterFile(footerFile);
		} catch (JsonProcessingException e) {
			throw new JsonProcessingError(messageResourceBundle.getExMessage(ConstantMessages.JSON_PROCESSING_ERROR,new Object[] {e.getMessage()}));
		} catch (Exception ex) {
			throw new InternalServerException(ex.getLocalizedMessage());
		}

		String masterId = user.getMasterId();
		String target = IConstants.FAILURE_KEY;
		String header_file_name = null;
		String footer_file_name = null;
		logger.info(messageResourceBundle.getLogMessage("subscription.req.update"),form.getId(),masterId);
		SubscribeEntry entry = this.subscribeEntryRepository.findById(form.getId())
				.orElseThrow(() -> new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SUBSCRIPTION_MSG_NOTFOUND)));
		try {
			entry.setId(form.getId());
			if (form.getMessage() != null && form.getMessage().length() > 0) {
				if (form.getMessageType().equalsIgnoreCase("7bit")) {
					if (getAsciiList(form.getMessage()) != null) {
						entry.setOrigMessage(new Converters().UTF16(form.getMessage()));
						String hexmsg = SevenBitChar.getHexValue(getAsciiList(form.getMessage()));
						System.out.println("hex: " + hexmsg);
						String content = Converter.getContent(hexmsg.toCharArray());
						System.out.println("content: " + content);
						if (hexmsg != null) {
							entry.setMessage(new Converters().UTF16(content));
							System.out.println(entry.getMessage());
						}
					}
				} else {
					entry.setOrigMessage(new Converters().UTF16(form.getMessage()));
					entry.setMessage(getHexValue(form.getMessage()));
				}
				entry.setMessageType(form.getMessageType());
			}
			if (form.getHeaderFile() != null && form.getHeaderFile().getOriginalFilename() != null
					&& form.getHeaderFile().getOriginalFilename().length() > 0) {
				logger.info(messageResourceBundle.getLogMessage("subscription.header.upload"),masterId,form.getHeaderFile().getOriginalFilename());
				String header_file_ext = form.getHeaderFile().getOriginalFilename()
						.substring(form.getHeaderFile().getOriginalFilename().lastIndexOf(".") + 1);
				if (form.getUsername().length() > 4) {
					header_file_name = form.getUsername().substring(0, 4);
				} else {
					header_file_name = form.getUsername();
				}
				header_file_name += "h" + new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date()) + "."
						+ header_file_ext;
				try {
					if (writeToFile(header_file_name, form.getHeaderFile())) {
						logger.info(messageResourceBundle.getLogMessage("subscription.header.created"),masterId,header_file_name);
					} else {
						header_file_name = null;
					}
				} catch (Exception ex) {
					logger.error(masterId + "-" + header_file_name, ex);
					header_file_name = null;
				}
				if(header_file_name!=null) {
					if(entry.getHeaderFileName()!=null && entry.getHeaderFileName().length() > 0) {
						deleteFile(entry.getHeaderFileName());
					}
				}
				entry.setHeaderFileName(header_file_name);
			}
			if (form.getFooterFile() != null && form.getFooterFile().getOriginalFilename() != null
					&& form.getFooterFile().getOriginalFilename().length() > 0) {
				logger.info(messageResourceBundle.getLogMessage("subscription.footer.upload"),masterId,form.getFooterFile().getOriginalFilename());
				String footer_file_ext = form.getFooterFile().getOriginalFilename()
						.substring(form.getFooterFile().getOriginalFilename().lastIndexOf(".") + 1);
				if (form.getUsername().length() > 4) {
					footer_file_name = form.getUsername().substring(0, 4);
				} else {
					footer_file_name = form.getUsername();
				}
				footer_file_name += "f" + new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date()) + "."
						+ footer_file_ext;
				try {
					if (writeToFile(footer_file_name, form.getFooterFile())) {
						logger.info(messageResourceBundle.getLogMessage("subscription.footer.created"),masterId,footer_file_name);
					} else {
						footer_file_name = null;
					}
				} catch (Exception ex) {
					logger.error(masterId + "-" + footer_file_name, ex);
					footer_file_name = null;
				}
				if(footer_file_name != null) {
					if(entry.getFooterFileName()!=null && entry.getFooterFileName().length() > 0) {
						deleteFile(entry.getFooterFileName());
					}
				}
				entry.setFooterFileName(footer_file_name);
			}
			entry.setSender(form.getSender());
			if (form.getCountryCode() != null) {
				try {
					Integer.parseInt(form.getCountryCode());
					entry.setCountryCode(form.getCountryCode());
				} catch (Exception e) {
					logger.error(messageResourceBundle.getLogMessage("subscription.invalid.cc"),masterId,form.getCountryCode());
					// throw new InternalServerException(masterId + " Invalid Country Code: " +
					// form.getCountryCode());
				}
			}
			SubscribeEntry updatedEntry = this.subscribeEntryRepository.save(entry);
			if (updatedEntry != null) {
				logger.info(messageResourceBundle.getLogMessage("subscription.update.success"));
				target = IConstants.SUCCESS_KEY;
			} else {
				logger.error(messageResourceBundle.getLogMessage("subscription.update.failed"));
				throw new InternalServerException("Unable to update SubscribeEntry!");
			}
		} catch (NotFoundException ex) {
			if (form.getHeaderFile() != null && form.getFooterFile() != null) {
				deleteFile(header_file_name);
				deleteFile(footer_file_name);
			}
			logger.error(masterId, ex.getLocalizedMessage());
			throw new NotFoundException(ex.getLocalizedMessage());
		} catch (Exception ex) {
			if (form.getHeaderFile() != null && form.getFooterFile() != null) {
				deleteFile(header_file_name);
				deleteFile(footer_file_name);
			}
			
			logger.error(messageResourceBundle.getLogMessage("subscription.msg.error"),ex.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SUBSCRIPTION_MSG_ERROR,new Object[] {ex.getMessage()}));
		}

		return new ResponseEntity<>(messageResourceBundle.getMessage(ConstantMessages.SUBSCRIPTION_UPDATE_SUCCESS), HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> deleteSubscribe(int id, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}
		String target = IConstants.FAILURE_KEY;
		logger.info(messageResourceBundle.getLogMessage("subscription.req.delete"),id,user.getMasterId());
		try {
			SubscribeEntry entry = this.subscribeEntryRepository.findById(id)
					.orElseThrow(() -> new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SUBSCRIPTION_MSG_NOTFOUND)));
			if (entry.getHeaderFileName() != null && entry.getHeaderFileName().length() > 0) {
				deleteFile(entry.getHeaderFileName());
			}
			if (entry.getFooterFileName() != null && entry.getFooterFileName().length() > 0) {
				deleteFile(entry.getFooterFileName());
			}
			this.subscribeEntryRepository.deleteById(id);
			logger.info(messageResourceBundle.getLogMessage("subscription.delete.success"));
			target = IConstants.SUCCESS_KEY;
		} catch (NotFoundException ex) {
			logger.error(user.getMasterId(), ex.getMessage());
			throw new NotFoundException(ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error(messageResourceBundle.getLogMessage("subscription.msg.error"),ex.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SUBSCRIPTION_MSG_ERROR,new Object[] {ex.getMessage()}));
		}
		return new ResponseEntity<>(messageResourceBundle.getMessage(ConstantMessages.SUBSCRIPTION_DELETE_SUCCESS), HttpStatus.OK);
	}

}
