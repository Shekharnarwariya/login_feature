package com.hti.smpp.common.service.impl;

import java.util.Collections;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.messages.dto.BulkEntry;
import com.hti.smpp.common.messages.dto.BulkMapEntry;
import com.hti.smpp.common.service.BulkDAService;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Service
public class BulkDAServiceImpl implements BulkDAService {
	// private BulkDAO bulkDAO;
	private Logger logger = LoggerFactory.getLogger(BulkDAServiceImpl.class);

	private SessionFactory sessionFactory;

	public BulkDAServiceImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

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
		Transaction transaction = null;
		try (Session session = sessionFactory.openSession()) {
			transaction = session.beginTransaction();

			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<BulkMapEntry> query = builder.createQuery(BulkMapEntry.class);
			Root<BulkMapEntry> root = query.from(BulkMapEntry.class);

			if (campaign != null) {
				query.where(builder.equal(root.get("name"), campaign));
			}
			if (systemId != null && systemId.length > 0) {
				query.where(root.get("systemId").in((Object[]) systemId));
			}

			List<BulkMapEntry> list = session.createQuery(query).getResultList();

			transaction.commit();

			logger.info("{} campaign entries: {}", campaign, list.size());
			return list;
		} catch (Exception e) {
			if (transaction != null) {
				transaction.rollback();
			}
			logger.error("Error retrieving campaign entries for {}: {}", campaign, e.getMessage());
			return Collections.emptyList();
		}
	}

	@Override
	public List<BulkMapEntry> list(String[] systemId, long from, long to) {
		logger.info("Checking for systemId: {} From: {} To: {}", systemId, from, to);
		Transaction transaction = null;
		try (Session session = sessionFactory.openSession()) {
			transaction = session.beginTransaction();

			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<BulkMapEntry> query = builder.createQuery(BulkMapEntry.class);
			Root<BulkMapEntry> root = query.from(BulkMapEntry.class);

			// Build the conditions (predicates) based on the method parameters
			Predicate conditions = builder.conjunction();
			if (systemId != null && systemId.length > 0) {
				conditions = builder.and(conditions, root.get("systemId").in((Object[]) systemId));
			}
			if (from > 0 && to > 0) {
				conditions = builder.and(conditions, builder.between(root.get("msgid"), from, to));
			}
			query.where(conditions);

			List<BulkMapEntry> list = session.createQuery(query).getResultList();

			transaction.commit();
			logger.info("Campaign entries: {}", list.size());
			return list;
		} catch (Exception e) {
			if (transaction != null) {
				transaction.rollback();
			}
			logger.error("Error retrieving campaign entries: {}", e.getMessage());
			return Collections.emptyList();
		}
	}
}


