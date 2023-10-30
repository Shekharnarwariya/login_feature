package com.hti.smpp.common.dto;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

@Service
public class Data_Base {

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public boolean createBulkMgmtContentTable(int batchId, List<BulkMgmtContent> bulkMgmtContentList) {
		try {
			String tableName = "bulk_mgmt_content_" + batchId;
			String sql = "create table " + tableName
					+ "  (id int(7) unsigned NOT NULL AUTO_INCREMENT,destination bigint(16) NOT NULL,content text NOT NULL,flag char(1) DEFAULT 'F',PRIMARY KEY (id))";

			entityManager.createNativeQuery(sql).executeUpdate();

			entityManager.persist(bulkMgmtContentList);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
