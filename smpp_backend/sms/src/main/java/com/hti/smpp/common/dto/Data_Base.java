package com.hti.smpp.common.dto;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class Data_Base {

	private static final Logger logger = LoggerFactory.getLogger(Data_Base.class);

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public boolean createBulkMgmtContentTable(int batchId, List<BulkMgmtContent> bulkMgmtContentList) {
		try {
			String tableName = "bulk_mgmt_content_" + batchId;
			{
				createTable(tableName);
				persistEntities(bulkMgmtContentList);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error creating table or persisting entities: {}", e.getMessage());
			return false;
		}
	}

	private void createTable(String tableName) {
		String sql = "create table " + tableName
				+ "  (id int(7) unsigned NOT NULL AUTO_INCREMENT,destination bigint(16) NOT NULL,content text NOT NULL,flag char(1) DEFAULT 'F',PRIMARY KEY (id))";
		entityManager.createNativeQuery(sql).executeUpdate();
	}

	private void persistEntities(List<BulkMgmtContent> bulkMgmtContentList) {
		for (BulkMgmtContent content : bulkMgmtContentList) {
			entityManager.persist(content);
		}
	}

}
