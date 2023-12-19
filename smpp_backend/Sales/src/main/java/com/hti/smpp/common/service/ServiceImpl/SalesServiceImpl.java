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

import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
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
	public ResponseEntity<String> save(SalesEntryForm salesEntryForm, String username) {
		
		Optional<User> user = userLoginRepo.findBySystemId(username);
		Set<Role> role = new HashSet<>();
		if (user.isPresent()) {
			if(!Access.isAuthorizedAll(user.get().getRoles())) {
				throw new UnauthorizedException("User Does'nt have Required Access.");
			}
			role = user.get().getRoles();
		} else {
			throw new NotFoundException("User not found");
		}
		
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

		logger.info("Sales User [{}-{}] Add Requested By {} [{}]", salesEntryForm.getUsername(),
				salesEntryForm.getRole(), systemId, role);
		try {
			BeanUtils.copyProperties(salesEntryForm, entry);
			if (GlobalVars.UserMapping.containsKey(entry.getUsername())) {
				logger.warn("Usermaster Entry Exists With same Username " + entry.getUsername());
			} else {
				SalesEntry sales = salesRepository.save(entry);
				int id = sales.getId();
				if (id > 0) {
					GlobalVars.ExecutiveEntryMap.put(id, entry);
					logger.info("Sales User Created: " + entry);
					target = IConstants.SUCCESS_KEY;
				} else {
					logger.error("Unable to create Sales User: {}", entry);
					throw new InternalServerException("Unable to create Sales User");
				}
			}
		} catch (InternalServerException ex) {
			logger.error("Process Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(ex.getLocalizedMessage());
			throw new InternalServerException(ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Unexpected Error: " + ex.getMessage() + "[" + ex.getCause() + "]", false);
			logger.error(ex.getLocalizedMessage());
			throw new InternalServerException(ex.getLocalizedMessage());
		}

		return new ResponseEntity<String>(target, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<String> update(SalesEntryForm form, String username) {
		Optional<User> user = userLoginRepo.findBySystemId(username);
		Set<Role> role = new HashSet<>();
		if (user.isPresent()) {
			if(!Access.isAuthorizedAll(user.get().getRoles())) {
				throw new UnauthorizedException("User Does'nt have Required Access.");
			}
			role = user.get().getRoles();
		} else {
			throw new NotFoundException("User not found");
		}
		String target = IConstants.FAILURE_KEY;
		// Finding the user by system ID
		String systemId = null;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found");
		}
		logger.info("Executive [{}] Update Requested By {} [{}]", form.getId(), systemId, role);
		SalesEntry seller = new SalesEntry();
		try {
			BeanUtils.copyProperties(form, seller);
			logger.info("Update Requested: {}", seller);
			SalesEntry entry = this.salesRepository.findById(seller.getId())
					.orElseThrow(() -> new NotFoundException("Entry not found with id: "+seller.getId()));
			SalesEntry savedEntry = this.salesRepository.save(entry);
			if(savedEntry != null) {
				GlobalVars.ExecutiveEntryMap.put(seller.getId(), seller);
				target = IConstants.SUCCESS_KEY;
			}else {
				logger.error("Error: Unable to save SalesEntry");
				throw new InternalServerException("Unable to save SalesEntry.");
			}
			
		} catch (InternalServerException ex) {
			logger.error("InternalServer Exception: {} [{}]", ex.getMessage(), ex.getCause());
			throw new InternalServerException(ex.getLocalizedMessage());
		} catch (NotFoundException ex) {
			logger.error("NotFound Exception: {} [{}]", ex.getMessage(), ex.getCause());
			throw new NotFoundException(ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.error("Process Error: {} [{}]", ex.getMessage(), ex.getCause());
			throw new InternalServerException("Unexpected Exception: "+ex.getLocalizedMessage());
		}
		return new ResponseEntity<String>(target, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<String> delete(int id, String username) {
		Optional<User> user = userLoginRepo.findBySystemId(username);
		if (user.isPresent()) {
			if(!Access.isAuthorizedAll(user.get().getRoles())) {
				throw new UnauthorizedException("User Does'nt have Required Access.");
			}
		} else {
			throw new NotFoundException("User not found");
		}
		
		logger.info("Delete Requested for Sale ID: " + id);
		String target = IConstants.FAILURE_KEY;
		try {
			this.salesRepository.deleteById(id);
			target = IConstants.SUCCESS_KEY;
			logger.info("Operation successful: Sale ID {}", id);
		} catch (Exception ex) {
			logger.error("Process Error: {} [{}]", ex.getMessage(), ex.getCause());
			throw new InternalServerException(ex.getLocalizedMessage());
		}
		return new ResponseEntity<String>(target, HttpStatus.OK);
	}

	private Map<Integer, SalesEntry> list() {
		Map<Integer, SalesEntry> map = new HashMap<Integer, SalesEntry>();
		List<SalesEntry> list = null;
		try {
			list = this.salesRepository.findAll();
		} catch (Exception e) {
			logger.error("Error: "+e.getLocalizedMessage());
			throw new InternalServerException(e.getLocalizedMessage());
		}
		for (SalesEntry entry : list) {
			map.put(entry.getId(), entry);
		}
		return map;
	}

	public Map<Integer, SalesEntry> list(String role) {
		Map<Integer, SalesEntry> map = new HashMap<Integer, SalesEntry>();
		List<SalesEntry> list = null;
		try {
			list = this.salesRepository.findAll();
		} catch (Exception e) {
			logger.error("Error: "+e.getLocalizedMessage());
			throw new InternalServerException(e.getLocalizedMessage());
		}
		for (SalesEntry entry : list) {
			if (entry.getRole().equalsIgnoreCase(role)) {
				map.put(entry.getId(), entry);
			}
		}
		return map;
	}

	private Map<Integer, SalesEntry> listSellersUnderManager(String mgrId, String role) {
		Map<Integer, SalesEntry> map = new HashMap<Integer, SalesEntry>();
		List<SalesEntry> list = null;
		try {
			list = this.salesRepository.findByMasterIdAndRole(mgrId, role);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			throw new InternalServerException(e.getLocalizedMessage());
		}
		for (SalesEntry entry : list) {
			map.put(entry.getId(), entry);
		}
		return map;
	}

	@Override
	public ResponseEntity<Collection<SalesEntry>> listSalesUsers(String username) {
		
		Optional<User> user = userLoginRepo.findBySystemId(username);
		Set<Role> role = new HashSet<>();
		if (user.isPresent()) {
			if(!Access.isAuthorizedAll(user.get().getRoles())) {
				throw new UnauthorizedException("User Does'nt have Required Access.");
			}
			role = user.get().getRoles();
		} else {
			throw new NotFoundException("User not found");
		}
		
		String target = IConstants.FAILURE_KEY;
		String masterId = null;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			masterId = userOptional.get().getMasterId();
		} else {
			throw new NotFoundException("UserEntry not found");
		}
		logger.info("Executive List Requested By {} [{}]", masterId, role);
		SalesEntry salesEntry = this.salesRepository.findByMasterId(masterId);
		Collection<SalesEntry> salesList = null;
		try {
			if (Access.isAuthorizedSuperAdminAndSystem(role)) {
				try {
					salesList = list().values();
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage());
					throw new InternalServerException(e.getLocalizedMessage());
				}
			} else if (salesEntry.getRole().equalsIgnoreCase("manager") || Access.isAuthorizedAdmin(role)) {
				try {
					salesList = listSellersUnderManager(masterId, "seller").values();
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage());
					throw new InternalServerException(e.getLocalizedMessage());
				}
			}

			if (salesList != null && !salesList.isEmpty()) {
				target = IConstants.SUCCESS_KEY;
				logger.info("Executives Under [" + masterId + "] : " + salesList.size());
			} else {
				target = IConstants.FAILURE_KEY;
				logger.error("No Executive Found Under " + masterId + "[" + role + "]" + "|" + target);
				throw new NotFoundException("No Executive Found Under " + masterId + "[" + role + "]" + "|" + target);
			}

		} catch (NotFoundException e) {
			logger.error("Not Found Exception: " + e.getMessage() + "[" + e.getCause() + "]");
			logger.error(masterId, e.toString());
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (InternalServerException e) {
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]");
			logger.error(masterId, e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]");
			logger.error(masterId, e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}
		return ResponseEntity.ok(salesList);
	}

	@Override
	public ResponseEntity<?> viewSalesEntry(int id, String username) {
		Optional<User> user = userLoginRepo.findBySystemId(username);
		Set<Role> role = new HashSet<>();
		if (user.isPresent()) {
			if(!Access.isAuthorizedSuperAdminAndSystem(user.get().getRoles())) {
				throw new UnauthorizedException("User Does'nt have Required Access.");
			}
			role = user.get().getRoles();
		} else {
			throw new NotFoundException("User not found");
		}
		String target = IConstants.FAILURE_KEY;
		// Finding the user by system ID
		String systemId = null;
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		if (userOptional.isPresent()) {
			systemId = userOptional.get().getSystemId();
		} else {
			throw new NotFoundException("UserEntry not found");
		}
		logger.info("Executive[" + id + "] Details Requested By " + systemId + "[" + role + "]");
		ViewSalesEntry response = new ViewSalesEntry();
		try {
			SalesEntry seller = null;
			Optional<SalesEntry> salesOptional = this.salesRepository.findById(id);
			if (salesOptional.isPresent()) {
				seller = salesOptional.get();
			} else {
				logger.error("SalesEntry not found");
				throw new NotFoundException("SalesEntry not found");
			}
			logger.info("Requested: " + seller);
			if (seller != null) {
				if (Access.isAuthorizedSuperAdminAndSystem(role)) {
					Collection<SalesEntry> list = null;
					try {
						list = list("manager").values();
					} catch (Exception e) {
						logger.error(e.getLocalizedMessage());
						throw new InternalServerException(e.getLocalizedMessage());
					}
					response.setManagers(list);
				}
				response.setSeller(seller);
				target = IConstants.SUCCESS_KEY;
			} else {
				logger.error("Operation failed");
				throw new InternalServerException("View Sales Entry Failed!");
			}
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (InternalServerException e) {
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]");
			throw new InternalServerException(e.getLocalizedMessage());
		} catch (NotFoundException e) {
			logger.error("NotFound Error: " + e.getMessage() + "[" + e.getCause() + "]");
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error("Process Error: " + e.getMessage() + "[" + e.getCause() + "]");
			throw new InternalServerException(e.getLocalizedMessage());
		}
	}

	@Override
	public ResponseEntity<Collection<SalesEntry>> setupSalesEntry(String username) {
		Optional<User> user = userLoginRepo.findBySystemId(username);
		Set<Role> role = new HashSet<>();
		if (user.isPresent()) {
			if(!Access.isAuthorizedAll(user.get().getRoles())) {
				throw new UnauthorizedException("User Does'nt have Required Access.");
			}
			role = user.get().getRoles();
		} else {
			throw new NotFoundException("User not found");
		}
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
		logger.info("Sales User Setup Requested By " + systemId + "[" + role + "]");
		SalesEntry salesEntry = this.salesRepository.findByMasterId(masterId);
		Collection<SalesEntry> list = null;
		if (Access.isAuthorizedSuperAdminAndSystem(role)) {
			try {
				list = list("manager").values();
			} catch (Exception e) {
				logger.error("ERROR " + e.getLocalizedMessage());
				throw new NotFoundException(e.getLocalizedMessage());
			}
		} else {
			if (salesEntry.getRole().equalsIgnoreCase("manager") || Access.isAuthorizedAdmin(role)) {
				logger.info("Authorized User :" + systemId);
			} else {
				target = "invalidRequest";
				logger.error("Authorization Failed: {}: {}", systemId, target);
				throw new UnauthorizedException("Authorization Failed");
			}
		}
		return ResponseEntity.ok(list);
	}

}
