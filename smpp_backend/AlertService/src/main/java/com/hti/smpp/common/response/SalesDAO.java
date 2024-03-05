package com.hti.smpp.common.response;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hazelcast.transaction.impl.Transaction;

import com.hti.smpp.common.sales.dto.SalesEntry;


import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@Repository
public class SalesDAO {
	
	private Logger logger = LoggerFactory.getLogger(SalesDAO.class);
	@Autowired
	private SessionFactory sessionFactory;
	
	public List<SalesEntry> list(String role) {
		
    List<SalesEntry> results = new ArrayList<>();
    Session session = null;
    Transaction transaction = null;
	
    try {
    	
	        session = sessionFactory.openSession();
	        transaction = (Transaction) session.beginTransaction();

	        CriteriaBuilder builder = session.getCriteriaBuilder();
	        CriteriaQuery<SalesEntry> query = builder.createQuery(SalesEntry.class);
	        Root<SalesEntry> root = query.from(SalesEntry.class);
	
	        // Apply the condition for the 'role'
	        query.where(builder.equal(root.get("role"), role));
	
	        results = session.createQuery(query).getResultList();
	
	        transaction.commit();
	        logger.info("SalesEntry list for role [{}]: {}", role, results.size());
	    } catch (RuntimeException e) {
	        if (transaction != null) {
	            transaction.rollback();
	        }
	        logger.error("Error retrieving SalesEntry records for role " + role + ": " + e.getMessage(), e);
	    } finally {
	        if (session != null && session.isOpen()) {
	            session.close();
	        }
	    }
	
	    return results;
	}

}
