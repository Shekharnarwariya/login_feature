package com.hti.smpp.common.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.messages.dto.BulkSmsDTO;
import com.hti.smpp.common.util.IConstants;

@Service
public class SendSmsService {

	private Logger logger = LoggerFactory.getLogger(SendSmsService.class);

	public BulkSmsDTO readScheduleFile(String filename) {
		ObjectInputStream fobj = null;
		BulkSmsDTO bulk = null;
		try {
			fobj = new ObjectInputStream(new FileInputStream(IConstants.SCHEDULE_DIR + filename));
			bulk = (BulkSmsDTO) fobj.readObject();
		} catch (Exception e) {
			logger.error(filename, e.fillInStackTrace());
		} finally {
			if (fobj != null) {
				try {
					fobj.close();
				} catch (IOException ex) {
				}
			}
		}
		return bulk;
	}

	public String createScheduleFile(BulkSmsDTO bulkDTO) {
		String filename = null;
		File f = null;
		FileOutputStream fin = null;
		ObjectOutputStream fobj = null;
		bulkDTO.setCampaignType("Scheduled");
		System.out.println("Org: " + bulkDTO.getOrigMessage());
		try {
			filename = bulkDTO.getSystemId() + "_" + new SimpleDateFormat("ddMMyyyyHHmmssSSS").format(new Date());
			f = new File(IConstants.SCHEDULE_DIR + filename);
			if (!f.exists()) {
				fin = new FileOutputStream(f);
				fobj = new ObjectOutputStream(fin);
				fobj.writeObject(bulkDTO);
				logger.info(bulkDTO.getSystemId() + ":" + f.getName() + " Schedule Created");
			} else {
				filename = null;
				logger.error(bulkDTO.getSystemId() + ":" + f.getName() + " Schedule Exist");
			}
		} catch (IOException e) {
			filename = null;
			logger.error(bulkDTO.getSystemId(), e.fillInStackTrace());
		} finally {
			if (fobj != null) {
				try {
					fobj.close();
				} catch (IOException ex) {
					fobj = null;
				}
			}
		}
		return filename;
	}

//	public String sendCoverageTest(BulkSmsDTO smsDTO, String smsc) {
//		String msgId = null;
//		try {
//			msgId = new Client().sendCoverageTest(smsDTO, smsc);
//		} catch (Exception e) {
//			// System.out.println("SendSmsService:sendSms::");
//			e.printStackTrace();
//		}
//		return msgId;
//	}

//	public String sendAlert(BulkSmsDTO smsDTO) throws DBException {
//		String msgId = null;
//		try {
//			msgId = new Client().sendAlert(smsDTO);
//		} catch (Exception e) {
//			logger.error(e.getMessage());
//		}
//		return msgId;
//	}

	/*
	 *
	 * For Re Send SMSC Developed by Sameer
	 *
	 */
	//////////////////////////////////////////////////////////////////////////////////
//	public String sendBulkSms(BulkSmsDTO bulkSmsDTO, boolean waitForApprove) throws DBException {
//		String response = "";
//		try {
//			// bulkSmsDTO.setSystemId(userSessionObject.getSystemId());
//			// bulkSmsDTO.setPassword(userSessionObject.getPassword());
//			Client client = new Client();
//			response = client.sendBulkMsg(bulkSmsDTO, waitForApprove);
//		} catch (Exception e) {
//			logger.error(bulkSmsDTO.getSystemId(), e.fillInStackTrace());
//		}
//		return response;
//	}
//
//	public String sendBulkSms(BulkSmsDTO bulkSmsDTO, ProgressEvent progressEvent, boolean waitForApprove)
//			throws DBException {
//		String response = "";
//		try {
//			// bulkSmsDTO.setSystemId(userSessionObject.getSystemId());
//			// bulkSmsDTO.setPassword(userSessionObject.getPassword());
//			Client client = new Client();
//			client.setProgressEvent(progressEvent);
//			response = client.sendBulkMsg(bulkSmsDTO, waitForApprove);
//		} catch (Exception e) {
//			logger.error(bulkSmsDTO.getSystemId(), e.fillInStackTrace());
//		}
//		return response;
//	}

//	public String sendScheduleSms(String file) throws DBException {
//		String toReturn = "Error In Scheduling";
//		try {
//			// String appName = "ScheduleAppl";
//			// System.out.println("Schedule services...");
//			logger.info(" Reading schedule File:-> " + file);
//			BulkSmsDTO bulkSmsDTO = readScheduleFile(file);
//			String mode = bulkSmsDTO.getUserMode();
//			logger.info(file + " [" + bulkSmsDTO.getSystemId() + ":" + bulkSmsDTO.getPassword() + "] " + mode);
//			long credits = 0;
//			double walletAmt = 0.0;
//			double totalWalletCost = bulkSmsDTO.getTotalWalletCost();
//			int user_id = GlobalVars.UserMapping.get(bulkSmsDTO.getSystemId());
//			WebMasterEntry webEntry = GlobalVars.WebmasterEntries.get(user_id);
//			BalanceEntry balance = GlobalVars.BalanceEntries.get(user_id);
//			if (mode.equalsIgnoreCase("credit")) {
//				credits = balance.getCredits();
//			} else {
//				walletAmt = balance.getWalletAmount();
//			}
//			long list = (long) bulkSmsDTO.getDestinationList().size();
//			if (mode.equalsIgnoreCase("credit")) {
//				if (list <= credits) {
//					logger.info(file + " [" + bulkSmsDTO.getSystemId() + "] Sufficient Credits: " + credits);
//					String response = new Client().sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove());
//					toReturn = "Scheduled Successfully" + response;
//				} else {
//					toReturn = "InSufficient Credits";
//					logger.error(file + " [" + bulkSmsDTO.getSystemId() + "] Insufficient Credits: " + credits);
//				}
//			} else if (mode.equalsIgnoreCase("wallet")) {
//				if (totalWalletCost <= walletAmt) {
//					logger.info(file + " [" + bulkSmsDTO.getSystemId() + "] Sufficient Balance: " + walletAmt
//							+ " Required:" + totalWalletCost);
//					String response = new Client().sendBulkMsg(bulkSmsDTO, webEntry.isBulkOnApprove());
//					toReturn = "Scheduled Successfully" + response;
//				} else {
//					toReturn = "InSufficient Wallet";
//					logger.error(file + " [" + bulkSmsDTO.getSystemId() + "] Insufficient Balance: " + walletAmt
//							+ " Required:" + totalWalletCost);
//				}
//			}
//		} catch (Exception e) {
//			logger.error(file, e.fillInStackTrace());
//		}
//		return toReturn;
//	}

	public String getDestination(ArrayList destinationList) {
		String destination = "";
		int len = destinationList.size();
		for (int i = 0; i < len; i++) {
			destination = destination + destinationList.get(i) + ",";
		}
		destination = destination.substring(0, destination.length() - 2);
		return destination;
	}
}
