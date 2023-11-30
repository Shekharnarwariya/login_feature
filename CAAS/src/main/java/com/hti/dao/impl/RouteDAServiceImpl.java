package com.hti.dao.impl;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.dao.RouteDAO;
import com.hti.dao.RouteDAService;
import com.hti.route.dto.HlrRouteEntry;
import com.hti.route.dto.MmsRouteEntry;
import com.hti.route.dto.OptionalRouteEntry;
import com.hti.route.dto.RouteEntry;
import com.hti.util.GlobalVar;

public class RouteDAServiceImpl implements RouteDAService {
	private RouteDAO routeDAO;
	private Logger logger = LoggerFactory.getLogger(RouteDAServiceImpl.class);

	public RouteDAServiceImpl() {
		this.routeDAO = GlobalVar.context.getBean(RouteDAO.class);
	}

	@Override
	public Map<Integer, RouteEntry> listBasic() {
		logger.info("<--- listing Basic routing --> ");
		Map<Integer, RouteEntry> map = new HashMap<Integer, RouteEntry>();
		List<RouteEntry> list = routeDAO.listBasic();
		for (RouteEntry entry : list) {
			map.put(entry.getId(), entry);
		}
		logger.info("<--- End listing Basic routing[" + map.size() + "] --> ");
		return map;
	}

	@Override
	public Map<Integer, RouteEntry> listBasic(int user_id) {
		Map<Integer, RouteEntry> map = new HashMap<Integer, RouteEntry>();
		List<RouteEntry> list = routeDAO.listBasic(user_id);
		for (RouteEntry entry : list) {
			map.put(entry.getId(), entry);
		}
		return map;
	}

	@Override
	public Map<Integer, OptionalRouteEntry> listOptional() {
		Map<Integer, OptionalRouteEntry> map = new HashMap<Integer, OptionalRouteEntry>();
		List<OptionalRouteEntry> list = routeDAO.listOptional();
		for (OptionalRouteEntry entry : list) {
			// ---- validate properties of entry ---------------
			if (entry.isReplaceContent()) {
				String replaceContent = entry.getReplacement();
				if (replaceContent != null && replaceContent.length() > 0) {
					Map<String, String> content_key_value = new HashMap<String, String>();
					StringTokenizer tokens = new StringTokenizer(replaceContent, ",");
					try {
						while (tokens.hasMoreTokens()) {
							String part = tokens.nextToken();
							if (part.contains("|")) {
								String key = uniHexToCharMsg(part.substring(0, part.indexOf("|")));
								String value = uniHexToCharMsg(part.substring(part.indexOf("|") + 1, part.length()));
								if (key.length() > 0 && value.length() > 0) {
									logger.debug(entry.getRouteId() + ": " + key + " -> " + value);
									content_key_value.put(key, value);
								}
							}
						}
					} catch (Exception e) {
						logger.error(entry.getRouteId() + "[" + replaceContent + "]", e.fillInStackTrace());
					}
					if (!content_key_value.isEmpty()) {
						logger.debug(entry.getRouteId() + ":" + content_key_value);
						entry.setReplaceContent(true);
						entry.setReplaceContentMap(content_key_value);
					} else {
						entry.setReplaceContent(false);
					}
				} else {
					entry.setReplaceContent(false);
				}
			}
			String sourceAppender = entry.getSourceAppender();
			if (sourceAppender != null && sourceAppender.length() > 0) {
				if (sourceAppender.contains("^") || sourceAppender.contains("$")) {
					// correct format
				} else {
					entry.setSourceAppender(null);
				}
			} else {
				entry.setSourceAppender(null);
			}
			String msgAppender = entry.getMsgAppender();
			if (msgAppender != null && msgAppender.length() > 0) {
				try {
					String msgAppenderConverted = uniHexToCharMsg(msgAppender);
					logger.info("msgAppender: " + msgAppender);
					if (msgAppenderConverted.contains("^") || msgAppenderConverted.contains("$")) {
						entry.setMsgAppender(msgAppenderConverted);
					} else {
						entry.setMsgAppender(null);
					}
				} catch (Exception e) {
					entry.setMsgAppender(null);
					logger.error(entry.getRouteId() + ": [" + msgAppender + "]", e.fillInStackTrace());
				}
			} else {
				entry.setMsgAppender(null);
			}
			// String num_smsc = null, backup_smsc = null, reg_smsc = null;
			// ---- end validation -----------------------------
			map.put(entry.getRouteId(), entry);
		}
		return map;
	}

	@Override
	public Map<Integer, HlrRouteEntry> listHlr() {
		Map<Integer, HlrRouteEntry> map = new HashMap<Integer, HlrRouteEntry>();
		List<HlrRouteEntry> list = routeDAO.listHlr();
		for (HlrRouteEntry entry : list) {
			map.put(entry.getRouteId(), entry);
		}
		return map;
	}

	private String uniHexToCharMsg(String msg) {
		if (msg == null || msg.length() == 0) {
			msg = "0020";
		}
		boolean reqNULL = false;
		byte[] charsByt, var;
		int x = 0;
		try {
			if (msg.substring(0, 2).compareTo("00") == 0) {
				reqNULL = true;
			}
			charsByt = new BigInteger(msg, 16).toByteArray();
			if (charsByt[0] == '\0') {
				var = new byte[charsByt.length - 1];
				for (int q = 1; q < charsByt.length; q++) {
					var[q - 1] = charsByt[q];
				}
				charsByt = var;
			}
			if (reqNULL) {
				var = new byte[charsByt.length + 1];
				x = 0;
				var[0] = '\0';
				reqNULL = false;
			} else {
				var = new byte[charsByt.length];
				x = -1;
			}
			for (int l = 0; l < charsByt.length; l++) {
				var[++x] = charsByt[l];
			}
			msg = new String(var, "UTF-16");
		} catch (Exception ex) {
			logger.error(msg, ex);
		}
		return msg;
	}

