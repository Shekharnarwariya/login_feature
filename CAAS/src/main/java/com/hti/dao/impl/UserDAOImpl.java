package com.hti.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.dao.UserDAO;
import com.hti.user.dto.BalanceEntry;
import com.hti.user.dto.DlrSettingEntry;
import com.hti.user.dto.ProfessionEntry;
import com.hti.user.dto.UserEntry;
import com.hti.user.dto.WebMasterEntry;

public class UserDAOImpl implements UserDAO {
	private Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);
	private SessionFactory sessionFactory;
	private Session session;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public List<UserEntry> listUser() {
		logger.debug("listing User Entries");
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<UserEntry> list = session.createCriteria(UserEntry.class).list();
			logger.debug("UserEntry Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<UserEntry> listUser(Integer[] user_id) {
		logger.debug("listing User Entries: " + user_id.length);
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			Criteria cr = session.createCriteria(UserEntry.class).add(Restrictions.in("id", user_id));
			@SuppressWarnings("unchecked")
			List<UserEntry> list = cr.list();
			logger.debug("UserEntry Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<BalanceEntry> listBalance() {
		logger.debug("listing Balance Entries");
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<BalanceEntry> list = session.createCriteria(BalanceEntry.class).list();
			logger.debug("Balance Entry Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public void updateBalance(BalanceEntry entry) {
		session = sessionFactory.openSession();
		session.beginTransaction();
		try {
			session.update(entry);
			session.getTransaction().commit();
		} catch (Exception ex) {
			logger.error(entry.toString(), ex);
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<ProfessionEntry> listProfession() {
		logger.debug("listing Profession Entries");
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<ProfessionEntry> list = session.createCriteria(ProfessionEntry.class).list();
			logger.debug("ProfessionEntry Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<WebMasterEntry> listWebMaster() {
		logger.debug("listing WebMaster Entries");
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<WebMasterEntry> list = session.createCriteria(WebMasterEntry.class).list();
			logger.debug("WebMasterEntry Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<DlrSettingEntry> listDlrSetting() {
		logger.debug("listing DlrSetting Entries");
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<DlrSettingEntry> list = session.createCriteria(DlrSettingEntry.class).list();
			logger.debug("DlrSettingEntry Found:" + list.size());
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public List<UserEntry> listUsernames() {
		logger.debug("listing Usernames");
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			Criteria criteria = session.createCriteria(UserEntry.class);
			criteria.setProjection(Projections.projectionList().add(Projections.property("id"), "id")
					.add(Projections.property("systemId"), "systemId"));
			criteria.setResultTransformer(new AliasToBeanResultTransformer(UserEntry.class));
			@SuppressWarnings("unchecked")
			List<UserEntry> list = criteria.list();
			logger.debug("Usernames Found:" + list);
			session.getTransaction().commit();
			return list;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public UserEntry getUserEntry(int user_id) {
		logger.info("Checking For UserEntry: " + user_id);
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			UserEntry entry = (UserEntry) session.load(UserEntry.class, new Integer(user_id));
			logger.info("User Entry Found:" + entry);
			session.getTransaction().commit();
			return entry;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public BalanceEntry getBalance(int user_id) {
		logger.info("Checking For BalanceEntry: " + user_id);
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			BalanceEntry entry = (BalanceEntry) session.load(BalanceEntry.class, new Integer(user_id));
			logger.info("BalanceEntry Found:" + entry);
			session.getTransaction().commit();
			return entry;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public ProfessionEntry getProfessionEntry(int user_id) {
		logger.info("Checking For ProfessionEntry: " + user_id);
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			ProfessionEntry entry = (ProfessionEntry) session.load(ProfessionEntry.class, new Integer(user_id));
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
	public WebMasterEntry getWebMasterEntry(int user_id) {
		logger.info("Checking For WebMasterEntry: " + user_id);
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			WebMasterEntry entry = (WebMasterEntry) session.load(WebMasterEntry.class, new Integer(user_id));
			logger.info("WebMasterEntry Found:" + entry);
			session.getTransaction().commit();
			return entry;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public DlrSettingEntry getDlrSettingEntry(int user_id) {
		logger.info("Checking For DlrSettingEntry: " + user_id);
		session = sessionFactory.openSession();
		try {
			session.beginTransaction();
			DlrSettingEntry entry = (DlrSettingEntry) session.load(DlrSettingEntry.class, new Integer(user_id));
			logger.info("DlrSettingEntry Found:" + entry);
			session.getTransaction().commit();
			return entry;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}
}
