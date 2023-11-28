package com.hti.smpp.common.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.bsfm.dto.Bsfm;
import com.hti.smpp.common.bsfm.repository.BsfmProfileRepository;
import com.hti.smpp.common.dto.BsfmDto;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Converters;
import com.hti.smpp.common.util.FlagStatus;
import com.hti.smpp.common.util.FlagUtil;

import jakarta.transaction.Transactional;

@Service
public class BsfmServiceImpl implements BsfmService {

	private static final Logger logger = LoggerFactory.getLogger(BsfmServiceImpl.class.getName());

	@Autowired
	private BsfmProfileRepository bsfmRepo;

	@Value("${flag.file.path}")
	String hostConfig;

	@Value("${load.success}")
	String successMsg;

	@Value("${load.failure}")
	String failureMsg;

	@Autowired
	private UserEntryRepository userRepository;

	private Bsfm convertToEntity(BsfmDto dto) {
		Bsfm entity = new Bsfm();
		entity.setActive(dto.isActive());
		entity.setActiveOnScheduleTime(dto.isActiveOnScheduleTime());
		entity.setContent(dto.getContent());
		entity.setDayTime(dto.getDayTime());
		entity.setEditBy(dto.getEditBy());
		entity.setEditOn(dto.getEditOn());
		entity.setForceSenderId(dto.getForceSenderId());
		entity.setLengthOpr(dto.getLengthOpr());
		entity.setMasterId(dto.getMasterId());
		entity.setMsgLength(dto.getMsgLength());
		entity.setNetworks(dto.getNetworks());
		entity.setPrefixes(dto.getPrefixes());
		entity.setPriority(dto.getPriority());
		entity.setProfilename(dto.getProfilename());
		entity.setReroute(dto.getReroute());
		entity.setRerouteGroupId(dto.getRerouteGroupId());
		entity.setRerouteGroupName(dto.getRerouteGroupName());
		entity.setReverse(dto.isReverse());
		entity.setSchedule(dto.isSchedule());
		entity.setSenderType(dto.getSenderType());
		entity.setSmsc(dto.getSmsc());
		entity.setSourceid(dto.getSourceid());
		entity.setUsername(dto.getUsername());
		return entity;
	}

	@Override
	@Transactional
	public void addBsfmProfile(BsfmDto bsfm, String username) throws Exception {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		String systemid = null;
		if (userOptional.isPresent()) {
			systemid = userOptional.get().getSystemId();
		}

		Bsfm bsfmEntity = convertToEntity(bsfm);

		boolean proceed = true;

		if (bsfm.getUsername() != null && bsfm.getUsername().length() > 0) {
			bsfmEntity.setUsername(String.join(",", bsfm.getUsername()));
		} else {
			proceed = false;
		}

		if (proceed) {

			if (bsfm.getSenderType() != null && bsfm.getSenderType().length() > 0) {
				bsfmEntity.setSenderType(new Converters().UTF16(bsfm.getSenderType()));
			} else {
				bsfmEntity.setSenderType(null);
			}

			if (!bsfm.isSchedule()) {
				bsfmEntity.setDayTime(null);
			}

			if (bsfm.getSmsc() != null && bsfm.getSmsc().length() > 0) {
				bsfmEntity.setSmsc(String.join(",", bsfm.getSmsc()));
			}

			if (bsfm.getNetworks() != null && bsfm.getNetworks().length() > 0) {
				bsfmEntity.setNetworks(String.join(",", bsfm.getNetworks()));
			}

			if (bsfm.getContent() != null && bsfm.getContent().trim().length() > 0) {
				String encoded_content = "";
				logger.info("Content: " + bsfm.getContent());
				for (String content_token : bsfm.getContent().split(",")) {
					logger.info("Token: " + content_token);
					try {
						encoded_content += new Converters().UTF16(content_token) + ",";
					} catch (Exception e) {
						logger.error(e.getMessage());
					}
				}
				if (encoded_content.length() > 0) {
					encoded_content = encoded_content.substring(0, encoded_content.length() - 1);
					bsfmEntity.setContent(encoded_content);
				} else {
					bsfmEntity.setContent(null);
				}
			} else {
				bsfmEntity.setContent(null);
			}
			bsfmEntity.setMasterId(systemid);
			int priority = 0;
			try {
				priority = this.bsfmRepo.findMaxPriority();
			} catch (Exception e) {
				logger.error("Error: Unable to get max priority.");
			}
			if (priority == 0) {
				bsfmEntity.setPriority(bsfm.getPriority());
			} else {
				logger.info("Bsfmaster Max Priority: " + priority);
				bsfmEntity.setPriority(++priority);
			}

			if (bsfm.getReroute() != null && bsfm.getReroute().trim().length() == 0) {
				bsfmEntity.setReroute(null);
			}

			bsfmEntity.setEditBy(systemid);

			boolean isUpdated = false;
			try {
				this.bsfmRepo.save(bsfmEntity);
				logger.info("Bsfm entity saved sucessfully.");
				isUpdated = true;
				if (isUpdated) {
					String bsfm_flag = FlagUtil.readFlag(hostConfig);
					if (bsfm_flag != null && bsfm_flag.equalsIgnoreCase("100")) {
						FlagUtil.changeFlag(hostConfig, FlagStatus.REFRESH);
					}
				}

			} catch (Exception e) {
				logger.error("Error: " + e.getLocalizedMessage());
				throw new Exception("Error: Failed to save Bsfm entity");
			}

		}
	}

