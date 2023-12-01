package com.hti.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.dao.SmscDAO;
import com.hti.smsc.dto.GroupEntry;
import com.hti.smsc.dto.GroupMemberEntry;
import com.hti.smsc.dto.SmscEntry;
import com.hti.smsc.dto.TrafficScheduleEntry;

public class SmscDAOImpl implements SmscDAO {
	private Logger logger = LoggerFactory.getLogger(SmscDAOImpl.class);
	private SessionFactory sessionFactory;
	private Session session;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public List<SmscEntry> list() {
		logger.debug("listing Smsc Entries");
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<SmscEntry> list = session.createCriteria(SmscEntry.class).add(Restrictions.gt("id", 0)).list();
			logger.debug("SmscEntry Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<SmscEntry> listNames() {
		logger.debug("listing names");
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			Criteria criteria = session.createCriteria(SmscEntry.class);
			criteria.add(Restrictions.gt("id", 0));
			criteria.setProjection(Projections.projectionList().add(Projections.property("id"), "id")
					.add(Projections.property("name"), "name"));
			criteria.setResultTransformer(new AliasToBeanResultTransformer(SmscEntry.class));
			@SuppressWarnings("unchecked")
			List<SmscEntry> list = criteria.list();
			logger.debug("Smsc names Found:" + list);
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<GroupMemberEntry> listGroupMember() {
		logger.debug("listing GroupMember Entries");
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<GroupMemberEntry> list = session.createCriteria(GroupMemberEntry.class).list();
			logger.debug("GroupMember Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public SmscEntry getEntry(int smsc_id) {
		logger.info("Checking For SmscEntry: " + smsc_id);
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			SmscEntry entry = (SmscEntry) session.load(SmscEntry.class, new Integer(smsc_id));
			logger.info("ProfessionEntry Found:" + entry);
			session.getTransaction().commit();
			return entry;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<TrafficScheduleEntry> listSchedule() {
		logger.debug("listing Smsc Schedule Entries");
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			@SuppressWarnings({ "deprecation", "unchecked" })
			List<TrafficScheduleEntry> list = session.createCriteria(TrafficScheduleEntry.class)
					.addOrder(Order.asc("smscId")).addOrder(Order.asc("day")).list();
			logger.debug("SmscSchedule Entry Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<GroupEntry> listGroup() {
		logger.debug("listing Group Entries");
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<GroupEntry> list = session.createCriteria(GroupEntry.class).add(Restrictions.gt("id", 0)).list();
			logger.debug("GroupEntry Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}
}
