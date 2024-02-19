package com.hti.smpp.common.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.report.dto.ProfitReportEntry;
import com.hti.smpp.common.report.dto.ReportCriteria;
import com.hti.smpp.common.service.ReportDAO;
import com.hti.smpp.common.util.MessageResourceBundle;

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

	public List<ProfitReportEntry> listProfitReport(ReportCriteria rc) {
		logger.info("report Criteria" + rc);

		Session session = null;

		try {
			session = sessionFactory.openSession();
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ProfitReportEntry> query = criteriaBuilder.createQuery(ProfitReportEntry.class);
			Root<ProfitReportEntry> root = query.from(ProfitReportEntry.class);
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(criteriaBuilder.equal(root.get("wallet"), true));

			if (rc.getStartMsgId() > 0 && rc.getEndMsgId() > 0) {
				predicates.add(criteriaBuilder.between(root.get("msgId"), rc.getStartMsgId(), rc.getEndMsgId()));
			}

			if (rc.getResellerId() > 0) {
				predicates.add(criteriaBuilder.equal(root.get("resellerId"), rc.getResellerId()));
			}

			if (rc.getUserId() > 0) {
				predicates.add(criteriaBuilder.equal(root.get("userId"), rc.getUserId()));
			}

			// Build the where clause with the collected predicates
			query.select(root).where(predicates.toArray(new Predicate[0]));

			// Execute the query
			List<ProfitReportEntry> list = session.createQuery(query).getResultList();

			logger.info(messageResourceBundle.getLogMessage("profit.report.entries.message"), list.size());

			// You might need to handle the connection accordingly based on your
			// application's requirements.

			return list;
		} finally {
			// Close the session in a finally block to ensure it's closed even if an
			// exception occurs.
			if (session != null) {
				session.close();
			}
		}
	}

}
