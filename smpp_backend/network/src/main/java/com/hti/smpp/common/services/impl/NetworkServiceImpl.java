package com.hti.smpp.common.services.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hti.smpp.common.dto.MccMncDTO;
import com.hti.smpp.common.dto.MccMncUpdateDTO;
import com.hti.smpp.common.dto.Network;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.JsonProcessingError;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.NumberFormatError;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.network.repository.NetworkEntryRepository;
import com.hti.smpp.common.request.MccMncForm;
import com.hti.smpp.common.request.MccMncUpdateForm;
import com.hti.smpp.common.response.AddResponse;
import com.hti.smpp.common.response.MncMccTokens;
import com.hti.smpp.common.response.PaginationResponse;
import com.hti.smpp.common.response.SearchResponse;
import com.hti.smpp.common.services.NetworkService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;
import com.hti.smpp.common.util.MultiUtility;

import jakarta.transaction.Transactional;

@Service
public class NetworkServiceImpl implements NetworkService {

	private final Logger logger = LoggerFactory.getLogger(NetworkServiceImpl.class);


	@Autowired
	private NetworkEntryRepository networkEntryRepo;

	@Autowired
	private UserEntryRepository userRepository;
	
	@Autowired
	private MessageResourceBundle messageResourceBundle;

//	@Value("${file.dir}")
//	private String filePath;
	
