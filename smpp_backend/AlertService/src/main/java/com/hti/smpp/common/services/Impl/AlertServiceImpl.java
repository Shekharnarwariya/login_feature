package com.hti.smpp.common.services.Impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.alert.dto.AlertEntity;
import com.hti.smpp.common.alert.repository.AlertRespository;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.request.AlertForm;
import com.hti.smpp.common.response.AlertEditResponse;
import com.hti.smpp.common.response.SetupAlertResponse;
import com.hti.smpp.common.services.AlertService;
import com.hti.smpp.common.smsc.dto.SmscEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;
import com.hti.smpp.common.util.dto.AlertDTO;

@Service
public class AlertServiceImpl implements AlertService{

	
	private static final Logger logger = LoggerFactory.getLogger(AlertServiceImpl.class);
	
	@Autowired
	private AlertRespository alertRepo;

	@Autowired
	private UserEntryRepository userRepository;

	
	
	
	@Override
	public ResponseEntity<String> saveAlert(AlertForm form, String username) {
		
		
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
		
		AlertDTO alert = new AlertDTO();
		
		logger.info("Add Performance Alert Request From " + user.getSystemId() + " [" + user.getRole() + "]");
		
		try {
			BeanUtils.copyProperties(form, alert);
			String[] countries = form.getCountries();
			String[] smsc = form.getRoutes();
			if (smsc != null && smsc.length > 0) {
				logger.info(user.getSystemId() + " smsc list: " + String.join(",", smsc));
			}
			if (countries != null && countries.length > 0) {
				logger.info(user.getSystemId() + " country list: " + String.join(",", countries));
			}
			addAlert(alert);
			MultiUtility.changeFlag(Constants.ALERT_FLAG_FILE, "707");
			logger.info("message: operation success");
			target = IConstants.SUCCESS_KEY;
		} catch (Exception e) {

		}
		
		return new ResponseEntity<>(target, HttpStatus.CREATED);
		
	}
	
