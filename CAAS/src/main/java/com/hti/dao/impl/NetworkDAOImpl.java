package com.hti.dao.impl;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.dao.NetworkDAO;
import com.hti.network.dto.NetworkEntry;

public class NetworkDAOImpl implements NetworkDAO {
	private Logger logger = LoggerFactory.getLogger(NetworkDAOImpl.class);
	private SessionFactory sessionFactory;
	private Session session;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public List<NetworkEntry> list() {
		logger.debug("listing Network Records");
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<NetworkEntry> list = session.createCriteria(NetworkEntry.class).add(Restrictions.gt("id", 0)).list();
			logger.debug("Network Records Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}
}