	/**
     * Adds a new network entry to the network.
     *
     * @param formMccMnc JSON-formatted MccMncForm data
     * @param file       Optional file in .xls format to be associated with the entry
     * @param username   Username of the user making the request
     * @return ResponseEntity containing the result of the operation
     */
	@Override
	public ResponseEntity<?> addNewMccMnc(String formMccMnc, MultipartFile file, String username) {

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

		String target = null;
		MccMncDTO mccMncDTO = new MccMncDTO();
		ArrayList<MccMncDTO> mccmncList = new ArrayList<>();
		ArrayList<MccMncDTO> replaceList = new ArrayList<>();
		String checkInsertion = "";
		int id = 0, count = 0;
		Iterator<MccMncDTO> iterator = null;
		String response = "";
		AddResponse addResponse = new AddResponse();
		try {

			if (formMccMnc != null) {

				MccMncForm form;
				try {
					ObjectMapper objectMapper = new ObjectMapper();
					form = objectMapper.readValue(formMccMnc, MccMncForm.class);
				} catch (JsonProcessingException e) {
					throw new JsonProcessingError(messageResourceBundle.getExMessage(ConstantMessages.JSON_PROCESSING_ERROR,new Object[] {e.getMessage()}));
				} catch (Exception ex) {
					throw new InternalServerException(ex.getLocalizedMessage());
				}

				BeanUtils.copyProperties(form, mccMncDTO);
				checkInsertion = mccMncDTO.getCheckMcc();
				if (checkInsertion.equalsIgnoreCase("single")) {
					id = checkDuplicateMccMnc(mccMncDTO);
					if (id == 0) {
						mccmncList.add(mccMncDTO);
					} else {
						mccMncDTO.setId(id);
						replaceList.add(mccMncDTO);
					}
				} else {
					logger.error(messageResourceBundle.getLogMessage("network.not.single"));
					throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.NETWORK_SELECT_SINGLE));
				}

			} else {
				if(!file.isEmpty()) {
					MccMncDTO mccMncDTOC = null;
					FileDataParser fileDataParser = new FileDataParser();
					ArrayList<MccMncDTO> tempList = fileDataParser.getMccMncList(file);
					System.out.println("TempList Size: " + tempList.size());
					iterator = tempList.iterator();
					while (iterator.hasNext()) {
						mccMncDTOC = (MccMncDTO) iterator.next();
						id = checkDuplicateMccMnc(mccMncDTOC);
						if (id == 0) {
							mccmncList.add(mccMncDTOC);
						} else {
							mccMncDTOC.setId(id);
							replaceList.add(mccMncDTOC);
						}
					}
				}else {
					throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.NETWORK_FILE_NOTSELECTED));
				}
			}
			if (mccmncList.size() > 0) {
				Integer totalRecord = mccmncList.size();
				Integer remained = 0;
				count = insertMccMnc(mccmncList);
				if (count == totalRecord) {
					target = IConstants.SUCCESS_KEY;
					logger.info(messageResourceBundle.getLogMessage("network.saved.success"));
					logger.info(messageResourceBundle.getLogMessage("network.total.record"), count);
					response = "Network Entry Saved Successful! Total record inserted: " + count;
					addResponse.setResponseMessage(response);
					addResponse.setTotalRecords(count);
				} else {
					remained = totalRecord - count;
					target = IConstants.FAILURE_KEY;
					logger.error(messageResourceBundle.getLogMessage("network.saved.failure"));
					logger.warn(messageResourceBundle.getLogMessage("network.remaining"),remained);
					addResponse.setTotalRecords(remained);
					addResponse.setResponseMessage("DBEntry Failure. Remaining: " + remained);;
					return new ResponseEntity<>(addResponse, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
			if (replaceList.size() > 0) {
				target = "replace";
				response = "Duplicate Entry Found! Total No. Of Duplicate Record: " + replaceList.size();
				String param_message = "Total Records : " + count+"."+response;
				logger.error(response);
				addResponse.setResponseMessage(param_message);
				addResponse.setList(replaceList);
				addResponse.setTotalRecords(replaceList.size());
				return new ResponseEntity<>(addResponse,HttpStatus.CONFLICT);
			}
			MultiUtility.changeFlag(Constants.NETWORK_FLAG_FILE, "707");
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("network.msg.error"));
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.NETWORK_MSG_ERROR, new Object[] {e.getMessage()}));
		}
		return new ResponseEntity<>(addResponse, HttpStatus.CREATED);
	}

	private int checkDuplicateMccMnc(MccMncDTO mccMncDTO) {
		int id = 0;
		String mcc = mccMncDTO.getMcc();
		List<String> mncList = extractMncList(mccMncDTO.getMnc());
		boolean flag = false;

		List<NetworkEntry> l = this.networkEntryRepo.findByMcc(mcc);

		for (NetworkEntry ne : l) {
			String mnc = ne.getMnc();
			if (mnc.contains(",")) {
				String[] mncArray = mnc.split(",");
				for (String m : mncArray) {
					if (mncList.contains(m)) {
						flag = true;
						break;
					}
				}
			} else {
				if (mncList.contains(mnc)) {
					flag = true;
				}
			}
			if (flag) {
				id = ne.getId();
				break;
			}
		}

		return id;
	}

	private List<String> extractMncList(String mnc) {
		List<String> mncList = new ArrayList<>();

		if (mnc.contains(",")) {
			String[] mncArray = mnc.split(",");
			for (String mncValue : mncArray) {
				try {
//					Integer intMnc = Integer.parseInt(mncValue.trim());
					mncList.add(mncValue);
				} catch (NumberFormatException e) {
					logger.error(e.toString());
					throw new NumberFormatError(e.getLocalizedMessage());
				} catch (Exception e) {
					logger.error(e.toString());
					throw new InternalServerException(e.getLocalizedMessage());
				}
			}
		} else {
			try {
//				Integer intMnc = Integer.parseInt(mnc.trim());
				mncList.add(mnc);
			} catch (NumberFormatException e) {
				logger.error(e.toString());
				throw new NumberFormatError(e.getLocalizedMessage());
			} catch (Exception e) {
				logger.error(e.toString());
				throw new InternalServerException(e.getLocalizedMessage());
			}
		}

		return mncList;
	}

	private Integer insertMccMnc(ArrayList<MccMncDTO> list) {
		NetworkEntry networkEntry = null;
		Integer count = 0;
		for (MccMncDTO data : list) {

			networkEntry = new NetworkEntry(data.getCountry(), data.getOperator(), data.getMcc().trim(),
					data.getMnc().trim(), Integer.parseInt(data.getCc()), data.getPrefix().trim());
			this.networkEntryRepo.save(networkEntry);
			count++;
		}
		return count;
	}
	
	
	/**
     * Updates an existing network entry in the network.
     *
     * @param form     JSON-formatted MccMncUpdateForm data
     * @param username Username of the user making the request
     * @return ResponseEntity containing the result of the operation
     */
	@Override
	public ResponseEntity<String> replace(MccMncUpdateForm form, String username) {

		Optional<UserEntry> userOptional = this.userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] {username}));
			}
		} else {
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] {username}));
		}
		String target = null;
		MccMncUpdateDTO updateDTO = new MccMncUpdateDTO();
		MccMncDTO mncDTO = null;
		int count = 0;
		ArrayList<MccMncDTO> list = new ArrayList<MccMncDTO>();
		try {
			BeanUtils.copyProperties(form, updateDTO);
			int[] id = updateDTO.getId();
			String[] country = updateDTO.getCountry();
			String[] operator = updateDTO.getOperator();
			String[] cc = updateDTO.getCc();
			String[] prefix = updateDTO.getPrefix();
			String[] mcc = updateDTO.getMcc();
			String[] mnc = updateDTO.getMnc();
			System.out.println("mcc :" + mcc);
			System.out.println("mnc :" + mnc);
			if (id.length > 0) {
				for (int i = 0; i < id.length; i++) {
					mncDTO = new MccMncDTO();
					mncDTO.setId(id[i]);
					mncDTO.setCc(cc[i].trim());
					mncDTO.setCountry(country[i]);
					mncDTO.setOperator(operator[i]);
					mncDTO.setPrefix(prefix[i].trim());
					mncDTO.setMcc(mcc[i]);
					mncDTO.setMnc(mnc[i]);
					list.add(mncDTO);
				}
				count = updateMccMnc(list);
				logger.info(messageResourceBundle.getLogMessage("network.msg.update"));
				logger.info(messageResourceBundle.getLogMessage("network.record.updated"), count);
				target = IConstants.SUCCESS_KEY;
				MultiUtility.changeFlag(Constants.NETWORK_FLAG_FILE, "707");
			} else {
				logger.error(messageResourceBundle.getLogMessage("network.record.unavailable"));
				target = IConstants.FAILURE_KEY;
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NETWORK_RECORD_UNAVAILABLE));
			}

		} catch (NotFoundException e) {
			target = IConstants.FAILURE_KEY;
			logger.error("Exception:" + e);
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			target = IConstants.FAILURE_KEY;
			logger.error(messageResourceBundle.getLogMessage("network.msg.error"),e.getMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.NETWORK_MSG_ERROR, new Object[] {e.getMessage()}));
		}
		return new ResponseEntity<String>(messageResourceBundle.getMessage(ConstantMessages.NETWORK_UPDATE_SUCCESS, new Object[] {count}), HttpStatus.CREATED);
	}

	public int updateMccMnc(ArrayList<MccMncDTO> list) {
		int count = 0;
		NetworkEntry entry = null;
		for (MccMncDTO data : list) {
			boolean isPresent = this.networkEntryRepo.existsById(data.getId());
			if(isPresent) {
				entry = new NetworkEntry();
				entry.setCc(Integer.parseInt(data.getCc()));
				entry.setCountry(data.getCountry());
				entry.setOperator(data.getOperator());
				entry.setMcc(data.getMcc());
				entry.setMnc(data.getMnc());
				entry.setPrefix(data.getPrefix());
				entry.setId(data.getId());
				this.networkEntryRepo.save(entry);
				count++;
			}else {
				logger.error(messageResourceBundle.getLogMessage("network.entry.notfound"),data.getId());
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NETWORK_NOT_FOUND, new Object[] {data.getId()}));
			}
		}
		return count;
	}
	
	/**
     * Deletes network entries by their IDs.
     *
     * @param ids      List of IDs of the entries to be deleted
     * @param username Username of the user making the request
     * @return ResponseEntity containing the result of the operation
     */
	@Override
	@Transactional
	public ResponseEntity<String> delete(List<Integer> ids, String username) {

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

		int count = ids.size();
		String target = null;
		try {
			if (!ids.isEmpty()) {
				this.networkEntryRepo.deleteByIdIn(ids);
				logger.info(messageResourceBundle.getLogMessage("network.deleted.record"),count);
				target = IConstants.SUCCESS_KEY;
				MultiUtility.changeFlag(Constants.NETWORK_FLAG_FILE, "707");
			} else {
				logger.error("Error: No Record Found To Delete!");
				target = IConstants.FAILURE_KEY;
				throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.NETWORK_DELETE_FAILED));
			}

		} catch (Exception ex) {
			count = 0;
			logger.error("Exception", ex);
			target = IConstants.FAILURE_KEY;
			throw new InternalServerException(ex.getLocalizedMessage());
		}

		return new ResponseEntity<String>(messageResourceBundle.getMessage(ConstantMessages.NETWORK_DELETE_SUCCESS, new Object[] {count}), HttpStatus.OK);
	}
	
	
	/**
     * Searches for network entries based on specified parameters.
     *
     * @param ccReq          Country Code parameter
     * @param mccReq         MCC (Mobile Country Code) parameter
     * @param mncReq         MNC (Mobile Network Code) parameter
     * @param checkCountryReq Check Country parameter
     * @param checkMccReq     Check MCC parameter
     * @param checkMncReq     Check MNC parameter
     * @param username       Username of the user making the request
     * @return ResponseEntity containing a list of MccMncDTO objects
     */
	@Override
	public ResponseEntity<SearchResponse> search(String ccReq, String mccReq, String mncReq, String checkCountryReq,
			String checkMccReq, String checkMncReq, String username, Pageable pageable) {

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

		String target = null;
		String checkCountry = "", checkMcc = "", checkMnc = "";
		String cc = "%", mcc = "%", mnc = "%";
		
		List<MccMncDTO> list = new ArrayList<>();
		SearchResponse searchResponse = new SearchResponse();
		try {
			checkCountry = checkCountryReq;
			checkMcc = checkMccReq;
			checkMnc = checkMncReq;
			if (checkCountry != null) {
				cc = ccReq;
				if (cc.equalsIgnoreCase("all")) {
					cc = "%";
				}
			}
			if (checkMcc != null) {
				mcc = mccReq;
				if (mcc.equalsIgnoreCase("all")) {
					mcc = "%";
				}
			}
			if (checkMnc != null) {
				mnc = mncReq;
				if (mnc.equalsIgnoreCase("all")) {
					mnc = "%";
				}
			}

			
			Page<NetworkEntry> resultPage = this.networkEntryRepo.findByPaginated(cc, mcc, mnc, pageable);
			PaginationResponse response = new PaginationResponse(resultPage.getNumber(),resultPage.getSize(),resultPage.getTotalPages(),resultPage.getTotalElements(),resultPage.isLast(),resultPage.isFirst());
			try {
				List<NetworkEntry> networks = resultPage.getContent();

				for (NetworkEntry network : networks) {
					MccMncDTO mccMncDTO = new MccMncDTO();
					mccMncDTO.setId(network.getId());
					mccMncDTO.setCc(Integer.toString(network.getCc()));
					mccMncDTO.setCountry(network.getCountry());
					mccMncDTO.setOperator(network.getOperator());
					mccMncDTO.setMcc(network.getMcc());
					mccMncDTO.setMnc(network.getMnc());
					mccMncDTO.setPrefix(network.getPrefix());
					list.add(mccMncDTO);
				}
			} catch (Exception ex) {
				logger.error(ex.toString());
				throw new InternalServerException(ex.getLocalizedMessage());
			}
			
			searchResponse.setPagiResponse(response);
			searchResponse.setResponseList(list);
			

		} catch (Exception e) {
			target = IConstants.FAILURE_KEY;
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}
		if (list.isEmpty()) {
			logger.error(messageResourceBundle.getLogMessage("network.record.unavailable"));
			throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NETWORK_MCCMNC_NOTFOUND));
		} else {
			logger.info(messageResourceBundle.getLogMessage("network.record.notfound"),list.size());
			target = IConstants.SUCCESS_KEY;
			return ResponseEntity.ok(searchResponse);
		}
		
	}
	
	private ArrayList<MccMncDTO> getPaginatedResult(String cc, String mcc, String mnc, Pageable pageable) {
		ArrayList<MccMncDTO> list = new ArrayList<>();
		Page<NetworkEntry> resultPage = this.networkEntryRepo.findByPaginated(cc, mcc, mnc, pageable);
		try {
			List<NetworkEntry> networks = resultPage.getContent();

			for (NetworkEntry network : networks) {
				MccMncDTO mccMncDTO = new MccMncDTO();
				mccMncDTO.setId(network.getId());
				mccMncDTO.setCc(Integer.toString(network.getCc()));
				mccMncDTO.setCountry(network.getCountry());
				mccMncDTO.setOperator(network.getOperator());
				mccMncDTO.setMcc(network.getMcc());
				mccMncDTO.setMnc(network.getMnc());
				mccMncDTO.setPrefix(network.getPrefix());
				list.add(mccMncDTO);
			}
		} catch (Exception ex) {
			logger.error(ex.toString());
			throw new InternalServerException(ex.getLocalizedMessage());
		}

		return list;
	}

	private ArrayList<MccMncDTO> editMccMnc(String cc, String mcc, String mnc) {
		ArrayList<MccMncDTO> list = new ArrayList<>();

		try {
			List<NetworkEntry> networks = this.networkEntryRepo.findByCcAndMccAndMnc(cc, mcc, mnc);

			for (NetworkEntry network : networks) {
				MccMncDTO mccMncDTO = new MccMncDTO();
				mccMncDTO.setId(network.getId());
				mccMncDTO.setCc(Integer.toString(network.getCc()));
				mccMncDTO.setCountry(network.getCountry());
				mccMncDTO.setOperator(network.getOperator());
				mccMncDTO.setMcc(network.getMcc());
				mccMncDTO.setMnc(network.getMnc());
				mccMncDTO.setPrefix(network.getPrefix());
				list.add(mccMncDTO);
			}
		} catch (Exception ex) {
			logger.error(ex.toString());
			throw new InternalServerException(ex.getLocalizedMessage());
		}

		return list;
	}
	
	
	/**
     * Downloads a file based on specified parameters.
     *
     * @param ccReq          Country Code parameter
     * @param mccReq         MCC (Mobile Country Code) parameter
     * @param mncReq         MNC (Mobile Network Code) parameter
     * @param checkCountryReq Check Country parameter
     * @param checkMccReq     Check MCC parameter
     * @param checkMncReq     Check MNC parameter
     * @param username       Username of the user making the request
     * @return ResponseEntity containing the file data
     */
	@Override
	public ResponseEntity<byte[]> download(String ccReq, String mccReq, String mncReq, String checkCountryReq,
			String checkMccReq, String checkMncReq, String username) {

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

		String target = null;
		String checkCountry = "", checkMcc = "", checkMnc = "";
		String cc = "%", mcc = "%", mnc = "%";
		try {
			checkCountry = checkCountryReq;
			checkMcc = checkMccReq;
			checkMnc = checkMncReq;
			if (checkCountry != null) {
				cc = ccReq;
				if (cc.equalsIgnoreCase("all")) {
					cc = "%";
				}
			}
			if (checkMcc != null) {
				mcc = mccReq;
				if (mcc.equalsIgnoreCase("all")) {
					mcc = "%";
				}
			}
			if (checkMnc != null) {
				mnc = mncReq;
				if (mnc.equalsIgnoreCase("all")) {
					mnc = "%";
				}
			}

			ArrayList<MccMncDTO> list = editMccMnc(cc, mcc, mnc);
			String fileName = "mccmnc_database.xls";
			if (!cc.equalsIgnoreCase("%")) {
				fileName = "mccmnc_database_" + cc + ".xls";
			}
//			String file = this.filePath + fileName;
			String file = IConstants.WEBAPP_DIR + "report//" + fileName;
			FileContentGenerator contentGenerator = new FileContentGenerator();
			contentGenerator.createMccMncContent(list, file);
			logger.info("<---- Preparing for Download --->");
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment", fileName);
			byte[] fileBytes;
			try (FileInputStream fis = new FileInputStream(file)) {
				fileBytes = fis.readAllBytes();
			} catch (IOException ex) {
				logger.error(ex.toString());
				throw new InternalServerException(ex.getLocalizedMessage());
			}

			// Delete the file after download
//			boolean deleted = new File(file).delete();
//			if (deleted) {
//				logger.info("Deleted File => " + file);
//			} else {
//				logger.error("Unable to Delete File => " + file);
//			}

			return ResponseEntity.ok().headers(headers).body(fileBytes);

		} catch (InternalServerException e) {
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}
	}

	private List<Network> getNetworkRecord() {
		List<NetworkEntry> list = this.networkEntryRepo.findAll();
		List<Network> networkList = new ArrayList<>();
		for (NetworkEntry n : list) {
			Network network = new Network();
			network.setId(n.getId());
			network.setCc(Integer.toString(n.getCc()));
			network.setMcc(n.getMcc());
			network.setMnc(n.getMnc());
			network.setCountry(n.getCountry());
			network.setOperator(n.getOperator());
			network.setPrefix(n.getPrefix());
			networkList.add(network);
		}
		return networkList;
	}
	
	/**
     * Retrieves the NetworkMap of Country and CC from all NetworkEntry.
     *
     * @param username Username of the user making the request
     * @return ResponseEntity containing the result of the operation
     */
	@Override
	public ResponseEntity<?> editMccMnc(String username) {

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

		String target = null;
		Map<String, String> networkmap = new HashMap<>();
		try {
			List<Network> networkList = getNetworkRecord();
			while (!networkList.isEmpty()) {
				Network network = (Network) networkList.remove(0);
				networkmap.put(network.getCountry(), network.getCc());
			}
			if (!networkmap.isEmpty()) {
				target = IConstants.SUCCESS_KEY;
				System.out.println(networkmap.size());
				return ResponseEntity.ok(networkmap);
			} else {
				logger.error(messageResourceBundle.getLogMessage("network.map.norecord"));
				target = IConstants.FAILURE_KEY;
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NETWORK_NOMAP_RECORD));
			}
		} catch (NotFoundException e) {
			logger.error(e.toString());
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			target = IConstants.FAILURE_KEY;
			logger.error(target, e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}

	}
	
	
	/**
     * Updates existing network entries by uploading a file.
     *
     * @param file     File containing the updated data
     * @param username Username of the user making the request
     * @return ResponseEntity containing the result of the operation
     */
	@Override
	public ResponseEntity<?> uploadUpdateMccMnc(MultipartFile file, String username) {

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
		String response = "";
		String target = null;
		int count = 0;
		try {
			FileDataParser fileDataParser = new FileDataParser();
			ArrayList<MccMncDTO> tempList = fileDataParser.getMccMncList(file);
			if (tempList.size() > 0) {
				count = updateMccMnc(tempList);
				if (tempList.size() == count) {
					target = IConstants.SUCCESS_KEY;
					logger.info(messageResourceBundle.getLogMessage("network.record.updated"),count);
					response = "Update Successful! Total Updated Records: " + count;
				} else {
					int remained = tempList.size() - count;
					target = IConstants.FAILURE_KEY;
					logger.error(messageResourceBundle.getLogMessage("network.update.failure"),remained);
					response = "Update Failure With Remaining Records: " + remained;
				}
				MultiUtility.changeFlag(Constants.NETWORK_FLAG_FILE, "707");
			} else {
				target = IConstants.FAILURE_KEY;
				logger.error(target, "Error fetching data or list is empty!");
				throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NETWORK_DATA_EMPTY));
			}
		} catch (NotFoundException e) {
			logger.error(e.toString());
			target = IConstants.FAILURE_KEY;
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error(e.toString());
			target = IConstants.FAILURE_KEY;
			throw new InternalServerException(e.getLocalizedMessage());
		}
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	public List<String> getMNCList(String MCC) {
		List<String> mncList = new ArrayList<>();

		try {
			List<Object[]> results = this.networkEntryRepo.findDistinctMNCAndOperatorByMCCLike(MCC);

			for (Object[] result : results) {
				String MNC = (String) result[0];
				String operator = (String) result[1];

				if (MNC != null && !MNC.isEmpty()) {
					MNC = operator + "#" + MNC;
					mncList.add(MNC);
				}
			}

		} catch (Exception ex) {
			throw new InternalServerException(ex.getLocalizedMessage());
		}

		return mncList;
	}
	
	
	/**
     * Retrieves MncMccTokens based on specified parameters.
     *
     * @param countryName Name of the country
     * @param mccParam    MCC (Mobile Country Code) parameter
     * @param username    Username of the user making the request
     * @return ResponseEntity containing MncMccTokens
     */
	@Override
	public ResponseEntity<MncMccTokens> findOption(String countryName, String mccParam, String username) {

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

		String mcc = "", mccTokens = "";
		String mnc = "", mncTokens = "";
		Iterator<String> iterator = null;
		MncMccTokens mncMccTokens = new MncMccTokens();
		try {
			if (!(countryName.equals("NA"))) {
				List<String> mccList = this.networkEntryRepo.findDistinctMCCByCc(countryName);
				iterator = mccList.iterator();
				while (iterator.hasNext()) {
					mcc = (String) iterator.next();
					mccTokens += mcc + ";";
				}
				mccTokens = mccTokens.substring(0, mccTokens.length() - 1);
				mncMccTokens.setMccTokens(mccTokens);
			}
			if (!(mccParam.equals("NA"))) {
				List<String> mncList = getMNCList(mccParam);
				iterator = mncList.iterator();
				while (iterator.hasNext()) {
					mnc = (String) iterator.next();
					mncTokens += mnc + ";";
				}
				mncTokens = mncTokens.substring(0, mncTokens.length() - 1);
				mncMccTokens.setMncTokens(mncTokens);
			}
		} catch (Exception e) {
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}
		return ResponseEntity.ok(mncMccTokens);
	}

	@Override
	public ResponseEntity<?> findMcc(String cc, String username) {
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
		
		try {
			if(cc!=null && !(cc.equals("NA"))) {
				List<String> getCC = this.networkEntryRepo.findDistinctMCCByCc(cc);
				if(getCC.isEmpty()) {
					throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NETWORK_MCC_NOTFOUND));
				}else {
					return ResponseEntity.ok(getCC);
				}
			}else {
				throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.NETWORK_FETCHMCC_FAILURE));
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getMessage());
		} catch (Exception e) {
			throw new InternalServerException(e.getMessage());
		}

		
	}

	@Override
	public ResponseEntity<?> findMnc(String mcc, String username) {
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
		
		try {
			List<String> mncList = new ArrayList<>();
			
			if(mcc!=null && !(mcc.equals("NA"))) {
				List<Object[]> results = this.networkEntryRepo.findDistinctMNCAndOperatorByMCCLike(mcc);

				for (Object[] result : results) {
					String MNC = (String) result[0];

					if (MNC != null && !MNC.isEmpty()) {
						mncList.add(MNC);
					}
				}
				
				if(mncList.isEmpty()) {
					throw new NotFoundException(messageResourceBundle.getExMessage(ConstantMessages.NETWORK_MNC_NOTFOUND));
				}else {
					return ResponseEntity.ok(mncList);
				}
				
			}else {
				throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.NETWORK_FETCHMNC_FAILURE));
			}
		} catch (NotFoundException e) {
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			throw new InternalServerException(e.getLocalizedMessage());
		}
		
	}

}
