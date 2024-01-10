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
import org.springframework.beans.factory.annotation.Value;
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
import com.hti.smpp.common.response.MncMccTokens;
import com.hti.smpp.common.services.NetworkService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;

import jakarta.transaction.Transactional;

@Service
public class NetworkServiceImpl implements NetworkService {

	private final Logger logger = LoggerFactory.getLogger(NetworkServiceImpl.class);


	@Autowired
	private NetworkEntryRepository networkEntryRepo;

	@Autowired
	private UserEntryRepository userRepository;

	@Value("${file.dir}")
	private String filePath;

	@Override
	public ResponseEntity<String> addNewMccMnc(String formMccMnc, MultipartFile file, String username) {

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

		String target = null;
		MccMncDTO mccMncDTO = new MccMncDTO();
		ArrayList<MccMncDTO> mccmncList = new ArrayList<>();
		ArrayList<MccMncDTO> replaceList = new ArrayList<>();
		String checkInsertion = "";
		int id = 0, count = 0;
		Iterator<MccMncDTO> iterator = null;

		try {

			if (formMccMnc != null) {

				MccMncForm form;
				try {
					ObjectMapper objectMapper = new ObjectMapper();
					form = objectMapper.readValue(formMccMnc, MccMncForm.class);
				} catch (JsonProcessingException e) {
					throw new JsonProcessingError("JsonProccessingError: " + e.getLocalizedMessage());
				} catch (Exception ex) {
					throw new InternalServerException("Error: " + ex.getLocalizedMessage());
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
					throw new InternalServerException("Check Mcc not selected to single");
				}

			} else {
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
			}
			if (mccmncList.size() > 0) {
				Integer totalRecord = mccmncList.size();
				Integer remained = 0;
				count = insertMccMnc(mccmncList);
				if (count == totalRecord) {
					target = IConstants.SUCCESS_KEY;
					logger.info("message: DBUploadSuccess");
					// request.setAttribute("param_value", count + "");
					logger.info("param_value", count + "");
					return new ResponseEntity<String>("Network Entry Saved Successful!", HttpStatus.CREATED);
				} else {
					remained = totalRecord - count;
					target = IConstants.FAILURE_KEY;
					logger.error("message: DBEntryFailure");
					// request.setAttribute("param_value", remained + "");
					logger.info("param_value", remained + "");
				}
			}
			if (replaceList.size() > 0) {
				target = "replace";
				String param_message = "Total Inserted Records : " + count;
				logger.error("error: record duplicate");
				// request.setAttribute("param_message", param_message);
				logger.error("param_message", param_message);
				// request.setAttribute("list", replaceList);
				logger.error("list", replaceList);
				// request.setAttribute("totalRecords", replaceList.size() + "");
				logger.error("totalRecords", replaceList.size() + "");
				throw new InternalServerException("Duplicate Entry Found!");
			}
			MultiUtility.changeFlag(Constants.NETWORK_FLAG_FILE, "707");
		} catch (Exception e) {
			logger.info("Error");
			throw new InternalServerException(e.getMessage());
		}
		return new ResponseEntity<>(target, HttpStatus.CREATED);
	}

