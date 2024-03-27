package com.hti.dao.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.dao.UserDAO;
import com.hti.dao.UserDAService;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.DlrSettingEntry;
import com.hti.smpp.common.user.dto.ProfessionEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.util.GlobalVar;

public class UserDAServiceImpl implements UserDAService {
	private UserDAO userDAO;
	private Logger logger = LoggerFactory.getLogger(UserDAServiceImpl.class);

	public UserDAServiceImpl() {
		this.userDAO = GlobalVar.context.getBean(UserDAO.class);
	}

	@Override
	public Map<Integer, UserEntry> listUser() {
		Map<Integer, UserEntry> map = new HashMap<Integer, UserEntry>();
		List<UserEntry> list = userDAO.listUser();
		for (UserEntry entry : list) {
			String accessIp = entry.getAccessIp();
			if (accessIp != null && accessIp.length() > 0) {
				String[] ip_arr = accessIp.split(",");
				Set<String> valid_ip_set = new HashSet<String>();
				for (String ip : ip_arr) {
					if (ip.indexOf("/") > 0) {
						logger.debug(entry.getSystemId() + " Access Ip Range Configured: " + ip);
						valid_ip_set.add(ip);
					} else {
						if (isValidIPV4(ip)) {
							valid_ip_set.add(ip);
						} else {
							logger.info(entry.getSystemId() + " Invalid Access Ip Configured: " + ip);
						}
					}
				}
				if (valid_ip_set.isEmpty()) {
					entry.setAccessIp(null);
				} else {
					entry.setAccessIp(String.join(",", valid_ip_set));
				}
			} else {
				entry.setAccessIp(null);
			}
			map.put(entry.getId(), entry);
		}
		return map;
	}

	@Override
	public Map<Integer, UserEntry> listUser(Integer[] user_id) {
		Map<Integer, UserEntry> map = new HashMap<Integer, UserEntry>();
		List<UserEntry> list = userDAO.listUser(user_id);
		for (UserEntry entry : list) {
			String accessIp = entry.getAccessIp();
			if (accessIp != null && accessIp.length() > 0) {
				String[] ip_arr = accessIp.split(",");
				Set<String> valid_ip_set = new HashSet<String>();
				for (String ip : ip_arr) {
					if (ip.indexOf("/") > 0) {
						logger.debug(entry.getSystemId() + " Access Ip Range Configured: " + ip);
						valid_ip_set.add(ip);
					} else {
						if (isValidIPV4(ip)) {
							valid_ip_set.add(ip);
						} else {
							logger.info(entry.getSystemId() + " Invalid Access Ip Configured: " + ip);
						}
					}
				}
				if (valid_ip_set.isEmpty()) {
					entry.setAccessIp(null);
				} else {
					entry.setAccessIp(String.join(",", valid_ip_set));
				}
			} else {
				entry.setAccessIp(null);
			}
			map.put(entry.getId(), entry);
		}
		return map;
	}

	private boolean isValidIPV4(String ipAddr) {
		Pattern ptn = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
		return ptn.matcher(ipAddr).find();
	}

	@Override
	public Map<Integer, BalanceEntry> listBalance() {
		Map<Integer, BalanceEntry> map = new HashMap<Integer, BalanceEntry>();
		List<BalanceEntry> list = userDAO.listBalance();
		for (BalanceEntry entry : list) {
			map.put(entry.getUserId(), entry);
		}
		return map;
	}

	@Override
	public Map<Integer, ProfessionEntry> listProfession() {
		Map<Integer, ProfessionEntry> map = new HashMap<Integer, ProfessionEntry>();
		List<ProfessionEntry> list = userDAO.listProfession();
		for (ProfessionEntry entry : list) {
			map.put(entry.getUserId(), entry);
		}
		return map;
	}

	@Override
	public Map<Integer, WebMasterEntry> listWebMaster() {
		Map<Integer, WebMasterEntry> map = new HashMap<Integer, WebMasterEntry>();
		List<WebMasterEntry> list = userDAO.listWebMaster();
		for (WebMasterEntry entry : list) {
			map.put(entry.getUserId(), entry);
		}
		return map;
	}

	@Override
	public Map<Integer, DlrSettingEntry> listDlrSetting() {
		Map<Integer, DlrSettingEntry> map = new HashMap<Integer, DlrSettingEntry>();
		List<DlrSettingEntry> list = userDAO.listDlrSetting();
		for (DlrSettingEntry entry : list) {
			String custom_gmt = entry.getCustomGmt();
			if (custom_gmt != null && custom_gmt.length() == 6 && custom_gmt.contains(":")) {
				// valid gmt format
			} else {
				entry.setCustomGmt(null);
			}
			if (entry.isWebDlr()) {
				String web_url = entry.getWebUrl();
				if (web_url != null && web_url.trim().length() > 0) {
					web_url = web_url.trim();
					if (!web_url.startsWith("http")) {
						logger.error(entry.getUserId() + " Invalid Web Url Configured<" + web_url + ">");
						entry.setWebDlr(false);
					}
				} else {
					logger.error(entry.getUserId() + " Invalid Web Url Configured<" + web_url + ">");
					entry.setWebDlr(false);
				}
			}
			map.put(entry.getUserId(), entry);
		}
		return map;
	}

	@Override
	public Map<String, Integer> listUsernames() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		List<UserEntry> list = userDAO.listUsernames();
		for (UserEntry entry : list) {
			map.put(entry.getSystemId(), entry.getId());
		}
		return map;
	}

	@Override
	public UserEntry getUserEntry(int user_id) {
		return userDAO.getUserEntry(user_id);
	}

	@Override
	public BalanceEntry getBalance(int user_id) {
		return userDAO.getBalance(user_id);
	}

	@Override
	public ProfessionEntry getProfessionEntry(int user_id) {
		return userDAO.getProfessionEntry(user_id);
	}

	@Override
	public WebMasterEntry getWebMasterEntry(int user_id) {
		return userDAO.getWebMasterEntry(user_id);
	}

	@Override
	public DlrSettingEntry getDlrSettingEntry(int user_id) {
		return userDAO.getDlrSettingEntry(user_id);
	}
}
