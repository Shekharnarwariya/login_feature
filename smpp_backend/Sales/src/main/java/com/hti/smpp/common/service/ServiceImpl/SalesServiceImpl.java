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
	private UserEntryRepository userRepository;

	
	/**
	 * Saves a sales entry based on the provided form data and username.
	 * Validates user authorization, copies form properties to the SalesEntry model,
	 * and logs relevant information. Handles exceptions and returns a ResponseEntity
	 * with a success or failure message.
	 *
	 * @param salesEntryForm The form data for creating a new sales entry.
	 * @param username The username associated with the request.
	 * @return ResponseEntity with a success or failure message.
	 */
	
	@Override
	public ResponseEntity<String> save(SalesEntryForm salesEntryForm, String username) {
		
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
		SalesEntry entry = new SalesEntry();

		String systemId = user.getSystemId();
	
		logger.info("Sales User [{}-{}] Add Requested By {} [{}]", salesEntryForm.getUsername(),
				salesEntryForm.getRole(), systemId, user.getRole());
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

	/**
	 * Updates a sales entry based on the provided form data and username.
	 * Validates user authorization, copies form properties to the SalesEntry model,
	 * logs relevant information, and handles exceptions. Returns a ResponseEntity
	 * with a success or failure message.
	 *
	 * @param form The form data for updating an existing sales entry.
	
	 */
	
	@Override
	public ResponseEntity<String> update(SalesEntryForm form, String username) {
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
		// Finding the user by system ID
		String systemId = user.getSystemId();
		
		logger.info("Executive [{}] Update Requested By {} [{}]", form.getId(), systemId, user.getRole());
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

	/**
	 * Deletes a sales entry with the specified ID, validating user authorization.
	 * Logs relevant information and handles exceptions. Returns a ResponseEntity
	 * with a success or failure message.
	 *
	 * @param id The ID of the sales entry to be deleted.
	
	 */
	
	@Override
	public ResponseEntity<String> delete(int id, String username) {
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

	
	/**
	 * Retrieves a list of sales entries and maps them to their respective IDs.
	 * Returns a Map<Integer, SalesEntry> containing the sales entries.
	 *
	 * @return Map<Integer, SalesEntry> containing sales entries mapped by their IDs.
	 */
	
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

	/**
	 * Retrieves a list of sales entries filtered by the specified role and maps them to their respective IDs.
	 * Returns a Map<Integer, SalesEntry> containing the filtered sales entries.
	 *
	 * @param role The role by which sales entries are filtered.
	 * @return Map<Integer, SalesEntry> containing filtered sales entries mapped by their IDs.
	 */
	
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

	/**
	 * Retrieves a list of sales entries for sellers under a specific manager and role.
	 * Returns a Map<Integer, SalesEntry> containing the sales entries mapped by their IDs.
	 *
	 * @param mgrId The ID of the manager.
	 * @param role The role of the sales entries to be retrieved.
	 * @return Map<Integer, SalesEntry> containing sales entries mapped by their IDs.
	 */
	
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

	/**
	 * Retrieves a collection of sales entries for executives based on the provided username.
	 * Validates user authorization, logs relevant information, and handles exceptions.
	 * Returns a ResponseEntity with the collection of sales entries or an appropriate response.
	 *
	 * @param username The username associated with the request.
	 * @return ResponseEntity with a collection of sales entries or an appropriate response.
	 */
	
	@Override
	public ResponseEntity<Collection<SalesEntry>> listSalesUsers(String username) {
		
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
		logger.info("Executive List Requested By {} [{}]", masterId, user.getRole());
		SalesEntry salesEntry = this.salesRepository.findByMasterId(masterId);
		Collection<SalesEntry> salesList = null;
		try {
			if (Access.isAuthorized(user.getRole(),"isAuthorizedSuperAdminAndSystem")) {
					salesList = list().values();
			} else if (salesEntry.getRole().equalsIgnoreCase("manager") || Access.isAuthorized(user.getRole(),"isAuthorizedAdmin")) {
					salesList = listSellersUnderManager(masterId, "seller").values();
			}

			if (salesList != null && !salesList.isEmpty()) {
				target = IConstants.SUCCESS_KEY;
				logger.info("Executives Under [" + masterId + "] : " + salesList.size());
			} else {
				target = IConstants.FAILURE_KEY;
				logger.error("No Executive Found Under " + masterId + "[" + user.getRole() + "]" + "|" + target);
				throw new NotFoundException("No Executive Found Under " + masterId + "[" + user.getRole() + "]" + "|" + target);
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

	/**
	 * Retrieves details of a sales entry with the specified ID based on the provided username.
	 * Validates user authorization, logs relevant information, and handles exceptions.
	 * Returns a ResponseEntity with the details of the sales entry or an appropriate response.
	 *
	 * @param id The ID of the sales entry to be viewed.
	 * @param username The username associated with the request.
	 * @return ResponseEntity with details of the sales entry or an appropriate response.
	 */
	
	@Override
	public ResponseEntity<?> viewSalesEntry(int id, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		String target = IConstants.FAILURE_KEY;
		// Finding the user by system ID
		String systemId = user.getSystemId();
		logger.info("Executive[" + id + "] Details Requested By " + systemId + "[" + user.getRole() + "]");
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
				if (Access.isAuthorized(user.getRole(),"isAuthorizedSuperAdminAndSystem")) {
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

	/**
	 * Sets up sales entries based on the provided username.
	 * Validates user authorization, logs relevant information, and handles exceptions.
	 * Returns a ResponseEntity with a collection of sales entries or an appropriate response.
	 *
	 * @param username The username associated with the request.
	 * @return ResponseEntity with a collection of sales entries or an appropriate response.
	 */
	
	@Override
	public ResponseEntity<Collection<SalesEntry>> setupSalesEntry(String username) {
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
		String target = IConstants.SUCCESS_KEY;
		// Finding the user by system ID
		String systemId = user.getSystemId();
		String masterId = user.getMasterId();
		
		logger.info("Sales User Setup Requested By " + systemId + "[" + user.getRole() + "]");
		SalesEntry salesEntry = this.salesRepository.findByMasterId(masterId);
		Collection<SalesEntry> list = null;
		if (Access.isAuthorized(user.getRole(),"isAuthorizedSuperAdminAndSystem")) {
			try {
				list = list("manager").values();
			} catch (Exception e) {
				logger.error("ERROR " + e.getLocalizedMessage());
				throw new NotFoundException(e.getLocalizedMessage());
			}
		} else {
			if (salesEntry.getRole().equalsIgnoreCase("manager") || Access.isAuthorized(user.getRole(),"isAuthorizedAdmin")) {
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
