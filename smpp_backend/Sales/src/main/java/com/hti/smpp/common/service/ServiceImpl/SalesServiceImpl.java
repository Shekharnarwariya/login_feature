package com.hti.smpp.common.service.ServiceImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.login.dto.Role;
import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.UserRepository;
import com.hti.smpp.common.request.SalesEntryForm;
import com.hti.smpp.common.response.ViewSalesEntry;
import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.sales.repository.SalesRepository;
import com.hti.smpp.common.service.SalesService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;

@Service
public class SalesServiceImpl implements SalesService {

	private final Logger logger = LoggerFactory.getLogger(SalesServiceImpl.class);

	@Autowired
	private SalesRepository salesRepository;

	@Autowired
	private UserRepository userLoginRepo;

	@Autowired
	private UserEntryRepository userRepository;

	@Override
	public String save(SalesEntryForm salesEntryForm, String username) {
		String target = IConstants.FAILURE_KEY;
		SalesEntry entry = new SalesEntry();
		// Finding the user by system ID
		String systemId = null;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found");
		}

		Optional<User> user = userLoginRepo.findBySystemId(systemId);
		Set<Role> role = new HashSet<>();
		if (user.isPresent()) {
			role = user.get().getRoles();
		} else {
			throw new NotFoundException("User not found");
		}
		logger.info("Sales User[" + salesEntryForm.getUsername() + "-" + salesEntryForm.getRole()
				+ "] Add Requested By " + systemId + "[" + role + "]");
		try {
			BeanUtils.copyProperties(salesEntryForm, entry);
			if (GlobalVars.UserMapping.containsKey(entry.getUsername())) {
				logger.error("error.record.duplicate");
				logger.error("Usermaster Entry Exists With same Username " + entry.getUsername());
			} else {
				SalesEntry sales = salesRepository.save(entry);
				int id = sales.getId();
				if (id > 0) {
					GlobalVars.ExecutiveEntryMap.put(id, entry);
					logger.info("Sales User Created: " + entry);
					target = IConstants.SUCCESS_KEY;
				} else {
					logger.error("Unable to create " + entry);
				}
			}
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error("", ex.fillInStackTrace());
		}