	private int checkDuplicateMccMnc(MccMncDTO mccMncDTO) {
		int id = 0;
		String mcc = mccMncDTO.getMcc();
		List<String> mncList = extractMncList(mccMncDTO.getMnc());
boolean flag = false;
		
		List<NetworkEntry> l = this.networkEntryRepo.findByMcc(mcc);

		for(NetworkEntry ne : l) {
			String mnc = ne.getMnc();
			if(mnc.contains(",")) {
				String[] mncArray = mnc.split(",");
				for(String m : mncArray) {
					if(mncList.contains(m)) {
						flag = true;
						break;
					}
				}
			}else {
				if(mncList.contains(mnc)) {
					flag = true;
				}
			}
			if(flag) {
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

	@Override
	public ResponseEntity<String> replace(MccMncUpdateForm form, String username) {

		Optional<UserEntry> userOptional = this.userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}

		String target = null;
		MccMncUpdateDTO updateDTO = new MccMncUpdateDTO();
		MccMncDTO mncDTO = null;
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
				int count = updateMccMnc(list);
				logger.info("message: DBUpdateSuccess");
				// request.setAttribute("param_value", count + "");
				logger.info("param_value", count + "");
				target = IConstants.SUCCESS_KEY;
				MultiUtility.changeFlag(Constants.NETWORK_FLAG_FILE, "707");
			} else {
				logger.error("error: record unavailable");
				target = IConstants.FAILURE_KEY;
				throw new NotFoundException("Record unavailable!");
			}

		} catch (NotFoundException e) {
			logger.error("error: processError");
			target = IConstants.FAILURE_KEY;
			logger.error("Exception:ReplaceMccMncAction(1)::" + e);
			throw new NotFoundException(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error("error: processError");
			target = IConstants.FAILURE_KEY;
			logger.error("Exception:ReplaceMccMncAction(1)::" + e);
			throw new InternalServerException(e.getLocalizedMessage());
		}
		return new ResponseEntity<String>(target, HttpStatus.CREATED);
	}

	public int updateMccMnc(ArrayList<MccMncDTO> list) {
		int count = 0;
		NetworkEntry entry = null;
		for (MccMncDTO data : list) {
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
		}
		return count;
	}

	@Override
	@Transactional
	public ResponseEntity<String> delete(List<Integer> ids, String username) {

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

		int count = ids.size();
		String target = null;
		try {
			if (!ids.isEmpty()) {
				this.networkEntryRepo.deleteByIdIn(ids);
				logger.info("Deleted Records: " + count);
				target = IConstants.SUCCESS_KEY;
				MultiUtility.changeFlag(Constants.NETWORK_FLAG_FILE, "707");
			} else {
				logger.error("error: record unavailable");
				target = IConstants.FAILURE_KEY;
			}

		} catch (Exception ex) {
			count = 0;
			logger.error("Exception in deleteMccMnc()", ex);
			target = IConstants.FAILURE_KEY;
			throw new InternalServerException(ex.getLocalizedMessage());
		}

		return new ResponseEntity<String>(target, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<MccMncDTO>> search(String ccReq, String mccReq, String mncReq, String checkCountryReq,
			String checkMccReq, String checkMncReq, String username) {

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

		String target = null;
		String checkCountry = "", checkMcc = "", checkMnc = "";
		String cc = "%", mcc = "%", mnc = "%";
		ArrayList<MccMncDTO> list = null;
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

			list = editMccMnc(cc, mcc, mnc);

		} catch (Exception e) {
			target = IConstants.FAILURE_KEY;
			logger.error(e.toString());
			throw new InternalServerException(e.getLocalizedMessage());
		}
		if (list.isEmpty()) {
			throw new NotFoundException("MccMnc data not found! List is empty!");
		} else {
			logger.info("Total records found: " + list.size());
			target = IConstants.SUCCESS_KEY;
			return ResponseEntity.ok(list);
		}

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

	@Override
	public ResponseEntity<byte[]> download(String ccReq, String mccReq, String mncReq, String checkCountryReq,
			String checkMccReq, String checkMncReq, String username) {

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
			String file = this.filePath + fileName;
			FileContentGenerator contentGenerator = new FileContentGenerator();
			contentGenerator.createMccMncContent(list, file);
			logger.info("<---- Preparing for Download ---> ");
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

	@Override
	public ResponseEntity<?> editMccMnc(String username) {

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
				return ResponseEntity.ok(networkmap);
			} else {
				logger.error("error: record unavailable");
				target = IConstants.FAILURE_KEY;
				throw new NotFoundException("No record found in network map!");
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

	@Override
	public ResponseEntity<?> uploadUpdateMccMnc(MultipartFile file, String username) {

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

		String target = null;
		int count = 0;
		try {
			FileDataParser fileDataParser = new FileDataParser();
			ArrayList<MccMncDTO> tempList = fileDataParser.getMccMncList(file);
			if (tempList.size() > 0) {
				count = updateMccMnc(tempList);
				if (tempList.size() == count) {
					target = IConstants.SUCCESS_KEY;
					logger.info("message.DBUpdateSuccess");
					// request.setAttribute("param_value", count + "");
					logger.info("param_value", count + "");
				} else {
					int remained = tempList.size() - count;
					target = IConstants.FAILURE_KEY;
					logger.error("message.DBUpdateFailure");
					// request.setAttribute("param_value", remained + "");
					logger.error("param_value", remained + "");
				}
				MultiUtility.changeFlag(Constants.NETWORK_FLAG_FILE, "707");
			} else {
				target = IConstants.FAILURE_KEY;
				logger.error(target, "Error fetching data or list is empty!");
				throw new NotFoundException("No data found in MccMncDTO list!");
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
		return new ResponseEntity<>(target, HttpStatus.CREATED);
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

	@Override
	public ResponseEntity<MncMccTokens> findOption(String countryName, String mccParam, String username) {

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
			throw new InternalServerException(e.getLocalizedMessage());
		}
		return ResponseEntity.ok(mncMccTokens);
	}

}
