package com.hti.smpp.common.service.ServiceImpl;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.Security.BCryptPasswordEncoder;
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
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;

@Service
public class SalesServiceImpl implements SalesService {

	private final Logger logger = LoggerFactory.getLogger(SalesServiceImpl.class);

	@Autowired
	private SalesRepository salesRepository;

	@Autowired
	private UserEntryRepository userRepository;
	
	@Autowired
	private MessageResourceBundle messageResourceBundle;
	
	@Autowired
	private BCryptPasswordEncoder encoder;

	/**
	 * Saves a sales entry based on the provided form data and username. Validates
	 * user authorization, copies form properties to the SalesEntry model, and logs
	 * relevant information. Handles exceptions and returns a ResponseEntity with a
	 * success or failure message.
	 *
	 * @param salesEntryForm The form data for creating a new sales entry.
	 * @param username       The username associated with the request.
	 * @return ResponseEntity with a success or failure message.
	 */

	@Override
	public ResponseEntity<String> save(SalesEntryForm salesEntryForm, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}
		String target = IConstants.FAILURE_KEY;
		SalesEntry entry = new SalesEntry();
		
		String systemId = user.getSystemId();

		logger.info(messageResourceBundle.getLogMessage("sales.req.add"),salesEntryForm.getUsername(),
				salesEntryForm.getRole(), systemId, user.getRole());
		try {
			BeanUtils.copyProperties(salesEntryForm, entry);
			if (userRepository.existsBySystemId(entry.getUsername())) {
				logger.error(messageResourceBundle.getLogMessage("sales.warn.user"),entry.getUsername());
				throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SALES_USER_EXIST, new Object[] {entry.getUsername()}));
			} else {
				entry.setCreatedOn(LocalDate.now() + "");
				
				entry.setPassword(encoder.encode(salesEntryForm.getPassword()));
				
				SalesEntry sales = salesRepository.save(entry);
				int id = sales.getId();
				if (id > 0) {
					GlobalVars.ExecutiveEntryMap.put(id, entry);
					target = IConstants.SUCCESS_KEY;
					logger.info(messageResourceBundle.getLogMessage("sales.add.success"),id,target);
				} else {
					logger.error(messageResourceBundle.getLogMessage("sales.add.failure"),entry.getUsername());
					throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SALES_ADD_FAILED));
				}

			}
		} catch (DataIntegrityViolationException ex) {
			logger.error(messageResourceBundle.getLogMessage("sales.msg.error"),ex.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SALES_DUPLICATE_USER, new Object[] {entry.getUsername()}));
		} catch (Exception ex) {
			logger.error(messageResourceBundle.getLogMessage("sales.msg.error"),ex.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SALES_MSG_ERROR, new Object[] {ex.getMessage()}));
		}

		return new ResponseEntity<String>(messageResourceBundle.getMessage(ConstantMessages.SALES_ADD_SUCCESS), HttpStatus.CREATED);
	}

	/**
	 * Updates a sales entry based on the provided form data and username. Validates
	 * user authorization, copies form properties to the SalesEntry model, logs
	 * relevant information, and handles exceptions. Returns a ResponseEntity with a
	 * success or failure message.
	 *
	 * @param form The form data for updating an existing sales entry.
	 * 
	 */

	@Override
	public ResponseEntity<String> update(SalesEntryForm form, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}
		String target = IConstants.FAILURE_KEY;
		// Finding the user by system ID
		String systemId = user.getSystemId();

		logger.info(messageResourceBundle.getLogMessage("sales.update.req"), form.getId(), systemId, user.getRole());
		SalesEntry seller = new SalesEntry();
		try {
			BeanUtils.copyProperties(form, seller);
			logger.info(messageResourceBundle.getLogMessage("sales.updateRequest"), seller.getUsername());
			SalesEntry savedEntry = null;
			if (this.salesRepository.existsById(seller.getId())) {
				savedEntry = this.salesRepository.save(seller);
			} else {
				logger.error(messageResourceBundle.getLogMessage("sales.entry.notfound"),seller.getId());
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SALES_ENTRY_NOTFOUND, new Object[] {seller.getId()}));
			}

			if (savedEntry != null) {
				target = IConstants.SUCCESS_KEY;
				logger.info(messageResourceBundle.getLogMessage("sales.update.success"),target);
				GlobalVars.ExecutiveEntryMap.put(seller.getId(), seller);
			} else {
				logger.error(messageResourceBundle.getLogMessage("sales.update.failure"),seller.getUsername());
				throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SALES_UPDATE_FAILED, new Object[] {seller.getUsername()}));
			}

		} catch (NotFoundException ex) {
			throw new NotFoundException(ex.getMessage());
		} catch (Exception ex) {
			logger.error(messageResourceBundle.getLogMessage("sales.msg.error"),ex.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SALES_MSG_ERROR, new Object[] {ex.getMessage()}));
		}
		return new ResponseEntity<String>(messageResourceBundle.getMessage(ConstantMessages.SALES_UPDATE_SUCCESS), HttpStatus.CREATED);
	}

	/**
	 * Deletes a sales entry with the specified ID, validating user authorization.
	 * Logs relevant information and handles exceptions. Returns a ResponseEntity
	 * with a success or failure message.
	 *
	 * @param id The ID of the sales entry to be deleted.
	 * 
	 */

	@Override
	public ResponseEntity<String> delete(int id, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}

		logger.info(messageResourceBundle.getLogMessage("sales.req.delete"),id);
		String target = IConstants.FAILURE_KEY;
		try {
			if (this.salesRepository.existsById(id)) {
				this.salesRepository.deleteById(id);
				target = IConstants.SUCCESS_KEY;
				logger.info(messageResourceBundle.getLogMessage("sales.operation.success"), id);
			} else {
				logger.error(messageResourceBundle.getLogMessage("sales.entry.notfound"),id);
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SALES_ENTRY_NOTFOUND, new Object[] {id}));
			}

		} catch (NotFoundException ex) {
			throw new NotFoundException(ex.getMessage());
		} catch (Exception ex) {
			logger.error(messageResourceBundle.getLogMessage("sales.msg.error"),ex.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SALES_MSG_ERROR, new Object[] {ex.getMessage()}));
		}
		return new ResponseEntity<String>(messageResourceBundle.getMessage(ConstantMessages.SALES_DELETED_SUCCESS), HttpStatus.OK);
	}

	/**
	 * Retrieves a list of sales entries and maps them to their respective IDs.
	 * Returns a Map<Integer, SalesEntry> containing the sales entries.
	 *
	 * @return Map<Integer, SalesEntry> containing sales entries mapped by their
	 *         IDs.
	 */

	private Map<Integer, SalesEntry> list() {
		Map<Integer, SalesEntry> map = new HashMap<Integer, SalesEntry>();
		List<SalesEntry> list = null;
		try {
			list = this.salesRepository.findAll();
			if(!list.isEmpty()) {
				for (SalesEntry entry : list) {
					map.put(entry.getId(), entry);
				}
			}else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SALES_NOTFOUND));
			}
		} catch (NotFoundException e) {
			logger.error(messageResourceBundle.getLogMessage("sales.msg.error"),e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("sales.msg.error"),e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SALES_MSG_ERROR, new Object[] {e.getMessage()}));
		}
		
		return map;
	}

	/**
	 * Retrieves a list of sales entries filtered by the specified role and maps
	 * them to their respective IDs. Returns a Map<Integer, SalesEntry> containing
	 * the filtered sales entries.
	 *
	 * @param role The role by which sales entries are filtered.
	 * @return Map<Integer, SalesEntry> containing filtered sales entries mapped by
	 *         their IDs.
	 */

	public Map<Integer, SalesEntry> list(String role) {
		Map<Integer, SalesEntry> map = new HashMap<Integer, SalesEntry>();
		List<SalesEntry> list = null;
		try {
			list = this.salesRepository.findAll();
			if(!list.isEmpty()) {
				for (SalesEntry entry : list) {
					if (entry.getRole().equalsIgnoreCase(role)) {
						map.put(entry.getId(), entry);
					}
				}
			}else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SALES_NOTFOUND));
			}
		} catch (NotFoundException e) {
			logger.error(messageResourceBundle.getLogMessage("sales.msg.error"),e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("sales.msg.error"),e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SALES_MSG_ERROR, new Object[] {e.getMessage()}));
		}
		
		return map;
	}

	/**
	 * Retrieves a list of sales entries for sellers under a specific manager and
	 * role. Returns a Map<Integer, SalesEntry> containing the sales entries mapped
	 * by their IDs.
	 *
	 * @param mgrId The ID of the manager.
	 * @param role  The role of the sales entries to be retrieved.
	 * @return Map<Integer, SalesEntry> containing sales entries mapped by their
	 *         IDs.
	 */

	private Map<Integer, SalesEntry> listSellersUnderManager(String mgrId, String role) {
		Map<Integer, SalesEntry> map = new HashMap<Integer, SalesEntry>();
		List<SalesEntry> list = null;
		try {
			list = this.salesRepository.findByMasterIdAndRole(mgrId, role);
			if(!list.isEmpty()) {
				for (SalesEntry entry : list) {
					map.put(entry.getId(), entry);
				}
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SALES_NOTFOUND));
			}
		} catch (NotFoundException e) {
			logger.error(messageResourceBundle.getLogMessage("sales.msg.error"),e.getMessage());
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("sales.msg.error"),e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SALES_MSG_ERROR, new Object[] {e.getMessage()}));
		}
		
		return map;
	}

	/**
	 * Retrieves a collection of sales entries for executives based on the provided
	 * username. Validates user authorization, logs relevant information, and
	 * handles exceptions. Returns a ResponseEntity with the collection of sales
	 * entries or an appropriate response.
	 *
	 * @param username The username associated with the request.
	 * @return ResponseEntity with a collection of sales entries or an appropriate
	 *         response.
	 */

	@Override
	public ResponseEntity<Collection<SalesEntry>> listSalesUsers(String username) {

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
		logger.info(messageResourceBundle.getLogMessage("sales.req.list"), masterId, user.getRole());

		Collection<SalesEntry> salesList = null;
		try {
			if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				salesList = list().values();
			} else if (Access.isAuthorized(user.getRole(), "isAuthorizedManager")
					|| Access.isAuthorized(user.getRole(), "isAuthorizedAdmin")) {
				salesList = listSellersUnderManager(masterId, "seller").values();
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
			}

			if (salesList != null && !salesList.isEmpty()) {
				target = IConstants.SUCCESS_KEY;
				logger.info(messageResourceBundle.getLogMessage("sales.list.users"),masterId,salesList.size());
			} else {
				target = IConstants.FAILURE_KEY;
				logger.error(messageResourceBundle.getLogMessage("sales.list.nousers"),masterId,user.getRole(),target);
				throw new NotFoundException(
						messageResourceBundle.getExMessage(ConstantMessages.SALES_NOEXECUTIVE, new Object[] {masterId,user.getRole(),target}));
			}

		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("sales.msg.error"),e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SALES_MSG_ERROR, new Object[] {e.getMessage()}));
		}
		return ResponseEntity.ok(salesList);
	}

	/**
	 * Retrieves details of a sales entry with the specified ID based on the
	 * provided username. Validates user authorization, logs relevant information,
	 * and handles exceptions. Returns a ResponseEntity with the details of the
	 * sales entry or an appropriate response.
	 *
	 * @param id       The ID of the sales entry to be viewed.
	 * @param username The username associated with the request.
	 * @return ResponseEntity with details of the sales entry or an appropriate
	 *         response.
	 */

	@Override
	public ResponseEntity<ViewSalesEntry> viewSalesEntry(int id, String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}
		String target = IConstants.FAILURE_KEY;
		// Finding the user by system ID
		String systemId = user.getSystemId();
		logger.info(messageResourceBundle.getLogMessage("sales.view.req"),id,systemId,user.getRole());
		ViewSalesEntry response = new ViewSalesEntry();
		try {
			SalesEntry seller = null;
			Optional<SalesEntry> salesOptional = this.salesRepository.findById(id);
			if (salesOptional.isPresent()) {
				seller = salesOptional.get();
			} else {
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SALES_NOTFOUND));
			}

			if (seller != null) {
				if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
					Collection<SalesEntry> list = null;
					list = list("manager").values();
					
					if(!list.isEmpty()) {
						response.setManagers(list);
					}else {
						logger.warn(messageResourceBundle.getLogMessage("sales.manager.noentry"));
					}
				}
				response.setSeller(seller);
				target = IConstants.SUCCESS_KEY;
			}
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (NotFoundException e) {
			logger.error(messageResourceBundle.getLogMessage("sales.msg.error"),e.getMessage());
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("sales.msg.error"),e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SALES_MSG_ERROR, new Object[] {e.getMessage()}));
		}
	}

	/**
	 * Sets up sales entries based on the provided username. Validates user
	 * authorization, logs relevant information, and handles exceptions. Returns a
	 * ResponseEntity with a collection of sales entries or an appropriate response.
	 *
	 * @param username The username associated with the request.
	 * @return ResponseEntity with a collection of sales entries or an appropriate
	 *         response.
	 */

	@Override
	public ResponseEntity<Collection<SalesEntry>> setupSalesEntry(String username) {
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}
		String target = IConstants.SUCCESS_KEY;
		// Finding the user by system ID
		String systemId = user.getSystemId();
		logger.info(messageResourceBundle.getLogMessage("sales.setup.req"),systemId,user.getRole());
		Collection<SalesEntry> list = null;
		if (Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
			try {
				list = list("manager").values();
				
				if(list.isEmpty()) {
					logger.error(messageResourceBundle.getLogMessage("sales.manager.noentry"));
					throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.SALES_NOTFOUND));
				}
			} catch (NotFoundException e) {
				logger.error(messageResourceBundle.getLogMessage("sales.msg.error"),e.getMessage());
				throw new NotFoundException(e.getLocalizedMessage());
			} catch (Exception e) {
				logger.error(messageResourceBundle.getLogMessage("sales.msg.error"),e.getMessage());
				throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.SALES_MSG_ERROR, new Object[] {e.getMessage()}));
			}
		} else {
			if (Access.isAuthorized(user.getRole(), "isAuthorizedManager")
					|| Access.isAuthorized(user.getRole(), "isAuthorizedAdmin")) {
				logger.info("Authorized User :" + systemId);
			} else {
				target = "invalidRequest";
				logger.error("Authorization Failed: {}: {}", systemId, target);
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		}
		return ResponseEntity.ok(list);
	}

}
