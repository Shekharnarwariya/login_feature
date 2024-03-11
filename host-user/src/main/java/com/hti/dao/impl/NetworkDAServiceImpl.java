package com.hti.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.dao.NetworkDAService;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;

public class NetworkDAServiceImpl implements NetworkDAService {
	private static Logger logger = LoggerFactory.getLogger("ProcLogger");

	public NetworkDAServiceImpl() {
		GlobalCache.NetworkEntries = GlobalVars.hazelInstance.getMap("network_entries");
		logger.info("Network Entries: " + GlobalCache.NetworkEntries.size());
	}

	@Override
	public void loadNetworkConfig() {
		logger.info("Start Loading Network Configuration");
		Map<String, Integer> prefix_mapping = new HashMap<String, Integer>();
		Map<String, Integer> nnc_mapping = new HashMap<String, Integer>();
		Map<Integer, Set<String>> id_prefix_mapping = new HashMap<Integer, Set<String>>();
		for (NetworkEntry entry : GlobalCache.NetworkEntries.values()) {
			// ------- checking for prefix_mapping ---------------
			String prefixlist = entry.getPrefix();
			String cc = String.valueOf(entry.getCc());
			if ((prefixlist == null)) {
				prefixlist = cc;
			} else {
				if (prefixlist.trim().equalsIgnoreCase("0")) {
					prefixlist = cc;
				} else {
					prefixlist = cc + prefixlist;
				}
			}
			if (prefixlist.indexOf(",") > 0) {
				prefixlist = prefixlist.replaceAll(",", "," + cc);
			}
			String[] tokens = prefixlist.split(",");
			Set<String> prefix_set = new java.util.HashSet<String>();
			for (String token : tokens) {
				try {
					token = token.replaceAll("\\s+", "");
					Long.parseLong(token);
					prefix_mapping.put(token, entry.getId());
					prefix_set.add(token);
					logger.debug("Prefix Added[" + entry.getId() + "]: " + token);
				} catch (Exception ex) {
					logger.info("Invalid Prefix Found[" + entry.getId() + "]: " + token);
				}
			}
			if (!prefix_set.isEmpty() && entry.getId() > 0) {
				id_prefix_mapping.put(entry.getId(), prefix_set);
			}
			// ------- checking for nnc_mapping ---------------
			if (entry.getMcc() != null && entry.getMnc() != null) {
				int mcc = 0, mnc = 0;
				if (entry.getMcc().length() > 0 && entry.getMnc().length() > 0) {
					try {
						mcc = Integer.parseInt(entry.getMcc());
						if (entry.getMnc().contains(",")) {
							tokens = entry.getMnc().split(",");
							for (String token : tokens) {
								mnc = Integer.parseInt(token);
								nnc_mapping.put(mcc + "" + mnc, entry.getId());
							}
						} else {
							mnc = Integer.parseInt(entry.getMnc());
							nnc_mapping.put(mcc + "" + mnc, entry.getId());
						}
					} catch (Exception e) {
						logger.error(
								e + " While Parsing " + entry.getId() + ":->" + entry.getMcc() + " " + entry.getMnc());
					}
				}
			}
		}
		GlobalCache.PrefixMapping.clear();
		GlobalCache.PrefixMapping.putAll(prefix_mapping);
		logger.info("PrefixMapping: " + GlobalCache.PrefixMapping.size());
		GlobalCache.NncMapping.clear();
		GlobalCache.NncMapping.putAll(nnc_mapping);
		logger.info("NncMapping: " + GlobalCache.NncMapping.size());
		GlobalCache.IdPrefixMapping.clear();
		GlobalCache.IdPrefixMapping.putAll(id_prefix_mapping);
		logger.info("IdPrefixMapping: " + GlobalCache.IdPrefixMapping.size());
		logger.info("End Loading Network Configuration");
	}

	@Override
	public void initializeNetworkBsfm() {
		GlobalCache.NetworkBsfm.clear();
		Statement statement = null;
		ResultSet rs = null;
		String sql = "select * from bsfm_network";
		Connection con = null;
		try {
			con = GlobalCache.connection_pool_user.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				String source = rs.getString("source");
				if (source != null && source.length() > 0) {
					Map<String, Boolean> source_set = new HashMap<String, Boolean>();
					for (String source_token : source.split(",")) {
						if (source_token != null && source_token.length() > 0) {
							source_set.put(source_token.toLowerCase(), rs.getBoolean("reverse"));
						}
					}
					GlobalCache.NetworkBsfm.put(rs.getInt("network_id"), source_set);
				}
			}
		} catch (Exception ex) {
			logger.error("initializeNetworkBsfm()", ex.fillInStackTrace());
		} finally {
			GlobalCache.connection_pool_user.putConnection(con);
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
				}
			}
		}
		logger.info("Network Based Bsfm: " + GlobalCache.NetworkBsfm);
	}
}