		return target;
	}

	@Override
	public String update(SalesEntryForm form, String username) {
		String target = IConstants.FAILURE_KEY;
		// Finding the user by system ID
		String systemId = null;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found");
		}

		Optional<User> user = userLoginRepo.findBySystemId(systemId);
		Set<Role> role = new HashSet<>();
		if (user.isPresent()) {
			role = user.get().getRoles();
		} else {
			throw new NotFoundException("User not found");
		}
		logger.info("Executive[" + form.getId() + "] Update Requested By " + systemId + "[" + role + "]");
		SalesEntry seller = new SalesEntry();
		try {
			BeanUtils.copyProperties(form, seller);
			logger.info("UpdateRequested: " + seller);
			SalesEntry entry = this.salesRepository.findById(seller.getId())
					.orElseThrow(() -> new NotFoundException("Entry not found."));
			this.salesRepository.save(entry);
			GlobalVars.ExecutiveEntryMap.put(seller.getId(), seller);
			target = IConstants.SUCCESS_KEY;
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]");
		}
		return target;
	}

	@Override
	public String delete(int id, String username) {
		logger.info("Delete Requested for Sale ID: " + id);
		String target = IConstants.FAILURE_KEY;
		try {
			this.salesRepository.deleteById(id);
			target = IConstants.SUCCESS_KEY;
			logger.info("message.operation.success");
		} catch (Exception ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]");
		}
		return target;
	}

	private Map<Integer, SalesEntry> list() {
		Map<Integer, SalesEntry> map = new HashMap<Integer, SalesEntry>();
		List<SalesEntry> list = this.salesRepository.findAll();
		for (SalesEntry entry : list) {
			map.put(entry.getId(), entry);
		}
		return map;
	}

	public Map<Integer, SalesEntry> list(String role) {
		Map<Integer, SalesEntry> map = new HashMap<Integer, SalesEntry>();
		List<SalesEntry> list = this.salesRepository.findAll();
		for (SalesEntry entry : list) {
			if (entry.getRole().equalsIgnoreCase(role)) {
				map.put(entry.getId(), entry);
			}
		}
		return map;
	}

	private Map<Integer, SalesEntry> listSellersUnderManager(String mgrId, String role) {
		Map<Integer, SalesEntry> map = new HashMap<Integer, SalesEntry>();
		List<SalesEntry> list = this.salesRepository.findByMasterIdAndRole(mgrId, role);
		for (SalesEntry entry : list) {
			map.put(entry.getId(), entry);
		}
		return map;
	}

	@Override
	public Collection<SalesEntry> listSalesUsers(String username) {
		String target = IConstants.FAILURE_KEY;
		String masterId = null;
		String systemId = null;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			masterId = userOptional.get().getMasterId();
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found");
		}
		Optional<User> user = userLoginRepo.findBySystemId(systemId);
		Set<Role> role = new HashSet<>();
		if (user.isPresent()) {
			role = user.get().getRoles();
		} else {
			throw new NotFoundException("User not found");
		}
		logger.info("Executive List Requested By " + masterId + "[" + role + "]");
		SalesEntry salesEntry = this.salesRepository.findByMasterId(masterId);
		Collection<SalesEntry> salesList = null;
		try {
			if (Access.isAuthorizedSuperAdminAndSystem(role)) {
				salesList = list().values();
			} else if (salesEntry.getRole().equalsIgnoreCase("manager") || Access.isAuthorizedAdmin(role)) {
				salesList = listSellersUnderManager(masterId, "seller").values();
			}

			if (salesList != null && !salesList.isEmpty()) {
				target = IConstants.SUCCESS_KEY;
				logger.info("Executives Under [" + masterId + "] : " + salesList.size());
			} else {
				target = IConstants.FAILURE_KEY;
				logger.info("No Executive Found Under " + masterId + "[" + role + "]"+"|"+target);
			}

		} catch (Exception e) {
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]");
			logger.error(masterId, e.toString());
		}
		return salesList;
	}

	@Override
	public ResponseEntity<?> viewSalesEntry(int id, String username) {
		String target = IConstants.FAILURE_KEY;
		// Finding the user by system ID
		String systemId = null;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found");
		}

		Optional<User> user = userLoginRepo.findBySystemId(systemId);
		Set<Role> role = new HashSet<>();
		if (user.isPresent()) {
			role = user.get().getRoles();
		} else {
			throw new NotFoundException("User not found");
		}
		logger.info("Executive[" + id + "] Details Requested By " + systemId + "[" + role + "]");
		ViewSalesEntry response = new ViewSalesEntry();
		try {
			SalesEntry seller = null;
			Optional<SalesEntry> salesOptional = this.salesRepository.findById(id);
			if (salesOptional.isPresent()) {
				seller = salesOptional.get();
			} else {
				throw new NotFoundException("SalesEntry not found");
			}
			logger.info("Requested: " + seller);
			if (seller != null) {
				if (Access.isAuthorizedSuperAdminAndSystem(role)) {
					Collection<SalesEntry> list = list("manager").values();
					response.setManagers(list);
				}
				response.setSeller(seller);
				target = IConstants.SUCCESS_KEY;
			} else {
				logger.error("error.processError");
			}
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]");
			return new ResponseEntity<>(target, HttpStatus.BAD_GATEWAY);
		}
	}

	@Override
	public Collection<SalesEntry> setupSalesEntry(String username) {
		String target = IConstants.SUCCESS_KEY;
		// Finding the user by system ID
		String systemId = null;
		String masterId = null;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
			masterId = userOptional.get().getMasterId();
		} else {
			throw new NotFoundException("UserEntry not found");
		}

		Optional<User> user = userLoginRepo.findBySystemId(systemId);
		Set<Role> role = new HashSet<>();
		if (user.isPresent()) {
			role = user.get().getRoles();
		} else {
			throw new NotFoundException("User not found");
		}
		logger.info("Sales User Setup Requested By " + systemId + "["
				+ role + "]");
		SalesEntry salesEntry = this.salesRepository.findByMasterId(masterId);
		Collection<SalesEntry> list = null;
		if(Access.isAuthorizedSuperAdminAndSystem(role)) {
			list = list("manager").values();
		}else {
			if(salesEntry.getRole().equalsIgnoreCase("manager") || Access.isAuthorizedAdmin(role)) {
				logger.info("Authorized User :" + systemId);
			}else {
				target = "invalidRequest";
				logger.info("Authorization Failed :" + systemId + ": "+target);
			}
		}
		return list;
	}

}