	@Override
	public List<Bsfm> showBsfmProfile(String masterId) {
		List<Bsfm> profiles = this.bsfmRepo.findByMasterIdOrderByPriority(masterId);
		return profiles;
	}

	@Override
	@Transactional
	public void updateBsfmProfile(BsfmDto bsfm, String username) throws Exception {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		String systemid = null;
		if (userOptional.isPresent()) {
			systemid = userOptional.get().getSystemId();
		}
		Bsfm bsfmEntity = this.bsfmRepo.findByUsername(username);

		logger.info("SystemId: " + systemid + "Update Spam Profile: " + bsfm.getProfilename());

		boolean proceed = true;

		if (bsfm.getUsername() != null && bsfm.getUsername().length() > 0) {
			bsfmEntity.setUsername(String.join(",", bsfm.getUsername()));
		} else {

			proceed = false;
		}

		if (proceed) {

			if (bsfm.getSenderType() != null && bsfm.getSenderType().length() > 0) {
				bsfmEntity.setSenderType(new Converters().UTF16(bsfm.getSenderType()));
			} else {
				bsfmEntity.setSenderType(null);
			}

			if (bsfm.isSchedule()) {
				if (bsfm.getDayTime() == null || bsfm.getDayTime().length() < 19) {
					logger.info(bsfm.getProfilename() + " Invalid DayTime Configured: " + bsfm.getDayTime());
					bsfmEntity.setSchedule(false);
				}

			}
			bsfmEntity.setEditBy(systemid);
			if (bsfm.getSmsc() != null && bsfm.getSmsc().length() > 0) {
				bsfmEntity.setSmsc(String.join(",", bsfm.getSmsc()));
			}

			if (bsfm.getNetworks() != null && bsfm.getNetworks().length() > 0) {
				bsfmEntity.setNetworks(String.join(",", bsfm.getNetworks()));
			}

			if (bsfm.getContent() != null && bsfm.getContent().trim().length() > 0) {
				String encoded_content = "";
				logger.info("Content: " + bsfm.getContent());
				for (String content_token : bsfm.getContent().split(",")) {
					logger.info("Token: " + content_token);
					try {
						encoded_content += new Converters().UTF16(content_token) + ",";
					} catch (Exception e) {
						logger.error(e.getMessage());
					}
				}
				if (encoded_content.length() > 0) {
					encoded_content = encoded_content.substring(0, encoded_content.length() - 1);
					bsfmEntity.setContent(encoded_content);
				} else {
					bsfmEntity.setContent(null);
				}
			} else {
				bsfmEntity.setContent(null);
			}
			bsfmEntity.setMasterId(systemid);

			if (bsfm.getReroute() != null && bsfm.getReroute().trim().length() == 0) {
				bsfmEntity.setReroute(null);
			}
			bsfmEntity.setEditBy(systemid);

			boolean isUpdated = false;

			try {
				this.bsfmRepo.save(bsfmEntity);
				isUpdated = true;

				if (isUpdated) {
					String bsfm_flag = FlagUtil.readFlag(hostConfig);
					if (bsfm_flag != null && bsfm_flag.equalsIgnoreCase("100")) {
						FlagUtil.changeFlag(hostConfig, FlagStatus.REFRESH);
					}
				}

				logger.info("Success Msg: " + successMsg);
				logger.info("Bsfm entity updated sucessfully.");
			} catch (Exception e) {
				logger.error("Error: " + e.getLocalizedMessage());
				isUpdated = false;
				throw new Exception("Error: Failed to update Bsfm entity");
			}

		}

	}

	@Override
	public void deleteBsfmActiveProfile(String profilename) throws Exception {
		try {
			Bsfm bsfm = this.bsfmRepo.findByProfilename(profilename);
			this.bsfmRepo.delete(bsfm);
			logger.info("Bsfm Active Profile Deleted.");
		} catch (Exception e) {
			logger.error("Error: " + e.getLocalizedMessage());
			throw new Exception("Error: Failed to delete Bsfm Active Profile.");
		}

	}

	@Override
	public boolean updateBsfmProfileFlag(String flagValue) throws Exception {

		String text = "FLAG = " + flagValue;
		boolean isUpdated = false;

		try {
			File file = new File(hostConfig);

			try {
				if (!file.exists()) {
					System.out.println("file not exist");
					file.createNewFile();

				}
			} catch (Exception e1) {

				e1.printStackTrace();
			}

			try (Writer output = new BufferedWriter(new FileWriter(file))) {
				output.write(text);
				isUpdated = true;
				logger.info("Success: " + successMsg);
			} catch (IOException e) {
				logger.error("Error writing to file: " + e.getMessage());
				isUpdated = false;
				throw new Exception("Error: Unable to update flag.");
			}
		} catch (IOException e) {
			logger.error("Error opening file: " + e.getMessage());
			isUpdated = false;
			throw new Exception("Error: Unable to update flag.");
		}

		return isUpdated;
	}

	@Override
	@Transactional
	public void bsfmDeleteProfile() throws Exception {
		int id = 1;
		Optional<Bsfm> pd = this.bsfmRepo.findById(id);
		Bsfm profileDetail = pd.get();

		if (profileDetail != null) {
			profileDetail.setSenderType(new Converters().HexCodePointsToCharMsg(profileDetail.getSenderType()));
		}
		List<String> underUserList = null;
	}

}
