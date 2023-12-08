package com.hti.smpp.common.twoway.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.login.dto.Role;
import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.UserRepository;
import com.hti.smpp.common.twoway.dto.KeywordEntry;
import com.hti.smpp.common.twoway.repository.KeywordEntryRepository;
import com.hti.smpp.common.twoway.request.KeywordEntryForm;
import com.hti.smpp.common.twoway.service.KeywordService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMenuAccessEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMenuAccessEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder.EntryObject;
import com.hazelcast.query.impl.PredicateBuilderImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class KeywordServiceImpl implements KeywordService {

	private Logger logger = LoggerFactory.getLogger(KeywordServiceImpl.class);

	@Autowired
	private UserRepository loginRepo;

	@Autowired
	private WebMenuAccessEntryRepository webMenuRepo;

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private KeywordEntryRepository keywordRepo;

	@Override
	public String addKeyword(KeywordEntryForm entryForm, String username) {

		Optional<User> optionalUser = loginRepo.findBySystemId(username);
		User user = null;
		Set<Role> role = new HashSet<>();
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			role = user.getRoles();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}

		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		int userId = user.getUserId().intValue();
		WebMenuAccessEntry webMenu = null;
		Optional<WebMenuAccessEntry> webEntryOptional = this.webMenuRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException("WebMenuAccessEntry not found.");
		}

		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found.");
		}

		String target = IConstants.FAILURE_KEY;
		logger.info(systemId + "[" + role + "] Setup Keyword Request: " + entryForm.getPrefix());

		try {
			if (Access.isAuthorizedSuperAdminAndSystem(role) || webMenu.isTwoWay()) {
				KeywordEntry entry = new KeywordEntry();
				BeanUtils.copyProperties(entryForm, entry);
				try {
					entry.setCreatedBy(systemId);
					this.keywordRepo.save(entry);
					target = IConstants.SUCCESS_KEY;
					logger.info("message.operation.success");
					MultiUtility.changeFlag(Constants.KEYWORD_FLAG_FILE, "707");
				} catch (Exception e) {
					logger.error(entryForm.getPrefix() + " Keyword Already Exist");
					logger.error("error.record.duplicate");
					throw new InternalServerException(e.toString());
				}
			} else {
				logger.error("Authorization Failed :" + systemId);
				target = "invalidRequest";
			}

		} catch (Exception e) {
			logger.error(systemId, e.fillInStackTrace());
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]");
			throw new InternalServerException(e.toString());
		}
		logger.info(systemId + "[" + role + "] Add Keyword Target: " + target);

		return target;
	}

	public List<KeywordEntry> listKeyWord() {
		List<KeywordEntry> list = null;
		try {
			list = this.keywordRepo.findAll();
		} catch (Exception e) {
			logger.error(e.toString());
			throw new NotFoundException("No KeywordEntry Found.");
		}
		for (KeywordEntry entry : list) {
			if (GlobalVars.UserEntries.containsKey(entry.getUserId())) {
				entry.setSystemId(GlobalVars.UserEntries.get(entry.getUserId()).getSystemId());
			}
		}
		return list;
	}

	public List<KeywordEntry> listKeyWord(Integer[] users) {
		List<KeywordEntry> list = null;
		try {
			list = this.keywordRepo.findByUserIdIn(users);
		} catch (Exception e) {
			logger.error(e.toString());
			throw new NotFoundException("No KeywordEntry Found.");
		}
		for (KeywordEntry entry : list) {
			if (GlobalVars.UserEntries.containsKey(entry.getUserId())) {
				entry.setSystemId(GlobalVars.UserEntries.get(entry.getUserId()).getSystemId());
			}
		}
		return list;
	}

	@Override
	public List<KeywordEntry> listKeyword(String username) {
		String target = IConstants.FAILURE_KEY;
		Optional<User> optionalUser = loginRepo.findBySystemId(username);
		User user = null;
		Set<Role> role = new HashSet<>();
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			role = user.getRoles();
			if (!Access.isAuthorizedSuperAdminAndSystemAndAdmin(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}

		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		int userId = user.getUserId().intValue();
		WebMenuAccessEntry webMenu = null;
		Optional<WebMenuAccessEntry> webEntryOptional = this.webMenuRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException("WebMenuAccessEntry not found.");
		}

		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found.");
		}

		logger.info(systemId + "[" + role + "] List Keyword Request");

		List<KeywordEntry> list = null;
		try {
			if (Access.isAuthorizedSuperAdminAndSystem(role)) {
				list = listKeyWord();
				if (list.isEmpty()) {
					logger.info(systemId + "[" + role + "] Keyword List Empty");
				} else {
					logger.info(systemId + "[" + role + "] Keyword List:" + list.size());
					target = IConstants.SUCCESS_KEY;
					return list;
				}

			} else if (webMenu.isTwoWay()) {
				Integer[] users = null;
				if (Access.isAuthorizedAdmin(role)) {
					Predicate<Integer, UserEntry> p = new PredicateBuilderImpl().getEntryObject().get("masterId")
							.equal(systemId);
					Set<Integer> set = new HashSet<Integer>(GlobalVars.UserEntries.keySet());
					set.add(userOptional.get().getId());
					users = set.toArray(new Integer[0]);
				} else {
					users = new Integer[1];
					users[0] = user.getUserId().intValue();
				}
				list = listKeyWord(users);
				if (list.isEmpty()) {
					logger.info(systemId + "[" + role + "] Keyword List Empty");
				} else {
					logger.info(systemId + "[" + role + "] Keyword List:" + list.size());
					target = IConstants.SUCCESS_KEY;
					return list;
				}

			} else {
				logger.error("Authorization Failed :" + systemId);
				target = "invalidRequest";
			}
		} catch (Exception e) {
			logger.error(systemId, e.toString());
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]");
		}
		logger.info(systemId + "[" + role + "] List Keyword Target: " + target);
		return list;
	}

	@Override
	public String updateKeyword(KeywordEntryForm entryForm, String username) {
		String target = IConstants.FAILURE_KEY;
		Optional<User> optionalUser = loginRepo.findBySystemId(username);
		User user = null;
		Set<Role> role = new HashSet<>();
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			role = user.getRoles();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}

		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		int userId = user.getUserId().intValue();
		WebMenuAccessEntry webMenu = null;
		Optional<WebMenuAccessEntry> webEntryOptional = this.webMenuRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException("WebMenuAccessEntry not found.");
		}

		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found.");
		}
		logger.info(systemId + "[" + role + "] Update Keyword Request: " + entryForm.getId() + "]");

		try {
			if (Access.isAuthorizedSuperAdminAndAdmin(role) || webMenu.isTwoWay()) {
				KeywordEntry entry = new KeywordEntry();
				BeanUtils.copyProperties(entryForm, entry);
				KeywordEntry updateEntry = this.keywordRepo.findById(entry.getId())
						.orElseThrow(() -> new NotFoundException("KeywordEntry not found."));
				this.keywordRepo.save(updateEntry);
				target = IConstants.SUCCESS_KEY;
				MultiUtility.changeFlag(Constants.KEYWORD_FLAG_FILE, "707");
			}else {
				logger.info(systemId + "[" + role + "] Unauthorized Request");
			}
		} catch (Exception e) {
			logger.error(systemId, e.toString());
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]");
			throw new InternalServerException("Process Error: "+e.getLocalizedMessage());
		}
		return target;
	}

	@Override
	public String deleteKeyword(int id, String username) {
		String target = IConstants.FAILURE_KEY;
		
		Optional<User> optionalUser = loginRepo.findBySystemId(username);
		User user = null;
		Set<Role> role = new HashSet<>();
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			role = user.getRoles();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}

		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found.");
		}
		int userId = user.getUserId().intValue();
		WebMenuAccessEntry webMenu = null;
		Optional<WebMenuAccessEntry> webEntryOptional = this.webMenuRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException("WebMenuAccessEntry not found.");
		}
		logger.info(systemId + "[" + role + "] Delete Keyword Request: " + id + "]");
		
		try {
			if(Access.isAuthorizedSuperAdminAndSystem(role) || webMenu.isTwoWay()) {
				this.keywordRepo.deleteById(id);
				target = IConstants.SUCCESS_KEY;
				MultiUtility.changeFlag(Constants.KEYWORD_FLAG_FILE, "707");
			}else {
				logger.info(systemId + "[" + role + "] Unauthorized Request");
			}
		}catch(Exception e) {
			logger.error(systemId, e.toString());
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]");
			throw new InternalServerException("Process Error: "+e.toString());
		}
		
		return target;
	}

	@Override
	public Collection<UserEntry> setupKeyword(String username) {
		String target = IConstants.FAILURE_KEY;
		Optional<User> optionalUser = loginRepo.findBySystemId(username);
		User user = null;
		Set<Role> role = new HashSet<>();
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			role = user.getRoles();
			if (!Access.isAuthorizedSuperAdminAndSystemAndAdmin(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}

		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found.");
		}
		int userId = user.getUserId().intValue();
		WebMenuAccessEntry webMenu = null;
		Optional<WebMenuAccessEntry> webEntryOptional = this.webMenuRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException("WebMenuAccessEntry not found.");
		}
		logger.info(systemId + "[" + role + "] Setup Keyword Request");
		Collection<UserEntry> users = null;
		try {
			if(Access.isAuthorizedSuperAdminAndSystem(role)) {
				if(Access.isAuthorizedSystem(role)) {
					EntryObject e = new PredicateBuilderImpl().getEntryObject();
					Predicate p = e.get("role").in("admin", "user").or(e.get("id").equal(userOptional.get().getId()));
					users = GlobalVars.UserEntries.values(p);
				}else {
					users = GlobalVars.UserEntries.values();
				}
				target = IConstants.SUCCESS_KEY;
				return users;
			}else if(webMenu.isTwoWay()) {
				if(Access.isAuthorizedAdmin(role)) {
					Predicate<Integer, UserEntry> p = new PredicateBuilderImpl().getEntryObject().get("masterId")
							.equal(systemId);
					users = new ArrayList<UserEntry>(GlobalVars.UserEntries.values(p));
					users.add(GlobalVars.UserEntries.get(userOptional.get().getId()));
					return users;
				}
				target = IConstants.SUCCESS_KEY;
			}else {
				logger.error("Authorization Failed :" + systemId);
				target = "invalidRequest";
			}
		}catch(Exception e) {
			logger.error(systemId, e.toString());
		}
		
		return users;
	}
	
	public KeywordEntry getEntry(int id) {
		Optional<KeywordEntry> entryOptional = this.keywordRepo.findById(id);
		KeywordEntry entry = null;
		if(entryOptional.isPresent()) {
			entry = entryOptional.get();
		}else {
			throw new NotFoundException("KeywordEntry not found.");
		}
		if (GlobalVars.UserEntries.containsKey(entry.getUserId())) {
			entry.setSystemId(GlobalVars.UserEntries.get(entry.getUserId()).getSystemId());
		}
		return entry;
	}

	@Override
	public KeywordEntry viewKeyword(int id, String username) {
		String target = IConstants.FAILURE_KEY;
		String systemId = null;
		// Finding the user by system ID
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found.");
		}
		Optional<User> optionalUser = loginRepo.findBySystemId(username);
		
		User user = null;
		Set<Role> role = new HashSet<>();
		if (optionalUser.isPresent()) {
			user = optionalUser.get();
			role = user.getRoles();
			if (!Access.isAuthorizedSuperAdminAndSystem(user.getRoles())) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}

		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		int userId = user.getUserId().intValue();
		WebMenuAccessEntry webMenu = null;
		Optional<WebMenuAccessEntry> webEntryOptional = this.webMenuRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException("WebMenuAccessEntry not found.");
		}
		logger.info(systemId + "[" + role + "] View Keyword Request: " + id);
		KeywordEntry entry = null;
		try {
			if(webMenu.isTwoWay()) {
				entry = getEntry(id);
				target = IConstants.SUCCESS_KEY;
				if(entry!=null) {
					return entry;
				}
			}else {
				logger.error("Authorization Failed :" + systemId);
				target = "invalidRequest";
			}
			
		}catch(Exception e) {
			logger.error(systemId, e.toString());
			throw new InternalServerException(e.toString());
		}
		return entry;
	}

}