	private void addAlert(AlertDTO alert) {
		try {
			String routes = null, countries = null;
			if (alert.getRoutes() != null) {
				routes = String.join(",", alert.getRoutes());
			}
			if (alert.getCountries() != null) {
				countries = String.join(",", alert.getCountries());
			}
			AlertEntity entity = new AlertEntity();

			entity.setRoutes(routes);
			entity.setCountries(countries);
			entity.setAlertNumber(alert.getAlertNumber());
			entity.setDuration(alert.getDuration());
			entity.setEmail(alert.getEmail());
			entity.setHoldTraffic(alert.isHoldTraffic());
			entity.setMinlimit(alert.getMinlimit());
			entity.setPercent(alert.getPercent());
			entity.setRemarks(alert.getRemarks());
			entity.setSenders(alert.getSenders());
			entity.setStatus(alert.getStatus());
			this.alertRepo.save(entity);
		} catch (Exception e) {
			logger.error("Error: " + e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}
	}

	@Override
	public ResponseEntity<AlertEditResponse> editAlert(int id, String username) {
		
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
		AlertEditResponse response = new AlertEditResponse();
		try {
			AlertDTO alert = getAlert(id);
			logger.info("id -- "+id);
			if (alert != null) {
				logger.info("alert -- "+alert.getCountries()[0]);
				target = "edit";
				Map<Integer, String> existCountries = new HashMap<Integer, String>();
				Set<String> existRoutes = new HashSet<String>();
				Map<Integer, String> networkmap = listCountries();
				List<String> smscList = new ArrayList<>(listNames().values());
				
				if (alert.getCountries() != null && alert.getCountries().length > 0) {
					String[] configuredCountries = alert.getCountries();
					
					for (int i = 0; i < configuredCountries.length; i++) {
						int network_id = Integer.parseInt(configuredCountries[i]);
						if (networkmap.containsKey(network_id)) {
							String opr_country = networkmap.remove(network_id);
							existCountries.put(network_id, opr_country);
						}
					}
				}
				if (alert.getRoutes() != null && alert.getRoutes().length > 0) {
					String[] configuredRoutes = alert.getRoutes();
					for (int i = 0; i < configuredRoutes.length; i++) {
						String route = configuredRoutes[i];
						if (smscList.contains(route)) {
							smscList.remove(route);
						}
						existRoutes.add(route);
					}
				}
				response.setAlert(alert);
				response.setExistCountries(existCountries);
				response.setExistRoutes(existRoutes);
				response.setNetworkmap(networkmap);
				response.setSmscList(smscList);
			}
		} catch (Exception e) {
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}
		return ResponseEntity.ok(response);
	}
	
	
	private AlertDTO getAlert(int id) {
		AlertDTO alert = null;
		try {
			AlertEntity alertEntity = this.alertRepo.findById(id)
					.orElseThrow(() -> new NotFoundException("Alert Entity Not Found!"));
			String smsc = alertEntity.getRoutes();
			String country = alertEntity.getCountries();
			
			String[] smsc_arr = null, country_arr = null;
			if (smsc != null && smsc.length() > 0) {
				smsc_arr = smsc.split(",");
			}
			if (country != null && country.length() > 0) {
				country_arr = country.split(",");
			}
			logger.info("country - " +country_arr[0] );
			alert = new AlertDTO(alertEntity.getId(), country_arr, smsc_arr, alertEntity.getPercent(),
					alertEntity.getDuration(), alertEntity.getStatus(), alertEntity.getEmail(),
					alertEntity.getAlertNumber(), alertEntity.getRemarks(), alertEntity.getMinlimit(),
					alertEntity.isHoldTraffic(), alertEntity.getSenders());

		} catch (NotFoundException e) {
			logger.error(e.toString());
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}
		return alert;
	}
	
	
	public Map<Integer, String> listCountries() {
		Map<Integer, String> countries = new HashMap<Integer, String>();
		try {
			for (NetworkEntry entry : GlobalVars.NetworkEntries.values()) {
				countries.put(entry.getId(), entry.getCountry() + "-" + entry.getOperator());
			}
		} catch (Exception e) {
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}
		return countries;
	}

	public Map<Integer, String> listNames() {
		Map<Integer, String> names = new HashMap<Integer, String>();
		try {
			for (SmscEntry entry : GlobalVars.SmscEntries.values()) {
				names.put(entry.getId(), entry.getName());
			}
			names = names.entrySet().stream().sorted(Entry.comparingByValue())
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		} catch (Exception e) {
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}
		return names;
	}

	@Override
	public ResponseEntity<String> deleteAlert(int id, String username) {
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
		try {
			if (this.alertRepo.existsById(id)) {
				this.alertRepo.deleteById(id);
				target = IConstants.SUCCESS_KEY;
				logger.info("Alert deleted successfully!");
				MultiUtility.changeFlag(Constants.ALERT_FLAG_FILE, "101"); // remove flag
			} else {
				logger.error("Operation Failed");
				throw new InternalServerException("Unable to delete alert.");
			}
		} catch (InternalServerException e) {
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}

		return new ResponseEntity<>(target, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<AlertDTO>> getAlerts(String username) {
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
		List<AlertDTO> alertsValues = null;
		try {
			Map<Integer, AlertDTO> getAllAlerts = getAllAlerts();
			if(getAllAlerts.isEmpty()) {
				logger.error("All alerts not found!");
				throw new NotFoundException("Unable to found all alerts!");
			} else {
				target = IConstants.SUCCESS_KEY;
				logger.info("All alerts fetched successfully!");
				alertsValues = new ArrayList<>(getAllAlerts.values());
			}
			
		} catch (NotFoundException e) {
			logger.error(e.toString());
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}
		logger.info("Message: "+target);
		return ResponseEntity.ok(alertsValues);
	}

	
	private Map<Integer, AlertDTO> getAllAlerts() {
		List<AlertEntity> allAlertEntities = this.alertRepo.findAll();
		Map<Integer, AlertDTO> list = new HashMap<Integer, AlertDTO>();
		if (allAlertEntities.isEmpty()) {
			logger.error("Unable to found AlertEntries!");
			throw new NotFoundException("AlertEntries not found!");
		} else {
			Map<Integer, String> network_map = listCountries();
			allAlertEntities.forEach(alert -> {
				String countries = "";
				String[] smsc_arr = null, country_arr = null;
				String network_id = alert.getCountries();
				String smsc = alert.getRoutes();

				if (network_id != null && network_id.length() > 0) {
					StringTokenizer tokens = new StringTokenizer(network_id, ",");
					while (tokens.hasMoreTokens()) {
						String next = tokens.nextToken();
						if (network_map.containsKey(next)) {
							countries += (String) network_map.get(next) + ",";
						}
					}
					if (countries.length() > 0) {
						countries = countries.substring(0, countries.length() - 1);
						country_arr = countries.split(",");
					}
				}

				if (smsc != null && smsc.length() > 0) {
					smsc_arr = smsc.split(",");
				}

				AlertDTO alertDto = new AlertDTO(alert.getId(), country_arr, smsc_arr, alert.getPercent(),
						alert.getDuration(), alert.getStatus(), alert.getEmail(), alert.getAlertNumber(),
						alert.getRemarks(), alert.getMinlimit(), alert.isHoldTraffic(), alert.getSenders());
				list.put(alert.getId(), alertDto);
			});
			return list;
		}
	}

	
	
	@Override
	public ResponseEntity<String> updateAlert(int id, AlertForm form, String username) {
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
		AlertDTO alert = new AlertDTO();
		logger.info("Modify Performance Alert Request From " + user.getSystemId() + " ["
				+ user.getRole() + "]");
		try {
			BeanUtils.copyProperties(form, alert);
			String[] countries = form.getCountries();
			String[] smsc = form.getRoutes();
			if (smsc != null && smsc.length > 0) {
				logger.info(user.getSystemId() + " smsc list: " + String.join(",", smsc));
			}
			if (countries != null && countries.length > 0) {
				logger.info(user.getSystemId() + " country list: " + String.join(",", countries));
			}
			modifyAlert(id,alert);
			MultiUtility.changeFlag(Constants.ALERT_FLAG_FILE, "707");
			logger.info("message: operation success");
			target = IConstants.SUCCESS_KEY;
		} catch (Exception e) {
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}
		return new ResponseEntity<>(target,HttpStatus.CREATED);
	}
	
	
	private void modifyAlert(int id, AlertDTO alert) {
		AlertEntity updateAlert = this.alertRepo.findById(id).orElseThrow(() -> new NotFoundException("Alert not found!"));
		try {
			String routes = null, countries = null;
			if (alert.getRoutes() != null) {
				routes = String.join(",", alert.getRoutes());
			}
			if (alert.getCountries() != null) {
				countries = String.join(",", alert.getCountries());
			}
			
			logger.info("alter update "+updateAlert);
			
			updateAlert.setRoutes(routes);
			updateAlert.setCountries(countries);			
			updateAlert.setStatus(alert.getStatus());
			updateAlert.setDuration(alert.getDuration());
			updateAlert.setPercent(alert.getPercent());
			updateAlert.setEmail(alert.getEmail());
			updateAlert.setAlertNumber(alert.getAlertNumber());
			updateAlert.setRemarks(alert.getRemarks());
			updateAlert.setMinlimit(alert.getMinlimit());
			updateAlert.setHoldTraffic(alert.isHoldTraffic());
			updateAlert.setSenders(alert.getSenders());
			updateAlert.setId(alert.getId());
			
			logger.info("country "+ countries);
			AlertEntity saveUpdate = this.alertRepo.save(updateAlert);
			logger.info("om");
			if(saveUpdate != null) {
				logger.info("AlertEntity updated successfully!");
			} else {
				logger.error("Error updating the alert entity!");
				throw new InternalServerException("Unable to update alert entity!");
			}
			
		} catch (InternalServerException e) {
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}
	}

	@Override
	public ResponseEntity<?> setupAlert(String username) {

//		String role = SessionHelper.getClientRole(request);
//		String systemId = SessionHelper.getClientId(request);
		
		
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
		
		String systemId = user.getSystemId();
		
		SetupAlertResponse setupAlertResponse = new SetupAlertResponse();
		
		String target = IConstants.FAILURE_KEY;
//		ActionMessages messages = new ActionMessages();
//		ActionMessage message = null;
		
		logger.info(systemId + " Setup Alert Request");
			try {
				Collection<String> smscList = listNames().values();
				Map<Integer, String> countries = listCountries();
				
				setupAlertResponse.setCountries(countries);
				setupAlertResponse.setSmscList(smscList);
				
//				request.setAttribute("networkmap", countries);
//				request.setAttribute("smscList", smscList);
				
				target = IConstants.SUCCESS_KEY;
			} catch (Exception ex) {
				logger.error(systemId, ex);
//				message = new ActionMessage("error.processError");
			}
		
		logger.info(systemId + " Setup Alert Target:" + target);
//		messages.add(ActionMessages.GLOBAL_MESSAGE, message);
//		saveMessages(request, messages);
//		return mapping.findForward(target);
	
		return new ResponseEntity<>(setupAlertResponse,HttpStatus.CREATED);
	}
	
	
	
	

	
}
