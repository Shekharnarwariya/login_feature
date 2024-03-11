package com.hti.dlt;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.util.Converter;
import com.hti.util.GlobalCache;

public class DltFilter {
	private static Logger logger = LoggerFactory.getLogger("ProcLogger");
	private static Set<String> DLT_SENDERS = Collections.synchronizedSet(new HashSet<String>());

	public static synchronized void loadRules() {
		// --------- checking for senderId mappings -------------
		Set<String> tempSenders = new HashSet<String>();
		Connection con = null;
		Statement statement = null;
		ResultSet rs = null;
		String sql = "select sender,pe_id from dlt_config";
		// String temp_content = null;
		try {
			con = GlobalCache.connection_pool_proc.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				String source = rs.getString("sender");
				String peId = rs.getString("pe_id");
				if (source != null) {
					if (source.trim().length() > 0) {
						source = source.toLowerCase();
					} else {
						source = null;
					}
				}
				if (peId == null || peId.length() == 0) {
					logger.error(source + " <-- Invalid Dlt PE_ID Configured --> ");
					continue;
				}
				if (source != null) {
					tempSenders.add(source);
				}
			}
			DLT_SENDERS.clear();
			DLT_SENDERS.addAll(tempSenders);
			System.out.println("*******************************************");
			logger.info("Dlt Active Senders:" + DLT_SENDERS.size());
			System.out.println("*******************************************");
		} catch (Exception e) {
			logger.error("loadProfiles()", e.fillInStackTrace());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			GlobalCache.connection_pool_proc.putConnection(con);
		}
	}

	public static boolean findSender(String sender) {
		try {
			if (DLT_SENDERS.contains(sender.toLowerCase())) {
				return true;
			}
		} catch (Exception e) {
			logger.error(sender, e.getMessage());
		}
		return false;
	}
}
