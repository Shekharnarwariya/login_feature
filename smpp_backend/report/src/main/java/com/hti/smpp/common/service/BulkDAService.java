package com.hti.smpp.common.service;

import java.util.List;

import com.hti.smpp.common.messages.dto.BulkContentEntry;
import com.hti.smpp.common.messages.dto.BulkEntry;
import com.hti.smpp.common.messages.dto.BulkMapEntry;

public interface BulkDAService {
//	public BulkEntry getEntry(int id);
//
//	public int save(BulkEntry entry)  throws Exception;
//
//	public void update(BulkEntry entry) throws Exception;
//
//	public void saveContent(int batch_id, List<BulkContentEntry> list) throws Exception;

	//public void saveContent(int batch_id, List<BulkContentEntry> list, ProgressEvent progressEvent) throws Exception;

//	public void updateContent(int batch_id, List<BulkContentEntry> list);
//
//	public List<BulkContentEntry> listContent(int batch_id);
//
//	public long rowCount(int batch_id);

	public List<BulkEntry> listBatches();

	//public List<BulkMapEntry> listDistinctEntry();

	public List<BulkMapEntry> list(String campaign, String[] systemId);

	public List<BulkMapEntry> list(String[] systemId, long from, long to);
}
