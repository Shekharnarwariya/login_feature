package com.hti.smpp.common.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.report.dto.ProfitReportEntry;
import com.hti.smpp.common.report.dto.ReportCriteria;
import com.hti.smpp.common.service.ReportDAO;
import com.hti.smpp.common.util.MessageResourceBundle;

import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Service
public class ReportDAOImpl implements ReportDAO {

	private Logger logger = LoggerFactory.getLogger(ReportDAOImpl.class);

	private SessionFactory sessionFactory;

	@Autowired
	private MessageResourceBundle messageResourceBundle;

	// Constructor or setter method to inject the SessionFactory
	public ReportDAOImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public Page<ProfitReportEntry> listProfitReport(ReportCriteria rc, Pageable pageable) {
	    Session session = null;
	    try {
	        session = sessionFactory.openSession();
	        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
	        CriteriaQuery<ProfitReportEntry> criteriaQuery = criteriaBuilder.createQuery(ProfitReportEntry.class);
	        Root<ProfitReportEntry> root = criteriaQuery.from(ProfitReportEntry.class);

	        List<Predicate> predicates = new ArrayList<>();
	        // Your existing predicate logic here...

	        criteriaQuery.select(root).where(predicates.toArray(new Predicate[0]));

	        // Apply sorting and pagination
	        Query query = session.createQuery(criteriaQuery);

	        // Apply pagination
	        int totalRows = query.getResultList().size(); // Get total rows without pagination
	        query.setFirstResult((int) pageable.getOffset());
	        query.setMaxResults(pageable.getPageSize());

	        List<ProfitReportEntry> entries = query.getResultList();

	        logger.info(messageResourceBundle.getLogMessage("profit.report.entries.message"), entries.size());

	        return new PageImpl<>(entries, pageable, totalRows);
	    } finally {
	        if (session != null) {
	            session.close();
	        }
	    }
	}

	
}
