package com.hti.smpp.common.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.UserRepository;
import com.hti.smpp.common.request.SubscribeEntryForm;
import com.hti.smpp.common.service.SubscribeService;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.Converter;
import com.hti.smpp.common.util.Converters;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.SevenBitChar;
import com.hti.smpp.common.util.dto.GroupEntrySub;
import com.hti.smpp.common.util.dto.SubscribeEntry;
import com.hti.smpp.common.util.repository.GroupRepository;
import com.hti.smpp.common.util.repository.SubscribeEntryRepository;

public class SubscribeServiceImpl implements SubscribeService {

	@Autowired
	private UserRepository loginRepository;

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private SubscribeEntryRepository subscribeEntryRepository;

	private static final Logger logger = LoggerFactory.getLogger(SubscribeServiceImpl.class);

	@Override
	public ResponseEntity<?> saveSubscribe(SubscribeEntryForm form, String username) {

		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		User user = null;
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			if (!Access.isAuthorizedAll(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String target = IConstants.FAILURE_KEY;
		String masterId = user.getSystemId();
		logger.info("Add Subcription page Request By " + masterId + " Name: " + form.getPageName() + " Sender: "
				+ form.getSender());
		SubscribeEntry entry = new SubscribeEntry();
		try {
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
				}
			}
			GroupEntrySub groupEntry = new GroupEntrySub();
			groupEntry.setName(new Converters().UTF16(form.getPageName()));
			groupEntry.setMasterId(masterId);
			int groupId = groupRepository.save(groupEntry).getId();
			if (groupId > 0) {
				entry.setGroupId(groupId);
				int generatedId = subscribeEntryRepository.save(entry).getId();
				if (generatedId > 0) {
					logger.info("message.operation.success");
					target = IConstants.SUCCESS_KEY;
				} else {
					logger.error("error.processError");
				}
			} else {
				logger.error("Process Error: Contact Group not created", false);
			}
		} catch (Exception ex) {
			logger.error(masterId, ex.fillInStackTrace());
			throw new InternalServerException("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]" + false);
		}
		return new ResponseEntity<>(target, HttpStatus.OK);
	}

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
				}
			}
		}
		return true;
	}

}
