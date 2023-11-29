package com.hti.smpp.common.services;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hazelcast.sql.impl.type.converter.Converters;
import com.hti.exception.DuplicateEntryException;
import com.hti.smpp.common.bsfm.repository.BsfmProfileRepository;
import com.hti.smpp.common.dto.BsfmFilterFrom;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.UserRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.IConstants;
import com.hti.webems.bsfm.BsfmDTO;
import com.hti.webems.database.HtiSmsDB;
import com.hti.webems.database.IDatabaseService;
import com.hti.webems.util.MultiUtility;

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
	private UserRepository loginRepository;

	@Override
	@Transactional
	public void addBsfmProfile(BsfmFilterFrom bsfmFilterFrom, String username) throws Exception {
		Optional<User> optionalUser = loginRepository.findBySystemId(username);
		User user = null;
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String systemid = user.getSystemId();
		BsfmFilterFrom bsfmForm = (BsfmFilterFrom) form;
		logger.info(systemid + "[" + role + "] Add Spam Profile: " + bsfmForm.getProfilename());
		if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system") || role.equalsIgnoreCase("admin")) {
			BsfmDTO bdto = new BsfmDTO();
			BeanUtils.copyProperties(bdto, bsfmForm);
			boolean proceed = true;
			if (role.equalsIgnoreCase("admin")) {
				if (bsfmForm.getUsername() != null && bsfmForm.getUsername().length > 0) {
					bdto.setUsername(String.join(",", bsfmForm.getUsername()));
				} else {
					logger.error(systemid + "[" + role + "]  No User Selected.");
					proceed = false;
				}
			} else {
				if (bsfmForm.getUsername() != null && bsfmForm.getUsername().length > 0) {
					bdto.setUsername(String.join(",", bsfmForm.getUsername()));
				}
			}
			if (proceed) {
				if (bsfmForm.getSenderType() != null && bsfmForm.getSenderType().length() > 0) {
					bdto.setSenderType(new Converters().UTF16(bsfmForm.getSenderType()));
				} else {
					bdto.setSenderType(null);
				}
				if (!bdto.isSchedule()) {
					bdto.setDayTime(null);
				}
				if (bsfmForm.getSmsc() != null && bsfmForm.getSmsc().length > 0) {
					bdto.setSmsc(String.join(",", bsfmForm.getSmsc()));
				}
				if (bsfmForm.isCountryWise()) {
					if (bsfmForm.getMcc() != null && bsfmForm.getMcc().length > 0) {
						Predicate p = new PredicateBuilderImpl().getEntryObject().get("mcc").in(bsfmForm.getMcc());
						Set<Integer> set = com.hti.webems.util.GlobalVars.NetworkEntries.keySet(p);
						String networks = set.stream().map(i -> i.toString()).collect(Collectors.joining(","));
						System.out.println("Networks:" + networks);
						bdto.setNetworks(networks);
					}
				} else {
					if (bsfmForm.getNetworks() != null && bsfmForm.getNetworks().length > 0) {
						bdto.setNetworks(String.join(",", bsfmForm.getNetworks()));
					}
				}
				if (bsfmForm.getContent() != null && bsfmForm.getContent().trim().length() > 0) {
					// StringTokenizer tokens = new StringTokenizer(getContent(), ",");
					String encoded_content = "";
					System.out.println("Content: " + bsfmForm.getContent());
					for (String content_token : bsfmForm.getContent().split(",")) {
						System.out.println("Token: " + content_token);
						try {
							encoded_content += new Converters().UTF16(content_token) + ",";
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (encoded_content.length() > 0) {
						encoded_content = encoded_content.substring(0, encoded_content.length() - 1);
						bdto.setContent(encoded_content);
					} else {
						bdto.setContent(null);
					}
				} else {
					bdto.setContent(null);
				}
				bdto.setMasterId(systemid);
				IDatabaseService dbService = HtiSmsDB.getInstance();
				try {
					int priority = dbService.maxBsfmPriority();
					System.out.println("Bsfmaster Max Priority: " + priority);
					if (bdto.getReroute() != null && bdto.getReroute().trim().length() == 0) {
						bdto.setReroute(null);
					}
					bdto.setPriority(++priority);
					bdto.setEditBy(systemid);
					if (dbService.addNewBsfmProfile(bdto)) {
						message = new ActionMessage("message.operation.success");
						target = IConstants.SUCCESS_KEY;
						String bsfm_flag = MultiUtility.readFlag(Constants.BSFM_FLAG_FILE);
						if (bsfm_flag != null && bsfm_flag.equalsIgnoreCase("100")) {
							MultiUtility.changeFlag(Constants.BSFM_FLAG_FILE, "707");
						}
					} else {
						message = new ActionMessage("message.operation.failed");
					}
				} catch (DuplicateEntryException ex) {
					message = new ActionMessage("error.record.duplicate");
				} catch (Exception ex) {
					message = new ActionMessage("message.operation.failed");
				}
			} else {
				message = new ActionMessage("error.noUserforSelectedmode");
			}
		} else {
			logger.info(systemid + "[" + role + "] Unauthorized Request");
			message = new ActionMessage("error.unauthorized");
		}
		messages.add(ActionMessages.GLOBAL_MESSAGE, message);
		saveMessages(request, messages);
		return mapping.findForward(target);
	}

//	@Override
//	public List<Bsfm> showBsfmProfile(String masterId) {
//		List<Bsfm> profiles = this.bsfmRepo.findByMasterIdOrderByPriority(masterId);
//		return profiles;
//	}
//
//	@Override
//	@Transactional
//	public void updateBsfmProfile(BsfmDto bsfm, String username) throws Exception {
//		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
//		String systemid = null;
//		if (userOptional.isPresent()) {
//			systemid = userOptional.get().getSystemId();
//		}
//		Bsfm bsfmEntity = this.bsfmRepo.findByUsername(username);
//
//		logger.info("SystemId: " + systemid + "Update Spam Profile: " + bsfm.getProfilename());
//
//		boolean proceed = true;
//
//		if (bsfm.getUsername() != null && bsfm.getUsername().length() > 0) {
//			bsfmEntity.setUsername(String.join(",", bsfm.getUsername()));
//		} else {
//
//			proceed = false;
//		}
//
//		if (proceed) {
//
//			if (bsfm.getSenderType() != null && bsfm.getSenderType().length() > 0) {
//				bsfmEntity.setSenderType(new Converters().UTF16(bsfm.getSenderType()));
//			} else {
//				bsfmEntity.setSenderType(null);
//			}
//
//			if (bsfm.isSchedule()) {
//				if (bsfm.getDayTime() == null || bsfm.getDayTime().length() < 19) {
//					logger.info(bsfm.getProfilename() + " Invalid DayTime Configured: " + bsfm.getDayTime());
//					bsfmEntity.setSchedule(false);
//				}
//
//			}
//			bsfmEntity.setEditBy(systemid);
//			if (bsfm.getSmsc() != null && bsfm.getSmsc().length() > 0) {
//				bsfmEntity.setSmsc(String.join(",", bsfm.getSmsc()));
//			}
//
//			if (bsfm.getNetworks() != null && bsfm.getNetworks().length() > 0) {
//				bsfmEntity.setNetworks(String.join(",", bsfm.getNetworks()));
//			}
//
//			if (bsfm.getContent() != null && bsfm.getContent().trim().length() > 0) {
//				String encoded_content = "";
//				logger.info("Content: " + bsfm.getContent());
//				for (String content_token : bsfm.getContent().split(",")) {
//					logger.info("Token: " + content_token);
//					try {
//						encoded_content += new Converters().UTF16(content_token) + ",";
//					} catch (Exception e) {
//						logger.error(e.getMessage());
//					}
//				}
//				if (encoded_content.length() > 0) {
//					encoded_content = encoded_content.substring(0, encoded_content.length() - 1);
//					bsfmEntity.setContent(encoded_content);
//				} else {
//					bsfmEntity.setContent(null);
//				}
//			} else {
//				bsfmEntity.setContent(null);
//			}
//			bsfmEntity.setMasterId(systemid);
//
//			if (bsfm.getReroute() != null && bsfm.getReroute().trim().length() == 0) {
//				bsfmEntity.setReroute(null);
//			}
//			bsfmEntity.setEditBy(systemid);
//
//			boolean isUpdated = false;
//
//			try {
//				this.bsfmRepo.save(bsfmEntity);
//				isUpdated = true;
//
//				if (isUpdated) {
//					String bsfm_flag = FlagUtil.readFlag(hostConfig);
//					if (bsfm_flag != null && bsfm_flag.equalsIgnoreCase("100")) {
//						FlagUtil.changeFlag(hostConfig, FlagStatus.REFRESH);
//					}
//				}
//
//				logger.info("Success Msg: " + successMsg);
//				logger.info("Bsfm entity updated sucessfully.");
//			} catch (Exception e) {
//				logger.error("Error: " + e.getLocalizedMessage());
//				isUpdated = false;
//				throw new Exception("Error: Failed to update Bsfm entity");
//			}
//
//		}
//
//	}
//
//	@Override
//	public void deleteBsfmActiveProfile(String profilename) throws Exception {
//		try {
//			Bsfm bsfm = this.bsfmRepo.findByProfilename(profilename);
//			this.bsfmRepo.delete(bsfm);
//			logger.info("Bsfm Active Profile Deleted.");
//		} catch (Exception e) {
//			logger.error("Error: " + e.getLocalizedMessage());
//			throw new Exception("Error: Failed to delete Bsfm Active Profile.");
//		}
//
//	}
//
//	@Override
//	public boolean updateBsfmProfileFlag(String flagValue) throws Exception {
//
//		String text = "FLAG = " + flagValue;
//		boolean isUpdated = false;
//
//		try {
//			File file = new File(hostConfig);
//
//			try {
//				if (!file.exists()) {
//					System.out.println("file not exist");
//					file.createNewFile();
//
//				}
//			} catch (Exception e1) {
//
//				e1.printStackTrace();
//			}
//
//			try (Writer output = new BufferedWriter(new FileWriter(file))) {
//				output.write(text);
//				isUpdated = true;
//				logger.info("Success: " + successMsg);
//			} catch (IOException e) {
//				logger.error("Error writing to file: " + e.getMessage());
//				isUpdated = false;
//				throw new Exception("Error: Unable to update flag.");
//			}
//		} catch (IOException e) {
//			logger.error("Error opening file: " + e.getMessage());
//			isUpdated = false;
//			throw new Exception("Error: Unable to update flag.");
//		}
//
//		return isUpdated;
//	}
//
//	@Override
//	@Transactional
//	public void bsfmDeleteProfile() throws Exception {
//		int id = 1;
//		Optional<Bsfm> pd = this.bsfmRepo.findById(id);
//		Bsfm profileDetail = pd.get();
//
//		if (profileDetail != null) {
//			profileDetail.setSenderType(new Converters().HexCodePointsToCharMsg(profileDetail.getSenderType()));
//		}
//		List<String> underUserList = null;
//	}

}
