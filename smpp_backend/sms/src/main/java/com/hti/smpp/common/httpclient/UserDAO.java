package com.hti.smpp.common.httpclient;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.user.dto.OTPEntry;
import com.hti.smpp.common.user.dto.RechargeEntry;
import com.hti.smpp.common.user.repository.OtpEntryRepository;
import com.hti.smpp.common.user.repository.RechargeEntryRepository;

import jakarta.transaction.Transactional;

@Service
public class UserDAO {

	private Logger logger = LoggerFactory.getLogger(UserDAO.class);

	@Autowired
	private OtpEntryRepository otpEntryRepository;

	@Autowired
	private RechargeEntryRepository rechargeEntryRepository;

	public OTPEntry getOTPEntry(String systemId) {
		logger.info("Checking of OTPEntry For : " + systemId);
		Optional<OTPEntry> otpOptional = otpEntryRepository.findBySystemId(systemId);

		if (otpOptional.isPresent()) {
			throw new NotFoundException("otp not found ......user Id :{}" + systemId);
		}
		return otpOptional.get();

	}

	public void updateOTPEntry(OTPEntry entry) {
		logger.info("Updating OTPEntry: " + entry);
		try {
			otpEntryRepository.save(entry);
			logger.info("OTPEntry Updated: " + entry);
		} catch (Exception e) {
			logger.error("Error updating OTPEntry: ", e);
			throw new InternalServerException(
					"Error updating OTPEntry for systemId: " + entry.getSystemId() + e.getLocalizedMessage());
		}
	}

	@Transactional
	public int saveOTPEntry(OTPEntry entry) {
		logger.info("Saving OTPEntry: {}", entry);
		try {
			OTPEntry savedEntry = otpEntryRepository.save(entry);
			logger.info("OTPEntry Saved: {}", savedEntry);
			return savedEntry.getId();
		} catch (Exception e) {
			logger.error("Failed to save OTPEntry: ", e);
			throw new InternalServerException("Failed to save OTPEntry" + e.getLocalizedMessage());
		}
	}

	@Transactional
	public int saveRechargeEntry(RechargeEntry entry) {
		logger.info("Saving RechargeEntry: {}", entry);
		try {
			RechargeEntry savedEntry = rechargeEntryRepository.save(entry);
			logger.info("RechargeEntry Added: {}", savedEntry);
			return savedEntry.getId();
		} catch (Exception ex) {
			logger.error("Failed to save RechargeEntry: {}", entry, ex);
			throw new InternalServerException("Failed to save RechargeEntry" + ex.getLocalizedMessage());
		}
	}

}
