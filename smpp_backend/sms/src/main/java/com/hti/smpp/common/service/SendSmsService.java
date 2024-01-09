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
			fobj = new ObjectInputStream(new FileInputStream(IConstants.WEBSMPP_EXT_DIR + "schedule//" + filename));
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
			f = new File(IConstants.WEBSMPP_EXT_DIR + "schedule//" + filename);
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
