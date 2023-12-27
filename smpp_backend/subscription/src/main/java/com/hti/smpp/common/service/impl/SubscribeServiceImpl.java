package com.hti.smpp.common.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.hti.smpp.common.util.Converter;
import com.hti.smpp.common.util.Converters;
import com.hti.smpp.common.util.IConstants;
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

	private static final Logger logger = LoggerFactory.getLogger(SubscribeServiceImpl.class);
  
  
	@Override
	public ResponseEntity<?> saveSubscribe(String request, MultipartFile headerFile, MultipartFile footerFile, String username) {
    // Validate user authorization
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

		// Initialize variables
		SubscribeEntryForm form;
		
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			form = objectMapper.readValue(request, SubscribeEntryForm.class);
			form.setHeaderFile(headerFile);
			form.setFooterFile(footerFile);
		} catch (JsonProcessingException e) {
			throw new JsonProcessingError("JsonProccessingError: " + e.getLocalizedMessage());
		} catch (Exception ex) {
			throw new InternalServerException("Error: " + ex.getLocalizedMessage());
		}
		String target = IConstants.FAILURE_KEY;
		String masterId = user.getSystemId();
		logger.info("Add Subcription page Request By " + masterId + " Name: " + form.getPageName() + " Sender: "
				+ form.getSender());
		SubscribeEntry entry = new SubscribeEntry();
		try {
			// Process message content
			if (form.getMessage() != null && form.getMessage().length() > 0) {
				if (form.getMessageType().equalsIgnoreCase("7bit")) {
					if (form.getAsciiList() != null) {
						entry.setOrigMessage(new Converters().UTF16(form.getMessage()));
						String hexmsg = SevenBitChar.getHexValue(form.getAsciiList());
						logger.info("hex: " + hexmsg);
						String content = Converter.getContent(hexmsg.toCharArray());
						logger.info("content: " + content);
						if (hexmsg != null) {
							entry.setMessage(new Converters().UTF16(content));
							logger.info(entry.getMessage());
						}
					}
				} else {
					entry.setOrigMessage(form.getMessage());
					entry.setMessage(form.getMessage());
				}
				entry.setMessageType(form.getMessageType());
			}
			 // Process header file
			if (form.getHeaderFile() != null && form.getHeaderFile().getName() != null
					&& form.getHeaderFile().getName().length() > 0) {
				logger.info(masterId + " Header File Uploaded: " + form.getHeaderFile().getName());
				String header_file_name = null;
				String header_file_ext = form.getHeaderFile().getName()
						.substring(form.getHeaderFile().getName().lastIndexOf(".") + 1);
				if (form.getUsername().length() > 4) {
					header_file_name = form.getUsername().substring(0, 4);
				} else {
					header_file_name = form.getUsername();
				}
				header_file_name += "h" + new java.text.SimpleDateFormat("yyMMddHHmmssSSS").format(new Date()) + "."
						+ header_file_ext;
				try {
					if (writeToFile(header_file_name, form.getHeaderFile().getInputStream())) {
						logger.info(masterId + " Header File Created: " + header_file_name);
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
			if (form.getFooterFile() != null && form.getFooterFile().getName() != null
					&& form.getFooterFile().getName().length() > 0) {
				System.out.println(masterId + " Footer File Uploaded: " + form.getFooterFile().getName());
				String footer_file_name = null;
				String footer_file_ext = form.getFooterFile().getName()
						.substring(form.getFooterFile().getName().lastIndexOf(".") + 1);
				if (form.getUsername().length() > 4) {
					footer_file_name = form.getUsername().substring(0, 4);
				} else {
					footer_file_name = form.getUsername();
				}
				footer_file_name += "f" + new java.text.SimpleDateFormat("yyMMddHHmmssSSS").format(new Date()) + "."
						+ footer_file_ext;
				try {
					if (writeToFile(footer_file_name, form.getFooterFile().getInputStream())) {
						logger.info(masterId + " Footer File Created: " + footer_file_name);
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
					logger.error(masterId + " Invalid Country Code: " + form.getCountryCode());
					throw new InternalServerException(masterId + " Invalid Country Code: " + form.getCountryCode());
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
					logger.info("message.operation.success");
					target = IConstants.SUCCESS_KEY;
				} else {
					logger.error("error: process error");
					throw new InternalServerException("process error");
				}
			} else {
				logger.error("Process Error: Contact Group not created", false);
				throw new InternalServerException("Process Error: Contact Group not created");
			}
		} catch (InternalServerException ex) {
			logger.error(masterId, ex.fillInStackTrace());
			throw new InternalServerException("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]" + false);
		} catch (Exception ex) {
			logger.error(masterId, ex.fillInStackTrace());
			throw new InternalServerException("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]" + false);
		}
		return new ResponseEntity<>(target, HttpStatus.CREATED);
	}
	 // Utility method to write InputStream to a file
	private boolean writeToFile(String fileName, InputStream stream) throws IOException, FileNotFoundException {
		OutputStream bos = null;
		int bytesRead = 0;
		try {
			File dir = new File(IConstants.WEBAPP_DIR + "images//subscription");
			if (!dir.exists()) {
				if (dir.mkdir()) {
					logger.info("Subscription Dir Created");
				} else {
					logger.error("Subscription Dir Creation Failed");
					return false;
				}
			}
			bos = new FileOutputStream(IConstants.WEBAPP_DIR + "images//subscription//" + fileName);
			byte[] buffer = new byte[8192];
			while ((bytesRead = stream.read(buffer, 0, 8192)) != -1) {
				bos.write(buffer, 0, bytesRead);
			}
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (Exception e) {
					 throw new InternalServerException(e.getLocalizedMessage());
				}
			}
		}
		return true;
	}

	@Override
	public ResponseEntity<SubscribeEntry> viewSubscribeEntry(int id, String username) {
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
		logger.info("View Subcription Page[" + id + "] Request By " + user.getMasterId());
		SubscribeEntry entry = null;
		try {
			Optional<SubscribeEntry> optionalEntry = this.subscribeEntryRepository.findById(id);
			if(optionalEntry.isPresent()) {
				entry = optionalEntry.get();
			}else {
				logger.error(user.getMasterId() + " No Entry found.");
				throw new NotFoundException("Subscribe entry not found!");
			}
			Converters convert = new Converters();
			entry.setMessage(convert.HexCodePointsToCharMsg(entry.getMessage()));
			entry.setOrigMessage(convert.HexCodePointsToCharMsg(entry.getOrigMessage()));
			logger.info(user.getMasterId() + " Subscribe Entry Found: " + entry);
			target = IConstants.SUCCESS_KEY;
		} catch (NotFoundException e) {
			logger.error("NotFoundException: "+e.toString());
			throw new NotFoundException("Exception: "+e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error("Exception: "+e.toString());
			throw new InternalServerException("Exception: "+e.getLocalizedMessage());
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
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String target = IConstants.FAILURE_KEY;
		String masterId = user.getMasterId();
		logger.info("List Subcription page Request By " + masterId);
		List<SubscribeEntry> listEntry = null;
		try {
			listEntry = this.subscribeEntryRepository.findByCreatedBy(masterId);
			if(listEntry.isEmpty()) {
				logger.info(masterId + " No Entry found.");
				throw new NotFoundException(masterId + " No Entry found.");
			}else {
				logger.info(masterId + " Subscribe Entries: " + listEntry.size());
				target = IConstants.SUCCESS_KEY;
			}
		} catch (NotFoundException e) {
			logger.error(e.toString());
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]");
			throw new InternalServerException(e.getLocalizedMessage());
		}
		return ResponseEntity.ok(listEntry);
	}

	@Override
	public ResponseEntity<?> updateSubscribe(String request, MultipartFile headerFile, MultipartFile footerFile, String username) {
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
		
		SubscribeEntryForm form;
		
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			form = objectMapper.readValue(request, SubscribeEntryForm.class);
			form.setHeaderFile(headerFile);
			form.setFooterFile(footerFile);
		} catch (JsonProcessingException e) {
			throw new JsonProcessingError("JsonProccessingError: " + e.getLocalizedMessage());
		} catch (Exception ex) {
			throw new InternalServerException("Error: " + ex.getLocalizedMessage());
		}
		
		String masterId = user.getMasterId();
		String target = IConstants.FAILURE_KEY;
		logger.info("Update Subcription Page[" + form.getId() + "] Request By " + masterId);
		SubscribeEntry entry = this.subscribeEntryRepository.findById(form.getId()).orElseThrow(()-> new NotFoundException("SubscribeEntry Not Found!"));
		try {
			entry.setId(form.getId());
			if (form.getMessage() != null && form.getMessage().length() > 0) {
				if (form.getMessageType().equalsIgnoreCase("7bit")) {
					if (form.getAsciiList() != null) {
						entry.setOrigMessage(new Converters().UTF16(form.getMessage()));
						String hexmsg = SevenBitChar.getHexValue(form.getAsciiList());
						System.out.println("hex: " + hexmsg);
						String content = Converter.getContent(hexmsg.toCharArray());
						System.out.println("content: " + content);
						if (hexmsg != null) {
							entry.setMessage(new Converters().UTF16(content));
							System.out.println(entry.getMessage());
						}
					}
				} else {
					entry.setOrigMessage(form.getMessage());
					entry.setMessage(form.getMessage());
				}
				entry.setMessageType(form.getMessageType());
			}
			if (form.getHeaderFile() != null && form.getHeaderFile().getName() != null
					&& form.getHeaderFile().getName().length() > 0) {
				System.out.println(masterId + " Header File Uploaded: " + form.getHeaderFile().getName());
				String header_file_name = null;
				String header_file_ext = form.getHeaderFile().getName()
						.substring(form.getHeaderFile().getName().lastIndexOf(".") + 1);
				if (form.getUsername().length() > 4) {
					header_file_name = form.getUsername().substring(0, 4);
				} else {
					header_file_name = form.getUsername();
				}
				header_file_name += "h" + new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date()) + "."
						+ header_file_ext;
				try {
					if (writeToFile(header_file_name, form.getHeaderFile().getInputStream())) {
						logger.info(masterId + " Header File Created: " + header_file_name);
					} else {
						header_file_name = null;
					}
				} catch (Exception ex) {
					logger.error(masterId + "-" + header_file_name, ex);
					header_file_name = null;
				}
				entry.setHeaderFileName(header_file_name);
			}
			if (form.getFooterFile() != null && form.getFooterFile().getName() != null
					&& form.getFooterFile().getName().length() > 0) {
				System.out.println(masterId + " Footer File Uploaded: " + form.getFooterFile().getName());
				String footer_file_name = null;
				String footer_file_ext = form.getFooterFile().getName()
						.substring(form.getFooterFile().getName().lastIndexOf(".") + 1);
				if (form.getUsername().length() > 4) {
					footer_file_name = form.getUsername().substring(0, 4);
				} else {
					footer_file_name = form.getUsername();
				}
				footer_file_name += "f" + new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date()) + "."
						+ footer_file_ext;
				try {
					if (writeToFile(footer_file_name, form.getFooterFile().getInputStream())) {
						logger.info(masterId + " Footer File Created: " + footer_file_name);
					} else {
						footer_file_name = null;
					}
				} catch (Exception ex) {
					logger.error(masterId + "-" + footer_file_name, ex);
					footer_file_name = null;
				}
				entry.setFooterFileName(footer_file_name);
			}
			entry.setSender(form.getSender());
			if (form.getCountryCode() != null) {
				try {
					Integer.parseInt(form.getCountryCode());
					entry.setCountryCode(form.getCountryCode());
				} catch (Exception e) {
					logger.error(masterId + " Invalid Country Code: " + form.getCountryCode());
					throw new InternalServerException(masterId + " Invalid Country Code: " + form.getCountryCode());
				}
			}
			SubscribeEntry updatedEntry = this.subscribeEntryRepository.save(entry);
			if(updatedEntry!=null) {
				logger.info("message: operation success");
				target = IConstants.SUCCESS_KEY;
			}else {
				logger.error("Exception: Failed Updating SubscribeEntry!");
				throw new InternalServerException("Unable to update SubscribeEntry!");
			}
		} catch (InternalServerException ex) {
			logger.error(masterId, ex.fillInStackTrace());
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			throw new InternalServerException("Exception: "+ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error(masterId, ex.fillInStackTrace());
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			throw new InternalServerException("Exception: "+ex.getLocalizedMessage());
		}
		
		return new ResponseEntity<>(target, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> deleteSubscribe(int id, String username) {
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
		logger.info("Delete Subcription Page[" + id + "] Request By " + user.getMasterId());
		try {
			this.subscribeEntryRepository.deleteById(id);
			logger.info("message: operation success");
			target = IConstants.SUCCESS_KEY;
		} catch (Exception ex) {
			logger.error(user.getMasterId(), ex.fillInStackTrace());
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			throw new InternalServerException("Process Error: "+ex.getLocalizedMessage());
		}
		return new ResponseEntity<>(target, HttpStatus.OK);
	}

}
