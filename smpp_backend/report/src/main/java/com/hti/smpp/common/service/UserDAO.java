package com.hti.smpp.common.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.user.dto.RechargeEntry;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.validation.Configuration;

@Service
public class UserDAO {
	
	  private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
	  @Autowired
	    private SessionFactory sessionFactory;

	   public List<RechargeEntry> listTransactions(Integer[] userId, String txnType, String startTime, String endTime) {
	        Transaction transaction = null;
	        List<RechargeEntry> results = new ArrayList<>();

	        try (Session session = sessionFactory.openSession()) {
	            transaction = session.beginTransaction();

	            CriteriaBuilder builder = session.getCriteriaBuilder();
	            CriteriaQuery<RechargeEntry> query = builder.createQuery(RechargeEntry.class);
	            Root<RechargeEntry> root = query.from(RechargeEntry.class);
	            
//	            Configuration configuration = new Configuration().configure(); // Loads hibernate.cfg.xml
//	            StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
//	                .applySettings(configuration.getProperties());
//	            SessionFactory sessionFactory = configuration.buildSessionFactory(builder.build());
	            
	            if (userId != null && userId.length > 0) {
	            	query.where(root.get("userId").in((Object[]) userId));
	            }
	            if (txnType != null) {
	            	query.where(builder.like(root.get("txnType"), "%" + txnType + "%"));
	            }
	            if (startTime != null && endTime != null) {
	            	query.where(builder.between(root.get("time"), startTime, endTime));
	            }
	            results = session.createQuery(query).getResultList();
	            transaction.commit();

	            logger.info("Transactions fetched: {}", results.size());
	        } catch (Exception e) {
	            if (transaction != null) {
	                transaction.rollback();
	            }
	            logger.error("Error retrieving transactions: {}", e.getMessage());
	            return Collections.emptyList();
	        }
	        return results;
	    }
			

}