	@Override
	public Map<Integer, OptionalRouteEntry> listOptional(Integer[] route_id) {
		logger.info("<--- listing Optional routing --> ");
		Map<Integer, OptionalRouteEntry> map = new HashMap<Integer, OptionalRouteEntry>();
		List<OptionalRouteEntry> list = routeDAO.listOptional(route_id);
		Map<String, Map<String, String>> conversionContent = new HashMap<String, Map<String, String>>();
		for (OptionalRouteEntry entry : list) {
			// ---- validate properties of entry ---------------
			if (entry.isReplaceContent()) {
				String replaceContent = entry.getReplacement();
				if (replaceContent != null && replaceContent.length() > 0) {
					Map<String, String> content_key_value = new HashMap<String, String>();
					if (conversionContent.containsKey(replaceContent)) {
						content_key_value.putAll(conversionContent.get(replaceContent));
					} else {
						StringTokenizer tokens = new StringTokenizer(replaceContent, ",");
						try {
							while (tokens.hasMoreTokens()) {
								String part = tokens.nextToken();
								if (part.contains("|")) {
									String key = uniHexToCharMsg(part.substring(0, part.indexOf("|")));
									String value = uniHexToCharMsg(
											part.substring(part.indexOf("|") + 1, part.length()));
									if (key.length() > 0 && value.length() > 0) {
										content_key_value.put(key, value);
									}
								}
							}
						} catch (Exception e) {
							logger.error(entry.getRouteId() + "[" + replaceContent + "]", e.fillInStackTrace());
						}
						if (!content_key_value.isEmpty()) {
							conversionContent.put(replaceContent, new HashMap<String, String>(content_key_value));
						}
					}
					if (!content_key_value.isEmpty()) {
						logger.debug(entry.getRouteId() + ":" + content_key_value);
						entry.setReplaceContent(true);
						entry.setReplaceContentMap(content_key_value);
					} else {
						entry.setReplaceContent(false);
					}
				} else {
					entry.setReplaceContent(false);
				}
			}
			String sourceAppender = entry.getSourceAppender();
			if (sourceAppender != null && sourceAppender.length() > 0) {
				if (sourceAppender.contains("^") || sourceAppender.contains("$")) {
					// correct format
				} else {
					entry.setSourceAppender(null);
				}
			} else {
				entry.setSourceAppender(null);
			}
			String msgAppender = entry.getMsgAppender();
			if (msgAppender != null && msgAppender.length() > 0) {
				try {
					String msgAppenderConverted = uniHexToCharMsg(msgAppender);
					if (msgAppenderConverted.contains("^") || msgAppenderConverted.contains("$")) {
						entry.setMsgAppender(msgAppenderConverted);
					} else {
						entry.setMsgAppender(null);
					}
				} catch (Exception e) {
					entry.setMsgAppender(null);
					logger.error(entry.getRouteId() + ": [" + msgAppender + "]", e.fillInStackTrace());
				}
			} else {
				entry.setMsgAppender(null);
			}
			// ---- end validation -----------------------------
			map.put(entry.getRouteId(), entry);
		}
		logger.info("<--- End listing Optional routing[" + map.size() + "] --> ");
		return map;
	}

	@Override
	public Map<Integer, HlrRouteEntry> listHlr(Integer[] route_id) {
		logger.info("<--- listing Hlr routing --> ");
		Map<Integer, HlrRouteEntry> map = new HashMap<Integer, HlrRouteEntry>();
		List<HlrRouteEntry> list = routeDAO.listHlr(route_id);
		for (HlrRouteEntry entry : list) {
			map.put(entry.getRouteId(), entry);
		}
		logger.info("<--- End listing Hlr routing[" + map.size() + "] --> ");
		return map;
	}

	@Override
	public Map<Integer, Map<Integer, RouteEntry>> listBasic(Integer[] user_id) {
		logger.info("<--- listing Basic routing.Total[" + user_id.length + "] --> ");
		Map<Integer, Map<Integer, RouteEntry>> map = new HashMap<Integer, Map<Integer, RouteEntry>>();
		List<RouteEntry> list = routeDAO.listBasic(user_id);
		for (RouteEntry entry : list) {
			Map<Integer, RouteEntry> inner = null;
			if (map.containsKey(entry.getUserId())) {
				inner = map.get(entry.getUserId());
			} else {
				inner = new HashMap<Integer, RouteEntry>();
			}
			inner.put(entry.getId(), entry);
			map.put(entry.getUserId(), inner);
		}
		logger.info("<--- End listing Basic routing.Total[" + user_id.length + "] --> ");
		return map;
	}

	@Override
	public Map<Integer, MmsRouteEntry> listMms() {
		Map<Integer, MmsRouteEntry> map = new HashMap<Integer, MmsRouteEntry>();
		List<MmsRouteEntry> list = routeDAO.listMms();
		for (MmsRouteEntry entry : list) {
			map.put(entry.getRouteId(), entry);
		}
		return map;
	}

	@Override
	public Map<Integer, MmsRouteEntry> listMms(Integer[] route_id) {
		logger.info("<--- listing Mms routing --> ");
		Map<Integer, MmsRouteEntry> map = new HashMap<Integer, MmsRouteEntry>();
		List<MmsRouteEntry> list = routeDAO.listMms(route_id);
		for (MmsRouteEntry entry : list) {
			map.put(entry.getRouteId(), entry);
		}
		logger.info("<--- End listing Mms routing[" + map.size() + "] --> ");
		return map;
	}
}
