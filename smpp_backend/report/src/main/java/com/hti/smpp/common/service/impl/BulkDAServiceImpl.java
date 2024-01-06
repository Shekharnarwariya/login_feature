package com.hti.smpp.common.service.impl;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.smpp.common.messages.dto.BulkContentEntry;
import com.hti.smpp.common.messages.dto.BulkEntry;
import com.hti.smpp.common.messages.dto.BulkMapEntry;
import com.hti.smpp.common.service.BulkDAService;
import com.hti.smpp.common.util.GlobalVars;



public class BulkDAServiceImpl implements BulkDAService {
	//private BulkDAO bulkDAO;
	private Logger logger = LoggerFactory.getLogger(BulkDAServiceImpl.class);

//	public BulkDAServiceImpl() {
//		this.bulkDAO = GlobalVars.context.getBean(BulkDAO.class);
//	}
//
//	@Override
//	public BulkEntry getEntry(int id) {
//		return bulkDAO.getEntry(id);
//	}
//
//	@Override
//	public int save(BulkEntry entry) throws DuplicateEntryException, Exception {
//		return bulkDAO.save(entry);
//	}
//
//	@Override
//	public void update(BulkEntry entry) throws Exception {
//		bulkDAO.update(entry);
//	}
//
//	@Override
//	public void saveContent(int batch_id, List<BulkContentEntry> list) throws Exception {
//		bulkDAO.saveContent(batch_id, list);
//	}
//
//	@Override
//	public void saveContent(int batch_id, List<BulkContentEntry> list, ProgressEvent progressEvent) throws Exception {
//		bulkDAO.saveContent(batch_id, list, progressEvent);
//	}
//
//	@Override
//	public void updateContent(int batch_id, List<BulkContentEntry> list) {
//		bulkDAO.updateContent(batch_id, list);
//	}

//	@Override
//	public List<BulkContentEntry> listContent(int batch_id) {
//		return bulkDAO.listContent(batch_id);
//	}
//
//	@Override
//	public long rowCount(int batch_id) {
//		return bulkDAO.rowCount(batch_id);
//	}

	@Override
	public List<BulkEntry> listBatches() {
		return listBatches();
	}

//	@Override
//	public List<BulkMapEntry> listDistinctEntry() {
//		logger.info("listing distinct campaigns");
//		IDatabaseService dbService = HtiSmsDB.getInstance();
//		return dbService.listDistinctCampaign();
//	}

	@Override
	public List<BulkMapEntry> list(String campaign, String[] systemId) {
		return list(campaign, systemId);
	}

	@Override
	public List<BulkMapEntry> list(String[] systemId, long from, long to) {
		return list(systemId, from, to);
	}
}
