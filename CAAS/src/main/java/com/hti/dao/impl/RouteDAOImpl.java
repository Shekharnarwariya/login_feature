package com.hti.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.dao.RouteDAO;
import com.hti.route.dto.HlrRouteEntry;
import com.hti.route.dto.MmsRouteEntry;
import com.hti.route.dto.OptionalRouteEntry;
import com.hti.route.dto.RouteEntry;

public class RouteDAOImpl implements RouteDAO {
	private Logger logger = LoggerFactory.getLogger(RouteDAOImpl.class);
	private SessionFactory sessionFactory;
	private Session session;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public List<RouteEntry> listBasic() {
		logger.debug("listing Basic Routing");
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<RouteEntry> list = session.createCriteria(RouteEntry.class).list();
			logger.debug("RouteEntry Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<RouteEntry> listBasic(int user_id) {
		logger.debug("listing Basic Routing For User: " + user_id);
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			Criteria cr = session.createCriteria(RouteEntry.class).add(Restrictions.in("userId", user_id));
			@SuppressWarnings("unchecked")
			List<RouteEntry> list = cr.list();
			logger.debug("User[" + user_id + "] RouteEntry Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}
	
	@Override
	public List<RouteEntry> listBasic(Integer[] user_id) {
		logger.debug("listing Basic Routing For Users: " + user_id.length);
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			Criteria cr = session.createCriteria(RouteEntry.class).add(Restrictions.in("userId", user_id));
			@SuppressWarnings("unchecked")
			List<RouteEntry> list = cr.list();
			logger.debug("Users[" + user_id.length + "] RouteEntry Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<OptionalRouteEntry> listOptional() {
		logger.debug("listing Optional Routing");
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<OptionalRouteEntry> list = session.createCriteria(OptionalRouteEntry.class).list();
			logger.debug("OptionalRouteEntry Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<HlrRouteEntry> listHlr() {
		logger.debug("listing Hlr Routing");
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<HlrRouteEntry> list = session.createCriteria(HlrRouteEntry.class).list();
			logger.debug("HlrRouteEntry Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<OptionalRouteEntry> listOptional(Integer[] route_id) {
		logger.debug("listing Optional Routing");
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<OptionalRouteEntry> list = session.createCriteria(OptionalRouteEntry.class)
					.add(Restrictions.in("routeId", route_id)).list();
			logger.debug("OptionalRouteEntry Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<HlrRouteEntry> listHlr(Integer[] route_id) {
		logger.debug("listing Hlr Routing");
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<HlrRouteEntry> list = session.createCriteria(HlrRouteEntry.class)
					.add(Restrictions.in("routeId", route_id)).list();
			logger.debug("HlrRouteEntry Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<MmsRouteEntry> listMms() {
		logger.debug("listing Mms Routing");
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<MmsRouteEntry> list = session.createCriteria(MmsRouteEntry.class).list();
			logger.debug("MmsRouteEntry Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<MmsRouteEntry> listMms(Integer[] route_id) {
		logger.debug("listing Mms Routing");
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<MmsRouteEntry> list = session.createCriteria(MmsRouteEntry.class)
					.add(Restrictions.in("routeId", route_id)).list();
			logger.debug("MmsRouteEntry Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}
}
/*
 * @Override public List<ProfileAllocEntry> listProfileAllocation() { logger.debug("listing Routing Profile Allocation"); session = sessionFactory.openSession(); try { session.beginTransaction();
 * 
 * @SuppressWarnings("unchecked") List<ProfileAllocEntry> list = session.createCriteria(ProfileAllocEntry.class).list(); logger.debug("ProfileAllocEntry Found:" + list.size());
 * session.getTransaction().commit(); return list; } finally { if (session.isOpen()) { session.close(); } } }
 * 
 * @Override public List<ProfileAllocEntry> listProfileAllocation(int userId) { logger.debug("listing Routing Profile Allocation"); session = sessionFactory.openSession(); try {
 * session.beginTransaction();
 * 
 * @SuppressWarnings("unchecked") List<ProfileAllocEntry> list = session.createCriteria(ProfileAllocEntry.class) .add(Restrictions.in("userId", userId)).list(); logger.debug("ProfileAllocEntry Found:"
 * + list.size()); session.getTransaction().commit(); return list; } finally { if (session.isOpen()) { session.close(); } } } }
 */